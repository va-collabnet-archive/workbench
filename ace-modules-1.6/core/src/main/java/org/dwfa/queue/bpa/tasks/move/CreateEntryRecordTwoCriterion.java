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
package org.dwfa.queue.bpa.tasks.move;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.queue.bpa.tasks.failsafe.QueueEntryData;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author kec
 *         <p>
 *         Creates an entry used to store a process in a queue using two
 *         criterion to specify the queue. Useful when another process needs to
 *         know an entry record to read or delete a process in the future.
 * 
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/queue tasks/entry", type = BeanType.TASK_BEAN) })
public class CreateEntryRecordTwoCriterion extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String localPropName = "";

    private TermEntry queueTypeOne = TermEntry.getQueueType();
    private TermEntry queueTypeTwo = TermEntry.getQueueType();

    /**
     * @return Returns the queueTypeOne.
     */
    public TermEntry getQueueTypeOne() {
        return queueTypeOne;
    }

    public void setQueueTypeOne(TermEntry elementId) {
        TermEntry oldValue = this.queueTypeOne;
        this.queueTypeOne = elementId;
        this.firePropertyChange("queueTypeOne", oldValue, this.queueTypeOne);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(localPropName);
        out.writeObject(this.queueTypeOne);
        out.writeObject(this.queueTypeTwo);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            localPropName = (String) in.readObject();
            this.queueTypeOne = (TermEntry) in.readObject();
            this.queueTypeTwo = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * 
     */
    public CreateEntryRecordTwoCriterion() {
        super();
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        return Condition.CONTINUE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            ServiceID serviceID = null;
            Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
            Entry[] attrSetTemplates = new Entry[] { this.queueTypeOne, this.queueTypeTwo };
            ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
            ServiceItemFilter filter = null;
            ServiceItem service = worker.lookup(template, filter);
            ServiceID queueServiceID = service.serviceID;
            I_QueueProcesses q = (I_QueueProcesses) service.service;

            String origin = q.getNodeInboxAddress();
            EntryID entryID = new EntryID(UUID.randomUUID());
            QueueEntryData qed = new QueueEntryData(origin, queueServiceID, null, entryID);

            PropertyDescriptor[] localDescriptors = process.getAllPropertiesBeanInfo().getPropertyDescriptors();
            PropertyDescriptorWithTarget localDescriptor = null;
            for (PropertyDescriptor d : localDescriptors) {
                if (PropertyDescriptorWithTarget.class.isAssignableFrom(d.getClass())) {
                    PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                    if (dwt.getLabel().equals(this.localPropName)) {
                        localDescriptor = dwt;
                        break;
                    }
                }
            }
            localDescriptor.getWriteMethod().invoke(localDescriptor.getTarget(), qed);
            worker.getLogger().info(
                worker.getWorkerDesc() + " Created QueueEntryRecord for: " + process.getName() + " ("
                    + process.getProcessID() + ")");
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * @return Returns the localPropName.
     */
    public String getLocalPropName() {
        return localPropName;
    }

    /**
     * @param localPropName The localPropName to set.
     */
    public void setLocalPropName(String localPropName) {
        this.localPropName = localPropName;
    }

    public TermEntry getQueueTypeTwo() {
        return queueTypeTwo;
    }

    public void setQueueTypeTwo(TermEntry queueTypeTwo) {
        TermEntry oldValue = this.queueTypeTwo;
        this.queueTypeTwo = queueTypeTwo;
        this.firePropertyChange("queueTypeTwo", oldValue, this.queueTypeTwo);
    }
}
