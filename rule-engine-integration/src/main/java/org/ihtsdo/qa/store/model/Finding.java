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

import java.util.UUID;

/**
 * The Class Finding.
 */
public class Finding extends ViewPointSpecificObject {
	
	/** The finding uuid. */
	private UUID findingUuid;
	
	/** The execution uuid. */
	private UUID executionUuid;
	
	/** The rule uuid. */
	private UUID ruleUuid;
	
	/** The component uuid. */
	private UUID componentUuid;
    
    /** The details. */
    private String details;

	/**
	 * Instantiates a new finding.
	 */
	public Finding() {
	}

	/**
	 * Gets the finding uuid.
	 *
	 * @return the finding uuid
	 */
	public UUID getFindingUuid() {
		return findingUuid;
	}

	/**
	 * Sets the finding uuid.
	 *
	 * @param findingUuid the new finding uuid
	 */
	public void setFindingUuid(UUID findingUuid) {
		this.findingUuid = findingUuid;
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
	 * Gets the rule uuid.
	 *
	 * @return the rule uuid
	 */
	public UUID getRuleUuid() {
		return ruleUuid;
	}

	/**
	 * Sets the rule uuid.
	 *
	 * @param ruleUuid the new rule uuid
	 */
	public void setRuleUuid(UUID ruleUuid) {
		this.ruleUuid = ruleUuid;
	}

	/**
	 * Gets the component uuid.
	 *
	 * @return the component uuid
	 */
	public UUID getComponentUuid() {
		return componentUuid;
	}

	/**
	 * Sets the component uuid.
	 *
	 * @param componentUuid the new component uuid
	 */
	public void setComponentUuid(UUID componentUuid) {
		this.componentUuid = componentUuid;
	}

	/**
	 * Gets the details.
	 *
	 * @return the details
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * Sets the details.
	 *
	 * @param details the new details
	 */
	public void setDetails(String details) {
		this.details = details;
	}

}
