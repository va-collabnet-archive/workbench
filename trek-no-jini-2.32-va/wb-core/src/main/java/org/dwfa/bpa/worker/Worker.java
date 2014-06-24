/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * Created on Apr 20, 2005
 */
package org.dwfa.bpa.worker;

//~--- non-JDK imports --------------------------------------------------------
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.config.imp.AbstractUserTransactionServiceFactory;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_PluginToWorker;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.PropertyChangeSupportWithPropagationId;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;

import javax.transaction.Transaction;

/**
 * @author kec
 *
 */
public abstract class Worker implements I_Work {

    public static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    protected static List<Worker> workerList = new ArrayList<>();
    protected static final Logger logger =
            Logger.getLogger(I_Work.class.getName());
    private static final UserTransactionManager tm;

    static {
        System.setProperty(UserTransactionServiceImp.NO_FILE_PROPERTY_NAME, "true");
        System.setProperty("com.atomikos.icatch.service",
                "com.atomikos.icatch.standalone.UserTransactionServiceFactory");
        System.setProperty(AbstractUserTransactionServiceFactory.MAX_TIMEOUT_PROPERTY_NAME, "60000000000");
        tm = new UserTransactionManager();
        try {
            tm.setTransactionTimeout(400000);
            tm.init();
        } catch (SystemException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    private boolean stopExecutionFlagged = false;
    private PropertyChangeSupport propChangeSupport =
            new PropertyChangeSupportWithPropagationId(this);
    private Stack<I_EncodeBusinessProcess> processStack = new Stack<>();
    protected Map<Class<? extends I_PluginToWorker>, I_PluginToWorker> pluginMap = new HashMap<>();
    private HashMap<String, Object> attachments = new HashMap<>();
    private boolean executing = false;
    protected UUID id;
    protected String desc;

    /**
     * @param config
     * @param id
     * @param desc
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public Worker(UUID id, String desc)
            throws IOException {
        super();
        this.id = id;
        this.desc = desc;
        workerList.add(this);
        logger.log(Level.INFO, "Worker {0}({1}) logged in. {2}", new Object[]{this.desc, this.id,
                    this.getClass().getName()});
    }

    @Override
    public void flagExecutionStop() {
        stopExecutionFlagged = true;
    }

    @Override
    public boolean isExecutionStopFlagged() {
        return stopExecutionFlagged;
    }

    @Override
    public Object readAttachement(String key) {
        return attachments.get(key);
    }

    @Override
    public Object takeAttachment(String key) {
        return attachments.remove(key);
    }

    @Override
    public void writeAttachment(String key, Object value) {
        attachments.put(key, value);
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getLogger()
     */
    @Override
    public Logger getLogger() {
        return logger;
    }

    protected void executeStartupProcesses() {
        File startupDirectory = (File) new File("processes/startup/launcher");
        File[] startupFiles = startupDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".bp");
            }
        });

        if (startupFiles != null) {
            for (int i = 0; i < startupFiles.length; i++) {
                try {
                    logger.log(Level.INFO, "Executing business process: {0}", startupFiles[i]);

                    FileInputStream fis = new FileInputStream(startupFiles[i]);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();

                    this.execute(process);
                    this.commitTransactionIfActive();
                    logger.log(Level.INFO, "Finished business process: {0}", startupFiles[i]);
                } catch (IOException | ClassNotFoundException | TaskFailedException e1) {
                    logger.log(Level.SEVERE, e1.getMessage() + " thrown by " + startupFiles[i], e1);
                }
            }
        }
    }

    @Override
    public String toString() {
        return this.desc + " (" + this.id + ")";
    }

    @Override
    public synchronized Condition execute(I_EncodeBusinessProcess process) throws TaskFailedException {
        stopExecutionFlagged = false;
        setExecuting(true);

        Condition condition;

        try {

            condition = executeProcess(process);

            setExecuting(false);

            return condition;
        } catch (TaskFailedException ex) {
            try {
                setExecuting(false);
                if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    tm.rollback();
                }

                throw ex;
            } catch (SystemException ex1) {
                throw new TaskFailedException(ex1);
            }
        }
    }

    private void setExecuting(boolean executing) {
        boolean oldState = this.executing;

        this.executing = executing;
        this.propChangeSupport.firePropertyChange("executing", oldState, this.executing);
    }

    @Override
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
            logger.log(Level.INFO, "{0}; process: {1} ({2}" + ") " + " end execute: {3}",
                    new Object[]{this.getWorkerDesc(),
                        process.getName(), process.getProcessID(), condition});
        }

        try {
            this.commitTransactionIfActive();

            return condition;
        } catch (Throwable e) {
            throw new TaskFailedException(e);
        }
    }

    /**
     * @throws RemoteException
     * @throws CannotCommitException
     * @throws UnknownTransactionException
     *
     */
    @Override
    public void commitTransactionIfActive() {
        try {
            if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                tm.commit();
            }
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void abortActiveTransaction() throws SystemException, IllegalStateException {
        try {
            if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                tm.rollback();
            }
        } catch (SecurityException | IllegalStateException | SystemException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Transaction getActiveTransaction() throws SystemException, IllegalStateException, NotSupportedException {
        if (tm.getStatus() == Status.STATUS_NO_TRANSACTION) {
            tm.begin();
        }
        return tm.getTransaction();
    }

    public Transaction getTransactionIfActive() throws SystemException, IllegalStateException, NotSupportedException {
        if (tm.getStatus() == Status.STATUS_NO_TRANSACTION) {
            return null;
        }
        return tm.getTransaction();
    }

    @Override
    public void commitActiveTransaction() throws SystemException, IllegalStateException,
            NotSupportedException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        if (tm.getStatus() == Status.STATUS_NO_TRANSACTION) {
            return;
        }

        tm.commit();
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getId()
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * @see org.dwfa.bpa.process.I_Work#getWorkerDesc()
     */
    @Override
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
     * @see
     * org.dwfa.bpa.process.I_ManageProperties#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @see
     * org.dwfa.bpa.process.I_ManageProperties#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#getPropertyChangeListeners()
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return this.propChangeSupport.getPropertyChangeListeners();
    }

    /**
     * @see
     * org.dwfa.bpa.process.I_ManageProperties#addPropertyChangeListener(java.lang.String,
     * java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @see
     * org.dwfa.bpa.process.I_ManageProperties#removePropertyChangeListener(java.lang.String,
     * java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * @see
     * org.dwfa.bpa.process.I_ManageProperties#getPropertyChangeListeners(java.lang.String)
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return this.propChangeSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * @see
     * org.dwfa.bpa.process.I_ManageProperties#hasListeners(java.lang.String)
     */
    public boolean hasListeners(String propertyName) {
        return this.propChangeSupport.hasListeners(propertyName);
    }

    /**
     * @return Returns the processStack.
     */
    @Override
    public Stack<I_EncodeBusinessProcess> getProcessStack() {
        return processStack;
    }

    /**
     * @param processStack The processStack to set.
     */
    @Override
    public void setProcessStack(Stack<I_EncodeBusinessProcess> processStack) {
        this.processStack = processStack;
    }

    @Override
    public Transaction createTransaction(final long transactionDuration) throws NotSupportedException, SystemException {
        tm.begin();
        return tm.getTransaction();
    }

    @Override
    public long getTime() {
        return System.currentTimeMillis();
    }

    @Override
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

    @Override
    public void setPluginForInterface(Class<? extends I_PluginToWorker> pluginInterface, I_PluginToWorker plugin) {
        pluginMap.put(pluginInterface, plugin);
    }

    public List<Worker> getWorkerList() {
        return workerList;
    }

    public static class WorkerLevel extends Level {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private static WorkerLevel infoPlus;

        protected WorkerLevel(String name, int value) {
            super(name, value);
        }

        public static WorkerLevel getInfoPlusLevel() {
            if (infoPlus == null) {
                infoPlus = new WorkerLevel("INFO_PLUS", 801);
            }

            return infoPlus;
        }
    }
}
