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
 * Created on Mar 8, 2006
 */
package org.dwfa.bpa.tasks.process;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.rmi.MarshalledObject;
import java.util.Collection;
import java.util.Stack;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.data.ProcessContainer;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

/**
 * Ensures that the process launched is an independent object from the process
 * in the container by Marshalling then unmarshalling the object. Puts a process in the container and launches it. 
 * Useful if attachments need to be modified before launching the process.
 * 
 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/processes/start tasks", type = BeanType.TASK_BEAN)})
public class LaunchProcessFromContainer extends AbstractTask {

    private int processDataId = -1;
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(processDataId);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
             processDataId = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    public LaunchProcessFromContainer() throws MalformedURLException {
        super();
    }

    @SuppressWarnings("unchecked")
   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            

            ProcessContainer pc = (ProcessContainer) process.getDataContainer(this.processDataId);
            I_EncodeBusinessProcess processToLaunch = (I_EncodeBusinessProcess) pc.getData();
            //make an independent copy...
            MarshalledObject mo = new MarshalledObject(processToLaunch);
            processToLaunch = (I_EncodeBusinessProcess) mo.get();
            
            Stack<I_EncodeBusinessProcess> ps = worker.getProcessStack();
            worker.setProcessStack(new Stack<I_EncodeBusinessProcess>());
            processToLaunch.execute(worker);
            worker.setProcessStack(ps);
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] { this.processDataId };
    }

    /**
     * @return Returns the processDataId.
     */
    public int getProcessDataId() {
        return processDataId;
    }

    /**
     * @param processDataId The processDataId to set.
     */
    public void setProcessDataId(int processDataId) {
        this.processDataId = processDataId;
    }
    public void setProcessDataId(Integer processDataId) {
        this.processDataId = processDataId;
    }


}
