package org.dwfa.ace.task.address;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
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
 * Adds an electronic address to the viewer.
 * @author Christine Hill
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/ace/address", type = BeanType.TASK_BEAN) })
public class AddElectronicAddress extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String address;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(address);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            address = (String) in.readObject();
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
            if (address == null) {
                throw new TaskFailedException("Electronic address is null.");
            }
            I_ConfigAceFrame configFrame = (I_ConfigAceFrame) worker
                .readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            List<String> list = configFrame.getAddressesList();
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                if (worker.getLogger().isLoggable(Level.INFO)) {
                    worker.getLogger().info(("Before adding new address: "
                            + iterator.next()));
                }
            }
            configFrame.getAddressesList().add(address);
            list = configFrame.getAddressesList();
            iterator = list.iterator();
            while (iterator.hasNext()) {
                if (worker.getLogger().isLoggable(Level.INFO)) {
                    worker.getLogger().info(("After adding new address:"
                            + iterator.next()));
                }
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
