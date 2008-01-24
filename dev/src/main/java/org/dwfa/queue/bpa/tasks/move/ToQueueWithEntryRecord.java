/*
 * Created on Mar 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.move;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceID;
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
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.queue.bpa.tasks.failsafe.QueueEntryData;
/**
 * @author kec<p>
 * Moves a process from one queue to another with the processes entry record. The queue type to move to is 
 * specified in the task.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/queue", type = BeanType.TASK_BEAN)})
public class ToQueueWithEntryRecord extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String localPropName = "";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(localPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            localPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * 
     */
    public ToQueueWithEntryRecord() {
        super();
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        return Condition.STOP;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

        try {
            QueueEntryData qed = null;
            for (PropertyDescriptor d : process
                    .getAllPropertiesBeanInfo().getPropertyDescriptors()) {
                    PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                    if (dwt.getLabel().equals(this.localPropName)) {
                        qed = (QueueEntryData)  dwt.getReadMethod().invoke(dwt.getTarget(), new Object[] {});
                        break;
                    }
                
            }
             

            ServiceID serviceID = qed.getQueueID();
            Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
            Entry[] attrSetTemplates = new Entry[] { };
            ServiceTemplate template = new ServiceTemplate(serviceID,
                    serviceTypes, attrSetTemplates);
            ServiceItemFilter filter = null;
            ServiceItem service = worker.lookup(template, filter);
            I_QueueProcesses q = (I_QueueProcesses) service.service;
            q.write(process, qed.getEntryID(), worker.getActiveTransaction());

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

    /**
     * @return Returns the localPropName.
     */
    public String getLocalPropName() {
        return localPropName;
    }

    /**
     * @param localPropName
     *            The localPropName to set.
     */
    public void setLocalPropName(String localPropName) {
        this.localPropName = localPropName;
    }

}