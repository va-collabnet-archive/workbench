package org.ihtsdo.project.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

public class WorklistMetadata implements Serializable{
	/** The name. */
	private String name;
	
	/** The id. */
	private int id;
	
	/** The uids. */
	private List<UUID> uids;
	
	/** The partition id. */
	private UUID partitionUUID;
	
	private WorkflowDefinition workflowDefinition;
	
	private String workflowDefinitionFileName;
	
	private List<WfMembership> workflowUserRoles;

	public WorklistMetadata(String name, int id, List<UUID> uids,
			UUID partitionUUID, WorkflowDefinition workflowDefinition,
			String workflowDefinitionFileName,
			List<WfMembership> workflowUserRoles) {
		super();
		this.name = name;
		this.id = id;
		this.uids = uids;
		this.partitionUUID = partitionUUID;
		this.workflowDefinition = workflowDefinition;
		this.workflowDefinitionFileName = workflowDefinitionFileName;
		this.workflowUserRoles = workflowUserRoles;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<UUID> getUids() {
		return uids;
	}

	public void setUids(List<UUID> uids) {
		this.uids = uids;
	}

	public UUID getPartitionUUID() {
		return partitionUUID;
	}

	public void setPartitionUUID(UUID partitionUUID) {
		this.partitionUUID = partitionUUID;
	}

	public WorkflowDefinition getWorkflowDefinition() {
		return workflowDefinition;
	}

	public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
		this.workflowDefinition = workflowDefinition;
	}

	public String getWorkflowDefinitionFileName() {
		return workflowDefinitionFileName;
	}

	public void setWorkflowDefinitionFileName(String workflowDefinitionFileName) {
		this.workflowDefinitionFileName = workflowDefinitionFileName;
	}

	public List<WfMembership> getWorkflowUserRoles() {
		return workflowUserRoles;
	}

	public void setWorkflowUserRoles(List<WfMembership> workflowUserRoles) {
		this.workflowUserRoles = workflowUserRoles;
	}
}
