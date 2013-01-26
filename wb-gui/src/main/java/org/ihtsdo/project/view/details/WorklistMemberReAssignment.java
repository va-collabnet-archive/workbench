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

package org.ihtsdo.project.view.details;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfStateComparator;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WfUserComparator;

/**
 * The Class WorklistMemberReAssignment.
 * 
 * @author Guillermo Reynoso
 */
public class WorklistMemberReAssignment extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant DELETE_OPTION. */
	private static final String DELETE_OPTION = "Delete from queue";

	/** The work list. */
	private WorkList workList;

	/** The provider. */
	private WfComponentProvider provider;

	/** The interpreter. */
	private WorkflowInterpreter interpreter;
	/** The members worker. */
	private WorklistMembersReassignmentWorker membersReassWorker;
	private WorkflowInterperterInitWorker workflowInterpreterInitWorker;
	private GetUsersWorker userProviderWorker;
	private DefaultTableModel model;

	private GetStateWorker stateProviderWorker;

	private WfState wfState;

	private WfUser wfUser;

	/**
	 * Instantiates a new worklist member re assignment.
	 */
	public WorklistMemberReAssignment() {

	}

	/**
	 * Instantiates a new worklist member re assignment.
	 * 
	 * @param workList
	 *            the work list
	 * @param config
	 *            the config
	 */
	public WorklistMemberReAssignment(WorkList workList, I_ConfigAceFrame config) {
		initComponents();
		this.workList = workList;
		try {
			I_TermFactory tf = Terms.get();
			provider = new WfComponentProvider();

			workflowInterpreterInitWorker = new WorkflowInterperterInitWorker(workList);
			workflowInterpreterInitWorker.addPropertyChangeListener(new ProgressListener(pBarW));
			workflowInterpreterInitWorker.execute();

			ProjectPermissionsAPI permissionApi = new ProjectPermissionsAPI(config);
			pBarW.setVisible(false);
			getMembersTableModel();
			getMemberList();
			membersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			membersTable.setAutoCreateRowSorter(true);

			DefaultTableModel model2 = new DefaultTableModel();
			model2.addColumn("WorkList member");
			model2.addColumn("Status");

			membersTable2.setModel(model2);

			membersTable2.setDefaultEditor(model2.getColumnClass(0), null);
			membersTable2.setDefaultEditor(model2.getColumnClass(1), null);
			membersTable2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			membersTable2.setAutoCreateRowSorter(true);

			boolean canReassign = permissionApi.checkPermissionForProject(config.getDbConfig().getUserConcept(), tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()), tf.getConcept(ArchitectonicAuxiliary.Concept.REASSINGNMENTS_PERMISSION.localize().getNid()));

			if (canReassign) {
				destinationCombo.setEnabled(true);
				statusCombo.setEnabled(true);
				sendButton.setEnabled(true);
				bAdd.setEnabled(true);
				bDel.setEnabled(true);
			} else {
				destinationCombo.setEnabled(false);
				statusCombo.setEnabled(false);
				sendButton.setEnabled(false);
				bAdd.setEnabled(false);
				bDel.setEnabled(false);
			}

			stateProviderWorker = new GetStateWorker();
			stateProviderWorker.addPropertyChangeListener(new ProgressListener(pBarW));
			stateProviderWorker.execute();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Gets the members table model.
	 * 
	 * @return the members table model
	 */
	private DefaultTableModel getMembersTableModel() {
		model = new DefaultTableModel();
		model.addColumn("WorkList member");
		model.addColumn("Status");
		membersTable.setModel(model);
		membersTable.setDefaultEditor(model.getColumnClass(0), null);
		membersTable.setDefaultEditor(model.getColumnClass(1), null);
		return model;
	}

	/**
	 * Gets the member list.
	 * 
	 * @param workList
	 *            the work list
	 * @param membersTable3
	 * @param model2
	 * @param model
	 *            the model
	 * @return the member list
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */

	private void getMemberList() throws TerminologyException, IOException {
		if (membersReassWorker != null && !membersReassWorker.isDone()) {
			membersReassWorker.cancel(true);
			membersReassWorker = null;
		}
		membersReassWorker = new WorklistMembersReassignmentWorker(workList, model, membersTable);
		membersReassWorker.addPropertyChangeListener(new ProgressListener(pBarW));
		membersReassWorker.execute();

	}

	/**
	 * Send button action performed.
	 */
	private void sendButtonActionPerformed() {
		sendWorklistMembers(destinationCombo.getSelectedItem(), statusCombo.getSelectedItem());
	}

	/**
	 * Send worklist members.
	 * 
	 * @param user
	 *            the user
	 * @param status
	 *            the status
	 */
	private void sendWorklistMembers(Object user, Object status) {
		DefaultTableModel model = (DefaultTableModel) membersTable2.getModel();
		if (model.getRowCount() > 0) {
			pBarW.setVisible(true);
			if (status instanceof WfState && user instanceof WfUser) {
				wfUser = (WfUser) user;
				wfState = (WfState) status;
			} else if (status.toString().equals(DELETE_OPTION)) {
				try {
					I_GetConceptData canceledConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CANCELED_STATUS.localize().getNid());
					wfState = provider.statusConceptToWfState(canceledConcept);
					wfUser = provider.userConceptToWfUser(canceledConcept);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (TerminologyException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			} else if (status == null || user == null || status.toString().trim().equals("") || user.toString().trim().equals("") || status.toString().equalsIgnoreCase("Loading...")) {
				JOptionPane.showMessageDialog(WorklistMemberReAssignment.this, "Status and Destination required.", "Message", JOptionPane.INFORMATION_MESSAGE);
				pBarW.setVisible(false);
				return;
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					try {
						DefaultTableModel model = (DefaultTableModel) membersTable2.getModel();
						while (model.getRowCount() > 0) {
							WorkListMember wlMember = (WorkListMember) model.getValueAt(0, 0);
							WfInstance wfInstance = wlMember.getWfInstance();
							WfInstance.updateInstanceState(wfInstance, wfState);
							WfInstance.updateDestination(wfInstance, wfUser);
							model.removeRow(0);
						}
						Terms.get().commit();
						pBarW.setVisible(false);
						JOptionPane.showMessageDialog(WorklistMemberReAssignment.this, "Worklist members sent!", "Message", JOptionPane.INFORMATION_MESSAGE);
						// worker.execute(process);
					} catch (Exception e) {
						// error getting the workflow
						pBarW.setVisible(false);
						AceLog.getAppLog().alertAndLogException(e);
						JOptionPane.showMessageDialog(WorklistMemberReAssignment.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			pBarW.setVisible(false);
		}
	}

	/**
	 * B add action performed.
	 */
	private void bAddActionPerformed() {
		addMembersToTargetTable();
	}

	/**
	 * Adds the members to target table.
	 */
	private void addMembersToTargetTable() {
		int[] sRows = membersTable.getSelectedRows();
		DefaultTableModel model = (DefaultTableModel) membersTable.getModel();
		DefaultTableModel model2 = (DefaultTableModel) membersTable2.getModel();
		for (int i = sRows.length - 1; i > -1; i--) {
			int rowModel = membersTable.convertRowIndexToModel(sRows[i]);
			WorkListMember member = (WorkListMember) model.getValueAt(rowModel, 0);
			String status = (String) model.getValueAt(rowModel, 1);

			model2.addRow(new Object[] { member, status });
			model.removeRow(rowModel);
			membersTable.validate();
		}
		// updateUsersCombo();
	}

	/**
	 * B del action performed.
	 */
	private void bDelActionPerformed() {
		delMembersFromTargetTable();
	}

	/**
	 * Del members from target table.
	 */
	private void delMembersFromTargetTable() {
		int[] sRows = membersTable2.getSelectedRows();
		DefaultTableModel model = (DefaultTableModel) membersTable.getModel();
		DefaultTableModel model2 = (DefaultTableModel) membersTable2.getModel();
		for (int i = sRows.length - 1; i > -1; i--) {
			int rowModel = membersTable2.convertRowIndexToModel(sRows[i]);
			WorkListMember member = (WorkListMember) model2.getValueAt(rowModel, 0);
			String status = (String) model2.getValueAt(rowModel, 1);

			model.addRow(new Object[] { member, status });
			model2.removeRow(rowModel);
			membersTable2.validate();
		}
		// updateUsersCombo();
	}

	private void updateUsersCombo() {
		List<WorkListMember> wlMembers = new ArrayList<WorkListMember>();

		DefaultTableModel model2 = (DefaultTableModel) membersTable2.getModel();
		int rowCount = model2.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			WorkListMember member = (WorkListMember) model2.getValueAt(i, 0);
			wlMembers.add(member);
		}
		if (!wlMembers.isEmpty()) {
			userProviderWorker = new GetUsersWorker(destinationCombo, wlMembers);
			userProviderWorker.addPropertyChangeListener(new ProgressListener(pBarW));
			userProviderWorker.execute();
		} else {
			while (destinationCombo.getItemCount() != 0) {
				destinationCombo.removeItemAt(0);
			}
		}
	}

	/**
	 * Refresh button action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void refreshButtonActionPerformed(ActionEvent e) {
		try {
			getMemberList();
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	private void statusComboItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			while (!workflowInterpreterInitWorker.isDone()) {

			}
			try {
				interpreter = workflowInterpreterInitWorker.get();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				e1.printStackTrace();
			}
		}
		if (!e.getItem().toString().equals(DELETE_OPTION) && !e.getItem().toString().startsWith("Loading")) {
			destinationCombo.removeAllItems();
			WfState selectedState = (WfState) e.getItem();
			List<WfUser> newItems = getPosibleUsers(selectedState);

			List<WfUser> reducedList = getPosibleUsers(selectedState);
			for (WfUser wfUser : newItems) {
				if (!reducedList.contains(wfUser)) {
					reducedList.add(wfUser);
				}
			}
			for (WfUser wfUser : reducedList) {
				destinationCombo.addItem(wfUser);
			}
		} else if (e.getItem().toString().equals(DELETE_OPTION)) {
			destinationCombo.removeAllItems();
		}
	}

	private List<WfUser> getPosibleUsers(WfState selectedState) {
		List<WfUser> users = workList.getUsers();
		List<WfUser> result = new ArrayList<WfUser>();
		for (WfUser wfUser : users) {
			WfInstance wfInstance = new WfInstance();
			// For now it does not meter.
			DefaultTableModel model1 = (DefaultTableModel) membersTable.getModel();
			DefaultTableModel model2 = (DefaultTableModel) membersTable2.getModel();
			WorkListMember member = null;
			if (model2.getRowCount() > 0) {
				member = (WorkListMember) model2.getValueAt(0, 0);
			} else if (model1.getRowCount() > 0) {
				member = (WorkListMember) model1.getValueAt(0, 0);
			}
			if (member != null) {
				wfInstance.setComponentId(member.getWfInstance().getComponentId());
				wfInstance.setState(selectedState);
				wfInstance.setDestination(wfUser);
				wfInstance.setWfDefinition(workList.getWorkflowDefinition());
				List<WfAction> posibleActions = interpreter.getPossibleActions(wfInstance, wfUser);
				if (posibleActions != null && !posibleActions.isEmpty()) {
					result.add(wfUser);
				}
			}
		}
		Collections.sort(result, new WfUserComparator());
		return result;
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		label9 = new JLabel();
		label10 = new JLabel();
		membersTableScrollPanel = new JScrollPane();
		membersTable = new JTable();
		panel1 = new JPanel();
		bAdd = new JButton();
		bDel = new JButton();
		membersTableScrollPanel2 = new JScrollPane();
		membersTable2 = new JTable();
		panel2 = new JPanel();
		refreshButton = new JButton();
		label1 = new JLabel();
		statusCombo = new JComboBox();
		label8 = new JLabel();
		destinationCombo = new JComboBox();
		sendButton = new JButton();
		pBarW = new JProgressBar();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0, 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0, 16, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 0.0, 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 1.0E-4 };

		// ---- label9 ----
		label9.setText("WorkList members");
		add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

		// ---- label10 ----
		label10.setText("WorkList members to re-assign");
		add(label10, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== membersTableScrollPanel ========
		{
			membersTableScrollPanel.setViewportView(membersTable);
		}
		add(membersTableScrollPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

		// ======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 0 };
			((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 0.0, 1.0E-4 };
			((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };

			// ---- bAdd ----
			bAdd.setText(">");
			bAdd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bAddActionPerformed();
				}
			});
			panel1.add(bAdd, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

			// ---- bDel ----
			bDel.setText("<");
			bDel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bDelActionPerformed();
				}
			});
			panel1.add(bDel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

		// ======== membersTableScrollPanel2 ========
		{
			membersTableScrollPanel2.setViewportView(membersTable2);
		}
		add(membersTableScrollPanel2, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout) panel2.getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			((GridBagLayout) panel2.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel2.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- refreshButton ----
			refreshButton.setText("Refresh");
			refreshButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			refreshButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					refreshButtonActionPerformed(e);
				}
			});
			panel2.add(refreshButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label1 ----
			label1.setText("Status:");
			panel2.add(label1, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- statusCombo ----
			statusCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			statusCombo.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					statusComboItemStateChanged(e);
				}
			});
			panel2.add(statusCombo, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label8 ----
			label8.setText("Destination:");
			panel2.add(label8, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- destinationCombo ----
			destinationCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel2.add(destinationCombo, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- sendButton ----
			sendButton.setText("Send");
			sendButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			sendButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sendButtonActionPerformed();
				}
			});
			panel2.add(sendButton, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ---- pBarW ----
		pBarW.setIndeterminate(true);
		add(pBarW, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JLabel label9;
	private JLabel label10;
	private JScrollPane membersTableScrollPanel;
	private JTable membersTable;
	private JPanel panel1;
	private JButton bAdd;
	private JButton bDel;
	private JScrollPane membersTableScrollPanel2;
	private JTable membersTable2;
	private JPanel panel2;
	private JButton refreshButton;
	private JLabel label1;
	private JComboBox statusCombo;
	private JLabel label8;
	private JComboBox destinationCombo;
	private JButton sendButton;
	private JProgressBar pBarW;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	class GetUsersWorker extends SwingWorker<String, WfUser> {

		private JComboBox destinationCombo;
		private List<WorkListMember> wlMembers;

		public GetUsersWorker(JComboBox destinationCombo, List<WorkListMember> wlMembers) {
			super();
			this.destinationCombo = destinationCombo;
			this.wlMembers = wlMembers;
		}

		@Override
		protected String doInBackground() throws Exception {
			while (destinationCombo.getItemCount() != 0) {
				destinationCombo.removeItemAt(0);
			}
			// statusCombo.removeAllItems();
			destinationCombo.addItem("Loading...");
			if (workflowInterpreterInitWorker != null) {
				while (!workflowInterpreterInitWorker.isDone()) {

				}
				try {
					if (interpreter == null) {
						interpreter = workflowInterpreterInitWorker.get();
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				}
			}
			if (statusCombo.getSelectedItem() instanceof WfState) {
				List<WfUser> finalDestinations = getPosibleUsers((WfState) statusCombo.getSelectedItem());
				for (WfUser wfUser : finalDestinations) {
					publish(wfUser);
				}
			}
			return "Done";
		}

		@Override
		protected void process(List<WfUser> chunks) {
			for (WfUser wfUser : chunks) {
				destinationCombo.addItem(wfUser);
			}
		}

		@Override
		public void done() {
			try {
				get();
				destinationCombo.removeItemAt(0);
			} catch (Exception ignore) {
				//ignorAceLog.getAppLog().alertAndLogException(e);
			}
		}

	}

	class GetStateWorker extends SwingWorker<String, WfState> {

		public GetStateWorker() {
			super();
		}

		@Override
		protected String doInBackground() throws Exception {
			statusCombo.addItem("Loading...");
			Concept wias = ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS;
			I_GetConceptData stateConcept = Terms.get().getConcept(wias.getUids());
			statusCombo.addItem(new WfState(stateConcept.getInitialText(), stateConcept.getPrimUuid()));
			if (workflowInterpreterInitWorker != null) {
				while (!workflowInterpreterInitWorker.isDone()) {

				}
				try {
					if (interpreter == null) {
						interpreter = workflowInterpreterInitWorker.get();
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				}
			}
			statusCombo.addItem(DELETE_OPTION);
			List<WfState> states = interpreter.getWfDefinition().getStates();
			Collections.sort(states, new WfStateComparator());
			for (WfState state : states) {
				publish(state);
			}
			return "Done";
		}

		@Override
		protected void process(List<WfState> chunks) {
			for (WfState state : chunks) {
				statusCombo.addItem(state);
			}
		}

		@Override
		public void done() {
			try {
				get();
				statusCombo.removeItemAt(0);
				statusCombo.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						statusComboItemStateChanged(e);
					}
				});
			} catch (Exception ignore) {
				AceLog.getAppLog().alertAndLogException(ignore);
			}
		}

	}
}

class WorklistMembersReassignmentWorker extends SwingWorker<String, Object[]> {

	private JTable membersTable;
	private DefaultTableModel model;
	private WorkList workList;

	public WorklistMembersReassignmentWorker(WorkList workList, DefaultTableModel model, JTable membersTable) {
		super();
		this.model = model;
		this.membersTable = membersTable;
		this.workList = workList;
		clearTable();
	}

	private void clearTable() {
		while (model.getRowCount() > 0) {
			model.removeRow(0);
		}
	}

	@Override
	protected String doInBackground() throws Exception {
		List<WorkListMember> members = workList.getWorkListMembers();
		Collections.sort(members, new Comparator<WorkListMember>() {
			public int compare(WorkListMember f1, WorkListMember f2) {
				return f1.toString().compareTo(f2.toString());
			}
		});
		for (WorkListMember member : members) {
			I_GetConceptData activityStatus = member.getActivityStatus();
			publish(new Object[] { member, activityStatus.toString() });
		}
		return "Done";
	}

	@Override
	protected void process(List<Object[]> chunks) {
		for (Object[] objects : chunks) {
			model.addRow(objects);
		}
	}

	@Override
	public void done() {
		try {
			get();
			membersTable.revalidate();
			membersTable.repaint();
		} catch (Exception ignore) {
			AceLog.getAppLog().alertAndLogException(ignore);
		}
	}

}