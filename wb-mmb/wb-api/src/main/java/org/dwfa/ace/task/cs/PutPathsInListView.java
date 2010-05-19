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
package org.dwfa.ace.task.cs;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import javax.swing.JList;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.utypes.cs.CollectEditPaths;
import org.dwfa.ace.utypes.cs.UniversalChangeSetReader;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/change sets", type = BeanType.TASK_BEAN) })
public class PutPathsInListView extends AbstractTask {

    private String inputFilePropName = "A: INPUT_FILE";

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 0;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(inputFilePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            inputFilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            String csFileName = (String) process.getProperty(inputFilePropName);
            File csFile = new File(csFileName);
            if (!csFile.exists() || !csFile.canRead()) {
                throw new TaskFailedException("Specified file '" + csFileName
                    + "' either does not exist or cannot be read");
            }

            CollectEditPaths editPaths = new CollectEditPaths();

            UniversalChangeSetReader csr = new UniversalChangeSetReader(editPaths, csFile);
            csr.read();

            I_ConfigAceFrame profile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            JList conceptList = profile.getBatchConceptList();
            final I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();

            final Set<UUID> idSet = editPaths.getPathSet();
            AceLog.getAppLog().info("Adding list of size: " + idSet.size());

            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    model.clear();
                    for (UUID id : idSet) {
                        try {
                            I_GetConceptData conceptInList = AceTaskUtil.getConceptFromObject(id);
                            model.addElement(conceptInList);
                        } catch (TerminologyException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                            return;
                        } catch (IOException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                            return;
                        }
                    }
                }

            });

        } catch (IllegalArgumentException e1) {
            throw new TaskFailedException(e1);
        } catch (IntrospectionException e1) {
            throw new TaskFailedException(e1);
        } catch (IllegalAccessException e1) {
            throw new TaskFailedException(e1);
        } catch (InvocationTargetException e1) {
            throw new TaskFailedException(e1);
        } catch (InterruptedException e1) {
            throw new TaskFailedException(e1);
        } catch (IOException e1) {
            throw new TaskFailedException(e1);
        } catch (ClassNotFoundException e1) {
            throw new TaskFailedException(e1);
        }
        return Condition.CONTINUE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do.
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getInputFilePropName() {
        return inputFilePropName;
    }

    public void setInputFilePropName(String inputFilePropName) {
        this.inputFilePropName = inputFilePropName;
    }
}
