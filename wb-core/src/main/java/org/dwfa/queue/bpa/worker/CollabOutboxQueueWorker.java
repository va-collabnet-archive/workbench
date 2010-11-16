/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
/*
 * Created on May 31, 2005
 */
package org.dwfa.queue.bpa.worker;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;

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

import com.collabnet.ce.soap50.types.SoapFieldValues;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDependencySoapList;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactDependencySoapRow;
import com.collabnet.ce.soap50.webservices.tracker.ArtifactSoapDO;

/**
 * @author Marc Campbell
 *
 */
public class CollabOutboxQueueWorker extends Worker implements I_GetWorkFromQueue, Runnable {

    public static final String PROCESS_ATTACHMENT_TYPE = "application/x-java-serialized-object-base64";

    private I_QueueProcesses queue;

    // WORKER
    private Thread workerThread;
    private boolean sleeping;
    private long sleepTime = 1000 * 60 * 1;
    private I_SelectProcesses selector;
    private Properties props;

    // CollabNet parameters
    private String repoUrlStr;
    private String repoTrackerIdStr;
    private String userNameStr;
    private String userPwdStr;

    public CollabOutboxQueueWorker(Configuration config, UUID id, String desc,
            I_SelectProcesses selector) throws ConfigurationException, LoginException, IOException,
            PrivilegedActionException {
        super(config, id, desc);
        this.selector = selector;
        props = new Properties();

        try {
            // TRACKER CONNECTION PARAMETERS
            repoUrlStr = (String) this.config.getEntry(this.getClass().getName(), "repoUrlStr",
                    String.class);
            props.put("repo.url.str", repoUrlStr);

            repoTrackerIdStr = (String) this.config.getEntry(this.getClass().getName(),
                    "repoTrackerIdStr", String.class);
            props.put("repo.trackid.str", repoTrackerIdStr);

            // LOGIN SESSION PARAMETERS
            userNameStr = (String) this.config.getEntry(this.getClass().getName(), "userNameStr",
                    String.class);
            props.put("user.name.str", userNameStr);

            userPwdStr = (String) this.config.getEntry(this.getClass().getName(), "userPwdStr",
                    String.class);
            props.put("user.pwd.str", userPwdStr);

        } catch (ConfigurationException e) {
            this.getLogger().info(
                    "Collabnet Server not configured. Collabnet sending will be turned off. ");
        }

        this.setPluginForInterface(I_GetWorkFromQueue.class, this);
    }

    /**
     * @see org.dwfa.bpa.worker.task.I_GetWorkFromQueue#queueContentsChanged()
     */
    public void queueContentsChanged() {
        if (this.sleeping) {
            this.workerThread.interrupt();
        }
    }

    /**
     * @see org.dwfa.bpa.worker.task.I_GetWorkFromQueue#start(org.dwfa.bpa.process.I_QueueProcesses)
     */
    public void start(I_QueueProcesses queue) {
        this.queue = queue;
        this.workerThread = new Thread(this, "Worker " + this.getWorkerDesc());
        this.workerThread.start();
    }

    public void sleep() {
        this.sleeping = true;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            // NOTHING TO DO
        }
        this.sleeping = false;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        // check of any *.bp in the outbox directory
        // For each BP submit Tracker Artifact to Collabnet

        Transaction t;
        while (true) {
            try {
                BusinessProcess.validateAddress(this.queue.getNodeInboxAddress(), null);
                while (true) {
                    logger.info("RUN: CollabOutboxQueueWorker.run() ... " + this.getWorkerDesc());
                    t = this.getActiveTransaction();

                    I_EncodeBusinessProcess process = this.queue.take(selector, t);
                    logger.info(this.getWorkerDesc() + " found process: " + process);

                    try {
                        BusinessProcess.validateAddress(process.getOriginator(), process
                                .getProcessID());
                    } catch (TaskFailedException ex) {
                        logger.info(this.getWorkerDesc()
                                + " found missing or malformed origin for process: " + process
                                + " setting origin to queue's node inbox address");
                        process.setOriginator(this.queue.getNodeInboxAddress());

                    }

                    if (doCollabNetDelivery(process, t)) {
                        this.commitTransactionIfActive();
                    } else {
                        this.discardActiveTransaction();
                        logger.info("Worker: " + this.getWorkerDesc() + " (" + this.getId()
                                + ") cannot deliver process to: " + process.getDestination());
                    }

                }

            } catch (NoMatchingEntryException ex) {

                try {
                    this.abortActiveTransaction();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Worker: " + this.getWorkerDesc() + " ("
                            + this.getId() + ") " + e.getMessage(), e);
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
                ex.printStackTrace();
                logger.log(Level.SEVERE, this.getWorkerDesc(), ex);
            }
        }
    }

