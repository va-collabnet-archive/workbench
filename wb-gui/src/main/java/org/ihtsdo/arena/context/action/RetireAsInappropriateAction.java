package org.ihtsdo.arena.context.action;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.Refsets;
import org.ihtsdo.arena.WizardPanel;
import org.ihtsdo.arena.conceptview.ConceptViewRenderer;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.swing.wizard.WizardBI;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.util.swing.GuiUtil;

public class RetireAsInappropriateAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    AnalogBI analog;
    I_ConfigAceFrame config;
    TerminologyList tl;
    WizardPanel wizard;
    ConceptViewSettings settings;

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
                    retireConcept();
                    addToRefersToRefset(nid);
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

    private void retireConcept() {
        try {
            if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                I_AmPart componentVersion = (I_AmPart) component;
                for (PathBI ep : config.getEditingPathSet()) {
                    analog = componentVersion.makeAnalog(
                            ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid(),
                            config.getDbConfig().getUserConcept().getNid(),
                            ep.getConceptNid(),
                            Long.MAX_VALUE);
                }
                ComponentVersionBI newComponent = (ComponentVersionBI) analog;

                I_GetConceptData concept = Terms.get().getConceptForNid(newComponent.getNid());
                Terms.get().addUncommitted(concept);
            }
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    private void addToRefersToRefset(int nid) {
        try {
            RefexCAB newSpec = new RefexCAB(
                    TK_REFSET_TYPE.CID,
                    component.getNid(),
                    Ts.get().getNidForUuids(Refsets.REFERS_TO.getLenient().getPrimUuid()));
            newSpec.put(RefexProperty.CNID1, nid);
            TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                    config.getViewCoordinate());
            tc.construct(newSpec);
            ConceptChronicleBI refexConcept = Ts.get().getConcept(Refsets.REFERS_TO.getLenient().getNid());
            if (!refexConcept.isAnnotationStyleRefex()) {
                Ts.get().addUncommitted(refexConcept);
            }
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InvalidCAB e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}
