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
package org.ihtsdo.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.tk.api.Precedence;

/**
 * The Class TestContextualizedDescriptions.
 */
public class TestProjectsCRUD extends TestCase {

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
		System.out.println("Deleting test fixture");
		deleteDirectory(new File("berkeley-db"));
		System.out.println("Creating test fixture");
		copyDirectory(new File("src/test/java/org/ihtsdo/project/berkeley-db"), new File("berkeley-db"));
		vodbDirectory = new File("berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = (I_ImplementTermFactory) Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	public void testCreateNewProjectAndUpdateName() throws Exception {
		List<TranslationProject> projects = new ArrayList<TranslationProject>();
		projects = TerminologyProjectDAO.getAllTranslationProjects(config);
		assertEquals(0, projects.size());
		
		String name = "Test project " + UUID.randomUUID().toString();
		TranslationProject tempProjectForMetadata = new TranslationProject(name, 0, null);
		TranslationProject project = 
			TerminologyProjectDAO.createNewTranslationProject(tempProjectForMetadata, config);
		tf.commit();
		newProjectConcept = project.getConcept();
		assertTrue(project.getName().startsWith(name));

		project = TerminologyProjectDAO.getTranslationProject(newProjectConcept, config);
		assertTrue(project.getName().startsWith(name));
		
		sleep(1);
		
		String newName = name + " 2";
		
		project.setName(newName);
		TerminologyProjectDAO.updateTranslationProjectMetadata(project, config);
		tf.commit();
		sleep(1);
		project = TerminologyProjectDAO.getTranslationProject(newProjectConcept, config);
		assertEquals(newName, project.getName());

		assertEquals(1, TerminologyProjectDAO.getAllTranslationProjects(config).size());
		String name2 = "Test project " + UUID.randomUUID().toString();
		TranslationProject tempProjectForMetadata2 = new TranslationProject(name2, 0, null);
		TerminologyProjectDAO.createNewTranslationProject(tempProjectForMetadata2, config);
		sleep(1);
		
		assertEquals(2, TerminologyProjectDAO.getAllTranslationProjects(config).size());
		for (TranslationProject loopProject : TerminologyProjectDAO.getAllTranslationProjects(config)) {
			try {
				TerminologyProjectDAO.retireProject(loopProject, config);
				Terms.get().commit();
//				I_GetConceptData retiredConcept = loopProject.getConcept(); 
//				List<? extends I_ConceptAttributeTuple> lastAttributePart =retiredConcept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy());
//				I_GetConceptData lastAttributePartStatus = tf.getConcept(lastAttributePart.get(0).getConceptStatus());
//				I_GetConceptData retiredStatus = tf.getConcept(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
//				assertEquals(lastAttributePartStatus.getConceptId(), retiredStatus.getConceptId());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		assertEquals(0, TerminologyProjectDAO.getAllTranslationProjects(config).size());
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
			
//			I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
//	        newDbProfile.setUsername("username");
//	        newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
//	        newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
//	        newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
//	        newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
//	        config.setDbConfig(newDbProfile);
			
			config.setPrecedence(Precedence.TIME);
	        
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return config;
	}
	
	// If targetLocation does not exist, it will be created.
	public void copyDirectory(File sourceLocation , File targetLocation)
	throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	public static void sleep(int n){
		long t0, t1;
		t0 =  System.currentTimeMillis();
		do{
			t1 = System.currentTimeMillis();
		}
		while ((t1 - t0) < (n * 1000));
	}
}


