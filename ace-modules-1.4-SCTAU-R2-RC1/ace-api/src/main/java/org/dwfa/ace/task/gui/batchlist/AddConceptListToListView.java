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
package org.dwfa.ace.task.gui.batchlist;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/listview", type = BeanType.TASK_BEAN) })
public class AddConceptListToListView extends AbstractTask {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String conceptListPropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(conceptListPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            conceptListPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    @SuppressWarnings("unchecked")
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame configFrame = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            JList conceptList = configFrame.getBatchConceptList();
            I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();

            List<I_GetConceptData> conceptsToAdd = (List<I_GetConceptData>) process.readAttachement(conceptListPropName);

            model.addElements(conceptsToAdd);
            
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getConceptListPropName() {
        return conceptListPropName;
    }

    public void setConceptListPropName(String conceptListPropName) {
        this.conceptListPropName = conceptListPropName;
    }

}
