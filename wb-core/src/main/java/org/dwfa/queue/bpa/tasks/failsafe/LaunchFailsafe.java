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
import java.rmi.MarshalledObject;
import java.util.Collection;
import java.util.Stack;
import java.util.logging.Level;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task will unmarshal and continue execution of a process that has been
 * attached to the
 * process that contains this task. The marshaled process must be attached with
 * the key specified in the <code>failsafeKey</code> field.
 * <p>
 * This task is not typically used in manually constructed processes, but is
 * part of the process created by the <code>CreateFailsafe</code> task.
 * <p>
 * Process execution is acomplished by temporarily substituting a new process
 * stack for the worker executing this task,
 * 
 * @author kec
 * @see CreateFailsafe
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/queue tasks/failsafe", type = BeanType.TASK_BEAN) })
public class LaunchFailsafe extends AbstractTask {
    /**
     *  
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String failsafeKey = null;

    /**
     * @return Returns the failsafeKey.
     */
    public String getFailsafeKey() {
        return failsafeKey;
    }

    public void setFailsafeKey(String failsafeKey) {
        String oldValue = this.failsafeKey;
        this.failsafeKey = failsafeKey;
        this.firePropertyChange("failsafeKey", oldValue, this.failsafeKey);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.failsafeKey);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.failsafeKey = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public LaunchFailsafe() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            MarshalledObject marshalledFailsafe = (MarshalledObject) process.readAttachement(this.failsafeKey);
            I_EncodeBusinessProcess failsafe;
            failsafe = (I_EncodeBusinessProcess) marshalledFailsafe.get();
            if (worker.getLogger().isLoggable(Level.INFO)) {
                worker.getLogger().info("Opened attached failsafe: " + failsafe);
            }
            Stack<I_EncodeBusinessProcess> processStack = worker.getProcessStack();
            worker.setProcessStack(new Stack<I_EncodeBusinessProcess>());
            failsafe.evaluate(failsafe, worker);
            failsafe.complete(failsafe, worker);
            worker.setProcessStack(processStack);
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

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

}
