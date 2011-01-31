package org.ihtsdo.qadb.data;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class Execution extends ViewPointSpecificObject {
	
	private String executionUuid;
	private String name;
	private Date date;
	private String description;
	private String pathName;
	private String contextName;
	private Calendar startTime;
	private Calendar endTime;
	
	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public Execution() {
		super();
	}

	public String getExecutionUuid() {
		return executionUuid;
	}

	public void setExecutionUuid(String executionUuid) {
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

	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

}
