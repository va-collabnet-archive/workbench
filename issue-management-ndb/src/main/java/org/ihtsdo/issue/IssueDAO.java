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
package org.ihtsdo.issue;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.issue.manager.implementation.CollabnetIssueManager;
import org.ihtsdo.issue.manager.implementation.I_IssueManager;

/**
 * The Class IssueDAO.
 */
public class IssueDAO {

	/**
	 * Creates the issue.
	 * 
	 * @param repository
	 *            the repository
	 * @param issue
	 *            the issue
	 * 
	 * @return the issue
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Issue createIssue(IssueRepository repository, Issue issue)
			throws Exception {

		I_IssueManager cim = getIssueManager(repository);
		String id = cim.postNewIssue(issue);
		issue.setLastModifiedDate(new Date().getTime());
		issue.setExternalId(id);
		issue.setRepositoryUUId(repository.getUuid());
		return issue;
	}

	/**
	 * Create the issue
	 * 
	 * @param repository
	 * 				the repository
	 * @param issue
	 * 				the issue
	 * @param repoRegis
	 * 				the report register
	 * @return the issue
	 * 
	 * @throws Exception
	 */
	public Issue createIssue(IssueRepository repository, Issue issue,
			IssueRepoRegistration repoRegis) throws Exception {

		I_IssueManager cim = getIssueManager(repository, repoRegis);
		String id = cim.postNewIssue(issue);
		issue.setLastModifiedDate(new Date().getTime());
		issue.setExternalId(id);
		issue.setRepositoryUUId(repository.getUuid());
		return issue;
	}

