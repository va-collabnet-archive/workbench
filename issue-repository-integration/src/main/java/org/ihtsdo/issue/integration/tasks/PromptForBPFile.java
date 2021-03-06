/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.issue.integration.tasks;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The Class PromptForBPFile.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class PromptForBPFile extends AbstractTask {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	/** The Constant dataVersion. */
	private static final int dataVersion = 1;

	/** The instruction. */
	private String instruction = "<html>Instruction";

	/** The return condition. */
	private transient Condition returnCondition;

	/** The done. */
	private transient boolean done;

	/** The Constant bpAttachmentKey. */
	private final static String bpAttachmentKey = "bpAttachmentKey";

	/** The bp file. */
	private static File bpFile;
	
	/** The bp file name label. */
	private static JLabel bpFileNameLabel;
	
	/** The business process. */
	BusinessProcess businessProcess = null;

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	/**
	 * Read object.
	 * 
	 * @param in the in
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new prompt for bp file.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public PromptForBPFile() throws MalformedURLException {
		super();
	}

	/**
	 * The listener interface for receiving stepAction events.
	 * The class that is interested in processing a stepAction
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addStepActionListener<code> method. When
	 * the stepAction event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see StepActionEvent
	 */
	private class StepActionListener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (businessProcess != null) {
				returnCondition = Condition.ITEM_COMPLETE;
				done = true;
				synchronized (PromptForBPFile.this) {
					PromptForBPFile.this.notifyAll();
				}
			} 
		}

	}

	/**
	 * The listener interface for receiving stopAction events.
	 * The class that is interested in processing a stopAction
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addStopActionListener<code> method. When
	 * the stopAction event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see StopActionEvent
	 */
	private class StopActionListener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			returnCondition = Condition.ITEM_CANCELED;
			done = true;
			synchronized (PromptForBPFile.this) {
				PromptForBPFile.this.notifyAll();
			}

		}

	}

	/**
	 * Wait till done.
	 * 
	 * @param l the l
	 */
	private void waitTillDone(Logger l) {
		while (!this.isDone()) {
			try {
				wait();
			} catch (InterruptedException e) {
				l.log(Level.SEVERE, e.getMessage(), e);
			}
		}

	}

	/**
	 * Checks if is done.
	 * 
	 * @return true, if is done
	 */
	public boolean isDone() {
		return this.done;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {
		I_ConfigAceFrame config = (I_ConfigAceFrame) worker
		.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

		this.done = false;
		boolean builderVisible = config.isBuilderToggleVisible();
		config.setBuilderToggleVisible(false);
		boolean subversionButtonVisible = config.isSubversionToggleVisible();
		config.setSubversionToggleVisible(false);
		boolean inboxButtonVisible = config.isInboxToggleVisible();
		config.setInboxToggleVisible(false);
		try {
			final JPanel workflowPanel = config.getWorkflowPanel();
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
					//c.weightx = 0.0;

					final JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setDialogTitle("Select BusinessProcess to attach...");

					class BPFilter extends javax.swing.filechooser.FileFilter {
						public boolean accept(File file) {
							String filename = file.getName();
							return filename.endsWith(".bp");
						}
						public String getDescription() {
							return "*.bp";
						}
					}

					fileChooser.addChoosableFileFilter(new BPFilter());

					JButton chooseFileButton = new JButton("Choose BusinessProcess to attach");
					workflowPanel.add(chooseFileButton, c);

					chooseFileButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								int returnValue = fileChooser
								.showDialog(new Frame(), "Choose BP file");
								if (returnValue == JFileChooser.APPROVE_OPTION) {
									bpFile = fileChooser.getSelectedFile();
									bpFileNameLabel.setText(bpFile.getPath());
								} else {
									throw new TaskFailedException("User failed to select a file.");
								}
								InputStream file = new FileInputStream(bpFile);
								InputStream buffer = new BufferedInputStream(file);
								ObjectInput input = new ObjectInputStream (buffer);
								businessProcess = (BusinessProcess) input.readObject();
							} catch (FileNotFoundException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							} catch (ClassNotFoundException e1) {
								e1.printStackTrace();
							} catch (TaskFailedException e1) {
								e1.printStackTrace();
							}
						}
					});

					c.gridx++;
					bpFileNameLabel = new JLabel("No file selected....");
					workflowPanel.add(bpFileNameLabel, c);
					c.gridx++;
					c.anchor = GridBagConstraints.SOUTHWEST;
					final JButton stepButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getTrueImage())));
					stepButton.setToolTipText("Step");
					workflowPanel.add(stepButton, c);
					c.gridx++;
					stepButton.addActionListener(new StepActionListener());
					final JButton stopButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getFalseImage())));
					stopButton.addActionListener(new StopActionListener());
					stopButton.setToolTipText("Cancel");
					workflowPanel.add(stopButton, c);
					c.gridx++;
					workflowPanel.add(new JLabel("  "), c);
					workflowPanel.validate();
					Container cont = workflowPanel;

					while (cont != null) {
						cont.validate();
						cont = cont.getParent();
					}
					workflowPanel.setVisible(true);
					workflowPanel.repaint();
					stepButton.requestFocusInWindow();
				}
			});
			synchronized (this) {
				this.waitTillDone(worker.getLogger());
			}
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					Component[] components = workflowPanel.getComponents();
					for (int i = 0; i < components.length; i++) {
						workflowPanel.remove(components[i]);
					}
					workflowPanel.validate();
					Container cont = workflowPanel;

					while (cont != null) {
						cont.validate();
						cont = cont.getParent();
					}
					workflowPanel.repaint();
					workflowPanel.setVisible(false);
				}

			});

			config.setProperty(bpAttachmentKey, bpFile);

		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		config.setBuilderToggleVisible(builderVisible);
		config.setSubversionToggleVisible(subversionButtonVisible);
		config.setInboxToggleVisible(inboxButtonVisible);

		return returnCondition;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
	throws TaskFailedException {

	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] {  };
	}

	/**
	 * Gets the true image.
	 * 
	 * @return the true image
	 */
	protected String getTrueImage() {
		return "/16x16/plain/media_step_forward.png";
	}

	/**
	 * Gets the back image.
	 * 
	 * @return the back image
	 */
	protected String getBackImage() {
		return "/16x16/plain/media_step_back.png";
	}

	/**
	 * Gets the false image.
	 * 
	 * @return the false image
	 */
	protected String getFalseImage() {
		return "/16x16/plain/media_stop_red.png";
	}

	/**
	 * Gets the instruction.
	 * 
	 * @return the instruction
	 */
	public String getInstruction() {
		return instruction;
	}

	/**
	 * Sets the instruction.
	 * 
	 * @param instruction the new instruction
	 */
	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

}