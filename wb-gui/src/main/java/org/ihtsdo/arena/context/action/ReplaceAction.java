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
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.drools.facts.ComponentFact;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.RelFact;
import org.ihtsdo.tk.drools.facts.RelSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.spec.RelSpec;

public class ReplaceAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	ComponentVersionBI component;
	int conceptNid; 
	SpecFact<?> spec;
	ConceptVersionBI concept;
	
	public ReplaceAction(String actionName, RelFact fact, ConceptVersionBI concept, SpecFact<?> spec){
		super(actionName);
		this.component = fact.getComponent();
		this.spec = spec;
		this.concept = concept;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try{
		if (RelSpecFact.class.isAssignableFrom(spec.getClass())) {
			addRel();
		} else if (I_AmPart.class.isAssignableFrom(component.getClass())) {
			retireRel();
		} else {
			throw new Exception("Can't handle type: " + spec);
			}
		}catch (Exception ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
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
					ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),  //TODO should be retired?
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
	
	private void retireRel(){
		try {
			I_AmPart componentVersion = (I_AmPart) component;
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			for (PathBI ep: config.getEditingPathSet()) {
				componentVersion.makeAnalog(
						ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(), 
						config.getDbConfig().getUserConcept().getNid(),
						ep.getConceptNid(), 
						Long.MAX_VALUE);
				}
			I_GetConceptData concept = Terms.get().getConceptForNid(componentVersion.getNid());
			Terms.get().addUncommitted(concept);
		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}

	}

}
