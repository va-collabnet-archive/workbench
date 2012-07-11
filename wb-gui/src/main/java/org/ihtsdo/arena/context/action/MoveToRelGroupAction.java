package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipAnalogBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.RelFact;
import org.ihtsdo.tk.drools.facts.RelGroupFact;

public class MoveToRelGroupAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI sourceComponent;
    ComponentVersionBI targetComponent;
    I_ConfigAceFrame config;

    public MoveToRelGroupAction(String actionName, RelFact sourceFact, RelGroupFact destFact, I_ConfigAceFrame config) {
        super(actionName);
        this.sourceComponent = sourceFact.getComponent();
        this.targetComponent = destFact.getComponent();
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            I_GetConceptData concept = Terms.get().getConceptForNid(targetComponent.getNid());
            Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
            if (ConceptAttributeVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
                throw new UnsupportedOperationException();
            }
            if (ConceptVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
                throw new UnsupportedOperationException();
            }
            if (DescriptionVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
                throw new UnsupportedOperationException();
            }
            if (sourceComponent.getConceptNid() == targetComponent.getConceptNid()) {
                RelationshipVersionBI rel = (RelationshipVersionBI) sourceComponent;
                RelationshipGroupVersionBI relGroup = (RelationshipGroupVersionBI) targetComponent;
                if(rel.isUncommitted()){
                    RelationshipAnalogBI relAnalog = (RelationshipAnalogBI) rel;
                    relAnalog.setGroup(relGroup.getRelationshipGroupNumber());
                }else{
                    for (PathBI ep : config.getEditingPathSet()) {
                    RelationshipAnalogBI relAnalog = (RelationshipAnalogBI) rel.makeAnalog(
                            SnomedMetadataRfx.getSTATUS_CURRENT_NID(),
                            Long.MAX_VALUE,
                            config.getEditCoordinate().getAuthorNid(),
                            config.getEditCoordinate().getModuleNid(), 
                            ep.getConceptNid());
                    relAnalog.setGroup(relGroup.getRelationshipGroupNumber());
                }
                }
                Terms.get().addUncommitted(concept);
            } else {
                if (RelationshipVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
                    RelationshipVersionBI rel = (RelationshipVersionBI) sourceComponent;
                    RelationshipGroupVersionBI relGroup = (RelationshipGroupVersionBI) targetComponent;
                    I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), concept,
                            rel.getTypeNid(),
                            rel.getTargetNid(),
                            rel.getCharacteristicNid(),
                            rel.getRefinabilityNid(),
                            relGroup.getRelationshipGroupNumber(),
                            rel.getStatusNid(),
                            config.getDbConfig().getUserConcept().getNid(),
                            pathItr.next().getConceptNid(),
                            Long.MAX_VALUE);
                    Terms.get().addUncommitted(concept);
                }
                if (I_AmPart.class.isAssignableFrom(sourceComponent.getClass())) {
                    I_AmPart componentVersion = (I_AmPart) sourceComponent;
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
        } catch (PropertyVetoException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }
}