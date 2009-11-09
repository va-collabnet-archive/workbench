package org.dwfa.ace.task.refset.refresh;

import java.awt.Component;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;

import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import org.dwfa.ace.task.refset.refresh.PanelRefsetVersion;

/**
 * Gets the selected Refset Version data from the WF Details panel.
 * 
 * @author Perry Reid
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class GetRefsetVersionPanelDataTask extends AbstractTask {
	private static final long serialVersionUID = 1;

	private static final int dataVersion = 2;

	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	private String positionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
		out.writeObject(positionSetPropName);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
			profilePropName = (String) in.readObject();
			if (objDataVersion >= 2) {
				positionSetPropName = (String) in.readObject();
			} else {
				positionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
			}
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
			I_ConfigAceFrame config = (I_ConfigAceFrame) process.readProperty(getProfilePropName());
			JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
			for (Component c: workflowDetailsSheet.getComponents()) {
				if (PanelRefsetVersion.class.isAssignableFrom(c.getClass())) {
					PanelRefsetVersion panel = (PanelRefsetVersion) c;
					Set<I_Position> positionSet = panel.getPositionSet();
					process.setProperty(positionSetPropName, positionSet);
					return Condition.CONTINUE;
				}
			}
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		} 
		throw new TaskFailedException("Cannot find PanelRefsetVersion.");
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

	public String getPositionSetPropName() {
		return positionSetPropName;
	}

	public void setPositionSetPropName(String positionSetPropName) {
		this.positionSetPropName = positionSetPropName;
	}

}
