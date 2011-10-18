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
package org.ihtsdo.issue.manager.implementation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.IssueAttachmentRef;
import org.ihtsdo.issue.IssueComment;
import org.ihtsdo.issue.IssueDependency;
import org.ihtsdo.issue.issuerepository.IssueRepository;


/**
 * The Interface I_IssueManager.
 */
public interface I_IssueManager {

	/**
	 * Open repository.
	 * 
	 * @param issueRepository the issue repository
	 * @param user the user
	 * @param password the password
	 * 
	 * @return the int
	 * 
	 * @throws Exception the exception
	 */
	public int openRepository(IssueRepository issueRepository ,String user, String password) throws Exception ;
	
	/**
	 * Gets the all issues.
	 * 
	 * @return the all issues
	 * 
	 * @throws Exception the exception
	 */
	public List<Issue> getAllIssues() throws Exception ;
	
	/**
	 * Gets the issues for component id.
	 * 
	 * @param componentId the component id
	 * 
	 * @return the issues for component id
	 * 
	 * @throws Exception the exception
	 */
	public List<Issue> getIssuesForComponentId(String componentId)throws Exception ;

	/**
	 * Gets the issue data.
	 * 
	 * @param issueExternalId the issue external id
	 * 
	 * @return the issue data
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public Issue getIssueData(String issueExternalId) throws IOException, ClassNotFoundException;
	
	/**
	 * Gets the issues for criteria.
	 * 
	 * @param userId the user id
	 * @param downloadStatus the download status
	 * @param priority the priority
	 * @param role the role
	 * @param queueName the queue name
	 * @param workflowStatus the workflow status
	 * @param map the map
	 * 
	 * @return the issues for criteria
	 * 
	 * @throws Exception the exception
	 */
	public List<Issue> getIssuesForCriteria(String userId,String downloadStatus,String priority,String role,String queueName,String workflowStatus,HashMap<String,Object> map) throws Exception ;
	
	/**
	 * Post new issue.
	 * 
	 * @param issue the issue
	 * 
	 * @return the string
	 * 
	 * @throws Exception the exception
	 */
	public String postNewIssue(Issue issue)throws Exception ;
	
	/**
	 * Sets the issue data.
	 * 
	 * @param issue the new issue data
	 * 
	 * @throws Exception the exception
	 */
	public void setIssueData(Issue issue)throws Exception ;
	
	/**
	 * Sets the issue field map.
	 * 
	 * @param issue the new issue field map
	 * 
	 * @throws Exception the exception
	 */
	public void setIssueFieldMap(Issue issue)throws Exception ;
	
	/**
	 * Delete issue.
	 * 
	 * @param issueExternalId the issue external id
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public void deleteIssue(String issueExternalId) throws IOException, ClassNotFoundException;
	
	/**
	 * Gets the comments list.
	 * 
	 * @param issue the issue
	 * 
	 * @return the comments list
	 * 
	 * @throws Exception the exception
	 */
	public List<IssueComment> getCommentsList (Issue issue) throws Exception;
	
	
	public List<IssueAttachmentRef> getAttachmentList (Issue issue) throws Exception;
	
	
	public void addAttachment(Issue issue, File file, String mimeType) throws Exception;
	
	
	public void delAttachment(IssueAttachmentRef issueAttachmentRef) throws Exception;
	
	public void delDependency(Issue originIssue, Issue targetIssue) throws Exception;

	public File getAttachmentFile(IssueAttachmentRef issueAttachmentRef, File toFolder)
			throws Exception;

	public List<IssueDependency> getParentDependencyList(Issue issue) throws Exception;
	

	public List<IssueDependency> getChildDependencyList(Issue issue) throws Exception;

	void delDependency(Issue originIssue, IssueDependency issueDependency)
			throws Exception;

	void addDependency(Issue originIssue, IssueDependency issueDependency)
			throws Exception;
	
	
}
