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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.integration.panels.RefsetSpecReviewDetailPanel;
import org.ihtsdo.issue.integration.util.IssueAssignmentsUtil;

/**
 * The Class GetSMEReviewDetails.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class GetSMEReviewDetails extends AbstractTask {

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

	/** The bp attachment key. */
	private String bpAttachmentKey = "bpAttachmentKey";
	
	/** The refset uid key. */
	private String refsetUidKey = "refsetUidKey";
	
	/** The issue key. */
	private String issueKey = "issueKey";

	/** The send to review details panel. */
	private RefsetSpecReviewDetailPanel sendToReviewDetailsPanel;

	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The worker. */
	private I_Work worker;

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
	 * Instantiates a new gets the sme review details.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public GetSMEReviewDetails() throws MalformedURLException {
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
			if (sendToReviewDetailsPanel.getBusinessProcess() != null &&
					sendToReviewDetailsPanel.getSMEName() != null &&
					sendToReviewDetailsPanel.getComment() != null) {
				createAndDeliverIssue();
				returnCondition = Condition.ITEM_COMPLETE;
				done = true;
				synchronized (GetSMEReviewDetails.this) {
					GetSMEReviewDetails.this.notifyAll();
				}
			} else {
				config.setStatusMessage("Missing data, can't continue...");
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
			synchronized (GetSMEReviewDetails.this) {
				GetSMEReviewDetails.this.notifyAll();
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
		this.worker = worker;
		config = (I_ConfigAceFrame) worker
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
			final JPanel workflowDetailPanel = config.getWorkflowDetailsSheet();
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
					final JLabel nameLabel = new JLabel("Preparing to send Refset for SME review");
					workflowPanel.add(nameLabel, c);
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

					components = workflowDetailPanel.getComponents();
					for (int i = 0; i < components.length; i++) {
						workflowDetailPanel.remove(components[i]);
					}
					workflowDetailPanel.setLayout(new GridBagLayout());
					int width = 475;
					int height = 390;
					workflowDetailPanel.setSize(width, height);
					c = new GridBagConstraints();
					c.fill = GridBagConstraints.BOTH;
					c.gridx = 0;
					c.gridy = 0;
					c.weightx = 1.0;
					c.weighty = 0;
					c.anchor = GridBagConstraints.WEST;

					sendToReviewDetailsPanel = new RefsetSpecReviewDetailPanel(config);

					workflowDetailPanel.add(sendToReviewDetailsPanel, c);
					workflowDetailPanel.setVisible(true);
					workflowDetailPanel.repaint();
					config.setShowWorkflowDetailSheet(true);

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

					components = workflowDetailPanel.getComponents();
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
					workflowDetailPanel.repaint();
					workflowPanel.setVisible(false);
					config.setShowWorkflowDetailSheet(false);
				}

			});


		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		}
		
		process.writeAttachment("REFSET_UUID", UUID.fromString(sendToReviewDetailsPanel.getRefsetUUId()));
		
		config.setBuilderToggleVisible(builderVisible);
		config.setSubversionToggleVisible(subversionButtonVisible);
		config.setInboxToggleVisible(inboxButtonVisible);

		return returnCondition;
	}

	/**
	 * Creates the and deliver issue.
	 */
	private void createAndDeliverIssue() {
		try {
			Issue issueForReview = new Issue();
			issueForReview.setComponentId(sendToReviewDetailsPanel.getRefsetUUId().toString());
			issueForReview.setWorkflowStatus("Out for SME review");
			issueForReview.setDownloadStatus("Ready to download");
			issueForReview.setExternalUser(sendToReviewDetailsPanel.getSMEName());
			issueForReview.setPriority("3");
			issueForReview.setDescription(sendToReviewDetailsPanel.getComment());
			issueForReview.setTitle("Refset Spec Review : " + sendToReviewDetailsPanel.getRefsetName());
			issueForReview.setRepositoryUUId((UUID) config.getDbConfig().getProperties().get("SMERepositoryUUID"));

			BusinessProcess reviewBp = sendToReviewDetailsPanel.getBusinessProcess();
			reviewBp.setDestination(sendToReviewDetailsPanel.getSMEName());
			reviewBp.setSubject("Refset Spec Review : " + sendToReviewDetailsPanel.getRefsetName());
			reviewBp.writeAttachment("MESSAGE", "<html><h2>" + "Refset Spec Review : " + 
					sendToReviewDetailsPanel.getRefsetName() +
					"</h2><body>" +
					sendToReviewDetailsPanel.getComment() + "</body></html>");
			reviewBp.writeAttachment("issueKey", issueForReview);
			reviewBp.writeAttachment("REFSET_UUID", UUID.fromString(sendToReviewDetailsPanel.getRefsetUUId()));
			reviewBp.setOriginator(config.getUsername().trim());

			HashMap<String,Object> map = new HashMap<String,Object>();
			map.put(bpAttachmentKey, reviewBp);
			issueForReview.setFieldMap(map);
			
			// TODO: add SME
			IssueAssignmentsUtil.deliverAssignmentToNamedQueue(reviewBp, 
					config.getUsername().trim() + ".outbox", worker);

			message("<html>Refset Spec review process initiated.<br>Assignment sent to outbox.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Message.
	 * 
	 * @param string the string
	 */
	private void message(String string) {
		JOptionPane.showOptionDialog(   
				null,   
				string,   
				"Information", JOptionPane.DEFAULT_OPTION,   
				JOptionPane.INFORMATION_MESSAGE, null, null,   
				null );   
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