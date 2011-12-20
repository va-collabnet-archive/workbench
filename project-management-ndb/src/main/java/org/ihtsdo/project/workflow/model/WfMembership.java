package org.ihtsdo.project.workflow.model;

import java.io.Serializable;
import java.util.UUID;

public class WfMembership implements Serializable{
	
	private UUID id;
	private WfUser user;
	private WfRole role;
	private boolean defaultAssignment;
	
	public WfMembership(UUID id, WfUser user, WfRole role, boolean defaultAssignment) {
		super();
		this.id = id;
		this.user = user;
		this.role = role;
		this.defaultAssignment = defaultAssignment;
	}

	public WfUser getUser() {
		return user;
	}

	public void setUser(WfUser user) {
		this.user = user;
	}

	public WfRole getRole() {
		return role;
	}

	public void setRole(WfRole role) {
		this.role = role;
	}

	public boolean isDefaultAssignment() {
		return defaultAssignment;
	}

	public void setDefaultAssignment(boolean defaultAssignment) {
		this.defaultAssignment = defaultAssignment;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
	
}
