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
package org.ihtsdo.translation.ui;

import java.io.Serializable;

/**
 * The Class ThinFolderTreeStructure.
 */
public class ThinFolderTreeStructure implements Serializable {

	/** The folder name. */
	private String folderName;
	
	/** The children. */
	private ThinFolderTreeStructure[] children;
	
	/**
	 * Gets the folder name.
	 *
	 * @return the folder name
	 */
	public String getFolderName() {
		return folderName;
	}
	
	/**
	 * Sets the folder name.
	 *
	 * @param folderName the new folder name
	 */
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	
	/**
	 * Gets the children.
	 *
	 * @return the children
	 */
	public ThinFolderTreeStructure[] getChildren() {
		return children;
	}
	
	/**
	 * Sets the children.
	 *
	 * @param children the new children
	 */
	public void setChildren(ThinFolderTreeStructure[] children) {
		this.children = children;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return folderName;
	}
}
