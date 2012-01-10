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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
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

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.panel.details.ZebraJTable;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.wizard.I_fastWizard;

/**
 * @author Guillermo Reynoso
 */
public class DataGridCollectorFromList extends JPanel implements I_fastWizard{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3557213261369154457L;
	/**
	 * 
	 */
	private String[] columnNames;
	private String key;
	private List<WfUser> users;
	private List<WfRole> roles;
	
	
	public List<WfUser> getUsers() {
		return users;
	}

	public List<WfRole> getRoles() {
		return roles;
	}

	public void setUsers(List<WfUser> users) {
		this.users = users;
	}

	public void setRoles(List<WfRole> roles) {
		this.roles = roles;
	}

	
	public DataGridCollectorFromList(List<WfRole> roles, List<WfUser> users) {
		initComponents();
		this.roles=roles;
		this.users=users;
		tblObjs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblObjs.getTableHeader().setReorderingAllowed(false);
		setColumNames();
		loadObjects();
	}
	
	public DataGridCollectorFromList() {
		initComponents();
		tblObjs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	public void setColumNames(){
		Collections.sort(roles, new Comparator<WfRole>() {
			@Override
			public int compare(WfRole o1, WfRole o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		this.columnNames=new String[roles.size()*2+1];
		this.columnNames[0]="Users";
		int j=1;
		for (int i = 0; i < roles.size(); i++) {
			this.columnNames[j++]=roles.get(i).toString();
			this.columnNames[j++]="default";
		}
	}
	public void setKey(String key){
		this.key=key;
	}
	public void loadObjects(){

		try {
			ProjectPermissionsAPI ppa= new ProjectPermissionsAPI(Terms.get().getActiveAceFrameConfig());
			I_GetConceptData projectRoot= Terms.get().getConcept(SNOMED.Concept.ROOT.getUids());
			WfComponentProvider wfcp = new WfComponentProvider();
			List<Object[]> rows= new ArrayList<Object[]>();
			List<Object[]> active= new ArrayList<Object[]>();
			
			for(WfUser obj:users){
				Set<I_GetConceptData> userRoles= ppa.getRolesForUser(Terms.get().getConcept(obj.getId()), projectRoot);
				if(userRoles.size()==0) continue;
				Object[] row = new Object[columnNames.length];
				Object[] act = new Object[columnNames.length];
				for (int i = 0; i < columnNames.length; i++) {
					row[i]= false;
					act[i]= false;
				}
				int counter=0;
				for (I_GetConceptData i_GetConceptData : userRoles) {
					WfRole userRole = wfcp.roleConceptToWfRole(i_GetConceptData);
					int index=-1;
					for (int i = 0; i < roles.size(); i++) {
						if(roles.get(i).getId().equals(userRole.getId())){
							index=i;
							break;
						}
					}
					if(index!=-1){
						counter++;
						act[index*2+1]=true;
						act[index*2+2]=true;
					}
				}
				if(counter==0) continue;
				row[0]=obj;
				rows.add(row);
				active.add(act);
			}
			
			Object[][] data = new Object[active.size()][];
			for (int i = 0; i < data.length; i++) {
				data[i]=(Object[]) active.get(i);
			}
			class GridTableModel extends DefaultTableModel {
				private static final long serialVersionUID = 1L;
				private Object[][] grid;
				public GridTableModel(Object[][] d, String[] c, Object[][] g){
					super(d,c);
					grid=g;
				};
				public boolean isCellEditable(int x, int y) {
					return (Boolean)grid[x][y];
				}
			};
			
			GridTableModel model = new GridTableModel(null, columnNames,data);
			for (Object[] row : rows) {
				model.addRow(row);
			}
			tblObjs.setModel(model);
			TableColumnModel cmodel = tblObjs.getColumnModel();
			CheckBoxRenderer checkBoxRenderer = new CheckBoxRenderer();
			RadioRenderer rr= new RadioRenderer();
			cmodel.getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
			for (int i = 1; i < (columnNames.length); i++) {
				if(i%2==1){
				cmodel.getColumn(i).setCellRenderer(checkBoxRenderer);
				cmodel.getColumn(i).setCellEditor(new CheckBoxEditor(new JCheckBox(),new JCheckBox()));
				}
				else{
				cmodel.getColumn(i).setCellRenderer(rr);
				cmodel.getColumn(i).setCellEditor(new RadioEditor(new JCheckBox(),new JRadioButton()));
				cmodel.getColumn(i).setMaxWidth(0);
				cmodel.getColumn(i).setMinWidth(0);
				}
			}
			hideColumns();
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}
	public void setLabel(String strLabel){
		this.label1.setText(strLabel);
	}

	class RadioRenderer extends DefaultTableCellRenderer implements
	TableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		RadioRenderer() {
			super();
			setHorizontalAlignment(LEFT);
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

	class CheckBoxRenderer extends DefaultTableCellRenderer implements
	TableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		CheckBoxRenderer() {
			super();
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			JCheckBox button = new JCheckBox();
			Boolean val=(Boolean)value;
			button.setSelected(val);
			button.setHorizontalAlignment(JButton.RIGHT);
			button.setHorizontalTextPosition(JButton.LEFT);
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
		int column;
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
			this.column= column;
			return rbutton;
		}

		public Object getCellEditorValue() {

			int rowModel = tblObjs.convertRowIndexToModel(row);
			Boolean val=(Boolean)tblObjs.getModel().getValueAt(rowModel, column);
			return val;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JRadioButton button = (JRadioButton)e.getSource();
			if (button.isSelected())
				itemRadioActionPerformed(1,this.row,column);
			else
				itemRadioActionPerformed(2,this.row,column);
			
		}

	}

	class CheckBoxEditor extends DefaultCellEditor implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JCheckBox rbutton;
		int row;
		int column;
		public CheckBoxEditor(JCheckBox checkBox, JCheckBox button) {
			super(checkBox);
			this.rbutton = button;
			rbutton.setHorizontalAlignment(JButton.RIGHT);
			rbutton.setHorizontalTextPosition(JButton.LEFT);
			this.rbutton.addActionListener(this);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			Boolean val=(Boolean) value;
			rbutton.setSelected(val);
			this.row=row;
			this.column= column;
			return rbutton;
		}

		public Object getCellEditorValue() {
			return new Boolean(rbutton.isSelected());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox button = (JCheckBox)e.getSource();
			if (button.isSelected())
				itemCheckBoxActionPerformed(1,this.row,column);
			else
				itemCheckBoxActionPerformed(2,this.row,column);
			
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
		hideColumns();
	}

	
	private void hideColumns() {
		DefaultTableModel model= (DefaultTableModel) tblObjs.getModel();
		for (int i = 1; i < model.getColumnCount(); i+=2) {
			int counter=0;
			int counter2=0;
			int lastPos=0;
			for (int j = 0; j < model.getRowCount(); j++) {
				if(((Boolean)model.getValueAt(j, i))==true){
					counter++;
					lastPos=j;
					if(((Boolean)model.getValueAt(j, i+1))==true)
						counter2++;
				}
			}
			if(counter<2){
				if(counter==1){
					for (int j = 0; j < model.getRowCount(); j++){
						model.setValueAt(false, j, i+1);
					}
					model.setValueAt(true, lastPos, i+1);
				}
				tblObjs.getColumnModel().getColumn(i+1).setMaxWidth(0);
				tblObjs.getColumnModel().getColumn(i+1).setMinWidth(0);
			}
			else{ 
				tblObjs.getColumnModel().getColumn(i+1).setMaxWidth(50);
				tblObjs.getColumnModel().getColumn(i+1).setMinWidth(50);
			}
		}
		tblObjs.setModel(model);
		model.fireTableDataChanged();
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

		
		for (int j = 1; j < columnNames.length; j+=2) {
			boolean bExists = false;
			for (int i=0;i<model.getRowCount();i++){
				Boolean sel=(Boolean)model.getValueAt(i, j);
				if ( sel==true) {
					bExists=true;
					break; 
				}
			}
			if (!bExists){
				throw new Exception(model.getColumnName(j)+" is empty.");
			}
		}
		
		
		for (int j = 2; j < columnNames.length; j+=2) {
			int bExists = 0;
			int dExists = 0;
			for (int i=0;i<model.getRowCount();i++){
				Boolean def=(Boolean)model.getValueAt(i, j);
				Boolean sel=(Boolean)model.getValueAt(i, j-1);
				if (sel==true)
					bExists++;
				if (def==true) 
					dExists++;
			}
			if (bExists==0){
			throw new Exception(model.getColumnName(j)+" is empty.");
			}
			if (dExists>1){
				throw new Exception(model.getColumnName(j-1)+" default must be only one.");
			}
		}
		hmRes.put(key,model);	
		return hmRes;
	}
}
