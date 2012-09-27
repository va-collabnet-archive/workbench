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
package org.dwfa.bpa.worker.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
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
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.queue.SelectAllWithSatisfiedDbConstraints;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/worker plugins", type = BeanType.TASK_BEAN) })
public class AddGetWorkFromQueuePlugin extends AbstractTask {
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private String workerPropName = ProcessAttachmentKeysForWorkerTasks.WORKER.getAttachmentKey();
    private String queueAddress = "pcpInbox.@informatics.com";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(workerPropName);
        out.writeObject(queueAddress);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.workerPropName = (String) in.readObject();
            this.queueAddress = (String) in.readObject();
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

            attrSetTemplates = new Entry[] { new ElectronicAddress(queueAddress) };
            getLogger().info("Setting queue attributes to to: " + Arrays.asList(attrSetTemplates));

            ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);

            ServiceItemFilter filter = worker.getServiceProxyFilter();
            ServiceItem[] services = worker.lookup(template, 1, 500, filter, 1000 * 15);

            I_Work workerToModify = (I_Work) process.readProperty(ProcessAttachmentKeysForWorkerTasks.WORKER.getAttachmentKey());

            GetWorkFromQueuePlugin getWorkPlugin = new GetWorkFromQueuePlugin(workerToModify,
                (I_QueueProcesses) services[0].service, new SelectAllWithSatisfiedDbConstraints());
            workerToModify.setPluginForInterface(I_GetWorkFromQueue.class, getWorkPlugin);
            getWorkPlugin.start((I_QueueProcesses) services[0].service);

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do...

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

    public String getWorkerPropName() {
        return workerPropName;
    }

    public void setWorkerPropName(String workerPropName) {
        this.workerPropName = workerPropName;
    }

    public String getQueueAddress() {
        return queueAddress;
    }

    public void setQueueAddress(String queueName) {
        Object old = this.queueAddress;
        this.queueAddress = queueName;
        this.firePropertyChange("queueName", old, queueName);
    }

}
