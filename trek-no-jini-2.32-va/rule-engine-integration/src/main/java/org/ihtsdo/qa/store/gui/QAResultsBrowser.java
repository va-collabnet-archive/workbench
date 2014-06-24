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

package org.ihtsdo.qa.store.gui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.log.AceLog;
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
 * The Class QAResultsBrowser.
 *
 * @author Guillermo Reynoso
 */
public class QAResultsBrowser extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The store. */
	private QAStoreBI store;
	
	/** The table model. */
	private ResutlTableModel tableModel;
	
	/** The disposition statuses. */
	private Set<DispositionStatus> dispositionStatuses;
	
	/** The severities. */
	private List<Severity> severities;
	
	/** The show filters. */
	private boolean showFilters = false;
	
	/** The filter changed. */
	private boolean filterChanged = false;
	
	/** The coordinate. */
	private QACoordinate coordinate;
	
	/** The start line. */
	private int startLine = 0;
	
	/** The final line. */
	private int finalLine = 0;
	
	/** The total lines. */
	private int totalLines = 0;
	
	/** The sort by. */
	Map<RulesReportColumn, Boolean> sortBy = null;
	
	/** The parent tabbed panel. */
	private JTabbedPane parentTabbedPanel = null;
	
	/** The all categories. */
	private List<Category> allCategories;
	
	/** The rule. */
	private Rule rule = null;
	
	/** The sdf. */
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
	
	/** The first load. */
	private boolean firstLoad = true;
	
	/** The qa store panel. */
	private QAStorePanel qaStorePanel;
	
	/** The all paths for database. */
	HashMap<QADatabase, List<TerminologyComponent>> allPathsForDatabase = new HashMap<QADatabase, List<TerminologyComponent>>();
	
	/** The all databases. */
	List<QADatabase> allDatabases;
	
	/** The all times for path. */
	HashMap<String, List<String>> allTimesForPath = new HashMap<String, List<String>>();

	/**
	 * Instantiates a new qA results browser.
	 *
	 * @param store the store
	 * @param parentTabbedPane the parent tabbed pane
	 * @param qaStorePanel the qa store panel
	 */
	public QAResultsBrowser(QAStoreBI store, JTabbedPane parentTabbedPane, QAStorePanel qaStorePanel) {
		this.store = store;
		this.qaStorePanel = qaStorePanel;
		allCategories = new ArrayList<Category>();
		allCategories = store.getAllCategories();
		initComponents();
		this.parentTabbedPanel = parentTabbedPane;

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
		columnNames.add("Rule date");
		columnNames.add("Last run");

		tableModel = new ResutlTableModel(columnNames.toArray());
		table1.setModel(tableModel);

		table1.getColumnModel().getColumn(0).setPreferredWidth(0);
		table1.getColumnModel().getColumn(0).setMinWidth(0);
		table1.getColumnModel().getColumn(0).setMaxWidth(0);
		table1.getColumnModel().getColumn(0).setWidth(0);

		TableColumn ruleNameCol = table1.getColumnModel().getColumn(1);
		ruleNameCol.setPreferredWidth(350);
		ruleNameCol.setMinWidth(250);
		ruleNameCol.setMaxWidth(450);

		TableColumn categoryCol = table1.getColumnModel().getColumn(2);
		categoryCol.setPreferredWidth(70);
		categoryCol.setMinWidth(70);
		categoryCol.setMaxWidth(150);

		TableColumn severityColumn = table1.getColumnModel().getColumn(3);
		severityColumn.setPreferredWidth(70);
		severityColumn.setMinWidth(70);
		severityColumn.setMaxWidth(150);

		TableColumn openColumn = table1.getColumnModel().getColumn(4);
		openColumn.setPreferredWidth(50);
		openColumn.setMinWidth(50);
		openColumn.setMaxWidth(150);

		TableColumn clearedCol = table1.getColumnModel().getColumn(5);
		clearedCol.setPreferredWidth(50);
		clearedCol.setMinWidth(50);
		clearedCol.setMaxWidth(150);

		TableColumn escalatedCol = table1.getColumnModel().getColumn(6);
		escalatedCol.setPreferredWidth(60);
		escalatedCol.setMinWidth(60);
		escalatedCol.setMaxWidth(150);

		TableColumn differedCol = table1.getColumnModel().getColumn(7);
		differedCol.setPreferredWidth(60);
		differedCol.setMinWidth(60);
		differedCol.setMaxWidth(150);

		TableColumn indiscutionCol = table1.getColumnModel().getColumn(8);
		indiscutionCol.setPreferredWidth(80);
		indiscutionCol.setMinWidth(80);
		indiscutionCol.setMaxWidth(150);

		TableColumn notClear = table1.getColumnModel().getColumn(9);
		notClear.setPreferredWidth(70);
		notClear.setMinWidth(70);
		notClear.setMaxWidth(150);

		TableColumn closedCol = table1.getColumnModel().getColumn(10);
		closedCol.setPreferredWidth(70);
		closedCol.setMinWidth(70);
		closedCol.setMaxWidth(150);

		JTextArea ruleNameField = new JTextArea();
		ruleNameField.setRows(2);

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
		setupOrderCombo();
		setupSortByCombo();
		filterChanged = false;
	}

	/**
	 * Gets the rule.
	 *
	 * @return the rule
	 */
	public Rule getRule() {
		int selectedRow = table1.getSelectedRow();
		Object[] rowData = tableModel.getRow(selectedRow);
		if (rule != null && rule.getRuleUuid().toString().equals(rowData[12].toString())) {
			return rule;
		}
		rule = store.getRule(UUID.fromString(rowData[12].toString()));
		return rule;
	}

	/**
	 * Setup order combo.
	 */
	private void setupOrderCombo() {
		orderCombo.removeAllItems();
		orderCombo.addItem("Ascendent");
		orderCombo.addItem("Descendent");
	}

	/**
	 * Setup sort by combo.
	 */
	private void setupSortByCombo() {
		sortByComboBox.removeAllItems();
		sortByComboBox.addItem(RulesReportColumn.RULE_NAME);
		sortByComboBox.addItem(RulesReportColumn.OPEN);
		sortByComboBox.addItem(RulesReportColumn.CLEARED);
		sortByComboBox.addItem(RulesReportColumn.ESCALATED);
		sortByComboBox.addItem(RulesReportColumn.DEFERRED);
		sortByComboBox.addItem(RulesReportColumn.RULE_DATE);
		sortBy.put(RulesReportColumn.RULE_NAME, true);
	}

	/**
	 * Update page counters.
	 */
	private void updatePageCounters() {
		startLineLable.setText(String.valueOf(startLine));
		endLineLabel.setText(String.valueOf(finalLine));
		totalLinesLabel.setText(String.valueOf(totalLines));
		previousButton.setEnabled(startLine > 1);
		nextButton.setEnabled(finalLine < totalLines);
		panel3.revalidate();
	}

	/**
	 * Setup page lines combo.
	 */
	private void setupPageLinesCombo() {
		showItemsCombo.removeAllItems();
		showItemsCombo.addItem("25");
		showItemsCombo.addItem("50");
		showItemsCombo.addItem("75");
		showItemsCombo.addItem("100");
	}

	/**
	 * Gets the table.
	 *
	 * @return the table
	 */
	public JTable getTable() {
		return table1;
	}

	/**
	 * Gets the qA coordinate.
	 *
	 * @return the qA coordinate
	 */
	public QACoordinate getQACoordinate() {
		return coordinate;
	}

	/**
	 * Setup category combo.
	 */
	private void setupCategoryCombo() {
		categoryComboBox.removeAllItems();
		categoryComboBox.addItem("Any");
		for (Category category : allCategories) {
			categoryComboBox.addItem(category);
		}
	}

	/**
	 * Setup severity combo.
	 */
	private void setupSeverityCombo() {
		severityComboBox.removeAllItems();
		severityComboBox.addItem("Any");
		for (Severity loopSeverity : severities) {
			severityComboBox.addItem(loopSeverity);
		}
	}

	/**
	 * Setup disposition combo.
	 */
	private void setupDispositionCombo() {
		dispositionComboBox.removeAllItems();
		dispositionComboBox.addItem("Any");
		for (DispositionStatus loopStatus : dispositionStatuses) {
			dispositionComboBox.addItem(loopStatus);
		}
	}

	/**
	 * Setup status combo.
	 */
	private void setupStatusCombo() {
		statusComboBox.removeAllItems();
		statusComboBox.addItem("Any");
		statusComboBox.addItem("Open cases");
		statusComboBox.addItem("No open cases");
		statusComboBox.addItem("Closed cases");
		statusComboBox.addItem("No closed cases");
	}

	/**
	 * Setup databases combo.
	 */
	private void setupDatabasesCombo() {
		comboBox1.removeAllItems();
		if (allDatabases == null) {
			allDatabases = store.getAllDatabases();
		}
		if (allDatabases != null) {
			for (QADatabase loopDatabase : allDatabases) {
				comboBox1.addItem(loopDatabase);
			}
		}
	}

	/**
	 * Setup paths combo.
	 */
	private void setupPathsCombo() {
		comboBox2.removeAllItems();
		if (comboBox1.getSelectedItem() != null && allPathsForDatabase != null) {
			QADatabase selectedDatabase = (QADatabase) comboBox1.getSelectedItem();
			List<TerminologyComponent> paths = null;
			if (!allPathsForDatabase.containsKey(selectedDatabase)) {
				paths= store.getAllPathsForDatabase(((QADatabase) comboBox1.getSelectedItem()).getDatabaseUuid());
				allPathsForDatabase.put(selectedDatabase, paths);
			} else {
				paths = allPathsForDatabase.get(selectedDatabase);
			}
			for (TerminologyComponent loopPath : paths) {
				comboBox2.addItem(loopPath);
			}
			comboBox2.setSelectedIndex(0);
		}
	}

	/**
	 * Setup time combo.
	 */
	private void setupTimeCombo() {
		comboBox3.removeAllItems();
		if (comboBox1.getSelectedItem() != null && comboBox2.getSelectedItem() != null && allTimesForPath != null) {
			UUID databaseUUID = ((QADatabase) comboBox1.getSelectedItem()).getDatabaseUuid();
			UUID pathUUID = ((TerminologyComponent) comboBox2.getSelectedItem()).getComponentUuid();
			String key = databaseUUID.toString() + pathUUID.toString();
			
			if (!allTimesForPath.containsKey(key)) {
				List<String> timeFOrPath = store.getAllTimesForPath(databaseUUID, pathUUID);
				allTimesForPath.put(key, timeFOrPath);
			}
			for (String loopDate : allTimesForPath.get(key)) {
				comboBox3.addItem(loopDate);
			}
		}
	}

	/**
	 * Search action performed.
	 *
	 * @param e the e
	 */
	private void searchActionPerformed(ActionEvent e) {
		search();
	}

	/**
	 * Search.
	 */
	private void search() {
		firstLoad = false;
		if (comboBox1.getSelectedItem() != null && comboBox2.getSelectedItem() != null && comboBox3.getSelectedItem() != null) {
			QADatabase database = (QADatabase) comboBox1.getSelectedItem();
			TerminologyComponent path = (TerminologyComponent) comboBox2.getSelectedItem();
			String time = (String) comboBox3.getSelectedItem();
			coordinate = new QACoordinate(database.getDatabaseUuid(), path.getComponentUuid(), time);
			if (startLine < 1) {
				startLine = 1;
			}
			if (filterChanged) {
				startLine = 1;
				filterChanged = false;
			}
			updateTable1(coordinate);
		}
	}

	/**
	 * Clear table1.
	 */
	private void clearTable1() {
		tableModel.clearData();
		table1.revalidate();
		table1.repaint();
	}

	/**
	 * Update table1.
	 *
	 * @param coordinate the coordinate
	 */
	private void updateTable1(QACoordinate coordinate) {
		try {
			clearTable1();
			// List<RulesReportLine> lines =
			// store.getRulesReportLines(coordinate);

			HashMap<RulesReportColumn, Object> filter = new HashMap<RulesReportColumn, Object>();
			getFilterData(filter);
			Integer selectedPageLengh = Integer.parseInt((String) showItemsCombo.getSelectedItem());
			RulesReportPage page = store.getRulesReportLinesByPage(coordinate, sortBy, filter, startLine, selectedPageLengh);
			List<RulesReportLine> lines = page.getLines();
			totalLines = page.getTotalLines();
			startLine = page.getInitialLine();
			finalLine = page.getFinalLine();
			updatePageCounters();
			for (RulesReportLine line : lines) {
				boolean lineApproved = true;
				if (lineApproved) {
					List<Object> row = new ArrayList<Object>();
					row.add(String.valueOf(line.getRule().getRuleCode()));
					row.add(line.getRule());
					Category rowCategory = new Category();
					for (Category category : allCategories) {
						if (category.getCategoryUuid().toString().equals(line.getRule().getCategory())) {
							rowCategory = category;
						}
					}
					row.add(rowCategory);
					row.add(line.getRule().getSeverity());
					Integer openStatusCount = line.getStatusCount().get(true);
					row.add(openStatusCount);
					int cleardCount = 0;
					for (DispositionStatus loopStatus : dispositionStatuses) {
						if (loopStatus.getName().equalsIgnoreCase("Cleared")) {
							cleardCount = line.getDispositionStatusCount().get(loopStatus.getDispositionStatusUuid());
						}
					}
					for (DispositionStatus loopStatus : dispositionStatuses) {
						if (loopStatus.getName().equalsIgnoreCase("not clear")) {
							row.add(openStatusCount - cleardCount);
						} else {
							row.add(line.getDispositionStatusCount().get(loopStatus.getDispositionStatusUuid()));
						}
					}
					row.add(line.getStatusCount().get(false));
					if (line.getRule().getEffectiveTime() != null) {
						row.add(sdf.format(new Date(line.getRule().getEffectiveTime().getTime())));
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
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Gets the filter data.
	 *
	 * @param filter the filter
	 * @return the filter data
	 */
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
			Object categoryFilter = categoryComboBox.getSelectedItem();
			if (categoryFilter != null && categoryFilter instanceof Category) {
				Category category = (Category) categoryFilter;
				filter.put(RulesReportColumn.CATEGORY, category.getCategoryUuid());
			}
			Object severityObj = severityComboBox.getSelectedItem();
			if (severityObj != null && severityObj instanceof Severity) {
				Severity severityFilter = (Severity) severityObj;
				filter.put(RulesReportColumn.SEVERITY, severityFilter.getSeverityUuid());
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

	/**
	 * Combo box1 item state changed.
	 *
	 * @param e the e
	 */
	private void comboBox1ItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			setupPathsCombo();
			setupTimeCombo();
			clearTable1();
		}
	}

	/**
	 * Combo box2 item state changed.
	 *
	 * @param e the e
	 */
	private void comboBox2ItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			setupTimeCombo();
			clearTable1();
		}
	}

	/**
	 * Combo box3 item state changed.
	 *
	 * @param e the e
	 */
	private void comboBox3ItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			clearTable1();
		}
	}

	/**
	 * Button2 action performed.
	 *
	 * @param e the e
	 */
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

	/**
	 * Table1 mouse clicked.
	 *
	 * @param e the e
	 */
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
				rule = getRule();
				RulesDetailsPanel rulesDetailsPanel = new RulesDetailsPanel(store, rule, allCategories, severities, this);
				parentTabbedPanel.addTab(ruleName.substring(0, 7) + "...", null, rulesDetailsPanel, ruleName);
				initTabComponent(parentTabbedPanel.getTabCount() - 1);
				parentTabbedPanel.setSelectedIndex(parentTabbedPanel.getTabCount() - 1);
			}
		}

	}

	/**
	 * Inits the tab component.
	 *
	 * @param i the i
	 */
	private void initTabComponent(int i) {
		parentTabbedPanel.setTabComponentAt(i, new ButtonTabComponent(parentTabbedPanel));
	}

	/**
	 * Previous button action performed.
	 *
	 * @param e the e
	 */
	private void previousButtonActionPerformed(ActionEvent e) {
		// previous page
		Integer selectedPageLengh = Integer.parseInt((String) showItemsCombo.getSelectedItem());
		startLine = startLine - selectedPageLengh;
		if (startLine < 1) {
			startLine = 1;
		}
		updateTable1(coordinate);
	}

	/**
	 * Next button action performed.
	 *
	 * @param e the e
	 */
	private void nextButtonActionPerformed(ActionEvent e) {
		// next page
		Integer selectedPageLengh = Integer.parseInt((String) showItemsCombo.getSelectedItem());
		startLine = startLine + selectedPageLengh;
		updateTable1(coordinate);
	}

	/**
	 * Sort by combo box item state changed.
	 *
	 * @param e the e
	 */
	private void sortByComboBoxItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Object selectedItem = e.getItem();
			if (selectedItem instanceof RulesReportColumn) {
				RulesReportColumn column = (RulesReportColumn) selectedItem;
				sortBy.clear();
				sortBy.put(column, orderCombo.getSelectedItem().toString().equals("Ascendent"));
			}
		}
	}

	/**
	 * Rule code text field key pressed.
	 *
	 * @param e the e
	 */
	private void ruleCodeTextFieldKeyPressed(KeyEvent e) {
		filterChanged = true;
	}

	/**
	 * Rule name text field key pressed.
	 *
	 * @param e the e
	 */
	private void ruleNameTextFieldKeyPressed(KeyEvent e) {
		filterChanged = true;
	}

	/**
	 * Category combo box item state changed.
	 *
	 * @param e the e
	 */
	private void categoryComboBoxItemStateChanged(ItemEvent e) {
		filterChanged = true;
	}

	/**
	 * Severity combo box item state changed.
	 *
	 * @param e the e
	 */
	private void severityComboBoxItemStateChanged(ItemEvent e) {
		filterChanged = true;
	}

	/**
	 * Status combo box item state changed.
	 *
	 * @param e the e
	 */
	private void statusComboBoxItemStateChanged(ItemEvent e) {
		filterChanged = true;
	}

	/**
	 * Disposition combo box item state changed.
	 *
	 * @param e the e
	 */
	private void dispositionComboBoxItemStateChanged(ItemEvent e) {
		filterChanged = true;
	}

	/**
	 * Order combo item state changed.
	 *
	 * @param e the e
	 */
	private void orderComboItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Object selectedItem = sortByComboBox.getSelectedItem();
			if (selectedItem instanceof RulesReportColumn) {
				RulesReportColumn column = (RulesReportColumn) selectedItem;
				sortBy.clear();
				sortBy.put(column, sortByComboBox.getSelectedItem().toString().equals("Ascendent"));
			}
		}
	}

	/**
	 * Show items combo item state changed.
	 *
	 * @param e the e
	 */
	private void showItemsComboItemStateChanged(ItemEvent e) {
		if (!firstLoad) {
			search();
		}
	}

	/**
	 * Update rule.
	 *
	 * @param rule the rule
	 */
	public void updateRule(Rule rule) {
		if (rule.getRuleUuid().toString().equals(this.rule.getRuleUuid().toString())) {
			this.rule = rule;
		}
		int rowCount = table1.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			Object[] rowData = tableModel.getRow(i);
			if (rule.getRuleUuid().toString().equals(rowData[12].toString())) {
				rowData[1] = rule;
				Category selectedCategory = new Category();
				for (Category category : allCategories) {
					if (category.getCategoryUuid().toString().equals(rule.getCategory())) {
						selectedCategory = category;
					}
				}
				rowData[2] = selectedCategory;
				rowData[3] = rule.getSeverity();
			}
		}
		table1.revalidate();
		table1.repaint();
		qaStorePanel.updateCasePanel();
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		label2 = new JLabel();
		label3 = new JLabel();
		label17 = new JLabel();
		label12 = new JLabel();
		comboBox1 = new JComboBox();
		comboBox2 = new JComboBox();
		comboBox3 = new JComboBox();
		button1 = new JButton();
		button2 = new JButton();
		sortByComboBox = new JComboBox();
		orderCombo = new JComboBox();
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
		showItemsCombo = new JComboBox();
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
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0E-4 };

		// ======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 107, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
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
			panel1.add(label17, new GridBagConstraints(10, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label12 ----
			label12.setText("Order");
			panel1.add(label12, new GridBagConstraints(11, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

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
			panel1.add(sortByComboBox, new GridBagConstraints(10, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- orderCombo ----
			orderCombo.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					orderComboItemStateChanged(e);
				}
			});
			panel1.add(orderCombo, new GridBagConstraints(11, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
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

			// ---- ruleCodeTextField ----
			ruleCodeTextField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					ruleCodeTextFieldKeyPressed(e);
				}
			});
			panel4.add(ruleCodeTextField, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));

			// ---- ruleNameTextField ----
			ruleNameTextField.setColumns(50);
			ruleNameTextField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					ruleNameTextFieldKeyPressed(e);
				}
			});
			panel4.add(ruleNameTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));

			// ---- categoryComboBox ----
			categoryComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					categoryComboBoxItemStateChanged(e);
				}
			});
			panel4.add(categoryComboBox, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));

			// ---- severityComboBox ----
			severityComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					severityComboBoxItemStateChanged(e);
				}
			});
			panel4.add(severityComboBox, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));

			// ---- statusComboBox ----
			statusComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					statusComboBoxItemStateChanged(e);
				}
			});
			panel4.add(statusComboBox, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));

			// ---- dispositionComboBox ----
			dispositionComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					dispositionComboBoxItemStateChanged(e);
				}
			});
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

			// ---- showItemsCombo ----
			showItemsCombo.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			showItemsCombo.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					showItemsComboItemStateChanged(e);
				}
			});
			panel3.add(showItemsCombo, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

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
	/** The panel1. */
	private JPanel panel1;
	
	/** The label1. */
	private JLabel label1;
	
	/** The label2. */
	private JLabel label2;
	
	/** The label3. */
	private JLabel label3;
	
	/** The label17. */
	private JLabel label17;
	
	/** The label12. */
	private JLabel label12;
	
	/** The combo box1. */
	private JComboBox comboBox1;
	
	/** The combo box2. */
	private JComboBox comboBox2;
	
	/** The combo box3. */
	private JComboBox comboBox3;
	
	/** The button1. */
	private JButton button1;
	
	/** The button2. */
	private JButton button2;
	
	/** The sort by combo box. */
	private JComboBox sortByComboBox;
	
	/** The order combo. */
	private JComboBox orderCombo;
	
	/** The panel4. */
	private JPanel panel4;
	
	/** The label11. */
	private JLabel label11;
	
	/** The label8. */
	private JLabel label8;
	
	/** The label10. */
	private JLabel label10;
	
	/** The label9. */
	private JLabel label9;
	
	/** The label4. */
	private JLabel label4;
	
	/** The label5. */
	private JLabel label5;
	
	/** The rule code text field. */
	private JTextField ruleCodeTextField;
	
	/** The rule name text field. */
	private JTextField ruleNameTextField;
	
	/** The category combo box. */
	private JComboBox categoryComboBox;
	
	/** The severity combo box. */
	private JComboBox severityComboBox;
	
	/** The status combo box. */
	private JComboBox statusComboBox;
	
	/** The disposition combo box. */
	private JComboBox dispositionComboBox;
	
	/** The panel2. */
	private JPanel panel2;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The table1. */
	private JTable table1;
	
	/** The panel3. */
	private JPanel panel3;
	
	/** The label6. */
	private JLabel label6;
	
	/** The show items combo. */
	private JComboBox showItemsCombo;
	
	/** The label7. */
	private JLabel label7;
	
	/** The h spacer1. */
	private JPanel hSpacer1;
	
	/** The previous button. */
	private JButton previousButton;
	
	/** The start line lable. */
	private JLabel startLineLable;
	
	/** The label16. */
	private JLabel label16;
	
	/** The end line label. */
	private JLabel endLineLabel;
	
	/** The label14. */
	private JLabel label14;
	
	/** The total lines label. */
	private JLabel totalLinesLabel;
	
	/** The next button. */
	private JButton nextButton;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	/**
	 * The Class ResutlTableModel.
	 */
	class ResutlTableModel extends AbstractTableModel {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -2582804161676112393L;
		
		/** The Constant ROW_DATA_SIZE. */
		public static final int ROW_DATA_SIZE = 14;
		
		/** The column names. */
		private Object[] columnNames = { "Rule code", "Rule name", "Category", "Severity", "Open", "Cleared", "Escalated", "Deferred", "In Discussion", "Not clear", "Closed", "Rule date", "Last run", "Rule UUID" };

		/**
		 * Instantiates a new resutl table model.
		 *
		 * @param columnNames the column names
		 */
		public ResutlTableModel(Object[] columnNames) {
			super();
			this.columnNames = columnNames.clone();
		}

		/** The data list. */
		private List<Object[]> dataList = new ArrayList<Object[]>();
		
		/** The data. */
		private Object[][] data = new Object[0][ROW_DATA_SIZE];

		/**
		 * Gets the row.
		 *
		 * @param rowNum the row num
		 * @return the row
		 */
		public Object[] getRow(int rowNum) {
			return data[rowNum];
		}

		/**
		 * Clear data.
		 */
		public void clearData() {
			dataList = new ArrayList<Object[]>();
			data = new Object[0][ROW_DATA_SIZE];
			System.gc();
		}

		/**
		 * Adds the data.
		 *
		 * @param row the row
		 */
		public void addData(List<Object> row) {
			dataList.add(row.toArray());
			data = new Object[dataList.size()][ROW_DATA_SIZE];
			for (int j = 0; j < dataList.size(); j++) {
				data[j] = dataList.get(j);
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		public String getColumnName(int col) {
			return columnNames[col].toString();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		@Override
		public int getColumnCount() {
			return columnNames.length - 1;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return data.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int row, int column) {
			return data[row][column];
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return super.getColumnClass(columnIndex);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int x, int y) {
			return false;
		}

	}

}
