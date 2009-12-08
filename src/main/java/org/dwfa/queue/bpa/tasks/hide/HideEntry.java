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
package org.dwfa.queue.bpa.tasks.hide;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.queue.bpa.tasks.failsafe.QueueEntryData;

/**
 * Hide an entry in a queue for the duration of a transaction. Useful for
 * allowing a process on one queue to prevent concurrent access to a process on
 * another queue. If the an entry with the specified id is missing from the
 * specified queue, the result is logged, but the task continues without
 * throwing a task failed exception.
 * <p>
 * This task reads a QueueEntryData object from the enclosing process, but does
 * not modify this object.
 * 
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/queue tasks/entry", type = BeanType.TASK_BEAN) })
public class HideEntry extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String queueEntryPropName = "";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(queueEntryPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.queueEntryPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public HideEntry() {
        super();
    }

    @SuppressWarnings("unchecked")
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            Class<I_QueueProcesses>[] serviceTypes = new Class[] { I_QueueProcesses.class };
            Entry[] attrSetTemplates = new Entry[] {};
            QueueEntryData qed = null;
            for (PropertyDescriptor d : process.getAllPropertiesBeanInfo().getPropertyDescriptors()) {
                PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                if (dwt.getLabel().equals(this.queueEntryPropName)) {
                    qed = (QueueEntryData) dwt.getReadMethod().invoke(dwt.getTarget(), new Object[] {});
                    break;
                }

            }

            ServiceTemplate template = new ServiceTemplate(qed.getQueueID(), serviceTypes, attrSetTemplates);
            ServiceItemFilter filter = null;
            ServiceItem service = worker.lookup(template, filter);
            I_QueueProcesses q = (I_QueueProcesses) service.service;
            try {
                q.hide(qed.getEntryID(), worker.getActiveTransaction());
            } catch (NoMatchingEntryException e) {
                worker.getLogger().info(e.toString());
            }
            return Condition.CONTINUE;
        } catch (Exception ex) {
            throw new TaskFailedException(ex);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

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
     * @return Returns the queueEntryPropName.
     */
    public String getQueueEntryPropName() {
        return queueEntryPropName;
    }

    /**
     * @param queueEntryPropName The queueEntryPropName to set.
     */
    public void setQueueEntryPropName(String queueEntryPropName) {
        this.queueEntryPropName = queueEntryPropName;
    }

}
