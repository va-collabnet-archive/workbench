/*
 * Created on Jun 9, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.failsafe;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.ProcessID;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.tasks.deadline.SetDeadlineRelative;
import org.dwfa.cement.QueueType;
import org.dwfa.jini.TermEntry;
import org.dwfa.queue.bpa.tasks.move.ToSpecifiedQueue;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The Create Failsafe task locates a <code>I_QueueProcesses</code> service that 
 * also has a <code>QueueTypeAging</code> attribute. After locating this queue,
 * it creates a new "failsafe" <code>BusinessProcess</code> that contains a copy of 
 * a business process as an attachment, and also contains a sequence of task that 
 * will cause the failsafe process to periodically launch a copy of the 
 * attached business process at the interval specified by this task. 
 * <p>
 * The failsafe data is stored associated with the process rather than with the task
 * since other tasks need to access the failsafe data in a task independent manner. 
 * <p>
 * Note that this task creates a failsafe of the process it is part of. If this processes
 * is embedded as a task inside another process, it may be desirable to create a failsafe of the 
 * outer process. Creating a failsafe for the parent or for the root process may generate the 
 * desired behaviour. 
 * <p>
 * @see DestroyFailsafe
 * @see CreateFailsafeParentProcess
 * @see CreateFailsafeRootProcess
 * <p>
 * @author kec
  * 
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/queue tasks/failsafe", type = BeanType.TASK_BEAN)})
public class CreateFailsafe extends AbstractTask {

    public static final String FAIL_SAFE_KEY = "failsafe";

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private Integer relativeTimeInMins = new Integer(2);

    protected int failsafeDataId = -1;

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
        this.firePropertyChange("failsafeDataId", oldValue, this.failsafeDataId);
    }
    
    /**
     * @return Returns the relativeTimeInMins.
     */
    public Integer getRelativeTimeInMins() {
        return this.relativeTimeInMins;
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

    /**
     * 
     */
    public CreateFailsafe() {
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
            I_ContainData failsafeIdContainer = process.getDataContainer(this.failsafeDataId);
            createFailsafe(process, process, failsafeIdContainer, worker);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

    }

    /**
	 * @param process
	 * @param worker
	 * @throws IOException
	 * @throws PropertyVetoException
	 * @throws RemoteException
	 * @throws TransactionException
	 * @throws LeaseDeniedException
	 * @throws InterruptedException
     * @throws PrivilegedActionException 
     * @throws ConfigurationException 
	 */
	@SuppressWarnings("unchecked")
   protected void createFailsafe(I_EncodeBusinessProcess process, I_EncodeBusinessProcess removingProcess, I_ContainData failsafeIdContainer,
            I_Work worker) throws IOException, PropertyVetoException, RemoteException, TransactionException, LeaseDeniedException, InterruptedException, PrivilegedActionException, ConfigurationException {
		ServiceID serviceID = null;
		Class[] serviceTypes = new Class[] { I_QueueProcesses.class };
		Entry[] attrSetTemplates = new Entry[] { new TermEntry(QueueType.Concept.AGING_QUEUE.getUids()) };
		ServiceTemplate template = new ServiceTemplate(serviceID,
		   serviceTypes,
		   attrSetTemplates);
		ServiceItemFilter filter = null;
		ServiceItem service = worker.lookup(template, filter);
		if (service == null) {
			throw new ConfigurationException("Unable to fine an aging queue");
		}
		ServiceID agingQueueServiceID = service.serviceID;
		I_QueueProcesses q = (I_QueueProcesses) service.service;
         
		BusinessProcess failsafeProcess = new BusinessProcess(
		        FAIL_SAFE_KEY, Condition.CONTINUE, false);
		ProcessID failsafeId = failsafeProcess.getProcessID();
		failsafeIdContainer.setData(failsafeId.toString());
		Date now = new Date();

		failsafeProcess.setOriginator(process.getOriginator());
		failsafeProcess.setDeadline(new Date(now.getTime()
		        + this.relativeTimeInMins.intValue() * 1000 * 60));
		failsafeProcess.setPriority(Priority.NORMAL);
		failsafeProcess.setSubject("Failsafe for: "
		        + process.getProcessID());
		
		
		LaunchFailsafe launchTask = new LaunchFailsafe();
		launchTask.setFailsafeKey(FAIL_SAFE_KEY);
		failsafeProcess.addTask(launchTask);
		
		
		SetDeadlineRelative setDeadlineTask = new SetDeadlineRelative();
		setDeadlineTask.setRelativeTimeInMins(this.relativeTimeInMins);
		failsafeProcess.addTask(setDeadlineTask);
		failsafeProcess.addBranch(launchTask, setDeadlineTask, Condition.CONTINUE);
		
		// Go back to the queue...
		ToSpecifiedQueue goBack = new ToSpecifiedQueue(agingQueueServiceID);
		failsafeProcess.addTask(goBack);
		failsafeProcess.addBranch(setDeadlineTask, goBack, Condition.CONTINUE);
		
		// Do it all over again...
		failsafeProcess.addBranch(goBack, launchTask, Condition.STOP);

		String origin = q.getNodeInboxAddress(); 
		EntryID entryID = new EntryID(UUID.randomUUID());
		QueueEntryData fsd = new QueueEntryData(origin, agingQueueServiceID, failsafeId, entryID);
		removingProcess.writeAttachment(failsafeId.toString(), fsd);
		failsafeProcess.writeAttachment(failsafeId.toString(), fsd);

		
		//Must marshall the process so that reference by pointer does not cause
		//process to change each time it is re-launched...
		
		MarshalledObject marshalledProcess = new MarshalledObject(process);
		failsafeProcess.writeAttachment(FAIL_SAFE_KEY, marshalledProcess);

		q.write(failsafeProcess, entryID,  worker.getActiveTransaction());
		worker.getLogger().info(worker.getWorkerDesc() + " Created failsafeId: " + failsafeId +
		        " for process: " + process.getName() + " (" + process.getProcessID() + ")");
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
