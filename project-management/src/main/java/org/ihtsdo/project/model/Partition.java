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
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;

/**
 * The Class Partition.
 */
public class Partition implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The name. */
	private String name;

	/** The id. */
	private int id;

	/** The uids. */
	private List<UUID> uids;

	/** The WorkSet id. */
	private UUID partitionSchemeUUID;
	
	/** The concept. */
	private I_GetConceptData concept;

	/**
	 * Instantiates a new work set.
	 *
	 * @param name the name
	 * @param id the id
	 * @param uids the uids
	 * @param partitionSchemeUUID the project id
	 */
	public Partition(String name, int id, List<UUID> uids,
			UUID partitionSchemeUUID) {
		super();
		this.name = name;
		this.id = id;
		this.uids = uids;
		this.partitionSchemeUUID = partitionSchemeUUID;
	}

	/**
	 * Gets the concept.
	 * 
	 * @return the concept
	 */
	public I_GetConceptData getConcept() {
		if (concept != null) {
			return concept;
		}
		try {
			concept = Terms.get().getConcept(uids);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
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
	public PartitionScheme getPartitionScheme(I_ConfigAceFrame config) {
		I_GetConceptData concept = null;
		try {
			concept = Terms.get().getConcept(partitionSchemeUUID);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return TerminologyProjectDAO.getPartitionScheme(concept, config);
	}

	/**
	 * Gets the work lists.
	 *
	 * @return the work lists
	 */
	public List<WorkList> getWorkLists() {
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return TerminologyProjectDAO.getAllWorkListsForRefset(this.getConcept(), config);
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
	 * Gets the uids.
	 *
	 * @return the uids
	 */
	public List<UUID> getUids() {
		return uids;
	}

	/**
	 * Sets the uids.
	 *
	 * @param uids the new uids
	 */
	public void setUids(List<UUID> uids) {
		this.uids = uids;
	}

	/**
	 * Gets the partition scheme uuid.
	 *
	 * @return the partition scheme uuid
	 */
	public UUID getPartitionSchemeUUID() {
		return partitionSchemeUUID;
	}

	/**
	 * Sets the partition scheme uuid.
	 *
	 * @param partitionSchemeUUID the new partition scheme uuid
	 */
	public void setPartitionSchemeUUID(UUID partitionSchemeUUID) {
		this.partitionSchemeUUID = partitionSchemeUUID;
	}
	
	/**
	 * Gets the partition members.
	 *
	 * @return the partition members
	 */
	public List<PartitionMember> getPartitionMembers() {
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return TerminologyProjectDAO.getAllPartitionMembers(this, config);
	}
	
	/**
	 * Gets the sub partition schemes.
	 *
	 * @param config the config
	 * @return the sub partition schemes
	 */
	public List<PartitionScheme> getSubPartitionSchemes(I_ConfigAceFrame config) {
		return TerminologyProjectDAO.getAllPartitionSchemesForRefsetConcept(this.getConcept(), config);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}
	
	/**
	 * Gets the project.
	 *
	 * @return the project
	 */
	public I_TerminologyProject getProject() {
		//TODO: implement
		return null;
	}

	/**
	 * Sets the concept.
	 *
	 * @param concept the new concept
	 */
	public void setConcept(I_GetConceptData concept) {
		this.concept = concept;
	}

}
