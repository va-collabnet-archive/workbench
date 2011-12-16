/*
 * Created by JFormDesigner on Mon Dec 12 17:59:02 GMT-03:00 2011
 */

package org.ihtsdo.project.panel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

/**
 * @author Cesar Zamorano
 */
@SuppressWarnings("serial")
public class WorkflowDefinitionPanel extends JPanel {
	
	private WfAction currentAction;
	private HashMap<String,WfAction> actions;
	private List<String> drlFiles;
	private List<String> xlsFiles;
	private List<WfRole> selRoles;
	private List<WfState> selStates;
	private HashMap<String,WfRole> roles;
	private HashMap<String,WfState> states;
	private File workflowDefinitionFile;
	
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
			consequenceComboBox.addItem(state.getName());
		}
		statesTable.setModel(model);
		statesTable.getTableHeader().setReorderingAllowed(false);

		consequenceComboBox.setSelectedIndex(-1);
	}

	private void newButtonActionPerformed(ActionEvent e) {
		if(removeButton.getText().equals("Remove Action")){
			for (Component comp : this.getComponents()) {
				if(JButton.class.isInstance(comp)) comp.setEnabled(false);
			}
			newButton.setText("OK");
			newButton.setEnabled(true);
			removeButton.setText("Cancel");
			removeButton.setEnabled(true);
			currentAction= new WfAction();
			UUID id= UUID.randomUUID();
			currentAction.setId(id);
			idTextField.setText(id.toString());
			nameTextField.setText("");
			businessTextField.setText("");
			consequenceComboBox.setSelectedIndex(-1);
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
					actions.put(currentAction.getName(),currentAction);
					actionList.removeAll();
					actionList.setListData(actions.keySet().toArray());
					actionList.updateUI();
				}
				
				for (Component comp : this.getComponents()) {
					if(JButton.class.isInstance(comp)) comp.setEnabled(true);
				}
				newButton.setText("New Action");
				removeButton.setText("Remove Action");
				idTextField.setText("");
				nameTextField.setText("");
				businessTextField.setText("");
				consequenceComboBox.setSelectedIndex(-1);
			}
			else
				JOptionPane.showMessageDialog(this, "You must write a name for the current Action.");
		}
	}

	private void removeButtonActionPerformed(ActionEvent e) {
		if(removeButton.getText().equals("Remove Action")){
			int i=actionList.getSelectedIndex();
			if(i>-1){
				actions.remove(actionList.getSelectedValue());
				actionList.setSelectedIndex(-1);
				idTextField.setText("");
				nameTextField.setText("");
				businessTextField.setText("");
				consequenceComboBox.setSelectedIndex(-1);
				actionList.removeAll();
				actionList.setListData(actions.keySet().toArray());
				actionList.updateUI();
			}
		}
		else{
			for (Component comp : this.getComponents()) {
				if(JButton.class.isInstance(comp)) comp.setEnabled(true);
			}
			newButton.setText("New Action");
			removeButton.setText("Remove Action");
			idTextField.setText("");
			nameTextField.setText("");
			businessTextField.setText("");
			consequenceComboBox.setSelectedIndex(-1);
		}
	}

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

	private void actionListValueChanged(ListSelectionEvent e) {
		if(newButton.getText().equals("New Action") && actionList.getSelectedIndex()>-1){
			WfAction a= actions.get(actionList.getSelectedValue());
			idTextField.setText(a.getId().toString());
			nameTextField.setText(a.getName());
			businessTextField.setText(a.getBusinessProcess().getAbsolutePath());
			consequenceComboBox.setSelectedItem(a.getConsequence().getName());
		}else{
			actionList.setSelectedIndex(-1);
		}
	}

	private void saveButtonActionPerformed(ActionEvent e) {
		if(workflowNameTextField.getText()!=null || workflowNameTextField.getText().length()>0){
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
			WfComponentProvider.writeWfDefinition(workflowDefinition);
		}
		else
			JOptionPane.showMessageDialog(this, "Please write a name for the Workflow definition");
	}

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
				return "Extension must be \"wfd\"";
			}
			
			@Override
			public boolean accept(File f) {
				if(f.getName().substring(f.getName().length()-4).equalsIgnoreCase(".wfd")) return true;
				else return false;
			}
		});
		if(fc.showOpenDialog(this)==JFileChooser.CANCEL_OPTION) return;
		workflowDefinitionFile= fc.getSelectedFile();
		WorkflowDefinition workflowDefinition = WfComponentProvider.readWfDefinition(workflowDefinitionFile);
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
			consequenceComboBox.addItem(state.getName());
		}
		statesTable.setModel(model);
		
		actionList.removeAll();
		actionList.setListData(actions.keySet().toArray());
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
	}
	

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel8 = new JPanel();
		label11 = new JLabel();
		workflowNameTextField = new JTextField();
		separator4 = new JSeparator();
		panel2 = new JPanel();
		scrollPane2 = new JScrollPane();
		rolesTable = new JTable();
		scrollPane3 = new JScrollPane();
		statesTable = new JTable();
		separator1 = new JSeparator();
		scrollPane1 = new JScrollPane();
		actionList = new JList();
		panel1 = new JPanel();
		panel5 = new JPanel();
		newButton = new JButton();
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
		separator2 = new JSeparator();
		panel3 = new JPanel();
		label9 = new JLabel();
		xlsTextField = new JTextField();
		xlsBrowseButton = new JButton();
		panel4 = new JPanel();
		label10 = new JLabel();
		drlTextField = new JTextField();
		drlBrowseButton = new JButton();
		separator3 = new JSeparator();
		panel7 = new JPanel();
		openButton = new JButton();
		saveButton = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {25, 55, 75, 25, 55, 105, 20, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {25, 0, 20, 0, 20, 0, 20, 0, 0, 20, 0, 20, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

		//======== panel8 ========
		{
			panel8.setLayout(new GridBagLayout());
			((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {205, 325, 0};
			((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label11 ----
			label11.setText("Name of Workflow Definition:");
			panel8.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel8.add(workflowNameTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel8, new GridBagConstraints(1, 1, 5, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(separator4, new GridBagConstraints(0, 2, 7, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {255, 0, 250, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {100, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

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
				scrollPane2.setViewportView(rolesTable);
			}
			panel2.add(scrollPane2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

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
				scrollPane3.setViewportView(statesTable);
			}
			panel2.add(scrollPane3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(1, 3, 5, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(separator1, new GridBagConstraints(0, 4, 7, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

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
				}
			});
			scrollPane1.setViewportView(actionList);
		}
		add(scrollPane1, new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0,
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
				((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0};
				((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
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

				//---- removeButton ----
				removeButton.setText("Remove Action");
				removeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						removeButtonActionPerformed(e);
					}
				});
				panel5.add(removeButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
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
			idTextField.setEditable(false);
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
		add(panel1, new GridBagConstraints(4, 5, 2, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(separator2, new GridBagConstraints(0, 6, 7, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

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
		add(panel3, new GridBagConstraints(1, 7, 5, 1, 0.0, 0.0,
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
		add(panel4, new GridBagConstraints(1, 8, 5, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(separator3, new GridBagConstraints(0, 9, 7, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel7 ========
		{
			panel7.setLayout(new GridBagLayout());
			((GridBagLayout)panel7.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel7.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel7.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel7.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- openButton ----
			openButton.setText("Open");
			openButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openButtonActionPerformed(e);
				}
			});
			panel7.add(openButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- saveButton ----
			saveButton.setText("Save");
			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveButtonActionPerformed(e);
				}
			});
			panel7.add(saveButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel7, new GridBagConstraints(5, 10, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel8;
	private JLabel label11;
	private JTextField workflowNameTextField;
	private JSeparator separator4;
	private JPanel panel2;
	private JScrollPane scrollPane2;
	private JTable rolesTable;
	private JScrollPane scrollPane3;
	private JTable statesTable;
	private JSeparator separator1;
	private JScrollPane scrollPane1;
	private JList actionList;
	private JPanel panel1;
	private JPanel panel5;
	private JButton newButton;
	private JButton removeButton;
	private JLabel label3;
	private JTextField idTextField;
	private JLabel label4;
	private JTextField nameTextField;
	private JLabel label5;
	private JComboBox consequenceComboBox;
	private JLabel label6;
	private JPanel panel6;
	private JTextField businessTextField;
	private JButton businessBrowseButton;
	private JSeparator separator2;
	private JPanel panel3;
	private JLabel label9;
	private JTextField xlsTextField;
	private JButton xlsBrowseButton;
	private JPanel panel4;
	private JLabel label10;
	private JTextField drlTextField;
	private JButton drlBrowseButton;
	private JSeparator separator3;
	private JPanel panel7;
	private JButton openButton;
	private JButton saveButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
