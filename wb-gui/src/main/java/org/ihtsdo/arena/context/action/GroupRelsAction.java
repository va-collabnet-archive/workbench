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
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.drools.facts.RelFact;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.cement.ArchitectonicAuxiliary;



public class GroupRelsAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		RelationshipVersionBI sourceComponent;
		ComponentVersionBI targetComponent;
		
		public GroupRelsAction(String actionName, RelFact sourceFact, RelFact destFact) {
			super(actionName);
			this.sourceComponent = sourceFact.getComponent();
			this.targetComponent = destFact.getComponent();
		}

	
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				I_GetConceptData concept = Terms.get().getConceptForNid(targetComponent.getNid());
				I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		
				//get group numbers in target concept
				ConceptChronicleBI target = (ConceptChronicleBI) concept; 
				Collection targetGroups = target.getRelGroups(config.getViewCoordinate());
				int max = 0;
				for (Object groupObject : targetGroups) {
					RelGroupVersionBI rg = (RelGroupVersionBI) groupObject;
					int group = rg.getRelGroup();
					if (group > max){
						 max = group;
					 }
				}
				
				int groupNumber = max + 1;
				Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
				int pathNid = pathItr.next().getConceptNid();
				
				RelationshipVersionBI targetRel = (RelationshipVersionBI) targetComponent;
				I_RelVersioned newTargetRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), concept, 
						targetRel.getTypeNid(), 
						targetRel.getDestinationNid(), 
						targetRel.getCharacteristicNid(), 
						targetRel.getRefinabilityNid(), 
						groupNumber, 		//assign new group number to rels
						targetRel.getStatusNid(), 
						config.getDbConfig().getUserConcept().getNid(), 
						pathNid, 
						Long.MAX_VALUE);
				
				while (pathItr.hasNext()) {
					newTargetRel.makeAnalog(newTargetRel.getStatusNid(), newTargetRel.getAuthorNid(), 
							pathItr.next().getConceptNid(), Long.MAX_VALUE);
				}
				
				RelationshipVersionBI sourceRel = (RelationshipVersionBI) sourceComponent;
				I_RelVersioned newSourceRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), concept, 
						sourceRel.getTypeNid(), 
						sourceRel.getDestinationNid(), 
						sourceRel.getCharacteristicNid(), 
						sourceRel.getRefinabilityNid(), 
						groupNumber, 		//assign new group number to rels
						sourceRel.getStatusNid(), 
						config.getDbConfig().getUserConcept().getNid(), 
						pathNid, 
						Long.MAX_VALUE);
				
				while (pathItr.hasNext()) {
					newTargetRel.makeAnalog(newSourceRel.getStatusNid(), newSourceRel.getAuthorNid(), 
							pathItr.next().getConceptNid(), Long.MAX_VALUE);
				}
				
				Terms.get().addUncommitted(concept);
				
				if (I_AmPart.class.isAssignableFrom(targetComponent.getClass())) {
					I_AmPart componentVersionTarget = (I_AmPart) targetComponent;
					for (PathBI ep: config.getEditingPathSet()) {
						componentVersionTarget.makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(), 
								config.getDbConfig().getUserConcept().getNid(),
								ep.getConceptNid(), 
								Long.MAX_VALUE);
					}
					I_GetConceptData retireConceptTarget = Terms.get().getConceptForNid(componentVersionTarget.getNid());
					Terms.get().addUncommitted(retireConceptTarget);
				}
				
				if (I_AmPart.class.isAssignableFrom(sourceComponent.getClass())) {
					I_AmPart componentVersionSource = (I_AmPart) sourceComponent;
					for (PathBI ep: config.getEditingPathSet()) {
						componentVersionSource.makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(), 
								config.getDbConfig().getUserConcept().getNid(),
								ep.getConceptNid(), 
								Long.MAX_VALUE);
					}
					I_GetConceptData retireConceptSource = Terms.get().getConceptForNid(componentVersionSource.getNid());
					Terms.get().addUncommitted(retireConceptSource);
				}
					 
			} catch (TerminologyException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			} catch (IOException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			} catch (ContraditionException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}
		}
	}