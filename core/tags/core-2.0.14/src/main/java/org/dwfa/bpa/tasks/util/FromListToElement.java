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
package org.dwfa.bpa.tasks.util;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;


/**
 * Takes the first element from a list data container and places the value in the element data container. 
 * <p>
 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/list tasks", type = BeanType.TASK_BEAN)})
public class FromListToElement extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private int elementId = -1;
    private int listId = -1;

    /**
     * @return Returns the elementId.
     */
    public int getElementId() {
        return elementId;
    }
    public void setElementId(Integer elementId) {
        setElementId(elementId.intValue());
    }
    public void setElementId(int elementId) {
        int oldValue = this.elementId;
        this.elementId = elementId;
        this.firePropertyChange("elementId", oldValue, this.elementId);
    }
    /**
     * @return Returns the listId.
     */
    public int getListId() {
        return listId;
    }
    public void setListId(Integer listId) {
        setListId(listId.intValue());
    }
    /**
     * @param listId The listId to set.
     */
    public void setListId(int listId) {
        int oldValue = this.listId;
        this.listId = listId;
        this.firePropertyChange("listId", oldValue, this.listId);
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(this.elementId);
        out.writeInt(this.listId);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
                this.elementId = in.readInt();
            this.listId = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    /**
     * @param name
     */
    public FromListToElement() {
        super();
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {        
        try {
            I_ContainData listContainer = process.getDataContainer(this.listId);
            I_ContainData elementContainer = process.getDataContainer(this.elementId);
            List list = (List) listContainer.getData();
            if (list.size() == 0) {
             return Condition.FALSE;   
            }
            elementContainer.setData((Serializable) list.remove(0));
            return Condition.TRUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        //Nothing to do
        
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS;
    }


    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] { this.elementId, this.listId };
    }
}
