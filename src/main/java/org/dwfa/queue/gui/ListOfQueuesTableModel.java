/*
 * Created on Jan 21, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.queue.gui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ListOfQueuesTableModel extends AbstractTableModel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private ArrayList<QueueAdaptor> queues = new ArrayList<QueueAdaptor>();
    
    private String[] columnNames = { "Queue Name"};

    public ListOfQueuesTableModel() {
        super();
    }

    public int getRowCount() {
        return queues.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if ((rowIndex < 0) || (rowIndex >= queues.size())) {
            return null;
        }
        return queues.get(rowIndex);
    }
    public void addQueue(QueueAdaptor q) {
        queues.add(q);
        fireTableCellUpdated(queues.size() - 1, 0);
    }
    
    public String getColumnName(int col) {
        return columnNames[col].toString();
    }
    public Class<?> getColumnClass(int c) {
        return String.class;
    }
    
    public void clear() {
        queues.clear();
        fireTableDataChanged();
    }
    
    public boolean isCellEditable(int row, int col) {
        return false;
    }

}
