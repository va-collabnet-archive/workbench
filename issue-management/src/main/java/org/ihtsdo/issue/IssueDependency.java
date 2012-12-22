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
package org.ihtsdo.issue;

import java.io.Serializable;

/**
 * The Class IssueDependency.
 */
public class IssueDependency implements Serializable {

	/** The issue target. */
	private Issue issueTarget;
	
	/** The Description. */
	private String Description;
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;


	/**
	 * Gets the target issue.
	 *
	 * @return the target issue
	 */
	public Issue getTargetIssue() {
		return issueTarget;
	}

	/**
	 * Sets the target issue.
	 *
	 * @param issueTarget the new target issue
	 */
	public void setTargetIssue(Issue issueTarget) {
		this.issueTarget = issueTarget;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return Description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		Description = description;
	}
	
}
