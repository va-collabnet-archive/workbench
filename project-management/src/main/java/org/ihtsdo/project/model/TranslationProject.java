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
 * The Class TranslationProject.
 * 
 * TODO:
 * Project properties:
 * Coleccion de business process
 * Issue management
 */
public class TranslationProject implements I_TerminologyProject, Serializable, Comparable<I_TerminologyProject>{
	
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
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return concept;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.model.I_TerminologyProject#getWorkSets(org.dwfa.ace.api.I_ConfigAceFrame)
	 */
	public List<WorkSet> getWorkSets(I_ConfigAceFrame config) {
		return TerminologyProjectDAO.getAllWorkSetsForProject(this, config);
	}
	
	/**
	 * Gets the exclusion refsets.
	 *
	 * @return the exclusion refsets
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<I_GetConceptData> getExclusionRefsets() throws TerminologyException, IOException {
		return TerminologyProjectDAO.getExclusionRefsetsForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Gets the common refsets.
	 *
	 * @return the common refsets
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<I_GetConceptData> getCommonRefsets() throws TerminologyException, IOException {
		return TerminologyProjectDAO.getCommonRefsetsForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Gets the source language refsets.
	 *
	 * @return the source language refsets
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public List<I_GetConceptData> getSourceLanguageRefsets() throws TerminologyException, IOException {
		return TerminologyProjectDAO.getSourceLanguageRefsetsForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Adds the refset as exclusion.
	 *
	 * @param refset the refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void addRefsetAsExclusion(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.addRefsetAsExclusion(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Adds the refset as common.
	 *
	 * @param refset the refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void addRefsetAsCommon(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.addRefsetAsCommon(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Adds the refset as source language.
	 *
	 * @param refset the refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void addRefsetAsSourceLanguage(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.addRefsetAsSourceLanguage(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Removes the refset as exclusion.
	 *
	 * @param refset the refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void removeRefsetAsExclusion(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.removeRefsetAsExclusion(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Removes the refset as common.
	 *
	 * @param refset the refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void removeRefsetAsCommon(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.removeRefsetAsCommon(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Removes the refset as source language.
	 *
	 * @param refset the refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void removeRefsetAsSourceLanguage(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.removeRefsetAsSourceLanguage(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Gets the target language refset.
	 *
	 * @return the target language refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public I_GetConceptData getTargetLanguageRefset() throws TerminologyException, IOException, Exception {
		return TerminologyProjectDAO.getTargetLanguageRefsetForProject(this, Terms.get().getActiveAceFrameConfig());
	}

	/**
	 * Gets the release path refset.
	 *
	 * @return the release path refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public I_GetConceptData getReleasePath() throws TerminologyException, IOException, Exception {
		return TerminologyProjectDAO.getReleasePathForProject(this, Terms.get().getActiveAceFrameConfig());
	}

	/**
	 * Gets the module id refset.
	 *
	 * @return the module id refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public I_GetConceptData getModuleIdRefset() throws TerminologyException, IOException, Exception {
		return TerminologyProjectDAO.getModuleIdRefsetForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Gets the namespace refset.
	 *
	 * @return the namespace refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public String getNamespaceRefset() throws TerminologyException, IOException, Exception {
		return TerminologyProjectDAO.getNamespaceRefsetForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Gets the source issue repo.
	 *
	 * @return the source issue repo
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public I_GetConceptData getSourceIssueRepo() throws TerminologyException, IOException, Exception {
		return TerminologyProjectDAO.getSourceIssueRepoForProject(this, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Gets the project issue repo.
	 *
	 * @return the project issue repo
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public I_GetConceptData getProjectIssueRepo() throws TerminologyException, IOException, Exception {
		return TerminologyProjectDAO.getProjectIssueRepoForProject(this, Terms.get().getActiveAceFrameConfig());
	}

	/**
	 * Sets the target language refset.
	 *
	 * @param refset the new target language refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setTargetLanguageRefset(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.setLanguageTargetRefset(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Sets the release path refset.
	 *
	 * @param refset the new release path refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setReleasePathRefset(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.setReleasePathRefset(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Sets the module id refset.
	 *
	 * @param refset the new module id refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setModuleIdRefset(I_GetConceptData refset) throws TerminologyException, IOException {
		TerminologyProjectDAO.setModuleIdRefset(this, refset, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Sets the namespace refset.
	 *
	 * @param namespaceText the new namespace refset
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setNamespaceRefset(String namespaceText) throws TerminologyException, IOException {
		TerminologyProjectDAO.setNamespaceRefset(this, namespaceText, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Sets the source issue repo.
	 *
	 * @param repo the new source issue repo
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setSourceIssueRepo(I_GetConceptData repo) throws TerminologyException, IOException {
		TerminologyProjectDAO.setSourceIssueRepo(this, repo, Terms.get().getActiveAceFrameConfig());
	}
	
	/**
	 * Sets the project issue repo.
	 *
	 * @param repo the new project issue repo
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setProjectIssueRepo(I_GetConceptData repo) throws TerminologyException, IOException {
		TerminologyProjectDAO.setProjectIssueRepo(this, repo, Terms.get().getActiveAceFrameConfig());
	}

	@Override
	public int compareTo(I_TerminologyProject o) {
		return this.name.compareTo(o.getName());
	}

	@Override
	public Type getProjectType() {
		return Type.TRANSLATION;
	}
}
