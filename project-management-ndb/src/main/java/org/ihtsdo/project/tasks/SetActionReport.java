/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.tasks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.project.workflow.model.WfInstance;

/**
 * The Class SetActionReport.
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/profile", type = BeanType.TASK_BEAN) })
public class SetActionReport extends AbstractTask {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant dataVersion. */
    private static final int dataVersion = 1;

    /** The action report. */
    private WfInstance.ActionReport actionReport = WfInstance.ActionReport.CANCEL ;

    /**
     * Write object.
     *
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(actionReport);
    }

    /**
     * Read object.
     *
     * @param in the in
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	actionReport = (WfInstance.ActionReport) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
			WfInstance instance = (WfInstance) process.readAttachement("WfInstance");
		
			instance.setActionReport(actionReport);
           
			 process.writeAttachment("WfInstance",instance);
			 
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

	/**
	 * Gets the action report.
	 *
	 * @return the action report
	 */
	public WfInstance.ActionReport getActionReport() {
		return actionReport;
	}

	/**
	 * Sets the action report.
	 *
	 * @param actionReport the new action report
	 */
	public void setActionReport(WfInstance.ActionReport actionReport) {
		this.actionReport = actionReport;
	}

}
