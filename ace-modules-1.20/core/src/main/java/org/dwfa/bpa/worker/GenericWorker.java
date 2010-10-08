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
 * Created on Apr 18, 2005
 */
package org.dwfa.bpa.worker;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import javax.security.auth.login.LoginException;
import javax.swing.SwingUtilities;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;

import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.bpa.process.GenericTaskEntry;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoSuchWorkspaceException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.WorkspaceActiveException;
import org.dwfa.bpa.util.StopThreadException;

/**
 * @author kec
 * 
 */
public class GenericWorker extends Worker implements Runnable {

    private JavaSpace05 space;
    private String status;
    private InetAddress host;
    private WorkerTableModel model;
    private int tableRow;
    private int tasksCompleted = 0;
    private GenericTaskEntry currentTask;
    private boolean stop = false;

    /**
     * @return Returns the currentTask.
     */
    protected GenericTaskEntry getCurrentTask() {
        return currentTask;
    }

    /**
     * @return Returns the tasksCompleted.
     */
    protected int getTasksCompleted() {
        return tasksCompleted;
    }

    public void stop() {
        stop = true;
    }

    /**
     * @return Returns the currentWorkspace.
     */
    public I_Workspace getCurrentWorkspace() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param currentWorkspace
     *            The currentWorkspace to set.
     */
    public void setCurrentWorkspace(I_Workspace currentWorkspace) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param config
     * @param id
     * @param desc
     * @throws ConfigurationException
     * @throws LoginException
     * @throws IOException
     * @throws PrivilegedActionException
     */
    public GenericWorker(Configuration config, UUID id, String desc, InetAddress host, JavaSpace05 space,
            WorkerTableModel model, int tableRow) throws ConfigurationException, LoginException, IOException,
            PrivilegedActionException {
        super(config, id, desc);
        this.host = host;
        this.space = space;
        this.model = model;
        this.tableRow = tableRow;
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Created new worker: " + desc + " id: " + id);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#isWorkspaceActive(UUID)
     */
    public boolean isWorkspaceActive(UUID workspaceId) {
        return false;
    }

    /**
     * @throws WorkspaceActiveException
     * @throws QueryException
     * @see org.dwfa.bpa.process.I_Work#createWorkspace(UUID, java.lang.String,
     *      org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createWorkspace(UUID workspaceId, String title, File menuDir) throws WorkspaceActiveException {
        throw new UnsupportedOperationException();
    }

    public I_Workspace createWorkspace(UUID workspaceId, String title, I_ManageUserTransactions transactionInterface,
            File menuDir) throws WorkspaceActiveException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#selectService(net.jini.core.lookup.ServiceItem[])
     */
    public Object selectFromList(Object[] list, String title, String labelText) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkspace(UUID)
     */
    public I_Workspace getWorkspace(UUID workspaceId) throws NoSuchWorkspaceException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkspaces()
     */
    public Collection<I_Workspace> getWorkspaces() {
        throw new UnsupportedOperationException();
    }

    private void checkStop() throws StopThreadException {
        if (stop) {
            throw new StopThreadException();
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            while (true) {
                try {
                    checkStop();
                    // See if the entry for this worker already exists...
                    GenericWorkerEntry gwe = new GenericWorkerEntry(this.getDescWithHost(), this.id);
                    Entry e = space.readIfExists(gwe, null, 1000 * 60);
                    if (e == null) {
                        // If not, then add one...
                        Lease lease = space.write(gwe, null, 1000 * 60);
                        this.renewFor(lease, Lease.FOREVER, 1000 * 60, null);
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info(this.getWorkerDesc() + " recorded in space. " + gwe);
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine(this.getWorkerDesc() + " lease info: " + lease);
                            }
                        }
                    } else {
                        if (logger.isLoggable(Level.INFO)) {
                            logger.info(this.getWorkerDesc() + " already recorded in space. " + e);
                        }
                    }
                    checkStop();
                    Entry taskTemplate = space.snapshot(new GenericTaskEntry());
                    while (true) {
                        checkStop();
                        try {
                            this.updateTableCell(WorkerTableModel.TASK);
                            this.currentTask = null;
                            Transaction t = this.getActiveTransaction();
                            try {
                                this.status = "<html><font color='blue'>waiting for task";
                                this.updateTableCell(WorkerTableModel.STATUS);
                                this.currentTask = (GenericTaskEntry) space.take(taskTemplate, t, Long.MAX_VALUE);
                                this.updateTableCell(WorkerTableModel.TASK);
                            } catch (TransactionException ex) {
                                if (ex.getMessage().startsWith("completed while operation in progress")) {
                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.fine(this.getWorkerDesc() + " exception: " + ex.getClass().getName()
                                            + ": " + ex.getMessage() + " (system should self heal)");
                                    }
                                    this.discardActiveTransaction();
                                } else {
                                    throw ex;
                                }

                            }
                            if (this.currentTask != null) {
                                this.status = "<html><font color='green'>executing task";
                                this.updateTableCell(WorkerTableModel.STATUS);
                                Entry result = this.currentTask.execute(this, space);
                                this.tasksCompleted++;
                                this.updateTableCell(WorkerTableModel.TASKS_COMPLETE);
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.fine(this.getWorkerDesc() + " executed task: " + this.currentTask
                                        + ", with result: " + result);
                                }
                                if (result != null) {
                                    space.write(result, t, this.currentTask.resultLeaseTime());
                                }
                                checkStop();
                                this.commitTransactionIfActive();
                            }
                        } catch (TaskFailedException ex) {
                            logger.log(Level.WARNING, this.getWorkerDesc(), ex);
                        }
                    }

                } catch (TransactionException ex) {
                    logger.log(Level.INFO, this.getWorkerDesc() + " exception: " + ex.getClass().getName() + ": "
                        + ex.getMessage() + " (system should self heal)");
                } catch (StopThreadException ex) {
                    throw ex;
                } catch (Throwable ex) {
                    logger.log(Level.SEVERE, this.getWorkerDesc(), ex);
                }
            }
        } catch (StopThreadException ex) {
            return;
        }
    }

    private void updateTableCell(int column) {
        SwingUtilities.invokeLater(new UpdateTableCellRunnable(column));
    }

    private class UpdateTableCellRunnable implements Runnable {
        int column;

        /**
         * @param column
         */
        public UpdateTableCellRunnable(int column) {
            super();
            this.column = column;
        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            if (model != null) {
                model.fireTableCellUpdated(tableRow, column);
            }

        }
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#createHeadlessWorkspace(net.jini.id.Uuid,
     *      org.dwfa.bpa.gui.TerminologyConfiguration)
     */
    public I_Workspace createHeadlessWorkspace(UUID workspace_id) throws WorkspaceActiveException, HeadlessException {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Returns the host.
     */
    public InetAddress getHost() {
        return host;
    }

    public String getDescWithHost() {
        return this.getWorkerDesc() + " " + " host: " + host.getHostName() + " ip: " + host;
    }

    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }

    public Object getObjFromFilesystem(Frame parent, String title, String startDir, FilenameFilter fileFilter) {
        throw new UnsupportedOperationException();
    }

    public void writeObjToFilesystem(Frame parent, String title, String startDir, String defaultFile, Object obj)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    public I_Work getTransactionIndependentClone() {
        throw new UnsupportedOperationException();
    }

}
