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
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.queue.SelectAll;

/**
 * The Class QueueConsolidate.
 */
public class QueueConsolidate {

    /**
     * The queue.
     */
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
     * The concepts.
     */
    private Set<I_GetConceptData> concepts;
    /**
     * The queue name.
     */
    private String queueName;

    /**
     * Instantiates a new queue consolidate.
     *
     * @param worker the worker
     * @param queueName the queue name
     * @param selector the selector
     * @param concepts the concepts
     */
    public QueueConsolidate(I_Work worker, String queueName, I_SelectProcesses selector, Set<I_GetConceptData> concepts) {
        this.queueName = queueName;
        if (selector == null) {
            this.selector = new SelectAll();
        } else {
            this.selector = selector;
        }
        this.worker = worker;

        this.concepts = concepts;
    }

    /**
     * Consolidate.
     *
     * @throws TaskFailedException the task failed exception
     * @throws LeaseDeniedException the lease denied exception
     * @throws RemoteException the remote exception
     * @throws InterruptedException the interrupted exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PrivilegedActionException the privileged action exception
     */
    public void consolidate() throws TaskFailedException, RemoteException, InterruptedException, IOException, PrivilegedActionException {
        throw new UnsupportedOperationException("TODO: Removal of Jini");
    }
}
