package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.RelGroupFact;

public class UngroupRelGroupAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI sourceComponent;
    I_ConfigAceFrame config;

    public UngroupRelGroupAction(String actionName, RelGroupFact sourceFact, ConceptFact destFact, I_ConfigAceFrame config) {
        super(actionName);
        this.sourceComponent = sourceFact.getComponent();
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            //get rels from sourceComponent 
            RelationshipGroupVersionBI source = (RelationshipGroupVersionBI) sourceComponent;
            Collection<?> sourceRels = source.getRelationshipsActive();
            I_GetConceptData concept = Terms.get().getConceptForNid(source.getConceptNid());
            //loop through rels
            for (Object relObject : sourceRels) {
                if (I_AmPart.class.isAssignableFrom(relObject.getClass())) {
                    I_AmPart componentVersion = (I_AmPart) relObject;
                    for (PathBI ep : config.getEditingPathSet()) {
                        I_AmPart part = (I_AmPart) componentVersion.makeAnalog(
                                SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                                Long.MAX_VALUE,
                                config.getEditCoordinate().getAuthorNid(),
                                config.getEditCoordinate().getModuleNid(), 
                                ep.getConceptNid());


                        I_RelPart rel = (I_RelPart) part;
                        rel.setGroup(0);
                    }
                }
            }
            Terms.get().addUncommitted(concept);
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}
