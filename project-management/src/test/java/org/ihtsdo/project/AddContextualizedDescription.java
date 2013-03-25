/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;

/**
 * The Class AddContextualizedDescription.
 */
public class AddContextualizedDescription implements I_ProcessConcepts {
	
	/** The config. */
	I_ConfigAceFrame config;
	
	/** The language refset concept. */
	I_GetConceptData languageRefsetConcept;
	
	/** The count. */
	int count = 0;

	/**
	 * Instantiates a new adds the contextualized description.
	 *
	 * @param config the config
	 * @param languageRefsetConcept the language refset concept
	 */
	public AddContextualizedDescription(I_ConfigAceFrame config, 
			I_GetConceptData languageRefsetConcept) {
		super();
		this.config = config;
		this.languageRefsetConcept = languageRefsetConcept;
	}


	/* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api.I_GetConceptData)
	 */
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
		loopConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
	}

}
