package org.dwfa.ace.task.wfpanel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;

public abstract class AbstractSetUserAndPwdPNC extends PreviousNextOrCancel {

	private static final long serialVersionUID = 1;
	private static final int dataVersion = 1;
	
	protected transient JTextField user;
	protected transient JTextField pwd;
	protected transient JLabel instruction;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			// nothing to read...
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process,
			final I_Work worker) throws TaskFailedException {
		try {
			setup(process);
			setupInput();
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					Component[] components = workflowPanel.getComponents();
					for (int i = 0; i < components.length; i++) {
						workflowPanel.remove(components[i]);
					}
					workflowPanel.setLayout(new GridBagLayout());
					GridBagConstraints c = new GridBagConstraints();
					c.fill = GridBagConstraints.BOTH;
					c.gridx = 0;
					c.gridy = 0;
					c.weightx = 1.0;
					c.weighty = 0;
					c.anchor = GridBagConstraints.WEST;
					workflowPanel.add(new JPanel(), c); // Filler
					c.gridx++;
					c.weightx = 0.0;
					workflowPanel.add(instruction, c);
					c.gridx++;
					c.weightx = 1.0;
					workflowPanel.add(user, c);
					c.gridx++;
					workflowPanel.add(pwd, c);
					c.weightx = 0.0;
					setupPreviousNextOrCancelButtons(workflowPanel, c);
				}
			});
			synchronized (this) {
				this.waitTillDone(worker.getLogger());
			}
			readInput();
			restore();
		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} catch (IntrospectionException e) {
			throw new TaskFailedException(e);
		} catch (IllegalAccessException e) {
			throw new TaskFailedException(e);
		}
		return returnCondition;
	}

	protected abstract void readInput();

	protected abstract void setupInput();


	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do

	}
}
