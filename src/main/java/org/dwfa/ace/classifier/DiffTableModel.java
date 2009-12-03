package org.dwfa.ace.classifier;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.task.classify.SnoRel;

@SuppressWarnings("serial")
public class DiffTableModel extends AbstractTableModel {
    private String[] columnNames = { "<html>"
            + "<font face='Dialog' size='3' color='green'>Concept 1 - </font>"
            + "<font face='Dialog' size='3' color='blue'>Role Type - </font>"
            + "<font face='Dialog' size='3' color='green'>Concept 2" };

    private Object[][] data;
    private ArrayList<SnoRel> srl;

    public DiffTableModel(Object[][] data, ArrayList<SnoRel> list) {
        super();
        this.data = data;
        this.srl = list;
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

    public int getNidAt(int rowIndex, int columnIndex) {
        if (srl != null && rowIndex >= 0 && rowIndex < srl.size())
            return srl.get(rowIndex).c1Id;

        return Integer.MIN_VALUE;
    }

    // Override default cell behavior
    public boolean isCellEditable(int row, int col) {
        return false;
    }

}
