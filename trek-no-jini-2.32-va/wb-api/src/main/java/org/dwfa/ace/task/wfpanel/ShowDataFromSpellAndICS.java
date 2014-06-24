/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.task.wfpanel;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.modeler.tool.AskForConceptName;
import org.dwfa.ace.modeler.tool.AskForConceptParent;
import org.dwfa.ace.modeler.tool.ShowConceptData;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
/**
 * 
 * @author ALO
 * @version 1.0, June 2010
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/wfpanel", type = BeanType.TASK_BEAN) })
public class ShowDataFromSpellAndICS extends AbstractTask {

	/*
	 * -----------------------
	 * Properties
	 * -----------------------
	 */
	// Serialization Properties
	private static final long serialVersionUID = 1L;
	private static final int dataVersion = 2;

	// Task Attribute Properties

	// Other Properties
	private I_TermFactory termFactory;

	/*
	 * -----------------------
	 * Serialization Methods
	 * -----------------------
	 */
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();

		if (objDataVersion <= dataVersion) {
			if (objDataVersion >= 1) {
				// Read version 1 data fields...
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
	 * @return void
	 * @param process The currently executing Workflow process
	 * @param worker The worker currently executing this task
	 * @exception TaskFailedException Thrown if a task fails for any reason.
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do
	}

	/**
	 * Performs the primary action of the task, which in this case is to gather and
	 * validate data that has been entered by the user on the Workflow Details Sheet.
	 * 
	 * @return The exit condition of the task
	 * @param process The currently executing Workflow process
	 * @param worker The worker currently executing this task
	 * @exception TaskFailedException Thrown if a task fails for any reason.
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

		try {

			termFactory = Terms.get();
			// TODO: move to read from profile
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

			JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
			
			AskForConceptName panel = null;
			
			for (Component c : workflowDetailsSheet.getComponents()) {
				if (AskForConceptName.class.isAssignableFrom(c.getClass())) {
					panel = (AskForConceptName) c;
				}
			}

			if (panel != null) {
				DefaultListModel listModel=panel.getParentListModel();
				String conceptName = panel.getConceptName().trim();
				String semtag=panel.getSemanticTag();
				if (listModel.getSize()>0 && !conceptName.equals("") && !semtag.equals("")){
					//				int width = 475;
					//				int height = 260;
					//				workflowDetailsSheet.setSize(width, height);
					//				workflowDetailsSheet.setLayout(new GridLayout(1, 1));

					ShowConceptData newPanel = new ShowConceptData(config,listModel, conceptName,semtag);

					workflowDetailsSheet.remove(panel);

					workflowDetailsSheet.add(newPanel);
					workflowDetailsSheet.revalidate();
					//
					//				int n = JOptionPane.showConfirmDialog(
					//						null,
					//						"You are going to send the concept \"" + selectedConcept.toString()+ "\"\n" +
					//						"to a change workflow in the worklist \"" + selectedWorkList.getName()+ "\"\n" +
					//						"Are you sure?",
					//						"Confirm",
					//						JOptionPane.YES_NO_OPTION);
					//				if (n == JOptionPane.YES_OPTION) {
					//					// Create worklist member for unassigned work
					//					WorkListMember workListMember = TerminologyProjectDAO.addConceptAsNacWorklistMember(
					//							selectedWorkList, selectedConcept,
					//							config.getUsername()+".outbox", config);
					//					
					//					process.setProperty(memberPropName, workListMember);
					//					process.setProperty(memberPropName, workListMember);

					return Condition.ITEM_COMPLETE;
				}else{
					return Condition.ITEM_CANCELED;

				}
			}else {
					return Condition.ITEM_CANCELED;

			}

			// If we got here we could not find the PanelRefsetAndParameters panel
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new TaskFailedException(e.getMessage());
		}
	}

	/**
	 * This method overrides: getDataContainerIds() in AbstractTask
	 * 
	 * @return The data container identifiers used by this task.
	 */
	public int[] getDataContainerIds() {
		return new int[] {};
	}

	/**
	 * This method implements the interface method specified by: getConditions() in I_DefineTask
	 * 
	 * @return The possible evaluation conditions for this task.
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
	}

}