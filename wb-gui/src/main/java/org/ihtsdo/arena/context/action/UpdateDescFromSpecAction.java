package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.Collection;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.ContraditionException;//THIS
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.DescSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.drools.facts.RelSpecFact;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;

public class UpdateDescFromSpecAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	ConceptVersionBI component;
	SpecFact<?> spec;

	public UpdateDescFromSpecAction(String actionName, 
			ConceptFact fact, SpecFact<?> spec) throws IOException {
		super(actionName);
		this.component = fact.getComponent();
		this.spec = spec;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (DescSpecFact.class.isAssignableFrom(spec.getClass())) {
				updateDesc();
			} else {
				throw new Exception("Can't handle type: " + spec);
			}
		} catch (Exception ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
	}

	private void updateDesc() throws TerminologyException, IOException, ContraditionException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_GetConceptData concept = Terms.get().getConceptForNid(component.getNid());
		
		Collection descriptions = component.getDescsActive();
		
		for (Object descObject : descriptions) {
			DescriptionVersionBI desc = (DescriptionVersionBI) descObject;
			DescriptionSpec descSpec = ((DescSpecFact) spec).getDescSpec(); 
			
			if (desc.getTypeNid() == descSpec.getDescTypeSpec().get(component.getViewCoordinate()).getNid() && !(desc.getText().equals(descSpec.getDescText()))) { //if desc type is equal and text has changed, retire and make new
				
				//description
				if (DescSpecFact.class.isAssignableFrom(spec.getClass())) {
					
					Terms.get().newDescription(UUID.randomUUID(),  Terms.get().getConcept(concept.getNid()), 
							descSpec.getLangText(), 
							descSpec.getDescText(), 
							Terms.get().getConcept(descSpec.getDescTypeSpec().get(component.getViewCoordinate()).getNid()), 
							config, ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
					Terms.get().addUncommitted(Terms.get().getConcept(concept.getNid()));
				}
				
				//concept
				if (RelSpecFact.class.isAssignableFrom(spec.getClass())) {
					RelSpec relSpec = ((RelSpecFact) spec).getRelSpec();
					Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
					I_GetConceptData originConcept = Terms.get().getConcept(concept.getNid());
					I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), 
							originConcept, 
							relSpec.getRelTypeSpec().get(component.getViewCoordinate()).getNid(), 
							relSpec.getDestinationSpec().get(component.getViewCoordinate()).getNid(),
							ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid(), 
							ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.localize().getNid(), 
							0, 
							ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), 
							config.getDbConfig().getUserConcept().getNid(),
							pathItr.next().getConceptNid(), 
				            Long.MAX_VALUE);
					
					while (pathItr.hasNext()) {
						newRel.makeAnalog(newRel.getStatusNid(), newRel.getAuthorNid(), pathItr.next().getConceptNid(), Long.MAX_VALUE);
					}
					Terms.get().addUncommitted(originConcept);
				}
			
			
			
				if (I_AmPart.class.isAssignableFrom(desc.getClass())) {
					I_AmPart componentVersion = (I_AmPart) desc;
					for (PathBI ep: config.getEditingPathSet()) {
						componentVersion.makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(), 
								config.getDbConfig().getUserConcept().getNid(),
								ep.getConceptNid(), 
								Long.MAX_VALUE);
					}
					I_GetConceptData retireConcept = Terms.get().getConceptForNid(componentVersion.getNid());
					Terms.get().addUncommitted(retireConcept);
				}
			}
			
			else{ //other: make analog and update
				I_DescriptionVersioned<?> description = Terms.get().getDescription(Terms.get().uuidToNative(descSpec.getUuids()));
				I_DescriptionPart descPart = description.getTuples(config.getConflictResolutionStrategy()).iterator().next().getMutablePart();
				I_DescriptionPart newPart = (I_DescriptionPart) descPart.makeAnalog(
						ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), 
						config.getEditingPathSet().iterator().next().getConceptNid(), 
						Long.MAX_VALUE);
				newPart.setText(descSpec.getDescText());
				description.addVersion(newPart);
			
			}
			Terms.get().addUncommitted(Terms.get().getConcept(concept.getNid()));
		}	
	}

}