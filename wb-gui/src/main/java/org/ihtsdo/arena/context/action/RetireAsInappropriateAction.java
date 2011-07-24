package org.ihtsdo.arena.context.action;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.WizardPanel;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

public class RetireAsInappropriateAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    ComponentVersionBI analog;
    I_ConfigAceFrame config;
    TerminologyList tl;
    WizardPanel wizard;
    ConceptViewSettings settings;
    ConceptChronicleBI refexConcept;

    public RetireAsInappropriateAction(String actionName, DescFact fact, ConceptViewSettings settings) {
        super(actionName);
        this.component = fact.getComponent();
        this.settings = settings;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            config = Terms.get().getActiveAceFrameConfig();
            if (DescriptionVersionBI.class.isAssignableFrom(component.getClass())) {
                wizard = settings.getView().getCvRenderer().getWizardPanel();
                JPanel wizardPanel = wizard.getWizardPanel();
                makeWizardPanel(wizardPanel);
                settings.getHost().unlink();
                if (SwingUtilities.isEventDispatchThread()) {
                    wizard.setWizardPanelVisible(true);
                } else {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            wizard.setWizardPanelVisible(true);
                        }
                    });
                }
            }
        } catch (InterruptedException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InvocationTargetException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }

    private void makeWizardPanel(JPanel wizardPanel) {
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
        wizardPanel.add(new JLabel("<html>Please add the concept to which <br>the synonym should refer:"), c);

        // Add the processing buttons
        c.weightx = 0.0;
        c.gridx = 2;
        c.gridwidth = 1;
        setUpButtons(wizardPanel, c);

        //add concept list
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.0;
        c.gridwidth = 10;
        wizardPanel.add(new JSeparator(), c);
        c.gridwidth = 2;
        c.gridy++;
        c.gridx = 0;
        c.weighty = 1.0;
        c.weightx = 1.0;
        tl = new TerminologyList(config);
        wizardPanel.add(tl, c);
        c.weighty = 0.0;

        //empty thing
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 1;
        wizardPanel.add(new JLabel(""), c);
    }

    protected void setUpButtons(final JPanel wizardPanel, GridBagConstraints c) {
        c.gridx++;
        wizardPanel.add(new JLabel("  "), c);
        c.gridx++;
        c.anchor = GridBagConstraints.SOUTHWEST;

        JButton continueButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getContinueImage())));
        continueButton.setToolTipText("continue");
        wizardPanel.add(continueButton, c);
        continueButton.addActionListener(new ContinueActionListener());
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

    private class ContinueActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            wizard.setWizardPanelVisible(false);
            if (tl != null) {
                TerminologyListModel model = (TerminologyListModel) tl.getModel();
                List<Integer> nidList = model.getNidsInList();
                for (int nid : nidList) {
                    retireSynonym();
                    addToRefersToRefset(nid);
                    retireFromRefexes(component);
                }
            }
        }
    }

    private class StopActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            wizard.setWizardPanelVisible(false);
        }
    }

    protected static String getContinueImage() {
        return "/16x16/plain/navigate_right.png";
    }

    protected static String getCancelImage() {
        return "/16x16/plain/navigate_cross.png";
    }

    private void retireSynonym() {
        try {
            if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                I_AmPart componentVersion = (I_AmPart) component;
                int statusNid = 0;
                if(Ts.get().usesRf2Metadata()){
                    statusNid = SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getNid();
                }else{
                    statusNid = SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getNid();
                }
                for (PathBI ep : config.getEditingPathSet()) {
                    analog = (ComponentVersionBI) componentVersion.makeAnalog(
                            statusNid,
                            config.getDbConfig().getUserConcept().getNid(),
                            ep.getConceptNid(),
                            Long.MAX_VALUE);
                }
                ComponentVersionBI newComponent = (ComponentVersionBI) analog;

                I_GetConceptData concept = Terms.get().getConceptForNid(newComponent.getNid());
                Terms.get().addUncommitted(concept);
            }
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
    
        private void retireFromRefexes(ComponentVersionBI component) {
        DescriptionVersionBI desc = (DescriptionVersionBI) component;
        try {
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            I_AmPart componentVersion;
            ViewCoordinate vc = config.getViewCoordinate();
            Collection<? extends RefexChronicleBI> refexes = desc.getCurrentRefexes(vc);
            int usNid = 0;
            int gbNid = 0;
            int dosNid = 0;
            if (Ts.get().hasUuid(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getPrimUuid())) {
                usNid = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getNid();
            } else {
                usNid = SnomedMetadataRf1.US_LANGUAGE_REFSET_RF1.getLenient().getNid();
            }
            if (Ts.get().hasUuid(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getPrimUuid())) {
                gbNid = SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getNid();
            } else {
                gbNid = SnomedMetadataRf1.GB_LANGUAGE_REFSET_RF1.getLenient().getNid();
            }
            if (Ts.get().hasUuid(SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getLenient().getPrimUuid())) {
                dosNid = SnomedMetadataRf2.DEGREE_OF_SYNONYMY_RF2.getLenient().getNid();
            } else {
                dosNid = SnomedMetadataRf1.DEGREE_OF_SYNONYMY_REFSET_RF1.getLenient().getNid();
            }
            for (RefexChronicleBI refex : refexes) {
                int refexNid = refex.getCollectionNid();
                if (refexNid == gbNid || refexNid == usNid || refexNid == dosNid) {
                    componentVersion = (I_AmPart) refex;
                    for (PathBI ep : config.getEditingPathSet()) {
                        componentVersion.makeAnalog(
                                SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                                config.getDbConfig().getUserConcept().getNid(),
                                ep.getConceptNid(),
                                Long.MAX_VALUE);
                    }
                    I_GetConceptData concept = Terms.get().getConceptForNid(analog.getNid());
                    Terms.get().addUncommitted(concept);
                } else {
                    throw new UnsupportedOperationException("Can't convert: RefexCnidVersionBI");
                }
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (TerminologyException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void addToRefersToRefset(int nid) {
        try {
            if (Ts.get().hasUuid(SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getLenient().getPrimUuid())) {
                refexConcept = SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getLenient();
            } else {
                refexConcept = SnomedMetadataRf1.REFERS_TO_REFSET_RF1.getLenient();
            }

            RefexCAB newSpec = new RefexCAB(
                    TK_REFSET_TYPE.CID,
                    component.getNid(),
                    refexConcept.getNid());
            newSpec.put(RefexProperty.CNID1, nid);
            TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                    config.getViewCoordinate());
            tc.construct(newSpec);
            if (!refexConcept.isAnnotationStyleRefex()) {
            Ts.get().addUncommitted(refexConcept);
            }
            I_GetConceptData concept = Terms.get().getConceptForNid(component.getConceptNid());
            Ts.get().addUncommitted(concept);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InvalidCAB e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}
