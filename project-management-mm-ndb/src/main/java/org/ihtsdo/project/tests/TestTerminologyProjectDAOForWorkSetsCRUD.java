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
package org.ihtsdo.project.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkSet;

/**
 * The Class TestTerminologyProjectDAOForWorkSetsCRUD.
 */
public class TestTerminologyProjectDAOForWorkSetsCRUD extends TestCase {
	
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
	
	/** The new work set concept. */
	I_GetConceptData newWorkSetConcept;
	
	/** The allowed statuses with retired. */
	I_IntSet allowedStatusesWithRetired;
	
	/** The project. */
	I_TerminologyProject project;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		//TODO get resource with relative path
		vodbDirectory = new File("/Users/alo/Documents/TermMed/Eclipse/MrcmMaven/project-manager/src/main/java/org/dwfa/termmed/projectmanager/tests/berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		LocalVersionedTerminology.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = LocalVersionedTerminology.get();
		try {
		    FileInputStream fin = new FileInputStream(
    			"/Users/alo/Documents/TermMed/Eclipse/MrcmMaven/project-manager/src/main/java/org/dwfa/termmed/projectmanager/tests/config.dat");
		    ObjectInputStream ois = new ObjectInputStream(fin);
		    config = (I_ConfigAceFrame) ois.readObject();
		    ois.close();
		    }
		catch (Exception e) { e.printStackTrace(); }
		tf.setActiveAceFrameConfig(config);
		allowedStatusesWithRetired =  tf.newIntSet();
		allowedStatusesWithRetired.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids()));
		allowedStatusesWithRetired.addAll(config.getAllowedStatus().getSetValues());
		
		I_GetConceptData projectConcept = null;
		try {
			projectConcept = tf.getConcept(new UUID[] {UUID.fromString("3efb77c9-1369-3728-a001-faa7ac668efd")});
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		project = TerminologyProjectDAO.getTranslationProject(projectConcept, config);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Test get all work sets.
	 */
	public void testGetAllWorkSets() {
		//List<WorkSet> workSets = TerminologyProjectDAO.getAllWorkSetsForProject(project, config);
		//assertEquals(0, workSets.size());
		assert(true);
	}

	/**
	 * Test create new project.
	 */
	public void testCreateNewProject() {
		String name = "Test WorkSet " + UUID.randomUUID().toString();
		String description = "Test Metadata";
		Calendar cal = Calendar.getInstance();
		waiting(1);
		
		//workSet.setDescription("Modified Description");
		//I_TerminologyProject modifiedProject = TerminologyProjectDAO.updateProjectMetadata(workSet, config);
		//assertEquals("Modified Description", modifiedProject.getDescription());
		
		
	}
	
	/**
	 * Test get all work sets2.
	 */
	public void testGetAllWorkSets2() {
		List<WorkSet> workSets = TerminologyProjectDAO.getAllWorkSetsForProject(project, config);
		//assertEquals(1, workSets.size());
	}

	/**
	 * Test retire project.
	 */
	public void testRetireProject() {
		List<WorkSet> workSets = TerminologyProjectDAO.getAllWorkSetsForProject(project, config);
		for (WorkSet workSet : workSets) {
			System.out.println("||" + workSet.getName().substring(0, 11) + "||");
			if (workSet.getName().substring(0, 12).equals("Test WorkSet")) {
				try {
					I_GetConceptData conceptToRetireUpdatedFromDB = tf.getConcept(workSet.getUids());
					TerminologyProjectDAO.retireConcept(conceptToRetireUpdatedFromDB, config);
					I_GetConceptData retiredConcept = tf.getConcept(workSet.getUids());
					assertEquals(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
							retiredConcept.getConceptAttributeTuples(allowedStatusesWithRetired, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next().getStatusId());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TerminologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Test get all work sets3.
	 */
	public void testGetAllWorkSets3() {
		List<WorkSet> workSets = TerminologyProjectDAO.getAllWorkSetsForProject(project, config);
		assertEquals(0, workSets.size());
	}
	
	/**
	 * Waiting.
	 * 
	 * @param n the n
	 */
	public static void waiting(int n){
        
        long t0, t1;
        t0 =  System.currentTimeMillis();
        do{
            t1 = System.currentTimeMillis();
        }
        while ((t1 - t0) < (n * 1000));
    }
	
}
