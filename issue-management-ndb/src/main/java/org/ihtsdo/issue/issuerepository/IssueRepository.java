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
package org.ihtsdo.issue.issuerepository;

import java.io.Serializable;
import java.util.UUID;

/**
 * The Class IssueRepository.
 */
public class IssueRepository implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The id. */
	private int id;
	
	/** The uuid. */
	private UUID uuid;
	
	/** The repository id. */
	private String repositoryId;
	
	/** The url. */
	private String url;
	
	/** The name. */
	private String name;
	
	/** The type. */
	private int type;
	
	/** The concept id. */
	private int conceptId;
	
	/** The project id. */
	private int projectId;
	
	/** The external project id. */
	private String externalProjectId;
	
	/**
	 * The Enum REPOSITORY_TYPE.
	 */
	public enum REPOSITORY_TYPE{
		
		/** The WE b_ site. */
		WEB_SITE, 
 /** The CONCEP t_ extension. */
 CONCEPT_EXTENSION
	}

	
	/**
	 * Instantiates a new issue repository.
	 */
	public IssueRepository() {}
	
	/**
	 * Instantiates a new issue repository.
	 * 
	 * @param repositoryId the repository id
	 * @param url the url
	 * @param name the name
	 * @param type the type
	 */
	public IssueRepository(String repositoryId, String url, String name, int type) {
		super();
		this.repositoryId = repositoryId;
		this.url = url;
		this.name = name;
		this.type = type;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the url.
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 * 
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
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
	 * Gets the concept id.
	 * 
	 * @return the concept id
	 */
	public int getConceptId() {
		return conceptId;
	}

	/**
	 * Sets the concept id.
	 * 
	 * @param conceptId the new concept id
	 */
	public void setConceptId(int conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * Gets the project id.
	 * 
	 * @return the project id
	 */
	public int getProjectId() {
		return projectId;
	}

	/**
	 * Sets the project id.
	 * 
	 * @param projectId the new project id
	 */
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	/**
	 * Gets the repository id.
	 * 
	 * @return the repository id
	 */
	public String getRepositoryId() {
		return repositoryId;
	}

	/**
	 * Sets the repository id.
	 * 
	 * @param repositoryId the new repository id
	 */
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	/**
	 * Gets the uuid.
	 * 
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Sets the uuid.
	 * 
	 * @param uuid the new uuid
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * Gets the external project id.
	 * 
	 * @return the external project id
	 */
	public String getExternalProjectId() {
		return externalProjectId;
	}

	/**
	 * Sets the external project id.
	 * 
	 * @param externalProjectId the new external project id
	 */
	public void setExternalProjectId(String externalProjectId) {
		this.externalProjectId = externalProjectId;
	}
	
	public String toString() {
		return this.name;
	}
	
}
