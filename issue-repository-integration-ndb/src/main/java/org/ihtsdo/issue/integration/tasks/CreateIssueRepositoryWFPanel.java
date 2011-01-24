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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class CreateIssueRepositoryWFPanel.
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/ide/issues", type = BeanType.TASK_BEAN)})
public class CreateIssueRepositoryWFPanel extends AbstractTask {

	/** The repository id. */
	private String repositoryId;

	/** The repository url. */
	private String repositoryUrl;

	/** The repository name. */
	private String repositoryName;

	/** The repository type. */
	private String repositoryType;

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
	
	/** The repository name field. */
	private static JTextField repositoryNameField;
	
	/** The repository url field. */
	private static JTextField repositoryURLField;
	
	/** The repository id field. */
	private static JTextField repositoryIDField;
	
	/** The repository type combo box. */
	private static JComboBox repositoryTypeComboBox;

	/**
	 * Write object.
	 * 
	 * @param out the out
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(repositoryId);
		out.writeObject(repositoryUrl);
		out.writeObject(repositoryName);
		out.writeObject(repositoryType);
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
			repositoryId = (String) in.readObject();
			repositoryUrl = (String) in.readObject();
			repositoryName = (String) in.readObject();
			repositoryType = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);   
		}

	}
	
	/**
	 * Instantiates a new creates the issue repository wf panel.
	 * 
	 * @throws MalformedURLException the malformed url exception
	 */
	public CreateIssueRepositoryWFPanel() throws MalformedURLException {
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
			returnCondition = Condition.ITEM_COMPLETE;
			done = true;
			synchronized (CreateIssueRepositoryWFPanel.this) {
				CreateIssueRepositoryWFPanel.this.notifyAll();
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
			synchronized (CreateIssueRepositoryWFPanel.this) {
				CreateIssueRepositoryWFPanel.this.notifyAll();
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
					final JLabel nameLabel = new JLabel("Name");
					workflowPanel.add(nameLabel, c);
					c.gridx++;
					CreateIssueRepositoryWFPanel.repositoryNameField = new JTextField(20);
					workflowPanel.add(repositoryNameField, c);
					c.gridx++;
					final JLabel urlLabel = new JLabel("URL");
					workflowPanel.add(urlLabel, c);
					c.gridx++;
					repositoryURLField = new JTextField(20);
					workflowPanel.add(repositoryURLField, c);
					c.gridx++;
					c.anchor = GridBagConstraints.SOUTHWEST;
					final JButton firstStepButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getTrueImage())));
					firstStepButton.setToolTipText("Step");
					workflowPanel.add(firstStepButton, c);
					
