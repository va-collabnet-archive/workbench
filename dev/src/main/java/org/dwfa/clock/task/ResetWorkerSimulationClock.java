package org.dwfa.clock.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.Collection;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.clock.I_KeepIncrementalTime;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Execution of this task resets the worker's clock to the same value as the master simulation clock.
 * 
 *  This task would typically be executed when a worker has completed executing a simulation component to 
 *  reset the clock to it's baseline state before picking up a new simulation component and allowing it's execution. 

* Execution of this task has no effect on the Master Simulation Clock. 
 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/clock tasks", type = BeanType.TASK_BEAN)})
public class ResetWorkerSimulationClock extends AbstractTask {
    
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public final Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
    	try {
        	I_KeepIncrementalTime workerClock = (I_KeepIncrementalTime) worker.getTimer();
			workerClock.reset();
	        return Condition.CONTINUE;
		} catch (RemoteException e) {
			throw new TaskFailedException(e);
		}
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public final void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do. 

    }
    
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public final Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public final int[] getDataContainerIds() {
        return new int[] {};
    }
}
