package org.ihtsdo.project.workflow.model;

import java.util.UUID;

public class WfPermission {
	
	private UUID id;
	private WfRole role;
	private UUID hiearchyId;
	
	public WfPermission() {
		super();
	}
	
	public WfPermission(UUID id, WfRole role, UUID hiearchyId) {
		super();
		this.id = id;
		this.role = role;
		this.hiearchyId = hiearchyId;
	}
	
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public UUID getHiearchyId() {
		return hiearchyId;
	}
	public void setHiearchyId(UUID hiearchyId) {
		this.hiearchyId = hiearchyId;
	}
	
	public String getRoleName() {
		return role.getName();
	}

	public WfRole getRole() {
		return role;
	}

	public void setRole(WfRole role) {
		this.role = role;
	}
}
