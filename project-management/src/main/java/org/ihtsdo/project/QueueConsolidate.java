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

import net.jini.core.entry.Entry;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.queue.SelectAll;
import org.ihtsdo.project.model.WorkListMember;

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
    public void consolidate() throws TaskFailedException, LeaseDeniedException, RemoteException, InterruptedException, IOException, PrivilegedActionException {
        try {
            ServiceID serviceID = null;
            Class<?>[] serviceTypes = new Class[]{I_QueueProcesses.class};
            Entry[] attrSetTemplates = new Entry[]{new Name(queueName)};
            ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
            ServiceItemFilter filter = null;
            ServiceItem service = worker.lookup(template, filter);
            if (service == null) {
                throw new TaskFailedException("No queue with the specified name could be found: "
                        + queueName);
            }
            queue = (I_QueueProcesses) service.service;


        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        I_GetConceptData concept = null;
        Transaction transaction = worker.getActiveTransaction();
        while (true) {
            I_EncodeBusinessProcess process = null;
            try {
                process = this.queue.take(selector, transaction);
                BusinessProcess.validateAddress(process.getOriginator(), process.getProcessID());
                WorkListMember member = (WorkListMember) process.readAttachement("A:WORKLIST_MEMBER");
                concept = (I_GetConceptData) member.getConcept();

            } catch (TaskFailedException ex) {
                System.out.println("Found missing or malformed origin for process: " + process
                        + " setting origin to queue's node inbox address");
                process.setOriginator(this.queue.getNodeInboxAddress());

            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                AceLog.getAppLog().alertAndLogException(e);
            } catch (TransactionException e) {
                // TODO Auto-generated catch block
                AceLog.getAppLog().alertAndLogException(e);
            } catch (NoMatchingEntryException e) {
                // TODO Auto-generated catch block
                AceLog.getAppLog().alertAndLogException(e);
                break;
            }

            for (I_GetConceptData con : concepts) {
                if (con.equals(concept)) {
                    try {
                        //if (jiniDelivery(process, transaction)) {
                        worker.commitTransactionIfActive();
//		                    }else {
//		                        worker.discardActiveTransaction();
//		                        System.out.println(" cannot deliver process to: " + process.getDestination());
//		                    }
                    } catch (UnknownTransactionException e) {
                        // TODO Auto-generated catch block
                        AceLog.getAppLog().alertAndLogException(e);
                    } catch (CannotCommitException e) {
                        // TODO Auto-generated catch block
                        AceLog.getAppLog().alertAndLogException(e);
                    } catch (TransactionException e) {
                        // TODO Auto-generated catch block
                        AceLog.getAppLog().alertAndLogException(e);
                    }
                }
            }
        }
    }
}
