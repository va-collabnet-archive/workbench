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
package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
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
 * Takes/removes the first item (UUID) in a specified attachment list and
 * returns a UUID.
 * 
 * @author Susan Castillo
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN) })
public class TakeFirstItemInAttachmentListReturnUUID extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String uuidListPropName = ProcessAttachmentKeys.UUID_LIST.getAttachmentKey();

    private String potDupUuidPropName = ProcessAttachmentKeys.CONCEPT_UUID.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(uuidListPropName);
        out.writeObject(potDupUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            uuidListPropName = (String) in.readObject();
            potDupUuidPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    @SuppressWarnings("unchecked")
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            List<UUID> temporaryListUuid = (List<UUID>) process.getProperty(uuidListPropName);

            if (worker.getLogger().isLoggable(Level.FINE)) {
                worker.getLogger().fine(("Removing first item in attachment list."));
            }

            worker.getLogger().info("uuidListPropName: " + uuidListPropName);
            worker.getLogger().info("temporaryListUuid: " + temporaryListUuid);
            UUID uuid = (UUID) temporaryListUuid.remove(0);
            worker.getLogger().info("uuid: " + uuid);

            process.setProperty(this.potDupUuidPropName, uuid);

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getUuidListPropName() {
        return uuidListPropName;
    }

    public void setUuidListPropName(String uuidListPropName) {
        this.uuidListPropName = uuidListPropName;
    }

    public String getPotDupUuidPropName() {
        return potDupUuidPropName;
    }

    public void setPotDupUuidPropName(String potDupUuidPropName) {
        this.potDupUuidPropName = potDupUuidPropName;
    }

}
