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
package org.dwfa.ace.task;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.db.HasUncommittedChanges;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

/*
 * This class was constructed for the specific purpose of taking actions on a
 * match
 * review item, though it could likely be adapted for similar tasks.
 * <br>
 * It places a control in the toolbar which facilitates editing based on the
 * selection in the display
 * <br>
 * The actions are:
 * <br>
 * Create a new concept
 * <br>
 * Clone a existing concept
 * <br>
 * Add a synonym to an existing concept
 * <br>
 * Exit and mark as completed
 * <br> Exit but leave as a to do item
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/instruct", type = BeanType.TASK_BEAN) })
public class InstructAndWaitDo extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 2;

    private String instruction = "<html>Instruction";

    private String term = "";

    private String termPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();

    private transient Condition returnCondition;

    private transient boolean done;
    
    private transient I_EncodeBusinessProcess process;

    // EKM - seems a but easier than passing it around
    private I_ConfigAceFrame config;

    private final String language = "en";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(instruction);
        out.writeObject(termPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            instruction = (String) in.readObject();
            if (objDataVersion >= 2) {
                termPropName = (String) in.readObject();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    protected void printDescriptionPart(I_DescriptionPart dp) {
        //
        // public int hashCode() {
        // int bhash = 0;
        // if (initialCaseSignificant) {
        // bhash = 1;
        // }
        // return HashFunction.hashCode(new int[] {
        // bhash, lang.hashCode(), pathId,
        // statusId, text.hashCode(),
        // typeId, version
        // });
        // }
        //
        System.out.println(">>>descr init case: " + dp.isInitialCaseSignificant());
        System.out.println(">>>descr lang: " + dp.getLang());
        System.out.println(">>>descr path: " + dp.getPathId());
        System.out.println(">>>descr status: " + dp.getStatusId());
        System.out.println(">>>descr text: " + dp.getText());
        System.out.println(">>>descr type: " + dp.getTypeId());
        System.out.println(">>>descr version: " + dp.getVersion());
    }

    /*
     * Add a description to an existing concept
     */
    private class DescrActionListener implements ActionListener {
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                // Get the selected concept
                I_GetConceptData con = getSelectedConcept();
                if (con == null)
                    return;
                // Prompt for the description using the input term as the
                // initial text
                String newDescrString =
                        (String) JOptionPane.showInputDialog(null, "Add description to:\n" + con.getInitialText(),
                            "Enter description", JOptionPane.QUESTION_MESSAGE, null, null, InstructAndWaitDo.this.term);
                if (newDescrString == null)
                    return;
                // Here's the drill for adding a description
                I_TermFactory termFactory = Terms.get();
                I_GetConceptData synonym_description_type =
                        termFactory.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
                // See SearchReplaceTermsInList
                Set<PathBI> paths = config.getEditingPathSet();
                I_DescriptionVersioned newDescr =
                        termFactory.newDescription(UUID.randomUUID(), con, language, newDescrString,
                            synonym_description_type, config);
                I_DescriptionPart newLastPart = newDescr.getLastTuple().getMutablePart();
                for (PathBI path : paths) {
                    if (newLastPart == null) {

                        newLastPart =
                                (I_DescriptionPart) newDescr.getLastTuple().getMutablePart().makeAnalog(
                                    newLastPart.getStatusId(), path.getConceptNid(), Long.MAX_VALUE);
                    }
                    // printDescriptionPart(newLastPart);
                    // System.out.println(">>> language: " + language);

                    termFactory.addUncommitted(con);
                    newLastPart = null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /*
     * Create a new concept
     * @param newDescrString - the FSN and preferred term for the new concept
     * @param semantic_tag - the semantic tag, may be null
     */
    protected I_GetConceptData createNewConcept(String newDescrString, String semantic_tag) throws Exception {
        System.out.println("ST: |" + semantic_tag + "|");
        if (semantic_tag == null)
            semantic_tag = "(?????)";
        I_TermFactory termFactory = Terms.get();
        I_GetConceptData fully_specified_description_type =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
        I_GetConceptData preferred_description_type =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
        I_GetConceptData newConcept = Terms.get().newConcept(UUID.randomUUID(), false, config);
        // Install the FSN
        termFactory.newDescription(UUID.randomUUID(), newConcept, language, newDescrString + " " + semantic_tag,
            fully_specified_description_type, config);
        // Install the preferred term
        termFactory.newDescription(UUID.randomUUID(), newConcept, language, newDescrString, preferred_description_type,
            config);
        return newConcept;
    }

    /*
     * Get the semantic tag from the concept's FSN <br> The semantic tag is the
     * substring starting at the last left paren
     * @param con - the concept
     * @return the semantic tag, null if none exists
     */
    private String getSemanticTag(I_GetConceptData con) throws Exception {
        I_TermFactory termFactory = Terms.get();
        I_GetConceptData current_status = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
        I_GetConceptData fully_specified_description_type =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
        Set<PositionBI> positionSet = new HashSet<PositionBI>();
        for (PathBI path : config.getEditingPathSet()) {
            positionSet.add(termFactory.newPosition(path, Integer.MAX_VALUE));
        }
        PositionSetReadOnly clonePositions = new PositionSetReadOnly(positionSet);
        for (I_DescriptionTuple desc : con.getDescriptionTuples(null, null, clonePositions, config.getPrecedence(),
            config.getConflictResolutionStrategy())) {
            // Description is current
            if (desc.getStatusId() != current_status.getConceptNid())
                continue;
            // Description is FSN
            if (desc.getTypeId() != fully_specified_description_type.getConceptNid())
                continue;
            // Get the substring starting at the last left paren
            String fsn = desc.getText();
            int lp = fsn.lastIndexOf("(");
            // Return null if not found
            if (lp == -1)
                return null;
            // Otherwise return the substring
            return fsn.substring(lp);
        }
        return null;
    }

    /*
     * Create a new concept as a clone of an existing concept
     */
    private class CloneActionListener implements ActionListener {
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                // Get the selected concept
                I_GetConceptData con = getSelectedConcept();
                if (con == null)
                    return;
                // Prompt for the name using the input term as initial text
                String newDescrString =
                        (String) JOptionPane
                            .showInputDialog(null, "Clone:\n" + con.getInitialText(), "Enter concept name",
                                JOptionPane.QUESTION_MESSAGE, null, null, InstructAndWaitDo.this.term);
                if (newDescrString == null)
                    return;
                // Create the new concept, using the semantic tag of the
                // selected concept
                I_GetConceptData newConcept = createNewConcept(newDescrString, getSemanticTag(con));
                // Now copy the rels from the exiting to the new
                I_TermFactory termFactory = Terms.get();
                Set<PositionBI> positionSet = new HashSet<PositionBI>();
                for (PathBI path : config.getEditingPathSet()) {
                    positionSet.add(termFactory.newPosition(path, Integer.MAX_VALUE));
                }
                PositionSetReadOnly clonePositions = new PositionSetReadOnly(positionSet);
                for (I_RelTuple rel : con.getSourceRelTuples(config.getAllowedStatus(), null, clonePositions, config
                    .getPrecedence(), config.getConflictResolutionStrategy())) {
                    termFactory.newRelationship(UUID.randomUUID(), newConcept, termFactory.getConcept(rel.getTypeId()),
                        termFactory.getConcept(rel.getC2Id()), termFactory.getConcept(rel.getCharacteristicId()),
                        termFactory.getConcept(rel.getRefinabilityId()), termFactory.getConcept(rel.getStatusId()), rel
                            .getGroup(), config);
                }
                I_HostConceptPlugins lcv = config.getListConceptViewer();
                lcv.setTermComponent(newConcept);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    /*
     * Create a new concept as a child
     */
    private class ChildActionListener implements ActionListener {
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                // Get the selected concept
                I_GetConceptData con = getSelectedConcept();
                if (con == null)
                    return;
                // Prompt for name using input term as initial text
                String newDescrString =
                        (String) JOptionPane
                            .showInputDialog(null, "Create a child of:\n" + con.getInitialText(), "Enter concept name",
                                JOptionPane.QUESTION_MESSAGE, null, null, InstructAndWaitDo.this.term);
                if (newDescrString == null)
                    return;
                // Create the new concept, using the semantic tag of the
                // selected concept
                I_GetConceptData newConcept = createNewConcept(newDescrString, getSemanticTag(con));
                // Now make the selected concept the parent of the new concept
                I_TermFactory termFactory = Terms.get();
                I_GetConceptData is_a_rel = termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
                I_GetConceptData defining_characteristic =
                        termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
                I_GetConceptData optional_refinability =
                        termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
                I_GetConceptData current_status =
                        termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
                termFactory.newRelationship(UUID.randomUUID(), newConcept, is_a_rel, con, defining_characteristic,
                    optional_refinability, current_status, 0, config);
                I_HostConceptPlugins lcv = config.getListConceptViewer();
                lcv.setTermComponent(newConcept);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    /*
     * Create a new concept, not using an existing
     */
    private class NewActionListener implements ActionListener {
        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                // Prompt for name using input term as initial text
                String newDescrString =
                        (String) JOptionPane.showInputDialog(null, "Create a new concept", "Enter concept name",
                            JOptionPane.QUESTION_MESSAGE, null, null, InstructAndWaitDo.this.term);
                if (newDescrString == null)
                    return;
                // Create the new concept, but since it's not based on an
                // existing concept the semantic tag is null
                I_GetConceptData newConcept = createNewConcept(newDescrString, null);
                I_HostConceptPlugins lcv = config.getListConceptViewer();
                lcv.setTermComponent(newConcept);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    /*
     * Set the state to done and the condition to complete
     */
    private class StepActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
        	if (Terms.get().getUncommitted().size() > 0) {
        		for (I_Transact c: Terms.get().getUncommitted()) {
        			AceLog.getAppLog().warning("Uncommitted changes to: " 
        					+ ((I_GetConceptData) c).toLongString());
        			
        		}
        		HasUncommittedChanges.askToCommit(process);
        	}
            if (Terms.get().getUncommitted().size() > 0) {
                if (!DwfaEnv.isHeadless()) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "There are uncommitted changes - please cancel or commit before continuing.", "",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                returnCondition = Condition.ITEM_COMPLETE;
                done = true;
                synchronized (InstructAndWaitDo.this) {
                    InstructAndWaitDo.this.notifyAll();
                }
            }
        }

    }

    /*
     * Set the state to done and the condition to canceled
     */
    private class StopActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.ITEM_CANCELED;
            done = true;
            synchronized (InstructAndWaitDo.this) {
                InstructAndWaitDo.this.notifyAll();
            }

        }

    }

    private void waitTillDone(Logger l) {
        while (!this.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                l.log(Level.SEVERE, e.getMessage(), e);
            }
        }

    }

    public boolean isDone() {
        return this.done;
    }

    /*
     * Get the selected elements from the control as concepts
     */
    protected List<I_GetConceptData> getSelectedConcepts() throws IOException {
        // System.out.println(">>>getSelectedConcept");
        JList conceptList = config.getBatchConceptList();
        int[] selected = conceptList.getSelectedIndices();
        I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
        // for (int i = 0; i < model.getSize(); i++) {
        // I_GetConceptData concept = model.getElementAt(i);
        // I_DescriptionVersioned d = concept.getDescriptions().get(0);
        // System.out.println(">>>" + d + "\nStatus:"
        // + d.getLastTuple().getStatusId());
        // }
        List<I_GetConceptData> ret = new ArrayList<I_GetConceptData>();
        for (int i : selected) {
            ret.add(model.getElementAt(i));
        }
        return ret;
    }

    /*
     * We only deal with one concept at a time. This alerts the user in case
     * they did multi
     */
    protected I_GetConceptData getSelectedConcept() throws IOException {
        List<I_GetConceptData> cons = getSelectedConcepts();
        if (cons.size() == 0) {
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Please select a concept");
            return null;
        }
        if (cons.size() != 1) {
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Please select only one concept");
            return null;
        }
        return cons.get(0);
    }

    /**
     * Build the control and set up the listeners
     * 
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            // The input term
            this.term = (String) process.getProperty(termPropName);
            this.done = false;
            this.process = process;
            this.config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            // Get some space
            boolean builderVisible = config.isBuilderToggleVisible();
            config.setBuilderToggleVisible(true);
            // EKM - changed to subversion toggle
            boolean subversionButtonVisible = config.isSubversionToggleVisible();
            config.setSubversionToggleVisible(false);
            boolean inboxButtonVisible = config.isInboxToggleVisible();
            config.setInboxToggleVisible(true);
            // When running from the queue we never see the task list
            final JPanel workflowPanel = config.getWorkflowPanel();
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    Component[] components = workflowPanel.getComponents();
                    for (int i = 0; i < components.length; i++) {
                        workflowPanel.remove(components[i]);
                    }
                    // Simple layout - label and buttons
                    workflowPanel.setLayout(new GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.gridx = 0;
                    c.gridy = 0;
                    c.weightx = 1.0;
                    c.weighty = 0;
                    c.anchor = GridBagConstraints.EAST;
                    workflowPanel.add(new JLabel("<html><b>Matches for:</b> " + term), c);
                    c.weightx = 0.0;
                    c.gridx++;
                    workflowPanel.add(new JLabel("  "), c);
                    c.gridx++;
                    c.anchor = GridBagConstraints.SOUTHWEST;

                    // Add description
                    JButton descrButton = new JButton("Descr");
                    descrButton.setToolTipText("Add description to selected concept");
                    workflowPanel.add(descrButton, c);
                    c.gridx++;
                    descrButton.addActionListener(new DescrActionListener());

                    // Clone concept
                    JButton cloneButton = new JButton("Clone");
                    cloneButton.setToolTipText("Clone selected concept");
                    workflowPanel.add(cloneButton, c);
                    c.gridx++;
                    cloneButton.addActionListener(new CloneActionListener());

                    // Make child of concept
                    JButton childButton = new JButton("Child");
                    childButton.setToolTipText("Create a child of selected concept");
                    workflowPanel.add(childButton, c);
                    c.gridx++;
                    childButton.addActionListener(new ChildActionListener());

                    // Completely new concept
                    JButton newButton = new JButton("New");
                    newButton.setToolTipText("Create a new concept");
                    workflowPanel.add(newButton, c);
                    c.gridx++;
                    newButton.addActionListener(new NewActionListener());

                    // Done - bp will move to queue
                    JButton stepButton = new JButton("Done");
                    workflowPanel.add(stepButton, c);
                    c.gridx++;
                    stepButton.addActionListener(new StepActionListener());

                    // Todo - bp will move to queue
                    JButton stopButton = new JButton("Todo");
                    workflowPanel.add(stopButton, c);
                    stopButton.addActionListener(new StopActionListener());
                    c.gridx++;

                    workflowPanel.add(new JLabel("     "), c);
                    workflowPanel.validate();
                    Container cont = workflowPanel;
                    while (cont != null) {
                        cont.validate();
                        cont = cont.getParent();
                    }
                    workflowPanel.setVisible(true);
                    stepButton.requestFocusInWindow();
                }
            });
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    Component[] components = workflowPanel.getComponents();
                    for (int i = 0; i < components.length; i++) {
                        workflowPanel.remove(components[i]);
                    }
                    workflowPanel.validate();
                    Container cont = workflowPanel;
                    while (cont != null) {
                        cont.validate();
                        cont = cont.getParent();
                    }
                }

            });
            // Restore to initial state
            config.setBuilderToggleVisible(builderVisible);
            config.setSubversionToggleVisible(subversionButtonVisible);
            config.setInboxToggleVisible(inboxButtonVisible);
            return returnCondition;
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getTermPropName() {
        return termPropName;
    }

    public void setTermPropName(String termPropName) {
        this.termPropName = termPropName;
    }

    protected String getTrueImage() {
        return "/16x16/plain/media_step_forward.png";
    }

    protected String getFalseImage() {
        return "/16x16/plain/media_stop_red.png";
    }
}
