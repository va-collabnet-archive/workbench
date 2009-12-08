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
 * Created on Apr 28, 2005
 */
package org.dwfa.queue.gui;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeQueueEntry;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.queue.SelectAll;

public class QueueTableModel extends AbstractTableModel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private String[] columnNames = { "Name", "Subject", "Deadline", "Priority", "Originator", "Process ID", "Entry ID" };

    private Object[][] rowData;

    private List<I_DescribeQueueEntry> metaList = new ArrayList<I_DescribeQueueEntry>();

    private I_QueueProcesses queue;

    Collection<I_DescribeBusinessProcess> metaData;

    /**
     * @throws RemoteException
     * @throws IOException
     * 
     */
    public QueueTableModel(I_QueueProcesses queue) throws RemoteException, IOException {
        super();
        this.queue = queue;
        updateQueueData();
    }

    public void updateQueueData() throws RemoteException, IOException {

        Collection<I_DescribeBusinessProcess> newData = queue.getProcessMetaData(new SelectAll());
        if (metaData == null) {
            metaData = new ArrayList<I_DescribeBusinessProcess>(newData);
        } else if (newData.equals(metaData)) {
            return;
        } else {
            metaData = new ArrayList<I_DescribeBusinessProcess>(newData);
            metaList = new ArrayList<I_DescribeQueueEntry>();
        }

        this.rowData = new Object[metaData.size()][columnNames.length];
        int i = 0;
        for (Iterator<I_DescribeBusinessProcess> metaItr = metaData.iterator(); metaItr.hasNext();) {
            I_DescribeQueueEntry meta = (I_DescribeQueueEntry) metaItr.next();
            metaList.add(meta);
            rowData[i][0] = meta.getName();
            rowData[i][1] = meta.getSubject();
            if (meta.getDeadline() == null) {
                rowData[i][2] = "unspecified";
            } else if (meta.getDeadline().getTime() == Long.MAX_VALUE) {
                rowData[i][2] = "unspecified";
            } else if (meta.getDeadline().getTime() == Long.MIN_VALUE) {
                rowData[i][2] = "immediate";
            } else {
                rowData[i][2] = dateFormat.format(meta.getDeadline());
            }
            rowData[i][3] = meta.getPriority();
            rowData[i][4] = meta.getOriginator();
            rowData[i][5] = meta.getProcessID();
            rowData[i][6] = meta.getEntryID();
            i++;
        }
        fireTableDataChanged();
    }

    public I_DescribeQueueEntry getRowMetaData(int row) {
        if (row < metaList.size() && row >= 0) {
            return metaList.get(row);
        }
        return null;
    }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public int getRowCount() {
        if (rowData == null) {
            rowData = new Object[0][columnNames.length];
        }
        return rowData.length;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int row, int col) {
        return rowData[row][col];
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        rowData[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    /**
     * @return Returns the queue.
     */
    public I_QueueProcesses getQueue() {
        return queue;
    }
}
