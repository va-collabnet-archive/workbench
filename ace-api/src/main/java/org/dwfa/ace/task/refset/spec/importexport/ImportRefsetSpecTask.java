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
package org.dwfa.ace.task.refset.spec.importexport;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.file.TupleFileUtil;
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
 * Imports the refset currently in the refset spec panel to the specified file.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/export", type = BeanType.TASK_BEAN) })
public class ImportRefsetSpecTask extends AbstractTask {

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private String inputFilePropName = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
    private String outputFilePropName = ProcessAttachmentKeys.OUTPUT_FILE.getAttachmentKey();
    private String pathUuidPropName = ProcessAttachmentKeys.PATH_UUID.getAttachmentKey();

    public String getInputFilePropName() {
        return inputFilePropName;
    }

    public void setInputFilePropName(String inputFilePropName) {
        this.inputFilePropName = inputFilePropName;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(inputFilePropName);
        out.writeObject(outputFilePropName);
        out.writeObject(pathUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= 1) {
            inputFilePropName = (String) in.readObject();
        } else if (objDataVersion == 2) {
            inputFilePropName = (String) in.readObject();
            outputFilePropName = (String) in.readObject();
            pathUuidPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        // initialize the progress panel
        I_ShowActivity activityPanel = null;
        try {
            activityPanel = LocalVersionedTerminology.get().newActivityPanel(true,
                LocalVersionedTerminology.get().getActiveAceFrameConfig());
            String importFileName = (String) process.readProperty(inputFilePropName);
            String outputFileName = (String) process.readProperty(outputFilePropName);
            Object pathObj = process.readProperty(pathUuidPropName);
            UUID pathUuid;
            if (pathObj == null) {
                pathUuid = null;
            } else {
                pathUuid = (UUID) pathObj;
            }

            activityPanel.setIndeterminate(true);
            activityPanel.setProgressInfoUpper("Importing refset spec from file : " + importFileName);
            activityPanel.setProgressInfoLower("<html><font color='black'> In progress.");

            TupleFileUtil tupleImporter = new TupleFileUtil();
            I_GetConceptData memberRefset = tupleImporter.importFile(new File(importFileName),
                new File(outputFileName), pathUuid);

            LocalVersionedTerminology.get().commit();

            activityPanel.setProgressInfoUpper("Importing refset spec from file : " + importFileName);
            activityPanel.setProgressInfoLower("<html><font color='red'> COMPLETE. <font color='black'>");

            activityPanel.complete();

            if (memberRefset != null) {
                process.setProperty(ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey(),
                    LocalVersionedTerminology.get().getUids(memberRefset.getConceptId()).iterator().next());
            }

            LocalVersionedTerminology.get().getActiveAceFrameConfig().setBuilderToggleVisible(true);
            LocalVersionedTerminology.get().getActiveAceFrameConfig().setInboxToggleVisible(true);

            return Condition.CONTINUE;
        } catch (Exception ex) {
            try {
                if (activityPanel != null) {
                    activityPanel.complete();
                }
                LocalVersionedTerminology.get().cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw new TaskFailedException(ex);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getOutputFilePropName() {
        return outputFilePropName;
    }

    public void setOutputFilePropName(String outputFilePropName) {
        this.outputFilePropName = outputFilePropName;
    }

    public String getPathUuidPropName() {
        return pathUuidPropName;
    }

    public void setPathUuidPropName(String pathUuidPropName) {
        this.pathUuidPropName = pathUuidPropName;
    }
}
