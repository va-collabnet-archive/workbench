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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Collection;
import org.dwfa.ace.reporting.ChangeReport;
import org.dwfa.ace.reporting.ChangeReportBase;
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
 * Creates difference report based on two different dates
 * 
 * @author akf
 * 
 */

@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class CreateReport extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String begDateProp = ProcessAttachmentKeys.START_DATE.getAttachmentKey();
    private String endDateProp = ProcessAttachmentKeys.END_DATE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(begDateProp);
        out.writeObject(endDateProp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            begDateProp = (String) in.readObject();
            endDateProp = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            //stuff todo 
            ChangeReport reporter = new ChangeReport((String) process.getProperty(begDateProp),
                    (String) process.getProperty(endDateProp));
            reporter.execute();

            return Condition.CONTINUE;

        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e){
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e){
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getBegDateProp() {
        return begDateProp;
    }

    public void setBegDateProp(String begDateProp) {
        this.begDateProp = begDateProp;
    }
    
    public String getEndDateProp() {
        return endDateProp;
    }

    public void setEndDateProp(String endDateProp) {
        this.endDateProp = endDateProp;
    }

}
