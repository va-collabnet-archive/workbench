package org.ihtsdo.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.project.workflow.api.wf2.implementation.WfProcessDefinition;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
import org.ihtsdo.project.workflow.model.TestXstream;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.tk.workflow.api.ProjectBI;
import org.ihtsdo.tk.workflow.api.WfActivityBI;
import org.ihtsdo.tk.workflow.api.WfProcessDefinitionBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WfRoleBI;
import org.ihtsdo.tk.workflow.api.WfStateBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;
import org.ihtsdo.tk.workflow.api.WorkListBI;
import org.ihtsdo.tk.workflow.api.WorkflowStoreBI;
import org.junit.Test;

public class TestWorkflow3 {
	
	/** The vodb directory. */
	static File vodbDirectory;

	/** The read only. */
	static boolean readOnly = false;

	/** The cache size. */
	static Long cacheSize = Long.getLong("600000000");

	/** The db setup config. */
	static DatabaseSetupConfig dbSetupConfig;

	/** The config. */
	static I_ConfigAceFrame config;

	/** The tf. */
	static I_TermFactory tf;

	/** The new project concept. */
	static I_GetConceptData newProjectConcept;

	/** The allowed statuses with retired. */
	static I_IntSet allowedStatusesWithRetired;

	/** The project. */
	static ProjectBI project = null;

