package org.ihtsdo.project.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowDefinitionManager;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

/**
 * @author Guillermo Reynoso
 */
public class WorkflowDefinitionPanel extends JPanel {
	private HashMap<String, File> workflowsDefinitions;
	private HashMap<String, File> businessFiles;
	private File workflowDefinitionFile;
	private HashMap<String, WfAction> actions;
	private HashMap<String, File> xlsHash;
	private HashMap<String, File> drlHash;
	private WfAction currentAction;
	private int selectedAction;
	private boolean activeSelection;
	/** The states. */
	private HashMap<String, WfState> statesHash;
	private List<WfRole> selRoles;
	private List<WfState> selStates;
	private HashMap<String, WfRole> rolesHash;
	private ArrayList<String> drlFilesList;
	private ArrayList<String> xlsFilesList;

	public WorkflowDefinitionPanel() {
		workflowsDefinitions = new HashMap<String, File>();
		businessFiles = new HashMap<String, File>();
		rolesHash = new HashMap<String, WfRole>();
		statesHash = new HashMap<String, WfState>();
		drlFilesList = new ArrayList<String>();
		xlsFilesList = new ArrayList<String>();
		initComponents();
		initCustomComponents();
		initWfDefinitionCombo();
	}

	private void initWfDefinitionCombo() {
		File[] list = new File("sampleProcesses/").listFiles();
		List<String> bps = new ArrayList<String>();
		List<String> wfdefString = new ArrayList<String>();
		for (File file : list) {
			if (file.getName().endsWith(".wfd")) {
				String wfDefName = file.getName().replaceAll(".wfd", "");
				if (!wfDefName.trim().isEmpty()) {
					workflowsDefinitions.put(wfDefName, file);
					wfdefString.add(wfDefName);
				}
			} else if (file.getName().endsWith(".bp")) {
				businessFiles.put(file.getName().replaceAll(".bp", ""), file);
				bps.add(file.getName().replaceAll(".bp", ""));
			}
		}
		Collections.sort(bps);
		for (String string : bps) {
			actionBpCmbo.addItem(string);
		}

		Collections.sort(wfdefString);
		wfDefs.removeAllItems();
		wfDefs.addItem("");
		for (String string : wfdefString) {
			wfDefs.addItem(string);
		}
	}