	/**
	 * Update issue.
	 * 
	 * @param repository
	 *            the repository
	 * @param issue
	 *            the issue
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void updateIssue(IssueRepository repository, Issue issue)
			throws Exception {

		I_IssueManager cim = getIssueManager(repository);
		cim.setIssueFieldMap(issue);
		cim.setIssueData(issue);
	}

	/**
	 * Update issue.
	 * 
	 * @param repository
	 *            the repository
	 * @param issue
	 *            the issue
	 * @param repoRegis
	 *            the repo regis
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void updateIssue(IssueRepository repository, Issue issue,
			IssueRepoRegistration repoRegis) throws Exception {

		I_IssueManager cim = getIssueManager(repository, repoRegis);
		cim.setIssueFieldMap(issue);
		cim.setIssueData(issue);
	}

	/**
	 * Gets the issue.
	 * 
	 * @param repository
	 *            the repository
	 * @param id
	 *            the id
	 * 
	 * @return the issue
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public Issue getIssue(IssueRepository repository, String id)
			throws Exception {

		I_IssueManager cim = getIssueManager(repository);
		Issue issue = cim.getIssueData(id);
		return issue;
	}

	/**
	 * Search issues.
	 * 
	 * @param repository
	 *            the repository
	 * @param criteria
	 *            the criteria
	 * 
	 * @return the list< issue>
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public List<Issue> searchIssues(IssueRepository repository,
			IssueSearchCriteria criteria) throws Exception {

		I_IssueManager cim = getIssueManager(repository);
		List<Issue> issueMatches = cim.getIssuesForCriteria(criteria
				.getUserId(), criteria.getDownloadStatus(), criteria
				.getPriority(), criteria.getRole(), criteria.getQueueName(),
				criteria.getWorkflowStatus(), criteria.getMap());
		return issueMatches;
	}

	/**
	 * Search issues.
	 * 
	 * @param repository
	 *            the repository
	 * @param criteria
	 *            the criteria
	 * @param repoRegis
	 *            the repo regis
	 * 
	 * @return the list< issue>
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public List<Issue> searchIssues(IssueRepository repository,
			IssueSearchCriteria criteria, IssueRepoRegistration repoRegis)
			throws Exception {

		I_IssueManager cim = getIssueManager(repository, repoRegis);
		List<Issue> issueMatches = cim.getIssuesForCriteria(criteria
				.getUserId(), criteria.getDownloadStatus(), criteria
				.getPriority(), criteria.getRole(), criteria.getQueueName(),
				criteria.getWorkflowStatus(), criteria.getMap());
		return issueMatches;
	}

	/**
	 * Delete issue.
	 * 
	 * @param repository
	 *            the repository
	 * @param id
	 *            the id
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void deleteIssue(IssueRepository repository, String id)
			throws Exception {

		I_IssueManager cim = getIssueManager(repository);
		cim.deleteIssue(id);
		return;
	}

	/**
	 * Gets the issue manager.
	 * 
	 * @param repository
	 *            the repository
	 * 
	 * @return the issue manager
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private I_IssueManager getIssueManager(IssueRepository repository)
			throws Exception {

		I_ConfigAceFrame config = (I_ConfigAceFrame) LocalVersionedTerminology
				.get().getActiveAceFrameConfig();
		IssueRepoRegistration repoRegis;
		repoRegis = IssueRepositoryDAO.getRepositoryRegistration(repository
				.getUuid(), config);
		I_IssueManager cim = new CollabnetIssueManager();
		cim.openRepository(repository, repoRegis.getUserId(), repoRegis
				.getPassword());
		return cim;
	}

	/**
	 * Gets the issue manager.
	 * 
	 * @param repository
	 *            the repository
	 * @param repoRegis
	 *            the repo regis
	 * 
	 * @return the issue manager
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private I_IssueManager getIssueManager(IssueRepository repository,
			IssueRepoRegistration repoRegis) throws Exception {

		I_IssueManager cim = new CollabnetIssueManager();
		cim.openRepository(repository, repoRegis.getUserId(), repoRegis
				.getPassword());
		return cim;
	}
	

	public List<IssueAttachmentRef> getAttachmentList (IssueRepository repository,Issue issue) throws Exception{
		I_IssueManager cim = getIssueManager(repository);
		List<IssueAttachmentRef> issueAttRef = cim.getAttachmentList(issue);
		return issueAttRef;
	}
	
	
	public void addAttachment(IssueRepository repository,Issue issue, File file, String mimeType) throws Exception{
		I_IssueManager cim = getIssueManager(repository);
		cim.addAttachment(issue, file, mimeType);
		return;
	}
	
	public void delAttachment(IssueRepository repository,IssueAttachmentRef issueAttachmentRef) throws Exception{
		I_IssueManager cim = getIssueManager(repository);
		cim.delAttachment(issueAttachmentRef);
		return;
	}
	
	public void delDependency(IssueRepository repository,Issue originIssue, Issue targetIssue) throws Exception{
		I_IssueManager cim = getIssueManager(repository);
		cim.delDependency(originIssue, targetIssue);
		return;
	}

	public File getAttachmentFile(IssueRepository repository,IssueAttachmentRef issueAttachmentRef, File toFolder)
			throws Exception{
		I_IssueManager cim = getIssueManager(repository);
		File file = cim.getAttachmentFile(issueAttachmentRef, toFolder);
		return file;
	}

	public List<IssueDependency> getParentDependencyList(IssueRepository repository,Issue issue) throws Exception{
		I_IssueManager cim = getIssueManager(repository);
		List<IssueDependency> issueDepList = cim.getParentDependencyList(issue);
		return issueDepList;
		
	}
	

	public List<IssueDependency> getChildDependencyList(IssueRepository repository,Issue issue) throws Exception{
		I_IssueManager cim = getIssueManager(repository);
		List<IssueDependency> issueDepList = cim.getChildDependencyList(issue);
		return issueDepList;
		
	}

	public void delDependency(IssueRepository repository,Issue originIssue, IssueDependency issueDependency)
			throws Exception{
		I_IssueManager cim = getIssueManager(repository);
		cim.delDependency(originIssue, issueDependency);
		return;
	}

	public void addDependency(IssueRepository repository,Issue originIssue, IssueDependency issueDependency)
			throws Exception{
		I_IssueManager cim = getIssueManager(repository);
		cim.addDependency(originIssue, issueDependency);
		return;
	}
}
