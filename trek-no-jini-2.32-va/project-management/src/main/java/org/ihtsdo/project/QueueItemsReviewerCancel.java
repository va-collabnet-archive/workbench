/*
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
package org.ihtsdo.project;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;

import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeQueueEntry;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.queue.ObjectServerCore;
import org.dwfa.queue.SelectAll;
import org.dwfa.swing.SwingWorker;

/**
 * The Class QueueItemsReviewerCancel.
 */
public class QueueItemsReviewerCancel {

    /**
     * The CUSTO m_ nod e_ key.
     */
    private String CUSTOM_NODE_KEY = "CUSTOM_NODE_KEY";
    ;
	
	/** The queue. */
	private I_QueueProcesses queue;
    /**
     * The selector.
     */
    private I_SelectProcesses selector;
    /**
     * The worker.
     */
    private I_Work worker;
    /**
     * The contract.
     */
    private HashSet<UUID> contract;
    /**
     * The queue name.
     */
    private String queueName;
    /**
     * The outbox worker.
     */
    private I_Work outboxWorker;
    /**
     * The outbox queue.
     */
    private I_QueueProcesses outboxQueue;
    /**
     * The contract uuid.
     */
    private UUID contractUuid;

    /**
     * Instantiates a new queue items reviewer cancel.
     *
     * @param worker the worker
     * @param queueName the queue name
     * @param selector the selector
     * @param contract the contract
     * @param contractUuid the contract uuid
     */
    public QueueItemsReviewerCancel(I_Work worker, String queueName, I_SelectProcesses selector, HashSet<UUID> contract, UUID contractUuid) {
        this.queueName = queueName;
        if (selector == null) {
            this.selector = new SelectAll();
        } else {
            this.selector = selector;
        }
        this.worker = worker;

        this.contract = contract;
        this.contractUuid = contractUuid;
    }

    /**
     * Process.
     *
     * @throws TaskFailedException the task failed exception
     * @throws LeaseDeniedException the lease denied exception
     * @throws RemoteException the remote exception
     * @throws InterruptedException the interrupted exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PrivilegedActionException the privileged action exception
     */
    public void process() throws TaskFailedException, RemoteException, InterruptedException, IOException, PrivilegedActionException {
        throw new UnsupportedOperationException("TODO: Removal of JINI");
    }

    /**
     * The Class RefreshServer.
     */
    public class RefreshServer extends SwingWorker<Boolean> {

        /* (non-Javadoc)
         * @see org.dwfa.swing.SwingWorker#construct()
         */
        @Override
        protected Boolean construct() throws Exception {
            ObjectServerCore.refreshServers();
            return true;
        }

        /* (non-Javadoc)
         * @see org.dwfa.swing.SwingWorker#finished()
         */
        @Override
        protected void finished() {
            try {
                get();
            } catch (InterruptedException e1) {

                e1.printStackTrace();
            } catch (ExecutionException e1) {

                e1.printStackTrace();
            }
        }
    }
}
