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
