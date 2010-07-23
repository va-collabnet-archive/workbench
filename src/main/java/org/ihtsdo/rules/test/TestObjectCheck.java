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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.testmodel.Concept;
import org.ihtsdo.testmodel.Description;

/**
 * The Class DomainModelTestCheck.
 */
public class TestObjectCheck extends TestCase {
	
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
			HashMap<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
			resources.put( ResourceFactory.newFileResource("rules/sample-descriptions-rules-for-objects.drl"), ResourceType.DRL );
			RulesLibrary.getKnowledgeBase(RulesLibrary.CONCEPT_MODEL_PKG, true, resources);
			System.out.println("Knowledge base updated");
			UUID conceptUUID = UUID.randomUUID();
			Concept concept = new Concept(conceptUUID, null, true, null, false);
			Description description = new Description(UUID.randomUUID(), null, true, null, conceptUUID, null, null, 
					"sample  description text with double space", false);
			List<Object> objects = new ArrayList<Object>();
			objects.add(concept);
			objects.add(description);
			ResultsCollectorWorkBench results = RulesLibrary.checkObjects(objects, 
					RulesLibrary.CONCEPT_MODEL_PKG);
			System.out.println("Done...");
			System.out.println("Results size: " + results.getErrorCodes().size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
