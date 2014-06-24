/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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

package org.ihtsdo.arena.task;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.dwfa.ace.api.I_ConfigAceFrame;
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
import org.ihtsdo.helper.msfile.DescriptionAdditionFileHelper;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.api.ContradictionException;

/**
 * Task for adding uuids to concept batch import files. Needs to be
 * used with a task for selecting the import file. Writes out a new file with uuids
 * inlcuded. DOES NOT write over initial file.
 * @author aimeefurber
 */
@BeanList(specs = {
@Spec(directory = "tasks/arena", type = BeanType.TASK_BEAN)})
public class AddUuidToImportFile extends AbstractTask{
    
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private transient Condition returnCondition;
    private String msFileProp = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
    
     private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(msFileProp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            msFileProp = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException, ContradictionException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(
                    WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            String fileName = (String) process.getProperty(ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey());
            File file = new File(fileName);
            ArrayList<String> incomingFileList = DescriptionAdditionFileHelper.getDescFileList(file);
            //write out to new file? Don't overwrite old file. Handle UTF-8
            File outputFile = new File(fileName.replace(".txt", "UUID.txt"));
            if(outputFile.exists()){
                outputFile = new File(fileName.replace(".txt", "UUID_" + TimeHelper.formatDateForFile(System.currentTimeMillis())+ ".txt"));
            }
            FileOutputStream fos = new FileOutputStream(outputFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            
            for(String line : incomingFileList){
                writer.write(UUID.randomUUID() + "\t" + line + "\r\n");
            }
            writer.close();
            JOptionPane.showMessageDialog(null, "UUIDs added.", "UUIDs added.", JOptionPane.OK_OPTION);
            returnCondition = Condition.CONTINUE;
        } catch (IntrospectionException ex) { //make multicatch
           throw new TaskFailedException(ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AddUuidToImportFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AddUuidToImportFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(AddUuidToImportFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AddUuidToImportFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnCondition; //fix this
    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do.
    }
    
    public int[] getDataContainerIds() {
        return new int[]{};
    }
    
    @Override
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }
    
    public String getMsFileProp() {
        return msFileProp;
    }

    public void setMsFileProp(String msFileProp) {
        this.msFileProp = msFileProp;
    }
}
