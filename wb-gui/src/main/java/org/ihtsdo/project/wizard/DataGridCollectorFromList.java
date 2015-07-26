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

package org.ihtsdo.project.wizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.view.details.ZebraJTable;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.wizard.I_fastWizard;

/**
 * The Class DataGridCollectorFromList.
 * 
 * @author Guillermo Reynoso
 */
public class DataGridCollectorFromList extends JPanel implements I_fastWizard {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3557213261369154457L;

	/** The column names. */
	private String[] columnNames;

	/** The key. */
	private String key;

	/** The users. */
	private List<WfUser> users;

	/** The roles. */
	private List<WfRole> roles;

	private Object[][] data;

	/**
	 * Gets the users.
	 * 
	 * @return the users
	 */
	public List<WfUser> getUsers() {
		return users;
	}

	/**
	 * Gets the roles.
	 * 
	 * @return the roles
	 */
	public List<WfRole> getRoles() {
		return roles;
	}

	/**
	 * Sets the users.
	 * 
	 * @param users
	 *            the new users
	 */
	public void setUsers(List<WfUser> users) {
		this.users = users;
	}

	/**
	 * Sets the roles.
	 * 
	 * @param roles
	 *            the new roles
	 */
	public void setRoles(List<WfRole> roles) {
		this.roles = roles;
	}

	/**
	 * Instantiates a new data grid collector from list.
	 * 
	 * @param roles
	 *            the roles
	 * @param users
	 *            the users
	 */
	public DataGridCollectorFromList(List<WfRole> roles, List<WfUser> users) {
		initComponents();
		this.roles = roles;
		this.users = users;
		tblObjs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblObjs.getTableHeader().setReorderingAllowed(false);
		setColumNames();
		loadObjects();
	}

