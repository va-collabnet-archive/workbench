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
 * Created on Feb 22, 2006
 */
package org.dwfa.bpa.tasks.prop;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Collection;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.data.ProcessContainer;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

/**
 * Set the specified local property to the value provided by the property in the remote process.

 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/processes/set tasks", type = BeanType.TASK_BEAN)})
public class SetLocalProperty extends AbstractTask {

    private int processDataId = -1;
    private String localPropName = "";
    private String remotePropName = "";
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(processDataId);
        out.writeObject(localPropName);
        out.writeObject(remotePropName);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            processDataId = in.readInt();
            localPropName = (String) in.readObject();
            remotePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    public SetLocalProperty() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            ProcessContainer pc = (ProcessContainer) process.getDataContainer(processDataId);
            I_EncodeBusinessProcess remoteProcess = (I_EncodeBusinessProcess) pc.getData();
            PropertyDescriptor[] remoteDescriptors = remoteProcess.getAllPropertiesBeanInfo().getPropertyDescriptors();
            PropertyDescriptor[] localDescriptors = process.getAllPropertiesBeanInfo().getPropertyDescriptors();
            PropertyDescriptorWithTarget remoteDescriptor = null;
            PropertyDescriptorWithTarget localDescriptor = null;
            for (PropertyDescriptor d: remoteDescriptors) {
                if (PropertyDescriptorWithTarget.class.isAssignableFrom(d.getClass())) {
                    PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                    if (dwt.getLabel().equals(this.remotePropName)) {
                        remoteDescriptor = dwt;
                        break;
                    }
                } 
            }
            for (PropertyDescriptor d: localDescriptors) {
                if (PropertyDescriptorWithTarget.class.isAssignableFrom(d.getClass())) {
                    PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                    if (dwt.getLabel().equals(this.localPropName)) {
                        localDescriptor = dwt;
                        break;
                    }
                } 
            }
            Method writeMethod = localDescriptor.getWriteMethod();
            Method readMethod = remoteDescriptor.getReadMethod();
            Object remoteVal = readMethod.invoke(remoteDescriptor.getTarget());
            writeMethod.invoke(localDescriptor.getTarget(), new Object[] { remoteVal });
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        return Condition.CONTINUE;
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        //Nothing to do...
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] { this.processDataId};
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

    /**
     * @return Returns the remotePropName.
     */
    public String getRemotePropName() {
        return remotePropName;
    }

    /**
     * @param remotePropName The remotePropName to set.
     */
    public void setRemotePropName(String remotePropName) {
        this.remotePropName = remotePropName;
    }

}
