package org.dwfa.ace.task.wfpanel;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Clear the workflow panel...
 * 
 * @author kec
 * 
 */
@BeanList(specs = {
		@Spec(directory = "tasks/ide/instruct", type = BeanType.TASK_BEAN),
		@Spec(directory = "tasks/ide/wfpanel", type = BeanType.TASK_BEAN) })

public class ClearWorkflowPanel extends AbstractTask {
	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	
	private transient Exception ex = null;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			profilePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public String getProfilePropName() {
		return profilePropName;
	}

	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}


	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process,
			final I_Work worker) throws TaskFailedException {
		try {
			ex = null;
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					I_ConfigAceFrame config;
					try {
						config = (I_ConfigAceFrame) process.readProperty(getProfilePropName());
						JPanel workflowPanel = config.getWorkflowPanel();
						Component[] components = workflowPanel.getComponents();
						for (int i = 0; i < components.length; i++) {
							workflowPanel.remove(components[i]);
						}
						workflowPanel.setVisible(false);
					} catch (Exception e) {
						ex = e;
					} 
					
				}
			});
		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} 
		if (ex != null) {
			throw new TaskFailedException(ex);
		}
		return Condition.CONTINUE;
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do

	}
	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

}
