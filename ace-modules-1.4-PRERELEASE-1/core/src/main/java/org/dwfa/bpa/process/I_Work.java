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

package org.dwfa.bpa.process;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Logger;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.lease.LeaseListener;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.bpa.gui.I_ManageUserTransactions;
import org.dwfa.clock.I_KeepTime;

/**
 * Interface for workers that execute business processes.
 * 
 * @author kec
 * 
 */
public interface I_Work extends I_ManageProperties, I_KeepTime {

    /**
     * The active transaction is not shared between the workflow/worker
     * environment and the terminology environment. MasterWorker transactions
     * are necessarily under the control of the workflow environment.
     * Termninology transactions are under control of the user, but workflow may
     * ensure transactional boundries on the terminology environment by
     * committing any active transactions.
     * 
     * @return A transaction
     * @throws LeaseDeniedException
     * @throws RemoteException
     * @throws IOException
     * @throws InterruptedException
     * @throws PrivilegedActionException
     */
    public Transaction getActiveTransaction() throws LeaseDeniedException, RemoteException, IOException,
            InterruptedException, PrivilegedActionException;

    /**
     * Creates a second transaction that will become the active transaction
     * after the active transaction commits. This second transaction supports
     * <code>writeThenTake</code> operations used by tasks to create
     * transactional boundaries without giving up control of a process.
     * 
     * @return A transaction
     * @throws LeaseDeniedException
     * @throws RemoteException
     * @throws IOException
     * @throws InterruptedException
     */
    public Transaction getNextTransaction() throws LeaseDeniedException, RemoteException, IOException,
            InterruptedException;

    public void abortActiveTransaction() throws UnknownTransactionException, CannotAbortException, RemoteException;

    public void discardActiveTransaction();

    /**
     * Commits the active transaction, and makes the next transaction (if any)
     * the active transaction.
     * 
     * @throws RemoteException
     * @throws TransactionException
     */
    public void commitActiveTransaction() throws RemoteException, TransactionException;

    public void commitTransactionIfActive() throws TransactionException, RemoteException;

    /**
     * @return Unique identifier for this worker.
     */
    public UUID getId();

    /**
     * @return Text description of this worker.
     */
    public String getWorkerDesc();

    /**
     * @param workspaceId
     * @return True if a workspace with the provided identifier is active.
     */
    public boolean isWorkspaceActive(UUID workspaceId);

    /**
     * Create a new workspace with the following properties.
     * 
     * @param workspaceId
     *            identifier for this workspace
     * @param name
     *            name of this workspace
     * @param config
     *            terminology configuraiton associated with this workspace
     * @return A new workspace with the provided properties.
     * @throws WorkspaceActiveException
     *             Thrown if a workspace with the provided id has already been
     *             created.
     * @throws QueryException
     * @throws HeadlessException
     * @throws Exception
     */
    public I_Workspace createWorkspace(UUID workspaceId, String name, File menuDir) throws WorkspaceActiveException,
            Exception;

    public I_Workspace createWorkspace(UUID workspaceId, String name, I_ManageUserTransactions transactionInterface,
            File menuDir) throws WorkspaceActiveException, Exception;

    /**
     * @param workspaceId
     * @return Workspace corresponding to the provided identifier
     * @throws NoSuchWorkspaceException
     */
    public I_Workspace getWorkspace(UUID workspaceId) throws NoSuchWorkspaceException;

    /**
     * @return the last workspace created or retrieved, or set with a call to
     *         <code>setCurrentWorkspace</code>.
     */
    public I_Workspace getCurrentWorkspace();

    /**
     * @param workspace
     *            Sets the current workspace.
     */
    public void setCurrentWorkspace(I_Workspace workspace);

    /**
     * @return a collection of all the active workspaces.
     */
    public Collection<I_Workspace> getWorkspaces();

    /**
     * @return the Jini configuration associated with this worker.
     */
    public Configuration getJiniConfig();

    /**
     * Present a list to the user for selection. Provided as a means to get
     * input from a user before a workspace is created.
     * 
     * @param list
     *            List of object for selection from
     * @param title
     *            Title for the dialog window
     * @param instructions
     *            Selection instruction included in the dialog window.
     * @return
     * @throws TaskFailedException
     */
    public Object selectFromList(Object[] list, String title, String instructions) throws TaskFailedException;

