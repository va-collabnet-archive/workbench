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
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;

/**
 * The Interface I_TerminologyProject.
 */
public interface I_TerminologyProject extends Comparable<I_TerminologyProject> {

	public enum Type {
		TERMINOLOGY, TRANSLATION, MAPPING
	}

	public Type getProjectType();

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name);

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public int getId();

	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the new id
	 */
	public void setId(int id);

	/**
	 * Gets the uids.
	 * 
	 * @return the uids
	 */
	public List<UUID> getUids();

	/**
	 * Sets the uids.
	 * 
	 * @param uids
	 *            the new uids
	 */
	public void setUids(List<UUID> uids);

	/**
	 * Gets the concept.
	 * 
	 * @return the concept
	 */
	public I_GetConceptData getConcept();

	/**
	 * Gets the work sets.
	 * 
	 * @param config
	 *            the config
	 * @return the work sets
	 */
	public List<WorkSet> getWorkSets(I_ConfigAceFrame config);

	/**
	 * To string.
	 * 
	 * @return the string
	 */
	public String toString();

	public List<I_GetConceptData> getExclusionRefsets() throws TerminologyException, IOException;

	public List<I_GetConceptData> getCommonRefsets() throws TerminologyException, IOException;

	public String getNamespaceRefset() throws TerminologyException, IOException, Exception;

	public I_GetConceptData getReleasePath() throws TerminologyException, IOException, Exception;

	public I_GetConceptData getModuleIdRefset() throws TerminologyException, IOException, Exception;

	public I_GetConceptData getSourceIssueRepo() throws TerminologyException, IOException, Exception;

	public I_GetConceptData getProjectIssueRepo() throws TerminologyException, IOException, Exception;

	public void setSourceIssueRepo(I_GetConceptData concept) throws TerminologyException, IOException;

	public void setProjectIssueRepo(I_GetConceptData concept) throws TerminologyException, IOException;

	public void setModuleIdRefset(I_GetConceptData i_GetConceptData) throws TerminologyException, IOException;

	public void setReleasePathRefset(I_GetConceptData i_GetConceptData) throws TerminologyException, IOException;

	public void setNamespaceRefset(String namespaceText) throws TerminologyException, IOException;

}