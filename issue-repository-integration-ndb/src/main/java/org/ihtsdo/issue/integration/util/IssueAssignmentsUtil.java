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
package org.ihtsdo.issue.integration.util;

import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.queue.SelectAll;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.IssueDAO;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.IssueSearchCriteria;
import org.ihtsdo.issue.integration.workers.IssueRepositoryInboxQueueWorker;
import org.ihtsdo.issue.issuerepository.IssueRepository;

/**
 * The Class IssueAssignmentsUtil.
 */
public class IssueAssignmentsUtil {

	/** The Constant bpAttachmentKey. */
	private final static String bpAttachmentKey = "bpAttachmentKey";
	
	/** The Constant issueAttachmentKey. */
	private final static String issueAttachmentKey = "issueKey";

	/**
	 * Search issues.
	 * 
	 * @param repository the repository
	 * @param criteria the criteria
	 * 
	 * @return the list< issue>
	 */
	public static List<Issue>  searchIssues(IssueRepository repository, IssueSearchCriteria criteria) {
		List<Issue> matchedIssues = new ArrayList<Issue>();
		
		IssueDAO issueDAO = new IssueDAO();

		try {
			matchedIssues = issueDAO.searchIssues(repository, criteria);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return matchedIssues;

	}
	
	/**
	 * Search issues.
	 * 
	 * @param repository the repository
	 * @param issueRepoRegistration the issue repo registration
	 * @param criteria the criteria
	 * 
	 * @return the list< issue>
	 */
	public static List<Issue>  searchIssues(IssueRepository repository, IssueRepoRegistration issueRepoRegistration,
			IssueSearchCriteria criteria) {
		List<Issue> matchedIssues = new ArrayList<Issue>();
		
		IssueDAO issueDAO = new IssueDAO();
		
		try {
			matchedIssues = issueDAO.searchIssues(repository, criteria, issueRepoRegistration);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return matchedIssues;
		
	}
	
	/**
	 * Creates the assignments from issues.
	 * 
	 * @param repository the repository
	 * @param criteria the criteria
	 * @param worker the worker
	 * @param nextStatus the next status
	 */
	public static void createAssignmentsFromIssues(IssueRepository repository, IssueSearchCriteria criteria, 
			I_Work worker, String nextStatus) {
		
		IssueDAO issueDAO = new IssueDAO();
		
		try {
			List<Issue> matchedIssues = issueDAO.searchIssues(repository, criteria);
			for (Issue issue : matchedIssues) {
				BusinessProcess businessProcess = (BusinessProcess) issue.getFieldMap().get(bpAttachmentKey);
				issue.setDownloadStatus(nextStatus);
				issueDAO.updateIssue(repository, issue);
				businessProcess.writeAttachment(issueAttachmentKey, issue);
				deliverAssignment(businessProcess, worker);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Creates the assignments from issues.
	 * 
	 * @param repository the repository
	 * @param criteria the criteria
	 * @param worker the worker
	 * @param nextStatus the next status
	 */
	public static void createAssignmentsFromIssues(IssueRepository repository, IssueSearchCriteria criteria, 
			IssueRepositoryInboxQueueWorker worker, String nextStatus) {

		IssueDAO issueDAO = new IssueDAO();

		try {
			List<Issue> matchedIssues = issueDAO.searchIssues(repository, criteria);
			for (Issue issue : matchedIssues) {
				BusinessProcess businessProcess = (BusinessProcess) issue.getFieldMap().get(bpAttachmentKey);
				issue.setDownloadStatus(nextStatus);
				issueDAO.updateIssue(repository, issue);
				businessProcess.writeAttachment(issueAttachmentKey, issue);
				
				Transaction t = worker.getActiveTransaction();
                worker.getQueue().write(businessProcess, t);
                worker.commitTransactionIfActive();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Initialize issue for assignments.
	 * 
	 * @param repository the repository
	 * @param criteria the criteria
	 * @param businessProcess the business process
	 * @param nextStatus the next status
	 */
	public static void initializeIssueForAssignments(IssueRepository repository, IssueSearchCriteria criteria, 
			BusinessProcess businessProcess, String nextStatus) {
		
		IssueDAO issueDAO = new IssueDAO();

		try {
			List<Issue> matchedIssues = issueDAO.searchIssues(repository, criteria);
			for (Issue issue : matchedIssues) {
				issue.setDownloadStatus(nextStatus);
				issue.getFieldMap().put(bpAttachmentKey, businessProcess);
				issueDAO.updateIssue(repository, issue);
				businessProcess.writeAttachment(issueAttachmentKey, issue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Deliver assignment.
	 * 
	 * @param businessProcess the business process
	 * @param worker the worker
	 * 
	 * @throws InterruptedException the interrupted exception
	 * @throws PrivilegedActionException the privileged action exception
	 * @throws ConfigurationException the configuration exception
	 * @throws LeaseDeniedException the lease denied exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TransactionException the transaction exception
	 */
	public static void deliverAssignment(BusinessProcess businessProcess, I_Work worker) throws InterruptedException, PrivilegedActionException, ConfigurationException, LeaseDeniedException, IOException, TransactionException {
		ServiceID serviceID = null;
		Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
		Entry[] attrSetTemplates = new Entry[] { new ElectronicAddress(businessProcess.getDestination()) };
		ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
		ServiceItemFilter filter = null;
		ServiceItem service = worker.lookup(template, filter);
		I_QueueProcesses q = (I_QueueProcesses) service.service;
		q.write(businessProcess, worker.getActiveTransaction());
	}
	
	/**
	 * Deliver assignment to named queue.
	 * 
	 * @param businessProcess the business process
	 * @param queueName the queue name
	 * @param worker the worker
	 * 
	 * @throws Exception the exception
	 */
	public static void deliverAssignmentToNamedQueue(BusinessProcess businessProcess, String queueName, I_Work worker) throws Exception {
		 try {
	            ServiceID serviceID = null;
	            Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
	            worker.getLogger().info(
	                "Moving process " + businessProcess.getProcessID() + " to Queue named: " + queueName);
	            Entry[] attrSetTemplates = new Entry[] { new Name(queueName) };
	            ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
	            ServiceItemFilter filter = null;
	            ServiceItem service = worker.lookup(template, filter);
	            if (service == null) {
	                throw new TaskFailedException("No queue with the specified name could be found: "
	                    + queueName);
	            }
	            I_QueueProcesses q = (I_QueueProcesses) service.service;
	            q.write(businessProcess, worker.getActiveTransaction());
	            worker.getLogger()
	                .info("Moved process " + businessProcess.getProcessID() + " to queue: " + q.getNodeInboxAddress());
	        } catch (Exception e) {
	            throw new Exception(e);
	        }
	}
	

	/**
	 * Update issue from bp.
	 * 
	 * @param repository the repository
	 * @param businessProcess the business process
	 * @param status the status
	 * 
	 * @throws Exception the exception
	 */
	public static void updateIssueFromBP (IssueRepository repository,BusinessProcess businessProcess,String status) throws Exception {
		IssueDAO issueDAO = new IssueDAO();
		Issue issue=(Issue)businessProcess.readAttachement(issueAttachmentKey);
		HashMap<String,Object> map=issue.getFieldMap();
		issue.setDownloadStatus(status);
		map.put(bpAttachmentKey, businessProcess);
		issueDAO.updateIssue(repository, issue);	
	}
	
	/**
	 * Update issue.
	 * 
	 * @param repository the repository
	 * @param issue the issue
	 * @param repoRegis the repo regis
	 * 
	 * @throws Exception the exception
	 */
	public static void updateIssue (IssueRepository repository,Issue issue, IssueRepoRegistration repoRegis) throws Exception {
		IssueDAO issueDAO = new IssueDAO();
		issueDAO.updateIssue(repository, issue, repoRegis);	
	}
	
	/**
	 * Update issue.
	 * 
	 * @param repository the repository
	 * @param issue the issue
	 * 
	 * @throws Exception the exception
	 */
	public static void updateIssue (IssueRepository repository,Issue issue) throws Exception {
		IssueDAO issueDAO = new IssueDAO();
		issueDAO.updateIssue(repository, issue);	
	}
	
	/**
	 * Creates the issue from bp.
	 * 
	 * @param repository the repository
	 * @param businessProcess the business process
	 * @param status the status
	 * 
	 * @return the issue
	 * 
	 * @throws Exception the exception
	 */
	public static Issue createIssueFromBP(IssueRepository repository,BusinessProcess businessProcess,String status) throws Exception{
		IssueDAO issueDAO = new IssueDAO();
		Issue issue=(Issue)businessProcess.readAttachement(issueAttachmentKey);
		HashMap<String,Object> map=issue.getFieldMap();
		issue.setDownloadStatus(status);
		map.put(bpAttachmentKey, businessProcess);
		issue=issueDAO.createIssue(repository, issue);	
		return issue;
	}

	/**
	 * Read queue and update issues.
	 * 
	 * @param repository the repository
	 * @param queueName the queue name
	 * @param worker the worker
	 * @param status the status
	 * 
	 * @throws Exception the exception
	 */
	public static void readQueueAndUpdateIssues(IssueRepository repository,String queueName,I_Work worker,String status) throws Exception {
		ServiceID serviceID = null;
        Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
        Entry[] attrSetTemplates = new Entry[] { new ElectronicAddress(queueName) };
        ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
        ServiceItemFilter filter = null;
        ServiceItem service = worker.lookup(template, filter);
        I_QueueProcesses q = (I_QueueProcesses) service.service;
        
        Transaction t =worker.getActiveTransaction();
        BusinessProcess process =(BusinessProcess) q.take(new SelectAll(), t);
        
        updateIssueFromBP(repository,process,status);
	}
	
}
