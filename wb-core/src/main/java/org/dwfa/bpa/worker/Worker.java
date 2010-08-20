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
 * Created on Apr 20, 2005
 */
package org.dwfa.bpa.worker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
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
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.ServerTransaction;
import net.jini.core.transaction.server.TransactionConstants;
import net.jini.lease.LeaseListener;
import net.jini.lease.LeaseRenewalManager;
import net.jini.lookup.JoinManager;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryListener;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.ServiceItemFilter;
import net.jini.security.AuthenticationPermission;
import net.jini.security.ProxyPreparer;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_PluginToWorker;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.clock.I_KeepTime;
import org.dwfa.clock.SystemTime;
import org.dwfa.jini.JiniManager;
import org.dwfa.util.bean.PropertyChangeSupportWithPropagationId;

/**
 * @author kec
 * 
 */
public abstract class Worker implements I_Work {

    public static class WorkerLevel extends Level {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        protected WorkerLevel(String name, int value) {
            super(name, value);
        }

        private static WorkerLevel infoPlus;

        public static WorkerLevel getInfoPlusLevel() {
            if (infoPlus == null) {
                infoPlus = new WorkerLevel("INFO_PLUS", 801);
            }
            return infoPlus;
        }
    }

    private boolean stopExecutionFlagged = false;

    public void flagExecutionStop() {
        stopExecutionFlagged = true;
    }

    public boolean isExecutionStopFlagged() {
        return stopExecutionFlagged;
    }

    public Object readAttachement(String key) {
        return attachments.get(key);
    }

    public Object takeAttachment(String key) {
        return attachments.remove(key);
    }

    public void writeAttachment(String key, Object value) {
        attachments.put(key, value);

    }

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private PropertyChangeSupport propChangeSupport = new PropertyChangeSupportWithPropagationId(this);

    private Stack<I_EncodeBusinessProcess> processStack = new Stack<I_EncodeBusinessProcess>();

    protected static List<Worker> workerList = new ArrayList<Worker>();

    protected static Logger logger = Logger.getLogger(I_Work.class.getName());

    private I_KeepTime timer = new SystemTime();

    /**
     * @see org.dwfa.bpa.process.I_Work#getLogger()
     */
    public Logger getLogger() {
        return logger;
    }

    protected Map<Class<? extends I_PluginToWorker>, I_PluginToWorker> pluginMap = new HashMap<Class<? extends I_PluginToWorker>, I_PluginToWorker>();

    private HashMap<String, Object> attachments = new HashMap<String, Object>();

    protected Configuration config;

    protected UUID id;

    protected String desc;

    private long transactionDuration;

    private LoginContext loginContext;

    private JiniManager jiniManager;

    private ServerTransaction activeTransaction;

    private ServerTransaction webTransaction;

    private ServerTransaction nextTransaction;

    private boolean executing = false;

