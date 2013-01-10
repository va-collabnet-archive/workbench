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
package org.ihtsdo.project.view.dnd;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class GetAndSetIssueRepo.
 */
public class GetAndSetIssueRepo implements I_GetItemForModel {

	/** The issue repo. */
	private IssueRepository issueRepo;
	
	/** The update repo function. */
	private I_UpdateRepository updateRepoFunction;
	
	/**
	 * Instantiates a new gets the and set issue repo.
	 *
	 * @param updateRepoFunction the update repo function
	 */
	public GetAndSetIssueRepo(I_UpdateRepository updateRepoFunction){
		this.updateRepoFunction=updateRepoFunction;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.panel.dnd.I_GetItemForModel#getItemFromConcept(org.dwfa.ace.api.I_GetConceptData)
	 */
	@Override
	public Object getItemFromConcept(I_GetConceptData concept) throws Exception {
		try{
		this.issueRepo=  IssueRepositoryDAO.getIssueRepository(concept);
		updateRepoFunction.update(issueRepo);
		}catch (Exception e){
			throw new Exception("Cannot find issue repository for concept " + concept.toString());
		}
		return null;
	}

}
