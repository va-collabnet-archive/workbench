/*
 * Created on Jul 8, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
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
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/queue", type = BeanType.TASK_BEAN)})
public class ToUserSelectedQueueAllGroups extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private TermEntry queueType = TermEntry.getQueueType();
    
    private transient I_QueueProcesses q;

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
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.queueType);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
                this.queueType = (TermEntry) in.readObject();
         } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            ServiceID serviceID = null;
            Class[] serviceTypes = new Class[] { I_QueueProcesses.class };
            Entry[] attrSetTemplates;
            if (this.queueType == null) {
                attrSetTemplates = null;
            } else {
                attrSetTemplates = new Entry[] { this.queueType };
            }
            ServiceTemplate template = new ServiceTemplate(serviceID,
                    serviceTypes,
                    attrSetTemplates);
        
            ServiceItemFilter filter = null;
            ServiceItem[] services = worker.lookupAllGroups(template, 500, filter);
            ServiceItem service = (ServiceItem) worker.selectFromList(services, "Select queue", "Select the queue you want this process placed in.");
            if (service == null) {
                throw new TaskFailedException("User did not select a queue...");
            }
            q = (I_QueueProcesses) service.service;
            return Condition.STOP;
        } catch (TaskFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        } 
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            q.write(process, worker.getActiveTransaction());
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


