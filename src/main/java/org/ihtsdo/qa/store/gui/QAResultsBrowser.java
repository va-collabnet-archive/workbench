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
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.ihtsdo.qa.store.QAStoreBI;
import org.ihtsdo.qa.store.model.Category;
import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.QACoordinate;
import org.ihtsdo.qa.store.model.QADatabase;
import org.ihtsdo.qa.store.model.Rule;
import org.ihtsdo.qa.store.model.Severity;
import org.ihtsdo.qa.store.model.TerminologyComponent;
import org.ihtsdo.qa.store.model.view.RulesReportColumn;
import org.ihtsdo.qa.store.model.view.RulesReportLine;
import org.ihtsdo.qa.store.model.view.RulesReportPage;

/**
 * @author Guillermo Reynoso
 */
public class QAResultsBrowser extends JPanel {

	private static final long serialVersionUID = 1L;
	private QAStoreBI store;
	private ResutlTableModel tableModel;
	private LinkedHashSet<DispositionStatus> dispositionStatuses;
	private List<Severity> severities;
	private boolean showFilters = false;
	private QACoordinate coordinate;
	private int startLine = 0;
	private int finalLine = 0;
	private int totalLines = 0;
	LinkedHashMap<RulesReportColumn, Boolean> sortBy = null;
	private JTabbedPane parentTabbedPanel = null;
	private List<Category> allCategories;

