package org.dwfa.ace.task.classify;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

/**
 * Returns a new classifier extension instance keyed off filename.
 *   
 * @author law223
 *
 */
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
    private static volatile String stateName = null;
    private static volatile I_SnorocketFactory rocket = null;
    private static volatile I_SnorocketFactory newRocket = null;

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
        new Thread(new Runnable() {
            public void run() {
                try {
                    getRocket();
                } catch (TaskFailedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1)
            throws TaskFailedException {
        // nothing to do...
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

        try {
            final I_SnorocketFactory rocket = getRocket();
            
            if (null == rocket) {
                return Condition.FALSE;
            }
            
            process.writeAttachment(ProcessKey.SNOROCKET.getAttachmentKey(), rocket);
        } catch (RuntimeException e) {
            throw new TaskFailedException(e);
        }

        return Condition.TRUE;
    }

    private I_SnorocketFactory getRocket() throws TaskFailedException {
        I_SnorocketFactory result;
        synchronized (LOCK) {
            if (null == rocket || !getFileName().equals(stateName)) {
                InputStream is;
                try {
                    stateName = getFileName();
                    is = new FileInputStream(stateName);
                    rocket = (I_SnorocketFactory) Class.forName(
                            "au.csiro.snorocket.ace.SnorocketFactory"
                    ).getConstructor(InputStream.class).newInstance(is);
                } catch (FileNotFoundException e) {
                    return null;
                } catch (Exception e) {
                    throw new TaskFailedException(e);
                }
            }
            result = null == newRocket ? rocket.createExtension() : newRocket;
            newRocket = null;	// ensure we don't attempt to use this instance more than once
            new Thread(new Runnable() {
                public void run() {
                    newRocket = rocket.createExtension();
                }
            }).start();
        }
        return result;
    }

    public Collection<Condition> getConditions() {
        return CONDITIONAL_TEST_CONDITIONS;
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
