package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.drools.facts.RelFact;

import org.dwfa.ace.api.I_RelPart;


import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

public class UngroupRelAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    RelationshipVersionBI component;
    I_ConfigAceFrame config;

    public UngroupRelAction(String actionName, RelFact fact, I_ConfigAceFrame config) {
        super(actionName);
        this.component = fact.getComponent();
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                I_AmPart componentVersion = (I_AmPart) component;
                for (PathBI ep : config.getEditingPathSet()) {
                    I_AmPart part = (I_AmPart) componentVersion.makeAnalog(
                            SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                            Long.MAX_VALUE,
                            config.getEditCoordinate().getAuthorNid(),
                            config.getEditCoordinate().getModuleNid(), 
                            ep.getConceptNid());


                    I_RelPart rel = (I_RelPart) part;
                    rel.setGroup(0);

                    I_GetConceptData concept = Terms.get().getConceptForNid(rel.getNid());

                    Terms.get().addUncommitted(concept);
                }
            }

        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}
