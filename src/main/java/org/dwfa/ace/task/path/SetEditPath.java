package org.dwfa.ace.task.path;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/path", type = BeanType.TASK_BEAN) })
public class SetEditPath extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
     private TermEntry editPathEntry = new TermEntry(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());

     private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(editPathEntry);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            editPathEntry = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            I_TermFactory tf = LocalVersionedTerminology.get();
            I_Path editPath = tf.getPath(editPathEntry.ids);
            I_ConfigAceFrame frameConfig = tf.getActiveAceFrameConfig();
            Set<I_Path> editSet = frameConfig.getEditingPathSet();
            editSet.clear();
            frameConfig.addEditingPath(editPath);

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public TermEntry getEditPathEntry() {
        return editPathEntry;
    }

    public void setEditPathEntry(TermEntry editPathEntry) {
        this.editPathEntry = editPathEntry;
    }

}
