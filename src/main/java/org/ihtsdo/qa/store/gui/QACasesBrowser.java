/*

 * Created by JFormDesigner on Thu Nov 25 12:27:48 GMT-03:00 2010
 */

package org.ihtsdo.qa.store.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.qa.gui.ObjectTransferHandler;
import org.ihtsdo.qa.store.QAStoreBI;
import org.ihtsdo.qa.store.model.DispositionStatus;
import org.ihtsdo.qa.store.model.QACase;
import org.ihtsdo.qa.store.model.QACoordinate;
import org.ihtsdo.qa.store.model.QADatabase;
import org.ihtsdo.qa.store.model.Rule;
import org.ihtsdo.qa.store.model.TerminologyComponent;
import org.ihtsdo.qa.store.model.view.QACasesReportColumn;
import org.ihtsdo.qa.store.model.view.QACasesReportLine;
import org.ihtsdo.qa.store.model.view.QACasesReportPage;
import org.ihtsdo.rules.RulesLibrary;

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
	private CaseTableModel tableModel;
	private QACoordinate coordinate;
	private Rule rule;
	private int startLine = 0;
	private int finalLine = 0;
	private int totalLines = 0;
	private ObjectTransferHandler th = null;
	private HashMap<QACasesReportColumn, Object> filter;
	private JTabbedPane parentTabbedPanel = null;
	
	private  QAResultsBrowser resultsPanel;
	private TerminologyComponent headerComponent; 
	private QADatabase qaDatabase;
	private QACase selectedCase;
	private TerminologyComponent selectedCaseComponent;
	private Set<I_GetConceptData> users;
	
	public QACasesBrowser(QAStoreBI store, QAResultsBrowser resultsPanel, JTabbedPane parentTabbedPanel) {
//		try {
//			th = new ObjectTransferHandler(Terms.get().getActiveAceFrameConfig(), null);
//		} catch (TerminologyException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		users = new HashSet<I_GetConceptData>();//.getUsers();

		filter = new HashMap<QACasesReportColumn, Object>();
		
		this.store = store;
		this.resultsPanel = resultsPanel;
		this.parentTabbedPanel = parentTabbedPanel;
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

		tableModel = new CaseTableModel();

		table1.setModel(tableModel);
		table1.setTransferHandler(th);
		table1.setDragEnabled(true);
		
		TableColumn conceptUuidCol = table1.getColumnModel().getColumn(tableModel.CONCEPT_UUID);
		TableColumn conceptSctidCol =table1.getColumnModel().getColumn(tableModel.CONCEPT_SCTID);
		TableColumn rowCheckBoxCol =table1.getColumnModel().getColumn(tableModel.ROW_CHECKBOX);
		
		conceptUuidCol.setPreferredWidth(0);
		conceptUuidCol.setMinWidth(0);
		conceptUuidCol.setMaxWidth(0);

		conceptSctidCol.setPreferredWidth(0);
		conceptSctidCol.setMinWidth(0);
		conceptSctidCol.setMaxWidth(0);
		
		rowCheckBoxCol.setPreferredWidth(20);
		rowCheckBoxCol.setMinWidth(20);
		rowCheckBoxCol.setMaxWidth(20);
		
		tableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				int col = e.getColumn();
				if(col == tableModel.ROW_CHECKBOX){
					batchAssigneTo.setEnabled(true);
					batchDispositionStatus.setEnabled(true);
					bathcSaveButton.setEnabled(true);
					messageLabel.setText("");
				}
			}
		});
		
		batchDispositionStatus.addItem("");
		for (DispositionStatus dispStatus : dispositionStatuses) {
			batchDispositionStatus.addItem(dispStatus);
		}
		
		batchAssigneTo.addItem("");
