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
package org.ihtsdo.qa.gui.viewers;

/**
 * The Class TreeEditorObjectWrapper.
 */
public class TreeEditorObjectWrapper {
	
	/** The name. */
	private String name;
	
	/** The type. */
	private int type;
	
	/** The user object. */
	private Object userObject;
	
	/** The CONCEPT. */
	public static int CONCEPT = 0;
	
	/** The ID. */
	public static int ID = 1;
	
	/** The CONCEPTID. */
	public static int CONCEPTID = 2;
	
	/** The ATTRIBUTE. */
	public static int ATTRIBUTE = 3;
	
	/** The FSNDESCRIPTION. */
	public static int FSNDESCRIPTION = 4;
	
	/** The PREFERRED. */
	public static int PREFERRED = 5;
	
	/** The SYNONYMN. */
	public static int SYNONYMN = 10;
	
	/** The SUPERTYPE. */
	public static int SUPERTYPE = 6;
	
	/** The ROLE. */
	public static int ROLE = 7;
	
	/** The DESCRIPTIONINFO. */
	public static int DESCRIPTIONINFO = 8;
	
	/** The RELATIONSHIPINFO. */
	public static int RELATIONSHIPINFO = 9;
	
	/** The ROLEGROUP. */
	public static int ROLEGROUP = 11;
	
	/** The ASSOCIATION. */
	public static int ASSOCIATION=12;
	
	/** The FOLDER. */
	public static int FOLDER=13;
	
	/** The NOTACCEPTABLE. */
	public static int NOTACCEPTABLE=14;
	
	/** The TRANSLATIO n_ project. */
	public static int TRANSLATION_PROJECT=21;
	
	/**
	 * Instantiates a new tree editor object wrapper.
	 *
	 * @param name the name
	 * @param type the type
	 * @param userObject the user object
	 */
	public TreeEditorObjectWrapper(String name, int type, Object userObject) {
		super();
		this.name = name;
		this.type = type;
		this.userObject = userObject;
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
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	/**
	 * Gets the user object.
	 *
	 * @return the userObject
	 */
	public Object getUserObject() {
		return userObject;
	}
	
	/**
	 * Sets the user object.
	 *
	 * @param userObject the userObject to set
	 */
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String colouredName = name;
		
		if (type == TreeEditorObjectWrapper.DESCRIPTIONINFO || type == TreeEditorObjectWrapper.RELATIONSHIPINFO) {
			colouredName = "<HTML><FONT color = blue size = '-2'>" + colouredName;
		}
		return colouredName;
	}
}