    private String doCollabNetArtfCreate(I_EncodeBusinessProcess process, TrackerAppSoapUtil tracker) throws IllegalArgumentException, IntrospectionException, IllegalAccessException, InvocationTargetException, IOException {
        // CREATE ARTIFACT
        int priority = 0;
        int estimatedHours = 0;
        String artTitle = process.getName();
        String artDescription = (String) process.getProperty("A: DESCRIPTION");
        String group = "";
        String category = (String) process.getProperty("A: CATEGORY");
        String customer = (String) process.getProperty("A: CUSTOMER");
        String sendStatus = (String) process.getProperty("A: SEND_STATUS");
        String sendToUser = (String) process.getProperty("A: SEND_TO_USER");
        String sendComment = (String) process.getProperty("A: SEND_COMMENT");
        String releasedId = "";

        // CUSTOM FLEX FIELDS (OPTIONAL)
        SoapFieldValues sfv = null;

        // CREATE ATTACHMENT
        String attachName = process.getProcessID().getUuid().toString() + ".bp";
        String attachMimeType = "application/octet-stream";

        process.setProperty("A: SEND_COMMENT", "");
        String attachmentFileId = tracker.uploadAttachment(process);

        // CREATE ARTIFACT
        ArtifactSoapDO asdo = tracker.createArtifact(repoTrackerIdStr, artTitle,
                artDescription, group, category, sendStatus, customer, priority, estimatedHours,
                sendToUser, releasedId, sfv, attachName, attachMimeType,
                attachmentFileId);

        tracker.setArtifactData(asdo, sendComment);

        String originId = (String) process.getProperty("A: ID_ARTF_PARENT");
        if (originId != null) {
            String targetId = asdo.getId();
            String description = category + " detail for " + customer;
            tracker.createArtifactDependency(originId, targetId, description);
        }

        // logArtfSoapDO(asdo);

        return asdo.getId();
    }

    private void doCollabNetArtfUpdate(I_EncodeBusinessProcess process, TrackerAppSoapUtil tracker,
            String sessionId, String artfId) throws IllegalArgumentException, IntrospectionException, IllegalAccessException, InvocationTargetException, IOException {
        String sendStatus = (String) process.getProperty("A: SEND_STATUS");
        String sendToUser = (String) process.getProperty("A: SEND_TO_USER");
        String sendComment = (String) process.getProperty("A: SEND_COMMENT");
        Integer rowTotal = (Integer) process.getProperty("A: ROW_TOTAL");

        // UPLOAD ATTACHMENT
        String attachName = process.getProcessID().getUuid().toString() + ".bp";
        String attachMimeType = "application/octet-stream";
        // String attachMimeType = "application/x-java-serialized-object";
        process.setProperty("A: SEND_COMMENT", "");
        String attachId = tracker.uploadAttachment(process);

        // SET STATUS
        ArtifactSoapDO asdo = tracker.getArtifactData(sessionId, artfId);
        asdo.setAssignedTo(sendToUser);
        asdo.setStatus(sendStatus);
        tracker.setArtifactData(asdo, sendComment, attachName, attachMimeType, attachId);

        if (sendStatus.equalsIgnoreCase("Detail closed")) {

            String sendParent = (String) process.getProperty("A: ID_ARTF_PARENT");
            if (sendParent != null && sendParent.length() > 0) {
                // CHECK IF PARENT NEEDS TO BE CLOSED
                ArtifactDependencySoapRow[] children = tracker.getChildDependencyRows(sendParent);

                // CASE: NOT ALL DETAILS STARTED
                if (children.length < rowTotal)
                    return;

                // CASE: ALL DETAILS STARTED...
                boolean allClosed = true;
                for (ArtifactDependencySoapRow child : children)
                    if (child.getTargetStatusClass().equalsIgnoreCase("Open"))
                        allClosed = false;

                if (allClosed) {
                    // CLOSE PARENT
                    ArtifactSoapDO asdoParent = tracker.getArtifactData(sessionId, sendParent);
                    asdoParent.setStatus("Master closed");
                    tracker.setArtifactData(asdoParent, "Details have been closed.", null, null,
                            null);
                }
            }
        }

    }

