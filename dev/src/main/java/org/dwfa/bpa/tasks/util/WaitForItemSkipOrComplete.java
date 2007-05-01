/*
 * Created on Mar 24, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.gui.InstructionPanel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.tasks.ws.PanelIds;


/**
 * @author kec
 *  
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/processes/workspace tasks", type = BeanType.TASK_BEAN)})
public class WaitForItemSkipOrComplete extends AbstractTask {
	private transient Exception ex;

	private transient Condition exitCondition;

	private transient ActionListener skipActionListener;

	private transient ActionListener completeActionListener;
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }


	private class SkipActionListener implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			exitCondition = Condition.ITEM_SKIPPED;
            synchronized(WaitForItemSkipOrComplete.this) {
                WaitForItemSkipOrComplete.this.notifyAll();
            }

		}

	}

	private class CompleteActionListener implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			exitCondition = Condition.ITEM_COMPLETE;
            synchronized(WaitForItemSkipOrComplete.this) {
            	    WaitForItemSkipOrComplete.this.notifyAll();
            }

		}

	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public synchronized Condition evaluate(final I_EncodeBusinessProcess process,
			final I_Work worker) throws TaskFailedException {
		ex = null;
        this.exitCondition = null;
		this.skipActionListener = new SkipActionListener();
		this.completeActionListener = new CompleteActionListener();
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					try {
						I_Workspace workspace = worker.getCurrentWorkspace();
						InstructionPanel instructionPanel = (InstructionPanel) workspace
								.getPanel(PanelIds.INSTRUCTION);
						instructionPanel
								.addSkipActionListener(skipActionListener);
						instructionPanel
								.addCompleteActionListener(completeActionListener);
						instructionPanel.setSkipEnabled(true);
						instructionPanel.setCompleteEnabled(true);
					} catch (Exception e) {
						ex = e;
					}
				}

			});
			if (ex != null) {
				throw new TaskFailedException(ex);
			}
			this.waitTillDone();
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					try {
						I_Workspace workspace = worker.getCurrentWorkspace();
						InstructionPanel instructionPanel = (InstructionPanel) workspace
								.getPanel(PanelIds.INSTRUCTION);
						instructionPanel
								.removeSkipActionListener(skipActionListener);
						instructionPanel
								.removeCompleteActionListener(completeActionListener);
						instructionPanel.setSkipEnabled(false);
						instructionPanel.setCompleteEnabled(false);
					} catch (Exception e) {
						ex = e;
					}
				}

			});
			if (ex != null) {
				throw new TaskFailedException(ex);
			}

		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			throw new TaskFailedException(e1);
		}

		return this.exitCondition;
	}

	private void waitTillDone() {
		while (!this.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }

	}

	public boolean isDone() {
		return this.exitCondition != null;
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		//nothing to do

	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return AbstractTask.ITEM_SKIPPED_OR_COMPLETE;
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[0];
	}

}