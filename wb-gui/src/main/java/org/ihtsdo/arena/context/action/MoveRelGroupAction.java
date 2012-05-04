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
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.RelGroupFact;
import org.ihtsdo.tk.drools.facts.ConceptFact;

public class MoveRelGroupAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI sourceComponent;
    ComponentVersionBI targetComponent;
    I_ConfigAceFrame config;

    public MoveRelGroupAction(String actionName, RelGroupFact sourceFact, ConceptFact destFact, I_ConfigAceFrame config) {
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
            RelGroupVersionBI relGroup = (RelGroupVersionBI) sourceComponent;
            int sourceGroup = relGroup.getRelGroup();

            //get group numbers in target concept
            ConceptChronicleBI target = (ConceptChronicleBI) targetComponent;
            Collection targetGroups = target.getRelGroups(config.getViewCoordinate());
            int max = 0;
            for (Object groupObject : targetGroups) {
                RelGroupVersionBI rg = (RelGroupVersionBI) groupObject;
                 Collection<? extends RelationshipVersionBI> currentRels = rg.getCurrentRels();
                boolean isStated = false;
                for(RelationshipVersionBI rel : currentRels){
                    if(rel.getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID()){
                        isStated = true;
                        break;
                    }
                }
                if(isStated){
                    int group = rg.getRelGroup();
                    if (group > max) {
                        max = group;
                    }
                }
            }

            int groupNumber = max + 1;

            //get rels with matching sourceGroup from sourceComponent 
            RelGroupVersionBI source = (RelGroupVersionBI) sourceComponent;
            Collection sourceRels = source.getCurrentRels();
            //loop through rels and add to target
            for (Object relObject : sourceRels) {
                Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
                ComponentVersionBI component = (ComponentVersionBI) relObject;

                RelationshipVersionBI rel = (RelationshipVersionBI) component;
                I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), concept,
                        rel.getTypeNid(),
                        rel.getDestinationNid(),
                        rel.getCharacteristicNid(),
                        rel.getRefinabilityNid(),
                        groupNumber, //assign new group number to rels
                        rel.getStatusNid(),
                        config.getDbConfig().getUserConcept().getNid(),
                        pathItr.next().getConceptNid(),
                        Long.MAX_VALUE);

                Terms.get().addUncommitted(concept);

                //retire from source 
                if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                    I_AmPart componentVersion = (I_AmPart) component;
                    for (PathBI ep : config.getEditingPathSet()) {
                        componentVersion.makeAnalog(
                                SnomedMetadataRfx.getSTATUS_RETIRED_NID(),
                                Long.MAX_VALUE,
                                config.getEditCoordinate().getAuthorNid(),
                                config.getEditCoordinate().getModuleNid(), 
                                ep.getConceptNid());
                    }
                    I_GetConceptData retireConcept = Terms.get().getConceptForNid(componentVersion.getNid());
                    Terms.get().addUncommitted(retireConcept);
                }
            }

        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (ContradictionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
}