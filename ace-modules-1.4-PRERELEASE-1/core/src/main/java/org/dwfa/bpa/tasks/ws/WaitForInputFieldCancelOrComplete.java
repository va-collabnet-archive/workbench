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
 * Created on Apr 19, 2005
 */
package org.dwfa.bpa.tasks.ws;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.gui.FieldInputPanel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.pwdbreaker.ConfigureCryptBreakerWorkspace;

/**
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/flow tasks", type = BeanType.TASK_BEAN) })
public class WaitForInputFieldCancelOrComplete extends WaitForCancelOrComplete {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // nothing to do

    }

    /**
     * @see org.dwfa.bpa.tasks.ws.WaitForCancelOrComplete#evaluateAfterAction(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work, org.dwfa.bpa.process.Condition)
     */
    @Override
    public void evaluateAfterAction(I_EncodeBusinessProcess process, I_Work worker, Condition exitCondition) {
        // nothing to do

    }

    /**
     * @see org.dwfa.bpa.tasks.ws.WaitForCancelOrComplete#evaluateStart(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void evaluateStart(I_EncodeBusinessProcess process, I_Work worker) {
        // nothing to do
    }

    /**
     * @param worker
     * @throws TaskFailedException
     */
    protected void addListeners(final I_Work worker) throws TaskFailedException {
        I_Workspace workspace = worker.getCurrentWorkspace();
        FieldInputPanel fieldInputPanel;
        try {
            fieldInputPanel = (FieldInputPanel) workspace.getPanel(ConfigureCryptBreakerWorkspace.INPUT);
        } catch (NoMatchingEntryException e) {
            throw new TaskFailedException(e);
        }
        fieldInputPanel.addCancelActionListener(cancelActionListener);
        fieldInputPanel.addCompleteActionListener(completeActionListener);
        fieldInputPanel.setCancelEnabled(true);
        fieldInputPanel.setCompleteEnabled(true);
    }

    /**
     * @param worker
     * @throws TaskFailedException
     */
    protected void removeListeners(final I_Work worker) throws TaskFailedException {
        I_Workspace workspace = worker.getCurrentWorkspace();
        FieldInputPanel fieldInputPanel;
        try {
            fieldInputPanel = (FieldInputPanel) workspace.getPanel(ConfigureCryptBreakerWorkspace.INPUT);
        } catch (NoMatchingEntryException e) {
            throw new TaskFailedException(e);
        }
        fieldInputPanel.removeCancelActionListener(cancelActionListener);
        fieldInputPanel.removeCompleteActionListener(completeActionListener);
        fieldInputPanel.setCancelEnabled(false);
        fieldInputPanel.setCompleteEnabled(false);
    }

}
