package org.dwfa.ace.task.wfpanel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.swing.SwingWorker;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Instruct and wait Previous, Next, or Cancel...
 * 
 * @author kec
 * 
 */
@BeanList(specs = {
		@Spec(directory = "tasks/ace/instruct", type = BeanType.TASK_BEAN),
		@Spec(directory = "tasks/ace/wfpanel", type = BeanType.TASK_BEAN) })
public class InstructAndWaitPNC extends PreviousNextOrCancel {

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	private String instruction = "<html>Instruction";

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(instruction);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			instruction = (String) in.readObject();
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
			DoSwing swinger = new DoSwing(process);
			swinger.start();
			swinger.get();
			synchronized (this) {
				this.waitTillDone(worker.getLogger());
			}
			restore();
		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} catch (ExecutionException e) {
			throw new TaskFailedException(e);
		}
		return returnCondition;
	}

	private class DoSwing extends SwingWorker<Boolean> {
		
		I_EncodeBusinessProcess process;
		
		public DoSwing(I_EncodeBusinessProcess process) {
			super();
			this.process = process;
		}

		@Override
		protected Boolean construct() throws Exception {
			setup(process);
			return true;
		}

		@Override
		protected void finished() {
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
			workflowPanel.add(new JLabel(instruction), c);
			setupPreviousNextOrCancelButtons(workflowPanel, c);
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

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}
	
	@Override
	protected boolean showPrevious() {
		return true;
	}

}
