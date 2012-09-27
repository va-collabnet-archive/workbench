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
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.file.ConceptListWriter;
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
public class WriteConceptListFile extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String outputFilePropName = ProcessAttachmentKeys.PROCESS_FILENAME.getAttachmentKey();
    private String conceptListPropName = ProcessAttachmentKeys.DEFAULT_CONCEPT_LIST.getAttachmentKey();
    private String errorMessagePropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();

    private int hostIndex = 3;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(hostIndex);
        out.writeObject(outputFilePropName);
        out.writeObject(conceptListPropName);
        out.writeObject(errorMessagePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            hostIndex = in.readInt();
            outputFilePropName = (String) in.readObject();
            conceptListPropName = (String) in.readObject();
            errorMessagePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    @SuppressWarnings("unchecked")
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        Condition returnValue = Condition.FALSE;
        try {
            List<I_GetConceptData> conceptList = (List<I_GetConceptData>) process.getProperty(conceptListPropName);

            String filename = (String) process.getProperty(outputFilePropName);
            File outputFile = new File(filename);

            if (!outputFile.exists() || outputFile.canWrite()) {

                ConceptListWriter writer = new ConceptListWriter();
                writer.open(outputFile, false);
                writer.write(conceptList);
                writer.close();

                returnValue = Condition.TRUE;

            } else {
                process.setProperty(errorMessagePropName, "Cannot write to file " + outputFile.getAbsolutePath() + ".");
            }

        } catch (Exception e) {
            try {
                process.setProperty(errorMessagePropName, "Cannot write to file due to exception - " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e1) {
                throw new TaskFailedException("failed creating error message for exception " + e.getMessage(), e1);
            }
        }

        return returnValue;
    }

    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS;
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

    public String getOutputFilePropName() {
        return outputFilePropName;
    }

    public void setOutputFilePropName(String outputFilePropName) {
        this.outputFilePropName = outputFilePropName;
    }

    public String getConceptListPropName() {
        return conceptListPropName;
    }

    public void setConceptListPropName(String conceptListPropName) {
        this.conceptListPropName = conceptListPropName;
    }

    public String getErrorMessagePropName() {
        return errorMessagePropName;
    }

    public void setErrorMessagePropName(String errorMessage) {
        this.errorMessagePropName = errorMessage;
    }

}