    /**
     * @param config
     * @param id
     * @param desc
     * @throws ConfigurationException
     * @throws LoginException
     * @throws IOException
     * @throws PrivilegedActionException
     */
    @SuppressWarnings("unchecked")
    public Worker(Configuration config, UUID id, String desc) throws ConfigurationException, LoginException,
            IOException, PrivilegedActionException {
        super();
        this.config = config;
        this.id = id;
        this.desc = desc;
        workerList.add(this);
        try {
            this.loginContext = (LoginContext) config.getEntry(this.getClass().getName(), "loginContext",
                LoginContext.class);
            loginContext.login();
            logger.info("Worker " + this.desc + "(" + this.id + ") logged in. " + this.getClass().getName());
            Subject.doAsPrivileged(loginContext.getSubject(), new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    initAsSubject(Worker.this.config);
                    return null;
                }
            }, null);
        } catch (ConfigurationException e) {
            logger.info("Worker " + this.desc + "(" + this.id + ") has null loginContext. " + this.getClass().getName()
                + "\n" + e.toString());
            initAsSubject(config);
        }

    }

    protected void executeStartupProcesses() {
        try {
            File startupDirectory = (File) config.getEntry(this.getClass().getName(), "startupDirectory", File.class);
            File[] startupFiles = startupDirectory.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".bp");
                }
            });
            if (startupFiles != null) {
                for (int i = 0; i < startupFiles.length; i++) {
                    try {
                        logger.info("Executing business process: " + startupFiles[i]);
                        FileInputStream fis = new FileInputStream(startupFiles[i]);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
                        this.execute(process);
                        this.commitTransactionIfActive();
                        logger.info("Finished business process: " + startupFiles[i]);
                    } catch (Throwable e1) {
                        logger.log(Level.SEVERE, e1.getMessage() + " thrown by " + startupFiles[i], e1);
                    }
                }
            }
        } catch (ConfigurationException e) {
            logger.info("No startup directory found. " + e.getMessage());
        }

    }

    /**
     * @param config
     * @throws ConfigurationException
     * @throws IOException
     */
    private void initAsSubject(Configuration config) throws ConfigurationException, IOException {
        Long tranDurLong = (Long) this.config.getEntry(this.getClass().getName(), "tranDurLong", Long.class);
        this.transactionDuration = tranDurLong.longValue();

        if (JiniManager.isLocalOnly()) {
            logger.info("making local only jiniManager");
            this.jiniManager = JiniManager.getLocalOnlyJiniManager();

        } else {
            logger.info(" getting serviceDiscovery");
            ServiceDiscoveryManager serviceDiscovery = (ServiceDiscoveryManager) config.getEntry(this.getClass()
                .getName(), "serviceDiscovery", ServiceDiscoveryManager.class);
            this.jiniManager = new JiniManager(serviceDiscovery);
        }

        // testLookup(serviceDiscovery);
    }

    public Worker(Configuration config, Class<?> workerClass) throws ConfigurationException, LoginException,
            IOException, PrivilegedActionException {
        this(config, (UUID) config.getEntry(workerClass.getName(), "workerUuid", UUID.class, UUID.randomUUID()),
            (String) config.getEntry(workerClass.getName(), "name", String.class, "Anonomous " + workerClass.getName()));
    }

    public String toString() {
        return this.desc + " (" + this.id + ")";
    }

    public synchronized Condition execute(I_EncodeBusinessProcess process) throws TaskFailedException {
        stopExecutionFlagged = false;
        setExecuting(true);
        Condition condition;
        try {
            if (this.loginContext != null) {
                ExecuteAsPriviledgedAction action = new ExecuteAsPriviledgedAction(process);
                try {
                    condition = (Condition) this.doAsPrivileged(action, null);
                } catch (PrivilegedActionException e) {
                    throw new TaskFailedException(e);
                }
            } else {
                condition = executeProcess(process);
            }
            setExecuting(false);
            return condition;
        } catch (TaskFailedException ex) {
            setExecuting(false);
            this.setActiveTransaction(null);
            this.nextTransaction = null;
            throw ex;
        }
    }

    private void setExecuting(boolean executing) {
        boolean oldState = this.executing;
        this.executing = executing;
        this.propChangeSupport.firePropertyChange("executing", oldState, this.executing);
    }

    public boolean isExecuting() {
        return executing;
    }

    /**
     * @param process
     * @throws TaskFailedException
     */
    private Condition executeProcess(I_EncodeBusinessProcess process) throws TaskFailedException {
        if (stopExecutionFlagged) {
            return Condition.ITEM_CANCELED;
        }
        this.setProcessStack(new Stack<I_EncodeBusinessProcess>());
        Condition condition = process.execute(this);
        if (logger.isLoggable(Level.INFO)) {
            logger.info(this.getWorkerDesc() + "; process: " + process.getName() + " (" + process.getProcessID() + ") "
                + " end execute: " + condition);
        }
        try {
            if (condition.equals(Condition.WAIT_FOR_WEB_FORM) == false) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(this.getWorkerDesc() + "; process: " + process.getName() + " ("
                        + process.getProcessID() + ") " + " Committing transaction: " + this.activeTransaction
                        + " condition: " + condition);
                }
                this.commitTransactionIfActive();
            } else {
                this.webTransaction = this.activeTransaction;
                this.setActiveTransaction(null);
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(this.getWorkerDesc() + "; process: " + process.getName() + " ("
                        + process.getProcessID() + ") " + " Saving web transaction");
                }
            }
            return condition;
        } catch (CannotCommitException e) {
            logger.severe("Cannot commit: " + this.activeTransaction);

            this.setActiveTransaction(null);
            this.nextTransaction = null;
            throw new TaskFailedException(e);
        } catch (Throwable e) {
            this.setActiveTransaction(null);
            this.nextTransaction = null;
            throw new TaskFailedException(e);
        }
    }

    public void restoreWebTransaction() {
        this.setActiveTransaction(this.webTransaction);
    }

    private class ExecuteAsPriviledgedAction implements PrivilegedExceptionAction<Object> {
        I_EncodeBusinessProcess process;

        /**
         * @param process
         */
        public ExecuteAsPriviledgedAction(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
        }

        public Object run() throws TaskFailedException {
            return Worker.this.executeProcess(this.process);
        }

    }

    /**
     * @throws RemoteException
     * @throws CannotCommitException
     * @throws UnknownTransactionException
     * 
     */
    public void commitTransactionIfActive() throws UnknownTransactionException, CannotCommitException, RemoteException {
        if (this.activeTransaction != null) {
            this.commitActiveTransaction();
        }

    }

    /**
     * @throws IOException
     * @see org.dwfa.bpa.process.I_Work#getJiniManager()
     */
    protected JiniManager getJiniManager() throws IOException {
        return this.jiniManager;
    }

    public void abortActiveTransaction() throws UnknownTransactionException, CannotAbortException, RemoteException {
        if (this.activeTransaction == null) {
            throw new UnknownTransactionException("activeTransaction is null");
        }
        this.activeTransaction.abort();
        this.setActiveTransaction(null);
        if (this.nextTransaction != null) {
            this.nextTransaction.abort();
            this.nextTransaction = null;
        }
    }

    /**
     * @return Returns the activeTransaction.
     * @throws LeaseDeniedException
     * @throws RemoteException
     * @throws IOException
     * @throws InterruptedException
     */
    public Transaction getActiveTransaction() throws LeaseDeniedException, RemoteException, InterruptedException,
            IOException, PrivilegedActionException {

        try {
            if ((activeTransaction != null) && (activeTransaction.getState() == TransactionConstants.ACTIVE)) {
                return activeTransaction;
            }
        } catch (UnknownTransactionException e) {
            Worker.logger.info(this.getWorkerDesc() + " exception: " + e.getClass().getName() + " " + e.getMessage());
        }
        this.doAsPrivileged(new PrivilegedExceptionAction<Object>() {

            public Object run() throws LeaseDeniedException, RemoteException, InterruptedException {
                Transaction t = Worker.this.jiniManager.createTransaction(Worker.this.transactionDuration);
                Worker.this.setActiveTransaction(t);
                return null;
            }
        }, null);
        return activeTransaction;
    }

    public Transaction getTransactionIfActive() throws LeaseDeniedException, RemoteException, InterruptedException,
            IOException, PrivilegedActionException, UnknownTransactionException {
        if ((activeTransaction != null) && (activeTransaction.getState() != TransactionConstants.ACTIVE)) {
            activeTransaction = null;
        }
        return activeTransaction;
    }

    public void discardActiveTransaction() {
        this.setActiveTransaction(null);
        this.nextTransaction = null;
    }

    public void setActiveTransaction(Transaction t) {
        ServerTransaction oldValue = this.activeTransaction;
        if (t != null) {
            this.activeTransaction = (ServerTransaction) t;
        } else {
            this.activeTransaction = null;
        }
        this.propChangeSupport.firePropertyChange("activeTransaction", oldValue, this.activeTransaction);
    }

    public void commitActiveTransaction() throws UnknownTransactionException, CannotCommitException, RemoteException {
        if (this.activeTransaction == null) {
            throw new UnknownTransactionException("activeTransaction is null");
        }
        this.activeTransaction.commit();
        if (this.nextTransaction != null) {
            this.setActiveTransaction(this.nextTransaction);
            this.nextTransaction = null;
        } else {
            this.setActiveTransaction(null);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getId()
     */
    public UUID getId() {
        return id;
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkerDesc()
     */
    public String getWorkerDesc() {
        return desc;
    }

    public void setWorkerDesc(String desc) {
        this.desc = desc;
    }

    public String getWorkerDescWithId() {
        return desc + " (" + this.id + ")";
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getJiniConfig()
     */
    public Configuration getJiniConfig() {
        return this.config;
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propChangeSupport.addPropertyChangeListener(listener);

    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return this.propChangeSupport.getPropertyChangeListeners();
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#addPropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#removePropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return this.propChangeSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#hasListeners(java.lang.String)
     */
    public boolean hasListeners(String propertyName) {
        return this.propChangeSupport.hasListeners(propertyName);
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getNextTransaction()
     */
    public Transaction getNextTransaction() throws LeaseDeniedException, RemoteException, IOException,
            InterruptedException {
        try {
            if ((nextTransaction != null) && (nextTransaction.getState() == TransactionConstants.ACTIVE)) {
                return nextTransaction;
            }
        } catch (UnknownTransactionException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Transaction t = this.jiniManager.createTransaction(Long.MAX_VALUE);
        this.nextTransaction = (ServerTransaction) t;
        return t;
    }

    /**
     * @return Returns the processStack.
     */
    public Stack<I_EncodeBusinessProcess> getProcessStack() {
        return processStack;
    }

    /**
     * @param processStack
     *            The processStack to set.
     */
    public void setProcessStack(Stack<I_EncodeBusinessProcess> processStack) {
        this.processStack = processStack;
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getLoginContext()
     */
    public LoginContext getLoginContext() {
        return this.loginContext;
    }

    public void login() throws LoginException {
        if (this.loginContext != null) {
            this.loginContext.login();
        }
        throw new LoginException("loginContext is null");
    }

    public void logout() throws LoginException {
        if (this.loginContext != null) {
            this.loginContext.logout();
        }
    }

    public ProxyPreparer getProxyPreparer() throws ConfigurationException {
        return (ProxyPreparer) config.getEntry(this.getClass().getName(), "preparer", ProxyPreparer.class);
    }

    public Object prepareProxy(final Object proxy, final Class<I_QueueProcesses> interfaceClass)
            throws ConfigurationException, PrivilegedActionException {
        return this.doAsPrivileged(new PrivilegedExceptionAction<Object>() {
            public Object run() throws Exception {
                ProxyPreparer preparer = getProxyPreparer();
                ProxyDoAsSubjectFactory proxyFactory = new ProxyDoAsSubjectFactory(Worker.this);
                return proxyFactory.makeProxy(preparer.prepareProxy(proxy), interfaceClass);
            }
        }, null);

    }

    /**
     * Performs proxy preparation on discovered services. For more information
     * see the javadoc for the net.jini.lookup.ServiceItemFilter interface.
     */
    private class ProxyPreparerFilter implements ServiceItemFilter {
        private ProxyPreparer preparer;

        private ServiceItemFilter extraFilter;

        ProxyPreparerFilter(ProxyPreparer preparer, ServiceItemFilter extraFilter) {
            this.preparer = preparer;
            this.extraFilter = extraFilter;
        }

        /** See the javadoc for the ServiceItemFilter.check method. */
        public boolean check(ServiceItem item) {
            try {
                item.service = preparer.prepareProxy(item.service);
                if (extraFilter != null) {
                    return extraFilter.check(item);
                } else {
                    return true;
                }
            } catch (SecurityException e) {// definite exception
                return false; // fail: don't try again
            } catch (RemoteException e) {// indefinite exception
                item.service = null;
                return true; // null & true == indefinite: will retry later
            }
        }
    }

    public ServiceItemFilter getServiceProxyFilter() throws ConfigurationException {
        return new ProxyPreparerFilter(this.getProxyPreparer(), null);
    }

    @SuppressWarnings("unchecked")
    public Object doAsPrivileged(PrivilegedExceptionAction action, AccessControlContext acc)
            throws PrivilegedActionException {
        if (this.loginContext != null) {
            logger.finest("Doing privileged action with loginContext: " + this.loginContext + " "
                + this.loginContext.getSubject());

            return Subject.doAsPrivileged(this.loginContext.getSubject(), action, acc);
        }
        try {
            logger.finest("Doing privileged action with null loginContext.");
            return action.run();
        } catch (Exception e) {
            throw new PrivilegedActionException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Object doAsPrivileged(PrivilegedAction action, AccessControlContext acc) {
        if (this.loginContext != null) {
            logger.finest("Doing privileged action with loginContext: " + this.loginContext);
            return Subject.doAsPrivileged(this.loginContext.getSubject(), action, acc);
        }
        logger.finest("Doing privileged action with null loginContext.");
        return action.run();
    }

    public ServiceItem lookup(final ServiceTemplate tmpl, final ServiceItemFilter filter, long waitDur)
            throws InterruptedException, RemoteException, ConfigurationException {
        logger.info("Looking up: " + tmpl + " filter: " + filter + " worker: " + this.getWorkerDescWithId());
        return Worker.this.jiniManager.lookup(tmpl, addServiceProxyFilter(filter), waitDur);
    }

    /**
     * @param tmpl
     * @param maxMatches
     * @param filter
     * @return
     * @throws InterruptedException
     * @throws RemoteException
     * @throws ConfigurationException
     * @deprecated
     */
    public ServiceItem[] lookup(ServiceTemplate tmpl, int maxMatches, ServiceItemFilter filter)
            throws InterruptedException, RemoteException, ConfigurationException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Looking up (3): " + tmpl + " filter: " + filter + " worker: " + this.getWorkerDescWithId()
                + "\n ClassLoader: " + this.getClass().getClassLoader() + "\n jiniManager: " + this.jiniManager);
        }
        try {
            ServiceItem[] results = this.jiniManager.lookup(tmpl, maxMatches, addServiceProxyFilter(filter));
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Looking up resulted in: " + results.length + " matches.");
            }
            if (results.length == 0) {
                logger.info("Trying same lookup again.");
                results = this.jiniManager.lookup(tmpl, maxMatches, addServiceProxyFilter(filter));
                logger.info("Looking up resulted in: " + results.length + " matches.");
            }
            if (results.length == 0) {
                logger.info("Trying lookup with alternate call");
                ServiceItem serviceItem = this.jiniManager.lookup(tmpl, addServiceProxyFilter(filter), 1000 * 20);
                if (serviceItem != null) {
                    results = new ServiceItem[] { serviceItem };
                }
                logger.info("Looking up with alternate call resulted in: " + results.length + " matches.");
            }
            if (results.length == 0) {
                logger.info("Trying lookup with new ServiceDiscoveryManager");
                ServiceDiscoveryManager serviceDiscovery = (ServiceDiscoveryManager) config.getEntry(this.getClass()
                    .getName(), "serviceDiscovery", ServiceDiscoveryManager.class);
                ServiceItem serviceItem = serviceDiscovery.lookup(tmpl, addServiceProxyFilter(filter), 1000 * 20);
                if (serviceItem != null) {
                    results = new ServiceItem[] { serviceItem };
                }
                logger.info("New ServiceDiscoveryManager lookup up resulted in: " + results.length + " matches.");
            }

            if (results.length == 0) {
                logger.info("Worker list: " + workerList);
                Iterator<Worker> workerItr = workerList.iterator();
                while (workerItr.hasNext()) {
                    Worker worker = workerItr.next();
                    if (worker != this) {
                        logger.info("Trying lookup with other worker: " + worker);
                        results = worker.lookup(tmpl, maxMatches, filter);
                        logger.info("Other worker found results: " + results.length);
                    }
                }

                // testAccess();
            }
            return results;
        } catch (Exception e) {
            // For some deep debugging of AccessControlException objects.
            logger.log(Level.WARNING, e.toString(), e);
            testAccess();

            throw new ConfigurationException(e.toString());
        }
    }

    /**
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    private void testAccess() {
        AuthenticationPermission p = null;
        try {
            SecurityManager sm = System.getSecurityManager();
            final AccessControlContext acc = AccessController.getContext();

            logger.info("SecurityManager: " + sm + "\nAccessControlContext:" + acc + "\nThread: "
                + Thread.currentThread().getName());
            Subject clientSubject = AccessController.<Subject> doPrivileged(new PrivilegedAction<Subject>() {
                public Subject run() {
                    return Subject.getSubject(acc);
                }
            });

            logger.info("class loader: " + this.getClass().getClassLoader());
            logger.info("protection domain: " + this.getClass().getProtectionDomain());
            logger.info("permission collection: " + this.getClass().getProtectionDomain().getPermissions());
            logger.info("clientSubject: " + clientSubject);
            Principal clientPrincipal = (Principal) clientSubject.getPrincipals().iterator().next();
            logger.info("clientPrincipal: " + clientPrincipal + " class: " + clientPrincipal.getClass().getName());
            Set<Principal> reggiePrincipalSet = (Set<Principal>) this.config.getEntry(this.getClass().getName(),
                "reggiePrincipal", Set.class);
            Principal reggiePrincipal = reggiePrincipalSet.iterator().next();
            logger.info("reggiePrincipal: " + reggiePrincipal + " class: " + reggiePrincipal.getClass());
            p = new AuthenticationPermission(clientSubject.getPrincipals(), reggiePrincipalSet, "connect");
            logger.info("Permission: " + p);
            sm.checkPermission(p);
            logger.log(Level.INFO, "Test of checkPermission: " + p + " succeeded.");
        } catch (Exception e1) {
            logger.log(Level.INFO, "Test of checkPermission: " + p + " failed." + e1.toString(), e1);
        }
    }

    public ServiceItem[] lookupAllGroups(ServiceTemplate tmpl, int maxMatches, ServiceItemFilter filter)
            throws InterruptedException, RemoteException, PrivilegedActionException, ConfigurationException {
        logger.info("Looking up: " + tmpl + " filter: " + filter + " worker: " + this.getWorkerDescWithId());
        return Worker.this.jiniManager.getAllGroupsServiceDiscoveryManager().lookup(tmpl, maxMatches,
            addServiceProxyFilter(filter));
    }

    public ServiceItem lookup(ServiceTemplate tmpl, ServiceItemFilter filter) throws InterruptedException,
            RemoteException, PrivilegedActionException, ConfigurationException {
        logger.info("Looking up: " + tmpl + " filter: " + filter + " worker: " + this.getWorkerDescWithId());
        return Worker.this.jiniManager.lookup(tmpl, addServiceProxyFilter(filter));

    }

    public ServiceItem[] lookup(ServiceTemplate tmpl, int minMatches, int maxMatches, ServiceItemFilter filter,
            long waitDur) throws InterruptedException, RemoteException, ConfigurationException {
        return lookup(tmpl, minMatches, maxMatches, filter, waitDur, false);
    }

    public ServiceItem[] lookup(ServiceTemplate tmpl, int minMatches, int maxMatches, ServiceItemFilter filter,
            long waitDur, boolean lookupLocal) throws InterruptedException, RemoteException, ConfigurationException {

        logger.info("Looking up (5): " + tmpl + " filter: " + filter + " worker: " + this.getWorkerDescWithId()
            + "\n ClassLoader: " + this.getClass().getClassLoader() + "\n jiniManager: " + this.jiniManager);

        if (tmpl.attributeSetTemplates != null) {
            for (Entry e : tmpl.attributeSetTemplates) {
                if (e == null) {
                    throw new NullPointerException();
                }
            }
        }
        try {
            ServiceItem[] results = this.jiniManager.lookup(tmpl, minMatches, maxMatches,
                addServiceProxyFilter(filter), waitDur, lookupLocal);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Looking up resulted in: " + results.length + " matches.");
            }
            /*
             * if (results.length == 0) { logger.info("Trying same lookup
             * again."); results = this.jiniManager.lookup(tmpl, minMatches,
             * maxMatches, addServiceProxyFilter(filter), waitDur);
             * logger.info("Looking up again resulted in: " + results.length + "
             * matches."); } if (results.length == 0) { logger.info("Trying
             * lookup with new ServiceDiscoveryManager");
             * ServiceDiscoveryManager serviceDiscovery =
             * (ServiceDiscoveryManager) config.getEntry(
             * this.getClass().getName(), "serviceDiscovery",
             * ServiceDiscoveryManager.class); results =
             * serviceDiscovery.lookup(tmpl, minMatches, maxMatches,
             * addServiceProxyFilter(filter), waitDur); logger.info("New
             * ServiceDiscoveryManager lookup up resulted in:
             * " + results.length + "
             * matches."); }
             * 
             * 
             * if (results.length == 0) { logger.info("Worker list: " +
             * workerList); Iterator workerItr = workerList.iterator(); while
             * (workerItr.hasNext()) { Worker worker = (Worker)
             * workerItr.next(); if (worker != this) { logger.info("Trying
             * lookup with other worker: " + worker); results =
             * worker.lookup(tmpl, minMatches, maxMatches,
             * addServiceProxyFilter(filter), waitDur);; logger.info("Other
             * worker found results: " + results.length); } }
             * 
             * //testAccess(); }
             */
            return results;
        } catch (Exception e) {
            // For some deep debugging of AccessControlException objects.
            logger.log(Level.WARNING, e.toString(), e);
            testAccess();

            throw new ConfigurationException(e.toString());
        }
    }

    public ServiceItem[] lookupAllGroups(ServiceTemplate tmpl, int minMatches, int maxMatches,
            ServiceItemFilter filter, long waitDur) throws InterruptedException, RemoteException,
            ConfigurationException {

        logger.info("Looking up: " + tmpl + " filter: " + filter + " worker: " + this.getWorkerDescWithId());
        if (JiniManager.isLocalOnly()) {
            return Worker.this.jiniManager.lookup(tmpl, minMatches, maxMatches, addServiceProxyFilter(filter), waitDur);
        } else {
            return Worker.this.jiniManager.getAllGroupsServiceDiscoveryManager().lookup(tmpl, minMatches, maxMatches,
                addServiceProxyFilter(filter), waitDur);
        }
    }

    public Transaction createTransaction(final long transactionDuration) throws PrivilegedActionException {
        return (Transaction) this.doAsPrivileged(new PrivilegedExceptionAction<Object>() {
            public Object run() throws Exception {
                return Worker.this.jiniManager.createTransaction(transactionDuration);
            }
        }, null);
    }

    public LookupCache createLookupCache(final ServiceTemplate tmpl, final ServiceItemFilter filter,
            final ServiceDiscoveryListener listener) throws RemoteException, PrivilegedActionException {
        return (LookupCache) this.doAsPrivileged(new PrivilegedExceptionAction<Object>() {
            public Object run() throws Exception {
                return Worker.this.jiniManager.createLookupCache(tmpl, filter, listener);
            }
        }, null);
    }

    public void renewFor(Lease lease, long desiredDuration, long renewDuration, LeaseListener listener) {
        this.jiniManager.renewFor(lease, desiredDuration, renewDuration, listener);
    }

    public JoinManager createJoinManager(final Object object, final Entry[] entries, final ServiceID serviceID,
            final LeaseRenewalManager leaseMgr) throws PrivilegedActionException {
        return (JoinManager) this.doAsPrivileged(new PrivilegedExceptionAction<Object>() {
            public Object run() throws Exception {
                return new JoinManager(object, entries, serviceID, Worker.this.jiniManager.getLookupDiscoveryManager(),
                    leaseMgr);
            }
        }, null);

    }

    public ServiceItemFilter getServiceProxyFilter(ServiceItemFilter extraFilter) throws ConfigurationException {
        return new ProxyPreparerFilter(this.getProxyPreparer(), extraFilter);
    }

    private ServiceItemFilter addServiceProxyFilter(ServiceItemFilter extraFilter) throws ConfigurationException {
        if ((extraFilter != null) && (ProxyPreparerFilter.class.isAssignableFrom(extraFilter.getClass()))) {
            return extraFilter;
        }
        return getServiceProxyFilter(extraFilter);
    }

    public I_KeepTime getTimer() {
        return timer;
    }

    public void setTimer(I_KeepTime timer) {
        this.timer = timer;
    }

    public long getTime() throws RemoteException {
        return timer.getTime();
    }

    public I_PluginToWorker getPluginForInterface(Class<? extends I_PluginToWorker> pluginInterface) {
        if (pluginMap.containsKey(pluginInterface)) {
            return pluginMap.get(pluginInterface);
        }
        for (Class<? extends I_PluginToWorker> pii : pluginMap.keySet()) {
            if (pluginInterface.isAssignableFrom(pii)) {
                return pluginMap.get(pii);
            }
        }
        return null;
    }

    public void setPluginForInterface(Class<? extends I_PluginToWorker> pluginInterface, I_PluginToWorker plugin) {
        pluginMap.put(pluginInterface, plugin);
    }

    public List<Worker> getWorkerList(){
    	return workerList;
    }
    
}
