package org.ihtsdo.project;

import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.ContextualizedDescription;

public class AddContextualizedDescription implements I_ProcessConcepts {
	I_ConfigAceFrame config;
	I_GetConceptData languageRefsetConcept;
	int count = 0;

	public AddContextualizedDescription(I_ConfigAceFrame config, 
			I_GetConceptData languageRefsetConcept) {
		super();
		this.config = config;
		this.languageRefsetConcept = languageRefsetConcept;
	}


	@Override
	public void processConcept(I_GetConceptData loopConcept) throws Exception {
		count++;
		//System.out.println(loopConcept.toString());
		I_ContextualizeDescription newDescription = ContextualizedDescription.createNewContextualizedDescription(
				loopConcept.getConceptNid(), languageRefsetConcept.getConceptNid(), "es");
		newDescription.setText(UUID.randomUUID().toString());
		newDescription.persistChangesNoChecks();
//		I_ContextualizeDescription newDescription2 = ContextualizedDescription.createNewContextualizedDescription(
//				loopConcept.getConceptNid(), languageRefsetConcept.getConceptNid(), "es");
//		newDescription2.setText(UUID.randomUUID().toString());
//		I_ContextualizeDescription newDescription3 = ContextualizedDescription.createNewContextualizedDescription(
//				loopConcept.getConceptNid(), languageRefsetConcept.getConceptNid(), "es");
//		newDescription3.setText(UUID.randomUUID().toString());
//		I_ContextualizeDescription newDescription4 = ContextualizedDescription.createNewContextualizedDescription(
//				loopConcept.getConceptNid(), languageRefsetConcept.getConceptNid(), "es");
//		newDescription4.setText(UUID.randomUUID().toString());
//		I_ContextualizeDescription newDescription5 = ContextualizedDescription.createNewContextualizedDescription(
//				loopConcept.getConceptNid(), languageRefsetConcept.getConceptNid(), "es");
//		newDescription5.setText(UUID.randomUUID().toString());
//		I_ContextualizeDescription newDescription6 = ContextualizedDescription.createNewContextualizedDescription(
//				loopConcept.getConceptNid(), languageRefsetConcept.getConceptNid(), "es");
//		newDescription6.setText(UUID.randomUUID().toString());
		Terms.get().addUncommittedNoChecks(loopConcept);
		System.out.println(count + " - " + loopConcept);
		Terms.get().commit();
	}

}
