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
package org.ihtsdo.workunit.sif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
//import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.contradiction.LastCommitWinsContradictionResolutionStrategy;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.workflow.api.WfActivityBI;
import org.ihtsdo.tk.workflow.api.WfActivityInstanceBI;
import org.ihtsdo.tk.workflow.api.WfCommentBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WorkListBI;
import org.ihtsdo.workunit.task.Action;
import org.ihtsdo.workunit.task.Activity;
import org.ihtsdo.workunit.task.ExecutionStatus;
import org.ihtsdo.workunit.task.RequestSystem;
import org.ihtsdo.workunit.task.ResourceType;
import org.ihtsdo.workunit.task.TaskResource;
import org.ihtsdo.workunit.task.TaskStatus;
import org.ihtsdo.workunit.task.WfHistoryItem;
import org.ihtsdo.workunit.task.WorkItem;
import org.ihtsdo.workunit.task.WorkList;

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

	private UUID sctIdentifierSchemeId;

	private UUID uuidIdentifierSchemeId;

	private WorkItem workItem;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
//						System.out.println("Deleting test fixture");
//						deleteDirectory(new File("berkeley-db"));
//						System.out.println("Creating test fixture");
//						copyDirectory(new File("/Applications/April-DK-UAT-PA-May-10 copy 2/berkeley-db"), new File("berkeley-db"));
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
	 * @throws Exception 
	 */
	public void testJsonExport() throws Exception {

		List<WorkItem> items = new ArrayList<WorkItem>();

		sctIdentifierSchemeId = UUID.fromString("479b3538-d549-4440-9e9e-7b832f9f380e");
		uuidIdentifierSchemeId = UUID.fromString("9dd8fa57-bcc4-4a81-8337-596001698c58");

		WorkflowStore wf = new WorkflowStore();
		int count = 0;
		WorkListBI worklist = wf.getWorklist(UUID.fromString("1fe5ba37-8212-409f-b6ff-9f6a9101d530"));
		for (WfProcessInstanceBI loopInstance : worklist.getInstances()) {
			count++;
			//if (count > 3) break;
			//if (loopInstance.getComponentPrimUuid().equals(UUID.fromString("db4cc3b7-da44-35b7-bcc3-ca1562a64eaa"))) {
			if (loopInstance.getComponentPrimUuid().equals(UUID.fromString("f23119bf-e904-3581-b34c-0e8c5ef528c5"))) {
				workItem = new WorkItem();
				workItem.setContext(new SifContext());
				workItem.getContext().setParents(new HashSet<SifConceptDescriptor>());
				workItem.getContext().setSiblings(new HashSet<SifConceptDescriptor>());
				workItem.getContext().setChildren(new HashSet<SifConceptDescriptor>());
				workItem.getContext().setMetadata(new HashSet<SifConceptDescriptor>());
				workItem.getContext().setRelationshipTargets(new HashSet<SifConceptDescriptor>());

				Activity tmpActivity = new Activity();
				tmpActivity.setName("Placeholder Activity");
				tmpActivity.setId(UUID.randomUUID());
				workItem.setActivity(tmpActivity);

				// Load concept data
				SifConcept attachedConceptBaseVersion = new SifConcept();

				ConceptVersionBI instanceConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), loopInstance.getComponentPrimUuid());

				ConceptAttributeVersionBI attributes = instanceConcept.getConceptAttributes().getVersion(config.getViewCoordinate());

				List<SifIdentifier> ids = new ArrayList<SifIdentifier>();
				ids.add(new SifIdentifier(uuidIdentifierSchemeId, instanceConcept.getPrimUuid().toString()));
				ids.add(getSctIdentifier(attributes));
				attachedConceptBaseVersion.setIdentifiers(ids);

				attachedConceptBaseVersion.setActive(attributes.isActive(config.getViewCoordinate()));
				attachedConceptBaseVersion.setEffectiveTime(attributes.getTime());
				attachedConceptBaseVersion.setModuleId(getSctIdentifier(attributes.getModuleNid()));
				workItem.getContext().getMetadata().add(getConceptDescriptor(attributes.getModuleNid()));

				ConceptVersionBI defStatusConcept = null;
				if (attributes.isDefined()) {
					defStatusConcept = SnomedMetadataRf2.DEFINED_RF2.getStrict(config.getViewCoordinate());
				} else {
					defStatusConcept = SnomedMetadataRf2.PRIMITIVE_RF2.getStrict(config.getViewCoordinate());
				}
				attachedConceptBaseVersion.setDefinitionStatusId(getSctIdentifier(defStatusConcept.getConceptAttributes().getVersion(config.getViewCoordinate())));
				workItem.getContext().getMetadata().add(getConceptDescriptor(defStatusConcept));
				attachedConceptBaseVersion.setSimpleTypeMemberships(getSimpleMemberships(instanceConcept.getConceptAttributes().getVersion(config.getViewCoordinate())));
				attachedConceptBaseVersion.setCidTypeMemberships(getCidMemberships(instanceConcept.getConceptAttributes().getVersion(config.getViewCoordinate())));

				// Load descriptions
				attachedConceptBaseVersion.setDescriptions(new ArrayList<SifDescription>());
				for (DescriptionChronicleBI loopDescriptionChronicle : instanceConcept.getDescriptions()) {
					DescriptionVersionBI loopDescription = loopDescriptionChronicle.getVersion(config.getViewCoordinate());
					SifDescription sifDescription = new SifDescription();
					sifDescription.setActive(loopDescription.isActive(config.getViewCoordinate()));

					ConceptVersionBI desICSConcept = null;
					if (loopDescription.isInitialCaseSignificant()) {
						desICSConcept = SnomedMetadataRf2.CASE_INSENSITIVE_RF2.getStrict(config.getViewCoordinate());
					} else {
						desICSConcept = SnomedMetadataRf2.CASE_SENSITIVE_RF2.getStrict(config.getViewCoordinate());
					}
					sifDescription.setCaseSignificanceId(getSctIdentifier(desICSConcept.getConceptAttributes().getVersion(config.getViewCoordinate())));
					workItem.getContext().getMetadata().add(getConceptDescriptor(desICSConcept));

					sifDescription.setEffectiveTime(loopDescription.getTime());
					sifDescription.setLanguageCode(loopDescription.getLang());

					List<SifIdentifier> dIds = new ArrayList<SifIdentifier>();
					dIds.add(new SifIdentifier(uuidIdentifierSchemeId, loopDescription.getPrimUuid().toString()));
					dIds.add(getSctIdentifier(loopDescription));
					sifDescription.setIdentifiers(dIds);

					sifDescription.setConceptId(getSctIdentifier(attributes));
					sifDescription.setTerm(loopDescription.getText());
					sifDescription.setTypeId(getSctIdentifier(loopDescription.getTypeNid()));
					workItem.getContext().getMetadata().add(getConceptDescriptor(loopDescription.getTypeNid()));
					sifDescription.setModuleId(getSctIdentifier(loopDescription.getModuleNid()));
					workItem.getContext().getMetadata().add(getConceptDescriptor(loopDescription.getModuleNid()));

					sifDescription.setSimpleTypeMemberships(getSimpleMemberships(loopDescription));
					sifDescription.setCidTypeMemberships(getCidMemberships(loopDescription));

					attachedConceptBaseVersion.getDescriptions().add(sifDescription);

				}

				// Load descriptions
				attachedConceptBaseVersion.setRelationships(new ArrayList<SifRelationship>());
				for (RelationshipChronicleBI loopRelChronicle : instanceConcept.getRelationshipsOutgoing()) {
					RelationshipVersionBI loopRel = loopRelChronicle.getVersion(config.getViewCoordinate());
					SifRelationship sifRelationship = new SifRelationship();

					sifRelationship.setActive(loopRel.isActive(config.getViewCoordinate()));

					List<SifIdentifier> rids = new ArrayList<SifIdentifier>();
					rids.add(new SifIdentifier(uuidIdentifierSchemeId, loopRel.getPrimUuid().toString()));
					rids.add(getSctIdentifier(loopRel));
					sifRelationship.setIdentifiers(rids);

					sifRelationship.setEffectiveTime(loopRel.getTime());
					sifRelationship.setModuleId(getSctIdentifier(loopRel.getModuleNid()));
					workItem.getContext().getMetadata().add(getConceptDescriptor(loopRel.getModuleNid()));
					sifRelationship.setCharacteristicTypeId(getSctIdentifier(loopRel.getCharacteristicNid()));
					workItem.getContext().getMetadata().add(getConceptDescriptor(loopRel.getCharacteristicNid()));
					sifRelationship.setDestinationId(getSctIdentifier(loopRel.getTargetNid()));
					sifRelationship.setTypeId(getSctIdentifier(loopRel.getTypeNid()));
					workItem.getContext().getMetadata().add(getConceptDescriptor(loopRel.getTypeNid()));
					sifRelationship.setSourceId(getSctIdentifier(loopRel.getSourceNid()));
					// Modifier Hardcoded to SOME
					sifRelationship.setModifierId(getSctIdentifier(Ts.get().getNidForUuids(UUID.fromString("a526d5af-b083-3f20-86af-9a6ce17a8b72"))));
					workItem.getContext().getMetadata().add(getConceptDescriptor(Ts.get().getNidForUuids(UUID.fromString("a526d5af-b083-3f20-86af-9a6ce17a8b72"))));
					sifRelationship.setRelationshipGroup(loopRel.getGroup());

					if (loopRel.getTypeNid() == Ts.get().getNidForUuids(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"))) {
						workItem.getContext().getParents().add(getConceptDescriptor(loopRel.getTargetNid()));
					}

					workItem.getContext().getRelationshipTargets().add(getConceptDescriptor(loopRel.getTargetNid()));

					attachedConceptBaseVersion.getRelationships().add(sifRelationship);
				}

				for (RelationshipChronicleBI loopRelChronicle : instanceConcept.getRelationshipsIncoming()) {
					RelationshipVersionBI loopRel = loopRelChronicle.getVersion(config.getViewCoordinate());
					if (loopRel.getTypeNid() == Ts.get().getNidForUuids(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"))) {
						workItem.getContext().getChildren().add(getConceptDescriptor(loopRel.getSourceNid()));
					}
				}

				workItem.setAttachedConceptBaseVersion(attachedConceptBaseVersion);

				// no changeset on start
				workItem.setChangeset(null);

				workItem.setDescription(instanceConcept.getDescriptionFullySpecified().getText());

				workItem.setDueDate(System.currentTimeMillis() + 1000000L);

				workItem.setExecutionStatus(ExecutionStatus.RESERVED);

				Map<UUID, Action> possibleActions = new HashMap<UUID, Action>();
				for (WfActivityBI loopActivity : loopInstance.getActivities(loopInstance.getAssignedUser())) {
					Action loopAction = new Action();
					loopAction.setName(loopActivity.getName());
					loopAction.setId(loopActivity.getUuid());
					possibleActions.put(loopActivity.getUuid(), loopAction);
				}

				List<Action> possibleActionsList = new ArrayList<Action>();
				possibleActionsList.addAll(possibleActions.values());
				workItem.setPossibleActions(possibleActionsList);

				workItem.setPriority(1);

				TaskResource resource = new TaskResource();
				resource.setResourceType(ResourceType.USER);
				resource.setId(loopInstance.getAssignedUser().getUuid());
				resource.setName(loopInstance.getAssignedUser().getName());
				workItem.setResource(resource);

				TaskStatus status = new TaskStatus();
				status.setId(loopInstance.getState().getUuid());
				status.setName(loopInstance.getState().getName());
				workItem.setStatus(status);

				workItem.setWorkItemId(UUID.randomUUID());

				WorkList workList = new WorkList();
				SifSource source = new SifSource();
				SifEnvironment environment = new SifEnvironment();
				environment.setName("Pilot test of Translation Bundle, April 2013");
				environment.setId(UUID.randomUUID());
				source.setEnvironment(environment);
				source.setModuleId(getSctIdentifier(config.getEditCoordinate().getModuleNid()));
				workItem.getContext().getMetadata().add(getConceptDescriptor(config.getEditCoordinate().getModuleNid()));
				SifOrganization organization = new SifOrganization();
				organization.setId(UUID.randomUUID());
				organization.setName("IHTSDO");
				source.setOrganization(organization);
				ConceptVersionBI pathConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), config.getEditCoordinate().getEditPaths()[0]);
				SifPath path = new SifPath();
				path.setId(pathConcept.getPrimUuid());
				path.setName(pathConcept.toUserString());
				source.setPath(path);
				SifPerson person = new SifPerson();
				person.setName(config.getDbConfig().getUserConcept().toUserString());
				person.setId(config.getDbConfig().getUserConcept().getPrimUuid());
				source.setPerson(person);
				SifSoftware software = new SifSoftware();
				software.setId(UUID.randomUUID());
				software.setName("IHTSDO Terminology Workbench");
				software.setVersion("3.22-nojini");
				source.setSoftware(software);
				workList.setCreatedBy(source);
				workList.setCreatedOn(System.currentTimeMillis());
				workList.setName(loopInstance.getWorkList().getName());
				workList.setRequestId("artf8231982");
				RequestSystem requestSystem = new RequestSystem();
				requestSystem.setId(UUID.randomUUID());
				requestSystem.setName("Collabnet");
				workList.setRequestSystem(requestSystem);
				workList.setWorklistId(loopInstance.getWorkList().getUuid());
				workList.setInstructions("The concepts should be translated to the Danish language. Only a preferred synonym is required. Do not create a Danish FSN");
				workList.setLinks(new ArrayList<URL>());
				workList.getLinks().add(new URL("http://www.ihtsdo.org"));
				workList.getLinks().add(new URL("http://www.google.com"));
				workList.getLinks().add(new URL("http://www.ncbi.nlm.nih.gov/pubmed/"));
				workItem.setWorkList(workList);
				workItem.setHistory(new LinkedList<WfHistoryItem>());
				for (WfActivityInstanceBI loopActivityInstance : loopInstance.getActivityInstances()) {
					WfHistoryItem history = new WfHistoryItem();
					history.setTime(loopActivityInstance.getTime());
					SifPerson historyAuthor = new SifPerson();
					historyAuthor.setId(loopActivityInstance.getAuthor().getUuid());
					historyAuthor.setIdScheme(uuidIdentifierSchemeId.toString());
					historyAuthor.setName(loopActivityInstance.getAuthor().getName());
					history.setAuthor(historyAuthor);
					TaskStatus historyStatus = new TaskStatus();
					historyStatus.setId(loopActivityInstance.getState().getUuid());
					historyStatus.setName(loopActivityInstance.getState().getName());
					history.setStatus(historyStatus);
					workItem.getHistory().add(history);
				}
				workItem.setComments(new LinkedList<SifComment>());
				for (WfCommentBI loopComment : loopInstance.getComments()) {
					SifPerson author = new SifPerson();
					author.setId(loopComment.getAuthor().getUuid().toString());
					author.setName(loopComment.getAuthor().getName());
					workItem.getComments().add(new SifComment(loopComment.getDate(), loopComment.getComment(), author));
				}

				items.add(workItem);
				Gson gson = new Gson();
				System.out.println(gson.toJson(workItem));
				System.out.println(gson.toJson(workItem).getBytes("UTF-8").length / 1024);
			}
		}
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
			//			config.addViewPosition(tf.newPosition(
			//					tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}), 
			//					Long.MAX_VALUE));
			//			config.addViewPosition(tf.newPosition(
			//					tf.getPath(new UUID[] {UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}), 
			//					Long.MAX_VALUE));
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("ef80c776-ea3b-5253-8ec9-b60c2d6b8814")}), 
					Long.MAX_VALUE));
			config.addEditingPath( tf.getPath(new UUID[] {UUID.fromString("ef80c776-ea3b-5253-8ec9-b60c2d6b8814")}));
			config.addPromotionPath( tf.getPath(new UUID[] {UUID.fromString("ef80c776-ea3b-5253-8ec9-b60c2d6b8814")}));

			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			config.getAllowedStatus().add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			config.getAllowedStatus().add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			config.getDestRelTypes().add(tf.uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
			config.setConflictResolutionStrategy(new LastCommitWinsContradictionResolutionStrategy());
			config.setPrecedence(Precedence.TIME);
			config.setLanguageSortPref(I_ConfigAceFrame.LANGUAGE_SORT_PREF.LANG_REFEX);
			config.getLanguagePreferenceList().clear();

			//BdbTermFactory tfb = (BdbTermFactory) tf;
//			I_ConfigAceDb newDbProfile = tfb.newAceDbConfig();
//			newDbProfile.setUsername("username");
//			newDbProfile.setUserConcept(tf.getConcept(UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c")));
//			newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
//			newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
//			newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
//			newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
//			config.setDbConfig(newDbProfile);


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

	private SifIdentifier getSctIdentifier(ComponentVersionBI component) throws IOException, TerminologyException {
		SifIdentifier identifier = new SifIdentifier();
		identifier.setIdentifierSchemeId(sctIdentifierSchemeId);
		identifier.setIdentifier("N/A");
		if (component.getAdditionalIds() != null) {
			for (IdBI loopId : component.getAdditionalIds()) {
				if (loopId.getAuthorityNid() == ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid()) {
					identifier.setIdentifier(((Long) loopId.getDenotation()).toString());
				}
			}
		}
		return identifier;
	}

	private SifIdentifier getSctIdentifier(int cnid) throws IOException, TerminologyException, ContradictionException {
		ConceptVersionBI concept = Ts.get().getConceptVersion(config.getViewCoordinate(), cnid);
		if (concept.getConceptAttributes() != null) {
			return getSctIdentifier(concept.getConceptAttributes().getVersion(config.getViewCoordinate()));
		} else return null;
	}

	private SifConceptDescriptor getConceptDescriptor(ConceptVersionBI concept) throws IOException, ContradictionException, TerminologyException {
		SifConceptDescriptor sifConceptDescriptor =  new SifConceptDescriptor();
		sifConceptDescriptor.setDescription(concept.getDescriptionFullySpecified().getText());
		String abbrev = "";
		for (DescriptionVersionBI loopDescription : concept.getDescriptionsActive()) {
			if ((abbrev.isEmpty() || abbrev.length() > loopDescription.getText().length()) && loopDescription.getLang().equals("en")) {
				abbrev = loopDescription.getText();
			}
		}
		sifConceptDescriptor.setAbbrev(abbrev);
		sifConceptDescriptor.setIdentifier(getSctIdentifier(concept));
		return sifConceptDescriptor;
	}

	private SifConceptDescriptor getConceptDescriptor(int cnid) throws IOException, ContradictionException, TerminologyException {
		ConceptVersionBI concept = Ts.get().getConceptVersion(config.getViewCoordinate(), cnid);
		return getConceptDescriptor(concept);
	}

	private List<SifRefsetMemberCidType> getCidMemberships(ComponentVersionBI component) throws IOException, TerminologyException, ContradictionException {
		List<SifRefsetMemberCidType> result = new ArrayList<SifRefsetMemberCidType>();
		for (RefexVersionBI<?> loopAnnotation : component.getAnnotationsActive(config.getViewCoordinate())) {
			if (loopAnnotation instanceof RefexNidVersionBI) {
				RefexNidVersionBI loopCidAnnotation = (RefexNidVersionBI) loopAnnotation;
				SifRefsetMemberCidType sifMember = new SifRefsetMemberCidType();
				sifMember.setActive(loopCidAnnotation.isActive(config.getViewCoordinate()));
				sifMember.setChangeTime(loopCidAnnotation.getTime());
				sifMember.setCid1(getSctIdentifier(loopCidAnnotation.getNid1()));
				sifMember.setEffectiveTime(loopCidAnnotation.getTime());
				sifMember.setModuleId(getSctIdentifier(loopCidAnnotation.getModuleNid()));
				sifMember.setReferencedComponentId(getSctIdentifier(loopCidAnnotation.getReferencedComponentNid()));
				sifMember.setRefsetId(getSctIdentifier(loopCidAnnotation.getRefexNid()));
				workItem.getContext().getMetadata().add(getConceptDescriptor(loopAnnotation.getRefexNid()));
				workItem.getContext().getMetadata().add(getConceptDescriptor(loopCidAnnotation.getNid1()));
				result.add(sifMember);
			}
		}
		return result;
	}

	private List<SifRefsetMemberSimpleType> getSimpleMemberships(ComponentVersionBI component) throws IOException, TerminologyException, ContradictionException {
		List<SifRefsetMemberSimpleType> result = new ArrayList<SifRefsetMemberSimpleType>();
		for (RefexVersionBI<?> loopAnnotation : component.getAnnotationsActive(config.getViewCoordinate())) {
			if (!(loopAnnotation instanceof RefexNidVersionBI)) {
				SifRefsetMemberSimpleType sifMember = new SifRefsetMemberSimpleType();
				sifMember.setActive(loopAnnotation.isActive(config.getViewCoordinate()));
				sifMember.setChangeTime(loopAnnotation.getTime());
				sifMember.setEffectiveTime(loopAnnotation.getTime());
				sifMember.setModuleId(getSctIdentifier(loopAnnotation.getModuleNid()));
				sifMember.setReferencedComponentId(getSctIdentifier(loopAnnotation.getReferencedComponentNid()));
				sifMember.setRefsetId(getSctIdentifier(loopAnnotation.getRefexNid()));
				workItem.getContext().getMetadata().add(getConceptDescriptor(loopAnnotation.getRefexNid()));
				result.add(sifMember);
			}
		}
		return result;
	}
}
