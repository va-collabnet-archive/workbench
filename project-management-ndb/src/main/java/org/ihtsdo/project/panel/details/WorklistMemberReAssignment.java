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

package org.ihtsdo.project.panel.details;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.api.WorkflowSearcher;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;

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
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The contract. */
	private HashMap<UUID, String> contract;
	
	/** The work list. */
	private WorkList workList;
	
	/** The provider. */
	private WfComponentProvider provider;
	
	/** The searcher. */
	private WorkflowSearcher searcher;
	
	/** The interpreter. */
	private WorkflowInterpreter interpreter;

	/**
	 * Instantiates a new worklist member re assignment.
	 */
	public WorklistMemberReAssignment() {

	}

	/**
	 * Instantiates a new worklist member re assignment.
	 *
	 * @param workList the work list
	 * @param config the config
	 */
	public WorklistMemberReAssignment(WorkList workList, I_ConfigAceFrame config) {
		initComponents();
		this.workList = workList;
		try {
			I_TermFactory tf = Terms.get();
			this.config = config;
			provider = new WfComponentProvider();
			searcher = new WorkflowSearcher();
			interpreter = WorkflowInterpreter.createWorkflowInterpreter(workList.getWorkflowDefinition());

			ProjectPermissionsAPI permissionApi = new ProjectPermissionsAPI(config);
			pBarW.setVisible(false);
			DefaultTableModel model = getMembersTableModel();
			getMemberList(workList, model);
			membersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			membersTable.setAutoCreateRowSorter(true);

			destinationCombo.addItem(DELETE_OPTION);
			List<WfUser> users = provider.getUsers();

			for (WfUser wfUser : users) {
				destinationCombo.addItem(wfUser);
			}

			DefaultTableModel model2 = new DefaultTableModel();
			model2.addColumn("WorkList member");
			model2.addColumn("Status");

			membersTable2.setModel(model2);

			membersTable2.setDefaultEditor(model2.getColumnClass(0), null);
			membersTable2.setDefaultEditor(model2.getColumnClass(1), null);
			membersTable2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			membersTable2.setAutoCreateRowSorter(true);

			boolean canReassign = permissionApi.checkPermissionForProject(config.getDbConfig().getUserConcept(),
					tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
					tf.getConcept(ArchitectonicAuxiliary.Concept.REASSINGNMENTS_PERMISSION.localize().getNid()));

			if (canReassign) {
				destinationCombo.setEnabled(true);
				statusCombo.setEnabled(true);
				sendButton.setEnabled(true);
				bAdd.setEnabled(true);
				bDel.setEnabled(true);
			} else {
				destinationCombo.setEnabled(false);
				destinationCombo.setEnabled(false);
				sendButton.setEnabled(false);
				bAdd.setEnabled(false);
				bDel.setEnabled(false);
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Gets the members table model.
	 *
	 * @return the members table model
	 */
	private DefaultTableModel getMembersTableModel() {
		DefaultTableModel model = new DefaultTableModel();
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
	 * @param workList the work list
	 * @param model the model
	 * @return the member list
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void getMemberList(WorkList workList, DefaultTableModel model) throws TerminologyException, IOException {
		List<WorkListMember> members = workList.getWorkListMembers();
		Collections.sort(members, new Comparator<WorkListMember>() {
			public int compare(WorkListMember f1, WorkListMember f2) {
				return f1.toString().compareTo(f2.toString());
			}
		});
		for (WorkListMember member : members) {
			I_GetConceptData activityStatus = member.getActivityStatus();
			model.addRow(new Object[] { member, activityStatus.toString() });
		}
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
	 * @param user the user
	 * @param status the status
	 */
	private void sendWorklistMembers(Object user, Object status) {
		DefaultTableModel model = (DefaultTableModel) membersTable2.getModel();
		if (model.getRowCount() > 0) {
			pBarW.setVisible(true);
			String destination;
			if (user instanceof WfUser) {
				final WfUser wfUser = (WfUser) user;
				final WfState wfState = (WfState) status;

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {

						try {
							DefaultTableModel model = (DefaultTableModel) membersTable2.getModel();
							while(model.getRowCount() > 0){
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
							e.printStackTrace();
							JOptionPane.showMessageDialog(WorklistMemberReAssignment.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				pBarW.setVisible(false);
			} else {
				while (model.getRowCount() > 0) {
					WorkListMember wlMember = (WorkListMember) model.getValueAt(0, 0);
					TerminologyProjectDAO.retireWorkListMember(wlMember);
					model.removeRow(0);
				}
				try {
					Terms.get().commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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

	}

	/**
	 * Refresh button action performed.
	 *
	 * @param e the e
	 */
	private void refreshButtonActionPerformed(ActionEvent e) {
		DefaultTableModel model = getMembersTableModel();
		try {
			getMemberList(workList, model);
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * Destination item state changed.
	 *
	 * @param e the e
	 */
	private void destinationItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (!e.getItem().toString().equals(DELETE_OPTION)) {
				statusCombo.removeAllItems();
				WfUser selectedUser = (WfUser) e.getItem();
				List<WfState> states = provider.getAllStates();
				for (WfState wfState : states) {
					WfInstance wlInstance = new WfInstance();
					wlInstance.setState(wfState);
					wlInstance.setDestination(selectedUser);
					List<WfAction> posibleActions = interpreter.getPossibleActions(wlInstance, selectedUser);
					if (posibleActions != null && !posibleActions.isEmpty()) {
						statusCombo.addItem(wfState);
					}
				}
			}
		}
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
		label8 = new JLabel();
		destinationCombo = new JComboBox();
		label1 = new JLabel();
		statusCombo = new JComboBox();
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

			// ---- label8 ----
			label8.setText("Destination:");
			panel2.add(label8, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- destinationCombo ----
			destinationCombo.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					destinationItemStateChanged(e);
				}
			});
			panel2.add(destinationCombo, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label1 ----
			label1.setText("Status:");
			panel2.add(label1, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
			panel2.add(statusCombo, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- sendButton ----
			sendButton.setText("Send");
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
	/** The label9. */
	private JLabel label9;
	
	/** The label10. */
	private JLabel label10;
	
	/** The members table scroll panel. */
	private JScrollPane membersTableScrollPanel;
	
	/** The members table. */
	private JTable membersTable;
	
	/** The panel1. */
	private JPanel panel1;
	
	/** The b add. */
	private JButton bAdd;
	
	/** The b del. */
	private JButton bDel;
	
	/** The members table scroll panel2. */
	private JScrollPane membersTableScrollPanel2;
	
	/** The members table2. */
	private JTable membersTable2;
	
	/** The panel2. */
	private JPanel panel2;
	
	/** The refresh button. */
	private JButton refreshButton;
	
	/** The label8. */
	private JLabel label8;
	
	/** The destination combo. */
	private JComboBox destinationCombo;
	
	/** The label1. */
	private JLabel label1;
	
	/** The status combo. */
	private JComboBox statusCombo;
	
	/** The send button. */
	private JButton sendButton;
	
	/** The p bar w. */
	private JProgressBar pBarW;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
