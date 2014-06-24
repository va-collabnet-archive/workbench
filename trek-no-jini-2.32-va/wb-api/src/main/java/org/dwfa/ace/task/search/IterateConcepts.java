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
package org.dwfa.ace.task.search;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Iterate over all the concept in the database. Putting each iteration into the
 * <code>componentPropName</code> so that subsequent tasks can test for
 * inclusion. If a concept should be excluded, the
 * <code>componentPropName</code> value should be set to null prior to returning
 * to this task for the next iteration.
 * The task returns true for each iteration element, and false when the
 * iteration is complete.
 * 
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN) })
public class IterateConcepts extends AbstractTask {

    private transient Iterator<I_GetConceptData> conIt;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * Property name for the term component to test.
     */
    private String componentPropName = ProcessAttachmentKeys.SEARCH_TEST_ITEM.getAttachmentKey();

    /**
     * Property name for a list of UUID lists, typically used to represent a
     * list
     * of concepts in a transportable way.
     * 
     * This task will create a new empty list the first time it is executed
     * within a jvm,
     * regardless of the contents of this property. It accomplishes this
     * secondary to using
     * a transient variable for the concept iterator.
     */
    private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.componentPropName);
        out.writeObject(this.uuidListListPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.componentPropName = (String) in.readObject();
            this.uuidListListPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1) throws TaskFailedException {
        // nothing to do...

    }

    @SuppressWarnings("unchecked")
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            if (conIt == null) {
                conIt = Terms.get().getConceptIterator();
                process.setProperty(uuidListListPropName, new ArrayList<List<UUID>>());
            } else {
                if (process.getProperty(this.componentPropName) != null) {
                    ArrayList<List<UUID>> componentList = (ArrayList<List<UUID>>) process.getProperty(uuidListListPropName);
                    I_GetConceptData concept = AceTaskUtil.getConceptFromProperty(process, this.componentPropName);
                    // add the component to the list here...
                    componentList.add(concept.getUids());
                    AceLog.getAppLog().info("Adding to list: " + concept.getUids());
                }
            }
            if (conIt.hasNext()) {
                I_GetConceptData concept = conIt.next();
                process.setProperty(this.componentPropName, concept);
                return Condition.TRUE;
            } else {
                return Condition.FALSE;
            }
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS_REVERSE;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getComponentPropName() {
        return componentPropName;
    }

    public void setComponentPropName(String componentPropName) {
        this.componentPropName = componentPropName;
    }

    public String getUuidListListPropName() {
        return uuidListListPropName;
    }

    public void setUuidListListPropName(String uuidListListPropName) {
        this.uuidListListPropName = uuidListListPropName;
    }

}
