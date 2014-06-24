package org.ihtsdo.arena.context.action;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.arena.WizardPanel;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

public class RetireAsInappropriateAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    ComponentVersionBI analogComponent;
    I_ConfigAceFrame config;
    TermComponentLabel tl;
    WizardPanel wizard;
    ConceptViewSettings settings;
    ConceptChronicleBI refexConcept;
    int refersToNid = 0;

    public RetireAsInappropriateAction(String actionName, DescFact fact, ConceptViewSettings settings, I_ConfigAceFrame config) {
        super(actionName);
        this.component = fact.getComponent();
        this.settings = settings;
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
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

    private void makeWizardPanel(JPanel wizardPanel) throws TerminologyException, IOException {
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
        tl = new TermComponentLabel();
        tl.addTermChangeListener(new ChangeListener());
        wizardPanel.add(tl, c);
        c.weighty = 0.0;

        //empty thing
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        c.weighty = 1;
        wizardPanel.add(new JLabel(""), c);
    }

    protected class ChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            I_GetConceptData newValue = (I_GetConceptData) e.getNewValue();
            refersToNid = newValue.getNid();
        }
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
            if (refersToNid != 0) {
                wizard.setWizardPanelVisible(false);
                retireFromRefexes(component);
                retireSynonym();
                addToRefersToRefset(refersToNid);
                try {
					I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());
					Ts.get().addUncommitted(concept);
					if (refexConcept != null && !refexConcept.isAnnotationStyleRefex()) {
                        Ts.get().addUncommitted(refexConcept);
                    }
				} catch (IOException e1) {
					AceLog.getAppLog().alertAndLogException(e1);
				}
            } else {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "Please enter a 'refers to' concept", "",
                        JOptionPane.ERROR_MESSAGE);
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
                int statusNid = SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID();
                for (PathBI ep : config.getEditingPathSet()) {
                    analogComponent = (ComponentVersionBI) componentVersion.makeAnalog(
                            statusNid,
                            Long.MAX_VALUE,
                            config.getEditCoordinate().getAuthorNid(),
                            config.getEditCoordinate().getModuleNid(), 
                            ep.getConceptNid());
                }
            }
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    private void retireFromRefexes(ComponentVersionBI component) {
        DescriptionVersionBI desc = (DescriptionVersionBI) component;
        try {
            I_AmPart componentVersion;
            ViewCoordinate vc = config.getViewCoordinate();
            Collection<? extends RefexChronicleBI> refexes = desc.getRefexesActive(vc);
            int usNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
            int gbNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
            int dosNid = SnomedMetadataRfx.getSYNONYMY_REFEX_NID();
            for (RefexChronicleBI refex : refexes) {
                int refexNid = refex.getRefexNid();
                if (refexNid == gbNid || refexNid == usNid || refexNid == dosNid) {
                    componentVersion = (I_AmPart) refex;
                    for (PathBI ep : config.getEditingPathSet()) {
                        componentVersion.makeAnalog(
                                SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                                Long.MAX_VALUE,
                                config.getEditCoordinate().getAuthorNid(),
                                config.getEditCoordinate().getModuleNid(), 
                                ep.getConceptNid());
                    }
                }
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } 
    }

    private void addToRefersToRefset(int nid) {
        try {
            refexConcept = Ts.get().getConcept(SnomedMetadataRfx.getREFERS_TO_REFEX_NID());

            RefexCAB newSpec = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    analogComponent.getNid(),
                    refexConcept.getNid());
            newSpec.put(RefexProperty.CNID1, nid);
            TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            tc.construct(newSpec);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InvalidCAB e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (ContradictionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}
