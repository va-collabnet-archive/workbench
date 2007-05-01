/*
 * Created on Feb 18, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.process;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Stack;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

/**
 * Launch a process that is represented as a task within the enclosing process. 
 * <p>
 * The GUI editor will allow tasks that implement the I_EncodeBusinessProcess interface
 * to be dragged onto the process id field. 
 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/processes/start tasks", type = BeanType.TASK_BEAN)})
public class LaunchProcess extends AbstractTask {

    private int processTaskId = -1;
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(processTaskId);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            processTaskId = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    public LaunchProcess() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        I_EncodeBusinessProcess processToLaunch = (I_EncodeBusinessProcess) process.getTask(processTaskId);
        Stack<I_EncodeBusinessProcess> ps = worker.getProcessStack();
        worker.setProcessStack(new Stack<I_EncodeBusinessProcess>());
        processToLaunch.execute(worker);
        worker.setProcessStack(ps);
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
    public int getProcessTaskId() {
        return processTaskId;
    }

    /**
     * @param processTaskId The processTaskId to set.
     */
    public void setProcessTaskId(int processTaskId) {
        this.processTaskId = processTaskId;
    }

}
