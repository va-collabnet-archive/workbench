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
 * The Class DispositionStatus.
 */
public class DispositionStatus implements Comparable {
	
	/** The disposition status uuid. */
	private UUID dispositionStatusUuid;
	
	/** The name. */
	private String name;
	
	/**
	 * Instantiates a new disposition status.
	 */
	public DispositionStatus() {
		super();
	}
	
	/**
	 * Instantiates a new disposition status.
	 *
	 * @param dispositionStatusUuid the disposition status uuid
	 * @param name the name
	 */
	public DispositionStatus(UUID dispositionStatusUuid, String name) {
		super();
		this.dispositionStatusUuid = dispositionStatusUuid;
		this.name = name;
	}
	
	/**
	 * Gets the disposition status uuid.
	 *
	 * @return the disposition status uuid
	 */
	public UUID getDispositionStatusUuid() {
		return dispositionStatusUuid;
	}
	
	/**
	 * Sets the disposition status uuid.
	 *
	 * @param dispositionStatusUuid the new disposition status uuid
	 */
	public void setDispositionStatusUuid(UUID dispositionStatusUuid) {
		this.dispositionStatusUuid = dispositionStatusUuid;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object otherObject) {
		DispositionStatus otherStatus = (DispositionStatus) otherObject;
		return otherStatus.getName().compareTo(getName());
	}
}
