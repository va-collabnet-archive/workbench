package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ComponentFact;

public class RetireAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    ComponentVersionBI analog;
    I_ConfigAceFrame config;

    public RetireAction(String actionName, ComponentFact<ComponentVersionBI> fact, I_ConfigAceFrame config) {
        super(actionName);
        this.component = fact.getComponent();
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (config.getEditingPathSet().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Editing path set is empty.", "Error", JOptionPane.ERROR_MESSAGE);;
            } else if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                I_AmPart componentVersion = (I_AmPart) component;
                for (PathBI ep : config.getEditingPathSet()) {
                    analog = (ComponentVersionBI) componentVersion.makeAnalog(
                            SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                            Long.MAX_VALUE,
                            config.getEditCoordinate().getAuthorNid(),
                            config.getEditCoordinate().getModuleNid(), 
                            ep.getConceptNid());
                }

                if (DescriptionVersionBI.class.isAssignableFrom(component.getClass())) {
                    retireFromRefexes(component);
                }
                if (RelationshipVersionBI.class.isAssignableFrom(component.getClass())) {
                    RelationshipAnalogBI ra = (RelationshipAnalogBI) analog;
                    RelationshipVersionBI rv = (RelationshipVersionBI) analog;
                    ra.setGroup(rv.getGroup());
                }
                I_GetConceptData concept = Terms.get().getConceptForNid(analog.getNid());
                Terms.get().addUncommitted(concept);
            }
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (PropertyVetoException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    private void retireFromRefexes(ComponentVersionBI component) {
        DescriptionVersionBI desc = (DescriptionVersionBI) component;
        try {
            I_AmPart componentVersion;
            ViewCoordinate vc = config.getViewCoordinate();
            Collection<? extends RefexChronicleBI> refexes = desc.getRefexesActive(vc);
            for (RefexChronicleBI refex : refexes) {
                int refexNid = refex.getRefexNid();
                componentVersion = (I_AmPart) refex;
                for (PathBI ep : config.getEditingPathSet()) {
                    componentVersion.makeAnalog(
                            SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                            Long.MAX_VALUE,
                            config.getEditCoordinate().getAuthorNid(),
                            config.getEditCoordinate().getModuleNid(), 
                            ep.getConceptNid());
                }
                I_GetConceptData concept = Terms.get().getConceptForNid(analog.getNid());
                Terms.get().addUncommitted(concept);
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
}
