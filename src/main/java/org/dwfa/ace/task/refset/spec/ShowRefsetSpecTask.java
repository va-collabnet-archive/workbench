package org.dwfa.ace.task.refset.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Shows the refset spec in the refset spec panel.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class ShowRefsetSpecTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private I_TermFactory termFactory;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {

            termFactory = LocalVersionedTerminology.get();

            Object obj = process.readProperty(ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey());
            UUID uuid = null;
            if (obj == null) {
                uuid = null;
            } else {
                uuid = (UUID) obj;
            }
            if (uuid != null) {
                I_GetConceptData refset = termFactory.getConcept(new UUID[] { uuid });

                // set new spec as focus
                termFactory.getActiveAceFrameConfig().setRefsetInSpecEditor(refset);
                termFactory.getActiveAceFrameConfig().setShowQueueViewer(false);
                termFactory.getActiveAceFrameConfig().showRefsetSpecPanel();

                int count = 0;
                while (termFactory.getActiveAceFrameConfig().getRefsetInSpecEditor() == null && count < 10) {
                    Thread.sleep(1000);
                    count++;
                }
                while (termFactory.getActiveAceFrameConfig().getRefsetSpecInSpecEditor() == null && count < 10) {
                    Thread.sleep(1000);
                    count++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Condition.CONTINUE;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }
}