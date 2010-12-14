package org.ihtsdo.qa.store.model;

import java.util.Date;
import java.util.UUID;

public class Execution extends ViewPointSpecificObject {
	
	private UUID executionUuid;
	private String name;
	private Date date;
	private String description;
	private String contextName;
	private Date startTime;
	private Date endTime;
	
	public Execution() {
		super();
	}

	public UUID getExecutionUuid() {
		return executionUuid;
	}

	public void setExecutionUuid(UUID executionUuid) {
		this.executionUuid = executionUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}
