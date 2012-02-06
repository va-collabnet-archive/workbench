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

package org.ihtsdo.project.panel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowDefinitionManager;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

/**
 * The Class WorkflowDefinitionPanel.
 *
 * @author Cesar Zamorano
 */
@SuppressWarnings("serial")
public class WorkflowDefinitionPanel extends JPanel {
	
	/** The current action. */
	private WfAction currentAction;
	
	/** The actions. */
	private HashMap<String,WfAction> actions;
	
	/** The drl files. */
	private List<String> drlFiles;
	
	/** The xls files. */
	private List<String> xlsFiles;
	
	/** The sel roles. */
	private List<WfRole> selRoles;
	
	/** The sel states. */
	private List<WfState> selStates;
	
	/** The roles. */
	private HashMap<String,WfRole> roles;
	
	/** The states. */
	private HashMap<String,WfState> states;
	
	/** The workflow definition file. */
	private File workflowDefinitionFile;
	
	/** The selected action. */
	private int selectedAction=-1;
	
	/** The active selection. */
	private boolean activeSelection= false;
	
	/** The action name. */
	private String actionName;
	
	/**
	 * Instantiates a new workflow definition panel.
	 */
	public WorkflowDefinitionPanel() {
		initComponents();
		WfComponentProvider wp= new WfComponentProvider();
		List<WfRole> rolesList = wp.getRoles();
		List<WfState> statesList = wp.getStates();
		actions= new  HashMap<String, WfAction>();
		roles= new HashMap<String, WfRole>();
		states= new HashMap<String, WfState>();
		drlFiles= new ArrayList<String>();
		xlsFiles= new ArrayList<String>();
		
		DefaultTableModel model= (DefaultTableModel) rolesTable.getModel();
		for (WfRole role : rolesList) {
			model.addRow(new Object[]{new Boolean(false),role.getName()});
			roles.put(role.getName(), role);
		}
		rolesTable.setModel(model);
		rolesTable.getTableHeader().setReorderingAllowed(false);
		model= (DefaultTableModel) statesTable.getModel();
		for (WfState state : statesList) {
			model.addRow(new Object[]{new Boolean(false),state.getName()});
			states.put(state.getName(), state);
		}
		statesTable.setModel(model);
		statesTable.getTableHeader().setReorderingAllowed(false);

		consequenceComboBox.setSelectedIndex(-1);
		
		newButton.setEnabled(true);
		editButton.setEnabled(false);
		removeButton.setEnabled(false);
		nameTextField.setEnabled(false);
		businessTextField.setEnabled(false);
		consequenceComboBox.setEnabled(true);
	}

	/**
	 * New button action performed.
	 *
	 * @param e the e
	 */
	private void newButtonActionPerformed(ActionEvent e) {
		if(removeButton.getText().equals("Remove")){
			newButton.setText("Save");
			removeButton.setText("Cancel");
			currentAction= new WfAction();
			UUID id= UUID.randomUUID();
			currentAction.setId(id);
			idTextField.setText(id.toString());
			nameTextField.setText("");
			businessTextField.setText("");
			consequenceComboBox.setSelectedIndex(-1);
			selectedAction=-1;
			activeSelection=false;
			consequenceComboBox.removeAllItems();
			for (int i = 0; i < statesTable.getRowCount(); i++) {
				if(((Boolean)statesTable.getValueAt(i, 0))==true)
					consequenceComboBox.addItem((String)statesTable.getValueAt(i, 1));
			}
			
			newButton.setEnabled(true);
			editButton.setEnabled(false);
			removeButton.setEnabled(true);
			nameTextField.setEnabled(true);
			businessTextField.setEnabled(true);
			consequenceComboBox.setEnabled(true);
			businessBrowseButton.setEnabled(true);
		}
		else{
			if(nameTextField.getText()!=null && nameTextField.getText().length()>0){
				currentAction.setName(nameTextField.getText());
				File aux= new File(businessTextField.getText());
				if(aux.isFile())
					currentAction.setBusinessProcess(aux);
				currentAction.setConsequence((WfState) states.get(consequenceComboBox.getSelectedItem()));
				if(actions.containsKey(currentAction.getName())){
					JOptionPane.showMessageDialog(this, "An action with the same name already exist.");
					return;
				}
				else{
					actions.put(currentAction.getName(), currentAction);
					DefaultListModel lmodel = new DefaultListModel();
					if(actions.size()>0)
						for (Object act : actions.keySet().toArray()) {
							lmodel.addElement(act.toString());
						}	
					actionList.setModel(lmodel);
					actionList.updateUI();
				}
				
				newButton.setText("New Action");
				removeButton.setText("Remove");
				idTextField.setText("");
				nameTextField.setText("");
				businessTextField.setText("");
				consequenceComboBox.setSelectedIndex(-1);
				
				newButton.setEnabled(true);
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
				nameTextField.setEnabled(false);
				businessTextField.setEnabled(false);
				consequenceComboBox.setEnabled(false);
				businessBrowseButton.setEnabled(false);
			}
			else
				JOptionPane.showMessageDialog(this, "You must write a name for the current Action.");
		}
	}