	/**
	 * Instantiates a new data grid collector from list.
	 */
	public DataGridCollectorFromList() {
		initComponents();
		tblObjs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * Sets the colum names.
	 */
	public void setColumNames() {
		Collections.sort(roles, new Comparator<WfRole>() {
			@Override
			public int compare(WfRole o1, WfRole o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		this.columnNames = new String[roles.size() * 2 + 1];
		this.columnNames[0] = "Users";
		int j = 1;
		for (int i = 0; i < roles.size(); i++) {
			this.columnNames[j++] = roles.get(i).toString();
			this.columnNames[j++] = "default";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.wizard.I_fastWizard#setKey(java.lang.String)
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Load objects.
	 */
	public void loadObjects() {

		try {
			ProjectPermissionsAPI ppa = new ProjectPermissionsAPI(Terms.get().getActiveAceFrameConfig());
			WfComponentProvider wfcp = new WfComponentProvider();
			List<Object[]> rows = new ArrayList<Object[]>();
			List<Object[]> active = new ArrayList<Object[]>();

			for (WfUser obj : users) {
				Set<I_GetConceptData> userRoles = ppa.getRolesForUser(Terms.get().getConcept(obj.getId())); // All Roles for all users
					if (userRoles.size() == 0)
						continue;
					Object[] row = new Object[columnNames.length];
					Object[] act = new Object[columnNames.length];
					for (int i = 0; i < columnNames.length; i++) {
						row[i] = false;
						act[i] = false;
					}
					int counter = 0;
					for (I_GetConceptData i_GetConceptData : userRoles) {
						WfRole userRole = wfcp.roleConceptToWfRole(i_GetConceptData);
						int index = -1;
						for (int i = 0; i < roles.size(); i++) {
							if (roles.get(i).getId().equals(userRole.getId())) {
								index = i;
								break;
							}
						}
						if (index != -1) {
							counter++;
							act[index * 2 + 1] = true;
							act[index * 2 + 2] = true;
						}
					}
					if (counter == 0)
						continue;
					row[0] = obj;
					rows.add(row);
					active.add(act);
				}
			
			data = new Object[active.size()][];
			for (int i = 0; i < data.length; i++) {
				data[i] = (Object[]) active.get(i);
			}
			class GridTableModel extends DefaultTableModel {
				private static final long serialVersionUID = 1L;
				private Object[][] grid;

				public GridTableModel(Object[][] d, String[] c, Object[][] g) {
					super(d, c);
					grid = g;
				};

				public boolean isCellEditable(int x, int y) {
					return (Boolean) grid[x][y];
				}
			}
			;

			GridTableModel model = new GridTableModel(null, columnNames, data);
			for (Object[] row : rows) {
				model.addRow(row);
			}
			tblObjs.setModel(model);
			TableColumnModel cmodel = tblObjs.getColumnModel();
			CheckBoxRenderer checkBoxRenderer = new CheckBoxRenderer(data);
			CheckBoxEditor checkBoxEditor = new CheckBoxEditor(new JCheckBox(), new JCheckBox());
			RadioRenderer radioRenderer = new RadioRenderer(data);
			RadioEditor radioEditor = new RadioEditor(new JCheckBox(), new JRadioButton());
			cmodel.getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
			for (int i = 1; i < (columnNames.length); i++) {
				if (i % 2 == 1) {
					cmodel.getColumn(i).setCellRenderer(checkBoxRenderer);
					cmodel.getColumn(i).setCellEditor(checkBoxEditor);
				} else {
					cmodel.getColumn(i).setCellRenderer(radioRenderer);
					cmodel.getColumn(i).setCellEditor(radioEditor);
					cmodel.getColumn(i).setMaxWidth(0);
					cmodel.getColumn(i).setMinWidth(0);
				}
			}
			hideColumns();
			for (int i = 1; i < model.getColumnCount(); i += 2) {
				boolean flag = true;
				for (int j = 0; j < model.getRowCount(); j++) {
					if ((Boolean) model.isCellEditable(j, i) == true) {
						flag = false;
						break;
					}
				}
				if (flag) {
					JOptionPane.showMessageDialog(this, "There is a role that cannot be assigned. " + "You must modify Workflow Definition first.");
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Sets the label.
	 * 
	 * @param strLabel
	 *            the new label
	 */
	public void setLabel(String strLabel) {
		this.label1.setText(strLabel);
	}

	/**
	 * The Class RadioRenderer.
	 */
	class RadioRenderer extends JPanel implements TableCellRenderer {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** The grid. */
		Object[][] grid;

		/**
		 * Instantiates a new radio renderer.
		 * 
		 * @param data
		 *            the data
		 */
		RadioRenderer(Object[][] data) {
			super();
			grid = data;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			this.removeAll();
			JRadioButton button = new JRadioButton();
			Boolean val = (Boolean) value;
			button.setSelected(val);
			button.setHorizontalAlignment(JButton.RIGHT);
			button.setHorizontalTextPosition(JButton.LEFT);
			button.setOpaque(true);
			setOpaque(true);
			if ((Boolean) grid[row][column] == false) {
				button.setForeground(Color.LIGHT_GRAY);
				button.setBackground(Color.LIGHT_GRAY);
				setForeground(Color.LIGHT_GRAY);
				setBackground(Color.LIGHT_GRAY);
			}
			this.add(button);
			return this;
		}
	}

	/**
	 * The Class CheckBoxRenderer.
	 */
	class CheckBoxRenderer extends JPanel implements TableCellRenderer {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** The grid. */
		Object[][] grid;

		/**
		 * Instantiates a new check box renderer.
		 * 
		 * @param data
		 *            the data
		 */
		CheckBoxRenderer(Object[][] data) {
			super();
			grid = data;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			this.removeAll();
			Boolean val = (Boolean) value;
			JCheckBox button = new JCheckBox();
			button.setHorizontalAlignment(JButton.RIGHT);
			button.setHorizontalTextPosition(JButton.LEFT);
			button.setOpaque(true);
			setOpaque(true);
			button.setSelected(val);
			if ((Boolean) grid[row][column] == false) {
				button.setForeground(Color.LIGHT_GRAY);
				button.setBackground(Color.LIGHT_GRAY);
				setForeground(Color.LIGHT_GRAY);
				setBackground(Color.LIGHT_GRAY);
			}
			this.add(button);
			return this;
		}
	}

	/**
	 * The Class RadioEditor.
	 */
	class RadioEditor extends DefaultCellEditor implements TableCellEditor, ActionListener {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** The rbutton. */
		JRadioButton rbutton;

		/** The panel. */
		JPanel panel;

		/** The row. */
		int row;

		/** The column. */
		int column;

		/**
		 * Instantiates a new radio editor.
		 * 
		 * @param checkBox
		 *            the check box
		 * @param button
		 *            the button
		 */
		public RadioEditor(JCheckBox checkBox, JRadioButton button) {
			super(checkBox);
			this.rbutton = button;
			this.rbutton.addActionListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing
		 * .JTable, java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			Boolean val = (Boolean) value;
			rbutton.setSelected(val);
			this.row = row;
			this.column = column;
			panel = new JPanel();
			panel.setOpaque(true);
			if (!table.getModel().isCellEditable(row, column)) {
				panel.setForeground(Color.LIGHT_GRAY);
				panel.setBackground(Color.LIGHT_GRAY);
			}
			panel.add(rbutton);
			panel.setMinimumSize(new Dimension(0, 20));
			return panel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.DefaultCellEditor#getCellEditorValue()
		 */
		public Object getCellEditorValue() {

			int rowModel = tblObjs.convertRowIndexToModel(row);
			Boolean val = (Boolean) tblObjs.getModel().getValueAt(rowModel, column);
			return val;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButton button = (JRadioButton) e.getSource();
			if (button.isSelected())
				itemRadioActionPerformed(1, this.row, column);
			else
				itemRadioActionPerformed(2, this.row, column);

		}

	}

	/**
	 * The Class CheckBoxEditor.
	 */
	class CheckBoxEditor extends DefaultCellEditor implements TableCellEditor, ActionListener {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** The rbutton. */
		JCheckBox rbutton;

		/** The row. */
		int row;

		/** The column. */
		int column;

		/**
		 * Instantiates a new check box editor.
		 * 
		 * @param checkBox
		 *            the check box
		 * @param button
		 *            the button
		 */
		public CheckBoxEditor(JCheckBox checkBox, JCheckBox button) {
			super(checkBox);
			this.rbutton = button;
			rbutton.setHorizontalAlignment(JButton.RIGHT);
			rbutton.setHorizontalTextPosition(JButton.LEFT);
			this.rbutton.addActionListener(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing
		 * .JTable, java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			Boolean val = (Boolean) value;
			rbutton.setSelected(val);
			this.row = row;
			this.column = column;
			JPanel panel = new JPanel();
			panel.setOpaque(true);
			if (!table.getModel().isCellEditable(row, column)) {
				panel.setForeground(Color.LIGHT_GRAY);
				panel.setBackground(Color.LIGHT_GRAY);
			}
			panel.add(rbutton);
			rbutton.setHorizontalAlignment(JButton.RIGHT);
			rbutton.setHorizontalTextPosition(JButton.LEFT);
			return panel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.DefaultCellEditor#getCellEditorValue()
		 */
		public Object getCellEditorValue() {
			return new Boolean(rbutton.isSelected());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox button = (JCheckBox) e.getSource();
			if (button.isSelected())
				itemCheckBoxActionPerformed(1, this.row, column);
			else
				itemCheckBoxActionPerformed(2, this.row, column);

		}
	}

	/**
	 * Item radio action performed.
	 * 
	 * @param state
	 *            the state
	 * @param row
	 *            the row
	 * @param column
	 *            the column
	 */
	private void itemRadioActionPerformed(int state, int row, int column) {
		boolean othersRow;
		boolean ownRow;

		if (state == 2) {
			othersRow = true;
			ownRow = false;
		} else {
			othersRow = false;
			ownRow = true;
		}
		for (int r = 0; r < tblObjs.getRowCount(); r++) {
			int rowModel = tblObjs.convertRowIndexToModel(r);
			if (r == row)
				tblObjs.getModel().setValueAt(ownRow, rowModel, column);
			else
				tblObjs.getModel().setValueAt(othersRow, rowModel, column);
		}

	}

	/**
	 * Item check box action performed.
	 * 
	 * @param state
	 *            the state
	 * @param row
	 *            the row
	 * @param column
	 *            the column
	 */
	private void itemCheckBoxActionPerformed(int state, int row, int column) {
		int rowModel = tblObjs.convertRowIndexToModel(row);
		if (state == 2) {
			tblObjs.getModel().setValueAt(false, rowModel, column);
			if ((boolean) tblObjs.getModel().getValueAt(rowModel, column + 1)) {
				tblObjs.getModel().setValueAt(false, rowModel, column + 1);
				for (int i = 0; i < tblObjs.getRowCount(); i++) {
					if ((boolean) tblObjs.getModel().getValueAt(i, column)) {
						tblObjs.getModel().setValueAt(true, i, column + 1);
						break;
					}
				}
			}
		} else {
			tblObjs.getModel().setValueAt(true, rowModel, column);
		}
		hideColumns();
	}

	/**
	 * Hide columns.
	 */
	private void hideColumns() {
		DefaultTableModel model = (DefaultTableModel) tblObjs.getModel();
		for (int i = 1; i < model.getColumnCount(); i += 2) {
			int counter = 0;
			int counter2 = 0;
			int lastPos = 0;
			for (int j = 0; j < model.getRowCount(); j++) {
				if (((Boolean) model.getValueAt(j, i)) == true) {
					counter++;
					lastPos = j;
					if (((Boolean) model.getValueAt(j, i + 1)) == true) {
						counter2++;
					}
				}
			}
			if (counter < 2) {
				if (counter == 1) {
					for (int j = 0; j < model.getRowCount(); j++) {
						model.setValueAt(false, j, i + 1);
					}
					model.setValueAt(true, lastPos, i + 1);
				}
				tblObjs.getColumnModel().getColumn(i + 1).setMaxWidth(0);
				tblObjs.getColumnModel().getColumn(i + 1).setMinWidth(0);
			} else {
				tblObjs.getColumnModel().getColumn(i + 1).setMaxWidth(50);
				tblObjs.getColumnModel().getColumn(i + 1).setMinWidth(50);
			}
		}
		tblObjs.setModel(model);
		model.fireTableDataChanged();
	}

	/**
	 * Button1 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button1ActionPerformed(ActionEvent e) {
		DefaultTableModel model = (DefaultTableModel) tblObjs.getModel();
		if (button1.getText().equals("Fill")) {
			button1.setText("Clear");
			for (int i = 1; i < model.getColumnCount(); i += 2) {
				boolean def = false;
				for (int j = 0; j < model.getRowCount(); j++) {
					if (model.isCellEditable(j, i)) {
						model.setValueAt(true, j, i);
						if (!def) {
							model.setValueAt(true, j, i + 1);
							def = true;
						} else {
							model.setValueAt(false, j, i + 1);
						}
					}
				}
			}
		} else {
			button1.setText("Fill");
			for (int i = 1; i < model.getColumnCount(); i++) {
				for (int k = 0; k < model.getRowCount(); k++) {
					model.setValueAt(false, k, i);
				}
			}
		}
		tblObjs.setModel(model);
		hideColumns();
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		button1 = new JButton();
		scrollPane1 = new JScrollPane();
		tblObjs = new ZebraJTable();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 0.0, 1.0, 0.0, 1.0E-4 };

		// ======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 1.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- label1 ----
			label1.setText("Set users for role:");
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- button1 ----
			button1.setText("Fill");
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel1.add(button1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== scrollPane1 ========
		{

			// ---- tblObjs ----
			tblObjs.setRowHeight(25);
			tblObjs.setRowSelectionAllowed(false);
			tblObjs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scrollPane1.setViewportView(tblObjs);
		}
		add(scrollPane1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JButton button1;
	private JScrollPane scrollPane1;
	private ZebraJTable tblObjs;

	// JFormDesigner - End of variables declaration //GEN-END:variables
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.wizard.I_fastWizard#getData()
	 */
	@Override
	public HashMap<String, Object> getData() throws Exception {
		HashMap<String, Object> hmRes = new HashMap<String, Object>();
		DefaultTableModel model = (DefaultTableModel) tblObjs.getModel();

		for (int j = 1; j < columnNames.length; j += 2) {
			boolean bExists = false;
			for (int i = 0; i < model.getRowCount(); i++) {
				Boolean sel = (Boolean) model.getValueAt(i, j);
				if (sel == true) {
					bExists = true;
					break;
				}
			}
			if (!bExists) {
				throw new Exception(model.getColumnName(j) + " is empty.");
			}
		}

		for (int j = 2; j < columnNames.length; j += 2) {
			int bExists = 0;
			int dExists = 0;
			for (int i = 0; i < model.getRowCount(); i++) {
				Boolean def = (Boolean) model.getValueAt(i, j);
				Boolean sel = (Boolean) model.getValueAt(i, j - 1);
				if (sel == true)
					bExists++;
				if (def == true)
					dExists++;
			}
			if (bExists == 0) {
				throw new Exception(model.getColumnName(j) + " is empty.");
			}
			if (dExists != 1) {
				throw new Exception(model.getColumnName(j - 1) + " default must be one.");
			}
		}
		hmRes.put(key, model);
		return hmRes;
	}

	public void setData(HashMap<String, List<WfMembership>> rolesMap, HashMap<String, String> def) {
		if (rolesMap == null || rolesMap.size() < 1)
			return;

		DefaultTableModel model = (DefaultTableModel) tblObjs.getModel();
		for (int i = 1; i < columnNames.length; i += 2) {
			if (rolesMap.containsKey(columnNames[i])) {
				String defUser = def.get(columnNames[i]);
				List<WfMembership> users = rolesMap.get(columnNames[i]);
				for (WfMembership user : users) {
					for (int j = 0; j < model.getRowCount(); j++) {
						if (model.getValueAt(j, 0).toString().equals(user.getUser().getUsername())) {
							model.setValueAt(true, j, i);
							if (user.getUser().getUsername().equals(defUser)) {
								model.setValueAt(true, j, i + 1);
							}
							break;
						}
					}
				}
			}
		}

		tblObjs.setModel(model);
	}
}
