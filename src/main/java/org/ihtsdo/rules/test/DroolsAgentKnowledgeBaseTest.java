/**
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
package org.ihtsdo.rules.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.testmodel.Concept;
import org.ihtsdo.testmodel.Description;
import org.ihtsdo.testmodel.Relationship;
import org.ihtsdo.testmodel.ResultsCollector;
import org.ihtsdo.testmodel.TransitiveClosureHelperMock;

/**
 * The Class DomainModelTestCheck.
 */
public class DroolsAgentKnowledgeBaseTest extends TestCase {
	
	/** The vodb directory. */
	File vodbDirectory;
	
	/** The read only. */
	boolean readOnly = false;
	
	/** The cache size. */
	Long cacheSize = Long.getLong("600000000");
	
	/** The db setup config. */
	DatabaseSetupConfig dbSetupConfig;
	
	/** The config. */
	I_ConfigAceFrame config;
	
	/** The tf. */
	I_TermFactory tf;
	
	/** The new project concept. */
	I_GetConceptData newProjectConcept;
	
	/** The allowed statuses with retired. */
	I_IntSet allowedStatusesWithRetired;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test state full.
	 */
	public void testStateFull() {
		try {
			System.out.println("Updating knowledge base...");
			RulesLibrary.getKnowledgeBase(RulesLibrary.CONCEPT_MODEL_PKG, 
					"rules/change-set.xml", true);
			System.out.println("Knowledge base updated");
			
			UUID conceptUUID = UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8");
			Concept concept = new Concept(conceptUUID, null, true, null, false);
			Description description = new Description(UUID.randomUUID(), null, true, null, conceptUUID, null, null, 
					"sample  description text with double ++space", false);
			description.setTypeId(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids().iterator().next());
			description.setConceptId(conceptUUID);
			Relationship relationship = new Relationship();
			relationship.setSourceId("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8");
			relationship.setTypeId("f770e2d8-91e6-3c55-91be-f794ee835265");
			relationship.setDestinationId("5032532f-6b58-31f9-84c1-4a365dde4449");
			List<Object> objects = new ArrayList<Object>();
			objects.add(concept);
			objects.add(description);
			description.setId(UUID.randomUUID());
			objects.add(description);
			objects.add(relationship);
			ResultsCollector results = RulesLibrary.checkObjectsTestModel(objects, 
					RulesLibrary.CONCEPT_MODEL_PKG);
			System.out.println("Done...");
			System.out.println("Results size: " + results.getErrorCodes().size());
			
			for (int errorCode : results.getErrorCodes().keySet() ) {
				System.out.println(errorCode + " - " + results.getErrorCodes().get(errorCode));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
