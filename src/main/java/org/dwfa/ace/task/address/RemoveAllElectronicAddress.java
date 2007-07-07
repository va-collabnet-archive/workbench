package org.dwfa.ace.task.address;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

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
 * Removes all electronic addresses from the viewer.
 * @author Christine Hill
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/address", type = BeanType.TASK_BEAN) })
public class RemoveAllElectronicAddress extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
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

            Collection<String> list = configFrame.getAddressesList();
            list.clear();

            list = configFrame.getAddressesList();
            if (worker.getLogger().isLoggable(Level.INFO)) {
                    worker.getLogger().info(("Number of addresses: "
                            + list.size()));
            }

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
}
