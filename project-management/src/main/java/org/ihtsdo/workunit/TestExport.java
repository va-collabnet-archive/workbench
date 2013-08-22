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
package org.ihtsdo.workunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.workflow.api.wf2.implementation.WfActivity;
import org.ihtsdo.project.workflow.api.wf2.implementation.WfActivityInstance;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfComment;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.workflow.api.WfActivityBI;
import org.ihtsdo.tk.workflow.api.WfActivityInstanceBI;
import org.ihtsdo.tk.workflow.api.WfCommentBI;
import org.ihtsdo.tk.workflow.api.WfStateBI;

import com.google.gson.Gson;

/**
 * The Class TestTerminologyProjectDAOForWorkSetsCRUD.
 */
public class TestExport extends TestCase {

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
		//		System.out.println("Deleting test fixture");
		//		deleteDirectory(new File("berkeley-db"));
		//		System.out.println("Creating test fixture");
		//		copyDirectory(new File("/Applications/April-DK-UAT-PA-May-10 copy/berkeley-db"), new File("berkeley-db"));
		vodbDirectory = new File("berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = (I_ImplementTermFactory) Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	/**
	 * Test new workflow.
	 * @throws IOException 
	 * @throws ContradictionException 
	 * @throws TerminologyException 
	 */
	public void testJsonExport() throws IOException, ContradictionException, TerminologyException {
		I_GetConceptData asthmaAttack = tf.getConcept(new UUID[] {UUID.fromString("51e26d81-090e-33ee-b14a-da77646c471d")});
		I_GetConceptData sctRoot = tf.getConcept(new UUID[] {UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")});
		I_GetConceptData procedInt = tf.getConcept(new UUID[] {UUID.fromString("c2be9e2c-0f5e-3174-8fb5-ff86d874fafa")});
		WorkUnit unit = new WorkUnit();
		unit.setId(UUID.randomUUID());
		unit.setWorkListId(UUID.randomUUID());
		unit.setWorklistName("Worklist 1");
		WfState translated = new WfState("translated", UUID.randomUUID());
		WfState rejected = new WfState("rejected", UUID.randomUUID());
		WfState sentToTPOReview = new WfState("sent to TPO review", UUID.randomUUID());
		WfState sentToEB = new WfState("sent to EB", UUID.randomUUID());
		WfState assigned = new WfState("assigned", UUID.randomUUID());
		unit.setState(translated);
		WfAction action1 = new WfAction("Reject", UUID.randomUUID(), rejected, null);
		WfAction action2 = new WfAction("Sent to TPO review", UUID.randomUUID(), sentToTPOReview, null);
		WfAction action3 = new WfAction("Sent to EB", UUID.randomUUID(), sentToEB, null);
		Map<WfActivityBI,WfStateBI> activitiesMap = new HashMap<WfActivityBI,WfStateBI>();
		activitiesMap.put(new WfActivity(action1), rejected);
		activitiesMap.put(new WfActivity(action2), sentToTPOReview);
		activitiesMap.put(new WfActivity(action3), sentToEB);
		unit.setPossibleActivities(activitiesMap);
		WfUser john = new WfUser("John", UUID.randomUUID());
		unit.setAssignedUser(john);
		WfActivityInstance actIns1 = new WfActivityInstance(System.currentTimeMillis() - 75000, 
				translated, new WfUser("Mary", UUID.randomUUID()), false, false);
		WfActivityInstance actIns2 = new WfActivityInstance(System.currentTimeMillis() - 120000, 
				assigned, new WfUser("Mary", UUID.randomUUID()), false, false);
		List<WfActivityInstanceBI> wfHistory = new ArrayList<WfActivityInstanceBI>();
		wfHistory.add(actIns1);
		wfHistory.add(actIns2);
		unit.setWorkflowHistory(wfHistory);
		WfComment comment = new WfComment();
		comment.setAuthor(john);
		comment.setComment("The translation is not the best one, please review according to similarity.");
		comment.setDate(System.currentTimeMillis() - 74500);
		comment.setRole("Reviewer");
		List<WfCommentBI> commentsList = new ArrayList<WfCommentBI>();
		commentsList.add(comment);
		unit.setComments(commentsList);
		unit.setInstructions("This is a Translation Work Unit, please translate the concept following the IHTSDO Translation guidelines. Do not trasnlate the FSN, only a preferred synonym is needed. Acceptable synonyms are allowed, but not required.");
		Map<String,URL> linksList = new HashMap<String,URL>();
		linksList.put("IHTSDO Site", URI.create("http://www.ihtso.org").toURL());
		linksList.put("Real Academia Española", URI.create("http://www.rae.es/rae.html").toURL());
		unit.setLinks(linksList);

		WuFocusConcept contextSample = new WuFocusConcept();
		TkConcept eC = new TkConcept(procedInt.getVersion(config.getViewCoordinate()));
		contextSample.setConcept(eC);
		Set<TkConcept> parents = new HashSet<TkConcept>();
		Set<TkConcept> children = new HashSet<TkConcept>();
		Set<TkConcept> siblings = new HashSet<TkConcept>();
		Set<TkConcept> others = new HashSet<TkConcept>();

		for (RelationshipChronicleBI loopRel : asthmaAttack.getRelationshipsOutgoing()) {
			RelationshipVersionBI loopRelVer = loopRel.getVersion(config.getViewCoordinate());
			if (loopRelVer.getCharacteristicNid() == tf.uuidToNative(UUID.fromString("1290e6ba-48d0-31d2-8d62-e133373c63f5"))) {
				if (loopRelVer.getTypeNid() == tf.uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"))) {
					I_GetConceptData parent = tf.getConcept(loopRelVer.getTargetNid());
					parents.add(new TkConcept(tf.getConcept(loopRelVer.getSourceNid())));
					for (RelationshipChronicleBI loopRel2 : asthmaAttack.getRelationshipsIncoming()) {
						RelationshipVersionBI loopRelVer2 = loopRel2.getVersion(config.getViewCoordinate());
						if (loopRelVer2.getCharacteristicNid() == tf.uuidToNative(UUID.fromString("1290e6ba-48d0-31d2-8d62-e133373c63f5"))) {
							if (loopRelVer2.getTypeNid() == tf.uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"))) {
								siblings.add(new TkConcept(tf.getConcept(loopRelVer2.getSourceNid())));
							}
						}
					}
				} else {
					others.add(new TkConcept(tf.getConcept(loopRelVer.getTargetNid())));
				}
			}
		}

		for (RelationshipChronicleBI loopRel : asthmaAttack.getRelationshipsIncoming()) {
			RelationshipVersionBI loopRelVer = loopRel.getVersion(config.getViewCoordinate());
			if (loopRelVer.getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
				if (loopRelVer.getTypeNid() == tf.uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"))) {
					children.add(new TkConcept(tf.getConcept(loopRelVer.getSourceNid())));
				}
			}
		}
		List<TkConcept> parentsList = new ArrayList<TkConcept>();
		parentsList.addAll(parents);
		contextSample.setDirectInferredParents(parentsList);
		
		List<TkConcept> childrenList = new ArrayList<TkConcept>();
		childrenList.addAll(children);
		contextSample.setDirectInferredChildren(childrenList);
		
		List<TkConcept> siblingsList = new ArrayList<TkConcept>();
		siblingsList.addAll(siblings);
		contextSample.setDirectInferredSiblings(siblingsList);
		
		List<TkConcept> othersList = new ArrayList<TkConcept>();
		othersList.addAll(others);
		contextSample.setDirectInferredSiblings(othersList);

		List<WuFocusConcept> focusList = new ArrayList<WuFocusConcept>();
		focusList.add(contextSample);
		unit.setFocusConcepts(focusList);

		WuProfile profileSample = new WuProfile();
		profileSample.setEditModuleId(UUID.fromString("8be90a21-4ef1-3a7c-bf42-02e4531d95a8"));
		profileSample.setEditPathId(UUID.fromString("ef80c776-ea3b-5253-8ec9-b60c2d6b8814"));
		profileSample.setAttributesEditable(true);
		profileSample.setDescriptionsEditable(true);
		profileSample.setMembersEditable(true);
		profileSample.setRelationshipsEditable(true);
		profileSample.setRestrictEditsToEditModule(false);

		unit.setProfile(profileSample);

		Gson gson = new Gson();
		System.out.println(gson.toJson(unit));
		System.out.println(gson.toJson(unit).getBytes("UTF-8").length / 1024);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}


	/**
	 * Gets the test config.
	 *
	 * @return the test config
	 */
	private I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			config = tf.newAceFrameConfig();
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}), 
					Long.MAX_VALUE));
			config.addEditingPath( tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));
			config.addPromotionPath( tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));

			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			config.getAllowedStatus().add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			config.getAllowedStatus().add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

			//			BdbTermFactory tfb = (BdbTermFactory) tf;
			//			I_ConfigAceDb newDbProfile = tfb.newAceDbConfig();
			//			newDbProfile.setUsername("username");
			//			newDbProfile.setUserConcept(tf.getConcept(UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c")));
			//			newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			//			newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			//			newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
			//			newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
			//			config.setDbConfig(newDbProfile);

			config.setPrecedence(Precedence.TIME);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

	/**
	 * Delete directory.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
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
