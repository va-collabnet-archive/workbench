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
 * Created on Apr 21, 2005
 */
package org.dwfa.queue.bpa.worker;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.transaction.Transaction;

import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.NoSuchWorkspaceException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.WorkspaceActiveException;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.bpa.worker.task.I_GetWorkFromQueue;

/**
 * @author kec
 * 
 */
public class HeadlessQueueWorker extends Worker implements I_GetWorkFromQueue, Runnable {

    private I_QueueProcesses queue;
    private Thread workerThread;
    private boolean sleeping;
    private long sleepTime = 1000 * 60 * 1;
    private I_SelectProcesses selector;

    /**
     * @param config
     * @param id
     * @param desc
     * @throws ConfigurationException
     * @throws LoginException
     * @throws IOException
     */
    public HeadlessQueueWorker(Configuration config, UUID id, String desc, I_SelectProcesses selector)
            throws ConfigurationException, LoginException, IOException, PrivilegedActionException {
        super(config, id, desc);
        this.selector = selector;
        this.setPluginForInterface(I_GetWorkFromQueue.class, this);
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#isWorkspaceActive(net.jini.id.UUID)
     */
    public boolean isWorkspaceActive(UUID workspaceId) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#createWorkspace(net.jini.id.UUID,
     *      java.lang.String, org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createWorkspace(UUID workspaceId, String name, File menuDir) throws WorkspaceActiveException,
            HeadlessException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkspace(net.jini.id.UUID)
     */
    public I_Workspace getWorkspace(UUID workspaceId) throws NoSuchWorkspaceException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getCurrentWorkspace()
     */
    public I_Workspace getCurrentWorkspace() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#setCurrentWorkspace(org.dwfa.bpa.process.I_Workspace)
     */
    public void setCurrentWorkspace(I_Workspace workspace) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkspaces()
     */
    public Collection<I_Workspace> getWorkspaces() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#selectFromList(java.lang.Object[],
     *      java.lang.String, java.lang.String)
     */
    public Object selectFromList(Object[] list, String title, String instructions) {
        throw new UnsupportedOperationException();
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

    private void sleep() {
        this.sleeping = true;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {

        }

        this.sleeping = false;
    }

    public void run() {
        Transaction t;
        while (true) {
            try {
                while (true) {
                    try {
                        t = this.getActiveTransaction();
                        I_EncodeBusinessProcess process = this.queue.take(selector, t);
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info(this.getWorkerDesc() + " TAKE: " + process.getName() + " ("
                                + process.getProcessID() + ") " + ": " + process.getCurrentTaskId() + " "
                                + process.getTask(process.getCurrentTaskId()).getName() + " deadline: "
                                + dateFormat.format(process.getDeadline()));
                        }

                        this.execute(process);
                        this.commitTransactionIfActive();
                    } catch (TaskFailedException ex) {
                        this.discardActiveTransaction();
                        logger.log(Level.WARNING, "Worker: " + this.getWorkerDesc() + " (" + this.getId() + ") "
                            + ex.getMessage(), ex);
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
     * @see org.dwfa.bpa.process.I_Work#createHeadlessWorkspace(net.jini.id.UUID,
     *      org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createHeadlessWorkspace(UUID workspace_id) throws WorkspaceActiveException, HeadlessException {
        throw new UnsupportedOperationException();
    }

    public I_Workspace createWorkspace(UUID arg0, String arg1, I_ManageUserTransactions arg2, File menuDir)
            throws WorkspaceActiveException, Exception {
        throw new UnsupportedOperationException();
    }

    public Object getObjFromFilesystem(Frame arg0, String arg1, String arg2, FilenameFilter arg3) throws IOException,
            ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    public void writeObjToFilesystem(Frame arg0, String arg1, String arg2, String arg3, Object arg4) throws IOException {
        throw new UnsupportedOperationException();
    }

    public I_Work getTransactionIndependentClone() throws LoginException, ConfigurationException, IOException,
            PrivilegedActionException {
        throw new UnsupportedOperationException();
    }

}