	@Test
	public void test() throws Exception {
		System.out.println("Deleting test fixture");
		deleteDirectory(new File("/Users/alo/Desktop/berkeley-db-test-wf-test"));
		System.out.println("Creating test fixture");
		copyDirectory(new File("/Users/alo/Desktop/berkeley-db-test-wf"), new File("/Users/alo/Desktop/berkeley-db-test-wf-test"));
		vodbDirectory = new File("/Users/alo/Desktop/berkeley-db-test-wf-test");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
		
		I_GetConceptData rootConcept = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
		I_GetConceptData procedure = tf.getConcept(UUID.fromString("bfbced4b-ad7d-30aa-ae5c-f848ccebd45b"));
		I_GetConceptData colonoscopicPolypectomy = tf.getConcept(UUID.fromString("bd2ce7f7-20a5-3ce4-b044-141284395579"));
		I_GetConceptData pneumonitisc = tf.getConcept(UUID.fromString("975420b8-e706-3744-a9f6-54180b5a5469"));
		ConceptVersionBI pneumonitis = Ts.get().getConceptVersion(config.getViewCoordinate(), pneumonitisc.getPrimUuid());
		I_GetConceptData english = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());
		I_GetConceptData spanish = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_ES.getUids());
		
		
		// Get the wfStore
		WorkflowStoreBI wfStore = new WorkflowStore();
		
		// Starting with a blank database, there should be no projects
		assertEquals(0,wfStore.getAllProjects().size());

		// Let's create one project
		project = wfStore.createProject("Test project");
		assertTrue(project != null);
		
		// There should be oen project now
		assertEquals(1,wfStore.getAllProjects().size());

		// Let's get a process definition from a file in the root folder
		WfProcessDefinitionBI wfDefinition = new WfProcessDefinition(TestXstream.readWfDefinition(new File("drools-rules/canada-fast-track-wf.wfd")));

		assertNotNull(wfDefinition);

		WfUserBI user = null;

		// There are 66 user concepts in the system. 
		// TODO: check only valid users by profile
		assertEquals(66, wfStore.getAllUsers().size());

		// Select one user to continue the test
		for (WfUserBI loopUser : wfStore.getAllUsers()) {
			if (loopUser.getName().equals("IHTSDO")) {
				user = loopUser;
			}
		}

		assertNotNull(user == null);

		config.getDbConfig().setUserConcept(tf.getConcept(user.getUuid()));
		
		// There are 11 roles in the database, created in ArchitectonicAuxiliary.
		// TODO: this is using translation roles only, see how to add others
		assertEquals(11, wfStore.getAllRoles().size());
		
		// There are 11 roles in the database, created in ArchitectonicAuxiliary
		assertEquals(8, wfDefinition.getRoles().size());
		
		// Set permission for our test user in the new project, assigning all roles (8)
		for (WfRoleBI loopRole : wfDefinition.getRoles()) {
			project.setPermission(user, loopRole, rootConcept.getPrimUuid());
		}
		
		assertEquals(8, project.getPermissions().size());
		
		// It's a new project, there should be no worklists
		assertEquals(0, project.getWorkLists().size());
		
		// Let's create one
		project.createWorkList(wfDefinition, "WorkList 1", project.getPermissions());
		
		// There should be 1 worklist now
		assertEquals(1, project.getWorkLists().size());
		
		WorkListBI workList = project.getWorkLists().iterator().next();
		
		// There should be no process instances in the worklist
		assertEquals(0, workList.getInstances().size());
		
		// Our test concept (pneumonitis) should have no instances
		assertEquals(0,  wfStore.getActiveProcessInstances(pneumonitis.getPrimUuid()).size());
		
		// Add pneumonitis to the worklist creating a new instance
		// It has the definition as a parameter, but in the current
		WfProcessInstanceBI instance = workList.createInstanceForComponent(pneumonitis.getPrimUuid(), wfDefinition);
		
		// There should be 1 process instance in the worklist
		assertEquals(1, workList.getInstances().size());
		
		// Pneumonitis should have one active instance
		assertEquals(1,  wfStore.getActiveProcessInstances(pneumonitis.getPrimUuid()).size());
		
		
		// Saving states for later tests
		WfStateBI assigned = null;
		WfStateBI translatedWithFastTrack = null;
		WfStateBI translated = null;
		WfStateBI referredToSme = null;
		
		for (WfStateBI loopState : wfStore.getAllStates()) {
			if (loopState.getName().equals("worklist item assigned")) {
				assigned = loopState;
			} else if (loopState.getName().equals("translated with fast track status")) {
				translatedWithFastTrack = loopState;
			} else if (loopState.getName().equals("translated status")) {
				translated = loopState;
			} else if (loopState.getName().equals("referred to SME status")) {
				referredToSme = loopState;
			}  
		}
		
		// Current state in the instance should be "translated"
		assertEquals(assigned, instance.getState());
		
		// The list of possible activities for translated is 2
		// this is represented in the xls file that defines the rules for this definition
		Collection<WfActivityBI> activities = wfStore.getActivities(instance, user);
		
		// There are 2 possible activities:
		//  - Send by fast track to tpo (not automatic)
		//  - Send translation to TSP reviewer (automatic)
		assertEquals(2, activities.size());
		
		for (WfActivityBI loopActivity : activities) {
			if (loopActivity.getName().equals("Send by fast track to tpo")) {
				assertEquals(false, loopActivity.isAutomatic());
			} else if (loopActivity.getName().equals("Send translation to TSP reviewer")) {
				assertEquals(true, loopActivity.isAutomatic());
			} else {
				assertTrue(false);
			}
		}
		
		// Now we manually set a new state, translated
		instance.setState(translated);
		
		// refreshing the instance from database
		instance = wfStore.getActiveProcessInstances(pneumonitis.getPrimUuid()).iterator().next();
		
		// Verifying that the new state was persisted correctly
		assertEquals(translated, instance.getState());
		
		activities = wfStore.getActivities(instance, user);
		
		// Now in the new state, the possible activities are 4:
		//  - Request feedback from SME
		//  - Send for revision by TPO reviewer
		//  - Reject and send back to translator
		//  - Escalate to Editorial Board (tsp rev)
		assertEquals(4, activities.size());
		
		// we will perform the activity "Request feedback from SME"
		for (WfActivityBI loopActivity : activities) {
			if (loopActivity.getName().equals("Request feedback from SME")) {
				loopActivity.perform(instance);
			}
		}
		
		// refreshing the instance from database
		instance = wfStore.getActiveProcessInstances(pneumonitis.getPrimUuid()).iterator().next();
		
		// current status should be referredToSme
		assertEquals(referredToSme, instance.getState());
		
		activities = wfStore.getActivities(instance, user);
		
		// Available activities are only one:
		//  - Send SME feedback
		
		assertEquals(1, activities.size());
		
		// Retrieve all worklists
		Collection<WorkListBI> workLists = project.getWorkLists();
		
		// There should be only one
		assertEquals(1, workLists.size());
		
		workList = workLists.iterator().next();
		
		// Name should be "WorkList 1"
		assertEquals("WorkList 1", workList.getName());
		
		Collection<WfProcessInstanceBI> instances = workList.getInstances();
		
		// Only one instance
		assertEquals(1, instances.size());
		
		// The only instance should be pneumonitis
		assertEquals(pneumonitis.getPrimUuid(), instances.iterator().next().getComponentPrimUuid());
		
		
		
		tf.close();
		
	}

	private static I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			//			config = tf.newAceFrameConfig();
			config = NewDefaultProfile.newProfile(null, null, null, null, null);
			
			config.getViewPositionSet().clear();
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("f9711e44-4f1f-5655-990d-0fe996c4fcc9")}), 
					Integer.MAX_VALUE));
			config.addEditingPath(tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));
			config.addPromotionPath(tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			config.getDestRelTypes().add(tf.uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

			BdbTermFactory tfb = (BdbTermFactory) tf;
			I_ConfigAceDb newDbProfile = tfb.newAceDbConfig();
			newDbProfile.setUsername("username");
			newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
			newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
			config.setDbConfig(newDbProfile);

			config.setPrecedence(Precedence.TIME);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return config;
	}

	// If targetLocation does not exist, it will be created.
	/**
	 * Copy directory.
	 *
	 * @param sourceLocation the source location
	 * @param targetLocation the target location
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void copyDirectory(File sourceLocation , File targetLocation)
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

	/**
	 * Delete directory.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public static boolean deleteDirectory(File path) {
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
