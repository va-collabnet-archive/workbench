/*
 * Created by JFormDesigner on Thu Aug 06 21:45:53 GMT-03:00 2009
 */

package org.ihtsdo.qa.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.drools.definition.rule.Rule;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.qa.gui.viewers.ui.TestFrame;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.context.RulesDeploymentPackageReferenceHelper;

/**
 * @author Guillermo Reynoso
 */
public class RulesContextEditorPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MyTableModel tableModel;
	private I_TermFactory tf;
	private I_ConfigAceFrame config;
	private RulesDeploymentPackageReferenceHelper rulesRepoHelper = null;
	private RulesContextHelper contextHelper = null;

	public RulesContextEditorPanel(I_ConfigAceFrame config) {
		initComponents();
		this.config = config;
		this.tf = Terms.get();
		rulesRepoHelper = new RulesDeploymentPackageReferenceHelper(config);
		contextHelper = new RulesContextHelper(config);
		label4.setText("");
		label4.repaint();

		tableModel = new MyTableModel();
		table1.setModel(tableModel);

		try {
			for (I_GetConceptData context : contextHelper.getAllContexts()) {
				contextComboBox.addItem(context);
			}
			updateCheckBox1();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateCheckBox1() throws Exception {
		I_GetConceptData selectedContext = (I_GetConceptData) contextComboBox.getSelectedItem();
		if (selectedContext != null) {
			List<RulesDeploymentPackageReference> pkgs = contextHelper.getPackagesForContext(selectedContext);
			comboBox1.removeAllItems();
			for (RulesDeploymentPackageReference repo : pkgs) {
				comboBox1.addItem(repo);
			}
			comboBox1.revalidate();
			if (pkgs.size() == 0) {
				tableModel.getDataVector().removeAllElements();
				table1.revalidate();
				table1.repaint();
			}
		} else {
			tableModel.getDataVector().removeAllElements();
			table1.revalidate();
			table1.repaint();
		}
	}

	private void updateTable1() {
		try {
			// table1.setAutoCreateRowSorter(true);
			label4.setText("Updating table...");
			label4.repaint();
			tableModel.data = new Object[0][0];
			tableModel.dataList = new ArrayList<Object[]>();
			if (comboBox1.getSelectedItem() != null && contextComboBox.getSelectedItem() != null) {
				I_GetConceptData agendaMetadataRefset = tf.getConcept(RefsetAuxiliary.Concept.RULES_CONTEXT_METADATA_REFSET.getUids());
				RulesDeploymentPackageReference selectedPackage = (RulesDeploymentPackageReference) comboBox1.getSelectedItem();
				I_GetConceptData selectedContext = (I_GetConceptData) contextComboBox.getSelectedItem();

				// Fiddle with the Sport column's cell editors/renderers.
				setUpSportColumn(table1, table1.getColumnModel().getColumn(3));

				if (selectedPackage.validate()) {
					Collection<Rule> rules = selectedPackage.getRules();
					List<Rule> rulesList = new ArrayList<Rule>();
					rulesList.addAll(rules);
					Collections.sort(rulesList, new Comparator<Rule>() {
						public int compare(Rule f1, Rule f2) {
							return f1.getName().compareTo(f2.getName());
						}
					});
					for (Rule rule : rulesList) {
						// System.out.println("** rule: " + rule.getName());
						String ruleUid = null;
						String description = null;
						String ditaUid = null;
						try {
							ruleUid = (String) rule.getMetaData().get("UUID");
							// ruleUid = rule.getMetaAttribute("UID");
							description = (String) rule.getMetaData().get("DESCRIPTION");
							// description =
							// rule.getMetaAttribute("DESCRIPTION");
							ditaUid = (String) rule.getMetaData().get("DITA_UID");
							// ditaUid = rule.getMetaAttribute("DITA_UID");
						} catch (Exception e) {
							// problem retrieving metadata, do nothing
							System.out.println("Malformed metadata..");
						}

						if (description == null)
							description = "";
						if (ditaUid == null)
							ditaUid = "";

						List<Object> row = new ArrayList<Object>();
						row.add(rule.getName());
						row.add(description);
						row.add(ditaUid);

						if (ruleUid != null) {
							I_GetConceptData role = contextHelper.getRoleInContext(ruleUid, selectedContext);
							if (role == null) {
								row.add("Included by default");
								row.add("");
							} else {
								row.add(role);
								row.add(role);
							}
						} else {
							row.add("No UUID");
							row.add("");
						}
						row.add(ruleUid);
						tableModel.addData(row);
					}
				} else {
					List<Object> row = new ArrayList<Object>();
					row.add("OFFLINE");
					row.add("");
					row.add("");
					row.add("");
					tableModel.addData(row);
				}
				TableColumnModel cmodel = table1.getColumnModel();
				table1.setGridColor(Color.BLACK);
				table1.setShowGrid(true);
				TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
				cmodel.getColumn(0).setCellRenderer(textAreaRenderer);
				cmodel.getColumn(1).setCellRenderer(textAreaRenderer);

			}
			// refresh table
			table1.revalidate();
			table1.repaint();
			label4.setText("");
			label4.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MyTableModel extends DefaultTableModel {

		private Object[][] data = new Object[0][6];
		private List<Object[]> dataList = new ArrayList<Object[]>();
		private String[] columnNames = new String[6];
		private final static int NAME = 0;
		private final static int DESCRPTION = 1;
		private final static int DITA_UUID = 2;
		private final static int STATUS_IN_CONTEXT = 3;
		private final static int ORIGINAL_STATUS_IN_CONTEXT = 4;
		private final static int RULE_UID = 5;

		public MyTableModel() {
			super();
			dataList = new ArrayList<Object[]>();
			columnNames[NAME] = "Name";
			columnNames[DESCRPTION] = "Description";
			columnNames[DITA_UUID] = "DITA UUID";
			columnNames[STATUS_IN_CONTEXT] = "Status in Context";
			columnNames[ORIGINAL_STATUS_IN_CONTEXT] = "original status in context";
			columnNames[RULE_UID] = "rule uide";
		}

		public void clearData() {
			dataList = new ArrayList<Object[]>();
		}

		public void addData(List<Object> row) {
			dataList.add(row.toArray());
			data = new Object[dataList.size()][4];
			for (int j = 0; j < dataList.size(); j++) {
				data[j] = dataList.get(j);
			}
		}

		public int getColumnCount() {
			return columnNames.length - 2;
		}

		public int getRowCount() {
			if (data == null) {
				return 0;
			}
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			if (getValueAt(0, c) != null) {
				return getValueAt(0, c).getClass();
			} else {
				return null;
			}
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int x, int y) {
			if (y == 3 && getValueAt(x, 5) != null) {
				return true;
			} else {
				return false;
			}
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

		public void saveStatusesInContext() {
			for (Object[] row : dataList) {
				Object statusInContext = row[STATUS_IN_CONTEXT];
				Object originalStContext = row[ORIGINAL_STATUS_IN_CONTEXT];

				if (statusInContext instanceof I_GetConceptData && !statusInContext.toString().equals(originalStContext.toString())) {
					contextHelper.setRoleInContext(row[RULE_UID].toString(), (I_GetConceptData) contextComboBox.getSelectedItem(), (I_GetConceptData) statusInContext);
				} else if (originalStContext instanceof I_GetConceptData && !(statusInContext instanceof I_GetConceptData)) {
					contextHelper.setRoleInContext(row[RULE_UID].toString(), (I_GetConceptData) contextComboBox.getSelectedItem(), null);
				}
			}
		}
	}

	public void setUpSportColumn(JTable table, TableColumn conceptColumn) {
		I_TermFactory tf = Terms.get();

		try {
			I_GetConceptData includeClause = tf.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids());
			I_GetConceptData excludeClause = tf.getConcept(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids());
			// Set up the editor for the sport cells.
			JComboBox comboBox = new JComboBox();
			comboBox.addItem("Included by default");
			comboBox.addItem(includeClause);
			comboBox.addItem(excludeClause);
			conceptColumn.setCellEditor(new DefaultCellEditor(comboBox));

			// Set up tool tips for the sport cells.
			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
			renderer.setToolTipText("Click for combo box");
			conceptColumn.setCellRenderer(renderer);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void comboBox2ItemStateChanged(ItemEvent e) {
		try {
			updateCheckBox1();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void comboBox1ItemStateChanged(ItemEvent e) {
		updateTable1();
	}

	private void button2ActionPerformed(ActionEvent e) {
		try {

			// comboBox1.removeItemListener(comboBox1.getItemListeners()[0]);
			// comboBox2.removeItemListener(comboBox2.getItemListeners()[0]);

			contextComboBox.removeAllItems();
			for (I_GetConceptData context : contextHelper.getAllContexts()) {
				contextComboBox.addItem(context);
			}
			comboBox1.removeAllItems();
			for (RulesDeploymentPackageReference repo : rulesRepoHelper.getAllRulesDeploymentPackages()) {
				comboBox1.addItem(repo);
			}

			// comboBox2.addItemListener(new ItemListener() {
			// public void itemStateChanged(ItemEvent e) {
			// comboBox2ItemStateChanged(e);
			// }
			// });
			//
			// comboBox1.addItemListener(new ItemListener() {
			// public void itemStateChanged(ItemEvent e) {
			// comboBox1ItemStateChanged(e);
			// }
			// });

			updateTable1();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		// updateTable1();
	}

	private void saveButtonActionPerformed(ActionEvent e) {
		tableModel.saveStatusesInContext();
		contextHelper.clearCache();
	}

	private void button1ActionPerformed(ActionEvent e) {
		// search rules
		updateTable1();
	}

	private void exportToExcelButtonActionPerformed(ActionEvent e) {
		if (comboBox1.getSelectedItem() != null && contextComboBox.getSelectedItem() != null) {
			updateTable1();
			try {
				RulesDeploymentPackageReference selectedPackage = (RulesDeploymentPackageReference) comboBox1.getSelectedItem();
				I_GetConceptData selectedContext = (I_GetConceptData) contextComboBox.getSelectedItem();

				File excelFile = new ExcelExportUtil().exortRulesContext(tableModel.data, tableModel.columnNames, selectedPackage, selectedContext);
				if (excelFile != null) {
					Desktop desktop = null;
					if (Desktop.isDesktopSupported()) {
						desktop = Desktop.getDesktop();
						desktop.open(excelFile);
					}
				} else {
					// showError(NO_DATA);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	private void hiddenMouseListener(MouseEvent e) {
		if(e.getClickCount() == 3){
			JFrame frame = new TestFrame();
			Dimension dim = new Dimension(600, 600);
			frame.setSize(dim);
			frame.setPreferredSize(dim );
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		panel2 = new JPanel();
		label3 = new JLabel();
		contextComboBox = new JComboBox();
		label2 = new JLabel();
		comboBox1 = new JComboBox();
		button1 = new JButton();
		exportToExcelButton = new JButton();
		label4 = new JLabel();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel3 = new JPanel();
		saveButton = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Rule-Context editor");
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label3 ----
			label3.setText("Context:");
			panel2.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- contextComboBox ----
			contextComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					comboBox2ItemStateChanged(e);
				}
			});
			panel2.add(contextComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label2 ----
			label2.setText("Repository:");
			label2.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					hiddenMouseListener(e);
				}
			});
			panel2.add(label2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel2.add(comboBox1, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button1 ----
			button1.setText("Search rules");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel2.add(button1, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- exportToExcelButton ----
			exportToExcelButton.setText("Export to Excel");
			exportToExcelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					exportToExcelButtonActionPerformed(e);
				}
			});
			panel2.add(exportToExcelButton, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label4 ----
			label4.setText("Notification");
			label4.setForeground(Color.red);
			panel2.add(label4, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(table1);
		}
		add(scrollPane1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- saveButton ----
			saveButton.setText("Save");
			saveButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveButtonActionPerformed(e);
				}
			});
			panel3.add(saveButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JPanel panel2;
	private JLabel label3;
	private JComboBox contextComboBox;
	private JLabel label2;
	private JComboBox comboBox1;
	private JButton button1;
	private JButton exportToExcelButton;
	private JLabel label4;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel3;
	private JButton saveButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}