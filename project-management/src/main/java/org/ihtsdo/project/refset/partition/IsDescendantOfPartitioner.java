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
package org.ihtsdo.project.refset.partition;

import java.io.Serializable;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * The Class IsDescendantOfPartitioner.
 */
public class IsDescendantOfPartitioner extends RefsetPartitioner implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The parent. */
	I_GetConceptData parent;
	
	/**
	 * Instantiates a new checks if is descendant of partitioner.
	 */
	public IsDescendantOfPartitioner() {
		super();
	}
	
	/**
	 * Instantiates a new checks if is descendant of partitioner.
	 *
	 * @param parent the parent
	 */
	public IsDescendantOfPartitioner(I_GetConceptData parent) {
		super();
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.partition.RefsetPartitioner#evaluateMember(org.dwfa.ace.api.I_GetConceptData, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	protected boolean evaluateMember(I_GetConceptData member, I_ConfigAceFrame config) {
		Boolean result = false;
		
		try {
			I_IntSet allowedTypes = Terms.get().newIntSet();
			allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			result = parent.isParentOfOrEqualTo(member); 
//			result = parent.isParentOfOrEqualTo(member, config.getAllowedStatus(), allowedTypes,
//					config.getViewPositionSetReadOnly(), 
//					config.getPrecedence(), config.getConflictResolutionStrategy());
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	public I_GetConceptData getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	public void setParent(I_GetConceptData parent) {
		this.parent = parent;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Is descendant of";
	}

}
