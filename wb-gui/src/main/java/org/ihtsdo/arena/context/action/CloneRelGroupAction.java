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
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.tk.drools.facts.ComponentFact;
import org.ihtsdo.tk.drools.facts.RelFact;
import org.ihtsdo.tk.drools.facts.RelGroupFact;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_RelPart;
import org.ihtsdo.tk.api.PositionBI;
import org.dwfa.cement.ArchitectonicAuxiliary;



public class CloneRelGroupAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		ComponentVersionBI sourceComponent;
		ComponentVersionBI targetComponent;
		
		public CloneRelGroupAction(String actionName, RelGroupFact sourceFact, ConceptFact destFact) {
			super(actionName);
			this.sourceComponent = sourceFact.getComponent();
			this.targetComponent = destFact.getComponent();
		}

	
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				I_GetConceptData concept = Terms.get().getConceptForNid(targetComponent.getNid());
				I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		
				//get group number of sourceComponent
				RelGroupVersionBI relGroup = (RelGroupVersionBI) sourceComponent;
				int sourceGroup = relGroup.getRelGroup();
				
				//get group numbers in target concept
				ConceptChronicleBI target = (ConceptChronicleBI) targetComponent; 
				Collection targetGroups = target.getRelGroups();
				int max = 0;
				for (Object groupObject : targetGroups) {
					RelGroupVersionBI rg = (RelGroupVersionBI) groupObject;
					int group = rg.getRelGroup();
					if (group > max){
						 max = group;
					 }
				}
				
				int groupNumber = max + 1;
				
				//get rels with matching sourceGroup from sourceComponent 
				RelGroupVersionBI source = (RelGroupVersionBI) sourceComponent; 
				Collection sourceRels = source.getCurrentRels(); //form getRels
				//loop through rels
				 for (Object relObject: sourceRels) {
					 Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
					 ComponentVersionBI component = (ComponentVersionBI) relObject; 
					 
					 
								RelationshipVersionBI rel = (RelationshipVersionBI) component;
	 							I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), concept, 
										rel.getTypeNid(), 
										rel.getDestinationNid(), 
										rel.getCharacteristicNid(), 
										rel.getRefinabilityNid(), 
										groupNumber, 		//assign new group number to rels
										rel.getStatusNid(), 
										config.getDbConfig().getUserConcept().getNid(), 
										pathItr.next().getConceptNid(), 
										Long.MAX_VALUE);
								
								while (pathItr.hasNext()) {
									newRel.makeAnalog(newRel.getStatusNid(), newRel.getAuthorNid(), 
											pathItr.next().getConceptNid(), Long.MAX_VALUE);
								}
							Terms.get().addUncommitted(concept);
						 
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