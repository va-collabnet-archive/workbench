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
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.NoSuchWorkspaceException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.WorkspaceActiveException;
import org.dwfa.bpa.util.Base64;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.bpa.worker.task.I_GetWorkFromQueue;
import org.dwfa.jini.ElectronicAddress;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.integration.util.IssueAssignmentsUtil;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class IssueRepositoryOutboxQueueWorker.
 */
public class IssueRepositoryOutboxQueueWorker extends Worker implements I_GetWorkFromQueue, Runnable {

    /** The Constant PROCESS_ATTACHMENT_TYPE. */
    public static final String PROCESS_ATTACHMENT_TYPE = "application/x-java-serialized-object-base64";

    /** The queue. */
    private I_QueueProcesses queue;

    /** The worker thread. */
    private Thread workerThread;

    /** The sleeping. */
    private boolean sleeping;

    /** The sleep time. */
    private long sleepTime = 1000 * 60 * 1;

    /** The selector. */
    private I_SelectProcesses selector;

    /** The props. */
    private Properties props;

    /** The no smtp. */
    private boolean noSmtp = true;
    
    /** The no issue repository. */
    private boolean noIssueRepository = false;

    /**
     * Instantiates a new issue repository outbox queue worker.
     * 
     * @param config the config
     * @param id the id
     * @param desc the desc
     * @param selector the selector
     * 
     * @throws ConfigurationException the configuration exception
     * @throws LoginException the login exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PrivilegedActionException the privileged action exception
     */
    public IssueRepositoryOutboxQueueWorker(Configuration config, UUID id, String desc, I_SelectProcesses selector)
            throws ConfigurationException, LoginException, IOException, PrivilegedActionException {
        super(config, id, desc);
        this.selector = selector;
        props = new Properties();
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
    	logger.fine("Running TermMed Outbox worker...");
    	logger.fine(this.getWorkerDesc() + " starting outbox run");
        Transaction t;
        while (true) {
            try {
                BusinessProcess.validateAddress(this.queue.getNodeInboxAddress(), null);
                while (true) {
                    logger.fine(this.getWorkerDesc() + " starting outbox run");
                    t = this.getActiveTransaction();
                    I_EncodeBusinessProcess process = this.queue.take(selector, t);
                    logger.info(this.getWorkerDesc() + " found process: " + process);
                    try {
                        BusinessProcess.validateAddress(process.getOriginator(), process.getProcessID());
                    } catch (TaskFailedException ex) {
                        logger.info(this.getWorkerDesc() + " found missing or malformed origin for process: " + process
                            + " setting origin to queue's node inbox address");
                        process.setOriginator(this.queue.getNodeInboxAddress());

                    }

                    try {
                        if (issueRepositoryDelivery(process, t)) {
                            this.commitTransactionIfActive();
                        } else if (smtpDelivery(process, t)) {
                            this.commitTransactionIfActive();
                        } else if (jiniDelivery(process, t)) {
                            this.commitTransactionIfActive();
                        }else {
                            this.discardActiveTransaction();
                            logger.info("Worker: " + this.getWorkerDesc() + " (" + this.getId()
                                + ") cannot deliver process to: " + process.getDestination());
                        }
                    } catch (TaskFailedException ex) {
                        logger.severe(this.getWorkerDesc() + " cannot deliver process " + process.getId() + " to: "
                            + process.getDestination());
                        this.discardActiveTransaction();
                    }
                }
            } catch (NoMatchingEntryException ex) {
                try {
                    this.abortActiveTransaction();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Worker: " + this.getWorkerDesc() + " (" + this.getId() + ") "
                        + e.getMessage(), e);
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(this.getWorkerDesc() + " (" + this.getId() + ") started sleep.");
                }
                this.sleep();
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(this.getWorkerDesc() + " (" + this.getId() + ") awake.");
                }
            } catch (Throwable ex) {
                this.discardActiveTransaction();
                logger.log(Level.SEVERE, this.getWorkerDesc(), ex);
            }
        }
    }

    /**
     * Smtp delivery.
     * 
     * @param process the process
     * @param t the t
     * 
     * @return true, if successful
     */
    private boolean smtpDelivery(I_EncodeBusinessProcess process, Transaction t) {
        if (noSmtp) {
            return false;
        }
        try {
            logger.info(this.getWorkerDesc() + " trying SMTP delivery. ");
            process.validateAddresses();
            Session mailConnection = Session.getInstance(props, null);
            Message msg = new MimeMessage(mailConnection);
            Address from = new InternetAddress(process.getOriginator());
            msg.setFrom(from);
            // TODO make this more robust by only looking for splits
            // outside of quotes.
            String[] destinations = process.getDestination().split("[,]");
            Address[] toAddresses = new Address[destinations.length];
            for (int i = 0; i < destinations.length; i++) {
                toAddresses[i] = new InternetAddress(destinations[i]);
            }
            msg.setRecipients(Message.RecipientType.TO, toAddresses);

            msg.setSubject(process.getProcessID().toString());
            msg.addHeader("X-Priority", process.getPriority().getXPriorityValue());
            MimeBodyPart bodyPart = new MimeBodyPart();
            StringBuffer msgBody = new StringBuffer();
            msgBody.append("process id: " + process.getProcessID().toString());
            msgBody.append("\nCurrent task: " + process.getCurrentTaskId());
            msgBody.append("\nDeadline: " + process.getDeadline());
            msgBody.append("\nPriority: " + process.getPriority());
            msgBody.append("\nSubject: " + process.getSubject());
            bodyPart.setText(msgBody.toString());

            MimeBodyPart processPart = new MimeBodyPart();
            processPart.setContent(Base64.encodeObject(process), PROCESS_ATTACHMENT_TYPE);
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);
            multipart.addBodyPart(processPart);
            msg.setContent(multipart);

            Transport.send(msg);

            logger.info(this.getWorkerDesc() + " process: " + process.getProcessID() + " delivered to: "
                + process.getDestination() + " via SMTP. ");
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, this.getWorkerDesc() + " cannot deliver message", e);
        }
        return false;
    }
    
    /**
     * Checks if is sue repository delivery.
     * 
     * @param process the process
     * @param t the t
     * 
     * @return true, if is sue repository delivery
     */
    private boolean issueRepositoryDelivery(I_EncodeBusinessProcess process, Transaction t) {
        if (noIssueRepository) {
            return false;
        }
        try {
            logger.info(this.getWorkerDesc() + " trying IssueRepository delivery. ");
            process.validateAddresses();
            
            // TODO: change to use standard key
            Issue attachedIssue = (Issue) process.readAttachement("issueKey");
            if (attachedIssue==null) {
            	logger.info(this.getWorkerDesc() + " Null Issue. ");
            	return false;
            }
            IssueRepository repository = IssueRepositoryDAO.getIssueRepository(
            		LocalVersionedTerminology.get().getConcept(attachedIssue.getRepositoryUUId()));
            
            if (attachedIssue.getExternalId() == null) {
            	// create
            	logger.info(this.getWorkerDesc() + " Before create. ");
                IssueAssignmentsUtil.createIssueFromBP(repository, (BusinessProcess) process, "Ready to download");
                logger.info(this.getWorkerDesc() + " After create. ");
            } else {
            	// update
            	logger.info(this.getWorkerDesc() + " Before update. ");
                IssueAssignmentsUtil.updateIssueFromBP(repository, (BusinessProcess) process, "Ready to download");
                logger.info(this.getWorkerDesc() + " After update. ");
            }
            
            logger.info(this.getWorkerDesc() + " process: " + process.getProcessID() + " delivered to: "
                    + process.getDestination() + " via IssueRepository. ");
            
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, this.getWorkerDesc() + " cannot deliver message by issue repository", e);
        }
        return false;
    }

    /**
     * Jini delivery.
     * 
     * @param process the process
     * @param t the t
     * 
     * @return true, if successful
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws TransactionException the transaction exception
     * @throws TaskFailedException the task failed exception
     */
    private boolean jiniDelivery(I_EncodeBusinessProcess process, Transaction t) throws IOException,
            TransactionException, TaskFailedException {

        ElectronicAddress eAddress = new ElectronicAddress(process.getDestination());
        logger.info(this.getWorkerDesc() + " trying jini delivery. ");
        ServiceTemplate tmpl = new ServiceTemplate(null, new Class[] { I_QueueProcesses.class },
            new Entry[] { eAddress });
        ServiceItemFilter filter = null;
        try {
            process.validateDestination();
            ServiceItem item = jinilookup(tmpl, filter);
            if (item == null) {
                return false;
            }
            I_QueueProcesses queue = (I_QueueProcesses) item.service;
            queue.write(process, t);
            logger.info(this.getWorkerDesc() + " process: " + process.getProcessID() + " delivered to: "
                + process.getDestination() + " via Jini discovered queue service. ");
            return true;
        } catch (InterruptedException e) {
            logger.info(this.getWorkerDesc() + " Jini delivery failed: " + e.toString());
        } catch (IOException e) {
            logger.info(this.getWorkerDesc() + " Jini delivery failed: " + e.toString());
        } catch (PrivilegedActionException e) {
            logger.info(this.getWorkerDesc() + " Jini delivery failed: " + e.toString());
        } catch (ConfigurationException e) {
            logger.info(this.getWorkerDesc() + " Jini delivery failed: " + e.toString());
        }
        return false;

    }

    /**
     * Jinilookup.
     * 
     * @param tmpl the tmpl
     * @param filter the filter
     * 
     * @return the service item
     * 
     * @throws InterruptedException the interrupted exception
     * @throws RemoteException the remote exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PrivilegedActionException the privileged action exception
     * @throws ConfigurationException the configuration exception
     */
    protected ServiceItem jinilookup(ServiceTemplate tmpl, ServiceItemFilter filter) throws InterruptedException,
            RemoteException, IOException, PrivilegedActionException, ConfigurationException {
        ServiceItem item = lookup(tmpl, filter, 1000 * 10);
        return item;
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
