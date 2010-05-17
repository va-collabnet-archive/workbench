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

/**
 * Sets the worker's clock to an incremental time clock that is based on
 * a master simulation clock plus an offset. The magnitude of the increment is
 * set by this task with a granularity of milliseconds.
 * Execution of the Increment Worker Simulation Clock increments the
 * workers clock by the amount defined by this task.
 * 
 * Execution of this task has no effect on the Master Simulation Clock.
 * 
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/jehri tasks/clock", type = BeanType.TASK_BEAN) })
public class SetWorkerSimulationClock extends MasterClockTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;
    private int increment = 1000 * 60; // one min

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(increment);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            increment = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    @Override
    protected void doTask(I_EncodeBusinessProcess process, I_Work worker, I_KeepIncrementalTime masterClock)
            throws RemoteException {
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
