/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.arena.task;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.reporting.ChangeReport;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.arena.conceptview.FixedWidthJEditorPane;
import org.ihtsdo.util.swing.GuiUtil;

/**
 * Creates difference report based on two different dates
 * 
 * @author akf
 * 
 */
@BeanList(specs = {
    @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN)})
public class CreateReport extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String begDateProp = ProcessAttachmentKeys.START_DATE.getAttachmentKey();
    private String endDateProp = ProcessAttachmentKeys.END_DATE.getAttachmentKey();
    private transient Condition returnCondition;
    protected transient boolean done;
    private I_ConfigAceFrame config;
    //for report constructor
    protected String path1_uuid;
    protected String path2_uuid;
    protected String v1;
    protected String v2;
    protected boolean added_concepts = false;
    protected boolean deleted_concepts = false;
    protected boolean added_concepts_refex = false;
    protected boolean deleted_concepts_refex = false;
    protected boolean changed_concept_status = false;
    protected boolean changed_concept_author = false;
    protected boolean changed_description_author = false;
    protected boolean changed_rel_author = false;
    protected boolean changed_refex_author = false;
    protected boolean changed_defined = false;
    protected boolean added_descriptions = false;
    protected boolean deleted_descriptions = false;
    protected boolean changed_description_status = false;
    protected boolean changed_description_term = false;
    protected boolean changed_description_type = false;
    protected boolean changed_description_language = false;
    protected boolean changed_description_case = false;
    protected boolean added_relationships = false;
    protected boolean deleted_relationships = false;
    protected boolean changed_relationship_status = false;
    protected boolean changed_relationship_characteristic = false;
    protected boolean changed_relationship_refinability = false;
    protected boolean changed_relationship_type = false;
    protected boolean changed_relationship_group = false;
    protected String author1;
    protected String author2;
    protected int parentConceptNid;
    protected TerminologyList taxonomyList;
    protected TerminologyList v1PathList;
    protected TerminologyList v2PathList;
    protected TerminologyList v1AuthList;
    protected TerminologyList v2AuthList;
    protected FixedWidthJEditorPane dateV1;
    protected FixedWidthJEditorPane dateV2;
    protected JPanel panel;
    protected JPanel workflowPanel;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            DoSwing swinger = new DoSwing(process);
            swinger.execute();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }

            if (SwingUtilities.isEventDispatchThread()) {
                doRun(process, worker);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        doRun(process, worker);
                    }
                });
            }
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        }
        return returnCondition;
    }

    public void doRun(final I_EncodeBusinessProcess process, I_Work worker) {

        if (returnCondition == Condition.CONTINUE) {
            try {
                //get taxonomy parent
                TerminologyListModel modelParent = (TerminologyListModel) taxonomyList.getModel();
                I_GetConceptData parent = modelParent.getElementAt(0);
                parentConceptNid = parent.getConceptNid();

                //get v1 path
                TerminologyListModel modelPathV1 = (TerminologyListModel) v1PathList.getModel();
                I_GetConceptData pathV1 = modelPathV1.getElementAt(0);
                path1_uuid = pathV1.getPrimUuid().toString();

                //get v1 path
                TerminologyListModel modelPathV2 = (TerminologyListModel) v2PathList.getModel();
                I_GetConceptData pathV2 = modelPathV2.getElementAt(0);
                path2_uuid = pathV2.getPrimUuid().toString();

                //get v1 path
                TerminologyListModel modelAuthV1 = (TerminologyListModel) v1AuthList.getModel();
                I_GetConceptData authV1 = modelAuthV1.getElementAt(0);
                if (authV1 != null) {
                    author1 = authV1.getPrimUuid().toString();
                }

                //get v1 path
                TerminologyListModel modelAuthV2 = (TerminologyListModel) v2AuthList.getModel();
                I_GetConceptData authV2 = modelAuthV2.getElementAt(0);
                if (authV2 != null) {
                    author2 = authV2.getPrimUuid().toString();
                }

                ChangeReport reporter = new ChangeReport(v1, v2, path1_uuid, path2_uuid,
                        added_concepts, deleted_concepts, added_concepts_refex, deleted_concepts_refex,
                        changed_concept_status, changed_concept_author, changed_description_author,
                        changed_rel_author, changed_refex_author, author1, author2, changed_defined,
                        added_descriptions, deleted_descriptions, changed_description_status,
                        changed_description_term, changed_description_type, changed_description_language,
                        changed_description_case, added_relationships, deleted_relationships,
                        changed_relationship_status, changed_relationship_characteristic,
                        changed_relationship_refinability, changed_relationship_type,
                        changed_relationship_group, config, parentConceptNid);
                reporter.execute();
            } catch (TaskFailedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    private class DoSwing extends SwingWorker<Boolean, Boolean> {

        I_EncodeBusinessProcess process;

        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
        }

        @Override
        protected void done() {
            //make instructions
            panel = config.getWorkflowPanel();
            Component[] comp = panel.getComponents();
            for (int i = 0; i < comp.length; i++) {
                panel.remove(comp[i]);
            }
            panel.setVisible(true);
            panel.setLayout(new GridBagLayout());
            GridBagConstraints cPanel = new GridBagConstraints();
            cPanel.fill = GridBagConstraints.BOTH;

            cPanel.gridx = 0;
            cPanel.gridy = 0;
            cPanel.weightx = 1.0;
            cPanel.weighty = 0;
            cPanel.gridwidth = 2;
            cPanel.anchor = GridBagConstraints.EAST;
            panel.add(new JLabel("Please select parameters for report"), cPanel);
            // Add the processing buttons
            cPanel.weightx = 0.0;
            cPanel.gridx = 2;
            cPanel.gridwidth = 1;
            JButton stepButton =
                    new JButton(new ImageIcon(InstructAndWait.class.getResource("/16x16/plain/media_step_forward.png")));
            stepButton.setToolTipText("Next step");
            panel.add(stepButton, cPanel);

            cPanel.gridx++;
            stepButton.addActionListener(new StepActionListener());
            JButton stopButton =
                    new JButton(new ImageIcon(InstructAndWait.class.getResource("/16x16/plain/navigate_cross.png")));
            stopButton.setToolTipText("Cancel");
            panel.add(stopButton, cPanel);
            stopButton.addActionListener(new StopActionListener());
            cPanel.gridx++;
            panel.add(new JLabel("     "), cPanel);
            panel.validate();
            Container cont = panel;
            while (cont != null) {
                cont.validate();
                cont = cont.getParent();
            }
            stepButton.requestFocusInWindow();

            //make details sheet
            workflowPanel = config.getWorkflowDetailsSheet();
            Component[] components = workflowPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                workflowPanel.remove(components[i]);
            }
            workflowPanel.setVisible(true);
            workflowPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            c.gridx = 0;
            c.gridy++;
            c.weightx = 0.0;
            c.gridwidth = 10;
            workflowPanel.add(new JSeparator(), c);
            c.gridy++;
            workflowPanel.add(new JLabel("Parent for Taxonomy"), c);
            c.gridy++;
            c.ipady = 20;
            c.gridwidth = 2;
            taxonomyList = new TerminologyList(config);
            workflowPanel.add(taxonomyList, c);

            c.gridy++;
            c.weightx = 0.0;
            c.ipady = 0;
            c.gridwidth = 0;
            workflowPanel.add(new JLabel("path v1"), c);
            c.gridy++;
            c.ipady = 20;
            c.gridwidth = 2;
            v1PathList = new TerminologyList(config);
            workflowPanel.add(v1PathList, c);

            c.gridy++;
            c.weightx = 0.0;
            c.ipady = 0;
            c.gridwidth = 0;
            workflowPanel.add(new JLabel("path v2"), c);
            c.gridy++;
            c.ipady = 20;
            c.gridwidth = 2;
            v2PathList = new TerminologyList(config);
            workflowPanel.add(v2PathList, c);

            c.gridy++;
            c.weightx = 0.0;
            c.ipady = 0;
            c.gridwidth = 0;
            workflowPanel.add(new JLabel("start date (v1):"), c);
            c.gridy++;
            c.weightx = 1.0;
            dateV1 = new FixedWidthJEditorPane();
            dateV1.getDocument().addDocumentListener(new DateV1DocumentListener());
            dateV1.setText("");
            dateV1.setFixedWidth(300);
            dateV1.setEditable(true);
            workflowPanel.add(dateV1, c);

            c.gridy++;
            c.weightx = 0.0;
            workflowPanel.add(new JLabel("end date (v2):"), c);
            c.gridy++;
            c.weightx = 1.0;
            dateV2 = new FixedWidthJEditorPane();
            dateV2.getDocument().addDocumentListener(new DateV2DocumentListener());
            dateV2.setText("");
            dateV2.setFixedWidth(300);
            dateV2.setEditable(true);
            workflowPanel.add(dateV2, c);

            c.gridy++;
            c.weightx = 0.0;
            workflowPanel.add(new JLabel("Author v1"), c);
            c.gridy++;
            c.ipady = 20;
            c.gridwidth = 2;
            v1AuthList = new TerminologyList(config);
            workflowPanel.add(v1AuthList, c);

            c.gridy++;
            c.weightx = 0.0;
            c.gridwidth = 0;
            c.ipady = 0;
            workflowPanel.add(new JLabel("Author v2"), c);
            c.gridy++;
            c.ipady = 20;
            c.gridwidth = 2;
            v2AuthList = new TerminologyList(config);
            workflowPanel.add(v2AuthList, c);

            c.gridy++;
            c.ipady = 0;
            c.gridwidth = 1;
            c.weightx = 0.0;
            JCheckBox addedConcepts = new JCheckBox("Added Concepts");
            addedConcepts.addItemListener(new AddedConceptsItemListener());
            workflowPanel.add(addedConcepts, c);

            c.gridx = 1;
            c.gridwidth = 1;
            c.weightx = 0.0;
            JCheckBox deletedConcepts = new JCheckBox("Deleted Concepts");
            deletedConcepts.addItemListener(new DeletedConceptsItemListener());
            workflowPanel.add(deletedConcepts, c);

            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 1;
            c.weightx = 0.0;
            JCheckBox addedConceptsRefex = new JCheckBox("Added Concepts to Refex");
            addedConceptsRefex.addItemListener(new AddedConceptsRefexItemListener());
            workflowPanel.add(addedConceptsRefex, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox deletedConceptsRefex = new JCheckBox("Deleted Concepts from Refex");
            deletedConceptsRefex.addItemListener(new DeletedConceptsRefexItemListener());
            workflowPanel.add(deletedConceptsRefex, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 1.0;
            c.gridwidth = 1;
            c.weightx = 0.0;
            JCheckBox changedStatus = new JCheckBox("Changed Concept Status");
            changedStatus.addItemListener(new ChangedConceptStatusItemListener());
            workflowPanel.add(changedStatus, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            c.weightx = 0.0;
            JCheckBox changedConceptAuth = new JCheckBox("<html>Changed Concept <br>(filtered by Author)");
            changedConceptAuth.addItemListener(new ChangedConceptAuthorItemListener());
            workflowPanel.add(changedConceptAuth, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedDescAuth = new JCheckBox("<html>Changed Description <br>(filtered by Author)");
            changedDescAuth.addItemListener(new ChangedDescAuthorItemListener());
            workflowPanel.add(changedDescAuth, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedRelAuth = new JCheckBox("<html>Changed Relationship <br>(filtered by Author)");
            changedRelAuth.addItemListener(new ChangedRelAuthorItemListener());
            workflowPanel.add(changedRelAuth, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedRefexAuth = new JCheckBox("<html>Changed Refex <br>(filtered by Author)");
            changedRefexAuth.addItemListener(new ChangedRefexAuthorItemListener());
            workflowPanel.add(changedRefexAuth, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedDefined = new JCheckBox("Changed Defined");
            changedDefined.addItemListener(new ChangedDefinedItemListener());
            workflowPanel.add(changedDefined, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox addedDesc = new JCheckBox("Added Descriptions");
            addedDesc.addItemListener(new AddedDescriptionsItemListener());
            workflowPanel.add(addedDesc, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            c.weightx = 0.0;
            JCheckBox deletedDesc = new JCheckBox("Deleted Descriptions");
            deletedDesc.addItemListener(new DeletedDescriptionsItemListener());
            workflowPanel.add(deletedDesc, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedDescStatus = new JCheckBox("Changed Description Status");
            changedDescStatus.addItemListener(new ChangedDescStatusItemListener());
            workflowPanel.add(changedDescStatus, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedDescTerm = new JCheckBox("Changed Description Term");
            changedDescTerm.addItemListener(new ChangedDescTermItemListener());
            workflowPanel.add(changedDescTerm, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedDescType = new JCheckBox("Changed Description Type");
            changedDescType.addItemListener(new ChangedDescTypeItemListener());
            workflowPanel.add(changedDescType, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedDescLang = new JCheckBox("Changed Description Language");
            changedDescLang.addItemListener(new ChangedDescLangItemListener());
            workflowPanel.add(changedDescLang, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 1.0;
            c.gridwidth = 1;
            c.weightx = 0.0;
            JCheckBox changedDescCase = new JCheckBox("Changed Description Case");
            changedDescCase.addItemListener(new ChangedDescCaseItemListener());
            workflowPanel.add(changedDescCase, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            c.weightx = 0.0;
            JCheckBox addedRels = new JCheckBox("Added Relationships");
            addedRels.addItemListener(new AddedRelsItemListener());
            workflowPanel.add(addedRels, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox deletedRels = new JCheckBox("Deleted Relationships");
            deletedRels.addItemListener(new DeletedRelsItemListener());
            workflowPanel.add(deletedRels, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedRelStatus = new JCheckBox("Changed Relationship Status");
            changedRelStatus.addItemListener(new ChangedRelStatusItemListener());
            workflowPanel.add(changedRelStatus, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedRelChar = new JCheckBox("<html>Changed Relationship <br>Characteristic");
            changedRelChar.addItemListener(new ChangedRelCharItemListener());
            workflowPanel.add(changedRelChar, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            c.weightx = 0.0;
            JCheckBox changedRelRefine = new JCheckBox("Changed Relationship Refinability");
            changedRelRefine.addItemListener(new ChangedRelRefineItemListener());
            workflowPanel.add(changedRelRefine, c);

            c.gridy++;
            c.gridx = 0;
            c.weightx = 1.0;
            c.gridwidth = 1;
            c.weightx = 0.0;
            JCheckBox changedRelType = new JCheckBox("Changed Relationship Type");
            changedRelType.addItemListener(new ChangedRelTypeItemListener());
            workflowPanel.add(changedRelType, c);

            c.gridx = 1;
            c.weightx = 1.0;
            c.gridwidth = 1;
            JCheckBox changedRelGroup = new JCheckBox("Changed Relationship Group");
            changedRelGroup.addItemListener(new ChangedRelGroupItemListener());
            workflowPanel.add(changedRelGroup, c);

            //empty thing
            c.gridx = 0;
            c.gridy++;
            c.weightx = 0;
            c.weighty = 1;
            workflowPanel.add(new JPanel(), c);
            GuiUtil.tickle(workflowPanel);
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            //nothing to do
            return true;
        }
    }

    private class DateV1DocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent de) {
            CreateReport.this.v1 = dateV1.extractText();
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            CreateReport.this.v1 = dateV1.extractText();
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            CreateReport.this.v1 = dateV1.extractText();
        }
    }

    private class DateV2DocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent de) {
            CreateReport.this.v2 = dateV2.extractText();
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            CreateReport.this.v2 = dateV2.extractText();
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            CreateReport.this.v2 = dateV2.extractText();
        }
    }

    private class AddedConceptsItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                added_concepts = true;
            } else {
                added_concepts = false;
            }
        }
    }

    private class DeletedConceptsItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                deleted_concepts = true;
            } else {
                deleted_concepts = false;
            }
        }
    }

    private class AddedConceptsRefexItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                added_concepts_refex = true;
            } else {
                added_concepts_refex = false;
            }
        }
    }

    private class DeletedConceptsRefexItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                deleted_concepts_refex = true;
            } else {
                deleted_concepts_refex = false;
            }
        }
    }

    private class ChangedConceptStatusItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_concept_status = true;
            } else {
                changed_concept_status = false;
            }
        }
    }

    private class ChangedConceptAuthorItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_concept_author = true;
            } else {
                changed_concept_author = false;
            }
        }
    }

    private class ChangedDescAuthorItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_description_author = true;
            } else {
                changed_description_author = false;
            }
        }
    }

    private class ChangedRelAuthorItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_rel_author = true;
            } else {
                changed_rel_author = false;
            }
        }
    }

    private class ChangedRefexAuthorItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_refex_author = true;
            } else {
                changed_refex_author = false;
            }
        }
    }

    private class ChangedDefinedItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_defined = true;
            } else {
                changed_defined = false;
            }
        }
    }

    private class AddedDescriptionsItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                added_descriptions = true;
            } else {
                added_descriptions = false;
            }
        }
    }

    private class DeletedDescriptionsItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                deleted_descriptions = true;
            } else {
                deleted_descriptions = false;
            }
        }
    }

    private class ChangedDescStatusItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_description_status = true;
            } else {
                changed_description_status = false;
            }
        }
    }

    private class ChangedDescTermItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_description_term = true;
            } else {
                changed_description_term = false;
            }
        }
    }

    private class ChangedDescTypeItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_description_type = true;
            } else {
                changed_description_type = false;
            }
        }
    }

    private class ChangedDescLangItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_description_language = true;
            } else {
                changed_description_language = false;
            }
        }
    }

    private class ChangedDescCaseItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_description_case = true;
            } else {
                changed_description_case = false;
            }
        }
    }

    private class AddedRelsItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                added_relationships = true;
            } else {
                added_relationships = false;
            }
        }
    }

    private class DeletedRelsItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                deleted_relationships = true;
            } else {
                deleted_relationships = false;
            }
        }
    }

    private class ChangedRelStatusItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_relationship_status = true;
            } else {
                changed_relationship_status = false;
            }
        }
    }

    private class ChangedRelCharItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_relationship_characteristic = true;
            } else {
                changed_relationship_characteristic = false;
            }
        }
    }

    private class ChangedRelRefineItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_relationship_refinability = true;
            } else {
                changed_relationship_refinability = false;
            }
        }
    }

    private class ChangedRelTypeItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_relationship_type = true;
            } else {
                changed_relationship_type = false;
            }
        }
    }

    private class ChangedRelGroupItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                changed_relationship_group = true;
            } else {
                changed_relationship_group = false;
            }
        }
    }

    private class StepActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            panel.setVisible(false);
            workflowPanel.setVisible(false);
            returnCondition = Condition.CONTINUE;
            done = true;
            synchronized (CreateReport.this) {
                CreateReport.this.notifyAll();
            }
        }
    }

    private class StopActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            panel.setVisible(false);
            workflowPanel.setVisible(false);
            returnCondition = Condition.ITEM_CANCELED;
            done = true;
            synchronized (CreateReport.this) {
                CreateReport.this.notifyAll();
            }

        }
    }

    protected void waitTillDone(Logger l) {
        while (!this.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                l.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    protected void notifyTaskDone() {
        synchronized (CreateReport.this) {
            CreateReport.this.notifyAll();
        }
    }

    public boolean isDone() {
        return this.done;
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CANCEL;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }
}
