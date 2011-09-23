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
package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import java.util.Set;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
    @Spec(directory = "tasks/arena", type = BeanType.TASK_BEAN)})
public class TakeFirstStringInSet extends AbstractTask {

    /**
     * Takes first string in set until list is empty
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private transient Condition returnCondition;
    private String submissionLineProp = ProcessAttachmentKeys.NAME1.getAttachmentKey();
    private String msStringSetProp = ProcessAttachmentKeys.STRING_SET.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(submissionLineProp);
        out.writeObject(msStringSetProp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            submissionLineProp = (String) in.readObject();
            msStringSetProp = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            Set<String> msStringSet = (Set<String>) process.getProperty(
                    ProcessAttachmentKeys.STRING_SET.getAttachmentKey());
            if(!msStringSet.isEmpty()){
                String firstLine = msStringSet.iterator().next();
                msStringSet.remove(firstLine);
                process.setProperty(this.submissionLineProp, firstLine);
                returnCondition = Condition.TRUE;
            }else {
                returnCondition = Condition.FALSE;
            }
            return returnCondition;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public String getSubmissionLineProp() {
        return submissionLineProp;
    }

    public void setSubmissionLineProp(String submissionLineProp) {
        this.submissionLineProp = submissionLineProp;
    }
    
    public String getMsStringSetProp() {
        return msStringSetProp;
    }

    public void setMsStringSetProp(String msStringSetProp) {
        this.msStringSetProp = msStringSetProp;
    }
}
