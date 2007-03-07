/*
 * Created on Apr 28, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
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

    private SimpleDateFormat dateFormat = new SimpleDateFormat(
    "yyyy.MM.dd HH:mm:ss");

    private String[] columnNames = { "Name", "Subject", "Deadline",
            "Priority", "Originator", "Process ID" };

    private Object[][] rowData;
    
    private List<I_DescribeQueueEntry> metaList = new ArrayList<I_DescribeQueueEntry>();

    private I_QueueProcesses queue;

    /**
     * @throws RemoteException
     * @throws IOException
     *  
     */
    public QueueTableModel(I_QueueProcesses queue) throws RemoteException,
            IOException {
        super();
        this.queue = queue;
        Collection<I_DescribeBusinessProcess> metaData = queue.getProcessMetaData(new SelectAll());

        this.rowData = new Object[metaData.size()][columnNames.length];
        int i = 0;
        for (Iterator<I_DescribeBusinessProcess> metaItr = metaData.iterator(); metaItr.hasNext();) {
            I_DescribeQueueEntry meta = (I_DescribeQueueEntry) metaItr
                    .next();
            metaList.add(meta);
            rowData[i][0] = meta.getName();
            rowData[i][1] = meta.getSubject();
            rowData[i][2] = dateFormat.format(meta.getDeadline());
            rowData[i][3] = meta.getPriority();
            rowData[i][4] = meta.getOriginator();
            rowData[i][5] = meta.getProcessID();
            i++;
        }
    }
    
    public I_DescribeQueueEntry getRowMetaData(int row) {
        return metaList.get(row);
    }
    
    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public int getRowCount() {
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