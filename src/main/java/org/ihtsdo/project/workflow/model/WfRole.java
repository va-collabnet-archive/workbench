package org.ihtsdo.project.workflow.model;

import java.util.UUID;

public class WfRole {
	
	private String name;
	private UUID id;
	
	public WfRole() {
		super();
	}
	
	public WfRole(String name, UUID id) {
		super();
		this.name = name;
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}

}
