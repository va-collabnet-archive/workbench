package org.dwfa.clock.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.clock.I_KeepIncrementalTime;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Execution of this task increments the master clock by one unit. 
 * The size of the unit is determined by the configuration of the master clock. The
 * Master clock is a network accessible service. A properly configured simulation environment
 * will only have one master clock. 
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/jehri tasks/clock", type = BeanType.TASK_BEAN)})
public class IncrementMasterClock extends MasterClockTask {
    
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

	@Override
	protected void doTask(I_EncodeBusinessProcess process, I_Work worker, I_KeepIncrementalTime clock) throws RemoteException {
		clock.increment();
	}

 }
