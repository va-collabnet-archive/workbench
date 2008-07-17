package org.dwfa.bpa.worker.task;

public enum ProcessAttachmentKeysForWorkerTasks {
  
  WORKER;

  public String getAttachmentKey() {
    return "A: " + this.name();
}

}