	/**
	 * Removes the button action performed.
	 *
	 * @param e the e
	 */
	private void removeButtonActionPerformed(ActionEvent e) {
		if(removeButton.getText().equals("Remove")){
			int i=actionList.getSelectedIndex();
			if(i>-1){
				actions.remove(actionList.getSelectedValue());
				actionList.setSelectedIndex(-1);
				idTextField.setText("");
				nameTextField.setText("");
				businessTextField.setText("");
				consequenceComboBox.setSelectedIndex(-1);
				DefaultListModel lmodel = new DefaultListModel();
				if(actions.size()>0)
					for (Object act : actions.keySet().toArray()) {
						lmodel.addElement(act.toString());
					}	
				actionList.setModel(lmodel);
				actionList.updateUI();
				activeSelection=false;
				selectedAction=-1;
			}
		}
		else{																						//CANCEL
			if(!activeSelection){																	//NEW
				newButton.setText("New Action");
				removeButton.setText("Remove");
				idTextField.setText("");
				nameTextField.setText("");
				businessTextField.setText("");
				consequenceComboBox.setSelectedIndex(-1);

				newButton.setEnabled(true);
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
				nameTextField.setEnabled(false);
				businessTextField.setEnabled(false);
				consequenceComboBox.setEnabled(false);
			}
			else{																					//EDIT
				editButton.setText("Edit");
				removeButton.setText("Remove");
				WfAction a= actions.get(actionList.getSelectedValue());
				idTextField.setText(a.getId().toString());
				nameTextField.setText(a.getName());
				if(a.getBusinessProcess()!=null)
					businessTextField.setText(a.getBusinessProcess().getAbsolutePath());
				if(a.getConsequence()!=null)
				consequenceComboBox.setSelectedItem(a.getConsequence().getName());
				
				newButton.setEnabled(true);
				editButton.setEnabled(true);
				removeButton.setEnabled(true);
				nameTextField.setEnabled(false);
				businessTextField.setEnabled(false);
				consequenceComboBox.setEnabled(false);
			}
		}
	}

