package org.dwfa.clock.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.clock.I_KeepIncrementalTime;
import org.dwfa.clock.IncrementalTime;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = 
{ @Spec(directory = "tasks/clock", type = BeanType.TASK_BEAN)})
public class SetWorkerSimulationClock extends MasterClockTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;
	private int increment = 1000 * 60; // one min

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(increment);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        	increment = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

	@Override
	protected void doTask(I_EncodeBusinessProcess process, I_Work worker, I_KeepIncrementalTime masterClock) throws RemoteException {
		IncrementalTime simClock = new IncrementalTime(increment, masterClock);
		worker.setTimer(simClock);
	}

	public int getIncrement() {
		return increment;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}

}
