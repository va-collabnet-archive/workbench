/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.task;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.wfpanel.PreviousNextOrCancel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.arena.conceptview.ConceptView;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.arena.conceptview.FixedWidthJEditorPane;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.WizardBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.util.swing.GuiUtil;

import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.helper.cswords.CsWordsHelper;
import org.ihtsdo.helper.dialect.DialectHelper;
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.binding.snomed.Language;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ValidationException;

/**
 *
 * @author afurber
 */
@BeanList(specs = {
    @Spec(directory = "tasks/ide/instruct", type = BeanType.TASK_BEAN),
    @Spec(directory = "tasks/arena/wizard", type = BeanType.TASK_BEAN)})
public class NewConceptFromBatch extends PreviousNextOrCancel {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    // Task Attribute Properties
    private String submissionLineProp = ProcessAttachmentKeys.NAME1.getAttachmentKey();
    // Other Properties
    private I_ConfigAceFrame config;
    private I_HostConceptPlugins host;
    private transient WizardBI wizard;
    private TerminologyList tl;
    private FixedWidthJEditorPane fsn;
    private JLabel inputFsnLabel;
    private JLabel inputPrefLabel;
    private FixedWidthJEditorPane pref;
    private String usFsn;
    private String gbFsn;
    private JCheckBox gbBoxFsn;
    private JCheckBox usBoxFsn;
    private JLabel gbLabelFsn;
    private JLabel usLabelFsn;
    private String usPref;
    private String gbPref;
    private JCheckBox gbBoxPref;
    private JCheckBox usBoxPref;
    private JLabel gbLabelPref;
    private JLabel usLabelPref;
    private boolean addUsDescFsn = false;
    private boolean addGbDescFsn = false;
    private boolean addUsDescPref = false;
    private boolean addGbDescPref = false;
    private ConceptCB conceptSpec;
    private DescriptionCAB descSpecGbFsn;
    private DescriptionCAB descSpecUsFsn;
    private RefexCAB refexSpecGbFsn;
    private RefexCAB refexSpecUsFsn;
    private DescriptionCAB descSpecGbPref;
    private DescriptionCAB descSpecUsPref;
    private RefexCAB refexSpecGbPref;
    private RefexCAB refexSpecUsPref;
    private RefexCAB refexSpecUsAcct;
    private RefexCAB refexSpecGbAcct;
    private TerminologyBuilderBI tc;
    private ConceptChronicleBI newConcept;
    private String lang;
    private ConceptChronicleBI gbRefexConcept;
    private ConceptChronicleBI usRefexConcept;
    private UUID gbUuid;
    private UUID usUuid;
    private ConceptChronicleBI acceptableConcept;
    private ConceptChronicleBI preferredConcept;
    private ConceptChronicleBI fsnConcept;
    private ConceptChronicleBI synConcept;
    private String fsnText;
    private String fsnSctId;
    private String prefText;
    private String newConceptId;
    private String newConceptSctId;
    private UUID newConceptUuid;
    private UUID parentUuid;
    private String parentId;
    private String parentFsn;
    private FixedWidthJEditorPane newUuidPane;
    private FixedWidthJEditorPane parentUuidPane;
    private FixedWidthJEditorPane parentFsnPane;
    private ConceptSpec parent;
    private ComponentChronicleBI parentConcept;
    private boolean hasPanel = false;
    private boolean hasSctId = false;
    private boolean hasFsnSctId = false;

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(submissionLineProp);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            submissionLineProp = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    @Override
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

