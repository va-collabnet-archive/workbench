package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.Collection;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.RelGroupFact;
import org.ihtsdo.tk.drools.facts.ConceptFact;

public class CopyRelGroupAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI sourceComponent;
    ComponentVersionBI targetComponent;
    I_ConfigAceFrame config;

    public CopyRelGroupAction(String actionName, RelGroupFact sourceFact, ConceptFact destFact, I_ConfigAceFrame config) {
        super(actionName);
        this.sourceComponent = sourceFact.getComponent();
        this.targetComponent = destFact.getComponent();
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            I_GetConceptData concept = Terms.get().getConceptForNid(targetComponent.getNid());

            //get group number of sourceComponent
            RelationshipGroupVersionBI relGroup = (RelationshipGroupVersionBI) sourceComponent;
            int sourceGroup = relGroup.getRelationshipGroupNumber();

            //get group numbers in target concept
            ConceptChronicleBI target = (ConceptChronicleBI) targetComponent;
            Collection targetGroups = target.getRelationshipOutgoingGroups(config.getViewCoordinate());
            int max = 0;
            for (Object groupObject : targetGroups) {
                RelationshipGroupVersionBI rg = (RelationshipGroupVersionBI) groupObject;
                Collection<? extends RelationshipVersionBI> currentRels = rg.getRelationshipsActive();
                boolean isStated = false;
                for(RelationshipVersionBI rel : currentRels){
                    if(rel.getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID()){
                        isStated = true;
                        break;
                    }
                }
                if(isStated){
                    int group = rg.getRelationshipGroupNumber();
                    if (group > max) {
                        max = group;
                    }
                }
            }

            int groupNumber = max + 1;

            //get rels with matching sourceGroup from sourceComponent 
            RelationshipGroupVersionBI source = (RelationshipGroupVersionBI) sourceComponent;
            Collection sourceRels = source.getRelationshipsActive();
            //loop through rels
            for (Object relObject : sourceRels) {
                Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
                ComponentVersionBI component = (ComponentVersionBI) relObject;

                RelationshipVersionBI rel = (RelationshipVersionBI) component;
                I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), concept,
                        rel.getTypeNid(),
                        rel.getTargetNid(),
                        SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID(),
                        rel.getRefinabilityNid(),
                        groupNumber, //assign new group number to rels
                        rel.getStatusNid(),
                        config.getDbConfig().getUserConcept().getNid(),
                        pathItr.next().getConceptNid(),
                        Long.MAX_VALUE);
            }
            
            Terms.get().addUncommitted(concept);
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (ContradictionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}
