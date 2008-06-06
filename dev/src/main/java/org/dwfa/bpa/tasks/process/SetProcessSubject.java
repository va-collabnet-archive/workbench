package org.dwfa.bpa.tasks.process;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
/**
 * @author kec<p>
 * Sets the tasks subject to the text specified in the task.
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/set tasks", type = BeanType.TASK_BEAN)})
public class SetProcessSubject extends AbstractTask {

    private String newSubject = "newSubject";
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(newSubject);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            newSubject = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    public SetProcessSubject() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
			process.setSubject(newSubject);
		} catch (PropertyVetoException e) {
			throw new TaskFailedException(e);
		}
        return Condition.CONTINUE;
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do...
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {  };
    }

    /**
     * @return Returns the processTaskId.
     */
    public String getNewSubject() {
        return newSubject;
    }

    /**
     * @param processTaskId The processTaskId to set.
     */
    public void setNewSubject(String newName) {
        this.newSubject = newName;
    }
}