	private void initCustomComponents() {
		statesTable.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Included", "States" }) {
			Class<?>[] columnTypes = new Class<?>[] { Boolean.class, Object.class };
			boolean[] columnEditable = new boolean[] { true, false };

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnEditable[columnIndex];
			}
		});
		TableColumnModel cm = statesTable.getColumnModel();
		cm.getColumn(0).setResizable(false);
		cm.getColumn(0).setMinWidth(55);
		cm.getColumn(0).setMaxWidth(55);
		cm.getColumn(0).setPreferredWidth(55);

		statesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		statesTable.setRowSelectionAllowed(false);

		rolesTable.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Included", "Role" }) {
			Class<?>[] columnTypes = new Class<?>[] { Boolean.class, Object.class };
			boolean[] columnEditable = new boolean[] { true, false };

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnEditable[columnIndex];
			}
		});
		TableColumnModel rcm = rolesTable.getColumnModel();
		rcm.getColumn(0).setResizable(false);
		rcm.getColumn(0).setMinWidth(55);
		rcm.getColumn(0).setMaxWidth(55);
		rcm.getColumn(0).setPreferredWidth(55);

		rolesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rolesTable.setRowSelectionAllowed(false);
		rolesTable.setMinimumSize(new Dimension(0, 0));

		WfComponentProvider wp = new WfComponentProvider();
		List<WfRole> rolesList = wp.getRoles();
		Collections.sort(rolesList);
		DefaultTableModel model = (DefaultTableModel) rolesTable.getModel();
		for (WfRole role : rolesList) {
			model.addRow(new Object[] { new Boolean(false), role.getName() });
			rolesHash.put(role.getName(), role);
		}

		List<WfState> states = wp.getAllStates();
		Collections.sort(states);
		DefaultTableModel statusModel = (DefaultTableModel) statesTable.getModel();
		for (WfState state : states) {
			statusModel.addRow(new Object[] { new Boolean(false), state.getName() });
			statesHash.put(state.getName(), state);
		}

		DefaultListModel<String> drlModel = new DefaultListModel<String>();
		DefaultListModel<String> xlsModel = new DefaultListModel<String>();
		drlFiles.setModel(drlModel);
		xlsFiles.setModel(xlsModel);
		drlHash = new HashMap<String, File>();
		xlsHash = new HashMap<String, File>();
		File[] drlList = new File("drools-rules/").listFiles();
		drlModel.addElement("No selected files");
		xlsModel.addElement("No selected files");
		List<String> xlsFileList = new ArrayList<String>();
		List<String> drlFileList = new ArrayList<String>();
		for (File file : drlList) {
			if (file.getName().endsWith(".drl")) {
				String fileName = file.getName().replaceAll(".drl", "");
				drlHash.put(fileName, file);
				drlFileList.add(fileName);
			} else if (file.getName().endsWith(".xls")) {
				String fileName = file.getName().replaceAll(".xls", "");
				xlsHash.put(fileName, file);
				xlsFileList.add(fileName);
			}
		}

		Collections.sort(drlFileList);
		for (String string : drlFileList) {
			drlModel.addElement(string);
		}
		Collections.sort(xlsFileList);
		for (String string : xlsFileList) {
			xlsModel.addElement(string);
		}

		drlFiles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		xlsFiles.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		drlFiles.setSelectedIndex(0);
		xlsFiles.setSelectedIndex(0);

	}

	protected void wfDefsItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && workflowsDefinitions != null) {
			File file = workflowsDefinitions.get(wfDefs.getSelectedItem());
			if (workflowDefinitionFile != null && workflowDefinitionFile.equals(file)) {
				return;
			}
			if (file == null) {
				return;
			}
			workflowDefinitionFile = file;
			loadFile(workflowDefinitionFile);
		}
	}

	private void loadFile(File workflowDefinitionFile2) {
		System.out.println("LOAD: " + workflowDefinitionFile2.getName());
		WorkflowDefinition wd = WorkflowDefinitionManager.readWfDefinition(workflowDefinitionFile2.getName());
		actionsList.setModel(new DefaultListModel());

		actions = null;
		selRoles = new ArrayList<WfRole>();
		selStates = new ArrayList<WfState>();

		List<WfRole> roles = wd.getRoles();
		for (int i = 0; i < rolesTable.getRowCount(); i++) {
			rolesTable.setValueAt(false, i, 0);
			for (WfRole wfRole : roles) {
				if (rolesTable.getValueAt(i, 1).equals(wfRole.getName())) {
					rolesTable.setValueAt(true, i, 0);
					selRoles.add(wfRole);
					break;
				}
			}
		}
		List<WfState> states = wd.getStates();
		for (int i = 0; i < statesTable.getRowCount(); i++) {
			statesTable.setValueAt(false, i, 0);
			for (WfState wfState : states) {
				if (statesTable.getValueAt(i, 1).equals(wfState.getName())) {
					statesTable.setValueAt(true, i, 0);
					selStates.add(wfState);
				}
			}
		}

		actions = (HashMap<String, WfAction>) wd.getActions();
		actionsListPopulate();

		xlsFiles.clearSelection();
		List<String> excelFiles = wd.getXlsFileName();
		ListModel<String> xlsModel = xlsFiles.getModel();
		List<Integer> vector = new ArrayList<Integer>();
		for (String excelFile : excelFiles) {
			String splitRegex = Pattern.quote(System.getProperty("file.separator"));
			String[] split = excelFile.split(splitRegex);
			String key = split[split.length - 1];
			String replacedKey = key.replaceAll(".xls", "");
			if (xlsHash.containsKey(replacedKey)) {
				for (int j = 0; j < xlsModel.getSize(); j++) {
					File file = xlsHash.get(replacedKey);
					String string = file.getName().replaceAll(".xls", "");
					if (xlsModel.getElementAt(j).toString().equals(string)) {
						vector.add(j);
						break;
					}
				}
			}
		}
		int[] indices = new int[vector.size()];
		int i = 0;
		for (Integer j : vector) {
			indices[i] = j;
			i++;
		}
		xlsFiles.setSelectedIndices(indices);
		if (excelFiles.size() == 0) {
			xlsFiles.setSelectedIndex(0);
		}

		drlFiles.clearSelection();
		List<String> rulesFiles = wd.getDrlFileName();
		ListModel<String> drlModel = drlFiles.getModel();
		List<Integer> vectordrl = new ArrayList<Integer>();
		for (String drlFile : rulesFiles) {
			String splitRegex = Pattern.quote(System.getProperty("file.separator"));
			String[] split = drlFile.split(splitRegex);
			String key = split[split.length - 1];
			String replacedKey = key.replaceAll(".drl", "");
			if (drlHash.containsKey(replacedKey)) {
				for (int j = 0; j < drlModel.getSize(); j++) {
					File file = drlHash.get(replacedKey);
					String string = file.getName().replaceAll(".drl", "");
					if (drlModel.getElementAt(j).toString().equals(string)) {
						vectordrl.add(j);
						break;
					}
				}
			}
		}
		int[] indicesDrl = new int[vectordrl.size()];
		int idrl = 0;
		for (Integer j : vectordrl) {
			indicesDrl[idrl] = j;
			idrl++;
		}
		drlFiles.setSelectedIndices(indicesDrl);
		if (rulesFiles.size() == 0) {
			drlFiles.setSelectedIndex(0);
		}

		actionConsequenceCmbo.setSelectedIndex(-1);
		activeSelection = false;
		selectedAction = -1;

		actionBpCmbo.setSelectedIndex(-1);

		revertToInitialState();
	}

	private void revertToInitialState() {
		actionNameField.setText("");
		actionUuidField.setText("");
		newActionButton.setText("New Action");
		editActionButton.setText("Edit");
		removeButton.setText("Remove");
		actionUuidField.setEnabled(false);
		actionNameField.setEnabled(false);
		actionBpCmbo.setEditable(false);
		actionConsequenceCmbo.setEnabled(false);
		revertButton.setEnabled(true);
		removeButton.setEnabled(true);
		saveButton.setEnabled(true);
		newActionButton.setEnabled(true);
		editActionButton.setEnabled(true);
	}

	private void addWfActionPreformed(ActionEvent e) {
		String name = JOptionPane.showInputDialog("Enter new Workflow Definition's name:");
		if (name == null || name.length() == 0) {
			JOptionPane.showMessageDialog(this, "Error in name, Workflow Definition not created.");
			return;
		}
		File file = new File("sampleProcesses/" + name.concat(".wfd"));
		WorkflowDefinition wfDefinition = new WorkflowDefinition(new ArrayList<WfRole>(), new ArrayList<WfState>(), new HashMap<String, WfAction>());
		wfDefinition.setName(name);
		WorkflowDefinitionManager.writeWfDefinition(wfDefinition);
		workflowsDefinitions.put(name, file);
		wfDefs.addItem(name);
		wfDefs.setSelectedItem(name);
	}

	protected void editButtonActionPerformed(ActionEvent e) {
		if (editActionButton.getText().equals("Edit")) {
			editActionButton.setText("Save");
			WfAction a = actions.get(actionsList.getSelectedValue().getName());
			actionUuidField.setText(a.getId().toString());
			actionNameField.setText(a.getName());
			if (a.getBusinessProcess() != null) {
				actionBpCmbo.setSelectedItem(a.getBusinessProcess().getName().replaceAll(".bp", ""));
			}
			if (a.getConsequence() != null) {
				actionConsequenceCmbo.setSelectedItem(a.getConsequence().getName());
			}

			newActionButton.setEnabled(false);
			editActionButton.setEnabled(true);
			removeButton.setEnabled(false);
			actionNameField.setEnabled(true);
			actionBpCmbo.setEnabled(true);
			actionConsequenceCmbo.setEnabled(true);
		} else {
			currentAction = new WfAction();
			currentAction.setId(UUID.fromString(actionUuidField.getText()));
			currentAction.setName(actionNameField.getText());
			File aux = businessFiles.get(actionBpCmbo.getSelectedItem().toString());
			if (aux.isFile()) {
				currentAction.setBusinessProcess(aux);
			}
			currentAction.setConsequence((WfState) statesHash.get(actionConsequenceCmbo.getSelectedItem()));
			actions.put(currentAction.getName(), currentAction);

			actionUuidField.setText("");
			actionNameField.setText("");
			actionBpCmbo.setSelectedIndex(-1);
			actionConsequenceCmbo.setSelectedIndex(-1);

			newActionButton.setEnabled(true);
			editActionButton.setEnabled(true);
			removeButton.setEnabled(true);
			actionNameField.setEnabled(false);
			actionBpCmbo.setEnabled(false);
			actionConsequenceCmbo.setEnabled(false);

			editActionButton.setText("Edit");
			saveButton.setEnabled(true);
			revertButton.setEnabled(true);
		}
	}

	private void actionsListPopulate() {
		DefaultListModel<WfAction> lmodel = new DefaultListModel<WfAction>();
		if (actions != null && actions.size() > 0) {
			Set<String> keyset = actions.keySet();
			List<String> keys = new ArrayList<String>();
			keys = Arrays.asList(keyset.toArray(new String[0]));
			Collections.sort(keys);
			for (String act : keys) {
				lmodel.addElement(actions.get(act));
			}
		}
		actionsList.setModel(lmodel);
		actionsList.updateUI();
	}

	private void newActionActionPreformed(ActionEvent e) {
		if (removeButton.getText().equals("Remove")) {
			newActionButton.setText("Save");
			removeButton.setText("Cancel");
			currentAction = new WfAction();
			UUID id = UUID.randomUUID();
			currentAction.setId(id);
			actionUuidField.setText(id.toString());
			actionNameField.setText("");
			actionBpCmbo.setSelectedIndex(-1);
			actionConsequenceCmbo.setSelectedIndex(-1);
			selectedAction = -1;
			activeSelection = false;
			actionConsequenceCmbo.removeAllItems();
			List<String> consequenceItems = new ArrayList<String>();
			for (int i = 0; i < statesTable.getRowCount(); i++) {
				if (((Boolean) statesTable.getValueAt(i, 0)) == true) {
					consequenceItems.add((String) statesTable.getValueAt(i, 1));
				}
			}
			Collections.sort(consequenceItems);
			for (String string : consequenceItems) {
				actionConsequenceCmbo.addItem(string);
			}

			newActionButton.setEnabled(true);
			editActionButton.setEnabled(false);
			removeButton.setEnabled(true);
			actionNameField.setEnabled(true);
			actionBpCmbo.setEnabled(true);
			actionConsequenceCmbo.setEnabled(true);
		} else {
			if (actionNameField.getText() != null && actionNameField.getText().length() > 0) {
				currentAction.setName(actionNameField.getText());
				File aux = businessFiles.get(actionBpCmbo.getSelectedItem().toString());
				if (aux.isFile()) {
					currentAction.setBusinessProcess(aux);
				}
				currentAction.setConsequence((WfState) statesHash.get(actionConsequenceCmbo.getSelectedItem()));
				if (actions.containsKey(currentAction.getName())) {
					JOptionPane.showMessageDialog(this, "An action with the same name already exist.");
					return;
				} else {
					actions.put(currentAction.getName(), currentAction);
					actionsListPopulate();
				}

				newActionButton.setText("New Action");
				removeButton.setText("Remove");
				actionUuidField.setText("");
				actionNameField.setText("");
				actionBpCmbo.setSelectedIndex(-1);
				actionConsequenceCmbo.setSelectedIndex(-1);

				newActionButton.setEnabled(true);
				editActionButton.setEnabled(false);
				removeButton.setEnabled(false);
				actionNameField.setEnabled(false);
				actionBpCmbo.setEnabled(false);
				actionConsequenceCmbo.setEnabled(false);

				saveButton.setText("Save");
				saveButton.setEnabled(true);
				revertButton.setEnabled(true);
			} else {
				JOptionPane.showMessageDialog(this, "You must write a name for the current Action.");
			}
		}

	}

	protected void removeButtonActionPerformed() {
		if (removeButton.getText().equals("Remove")) {
			int i = actionsList.getSelectedIndex();
			if (i > -1) {
				actions.remove(actionsList.getSelectedValue().getName());
				actionsList.setSelectedIndex(-1);
				actionUuidField.setText("");
				actionNameField.setText("");
				actionBpCmbo.setSelectedIndex(-1);
				actionConsequenceCmbo.setSelectedIndex(-1);

				actionsListPopulate();

				activeSelection = false;
				selectedAction = -1;

				saveButton.setText("Save");
				saveButton.setEnabled(true);
				revertButton.setEnabled(true);
			}
		} else { // CANCEL
			if (!activeSelection) { // NEW
				newActionButton.setText("New Action");
				removeButton.setText("Remove");
				actionUuidField.setText("");
				actionNameField.setText("");
				actionBpCmbo.setSelectedIndex(-1);
				actionConsequenceCmbo.setSelectedIndex(-1);

				newActionButton.setEnabled(true);
				editActionButton.setEnabled(false);
				removeButton.setEnabled(false);
				actionNameField.setEnabled(false);
				actionBpCmbo.setEnabled(false);
				actionConsequenceCmbo.setEnabled(false);
			} else { // EDIT
				editActionButton.setText("Edit");
				removeButton.setText("Remove");
				WfAction a = actions.get(actionsList.getSelectedValue().getName());
				actionUuidField.setText(a.getId().toString());
				actionNameField.setText(a.getName());
				if (a.getBusinessProcess() != null) {
					actionBpCmbo.setSelectedItem(a.getBusinessProcess().getName().replaceAll(".bp", ""));
				}
				if (a.getConsequence() != null) {
					actionConsequenceCmbo.setSelectedItem(a.getConsequence().getName());
				}

				newActionButton.setEnabled(true);
				editActionButton.setEnabled(true);
				removeButton.setEnabled(true);
				actionNameField.setEnabled(false);
				actionBpCmbo.setEnabled(false);
				actionConsequenceCmbo.setEnabled(false);
			}
		}
	}

	protected void actionsListValueChanged(ListSelectionEvent e) {
		if (actionsList.getSelectedIndex() == -1) {
			return;
		}
		if (newActionButton.getText().equals("New Action") && newActionButton.isEnabled()) {
			activeSelection = true;
			selectedAction = actionsList.getSelectedIndex();
			WfAction a = actions.get(actionsList.getSelectedValue().getName());
			actionUuidField.setText(a.getId().toString());
			actionNameField.setText(a.getName());
			actionBpCmbo.setSelectedItem(-1);
			if (a.getBusinessProcess() != null) {
				actionBpCmbo.setSelectedItem(a.getBusinessProcess().getName().replaceAll(".bp", ""));
			}
			actionConsequenceCmbo.removeAllItems();
			List<String> consequenceItems = new ArrayList<String>();
			for (int i = 0; i < statesTable.getRowCount(); i++) {
				if (((Boolean) statesTable.getValueAt(i, 0)) == true) {
					consequenceItems.add((String) statesTable.getValueAt(i, 1));
				}
			}
			Collections.sort(consequenceItems);
			for (String string : consequenceItems) {
				actionConsequenceCmbo.addItem(string);
			}
			if (a.getConsequence() != null) {
				actionConsequenceCmbo.setSelectedItem(a.getConsequence().getName());
			}
			editActionButton.setEnabled(true);
			removeButton.setEnabled(true);
		} else {
			if (activeSelection) {
				actionsList.setSelectedIndex(selectedAction);
			} else {
				actionsList.setSelectedIndex(-1);
			}
		}

	}

	protected void saveButtonActionPerformed(ActionEvent e) {
		saveFile();
	}

	protected void closeButtonActionPerformed(ActionEvent e) {
		this.getParent().remove(this);
		this.invalidate();
	}

	private boolean saveFile() {
		if (removeButton.getText().equals("Cancel")) {
			JOptionPane.showMessageDialog(this, "Before save, please finish Action edition or creation.");
			return false;
		}
		if (wfDefs.getSelectedItem() != null) {
			selRoles = new ArrayList<WfRole>();
			selStates = new ArrayList<WfState>();
			for (int i = 0; i < rolesTable.getRowCount(); i++) {
				if (((Boolean) rolesTable.getValueAt(i, 0)) == true) {
					if (rolesHash.get((String) rolesTable.getValueAt(i, 1)) != null) {
						selRoles.add(rolesHash.get((String) rolesTable.getValueAt(i, 1)));
					} else {
						System.out.println("Warning: roles not present");
					}
				}
			}
			List<String> consequenceItems = new ArrayList<String>();
			for (int i = 0; i < statesTable.getRowCount(); i++) {
				if (((Boolean) statesTable.getValueAt(i, 0)) == true) {
					consequenceItems.add((String) statesTable.getValueAt(i, 1));
					selStates.add(statesHash.get((String) statesTable.getValueAt(i, 1)));
				}
			}
			Collections.sort(consequenceItems);
			for (String string : consequenceItems) {
				actionConsequenceCmbo.addItem(string);
			}

			drlFilesList = new ArrayList<String>();
			ListModel<String> drlFilesModel = drlFiles.getModel();
			List<String> selectedFiles = drlFiles.getSelectedValuesList();
			for (String string : selectedFiles) {
				File drlFile = drlHash.get(string);
				if (drlFile != null) {
					drlFilesList.add(drlFile.toString());
				}
			}
			xlsFilesList = new ArrayList<String>();
			List<String> xlsSelectedFiles = xlsFiles.getSelectedValuesList();
			for (String string : xlsSelectedFiles) {
				File xlsFile = xlsHash.get(string);
				if (xlsFile != null) {
					xlsFilesList.add(xlsFile.toString());
				}
			}
			File f = new File("file.file");
			if (f.exists()) {
				f.delete();
			}

			WorkflowDefinition workflowDefinition = new WorkflowDefinition(selRoles, selStates, actions);
			workflowDefinition.setName(wfDefs.getSelectedItem().toString());
			workflowDefinition.setDrlFileName(drlFilesList);
			workflowDefinition.setXlsFileName(xlsFilesList);
			WorkflowDefinitionManager.writeWfDefinition(workflowDefinition);
			JOptionPane.showMessageDialog(this, "\"" + wfDefs.getSelectedItem().toString() + "\" saved.");
			workflowDefinitionFile = new File("sampleProcesses/" + workflowDefinition.getName() + ".wfd");
			return true;
		} else {
			JOptionPane.showMessageDialog(this, "Please write a name for the Workflow definition");
			return false;
		}
	}

	public static void main(String[] args) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		System.out.println(classLoader.getResource(".").getPath());

	}

	protected void revertButtonActionPerformed(ActionEvent e) {
		if (workflowDefinitionFile == null) {
			actions = new HashMap<String, WfAction>();
			actionsList.setModel(new DefaultListModel());
			actionUuidField.setText("");
			actionNameField.setText("");
			actionConsequenceCmbo.setSelectedIndex(-1);
			actionConsequenceCmbo.removeAllItems();
			actionBpCmbo.setSelectedIndex(-1);
			DefaultTableModel model = (DefaultTableModel) rolesTable.getModel();
			for (int i = 0; i < model.getRowCount(); i++) {
				model.setValueAt(false, i, 0);
			}
			rolesTable.setModel(model);
			DefaultTableModel statesModel = (DefaultTableModel) statesTable.getModel();
			for (int i = 0; i < statesModel.getRowCount(); i++) {
				statesModel.setValueAt(false, i, 0);
			}
			statesTable.setModel(statesModel);
		} else {
			loadFile(workflowDefinitionFile);
		}
		revertToInitialState();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		panel2 = new JPanel();
		saveButton = new JButton();
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveButtonActionPerformed(e);
			}
		});
		revertButton = new JButton();
		revertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				revertButtonActionPerformed(e);
			}
		});
		panel3 = new JPanel();
		separator1 = new JSeparator();
		panel5 = new JPanel();
		label6 = new JLabel();
		scrollPane4 = new JScrollPane();
		xlsFiles = new JList<String>();
		label7 = new JLabel();
		scrollPane5 = new JScrollPane();
		drlFiles = new JList<String>();
		separator2 = new JSeparator();

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());
		GridBagLayout gbl_panel1 = new GridBagLayout();
		gbl_panel1.columnWidths = new int[] { 133, 153, 75, 0, 79, 0 };
		gbl_panel1.rowHeights = new int[] { 29, 0 };
		gbl_panel1.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel1.setLayout(gbl_panel1);
		addWfDef = new JButton();
		addWfDef.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addWfActionPreformed(e);
			}
		});
		label1 = new JLabel();

		// ---- label1 ----
		label1.setText("Workflow Definitions");
		GridBagConstraints gbc_label1 = new GridBagConstraints();
		gbc_label1.anchor = GridBagConstraints.WEST;
		gbc_label1.insets = new Insets(0, 0, 0, 5);
		gbc_label1.gridx = 0;
		gbc_label1.gridy = 0;
		panel1.add(label1, gbc_label1);
		wfDefs = new JComboBox();
		wfDefs.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				wfDefsItemStateChanged(e);
			}
		});

		// ======== panel1 ========
		{
			GridBagConstraints gbc_wfDefs = new GridBagConstraints();
			gbc_wfDefs.fill = GridBagConstraints.HORIZONTAL;
			gbc_wfDefs.insets = new Insets(0, 0, 0, 5);
			gbc_wfDefs.gridx = 1;
			gbc_wfDefs.gridy = 0;
			panel1.add(wfDefs, gbc_wfDefs);
		}

		// ---- addWfDef ----
		addWfDef.setText("Add");
		GridBagConstraints gbc_addWfDef = new GridBagConstraints();
		gbc_addWfDef.anchor = GridBagConstraints.WEST;
		gbc_addWfDef.insets = new Insets(0, 0, 0, 5);
		gbc_addWfDef.gridx = 2;
		gbc_addWfDef.gridy = 0;
		panel1.add(addWfDef, gbc_addWfDef);
		add(panel1, BorderLayout.NORTH);

		// ======== panel2 ========
		{
			panel2.setLayout(new FlowLayout(FlowLayout.RIGHT, 2, 2));

			// ---- saveButton ----
			saveButton.setText("Save");
			panel2.add(saveButton);

			// ---- revertButton ----
			revertButton.setText("Revert");
			panel2.add(revertButton);
		}
		add(panel2, BorderLayout.SOUTH);

		btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeButtonActionPerformed(e);
			}
		});
		panel2.add(btnClose);
		btnClose.setHorizontalAlignment(SwingConstants.RIGHT);

		// ======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout) panel3.getLayout()).columnWidths = new int[] { 0, 0 };
			((GridBagLayout) panel3.getLayout()).rowHeights = new int[] { 16, 0, 0, 9, 0 };
			((GridBagLayout) panel3.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
			((GridBagLayout) panel3.getLayout()).rowWeights = new double[] { 0.0, 1.0, 1.0, 0.0, 1.0E-4 };
			panel3.add(separator1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
					5, 0), 0, 0));

			splitPane = new JSplitPane();
			splitPane.setResizeWeight(0.5);
			GridBagConstraints gbc_splitPane = new GridBagConstraints();
			gbc_splitPane.insets = new Insets(0, 0, 5, 0);
			gbc_splitPane.fill = GridBagConstraints.BOTH;
			gbc_splitPane.gridx = 0;
			gbc_splitPane.gridy = 1;
			panel3.add(splitPane, gbc_splitPane);

			panel = new JPanel();
			splitPane.setLeftComponent(panel);
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[] { 0, 0, 0 };
			gbl_panel.rowHeights = new int[] { 0, 0, 0 };
			gbl_panel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
			gbl_panel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			panel.setLayout(gbl_panel);
			panel.setBorder(new EmptyBorder(5, 5, 5, 5));

			lblRoles = new JLabel("Roles");
			GridBagConstraints gbc_lblRoles = new GridBagConstraints();
			gbc_lblRoles.anchor = GridBagConstraints.WEST;
			gbc_lblRoles.insets = new Insets(0, 0, 5, 5);
			gbc_lblRoles.gridx = 0;
			gbc_lblRoles.gridy = 0;
			panel.add(lblRoles, gbc_lblRoles);

			lblStates = new JLabel("States");
			GridBagConstraints gbc_lblStates = new GridBagConstraints();
			gbc_lblStates.anchor = GridBagConstraints.WEST;
			gbc_lblStates.insets = new Insets(0, 0, 5, 0);
			gbc_lblStates.gridx = 1;
			gbc_lblStates.gridy = 0;
			panel.add(lblStates, gbc_lblStates);
			scrollPane1 = new JScrollPane();
			GridBagConstraints gbc_scrollPane1 = new GridBagConstraints();
			gbc_scrollPane1.fill = GridBagConstraints.BOTH;
			gbc_scrollPane1.insets = new Insets(0, 0, 0, 5);
			gbc_scrollPane1.gridx = 0;
			gbc_scrollPane1.gridy = 1;
			panel.add(scrollPane1, gbc_scrollPane1);
			rolesTable = new JTable();

			// ======== scrollPane1 ========
			{

				// ---- rolesTable ----
				rolesTable.setMaximumSize(new Dimension(2147483647, 32900));
				rolesTable.setModel(new DefaultTableModel(new Object[][] { { "asfadsfa", "sdfadsf" }, { "sdfasdfasd", "sdf" }, }, new String[] {
						null, null }));
				scrollPane1.setViewportView(rolesTable);
			}
			scrollPane2 = new JScrollPane();
			GridBagConstraints gbc_scrollPane2 = new GridBagConstraints();
			gbc_scrollPane2.fill = GridBagConstraints.BOTH;
			gbc_scrollPane2.gridx = 1;
			gbc_scrollPane2.gridy = 1;
			panel.add(scrollPane2, gbc_scrollPane2);
			statesTable = new JTable();

			// ======== scrollPane2 ========
			{

				// ---- statusTable ----
				statesTable.setMaximumSize(new Dimension(2147483647, 328888));
				statesTable.setModel(new DefaultTableModel(new Object[][] { { "sdfadsf", "sdfasdfads" }, { "dfadsfadsf", "sdfasdfasfadsf" }, },
						new String[] { null, null }));
				scrollPane2.setViewportView(statesTable);
			}
			panel4 = new JPanel();
			splitPane.setRightComponent(panel4);
			scrollPane3 = new JScrollPane();
			actionsList = new JList<WfAction>();
			actionsList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					actionsListValueChanged(e);
				}
			});
			newActionButton = new JButton();
			newActionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					newActionActionPreformed(e);
				}
			});
			editActionButton = new JButton();
			editActionButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					editButtonActionPerformed(e);
				}
			});
			removeButton = new JButton();
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeButtonActionPerformed();
				}
			});
			label2 = new JLabel();
			actionUuidField = new JTextField();
			actionUuidField.setEnabled(false);
			label3 = new JLabel();
			actionNameField = new JTextField();
			actionNameField.setEnabled(false);
			label4 = new JLabel();
			actionConsequenceCmbo = new JComboBox<String>();
			actionConsequenceCmbo.setEnabled(false);
			label5 = new JLabel();
			actionBpCmbo = new JComboBox();
			actionBpCmbo.setEnabled(false);

			// ======== panel4 ========
			{
				panel4.setLayout(new GridBagLayout());
				((GridBagLayout) panel4.getLayout()).columnWidths = new int[] { 0, 0, 0, 0 };
				((GridBagLayout) panel4.getLayout()).rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
				((GridBagLayout) panel4.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0E-4 };
				((GridBagLayout) panel4.getLayout()).rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };

				// ======== scrollPane3 ========
				{
					scrollPane3.setViewportView(actionsList);
				}
				panel4.add(scrollPane3, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 5, 0), 0, 0));

				// ---- newActionButton ----
				newActionButton.setText("New Action");
				panel4.add(newActionButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

				// ---- editActionButton ----
				editActionButton.setText("Edit");
				panel4.add(editActionButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

				// ---- removeButton ----
				removeButton.setText("Remove");
				panel4.add(removeButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 5, 0), 0, 0));

				// ---- label2 ----
				label2.setText("ID");
				panel4.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
						5, 5), 0, 0));
				panel4.add(actionUuidField, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

				// ---- label3 ----
				label3.setText("Name");
				panel4.add(label3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
						5, 5), 0, 0));
				panel4.add(actionNameField, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

				// ---- label4 ----
				label4.setText("Consequence");
				panel4.add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
						5, 5), 0, 0));
				panel4.add(actionConsequenceCmbo, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

				// ---- label5 ----
				label5.setText("Business Process");
				panel4.add(label5, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
						0, 5), 0, 0));
				panel4.add(actionBpCmbo, new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
			}

			// ======== panel5 ========
			{
				panel5.setLayout(new GridBagLayout());
				((GridBagLayout) panel5.getLayout()).columnWidths = new int[] { 0, 0, 0 };
				((GridBagLayout) panel5.getLayout()).rowHeights = new int[] { 0, 0, 0, 0, 0 };
				((GridBagLayout) panel5.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
				((GridBagLayout) panel5.getLayout()).rowWeights = new double[] { 0.0, 1.0, 0.0, 1.0, 1.0E-4 };

				// ---- label6 ----
				label6.setText("XLS File");
				panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
						5, 5), 0, 0));

				// ======== scrollPane4 ========
				{
					scrollPane4.setViewportView(xlsFiles);
				}
				panel5.add(scrollPane4, new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 5, 0), 0, 0));

				// ---- label7 ----
				label7.setText("DRL File");
				panel5.add(label7, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
						5, 5), 0, 0));

				// ======== scrollPane5 ========
				{
					scrollPane5.setViewportView(drlFiles);
				}
				panel5.add(scrollPane5, new GridBagConstraints(1, 2, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
			}
			panel3.add(panel5, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));
			panel3.add(separator2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
					0, 0), 0, 0));
		}
		add(panel3, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JComboBox wfDefs;
	private JButton addWfDef;
	private JPanel panel2;
	private JButton saveButton;
	private JButton revertButton;
	private JPanel panel3;
	private JSeparator separator1;
	private JScrollPane scrollPane1;
	private JTable rolesTable;
	private JScrollPane scrollPane2;
	private JTable statesTable;
	private JPanel panel4;
	private JScrollPane scrollPane3;
	private JList<WfAction> actionsList;
	private JButton newActionButton;
	private JButton editActionButton;
	private JButton removeButton;
	private JLabel label2;
	private JTextField actionUuidField;
	private JLabel label3;
	private JTextField actionNameField;
	private JLabel label4;
	private JComboBox<String> actionConsequenceCmbo;
	private JLabel label5;
	private JComboBox actionBpCmbo;
	private JPanel panel5;
	private JLabel label6;
	private JScrollPane scrollPane4;
	private JList<String> xlsFiles;
	private JLabel label7;
	private JScrollPane scrollPane5;
	private JList<String> drlFiles;
	private JSeparator separator2;
	private JSplitPane splitPane;
	private JPanel panel;
	private JLabel lblRoles;
	private JLabel lblStates;
	private JButton btnClose;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
