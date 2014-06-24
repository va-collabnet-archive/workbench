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
 * The Class TerminologyComponent.
 */
public class TerminologyComponent {
	
	/** The component uuid. */
	private UUID componentUuid;
	
	/** The component name. */
	private String componentName;
	
	/** The sctid. */
	private Long sctid;
	
	/**
	 * Instantiates a new terminology component.
	 */
	public TerminologyComponent() {
		super();
	}
	
	/**
	 * Instantiates a new terminology component.
	 *
	 * @param componentUuid the component uuid
	 * @param componentName the component name
	 * @param sctid the sctid
	 */
	public TerminologyComponent(UUID componentUuid, String componentName,
			Long sctid) {
		super();
		this.componentUuid = componentUuid;
		this.componentName = componentName;
		this.sctid = sctid;
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
	 * Gets the component name.
	 *
	 * @return the component name
	 */
	public String getComponentName() {
		return componentName;
	}

	/**
	 * Sets the component name.
	 *
	 * @param componentName the new component name
	 */
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	/**
	 * Gets the sctid.
	 *
	 * @return the sctid
	 */
	public Long getSctid() {
		return sctid;
	}

	/**
	 * Sets the sctid.
	 *
	 * @param sctid the new sctid
	 */
	public void setSctid(Long sctid) {
		this.sctid = sctid;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getComponentName();
	}

}
