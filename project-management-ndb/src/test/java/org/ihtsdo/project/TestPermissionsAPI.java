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
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.Precedence;

/**
 * The Class TestContextualizedDescriptions.
 */
public class TestPermissionsAPI extends TestCase {

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
		tf = Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	/**
	 * Test state full.
	 */
	public void testStateFull() {
		try {
			// Create sample users
			I_GetConceptData user = Terms.get().newConcept(UUID.randomUUID(), false, config);
            Terms.get().newDescription(UUID.randomUUID(), user, "en",
                "user1 (user)",
                Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), 
                config);
            Terms.get().newDescription(UUID.randomUUID(), user, "en",
                "user1", 
                Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()),
                config);
            Terms.get().newDescription(UUID.randomUUID(), user, "en",
            		"user1.inbox", 
            		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids()),
            		config);
            tf.newRelationship(UUID.randomUUID(), user, 
            		tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
            		tf.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids()),
    				tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
    				tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
    				tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);
			
            I_GetConceptData user2 = Terms.get().newConcept(UUID.randomUUID(), false, config);
            Terms.get().newDescription(UUID.randomUUID(), user2, "en",
            		"user2 (user)",
            		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), 
            		config);
            Terms.get().newDescription(UUID.randomUUID(), user2, "en",
            		"user2", 
            		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()),
            		config);
            Terms.get().newDescription(UUID.randomUUID(), user2, "en",
            		"user2.inbox", 
            		Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids()),
            		config);
            tf.newRelationship(UUID.randomUUID(), user2, 
            		tf.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
            		tf.getConcept(ArchitectonicAuxiliary.Concept.USER.getUids()),
            		tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
            		tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
            		tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);
            
            tf.addUncommittedNoChecks(user);
            tf.addUncommittedNoChecks(user2);
            tf.commit();
            
            //Select sample permission and domain
			I_GetConceptData permission = tf.getConcept(ArchitectonicAuxiliary.Concept.SME_ROLE.getUids());
			I_GetConceptData domain = tf.getConcept(ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC.getUids());
			
			//Get the api
			ProjectPermissionsAPI permissionApi = new ProjectPermissionsAPI(config);
			
			//There should be no permission for this domain yet
			assertFalse(permissionApi.checkPermissionForProject(user, domain, permission));
			
			assertEquals(0, permissionApi.getUsersForRole(permission, domain).size());
			
			//add permissions for both users
			permissionApi.addPermission(user, permission, domain);
			permissionApi.addPermission(user2, permission, domain);
			
			assertTrue(permissionApi.checkPermissionForProject(user, domain, permission));
			assertTrue(permissionApi.checkPermissionForProject(user2, domain, permission));
			assertEquals(2, permissionApi.getUsersForRole(permission, domain).size());
			
			//remove permission for one user
			permissionApi.removePermission(user, permission, domain);
			
			assertFalse(permissionApi.checkPermissionForProject(user, domain, permission));
			assertTrue(permissionApi.checkPermissionForProject(user2, domain, permission));
			assertEquals(1, permissionApi.getUsersForRole(permission, domain).size());

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
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
			config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

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
}