	public QAResultsBrowser(QAStoreBI store) {
		this.store = store;
		allCategories = store.getAllCategories();
		initComponents();
		panel4.setVisible(false);
		dispositionStatuses = new LinkedHashSet<DispositionStatus>();
		dispositionStatuses.addAll(store.getAllDispositionStatus());
		severities = new ArrayList<Severity>();
		severities.addAll(store.getAllSeverities());

		List<String> columnNames = new ArrayList<String>();
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

		tableModel = new ResutlTableModel(columnNames.toArray());
		table1.setModel(tableModel);
		sortBy = new LinkedHashMap<RulesReportColumn, Boolean>();
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

	public void setParentTabbedPanel(JTabbedPane parentTabbedPanel) {
		this.parentTabbedPanel = parentTabbedPanel;
	}

	private void setupSortByCombo() {
		sortByComboBox.removeAllItems();
		sortByComboBox.addItem(RulesReportColumn.RULE_NAME);
		sortByComboBox.addItem(RulesReportColumn.OPEN);
		sortByComboBox.addItem(RulesReportColumn.CLEARED);
		sortByComboBox.addItem(RulesReportColumn.ESCALATED);
		sortByComboBox.addItem(RulesReportColumn.DEFERRED);
		sortBy.put(RulesReportColumn.RULE_NAME, true);
	}

	private void updatePageCounters() {
		startLineLable.setText(String.valueOf(startLine));
		endLineLabel.setText(String.valueOf(finalLine));
		totalLinesLabel.setText(String.valueOf(totalLines));
		previousButton.setEnabled(startLine > 1);
		nextButton.setEnabled(finalLine < totalLines);
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
		categoryComboBox.removeAllItems();
		categoryComboBox.addItem("Any");
		categoryComboBox.addItem("Concept model");
		categoryComboBox.addItem("Descriptions model");
	}

	private void setupSeverityCombo() {
		severityComboBox.removeAllItems();
		severityComboBox.addItem("Any");
		for (Severity loopSeverity : severities) {
			severityComboBox.addItem(loopSeverity);
		}
	}

	private void setupDispositionCombo() {
		dispositionComboBox.removeAllItems();
		dispositionComboBox.addItem("Any");
		for (DispositionStatus loopStatus : dispositionStatuses) {
			dispositionComboBox.addItem(loopStatus);
		}
	}

	private void setupStatusCombo() {
		statusComboBox.removeAllItems();
		statusComboBox.addItem("Any");
		statusComboBox.addItem("Open cases");
		statusComboBox.addItem("No open cases");
		statusComboBox.addItem("Closed cases");
		statusComboBox.addItem("No closed cases");
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
		if (comboBox1.getSelectedItem() != null && comboBox2.getSelectedItem() != null && comboBox3.getSelectedItem() != null) {
			QADatabase database = (QADatabase) comboBox1.getSelectedItem();
			TerminologyComponent path = (TerminologyComponent) comboBox2.getSelectedItem();
			String time = (String) comboBox3.getSelectedItem();
			coordinate = new QACoordinate(database.getDatabaseUuid(), path.getComponentUuid(), time);
			if (startLine < 1) {
				startLine = 1;
			}
			updateTable1(coordinate);
		}
	}

	private void clearTable1() {
		tableModel.clearData();
		table1.revalidate();
		table1.repaint();
	}

	private void updateTable1(QACoordinate coordinate) {
		try {
			clearTable1();
			// List<RulesReportLine> lines =
			// store.getRulesReportLines(coordinate);

			HashMap<RulesReportColumn, Object> filter = new HashMap<RulesReportColumn, Object>();
			getFilterData(filter);
			Integer selectedPageLengh = Integer.parseInt((String) comboBox4.getSelectedItem());
			RulesReportPage page = store.getRulesReportLinesByPage(coordinate, sortBy, filter, startLine, selectedPageLengh);
			List<RulesReportLine> lines = page.getLines();
			totalLines = page.getTotalLines();
			startLine = page.getInitialLine();
			finalLine = page.getFinalLine();
			updatePageCounters();
			for (RulesReportLine line : lines) {
				boolean lineApproved = true;
				if (lineApproved) {
					LinkedList<Object> row = new LinkedList<Object>();
					row.add(String.valueOf(line.getRule().getRuleCode()));
					row.add(line.getRule());
					Category rowCategory = new Category();
					for (Category category : allCategories) {
						if(category.getCategoryUuid().toString().equals(line.getRule().getCategory())){
							rowCategory = category;
						}
					}
					row.add(rowCategory);
					row.add(line.getRule().getSeverity());
					row.add(line.getStatusCount().get(true));
					for (DispositionStatus loopStatus : dispositionStatuses) {
						row.add(line.getDispositionStatusCount().get(loopStatus.getDispositionStatusUuid()));
					}
					row.add(line.getStatusCount().get(false));
					if (line.getLastExecutionTime() != null) {
						row.add(line.getLastExecutionTime().toString());
					} else {
						row.add("");
					}
					row.add(line.getRule().getRuleUuid());
					tableModel.addData(row);
				}
			}
			table1.revalidate();
			table1.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getFilterData(HashMap<RulesReportColumn, Object> filter) {
		filter.clear();
		if (showFilters) {
			String ruleCodeFilter = ruleCodeTextField.getText();
			if (ruleCodeFilter != null && !ruleCodeFilter.trim().equals("")) {
				filter.put(RulesReportColumn.RULE_CODE, ruleCodeFilter);
			}
			String ruleNameFilter = ruleNameTextField.getText();
			if (ruleNameFilter != null && !ruleNameFilter.trim().equals("")) {
				filter.put(RulesReportColumn.RULE_NAME, ruleNameFilter);
			}
			String categoryFilter = (String) categoryComboBox.getSelectedItem();
			if (categoryFilter != null && !categoryFilter.equals("Any")) {
				filter.put(RulesReportColumn.CATEGORY, categoryFilter);
			}
			Object severityObj = severityComboBox.getSelectedItem();
			if (severityObj != null && severityObj instanceof Severity) {
				Severity severityFilter = (Severity) severityObj;
				filter.put(RulesReportColumn.SEVERITY, severityFilter);
			}
			String statusFilter = (String) statusComboBox.getSelectedItem();
			if (!statusFilter.equals("Any")) {
				if (statusFilter.equals("Open cases")) {
					filter.put(RulesReportColumn.STATUS, "Open cases");
				} else if (statusFilter.equals("No open cases")) {
					filter.put(RulesReportColumn.STATUS, "No open cases");
				} else if (statusFilter.equals("Closed cases")) {
					filter.put(RulesReportColumn.STATUS, "Closed cases");
				} else if (statusFilter.equals("No closed cases")) {
					filter.put(RulesReportColumn.STATUS, "No closed cases");
				}
			}
			Object dispoObj = dispositionComboBox.getSelectedItem();
			if (dispoObj != null && dispoObj instanceof DispositionStatus) {
				DispositionStatus dispStatus = (DispositionStatus) dispoObj;
				filter.put(RulesReportColumn.DISPOSITION_STATUS_FILTER, dispStatus.getDispositionStatusUuid().toString());
			}

		}

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
		if (e.getClickCount() == 2) {
			int tabCount = parentTabbedPanel.getTabCount();
			int selectedRow = table1.getSelectedRow();
			Object[] rowData = tableModel.getRow(selectedRow);
			String ruleName = rowData[1].toString();
			boolean tabExists = false;
			for (int i = 0; i < tabCount; i++) {
				if (parentTabbedPanel.getTitleAt(i).equals(ruleName.substring(0, 7) + "...")) {
					tabExists = true;
					parentTabbedPanel.setSelectedIndex(i);
				}
			}

			if (!tabExists) {
				Rule  rule = store.getRule(UUID.fromString(rowData[12].toString()));
				RulesDetailsPanel rulesDetailsPanel = new RulesDetailsPanel(store, rule,allCategories,severities);
				
				parentTabbedPanel.addTab(ruleName.substring(0, 7) + "...", null, rulesDetailsPanel, ruleName);
				initTabComponent(parentTabbedPanel.getTabCount() - 1);
				parentTabbedPanel.setSelectedIndex(parentTabbedPanel.getTabCount() - 1);
			}
		}

	}

	private void initTabComponent(int i) {
		parentTabbedPanel.setTabComponentAt(i, new ButtonTabComponent(parentTabbedPanel));
	}

	private void previousButtonActionPerformed(ActionEvent e) {
		// previous page
		Integer selectedPageLengh = Integer.parseInt((String) comboBox4.getSelectedItem());
		startLine = startLine - selectedPageLengh;
		if (startLine < 1) {
			startLine = 1;
		}
		updateTable1(coordinate);
	}

	private void nextButtonActionPerformed(ActionEvent e) {
		// next page
		Integer selectedPageLengh = Integer.parseInt((String) comboBox4.getSelectedItem());
		startLine = startLine + selectedPageLengh;
		updateTable1(coordinate);
	}

	private void sortByComboBoxItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Object selectedItem = e.getItem();
			if (selectedItem instanceof RulesReportColumn) {
				RulesReportColumn column = (RulesReportColumn) selectedItem;
				sortBy.clear();
				sortBy.put(column, true);
			}
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
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
		sortByComboBox = new JComboBox();
		panel4 = new JPanel();
		label11 = new JLabel();
		label8 = new JLabel();
		label10 = new JLabel();
		label9 = new JLabel();
		label4 = new JLabel();
		label5 = new JLabel();
		ruleCodeTextField = new JTextField();
		ruleNameTextField = new JTextField();
		categoryComboBox = new JComboBox();
		severityComboBox = new JComboBox();
		statusComboBox = new JComboBox();
		dispositionComboBox = new JComboBox();
		panel2 = new JPanel();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel3 = new JPanel();
		label6 = new JLabel();
		comboBox4 = new JComboBox();
		label7 = new JLabel();
		hSpacer1 = new JPanel(null);
		previousButton = new JButton();
		startLineLable = new JLabel();
		label16 = new JLabel();
		endLineLabel = new JLabel();
		label14 = new JLabel();
		totalLinesLabel = new JLabel();
		nextButton = new JButton();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0E-4 };

		// ======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 107, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };

			// ---- label1 ----
			label1.setText("Database");
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label2 ----
			label2.setText("Path");
			panel1.add(label2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label3 ----
			label3.setText("Time");
			panel1.add(label3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label17 ----
			label17.setText("Sort by");
			panel1.add(label17, new GridBagConstraints(11, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- comboBox1 ----
			comboBox1.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					comboBox1ItemStateChanged(e);
				}
			});
			panel1.add(comboBox1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- comboBox2 ----
			comboBox2.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					comboBox2ItemStateChanged(e);
				}
			});
			panel1.add(comboBox2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- comboBox3 ----
			comboBox3.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					comboBox3ItemStateChanged(e);
				}
			});
			panel1.add(comboBox3, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- button1 ----
			button1.setText("Search");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					searchActionPerformed(e);
				}
			});
			panel1.add(button1, new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- button2 ----
			button2.setText("Show filters");
			button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			panel1.add(button2, new GridBagConstraints(8, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- sortByComboBox ----
			sortByComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					sortByComboBoxItemStateChanged(e);
				}
			});
			panel1.add(sortByComboBox, new GridBagConstraints(11, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== panel4 ========
		{
			panel4.setLayout(new GridBagLayout());
			((GridBagLayout) panel4.getLayout()).columnWidths = new int[] { 85, 255, 159, 115, 0, 0, 0 };
			((GridBagLayout) panel4.getLayout()).rowHeights = new int[] { 0, 0, 0 };
			((GridBagLayout) panel4.getLayout()).columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0E-4 };
			((GridBagLayout) panel4.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };

			// ---- label11 ----
			label11.setText("Rule code");
			panel4.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 15), 0, 0));

			// ---- label8 ----
			label8.setText("Rule name");
			panel4.add(label8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 15), 0, 0));

			// ---- label10 ----
			label10.setText("Category");
			panel4.add(label10, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 15), 0, 0));

			// ---- label9 ----
			label9.setText("Severity");
			panel4.add(label9, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 15), 0, 0));

			// ---- label4 ----
			label4.setText("Status");
			panel4.add(label4, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 15), 0, 0));

			// ---- label5 ----
			label5.setText("Disposition");
			panel4.add(label5, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
			panel4.add(ruleCodeTextField, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));

			// ---- ruleNameTextField ----
			ruleNameTextField.setColumns(50);
			panel4.add(ruleNameTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));
			panel4.add(categoryComboBox, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));
			panel4.add(severityComboBox, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));
			panel4.add(statusComboBox, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));
			panel4.add(dispositionComboBox, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout) panel2.getLayout()).columnWidths = new int[] { 0, 0 };
			((GridBagLayout) panel2.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
			((GridBagLayout) panel2.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

			// ======== scrollPane1 ========
			{

				// ---- table1 ----
				table1.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						table1MouseClicked(e);
					}
				});
				scrollPane1.setViewportView(table1);
			}
			panel2.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout) panel3.getLayout()).columnWidths = new int[] { 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 58, 0, 0, 0, 0, 0, 0, 0 };
			((GridBagLayout) panel3.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel3.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel3.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- label6 ----
			label6.setText("Show ");
			label6.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- comboBox4 ----
			comboBox4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(comboBox4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label7 ----
			label7.setText("rows per page");
			label7.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label7, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
			panel3.add(hSpacer1, new GridBagConstraints(3, 0, 9, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- previousButton ----
			previousButton.setText("<");
			previousButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			previousButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					previousButtonActionPerformed(e);
				}
			});
			panel3.add(previousButton, new GridBagConstraints(12, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- startLineLable ----
			startLineLable.setText("0");
			startLineLable.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(startLineLable, new GridBagConstraints(13, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label16 ----
			label16.setText("to");
			label16.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label16, new GridBagConstraints(14, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- endLineLabel ----
			endLineLabel.setText("0");
			endLineLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(endLineLabel, new GridBagConstraints(15, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label14 ----
			label14.setText("of");
			label14.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label14, new GridBagConstraints(16, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- totalLinesLabel ----
			totalLinesLabel.setText("0");
			totalLinesLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(totalLinesLabel, new GridBagConstraints(17, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- nextButton ----
			nextButton.setText(">");
			nextButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			nextButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					nextButtonActionPerformed(e);
				}
			});
			panel3.add(nextButton, new GridBagConstraints(18, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
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
	private JComboBox sortByComboBox;
	private JPanel panel4;
	private JLabel label11;
	private JLabel label8;
	private JLabel label10;
	private JLabel label9;
	private JLabel label4;
	private JLabel label5;
	private JTextField ruleCodeTextField;
	private JTextField ruleNameTextField;
	private JComboBox categoryComboBox;
	private JComboBox severityComboBox;
	private JComboBox statusComboBox;
	private JComboBox dispositionComboBox;
	private JPanel panel2;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel3;
	private JLabel label6;
	private JComboBox comboBox4;
	private JLabel label7;
	private JPanel hSpacer1;
	private JButton previousButton;
	private JLabel startLineLable;
	private JLabel label16;
	private JLabel endLineLabel;
	private JLabel label14;
	private JLabel totalLinesLabel;
	private JButton nextButton;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	class ResutlTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -2582804161676112393L;
		public static final int ROW_DATA_SIZE = 13;
		private Object[] columnNames = { "Rule code", "Rule name", "Category", "Severity", "Open", "Cleared", "Escalated", "Deferred", "In Discussion", "Not clear", "Closed", "Last run", "Rule UUID" };

		public ResutlTableModel(Object[] columnNames) {
			super();
			this.columnNames = columnNames;
		}

		private List<Object[]> dataList = new ArrayList<Object[]>();
		private Object[][] data = new Object[0][ROW_DATA_SIZE];

		public Object[] getRow(int rowNum) {
			return data[rowNum];
		}

		public void clearData() {
			dataList = new ArrayList<Object[]>();
			data = new Object[0][ROW_DATA_SIZE];
		}

		public void addData(List<Object> row) {
			dataList.add(row.toArray());
			data = new Object[dataList.size()][4];
			for (int j = 0; j < dataList.size(); j++) {
				data[j] = dataList.get(j);
			}
		}

		public String getColumnName(int col) {
			return columnNames[col].toString();
		}

		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

		@Override
		public int getColumnCount() {
			return columnNames.length - 1;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public Object getValueAt(int row, int column) {
			return data[row][column];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return super.getColumnClass(columnIndex);
		}

		public boolean isCellEditable(int x, int y) {
			return false;
		}

	}

}
