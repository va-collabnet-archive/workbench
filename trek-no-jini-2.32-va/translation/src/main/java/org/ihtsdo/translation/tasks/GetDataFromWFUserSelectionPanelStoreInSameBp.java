/*
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
package org.ihtsdo.translation.tasks;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task collects the data entered on the Select users for workflow panel
 * currently displayed in the Workflow Details Sheet and verifies that
 * the required data has been filled in.
 * 
 * @author ALO
 * @version 1.0, June 2010
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN) })
public class GetDataFromWFUserSelectionPanelStoreInSameBp extends AbstractTask {

	/*
	 * -----------------------
	 * Properties
	 * -----------------------
	 */
	// Serialization Properties
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The Constant dataVersion. */
	private static final int dataVersion = 2;

	// Task Attribute Properties
	/** The profile prop name. */
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	
	/** The translator inbox prop name. */
	private String translatorInboxPropName = ProcessAttachmentKeys.TRANSLATOR_ROLE_INBOX.getAttachmentKey();
	
	/** The reviewer1 inbox prop name. */
	private String reviewer1InboxPropName = ProcessAttachmentKeys.REVIEWER_1_ROLE_INBOX.getAttachmentKey();
	
	/** The reviewer2 inbox prop name. */
	private String reviewer2InboxPropName = ProcessAttachmentKeys.REVIEWER_2_ROLE_INBOX.getAttachmentKey();
	
	/** The sme inbox prop name. */
	private String smeInboxPropName = ProcessAttachmentKeys.SME_ROLE_INBOX.getAttachmentKey();
	
	/** The editorial board inbox prop name. */
	private String editorialBoardInboxPropName = ProcessAttachmentKeys.EDITORIAL_BOARD_ROLE_INBOX.getAttachmentKey();

	// Other Properties
	/** The term factory. */
	private I_TermFactory termFactory;

	/*
	 * -----------------------
	 * Serialization Methods
	 * -----------------------
	 */
	/**
	 * Write object.
	 *
	 * @param out the out
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
		out.writeObject(translatorInboxPropName);
		out.writeObject(reviewer1InboxPropName);
		out.writeObject(reviewer2InboxPropName);
		out.writeObject(smeInboxPropName);
		out.writeObject(editorialBoardInboxPropName);
	}

	/**
	 * Read object.
	 *
	 * @param in the in
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();

		if (objDataVersion <= dataVersion) {
			if (objDataVersion >= 1) {
				// Read version 1 data fields...
				profilePropName = (String) in.readObject();
				translatorInboxPropName = (String) in.readObject();
				reviewer1InboxPropName = (String) in.readObject();
				reviewer2InboxPropName = (String) in.readObject();
				smeInboxPropName = (String) in.readObject();
				editorialBoardInboxPropName = (String) in.readObject();
			}
			// Initialize transient properties
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	/**
	 * Handles actions required by the task after normal task completion (such as moving a
	 * process to another user's input queue).
	 *
	 * @param process The currently executing Workflow process
	 * @param worker The worker currently executing this task
	 * @return void
	 * @throws TaskFailedException Thrown if a task fails for any reason.
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 * org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do
	}

	/**
	 * Performs the primary action of the task, which in this case is to gather and
	 * validate data that has been entered by the user on the Workflow Details Sheet.
	 *
	 * @param process The currently executing Workflow process
	 * @param worker The worker currently executing this task
	 * @return The exit condition of the task
	 * @throws TaskFailedException Thrown if a task fails for any reason.
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 * org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

		try {

			termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

			JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();

			for (Component c : workflowDetailsSheet.getComponents()) {
				if (UsersSelectionForWorkflowPanel.class.isAssignableFrom(c.getClass())) {
					UsersSelectionForWorkflowPanel panel = (UsersSelectionForWorkflowPanel) c;

					// ---------------------------------------------
					// Retrieve values from the panel / environment
					// ---------------------------------------------
					String translatorInbox = null;
					if (panel.getTranslator()) {
						translatorInbox = (String) panel.getTranslatorCombo().getSelectedItem();
						process.setDestination(translatorInbox);
					}
					String reviewer1Inbox = null;
					if (panel.getReviewer1()) {
						reviewer1Inbox = (String) panel.getReviewer1Combo().getSelectedItem();
					}
					String reviewer2Inbox = null;
					if (panel.getReviewer2()) {
						reviewer2Inbox = (String) panel.getReviewer2Combo().getSelectedItem();
					}
					String smeInbox = null;
					if (panel.getSme()) {
						smeInbox = (String) panel.getSmeCombo().getSelectedItem();
					}
					String editorialBoardInbox = null;
					if (panel.getEditorialBoard()) {
						editorialBoardInbox = (String) panel.getEbCombo().getSelectedItem();
					}

					// -------------------------------------------------------------------------
					// VERIFY ALL REQUIRED FIELDS AND STORE THE ENTERED DATA INTO PROPERTY KEYS
					// -------------------------------------------------------------------------

					// -----------------------------------------
					// Translator
					// -----------------------------------------
					if (translatorInbox == null || translatorInbox.isEmpty()) {
						// Warn the user that translatorInbox is required.
						JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
								"You must select a Translator Inbox. ", "", JOptionPane.ERROR_MESSAGE);
						return Condition.ITEM_CANCELED;
					} else {
						process.setProperty(translatorInboxPropName, translatorInbox);
					}

					// -----------------------------------------
					// Reviewer 1
					// -----------------------------------------
					if (panel.getReviewer1()) {
						if (reviewer1Inbox == null || reviewer1Inbox.isEmpty()) {
							// Warn the user that translatorInbox is required.
							JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
									"You must select a Reviewer 1 Inbox. ", "", JOptionPane.ERROR_MESSAGE);
							return Condition.ITEM_CANCELED;
						} else {
							process.setProperty(reviewer1InboxPropName, reviewer1Inbox);
						}
					}

					// -----------------------------------------
					// Reviewer 2
					// -----------------------------------------
					if (panel.getReviewer2()) {
						if (reviewer2Inbox == null || reviewer2Inbox.isEmpty()) {
							// Warn the user that translatorInbox is required.
							JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
									"You must select a Reviewer 2 Inbox. ", "", JOptionPane.ERROR_MESSAGE);
							return Condition.ITEM_CANCELED;
						} else {
							process.setProperty(reviewer2InboxPropName, reviewer2Inbox);
						}
					}

					// -----------------------------------------
					// SME
					// -----------------------------------------
					if (panel.getSme()) {
						if (smeInbox == null || smeInbox.isEmpty()) {
							// Warn the user that translatorInbox is required.
							JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
									"You must select a SME Inbox. ", "", JOptionPane.ERROR_MESSAGE);
							return Condition.ITEM_CANCELED;
						} else {
							process.setProperty(smeInboxPropName, smeInbox);
						}
					}

					// -----------------------------------------
					// Editorial Board
					// -----------------------------------------
					if (panel.getEditorialBoard()) {
						if (editorialBoardInbox == null || editorialBoardInbox.isEmpty()) {
							// Warn the user that translatorInbox is required.
							JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
									"You must select an Editorial Board Inbox. ", "", JOptionPane.ERROR_MESSAGE);
							return Condition.ITEM_CANCELED;
						} else {
							process.setProperty(editorialBoardInboxPropName, editorialBoardInbox);
						}
					}

					// Under normal conditions this is where we should return from
					return Condition.ITEM_COMPLETE;

				}
			}

			// If we got here we could not find the PanelRefsetAndParameters panel
			// so warn the user and cancel the task.
			JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
					"Could not locate the 'CreateRefsetPanel' panel. \n " + "Canceling the task. ", "",
					JOptionPane.ERROR_MESSAGE);
			return Condition.ITEM_CANCELED;

		} catch (Exception e) {
			e.printStackTrace();
			throw new TaskFailedException(e.getMessage());
		}
	}

	/**
	 * This method overrides: getDataContainerIds() in AbstractTask.
	 *
	 * @return The data container identifiers used by this task.
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}

	/**
	 * This method implements the interface method specified by: getConditions() in I_DefineTask.
	 *
	 * @return The possible evaluation conditions for this task.
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
	}

	/**
	 * Gets the profile prop name.
	 *
	 * @return the profile prop name
	 */
	public String getProfilePropName() {
		return profilePropName;
	}

	/**
	 * Sets the profile prop name.
	 *
	 * @param profilePropName the new profile prop name
	 */
	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

	/**
	 * Gets the translator inbox prop name.
	 *
	 * @return the translator inbox prop name
	 */
	public String getTranslatorInboxPropName() {
		return translatorInboxPropName;
	}

	/**
	 * Sets the translator inbox prop name.
	 *
	 * @param translatorInboxPropName the new translator inbox prop name
	 */
	public void setTranslatorInboxPropName(String translatorInboxPropName) {
		this.translatorInboxPropName = translatorInboxPropName;
	}

	/**
	 * Gets the reviewer1 inbox prop name.
	 *
	 * @return the reviewer1 inbox prop name
	 */
	public String getReviewer1InboxPropName() {
		return reviewer1InboxPropName;
	}

	/**
	 * Sets the reviewer1 inbox prop name.
	 *
	 * @param reviewer1InboxPropName the new reviewer1 inbox prop name
	 */
	public void setReviewer1InboxPropName(String reviewer1InboxPropName) {
		this.reviewer1InboxPropName = reviewer1InboxPropName;
	}

	/**
	 * Gets the reviewer2 inbox prop name.
	 *
	 * @return the reviewer2 inbox prop name
	 */
	public String getReviewer2InboxPropName() {
		return reviewer2InboxPropName;
	}

	/**
	 * Sets the reviewer2 inbox prop name.
	 *
	 * @param reviewer2InboxPropName the new reviewer2 inbox prop name
	 */
	public void setReviewer2InboxPropName(String reviewer2InboxPropName) {
		this.reviewer2InboxPropName = reviewer2InboxPropName;
	}

	/**
	 * Gets the sme inbox prop name.
	 *
	 * @return the sme inbox prop name
	 */
	public String getSmeInboxPropName() {
		return smeInboxPropName;
	}

	/**
	 * Sets the sme inbox prop name.
	 *
	 * @param smeInboxPropName the new sme inbox prop name
	 */
	public void setSmeInboxPropName(String smeInboxPropName) {
		this.smeInboxPropName = smeInboxPropName;
	}

	/**
	 * Gets the editorial board inbox prop name.
	 *
	 * @return the editorial board inbox prop name
	 */
	public String getEditorialBoardInboxPropName() {
		return editorialBoardInboxPropName;
	}

	/**
	 * Sets the editorial board inbox prop name.
	 *
	 * @param editorialBoardInboxPropName the new editorial board inbox prop name
	 */
	public void setEditorialBoardInboxPropName(String editorialBoardInboxPropName) {
		this.editorialBoardInboxPropName = editorialBoardInboxPropName;
	}


}