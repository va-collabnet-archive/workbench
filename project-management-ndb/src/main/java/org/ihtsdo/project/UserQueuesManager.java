package org.ihtsdo.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.WorklistMetadata;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;

public class UserQueuesManager {

	I_ConfigAceFrame config;
	I_TermFactory tf;

	public UserQueuesManager(I_ConfigAceFrame config) {
		super();
		this.config = config;
		tf = Terms.get();
	}

	public I_ConfigAceFrame getConfig() {
		return config;
	}

	public void setConfig(I_ConfigAceFrame config) {
		this.config = config;
	}

	public WfState getState(int promotionStatusNid) {
		WfState state = new WfState();

		//TODO: implement

		return state;
	}

	public WfUser getUser(int destinationNid) {
		WfUser user = new WfUser();

		//TODO: implement

		return user;
	}

	public void persistInstanceChanges(WfInstance instance) {
		try {
			WorkList workList = instance.getWorkList();
			PromotionAndAssignmentRefset promDestRefset = workList.getPromotionRefset(config);

			if (!promDestRefset.getDestination(tf.uuidToNative(
					instance.getComponentId()), config).getUids().contains(instance.getDestination().getId())) {
				setDestination(instance, instance.getDestination());
			}

			if (!promDestRefset.getPromotionStatus(tf.uuidToNative(
					instance.getComponentId()), config).getUids().contains(instance.getState().getId())) {
				setStatus(instance, instance.getState());
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	private void setDestination(WfInstance instance, WfUser destinationUser) {
		try {
			WorkList workList = instance.getWorkList();
			PromotionAndAssignmentRefset promDestRefset = workList.getPromotionRefset(config);
			promDestRefset.setDestination(tf.uuidToNative(instance.getComponentId()), 
					tf.uuidToNative(destinationUser.getId()));
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	private void setStatus(WfInstance instance, WfState newStatus) {
		try {
			WorkList workList = instance.getWorkList();
			PromotionAndAssignmentRefset promDestRefset = workList.getPromotionRefset(config);
			promDestRefset.setPromotionStatus(tf.uuidToNative(instance.getComponentId()), 
					tf.uuidToNative(newStatus.getId()));
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}


	public List<WfInstance> getAssignmentsForUser(WfUser user) {
		List<WfInstance> items = new ArrayList<WfInstance>();
		try {
			int userNid = tf.uuidToNative(user.getId());
			I_GetConceptData workListRefset = tf.getConcept(
					ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.getUids());
			WorkList deserializedWorkListWithMetadata = null;
			for (I_ExtendByRef extension : tf.getRefsetExtensionMembers(workListRefset.getConceptNid())) {
				I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(extension);
				I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
				String metadata = part.getStringValue();
				WorklistMetadata deserializedWorkListMetadata = (WorklistMetadata) TerminologyProjectDAO.deserialize(metadata);
				deserializedWorkListWithMetadata=WorkList.getInstanceFromMetadata(deserializedWorkListMetadata);
				if (deserializedWorkListWithMetadata.getUsers().contains(user)) {
					PromotionAndAssignmentRefset promDestRefset = deserializedWorkListWithMetadata.getPromotionRefset(config);
					for (WorkListMember loopMember : deserializedWorkListWithMetadata.getWorkListMembers()) {
						I_GetConceptData destination = promDestRefset.getDestination(loopMember.getId(), config);
						if (userNid == destination.getNid()) {
							WfInstance instance = new WfInstance();
							instance.setComponentId(loopMember.getUids().iterator().next());
							instance.setState(getState(promDestRefset.getPromotionStatus(loopMember.getId(), config).getNid()));
							instance.setWfDefinition(deserializedWorkListWithMetadata.getWorkflowDefinition());
							instance.setWorkList(deserializedWorkListWithMetadata);
							instance.setDestination(user);
							items.add(instance);
						}
					}
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return items;
	}



}
