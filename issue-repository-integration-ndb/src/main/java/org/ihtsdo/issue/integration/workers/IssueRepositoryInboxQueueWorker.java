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
package org.ihtsdo.issue.integration.workers;

	import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoSuchWorkspaceException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.WorkspaceActiveException;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.bpa.worker.task.I_GetWorkFromQueue;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.issue.IssueSearchCriteria;
import org.ihtsdo.issue.integration.util.IssueAssignmentsUtil;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

	/**
	 * The Class IssueRepositoryInboxQueueWorker.
	 */
	public class IssueRepositoryInboxQueueWorker extends Worker implements I_GetWorkFromQueue, Runnable {
	    
    	/** The queue. */
    	private I_QueueProcesses queue;

	    /** The worker thread. */
    	private Thread workerThread;

	    /** The sleeping. */
    	private boolean sleeping;

	    /** The sleep time. */
    	private long sleepTime = 1000 * 60 * 1;

	    /** The props. */
    	private Properties props;
	    
    	/** The username. */
    	private String username;
	    
    	/** The issue repo uid. */
    	private String issueRepoUid;

	    /**
    	 * Instantiates a new issue repository inbox queue worker.
    	 * 
    	 * @param config the config
    	 * @param id the id
    	 * @param desc the desc
    	 * @param selector the selector
    	 * 
    	 * @throws Exception the exception
    	 */
	    public IssueRepositoryInboxQueueWorker(Configuration config, UUID id, String desc, I_SelectProcesses selector) throws Exception {
	        super(config, id, desc);
	        props = new Properties();
	        username = (String) this.config.getEntry(this.getClass().getName(), "username", String.class);
	        issueRepoUid = (String) this.config.getEntry(this.getClass().getName(), "issueRepoUid", String.class);
	        
	        this.setPluginForInterface(I_GetWorkFromQueue.class, this);
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.worker.task.I_GetWorkFromQueue#queueContentsChanged()
    	 */
	    public void queueContentsChanged() {
	        if (this.sleeping) {
	            this.workerThread.interrupt();
	        }
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.worker.task.I_GetWorkFromQueue#start(org.dwfa.bpa.process.I_QueueProcesses)
    	 */
	    public void start(I_QueueProcesses queue) {
	        this.queue = queue;
	        this.workerThread = new Thread(this, "Worker " + this.getWorkerDesc());
	        this.workerThread.start();

	    }

	    /**
    	 * Sleep.
    	 */
    	public void sleep() {
	        this.sleeping = true;
	        try {
	            Thread.sleep(sleepTime);
	        } catch (InterruptedException e) {

	        }

	        this.sleeping = false;
	    }

	    /* (non-Javadoc)
    	 * @see java.lang.Runnable#run()
    	 */
	    public void run() {

	        while (true) {
	        		this.sleep();
	                logger.info("TermMed worker starting....");
	                logger.info(this.getWorkerDesc() + " starting inbox run");
	        		
	            	IssueSearchCriteria criteria = new IssueSearchCriteria(username, "Ready to download", null, null,null,null,null);
	            	IssueRepository repository = null;
					try {
						repository = IssueRepositoryDAO.getIssueRepository(LocalVersionedTerminology.get().getConcept(UUID.fromString(issueRepoUid)));
					} catch (TerminologyException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					IssueAssignmentsUtil.createAssignmentsFromIssues(repository, criteria, this, "Downloaded") ;
	        }
	    }
	    
	    /**
    	 * Gets the queue.
    	 * 
    	 * @return the queue
    	 */
    	public I_QueueProcesses getQueue() {
	    	return this.queue;
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.worker.Worker#execute(org.dwfa.bpa.process.I_EncodeBusinessProcess)
    	 */
	    public synchronized Condition execute(I_EncodeBusinessProcess process) throws TaskFailedException {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#isWorkspaceActive(java.util.UUID)
    	 */
	    public boolean isWorkspaceActive(UUID workspaceId) {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#createWorkspace(java.util.UUID, java.lang.String, java.io.File)
    	 */
	    public I_Workspace createWorkspace(UUID workspaceId, String name, File menuDir) throws WorkspaceActiveException,
	            Exception {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#getWorkspace(java.util.UUID)
    	 */
	    public I_Workspace getWorkspace(UUID workspaceId) throws NoSuchWorkspaceException {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#getCurrentWorkspace()
    	 */
	    public I_Workspace getCurrentWorkspace() {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#setCurrentWorkspace(org.dwfa.bpa.process.I_Workspace)
    	 */
	    public void setCurrentWorkspace(I_Workspace workspace) {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#getWorkspaces()
    	 */
	    public Collection<I_Workspace> getWorkspaces() {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#selectFromList(java.lang.Object[], java.lang.String, java.lang.String)
    	 */
	    public Object selectFromList(Object[] list, String title, String instructions) {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#createHeadlessWorkspace(java.util.UUID)
    	 */
	    public I_Workspace createHeadlessWorkspace(UUID workspace_id) throws WorkspaceActiveException, HeadlessException {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#createWorkspace(java.util.UUID, java.lang.String, org.dwfa.bpa.gui.I_ManageUserTransactions, java.io.File)
    	 */
    	public I_Workspace createWorkspace(UUID arg0, String arg1, I_ManageUserTransactions arg2, File menuDir)
	            throws WorkspaceActiveException, Exception {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#getObjFromFilesystem(java.awt.Frame, java.lang.String, java.lang.String, java.io.FilenameFilter)
    	 */
    	public Object getObjFromFilesystem(Frame arg0, String arg1, String arg2, FilenameFilter arg3) throws IOException,
	            ClassNotFoundException {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#writeObjToFilesystem(java.awt.Frame, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
    	 */
    	public void writeObjToFilesystem(Frame arg0, String arg1, String arg2, String arg3, Object arg4) throws IOException {
	        throw new UnsupportedOperationException();
	    }

	    /* (non-Javadoc)
    	 * @see org.dwfa.bpa.process.I_Work#getTransactionIndependentClone()
    	 */
    	public I_Work getTransactionIndependentClone() throws LoginException, ConfigurationException, IOException,
	            PrivilegedActionException {
	        throw new UnsupportedOperationException();
	    }

	}
