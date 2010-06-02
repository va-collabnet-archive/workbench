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
package org.dwfa.ace.classifier;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.task.classify.SnoRel;

@SuppressWarnings("serial")
public class DiffTableModel extends AbstractTableModel {
    private String[] columnNames = { "<html>" + "<font face='Dialog' size='3' color='black'>Concept 1 - </font>"
        + "<font face='Dialog' size='3' color='black'>Role Type - </font>"
        + "<font face='Dialog' size='3' color='black'>Concept 2" };

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
