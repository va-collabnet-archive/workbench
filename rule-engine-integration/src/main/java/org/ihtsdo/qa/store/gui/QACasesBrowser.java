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

import java.awt.Color;
import java.awt.Component;
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
import java.awt.event.MouseListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
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
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * The Class QACasesBrowser.
 *
 * @author Guillermo Reynoso
 */
public class QACasesBrowser extends JPanel {

	/** The logger. */
	private Logger logger = Logger.getLogger(QACasesBrowser.class);

	/** The config. */
	private I_ConfigAceFrame config = null;
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The show filters. */
	private boolean showFilters = false;
	
	/** The disposition statuses. */
	private LinkedHashSet<DispositionStatus> dispositionStatuses;
	
	/** The store. */
	private QAStoreBI store;
	
	/** The table model. */
	private CaseTableModel tableModel;
	
	/** The coordinate. */
	private QACoordinate coordinate;
	
	/** The rule. */
	private Rule rule;
	
	/** The start line. */
	private int startLine = 0;
	
	/** The final line. */
	private int finalLine = 0;
	
	/** The total lines. */
	private int totalLines = 0;
	
	/** The th. */
	private ObjectTransferHandler th = null;
	
	/** The filter. */
	private HashMap<QACasesReportColumn, Object> filter;
	
	/** The parent tabbed panel. */
	private JTabbedPane parentTabbedPanel = null;

	/** The results panel. */
	private QAResultsBrowser resultsPanel;
	
	/** The header component. */
	private TerminologyComponent headerComponent;
	
	/** The qa database. */
	private QADatabase qaDatabase;
	
	/** The selected case. */
	private QACase selectedCase;
	
	/** The selected case component. */
	private TerminologyComponent selectedCaseComponent;
	
	/** The users. */
	private Set<I_GetConceptData> users;
	
	/** The sdf. */
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
	
	/** The first load. */
	private boolean firstLoad = true;

	/** The inactive values set. */
	private I_IntSet inactiveValuesSet;

