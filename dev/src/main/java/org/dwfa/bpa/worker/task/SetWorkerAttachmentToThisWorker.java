package org.dwfa.bpa.worker.task;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/worker plugins", type = BeanType.TASK_BEAN) })
public class SetWorkerAttachmentToThisWorker extends AbstractTask {
  private static final long serialVersionUID = 1;
  private static final int  dataVersion      = 1;
  
  private String workerPropName = ProcessAttachmentKeysForWorkerTasks.WORKER.getAttachmentKey();
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeInt(dataVersion);
    out.writeObject(workerPropName);
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    int objDataVersion = in.readInt();
    if (objDataVersion == 1) {
      workerPropName = (String) in.readObject();
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
       process.setProperty(workerPropName, worker);
    } catch (Exception e) {
      throw new TaskFailedException(e);
    } 
    return Condition.CONTINUE;
  }

  /**
   * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
   *      org.dwfa.bpa.process.I_Work)
   */
  public void complete(I_EncodeBusinessProcess process, I_Work worker)
      throws TaskFailedException {
    // nothing to do...

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

  public String getWorkerPropName() {
    return workerPropName;
  }

  public void setWorkerPropName(String workerPropName) {
    Object old = this.workerPropName;
    this.workerPropName = workerPropName;
    this.firePropertyChange("workerPropName", old, workerPropName);
  }

}

