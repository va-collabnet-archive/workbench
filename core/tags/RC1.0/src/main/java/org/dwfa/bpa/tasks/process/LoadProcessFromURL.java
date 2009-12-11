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
/*
 * Created on Mar 8, 2006
 */
package org.dwfa.bpa.tasks.process;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.data.ProcessContainer;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

@BeanList(specs = 
{ @Spec(directory = "tasks/processes/start tasks", type = BeanType.TASK_BEAN)})
public class LoadProcessFromURL extends AbstractTask {

    private URL processURL = new URL("http://www.informatics.com/hello.xml");

    private int processDataId = -1;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(processURL);
        out.writeInt(processDataId);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            processURL = (URL) in.readObject();
            processDataId = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public LoadProcessFromURL() throws MalformedURLException {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            I_EncodeBusinessProcess processToLaunch;
            if (processURL.toString().toLowerCase().endsWith(".xml")) {
                InputStream inStream;
                if (processURL.getProtocol().toLowerCase().startsWith("file")) {
                    inStream = new FileInputStream(processURL.getFile());
                } else {
                    inStream = process.getStreamFromURL(processURL);
                }
                XMLDecoder d = new XMLDecoder(new BufferedInputStream(inStream));
                processToLaunch = (I_EncodeBusinessProcess) d.readObject();
            } else {
                processToLaunch = (I_EncodeBusinessProcess) process.getObjectFromURL(processURL);
            }
            ProcessContainer pc = (ProcessContainer) process
                    .getDataContainer(this.processDataId);
            pc.setData(processToLaunch);
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] { this.processDataId };
    }

    /**
     * @return Returns the processURL.
     */
    public String getProcessURLString() {
        return processURL.toString();
    }

    /**
     * @return Returns the processDataId.
     */
    public int getProcessDataId() {
        return processDataId;
    }

    /**
     * @param processDataId
     *            The processDataId to set.
     */
    public void setProcessDataId(int processDataId) {
        this.processDataId = processDataId;
    }

    public void setProcessDataId(Integer processDataId) {
        this.processDataId = processDataId;
    }

    /**
     * @param processURL
     *            The processURL to set.
     * @throws MalformedURLException
     */
    public void setProcessURLString(String processURLString)
            throws MalformedURLException {
        this.processURL = new URL(processURLString);
    }

}
