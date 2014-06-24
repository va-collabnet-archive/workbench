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
package org.ihtsdo.arena.task;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;
import org.dwfa.ace.task.ProcessAttachmentKeys;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.helper.msfile.MemberSubmissionFileHelper;

/**
 * Processes member submission files (TDTF) and returns a list of strings
 * 
 * @author akf
 * 
 */
@BeanList(specs = {
    @Spec(directory = "tasks/arena", type = BeanType.TASK_BEAN)})
public class ProcessMemberSubmissions extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private transient Condition returnCondition;
    private String msFileProp = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
    private String stringSetProp = ProcessAttachmentKeys.STRING_SET.getAttachmentKey();


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(msFileProp);
        out.writeObject(stringSetProp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
           msFileProp = (String) in.readObject();
           stringSetProp = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        
        try{
            String fileName = (String) process.getProperty(
                    ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey());
            File file = new File(fileName);
            Set<String> msFileSet = MemberSubmissionFileHelper.getMsFileSet("NHS", file);
            process.setProperty(this.stringSetProp, msFileSet);   
            returnCondition = Condition.CONTINUE;
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        }catch(IOException e){
            throw new TaskFailedException(e);
        }catch(IntrospectionException e){
            throw new TaskFailedException(e);
        }
        return returnCondition;
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }
    
    public String getStringSetProp() {
        return stringSetProp;
    }

    public void setStringSetProp(String stringSetProp) {
        this.stringSetProp = stringSetProp;
    }
    
    public String getMsFileProp() {
        return msFileProp;
    }

    public void setMsFileProp(String msFileProp) {
        this.msFileProp = msFileProp;
    }
}
