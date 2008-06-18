package org.dwfa.ace.task.classify;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

@BeanList(specs = { @Spec(directory = "tasks/ace/classify", type = BeanType.TASK_BEAN) })
public class GetBaseClassification extends AbstractTask {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    final private static Object LOCK = new Object();
    private static I_SnorocketFactory rocket = null;

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
            I_SnorocketFactory rocket = getRocket();
            
            process.writeAttachment(ProcessKey.SNOROCKET.getAttachmentKey(), rocket.createExtension());
        } catch (RuntimeException e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    private static I_SnorocketFactory getRocket() throws TaskFailedException {
        synchronized (LOCK) {
            if (null == rocket) {
                InputStream is;
                try {
                    is = new FileInputStream("baseState.txt");
                    rocket = (I_SnorocketFactory) Class.forName(
                            "au.csiro.snorocket.ace.SnorocketFactory"
                    ).getConstructor(InputStream.class).newInstance(is);
                } catch (Exception e) {
                    throw new TaskFailedException(e);
                }
            }
        }
        return rocket;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
