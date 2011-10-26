/**
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
package org.ihtsdo.translation.ui;

import org.dwfa.ace.api.I_DescriptionTuple;

/**
 * The Class DescriptionTreeNode.
 */
public class DescriptionTreeNode {
	
	/** The description. */
	private I_DescriptionTuple description;
	
	/** The LABE l_ type. */
	public static int LABEL_TYPE = 0;
	
	/** The DESCRIPTIO n_ type. */
	public static int DESCRIPTION_TYPE = 1;
	
	/** The ATTRIBUT e_ type. */
	public static int ATTRIBUTE_TYPE = 2;
	
	/** The type. */
	private int type;
	
	/** The name. */
	private String name;

	/**
	 * Instantiates a new description tree node.
	 * 
	 * @param name the name
	 * @param type the type
	 * @param description the description
	 */
	public DescriptionTreeNode(String name, int type, I_DescriptionTuple description) {
		super();
		this.description = description;
		this.type = type;
		this.name = name;
	}

	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public I_DescriptionTuple getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 * 
	 * @param description the new description
	 */
	public void setDescription(I_DescriptionTuple description) {
		this.description = description;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = "";
		if (type == DescriptionTreeNode.DESCRIPTION_TYPE) {
			result = "<html><body><b>" + description.getLang() + "</b>:<i> " + description.getText();
			if (description.getVersion() == Integer.MAX_VALUE) {
				result = result + " - Uncommited " + "</i></body></html>";
			} else {
				result = result + "" + "</i></body></html>";
			}
		} else if (type == DescriptionTreeNode.LABEL_TYPE) {
			result = name;
		}
		
		return result;
			
		//return description.getLang() + ": " + description.getText();
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type the new type
	 */
	public void setType(int type) {
		this.type = type;
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
}
