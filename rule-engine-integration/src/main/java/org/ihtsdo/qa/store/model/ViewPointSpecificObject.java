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
 * The Class ViewPointSpecificObject.
 */
public abstract class ViewPointSpecificObject {
	
	/** The database uuid. */
	private UUID databaseUuid;
	
	/** The path uuid. */
	private UUID pathUuid;
	
	/** The view point time. */
	private String viewPointTime;
	
	/**
	 * Instantiates a new view point specific object.
	 */
	public ViewPointSpecificObject() {
		super();
	}
	
	/**
	 * Gets the path uuid.
	 *
	 * @return the path uuid
	 */
	public UUID getPathUuid() {
		return pathUuid;
	}

	/**
	 * Sets the path uuid.
	 *
	 * @param pathUuid the new path uuid
	 */
	public void setPathUuid(UUID pathUuid) {
		this.pathUuid = pathUuid;
	}

	/**
	 * Gets the view point time.
	 *
	 * @return the view point time
	 */
	public String getViewPointTime() {
		return viewPointTime;
	}

	/**
	 * Sets the view point time.
	 *
	 * @param viewPointTime the new view point time
	 */
	public void setViewPointTime(String viewPointTime) {
		this.viewPointTime = viewPointTime;
	}

	/**
	 * Gets the database uuid.
	 *
	 * @return the database uuid
	 */
	public UUID getDatabaseUuid() {
		return databaseUuid;
	}

	/**
	 * Sets the database uuid.
	 *
	 * @param databaseUuid the new database uuid
	 */
	public void setDatabaseUuid(UUID databaseUuid) {
		this.databaseUuid = databaseUuid;
	}

}
