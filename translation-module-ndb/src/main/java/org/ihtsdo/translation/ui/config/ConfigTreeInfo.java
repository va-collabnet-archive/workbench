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
package org.ihtsdo.translation.ui.config;

/**
 * The Class ConfigTreeInfo.
 */
public class ConfigTreeInfo {
	
	/** The name. */
	private String name;
	
	/** The item. */
	private String item;

	/**
	 * Instantiates a new config tree info.
	 *
	 * @param name the name
	 * @param item the item
	 */
	public ConfigTreeInfo(String name, String item) {
		super();
		this.name = name;
		this.item = item;
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
	 * Gets the item.
	 *
	 * @return the item
	 */
	public String getItem() {
		return item;
	}

	/**
	 * Sets the item.
	 *
	 * @param item the new item
	 */
	public void setItem(String item) {
		this.item = item;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return this.name;
	}

}
