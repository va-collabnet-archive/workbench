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
package org.ihtsdo.project.view.tag;

import java.util.List;

/**
 * The Class InboxTag.
 */
public class InboxTag {
	public static Integer TERM_WORKLIST_UUID_INDEX = 0;
	public static Integer TERM_UUID_INDEX = 1;
	
	/** The tag name. */
	private String tagName;
	
	/** The color. */
	private String color;
	
	/** The text color. */
	private String textColor;
	
	/** The uuid list. */
	private List<String[]> uuidList;

	/**
	 * Instantiates a new inbox tag.
	 *
	 * @param tagName the tag name
	 * @param color the color
	 * @param textColor the text color
	 * @param uuidList the uuid list
	 */
	public InboxTag(String tagName, String color,String textColor, List<String[]> uuidList) {
		super();
		this.tagName = tagName;
		this.uuidList = uuidList;
		this.textColor = textColor;
		this.color = color;
	}

	/**
	 * Sets the text color.
	 *
	 * @param textColor the new text color
	 */
	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	/**
	 * Gets the text color.
	 *
	 * @return the text color
	 */
	public String getTextColor() {
		return textColor;
	}

	/**
	 * Gets the tag name.
	 *
	 * @return the tag name
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * Sets the tag name.
	 *
	 * @param tagName the new tag name
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * Gets the uuid list.
	 *
	 * @return the uuid list
	 */
	public List<String[]> getUuidList() {
		return uuidList;
	}

	/**
	 * Sets the uuid list.
	 *
	 * @param uuidList the new uuid list
	 */
	public void setUuidList(List<String[]> uuidList) {
		this.uuidList = uuidList;
	}

	/**
	 * Sets the color.
	 *
	 * @param color the new color
	 */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	public String getColor() {
		return color;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InboxTag) {
			InboxTag t = (InboxTag) obj;
			if (this.tagName != null && this.color != null) {
				return this.tagName.equals(t.getTagName()) && this.getColor().equals(t.getColor());
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * To item string.
	 *
	 * @return the string
	 */
	private String toItemString() {
		return TagManager.getInstance().getHeader(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.toItemString();
	}

}
