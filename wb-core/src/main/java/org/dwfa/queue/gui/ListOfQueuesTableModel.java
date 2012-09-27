/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
/*
 * Created on Jan 21, 2006
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

    private String[] columnNames = { "Queue Name" };

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
        return queues.get(rowIndex).toString();
    }

    public void addQueue(QueueAdaptor q) {
        queues.add(q);
        fireTableDataChanged();
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

    public QueueAdaptor getQueueAt(int rowIndex) {
       return queues.get(rowIndex);
    }

}
