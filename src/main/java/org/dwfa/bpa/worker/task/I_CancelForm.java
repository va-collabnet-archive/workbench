package org.dwfa.bpa.worker.task;

import java.awt.event.ActionListener;

import org.dwfa.bpa.process.I_PluginToWorker;

public interface I_CancelForm extends I_PluginToWorker {

  public void setCancelActionListener(ActionListener l);

}
