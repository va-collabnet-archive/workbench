package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ComponentFact;
import org.ihtsdo.tk.drools.facts.RelFact;
import org.ihtsdo.tk.drools.facts.RelGroupFact;
import org.ihtsdo.tk.drools.facts.ConceptFact;

public class MoveToRelGroupAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	ComponentVersionBI sourceComponent;
	ComponentVersionBI targetComponent;
	
	public MoveToRelGroupAction(String actionName, RelFact sourceFact, RelGroupFact destFact) {
		super(actionName);
		this.sourceComponent = sourceFact.getComponent();
		this.targetComponent = destFact.getComponent();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			I_GetConceptData concept = Terms.get().getConceptForNid(targetComponent.getNid());
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
			if (ConAttrVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
				throw new UnsupportedOperationException();
			}
			if (ConceptVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
				throw new UnsupportedOperationException();
			}
			if (DescriptionVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
				DescriptionVersionBI desc = (DescriptionVersionBI) sourceComponent;
				Terms.get().newDescription(UUID.randomUUID(), concept, 
						desc.getLang(), desc.getText(), Terms.get().getConcept(desc.getTypeNid()), 
						config, Terms.get().getConcept(desc.getStatusNid()), Long.MAX_VALUE);
			}
			if (RelationshipVersionBI.class.isAssignableFrom(sourceComponent.getClass())) {
				RelationshipVersionBI rel = (RelationshipVersionBI) sourceComponent;
				RelGroupVersionBI relGroup = (RelGroupVersionBI) targetComponent;
				I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), concept, 
						rel.getTypeNid(), 
						rel.getDestinationNid(), 
						rel.getCharacteristicNid(), 
						rel.getRefinabilityNid(), 
						relGroup.getRelGroup(), 
						rel.getStatusNid(), 
						config.getDbConfig().getUserConcept().getNid(), 
						pathItr.next().getConceptNid(), 
						Long.MAX_VALUE);
				
				while (pathItr.hasNext()) {
					newRel.makeAnalog(newRel.getStatusNid(), newRel.getAuthorNid(), 
							pathItr.next().getConceptNid(), Long.MAX_VALUE);
				}
			}
			
			Terms.get().addUncommitted(concept);
			
			
			
			if (I_AmPart.class.isAssignableFrom(sourceComponent.getClass())) {
				I_AmPart componentVersion = (I_AmPart) sourceComponent;
				for (PathBI ep: config.getEditingPathSet()) {
					componentVersion.makeAnalog(
							SnomedMetadataRfx.getRETIRED_NID(), 
							config.getDbConfig().getUserConcept().getNid(),
							ep.getConceptNid(), 
							Long.MAX_VALUE);
				}
				I_GetConceptData retireConcept = Terms.get().getConceptForNid(componentVersion.getNid());
				Terms.get().addUncommitted(retireConcept);
			}
			
			
			
			
			
			
		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}

	}

}