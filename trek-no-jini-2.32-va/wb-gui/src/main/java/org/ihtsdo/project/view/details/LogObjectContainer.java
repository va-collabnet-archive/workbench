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
package org.ihtsdo.project.view.details;

import org.dwfa.ace.api.I_AmPart;

/**
 * The Class LogObjectContainer.
 */
public class LogObjectContainer {
	
	/**
	 * The Enum PARTS.
	 */
	public enum PARTS{
/** The PROMOTIO n_ part. */
PROMOTION_PART,
/** The DESCRIPTIO n_ part. */
DESCRIPTION_PART,
/** The LAN g_ refse t_ part. */
LANG_REFSET_PART,
/** The COMMENT s_ part. */
COMMENTS_PART,
/** The ISSU e_ part. */
ISSUE_PART};
	
	/** The part type. */
	private PARTS partType;

	/** The part. */
	private I_AmPart part;
	
	/** The string part. */
	private String stringPart;
	
	/** The did. */
	private Integer did;

	/** The user name. */
	private String userName;

	/**
	 * Instantiates a new log object container.
	 *
	 * @param logObjectContainerType the log object container type
	 * @param part the part
	 * @param did the did
	 */
	public LogObjectContainer(PARTS logObjectContainerType, I_AmPart part, Integer did){
		this.partType=logObjectContainerType;
		this.part=part;
		this.did=did;
	}
	
	/**
	 * Instantiates a new log object container.
	 *
	 * @param logObjectContainerType the log object container type
	 * @param stringPart the string part
	 */
	public LogObjectContainer(PARTS logObjectContainerType, String stringPart){
		this.partType=logObjectContainerType;
		this.stringPart=stringPart;
	}
	
	/**
	 * Instantiates a new log object container.
	 *
	 * @param logObjectContainerType the log object container type
	 * @param stringPart the string part
	 * @param userName the user name
	 */
	public LogObjectContainer(PARTS logObjectContainerType, String stringPart,String userName){
		this.partType=logObjectContainerType;
		this.stringPart=stringPart;
		this.userName=userName;
	}

	/**
	 * Gets the part type.
	 *
	 * @return the part type
	 */
	public PARTS getPartType() {
		return partType;
	}

	/**
	 * Gets the part.
	 *
	 * @return the part
	 */
	public I_AmPart getPart() {
		return part;
	}
	
	/**
	 * Gets the string part.
	 *
	 * @return the string part
	 */
	public String getStringPart() {
		return stringPart;
	}
	
	/**
	 * Gets the did.
	 *
	 * @return the did
	 */
	public Integer getDid() {
		return did;
	}
	
	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}


}
