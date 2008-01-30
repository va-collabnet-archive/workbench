package org.dwfa.ace.task.ebr;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/ebr", type = BeanType.TASK_BEAN) })
public class CountExtensionsInRefset extends AbstractTask {


    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;
    
    private TermEntry refsetTermEntry = new TermEntry(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(refsetTermEntry);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
                                                                 ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            refsetTermEntry = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
    throws TaskFailedException {
        try {
            List<I_ThinExtByRefVersioned> extensions = LocalVersionedTerminology.get().getRefsetExtensionMembers(refsetTermEntry.getLocalConcept().getNid());
            worker.getLogger().info("Counted " + extensions.size() + " extensions for " + refsetTermEntry.getLocalDesc().getText() + ".");
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

    }


    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
        throws TaskFailedException {
        // Nothing to do.

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

    public TermEntry getRefsetTermEntry() {
        return refsetTermEntry;
    }

    public void setRefsetTermEntry(TermEntry refsetTermEntry) {
        this.refsetTermEntry = refsetTermEntry;
    }


}