        try {
            // Present the user interface in the Workflow panel
            config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            Set<WeakReference> panelSet = ConceptViewSettings.arenaPanelMap.get(1);
            if (panelSet != null) {
                for (WeakReference r : panelSet) {
                    ConceptView cv = (ConceptView) r.get();
                    if (r != null) {
                        if (cv.getRootPane() != null) {
                            wizard = cv.getCvRenderer().getWizardPanel();
                            host = cv.getSettings().getHost();
                            hasPanel = true;
                            break;
                        }
                    }
                }
            }
            if (!hasPanel) {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "<html>There are no tab 1 panels in the arena.<br>"
                        + "Please add one and re-launch process", "",
                        JOptionPane.ERROR_MESSAGE);
                returnCondition = Condition.PREVIOUS;
                done = true;
                NewConceptFromBatch.this.notifyTaskDone();
            }
            if (hasPanel) {
                String fileName = (String) process.getProperty(
                        ProcessAttachmentKeys.NAME1.getAttachmentKey());
                String[] part = fileName.split("\\t");
                if (part.length == 4) {
                    newConceptId = part[0];
                    fsnText = part[1];
                    parentId = part[2];
                    parentFsn = part[3];
                } else if (part.length == 5) {
                    newConceptId = part[0];
                    fsnText = part[1];
                    parentId = part[2];
                    parentFsn = part[3];
                    newConceptSctId = part[4];
                    hasSctId = true;
                } else if (part.length == 6) {
                    newConceptId = part[0];
                    fsnText = part[1];
                    parentId = part[2];
                    parentFsn = part[3];
                    newConceptSctId = part[4];
                    fsnSctId = part[5];
                    hasSctId = true;
                    hasFsnSctId = true;
                }

                if (newConceptId.length() == 32) {
                    newConceptId = newConceptId.toLowerCase();
                    //split UUID into parts 8-4-4-4-12 and insert dashes 
                    String one = newConceptId.substring(0, 8);
                    String two = newConceptId.substring(8, 12);
                    String three = newConceptId.substring(12, 16);
                    String four = newConceptId.substring(16, 20);
                    String five = newConceptId.substring(20, 32);
                    newConceptId = one + "-" + two + "-" + three + "-" + four + "-" + five;
                }

                if (parentId.length() == 32) {
                    parentId = parentId.toLowerCase();
                    //split UUID into parts 8-4-4-4-12 and insert dashes 
                    String one = parentId.substring(0, 8);
                    String two = parentId.substring(8, 12);
                    String three = parentId.substring(12, 16);
                    String four = parentId.substring(16, 20);
                    String five = parentId.substring(20, 32);
                    parentId = one + "-" + two + "-" + three + "-" + four + "-" + five;
                }


                DoSwing swinger = new DoSwing(process);
                swinger.execute();
                new Thread(
                        new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    CsWordsHelper.lazyInit();
                                } catch (IOException ex) {
                                    AceLog.getAppLog().alertAndLogException(ex);
                                }
                            }
                        }).start();

                synchronized (this) {
                    this.waitTillDone(worker.getLogger());
                }
                MakeNewConcept maker = new MakeNewConcept();
                maker.execute();
                maker.getLatch().await();
            }
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        }

        return returnCondition;
    }

    private class MakeNewConcept extends SwingWorker<Object, Object> {

        CountDownLatch latch = new CountDownLatch(1);

        public CountDownLatch getLatch() {
            return latch;
        }

        @Override
        protected Object doInBackground(){
            // check return condition for CONTINUE or ITEM_CANCELLED
            if (returnCondition == Condition.CONTINUE) {
                try {
                    createBlueprintConcept();
                    //get rf1 or rf2 versions
                    gbRefexConcept = Ts.get().getConcept(SnomedMetadataRfx.getGB_DIALECT_REFEX_NID());
                    gbUuid = gbRefexConcept.getPrimUuid();
                    usRefexConcept = Ts.get().getConcept(SnomedMetadataRfx.getUS_DIALECT_REFEX_NID());
                    usUuid = usRefexConcept.getPrimUuid();
                    acceptableConcept = Ts.get().getConcept(SnomedMetadataRfx.getDESC_ACCEPTABLE_NID());
                    preferredConcept = Ts.get().getConcept(SnomedMetadataRfx.getDESC_PREFERRED_NID());
                    fsnConcept = Ts.get().getConcept(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());
                    synConcept = Ts.get().getConcept(SnomedMetadataRfx.getDES_SYNONYM_NID());

                    //create blueprints
                    if (lang.equals("en")) {
                        createBlueprintUsFsnRefex(conceptSpec.makeFullySpecifiedNameCAB().getComponentNid());
                        createBlueprintGbFsnRefex(conceptSpec.makeFullySpecifiedNameCAB().getComponentNid());
                        createBlueprintUsPrefRefex(conceptSpec.makePreferredCAB().getComponentNid());
                        createBlueprintGbPrefRefex(conceptSpec.makePreferredCAB().getComponentNid());
                    }
                    if (lang.equals("en-us")) {
                        createBlueprintUsFsnRefex(conceptSpec.makeFullySpecifiedNameCAB().getComponentNid());
                        createBlueprintUsPrefRefex(conceptSpec.makePreferredCAB().getComponentNid());
    //                   createBlueprintGbAcctRefex(conceptSpec.getPreferredCAB().getComponentNid()); //removed for rf2
                    }
                    if (lang.equals("en-gb")) {
    //                    createBlueprintGbFsnRefex(conceptSpec.getFsnCAB().getComponentNid());
                        createBlueprintGbFsnRefex(conceptSpec.makeFullySpecifiedNameCAB().getComponentNid()); //only using one fsn
                        createBlueprintGbPrefRefex(conceptSpec.makePreferredCAB().getComponentNid());
    //                   createBlueprintUsAcctRefex(conceptSpec.getPreferredCAB().getComponentNid()); //removed for rf2
                    }
                    if (addUsDescFsn) {
    //                    createBlueprintUsFsnDesc();
                        createBlueprintUsFsnRefex(conceptSpec.makeFullySpecifiedNameCAB().getComponentNid());
                    }
                    if (addGbDescFsn) {
    //                    createBlueprintGbFsnDesc();
    //                    createBlueprintGbFsnRefex(descSpecGbFsn.getComponentNid());
                        createBlueprintGbFsnRefex(conceptSpec.makeFullySpecifiedNameCAB().getComponentNid()); //only using one fsn (US)
                    }
                    if (addUsDescPref) {
                        createBlueprintUsPrefDesc();
                        createBlueprintUsPrefRefex(descSpecUsPref.getComponentNid());
    //                    createBlueprintGbAcctRefex(descSpecUsPref.getComponentNid()); //removed for rf2
                    }
                    if (addGbDescPref) {
                        createBlueprintGbPrefDesc();
                        createBlueprintGbPrefRefex(descSpecGbPref.getComponentNid());
    //                    createBlueprintUsAcctRefex(descSpecGbPref.getComponentNid()); //removed for rf2
                    }
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                    return null;
                } catch (InvalidCAB ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                    return null;
                } catch (ContradictionException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                    return null;
                }

            }
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                I_AmTermComponent newTerm = (I_AmTermComponent) newConcept;
                wizard.setWizardPanelVisible(false);
                host.setTermComponent(newTerm);
                Ts.get().addUncommitted(newConcept);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
                returnCondition = Condition.ITEM_CANCELED;
            } finally {
                latch.countDown();
            }
        }
    }

    /**
     * This method overrides a method by the same name in the parent class. It
     * is used
     * to tell the parent class (PreviousNextOrCancel) whether to show
     * the previous button or not. Since we only want the Next and Cancel
     * buttons,
     * this method returns false .
     */
    @Override
    protected boolean showPrevious() {
        return false;
    }

    private class DoSwing extends SwingWorker<Boolean, Boolean> {

        I_EncodeBusinessProcess process;

        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
            wizard.setWizardPanelVisible(true);
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            setup(process);
            return true;
        }

        @Override
        protected void done() {
            try {
                get();
                JPanel wizardPanel = wizard.getWizardPanel();

                Component[] components = wizardPanel.getComponents();
                for (int i = 0; i < components.length; i++) {
                    wizardPanel.remove(components[i]);
                }

                wizardPanel.setLayout(new GridBagLayout());
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;

                // Add the Instructions
                c.gridx = 0;
                c.gridy = 0;
                c.weightx = 1.0;
                c.weighty = 0;
                c.gridwidth = 2;
                c.anchor = GridBagConstraints.EAST;
                wizardPanel.add(new JLabel("<html>Concept data from file:"), c);
                // Add the processing buttons
                c.weightx = 0.0;
                c.gridx = 2;
                c.gridwidth = 1;
                setUpButtons(wizardPanel, c);

                //new concept fsn
                c.gridx = 0;
                c.gridy++;
                c.weightx = 0.0;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridwidth = 10;
                c.gridy++;
                wizardPanel.add(new JLabel("fully specified name:"), c);
                c.gridy++;
                c.weightx = 1.0;
                fsn = new FixedWidthJEditorPane();
                fsn.setFixedWidth(400);
                fsn.setText(fsnText);
                fsn.setEditable(true);
                fsn.getDocument().addDocumentListener(new CopyTextDocumentListener());
                wizardPanel.add(fsn, c);

                c.gridwidth = 4;
                c.gridx += c.gridwidth;
                c.gridwidth = 2;
                c.weightx = 0.0;
                inputFsnLabel = new JLabel("fsn");
                inputFsnLabel.setVisible(false);
                wizardPanel.add(inputFsnLabel, c);
                c.gridy++;
                c.gridx = 0;
                c.weightx = 0.0;

                //new concept fsn SctID
                c.gridy++;
                c.weightx = 0.0;
                JLabel fsnScdIdLabel = new JLabel("Fsn SctId:");
                fsnScdIdLabel.setVisible(hasFsnSctId);
                wizardPanel.add(fsnScdIdLabel, c);
                c.gridy++;
                c.weightx = 1.0;
                FixedWidthJEditorPane fsnSctIdPane = new FixedWidthJEditorPane();
                fsnSctIdPane.setFixedWidth(350);
                fsnSctIdPane.setText(fsnSctId);
                fsnSctIdPane.setEditable(false);
                fsnSctIdPane.setVisible(hasFsnSctId);
                wizardPanel.add(fsnSctIdPane, c);

                //new concept preferred name
                c.gridy++;
                c.weightx = 0.0;
                wizardPanel.add(new JLabel("preferred name:"), c);
                c.gridy++;
                c.weightx = 1.0;
                pref = new FixedWidthJEditorPane();
                pref.setFixedWidth(350);
                pref.setText("");
                pref.setEditable(true);
                wizardPanel.add(pref, c);

                c.gridwidth = 4;
                c.gridx += c.gridwidth;
                c.gridwidth = 2;
                c.weightx = 0.0;
                inputPrefLabel = new JLabel("pref");
                inputPrefLabel.setVisible(false);
                wizardPanel.add(inputPrefLabel, c);
                c.gridy++;
                c.gridx = 0;
                c.weightx = 0.0;

                //new concept UUID
                c.gridy++;
                c.weightx = 0.0;
                wizardPanel.add(new JLabel("UUID:"), c);
                c.gridy++;
                c.weightx = 1.0;
                newUuidPane = new FixedWidthJEditorPane();
                newUuidPane.setFixedWidth(350);
                newUuidPane.setText(newConceptId);
                newUuidPane.setEditable(false);
                newUuidPane.getDocument().addDocumentListener(new NewUuidDocumentListener());
                wizardPanel.add(newUuidPane, c);

                //new concept SctID
                c.gridy++;
                c.weightx = 0.0;
                JLabel sctIdLabel = new JLabel("SctId:");
                sctIdLabel.setVisible(hasSctId);
                wizardPanel.add(sctIdLabel, c);
                c.gridy++;
                c.weightx = 1.0;
                FixedWidthJEditorPane sctIdPane = new FixedWidthJEditorPane();
                sctIdPane.setFixedWidth(350);
                sctIdPane.setText(newConceptSctId);
                sctIdPane.setEditable(false);
                sctIdPane.setVisible(hasSctId);
                wizardPanel.add(sctIdPane, c);

                //parent fsn
                c.gridy++;
                c.weightx = 0.0;
                wizardPanel.add(new JLabel("parent fsn:"), c);
                c.gridy++;
                c.weightx = 1.0;
                parentFsnPane = new FixedWidthJEditorPane();
                parentFsnPane.setFixedWidth(350);
                parentFsnPane.setText(parentFsn);
                parentFsnPane.setEditable(true);
                parentFsnPane.getDocument().addDocumentListener(new ParentFsnDocumentListener());
                wizardPanel.add(parentFsnPane, c);

                //parent UUID
                c.gridy++;
                c.weightx = 0.0;
                wizardPanel.add(new JLabel("parent ID:"), c);
                c.gridy++;
                c.weightx = 1.0;
                parentUuidPane = new FixedWidthJEditorPane();
                parentUuidPane.setFixedWidth(350);
                parentUuidPane.setText(parentId);
                parentUuidPane.setEditable(true);
                parentUuidPane.getDocument().addDocumentListener(new ParentUuidDocumentListener());
                wizardPanel.add(parentUuidPane, c);

                //empty thing
                c.gridx = 0;
                c.gridy++;
                c.weightx = 0;
                c.weighty = 1;
                wizardPanel.add(new JPanel(), c);
                initialUpdate();
                GuiUtil.tickle(wizardPanel);
            } catch (InterruptedException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    protected static String getToDoImage() {
        return "/24x24/plain/inbox_into.png";
    }

    protected void setUpButtons(final JPanel wizardPanel, GridBagConstraints c) {
        c.gridx++;
        wizardPanel.add(new JLabel("  "), c);
        c.gridx++;
        c.anchor = GridBagConstraints.SOUTHWEST;

        JButton continueButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getContinueImage())));
        continueButton.setToolTipText("continue");
        wizardPanel.add(continueButton, c);
        continueButton.addActionListener(new BlueprintContinueActionListener());
        c.gridx++;

        JButton saveButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getToDoImage())));
        saveButton.setToolTipText("save");
        wizardPanel.add(saveButton, c);
        saveButton.addActionListener(new PreviousActionListener());
        c.gridx++;

        c.gridx++;
        wizardPanel.add(new JLabel("     "), c);
        wizardPanel.validate();
        Container cont = wizardPanel;
        while (cont != null) {
            cont.validate();
            cont = cont.getParent();
        }
        continueButton.requestFocusInWindow();
        wizardPanel.repaint();
    }

    protected void initialUpdate() {
        //set pref text and get spelling variants
        int paren = fsnText.indexOf("(");
        if (paren == -1) {
            prefText = fsnText;
            pref.setText(prefText);
        } else {
            prefText = fsnText.substring(0, paren).trim();
            pref.setText(prefText);
        }

        addSpellingVarients(prefText, fsnText);

        //set UUIDs
        newConceptUuid = UUID.fromString(newConceptId);

    }

    public class CopyTextDocumentListener implements DocumentListener {

        int paren;

        @Override
        public void changedUpdate(DocumentEvent arg0) {
            fsnText = "";
            prefText = "";
            fsnText = fsn.extractText();
            fsnText = fsnText.replaceAll("[\\s]", " ");
            fsnText = fsnText.replaceAll("   *", " ");
            paren = fsnText.indexOf("(");
            if (paren == -1) {
                prefText = fsnText;
                pref.setText(prefText);
            } else {
                prefText = fsnText.substring(0, paren).trim();
                pref.setText(prefText);
            }

            addSpellingVarients(prefText, fsnText);

        }

        @Override
        public void insertUpdate(DocumentEvent arg0) {
            fsnText = "";
            prefText = "";
            fsnText = fsn.extractText();
            fsnText = fsnText.replaceAll("[\\s]", " ");
            fsnText = fsnText.replaceAll("   *", " ");
            paren = fsnText.indexOf("(");
            if (paren == -1) {
                prefText = fsnText;
                pref.setText(prefText);
            } else {
                prefText = fsnText.substring(0, paren).trim();
                pref.setText(prefText);
            }

            addSpellingVarients(prefText, fsnText);

        }

        @Override
        public void removeUpdate(DocumentEvent arg0) {
            fsnText = "";
            prefText = "";
            if (fsn.extractText() != null) {
                fsnText = fsn.extractText();
                fsnText = fsnText.replaceAll("[\\s]", " ");
                fsnText = fsnText.replaceAll("   *", " ");
                paren = fsnText.indexOf("(");
                if (paren == -1) {
                    prefText = fsnText;
                    pref.setText(prefText);
                } else {
                    prefText = fsnText.substring(0, paren).trim();
                    pref.setText(prefText);
                }

                addSpellingVarients(prefText, fsnText);
            }

        }
    }

    public class NewUuidDocumentListener implements DocumentListener {

        int paren;

        @Override
        public void changedUpdate(DocumentEvent arg0) {
            String text;
            text = newUuidPane.extractText();
            text = text.replaceAll("[\\s]", " ");
            text = text.replaceAll("   *", " ");
            newConceptUuid = UUID.fromString(text);
        }

        @Override
        public void insertUpdate(DocumentEvent arg0) {
            String text;
            text = newUuidPane.extractText();
            text = text.replaceAll("[\\s]", " ");
            text = text.replaceAll("   *", " ");
            newConceptUuid = UUID.fromString(text);
        }

        @Override
        public void removeUpdate(DocumentEvent arg0) {
            String text;
            text = newUuidPane.extractText();
            text = text.replaceAll("[\\s]", " ");
            text = text.replaceAll("   *", " ");
            newConceptUuid = UUID.fromString(text);
        }
    }

    public class ParentUuidDocumentListener implements DocumentListener {

        int paren;

        @Override
        public void changedUpdate(DocumentEvent arg0) {
            String text;
            text = parentUuidPane.extractText();
            text = text.replaceAll("[\\s]", " ");
            text = text.replaceAll("   *", " ");
            if (text.length() == 36) {
                parentUuid = UUID.fromString(text);
            } else {
                parentId = text;
            }
        }

        @Override
        public void insertUpdate(DocumentEvent arg0) {
            String text;
            text = parentUuidPane.extractText();
            text = text.replaceAll("[\\s]", " ");
            text = text.replaceAll("   *", " ");
            if (text.length() == 36) {
                parentUuid = UUID.fromString(text);
            } else {
                parentId = text;
            }
        }

        @Override
        public void removeUpdate(DocumentEvent arg0) {
            String text;
            text = parentUuidPane.extractText();
            text = text.replaceAll("[\\s]", " ");
            text = text.replaceAll("   *", " ");
            if (text.length() == 36) {
                parentUuid = UUID.fromString(text);
            } else {
                parentId = text;
            }
        }
    }

    public class ParentFsnDocumentListener implements DocumentListener {

        int paren;

        @Override
        public void changedUpdate(DocumentEvent arg0) {
            parentFsn = parentFsnPane.extractText();
            parentFsn = parentFsn.replaceAll("[\\s]", " ");
            parentFsn = parentFsn.replaceAll("   *", " ");
        }

        @Override
        public void insertUpdate(DocumentEvent arg0) {
            parentFsn = parentFsnPane.extractText();
            parentFsn = parentFsn.replaceAll("[\\s]", " ");
            parentFsn = parentFsn.replaceAll("   *", " ");
        }

        @Override
        public void removeUpdate(DocumentEvent arg0) {
            parentFsn = parentFsnPane.extractText();
            parentFsn = parentFsn.replaceAll("[\\s]", " ");
            parentFsn = parentFsn.replaceAll("   *", " ");
        }
    }

    public void addSpellingVarients(String prefText, String fsnText) {
        String varient = "";
        String extra = "";
        if (fsnText.indexOf("(") != -1 && fsnText.indexOf(")") != -1) {
            extra = fsnText.substring(fsnText.indexOf("("), fsnText.indexOf(")") + 1);
        }
        try {
            if (DialectHelper.isTextForDialect(prefText, Language.EN_US.getLenient().getNid())
                    && DialectHelper.isTextForDialect(prefText, Language.EN_UK.getLenient().getNid())) {
                lang = "en";

                this.inputFsnLabel.setText("fsn en-GB / en-US");
                this.inputFsnLabel.setVisible(true);
                this.inputPrefLabel.setText("pref en-GB / en-US");
                this.inputPrefLabel.setVisible(true);

                addUsDescFsn = false;
                addUsDescPref = false;
                addGbDescPref = false;
                addGbDescFsn = false;
            } else if (DialectHelper.isTextForDialect(prefText, Language.EN_UK.getLenient().getNid())) { //check if lang is en-us
                lang = "en-us";

                this.inputFsnLabel.setText("fsn en-US");
                this.inputFsnLabel.setVisible(true);
                this.inputPrefLabel.setText("pref en-US");
                this.inputPrefLabel.setVisible(true);
                //get fsn
                varient = DialectHelper.makeTextForDialect(prefText, Language.EN_US.getLenient().getNid());
                this.gbFsn = varient + " " + extra;

                //get pref
                varient = DialectHelper.makeTextForDialect(prefText, Language.EN_US.getLenient().getNid());
                this.gbPref = varient;

                addUsDescFsn = false;
                addUsDescPref = false;
                addGbDescPref = true;
                addGbDescFsn = true;

            } else if (DialectHelper.isTextForDialect(prefText, Language.EN_US.getLenient().getNid())) { //check if lang is en-gb
                lang = "en-gb";

                this.inputFsnLabel.setText("fsn en-GB");
                this.inputFsnLabel.setVisible(true);
                this.inputPrefLabel.setText("pref en-GB");
                this.inputPrefLabel.setVisible(true);
                //get fsn
                varient = DialectHelper.makeTextForDialect(prefText, Language.EN_UK.getLenient().getNid());
                this.usFsn = varient + " " + extra;

                //get pref
                varient = DialectHelper.makeTextForDialect(prefText, Language.EN_UK.getLenient().getNid());
                this.usPref = varient;

                addUsDescFsn = true;
                addUsDescPref = true;
                addGbDescPref = false;
                addGbDescFsn = false;
            }else{
                throw new UnsupportedDialectOrLanguage("Check to make sure description does not contain both US and GB terms.");
            }
        } catch (UnsupportedDialectOrLanguage ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }

    }

    private void createBlueprintConcept() throws IOException, InvalidCAB, ContradictionException {
        tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                config.getViewCoordinate());
            //get parents
            UUID[] uuidArray = new UUID[1];
            uuidArray[0] = parentConcept.getPrimUuid();

            //create concept blue print
            if (lang.equals("en-gb")) {
                conceptSpec = new ConceptCB(fsnText, prefText, LANG_CODE.EN,
                        Snomed.IS_A.getLenient().getPrimUuid(), uuidArray);
            } else {
                conceptSpec = new ConceptCB(fsnText, prefText, LANG_CODE.EN,
                        Snomed.IS_A.getLenient().getPrimUuid(), uuidArray);
            }

            conceptSpec.setComponentUuid(newConceptUuid);
            newConcept = tc.constructIfNotCurrent(conceptSpec);

            // add sct id of component
            if (hasSctId) {
                Long sctId = Long.parseLong(newConceptSctId);
                ConceptAttributeAnalogBI analog = (ConceptAttributeAnalogBI) newConcept.getConceptAttributes();
                analog.addLongId(sctId,
                        Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids()),
                        SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                        config.getEditCoordinate(),
                        Long.MAX_VALUE);
            }
            // add sct id of fsn
            if (hasFsnSctId) {
                Long sctId = Long.parseLong(fsnSctId);
                DescriptionAnalogBI analog = 
                        (DescriptionAnalogBI) newConcept.getVersion(config.getViewCoordinate()).getDescriptionFullySpecified();
                for (PathBI ep : config.getEditingPathSet()) {
                    analog.addLongId(sctId,
                            Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids()),
                            SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                            config.getEditCoordinate(),
                            Long.MAX_VALUE);

                }
            }
    }

    private void createBlueprintGbFsnDesc() throws ContradictionException {
        String text = this.gbFsn;
        text = text.replaceAll("[\\s]", " ");
        text = text.replaceAll("   *", " ");
        try {
            descSpecGbFsn = new DescriptionCAB(
                    conceptSpec.getComponentUuid(),
                    fsnConcept.getPrimUuid(),
                    LANG_CODE.EN_GB,
                    text,
                    false);

            tc.construct(descSpecGbFsn);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintGbFsnRefex(int componentNid) throws ContradictionException {
        try {
            refexSpecGbFsn = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    componentNid,
                    Ts.get().getNidForUuids(gbUuid));
            refexSpecGbFsn.put(RefexProperty.CNID1, preferredConcept.getNid());
            tc.construct(refexSpecGbFsn);
            if (!gbRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(gbRefexConcept);
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintGbPrefDesc() throws ContradictionException {
        String text = this.gbPref;
        text = text.replaceAll("[\\s]", " ");
        text = text.replaceAll("   *", " ");
        try {
            descSpecGbPref = new DescriptionCAB(
                    conceptSpec.getComponentUuid(),
                    synConcept.getPrimUuid(),
                    LANG_CODE.EN_GB,
                    text,
                    false);

            tc.construct(descSpecGbPref);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintGbPrefRefex(int componentNid) throws ContradictionException {
        try {
            refexSpecGbPref = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    componentNid,
                    Ts.get().getNidForUuids(gbUuid));
            refexSpecGbPref.put(RefexProperty.CNID1, preferredConcept.getNid());

            tc.construct(refexSpecGbPref);
            if (!gbRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(gbRefexConcept);
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintGbAcctRefex(int componentNid) throws ContradictionException {
        try {
            refexSpecGbAcct = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    componentNid,
                    Ts.get().getNidForUuids(gbUuid));
            refexSpecGbAcct.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));

            tc.construct(refexSpecGbAcct);
            if (!gbRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(gbRefexConcept);
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintUsFsnDesc() throws ContradictionException {
        String text = this.usFsn;
        text = text.replaceAll("[\\s]", " ");
        text = text.replaceAll("   *", " ");
        try {
            descSpecUsFsn = new DescriptionCAB(
                    conceptSpec.getComponentUuid(),
                    fsnConcept.getPrimUuid(),
                    LANG_CODE.EN_US,
                    text,
                    false);

            tc.construct(descSpecUsFsn);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintUsFsnRefex(int componentNid) throws ContradictionException {
        try {
            refexSpecUsFsn = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    componentNid,
                    Ts.get().getNidForUuids(usUuid));

            refexSpecUsFsn.put(RefexProperty.CNID1, preferredConcept.getNid());

            tc.construct(refexSpecUsFsn);
            if (!usRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(usRefexConcept);
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintUsPrefDesc() throws ContradictionException {
        String text = this.usPref;
        text = text.replaceAll("[\\s]", " ");
        text = text.replaceAll("   *", " ");
        try {
            descSpecUsPref = new DescriptionCAB(
                    conceptSpec.getComponentUuid(),
                    synConcept.getPrimUuid(),
                    LANG_CODE.EN_US,
                    text,
                    false);

            tc.construct(descSpecUsPref);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintUsPrefRefex(int componentNid) throws ContradictionException {
        try {
            refexSpecUsPref = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    componentNid,
                    Ts.get().getNidForUuids(usUuid));

            refexSpecUsPref.put(RefexProperty.CNID1, preferredConcept.getNid());

            tc.construct(refexSpecUsPref);
            if (!usRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(usRefexConcept);
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintUsAcctRefex(int componentNid) throws ContradictionException {
        try {
            refexSpecUsAcct = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    componentNid,
                    Ts.get().getNidForUuids(usUuid));

            refexSpecUsAcct.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));

            tc.construct(refexSpecUsAcct);
            if (!usRefexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(usRefexConcept);
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    public class PreviousActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.PREVIOUS;
            done = true;
            NewConceptFromBatch.this.notifyTaskDone();
        }
    }

    public class BlueprintContinueActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            //make sure parent is valid
            if (parentId != null) { //test for valid parent
                try {
                    Set<I_GetConceptData> concepts;
                    concepts = Terms.get().getConcept(parentId);
                    
                    if(concepts.isEmpty()){
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "<html>The parent concpet has not been created."
                            + "<br>Fsn: " + parentFsn, "",
                            JOptionPane.ERROR_MESSAGE);
                    }else{
                        for (I_GetConceptData concept : concepts) {
                            parentUuid = concept.getPrimUuid();
                        }
                        parentConcept = Ts.get().getComponent(parentUuid);
                        parent = new ConceptSpec(parentFsn, parentUuid);
                        parent.getLenient();
                        returnCondition = Condition.CONTINUE;
                        done = true;
                        NewConceptFromBatch.this.notifyTaskDone();
                    }
                } catch (ValidationException ex) {
                    int option = JOptionPane.showConfirmDialog(LogWithAlerts.getActiveFrame(null),
                                "<html>The parent fsn and ID do not match."
                                + "<br>Fsn: " + parentFsn + " ID: " + parentId
                                +"<br>Do you want to continue?","", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            returnCondition = Condition.CONTINUE;
                            done = true;
                            NewConceptFromBatch.this.notifyTaskDone();
                        }
                } catch (AssertionError ex) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "<html>The parent concpet has not been created."
                            + "<br>Fsn: " + parentFsn, "",
                            JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "<html>The parent concpet has not been created."
                            + "<br>Fsn: " + parentFsn, "",
                            JOptionPane.ERROR_MESSAGE);
                } catch (TerminologyException ex) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "<html>The parent concpet has not been created."
                            + "<br>Fsn: " + parentFsn, "",
                            JOptionPane.ERROR_MESSAGE);
                } catch (java.text.ParseException ex) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "<html>The parent concpet has not been created."
                            + "<br>Fsn: " + parentFsn, "",
                            JOptionPane.ERROR_MESSAGE);
                } 

            } else if (fsn.extractText().length() == 0) {
                //please enter the fsn
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "please enter the fsn", "",
                        JOptionPane.ERROR_MESSAGE);
            } else if ((fsn.extractText().length() != 0) && (fsn.extractText().indexOf("(") == -1 || fsn.extractText().indexOf(")") == -1)) {
                //test for semantic tag
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "please enter the semantic tag", "",
                        JOptionPane.ERROR_MESSAGE);
            } else if (parentFsnPane.extractText().length() == 0
                    || parentUuidPane.extractText().length() == 0) {
                //please list parents for the new concept
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "please list parents for the new concept", "",
                        JOptionPane.ERROR_MESSAGE);
            } else if (fsn.extractText().length() != 0 && fsn.extractText().indexOf("(") > 0
                    && fsn.extractText().indexOf(")") > fsn.extractText().indexOf("(")) {
                //get text parts and make query term
                String fullFsn = fsn.extractText();
                String[] fsnWords = fullFsn.split("\\s");
                HashSet<String> wordSet = new HashSet<String>();
                for (String word : fsnWords) {
                    if (!wordSet.contains(word) && word.length() > 1
                            && !word.startsWith("(") && !word.endsWith(")")) {
                        word = QueryParser.escape(word);
                        wordSet.add(word);
                    }
                }
                String queryTerm = null;
                for (String word : wordSet) {
                    if (queryTerm == null) {
                        queryTerm = "+" + word;
                    } else {
                        queryTerm = queryTerm + " " + "+" + word;
                    }
                }
                try {
                    SearchResult result = Terms.get().doLuceneSearch(queryTerm);
                    if (result.topDocs.totalHits == 0) {
                        returnCondition = Condition.CONTINUE;
                        done = true;
                        NewConceptFromBatch.this.notifyTaskDone();
                    }
                    NidSetBI allowedStatusNids = config.getViewCoordinate().getAllowedStatusNids();
                    Boolean found = false;
                    search:
                    for (int i = 0; i < result.topDocs.totalHits; i++) {
                        Document doc = result.searcher.doc(result.topDocs.scoreDocs[i].doc);
                        int cnid = Integer.parseInt(doc.get("cnid"));
                        int dnid = Integer.parseInt(doc.get("dnid"));

                        I_DescriptionVersioned<?> potential_fsn = Terms.get().getDescription(dnid, cnid);
                        if (potential_fsn != null) {
                            for (I_DescriptionPart part_search : potential_fsn.getMutableParts()) {
                                String test = part_search.getText();
                                if (allowedStatusNids.contains(part_search.getStatusNid())
                                        && part_search.getText().equals(fullFsn)) {
                                    found = true;
                                    break search;
                                } else {
                                    found = false;
                                }
                            }
                        }
                    }
                    if (found) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                "<html>FSN already used: " + fullFsn, "",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        returnCondition = Condition.CONTINUE;
                        done = true;
                        NewConceptFromBatch.this.notifyTaskDone();
                    }
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                } catch (TerminologyException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                } catch (java.text.ParseException ex) {
                    Logger.getLogger(NewConceptFromBatch.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.PREVIOUS_CONTINUE_CANCEL;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public String getSubmissionLineProp() {
        return submissionLineProp;
    }

    public void setSubmissionLineProp(String submissionLineProp) {
        this.submissionLineProp = submissionLineProp;
    }
}
