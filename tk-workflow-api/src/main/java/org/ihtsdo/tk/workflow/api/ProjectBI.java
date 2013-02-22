/*
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
package org.ihtsdo.tk.workflow.api;

import java.util.Collection;
import java.util.UUID;

/**
 * A representation of a Terminology Authoring Project. A set of 
 * rules and metadata are associated to each project. Users play
 * different roles in different projects.
 * 
 * @author alo
 */
public interface ProjectBI {

	/**
	 * The type of project, different projects will have different 
	 * metadata and UI requirements
	 */
	public enum ProjectType {

		/** A generic terminology project. */
		TERMINOLOGY, 
		/** A translation project. */
		TRANSLATION, 
		/** A mapping project. */
		MAPPING
	}

	/**
	 * Gets the name of the project.
	 *
	 * @return the {@link String}
	 */
	String getName();

	/**
	 * Gets the UUID of the project.
	 *
	 * @return the {@link UUID}
	 */
	UUID getUuid();

	/**
	 * Gets all work lists in the project.
	 *
	 * @return the {@link Collection}
	 * @throws Exception the exception
	 */
	Collection<WorkListBI> getWorkLists() throws Exception;


	/**
	 * Gets permissions for the user (in this project).
	 *
	 * @param user the user
	 * @return the {@link Collection}
	 * @throws Exception the exception
	 */
	Collection<WfPermissionBI> getPermissions(WfUserBI user) throws Exception;

	/**
	 * Sets a new permission.
	 *
	 * @param user the user
	 * @param role the role
	 * @param hierarchyUuid the hierarchy uuid
	 * @throws Exception the exception
	 */
	void setPermission(WfUserBI user, WfRoleBI role, UUID hierarchyUuid) throws Exception;

	/**
	 * Gets permissions in this project.
	 *
	 * @return the {@link Collection}
	 * @throws Exception the exception
	 */
	Collection<WfPermissionBI> getPermissions() throws Exception;

	/**
	 * Gets a description of the project, goals, instructions on how to edit, etc.
	 *
	 * @return the {@link String}
	 */
	String getDescription();

	/**
	 * Creates a work list.
	 *
	 * @param definition the definition
	 * @param name the name
	 * @param permissions the permissions
	 * @throws Exception the exception
	 */
	void createWorkList(WfProcessDefinitionBI definition, String name, Collection<WfPermissionBI> permissions) throws Exception;

}
