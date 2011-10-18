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
 * The Class TranslationProject.
 * 
 * TODO:
 * Project properties:
 * Coleccion de business process
 * Issue management
 */
public class TranslationProject implements I_TerminologyProject, Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The name. */
	private String name;
	
	/** The id. */
	private int id;
	
	/** The uids. */
	private List<UUID> uids;
	
	/**
	 * Instantiates a new terminology project.
	 * 
	 * @param name the name
	 * @param id the id
	 * @param uids the uids
	 * @param addresses the addresses
	 * @param exlusionRefsets the exlusion refsets
	 * @param inclusionRefsets the inclusion refsets
	 * @param description the description
	 */
	public TranslationProject(String name, int id, List<UUID> uids) {
		super();
		this.name = name;
		this.id = id;
		this.uids = uids;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.model.I_TerminologyProject#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.model.I_TerminologyProject#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.model.I_TerminologyProject#getId()
	 */
	public int getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.model.I_TerminologyProject#setId(int)
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.model.I_TerminologyProject#getUids()
	 */
	public List<UUID> getUids() {
		return uids;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.model.I_TerminologyProject#setUids(java.util.List)
	 */
	public void setUids(List<UUID> uids) {
		this.uids = uids;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.model.I_TerminologyProject#getConcept()
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
	
	public String toString() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.model.I_TerminologyProject#getWorkSets(org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	public List<WorkSet> getWorkSets(I_ConfigAceFrame config) {
		return TerminologyProjectDAO.getAllWorkSetsForProject(this, config);
	}
	
	public List<I_GetConceptData> getExclusionRefsets() throws TerminologyException, IOException {
		return TerminologyProjectDAO.getExclusionRefsetsForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	public List<I_GetConceptData> getCommonRefsets() throws TerminologyException, IOException {
		return TerminologyProjectDAO.getCommonRefsetsForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	public List<I_GetConceptData> getSourceLanguageRefsets() throws TerminologyException, IOException {
		return TerminologyProjectDAO.getSourceLanguageRefsetsForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	public void addRefsetAsExclusion(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.addRefsetAsExclusion(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	public void addRefsetAsCommon(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.addRefsetAsCommon(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	public void addRefsetAsSourceLanguage(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.addRefsetAsSourceLanguage(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	public void removeRefsetAsExclusion(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.removeRefsetAsExclusion(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	public void removeRefsetAsCommon(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.removeRefsetAsCommon(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	public void removeRefsetAsSourceLanguage(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.removeRefsetAsSourceLanguage(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	public I_GetConceptData getTargetLanguageRefset() throws TerminologyException, IOException, Exception {
		return TerminologyProjectDAO.getTargetLanguageRefsetForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	public I_GetConceptData getSourceIssueRepo() throws TerminologyException, IOException, Exception {
		return TerminologyProjectDAO.getSourceIssueRepoForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	public I_GetConceptData getProjectIssueRepo() throws TerminologyException, IOException, Exception {
		return TerminologyProjectDAO.getProjectIssueRepoForProject(this, Terms.get().getActiveAceFrameConfig());
	}

	public void setTargetLanguageRefset(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.setLanguageTargetRefset(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	public void setSourceIssueRepo(I_GetConceptData repo) throws TerminologyException, IOException {
		TerminologyProjectDAO.setSourceIssueRepo(this, repo, Terms.get().getActiveAceFrameConfig());
	}
	public void setProjectIssueRepo(I_GetConceptData repo) throws TerminologyException, IOException {
		TerminologyProjectDAO.setProjectIssueRepo(this, repo, Terms.get().getActiveAceFrameConfig());
	}
}
