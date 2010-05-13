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
package org.dwfa.bpa.tasks.prop;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Copy an object from one process property to another.
 * 
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/property tasks", type = BeanType.TASK_BEAN) })
public class CopyLocalProperty extends AbstractTask {

    private String originalPropName = "";
    private String copyPropName = "";
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(originalPropName);
        out.writeObject(copyPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            originalPropName = (String) in.readObject();
            copyPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public CopyLocalProperty() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            process.setProperty(copyPropName, process.readProperty(originalPropName));
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        return Condition.CONTINUE;
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * @return Returns the localPropName.
     */
    public String getLocalPropName() {
        return originalPropName;
    }

    /**
     * @param localPropName The localPropName to set.
     */
    public void setLocalPropName(String localPropName) {
        this.originalPropName = localPropName;
    }

    /**
     * @return Returns the remotePropName.
     */
    public String getCopyPropName() {
        return copyPropName;
    }

    /**
     * @param remotePropName The remotePropName to set.
     */
    public void setCopyPropName(String remotePropName) {
        this.copyPropName = remotePropName;
    }

}
