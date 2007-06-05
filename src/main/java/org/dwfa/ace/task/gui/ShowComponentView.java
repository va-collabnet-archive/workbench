package org.dwfa.ace.task.gui;

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
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.WorkerAttachmentKeys;

/**
 * Task which shows the component view.
 * @author Christine Hill
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/gui", type = BeanType.TASK_BEAN) })
public class ShowComponentView extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private boolean visible = false;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeBoolean(visible);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            visible = in.readBoolean();
        } else {
            throw new IOException(
                    "Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
                         throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
                                throws TaskFailedException {
        try {
            I_ConfigAceFrame configFrame = (I_ConfigAceFrame) worker
                .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            configFrame.setShowComponentView(visible);

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
