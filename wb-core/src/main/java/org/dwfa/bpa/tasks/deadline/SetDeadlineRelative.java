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
 * Created on Apr 22, 2005
 */
package org.dwfa.bpa.tasks.deadline;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/set tasks", type = BeanType.TASK_BEAN) })
public class SetDeadlineRelative extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private int relativeTimeInMins = 0;

    /**
     * 
     */
    public SetDeadlineRelative() {
        super();
    }

    /**
     * @return Returns the relativeTimeInMins.
     */
    public Integer getRelativeTimeInMins() {
        return new Integer(relativeTimeInMins);
    }

    public void setRelativeTimeInMins(Integer relativeTime) {
        Integer oldValue = new Integer(relativeTimeInMins);
        this.relativeTimeInMins = relativeTime.intValue();
        this.firePropertyChange("relativeTimeInMins", oldValue, relativeTime);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(this.relativeTimeInMins);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            this.relativeTimeInMins = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        Date now;
        try {
            now = new Date(worker.getTime());
        } catch (RemoteException e) {
            throw new TaskFailedException(e);
        }
        long lengthToExtend = relativeTimeInMins;
        lengthToExtend = lengthToExtend * 1000 * 60;
        Date deadline = new Date(now.getTime() + lengthToExtend);
        if (worker.getLogger().isLoggable(Level.FINER)) {
            worker.getLogger().finer(
                this.getName() + " Now: " + Worker.dateFormat.format(now) + " relative time in min: "
                    + this.relativeTimeInMins + " new deadline: " + Worker.dateFormat.format(deadline));
        }
        process.setDeadline(deadline);
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

}
