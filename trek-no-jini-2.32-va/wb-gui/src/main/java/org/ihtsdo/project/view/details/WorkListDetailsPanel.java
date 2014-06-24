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
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.I_TerminologyProject.Type;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.project.wizard.DataGridCollectorFromList;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.workflow.api.ProjectBI.ProjectType;

/**
 * The Class WorkListDetailsPanel.
 * 
 * @author Guillermo Reynoso
 */
public class WorkListDetailsPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The work list. */
	private WorkList workList;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The transl project. */
	private I_TerminologyProject translProject;

	/** The members worker. */
	private WorklistMembersWorker membersWorker;

	/** The members table model. */
	private DefaultTableModel membersTableModel;

	private HashMap<String, List<WfMembership>> rolesMap;

	private HashMap<String, String> defaultRoleUsers;

	/**
	 * Instantiates a new work list details panel.
	 * 
	 * @param workList
	 *            the work list
	 * @param config
	 *            the config
	 * @param worker
	 *            the worker
	 */
	public WorkListDetailsPanel(WorkList workList, I_ConfigAceFrame config, I_Work worker) {
		initComponents();
		this.workList = workList;
		this.config = config;
		translProject = TerminologyProjectDAO.getProjectForWorklist(workList, config);
		label13.setIcon(IconUtilities.helpIcon);
		label13.setText("");
		pBarW.setVisible(false);
		pBarBP.setVisible(false);
		pBarD.setVisible(false);
		I_TermFactory tf = Terms.get();

		// -- buttons are no longer necessary

		button2.setVisible(false);
		button1.setVisible(false);

		try {
			WorklistMemberStatusPanel worklistMemberStatusPanel1 = new WorklistMemberStatusPanel(workList, config);
			worklistMemberStatusPanel1.revalidate();

			tabbedPane1.addTab("Status", worklistMemberStatusPanel1);
			textField1.setText(workList.getName());
			label6.setText(workList.getPartition().getName());
			label16.setText("Partition: " + workList.getPartition().getName());
			label17.setText("Partition: " + workList.getPartition().getName());
			label18.setText("Partition: " + workList.getPartition().getName());
			// label12.setText(workList.getBusinessProcess().getName());
			configMembersTable();
			updateMembersTable();

			for (String address : config.getAddressesList()) {
				if (address.trim().endsWith(".inbox")) {
					comboBox1.addItem(address);
				}
			}

			// comboBox1.setSelectedItem(workList.getDestination());

			ProjectPermissionsAPI permissionApi = new ProjectPermissionsAPI(config);

			boolean isPartitioningManager = permissionApi.checkPermissionForProject(config.getDbConfig().getUserConcept(), tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
					tf.getConcept(ArchitectonicAuxiliary.Concept.PARTITIONING_MANAGER_ROLE.localize().getNid()));

			if (!isPartitioningManager) {
				button1.setVisible(false);
				button2.setVisible(false);
				button4.setVisible(false);
				button6.setVisible(false);
				button8.setVisible(false);
				comboBox1.setEditable(false);
				textField1.setEditable(false);
			}

			WorklistMemberReAssignment reassigPanel = new WorklistMemberReAssignment(workList, config);

			tabbedPane1.addTab("Re assign", reassigPanel);
			reassigPanel.revalidate();
			tabbedPane1.remove(2);
			tabbedPane1.remove(1);
			tabbedPane1.revalidate();

			if (!TerminologyProjectDAO.getProjectForWorklist(workList, config).getProjectType().equals(Type.TRANSLATION)) {
				button5.setVisible(false);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			AceLog.getAppLog().alertAndLogException(e);
		}

		// Roles table!
		loadRolesTable();
	}

	/**
	 * Load roles table.
	 */
	private void loadRolesTable() {

		String wfDefinitionName = workList.getWorkflowDefinition().getName();
		wfDefinitionLabel.setText("Workflow definition: " + wfDefinitionName);

		List<WfMembership> wur = workList.getWorkflowUserRoles();
		rolesMap = new HashMap<String, List<WfMembership>>();
		defaultRoleUsers = new HashMap<String, String>();
		if (wur == null || wur.size() == 0) {
			button8.setVisible(false);
			return;
		}
		for (WfMembership wm : wur) {
			if (rolesMap.containsKey(wm.getRole().getName())) {
				List<WfMembership> user = rolesMap.get(wm.getRole().getName());
				user.add(wm);
				rolesMap.put(wm.getRole().getName(), user);
			} else {
				List<WfMembership> wml = new ArrayList<WfMembership>();
				wml.add(wm);
				rolesMap.put(wm.getRole().getName(), wml);
			}
			if (wm.isDefaultAssignment()) {
				defaultRoleUsers.put(wm.getRole().getName(), wm.getUser().getUsername());
			}
		}
		Object[] keys = rolesMap.keySet().toArray();
		DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[] { "Role", "Users" }) {
			Class<?>[] columnTypes = new Class<?>[] { String.class, String.class };
			boolean[] columnEditable = new boolean[] { false, false };

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnEditable[columnIndex];
			}
		};
		for (int i = 0; i < rolesMap.size(); i++) {
			List<WfMembership> list = rolesMap.get(keys[i]);
			StringBuffer users = new StringBuffer();
			for (int j = 0; j < list.size(); j++) {
				if (j == 0 && list.size() > 1) {
					users.append(list.get(j).getUser().getUsername() + (list.get(j).isDefaultAssignment() ? "*" : ""));
				} else if (j != 0 && list.size() > 1) {
					users.append("," + list.get(j).getUser().getUsername() + (list.get(j).isDefaultAssignment() ? "*" : ""));
				} else if (list.size() == 1) {
					users.append(list.get(j).getUser().getUsername());
				}
			}
			model.addRow(new Object[] { keys[i], users.toString() });
		}
		rolesTable.setModel(model);
		rolesTable.updateUI();
	}

	/**
	 * Config members table.
	 */
	private void configMembersTable() {
		membersTableModel = new DefaultTableModel();
		membersTableModel.addColumn("WorkList member");
		membersTableModel.addColumn("Status");
		membersTableModel.addColumn("Destination");

		membersTable.setModel(membersTableModel);

		membersTable.setDefaultEditor(membersTableModel.getColumnClass(0), null);
		membersTable.setDefaultEditor(membersTableModel.getColumnClass(1), null);
		membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		membersTable.setAutoCreateRowSorter(true);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(membersTableModel);
		membersTable.setRowSorter(sorter);
	}

	/**
	 * Update members table.
	 */
	private void updateMembersTable() {

		if (membersWorker != null && !membersWorker.isDone()) {
			membersWorker.cancel(true);
			membersWorker = null;
		}
		membersWorker = new WorklistMembersWorker(membersTable, membersTableModel, translProject, workList, config);
		membersWorker.addPropertyChangeListener(new ProgressListener(progressBar1));
		membersWorker.execute();
	}

	/**
	 * Text field1 key typed.
	 * 
	 * @param e
	 *            the e
	 */
	private void textField1KeyTyped(KeyEvent e) {
		if (textField1.getText().equals(workList.getName())) {
			button4.setEnabled(false);
		} else {
			button4.setEnabled(true);
		}
	}

	/**
	 * Button4 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button4ActionPerformed(ActionEvent e) {
		workList.setName(textField1.getText());
		TerminologyProjectDAO.updateWorkListMetadata(workList, config);
		try {
			Terms.get().commit();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		button4.setEnabled(false);
		JOptionPane.showMessageDialog(this, "WorkList saved!", "Message", JOptionPane.INFORMATION_MESSAGE);
		TranslationHelperPanel.refreshProjectPanelNode(config);
	}

	/**
	 * Button6 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button6ActionPerformed(ActionEvent e) {
		config.getChildrenExpandedNodes().clear();
		pBarD.setMinimum(0);
		pBarD.setMaximum(100);
		pBarD.setIndeterminate(true);
		pBarD.setVisible(true);
		pBarD.repaint();
		pBarD.revalidate();
		panel4.repaint();
		panel4.revalidate();
		repaint();
		revalidate();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				Thread appThr = new Thread() {
					public void run() {
						try {
							// workList.setDestination((String) comboBox1
							// .getSelectedItem());
							// TerminologyProjectDAO.updateWorkListMetadata(
							// workList, config);
							// Terms.get().commit();
							// for (WorkListMember member : workList
							// .getWorkListMembers()) {
							// if (ArchitectonicAuxiliary.Concept.ADJUDICATED
							// .getUids().contains(
							// member.getActivityStatus())) {
							// member.setDestination(workList
							// .getDestination());
							// //
							// member.getBusinessProcessWithAttachments().setDestination(workList.getDestination());
							// TerminologyProjectDAO
							// .updateWorkListMemberMetadata(
							// member, config);
							// }
							// }
							// Terms.get().commit();
						} catch (Exception e1) {
							e1.printStackTrace();
							JOptionPane.showMessageDialog(WorkListDetailsPanel.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
						pBarD.setVisible(false);
						button6.setEnabled(false);
						JOptionPane.showMessageDialog(WorkListDetailsPanel.this, "New destination is saved!", "Message", JOptionPane.INFORMATION_MESSAGE);

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								TranslationHelperPanel.refreshProjectPanelNode(config);
							}
						});
					}
				};
				appThr.start();
			}
		});
	}

	/**
	 * Button1 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button1ActionPerformed(ActionEvent e) {
		// int n = JOptionPane
		// .showConfirmDialog(
		// this,
		// "Would you like attach a different\n business process to this worklist?",
		// "Confirmation", JOptionPane.YES_NO_OPTION);
		//
		// if (n == 0) {
		// File businessProcessFile = null;
		// JFileChooser chooser = new JFileChooser();
		// chooser.setCurrentDirectory(new File("."));
		//
		// chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
		// public boolean accept(File f) {
		// return f.getName().toLowerCase().endsWith(".bp");
		// }
		//
		// public String getDescription() {
		// return "BP Business Process files";
		// }
		// });
		//
		// int r = chooser.showOpenDialog(new JFrame());
		// if (r == JFileChooser.APPROVE_OPTION) {
		// businessProcessFile = chooser.getSelectedFile();
		// businessProcess = getBusinessProcess(businessProcessFile);
		//
		// config.getChildrenExpandedNodes().clear();
		// pBarBP.setMinimum(0);
		// pBarBP.setMaximum(100);
		// pBarBP.setIndeterminate(true);
		// pBarBP.setVisible(true);
		// pBarBP.repaint();
		// pBarBP.revalidate();
		// panel11.repaint();
		// panel11.revalidate();
		// repaint();
		// revalidate();
		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		//
		// Thread appThr = new Thread() {
		// public void run() {
		// // try {
		// // workList.setBusinessProcess(businessProcess);
		// // TerminologyProjectDAO
		// // .updateWorkListMetadata(workList,
		// // config);
		// // Terms.get().commit();
		// // } catch (Exception e) {
		// // AceLog.getAppLog().alertAndLogException(e);
		// // JOptionPane.showMessageDialog(
		// // WorkListDetailsPanel.this,
		// // e.getMessage(), "Error",
		// // JOptionPane.ERROR_MESSAGE);
		// // }
		// pBarBP.setVisible(false);
		// label12.setText(businessProcess.getName());
		// JOptionPane
		// .showMessageDialog(
		// WorkListDetailsPanel.this,
		// "The new business process is attached.",
		// "Success",
		// JOptionPane.INFORMATION_MESSAGE);
		//
		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		// TranslationHelperPanel
		// .refreshProjectPanelNode(config);
		// }
		// });
		// }
		// };
		// appThr.start();
		// }
		// });
		// }
		// }
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

	/**
	 * Button3 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button3ActionPerformed(ActionEvent e) {
		// retire workList
		int n = JOptionPane.showConfirmDialog(this, "Would you like to retire the worklist?", "Confirmation", JOptionPane.YES_NO_OPTION);

		if (n == 0) {
			try {
				pBarW.setMinimum(0);
				pBarW.setMaximum(100);
				pBarW.setIndeterminate(true);
				pBarW.setVisible(true);
				pBarW.repaint();
				pBarW.revalidate();
				panel7.repaint();
				panel7.revalidate();
				TerminologyProjectDAO.retireWorkList(workList, config);
				Terms.get().commit();
				TranslationHelperPanel.refreshProjectPanelParentNode(config);
				pBarW.setVisible(false);
				JOptionPane.showMessageDialog(this, "WorkList retired!", "Message", JOptionPane.INFORMATION_MESSAGE);
				TranslationHelperPanel.closeProjectDetailsTab(config);
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(this, e3.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e3.printStackTrace();
			}
		}
	}

	/**
	 * Button2 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button2ActionPerformed(ActionEvent e) {
		// int n = JOptionPane.showConfirmDialog(this,
		// "Would you like to deliver the worklist?", "Confirmation",
		// JOptionPane.YES_NO_OPTION);
		//
		// if (n == 0) {
		// try {
		// config.getChildrenExpandedNodes().clear();
		// pBarW.setMinimum(0);
		// pBarW.setMaximum(100);
		// pBarW.setIndeterminate(true);
		// pBarW.setVisible(true);
		// pBarW.repaint();
		// pBarW.revalidate();
		// panel7.repaint();
		// panel7.revalidate();
		// repaint();
		// revalidate();
		// final I_Work transactionIndependentClone =
		// worker.getTransactionIndependentClone();
		// transactionIndependentClone.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(),
		// Terms.get().getActiveAceFrameConfig());
		//
		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		// try {
		// TerminologyProjectDAO.deliverWorklistBusinessProcessToOutbox(workList,
		// transactionIndependentClone);
		// Terms.get().commit();
		// } catch (Exception e) {
		// AceLog.getAppLog().alertAndLogException(e);
		// JOptionPane.showMessageDialog(WorkListDetailsPanel.this,
		// e.getMessage(),
		// "Error", JOptionPane.ERROR_MESSAGE);
		// }
		// pBarW.setVisible(false);
		// JOptionPane.showMessageDialog(WorkListDetailsPanel.this,
		// "Assignments delivered!", "Message",JOptionPane.INFORMATION_MESSAGE);
		//
		// TranslationHelperPanel.refreshProjectPanelNode(config);
		// }
		// });
		// } catch (Exception e3) {
		// e3.printStackTrace();
		// JOptionPane.showMessageDialog(this, e3.getMessage(), "Error",
		// JOptionPane.ERROR_MESSAGE);
		// }
		// }
	}

	/**
	 * Button5 action performed.
	 */
	private void button5ActionPerformed() {
		showHistory();
	}

	/**
	 * Show history.
	 */
	private void showHistory() {
		if (membersTable.getSelectedRow() > -1) {
			DefaultTableModel model = (DefaultTableModel) membersTable.getModel();
			int rowModel = membersTable.convertRowIndexToModel(membersTable.getSelectedRow());
			WorkListMember member = (WorkListMember) model.getValueAt(rowModel, 0);
			WorklistMemberHistoryFrame memberFrame = new WorklistMemberHistoryFrame("History of: " + member.toString());
			memberFrame.refreshPanel(member, translProject, null, null);
			memberFrame.setVisible(true);
		}
	}

	/**
	 * Members table mouse clicked.
	 * 
	 * @param e
	 *            the e
	 */
	private void membersTableMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			showHistory();
		}
	}

	/**
	 * Button7 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button7ActionPerformed(ActionEvent e) {
		// refresh
		updateMembersTable();
	}

	/**
	 * Label13 mouse clicked.
	 * 
	 * @param e
	 *            the e
	 */
	private void label13MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("WORKLIST_DETAILS");
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
				updateMembersTable();
			}
		}
	}

	private void button8ActionPerformed(ActionEvent e) {

		WorkflowDefinition wd = workList.getWorkflowDefinition();
		final DataGridCollectorFromList dataGrid = new DataGridCollectorFromList(wd.getRoles(), new WfComponentProvider().getUsers());
		dataGrid.setData(rolesMap, defaultRoleUsers);
		dataGrid.setKey("roles");
		final JDialog dialog = new JDialog();
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setMinimumSize(new Dimension(800, 600));
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenSize = tk.getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth = screenSize.width;
		dialog.setLocation(screenWidth / 4, screenHeight / 4);

		JButton button = new JButton("Save");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				DefaultTableModel model = null;
				try {
					model = (DefaultTableModel) (dataGrid.getData().get("roles"));
					if (model == null)
						return;
					final ArrayList<WfMembership> workflowUserRoles = new ArrayList<WfMembership>();
					WfComponentProvider wcp = new WfComponentProvider();

					for (int j = 1; j < model.getColumnCount(); j += 2) {

						WfRole role = null;
						for (WfRole wfRole : wcp.getRoles()) {
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
								for (WfUser wfUser : wcp.getUsers()) {
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
					workList.setWorkflowUserRoles(workflowUserRoles);
					TerminologyProjectDAO.updateWorklistAndMembers(workList, config);
					dialog.dispose();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(dialog, e1.getMessage());
				}
			}
		});
		dialog.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		dataGrid.setMinimumSize(new Dimension(750, 400));
		dialog.getContentPane().add(dataGrid, gbc);
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		dialog.getContentPane().add(button, gbc);
		dialog.setVisible(true);
		loadRolesTable();

	}

	private void refreshRolesActionPerformed(ActionEvent e) {
		TerminologyProjectDAO.workListCache.remove(workList.getUids().iterator().next());
		loadRolesTable();
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
		panel17 = new JPanel();
		label2 = new JLabel();
		textField1 = new JTextField();
		label3 = new JLabel();
		label6 = new JLabel();
		panel16 = new JPanel();
		label11 = new JLabel();
		panel7 = new JPanel();
		button2 = new JButton();
		pBarW = new JProgressBar();
		button4 = new JButton();
		label13 = new JLabel();
		panel8 = new JPanel();
		label7 = new JLabel();
		panel10 = new JPanel();
		label12 = new JLabel();
		label16 = new JLabel();
		panel9 = new JPanel();
		label4 = new JLabel();
		panel11 = new JPanel();
		button1 = new JButton();
		pBarBP = new JProgressBar();
		panel3 = new JPanel();
		panel12 = new JPanel();
		label8 = new JLabel();
		panel13 = new JPanel();
		comboBox1 = new JComboBox();
		panel18 = new JPanel();
		panel22 = new JPanel();
		label17 = new JLabel();
		label5 = new JLabel();
		panel19 = new JPanel();
		panel4 = new JPanel();
		button6 = new JButton();
		pBarD = new JProgressBar();
		panel14 = new JPanel();
		panel23 = new JPanel();
		label9 = new JLabel();
		label18 = new JLabel();
		membersTableScrollPanel = new JScrollPane();
		membersTable = new JTable();
		panel15 = new JPanel();
		label10 = new JLabel();
		panel6 = new JPanel();
		button5 = new JButton();
		button7 = new JButton();
		progressBar1 = new JProgressBar();
		rolesPanel = new JPanel();
		wfDefinitionLabel = new JLabel();
		scrollPane1 = new JScrollPane();
		rolesTable = new JTable();
		panel5 = new JPanel();
		button8 = new JButton();
		refreshRoles = new JButton();

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== tabbedPane1 ========
		{
			tabbedPane1.setBackground(new Color(238, 238, 238));
			tabbedPane1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			tabbedPane1.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					tabbedPane1StateChanged(e);
				}
			});

			//======== panel0 ========
			{
				panel0.setLayout(new GridBagLayout());
				((GridBagLayout)panel0.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel0.getLayout()).rowHeights = new int[] {0, 0, 0};
				((GridBagLayout)panel0.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel0.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

				//======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

					//======== panel2 ========
					{
						panel2.setLayout(new GridBagLayout());
						((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {365, 0};
						((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 24, 0};
						((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

						//---- label1 ----
						label1.setText("Worklist details");
						label1.setFont(new Font("Lucida Grande", Font.BOLD, 14));
						panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== panel17 ========
						{
							panel17.setLayout(new GridBagLayout());
							((GridBagLayout)panel17.getLayout()).columnWidths = new int[] {0, 0, 0};
							((GridBagLayout)panel17.getLayout()).rowHeights = new int[] {0, 0, 0};
							((GridBagLayout)panel17.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
							((GridBagLayout)panel17.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

							//---- label2 ----
							label2.setText("Name:");
							panel17.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- textField1 ----
							textField1.addKeyListener(new KeyAdapter() {
								@Override
								public void keyTyped(KeyEvent e) {
									textField1KeyTyped(e);
								}
							});
							panel17.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//---- label3 ----
							label3.setText("Partition:");
							panel17.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 5), 0, 0));

							//---- label6 ----
							label6.setText("text");
							panel17.add(label6, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						panel2.add(panel17, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));
					}
					panel1.add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//======== panel16 ========
					{
						panel16.setLayout(new GridBagLayout());
						((GridBagLayout)panel16.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel16.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel16.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel16.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

						//---- label11 ----
						label11.setText("<html><body>\nClick \u2018Deliver assignments\u2019 for submitting the selected worklist to a specified user<br><br>\n\nClick \u2018Retire worklist\u2019 to retire a list. However, it will not be retired unless all members have been delivered or are inactive\n</html>");
						panel16.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel16, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel0.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel7 ========
				{
					panel7.setLayout(new GridBagLayout());
					((GridBagLayout)panel7.getLayout()).columnWidths = new int[] {0, 0, 0, 108, 0, 40, 0, 0, 0};
					((GridBagLayout)panel7.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel7.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel7.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button2 ----
					button2.setText("Deliver assignements");
					button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button2.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button2ActionPerformed(e);
						}
					});
					panel7.add(button2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- pBarW ----
					pBarW.setIndeterminate(true);
					panel7.add(pBarW, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button4 ----
					button4.setText("Save");
					button4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button4.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button4ActionPerformed(e);
						}
					});
					panel7.add(button4, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label13 ----
					label13.setText("text");
					label13.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							label13MouseClicked(e);
						}
					});
					panel7.add(label13, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel0.add(panel7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("WorkList", panel0);


			//======== panel8 ========
			{
				panel8.setLayout(new GridBagLayout());
				((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0, 0};
				((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
				((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4};

				//---- label7 ----
				label7.setText("Business process");
				panel8.add(label7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel10 ========
				{
					panel10.setLayout(new GridBagLayout());
					((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {270, 0};
					((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

					//---- label12 ----
					label12.setText("text");
					panel10.add(label12, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label16 ----
					label16.setText("text");
					panel10.add(label16, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel8.add(panel10, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel9 ========
				{
					panel9.setLayout(new GridBagLayout());
					((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {230, 0};
					((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

					//---- label4 ----
					label4.setText("<html><body>\nThe business process that will be used in the workflow is displayed<br><br>\n\nClick \u2018Replace BP file\u2019  for changing business process\n</html>");
					panel9.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel8.add(panel9, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel11 ========
				{
					panel11.setLayout(new GridBagLayout());
					((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button1 ----
					button1.setText("Replace BP file");
					button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button1ActionPerformed(e);
						}
					});
					panel11.add(button1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- pBarBP ----
					pBarBP.setIndeterminate(true);
					panel11.add(pBarBP, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel8.add(panel11, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel3 ========
				{
					panel3.setLayout(new GridBagLayout());
					((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
				}
				panel8.add(panel3, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Business process", panel8);


			//======== panel12 ========
			{
				panel12.setLayout(new GridBagLayout());
				((GridBagLayout)panel12.getLayout()).columnWidths = new int[] {0, 230, 0};
				((GridBagLayout)panel12.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
				((GridBagLayout)panel12.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel12.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

				//---- label8 ----
				label8.setText("Initial destination:");
				panel12.add(label8, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel13 ========
				{
					panel13.setLayout(new GridBagLayout());
					((GridBagLayout)panel13.getLayout()).columnWidths = new int[] {275, 0, 0};
					((GridBagLayout)panel13.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel13.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel13.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					panel13.add(comboBox1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel12.add(panel13, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel18 ========
				{
					panel18.setLayout(new GridBagLayout());
					((GridBagLayout)panel18.getLayout()).columnWidths = new int[] {148, 0};
					((GridBagLayout)panel18.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel18.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel18.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
				}
				panel12.add(panel18, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel22 ========
				{
					panel22.setLayout(new GridBagLayout());
					((GridBagLayout)panel22.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel22.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel22.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel22.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label17 ----
					label17.setText("text");
					panel22.add(label17, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel12.add(panel22, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label5 ----
				label5.setText("<html><body>\nSelect the initial destination by choosing the desired destination option displayed in the combo box<br><br>\n\nPress \u2018Save\u201d for persisting changes\n</html>");
				panel12.add(label5, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel19 ========
				{
					panel19.setLayout(new GridBagLayout());
					((GridBagLayout)panel19.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel19.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel19.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel19.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
				}
				panel12.add(panel19, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== panel4 ========
				{
					panel4.setLayout(new GridBagLayout());
					((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button6 ----
					button6.setText("Save");
					button6.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button6.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button6ActionPerformed(e);
						}
					});
					panel4.add(button6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- pBarD ----
					pBarD.setIndeterminate(true);
					panel4.add(pBarD, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel12.add(panel4, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Destination", panel12);


			//======== panel14 ========
			{
				panel14.setLayout(new GridBagLayout());
				((GridBagLayout)panel14.getLayout()).columnWidths = new int[] {353, 170, 0};
				((GridBagLayout)panel14.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel14.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
				((GridBagLayout)panel14.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//======== panel23 ========
				{
					panel23.setLayout(new GridBagLayout());
					((GridBagLayout)panel23.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel23.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel23.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel23.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label9 ----
					label9.setText("WorkList members");
					panel23.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label18 ----
					label18.setText("text");
					panel23.add(label18, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel14.add(panel23, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== membersTableScrollPanel ========
				{

					//---- membersTable ----
					membersTable.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							membersTableMouseClicked(e);
						}
					});
					membersTableScrollPanel.setViewportView(membersTable);
				}
				panel14.add(membersTableScrollPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel15 ========
				{
					panel15.setLayout(new GridBagLayout());
					((GridBagLayout)panel15.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel15.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)panel15.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel15.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

					//---- label10 ----
					label10.setText("<html><body> The worklist members are displayed </html>");
					panel15.add(label10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 5, 0), 0, 0));
				}
				panel14.add(panel15, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel6 ========
				{
					panel6.setLayout(new GridBagLayout());
					((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button5 ----
					button5.setText("View Member History");
					button5.setMnemonic('V');
					button5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button5.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button5ActionPerformed();
						}
					});
					panel6.add(button5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button7 ----
					button7.setText("Refresh");
					button7.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button7.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button7ActionPerformed(e);
						}
					});
					panel6.add(button7, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel14.add(panel6, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- progressBar1 ----
				progressBar1.setVisible(false);
				panel14.add(progressBar1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Members", panel14);


			//======== rolesPanel ========
			{
				rolesPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
				rolesPanel.setLayout(new GridBagLayout());
				((GridBagLayout)rolesPanel.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)rolesPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)rolesPanel.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)rolesPanel.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//---- wfDefinitionLabel ----
				wfDefinitionLabel.setText("Workflow definition: ");
				rolesPanel.add(wfDefinitionLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== scrollPane1 ========
				{

					//---- rolesTable ----
					rolesTable.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
							"Role", "Users"
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							String.class, String.class
						};
						boolean[] columnEditable = new boolean[] {
							false, false
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
						@Override
						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return columnEditable[columnIndex];
						}
					});
					{
						TableColumnModel cm = rolesTable.getColumnModel();
						cm.getColumn(0).setPreferredWidth(150);
					}
					scrollPane1.setViewportView(rolesTable);
				}
				rolesPanel.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel5 ========
				{
					panel5.setLayout(new GridBagLayout());
					((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
					((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button8 ----
					button8.setText("Edit Roles");
					button8.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button8.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button8ActionPerformed(e);
						}
					});
					panel5.add(button8, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- refreshRoles ----
					refreshRoles.setText("Refresh");
					refreshRoles.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							refreshRolesActionPerformed(e);
						}
					});
					panel5.add(refreshRoles, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				rolesPanel.add(panel5, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Roles", rolesPanel);

		}
		add(tabbedPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JTabbedPane tabbedPane1;
	private JPanel panel0;
	private JPanel panel1;
	private JPanel panel2;
	private JLabel label1;
	private JPanel panel17;
	private JLabel label2;
	private JTextField textField1;
	private JLabel label3;
	private JLabel label6;
	private JPanel panel16;
	private JLabel label11;
	private JPanel panel7;
	private JButton button2;
	private JProgressBar pBarW;
	private JButton button4;
	private JLabel label13;
	private JPanel panel8;
	private JLabel label7;
	private JPanel panel10;
	private JLabel label12;
	private JLabel label16;
	private JPanel panel9;
	private JLabel label4;
	private JPanel panel11;
	private JButton button1;
	private JProgressBar pBarBP;
	private JPanel panel3;
	private JPanel panel12;
	private JLabel label8;
	private JPanel panel13;
	private JComboBox comboBox1;
	private JPanel panel18;
	private JPanel panel22;
	private JLabel label17;
	private JLabel label5;
	private JPanel panel19;
	private JPanel panel4;
	private JButton button6;
	private JProgressBar pBarD;
	private JPanel panel14;
	private JPanel panel23;
	private JLabel label9;
	private JLabel label18;
	private JScrollPane membersTableScrollPanel;
	private JTable membersTable;
	private JPanel panel15;
	private JLabel label10;
	private JPanel panel6;
	private JButton button5;
	private JButton button7;
	private JProgressBar progressBar1;
	private JPanel rolesPanel;
	private JLabel wfDefinitionLabel;
	private JScrollPane scrollPane1;
	private JTable rolesTable;
	private JPanel panel5;
	private JButton button8;
	private JButton refreshRoles;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}

class WorklistMembersWorker extends SwingWorker<String, Object[]> {

	private JTable membersTable;
	private DefaultTableModel model;
	private I_TerminologyProject translProject;
	private WorkList workList;
	private I_ConfigAceFrame config;

	public WorklistMembersWorker(JTable membersTable, DefaultTableModel model, I_TerminologyProject translProject, WorkList workList, I_ConfigAceFrame config) {
		super();
		while (model.getRowCount() > 0) {
			model.removeRow(0);
		}
		this.membersTable = membersTable;
		this.model = model;
		this.translProject = translProject;
		this.workList = workList;
		this.config = config;
	}

	@Override
	protected String doInBackground() throws Exception {
		try {
			translProject = TerminologyProjectDAO.getProjectForWorklist(workList, config);

			List<WorkListMember> members = workList.getWorkListMembers();
			Collections.sort(members, new Comparator<WorkListMember>() {
				public int compare(WorkListMember f1, WorkListMember f2) {
					return f1.toString().compareTo(f2.toString());
				}
			});
			for (WorkListMember member : members) {
				I_GetConceptData activityStatus = member.getActivityStatus();
				WfUser destination = member.getWfInstance().getDestination();
				publish(new Object[] { member, activityStatus.toString(), destination.toString() });
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
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
		String inboxItems = null;
		try {
			inboxItems = get();
			membersTable.revalidate();
			membersTable.repaint();
		} catch (Exception ignore) {
			// ignorAceLog.getAppLog().alertAndLogException(e);
		}
	}

}
