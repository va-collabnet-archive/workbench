/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api;


// TODO: Auto-generated Javadoc
/**
 * The Class Coordinate.
 */
public class Coordinate {
	
	/** The precedence. */
	private Precedence precedence;
	
	/** The position set. */
	private PositionSetBI positionSet;
	
	/** The allowed status nids. */
	private NidSetBI   allowedStatusNids;
	
	/** The isa type nids. */
	private NidSetBI   isaTypeNids;
	
	/** The contradiction manager. */
	private ContradictionManagerBI contradictionManager;
	
	/** The language nid. */
	private int languageNid;

	/**
	 * Instantiates a new coordinate.
	 *
	 * @param precedence the precedence
	 * @param positionSet the position set
	 * @param allowedStatusNids the allowed status nids
	 * @param isaTypeNids the isa type nids
	 * @param contradictionManager the contradiction manager
	 * @param languageNid the language nid
	 */
	public Coordinate(Precedence precedence, PositionSetBI positionSet,
			NidSetBI allowedStatusNids, NidSetBI isaTypeNids, 
			ContradictionManagerBI contradictionManager, 
			int languageNid) {
		super();
		assert precedence != null;
		assert positionSet != null;
		assert allowedStatusNids != null;
		assert isaTypeNids != null;
		assert contradictionManager != null;
		this.precedence = precedence;
		this.positionSet = positionSet;
		this.allowedStatusNids = allowedStatusNids;
		this.isaTypeNids = isaTypeNids;
		this.contradictionManager = contradictionManager;
		this.languageNid = languageNid;
	}
	
	/**
	 * Gets the position set.
	 *
	 * @return the position set
	 */
	public PositionSetBI getPositionSet() {
		return positionSet;
	}

	/**
	 * Gets the allowed status nids.
	 *
	 * @return the allowed status nids
	 */
	public NidSetBI getAllowedStatusNids() {
		return allowedStatusNids;
	}

	/**
	 * Gets the precedence.
	 *
	 * @return the precedence
	 */
	public Precedence getPrecedence() {
		return precedence;
	}
	
	/**
	 * Gets the isa type nids.
	 *
	 * @return the isa type nids
	 */
	public NidSetBI getIsaTypeNids() {
		return isaTypeNids;
	}

	/**
	 * Gets the contradiction manager.
	 *
	 * @return the contradiction manager
	 */
	public ContradictionManagerBI getContradictionManager() {
		return contradictionManager;
	}
	
	/**
	 * Gets the language nid.
	 *
	 * @return the language nid
	 */
	public int getLanguageNid() {
		return languageNid;
	}

}
