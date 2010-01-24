package org.dwfa.ace.table.refset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.vodb.types.ThinExtByRefTuple;

public class SelectableReflexiveTableModel extends ReflexiveRefsetTableModel {

    private HashMap<ThinExtByRefTuple, Boolean> selectedTuplesMap = new HashMap<ThinExtByRefTuple, Boolean>();
    private boolean showCheckBoxColumn = false;

    public SelectableReflexiveTableModel(I_HostConceptPlugins host, ReflexiveRefsetFieldData[] columns) {
        super(host, columns);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {

        if (columnIndex == getColumnCount() - 1) {
            ThinExtByRefTuple tuple = allTuples.get(rowIndex);
            if (!selectedTuplesMap.containsKey(tuple)) {
                selectedTuplesMap.put(tuple, Boolean.FALSE);
            }
            return selectedTuplesMap.get(tuple);
        }

        return super.getValueAt(rowIndex, columnIndex);
    }

    public Set<ThinExtByRefTuple> getSelectedTuples() {
        HashSet<ThinExtByRefTuple> selectedTuples = new HashSet<ThinExtByRefTuple>();
        for (ThinExtByRefTuple tuple : selectedTuplesMap.keySet()) {
            if (selectedTuplesMap.get(tuple)) {
                selectedTuples.add(tuple);
            }
        }

        return selectedTuples;
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == getColumnCount() - 1) {
            ThinExtByRefTuple tuple = allTuples.get(row);
            selectedTuplesMap.put(tuple, (Boolean) value);
        } else {
            super.setValueAt(value, row, col);
        }
    }

    public void setShowPromotionCheckBoxes(boolean show) {
        showCheckBoxColumn = show;
    }

    public int getColumnCount() {
        return super.getColumnCount() + 1;
    }

    public Class getColumnClass(int column) {
        if (column == getColumnCount() - 1) {
            return Boolean.class;
        }
        return super.getColumnClass(column);
    }

    public String getColumnName(int column) {
        if (column == getColumnCount() - 1) {
            return " ";
        }
        return super.getColumnName(column);
    }

    public boolean isCellEditable(int row, int column) {
        if (column == getColumnCount() - 1) {
            return true;
        }
        return super.isCellEditable(row, column);
    }
}
