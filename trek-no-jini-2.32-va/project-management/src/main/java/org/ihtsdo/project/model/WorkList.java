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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.refset.WorkflowRefset;
import org.ihtsdo.project.workflow.api.WorkflowDefinitionManager;
import org.ihtsdo.project.workflow.api.wf2.implementation.WfProcessDefinition;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.workflow.api.WfProcessDefinitionBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WorkListBI;

/**
 * The Class WorkList.
 */
public class WorkList extends WorkflowRefset implements Serializable, WorkListBI, Comparable<WorkList>{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The name. */
	private String name;

	/** The id. */
	private int id;

	/** The uids. */
	private List<UUID> uids;

	/** The partition id. */
	private UUID partitionUUID;

	/** The workflow definition. */
	private WorkflowDefinition workflowDefinition;

	/** The workflow user roles. */
	private List<WfMembership> workflowUserRoles;

	/** The prom refset. */
	private PromotionAndAssignmentRefset promRefset;


	/**
	 * Instantiates a new work list.
	 */
	public WorkList() {
		super();
	}

	/**
	 * Instantiates a new work list.
	 *
	 * @param name the name
	 * @param id the id
	 * @param uids the uids
	 * @param workSetUUID the work set uuid
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public WorkList(String name, int id, List<UUID> uids,UUID workSetUUID) throws TerminologyException, IOException {
		super();
		this.name = name;
		if (uids!=null)
			this.refsetConcept=Terms.get().getConcept(uids);
		this.id = id;
		this.refsetId=id;
		this.uids = uids;
		this.partitionUUID = workSetUUID;

	}

	/**
	 * Gets the instance from metadata.
	 *
	 * @param worklistMetadata the worklist metadata
	 * @return the instance from metadata
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static WorkList getInstanceFromMetadata(WorklistMetadata worklistMetadata) throws Exception{
		WorkList worklist=new WorkList(worklistMetadata.getName(),
				Terms.get().uuidToNative(worklistMetadata.getUids())
				,worklistMetadata.getUids(),worklistMetadata.getPartitionUUID());
		worklist.setWorkflowDefinition(WorkflowDefinitionManager.readWfDefinition(worklistMetadata.getWorkflowDefinitionFileName()));
		worklist.setWorkflowUserRoles(TerminologyProjectDAO.convertToMembershipList(worklistMetadata.getWorkflowUserRoles()));
		return worklist;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	@Override
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
	 * Gets the work list members.
	 *
	 * @return the work list members
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<WorkListMember> getWorkListMembers() throws TerminologyException, IOException {
		return TerminologyProjectDAO.getAllWorkListMembers(this, Terms.get().getActiveAceFrameConfig());
	}

	/**
	 * Gets the work set id.
	 * 
	 * @return the work set id
	 */
	public UUID getPartitionUUID() {
		return partitionUUID;
	}

	/**
	 * Sets the work set id.
	 *
	 * @param partitionUUID the new partition uuid
	 */
	public void setPartitionUUID(UUID partitionUUID) {
		this.partitionUUID = partitionUUID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.name;
	}

	/**
	 * Gets the partition.
	 *
	 * @return the partition
	 */
	public Partition getPartition() {
		Partition partition = null;
		I_GetConceptData concept = null;
		I_ConfigAceFrame config = null;
		try {
			concept = Terms.get().getConcept(partitionUUID);
			config = Terms.get().getActiveAceFrameConfig();
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		partition = TerminologyProjectDAO.getPartition(concept, config);
		return partition;
	}

	/**
	 * Gets the workflow definition.
	 *
	 * @return the workflow definition
	 */
	public WorkflowDefinition getWorkflowDefinition() {
		return workflowDefinition;
	}

	/**
	 * Sets the workflow definition.
	 *
	 * @param workflowDefinition the new workflow definition
	 */
	public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
		this.workflowDefinition = workflowDefinition;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.WorkflowRefset#getPromotionRefset(org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	public PromotionAndAssignmentRefset getPromotionRefset(I_ConfigAceFrame config) {

		if (promRefset != null) {
			return promRefset;
		} else {
			try {
				I_GetConceptData promotionRel = termFactory.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
				I_GetConceptData refsetConcept = getRefsetConcept();
				if (refsetConcept == null) {
					return null;
				}
				promRefset = new PromotionAndAssignmentRefset(getLatestSourceRelationshipTarget(refsetConcept, promotionRel, config));
				return promRefset;
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
				return null;
			}
		}
	}

	/**
	 * Gets the workflow user roles.
	 *
	 * @return the workflow user roles
	 */
	public List<WfMembership> getWorkflowUserRoles() {
		return workflowUserRoles;
	}

	/**
	 * Sets the workflow user roles.
	 *
	 * @param workflowUserRoles the new workflow user roles
	 */
	public void setWorkflowUserRoles(List<WfMembership> workflowUserRoles) {
		this.workflowUserRoles = workflowUserRoles;
	}

	/**
	 * Gets the users.
	 *
	 * @return the users
	 */
	public List<WfUser> getUsers() {
		List<WfUser> users = new ArrayList<WfUser>();

		for (WfMembership loopMembership : getWorkflowUserRoles()) {
			users.add(loopMembership.getUser());
		}

		return users;
	}

	/**
	 * Gets the workflow definition file name.
	 *
	 * @return the workflow definition file name
	 */
	public String getWorkflowDefinitionFileName() {
		if (workflowDefinition != null){
			return workflowDefinition.getName() + ".wfd";
		}
		return null;

	}

	@Override
	public UUID getUuid() {
		return getUids().iterator().next();
	}

	@Override
	public Collection<WfProcessInstanceBI> getInstances() throws Exception {
		List<WfProcessInstanceBI> instances = new ArrayList<WfProcessInstanceBI>();
		for (WorkListMember loopMember : TerminologyProjectDAO.getAllWorkListMembers(this, Terms.get().getActiveAceFrameConfig())) {
			instances.add(loopMember.getWfInstance());
		}
		return instances;
	}

	@Override
	public String getDescription() {
		return this.getName();
	}

	@Override
	public WfProcessInstanceBI createInstanceForComponent(UUID componentUuid,
			WfProcessDefinitionBI definition) throws Exception {
		// Definition is ignored, managed at worklist level in legacy implementation
		// Component is expected to be concept
		// default to commit=true
		WorkListMember newMember = TerminologyProjectDAO.addConceptAsNacWorklistMember(this, 
				Terms.get().getConcept(componentUuid), 
				Terms.get().getActiveAceFrameConfig(), true);
		
		return newMember.getWfInstance();
	}
	
	public WfProcessInstanceBI createInstanceForComponent(UUID componentUuid,
			WfProcessDefinitionBI definition, boolean commit) throws Exception {
		// Definition is ignored, managed at worklist level in legacy implementation
		// Component is expected to be concept
		WorkListMember newMember = TerminologyProjectDAO.addConceptAsNacWorklistMember(this, 
				Terms.get().getConcept(componentUuid), 
				Terms.get().getActiveAceFrameConfig(), commit);
		
		return newMember.getWfInstance();
		
	}

	@Override
	public int compareTo(WorkList o) {
		return this.name.compareTo(o.getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkList other = (WorkList) obj;
		if (id != other.id)
			return false;
		return true;
	}

	
}
