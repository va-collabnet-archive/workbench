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
package org.ihtsdo.project.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;

/**
 * The Class WorkSet.
 */
public class PartitionScheme implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The name. */
	private String name;

	/** The id. */
	private int id;

	/** The uids. */
	private List<UUID> uids;

	/** The WorkSet id. */
	private UUID sourceRefsetUUID;

	/**
	 * Instantiates a new work set.
	 * 
	 * @param name the name
	 * @param id the id
	 * @param uids the uids
	 * @param sourceRefsetUUID the project id
	 * @param creationDate the creation date
	 * @param lastChangeDate the last change date
	 * @param description the description
	 */
	public PartitionScheme(String name, int id, List<UUID> uids,
			UUID workSetUUID) {
		super();
		this.name = name;
		this.id = id;
		this.uids = uids;
		this.sourceRefsetUUID = workSetUUID;
	}
	
	/**
	 * Gets the concept.
	 * 
	 * @return the concept
	 */
	public I_GetConceptData getConcept() {
		I_GetConceptData concept = null;
		try {
			concept = Terms.get().getConcept(uids);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return concept;
	}

	/**
	 * Gets the project.
	 * 
	 * @param config the config
	 * 
	 * @return the project
	 */
	public I_GetConceptData getSourceRefset(I_ConfigAceFrame config) {
		I_GetConceptData concept = null;
		try {
			concept = Terms.get().getConcept(sourceRefsetUUID);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return concept;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<UUID> getUids() {
		return uids;
	}

	public void setUids(List<UUID> uids) {
		this.uids = uids;
	}

	public UUID getSourceRefsetUUID() {
		return sourceRefsetUUID;
	}

	public void setSourceRefsetUUID(UUID sourceRefsetUUID) {
		this.sourceRefsetUUID = sourceRefsetUUID;
	}
	
	public List<Partition> getPartitions() {
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return TerminologyProjectDAO.getAllPartitionsForScheme(this, config);
	}
	
	public String toString() {
		return name;
	}

}
