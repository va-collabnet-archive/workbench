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

import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.task.classify.SnoConGrp;
import org.dwfa.ace.task.classify.SnoConGrpList;

@SuppressWarnings("serial")
public class EquivTableModel extends AbstractTableModel {
    private String[] columnNames = { "#", "<html><font face='Dialog' size='3' color='black'>Concept" };

    private Object[][] data;
    private SnoConGrpList dataList;

    public EquivTableModel(Object[][] data, SnoConGrpList snoConGrpList) {
        super();
        this.data = data;
        this.dataList = snoConGrpList;
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
        if (rowIndex < 0 || dataList == null)
            return Integer.MIN_VALUE;

        // find the containing group or last group
        int maxGroups = dataList.size();
        int g = 0; // concept group index
        int i = -1; // concept index within group
        boolean found = false;

        SnoConGrp scg = null;
        while (g < maxGroups && !found) {
            scg = dataList.get(g);
            int maxCon = scg.size();

            if (i + maxCon >= rowIndex) {
                found = true;
            } else {
                g += 1;
                i += maxCon;
            }
        }

        if (found) {
            return scg.get(rowIndex - i - 1).id;
        } else
            return -1;
    }

    // Override default cell behavior
    public boolean isCellEditable(int row, int col) {
        return false;
    }

}
