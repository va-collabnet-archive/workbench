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
 * The Class QADatabase.
 */
public class QADatabase {
	
	/** The database uuid. */
	private UUID databaseUuid;
	
	/** The name. */
	private String name;
	
	/**
	 * Instantiates a new qA database.
	 *
	 * @param databaseUuid the database uuid
	 * @param name the name
	 */
	public QADatabase(UUID databaseUuid, String name) {
		super();
		this.databaseUuid = databaseUuid;
		this.name = name;
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
		return name;
	}

}
