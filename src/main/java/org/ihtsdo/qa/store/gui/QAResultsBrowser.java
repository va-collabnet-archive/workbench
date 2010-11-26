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
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.ihtsdo.qa.store.QAStoreBI;
import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.QACoordinate;
import org.ihtsdo.qa.store.model.QADatabase;
import org.ihtsdo.qa.store.model.TerminologyComponent;
import org.ihtsdo.qa.store.model.view.RulesReportLine;

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

	public QAResultsBrowser(QAStoreBI store) {
		this.store = store;
		initComponents();
		
		dispositionStatuses = new LinkedHashSet<DispositionStatus>();
		dispositionStatuses.addAll(store.getAllDispositionStatus());
		
		LinkedHashSet<String> columnNames = new LinkedHashSet<String>();
		columnNames.add("Error code");
		columnNames.add("Rule name");
		columnNames.add("Severity");
		columnNames.add("Open");
		for (DispositionStatus loopStatus : dispositionStatuses) {
			columnNames.add(loopStatus.getName());
		}
		columnNames.add("Closed");
		columnNames.add("Last run");
		
//		String[] columnNamesArray = new String[columnNames.size()];
//		int i = 0;
//		for (String loopString : columnNames) {
//			columnNamesArray[i] = loopString;
//			i++;
//		}

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
			QACoordinate coordinate = new QACoordinate(database.getDatabaseUuid(), path.getComponentUuid(), time);
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
		List<RulesReportLine> lines = store.getRulesReportLines(coordinate);
		for (RulesReportLine line : lines) {
			LinkedHashSet<String> row = new LinkedHashSet<String>();
			row.add(String.valueOf(line.getRule().getRuleCode()));
			row.add(line.getRule().getName());
			row.add(String.valueOf(line.getRule().getSeverity()));
			row.add(String.valueOf(line.getStatusCount().get(true)));
			for (DispositionStatus loopStatus : dispositionStatuses) {
				row.add(String.valueOf(line.getDispositionStatusCount().get(loopStatus.getDispositionStatusUuid())));
			}
			row.add(String.valueOf(line.getStatusCount().get(false)));
			row.add(line.getLastExecutionTime().toString());
			tableModel.addRow(row.toArray());
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

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		label2 = new JLabel();
		label3 = new JLabel();
		comboBox1 = new JComboBox();
		comboBox2 = new JComboBox();
		comboBox3 = new JComboBox();
		button1 = new JButton();
		panel2 = new JPanel();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel3 = new JPanel();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 107, 0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
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
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
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
		add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
		}
		add(panel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JLabel label2;
	private JLabel label3;
	private JComboBox comboBox1;
	private JComboBox comboBox2;
	private JComboBox comboBox3;
	private JButton button1;
	private JPanel panel2;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel3;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