    public boolean doCollabNetDelivery(I_EncodeBusinessProcess process, Transaction t) throws IllegalArgumentException, IntrospectionException, IllegalAccessException, InvocationTargetException {

        logger.info(this.getWorkerDesc() + " trying CollabNet delivery. ");
        try {
            // process.validateAddresses();

            // CONNECTION (URL_STR)
            CollabNetSoapConnection sfc = new CollabNetSoapConnection(repoUrlStr);
            logger.info("[INFO] connection object: " + sfc.toString());

            // SESSION (USER_STR, PWD_STR)
            String sessionId = sfc.login(userNameStr, userPwdStr);
            logger.info("[INFO]         sessionId: " + sessionId);

            // TRACKER (URL_STR, sessionId)
            TrackerAppSoapUtil tracker = new TrackerAppSoapUtil(repoUrlStr, sessionId);
            logger.info("[INFO]         trackerId: " + repoTrackerIdStr);
            logger.info("[INFO]           tracker: " + tracker);

            String artfId = (String) process.getProperty("A: ID_ARTF");
            if (artfId.equalsIgnoreCase("NA"))
                artfId = doCollabNetArtfCreate(process, tracker);
            else
                doCollabNetArtfUpdate(process, tracker, sessionId, artfId);

            // LOGOFF
            sfc.logoff(sessionId);
            logger.info("[INFO] logoff successful for \"" + sessionId + "\"");

            return true;
//        } catch (TaskFailedException e) {
//            e.printStackTrace();
//            logger.log(Level.WARNING, this.getWorkerDesc()
//                    + " cannot deliver message (addresses not valid)", e);
        } catch (RemoteException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, this.getWorkerDesc()
                    + " cannot deliver message (possible login issue)", e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.WARNING, this.getWorkerDesc()
                    + " cannot deliver message (possible upload issue)", e);
        }

        return false;
    }

    public synchronized Condition execute(I_EncodeBusinessProcess process)
            throws TaskFailedException {
        throw new UnsupportedOperationException();
    }
    public boolean isWorkspaceActive(UUID workspaceId) {
        throw new UnsupportedOperationException();
    }
    public I_Workspace createWorkspace(UUID workspaceId, String name, File menuDir)
            throws WorkspaceActiveException, Exception {
        throw new UnsupportedOperationException();
    }
    public I_Workspace getWorkspace(UUID workspaceId) throws NoSuchWorkspaceException {
        throw new UnsupportedOperationException();
    }
    public I_Workspace getCurrentWorkspace() {
        throw new UnsupportedOperationException();
    }
    public void setCurrentWorkspace(I_Workspace workspace) {
        throw new UnsupportedOperationException();
    }
    public Collection<I_Workspace> getWorkspaces() {
        throw new UnsupportedOperationException();
    }
    public Object selectFromList(Object[] list, String title, String instructions) {
        throw new UnsupportedOperationException();
    }
    public I_Workspace createHeadlessWorkspace(UUID workspace_id) throws WorkspaceActiveException,
            HeadlessException {
        throw new UnsupportedOperationException();
    }
    public I_Workspace createWorkspace(UUID arg0, String arg1, I_ManageUserTransactions arg2,
            File menuDir) throws WorkspaceActiveException, Exception {
        throw new UnsupportedOperationException();
    }
    public Object getObjFromFilesystem(Frame arg0, String arg1, String arg2, FilenameFilter arg3)
            throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
    }
    public void writeObjToFilesystem(Frame arg0, String arg1, String arg2, String arg3, Object arg4)
            throws IOException {
        throw new UnsupportedOperationException();
    }
    public I_Work getTransactionIndependentClone() throws LoginException, ConfigurationException,
            IOException, PrivilegedActionException {
        throw new UnsupportedOperationException();
    }
}