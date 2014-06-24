/*
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

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * The Class WorklistInitializerProcessor.
 */
public class WorklistInitializerProcessor implements
		ProcessUnfetchedConceptDataBI {

	/**
	 * The members nid set.
	 */
	NidBitSetBI membersNidSet;
	/**
	 * The vc.
	 */
	ViewCoordinate vc;
	/**
	 * The prom ref.
	 */
	PromotionAndAssignmentRefset promRef;
	/**
	 * The active nid.
	 */
	int activeNid;
	/**
	 * The active uuid.
	 */
	UUID activeUuid;
	/**
	 * The inactive nid.
	 */
	int inactiveNid;
	/**
	 * The inactive uuid.
	 */
	UUID inactiveUuid;
	/**
	 * The updater.
	 */
	ActivityUpdater updater;
	/**
	 * The tc.
	 */
	TerminologyBuilderBI tc;
	/**
	 * The work list nid.
	 */
	int workListNid;
	/**
	 * The interpreter.
	 */
	WorkflowInterpreter interpreter;
	/**
	 * The user.
	 */
	WfUser user;
	/**
	 * The work list.
	 */
	WorkList workList;
	/**
	 * The user nid.
	 */
	int userNid;
	/**
	 * The assigned nid.
	 */
	int assignedNid;
	/**
	 * The ts.
	 */
	TerminologyStoreDI ts;

	/**
	 * Instantiates a new worklist initializer processor.
	 * 
	 * @param partition
	 *            the partition
	 * @param workList
	 *            the work list
	 * @param config
	 *            the config
	 * @param updater
	 *            the updater
	 */
	public WorklistInitializerProcessor(Partition partition, WorkList workList,
			I_ConfigAceFrame config, ActivityUpdater updater) {
		super();
		try {
			vc = config.getViewCoordinate();
			tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
					config.getViewCoordinate());
			this.updater = updater;
			this.workList = workList;
			if (updater != null) {
				updater.startActivity();
			}
			workListNid = workList.getId();
			promRef = workList.getPromotionRefset(config);
			ts = Ts.get();
			interpreter = WorkflowInterpreter
					.createWorkflowInterpreter(workList.getWorkflowDefinition());

			membersNidSet = ts.getEmptyNidSet();

			if (updater != null) {
				updater.setTaskMessage("Processing partitionMembers");
			}
			ConceptChronicleBI partitionChronicle = (ConceptChronicleBI) partition
					.getConcept();
			Collection<? extends RefexVersionBI<?>> partitionMembersList = partitionChronicle
					.getRefsetMembersActive(vc);
			for (RefexVersionBI<?> loopMember : partitionMembersList) {
				membersNidSet.setMember(loopMember.getReferencedComponentNid());
			}

			activeNid = SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid();
			inactiveNid = SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1
					.getLenient().getNid();
			activeUuid = SnomedMetadataRf1.CURRENT_RF1.getLenient()
					.getPrimUuid();
			inactiveUuid = SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1
					.getLenient().getPrimUuid();
//			assignedNid = Terms
//					.get()
//					.uuidToNative(
//							ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS
//									.getUids());
	        if (Terms.get().getActiveAceFrameConfig().isVaProject()) {
				assignedNid = Terms
						.get()
						.uuidToNative(UUID.fromString("904447b8-d079-5167-8bf0-928bbdcb9e5b"));
			} else {
				assignedNid = Terms
						.get()
						.uuidToNative(UUID.fromString("2cd075aa-fa92-5aa5-9f3d-d68c1c241d42"));
			}
			if (updater != null) {
				updater.setTaskMessage("Initializing WorkList");
				updater.startCount(membersNidSet.cardinality());
			}
			WfInstance instance = new WfInstance();
			WfComponentProvider prov = new WfComponentProvider();
			instance.setComponentId(SNOMED.Concept.ROOT.getPrimoridalUid());
//			instance.setState(prov
//					.statusConceptToWfState(Terms
//							.get()
//							.getConcept(
//									ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS
//											.getUids())));
	        if (Terms.get().getActiveAceFrameConfig().isVaProject()) {
				instance.setState(prov
						.statusConceptToWfState(Terms
								.get()
								.getConcept(UUID.fromString("904447b8-d079-5167-8bf0-928bbdcb9e5b"))));
			} else {
				instance.setState(prov
						.statusConceptToWfState(Terms
								.get()
								.getConcept(UUID.fromString("2cd075aa-fa92-5aa5-9f3d-d68c1c241d42"))));
			}
			instance.setWfDefinition(workList.getWorkflowDefinition());
			instance.setWorkList(workList);
			instance.setLastChangeTime(System.currentTimeMillis());
			user = interpreter.getNextDestination(instance, workList);
			if (user == null) {
				userNid = ArchitectonicAuxiliary.Concept.USER.localize().getNid();
			} else {
				userNid = ts.getNidForUuids(user.getId());
			}
			

		} catch (ValidationException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.tk.api.ContinuationTrackerBI#continueWork()
	 */
	@Override
	public boolean continueWork() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#getNidSet()
	 */
	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return membersNidSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#processUnfetchedConceptData
	 * (int, org.ihtsdo.tk.api.ConceptFetcherBI)
	 */
	@Override
	public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
			throws Exception {
		if (membersNidSet.isMember(cNid)) {
			ConceptVersionBI c = fetcher.fetch(vc);
			if (processConcept(c)) {
				// fetcher.update(c.getChronicle());
				Terms.get().addUncommittedNoChecks(
						(I_GetConceptData) c.getChronicle());
			}
		}

	}

	/**
	 * Process concept.
	 * 
	 * @param concept
	 *            the concept
	 * @return true, if successful
	 */
	private boolean processConcept(ConceptVersionBI concept) {
		boolean update = true;
		if (updater != null) {
			updater.incrementCount();
		}

		try {
			RefexCAB newSpec = new RefexCAB(TK_REFEX_TYPE.CID,
					concept.getNid(), workListNid);
			newSpec.put(RefexProperty.CNID1, activeNid);
			RefexChronicleBI<?> newRefex = tc.constructIfNotCurrent(newSpec);

			RefexCAB newSpecForProm = new RefexCAB(TK_REFEX_TYPE.CID_CID,
					concept.getNid(), promRef.getRefsetId());
			newSpecForProm.put(RefexProperty.CNID1, assignedNid);
			newSpecForProm.put(RefexProperty.CNID2, userNid);
			RefexChronicleBI<?> newRefexForProm = tc
					.constructIfNotCurrent(newSpecForProm);
			concept.addAnnotation(newRefexForProm);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return update;

	}
}
