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
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.util.PlatformWebBrowser;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Displays an attachment in a web browser
 * 
 * @author Susan Castillo
 * 
 */

@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class ShowFileInWebBrowser extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String detailHtmlFileNameProp = ProcessAttachmentKeys.DETAIL_HTML_FILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(detailHtmlFileNameProp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            detailHtmlFileNameProp = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            String test1 = (String) process.readProperty(detailHtmlFileNameProp);
            worker.getLogger().info("test1: " + test1);
            File htmlFile = new File((String) process.readProperty(detailHtmlFileNameProp));
            URL fileUrl = htmlFile.toURI().toURL();

            worker.getLogger().info("URL: " + fileUrl.toString());
            PlatformWebBrowser.openURL(fileUrl);

            return Condition.CONTINUE;

        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (MalformedURLException e) {
            throw new TaskFailedException(e);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getDetailHtmlFileNameProp() {
        return detailHtmlFileNameProp;
    }

    public void setDetailHtmlFileNameProp(String detailHtmlFileNameProp) {
        this.detailHtmlFileNameProp = detailHtmlFileNameProp;
    }

}
