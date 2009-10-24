package org.dwfa.ace.table.refset;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;

import org.dwfa.ace.table.AceTableRenderer;

public class ExtTableRenderer extends AceTableRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel renderComponent =  (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);
        boolean same = false;
        if (value != null) {
            if (StringWithExtTuple.class.isAssignableFrom(value.getClass())) {
                StringWithExtTuple swt = (StringWithExtTuple) value;
                
                boolean uncommitted = swt.getTuple().getVersion() == Integer.MAX_VALUE;

                if (row > 0) {
                	if (table.getValueAt(row - 1, column).getClass().isAssignableFrom(StringWithExtTuple.class)) {
                        StringWithExtTuple prevSwt = (StringWithExtTuple) table.getValueAt(row - 1, column);
                        same = swt.getTuple().getMemberId() == prevSwt.getTuple().getMemberId();
                        setBorder(column, this, same, uncommitted);
                        if ((same) && (swt.getCellText().equals(prevSwt.getCellText()))) {
                            renderComponent.setText("");
                        }
                	} else {
                		boolean test = true;
                		if (test) {
                    		Object obj = table.getValueAt(row - 1, column);
                		}
                	}
                } else {
                    setBorder(column, this, false, uncommitted);
                }
            } else {
                renderComponent.setText(value.toString());
                setBorder(column, this, false, false);
            }
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