					c.gridy++;
					c.gridx= 0;
					final JLabel idLabel = new JLabel("ID");
					idLabel.setVisible(false);
					workflowPanel.add(idLabel, c);
					c.gridx++;
					repositoryIDField = new JTextField(10);
					repositoryIDField.setVisible(false);
					workflowPanel.add(repositoryIDField, c);
					c.gridx++;
					String[] types = { "WEB_SITE", "CONCEPT_EXTENSION" };
					final JLabel typeLabel = new JLabel("Type");
					typeLabel.setVisible(false);
					workflowPanel.add(typeLabel, c);
					c.gridx++;
					repositoryTypeComboBox = new JComboBox(types);
					repositoryTypeComboBox.setVisible(false);
					workflowPanel.add(repositoryTypeComboBox, c);
					c.gridx++;
					final JLabel spaceLabel = new JLabel("  ");
					spaceLabel.setVisible(false);
					workflowPanel.add(spaceLabel, c);
					c.gridx++;
					c.anchor = GridBagConstraints.SOUTHWEST;
					final JButton stepButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getTrueImage())));
					stepButton.setToolTipText("Step");
					stepButton.setVisible(false);
					workflowPanel.add(stepButton, c);
					c.gridx++;
					stepButton.addActionListener(new StepActionListener());
					final JButton stopButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getFalseImage())));
					stopButton.addActionListener(new StopActionListener());
					stopButton.setToolTipText("Cancel");
					stopButton.setVisible(false);
					workflowPanel.add(stopButton, c);
					c.gridx++;
					final JButton backButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getBackImage())));
					backButton.setToolTipText("Cancel");
					backButton.setVisible(false);
					workflowPanel.add(backButton, c);
					c.gridx++;
					workflowPanel.add(new JLabel("  "), c);
					workflowPanel.validate();
					Container cont = workflowPanel;
					
					firstStepButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							firstStepButton.setVisible(false);
							repositoryURLField.setVisible(false);
							urlLabel.setVisible(false);
							repositoryNameField.setVisible(false);
							nameLabel.setVisible(false);
							idLabel.setVisible(true);
							repositoryIDField.setVisible(true);
							typeLabel.setVisible(true);
							repositoryTypeComboBox.setVisible(true);
							spaceLabel.setVisible(true);
							stepButton.setVisible(true);
							stopButton.setVisible(true);
							backButton.setVisible(true);
						   }
					});
					backButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							firstStepButton.setVisible(true);
							repositoryURLField.setVisible(true);
							urlLabel.setVisible(true);
							repositoryNameField.setVisible(true);
							nameLabel.setVisible(true);
							idLabel.setVisible(false);
							repositoryIDField.setVisible(false);
							typeLabel.setVisible(false);
							repositoryTypeComboBox.setVisible(false);
							spaceLabel.setVisible(false);
							stepButton.setVisible(false);
							stopButton.setVisible(false);
							backButton.setVisible(false);
						}
					});
					
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
		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		}
		config.setBuilderToggleVisible(builderVisible);
		config.setSubversionToggleVisible(subversionButtonVisible);
		config.setInboxToggleVisible(inboxButtonVisible);
		
		repositoryId = repositoryIDField.getText();
		repositoryUrl = repositoryURLField.getText();
		repositoryName = repositoryNameField.getText();
		repositoryType = (String) repositoryTypeComboBox.getSelectedItem();

		if (repositoryId == null) {
			// get from attachment
			repositoryId = (String) process.readAttachement("repositoryIdKey");
		}
		
		if (repositoryId == null) {
			throw new TaskFailedException("Missing repository Id");
		}

		if (repositoryUrl == null) {
			// get from attachment
			repositoryUrl = (String) process.readAttachement("repositoryUrlKey");
		}
		
		if (repositoryUrl == null) {
			throw new TaskFailedException("Missing repository URL");
		}

		if (repositoryName == null) {
			// get from attachment
			repositoryName = (String) process.readAttachement("repositoryNameKey");
		}
		
		if (repositoryName == null) {
			throw new TaskFailedException("Missing repository Name");
		}

		if (repositoryType == null) {
			// get from attachment
			repositoryType = (String) process.readAttachement("repositoryTypeKey");
		}
		
		if (repositoryType == null) {
			throw new TaskFailedException("Missing repository Type");
		}

		Integer repositoryTypeInt = IssueRepository.REPOSITORY_TYPE.valueOf(repositoryType).ordinal();

		//System.out.println("****************" + repositoryUrl + "|" + repositoryName + "|" + repositoryTypeInt);

		IssueRepository newRepository = new IssueRepository(repositoryId, repositoryUrl, repositoryName, repositoryTypeInt);

		I_GetConceptData issueRepoConcept = IssueRepositoryDAO.addIssueRepoToMetahier(newRepository, config);

		try {
			newRepository = IssueRepositoryDAO.getIssueRepository(issueRepoConcept);
			process.writeAttachment("issueRepositoryKey", newRepository);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
	 * Gets the repository url.
	 * 
	 * @return the repository url
	 */
	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	/**
	 * Sets the repository url.
	 * 
	 * @param repositoryUrl the new repository url
	 */
	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	/**
	 * Gets the repository name.
	 * 
	 * @return the repository name
	 */
	public String getRepositoryName() {
		return repositoryName;
	}

	/**
	 * Sets the repository name.
	 * 
	 * @param repositoryName the new repository name
	 */
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	/**
	 * Gets the repository type.
	 * 
	 * @return the repository type
	 */
	public String getRepositoryType() {
		return repositoryType;
	}

	/**
	 * Sets the repository type.
	 * 
	 * @param repositoryType the new repository type
	 */
	public void setRepositoryType(String repositoryType) {
		this.repositoryType = repositoryType;
	}

	/**
	 * Gets the repository id.
	 * 
	 * @return the repository id
	 */
	public String getRepositoryId() {
		return repositoryId;
	}

	/**
	 * Sets the repository id.
	 * 
	 * @param repositoryId the new repository id
	 */
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
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