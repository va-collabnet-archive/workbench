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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionMember;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.project.wizard.WizardLauncher;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowDefinitionManager;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.tigris.subversion.javahl.ProgressEvent;

/**
 * The Class PartitionDetailsPanel.
 * 
 * @author Guillermo Reynoso
 */
public class PartitionDetailsPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The partition. */
	private Partition partition;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The table model. */
	private DefaultTableModel tableModel;

	/** The list3 model. */
	private DefaultListModel list3Model;

	/** The destination. */
	private String destination;

	/** The business process. */
	private BusinessProcess businessProcess;

	/** The name. */
	private String name;

	/** The partition member. */
	private String partitionMember;

	/** The members worker. */
	private MembersWorker membersWorker;

	/**
	 * Instantiates a new partition details panel.
	 * 
	 * @param partition
	 *            the partition
	 * @param config
	 *            the config
	 */
	public PartitionDetailsPanel(Partition partition, I_ConfigAceFrame config) {
		initComponents();
		label11.setIcon(IconUtilities.helpIcon);
		label11.setText("");
		pBarW.setVisible(false);
		pBarW2.setVisible(false);
		this.partition = partition;
		this.config = config;
		textField1.setText(partition.getName());
		label5.setText(partition.getPartitionScheme(config).getName());
		partitionMember = label4.getText();
		updateList3Content();

		button5.setEnabled(false);

		tableModel = new DefaultTableModel();
		tableModel.addColumn("Member");
		membersTable.setModel(tableModel);
		TableRowSorter<DefaultTableModel> trs = new TableRowSorter<DefaultTableModel>(tableModel);
		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		trs.setSortKeys(sortKeys);
		membersTable.setRowSorter(trs);
		trs.setSortsOnUpdates(true);

		I_TermFactory termFactory = Terms.get();
		boolean isPartitioningManager = false;
		try {
			isPartitioningManager = TerminologyProjectDAO.checkPermissionForProject(config.getDbConfig().getUserConcept(), termFactory.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.PARTITIONING_MANAGER_ROLE.localize().getNid()), config);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		if (!isPartitioningManager) {
			button1.setVisible(false);
			button2.setVisible(false);
			button3.setVisible(false);
			button4.setVisible(false);
			button5.setVisible(false);
			button6.setVisible(false);
			textField1.setEditable(false);
			label6.setVisible(false);
		}
	}

	/**
	 * Update list3 content.
	 */
	private void updateList3Content() {
		list3Model = new DefaultListModel();
		List<WorkList> worklists = partition.getWorkLists();
		Collections.sort(worklists, new Comparator<WorkList>() {
			public int compare(WorkList f1, WorkList f2) {
				return f1.toString().compareTo(f2.toString());
			}
		});
		for (WorkList workList : worklists) {
			list3Model.addElement(workList);
		}
		list3.setModel(list3Model);
		list3.validate();
	}

	/**
	 * Update list2 content.
	 */
	private void updateList2Content() {
		while (tableModel.getRowCount() > 0) {
			tableModel.removeRow(0);
		}
		List<PartitionMember> members = partition.getPartitionMembers();
		label4.setText(partitionMember + " (" + members.size() + ")");
		Collections.sort(members, new Comparator<PartitionMember>() {
			public int compare(PartitionMember f1, PartitionMember f2) {
				return f1.toString().compareTo(f2.toString());
			}
		});
		for (PartitionMember member : members) {
			tableModel.addRow(new PartitionMember[] { member });
		}
		membersTable.revalidate();
	}

	/**
	 * Text field1 key typed.
	 * 
	 * @param e
	 *            the e
	 */
	private void textField1KeyTyped(KeyEvent e) {
		if (textField1.getText().equals(partition.getName())) {
			button5.setEnabled(false);
		} else {
			button5.setEnabled(true);
		}
	}

	/**
	 * Button5 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button5ActionPerformed(ActionEvent e) {
		partition.setName(textField1.getText());
		TerminologyProjectDAO.updatePartitionMetadata(partition, config);
		try {
			Terms.get().commit();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		button5.setEnabled(false);
		JOptionPane.showMessageDialog(this, "Partition saved!", "Message", JOptionPane.INFORMATION_MESSAGE);
		TranslationHelperPanel.refreshProjectPanelNode(config);
	}

	/**
	 * Button3 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button3ActionPerformed(ActionEvent e) {
		// retire partition
		int n = JOptionPane.showConfirmDialog(this, "Would you like to retire the partition?", "Confirmation", JOptionPane.YES_NO_OPTION);

		if (n == 0) {
			try {
				TerminologyProjectDAO.retirePartition(partition, config);
				Terms.get().commit();
				JOptionPane.showMessageDialog(this, "Partition retired!", "Message", JOptionPane.INFORMATION_MESSAGE);
				TranslationHelperPanel.refreshProjectPanelParentNode(config);
				TranslationHelperPanel.closeProjectDetailsTab(config);
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(this, e3.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e3.printStackTrace();
			}
		}
	}

	/**
	 * Gets the business process.
	 * 
	 * @param f
	 *            the f
	 * @return the business process
	 */
	private BusinessProcess getBusinessProcess(File f) {
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
			BusinessProcess processToLunch = (BusinessProcess) in.readObject();
			in.close();
			return processToLunch;

		} catch (FileNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ClassNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return null;
	}

	/** The clone list. */
	List<I_Work> cloneList = new ArrayList<I_Work>();

	/** The workflow definition. */
	private WorkflowDefinition workflowDefinition;

	/** The no name. */
	private String noName;

	/** The worker. */
	private SwingWorker<String, String> worker;

	/**
	 * Button2 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button2ActionPerformed(ActionEvent e) {
		// generate worklist
		WfComponentProvider wcp = new WfComponentProvider();
		List<WfUser> users = wcp.getUsers();
		WizardLauncher wl = new WizardLauncher();
		wl.launchWfWizard(users);
		HashMap<String, Object> hsRes = wl.getResult();
		List<WfRole> roles = null;
		noName = "no name " + UUID.randomUUID().toString();
		workflowDefinition = null;
		final ArrayList<WfMembership> workflowUserRoles = new ArrayList<WfMembership>();

		for (String key : hsRes.keySet()) {
			Object val = hsRes.get(key);
			if (key.equals("WDS")) {
				workflowDefinition = WorkflowDefinitionManager.readWfDefinition(((File) val).getName());
				roles = workflowDefinition.getRoles();

			}

			if (key.equals("WORKLIST_NAME")) {
				noName = (String) val;
			}
			if (key.equals("roles")) {
				roles = wcp.getRoles();
				users = wcp.getUsers();
				DefaultTableModel model = (DefaultTableModel) hsRes.get(key);
				for (int j = 1; j < model.getColumnCount(); j += 2) {
					WfRole role = null;
					for (WfRole wfRole : roles) {
						if (wfRole.getName().equals(model.getColumnName(j))) {
							role = wfRole;
							break;
						}
					}
					for (int i = 0; i < model.getRowCount(); i++) {
						Boolean sel = (Boolean) model.getValueAt(i, j);
						if (sel) {
							Boolean def = (Boolean) model.getValueAt(i, j + 1);
							WfUser user = null;
							for (WfUser wfUser : users) {
								if (wfUser.getId().equals(((WfUser) model.getValueAt(i, 0)).getId())) {
									user = wfUser;
									break;
								}
							}
							WfMembership workflowUserRole = new WfMembership(UUID.randomUUID(), user, role, def);
							workflowUserRoles.add(workflowUserRole);
						}
					}
				}
			}
		}

		final I_ShowActivity activity = Terms.get().newActivityPanel(true, config, "<html>Generating Worklist from partition", true);
		activity.setIndeterminate(true);
		final Long startTime = System.currentTimeMillis();
		activity.update();
		worker = new SwingWorker<String, String>() {
			@Override
			protected String doInBackground() throws Exception {

				try {
					TerminologyProjectDAO.generateWorkListFromPartition(partition, workflowDefinition, workflowUserRoles, noName, config, activity);
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(PartitionDetailsPanel.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				TranslationHelperPanel.refreshProjectPanelNode(config);
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					long endTime = System.currentTimeMillis();

					long elapsed = endTime - startTime;
					String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);

					activity.setProgressInfoUpper("Worklist created...");
					activity.setProgressInfoLower("Elapsed: " + elapsedStr);
					activity.complete();

				} catch (CancellationException ce) {
					activity.setProgressInfoLower("Canceled");
					try {
						activity.complete();
					} catch (ComputationCanceled e) {
						activity.setProgressInfoLower("Canceled");
					}
				} catch (Exception e) {
					activity.setProgressInfoLower("Canceled with error");
					AceLog.getAppLog().alertAndLogException(e);
				}
			}

		};
		worker.execute();
		activity.addStopActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				worker.cancel(true);
			}
		});
		try {
			ActivityViewer.addActivity(activity);
		} catch (InterruptedException i1) {
			// thread canceled, cancel db changes
			try {
				Terms.get().cancel();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
	}

	/**
	 * Sleep.
	 * 
	 * @param n
	 *            the n
	 */
	public static void sleep(int n) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		do {
			t1 = System.currentTimeMillis();
		} while ((t1 - t0) < (n * 1000));
	}

	/**
	 * Button1 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button1ActionPerformed(ActionEvent e) {
		// retire members
		if (membersTable.getSelectedRowCount() > 0) {
			int[] selectedIndices = membersTable.getSelectedRows();

			int n = JOptionPane.showConfirmDialog(this, "Would you like to retire these partition members?", "Confirmation", JOptionPane.YES_NO_OPTION);

			if (n == 0) {
				try {
					List<PartitionMember> membersToRetire = new ArrayList<PartitionMember>();
					List<WorkList> worklists = partition.getWorkLists();
					for (int i : selectedIndices) {
						int modelIndex = membersTable.convertRowIndexToModel(i);
						PartitionMember partitionMemberToRetire = (PartitionMember) tableModel.getValueAt(modelIndex, 0);
						if(!worklists.isEmpty()){
							for (WorkList workList : worklists) {
								List<WorkListMember> wlMembers = workList.getWorkListMembers();
								for (WorkListMember workListMember : wlMembers) {
									if (workListMember.getId() == partitionMemberToRetire.getId()) {
										JOptionPane.showConfirmDialog(this, "<html><p style=\"font-style:italic\">" + partitionMemberToRetire.getName() + 
												"</p>Partition member cannot be retired, because it has " +
												"<br>been delivered (in the translation worklfow) " +
												"<br>and is still active", "Confirmation", JOptionPane.WARNING_MESSAGE);
										return;
									}
									membersToRetire.add(partitionMemberToRetire);
								}
							}
						}else{
							membersToRetire.add(partitionMemberToRetire);
						}
						
					}
					for (PartitionMember partitionMember : membersToRetire) {
						TerminologyProjectDAO.retirePartitionMember(partitionMember, config);
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
				try {
					Terms.get().commit();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				updateList2Content();
				TranslationHelperPanel.refreshProjectPanelNode(config);
			}
		}
	}

	/**
	 * Button4 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button4ActionPerformed(ActionEvent e) {
		// add partition scheme
		String partitionSchemeName = JOptionPane.showInputDialog(null, "Enter Partition Scheme Name : ", "", 1);
		if (partitionSchemeName != null) {
			try {
				if (TerminologyProjectDAO.createNewPartitionScheme(partitionSchemeName, partition.getUids().iterator().next(), config) != null) {
					Terms.get().commit();
					TranslationHelperPanel.refreshProjectPanelNode(config);
					JOptionPane.showMessageDialog(this, "Partition scheme created!", "Message", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(this, e3.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e3.printStackTrace();
			}
		}
	}

	/**
	 * Label11 mouse clicked.
	 * 
	 * @param e
	 *            the e
	 */
	private void label11MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("PARTITION_DETAILS");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Tabbed pane1 state changed.
	 * 
	 * @param e
	 *            the e
	 */
	private void tabbedPane1StateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JTabbedPane) {
			JTabbedPane panel = (JTabbedPane) e.getSource();
			int index = panel.getSelectedIndex();
			String title = panel.getTitleAt(index);
			if (title.equals("Members")) {
				if (membersWorker == null || membersWorker.isDone()) {
					membersWorker = new MembersWorker();
					membersWorker.addPropertyChangeListener(new ProgressListener(progressBar1));
					membersWorker.execute();
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
		tabbedPane1 = new JTabbedPane();
		panel0 = new JPanel();
		panel1 = new JPanel();
		panel2 = new JPanel();
		label1 = new JLabel();
		panel11 = new JPanel();
		label2 = new JLabel();
		textField1 = new JTextField();
		label3 = new JLabel();
		label5 = new JLabel();
		panel10 = new JPanel();
		label8 = new JLabel();
		panel7 = new JPanel();
		button2 = new JButton();
		button3 = new JButton();
		button4 = new JButton();
		button5 = new JButton();
		pBarW = new JProgressBar();
		label11 = new JLabel();
		panel9 = new JPanel();
		label4 = new JLabel();
		scrollPane1 = new JScrollPane();
		membersTable = new JTable();
		panel12 = new JPanel();
		label9 = new JLabel();
		label6 = new JLabel();
		progressBar1 = new JProgressBar();
		panel6 = new JPanel();
		button1 = new JButton();
		panel13 = new JPanel();
		panel14 = new JPanel();
		label7 = new JLabel();
		scrollPane3 = new JScrollPane();
		list3 = new JList();
		panel15 = new JPanel();
		label10 = new JLabel();
		panel3 = new JPanel();
		button6 = new JButton();
		pBarW2 = new JProgressBar();

		// ======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

		// ======== tabbedPane1 ========
		{
			tabbedPane1.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					tabbedPane1StateChanged(e);
				}
			});

			// ======== panel0 ========
			{
				panel0.setLayout(new GridBagLayout());
				((GridBagLayout) panel0.getLayout()).columnWidths = new int[] { 0, 0 };
				((GridBagLayout) panel0.getLayout()).rowHeights = new int[] { 0, 0, 0 };
				((GridBagLayout) panel0.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
				((GridBagLayout) panel0.getLayout()).rowWeights = new double[] { 1.0, 0.0, 1.0E-4 };

				// ======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 0, 0 };
					((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0 };
					((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 1.0, 1.0, 1.0E-4 };
					((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

					// ======== panel2 ========
					{
						panel2.setLayout(new GridBagLayout());
						((GridBagLayout) panel2.getLayout()).columnWidths = new int[] { 0, 0 };
						((GridBagLayout) panel2.getLayout()).rowHeights = new int[] { 0, 0, 0 };
						((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
						((GridBagLayout) panel2.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };

						// ---- label1 ----
						label1.setText("Partition details");
						label1.setFont(new Font("Lucida Grande", Font.BOLD, 14));
						panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

						// ======== panel11 ========
						{
							panel11.setLayout(new GridBagLayout());
							((GridBagLayout) panel11.getLayout()).columnWidths = new int[] { 0, 0, 0 };
							((GridBagLayout) panel11.getLayout()).rowHeights = new int[] { 0, 0, 0 };
							((GridBagLayout) panel11.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
							((GridBagLayout) panel11.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };

							// ---- label2 ----
							label2.setText("Name:");
							panel11.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

							// ---- textField1 ----
							textField1.addKeyListener(new KeyAdapter() {
								@Override
								public void keyTyped(KeyEvent e) {
									textField1KeyTyped(e);
								}
							});
							panel11.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

							// ---- label3 ----
							label3.setText("Partition scheme");
							panel11.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

							// ---- label5 ----
							label5.setText("text");
							panel11.add(label5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						}
						panel2.add(panel11, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

					// ======== panel10 ========
					{
						panel10.setLayout(new GridBagLayout());
						((GridBagLayout) panel10.getLayout()).columnWidths = new int[] { 0, 0 };
						((GridBagLayout) panel10.getLayout()).rowHeights = new int[] { 0, 0 };
						((GridBagLayout) panel10.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
						((GridBagLayout) panel10.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

						// ---- label8 ----
						label8.setText("<html><body>\nClick \u2018Generate a new worklist\u2019 to create a new worklist<br><br>\n\nClick \u2018Retire partition\u2019 to retire the selected partition<br><br>\n\nCreate a new partition scheme by clicking the \u2018Add partition scheme\u2019 button\n</html>");
						panel10.add(label8, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel10, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}
				panel0.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

				// ======== panel7 ========
				{
					panel7.setLayout(new GridBagLayout());
					((GridBagLayout) panel7.getLayout()).columnWidths = new int[] { 130, 112, 0, 0, 0, 0, 0, 0 };
					((GridBagLayout) panel7.getLayout()).rowHeights = new int[] { 0, 0 };
					((GridBagLayout) panel7.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4 };
					((GridBagLayout) panel7.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

					// ---- button2 ----
					button2.setText("Generate WorkList");
					button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button2.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button2ActionPerformed(e);
						}
					});
					panel7.add(button2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

					// ---- button3 ----
					button3.setText("Retire partition");
					button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button3.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button3ActionPerformed(e);
						}
					});
					panel7.add(button3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

					// ---- button4 ----
					button4.setText("New partition scheme");
					button4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button4.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button4ActionPerformed(e);
						}
					});
					panel7.add(button4, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

					// ---- button5 ----
					button5.setText("Save");
					button5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button5.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button5ActionPerformed(e);
						}
					});
					panel7.add(button5, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
					panel7.add(pBarW, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

					// ---- label11 ----
					label11.setText("text");
					label11.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							label11MouseClicked(e);
						}
					});
					panel7.add(label11, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}
				panel0.add(panel7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Partition", panel0);

			// ======== panel9 ========
			{
				panel9.setLayout(new GridBagLayout());
				((GridBagLayout) panel9.getLayout()).columnWidths = new int[] { 363, 0, 0 };
				((GridBagLayout) panel9.getLayout()).rowHeights = new int[] { 0, 0, 0, 0, 0 };
				((GridBagLayout) panel9.getLayout()).columnWeights = new double[] { 1.0, 1.0, 1.0E-4 };
				((GridBagLayout) panel9.getLayout()).rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 1.0E-4 };

				// ---- label4 ----
				label4.setText("Partition members");
				panel9.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ======== scrollPane1 ========
				{
					scrollPane1.setViewportView(membersTable);
				}
				panel9.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ======== panel12 ========
				{
					panel12.setBackground(new Color(238, 238, 238));
					panel12.setLayout(new GridBagLayout());
					((GridBagLayout) panel12.getLayout()).columnWidths = new int[] { 0, 0 };
					((GridBagLayout) panel12.getLayout()).rowHeights = new int[] { 0, 0 };
					((GridBagLayout) panel12.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
					((GridBagLayout) panel12.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

					// ---- label9 ----
					label9.setText("<html><body>\nThe list of partition members is displayed\n</html>");
					panel12.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				}
				panel9.add(panel12, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

				// ---- label6 ----
				label6.setText("Control + click for selecting multiple members");
				label6.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
				panel9.add(label6, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- progressBar1 ----
				progressBar1.setVisible(false);
				panel9.add(progressBar1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

				// ======== panel6 ========
				{
					panel6.setLayout(new GridBagLayout());
					((GridBagLayout) panel6.getLayout()).columnWidths = new int[] { 0, 0 };
					((GridBagLayout) panel6.getLayout()).rowHeights = new int[] { 0, 0 };
					((GridBagLayout) panel6.getLayout()).columnWeights = new double[] { 0.0, 1.0E-4 };
					((GridBagLayout) panel6.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

					// ---- button1 ----
					button1.setText("Retire selected members");
					button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button1ActionPerformed(e);
						}
					});
					panel6.add(button1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}
				panel9.add(panel6, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 5), 0, 0));

				// ======== panel13 ========
				{
					panel13.setLayout(new GridBagLayout());
					((GridBagLayout) panel13.getLayout()).columnWidths = new int[] { 0, 0 };
					((GridBagLayout) panel13.getLayout()).rowHeights = new int[] { 0, 0 };
					((GridBagLayout) panel13.getLayout()).columnWeights = new double[] { 0.0, 1.0E-4 };
					((GridBagLayout) panel13.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };
				}
				panel9.add(panel13, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Members", panel9);

			// ======== panel14 ========
			{
				panel14.setLayout(new GridBagLayout());
				((GridBagLayout) panel14.getLayout()).columnWidths = new int[] { 419, 0, 0 };
				((GridBagLayout) panel14.getLayout()).rowHeights = new int[] { 0, 0, 0 };
				((GridBagLayout) panel14.getLayout()).columnWeights = new double[] { 1.0, 1.0, 1.0E-4 };
				((GridBagLayout) panel14.getLayout()).rowWeights = new double[] { 0.0, 1.0, 1.0E-4 };

				// ---- label7 ----
				label7.setText("WorkLists");
				panel14.add(label7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ======== scrollPane3 ========
				{
					scrollPane3.setViewportView(list3);
				}
				panel14.add(scrollPane3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ======== panel15 ========
				{
					panel15.setLayout(new GridBagLayout());
					((GridBagLayout) panel15.getLayout()).columnWidths = new int[] { 0, 0 };
					((GridBagLayout) panel15.getLayout()).rowHeights = new int[] { 0, 0, 0 };
					((GridBagLayout) panel15.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
					((GridBagLayout) panel15.getLayout()).rowWeights = new double[] { 1.0, 0.0, 1.0E-4 };

					// ---- label10 ----
					label10.setText("<html><body>\nThe list of worklists is displayed\n</html>");
					panel15.add(label10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));

					// ======== panel3 ========
					{
						panel3.setLayout(new GridBagLayout());
						((GridBagLayout) panel3.getLayout()).columnWidths = new int[] { 0, 0, 0 };
						((GridBagLayout) panel3.getLayout()).rowHeights = new int[] { 0, 0 };
						((GridBagLayout) panel3.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
						((GridBagLayout) panel3.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

						// ---- button6 ----
						button6.setText("Generate WorkList");
						button6.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button6.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button2ActionPerformed(e);
							}
						});
						panel3.add(button6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
						panel3.add(pBarW2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					}
					panel15.add(panel3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}
				panel14.add(panel15, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("WorkLists", panel14);

		}
		add(tabbedPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JTabbedPane tabbedPane1;
	private JPanel panel0;
	private JPanel panel1;
	private JPanel panel2;
	private JLabel label1;
	private JPanel panel11;
	private JLabel label2;
	private JTextField textField1;
	private JLabel label3;
	private JLabel label5;
	private JPanel panel10;
	private JLabel label8;
	private JPanel panel7;
	private JButton button2;
	private JButton button3;
	private JButton button4;
	private JButton button5;
	private JProgressBar pBarW;
	private JLabel label11;
	private JPanel panel9;
	private JLabel label4;
	private JScrollPane scrollPane1;
	private JTable membersTable;
	private JPanel panel12;
	private JLabel label9;
	private JLabel label6;
	private JProgressBar progressBar1;
	private JPanel panel6;
	private JButton button1;
	private JPanel panel13;
	private JPanel panel14;
	private JLabel label7;
	private JScrollPane scrollPane3;
	private JList list3;
	private JPanel panel15;
	private JLabel label10;
	private JPanel panel3;
	private JButton button6;
	private JProgressBar pBarW2;

	// JFormDesigner - End of variables declaration //GEN-END:variables
	/**
	 * The Class MembersWorker.
	 */
	class MembersWorker extends SwingWorker<ArrayList<PartitionMember>, PartitionMember> {

		/**
		 * Instantiates a new members worker.
		 */
		public MembersWorker() {
			super();
			while (tableModel.getRowCount() > 0) {
				tableModel.removeRow(0);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected ArrayList<PartitionMember> doInBackground() throws Exception {
			I_TermFactory termFactory = Terms.get();
			ArrayList<PartitionMember> result = new ArrayList<PartitionMember>();
			List<PartitionMember> members = partition.getPartitionMembers();
			label4.setText(partitionMember + " (" + members.size() + ")");
			Collections.sort(members, new Comparator<PartitionMember>() {
				public int compare(PartitionMember f1, PartitionMember f2) {
					return f1.toString().compareTo(f2.toString());
				}
			});
			for (PartitionMember member : members) {
				publish(member);
			}
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		protected void process(java.util.List<PartitionMember> chunks) {
			for (PartitionMember member : chunks) {
				tableModel.addRow(new Object[] { member });
			}

		};

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		public void done() {
			ArrayList<PartitionMember> inboxItems = null;
			try {
				inboxItems = get();
				membersTable.revalidate();
			} catch (Exception ignore) {
				//AceLog.getAppLog().alertAndLogException(ignore);
			}
		}
	};

	/**
	 * The listener interface for receiving progress events. The class that is
	 * interested in processing a progress event implements this interface, and
	 * the object created with that class is registered with a component using
	 * the component's <code>addProgressListener<code> method. When
	 * the progress event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see ProgressEvent
	 */
	class ProgressListener implements PropertyChangeListener {
		// Prevent creation without providing a progress bar.
		/**
		 * Instantiates a new progress listener.
		 */
		private ProgressListener() {
		}

		/**
		 * Instantiates a new progress listener.
		 * 
		 * @param progressBar
		 *            the progress bar
		 */
		public ProgressListener(JProgressBar progressBar) {
			this.progressBar = progressBar;
			this.progressBar.setVisible(true);
			this.progressBar.setIndeterminate(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
		 * PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
				progressBar.setIndeterminate(false);
				progressBar.setVisible(false);
			}
		}

		/** The progress bar. */
		private JProgressBar progressBar;
	}
}
