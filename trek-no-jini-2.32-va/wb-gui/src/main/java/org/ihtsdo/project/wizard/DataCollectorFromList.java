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

package org.ihtsdo.project.wizard;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.ihtsdo.project.view.details.ZebraJTable;
import org.ihtsdo.wizard.I_fastWizard;

/**
 * The Class DataCollectorFromList.
 *
 * @author Guillermo Reynoso
 */
public class DataCollectorFromList extends JPanel implements I_fastWizard{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3557213261369154457L;
	
	/** The column names. */
	private String[] columnNames;
	
	/** The key. */
	private String key;
	
	/**
	 * Instantiates a new data collector from list.
	 */
	public DataCollectorFromList() {
		initComponents();

		tblObjs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblObjs.setModel(new DefaultTableModel());
	}
	
	/**
	 * Sets the colum names.
	 *
	 * @param columnNames the new colum names
	 */
	public void setColumNames(String[] columnNames){
		this.columnNames=columnNames;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.wizard.I_fastWizard#setKey(java.lang.String)
	 */
	public void setKey(String key){
		this.key=key;
	}
	
	/**
	 * Load objects.
	 *
	 * @param objects the objects
	 */
	public void loadObjects(java.util.List<Object> objects){

		Object[][] data = null;
		DefaultTableModel model = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				if (y==2)
					return false;
				return true;
			}
		};

