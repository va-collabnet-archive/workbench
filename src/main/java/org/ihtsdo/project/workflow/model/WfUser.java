package org.ihtsdo.project.workflow.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WfUser {
	
	private String username;
	private UUID id;
	private List<WfPermission> permissions;
	
	public WfUser() {
		super();
	}
	
	public WfUser(String username, UUID id) {
		super();
		this.username = username;
		this.id = id;
		this.permissions = new ArrayList<WfPermission>();
	}

	public WfUser(String username, UUID id, List<WfPermission> permissions) {
		super();
		this.username = username;
		this.id = id;
		this.permissions = permissions;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public List<WfPermission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<WfPermission> permissions) {
		this.permissions = permissions;
	}
	
	public String toString() {
		return username;
	}

}
