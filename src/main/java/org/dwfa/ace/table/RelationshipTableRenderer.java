package org.dwfa.ace.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;

import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;

public class RelationshipTableRenderer extends AceTableRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel renderComponent =  (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		boolean same = false;
		StringWithRelTuple swt = (StringWithRelTuple) value;
		boolean uncommitted = swt.getTuple().getVersion() == Integer.MAX_VALUE;

		if (row > 0) {
			StringWithRelTuple prevSwt = (StringWithRelTuple) table.getValueAt(row - 1, column);
			same = swt.getTuple().getRelId() == prevSwt.getTuple().getRelId();
			setBorder(column, this, same, uncommitted);
			if ((same) && (swt.getCellText().equals(prevSwt.getCellText()))) {
				renderComponent.setText("");
			}
		} else {
			setBorder(column, this, false, uncommitted);
		}
		
        if (isSelected == false) {
        	renderComponent.setBackground(colorForRow(row));
        	renderComponent.setForeground(UIManager.getColor("Table.foreground"));
        } else {
        	renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
        	renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
        }		
		return renderComponent;
	}


}