		for(Object obj:objects){
			Object[] row = new Object[]{false,false,obj}; 
			model.addRow(row);

		}
		tblObjs.setModel(model);
		TableColumnModel cmodel = tblObjs.getColumnModel();
		CheckBoxRenderer checkBoxRenderer = new CheckBoxRenderer();
		cmodel.getColumn(0).setCellRenderer(checkBoxRenderer);
		cmodel.getColumn(0).setCellEditor(new CheckBoxEditor(new JCheckBox(),new JCheckBox()));
		cmodel.getColumn(1).setCellRenderer(new RadioRenderer());
		cmodel.getColumn(1).setCellEditor(new RadioEditor(new JCheckBox(),new JRadioButton()));
		cmodel.getColumn(2).setCellRenderer(new DefaultTableCellRenderer());
	}
	
	/**
	 * Sets the label.
	 *
	 * @param strLabel the new label
	 */
	public void setLabel(String strLabel){
		this.label1.setText(strLabel);
	}

	/**
	 * The Class RadioRenderer.
	 */
	class RadioRenderer implements
	TableCellRenderer {
		
		/**
		 * Instantiates a new radio renderer.
		 */
		RadioRenderer() {
			super();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JRadioButton button = new JRadioButton();
			Boolean val=(Boolean)value;
			button.setSelected(val);
			return button;
		}
	}

	/**
	 * The Class CheckBoxRenderer.
	 */
	class CheckBoxRenderer implements
	TableCellRenderer {
		
		/**
		 * Instantiates a new check box renderer.
		 */
		CheckBoxRenderer() {
			super();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JCheckBox button = new JCheckBox();

			Boolean val=(Boolean)value;
			button.setSelected(val);
			return button;
		}
	}

	/**
	 * The Class RadioEditor.
	 */
	class RadioEditor extends DefaultCellEditor implements ActionListener {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
		
		/** The rbutton. */
		JRadioButton rbutton;
		
		/** The row. */
		int row;
		
		/**
		 * Instantiates a new radio editor.
		 *
		 * @param checkBox the check box
		 * @param button the button
		 */
		public RadioEditor(JCheckBox checkBox, JRadioButton button) {
			super(checkBox);
			this.rbutton = button;
			this.rbutton.addActionListener(this);
		}

		/* (non-Javadoc)
		 * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			Boolean val=(Boolean) value;
			rbutton.setSelected(val);
			this.row=row;
			return rbutton;
		}

		/* (non-Javadoc)
		 * @see javax.swing.DefaultCellEditor#getCellEditorValue()
		 */
		public Object getCellEditorValue() {

			int rowModel = tblObjs.convertRowIndexToModel(row);
			Boolean val=(Boolean)tblObjs.getModel().getValueAt(rowModel, 1);
			return val;
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButton button = (JRadioButton)e.getSource();
			if (button.isSelected())
				itemRadioActionPerformed(1,this.row,1);
			else
				itemRadioActionPerformed(2,this.row,1);
			
		}

	}

	/**
	 * The Class CheckBoxEditor.
	 */
	class CheckBoxEditor extends DefaultCellEditor implements ActionListener {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
		
		/** The rbutton. */
		JCheckBox rbutton;
		
		/** The row. */
		int row;
		
		/**
		 * Instantiates a new check box editor.
		 *
		 * @param checkBox the check box
		 * @param button the button
		 */
		public CheckBoxEditor(JCheckBox checkBox, JCheckBox button) {
			super(checkBox);
			this.rbutton = button;
			this.rbutton.addActionListener(this);
		}

		/* (non-Javadoc)
		 * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			Boolean val=(Boolean) value;
			rbutton.setSelected(val);
			this.row=row;
			return rbutton;
		}

		/* (non-Javadoc)
		 * @see javax.swing.DefaultCellEditor#getCellEditorValue()
		 */
		public Object getCellEditorValue() {
			return new Boolean(rbutton.isSelected());
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox button = (JCheckBox)e.getSource();
			if (button.isSelected())
				itemCheckBoxActionPerformed(1,this.row,0);
			else
				itemCheckBoxActionPerformed(2,this.row,0);
			
		}
	}


	/**
	 * Item radio action performed.
	 *
	 * @param state the state
	 * @param row the row
	 * @param column the column
	 */
	private void itemRadioActionPerformed(int state, int row,int column) {
		boolean othersRow;
		boolean ownRow;

		if (state==2){
			othersRow=true;
			ownRow=false;
		}else{
			othersRow=false;
			ownRow=true;
		}
		for (int r=0;r<tblObjs.getRowCount();r++){
			int rowModel = tblObjs.convertRowIndexToModel(r);
			if (r==row)
				tblObjs.getModel().setValueAt(ownRow,rowModel, column);
			else
				tblObjs.getModel().setValueAt(othersRow,rowModel, column);
		}

	}
	
	/**
	 * Item check box action performed.
	 *
	 * @param state the state
	 * @param row the row
	 * @param column the column
	 */
	private void itemCheckBoxActionPerformed(int state, int row,int column) {
		int rowModel = tblObjs.convertRowIndexToModel(row);
		if (state==2)
			tblObjs.getModel().setValueAt(false,rowModel, column);
		else
			tblObjs.getModel().setValueAt(true,rowModel, column);
	}
	
	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		scrollPane1 = new JScrollPane();
		tblObjs = new ZebraJTable();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Set users for role:");
		add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane1 ========
		{

			//---- tblObjs ----
			tblObjs.setColumnSelectionAllowed(true);
			tblObjs.setAutoCreateRowSorter(true);
			scrollPane1.setViewportView(tblObjs);
		}
		add(scrollPane1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The label1. */
	private JLabel label1;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The tbl objs. */
	private ZebraJTable tblObjs;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	/* (non-Javadoc)
	 * @see org.ihtsdo.wizard.I_fastWizard#getData()
	 */
	@Override
	public HashMap<String, Object> getData() throws Exception {
		HashMap<String, Object> hmRes=new HashMap<String, Object>();
		DefaultTableModel model=(DefaultTableModel) tblObjs.getModel();

		boolean bExists = false;
		for (int i=0;i<model.getRowCount();i++){
			Boolean sel=(Boolean)model.getValueAt(i, 0);
			if ( sel==true) {
				bExists=true;
				break; 
			}
		}
		if (!bExists){
			throw new Exception("Doesn't exist selected item");
		}
		bExists = false;
		for (int i=0;i<model.getRowCount();i++){
			Boolean def=(Boolean)model.getValueAt(i, 1);
			Boolean sel=(Boolean)model.getValueAt(i, 0);
			if (def==true && sel==true) {
				bExists=true;
				break; 
			}
		}
		if (!bExists){
			throw new Exception("Doesn't exist default item");
		}
		hmRes.put(key,model);	
		return hmRes;
	}
}
