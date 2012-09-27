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
package org.dwfa.ace.task.gui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/gui", type = BeanType.TASK_BEAN) })
public class EnableQueueButtons extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private Boolean enableNewInboxButton = true;
    private Boolean enableExistingInboxButton = true;
    private Boolean enableMoveListenerButton = true;
    private Boolean enableAllQueuesButton = true;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeBoolean(enableNewInboxButton);
        out.writeBoolean(enableExistingInboxButton);
        out.writeBoolean(enableMoveListenerButton);
        out.writeBoolean(enableAllQueuesButton);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            enableNewInboxButton = in.readBoolean();
            enableExistingInboxButton = in.readBoolean();
            enableMoveListenerButton = in.readBoolean();
            enableAllQueuesButton = in.readBoolean();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame configFrame =
                    (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            configFrame.setEnabledNewInboxButton(enableNewInboxButton);
            configFrame.setEnabledExistingInboxButton(enableExistingInboxButton);
            configFrame.setEnabledMoveListenerButton(enableMoveListenerButton);
            configFrame.setEnabledAllQueuesButton(enableAllQueuesButton);
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        }
    }

    @Override
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    @Override
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public Boolean getEnableNewInboxButton() {
        return enableNewInboxButton;
    }

    public void setEnableNewInboxButton(Boolean enableNewInboxButton) {
        this.enableNewInboxButton = enableNewInboxButton;
    }

    public Boolean getEnableExistingInboxButton() {
        return enableExistingInboxButton;
    }

    public void setEnableExistingInboxButton(Boolean enableExistingInboxButton) {
        this.enableExistingInboxButton = enableExistingInboxButton;
    }

    public Boolean getEnableMoveListenerButton() {
        return enableMoveListenerButton;
    }

    public void setEnableMoveListenerButton(Boolean enableMoveListenerButton) {
        this.enableMoveListenerButton = enableMoveListenerButton;
    }

    public Boolean getEnableAllQueuesButton() {
        return enableAllQueuesButton;
    }

    public void setEnableAllQueuesButton(Boolean enableAllQueuesButton) {
        this.enableAllQueuesButton = enableAllQueuesButton;
    }

}
