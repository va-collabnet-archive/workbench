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

@SuppressWarnings("serial")
public class DiffReportTableModel extends AbstractTableModel {
    private String[] columnNames = {" ", "source", "type", "destination", "group"};

    private Object[][] data;
    private ArrayList<SnoRelReport> srl;

    public DiffReportTableModel(Object[][] data, ArrayList<SnoRelReport> list) {
        super();
        this.data = data;
        this.srl = list;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    public int getNidAt(int rowIndex, int columnIndex) {
        if (srl != null && rowIndex == 0 && rowIndex < srl.size()) {
            return srl.get(rowIndex).snoRel.c1Id;
        } else if (srl != null && rowIndex == 1 && rowIndex < srl.size()) {
            return srl.get(rowIndex).snoRel.c1Id;
        } else if (srl != null && rowIndex == 2 && rowIndex < srl.size()) {
            return srl.get(rowIndex).snoRel.typeId;
        } else if (srl != null && rowIndex == 3 && rowIndex < srl.size()) {
            return srl.get(rowIndex).snoRel.c2Id;
        } else if (srl != null && rowIndex == 4 && rowIndex < srl.size()) {
            return srl.get(rowIndex).snoRel.c1Id;
        }

        return Integer.MIN_VALUE;
    }

    // Override default cell behavior
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

}
