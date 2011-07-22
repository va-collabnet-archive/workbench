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
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.DescSpecFact;
import org.ihtsdo.tk.drools.facts.RelSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.drools.facts.RelGroupFact;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;

public class AddToRelGroupAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	ComponentVersionBI component;
	SpecFact<?> spec;

	public AddToRelGroupAction(String actionName, 
			RelGroupFact relGroup, SpecFact<?> spec) throws IOException { //was concept fact
		super(actionName);
		this.component = relGroup.getRelGroup();//??
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
		Terms.get().newDescription(UUID.randomUUID(),  Terms.get().getConcept(component.getConceptNid()), 
				descSpec.getLangText(), 
				descSpec.getDescText(), 
				Terms.get().getConcept(descSpec.getDescTypeSpec().get(config.getViewCoordinate()).getNid()), 
				config, SnomedMetadataRfx.getCURRENT_NID());
		Terms.get().addUncommitted(Terms.get().getConcept(component.getConceptNid()));
	}
	
	private void addRel() {
		RelSpec relSpec = ((RelSpecFact) spec).getRelSpec();
		try {
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			RelGroupChronicleBI group = (RelGroupChronicleBI) component;
			Iterator<PathBI> pathItr = config.getEditingPathSet().iterator();
			I_GetConceptData originConcept = Terms.get().getConcept(component.getConceptNid());
			I_RelVersioned newRel = Terms.get().newRelationshipNoCheck(UUID.randomUUID(), 
					originConcept, 
					relSpec.getRelTypeSpec().get(config.getViewCoordinate()).getNid(), 
					relSpec.getDestinationSpec().get(config.getViewCoordinate()).getNid(),
					ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid(), 
					ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.localize().getNid(), 
					group.getRelGroup(), //set to relGroup
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