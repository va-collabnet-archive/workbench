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
package org.dwfa.bpa.tasks.process;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.MarshalledObject;
import java.util.Collection;
import java.util.Stack;
import java.util.logging.Level;

import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.SuppressDataChecks;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/start tasks", type = BeanType.TASK_BEAN) })
public class LoadSetLaunchProcessFromAttachment extends AbstractTask {

    private String processPropName;
    private String originator;
    private String destination;
    private String processName;
    private String processSubject;

    private boolean dataCheckingSuppressed = false;

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(processPropName);
        out.writeObject(dataCheckingSuppressed);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            processPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

        if (objDataVersion == dataVersion) {
            dataCheckingSuppressed = (Boolean) in.readObject();
        }
    }

    @SuppressWarnings("unchecked")
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            // Load process...
            MarshalledObject marshalledProcess = (MarshalledObject) process.readProperty(processPropName);
            if (marshalledProcess == null) {
                marshalledProcess = (MarshalledObject) process.readAttachement(processPropName);
            }
            I_EncodeBusinessProcess processToLaunch = (I_EncodeBusinessProcess) marshalledProcess.get();

            if (processSubject != null) {
                processToLaunch.setSubject(processSubject);
            }
            if (processName != null) {
                processToLaunch.setName(processName);
            }
            if (destination != null) {
                processToLaunch.setDestination(destination);
            }
            if (originator != null) {
                processToLaunch.setOriginator(originator);
            }

            // Set properties...
            for (PropertyDescriptor propDesc : processToLaunch.getBeanInfo().getPropertyDescriptors()) {
                if (PropertyDescriptorWithTarget.class.isAssignableFrom(propDesc.getClass())) {
                    try {
                        PropertyDescriptorWithTarget pdwt = (PropertyDescriptorWithTarget) propDesc;
                        Object value = process.readProperty(pdwt.getLabel());
                        propDesc.getWriteMethod().invoke(pdwt.getTarget(), value);
                        if (worker.getLogger().isLoggable(Level.FINE)) {
                            worker.getLogger().fine("Set " + pdwt.getLabel() + " to " + value);
                        }
                    } catch (Exception ex) {
                        worker.getLogger().warning(ex.getMessage());
                    }
                }
            }

            // Launch Process...
            Stack<I_EncodeBusinessProcess> ps = worker.getProcessStack();
            worker.setProcessStack(new Stack<I_EncodeBusinessProcess>());
            if (isDataCheckingSuppressed()) {
                executeWithSuppression(processToLaunch, worker);
            } else {
                processToLaunch.execute(worker);
            }
            worker.setProcessStack(ps);
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    @SuppressDataChecks
    private void executeWithSuppression(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        process.execute(worker);
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessSubject() {
        return processSubject;
    }

    public void setProcessSubject(String processSubject) {
        this.processSubject = processSubject;
    }

    public String getProcessPropName() {
        return processPropName;
    }

    public void setProcessPropName(String processPropName) {
        this.processPropName = processPropName;
    }

    public boolean isDataCheckingSuppressed() {
        return dataCheckingSuppressed;
    }

    public void setDataCheckingSuppressed(boolean dataCheckingSuppressed) {
        this.dataCheckingSuppressed = dataCheckingSuppressed;
    }

}
