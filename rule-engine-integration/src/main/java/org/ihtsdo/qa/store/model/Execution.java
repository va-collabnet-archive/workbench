/*
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
package org.ihtsdo.qa.store.model;

import java.util.Date;
import java.util.UUID;

/**
 * The Class Execution.
 */
public class Execution extends ViewPointSpecificObject {
	
	/** The execution uuid. */
	private UUID executionUuid;
	
	/** The name. */
	private String name;
	
	/** The date. */
	private Date date;
	
	/** The description. */
	private String description;
	
	/** The context name. */
	private String contextName;
	
	/** The start time. */
	private Date startTime;
	
	/** The end time. */
	private Date endTime;
	
	/**
	 * Instantiates a new execution.
	 */
	public Execution() {
		super();
	}

	/**
	 * Gets the execution uuid.
	 *
	 * @return the execution uuid
	 */
	public UUID getExecutionUuid() {
		return executionUuid;
	}

	/**
	 * Sets the execution uuid.
	 *
	 * @param executionUuid the new execution uuid
	 */
	public void setExecutionUuid(UUID executionUuid) {
		this.executionUuid = executionUuid;
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
	 * Gets the date.
	 *
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the date.
	 *
	 * @param date the new date
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the context name.
	 *
	 * @return the context name
	 */
	public String getContextName() {
		return contextName;
	}

	/**
	 * Sets the context name.
	 *
	 * @param contextName the new context name
	 */
	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * Sets the start time.
	 *
	 * @param startTime the new start time
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * Gets the end time.
	 *
	 * @return the end time
	 */
	public Date getEndTime() {
		return endTime;
	}

	/**
	 * Sets the end time.
	 *
	 * @param endTime the new end time
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}
