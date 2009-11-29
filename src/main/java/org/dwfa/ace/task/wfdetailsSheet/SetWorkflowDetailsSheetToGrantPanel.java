package org.dwfa.ace.task.wfdetailsSheet;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.grant.GrantPanel;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.profile.EditOnPromotePath;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/gui/workflow/detail sheet", type = BeanType.TASK_BEAN) })
public class SetWorkflowDetailsSheetToGrantPanel extends AbstractTask {
	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	private String commitProfilePropName = ProcessAttachmentKeys.COMMIT_PROFILE.getAttachmentKey();
	
	private transient Exception ex = null;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
		out.writeObject(commitProfilePropName);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			profilePropName = (String) in.readObject();
			commitProfilePropName = (String) in.readObject();
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
			if (SwingUtilities.isEventDispatchThread()) {
				doRun(process, worker);
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {

					public void run() {
						doRun(process, worker); 
					}

				});
			}
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

	private void doRun(final I_EncodeBusinessProcess process,
			final I_Work worker) {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
			I_ConfigAceFrame commitConfig = (I_ConfigAceFrame) process.getProperty(commitProfilePropName);
			ClearWorkflowDetailsSheet clear = new ClearWorkflowDetailsSheet();
			clear.setProfilePropName(getProfilePropName());
			clear.evaluate(process, worker);
			JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
	        int width = 400;
	        int height = 500;
	        workflowDetailsSheet.setSize(width, height);
	        workflowDetailsSheet.setLayout(new GridLayout(1, 1));
			workflowDetailsSheet.add(new GrantPanel(config, new EditOnPromotePath(commitConfig)));
		} catch (Exception e) {
			ex = e;
		}
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

	public String getCommitProfilePropName() {
		return commitProfilePropName;
	}

	public void setCommitProfilePropName(String commitProfilePropName) {
		this.commitProfilePropName = commitProfilePropName;
	}
}