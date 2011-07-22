package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.DescSpecFact;
import org.ihtsdo.tk.drools.facts.RelSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;

public class AddFromSpecAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	ConceptVersionBI concept;
	SpecFact<?> spec;

	public AddFromSpecAction(String actionName, 
			ConceptFact concept, SpecFact<?> spec) throws IOException {
		super(actionName);
		this.concept = concept.getConcept();
		this.spec = spec;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (RelSpecFact.class.isAssignableFrom(spec.getClass())) {
				addRel();
			} else if (DescSpecFact.class.isAssignableFrom(spec.getClass())) {
				addDesc();
			} else {
				throw new Exception("Can't handle type: " + spec);
			}
		} catch (Exception ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
	}

	private void addDesc() throws TerminologyException, IOException {
		DescriptionSpec descSpec = ((DescSpecFact) spec).getDescSpec();
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		Terms.get().newDescription(UUID.randomUUID(),  Terms.get().getConcept(concept.getNid()), 
				descSpec.getLangText(), 
				descSpec.getDescText(), 
				Terms.get().getConcept(descSpec.getDescTypeSpec().get(concept.getViewCoordinate()).getNid()), 
				config, SnomedMetadataRfx.getCURRENT_NID());
		Terms.get().addUncommitted(Terms.get().getConcept(concept.getNid()));
	}
	
	private void addRel() {
		RelSpec relSpec = ((RelSpecFact) spec).getRelSpec();
		try {
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			
			Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
			I_GetConceptData originConcept = Terms.get().getConcept(concept.getNid());
			I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), 
					originConcept, 
					relSpec.getRelTypeSpec().get(concept.getViewCoordinate()).getNid(), 
					relSpec.getDestinationSpec().get(concept.getViewCoordinate()).getNid(),
					ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid(), 
					ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.localize().getNid(), 
					0, 
					SnomedMetadataRfx.getCURRENT_NID(), 
					config.getDbConfig().getUserConcept().getNid(),
					pathItr.next().getConceptNid(), 
		            Long.MAX_VALUE);
			
			while (pathItr.hasNext()) {
				newRel.makeAnalog(newRel.getStatusNid(), newRel.getAuthorNid(), pathItr.next().getConceptNid(), Long.MAX_VALUE);
			}
			Terms.get().addUncommitted(originConcept);

		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}

}