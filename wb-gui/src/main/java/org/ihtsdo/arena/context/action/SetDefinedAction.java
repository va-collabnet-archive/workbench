package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.conattr.ConAttrAnalogBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ComponentFact;

public class SetDefinedAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    AnalogBI analog;
    I_ConceptAttributePart newAnalogAttr;
    I_ConfigAceFrame config;

    public SetDefinedAction(String actionName, ComponentFact<ConAttrVersionBI> fact, I_ConfigAceFrame config) {
        super(actionName);
        this.component = fact.getComponent();
        this.config = config;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                I_AmPart part = (I_AmPart) component;
                I_ConceptAttributePart cPart = (I_ConceptAttributePart) part;

                if (cPart.isDefined() == true) {
                    if (component.getChronicle().isUncommitted()) {
                        ConAttrAnalogBI analog = (ConAttrAnalogBI) component;
                        analog.setDefined(false);
                    } else {
                        //make analog
                        for (PathBI ep : config.getEditingPathSet()) {
                            AnalogBI newAnalog = part.makeAnalog(
                                    part.getChronicle().getPrimordialVersion().getStatusNid(),
                                    Long.MAX_VALUE,
                                    config.getEditCoordinate().getAuthorNid(),
                                    config.getEditCoordinate().getModuleNid(), 
                                    ep.getConceptNid());

                            newAnalogAttr = (I_ConceptAttributePart) newAnalog;
                            newAnalogAttr.setDefined(false);
                        }
                    }
                    I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());
                    Terms.get().addUncommitted(concept);
                } else {
                    if (component.getChronicle().isUncommitted()) {
                        ConAttrAnalogBI analog = (ConAttrAnalogBI) component;
                        analog.setDefined(true);
                    } else {
                        //make analog
                        for (PathBI ep : config.getEditingPathSet()) {
                            AnalogBI newAnalog = part.makeAnalog(
                                    SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                                    Long.MAX_VALUE,
                                    config.getEditCoordinate().getAuthorNid(),
                                    config.getEditCoordinate().getModuleNid(), 
                                    ep.getConceptNid());

                            newAnalogAttr = (I_ConceptAttributePart) newAnalog;
                            newAnalogAttr.setDefined(true);
                        }
                    }
                    I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());
                    Terms.get().addUncommitted(concept);
                }
            }
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}
