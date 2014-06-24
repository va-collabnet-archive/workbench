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
 * The Class Category.
 */
public class Category {
	
	/** The category uuid. */
	private UUID categoryUuid;
	
	/** The name. */
	private String name;
	
	/** The description. */
	private String description;
	
	/**
	 * Instantiates a new category.
	 */
	public Category(){
		super();
	}
	
	/**
	 * Instantiates a new category.
	 *
	 * @param severityUuid the severity uuid
	 * @param name the name
	 * @param description the description
	 */
	public Category(UUID severityUuid, String name, String description) {
		super();
		this.categoryUuid = severityUuid;
		this.name = name;
		this.description = description;
	}

	/**
	 * Gets the category uuid.
	 *
	 * @return the category uuid
	 */
	public UUID getCategoryUuid() {
		return categoryUuid;
	}

	/**
	 * Sets the category uuid.
	 *
	 * @param categoryUuid the new category uuid
	 */
	public void setCategoryUuid(UUID categoryUuid) {
		this.categoryUuid = categoryUuid;
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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}
	
}