	/**
	 * Xls browse button action performed.
	 *
	 * @param e the e
	 */
	private void xlsBrowseButtonActionPerformed(ActionEvent e) {
		File dir= new File(xlsTextField.getText().split(";")[0]);
		JFileChooser fc;
		if(dir.isFile())
			fc= new JFileChooser(dir.getParentFile());
		else
			fc= new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
			File[] xlsfiles = fc.getSelectedFiles();
			String files="";
			for (int i = 0; i < xlsfiles.length; i++) {
				xlsFiles.add(xlsfiles[i].getAbsolutePath());
				if(i==0) files=files.concat(xlsfiles[i].getAbsolutePath());
				else files=files.concat(";"+xlsfiles[i].getAbsolutePath());
			}
			xlsTextField.setText(files);
		}
	}

	/**
	 * Drl browse button action performed.
	 *
	 * @param e the e
	 */
	private void drlBrowseButtonActionPerformed(ActionEvent e) {
		File dir= new File(drlTextField.getText().split(";")[0]);
		JFileChooser fc;
		if(dir.isFile())
			fc= new JFileChooser(dir.getParentFile());
		else
			fc= new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
			File[] drlfiles = fc.getSelectedFiles();
			String files="";
			for (int i = 0; i < drlfiles.length; i++) {
				drlFiles.add(drlfiles[i].getAbsolutePath());
				if(i==0) files=files.concat(drlfiles[i].getAbsolutePath());
				else files=files.concat(";"+drlfiles[i].getAbsolutePath());
			}
			drlTextField.setText(files);
		}
	}

	/**
	 * Business browse button action performed.
	 *
	 * @param e the e
	 */
	private void businessBrowseButtonActionPerformed(ActionEvent e) {
		File dir= new File(businessTextField.getText());
		JFileChooser fc;
		if(dir.isFile())
			fc= new JFileChooser(dir.getParentFile());
		else
			fc= new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
				businessTextField.setText(fc.getSelectedFile().getAbsolutePath());
		}
	}

	/**
	 * Check state.
	 *
	 * @param row the row
	 */
	protected void checkState(int row) {
		boolean value= (Boolean) statesTable.getValueAt(row, 0);
		UUID uuid = null;
		if(!value)
			uuid= states.get(statesTable.getValueAt(row,1)).getId();
			if(actions.size()==0) return;
			for (WfAction action : actions.values()) {
				if(action.getConsequence().getId().equals(uuid)){
					JOptionPane.showMessageDialog(this, "This State is present in one or more actions. Remove it from the actions first.");
					statesTable.setValueAt(true, row, 0);
					return;
				} 
			}
	}

	/**
	 * Action list value changed.
	 *
	 * @param e the e
	 */
	private void actionListValueChanged(ListSelectionEvent e) {
		if(actionList.getSelectedIndex()==-1) return;
		if(newButton.getText().equals("New Action") && newButton.isEnabled()){
			activeSelection=true;
			selectedAction=actionList.getSelectedIndex();
			WfAction a= actions.get(actionList.getSelectedValue());
			idTextField.setText(a.getId().toString());
			nameTextField.setText(a.getName());
			if(a.getBusinessProcess()!=null)
				businessTextField.setText(a.getBusinessProcess().getAbsolutePath());
			consequenceComboBox.removeAllItems();
			for (int i = 0; i < statesTable.getRowCount(); i++) {
				if(((Boolean)statesTable.getValueAt(i, 0))==true)
					consequenceComboBox.addItem((String)statesTable.getValueAt(i, 1));
			}
			if(a.getConsequence()!=null)
				consequenceComboBox.setSelectedItem(a.getConsequence().getName());
			editButton.setEnabled(true);
			removeButton.setEnabled(true);
		}else{
			if(activeSelection) 
				actionList.setSelectedIndex(selectedAction);
			else
				actionList.setSelectedIndex(-1);
		}
	}

	/**
	 * Save button action performed.
	 *
	 * @param e the e
	 */
	private void saveButtonActionPerformed(ActionEvent e) {
		if(removeButton.getText().equals("Cancel")){
			JOptionPane.showMessageDialog(this, "Before save, please finish Action edition or creation.");
			return;
		}
		if(workflowNameTextField.getText()!=null || workflowNameTextField.getText().length()>0){
			selRoles= new ArrayList<WfRole>();
			selStates= new ArrayList<WfState>();
			for (int i = 0; i < rolesTable.getRowCount(); i++) {
				if(((Boolean)rolesTable.getValueAt(i, 0))==true)
					selRoles.add(roles.get((String)rolesTable.getValueAt(i, 1)));
			}
			for (int i = 0; i < statesTable.getRowCount(); i++) {
				if(((Boolean)statesTable.getValueAt(i, 0))==true)
					selStates.add(states.get((String)statesTable.getValueAt(i, 1)));
			}
			WorkflowDefinition workflowDefinition = new WorkflowDefinition(selRoles,selStates,actions);
			workflowDefinition.setName(workflowNameTextField.getText());
			drlFiles= new ArrayList<String>();
			xlsFiles= new ArrayList<String>();
			if(drlTextField.getText()!=null && drlTextField.getText().length()>0){
				String[] strings = drlTextField.getText().split(";");
				if(strings!=null && strings.length>0){
					for (String string : strings) {
						if(new File(string).exists()) drlFiles.add(string);
					}
				}
			}
			if(xlsTextField.getText()!=null && xlsTextField.getText().length()>0){
				String[] strings = xlsTextField.getText().split(";");
				if(strings!=null && strings.length>0){
					for (String string : strings) {
						if(new File(string).exists()) xlsFiles.add(string);
					}
				}
			}
			workflowDefinition.setDrlFileName(drlFiles);
			workflowDefinition.setXlsFileName(xlsFiles);
			WorkflowDefinitionManager.writeWfDefinition(workflowDefinition);
			JOptionPane.showMessageDialog(this, "\""+workflowNameTextField.getText()+"\" saved.");
		}
		else
			JOptionPane.showMessageDialog(this, "Please write a name for the Workflow definition");
	}

	/**
	 * Open button action performed.
	 *
	 * @param e the e
	 */
	private void openButtonActionPerformed(ActionEvent e) {
		JFileChooser fc;
		if(workflowDefinitionFile==null)
			fc= new JFileChooser("sampleProcesses/");
		else
			fc= new JFileChooser(workflowDefinitionFile.getParent());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "*.wfd";
			}
			
			@Override
			public boolean accept(File f) {
				if(f.getName().substring(f.getName().length()-4).equalsIgnoreCase(".wfd")) return true;
				else return false;
			}
		});
		if(fc.showOpenDialog(this)==JFileChooser.CANCEL_OPTION) return;
		workflowDefinitionFile= fc.getSelectedFile();
		loadFile();
	}
	
	
	/**
	 * Load file.
	 */
	private void loadFile() {
		
		WorkflowDefinition workflowDefinition = WorkflowDefinitionManager.readWfDefinition(workflowDefinitionFile.getName());
		workflowNameTextField.setText(workflowDefinition.getName());
		
		WfComponentProvider wp= new WfComponentProvider();
		List<WfRole> rolesList = wp.getRoles();
		List<WfState> statesList = wp.getStates();
		actions= (HashMap<String,WfAction>)workflowDefinition.getActions();
		selRoles= workflowDefinition.getRoles();
		selStates= workflowDefinition.getStates();
		roles= new HashMap<String, WfRole>();
		states= new HashMap<String, WfState>();
		
		for (WfRole role : selRoles) {
			roles.put(role.getName(), role);
		}
		for (WfState state : selStates) {
			states.put(state.getName(), state);
		}
		
		DefaultTableModel model= (DefaultTableModel) rolesTable.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			model.removeRow(i);
		}
		for (WfRole role : rolesList) {
			if(roles.containsKey(role.getName()))
				model.addRow(new Object[]{new Boolean(true),role.getName()});
			else{
				roles.put(role.getName(), role);
				model.addRow(new Object[]{new Boolean(false),role.getName()});
			}
		}
		rolesTable.setModel(model);
		
		model= (DefaultTableModel) statesTable.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			model.removeRow(i);
		}
		for (WfState state : statesList) {
			if(states.containsKey(state.getName()))
				model.addRow(new Object[]{new Boolean(true),state.getName()});
			else{
				states.put(state.getName(), state);
				model.addRow(new Object[]{new Boolean(false),state.getName()});
			}
		}
		((DefaultTableModel)statesTable.getModel()).addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				checkState(e.getFirstRow());
			}
		    });
		statesTable.setModel(model);
		consequenceComboBox.removeAllItems();
		for (int i = 0; i < statesTable.getRowCount(); i++) {
			if(((Boolean)statesTable.getValueAt(i, 0))==true)
				consequenceComboBox.addItem((String)statesTable.getValueAt(i, 1));
		}
		DefaultListModel lmodel = new DefaultListModel();
		if(actions.size()>0)
			for (Object act : actions.keySet().toArray()) {
				lmodel.addElement(act.toString());
			}	
		actionList.setModel(lmodel);
		actionList.updateUI();
		
		xlsFiles= workflowDefinition.getXlsFileName();
		String string = null;
		for (int i = 0; i < xlsFiles.size(); i++) {
			if(i==0) string=xlsFiles.get(0);
			else string= string.concat(";"+xlsFiles.get(i));
		}
		xlsTextField.setText(string);
		drlFiles= workflowDefinition.getDrlFileName();
		string = null;
		for (int i = 0; i < drlFiles.size(); i++) {
			if(i==0) string=drlFiles.get(0);
			else string= string.concat(";"+drlFiles.get(i));
		}
		drlTextField.setText(string);
		
		consequenceComboBox.setSelectedIndex(-1);
		activeSelection=false;
		selectedAction=-1;
		
		newButton.setEnabled(true);
		editButton.setEnabled(false);
		removeButton.setEnabled(false);
		nameTextField.setEnabled(false);
		businessTextField.setEnabled(false);
		consequenceComboBox.setEnabled(false);
		businessBrowseButton.setEnabled(false);
	}

	/**
	 * Close button action performed.
	 *
	 * @param e the e
	 */
	private void closeButtonActionPerformed(ActionEvent e) {					//CLOSE
		try {
				AceFrameConfig config;
				config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
				AceFrame ace=config.getAceFrame();
				JTabbedPane tp=ace.getCdePanel().getLeftTabs();
				if (tp != null) {
					int tabCount = tp.getTabCount();
					for (int i = 0; i < tabCount; i++) {
						if (tp.getTitleAt(i).equals(TranslationHelperPanel.WORKFLOWDEFINITION_TAB_NAME)) {
							tp.remove(i);
							tp.revalidate();
							tp.repaint();
						}
					}
				}
			} catch (TerminologyException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	}


	/**
	 * Cancel button action performed.
	 *
	 * @param e the e
	 */
	private void cancelButtonActionPerformed(ActionEvent e) {
		if(workflowDefinitionFile==null){
			actions= new HashMap<String, WfAction>();
			actionList.setModel(new DefaultListModel());
			idTextField.setText("");
			nameTextField.setText("");
			consequenceComboBox.setSelectedIndex(-1);
			consequenceComboBox.removeAllItems();
			businessTextField.setText("");
			xlsTextField.setText("");
			drlTextField.setText("");
			rolesTable.removeAll();
			roles= new HashMap<String, WfRole>();
			statesTable.removeAll();
			states= new  HashMap<String, WfState>();
		}
		else{
			loadFile();
		}
	}

	/**
	 * Edits the button action performed.
	 *
	 * @param e the e
	 */
	private void editButtonActionPerformed(ActionEvent e) {
		if(editButton.getText().equals("Edit")){
			editButton.setText("Save");
			removeButton.setText("Cancel");
			actionName= nameTextField.getText();
			
			newButton.setEnabled(false);
			editButton.setEnabled(true);
			removeButton.setEnabled(true);
			nameTextField.setEnabled(true);
			businessTextField.setEnabled(true);
			businessBrowseButton.setEnabled(true);
			consequenceComboBox.setEnabled(true);
			}
		else{
			if(nameTextField.getText()!=null && nameTextField.getText().length()>0){
				currentAction= new WfAction();
				currentAction.setId(UUID.fromString(idTextField.getText()));
				currentAction.setName(nameTextField.getText());
				File aux= new File(businessTextField.getText());
				if(aux.isFile())
					currentAction.setBusinessProcess(aux);
				currentAction.setConsequence((WfState) states.get(consequenceComboBox.getSelectedItem()));
				if(!currentAction.getName().equals(actionName) && actions.containsKey(currentAction.getName())){
					JOptionPane.showMessageDialog(this, "An action with the same name already exist.");
					return;
				}
				else{
					actions.remove(actionName);
					actionName="";
					actions.put(currentAction.getName(),currentAction);
					DefaultListModel lmodel = new DefaultListModel();
					if(actions.size()>0)
						for (Object act : actions.keySet().toArray()) {
							lmodel.addElement(act.toString());
						}	
					actionList.setModel(lmodel);
					actionList.updateUI();
				}
				editButton.setText("Edit");
				removeButton.setText("Remove");
				idTextField.setText("");
				nameTextField.setText("");
				businessTextField.setText("");
				consequenceComboBox.setSelectedIndex(-1);
				
				newButton.setEnabled(true);
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
				nameTextField.setEnabled(false);
				businessTextField.setEnabled(false);
				businessBrowseButton.setEnabled(false);
				consequenceComboBox.setEnabled(false);
			}
			else
				JOptionPane.showMessageDialog(this, "You must write a name for the current Action.");
		}
		
	}

	

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel8 = new JPanel();
		label11 = new JLabel();
		workflowNameTextField = new JTextField();
		openButton = new JButton();
		separator1 = new JSeparator();
		tabbedPane1 = new JTabbedPane();
		panel9 = new JPanel();
		scrollPane2 = new JScrollPane();
		rolesTable = new JTable();
		panel12 = new JPanel();
		scrollPane3 = new JScrollPane();
		statesTable = new JTable();
		panel10 = new JPanel();
		scrollPane1 = new JScrollPane();
		actionList = new JList();
		panel1 = new JPanel();
		panel5 = new JPanel();
		newButton = new JButton();
		editButton = new JButton();
		removeButton = new JButton();
		label3 = new JLabel();
		idTextField = new JTextField();
		label4 = new JLabel();
		nameTextField = new JTextField();
		label5 = new JLabel();
		consequenceComboBox = new JComboBox();
		label6 = new JLabel();
		panel6 = new JPanel();
		businessTextField = new JTextField();
		businessBrowseButton = new JButton();
		panel11 = new JPanel();
		panel3 = new JPanel();
		label9 = new JLabel();
		xlsTextField = new JTextField();
		xlsBrowseButton = new JButton();
		panel4 = new JPanel();
		label10 = new JLabel();
		drlTextField = new JTextField();
		drlBrowseButton = new JButton();
		separator2 = new JSeparator();
		panel7 = new JPanel();
		saveButton = new JButton();
		cancelButton = new JButton();
		closeButton = new JButton();

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {25, 305, 20, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {25, 0, 20, 20, 255, 20, 0, 20, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

		//======== panel8 ========
		{
			panel8.setLayout(new GridBagLayout());
			((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {25, 205, 205, 0, 0};
			((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label11 ----
			label11.setText("Name of Workflow Definition:");
			panel8.add(label11, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel8.add(workflowNameTextField, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- openButton ----
			openButton.setText("Open");
			openButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openButtonActionPerformed(e);
				}
			});
			panel8.add(openButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel8, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(separator1, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== tabbedPane1 ========
		{

			//======== panel9 ========
			{
				panel9.setLayout(new GridBagLayout());
				((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {15, 0, 10, 0};
				((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {15, 0, 10, 0};
				((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//======== scrollPane2 ========
				{

					//---- rolesTable ----
					rolesTable.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
							"Included", "Roles"
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							Boolean.class, String.class
						};
						boolean[] columnEditable = new boolean[] {
							true, false
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
						@Override
						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return columnEditable[columnIndex];
						}
					});
					{
						TableColumnModel cm = rolesTable.getColumnModel();
						cm.getColumn(0).setResizable(false);
						cm.getColumn(0).setMinWidth(55);
						cm.getColumn(0).setMaxWidth(55);
						cm.getColumn(0).setPreferredWidth(55);
					}
					rolesTable.setRowSelectionAllowed(false);
					scrollPane2.setViewportView(rolesTable);
				}
				panel9.add(scrollPane2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
			}
			tabbedPane1.addTab("Roles", panel9);


			//======== panel12 ========
			{
				panel12.setLayout(new GridBagLayout());
				((GridBagLayout)panel12.getLayout()).columnWidths = new int[] {15, 0, 10, 0};
				((GridBagLayout)panel12.getLayout()).rowHeights = new int[] {15, 0, 10, 0};
				((GridBagLayout)panel12.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel12.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//======== scrollPane3 ========
				{

					//---- statesTable ----
					statesTable.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
							"Included", "States"
						}
					) {
						Class<?>[] columnTypes = new Class<?>[] {
							Boolean.class, Object.class
						};
						boolean[] columnEditable = new boolean[] {
							true, false
						};
						@Override
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
						@Override
						public boolean isCellEditable(int rowIndex, int columnIndex) {
							return columnEditable[columnIndex];
						}
					});
					{
						TableColumnModel cm = statesTable.getColumnModel();
						cm.getColumn(0).setResizable(false);
						cm.getColumn(0).setMinWidth(55);
						cm.getColumn(0).setMaxWidth(55);
						cm.getColumn(0).setPreferredWidth(55);
					}
					statesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					statesTable.setRowSelectionAllowed(false);
					scrollPane3.setViewportView(statesTable);
				}
				panel12.add(scrollPane3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
			}
			tabbedPane1.addTab("States", panel12);


			//======== panel10 ========
			{
				panel10.setLayout(new GridBagLayout());
				((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {15, 145, 15, 197, 10, 0};
				((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {15, 0, 10, 0};
				((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

				//======== scrollPane1 ========
				{

					//---- actionList ----
					actionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					actionList.setToolTipText("Actions");
					actionList.addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e) {
							actionListValueChanged(e);
							actionListValueChanged(e);
							actionListValueChanged(e);
						}
					});
					scrollPane1.setViewportView(actionList);
				}
				panel10.add(scrollPane1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {55, 80, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//======== panel5 ========
					{
						panel5.setLayout(new GridBagLayout());
						((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
						((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0, 1.0E-4};
						((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- newButton ----
						newButton.setText("New Action");
						newButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								newButtonActionPerformed(e);
							}
						});
						panel5.add(newButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- editButton ----
						editButton.setText("Edit");
						editButton.setEnabled(false);
						editButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								editButtonActionPerformed(e);
							}
						});
						panel5.add(editButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- removeButton ----
						removeButton.setText("Remove");
						removeButton.setEnabled(false);
						removeButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								removeButtonActionPerformed(e);
							}
						});
						panel5.add(removeButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel5, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label3 ----
					label3.setText("Id:");
					panel1.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- idTextField ----
					idTextField.setEnabled(false);
					panel1.add(idTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label4 ----
					label4.setText("Name:");
					panel1.add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					panel1.add(nameTextField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label5 ----
					label5.setText("Consequence:");
					panel1.add(label5, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- consequenceComboBox ----
					consequenceComboBox.setEnabled(false);
					panel1.add(consequenceComboBox, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label6 ----
					label6.setText("Business Process:");
					panel1.add(label6, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//======== panel6 ========
					{
						panel6.setLayout(new GridBagLayout());
						((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {104, 30, 0};
						((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
						((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
						panel6.add(businessTextField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- businessBrowseButton ----
						businessBrowseButton.setText("Browse");
						businessBrowseButton.setEnabled(false);
						businessBrowseButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								businessBrowseButtonActionPerformed(e);
							}
						});
						panel6.add(businessBrowseButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel6, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel10.add(panel1, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
			}
			tabbedPane1.addTab("Actions", panel10);


			//======== panel11 ========
			{
				panel11.setLayout(new GridBagLayout());
				((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {15, 0, 0, 0, 0, 0, 10, 0};
				((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {25, 0, 25, 0, 25, 10, 0};
				((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};

				//======== panel3 ========
				{
					panel3.setLayout(new GridBagLayout());
					((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {85, 0, 70, 0};
					((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label9 ----
					label9.setText("XLS File:");
					panel3.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					panel3.add(xlsTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- xlsBrowseButton ----
					xlsBrowseButton.setText("Browse");
					xlsBrowseButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							xlsBrowseButtonActionPerformed(e);
						}
					});
					panel3.add(xlsBrowseButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel11.add(panel3, new GridBagConstraints(1, 1, 5, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel4 ========
				{
					panel4.setLayout(new GridBagLayout());
					((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {85, 0, 70, 0};
					((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label10 ----
					label10.setText("DRL File:");
					panel4.add(label10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					panel4.add(drlTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- drlBrowseButton ----
					drlBrowseButton.setText("Browse");
					drlBrowseButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							drlBrowseButtonActionPerformed(e);
						}
					});
					panel4.add(drlBrowseButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel11.add(panel4, new GridBagConstraints(1, 3, 5, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
			}
			tabbedPane1.addTab("Rules Files", panel11);

		}
		add(tabbedPane1, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(separator2, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel7 ========
		{
			panel7.setLayout(new GridBagLayout());
			((GridBagLayout)panel7.getLayout()).columnWidths = new int[] {25, 0, 0, 0, 0, 20, 0};
			((GridBagLayout)panel7.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel7.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel7.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- saveButton ----
			saveButton.setText("Save");
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveButtonActionPerformed(e);
				}
			});
			panel7.add(saveButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- cancelButton ----
			cancelButton.setText("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelButtonActionPerformed(e);
				}
			});
			panel7.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- closeButton ----
			closeButton.setText("Close");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeButtonActionPerformed(e);
				}
			});
			panel7.add(closeButton, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel7, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The panel8. */
	private JPanel panel8;
	
	/** The label11. */
	private JLabel label11;
	
	/** The workflow name text field. */
	private JTextField workflowNameTextField;
	
	/** The open button. */
	private JButton openButton;
	
	/** The separator1. */
	private JSeparator separator1;
	
	/** The tabbed pane1. */
	private JTabbedPane tabbedPane1;
	
	/** The panel9. */
	private JPanel panel9;
	
	/** The scroll pane2. */
	private JScrollPane scrollPane2;
	
	/** The roles table. */
	private JTable rolesTable;
	
	/** The panel12. */
	private JPanel panel12;
	
	/** The scroll pane3. */
	private JScrollPane scrollPane3;
	
	/** The states table. */
	private JTable statesTable;
	
	/** The panel10. */
	private JPanel panel10;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The action list. */
	private JList actionList;
	
	/** The panel1. */
	private JPanel panel1;
	
	/** The panel5. */
	private JPanel panel5;
	
	/** The new button. */
	private JButton newButton;
	
	/** The edit button. */
	private JButton editButton;
	
	/** The remove button. */
	private JButton removeButton;
	
	/** The label3. */
	private JLabel label3;
	
	/** The id text field. */
	private JTextField idTextField;
	
	/** The label4. */
	private JLabel label4;
	
	/** The name text field. */
	private JTextField nameTextField;
	
	/** The label5. */
	private JLabel label5;
	
	/** The consequence combo box. */
	private JComboBox consequenceComboBox;
	
	/** The label6. */
	private JLabel label6;
	
	/** The panel6. */
	private JPanel panel6;
	
	/** The business text field. */
	private JTextField businessTextField;
	
	/** The business browse button. */
	private JButton businessBrowseButton;
	
	/** The panel11. */
	private JPanel panel11;
	
	/** The panel3. */
	private JPanel panel3;
	
	/** The label9. */
	private JLabel label9;
	
	/** The xls text field. */
	private JTextField xlsTextField;
	
	/** The xls browse button. */
	private JButton xlsBrowseButton;
	
	/** The panel4. */
	private JPanel panel4;
	
	/** The label10. */
	private JLabel label10;
	
	/** The drl text field. */
	private JTextField drlTextField;
	
	/** The drl browse button. */
	private JButton drlBrowseButton;
	
	/** The separator2. */
	private JSeparator separator2;
	
	/** The panel7. */
	private JPanel panel7;
	
	/** The save button. */
	private JButton saveButton;
	
	/** The cancel button. */
	private JButton cancelButton;
	
	/** The close button. */
	private JButton closeButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