    /**
     * Causes this worker to execute a process
     * 
     * @param process
     *            The process to be executed
     * @throws TaskFailedException
     */
    public Condition execute(I_EncodeBusinessProcess process) throws TaskFailedException;

    public Stack<I_EncodeBusinessProcess> getProcessStack();

    public void setProcessStack(Stack<I_EncodeBusinessProcess> processStack);

    /**
     * Allow tasks to write to the worker's log.
     * 
     * @return Logger for this worker.
     */
    public Logger getLogger();

    /**
     * @param workspace_id
     * @param config
     * @return
     * @throws QueryException
     * @throws HeadlessException
     * @throws WorkspaceActiveException
     * @throws TransactionException
     */
    public I_Workspace createHeadlessWorkspace(UUID workspace_id) throws WorkspaceActiveException, HeadlessException,
            TransactionException;

    public LoginContext getLoginContext();

    public void login() throws LoginException;

    public void logout() throws LoginException;

    public Object prepareProxy(Object proxy, Class<I_QueueProcesses> interfaceClass) throws ConfigurationException,
            PrivilegedActionException;

    public ServiceItemFilter getServiceProxyFilter() throws ConfigurationException;

    public Object doAsPrivileged(PrivilegedExceptionAction<Object> action, AccessControlContext acc)
            throws PrivilegedActionException;

    public Object doAsPrivileged(PrivilegedAction<Object> action, AccessControlContext acc);

    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter, long waitDur)
            throws InterruptedException, RemoteException, PrivilegedActionException, ConfigurationException;

    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter) throws InterruptedException,
            RemoteException, PrivilegedActionException, ConfigurationException;

    public ServiceItem[] lookup(ServiceTemplate tmpl, int minMatches, int maxMatches, ServiceItemFilter filter,
            long waitDur) throws InterruptedException, RemoteException, PrivilegedActionException,
            ConfigurationException;

    public ServiceItem[] lookup(ServiceTemplate tmpl, int minMatches, int maxMatches, ServiceItemFilter filter,
            long waitDur, boolean lookupLocal) throws InterruptedException, RemoteException, PrivilegedActionException,
            ConfigurationException;

    public ServiceItem[] lookupAllGroups(ServiceTemplate tmpl, int minMatches, int maxMatches,
            ServiceItemFilter filter, long waitDur) throws InterruptedException, RemoteException,
            PrivilegedActionException, ConfigurationException;

    public ServiceItem[] lookupAllGroups(ServiceTemplate template, int maxMatches, ServiceItemFilter filter)
            throws InterruptedException, RemoteException, PrivilegedActionException, ConfigurationException;

    public Transaction createTransaction(long transactionDuration) throws PrivilegedActionException;

    public void renewFor(Lease lease, long desiredDuration, long renewDuration, LeaseListener listener);

    public LookupCache createLookupCache(ServiceTemplate tmpl, ServiceItemFilter filter,
            ServiceDiscoveryListener listener) throws RemoteException, PrivilegedActionException;

    public JoinManager createJoinManager(Object object, Entry[] entries, ServiceID serviceID,
            LeaseRenewalManager leaseMgr) throws PrivilegedActionException;

    public ServiceItemFilter getServiceProxyFilter(ServiceItemFilter extraFilter) throws ConfigurationException;

    public Object getObjFromFilesystem(Frame parent, String title, String startDir, FilenameFilter fileFilter)
            throws IOException, ClassNotFoundException;

    public void writeObjToFilesystem(Frame parent, String title, String startDir, String defaultFile, Object obj)
            throws IOException;

    public boolean isExecuting();

    public void flagExecutionStop();

    public boolean isExecutionStopFlagged();

    public I_Work getTransactionIndependentClone() throws LoginException, ConfigurationException, IOException,
            PrivilegedActionException;

    public I_KeepTime getTimer();

    public void setTimer(I_KeepTime timer);

    public void writeAttachment(String key, Object value);

    public Object readAttachement(String key);

    public Object takeAttachment(String key);

    public I_PluginToWorker getPluginForInterface(Class<? extends I_PluginToWorker> pluginInterface);

    public void setPluginForInterface(Class<? extends I_PluginToWorker> pluginInterface, I_PluginToWorker plugin);
}
