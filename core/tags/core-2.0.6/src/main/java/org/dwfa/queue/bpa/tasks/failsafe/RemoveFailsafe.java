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
 * Created on Jun 9, 2005
 */
package org.dwfa.queue.bpa.tasks.failsafe;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.tasks.util.Complete;
import org.dwfa.cement.QueueType;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task creates a new process to remove a failsafe from an aging queue. 
 * <p>
 * This task assumes that the business process has the following:
 * <ol>
 * <li> A data container that contains a UUID that for identifying a particular failsafe. 
 * The identifier for this datacontainer is stored in the <code>failsafeDataId</code> field. The indirection
 * provided by using a data container allows any particular proceses to have multiple failsafe components, that are
 * acted upon by different tasks within the process. 
 * <li> A <code>FailsafeData</code> attachment stored using the UUID  provided by item 1 as the attachment key. This attachment
 * specifies the location of the aging queue that hosts the failsafe, the <code>ServiceID</code> of the aging queue that
 * hosts the failsafe, and the process id of the failsafe. 
 * </ol>
 * <p>
 * The created business process has an originator equal to the process that
 * contains this task, and has a destination set equal
 * to the origin in the aging queue that hosts the failsafe. After the process is created, it is
 * placed in an outbox queue for delivery to the location of the aging queue. Once it has been delivered,
 * it will remove the failsafe process from the aging queue if it exists. If the process has already been removed, it will terminate gracefully. 
 * 
 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/queue/failsafe", type = BeanType.TASK_BEAN)})
public class RemoveFailsafe extends AbstractTask {
    public static final String REMOVE_FAIL_SAFE_KEY = "removeFailsafe";

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private Integer relativeTimeInMins = new Integer(0);

    private int failsafeDataId = -1;

    /**
     * @return Returns the failsafeDataId.
     */
    public int getFailsafeDataId() {
        return failsafeDataId;
    }

    public void setFailsafeDataId(Integer failsafeDataId) {
        setFailsafeDataId(failsafeDataId.intValue());
    }

    public void setFailsafeDataId(int failsafeDataId) {
        int oldValue = this.failsafeDataId;
        this.failsafeDataId = failsafeDataId;
        this.firePropertyChange("failsafeDataId", oldValue,
                        this.failsafeDataId);
    }

    /**
     * @return Returns the relativeTimeInMins.
     */
    public Integer getRelativeTimeInMins() {
        return relativeTimeInMins;
    }

    public void setRelativeTimeInMins(Integer relativeTime) {
        Integer oldValue = relativeTimeInMins;
        this.relativeTimeInMins = relativeTime;
        this.firePropertyChange("relativeTimeInMins", oldValue, relativeTime);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.relativeTimeInMins);
        out.writeInt(this.failsafeDataId);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.relativeTimeInMins = (Integer) in.readObject();
            this.failsafeDataId = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public RemoveFailsafe() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

        try {

            worker.getLogger().info(worker.getWorkerDesc() + " failsafeDataId: " + this.failsafeDataId);
            I_ContainData dc = process.getDataContainer(this.failsafeDataId);
            worker.getLogger().info(worker.getWorkerDesc() + " dc: " + dc);
            Uuid failsafeId = UuidFactory.create((String) dc.getData());
            worker.getLogger().info(worker.getWorkerDesc() + " Retrieved failsafeId: " + failsafeId);

            QueueEntryData fsd = (QueueEntryData) process
                    .readAttachement(failsafeId.toString());

            worker.getLogger().info(worker.getWorkerDesc() + " Retrieved fsd: " + fsd);

            BusinessProcess removeFailsafeProcess = new BusinessProcess(
                    REMOVE_FAIL_SAFE_KEY, Condition.CONTINUE, false);
            Date now = new Date();

            removeFailsafeProcess.setOriginator(process.getOriginator());
            removeFailsafeProcess.setDestination(fsd.getOrigin());
            removeFailsafeProcess.setDeadline(new Date(now.getTime()
                    + this.relativeTimeInMins.intValue() * 1000 * 60));
            removeFailsafeProcess.setPriority(Priority.NORMAL);
            removeFailsafeProcess.setSubject("Remove failsafe for: "
                    + process.getProcessID());
            DestroyFailsafe destroyTask = new DestroyFailsafe(fsd);
            removeFailsafeProcess.addTask(destroyTask);
            Complete completeTask = new Complete();
            removeFailsafeProcess.addTask(completeTask);
            removeFailsafeProcess.addBranch(destroyTask, completeTask, Condition.CONTINUE);

            ServiceID sid = null;
            Class[] serviceTypes = new Class[] { I_QueueProcesses.class };
            Entry[] attrSetTemplates = new Entry[] { new TermEntry(QueueType.Concept.OUTBOX_QUEUE.getUids()) };
            ServiceTemplate template = new ServiceTemplate(sid, serviceTypes, attrSetTemplates);
            ServiceItemFilter filter = null;
            ServiceItem service = worker.lookup(template,
                    filter);
            I_QueueProcesses q = (I_QueueProcesses) service.service;
            q.write(removeFailsafeProcess, worker.getActiveTransaction());
            
            
            
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException("Processing process: " + process.getName() + " (" + process.getProcessID() + ") " + process.getSubject(),e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // TODO Auto-generated method stub

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
        return new int[] { this.failsafeDataId };
    }

}
