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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.wfpanel.PreviousNextOrCancel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.arena.conceptview.FixedWidthJEditorPane;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.WizardBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.example.binding.WbDescType;
import org.ihtsdo.util.swing.GuiUtil;

import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.arena.spec.Refsets;
import org.ihtsdo.tk.example.binding.Snomed;
import org.ihtsdo.tk.example.binding.SnomedMetadataRf1;
import org.ihtsdo.tk.example.binding.SnomedMetadataRf2;
import org.ihtsdo.tk.helper.TerminologyHelperDrools;

/**
 *
 * @author kec
 */
@BeanList(specs = {
    @Spec(directory = "tasks/ide/instruct", type = BeanType.TASK_BEAN),
    @Spec(directory = "tasks/arena/wizard", type = BeanType.TASK_BEAN)})
public class NewConcept extends PreviousNextOrCancel {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    // Task Attribute Properties
    private String instruction = "<html>Enter data for new concept:";
    private String relParentPropName = ProcessAttachmentKeys.REL_PARENT.getAttachmentKey();
    // Other Properties
    private I_ConfigAceFrame config;
    private I_HostConceptPlugins host;
    private transient WizardBI wizard;
    private TerminologyList tl;
    private FixedWidthJEditorPane fsn;
    private JLabel inputFsnLabel;
    private JLabel inputPrefLabel;
    private FixedWidthJEditorPane pref;
    private FixedWidthJEditorPane usFsn;
    private FixedWidthJEditorPane gbFsn;
    private JCheckBox gbBoxFsn;
    private JCheckBox usBoxFsn;
    private JLabel gbLabelFsn;
    private JLabel usLabelFsn;
    private FixedWidthJEditorPane usPref;
    private FixedWidthJEditorPane gbPref;
    private JCheckBox gbBoxPref;
    private JCheckBox usBoxPref;
    private JLabel gbLabelPref;
    private JLabel usLabelPref;
    private boolean addUsDescFsn = false;
    private boolean addGbDescFsn = false;
    private boolean addUsDescPref = false;
    private boolean addGbDescPref = false;
    private List<Integer> nidList;
    private ConceptCB conceptSpec;
    private DescCAB descSpecGbFsn;
    private DescCAB descSpecUsFsn;
    private RefexCAB refexSpecGbFsn;
    private RefexCAB refexSpecUsFsn;
    private DescCAB descSpecGbPref;
    private DescCAB descSpecUsPref;
    private RefexCAB refexSpecGbPref;
    private RefexCAB refexSpecUsPref;
    private RefexCAB refexSpecUsAcct;
    private RefexCAB refexSpecGbAcct;
    private TerminologyConstructorBI tc;
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

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(instruction);
        out.writeObject(relParentPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                instruction = (String) in.readObject();
                relParentPropName = (String) in.readObject();
            } else {
                instruction = "<html>Select Parent for Concept Being Retired:";
            }

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
            wizard = (WizardBI) worker.readAttachement(WorkerAttachmentKeys.WIZARD_PANEL.name());
            config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            host = (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
            host.unlink();

            DoSwing swinger = new DoSwing(process);
            swinger.execute();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }
            restore();

