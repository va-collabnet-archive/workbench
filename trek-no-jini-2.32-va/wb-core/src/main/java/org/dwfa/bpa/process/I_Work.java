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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Logger;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import javax.transaction.Transaction;



/**
 * Interface for workers that execute business processes.
 * 
 * @author kec
 * 
 */
public interface I_Work extends I_ManageProperties {

     public long getTime();
     
    /**
     * The active transaction is not shared between the workflow/worker
     * environment and the terminology environment. MasterWorker transactions
     * are necessarily under the control of the workflow environment.
     * Termninology transactions are under control of the user, but workflow may
     * ensure transactional boundries on the terminology environment by
     * committing any active transactions.
     * 
     * @return A transaction
     */
    public Transaction getActiveTransaction() throws SystemException, IllegalStateException, NotSupportedException;

    public void abortActiveTransaction() throws SystemException, IllegalStateException;

    /**
     * Commits the active transaction, and makes the next transaction (if any)
     * the active transaction.
     * 
     * @throws TransactionException
     */
    public void commitActiveTransaction() throws SystemException, IllegalStateException, 
    NotSupportedException, RollbackException, HeuristicMixedException, HeuristicRollbackException;

    
    public void commitTransactionIfActive() throws SystemException, IllegalStateException, 
    NotSupportedException, RollbackException, HeuristicMixedException, HeuristicRollbackException;

    public Transaction createTransaction(long transactionDuration) throws SystemException, IllegalStateException, NotSupportedException;

    /**
     * @return Unique identifier for this worker.
     */
    public UUID getId();

    /**
     * @return Text description of this worker.
     */
    public String getWorkerDesc();

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

    public Object getObjFromFilesystem(Frame parent, String title, String startDir, FilenameFilter fileFilter)
            throws IOException, ClassNotFoundException;

    public void writeObjToFilesystem(Frame parent, String title, String startDir, String defaultFile, Object obj)
            throws IOException;

    public boolean isExecuting();

    public void flagExecutionStop();

    public boolean isExecutionStopFlagged();

    public I_Work getTransactionIndependentClone() throws IOException;

    public void writeAttachment(String key, Object value);

    public Object readAttachement(String key);

    public Object takeAttachment(String key);

    public I_PluginToWorker getPluginForInterface(Class<? extends I_PluginToWorker> pluginInterface);

    public void setPluginForInterface(Class<? extends I_PluginToWorker> pluginInterface, I_PluginToWorker plugin);
}
