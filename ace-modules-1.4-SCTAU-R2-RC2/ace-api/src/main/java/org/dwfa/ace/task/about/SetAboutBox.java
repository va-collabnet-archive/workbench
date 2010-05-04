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
package org.dwfa.ace.task.about;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JFrame;

import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.util.AboutBox;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author Luke
 *         Date: Nov 2, 2009
 *         Time: 11:01:05 AM
 */
@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class SetAboutBox extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private static int objDataVersion = -1;

    private String aboutBoxHtmlPropName = ProcessAttachmentKeys.ABOUT_BOX_HTML_TEXT.getAttachmentKey();
    private String aboutBoxTitlePropName = ProcessAttachmentKeys.ABOUT_BOX_TITLE_TEXT.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(aboutBoxHtmlPropName);
        out.writeObject(aboutBoxTitlePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            aboutBoxHtmlPropName = (String) in.readObject();
            aboutBoxTitlePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            String aboutBoxHtml = "" + process.readProperty(aboutBoxHtmlPropName);
            String aboutBoxTitle = "" + process.readProperty(aboutBoxTitlePropName);

            if (aboutBoxHtml != null && !aboutBoxHtml.equals("") && aboutBoxTitle != null && !aboutBoxTitle.equals("")) {

                JFrame parentFrame = getCurrentFrame();

                CustomAboutBox aboutBox = new CustomAboutBox(parentFrame, aboutBoxHtml, aboutBoxTitle);

                AboutBox.setAboutBox(aboutBox);
            }

        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        }
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        return Condition.CONTINUE;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public JFrame getCurrentFrame() {
        for (JFrame frame : OpenFrames.getFrames()) {
            if (frame.isActive()) {
                return frame;
            }
        }
        return null;
    }

    public static int getObjDataVersion() {
        return objDataVersion;
    }

    public static void setObjDataVersion(int objDataVersion) {
        SetAboutBox.objDataVersion = objDataVersion;
    }

    public String getAboutBoxHtmlPropName() {
        return aboutBoxHtmlPropName;
    }

    public void setAboutBoxHtmlPropName(String aboutBoxHtmlPropName) {
        this.aboutBoxHtmlPropName = aboutBoxHtmlPropName;
    }

    public String getAboutBoxTitlePropName() {
        return aboutBoxTitlePropName;
    }

    public void setAboutBoxTitlePropName(String aboutBoxTitlePropName) {
        this.aboutBoxTitlePropName = aboutBoxTitlePropName;
    }
}
