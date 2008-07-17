package org.dwfa.bpa.worker.task;

import java.awt.event.ActionListener;

import org.dwfa.bpa.process.I_PluginToWorker;

public interface I_CompleteForm extends I_PluginToWorker {
  
  public void setCompleteActionListener(ActionListener l);

}