            if (SwingUtilities.isEventDispatchThread()) {
                doRun(process, worker);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        doRun(process, worker);
                    }
                });
            }

        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        }

        return returnCondition;
    }

    public void doRun(final I_EncodeBusinessProcess process, I_Work worker) {
        wizard.setWizardPanelVisible(false);

        try {
            // check return condition for CONTINUE or ITEM_CANCELLED
            if (returnCondition == Condition.CONTINUE) {
                createBlueprintConcept();
                //get rf1 or rf2 versions
                if (Ts.get().hasUuid(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getPrimUuid())) {
                    gbRefexConcept = SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient();
                    gbUuid = gbRefexConcept.getPrimUuid();
                } else {
                    gbRefexConcept = SnomedMetadataRf1.GB_LANGUAGE_REFSET_RF1.getLenient();
                    gbUuid = gbRefexConcept.getPrimUuid();
                }
                if (Ts.get().hasUuid(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getPrimUuid())) {
                    usRefexConcept = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient();
                    usUuid = usRefexConcept.getPrimUuid();
                } else {
                    usRefexConcept = SnomedMetadataRf1.US_LANGUAGE_REFSET_RF1.getLenient();
                    usUuid = usRefexConcept.getPrimUuid();
                }
                if (Ts.get().hasUuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid())) {
                    acceptableConcept = SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient();
                } else {
                    acceptableConcept = SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1.getLenient();
                }
                if (Ts.get().hasUuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid())) {
                    preferredConcept = SnomedMetadataRf2.PREFERRED_RF2.getLenient();
                } else {
                    preferredConcept = SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient();
                }
                if (Ts.get().hasUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid())) {
                    fsnConcept = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient();
                } else {
                    fsnConcept = SnomedMetadataRf1.FULLY_SPECIFIED_DESCRIPTION_TYPE.getLenient();
                }
                if (Ts.get().hasUuid(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getPrimUuid())) {
                    synConcept = SnomedMetadataRf2.SYNONYM_RF2.getLenient();
                } else {
                    synConcept = SnomedMetadataRf1.SYNOMYM_DESCRIPTION_TYPE_RF1.getLenient();
                }

                //create blueprints
                if (addUsDescFsn) {
                    createBlueprintUsFsnDesc();
                    createBlueprintUsFsnRefex(descSpecUsFsn.getComponentNid());
                }
                if (addGbDescFsn) {
                    createBlueprintGbFsnDesc();
                    createBlueprintGbFsnRefex(descSpecGbFsn.getComponentNid());
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
                if (lang.equals("en")) {
                    createBlueprintUsFsnRefex(conceptSpec.getFsnCAB().getComponentNid());
                    createBlueprintGbFsnRefex(conceptSpec.getFsnCAB().getComponentNid());
                    createBlueprintUsPrefRefex(conceptSpec.getPreferredCAB().getComponentNid());
                    createBlueprintGbPrefRefex(conceptSpec.getPreferredCAB().getComponentNid());
                }
                if (lang.equals("en-us")) {
                    createBlueprintUsFsnRefex(conceptSpec.getFsnCAB().getComponentNid());
                    createBlueprintUsPrefRefex(conceptSpec.getPreferredCAB().getComponentNid());
 //                   createBlueprintGbAcctRefex(conceptSpec.getPreferredCAB().getComponentNid()); //removed for rf2
                }
                if (lang.equals("en-gb")) {
                    createBlueprintGbFsnRefex(conceptSpec.getFsnCAB().getComponentNid());
                    createBlueprintGbPrefRefex(conceptSpec.getPreferredCAB().getComponentNid());
 //                   createBlueprintUsAcctRefex(conceptSpec.getPreferredCAB().getComponentNid()); //removed for rf2
                }
                Ts.get().addUncommitted(newConcept);

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        I_AmTermComponent newTerm = (I_AmTermComponent) newConcept;
                        host.setTermComponent(newTerm);
                        wizard.setWizardPanelVisible(false);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnCondition = Condition.ITEM_CANCELED;
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
                wizardPanel.add(new JLabel(instruction), c);
                // Add the processing buttons
                c.weightx = 0.0;
                c.gridx = 2;
                c.gridwidth = 1;
                setUpButtons(wizardPanel, c);


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
                fsn.setText("");
                fsn.setFixedWidth(300);
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

                c.gridy++;
                c.weightx = 0.0;
                wizardPanel.add(new JLabel("preferred name:"), c);
                c.gridy++;
                c.weightx = 1.0;
                pref = new FixedWidthJEditorPane();
                pref.setText("");
                pref.setFixedWidth(300);
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



                c.gridy++;
                c.weightx = 0.0;
                wizardPanel.add(new JLabel("parents:"), c);
                c.gridy++;
                c.weightx = 1.0;
                c.weighty = 1.0;
                tl = new TerminologyList(config);
                wizardPanel.add(tl, c);
                c.weighty = 0.0;

                c.gridy++;
                c.gridx = 0;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy++;
                c.weightx = 1.0;
                c.gridwidth = 1;
                c.weightx = 0.0;
                gbBoxFsn = new JCheckBox();
                gbBoxFsn.addItemListener(new GbDialectFsnItemListener());
                gbBoxFsn.setVisible(false);
                wizardPanel.add(gbBoxFsn, c);
                c.gridx++;
                c.gridwidth = 3;
                c.weightx = 1.0;
                gbFsn = new FixedWidthJEditorPane();
                gbFsn.setFixedWidth(300);
                gbFsn.setEditable(true);
                gbFsn.setVisible(false);
                wizardPanel.add(gbFsn, c);
                c.gridx += c.gridwidth;
                c.gridwidth = 2;
                c.weightx = 0.0;
                gbLabelFsn = new JLabel("fsn en-GB");
                gbLabelFsn.setVisible(false);
                wizardPanel.add(gbLabelFsn, c);
                c.gridy++;
                c.gridx = 0;
                c.weightx = 0.0;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy++;


                c.gridy++;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy++;
                c.weightx = 1.0;
                c.gridwidth = 1;
                c.weightx = 0.0;
                gbBoxPref = new JCheckBox();
                gbBoxPref.addItemListener(new GbDialectPrefItemListener());
                gbBoxPref.setVisible(false);
                wizardPanel.add(gbBoxPref, c);
                c.gridx++;
                c.gridwidth = 3;
                c.weightx = 1.0;
                gbPref = new FixedWidthJEditorPane();
                gbPref.setFixedWidth(300);
                gbPref.setEditable(true);
                gbPref.setVisible(false);
                wizardPanel.add(gbPref, c);
                c.gridx += c.gridwidth;
                c.gridwidth = 2;
                c.weightx = 0.0;
                gbLabelPref = new JLabel("pref en-GB");
                gbLabelPref.setVisible(false);
                wizardPanel.add(gbLabelPref, c);
                c.gridy++;
                c.gridx = 0;
                c.weightx = 0.0;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy++;


                c.gridy++;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy++;
                c.weightx = 1.0;
                c.gridwidth = 1;
                c.weightx = 0.0;
                usBoxFsn = new JCheckBox();
                usBoxFsn.addItemListener(new UsDialectFsnItemListener());
                usBoxFsn.setVisible(false);
                wizardPanel.add(usBoxFsn, c);
                c.gridx++;
                c.gridwidth = 3;
                c.weightx = 1.0;
                usFsn = new FixedWidthJEditorPane();
                usFsn.setFixedWidth(300);
                usFsn.setEditable(true);
                usFsn.setVisible(false);
                wizardPanel.add(usFsn, c);
                c.gridx += c.gridwidth;
                c.gridwidth = 2;
                c.weightx = 0.0;
                usLabelFsn = new JLabel("fsn en-US");
                usLabelFsn.setVisible(false);
                wizardPanel.add(usLabelFsn, c);
                c.gridy++;
                c.gridx = 0;
                c.weightx = 0.0;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy++;


                c.gridy++;
                c.gridwidth = 10;
                wizardPanel.add(new JSeparator(), c);
                c.gridy++;
                c.weightx = 1.0;
                c.gridwidth = 1;
                c.weightx = 0.0;
                usBoxPref = new JCheckBox();
                usBoxPref.addItemListener(new UsDialectPrefItemListener());
                usBoxPref.setVisible(false);
                wizardPanel.add(usBoxPref, c);
                c.gridx++;
                c.gridwidth = 3;
                c.weightx = 1.0;
                usPref = new FixedWidthJEditorPane();
                usPref.setFixedWidth(300);
                usPref.setEditable(true);
                usPref.setVisible(false);
                wizardPanel.add(usPref, c);
                c.gridx += c.gridwidth;
                c.gridwidth = 2;
                c.weightx = 0.0;
                usLabelPref = new JLabel("pref en-US");
                usLabelPref.setVisible(false);
                wizardPanel.add(usLabelPref, c);


                //empty thing
                c.gridx = 0;
                c.gridy++;
                c.weightx = 0;
                c.weighty = 1;
                wizardPanel.add(new JPanel(), c);
                GuiUtil.tickle(wizardPanel);
            } catch (InterruptedException ex) {
                Logger.getLogger(NewConcept.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(NewConcept.class.getName()).log(Level.SEVERE, null, ex);
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

        JButton cancelButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getCancelImage())));
        cancelButton.setToolTipText("cancel");
        wizardPanel.add(cancelButton, c);
        cancelButton.addActionListener(new StopActionListener());
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

    public class CopyTextDocumentListener implements DocumentListener {

        String fsnText = "";
        int paren;
        String prefText = "";

        @Override
        public void changedUpdate(DocumentEvent arg0) {
            fsnText = fsn.extractText();
            paren = fsnText.indexOf("(");
            if (paren == -1) {
                prefText = fsnText;
                pref.setText(prefText);
            } else {
                prefText = fsnText.substring(0, paren - 1);
                pref.setText(prefText);
            }

            addSpellingVarients(prefText, fsnText);

        }

        @Override
        public void insertUpdate(DocumentEvent arg0) {
            fsnText = fsn.extractText();
            paren = fsnText.indexOf("(");
            if (paren == -1) {
                prefText = fsnText;
                pref.setText(prefText);
            } else {
                prefText = fsnText.substring(0, paren - 1);
                pref.setText(prefText);
            }

            addSpellingVarients(prefText, fsnText);

        }

        @Override
        public void removeUpdate(DocumentEvent arg0) {
            fsnText = fsn.extractText();
            paren = fsnText.indexOf("(");
            if (paren == -1) {
                prefText = fsnText;
                pref.setText(prefText);
            } else {
                prefText = fsnText.substring(0, paren - 1);
                pref.setText(prefText);
            }

            addSpellingVarients(prefText, fsnText);

        }
    }

    public void addSpellingVarients(String prefText, String fsnText) {
        TerminologyHelperDrools th = new TerminologyHelperDrools();
        String us = "en-us";
        String gb = "en-gb";
        String varient = "";
        String extra = "";
        if (fsnText.indexOf("(") != -1 && fsnText.indexOf(")") != -1) {
            extra = fsnText.substring(fsnText.indexOf("("), fsnText.indexOf(")") + 1);
        }

        if (th.loadProperties()) {
            if (th.checkTermSpelling(prefText, us) && th.checkTermSpelling(prefText, gb)) {
                lang = "en";

                this.inputFsnLabel.setText("fsn en-GB / en-US");
                this.inputFsnLabel.setVisible(true);
                this.inputPrefLabel.setText("pref en-GB / en-US");
                this.inputPrefLabel.setVisible(true);

                this.usBoxPref.setVisible(false);
                this.usPref.setVisible(false);
                this.usLabelPref.setVisible(false);

                this.gbBoxPref.setVisible(false);
                this.gbPref.setVisible(false);
                this.gbLabelPref.setVisible(false);

                this.gbBoxFsn.setSelected(false);
                this.gbBoxFsn.setVisible(false);
                this.gbFsn.setVisible(false);
                this.gbLabelFsn.setVisible(false);

                this.usBoxFsn.setVisible(false);
                this.usBoxFsn.setSelected(false);
                this.usFsn.setVisible(false);
                this.usLabelFsn.setVisible(false);
            } else if (th.checkTermSpelling(prefText, us)) { //check if lang is en-us
                lang = "en-us";

                this.inputFsnLabel.setText("fsn en-US");
                this.inputFsnLabel.setVisible(true);
                this.inputPrefLabel.setText("pref en-US");
                this.inputPrefLabel.setVisible(true);
                //get fsn
                varient = th.getSpellingTerm(prefText, us);
                this.gbFsn.setText(varient + " " + extra);
                this.gbBoxFsn.setSelected(true);
                this.gbBoxFsn.setVisible(true);
                this.gbFsn.setVisible(true);
                this.gbLabelFsn.setVisible(true);
                this.usBoxFsn.setVisible(false);
                this.usBoxFsn.setSelected(false);
                this.usFsn.setVisible(false);
                this.usLabelFsn.setVisible(false);

                //get pref
                varient = th.getSpellingTerm(prefText, us);
                this.gbPref.setText(varient);
                this.gbBoxPref.setSelected(true);
                this.gbBoxPref.setVisible(true);
                this.gbPref.setVisible(true);
                this.gbLabelPref.setVisible(true);
                this.usBoxPref.setVisible(false);
                this.usBoxPref.setSelected(false);
                this.usPref.setVisible(false);
                this.usLabelPref.setVisible(false);

            } else if (th.checkTermSpelling(prefText, gb)) { //check if lang is en-gb
                lang = "en-gb";

                this.inputFsnLabel.setText("fsn en-GB");
                this.inputFsnLabel.setVisible(true);
                this.inputPrefLabel.setText("pref en-GB");
                this.inputPrefLabel.setVisible(true);
                //get fsn
                varient = th.getSpellingTerm(prefText, gb);
                this.usFsn.setText(varient + " " + extra);
                this.usBoxFsn.setSelected(true);
                this.usBoxFsn.setVisible(true);
                this.usFsn.setVisible(true);
                this.usLabelFsn.setVisible(true);
                this.gbBoxFsn.setVisible(false);
                this.gbBoxFsn.setSelected(false);
                this.gbFsn.setVisible(false);
                this.gbLabelFsn.setVisible(false);

                //get pref
                varient = th.getSpellingTerm(prefText, gb);
                this.usPref.setText(varient);
                this.usBoxPref.setSelected(true);
                this.usBoxPref.setVisible(true);
                this.usPref.setVisible(true);
                this.usLabelPref.setVisible(true);
                this.gbBoxPref.setVisible(false);
                this.gbBoxPref.setSelected(false);
                this.gbPref.setVisible(false);
                this.gbLabelPref.setVisible(false);
            }
        }
    }

    private void createBlueprintConcept() {
        tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                config.getViewCoordinate());

        try {
            //get parents
            UUID isa = Snomed.IS_A.getLenient().getPrimUuid();
            UUID[] uuidArray = new UUID[nidList.size()];

            for (int index = 0; index < nidList.size(); index++) {
                uuidArray[index] = Terms.get().nidToUuid(nidList.get(index));
            }


            //create concept blue print
            conceptSpec = new ConceptCB(fsn.extractText(), pref.extractText(), "en", isa, uuidArray);
            newConcept = tc.constructIfNotCurrent(conceptSpec);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (InvalidCAB e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    private void createBlueprintGbFsnDesc() {
        try {
            descSpecGbFsn = new DescCAB(
                    conceptSpec.getComponentUuid(),
                    fsnConcept.getPrimUuid(),
                    "en-gb",
                    gbFsn.extractText(),
                    false);

            tc.construct(descSpecGbFsn);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintGbFsnRefex(int componentNid) {
        try {
            refexSpecGbFsn = new RefexCAB(
                    TK_REFSET_TYPE.CID,
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

    private void createBlueprintGbPrefDesc() {
        try {
            descSpecGbPref = new DescCAB(
                    conceptSpec.getComponentUuid(),
                    synConcept.getPrimUuid(),
                    "en-gb",
                    gbPref.extractText(),
                    false);

            tc.construct(descSpecGbPref);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintGbPrefRefex(int componentNid) {
        try {
            refexSpecGbPref = new RefexCAB(
                    TK_REFSET_TYPE.CID,
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

    private void createBlueprintGbAcctRefex(int componentNid) {
        try {
            refexSpecGbAcct = new RefexCAB(
                    TK_REFSET_TYPE.CID,
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

    private void createBlueprintUsFsnDesc() {
        try {
            descSpecUsFsn = new DescCAB(
                    conceptSpec.getComponentUuid(),
                    fsnConcept.getPrimUuid(),
                    "en-us",
                    usFsn.extractText(),
                    false);

            tc.construct(descSpecUsFsn);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintUsFsnRefex(int componentNid) {
        try {
            refexSpecUsFsn = new RefexCAB(
                    TK_REFSET_TYPE.CID,
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

    private void createBlueprintUsPrefDesc() {
        try {
            descSpecUsPref = new DescCAB(
                    conceptSpec.getComponentUuid(),
                    synConcept.getPrimUuid(),
                    "en-us",
                    usPref.extractText(),
                    false);

            tc.construct(descSpecUsPref);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void createBlueprintUsPrefRefex(int componentNid) {
        try {
            refexSpecUsPref = new RefexCAB(
                    TK_REFSET_TYPE.CID,
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

    private void createBlueprintUsAcctRefex(int componentNid) {
        try {
            refexSpecUsAcct = new RefexCAB(
                    TK_REFSET_TYPE.CID,
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

    public class BlueprintContinueActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            TerminologyListModel model = (TerminologyListModel) tl.getModel();
            nidList = model.getNidsInList();

            if (fsn.extractText().length() == 0) {
                //please enter the fsn
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "please enter the fsn", "",
                        JOptionPane.ERROR_MESSAGE);
            } else if ((fsn.extractText().length() != 0) && (fsn.extractText().indexOf("(") == -1 || fsn.extractText().indexOf(")") == -1)) {
                //test for semantic tag
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "please enter the semantic tag", "",
                        JOptionPane.ERROR_MESSAGE);
            } else if (nidList.isEmpty()) {
                //please list parents for the new concept
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "please list parents for the new concept", "",
                        JOptionPane.ERROR_MESSAGE);
            } else if (fsn.extractText().length() != 0 && fsn.extractText().indexOf("(") > 0
                    && fsn.extractText().indexOf(")") > fsn.extractText().indexOf("(")) {
                returnCondition = Condition.CONTINUE;
                done = true;
                NewConcept.this.notifyTaskDone();
            }
        }
    }

    public class GbDialectFsnItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                addGbDescFsn = true;
            } else {
                addGbDescFsn = false;
            }
        }
    }

    public class UsDialectFsnItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                addUsDescFsn = true;
            } else {
                addUsDescFsn = false;
            }
        }
    }

    public class GbDialectPrefItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                addGbDescPref = true;
            } else {
                addGbDescPref = false;
            }
        }
    }

    public class UsDialectPrefItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            AbstractButton abstractButton = (AbstractButton) e.getSource();
            boolean selected = abstractButton.getModel().isSelected();

            if (selected) {
                addUsDescPref = true;
            } else {
                addUsDescPref = false;
            }
        }
    }

    /**
     * Get the instructions for this task
     * 
     * @return The instructions for this task.
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * Set the instructions for this task
     * 
     * @param instruction The instructions for this task.
     * @return void
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getRelParentPropName() {
        return relParentPropName;
    }

    public void setRelParentPropName(String newStatusPropName) {
        this.relParentPropName = newStatusPropName;
    }
}
