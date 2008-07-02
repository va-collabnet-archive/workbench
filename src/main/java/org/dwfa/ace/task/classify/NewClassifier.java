package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_SupportClassifier;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.SNOMED;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/classify", type = BeanType.TASK_BEAN) })
public class NewClassifier extends AbstractTask {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1)
            throws TaskFailedException {
        // nothing to do...
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

        try {
            final I_SnorocketFactory rocket = (I_SnorocketFactory) Class.forName(
                    "au.csiro.snorocket.ace.SnorocketFactory"
            ).newInstance();
            final I_TermFactory tf = (I_TermFactory) LocalVersionedTerminology.get();
            int isaId = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            rocket.setIsa(isaId);
            
            process.writeAttachment(ProcessKey.SNOROCKET.getAttachmentKey(), rocket);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
