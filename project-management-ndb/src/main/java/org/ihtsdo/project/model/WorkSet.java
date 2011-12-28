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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.project.refset.WorkflowRefset;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;

/**
 * The Class WorkSet.
 */
public class WorkSet extends WorkflowRefset implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The name. */
	private String name;

	/** The id. */
	private int id;

	/** The uids. */
	private List<UUID> uids;

	/** The project id. */
	private UUID projectUUID;

	/**
	 * Instantiates a new work set.
	 * 
	 * @param name the name
	 * @param id the id
	 * @param uids the uids
	 * @param projectUUID the project id
	 */
	public WorkSet(String name, int id, List<UUID> uids,
			UUID projectUUID) {
		super();
		this.name = name;
		this.refsetName = name;
		this.id = id;
		this.refsetId = id;
		this.uids = uids;
		this.projectUUID = projectUUID;
	}

	public WorkSet(String name, UUID projectUUID) {
		super();
		this.name = name;
		this.uids = new ArrayList<UUID>();
		this.projectUUID = projectUUID;
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
		this.refsetName = name;
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
		this.refsetId = id;
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
	 * Gets the project.
	 * 
	 * @param config the config
	 * 
	 * @return the project
	 * @throws Exception 
	 */
	public I_TerminologyProject getProject(I_ConfigAceFrame config) throws Exception {
		I_TerminologyProject project = null;
		I_GetConceptData concept = null;
		try {
			concept = Terms.get().getConcept(projectUUID);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		project = TerminologyProjectDAO.getTranslationProject(concept, config);
		return project;
	}

	/**
	 * Gets the project id.
	 * 
	 * @return the project id
	 */
	public UUID getProjectUUID() {
		return projectUUID;
	}

	/**
	 * Sets the project id.
	 * 
	 * @param projectUUID the new project id
	 */
	public void setProjectUUID(UUID projectUUID) {
		this.projectUUID = projectUUID;
	}


	/**
	 * Gets the work set members.
	 * 
	 * @return the work set members
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	public List<WorkSetMember> getWorkSetMembers() throws TerminologyException, IOException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		return TerminologyProjectDAO.getAllWorkSetMembers(this, config);
	}

	/**
	 * Gets the serial version uid.
	 * 
	 * @return the serial version uid
	 */
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.name;
	}

	public I_GetConceptData getSourceRefset() throws TerminologyException, IOException, Exception {
		return TerminologyProjectDAO.getSourceRefsetForWorkSet(this, Terms.get().getActiveAceFrameConfig());
	}

	public void setSourceRefset(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.setSourceRefset(this, refset, Terms.get().getActiveAceFrameConfig());
	}

	public boolean hasMember(I_GetConceptData concept) throws TerminologyException, IOException {
		boolean hasMember = false;
		Collection<? extends RefexChronicleBI<?>> members = concept.getAnnotations();
		PromotionRefset promRefset = getPromotionRefset(Terms.get().getActiveAceFrameConfig());
		for (RefexChronicleBI<?> promotionMember : members) {
			if (promotionMember.getCollectionNid() == promRefset.getRefsetId()) {
				hasMember = true;
			}
		}

		return hasMember;
	}

	public List<PartitionScheme> getPartitionSchemes(I_ConfigAceFrame config) {
		return TerminologyProjectDAO.getAllPartitionSchemesForRefsetConcept(this.getConcept(), config);
	}

	public void sync(I_ConfigAceFrame config, I_ShowActivity activity) throws Exception {
		TerminologyProjectDAO.syncWorksetWithRefsetSpec(this, config, activity);
	}

	public void addRefsetAsExclusion(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.addRefsetAsExclusion(this, refset, Terms.get().getActiveAceFrameConfig());
	}

	public void removeRefsetAsExclusion(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.removeRefsetAsExclusion(this, refset, Terms.get().getActiveAceFrameConfig());
	}

	public List<I_GetConceptData> getExclusionRefsets() throws TerminologyException, IOException {
		return TerminologyProjectDAO.getExclusionRefsetsForWorkSet(this, Terms.get().getActiveAceFrameConfig());
	}

}
