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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN) })
/**
 * Write an attachment to disk
 * @author Susan Castillo
 *
 */
public class WriteAttachmentToDisk extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String uuidStrPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT_UUID.getAttachmentKey();

    private String htmlDataPropName = ProcessAttachmentKeys.HTML_STR.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(uuidStrPropName);
        out.writeObject(htmlDataPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            uuidStrPropName = (String) in.readObject();
            htmlDataPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public WriteAttachmentToDisk() throws MalformedURLException {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            File rootDir = new File("temp");

            rootDir.mkdirs();
            String uuidStr = (String) process.getProperty(uuidStrPropName);
            worker.getLogger().info("STR uuid is: " + uuidStr);
            String htmlStr = (String) process.getProperty(htmlDataPropName);

            File dataFile = new File(rootDir, uuidStr + ".html");
            worker.getLogger().info("html is: " + dataFile);
            FileWriter ddfw = new FileWriter(dataFile);
            BufferedWriter dataWriter = new BufferedWriter(ddfw);
            dataWriter.append(htmlStr);
            dataWriter.close();

            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getHtmlDataPropName() {
        return htmlDataPropName;
    }

    public void setHtmlDataPropName(String htmlDataPropName) {
        this.htmlDataPropName = htmlDataPropName;
    }

    public String getUuidStrPropName() {
        return uuidStrPropName;
    }

    public void setUuidStrPropName(String uuidStrPropName) {
        this.uuidStrPropName = uuidStrPropName;
    }

}
