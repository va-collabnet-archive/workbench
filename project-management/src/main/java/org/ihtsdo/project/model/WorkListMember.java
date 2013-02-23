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
package org.ihtsdo.project.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.model.WfInstance;

/**
 * The Class WorkListMember.
 */
public class WorkListMember implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The name. */
	private String name;
	
	/** The id. */
	private int id;
	
	/** The uids. */
	private List<UUID> uids;
	
	/** The work list id. */
	private UUID workListUUID;
	
	/** The activity status. */
	private I_GetConceptData activityStatus;
	
	/** The status last date. */
	private Long statusDate;
	
	/** The wf instance. */
	private WfInstance wfInstance;
	
	/**
	 * Instantiates a new work list member.
	 *
	 * @param name the name
	 * @param id the id
	 * @param uids the uids
	 * @param workListUUID the work list uuid
	 * @param activityStatus the activity status
	 * @param statusDate the status date
	 */
	public WorkListMember(String name, int id, List<UUID> uids,
			UUID workListUUID,
			I_GetConceptData activityStatus, Long statusDate) {
		super();
		this.name = name;
		this.id = id;
		this.uids = uids;
		this.workListUUID = workListUUID;
		this.activityStatus = activityStatus;
		this.statusDate=statusDate;
//		this.businessProcessWithAttachments = businessProcessWithAttachments;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Gets the uids.
	 * 
	 * @return the uids
	 */
	public List<UUID> getUids() {
		return uids;
	}
	
	/**
	 * Sets the uids.
	 * 
	 * @param uids the new uids
	 */
	public void setUids(List<UUID> uids) {
		this.uids = uids;
	}
	
	/**
	 * Gets the concept.
	 * 
	 * @return the concept
	 */
	public I_GetConceptData getConcept() {
		I_GetConceptData concept = null;
		try {
			concept = Terms.get().getConcept(uids);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return concept;
	}
	
	/**
	 * Gets the work list id.
	 * 
	 * @return the work list id
	 */
	public UUID getWorkListUUID() {
		return workListUUID;
	}
	
	/**
	 * Sets the work list id.
	 *
	 * @param workListUUID the new work list uuid
	 */
	public void setWorkListUUID(UUID workListUUID) {
		this.workListUUID = workListUUID;
	}
	
	
	/**
	 * Gets the activity status.
	 * 
	 * @return the activity status
	 */
	public I_GetConceptData getActivityStatus() {
		return activityStatus;
	}
	
	/**
	 * Sets the activity status.
	 * 
	 * @param activityStatus the new activity status
	 */
	public void setActivityStatus(I_GetConceptData activityStatus) {
		this.activityStatus = activityStatus;
	}
	
//	/**
//	 * Gets the business process with attachments.
//	 * 
//	 * @return the business process with attachments
//	 */
//	public BusinessProcess getBusinessProcessWithAttachments() {
//		return businessProcessWithAttachments;
//	}
//	
//	/**
//	 * Sets the business process with attachments.
//	 * 
//	 * @param businessProcessWithAttachments the new business process with attachments
//	 */
//	public void setBusinessProcessWithAttachments(
//			BusinessProcess businessProcessWithAttachments) {
//		this.businessProcessWithAttachments = businessProcessWithAttachments;
//	}
	
	/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString() {
		return this.name;
	}
	
	/**
	 * Gets the last author name.
	 *
	 * @return the last author name
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getLastAuthorName() throws TerminologyException, IOException {
		String name = "";
		I_TermFactory termFactory = Terms.get();
		//TODO: add as parameter
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
		I_GetConceptData workListConcept = termFactory.getConcept(this.workListUUID);
		WorkList workList = TerminologyProjectDAO.getWorkList(workListConcept, config);
		I_GetConceptData lastAuthor = workList.getPromotionRefset(config).getLastPromotionAuthor(id, config);
//		for (I_ExtendByRef promotionMember : termFactory.getAllExtensionsForComponent(id)) {
//			if (promotionMember.getRefsetId() == workList.getPromotionRefset(config).getRefsetId()) {
//				long lastVersion = Long.MIN_VALUE;
//				I_ExtendByRefPartCid promotionExtensionPart = null;
//				List<? extends I_ExtendByRefPart> loopParts = promotionMember.getMutableParts();
//				for (I_ExtendByRefPart loopPart : loopParts) {
//					Collection<ComponentVersionBI> listVersions = loopPart.getVersions(config.getViewCoordinate());
//					for (ComponentVersionBI ver : listVersions) {
//						if(lastVersion >= lastVersion){
//							lastVersion = ver.getTime();
//							promotionExtensionPart = (I_ExtendByRefPartCid) loopPart;
//						}
//					}
//				}
//				name = termFactory.getConcept(promotionExtensionPart.getAuthorNid()).toString();
//			}
//		}
		
		return lastAuthor.toString();
	}

	/**
	 * Gets the status date.
	 *
	 * @return the status date
	 */
	public Long getStatusDate() {
		return statusDate;
	}

	/**
	 * Sets the status date.
	 *
	 * @param statusDate the new status date
	 */
	public void setStatusDate(Long statusDate) {
		this.statusDate = statusDate;
	}

	/**
	 * Gets the wf instance.
	 *
	 * @return the wf instance
	 */
	public WfInstance getWfInstance() {
		WfComponentProvider provider = new WfComponentProvider();
		wfInstance = new WfInstance();
		try {
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			WorkList workList = TerminologyProjectDAO.getWorkList(tf.getConcept(workListUUID), 
					config);
			wfInstance.setComponentId(tf.nidToUuid(id));
			wfInstance.setWfDefinition(workList.getWorkflowDefinition());
			wfInstance.setWorkList(workList);
			wfInstance.setComponentName(getName());
			
			PromotionAndAssignmentRefset promAssignRefset = workList.getPromotionRefset(config);
			
			wfInstance.setState(provider.statusConceptToWfState(
					promAssignRefset.getPromotionStatus(id, config)));
			wfInstance.setDestination(provider.userConceptToWfUser(
					promAssignRefset.getDestination(id, config)));
			wfInstance.setLastChangeTime(promAssignRefset.getLastStatusTime(id, config));
			
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		
		return wfInstance;
	}
	
}
