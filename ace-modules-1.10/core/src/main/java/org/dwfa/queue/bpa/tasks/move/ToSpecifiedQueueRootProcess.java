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
 * Created on Jun 13, 2005
 */
package org.dwfa.queue.bpa.tasks.move;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

/**
 * The <code>ToSpecifiedQueueRootProcess</code> task will go to a specific queue
 * based on the
 * serviceID of the queue. Since the serviceID's are not typically avaible at
 * authoring time, this task is reserved for programatic actions that occur at
 * runtime such as the actions of the create failsafe and remove failsafe tasks.
 * 
 * @author kec
 * 
 */
public class ToSpecifiedQueueRootProcess extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private ServiceID queueServiceId = null;

    /**
     * @return Returns the queueServiceId.
     */
    public ServiceID getQueueServiceId() {
        return queueServiceId;
    }

    public void setQueueServiceId(ServiceID elementId) {
        ServiceID oldValue = this.queueServiceId;
        this.queueServiceId = elementId;
        this.firePropertyChange("queueServiceId", oldValue, this.queueServiceId);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.queueServiceId);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.queueServiceId = (ServiceID) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public ToSpecifiedQueueRootProcess(ServiceID queueServiceId) {
        super();
        this.queueServiceId = queueServiceId;
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        return Condition.STOP;
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_EncodeBusinessProcess rootProcess = worker.getProcessStack().get(0);
            Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
            Entry[] attrSetTemplates = null;
            ServiceTemplate template = new ServiceTemplate(this.queueServiceId, serviceTypes, attrSetTemplates);
            ServiceItemFilter filter = null;
            ServiceItem service = worker.lookup(template, filter);
            I_QueueProcesses q = (I_QueueProcesses) service.service;
            q.write(rootProcess, worker.getActiveTransaction());
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.STOP_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
