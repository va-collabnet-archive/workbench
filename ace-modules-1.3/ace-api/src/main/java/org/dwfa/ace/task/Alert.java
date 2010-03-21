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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class Alert extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 2;

    private String alertText = "<html>Alert text";
    private String alertTextProperty = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(alertText);
        out.writeObject(alertTextProperty);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            alertText = (String) in.readObject();
        }

        if (objDataVersion >= 2) {
            alertTextProperty = (String) in.readObject();
        } else {
            alertTextProperty = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        String readProperty;

        try {
            readProperty = (String) process.readProperty(alertTextProperty);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        if (readProperty == null) {
            readProperty = "";
        }

        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), alertText + readProperty, "",
            JOptionPane.WARNING_MESSAGE);

        return Condition.CONTINUE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getAlertText() {
        return alertText;
    }

    public void setAlertText(String alertText) {
        this.alertText = alertText;
    }

    public String getAlertTextProperty() {
        return alertTextProperty;
    }

    public void setAlertTextProperty(String alertTextProperty) {
        this.alertTextProperty = alertTextProperty;
    }
}