	/**
	 * Instantiates a new qA cases browser.
	 *
	 * @param store the store
	 * @param resultsPanel the results panel
	 * @param parentTabbedPanel the parent tabbed panel
	 */
	public QACasesBrowser(QAStoreBI store, QAResultsBrowser resultsPanel, JTabbedPane parentTabbedPanel) {
		try {
			config = Terms.get().getActiveAceFrameConfig();
			th = new ObjectTransferHandler(config, null);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		try {
			users = RulesLibrary.getUsers();
		} catch (Exception e) {
			users = new HashSet<I_GetConceptData>();
			AceLog.getAppLog().alertAndLogException(e);
		}

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

		caseTable.setModel(tableModel);
		caseTable.setTransferHandler(th);
		caseTable.setDragEnabled(true);

		int columnCount = caseTable.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			TableColumn column = caseTable.getColumnModel().getColumn(i);
			column.setCellRenderer(new CaseTableCellRenderer());
		}

		TableColumn tc = caseTable.getColumnModel().getColumn(1);
		tc.setCellEditor(caseTable.getDefaultEditor(Boolean.class));
		tc.setCellRenderer(caseTable.getDefaultRenderer(Boolean.class));
		tc.setHeaderRenderer(new CheckBoxHeader(new MyItemListener()));

		TableColumn conceptUuidCol = caseTable.getColumnModel().getColumn(tableModel.CONCEPT_UUID);
		TableColumn conceptSctidCol = caseTable.getColumnModel().getColumn(tableModel.CONCEPT_SCTID);
		TableColumn rowCheckBoxCol = caseTable.getColumnModel().getColumn(tableModel.ROW_CHECKBOX);
		TableColumn corloCol = caseTable.getColumnModel().getColumn(tableModel.COLOR);
		TableColumn caseCol = caseTable.getColumnModel().getColumn(tableModel.CASE);

		conceptUuidCol.setPreferredWidth(0);
		conceptUuidCol.setMinWidth(0);
		conceptUuidCol.setMaxWidth(0);

		conceptSctidCol.setPreferredWidth(0);
		conceptSctidCol.setMinWidth(0);
		conceptSctidCol.setMaxWidth(0);

		rowCheckBoxCol.setPreferredWidth(20);
		rowCheckBoxCol.setMinWidth(20);
		rowCheckBoxCol.setMaxWidth(20);

		corloCol.setPreferredWidth(0);
		corloCol.setMinWidth(0);
		corloCol.setMaxWidth(0);

		caseCol.setPreferredWidth(0);
		caseCol.setMinWidth(0);
		caseCol.setMaxWidth(0);

		tableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				int col = e.getColumn();
				if (col == tableModel.ROW_CHECKBOX) {
					batchAssigneTo.setEnabled(true);
					batchDispositionStatus.setEnabled(true);
					bathcSaveButton.setEnabled(true);
					messageLabel.setText("");
				}
			}
		});

		setupBatchDispoStatusCombo();

		batchAssigneTo.addItem("");
		if (users != null) {
			for (I_GetConceptData user : users) {
				batchAssigneTo.addItem(user.toString());
			}
		}

		if (coordinate != null && rule != null) {
			setupPanel(store);
		}
		setupAssignedToFilterCombo();
		setupPageLinesCombo();
		updatePageCounters();

	}

	/**
	 * Setup batch dispo status combo.
	 */
	private void setupBatchDispoStatusCombo() {
		if (rule != null) {
			batchDispositionStatus.removeAllItems();
			batchDispositionStatus.addItem("");
			for (DispositionStatus object : this.dispositionStatuses) {
				if (rule.isWhitelistAllowed() && object.getName().equals("Cleared")) {
					batchDispositionStatus.addItem(object);
				} else if (!object.getName().equals("Cleared")) {
					batchDispositionStatus.addItem(object);
				}
			}
			batchDispositionStatus.revalidate();
			batchDispositionStatus.repaint();
		}
	}

	/**
	 * Setup page lines combo.
	 */
	private void setupPageLinesCombo() {
		comboBox6.removeAllItems();
		comboBox6.addItem("25");
		comboBox6.addItem("50");
		comboBox6.addItem("75");
		comboBox6.addItem("100");
		comboBox6.addItem("500");
		comboBox6.addItem("1000");
	}

	/**
	 * Setup assigned to filter combo.
	 */
	private void setupAssignedToFilterCombo() {
		Iterator<I_GetConceptData> it = users.iterator();
		assignedToFilterComboBox.addItem("Any");
		while (it.hasNext()) {
			I_GetConceptData user = (I_GetConceptData) it.next();
			assignedToFilterComboBox.addItem(user);
		}
	}

	/**
	 * Sets the up panel.
	 *
	 * @param store the new up panel
	 */
	public void setupPanel(QAStoreBI store) {
		setupBatchDispoStatusCombo();
		this.coordinate = resultsPanel.getQACoordinate();
		if (rule == null || !this.rule.getRuleUuid().equals(resultsPanel.getRule().getRuleUuid())) {
			if (this.rule != null) {
				this.rule = resultsPanel.getRule();
				setupBatchDispoStatusCombo();
			}
			this.store = store;
			this.rule = resultsPanel.getRule();
			panel4.setVisible(false);

			clearTable1();
			((CheckBoxHeader) caseTable.getColumnModel().getColumn(1).getHeaderRenderer()).setSelected(false);
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
			filter = new HashMap<QACasesReportColumn, Object>();
			// updateTable1();
			startLine = 0;
			totalLines = 0;
			finalLine = 0;
			updatePageCounters();
		}
	}

	/**
	 * Gets the qa database.
	 *
	 * @param databaseUuid the database uuid
	 * @return the qa database
	 */
	private QADatabase getQaDatabase(UUID databaseUuid) {
		if (qaDatabase != null && qaDatabase.getDatabaseUuid().equals(databaseUuid)) {
			return qaDatabase;
		}
		qaDatabase = store.getQADatabase(databaseUuid);
		return qaDatabase;
	}

	/**
	 * Gets the header component.
	 *
	 * @param pathUuid the path uuid
	 * @return the header component
	 */
	private TerminologyComponent getHeaderComponent(UUID pathUuid) {
		if (headerComponent != null && headerComponent.getComponentUuid().equals(pathUuid)) {
			return headerComponent;
		}
		headerComponent = store.getComponent(pathUuid);
		;
		return headerComponent;
	}

	/**
	 * Gets the selected case component.
	 *
	 * @param componentUuid the component uuid
	 * @return the selected case component
	 */
	public TerminologyComponent getSelectedCaseComponent(UUID componentUuid) {
		if (selectedCaseComponent != null && selectedCaseComponent.getComponentUuid().equals(componentUuid)) {
			return selectedCaseComponent;
		}
		selectedCaseComponent = store.getComponent(componentUuid);
		;
		return selectedCaseComponent;
	}

	/**
	 * Update table1.
	 */
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
				UUID componentUuid = line.getComponent().getComponentUuid();
				row.add(componentUuid.toString());
				row.add(false);
				row.add(String.valueOf(line.getComponent().getSctid()));
				row.add(line.getComponent().getComponentName());
				if (line.getQaCase().isActive()) {
					row.add("Open");
				} else {
					row.add("Closed");
				}
				row.add(sdf.format(line.getQaCase().getLastChangedState().getTime()));
				row.add(line.getDisposition().getName());
				row.add(line.getQaCase().getAssignedTo());
				row.add(sdf.format(line.getQaCase().getEffectiveTime().getTime()));
				row.add(line.getQaCase());
				try {
					if (config != null) {
						I_GetConceptData concept = null;
						ConceptChronicleBI newConcept = Ts.get().getConcept(componentUuid);
						try {
							
							concept = Terms.get().getConcept(componentUuid);
							List<? extends I_ConceptAttributeTuple> conceptAttributeTuples = concept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy());
							if (!conceptAttributeTuples.isEmpty()) {
								I_ConceptAttributeTuple attr = conceptAttributeTuples.get(0);
								if (attr.getStatusNid() != SnomedMetadataRfx.getSTATUS_CURRENT_NID()) {
									row.add(Color.LIGHT_GRAY);
								} else {
									row.add(Color.WHITE);
								}
							} else {
								row.add(Color.WHITE);
							}
						} catch (Exception e) {
							row.add(Color.WHITE);
						}
						long lastModification = Long.MIN_VALUE;
//						ConceptVersionBI conceptVersion = newConcept.getVersion(config.getViewCoordinate());
//						ConAttrChronicleBI attributes = conceptVersion.getConAttrs();
//						if(attributes.getVersion(config.getViewCoordinate()).getTime() > lastModification){
//							lastModification = attributes.getVersion(config.getViewCoordinate()).getTime() ;
//						}
//						
//						Collection<? extends DescriptionChronicleBI> descriptions = conceptVersion.getDescs();
//						for (DescriptionChronicleBI descriptionChronicleBI : descriptions) {
//							long descTime = descriptionChronicleBI.getVersion(config.getViewCoordinate()).getTime();
//							if(descTime > lastModification){
//								lastModification = descTime;
//							}
//						}
//						Collection<? extends RelationshipChronicleBI> relationships = conceptVersion.getRelsOutgoing();
//						for (RelationshipChronicleBI relationshipChronicleBI : relationships) {
//							relationshipChronicleBI.get
//							long relTime = relationshipChronicleBI.getVersion(config.getViewCoordinate()).getTime();
//							if(relTime > lastModification){
//								lastModification = relTime;
//							}
//						}
//						
//						Collection<? extends RefexChronicleBI<?>> refsets = conceptVersion.getRefexes();
//						for (RefexChronicleBI<?> refexChronicleBI : refsets) {
//							long refTime = refexChronicleBI.getVersion(config.getViewCoordinate()).getTime();
//							if(refTime > lastModification){
//								lastModification = refTime;
//							}
//							
//						}
						List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(null, null, config.getViewPositionSetReadOnly(), config.getPrecedence(),
								config.getConflictResolutionStrategy());
						for (I_DescriptionTuple desc : descriptions) {
							long descTime = desc.getTime();
							Collection<? extends RefexChronicleBI<?>> annotations = desc.getAnnotations();
							for (RefexChronicleBI<?> refexChronicleBI : annotations) {
								Collection<? extends RefexChronicleBI<?>> refexces = refexChronicleBI.getRefexes();
								for (RefexChronicleBI<?> refexChronicleBI2 : refexces) {
									long time = refexChronicleBI2.getVersion(config.getViewCoordinate()).getTime();
									if(time > lastModification){
										lastModification = time;
									}
								}
							}
							if (descTime > lastModification) {
								lastModification = descTime;
							}
						}


						List<? extends I_ConceptAttributeTuple> attributes = concept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy());
						for (I_ConceptAttributeTuple attr : attributes) {
							long attrTime = attr.getTime();
							if (attrTime > lastModification) {
								lastModification = attrTime;
							}
						}

						List<? extends I_RelTuple> sourcRels = concept.getSourceRelTuples(null, null, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
						for (I_RelTuple i_RelTuple : sourcRels) {
							int charNid = i_RelTuple.getCharacteristicNid();
							int anotherNid = SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID();
							int architectonicAuxInferredNid = ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.localize().getNid();
							int snomedMetadataInferredNid = SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid();
							if (anotherNid !=  charNid && charNid != architectonicAuxInferredNid 
									&& snomedMetadataInferredNid != charNid) {
								long relTime = i_RelTuple.getTime();
								if (relTime > lastModification) {
									lastModification = relTime;
								}
							}
						}

						row.add(sdf.format(new Date(lastModification)));

					}
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
					row.add("");
				}

				tableModel.addData(row);
			}
		}
		caseTable.revalidate();
		caseTable.repaint();
	}

	/**
	 * Update filters.
	 *
	 * @param filter the filter
	 */
	private void updateFilters(HashMap<QACasesReportColumn, Object> filter) {
		filter.clear();
		if (showFilters) {
			String conceptNameFilter = conceptNameTextField.getText();
			if (conceptNameFilter != null && !conceptNameFilter.trim().equals("")) {
				filter.put(QACasesReportColumn.CONCEPT_NAME, conceptNameFilter);
			}
			String statusFilter = (String) statusComboBox.getSelectedItem();
			if (!statusFilter.equals("Any")) {
				if (statusFilter.equals("Open")) {
					filter.put(QACasesReportColumn.STATUS, "Open");
				} else if (statusFilter.equalsIgnoreCase("Closed")) {
					filter.put(QACasesReportColumn.STATUS, "Closed");
				}
			}
			Object dispoObj = dispoStatusComboBox.getSelectedItem();
			if (dispoObj != null && dispoObj instanceof DispositionStatus) {
				DispositionStatus dispStatus = (DispositionStatus) dispoObj;
				filter.put(QACasesReportColumn.DISPOSITION, dispStatus.getDispositionStatusUuid().toString());
			}
			String assignedFilter = (String) assignedToFilterComboBox.getSelectedItem();
			if (assignedFilter != null && !assignedFilter.equalsIgnoreCase("any")) {
				filter.put(QACasesReportColumn.ASSIGNED_TO, assignedFilter);
			}

		} else {
			filter.put(QACasesReportColumn.STATUS, "Open");
		}
	}

	/**
	 * Clear table1.
	 */
	private void clearTable1() {
		bathcSaveButton.setEnabled(false);
		batchDispositionStatus.setEditable(false);
		batchAssigneTo.setEditable(false);
		tableModel.clearData();
		caseTable.revalidate();
		caseTable.repaint();
	}

	/**
	 * Setup disposition combo.
	 */
	private void setupDispositionCombo() {
		dispoStatusComboBox.removeAllItems();
		dispoStatusComboBox.addItem("Any");
		for (DispositionStatus dispStatus : this.dispositionStatuses) {
			if (!rule.isWhitelistAllowed() && dispStatus.getName().equalsIgnoreCase("cleared")) {
				dispoStatusComboBox.addItem(dispStatus);
			} else if (!dispStatus.getName().equals("Cleared")) {
				dispoStatusComboBox.addItem(dispStatus);
			}
		}
		dispoStatusComboBox.revalidate();
		dispoStatusComboBox.repaint();
	}

	/**
	 * Setup status combo.
	 */
	private void setupStatusCombo() {
		statusComboBox.removeAllItems();
		statusComboBox.addItem("Any");
		statusComboBox.addItem("Open");
		statusComboBox.addItem("Closed");
	}

	/**
	 * Setup sort by combo.
	 */
	private void setupSortByCombo() {
		comboBox1.removeAllItems();
		comboBox1.addItem("Concept name, status, disposition");
		comboBox1.addItem("Status, disposition, name");
		comboBox1.addItem("Disposition, name, status");
	}

	/**
	 * Button2 action performed.
	 *
	 * @param e the e
	 */
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

	/**
	 * Update page counters.
	 */
	private void updatePageCounters() {
		startLineLabel.setText(String.valueOf(startLine));
		endLineLabel.setText(String.valueOf(finalLine));
		totalLinesLabel.setText(String.valueOf(totalLines));
		previousButton.setEnabled(startLine > 1);
		nextButton.setEnabled(finalLine < totalLines);
		panel3.revalidate();
	}

	/**
	 * Button3 action performed.
	 *
	 * @param e the e
	 */
	private void button3ActionPerformed(ActionEvent e) {
		doSearch();
	}

	/**
	 * Button4 action performed.
	 *
	 * @param e the e
	 */
	private void button4ActionPerformed(ActionEvent e) {
		Integer selectedPageLengh = Integer.parseInt((String) comboBox6.getSelectedItem());
		startLine = startLine + selectedPageLengh;
		updateTable1();
	}

	/**
	 * Table1 mouse clicked.
	 *
	 * @param e the e
	 */
	private void table1MouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			int tabCount = parentTabbedPanel.getTabCount();
			int selectedRow = caseTable.getSelectedRow();
			Object[] rowData = tableModel.getRow(selectedRow);
			String conceptName = rowData[tableModel.CONCEPT_NAME].toString();
			boolean tabExists = false;
			for (int i = 0; i < tabCount; i++) {
				if (conceptName.length() > 7 && parentTabbedPanel.getToolTipTextAt(i) != null) {
					if (parentTabbedPanel.getToolTipTextAt(i).equals(conceptName.substring(0, 7) + " (" + rowData[tableModel.CONCEPT_UUID] + ")")) {
						tabExists = true;
						parentTabbedPanel.setSelectedIndex(i);
					}
				} else if (parentTabbedPanel.getToolTipTextAt(i) != null) {
					if (parentTabbedPanel.getToolTipTextAt(i).equals(conceptName + " (" + rowData[tableModel.CONCEPT_UUID] + ")")) {
						tabExists = true;
						parentTabbedPanel.setSelectedIndex(i);
					}
				}
			}

			if (!tabExists) {
				Rule rule = resultsPanel.getRule();
				TerminologyComponent component = getSelectedCaseComponent(UUID.fromString(rowData[0].toString()));

				selectedCase = (QACase) rowData[tableModel.CASE];

				QACaseDetailsPanel rulesDetailsPanel = new QACaseDetailsPanel(rule, component, selectedCase, dispositionStatuses, headerComponent, qaDatabase, store);
				if (conceptName.length() > 7) {
					parentTabbedPanel.addTab(conceptName.substring(0, 7) + "...", null, rulesDetailsPanel, conceptName + " (" + rowData[tableModel.CONCEPT_UUID] + ")");
				} else {
					parentTabbedPanel.addTab(conceptName, null, rulesDetailsPanel, conceptName + " (" + rowData[tableModel.CONCEPT_UUID] + ")");
				}
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
	 * Bathc save button action performed.
	 *
	 * @param e the e
	 */
	private void bathcSaveButtonActionPerformed(ActionEvent e) {
		List<QACase> qaCaseList = new ArrayList<QACase>();
		for (int i = startLine - 1; i < finalLine; i++) {
			Object[] row = tableModel.getRow(i);

			if (row[tableModel.ROW_CHECKBOX] instanceof Boolean) {
				if ((Boolean) row[tableModel.ROW_CHECKBOX]) {
					Object qaCaseObject = row[tableModel.CASE];
					if (qaCaseObject != null && qaCaseObject instanceof QACase) {
						QACase qaCase = (QACase) qaCaseObject;
						Object selectedBatchDispoStatusObject = batchDispositionStatus.getSelectedItem();
						DispositionStatus selectedDispoStatus = null;
						if (selectedBatchDispoStatusObject instanceof DispositionStatus) {
							selectedDispoStatus = (DispositionStatus) selectedBatchDispoStatusObject;
						}
						String caseAssignedTo = qaCase.getAssignedTo() == null ? "" : qaCase.getAssignedTo();
						if ((!caseAssignedTo.equals(batchAssigneTo.getSelectedItem()) && !batchAssigneTo.getSelectedItem().toString().equals(""))
								|| (selectedDispoStatus != null && !selectedDispoStatus.getDispositionStatusUuid().equals(qaCase.getDispositionStatusUuid()))) {
							boolean caseChanged = false;
							if (!batchAssigneTo.getSelectedItem().toString().equals("")) {
								qaCase.setAssignedTo(batchAssigneTo.getSelectedItem().toString());
								caseChanged = true;
							}
							if (selectedDispoStatus != null) {
								qaCase.setDispositionStatusUuid(selectedDispoStatus.getDispositionStatusUuid());
								caseChanged = true;
							}
							if (caseChanged) {
								qaCaseList.add(qaCase);
							}
						}
					}
				}
			}
		}
		if (!qaCaseList.isEmpty()) {
			try {
				store.persistQACaseList(qaCaseList);
				batchAssigneTo.setEnabled(false);
				batchAssigneTo.setEnabled(false);
				bathcSaveButton.setEnabled(false);
				messageLabel.setText("Rows Updated succesfully");
				for (int i = startLine - 1; i < finalLine; i++) {
					tableModel.setValueAt(false, i, tableModel.ROW_CHECKBOX);
					QACase qaCase = (QACase) tableModel.getValueAt(i, tableModel.CASE);
					tableModel.setValueAt(qaCase.getAssignedTo(), i, tableModel.ASSIGNED_TO);

					for (DispositionStatus currentDisop : dispositionStatuses) {
						if (qaCase.getDispositionStatusUuid().equals(currentDisop.getDispositionStatusUuid())) {
							tableModel.setValueAt(currentDisop, i, tableModel.DISPOSITION_STATUS);
						}
					}
				}
			} catch (Exception e1) {
				AceLog.getAppLog().alertAndLogException(e1);
				for (int i = startLine - 1; i < finalLine; i++) {
					tableModel.setValueAt(false, i, tableModel.ROW_CHECKBOX);
				}
				messageLabel.setForeground(Color.RED);
				messageLabel.setText("Problems updating cases, please try again later.");
			}
		}
	}

	/**
	 * Do search.
	 */
	private void doSearch() {
		firstLoad = false;
		// previous page
		Integer selectedPageLengh = Integer.parseInt((String) comboBox6.getSelectedItem());
		startLine = startLine - selectedPageLengh;
		if (startLine < 1) {
			startLine = 1;
		}
		updateTable1();

	}

	/**
	 * Combo box6 item state changed.
	 *
	 * @param e the e
	 */
	private void comboBox6ItemStateChanged(ItemEvent e) {
		if (!firstLoad) {
			doSearch();
		}
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
		label21 = new JLabel();
		conceptNameTextField = new JTextField();
		statusComboBox = new JComboBox();
		dispoStatusComboBox = new JComboBox();
		assignedToFilterComboBox = new JComboBox();
		panel2 = new JPanel();
		scrollPane1 = new JScrollPane();
		caseTable = new JTable();
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

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 12, 0, 0, 0, 9, 22, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4 };

		// ======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 99, 52, 88, 0, 96, 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };

			// ---- label1 ----
			label1.setText("Database");
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label2 ----
			label2.setText("Path");
			panel1.add(label2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label3 ----
			label3.setText("Time");
			panel1.add(label3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label6 ----
			label6.setText("Rule code");
			panel1.add(label6, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label12 ----
			label12.setText("Rule name");
			panel1.add(label12, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label7 ----
			label7.setText("text");
			panel1.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label8 ----
			label8.setText("text");
			panel1.add(label8, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label9 ----
			label9.setText("text");
			panel1.add(label9, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label10 ----
			label10.setText("text");
			panel1.add(label10, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label13 ----
			label13.setText("text");
			panel1.add(label13, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			// ---- label14 ----
			label14.setText("Sort by");
			panel1.add(label14, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
			panel1.add(comboBox1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- searchButton ----
			searchButton.setText("Search");
			searchButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			searchButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel1.add(searchButton, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- filterButton ----
			filterButton.setText("Show filters");
			filterButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			filterButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			panel1.add(filterButton, new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 2, 0), 0, 0));
		add(separator1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 2, 0), 0, 0));

		// ======== panel4 ========
		{
			panel4.setVisible(false);
			panel4.setLayout(new GridBagLayout());
			((GridBagLayout) panel4.getLayout()).columnWidths = new int[] { 395, 0, 0, 0, 0 };
			((GridBagLayout) panel4.getLayout()).rowHeights = new int[] { 0, 0, 0 };
			((GridBagLayout) panel4.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel4.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };

			// ---- label11 ----
			label11.setText("Concept name");
			panel4.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 15), 0, 0));

			// ---- label4 ----
			label4.setText("Status");
			panel4.add(label4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 15), 0, 0));

			// ---- label5 ----
			label5.setText("Disposition");
			panel4.add(label5, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 15), 0, 0));

			// ---- label21 ----
			label21.setText("Assigned to");
			panel4.add(label21, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
			panel4.add(conceptNameTextField, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));
			panel4.add(statusComboBox, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));
			panel4.add(dispoStatusComboBox, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 15), 0, 0));
			panel4.add(assignedToFilterComboBox, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 2, 0), 0, 0));

		// ======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout) panel2.getLayout()).columnWidths = new int[] { 0, 0 };
			((GridBagLayout) panel2.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
			((GridBagLayout) panel2.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

			// ======== scrollPane1 ========
			{

				// ---- caseTable ----
				caseTable.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						table1MouseClicked(e);
					}
				});
				scrollPane1.setViewportView(caseTable);
			}
			panel2.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 2, 0), 0, 0));

		// ======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout) panel3.getLayout()).columnWidths = new int[] { 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 58, 0, 0, 0, 0, 0, 0, 0 };
			((GridBagLayout) panel3.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel3.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel3.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- label15 ----
			label15.setText("Show ");
			label15.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label15, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- comboBox6 ----
			comboBox6.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			comboBox6.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					comboBox6ItemStateChanged(e);
				}
			});
			panel3.add(comboBox6, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label16 ----
			label16.setText("rows per page");
			label16.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label16, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
			panel3.add(hSpacer1, new GridBagConstraints(3, 0, 9, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- previousButton ----
			previousButton.setText("<");
			previousButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			previousButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel3.add(previousButton, new GridBagConstraints(12, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- startLineLabel ----
			startLineLabel.setText("0");
			startLineLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(startLineLabel, new GridBagConstraints(13, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label18 ----
			label18.setText("to");
			label18.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label18, new GridBagConstraints(14, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- endLineLabel ----
			endLineLabel.setText("0");
			endLineLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(endLineLabel, new GridBagConstraints(15, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label20 ----
			label20.setText("of");
			label20.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(label20, new GridBagConstraints(16, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

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
					button4ActionPerformed(e);
				}
			});
			panel3.add(nextButton, new GridBagConstraints(18, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 2, 0), 0, 0));
		add(separator2, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 2, 0), 0, 0));

		// ======== panel5 ========
		{
			panel5.setLayout(new GridBagLayout());
			((GridBagLayout) panel5.getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			((GridBagLayout) panel5.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel5.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel5.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- label17 ----
			label17.setText("Assign to");
			panel5.add(label17, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- batchAssigneTo ----
			batchAssigneTo.setToolTipText("Select cases from the above table to make multiple assignment");
			batchAssigneTo.setEnabled(false);
			panel5.add(batchAssigneTo, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label19 ----
			label19.setText("Disposition status");
			panel5.add(label19, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- batchDispositionStatus ----
			batchDispositionStatus.setToolTipText("Select cases from the above table to change disposition statuces");
			batchDispositionStatus.setEnabled(false);
			panel5.add(batchDispositionStatus, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
			panel5.add(messageLabel, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- bathcSaveButton ----
			bathcSaveButton.setText("Save");
			bathcSaveButton.setEnabled(false);
			bathcSaveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bathcSaveButtonActionPerformed(e);
				}
			});
			panel5.add(bathcSaveButton, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel5, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
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
	
	/** The label6. */
	private JLabel label6;
	
	/** The label12. */
	private JLabel label12;
	
	/** The label7. */
	private JLabel label7;
	
	/** The label8. */
	private JLabel label8;
	
	/** The label9. */
	private JLabel label9;
	
	/** The label10. */
	private JLabel label10;
	
	/** The label13. */
	private JLabel label13;
	
	/** The label14. */
	private JLabel label14;
	
	/** The combo box1. */
	private JComboBox comboBox1;
	
	/** The search button. */
	private JButton searchButton;
	
	/** The filter button. */
	private JButton filterButton;
	
	/** The separator1. */
	private JSeparator separator1;
	
	/** The panel4. */
	private JPanel panel4;
	
	/** The label11. */
	private JLabel label11;
	
	/** The label4. */
	private JLabel label4;
	
	/** The label5. */
	private JLabel label5;
	
	/** The label21. */
	private JLabel label21;
	
	/** The concept name text field. */
	private JTextField conceptNameTextField;
	
	/** The status combo box. */
	private JComboBox statusComboBox;
	
	/** The dispo status combo box. */
	private JComboBox dispoStatusComboBox;
	
	/** The assigned to filter combo box. */
	private JComboBox assignedToFilterComboBox;
	
	/** The panel2. */
	private JPanel panel2;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The case table. */
	private JTable caseTable;
	
	/** The panel3. */
	private JPanel panel3;
	
	/** The label15. */
	private JLabel label15;
	
	/** The combo box6. */
	private JComboBox comboBox6;
	
	/** The label16. */
	private JLabel label16;
	
	/** The h spacer1. */
	private JPanel hSpacer1;
	
	/** The previous button. */
	private JButton previousButton;
	
	/** The start line label. */
	private JLabel startLineLabel;
	
	/** The label18. */
	private JLabel label18;
	
	/** The end line label. */
	private JLabel endLineLabel;
	
	/** The label20. */
	private JLabel label20;
	
	/** The total lines label. */
	private JLabel totalLinesLabel;
	
	/** The next button. */
	private JButton nextButton;
	
	/** The separator2. */
	private JSeparator separator2;
	
	/** The panel5. */
	private JPanel panel5;
	
	/** The label17. */
	private JLabel label17;
	
	/** The batch assigne to. */
	private JComboBox batchAssigneTo;
	
	/** The label19. */
	private JLabel label19;
	
	/** The batch disposition status. */
	private JComboBox batchDispositionStatus;
	
	/** The message label. */
	private JLabel messageLabel;
	
	/** The bathc save button. */
	private JButton bathcSaveButton;

	// //GEN-END:variables

	/**
	 * The Class CaseTableModel.
	 */
	class CaseTableModel extends AbstractTableModel {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -2582804161676112393L;

		/** The CONCEP t_ uuid. */
		public final Integer CONCEPT_UUID = 0;
		
		/** The RO w_ checkbox. */
		public final Integer ROW_CHECKBOX = 1;
		
		/** The CONCEP t_ sctid. */
		public final Integer CONCEPT_SCTID = 2;
		
		/** The CONCEP t_ name. */
		public final Integer CONCEPT_NAME = 3;
		
		/** The STATUS. */
		public final Integer STATUS = 4;
		
		/** The LAS t_ statu s_ changed. */
		public final Integer LAST_STATUS_CHANGED = 5;
		
		/** The DISPOSITIO n_ status. */
		public final Integer DISPOSITION_STATUS = 6;
		
		/** The ASSIGNE d_ to. */
		public final Integer ASSIGNED_TO = 7;
		
		/** The TIME. */
		public final Integer TIME = 8;
		
		/** The CASE. */
		public final Integer CASE = 9;
		
		/** The COLOR. */
		public final Integer COLOR = 10;
		
		/** The CONCEP t_ las t_ modified. */
		public final Integer CONCEPT_LAST_MODIFIED = 11;

		/** The column names. */
		private String[] columnNames = { "Concept UUID", " ", "Concept Sctid", "Concept Name", "Status", "Last status change", "Disposition", "Assigned to", "Time", "Case", "Row Color", "Concept last modified" };

		/** The data list. */
		private List<Object[]> dataList = new ArrayList<Object[]>();
		
		/** The data. */
		private Object[][] data = new Object[0][11];

		/**
		 * Instantiates a new case table model.
		 */
		public CaseTableModel() {
			super();
			setInactiveValues();
		}

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
			data = new Object[0][11];
			//System.gc();
		}

		/**
		 * Adds the data.
		 *
		 * @param row the row
		 */
		public void addData(List<Object> row) {
			dataList.add(row.toArray());
			data = new Object[dataList.size()][4];
			for (int j = 0; j < dataList.size(); j++) {
				data[j] = dataList.get(j);
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		public String getColumnName(int col) {
			return columnNames[col];
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
			return columnNames.length;
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
			if (getValueAt(0, columnIndex) != null) {
				return getValueAt(0, columnIndex).getClass();
			} else {
				return super.getColumnClass(columnIndex);
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int x, int y) {
			if (y == ROW_CHECKBOX) {
				return true;
			}
			return false;
		}

		/**
		 * Gets the row color.
		 *
		 * @param row the row
		 * @return the row color
		 */
		public Color getRowColor(int row) {
			return (Color) data[row][COLOR];
		}

	}

	/**
	 * Gets the inactive values set.
	 *
	 * @return the inactive values set
	 */
	public I_IntSet getInactiveValuesSet() {
		return inactiveValuesSet;
	}

	/**
	 * Sets the inactive values.
	 */
	private void setInactiveValues() {
		try {
			inactiveValuesSet = Terms.get().newIntSet();
			inactiveValuesSet.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			inactiveValuesSet.add(ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid());
			inactiveValuesSet.add(ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid());
			inactiveValuesSet.add(ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid());
			inactiveValuesSet.add(ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid());
			inactiveValuesSet.add(ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid());
			inactiveValuesSet.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
			inactiveValuesSet.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		} catch (ValidationException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (TerminologyException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		}

	}

	/**
	 * The Class CaseTableCellRenderer.
	 */
	static class CaseTableCellRenderer extends DefaultTableCellRenderer {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -4117756700808758059L;

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			CaseTableModel model = (CaseTableModel) table.getModel();
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (!isSelected) {
				Color color = model.getRowColor(row);
				c.setBackground(color);
			}
			return c;
		}
	}

	/**
	 * The Class CheckBoxHeader.
	 */
	class CheckBoxHeader extends JCheckBox implements TableCellRenderer, MouseListener {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -5834284323210153347L;
		
		/** The renderer component. */
		protected CheckBoxHeader rendererComponent;
		
		/** The column. */
		protected int column;
		
		/** The mouse pressed. */
		protected boolean mousePressed = false;

		/**
		 * Instantiates a new check box header.
		 *
		 * @param itemListener the item listener
		 */
		public CheckBoxHeader(ItemListener itemListener) {
			rendererComponent = this;
			rendererComponent.addItemListener(itemListener);
		}

		/**
		 * Gets the comp.
		 *
		 * @return the comp
		 */
		public CheckBoxHeader getComp() {
			return this.rendererComponent;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (table != null) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					rendererComponent.setForeground(header.getForeground());
					rendererComponent.setBackground(header.getBackground());
					rendererComponent.setFont(header.getFont());
					header.addMouseListener(rendererComponent);
				}
			}
			setColumn(column);
			rendererComponent.setText("Check All");
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			return rendererComponent;
		}

		/**
		 * Sets the column.
		 *
		 * @param column the new column
		 */
		protected void setColumn(int column) {
			this.column = column;
		}

		/**
		 * Gets the column.
		 *
		 * @return the column
		 */
		public int getColumn() {
			return column;
		}

		/**
		 * Handle click event.
		 *
		 * @param e the e
		 */
		protected void handleClickEvent(MouseEvent e) {
			if (mousePressed) {
				mousePressed = false;
				JTableHeader header = (JTableHeader) (e.getSource());
				JTable tableView = header.getTable();
				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				int column = tableView.convertColumnIndexToModel(viewColumn);

				if (viewColumn == this.column && e.getClickCount() == 1 && column != -1) {
					doClick();
				}
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			handleClickEvent(e);
			((JTableHeader) e.getSource()).repaint();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			mousePressed = true;
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}
	}

	/**
	 * The listener interface for receiving myItem events.
	 * The class that is interested in processing a myItem
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addMyItemListener<code> method. When
	 * the myItem event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see MyItemEvent
	 */
	class MyItemListener implements ItemListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
		 */
		public void itemStateChanged(ItemEvent e) {
			Object source = e.getSource();
			if (source instanceof AbstractButton == false)
				return;
			boolean checked = e.getStateChange() == ItemEvent.SELECTED;
			batchAssigneTo.setEnabled(checked);
			batchAssigneTo.setEnabled(checked);
			bathcSaveButton.setEnabled(checked);
			for (int x = 0, y = caseTable.getRowCount(); x < y; x++) {
				caseTable.setValueAt(new Boolean(checked), x, 1);
			}
		}
	}

}