//		batchAssigneTo.addItem("Ale");
//		batchAssigneTo.addItem("Alo");
//		batchAssigneTo.addItem("Cesar");
//		batchAssigneTo.addItem("Manu");
		for (I_GetConceptData user : users) {
			batchAssigneTo.addItem(user.toString());
		}
		
		
		if (coordinate != null && rule != null) {
			setupPanel(store);
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

	public void setupPanel(QAStoreBI store) {
		this.store = store;
		this.coordinate = resultsPanel.getQACoordinate();
		this.rule = resultsPanel.getRule();

		panel4.setVisible(false);

		clearTable1();

		QADatabase qaDatabase = getQaDatabase(coordinate.getDatabaseUuid()); 
		label7.setText(qaDatabase.getName());
		
		this.headerComponent = getHeaderComponent(coordinate.getPathUuid());
		if (headerComponent != null) {
			label8.setText(headerComponent.getComponentName());
		}
		label9.setText(coordinate.getViewPointTime());
		label10.setText(rule.getRuleCode());
		label13.setText(rule.getName());

		setupStatusCombo();
		setupDispositionCombo();
		conceptNameTextField.setText("");
		showFilters = false;
		filterButton.setText("Show filters");
		// updateTable1();
		updatePageCounters();
	}

	private QADatabase getQaDatabase(UUID databaseUuid) {
		if(qaDatabase != null && qaDatabase.getDatabaseUuid().equals(databaseUuid)){
			return qaDatabase;
		}
		qaDatabase = store.getQADatabase(databaseUuid);
		return qaDatabase;
	}

	private TerminologyComponent getHeaderComponent(UUID pathUuid) {
		if(headerComponent != null && headerComponent.getComponentUuid().equals(pathUuid)){
			return headerComponent;
		}
		headerComponent = store.getComponent(pathUuid);;
		return headerComponent;
	}

	public TerminologyComponent getSelectedCaseComponent(UUID componentUuid) {
		if(selectedCaseComponent != null && selectedCaseComponent.getComponentUuid().equals(componentUuid)){
			return selectedCaseComponent;
		}
		selectedCaseComponent = store.getComponent(componentUuid);;
		return selectedCaseComponent;
	}

	private void updateTable1() {
		clearTable1();

		LinkedHashMap<QACasesReportColumn, Boolean> sortBy = new LinkedHashMap<QACasesReportColumn, Boolean>();
		sortBy.put(QACasesReportColumn.CONCEPT_NAME, true);

		updateFilters(filter);
		
		Integer selectedPageLengh = Integer.parseInt((String) comboBox6.getSelectedItem());
		QACasesReportPage page = store.getQACasesReportLinesByPage(coordinate, rule.getRuleUuid(), sortBy, filter, startLine, selectedPageLengh);
		List<QACasesReportLine> lines = page.getLines();
		totalLines = page.getTotalLines();
		startLine = page.getInitialLine();
		finalLine = page.getFinalLine();
		updatePageCounters();
		for (QACasesReportLine line : lines) {
			boolean lineApproved = true;
			if (lineApproved) {
				List<Object> row = new ArrayList<Object>();
				row.add(line.getComponent().getComponentUuid().toString());
				row.add(false);
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
				row.add(line.getQaCase());
				tableModel.addData(row);
			}
		}
		table1.revalidate();
		table1.repaint();
	}

	private void updateFilters(HashMap<QACasesReportColumn, Object> filter) {
		filter.clear();
		if (showFilters) {
			String conceptNameFilter = conceptNameTextField.getText();
			if(conceptNameFilter != null && !conceptNameFilter.trim().equals("")){
				filter.put(QACasesReportColumn.CONCEPT_NAME, conceptNameFilter);
			}
			String statusFilter = (String) statusComboBox.getSelectedItem();
			if(!statusFilter.equals("Any")){
				if(statusFilter.equals("Open")){
					filter.put(QACasesReportColumn.STATUS, "Open");
				}else if(statusFilter.equalsIgnoreCase("Closed")){
					filter.put(QACasesReportColumn.STATUS, "Closed");
				}
			}
			Object dispoObj = dispoStatusComboBox.getSelectedItem();
			if(dispoObj != null && dispoObj instanceof DispositionStatus){
				DispositionStatus dispStatus = (DispositionStatus)dispoObj;
				filter.put(QACasesReportColumn.DISPOSITION, dispStatus.getDispositionStatusUuid().toString());
			}
				
		}else{
			filter.put(QACasesReportColumn.STATUS, "Open");
		}
	}

	private void clearTable1() {
		tableModel.clearData();
		table1.revalidate();
		table1.repaint();
	}

	private void setupDispositionCombo() {
		dispoStatusComboBox.removeAllItems();
		dispoStatusComboBox.addItem("Any");
		for (DispositionStatus loopStatus : dispositionStatuses) {
			dispoStatusComboBox.addItem(loopStatus);
		}
	}

	private void setupStatusCombo() {
		statusComboBox.removeAllItems();
		statusComboBox.addItem("Any");
		statusComboBox.addItem("Open");
		statusComboBox.addItem("Closed");
	}

	private void setupSortByCombo() {
		comboBox1.removeAllItems();
		comboBox1.addItem("Concept name, status, disposition");
		comboBox1.addItem("Status, disposition, name");
		comboBox1.addItem("Disposition, name, status");
	}

	private void button2ActionPerformed(ActionEvent e) {
		if (showFilters) {
			filterButton.setText("Show filters");
			panel4.setVisible(false);
			showFilters = false;
		} else {
			filterButton.setText("Hide filters");
			panel4.setVisible(true);
			showFilters = true;
		}
	}

	private void updatePageCounters() {
		startLineLabel.setText(String.valueOf(startLine));
		endLineLabel.setText(String.valueOf(finalLine));
		totalLinesLabel.setText(String.valueOf(totalLines));
		previousButton.setEnabled(startLine > 1);
		nextButton.setEnabled(finalLine < totalLines);
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

	private void table1MouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			int tabCount = parentTabbedPanel.getTabCount();
			int selectedRow = table1.getSelectedRow();
			Object[] rowData = tableModel.getRow(selectedRow);
			String conceptName = rowData[tableModel.CONCEPT_NAME].toString();
			boolean tabExists = false;
			for (int i = 0; i < tabCount; i++) {
				if(conceptName.length() > 7){
					if (parentTabbedPanel.getTitleAt(i).equals(conceptName.substring(0, 7) + "...")) {
						tabExists = true;
						parentTabbedPanel.setSelectedIndex(i);
					}
				}else{
					if (parentTabbedPanel.getTitleAt(i).equals(conceptName)) {
						tabExists = true;
						parentTabbedPanel.setSelectedIndex(i);
					}
				}
			}

			if (!tabExists) {
				Rule  rule = resultsPanel.getRule();
				TerminologyComponent component = getSelectedCaseComponent(UUID.fromString(rowData[0].toString()));
				
				selectedCase = (QACase) rowData[tableModel.CASE];
				
				QACaseDetailsPanel rulesDetailsPanel = new QACaseDetailsPanel(rule,component,selectedCase, dispositionStatuses, headerComponent,qaDatabase,store);
				if(conceptName.length() > 7){
					parentTabbedPanel.addTab(conceptName.substring(0, 7) + "...", null, rulesDetailsPanel, conceptName);
				}else{
					parentTabbedPanel.addTab(conceptName, null, rulesDetailsPanel, conceptName);
				}
				initTabComponent(parentTabbedPanel.getTabCount() - 1);
				parentTabbedPanel.setSelectedIndex(parentTabbedPanel.getTabCount() - 1);
			}
		}
	}
	
	private void initTabComponent(int i) {
		parentTabbedPanel.setTabComponentAt(i, new ButtonTabComponent(parentTabbedPanel));
	}
	private void bathcSaveButtonActionPerformed(ActionEvent e) {
		List<QACase> qaCaseList = new ArrayList<QACase>();
		for (int i = startLine-1; i < finalLine; i++) {
			Object[] row = tableModel.getRow(i);
			
			if(row[tableModel.ROW_CHECKBOX] instanceof Boolean){
				if((Boolean)row[tableModel.ROW_CHECKBOX]){
					Object qaCaseObject = row[tableModel.CASE];
					if(qaCaseObject != null && qaCaseObject instanceof QACase){
						QACase qaCase = (QACase)qaCaseObject;
						Object selectedBatchDispoStatusObject = batchDispositionStatus.getSelectedItem();
						DispositionStatus selectedDispoStatus = null;
						if(selectedBatchDispoStatusObject instanceof DispositionStatus){
							selectedDispoStatus = (DispositionStatus)selectedBatchDispoStatusObject;
						}
						String caseAssignedTo = qaCase.getAssignedTo() == null ? "" : qaCase.getAssignedTo();
						if((!caseAssignedTo.equals(batchAssigneTo.getSelectedItem()) && !batchAssigneTo.getSelectedItem().toString().equals("")) 
								|| (selectedDispoStatus != null && !selectedDispoStatus.getDispositionStatusUuid().equals(qaCase.getDispositionStatusUuid()))){
							boolean caseChanged = false;
							if(!batchAssigneTo.getSelectedItem().toString().equals("")){
								qaCase.setAssignedTo(batchAssigneTo.getSelectedItem().toString());
								caseChanged = true;
							}
							if(selectedDispoStatus != null){
								qaCase.setDispositionStatusUuid(selectedDispoStatus.getDispositionStatusUuid());
								caseChanged = true;
							}
							if(caseChanged){
								qaCaseList.add(qaCase);
							}
						}
					}
				}
			}
		}
		if(!qaCaseList.isEmpty()){
			try {
				store.persistQACaseList(qaCaseList);
				batchAssigneTo.setEnabled(false);
				batchAssigneTo.setEnabled(false);
				bathcSaveButton.setEnabled(false);
				messageLabel.setText("Rows Updated succesfully");
				for (int i = startLine-1; i < finalLine; i++) {
					tableModel.setValueAt(false, i, tableModel.ROW_CHECKBOX);
					QACase qaCase = (QACase)tableModel.getValueAt(i, tableModel.CASE);
					tableModel.setValueAt(qaCase.getAssignedTo(), i, tableModel.ASSIGNED_TO);

					for (DispositionStatus currentDisop : dispositionStatuses) {
						if(qaCase.getDispositionStatusUuid().equals(currentDisop.getDispositionStatusUuid())){
							tableModel.setValueAt(currentDisop, i, tableModel.DISPOSITION_STATUS);
						}
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				for (int i = startLine-1; i < finalLine; i++) {
					tableModel.setValueAt(false, i, tableModel.ROW_CHECKBOX);
				}
				messageLabel.setForeground(Color.RED);
				messageLabel.setText("Problems updating cases, please try again later.");
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
		label6 = new JLabel();
		label12 = new JLabel();
		label7 = new JLabel();
		label8 = new JLabel();
		label9 = new JLabel();
		label10 = new JLabel();
		label13 = new JLabel();
		label14 = new JLabel();
		comboBox1 = new JComboBox();
		searchButton = new JButton();
		filterButton = new JButton();
		separator1 = new JSeparator();
		panel4 = new JPanel();
		label11 = new JLabel();
		label4 = new JLabel();
		label5 = new JLabel();
		conceptNameTextField = new JTextField();
		statusComboBox = new JComboBox();
		dispoStatusComboBox = new JComboBox();
		panel2 = new JPanel();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel3 = new JPanel();
		label15 = new JLabel();
		comboBox6 = new JComboBox();
		label16 = new JLabel();
		hSpacer1 = new JPanel(null);
		previousButton = new JButton();
		startLineLabel = new JLabel();
		label18 = new JLabel();
		endLineLabel = new JLabel();
		label20 = new JLabel();
		totalLinesLabel = new JLabel();
		nextButton = new JButton();
		separator2 = new JSeparator();
		panel5 = new JPanel();
		label17 = new JLabel();
		batchAssigneTo = new JComboBox();
		label19 = new JLabel();
		batchDispositionStatus = new JComboBox();
		messageLabel = new JLabel();
		bathcSaveButton = new JButton();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 12, 0, 0, 0, 9, 22, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {99, 52, 88, 0, 96, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

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

			//---- label7 ----
			label7.setText("text");
			panel1.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label8 ----
			label8.setText("text");
			panel1.add(label8, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label9 ----
			label9.setText("text");
			panel1.add(label9, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label10 ----
			label10.setText("text");
			panel1.add(label10, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label13 ----
			label13.setText("text");
			panel1.add(label13, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label14 ----
			label14.setText("Sort by");
			panel1.add(label14, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel1.add(comboBox1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- searchButton ----
			searchButton.setText("Search");
			searchButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			searchButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel1.add(searchButton, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- filterButton ----
			filterButton.setText("Show filters");
			filterButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			filterButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			panel1.add(filterButton, new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 2, 0), 0, 0));
		add(separator1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 2, 0), 0, 0));

		//======== panel4 ========
		{
			panel4.setLayout(new GridBagLayout());
			((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {395, 0, 0, 0};
			((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

			//---- label11 ----
			label11.setText("Concept name");
			panel4.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 15), 0, 0));

			//---- label4 ----
			label4.setText("Status");
			panel4.add(label4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 15), 0, 0));

			//---- label5 ----
			label5.setText("Disposition");
			panel4.add(label5, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
			panel4.add(conceptNameTextField, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 15), 0, 0));
			panel4.add(statusComboBox, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 15), 0, 0));
			panel4.add(dispoStatusComboBox, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 2, 0), 0, 0));

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
		add(panel2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 2, 0), 0, 0));

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

			//---- previousButton ----
			previousButton.setText("<");
			previousButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			previousButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel3.add(previousButton, new GridBagConstraints(12, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- startLineLabel ----
			startLineLabel.setText("0");
			startLineLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(startLineLabel, new GridBagConstraints(13, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label18 ----
			label18.setText("to");
			label18.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label18, new GridBagConstraints(14, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- endLineLabel ----
			endLineLabel.setText("0");
			endLineLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(endLineLabel, new GridBagConstraints(15, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label20 ----
			label20.setText("of");
			label20.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label20, new GridBagConstraints(16, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- totalLinesLabel ----
			totalLinesLabel.setText("0");
			totalLinesLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(totalLinesLabel, new GridBagConstraints(17, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- nextButton ----
			nextButton.setText(">");
			nextButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			nextButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button4ActionPerformed(e);
				}
			});
			panel3.add(nextButton, new GridBagConstraints(18, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 2, 0), 0, 0));
		add(separator2, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 2, 0), 0, 0));

		//======== panel5 ========
		{
			panel5.setLayout(new GridBagLayout());
			((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};
			((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label17 ----
			label17.setText("Assign to");
			panel5.add(label17, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- batchAssigneTo ----
			batchAssigneTo.setToolTipText("Select cases from the above table to make multiple assignment");
			batchAssigneTo.setEnabled(false);
			panel5.add(batchAssigneTo, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label19 ----
			label19.setText("Disposition status");
			panel5.add(label19, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- batchDispositionStatus ----
			batchDispositionStatus.setToolTipText("Select cases from the above table to change disposition statuces");
			batchDispositionStatus.setEnabled(false);
			panel5.add(batchDispositionStatus, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel5.add(messageLabel, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- bathcSaveButton ----
			bathcSaveButton.setText("Save");
			bathcSaveButton.setEnabled(false);
			bathcSaveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bathcSaveButtonActionPerformed(e);
				}
			});
			panel5.add(bathcSaveButton, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel5, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JLabel label2;
	private JLabel label3;
	private JLabel label6;
	private JLabel label12;
	private JLabel label7;
	private JLabel label8;
	private JLabel label9;
	private JLabel label10;
	private JLabel label13;
	private JLabel label14;
	private JComboBox comboBox1;
	private JButton searchButton;
	private JButton filterButton;
	private JSeparator separator1;
	private JPanel panel4;
	private JLabel label11;
	private JLabel label4;
	private JLabel label5;
	private JTextField conceptNameTextField;
	private JComboBox statusComboBox;
	private JComboBox dispoStatusComboBox;
	private JPanel panel2;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel3;
	private JLabel label15;
	private JComboBox comboBox6;
	private JLabel label16;
	private JPanel hSpacer1;
	private JButton previousButton;
	private JLabel startLineLabel;
	private JLabel label18;
	private JLabel endLineLabel;
	private JLabel label20;
	private JLabel totalLinesLabel;
	private JButton nextButton;
	private JSeparator separator2;
	private JPanel panel5;
	private JLabel label17;
	private JComboBox batchAssigneTo;
	private JLabel label19;
	private JComboBox batchDispositionStatus;
	private JLabel messageLabel;
	private JButton bathcSaveButton;
	// //GEN-END:variables
	
	class CaseTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -2582804161676112393L;

		public final Integer CONCEPT_UUID = 0;
		public final Integer ROW_CHECKBOX = 1;
		public final Integer CONCEPT_SCTID = 2;
		public final Integer CONCEPT_NAME = 3;
		public final Integer STATUS = 4;
		public final Integer DISPOSITION_STATUS = 5;
		public final Integer ASSIGNED_TO = 6;
		public final Integer TIME = 7;
		public final Integer CASE = 8;
		
		private String[] columnNames = { "Concept UUID",
				" ",
				"Concept Sctid", 
				"Concept Name", 
				"Status", 
				"Disposition", 
				"Assigned to", 
				"Time", 
				"Case" };

		private List<Object[]> dataList = new ArrayList<Object[]>();
		private Object[][] data = new Object[0][8];
		
		public Object[] getRow(int rowNum){
			return data[rowNum];
		}
		
		public void clearData() {
			dataList = new ArrayList<Object[]>();
			data = new Object[0][8];
		}

		public void addData(List<Object> row) {
			dataList.add(row.toArray());
			data = new Object[dataList.size()][4];
			for (int j = 0; j < dataList.size(); j++) {
				data[j] = dataList.get(j);
			}
		}
		
		public String getColumnName(int col) {
			return columnNames[col];
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
			if(getValueAt(0, columnIndex) != null){
				return getValueAt(0, columnIndex).getClass();
			}else{
				return super.getColumnClass(columnIndex);
			}
		}
		
		public boolean isCellEditable(int x, int y) {
			if(y == ROW_CHECKBOX){
				return true;
			}
			return false;
		}

	}

}
