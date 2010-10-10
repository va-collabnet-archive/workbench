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
 * Created on May 16, 2005
 */
package org.dwfa.queue.bpa.tasks.move;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.Transaction;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.QueueType;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;

/**
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/queue tasks/move-to", type = BeanType.TASK_BEAN) })
public class ToUserSelectedQueue extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private TermEntry queueType = TermEntry.getQueueType();

    private transient I_QueueProcesses q;
    
    /**
     * 
     */
    public ToUserSelectedQueue() {
        super();
    }

    /**
     * @return Returns the queueType.
     */
    public TermEntry getQueueType() {
        return queueType;
    }

    public void setQueueType(TermEntry elementId) {
        TermEntry oldValue = this.queueType;
        this.queueType = elementId;
        this.firePropertyChange("queueType", oldValue, this.queueType);
        if (this.getLogger().isLoggable(Level.FINE)) {
            this.getLogger().fine("setQueueType to: " + elementId);

        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.queueType);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.queueType = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            ServiceID serviceID = null;
            Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
            Entry[] attrSetTemplates;
            if (this.queueType == null
                || this.queueType.ids[0].equals(QueueType.Concept.QUEUE_TYPE.getUids().iterator().next())) {
                attrSetTemplates = null;
                getLogger().info("Setting queue type to null.");
            } else {
                attrSetTemplates = new Entry[] { this.queueType };
                getLogger().info("Setting queue type to: " + Arrays.asList(attrSetTemplates));
            }
            ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);

            ServiceItemFilter filter = worker.getServiceProxyFilter();
            ServiceItem[] services = worker.lookup(template, 1, 500, filter, 1000 * 15);
            ServiceItem service = (ServiceItem) worker.selectFromList(services, "Select queue",
                "Select the queue you want this process placed in.");
            if (service == null) {
                throw new TaskFailedException("User did not select a queue...");
            }
            try {
                q = (I_QueueProcesses) service.service;
            } catch (ClassCastException e) {
                ClassLoader cl1 = I_QueueProcesses.class.getClassLoader();
                ClassLoader cl2 = service.service.getClass().getClassLoader();
                worker.getLogger().severe(
                    "Class cast exception on object: " + service.service
                        + "\n\nI_QueueProcesses.class.getClassLoader():" + cl1
                        + "\n\nservice.service.getClass().getClassLoader():" + cl2 + "\n\nClassloaders equal: "
                        + cl1.equals(cl2) + "\n\nParentClassloaders equal: " + cl1.getParent().equals(cl2.getParent())

                );
                worker.getLogger().severe(
                    "\n\nClass from 1: " + cl1.loadClass(I_QueueProcesses.class.getName()) + "\n\nClass from 2: "
                        + cl2.loadClass(I_QueueProcesses.class.getName()));
                worker.getLogger().severe(
                    "\n\nClass from parent 1: " + cl1.getParent().loadClass(I_QueueProcesses.class.getName())
                        + "\n\nClass from parent 2: " + cl2.getParent().loadClass(I_QueueProcesses.class.getName()));

                throw e;
            }
        	try {
    			process.setDbDependencies(Ts.get().getLatestChangeSetDependencies());
    		} catch (IOException e) {
    			throw new TaskFailedException(e);
    		}
            return Condition.STOP;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            getLogger().info("Starting complete, getting transaction.");
            Transaction t = worker.getActiveTransaction();
            getLogger().info("Got transaction: " + t);
            q.write(process, t);
            getLogger().info("Written to queue.");
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.STOP_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
