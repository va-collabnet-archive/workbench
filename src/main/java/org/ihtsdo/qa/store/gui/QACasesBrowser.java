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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
import org.ihtsdo.qa.store.model.Rule;
import org.ihtsdo.qa.store.model.view.QACasesReportColumn;
import org.ihtsdo.qa.store.model.view.QACasesReportLine;
import org.ihtsdo.qa.store.model.view.QACasesReportPage;
import org.ihtsdo.qa.store.model.view.RulesReportColumn;
import org.ihtsdo.qa.store.model.view.RulesReportPage;

/**
 * @author Guillermo Reynoso
 */
public class QACasesBrowser extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean showFilters = false;
	private LinkedHashSet<DispositionStatus> dispositionStatuses;
	private QAStoreBI store;
	private DefaultTableModel tableModel;
	private QACoordinate coordinate;
	private Rule rule;
	private int startLine = 0;
	private int finalLine = 0;
	private int totalLines = 0;

	public QACasesBrowser(QAStoreBI store, QACoordinate coordinate, Rule rule) {
		this.store = store;
		this.coordinate = coordinate;
		this.rule = rule;
		initComponents();
		dispositionStatuses = new LinkedHashSet<DispositionStatus>();
		dispositionStatuses.addAll(store.getAllDispositionStatus());
		panel4.setVisible(false);
		setupSortByCombo();
		
		label7.setText("");
		label8.setText("");
		label9.setText("");
		label10.setText("");
		label13.setText("");
		
		LinkedHashSet<String> columnNames = new LinkedHashSet<String>();
		columnNames.add("Concept UUID");
		columnNames.add("Concept Sctid");
		columnNames.add("Concept Name");
		columnNames.add("Status");
		columnNames.add("Disposition");
		columnNames.add("Assigned to");
		columnNames.add("Time");

		String[][] data = null;
		tableModel = new DefaultTableModel(data, columnNames.toArray()) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		table1.setModel(tableModel);
		
		if (coordinate != null && rule != null) {
			setupPanel(store, coordinate, rule);
		}
		
		setupPageLinesCombo();
		updatePageCounters();
		
	}
	
	private void setupPageLinesCombo() {
		comboBox6.removeAllItems();
		comboBox6.addItem("25");
		comboBox6.addItem("50");
		comboBox6.addItem("75");
		comboBox6.addItem("100");
	}
	
	public void setupPanel(QAStoreBI store, QACoordinate coordinate, Rule rule) {
		this.store = store;
		this.coordinate = coordinate;
		this.rule = rule;
		
		panel4.setVisible(false);
		
		clearTable1();
		
		label7.setText(store.getQADatabase(coordinate.getDatabaseUuid()).getName());
		label8.setText(store.getComponent(coordinate.getPathUuid()).getComponentName());
		label9.setText(coordinate.getViewPointTime());
		label10.setText(rule.getRuleCode());
		label13.setText(rule.getName());
		
		setupStatusCombo();
		setupDispositionCombo();
		textField1.setText("");
		showFilters = false;
		button2.setText("Show filters");
		//updateTable1();
		updatePageCounters();
	}

	private void updateTable1() {
		clearTable1();
		List <QACasesReportColumn> sortBy = new ArrayList<QACasesReportColumn>();
		sortBy.add(QACasesReportColumn.CONCEPT_NAME);

		HashMap<QACasesReportColumn, Object> filter = new HashMap<QACasesReportColumn, Object>();
		Integer selectedPageLengh = Integer.parseInt((String) comboBox6.getSelectedItem());
		QACasesReportPage page = store.getQACasesReportLinesByPage(coordinate, rule.getRuleUuid(), 
				sortBy, filter, startLine, selectedPageLengh);
		List<QACasesReportLine> lines = page.getLines();
		totalLines = page.getTotalLines();
		startLine = page.getInitialLine();
		finalLine =  page.getFinalLine();
		updatePageCounters();
		for (QACasesReportLine line : lines) {
			boolean lineApproved = true;
//			if (showFilters) {
//				if (comboBox4.getSelectedIndex() != 0){
//					String selectedStatus = (String) comboBox4.getSelectedItem();
//					if (selectedStatus.equals("Open")) {
//						if (!line.getQaCase().isActive()) {
//							lineApproved = false;
//						}
//					} else if (selectedStatus.equals("Closed")) {
//						if (line.getQaCase().isActive()) {
//							lineApproved = false;
//						}
//					}
//				}
//				if (comboBox5.getSelectedIndex() != 0){
//					DispositionStatus selectedDisposition = (DispositionStatus) comboBox5.getSelectedItem();
//					if (!line.getDisposition().getDispositionStatusUuid().equals(selectedDisposition.getDispositionStatusUuid())) {
//						lineApproved = false;
//					}
//				}
//				String filterText = textField1.getText().trim().toLowerCase();
//				if (!filterText.isEmpty()) {
//					if (!line.getComponent().getComponentName().toLowerCase().contains(filterText)) {
//						lineApproved = false;
//					}
//				}
//			}

			if (lineApproved) {
				LinkedHashSet<Object> row = new LinkedHashSet<Object>();
				row.add(line.getComponent().getComponentUuid().toString());
				row.add(String.valueOf(line.getComponent().getSctid()));
				row.add(line.getComponent().getComponentName());
				if (line.getQaCase().isActive()) {
					row.add("Open");
				} else {
					row.add("Closed");
				}
				row.add(line.getDisposition().getName());
				row.add(line.getQaCase().getAssignedTo());
				row.add(line.getQaCase().getEffectiveTime());
				tableModel.addRow(row.toArray());
			}
		}
		table1.revalidate();
	}

	private void clearTable1() {
		while (tableModel.getRowCount()>0){
			tableModel.removeRow(0);
		}
		table1.revalidate();
	}

	private void setupDispositionCombo() {
		comboBox5.removeAllItems();
		comboBox5.addItem("Any");
		for (DispositionStatus loopStatus : dispositionStatuses) {
			comboBox5.addItem(loopStatus);
		}
	}

	private void setupStatusCombo() {
		comboBox4.removeAllItems();
		comboBox4.addItem("Any");
		comboBox4.addItem("Open");
		comboBox4.addItem("Closed");
	}
	
	private void setupSortByCombo() {
		comboBox1.removeAllItems();
		comboBox1.addItem("Concept name, status, disposition");
		comboBox1.addItem("Status, disposition, name");
		comboBox1.addItem("Disposition, name, status");
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
	
	private void updatePageCounters() {
		label17.setText(String.valueOf(startLine));
		label19.setText(String.valueOf(finalLine));
		label21.setText(String.valueOf(totalLines));
		button4.setEnabled(startLine > 1);
		button5.setEnabled(finalLine < totalLines);
		panel3.revalidate();
	}

	private void button3ActionPerformed(ActionEvent e) {
		// previous page
		Integer selectedPageLengh = Integer.parseInt((String) comboBox6.getSelectedItem());
		startLine = startLine - selectedPageLengh;
		if (startLine < 1) {
			startLine = 1;
		}
		updateTable1();
	}

	private void button4ActionPerformed(ActionEvent e) {
		Integer selectedPageLengh = Integer.parseInt((String) comboBox6.getSelectedItem());
		startLine = startLine + selectedPageLengh;
		updateTable1();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		label2 = new JLabel();
		label3 = new JLabel();
		label6 = new JLabel();
		label12 = new JLabel();
		label14 = new JLabel();
		label7 = new JLabel();
		label8 = new JLabel();
		label9 = new JLabel();
		label10 = new JLabel();
		label13 = new JLabel();
		button3 = new JButton();
		button2 = new JButton();
		comboBox1 = new JComboBox();
		panel4 = new JPanel();
		label11 = new JLabel();
		label4 = new JLabel();
		label5 = new JLabel();
		textField1 = new JTextField();
		comboBox4 = new JComboBox();
		comboBox5 = new JComboBox();
		panel2 = new JPanel();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel3 = new JPanel();
		label15 = new JLabel();
		comboBox6 = new JComboBox();
		label16 = new JLabel();
		hSpacer1 = new JPanel(null);
		button4 = new JButton();
		label17 = new JLabel();
		label18 = new JLabel();
		label19 = new JLabel();
		label20 = new JLabel();
		label21 = new JLabel();
		button5 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
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

			//---- label6 ----
			label6.setText("Rule code");
			panel1.add(label6, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label12 ----
			label12.setText("Rule name");
			panel1.add(label12, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label14 ----
			label14.setText("Sort by");
			panel1.add(label14, new GridBagConstraints(12, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- label7 ----
			label7.setText("text");
			panel1.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label8 ----
			label8.setText("text");
			panel1.add(label8, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label9 ----
			label9.setText("text");
			panel1.add(label9, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label10 ----
			label10.setText("text");
			panel1.add(label10, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label13 ----
			label13.setText("text");
			panel1.add(label13, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button3 ----
			button3.setText("Search");
			button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel1.add(button3, new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0,
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
			panel1.add(button2, new GridBagConstraints(10, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel1.add(comboBox1, new GridBagConstraints(12, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel4 ========
		{
			panel4.setLayout(new GridBagLayout());
			((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

			//---- label11 ----
			label11.setText("Concept name");
			panel4.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label4 ----
			label4.setText("Status");
			panel4.add(label4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label5 ----
			label5.setText("Disposition");
			panel4.add(label5, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel4.add(textField1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel4.add(comboBox4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel4.add(comboBox5, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
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

			//---- label15 ----
			label15.setText("Show ");
			label15.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label15, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- comboBox6 ----
			comboBox6.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(comboBox6, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label16 ----
			label16.setText("rows per page");
			label16.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label16, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel3.add(hSpacer1, new GridBagConstraints(3, 0, 9, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button4 ----
			button4.setText("<");
			button4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel3.add(button4, new GridBagConstraints(12, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label17 ----
			label17.setText("0");
			label17.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label17, new GridBagConstraints(13, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label18 ----
			label18.setText("to");
			label18.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label18, new GridBagConstraints(14, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label19 ----
			label19.setText("0");
			label19.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label19, new GridBagConstraints(15, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label20 ----
			label20.setText("of");
			label20.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label20, new GridBagConstraints(16, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label21 ----
			label21.setText("0");
			label21.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label21, new GridBagConstraints(17, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button5 ----
			button5.setText(">");
			button5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button5.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button4ActionPerformed(e);
				}
			});
			panel3.add(button5, new GridBagConstraints(18, 0, 1, 1, 0.0, 0.0,
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
	private JLabel label6;
	private JLabel label12;
	private JLabel label14;
	private JLabel label7;
	private JLabel label8;
	private JLabel label9;
	private JLabel label10;
	private JLabel label13;
	private JButton button3;
	private JButton button2;
	private JComboBox comboBox1;
	private JPanel panel4;
	private JLabel label11;
	private JLabel label4;
	private JLabel label5;
	private JTextField textField1;
	private JComboBox comboBox4;
	private JComboBox comboBox5;
	private JPanel panel2;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel3;
	private JLabel label15;
	private JComboBox comboBox6;
	private JLabel label16;
	private JPanel hSpacer1;
	private JButton button4;
	private JLabel label17;
	private JLabel label18;
	private JLabel label19;
	private JLabel label20;
	private JLabel label21;
	private JButton button5;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
