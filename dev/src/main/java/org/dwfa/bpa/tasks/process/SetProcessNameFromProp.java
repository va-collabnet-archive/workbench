package org.dwfa.bpa.tasks.process;

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
 * Sets the process name to the contents of the specified property.
 * 
 * @todo Need tasks that will rename attachments or some other methods
 * to allow forms to pass in multiple encounter items, and retain them 
 * over time, such as when validating a sample id that was previously entered in a
 * different form. 
 * 
 * 
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/set tasks", type = BeanType.TASK_BEAN)})
public class SetProcessNameFromProp extends AbstractTask {

    private String newNameProp = "";
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(newNameProp);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            newNameProp = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    public SetProcessNameFromProp() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            Object newName = process.readProperty(newNameProp);
			process.setName(newName.toString());
		} catch (Exception e) {
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
    public String getNewNameProp() {
        return newNameProp;
    }

    /**
     * @param processTaskId The processTaskId to set.
     */
    public void setNewNameProp(String newName) {
        this.newNameProp = newName;
    }
}