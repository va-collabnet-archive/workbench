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

package org.ihtsdo.translation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.ListItemBean;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.TranslationSearchHelper;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.view.NacWorkListChooser;
import org.ihtsdo.project.view.PanelHelperFactory;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.translation.ui.TranslationConceptEditorRO;
import org.ihtsdo.translation.ui.ZebraJTable;

/**
 * The Class ListComponentViewerPanel.
 * 
 * @author Guillermo Reynoso
 */
public class ListComponentViewerPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6140825319002970607L;

	/** The project combo model. */
	private DefaultComboBoxModel projectComboModel;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The lst table model. */
	private DefaultTableModel lstTableModel;

	/** The columns. */
	private String[] columns;

	/**
	 * Instantiates a new list component viewer panel.
	 * 
	 * @param config
	 *            the config
	 */
	public ListComponentViewerPanel(I_ConfigAceFrame config) {
		this.config = config;
		initComponents();
		initCustomComponents();

	}

	/**
	 * The listener interface for receiving menuItem events. The class that is
	 * interested in processing a menuItem event implements this interface, and
	 * the object created with that class is registered with a component using
	 * the component's <code>addMenuItemListener<code> method. When
	 * the menuItem event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see MenuItemEvent
	 */
	class MenuItemListener implements ActionListener {

		/** The nodes. */
		private HashSet<Integer> nodes;

		/** The acc event. */
		private ActionEvent accEvent;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (nodes != null) {

				
					final I_Work tworker;
					if (config.getWorker().isExecuting()) {
                                try {
                                    tworker = config.getWorker().getTransactionIndependentClone();
                                    tworker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), config);
                                } catch (IOException ex) {
                                    Logger.getLogger(ListComponentViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
                                }

					} else {

						tworker = config.getWorker();
					}

					this.accEvent = e;
					Runnable r = new Runnable() {
						public void run() {
							try {
								int[] selectedRows = listTable.getSelectedRows();
								if (selectedRows.length > 0) {
									WorkList selectedWorkList = new NacWorkListChooser(config).showModalDialog();
									for (int i : selectedRows) {
										int modelIndex = listTable.convertRowIndexToModel(i);
										ContextualizedDescription conceptFsn = (ContextualizedDescription) lstTableModel.getValueAt(modelIndex, 0);
										TerminologyProjectDAO.addConceptAsNacWorklistMember(selectedWorkList, conceptFsn.getConcept(), config);
									}
									JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Concepts have been sent to translation.", "", JOptionPane.INFORMATION_MESSAGE);
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					new Thread(r).start();
			}

		}

		/**
		 * Sets the item.
		 * 
		 * @param descSet
		 *            the new item
		 */
		public void setItem(HashSet<Integer> descSet) {
			this.nodes = descSet;
		}

	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {

		columns = new String[] { "Source fsn", "Source preferred", "Target preferred", "Status" };

		String[][] data = null;
		lstTableModel = new DefaultTableModel(data, columns) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		listTable.setModel(lstTableModel);

		listTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			private MenuItemListener mItemListener;
			private JPopupMenu menu;
			private JMenuItem mItem;
			private int xPoint;
			private int yPoint;

			private JPopupMenu getMenu() {

				if (menu == null) {
					menu = new JPopupMenu();
					mItem = new JMenuItem();
					mItem.setText("Send to translation");
					mItem.setActionCommand("Send to translation");
					mItem.addActionListener(mItemListener);
					menu.add(mItem);
				}
				return menu;

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (e.getButton() == e.BUTTON3) {

						xPoint = e.getX();
						yPoint = e.getY();
						int row = listTable.rowAtPoint(new Point(xPoint, yPoint));
						if (row > -1) {
							DefaultTableModel model = (DefaultTableModel) listTable.getModel();
							int[] selRows = listTable.getSelectedRows();
							HashSet<Integer> descSet = new HashSet<Integer>();
							if (selRows.length < 2) {
								int rowModel = listTable.convertRowIndexToModel(row);
								ContextualizedDescription node = (ContextualizedDescription) model.getValueAt(rowModel, 0);
								if (node == null) {
									node = (ContextualizedDescription) model.getValueAt(rowModel, 1);
								}
								if (node == null) {
									node = (ContextualizedDescription) model.getValueAt(rowModel, 2);
								}
								if (node == null) {
									AceLog.getAppLog().alertAndLog(Level.SEVERE, "No description data.", new Exception("No description data."));
								}
								descSet.add(node.getConceptId());
							} else {
								for (int i = 0; i < selRows.length; i++) {
									int rowModel = listTable.convertRowIndexToModel(selRows[i]);
									ContextualizedDescription node = (ContextualizedDescription) model.getValueAt(rowModel, 0);
									if (node == null) {
										node = (ContextualizedDescription) model.getValueAt(rowModel, 1);
									}
									if (node == null) {
										node = (ContextualizedDescription) model.getValueAt(rowModel, 2);
									}
									if (node == null) {
										AceLog.getAppLog().alertAndLog(Level.SEVERE, "No description data.", new Exception("No description data."));
									}
									descSet.add(node.getConceptId());
								}
							}
							if (mItemListener == null) {

								mItemListener = new MenuItemListener();
							}
							mItemListener.setItem(descSet);
							getMenu();
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									menu.show(listTable, xPoint, yPoint);
								}
							});
						}
					}
					if (e.getClickCount() == 2) {
						ContextualizedDescription item;
						if (listTable.getSelectedRow() > 0) {
							if (lstTableModel.getValueAt(listTable.getSelectedRow(), 0) != null) {
								item = (ContextualizedDescription) lstTableModel.getValueAt(listTable.getSelectedRow(), 0);
							} else if (lstTableModel.getValueAt(listTable.getSelectedRow(), 1) != null) {
								item = (ContextualizedDescription) lstTableModel.getValueAt(listTable.getSelectedRow(), 1);
							} else {
								item = (ContextualizedDescription) lstTableModel.getValueAt(listTable.getSelectedRow(), 2);
							}
						} else {
							return;
						}

						I_TerminologyProject project = (I_TerminologyProject) projectComboModel.getSelectedItem();
						WorkListMember worklistMember = null;// new
																// WorkListMember(item.getText(),
																// item.getConceptId(),
																// item.getConcept().getUUIDs(),
																// null, null,
						// SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getUUIDs().iterator().next(),
						// new GregorianCalendar().getTimeInMillis());

						if (worklistMember != null) {
							TranslationConceptEditorRO editorRO = new TranslationConceptEditorRO();

							JDialog ro = new JDialog();
							ro.setContentPane(editorRO);
							ro.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
							ro.setSize(new Dimension(800, 650));
							Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
							Dimension ventana = ro.getSize();
							ro.setLocation((pantalla.width - ventana.width) / 2, (pantalla.height - ventana.height) / 2);

							worklistMember.setId(item.getConceptId());
							editorRO.updateUI((TranslationProject) project, worklistMember);
							ro.setVisible(true);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		projectComboModel = new DefaultComboBoxModel();
		projectCombo.setModel(projectComboModel);

		projectCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				messageLabel.setText("");
			}
		});

		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		projectComboModel.addElement("");
		for (I_TerminologyProject iTerminologyProject : projects) {
			projectComboModel.addElement(iTerminologyProject);
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
			String[][] data = null;
			lstTableModel = new DefaultTableModel(data, columns) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int x, int y) {
					return false;
				}
			};
			listTable.setModel(lstTableModel);

			// Populate table
			if (projectCombo.getSelectedItem() instanceof TranslationProject) {
				List<ListItemBean> list = TranslationSearchHelper.getListItemsForProject((TranslationProject) projectCombo.getSelectedItem());
				for (ListItemBean listItemBean : list) {
					lstTableModel.addRow(new Object[] { listItemBean.getSourceFsn(), listItemBean.getSourcePrefered(), listItemBean.getTargetPrefered(), listItemBean.getStatus() });
				}
			} else {
				messageLabel.setForeground(Color.RED);
				messageLabel.setText("No project selected.");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Close button action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void closeButtonActionPerformed(ActionEvent e) {
		TranslationHelperPanel thp;
		try {
			thp = PanelHelperFactory.getTranslationHelperPanel();
			JTabbedPane tp = thp.getTabbedPanel();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.LIST_COMPONENT_VIEWER_NAME)) {
						tp.remove(i);
						tp.revalidate();
						tp.repaint();
					}
				}
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		buttonCont = new JPanel();
		projectCombo = new JComboBox();
		refreshButton = new JButton();
		tableScroll = new JScrollPane();
		listTable = new ZebraJTable();
		panel1 = new JPanel();
		messageLabel = new JLabel();
		closeButton = new JButton();

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 133, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 0.0, 1.0, 0.0, 1.0E-4 };

		// ======== buttonCont ========
		{
			buttonCont.setLayout(new GridBagLayout());
			((GridBagLayout) buttonCont.getLayout()).columnWidths = new int[] { 199, 0, 0 };
			((GridBagLayout) buttonCont.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) buttonCont.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
			((GridBagLayout) buttonCont.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };
			buttonCont.add(projectCombo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- refreshButton ----
			refreshButton.setText("refresh");
			refreshButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					refreshButtonActionPerformed(e);
				}
			});
			buttonCont.add(refreshButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(buttonCont, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== tableScroll ========
		{
			tableScroll.setViewportView(listTable);
		}
		add(tableScroll, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 1.0, 1.0, 1.0E-4 };
			((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- messageLabel ----
			messageLabel.setForeground(UIManager.getColor("Desktop.background"));
			panel1.add(messageLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- closeButton ----
			closeButton.setText("close");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeButtonActionPerformed(e);
				}
			});
			panel1.add(closeButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The button cont. */
	private JPanel buttonCont;

	/** The project combo. */
	private JComboBox projectCombo;

	/** The refresh button. */
	private JButton refreshButton;

	/** The table scroll. */
	private JScrollPane tableScroll;

	/** The list table. */
	private ZebraJTable listTable;

	/** The panel1. */
	private JPanel panel1;

	/** The message label. */
	private JLabel messageLabel;

	/** The close button. */
	private JButton closeButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
