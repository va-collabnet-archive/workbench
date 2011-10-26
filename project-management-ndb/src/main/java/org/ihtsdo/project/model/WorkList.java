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
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.refset.WorkflowRefset;

/**
 * The Class WorkList.
 */
public class WorkList extends WorkflowRefset implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The name. */
	private String name;
	
	/** The id. */
	private int id;
	
	/** The uids. */
	private List<UUID> uids;
	
	/** The partition id. */
	private UUID partitionUUID;
	
	/** The destination. */
	private String destination;
	
	/** The business process. */
	private BusinessProcess businessProcess;
	
	/**
	 * Instantiates a new work list.
	 * 
	 * @param name the name
	 * @param id the id
	 * @param uids the uids
	 * @param workSetId the work set id
	 * @param batch the batch
	 * @param destination the destination
	 * @param businessProcess the business process
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	public WorkList(String name, int id, List<UUID> uids,UUID workSetUUID,
			String destination,
			BusinessProcess businessProcess) throws TerminologyException, IOException {
		super();
		this.name = name;
		if (uids!=null)
			this.refsetConcept=Terms.get().getConcept(uids);
		this.id = id;
		this.uids = uids;
		this.partitionUUID = workSetUUID;
		this.destination = destination;
		this.businessProcess = businessProcess;
		
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
	 * Gets the destination.
	 * 
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}
	
	/**
	 * Sets the destination.
	 * 
	 * @param destination the new destination
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	/**
	 * Gets the business process.
	 * 
	 * @return the business process
	 */
	public BusinessProcess getBusinessProcess() {
		return businessProcess;
	}
	
	/**
	 * Sets the business process.
	 * 
	 * @param businessProcess the new business process
	 */
	public void setBusinessProcess(BusinessProcess businessProcess) {
		this.businessProcess = businessProcess;
	}
	
	/**
	 * Gets the work list members.
	 * 
	 * @return the work list members
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	public List<WorkListMember> getWorkListMembers() throws TerminologyException, IOException {
		return TerminologyProjectDAO.getAllWorkListMembers(this, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Gets the work set id.
	 * 
	 * @return the work set id
	 */
	public UUID getPartitionUUID() {
		return partitionUUID;
	}
	
	/**
	 * Sets the work set id.
	 * 
	 * @param workSetId the new work set id
	 */
	public void setPartitionUUID(UUID partitionUUID) {
		this.partitionUUID = partitionUUID;
	}
	
	public String toString() {
		return this.name;
	}
	
	public Partition getPartition() {
		Partition partition = null;
		I_GetConceptData concept = null;
		I_ConfigAceFrame config = null;
		try {
			concept = Terms.get().getConcept(partitionUUID);
			config = Terms.get().getActiveAceFrameConfig();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		partition = TerminologyProjectDAO.getPartition(concept, config);
		return partition;
	}
	
	
}
