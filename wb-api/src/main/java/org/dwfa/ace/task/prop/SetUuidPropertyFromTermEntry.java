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
package org.dwfa.ace.task.prop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Sets a UUID property based on the inputted Term Entry.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/prop", type = BeanType.TASK_BEAN) })
public class SetUuidPropertyFromTermEntry extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String uuidPropName = ProcessAttachmentKeys.REFSET_COMPUTE_TYPE_UUID.getAttachmentKey();
    private TermEntry termEntry = new TermEntry(RefsetAuxiliary.Concept.CONCEPT_COMPUTE_TYPE.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(uuidPropName);
        out.writeObject(termEntry);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion > dataVersion) {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
        uuidPropName = (String) in.readObject();
        termEntry = (TermEntry) in.readObject();
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            process.setProperty(uuidPropName, new UUID[] { termEntry.getLocalConcept().getUids().iterator().next() });

            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public TermEntry getTermEntry() {
        return termEntry;
    }

    public void setTermEntry(TermEntry rootEntry) {
        this.termEntry = rootEntry;
    }

    public String getUuidPropName() {
        return uuidPropName;
    }

    public void setUuidPropName(String uuidPropName) {
        this.uuidPropName = uuidPropName;
    }
}
