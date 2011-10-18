package org.ihtsdo.project.panel.dnd;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

public class GetAndSetIssueRepo implements I_GetItemForModel {

	private IssueRepository issueRepo;
	private I_UpdateRepository updateRepoFunction;
	public GetAndSetIssueRepo(I_UpdateRepository updateRepoFunction){
		this.updateRepoFunction=updateRepoFunction;
	}
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
