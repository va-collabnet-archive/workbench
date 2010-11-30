package org.ihtsdo.qa.store.model;

import java.util.UUID;

public class Severity {
	private UUID severityUuid;
	private String name;
	private String description;
	
	public Severity(UUID severityUuid, String name, String description) {
		super();
		this.severityUuid = severityUuid;
		this.name = name;
		this.description = description;
	}

	public UUID getSeverityUuid() {
		return severityUuid;
	}

	public void setSeverityUuid(UUID severityUuid) {
		this.severityUuid = severityUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString() {
		return name;
	}
	
}
