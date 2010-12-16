/*
 * Created by JFormDesigner on Thu Nov 25 12:27:48 GMT-03:00 2010
 */

package org.ihtsdo.qa.store.gui;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.ihtsdo.qa.store.QAStoreBI;
import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.QACoordinate;
import org.ihtsdo.qa.store.model.QADatabase;
import org.ihtsdo.qa.store.model.Severity;
import org.ihtsdo.qa.store.model.TerminologyComponent;
import org.ihtsdo.qa.store.model.view.RulesReportColumn;
import org.ihtsdo.qa.store.model.view.RulesReportLine;
import org.ihtsdo.qa.store.model.view.RulesReportPage;

/**
 * @author Guillermo Reynoso
 */
public class QAResultsBrowser extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private QAStoreBI store;
	private DefaultTableModel tableModel;
	private LinkedHashSet<DispositionStatus> dispositionStatuses;
	private LinkedHashSet<Severity> severities;
	private boolean showFilters = false;
	private QACoordinate coordinate;
	private int startLine = 0;
	private int finalLine = 0;
	private int totalLines = 0;

	public QAResultsBrowser(QAStoreBI store) {
		this.store = store;
		initComponents();
		panel4.setVisible(false);
		dispositionStatuses = new LinkedHashSet<DispositionStatus>();
		dispositionStatuses.addAll(store.getAllDispositionStatus());
		severities = new LinkedHashSet<Severity>();
		severities.addAll(store.getAllSeverities());

		LinkedHashSet<String> columnNames = new LinkedHashSet<String>();
		columnNames.add("Rule code");
		columnNames.add("Rule name");
		columnNames.add("Category");
		columnNames.add("Severity");
		columnNames.add("Open");
		for (DispositionStatus loopStatus : dispositionStatuses) {
			columnNames.add(loopStatus.getName());
		}
		columnNames.add("Closed");
		columnNames.add("Last run");

		String[][] data = null;
		tableModel = new DefaultTableModel(data, columnNames.toArray()) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		table1.setModel(tableModel);
		setupDatabasesCombo();
		setupPathsCombo();
		setupTimeCombo();
		setupStatusCombo();
		setupDispositionCombo();
		setupSeverityCombo();
		setupCategoryCombo();
		setupPageLinesCombo();
		updatePageCounters();
		setupSortByCombo();
	}
	
	private void setupSortByCombo() {
		comboBox9.removeAllItems();
		comboBox9.addItem("Rule name");
		comboBox9.addItem("Open");
		comboBox9.addItem("Cleared");
		comboBox9.addItem("Escalated");
		comboBox9.addItem("Deferred");
	}

	private void updatePageCounters() {
		label12.setText(String.valueOf(startLine));
		label13.setText(String.valueOf(finalLine));
		label15.setText(String.valueOf(totalLines));
		button3.setEnabled(startLine > 1);
		button4.setEnabled(finalLine < totalLines);
		panel3.revalidate();
	}

	private void setupPageLinesCombo() {
		comboBox4.removeAllItems();
		comboBox4.addItem("25");
		comboBox4.addItem("50");
		comboBox4.addItem("75");
		comboBox4.addItem("100");
	}

	public JTable getTable() {
		return table1;
	}

	public QACoordinate getQACoordinate() {
		return coordinate;
	}

	private void setupCategoryCombo() {
		comboBox8.removeAllItems();
		comboBox8.addItem("Any");
		comboBox8.addItem("Concept model");
		comboBox8.addItem("Descriptions model");
	}

	private void setupSeverityCombo() {
		comboBox7.removeAllItems();
		comboBox7.addItem("Any");
		for (Severity loopSeverity : severities) {
			comboBox7.addItem(loopSeverity);
		}
	}

	private void setupDispositionCombo() {
		comboBox6.removeAllItems();
		comboBox6.addItem("Any");
		for (DispositionStatus loopStatus : dispositionStatuses) {
			comboBox6.addItem(loopStatus);
		}
	}

	private void setupStatusCombo() {
		comboBox5.removeAllItems();
		comboBox5.addItem("Any");
		comboBox5.addItem("Open cases");
		comboBox5.addItem("No open cases");
		comboBox5.addItem("Closed cases");
		comboBox5.addItem("No closed cases");
	}

	private void setupDatabasesCombo() {
		comboBox1.removeAllItems();
		for (QADatabase loopDatabase : store.getAllDatabases()) {
			comboBox1.addItem(loopDatabase);
		}
	}

	private void setupPathsCombo() {
		comboBox2.removeAllItems();
		if (comboBox1.getSelectedItem() != null) {
			for (TerminologyComponent loopPath : store.getAllPathsForDatabase(((QADatabase) comboBox1.getSelectedItem()).getDatabaseUuid())) {
				comboBox2.addItem(loopPath);
			}
		}
	}

	private void setupTimeCombo() {
		comboBox3.removeAllItems();
		if (comboBox1.getSelectedItem() != null && comboBox2.getSelectedItem() != null) {
			for (String loopDate : store.getAllTimesForPath(((QADatabase) comboBox1.getSelectedItem()).getDatabaseUuid(), 
					((TerminologyComponent) comboBox2.getSelectedItem()).getComponentUuid())) {
				comboBox3.addItem(loopDate);
			}
		}
	}

	private void searchActionPerformed(ActionEvent e) {
		if (comboBox1.getSelectedItem() != null && 
				comboBox2.getSelectedItem() != null &&
				comboBox3.getSelectedItem() != null) {
			QADatabase database = (QADatabase) comboBox1.getSelectedItem();
			TerminologyComponent path = (TerminologyComponent) comboBox2.getSelectedItem();
			String time = (String) comboBox3.getSelectedItem();
			coordinate = new QACoordinate(database.getDatabaseUuid(), path.getComponentUuid(), time);
			updateTable1(coordinate);
		}
	}

	private void clearTable1() {
		while (tableModel.getRowCount()>0){
			tableModel.removeRow(0);
		}
		table1.revalidate();
	}

	private void updateTable1(QACoordinate coordinate) {
		clearTable1();
		//List<RulesReportLine> lines = store.getRulesReportLines(coordinate);
		LinkedHashMap<RulesReportColumn, Boolean> sortBy = new LinkedHashMap<RulesReportColumn,Boolean>();
		sortBy.put(RulesReportColumn.RULE_NAME,true);

		HashMap<RulesReportColumn, Object> filter = new HashMap<RulesReportColumn, Object>();
		Integer selectedPageLengh = Integer.parseInt((String) comboBox4.getSelectedItem());
		RulesReportPage page = store.getRulesReportLinesByPage(coordinate, sortBy, filter, startLine, selectedPageLengh);
		List<RulesReportLine> lines = page.getLines();
		totalLines = page.getTotalLines();
		startLine = page.getInitialLine();
		finalLine =  page.getFinalLine();
		updatePageCounters();
		for (RulesReportLine line : lines) {
			boolean lineApproved = true;
			//			if (showFilters) {
			//				if (comboBox5.getSelectedIndex() != 0){
			//					String selectedStatus = (String) comboBox5.getSelectedItem();
			//					if (selectedStatus.equals("Open cases")) {
			//						if (line.getStatusCount().get(true) ==  0) {
			//							lineApproved = false;
			//						}
			//					} else if (selectedStatus.equals("No open cases")) {
			//						if (line.getStatusCount().get(true) >  0) {
			//							lineApproved = false;
			//						}
			//					} else if (selectedStatus.equals("Closed cases")) {
			//						if (line.getStatusCount().get(false) ==  0) {
			//							lineApproved = false;
			//						}
			//					} else if (selectedStatus.equals("No closed cases")) {
			//						if (line.getStatusCount().get(false) >  0) {
			//							lineApproved = false;
			//						}
			//					}
			//				}
			//				if (comboBox6.getSelectedIndex() != 0){
			//					DispositionStatus selectedDisposition = (DispositionStatus) comboBox6.getSelectedItem();
			//					if (line.getDispositionStatusCount().get(selectedDisposition.getDispositionStatusUuid()) == 0) {
			//						lineApproved = false;
			//					}
			//				}
			//				if (comboBox7.getSelectedIndex() != 0){
			//					Integer selectedSeverity = Integer.valueOf((String) comboBox7.getSelectedItem());
			//					if (line.getRule().getSeverity() != selectedSeverity) {
			//						lineApproved = false;
			//					}
			//				}
			//				if (comboBox8.getSelectedIndex() != 0){
			//					String selectedCategory = (String) comboBox8.getSelectedItem();
			//					if (!line.getRule().getCategory().equals(selectedCategory)) {
			//						lineApproved = false;
			//					}
			//				}
			//				String filterText = textField1.getText().trim().toLowerCase();
			//				if (!filterText.isEmpty()) {
			//					if (!line.getRule().getName().toLowerCase().contains(filterText)) {
			//						lineApproved = false;
			//					}
			//				}
			//				String filterCode = textField2.getText().trim().toLowerCase();
			//				if (!filterCode.isEmpty()) {
			//					if (!line.getRule().getRuleCode().toLowerCase().equals(filterCode)) {
			//						lineApproved = false;
			//					}
			//				}
			//			}

			if (lineApproved) {
				LinkedList<Object> row = new LinkedList<Object>();
				row.add(String.valueOf(line.getRule().getRuleCode()));
				row.add(line.getRule());
				row.add(line.getRule().getCategory());
				row.add(line.getRule().getSeverity());
				row.add(line.getStatusCount().get(true));
				for (DispositionStatus loopStatus : dispositionStatuses) {
					row.add(line.getDispositionStatusCount().get(loopStatus.getDispositionStatusUuid()));
				}
				row.add(line.getStatusCount().get(false));
				if(line.getLastExecutionTime() != null){
					row.add(line.getLastExecutionTime().toString());
				}
				tableModel.addRow(row.toArray());
			}
		}
		table1.revalidate();
	}

	private void comboBox1ItemStateChanged(ItemEvent e) {
		setupPathsCombo();
		setupTimeCombo();
		clearTable1();
	}

	private void comboBox2ItemStateChanged(ItemEvent e) {
		setupTimeCombo();
		clearTable1();
	}

	private void comboBox3ItemStateChanged(ItemEvent e) {
		clearTable1();
	}

	private void button2ActionPerformed(ActionEvent e) {
		if (showFilters) {
			button2.setText("Show filters");
			panel4.setVisible(false);
			showFilters = false;
		} else {
			button2.setText("Hide filters");
			panel4.setVisible(true);
			showFilters = true;
		}
	}

	private void table1MouseClicked(MouseEvent e) {
		// TODO add your code here
	}

	private void button3ActionPerformed(ActionEvent e) {
		// previous page
		Integer selectedPageLengh = Integer.parseInt((String) comboBox4.getSelectedItem());
		startLine = startLine - selectedPageLengh;
		if (startLine < 1) {
			startLine = 1;
		}
		updateTable1(coordinate);
	}

	private void button4ActionPerformed(ActionEvent e) {
		// next page
		Integer selectedPageLengh = Integer.parseInt((String) comboBox4.getSelectedItem());
		startLine = startLine + selectedPageLengh;
		updateTable1(coordinate);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		label2 = new JLabel();
		label3 = new JLabel();
		label17 = new JLabel();
		comboBox1 = new JComboBox();
		comboBox2 = new JComboBox();
		comboBox3 = new JComboBox();
		button1 = new JButton();
		button2 = new JButton();
		comboBox9 = new JComboBox();
		panel4 = new JPanel();
		label11 = new JLabel();
		label8 = new JLabel();
		label10 = new JLabel();
		label9 = new JLabel();
		label4 = new JLabel();
		label5 = new JLabel();
		textField2 = new JTextField();
		textField1 = new JTextField();
		comboBox8 = new JComboBox();
		comboBox7 = new JComboBox();
		comboBox5 = new JComboBox();
		comboBox6 = new JComboBox();
		panel2 = new JPanel();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel3 = new JPanel();
		label6 = new JLabel();
		comboBox4 = new JComboBox();
		label7 = new JLabel();
		hSpacer1 = new JPanel(null);
		button3 = new JButton();
		label12 = new JLabel();
		label16 = new JLabel();
		label13 = new JLabel();
		label14 = new JLabel();
		label15 = new JLabel();
		button4 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 107, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Database");
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label2 ----
			label2.setText("Path");
			panel1.add(label2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label3 ----
			label3.setText("Time");
			panel1.add(label3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label17 ----
			label17.setText("Sort by");
			panel1.add(label17, new GridBagConstraints(11, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- comboBox1 ----
			comboBox1.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					comboBox1ItemStateChanged(e);
				}
			});
			panel1.add(comboBox1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- comboBox2 ----
			comboBox2.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					comboBox2ItemStateChanged(e);
				}
			});
			panel1.add(comboBox2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- comboBox3 ----
			comboBox3.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					comboBox3ItemStateChanged(e);
				}
			});
			panel1.add(comboBox3, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button1 ----
			button1.setText("Search");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					searchActionPerformed(e);
				}
			});
			panel1.add(button1, new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button2 ----
			button2.setText("Show filters");
			button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			panel1.add(button2, new GridBagConstraints(8, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel1.add(comboBox9, new GridBagConstraints(11, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel4 ========
		{
			panel4.setLayout(new GridBagLayout());
			((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 209, 0, 0, 0, 0};
			((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

			//---- label11 ----
			label11.setText("Rule code");
			panel4.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label8 ----
			label8.setText("Rule name filter");
			panel4.add(label8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label10 ----
			label10.setText("Category");
			panel4.add(label10, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label9 ----
			label9.setText("Severity");
			panel4.add(label9, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label4 ----
			label4.setText("Status");
			panel4.add(label4, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label5 ----
			label5.setText("Disposition");
			panel4.add(label5, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
			panel4.add(textField2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- textField1 ----
			textField1.setColumns(50);
			panel4.add(textField1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel4.add(comboBox8, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel4.add(comboBox7, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel4.add(comboBox5, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel4.add(comboBox6, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

			//======== scrollPane1 ========
			{

				//---- table1 ----
				table1.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						table1MouseClicked(e);
					}
				});
				scrollPane1.setViewportView(table1);
			}
			panel2.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 58, 0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label6 ----
			label6.setText("Show ");
			label6.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- comboBox4 ----
			comboBox4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(comboBox4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label7 ----
			label7.setText("rows per page");
			label7.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label7, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel3.add(hSpacer1, new GridBagConstraints(3, 0, 9, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button3 ----
			button3.setText("<");
			button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel3.add(button3, new GridBagConstraints(12, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label12 ----
			label12.setText("0");
			label12.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label12, new GridBagConstraints(13, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label16 ----
			label16.setText("to");
			label16.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label16, new GridBagConstraints(14, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label13 ----
			label13.setText("0");
			label13.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label13, new GridBagConstraints(15, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label14 ----
			label14.setText("of");
			label14.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label14, new GridBagConstraints(16, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label15 ----
			label15.setText("0");
			label15.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label15, new GridBagConstraints(17, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button4 ----
			button4.setText(">");
			button4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button4ActionPerformed(e);
				}
			});
			panel3.add(button4, new GridBagConstraints(18, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JLabel label2;
	private JLabel label3;
	private JLabel label17;
	private JComboBox comboBox1;
	private JComboBox comboBox2;
	private JComboBox comboBox3;
	private JButton button1;
	private JButton button2;
	private JComboBox comboBox9;
	private JPanel panel4;
	private JLabel label11;
	private JLabel label8;
	private JLabel label10;
	private JLabel label9;
	private JLabel label4;
	private JLabel label5;
	private JTextField textField2;
	private JTextField textField1;
	private JComboBox comboBox8;
	private JComboBox comboBox7;
	private JComboBox comboBox5;
	private JComboBox comboBox6;
	private JPanel panel2;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel3;
	private JLabel label6;
	private JComboBox comboBox4;
	private JLabel label7;
	private JPanel hSpacer1;
	private JButton button3;
	private JLabel label12;
	private JLabel label16;
	private JLabel label13;
	private JLabel label14;
	private JLabel label15;
	private JButton button4;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
