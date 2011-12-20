/*
 * Created by JFormDesigner on Tue Dec 06 12:25:43 GMT-03:00 2011
 */

package org.ihtsdo.project.workflow.wizard;

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

import org.ihtsdo.project.panel.details.ZebraJTable;
import org.ihtsdo.wizard.I_fastWizard;

/**
 * @author Guillermo Reynoso
 */
public class DataCollectorFromList extends JPanel implements I_fastWizard{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3557213261369154457L;
	/**
	 * 
	 */
	private String[] columnNames;
	private String key;
	public DataCollectorFromList() {
		initComponents();

		tblObjs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblObjs.setModel(new DefaultTableModel());
	}
	public void setColumNames(String[] columnNames){
		this.columnNames=columnNames;
	}
	public void setKey(String key){
		this.key=key;
	}
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
	public void setLabel(String strLabel){
		this.label1.setText(strLabel);
	}

	class RadioRenderer implements
	TableCellRenderer {
		RadioRenderer() {
			super();
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JRadioButton button = new JRadioButton();
			Boolean val=(Boolean)value;
			button.setSelected(val);
			return button;
		}
	}

	class CheckBoxRenderer implements
	TableCellRenderer {
		CheckBoxRenderer() {
			super();
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JCheckBox button = new JCheckBox();

			Boolean val=(Boolean)value;
			button.setSelected(val);
			return button;
		}
	}

	class RadioEditor extends DefaultCellEditor implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JRadioButton rbutton;
		int row;
		public RadioEditor(JCheckBox checkBox, JRadioButton button) {
			super(checkBox);
			this.rbutton = button;
			this.rbutton.addActionListener(this);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			Boolean val=(Boolean) value;
			rbutton.setSelected(val);
			this.row=row;
			return rbutton;
		}

		public Object getCellEditorValue() {

			int rowModel = tblObjs.convertRowIndexToModel(row);
			Boolean val=(Boolean)tblObjs.getModel().getValueAt(rowModel, 1);
			return val;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButton button = (JRadioButton)e.getSource();
			if (button.isSelected())
				itemRadioActionPerformed(1,this.row,1);
			else
				itemRadioActionPerformed(2,this.row,1);
			
		}

	}

	class CheckBoxEditor extends DefaultCellEditor implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JCheckBox rbutton;
		int row;
		public CheckBoxEditor(JCheckBox checkBox, JCheckBox button) {
			super(checkBox);
			this.rbutton = button;
			this.rbutton.addActionListener(this);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			Boolean val=(Boolean) value;
			rbutton.setSelected(val);
			this.row=row;
			return rbutton;
		}

		public Object getCellEditorValue() {
			return new Boolean(rbutton.isSelected());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox button = (JCheckBox)e.getSource();
			if (button.isSelected())
				itemCheckBoxActionPerformed(1,this.row,0);
			else
				itemCheckBoxActionPerformed(2,this.row,0);
			
		}
	}


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
	private void itemCheckBoxActionPerformed(int state, int row,int column) {
		int rowModel = tblObjs.convertRowIndexToModel(row);
		if (state==2)
			tblObjs.getModel().setValueAt(false,rowModel, column);
		else
			tblObjs.getModel().setValueAt(true,rowModel, column);
	}
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
	private JLabel label1;
	private JScrollPane scrollPane1;
	private ZebraJTable tblObjs;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
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
