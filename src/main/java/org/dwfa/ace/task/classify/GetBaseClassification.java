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

    private static final int dataVersion = 2;

    /**
     * Bean property
     */
    private String fileName = "baseState.txt";

    final private static Object LOCK = new Object();
    private static String stateName = null;
    private static I_SnorocketFactory rocket = null;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(getFileName());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int objDataVersion = in.readInt();

        if (objDataVersion > 1) {
            setFileName((String) in.readObject());
        }
        if (objDataVersion > dataVersion) {
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
            I_SnorocketFactory rocket = getRocket().createExtension();
            
            process.writeAttachment(ProcessKey.SNOROCKET.getAttachmentKey(), rocket);
        } catch (RuntimeException e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    private I_SnorocketFactory getRocket() throws TaskFailedException {
        synchronized (LOCK) {
            if (null == rocket || !getFileName().equals(stateName)) {
                InputStream is;
                try {
                    stateName = getFileName();
                    is = new FileInputStream(stateName);
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

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
