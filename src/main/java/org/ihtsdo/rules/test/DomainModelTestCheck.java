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
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;

/**
 * The Class DomainModelTestCheck.
 */
public class DomainModelTestCheck extends TestCase {
	
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
		vodbDirectory = new File("/Users/alo/Desktop/berkeley-db");
		
		dbSetupConfig = new DatabaseSetupConfig();
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	/**
	 * Test state full.
	 */
	public void testStateFull() {
		try {
			I_GetConceptData concept = tf.getConcept(UUID.fromString("5ee78031-c76d-3b01-8df7-3d5243ba7876"));
			System.out.println("Concept: " + concept);
			System.out.println("Updating knowledge base...");
			HashMap<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
			resources.put( ResourceFactory.newFileResource("rules/sample-descriptions-rules.drl"), ResourceType.DRL );
			//resources.put( ResourceFactory.newFileResource("rules/sample-relationships-rules.drl"), ResourceType.DRL );
			RulesLibrary.getKnowledgeBase(RulesLibrary.CONCEPT_MODEL_PKG, true, resources);
			System.out.println("Knowledge base updated");
			ResultsCollectorWorkBench resultsCollector = RulesLibrary.checkConcept(concept, RulesLibrary.CONCEPT_MODEL_PKG,false);
			List<AlertToDataConstraintFailure> results = resultsCollector.getAlertList();
			System.out.println("Done..." + results.size());
			System.out.println("Results size: " + results.size());
			for (AlertToDataConstraintFailure alert : results) {
				System.out.println(alert.getAlertMessage());
			}
			resources = new HashMap<Resource, ResourceType>();
			resources.put( ResourceFactory.newFileResource("rules/sample-guidelines-rules.drl"), ResourceType.DRL );
			//resources.put( ResourceFactory.newFileResource("rules/sample-relationships-rules.drl"), ResourceType.DRL );
			RulesLibrary.getKnowledgeBase(RulesLibrary.LINGUISTIC_GUIDELINES_PKG, true, resources);
			System.out.println("Knowledge base updated");
			resultsCollector = RulesLibrary.checkConcept(concept, RulesLibrary.LINGUISTIC_GUIDELINES_PKG,false);
			results = resultsCollector.getAlertList();
			System.out.println("Done..." + results.size());
			System.out.println("Results size: " + results.size());
			for (AlertToDataConstraintFailure alert : results) {
				System.out.println(alert.getAlertMessage());
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			config = tf.newAceFrameConfig();
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}), 
					Integer.MAX_VALUE));
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}), 
					Integer.MAX_VALUE));
			config.addEditingPath(tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			
			BdbTermFactory tf2 = (BdbTermFactory) tf;
			I_ConfigAceDb newDbProfile = tf2.newAceDbConfig();
	        newDbProfile.setUsername("username");
	        newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
	        newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
	        newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
	        newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
	        config.setDbConfig(newDbProfile);
			
			config.setPrecedence(PRECEDENCE.TIME);
	        
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return config;
	}

}
