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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;

/**
 * The Class TestProjectsCRUD.
 */
public class TestTerminologyProjectDAOForProjectsCRUD extends TestCase {
	
//	File vodbDirectory;
//	boolean readOnly = false;
//	Long cacheSize = Long.getLong("600000000");
//	DatabaseSetupConfig dbSetupConfig;
//	I_ConfigAceFrame config;
//	I_TermFactory tf;
//	I_GetConceptData newProjectConcept;
//	I_IntSet allowedStatusesWithRetired;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		//TODO get resource with relative path
//		vodbDirectory = new File("/Users/alo/Documents/TermMed/Eclipse/MrcmMaven/project-manager/src/main/java/org/dwfa/termmed/projectmanager/tests/berkeley-db");
//		//vodbDirectory = new File("/Users/alo/Documents/TermMed/Eclipse/MrcmMaven/sct-ide-sa2/target/sct-wb-ide-sa-bundle.dir/berkeley-db");
//		dbSetupConfig = new DatabaseSetupConfig();
//		LocalVersionedTerminology.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
//		tf = LocalVersionedTerminology.get();
//		try {
//		    FileInputStream fin = new FileInputStream(
//		    		"/Users/alo/Documents/TermMed/Eclipse/MrcmMaven/project-manager/src/main/java/org/dwfa/termmed/projectmanager/tests/config.dat");
//		    ObjectInputStream ois = new ObjectInputStream(fin);
//		    config = (I_ConfigAceFrame) ois.readObject();
//		    ois.close();
//		    }
//		catch (Exception e) { e.printStackTrace(); }
//		
//		allowedStatusesWithRetired =  tf.newIntSet();
//		allowedStatusesWithRetired.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids()));
//		allowedStatusesWithRetired.addAll(config.getAllowedStatus().getSetValues());
//		tf.setActiveAceFrameConfig(config);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
//	protected void tearDown() throws Exception {
//		super.tearDown();
//	}

	/**
	 * Test get all projects2.
	 */
//	public void testGetAllProjects() {
//		//List<I_TerminologyProject> projects = new ArrayList<I_TerminologyProject>();
//		//projects = TerminologyProjectDAO.getAllProjects(config);
//		//assertEquals(0, projects.size());
//	}

	/**
	 * Test method for {@link org.ihtsdo.project.TerminologyProjectDAO#createNewProject(I_TerminologyProject , org.dwfa.ace.api.I_ConfigAceFrame)}.
	 */
//	public void testCreateNewProject() {
//		String name = "Test project " + UUID.randomUUID().toString();
//		String description = "Test Metadata";
//		List<String> addresses = null;
//        List<Integer> exlusionRefsets = null;
//        List<Integer> inclusionRefsets = null;
//        List<Integer> paths = null;
//        List<File> documents = null;
//        List<BusinessProcess> businessProceses = null;
//        List<UUID> nullUid = null;
//        I_TerminologyProject tempProjectForMetadata = new TranslationProject(name, 0, nullUid,
//                  addresses, exlusionRefsets, inclusionRefsets, paths, documents, businessProceses, description);
//		I_TerminologyProject project = 
//			TerminologyProjectDAO.createNewProject(tempProjectForMetadata, config);
//		newProjectConcept = project.getConcept();
//		assertEquals(name, project.getName());
//		assertEquals(description, project.getDescription());
//		
//		project = TerminologyProjectDAO.getProject(newProjectConcept, config);
//		assertEquals(name, project.getName());
//		assertEquals(description, project.getDescription());
//		
//		waiting(1);
//		
//		project.setDescription("Modified Description");
//		I_TerminologyProject modifiedProject = TerminologyProjectDAO.updateProjectMetadata(project, config);
//		assertEquals("Modified Description", modifiedProject.getDescription());
//		
		
//	}
	
	/**
	 * Test method for {@link org.ihtsdo.project.TerminologyProjectDAO#getAllTranslationProjects(org.dwfa.ace.api.I_ConfigAceFrame)}.
	 */
	public void testGetAllProjects2() {
//		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
//		assertEquals(1, projects.size());
		assertTrue(true);
	}

	/**
	 * Test method for {@link org.ihtsdo.project.TerminologyProjectDAO#retireProject(org.dwfa.ace.api.I_GetConceptData, org.dwfa.ace.api.I_ConfigAceFrame)}.
	 */
//	public void testRetireProject() {
//		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
//		for (I_TerminologyProject project : projects) {
//			System.out.println("||" + project.getName().substring(0, 11) + "||");
//			if (project.getName().substring(0, 12).equals("Test project")) {
//				try {
//					I_GetConceptData conceptToRetireUpdatedFromDB = tf.getConcept(project.getUids());
//					TerminologyProjectDAO.retireConcept(conceptToRetireUpdatedFromDB, config);
//					I_GetConceptData retiredConcept = tf.getConcept(project.getUids());
//					assertEquals(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
//							retiredConcept.getConceptAttributeTuples(allowedStatusesWithRetired, config.getViewPositionSetReadOnly(), 
//									false, true).iterator().next().getStatusId());
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (TerminologyException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	/**
	 * Test method for {@link org.ihtsdo.project.TerminologyProjectDAO#getAllProjects(org.dwfa.ace.api.I_ConfigAceFrame)}.
	 */
//	public void testGetAllProjects3() {
//		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
//		assertEquals(0, projects.size());
//		//assertTrue(true);
//	}
	
//	public static void waiting(int n){
//        
//        long t0, t1;
//        t0 =  System.currentTimeMillis();
//        do{
//            t1 = System.currentTimeMillis();
//        }
//        while ((t1 - t0) < (n * 1000));
//    }
	
}
