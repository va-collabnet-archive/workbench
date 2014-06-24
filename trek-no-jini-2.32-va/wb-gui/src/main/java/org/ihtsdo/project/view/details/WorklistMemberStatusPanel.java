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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ActivityUpdater;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;

/**
 * The Class WorklistMemberStatusPanel.
 * 
 * @author Guillermo Reynoso
 */
public class WorklistMemberStatusPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4054703594663442841L;

	/** The work list. */
	private WorkList workList;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The list5 model. */
	private DefaultListModel list5Model;

	/** The statuses. */
	private HashMap<I_GetConceptData, Integer> statuses;

	/** The list1 model. */
	private DefaultListModel list1Model;

	/** The members worker. */
	private WorklistMembersStatusWorker membersWorker;

	/**
	 * Instantiates a new worklist member status panel.
	 */
	public WorklistMemberStatusPanel() {
		initComponents();
		button2.setVisible(false);
		button2.setEnabled(false);
	}

	/**
	 * Instantiates a new worklist member status panel.
	 * 
	 * @param workList
	 *            the work list
	 * @param config
	 *            the config
	 */
	public WorklistMemberStatusPanel(WorkList workList, I_ConfigAceFrame config) {
		initComponents();
		
		// Hiding promote button
		button2.setVisible(false);
		button2.setEnabled(false);
		
		this.workList = workList;
		this.config = config;
		label19.setText("Partition: " + workList.getPartition().getName());
		setupList5();
		updateData();
		ProjectPermissionsAPI permissionApi = new ProjectPermissionsAPI(config);

		try {
			boolean isProjectManager = permissionApi.checkPermissionForProject(config.getDbConfig().getUserConcept(), Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
					Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECT_MANAGER_ROLE.localize().getNid()));
			if (!isProjectManager) {
				button2.setVisible(false);
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		this.revalidate();
	}

	/**
	 * Setup list5.
	 */
	private void setupList5() {
		list5Model = new DefaultListModel();
		list5.setModel(list5Model);
		list1Model = new DefaultListModel();
		list1Model.clear();
		list1.setModel(list1Model);
	}

	/**
	 * Update data.
	 */
	private void updateData() {
		if (membersWorker != null && !membersWorker.isDone()) {
			membersWorker.cancel(true);
			membersWorker = null;
		}
		membersWorker = new WorklistMembersStatusWorker();
		membersWorker.execute();
	}

	/**
	 * List5 value changed.
	 * 
	 * @param e
	 *            the e
	 */
	private void list5ValueChanged(ListSelectionEvent e) {
		// Status selected, update membersList
		String selectedValue = (String) list5.getSelectedValue();
		if (selectedValue != null) {
			list1Model = new DefaultListModel();
			list1Model.clear();
			for (I_GetConceptData status : statuses.keySet()) {
				if (status.toString().equals(selectedValue.substring(0, selectedValue.indexOf("(") - 1).trim())) {
					try {
						List<WorkListMember> members = TerminologyProjectDAO.getWorkListMembersWithStatus(workList, status, config);
						Collections.sort(members, new Comparator<WorkListMember>() {
							public int compare(WorkListMember f1, WorkListMember f2) {
								return f1.toString().compareTo(f2.toString());
							}
						});
						for (WorkListMember member : members) {
							list1Model.addElement(member.getName());
						}
						label15.setText("(" + status.toString() + ")");

					} catch (TerminologyException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
			list1.setModel(list1Model);
			list1.validate();
			label15.validate();
		}
	}

	/**
	 * Button1 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button1ActionPerformed(ActionEvent e) {
		// refresh
		updateData();
	}

	private void button2MouseClicked(MouseEvent e) {
		try {
			String selectedValue = (String) list5.getSelectedValue();
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			I_TerminologyProject iproject = TerminologyProjectDAO.getProjectForWorklist(workList, config);
			TranslationProject project = (TranslationProject) iproject;

			if (selectedValue != null) {
				int n = JOptionPane.showConfirmDialog(null, "You are about to promote all concepts with status: " + selectedValue.substring(0, selectedValue.indexOf("(") - 1).trim(), "Confirm", JOptionPane.YES_NO_OPTION);
				if (n == 0) {
					I_ShowActivity activity = Terms.get().newActivityPanel(true, config, "<html>Promoting content: <br>", true);
					ActivityUpdater updater = new ActivityUpdater(activity, "Promoting content");
					updater.startActivity();
					for (I_GetConceptData status : statuses.keySet()) {
						if (status.toString().equals(selectedValue.substring(0, selectedValue.indexOf("(") - 1).trim())) {
							try {
								List<WorkListMember> members = TerminologyProjectDAO.getWorkListMembersWithStatus(workList, status, config);
								updater.startCount(members.size());
								for (WorkListMember member : members) {
									TerminologyProjectDAO.promoteTargetContentToReleasePath(project, member.getConcept(), config);
									updater.incrementCount();
								}
							} catch (TerminologyException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
					updater.finish();
					try {
						activity.complete();
					} catch (CancellationException ce) {
						activity.setProgressInfoLower("Canceled");
						try {
							activity.complete();
						} catch (ComputationCanceled e4) {
							activity.setProgressInfoLower("Canceled");
						}
					} catch (Exception e5) {
						activity.setProgressInfoLower("Canceled with error");
						e5.printStackTrace();
					}
					Terms.get().commit();
					JOptionPane.showMessageDialog(null, "Concepts promoted");
				} else {
					JOptionPane.showMessageDialog(null, "Operation cancelled");
				}
			}
		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel20 = new JPanel();
		panel24 = new JPanel();
		label19 = new JLabel();
		label13 = new JLabel();
		panel5 = new JPanel();
		label14 = new JLabel();
		label15 = new JLabel();
		scrollPane5 = new JScrollPane();
		list5 = new JList();
		panel21 = new JPanel();
		scrollPane1 = new JScrollPane();
		list1 = new JList();
		panel1 = new JPanel();
		button1 = new JButton();
		button2 = new JButton();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 1.0, 0.0, 1.0E-4 };

		// ======== panel20 ========
		{
			panel20.setLayout(new GridBagLayout());
			((GridBagLayout) panel20.getLayout()).columnWidths = new int[] { 251, 77, 286, 0 };
			((GridBagLayout) panel20.getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
			((GridBagLayout) panel20.getLayout()).columnWeights = new double[] { 1.0, 0.0, 1.0, 1.0E-4 };
			((GridBagLayout) panel20.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0, 1.0E-4 };

			// ======== panel24 ========
			{
				panel24.setLayout(new GridBagLayout());
				((GridBagLayout) panel24.getLayout()).columnWidths = new int[] { 0, 0 };
				((GridBagLayout) panel24.getLayout()).rowHeights = new int[] { 0, 0 };
				((GridBagLayout) panel24.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
				((GridBagLayout) panel24.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };
				panel24.add(label19, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			panel20.add(panel24, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

			// ---- label13 ----
			label13.setText("Current statuses of WorkList members");
			panel20.add(label13, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ======== panel5 ========
			{
				panel5.setLayout(new GridBagLayout());
				((GridBagLayout) panel5.getLayout()).columnWidths = new int[] { 255, 10, 0 };
				((GridBagLayout) panel5.getLayout()).rowHeights = new int[] { 0, 0 };
				((GridBagLayout) panel5.getLayout()).columnWeights = new double[] { 1.0, 1.0, 1.0E-4 };
				((GridBagLayout) panel5.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

				// ---- label14 ----
				label14.setText("Members with selected status");
				panel5.add(label14, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));

				// ---- label15 ----
				label15.setText("-");
				panel5.add(label15, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
			}
			panel20.add(panel5, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

			// ======== scrollPane5 ========
			{

				// ---- list5 ----
				list5.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						list5ValueChanged(e);
					}
				});
				scrollPane5.setViewportView(list5);
			}
			panel20.add(scrollPane5, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ======== panel21 ========
			{
				panel21.setLayout(new GridBagLayout());
				((GridBagLayout) panel21.getLayout()).columnWidths = new int[] { 0, 0 };
				((GridBagLayout) panel21.getLayout()).rowHeights = new int[] { 0, 0 };
				((GridBagLayout) panel21.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
				((GridBagLayout) panel21.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

				// ======== scrollPane1 ========
				{
					scrollPane1.setViewportView(list1);
				}
				panel21.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			panel20.add(panel21, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel20, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- button1 ----
			button1.setText("Refresh");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel1.add(button1, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- button2 ----
			button2.setText("Promote to release");
			button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button2.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					button2MouseClicked(e);
				}
			});
			panel1.add(button2, new GridBagConstraints(12, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel20;
	private JPanel panel24;
	private JLabel label19;
	private JLabel label13;
	private JPanel panel5;
	private JLabel label14;
	private JLabel label15;
	private JScrollPane scrollPane5;
	private JList list5;
	private JPanel panel21;
	private JScrollPane scrollPane1;
	private JList list1;
	private JPanel panel1;
	private JButton button1;
	private JButton button2;

	// JFormDesigner - End of variables declaration //GEN-END:variables
	/**
	 * The Class WorklistMembersStatusWorker.
	 */
	class WorklistMembersStatusWorker extends SwingWorker<String, String> {

		/**
		 * Instantiates a new worklist members status worker.
		 */
		public WorklistMembersStatusWorker() {
			super();
			list5Model.clear();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected String doInBackground() throws Exception {
			try {
				List<WorkListMember> members = workList.getWorkListMembers();
				Collections.sort(members, new Comparator<WorkListMember>() {
					public int compare(WorkListMember f1, WorkListMember f2) {
						return f1.toString().compareTo(f2.toString());
					}
				});
				statuses = TerminologyProjectDAO.getWorkListMemberStatuses(workList, config,null);
				for (I_GetConceptData status : statuses.keySet()) {
					publish(status.toString() + " (" + statuses.get(status) + ")");
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(WorklistMemberStatusPanel.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				AceLog.getAppLog().alertAndLogException(e);
			}
			return "Done";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<String> chunks) {
			for (String objects : chunks) {
				list5Model.addElement(objects.toString());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		public void done() {
			try {
				get();
				list5.validate();
				list1.validate();
			} catch (Exception ignore) {
				AceLog.getAppLog().alertAndLogException(ignore);
			}
		}

	};
}
