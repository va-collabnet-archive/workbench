/*
 * Created on Apr 28, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;

import javax.swing.table.AbstractTableModel;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_RecordExecution;




public class ExecutionRecordTableModel extends AbstractTableModel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


    private String[] columnNames = { "Task", "Task Name", "Date",
            "Worker Description", "Worker ID", "Condition" };

    private I_RecordExecution[] rowData;

    private I_EncodeBusinessProcess process;

    /**
     * @throws RemoteException
     * @throws IOException
     *  
     */
    public ExecutionRecordTableModel(I_EncodeBusinessProcess process)  {
        super();
        this.process = process;
        rowData = (I_RecordExecution[]) process.getExecutionRecords().toArray(new I_RecordExecution[process.getExecutionRecords().size()]);
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
        switch (col) {
            case 0: 
                return Integer.toString(rowData[row].getTaskId());
            case 1:
                return this.process.getTask(rowData[row].getTaskId()).getName();
            case 2:
                return dateFormat.format(rowData[row].getDate());
            case 3:
                return rowData[row].getWorkerDesc();
            case 4:
                return rowData[row].getWorkerId().toString();
            case 5:
                return rowData[row].getCondition().toString();
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        throw new UnsupportedOperationException();
    }

    public Class<?> getColumnClass(int c) {
        return String.class;
    }


}