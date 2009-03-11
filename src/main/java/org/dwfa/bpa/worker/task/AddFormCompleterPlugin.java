package org.dwfa.bpa.worker.task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.Timer;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/worker plugins", type = BeanType.TASK_BEAN) })
public class AddFormCompleterPlugin extends AbstractTask {
  private static final long serialVersionUID = 1;
  private static final int  dataVersion      = 1;

  private String        workerPropName         = ProcessAttachmentKeysForWorkerTasks.WORKER.getAttachmentKey();
  private int           completeDelay          = 120;

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeInt(dataVersion);
    out.writeObject(workerPropName);
    out.writeInt(completeDelay);
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    int objDataVersion = in.readInt();
    if (objDataVersion == 1) {
       this.workerPropName = (String) in.readObject();
       this.completeDelay = in.readInt();
   } else {
      throw new IOException("Can't handle dataversion: " + objDataVersion);
    }

  }
  
  private static class Completer implements I_CompleteForm, ActionListener {
    private int completeDelay;
    private ActionListener l;
    private I_Work worker;
    private Timer timer;
    
    private Completer(int completeDelay, I_Work worker) {
      super();
      this.completeDelay = completeDelay;
      this.worker = worker;
    }
    public void setCompleteActionListener(ActionListener l) {
      this.l = l;
      timer = new Timer(completeDelay * 1000, this);
      timer.setRepeats(false);
      timer.start();
    }
    
    public void actionPerformed(ActionEvent e) {
      ActionEvent evt = new ActionEvent(worker, 1, "complete");
      l.actionPerformed(evt);
    }
    
  }

  /**
   * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
   *      org.dwfa.bpa.process.I_Work)
   */
  public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
      throws TaskFailedException {
    try {
      I_Work workerToModify = (I_Work) process.readProperty(ProcessAttachmentKeysForWorkerTasks.WORKER.getAttachmentKey());
      workerToModify.setPluginForInterface(I_CompleteForm.class, new Completer(completeDelay, worker));
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

  public Integer getCompleteDelay() {
    return completeDelay;
  }

  public void setCompleteDelay(Integer completeDelay) {
    int oldCompleteDelay = this.completeDelay;
    this.completeDelay = completeDelay;
    this.firePropertyChange("visible", oldCompleteDelay, (int) completeDelay);
  }

  public String getWorkerPropName() {
    return workerPropName;
  }

  public void setWorkerPropName(String workerPropName) {
    this.workerPropName = workerPropName;
  }

}
