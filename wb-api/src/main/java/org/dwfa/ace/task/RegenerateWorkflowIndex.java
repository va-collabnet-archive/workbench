/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;

@BeanList(specs = {
    @Spec(directory = "tasks/workflow", type = BeanType.TASK_BEAN)})
public class RegenerateWorkflowIndex extends AbstractTask{
    // Serialization Properties
    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }
    
    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }
    
    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try{
            new Thread(
                    new Runnable() {

                        @Override
                        public void run() {
                            try {
                                System.out.println("*** Starting workflow history lucene index regeneration.");
                                File wfLuceneDirectory = new File("workflow/lucene");
                                if (wfLuceneDirectory.exists()) {
                                    for (File wfFile : wfLuceneDirectory.listFiles()) {
                                        wfFile.delete();
                                    }
                                    wfLuceneDirectory.delete();
                                }
                                I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
                                Ts.get().regenerateWfHxLuceneIndex(config.getViewCoordinate());
                                System.out.println("*** Finished workflow history lucene index regeneration.");
                            } catch (IOException ex) {
                                AceLog.getAppLog().alertAndLogException(ex);
                            } catch (Exception ex) {
                                AceLog.getAppLog().alertAndLogException(ex);
                            }
                        }
                    }).start();

        }catch(Exception e){
            throw new TaskFailedException(e);
        }
         return Condition.CONTINUE;
    }
    
    @Override
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }
    
    @Override
    public int[] getDataContainerIds() {
        return new int[]{};
    }
}
