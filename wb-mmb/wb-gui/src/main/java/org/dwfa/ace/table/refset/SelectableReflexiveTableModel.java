package org.dwfa.ace.table.refset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;

public class SelectableReflexiveTableModel extends ReflexiveRefsetTableModel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<Integer, Boolean> selectedTuplesMap = new HashMap<Integer, Boolean>();

    public SelectableReflexiveTableModel(I_HostConceptPlugins host, ReflexiveRefsetFieldData[] columns) {
        super(host, columns);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {

        if (columnIndex == getColumnCount() - 1) {
            int tupleId = allTuples.get(rowIndex).getMemberId();
            if (!selectedTuplesMap.containsKey(tupleId)) {
                selectedTuplesMap.put(tupleId, Boolean.FALSE);
            }
            return selectedTuplesMap.get(tupleId);
        }

        return super.getValueAt(rowIndex, columnIndex);
    }

    public Set<Integer> getSelectedTuples() {
        HashSet<Integer> selectedTuples = new HashSet<Integer>();
        for (int tupleId : selectedTuplesMap.keySet()) {
            if (selectedTuplesMap.get(tupleId)) {
                selectedTuples.add(tupleId);
            }
        }

        return selectedTuples;
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == getColumnCount() - 1) {
            int tupleId = allTuples.get(row).getMemberId();
            selectedTuplesMap.put(tupleId, (Boolean) value);
        } else {
            super.setValueAt(value, row, col);
        }
    }

    public int getColumnCount() {
        return super.getColumnCount() + 1;
    }

    public Class<?> getColumnClass(int column) {
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

    public void removeRow(int rowIndex) {
        super.removeRow(rowIndex);
    }

    public void clearSelectedTuples() {
        selectedTuplesMap.clear();
    }

    public void selectAllTuples() {
        for (I_ExtendByRefVersion tuple : allTuples) {
            int tupleId = tuple.getMemberId();
            selectedTuplesMap.put(tupleId, Boolean.TRUE);
        }
    }
}
