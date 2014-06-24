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
package org.ihtsdo.project.task;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
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
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;

/**
 * The Class GetDataFromUAWPanelSR.
 * 
 * @author ALO
 * @version 1.0, June 2010
 */
@BeanList(specs = { @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN) })
public class GetDataFromUAWPanelSR extends AbstractTask {

	/*
	 * ----------------------- Properties -----------------------
	 */
	// Serialization Properties
	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The Constant dataVersion.
	 */
	private static final int dataVersion = 2;
	// Task Attribute Properties
	/**
	 * The profile prop name.
	 */
	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	/**
	 * The member prop name.
	 */
	private String memberPropName = ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey();
	// Other Properties
	/**
	 * The term factory.
	 */
	private I_TermFactory termFactory;

	/*
	 * ----------------------- Serialization Methods -----------------------
	 */
	/**
	 * Write object.
	 * 
	 * @param out
	 *            the out
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
		out.writeObject(memberPropName);
	}

	/**
	 * Read object.
	 * 
	 * @param in
	 *            the in
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();

		if (objDataVersion <= dataVersion) {
			if (objDataVersion >= 1) {
				// Read version 1 data fields...
				profilePropName = (String) in.readObject();
				memberPropName = (String) in.readObject();
			}
			// Initialize transient properties
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	/**
	 * Handles actions required by the task after normal task completion (such
	 * as moving a process to another user's input queue).
	 * 
	 * @param process
	 *            The currently executing Workflow process
	 * @param worker
	 *            The worker currently executing this task
	 * @return void
	 * @throws TaskFailedException
	 *             Thrown if a task fails for any reason.
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do
	}

	/**
	 * Performs the primary action of the task, which in this case is to gather
	 * and validate data that has been entered by the user on the Workflow
	 * Details Sheet.
	 * 
	 * @param process
	 *            The currently executing Workflow process
	 * @param worker
	 *            The worker currently executing this task
	 * @return The exit condition of the task
	 * @throws TaskFailedException
	 *             Thrown if a task fails for any reason.
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

		try {

			termFactory = Terms.get();
			// TODO: move to read from profile
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

			JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();

			GetInforForUnassignedWork panel = null;

			for (Component c : workflowDetailsSheet.getComponents()) {
				if (GetInforForUnassignedWork.class.isAssignableFrom(c.getClass())) {
					panel = (GetInforForUnassignedWork) c;
				}
			}

			if (panel != null) {
				I_TerminologyProject selectedProject = panel.getSelectedProject();
				WorkList selectedWorkList = panel.getSelectedWorkList();
				if (selectedWorkList == null) {
					JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "The worklist is null.' panel. \n " + "Canceling the task. ", "", JOptionPane.ERROR_MESSAGE);
					return Condition.ITEM_CANCELED;
				}
				I_DescriptionTuple tuple = config.getSearchResultsSelection();
				I_GetConceptData selectedConcept = termFactory.getConcept(tuple.getConceptNid());

				// Create worklist member for unassigned work
				WorkListMember member = TerminologyProjectDAO.addConceptAsNacWorklistMember(selectedWorkList, selectedConcept, config);

				return Condition.ITEM_COMPLETE;
			}

			// If we got here we could not find the PanelRefsetAndParameters
			// panel
			// so warn the user and cancel the task.
			JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Could not locate the 'GetInforForUnassignedWork' panel. \n " + "Canceling the task. ", "", JOptionPane.ERROR_MESSAGE);
			return Condition.ITEM_CANCELED;

		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
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
	 * This method implements the interface method specified by: getConditions()
	 * in I_DefineTask.
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
	 * @param profilePropName
	 *            the new profile prop name
	 */
	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

	/**
	 * Gets the member prop name.
	 * 
	 * @return the member prop name
	 */
	public String getMemberPropName() {
		return memberPropName;
	}

	/**
	 * Sets the member prop name.
	 * 
	 * @param memberPropName
	 *            the new member prop name
	 */
	public void setMemberPropName(String memberPropName) {
		this.memberPropName = memberPropName;
	}
}