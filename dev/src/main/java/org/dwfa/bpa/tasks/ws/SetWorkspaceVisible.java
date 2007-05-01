/*
 * Created on May 22, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.ws;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;


/**
 * @author kec <p>
 * Sets the workspace to be visible if checkbox is selected. Otherwise, workspace is hidden to the user.
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/processes/workspace tasks", type = BeanType.TASK_BEAN)})
public class SetWorkspaceVisible extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private Boolean visible = Boolean.TRUE;
    
    /**
     * @return Returns the elementId.
     */
    public Boolean getVisible() {
        return visible;
    }
    public void setVisible(Boolean visible) {
        Boolean oldValue = this.visible;
        this.visible = visible;
        this.firePropertyChange("visible", oldValue, this.visible);
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.visible);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
                this.visible = (Boolean) in.readObject();
         } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		I_Workspace activeWorkspace = worker.getCurrentWorkspace();
        activeWorkspace.setWorkspaceVisible(this.visible.booleanValue());
		return Condition.CONTINUE;
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// nothing to do...

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

}
