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

@BeanList(specs = 
{ @Spec(directory = "tasks/set tasks", type = BeanType.TASK_BEAN)})
public class SetProcessOriginFromProp extends AbstractTask {

    private String newOriginProp = "";
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(newOriginProp);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            newOriginProp = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    public SetProcessOriginFromProp() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		try {
			Object origin = process.readProperty(newOriginProp);
			process.setOriginator(origin.toString());
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
    public String getNewOriginProp() {
        return newOriginProp;
    }

    /**
     * @param processTaskId The processTaskId to set.
     */
    public void setNewOriginProp(String newName) {
        this.newOriginProp = newName;
    }
}
