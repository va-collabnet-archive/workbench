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
 * Created on May 21, 2005
 */
package org.dwfa.bpa.worker;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * @author kec
 * 
 */
public class WorkerTableModel extends AbstractTableModel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final int NAME = 0;
    public static final int STATUS = 1;
    public static final int TASK = 2;
    public static final int TASKS_COMPLETE = 3;
    public static final int HOST_NAME = 4;
    public static final int HOST_IP = 5;
    public static final int INSTANCE_ID = 6;
    private String[] columnNames = { "Name", "Status", "Task", "Completed", "Host Name", "Host IP", "Instance Id" };

    private Vector<GenericWorker> workers;

    /**
     * @throws RemoteException
     * @throws IOException
     * 
     */
    public WorkerTableModel(Vector<GenericWorker> workers) throws RemoteException, IOException {
        super();
        this.workers = workers;
    }

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public int getRowCount() {
        return workers.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int row, int col) {
        GenericWorker worker = workers.get(row);
        switch (col) {
        case NAME:
            return worker.getWorkerDesc();
        case STATUS:
            return worker.getStatus();
        case TASK:
            if (worker.getCurrentTask() != null) {
                return worker.getCurrentTask().getName();
            }
            return "None";
        case TASKS_COMPLETE:
            return new Integer(worker.getTasksCompleted());
        case HOST_NAME:
            return worker.getHost().getHostName();// Host name;
        case HOST_IP:
            return worker.getHost().getHostAddress();// Host ip;
        case INSTANCE_ID:
            return worker.getId();
        }
        return "Unknown column: " + col;
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        throw new UnsupportedOperationException();
        // fireTableCellUpdated(row, col);
    }

    public Class<?> getColumnClass(int c) {
        return String.class;
    }

}
