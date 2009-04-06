package org.dwfa.ace.task.classify;

import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Show progress
 * 
 * @author emays
 * 
 */

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class Progress extends AbstractTask {

	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private String progress = "0";

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(progress);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion >= 1) {
			progress = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	private static JProgressBar progressMonitor = null;

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {

		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
							.name());
			System.out.println(">>>Progress is: " + progress);
			final JPanel panel = config.getWorkflowPanel();
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					public void run() {
						int progress = 0;
						try {
							progress = Integer.parseInt(Progress.this.progress);
						} catch (NumberFormatException ex) {
						}
						progress = Math.max(progress, 0);
						progress = Math.min(progress, 100);
						if (progressMonitor == null || progress == 0) {
							progressMonitor = new JProgressBar(0, 100);
							// progressMonitor.setIndeterminate(true);
							progressMonitor.setString("Classify");
							progressMonitor.setStringPainted(true);
							progressMonitor.setBorderPainted(true);
							panel.add(progressMonitor);
						}
						progressMonitor.setValue(progress);
						if (progress == 100) {
							Component[] components = panel.getComponents();
							for (int i = 0; i < components.length; i++) {
								panel.remove(components[i]);
							}
						}
						Container cont = panel;
						while (cont != null) {
							cont.validate();
							cont = cont.getParent();
						}
					}
				});
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}

		} catch (Exception e) {
			throw new TaskFailedException(e);
		}

		return Condition.CONTINUE;
	}

	public void complete(I_EncodeBusinessProcess arg0, I_Work arg1)
			throws TaskFailedException {
		// nothing to do...
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

}
