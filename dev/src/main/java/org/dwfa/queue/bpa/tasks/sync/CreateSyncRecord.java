/*
 * Created on Mar 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.sync;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
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
import org.dwfa.bpa.worker.EditorGlueForWorker;
import org.dwfa.cement.QueueType;
import org.dwfa.jini.TermEntry;
import org.dwfa.queue.bpa.tasks.failsafe.QueueEntryData;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = 
{ @Spec(directory = "tasks/queue", type = BeanType.TASK_BEAN)})
public class CreateSyncRecord extends AbstractTask {

    /**
     * @author kec<p>
     * Creates an entry 
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
    public CreateSyncRecord() {
        super();
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        return Condition.CONTINUE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

        try {
            ServiceID serviceID = null;
            Class[] serviceTypes = new Class[] { I_QueueProcesses.class };
            Entry[] attrSetTemplates = new Entry[] { new TermEntry(QueueType.Concept.SYNCHRONIZATION_QUEUE.getUids()) };
            ServiceTemplate template = new ServiceTemplate(serviceID,
                    serviceTypes, attrSetTemplates);
            ServiceItemFilter filter = null;
            ServiceItem service = worker.lookup(template, filter);
            ServiceID syncQueueServiceID = service.serviceID;
            I_QueueProcesses q = (I_QueueProcesses) service.service;

            String origin = q.getNodeInboxAddress();
            EntryID entryID = new EntryID(UUID.randomUUID());
            QueueEntryData qed = new QueueEntryData(origin, syncQueueServiceID,
                    process.getProcessID(), entryID);

            PropertyDescriptor[] localDescriptors = process
                    .getAllPropertiesBeanInfo().getPropertyDescriptors();
            PropertyDescriptorWithTarget localDescriptor = null;
            for (PropertyDescriptor d : localDescriptors) {
                if (PropertyDescriptorWithTarget.class.isAssignableFrom(d
                        .getClass())) {
                    PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                    if (dwt.getLabel().equals(this.localPropName)) {
                        localDescriptor = dwt;
                        break;
                    }
                }
            }
            PropertyEditor editor = localDescriptor
                    .createPropertyEditor(localDescriptor.getTarget());
            editor.addPropertyChangeListener(new EditorGlueForWorker(editor,
                    localDescriptor.getWriteMethod(), localDescriptor
                            .getTarget(), worker));
            editor.setValue(qed);

            worker.getLogger().info(
                    worker.getWorkerDesc()
                            + " Created QueueEntryData for syncronization: "
                            + process.getName() + " (" + process.getProcessID()
                            + ")");
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

}