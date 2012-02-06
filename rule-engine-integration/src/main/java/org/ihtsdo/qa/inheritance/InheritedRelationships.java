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
package org.ihtsdo.qa.inheritance;

import java.util.List;

import org.dwfa.ace.api.I_RelTuple;

/**
 * The Class InheritedRelationships.
 */
public class InheritedRelationships {
	
	/** The role groups. */
	List<I_RelTuple[]> roleGroups;
	
	/** The single roles. */
	List<I_RelTuple> singleRoles;
	
	/**
	 * Instantiates a new inherited relationships.
	 *
	 * @param roleGroups the role groups
	 * @param singleRoles the single roles
	 */
	public InheritedRelationships(List<I_RelTuple[]> roleGroups,
			List<I_RelTuple> singleRoles) {
		super();
		this.roleGroups = roleGroups;
		this.singleRoles = singleRoles;
	}
	
	/**
	 * Gets the role groups.
	 *
	 * @return the role groups
	 */
	public List<I_RelTuple[]> getRoleGroups() {
		return roleGroups;
	}
	
	/**
	 * Gets the single roles.
	 *
	 * @return the single roles
	 */
	public List<I_RelTuple> getSingleRoles() {
		return singleRoles;
	}

}
