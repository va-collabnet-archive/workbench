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
package org.dwfa.ace.task.file;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.file.ConceptListReader;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/file", type = BeanType.TASK_BEAN) })
public class ReadConceptListFile extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String inputFilePropName = ProcessAttachmentKeys.PROCESS_FILENAME.getAttachmentKey();
    private String conceptListPropName = ProcessAttachmentKeys.DEFAULT_CONCEPT_LIST.getAttachmentKey();

    private int hostIndex = 3;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(hostIndex);
        out.writeObject(inputFilePropName);
        out.writeObject(conceptListPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            hostIndex = in.readInt();
            inputFilePropName = (String) in.readObject();
            conceptListPropName = (String) in.readObject();
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
            String filename = (String) process.readProperty(inputFilePropName);
            File inputFile = new File(filename);

            ConceptListReader conceptListReader = new ConceptListReader();
            conceptListReader.setSourceFile(inputFile);

            List<I_GetConceptData> conceptList = new ArrayList<I_GetConceptData>();

            for (I_GetConceptData getConceptData : conceptListReader) {
                conceptList.add(getConceptData);
            }

            process.setProperty(conceptListPropName, conceptList);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Integer getHostIndex() {
        return hostIndex;
    }

    public void setHostIndex(Integer hostIndex) {
        this.hostIndex = hostIndex;
    }

    public String getInputFilePropName() {
        return inputFilePropName;
    }

    public void setInputFilePropName(String inputFilePropName) {
        this.inputFilePropName = inputFilePropName;
    }

    public String getConceptListPropName() {
        return conceptListPropName;
    }

    public void setConceptListPropName(String conceptListPropName) {
        this.conceptListPropName = conceptListPropName;
    }

}
