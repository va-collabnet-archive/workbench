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
package org.dwfa.ace.task.assignment;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Takes a uuid list of lists and puts 250 and if less than 250 the size of the
 * list number in to uuid list property
 * 
 * @author Susan Castillo
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN) })
public class BatchingAssignments extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();

    private String uuidList2PropName = ProcessAttachmentKeys.BATCH_UUID_LIST2.getAttachmentKey();

    private Integer listListSize = 250;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(uuidListListPropName);
        out.writeObject(uuidList2PropName);
        out.writeObject(listListSize);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            uuidListListPropName = (String) in.readObject();
            uuidList2PropName = (String) in.readObject();
            listListSize = (Integer) in.readObject();
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
            List<Collection<UUID>> tempListList = (List<Collection<UUID>>) process.getProperty(uuidListListPropName);

            if (worker.getLogger().isLoggable(Level.FINE)) {
                worker.getLogger().fine(("Removing first batch in attachment list."));
            }

            int sizeOfList = tempListList.size();
            List<Collection<UUID>> uuidListList = null;
            if (sizeOfList > listListSize) {
                uuidListList = new ArrayList<Collection<UUID>>(tempListList.subList(0, listListSize));
            } else {
                uuidListList = new ArrayList<Collection<UUID>>(tempListList.subList(0, sizeOfList));
            }

            process.setProperty(this.uuidList2PropName, uuidListList);

            if (tempListList.removeAll(uuidListList)) {
                // do nothing
            } else {
                worker.getLogger().info("error encountered in removing uuid collection from list");
            }

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

    public String getUuidListListPropName() {
        return uuidListListPropName;
    }

    public void setUuidListListPropName(String uuidListListPropName) {
        this.uuidListListPropName = uuidListListPropName;
    }

    public Integer getListListSize() {
        return listListSize;
    }

    public void setListListSize(Integer listListSize) {
        this.listListSize = listListSize;
    }

    public String getUuidList2PropName() {
        return uuidList2PropName;
    }

    public void setUuidList2PropName(String uuidList2PropName) {
        this.uuidList2PropName = uuidList2PropName;
    }
}
