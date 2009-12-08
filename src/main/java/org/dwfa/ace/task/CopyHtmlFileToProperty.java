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

import java.io.File;
import java.io.FileReader;
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
import org.dwfa.util.io.FileIO;

@BeanList(specs = { @Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN) })
/**
 * Copy the contents of an html file to a property
 * @author Susan Castillo
 *
 */
public class CopyHtmlFileToProperty extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String htmlDataPropName = ProcessAttachmentKeys.HTML_STR.getAttachmentKey();
    private String detailHtmlFileNameProp = ProcessAttachmentKeys.DETAIL_HTML_FILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(htmlDataPropName);
        out.writeObject(detailHtmlFileNameProp);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            htmlDataPropName = (String) in.readObject();
            detailHtmlFileNameProp = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public CopyHtmlFileToProperty() throws MalformedURLException {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            String detailHtmlFileName = (String) process.readProperty(detailHtmlFileNameProp);
            // worker.getLogger().info("html file is: " + detailHtmlFileName);
            if (new File(detailHtmlFileName).exists()) {
                String dataString = FileIO.readerToString(new FileReader(detailHtmlFileName));
                // worker.getLogger().info("data String is: "+dataString);
                process.setProperty(htmlDataPropName, dataString);
            } else {
                process.setProperty(htmlDataPropName, " ");
            }

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

    public String getDetailHtmlFileNameProp() {
        return detailHtmlFileNameProp;
    }

    public void setDetailHtmlFileNameProp(String detailHtmlFileNameProp) {
        this.detailHtmlFileNameProp = detailHtmlFileNameProp;
    }

}
