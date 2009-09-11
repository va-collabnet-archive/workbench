package org.dwfa.ace.table.forms;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class FormsTableModel extends AbstractTableModel {
    private String[] columnNames = {
            "SF",
            "I",
            "DN",
            "AN",
            "SC",
            "LC",
            "<html><font face='Dialog' size='3' color='blue'>Type: </font>"
                    + "<font face='Dialog' size='3' color='green'>Value" };

    private Object[][] data;

    public FormsTableModel(Object[][] data) {
        super();
        this.data = data;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public int getRowCount() {
        return data.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    // Override default cell behavior
    public boolean isCellEditable(int row, int col) {
        return false;
    }

}
