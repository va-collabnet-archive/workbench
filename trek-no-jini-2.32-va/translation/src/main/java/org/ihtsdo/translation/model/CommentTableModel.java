package org.ihtsdo.translation.model;

import java.util.LinkedList;

import javax.swing.table.DefaultTableModel;

public class CommentTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;
	protected String[] columns = { "Comment type", "Comment", "CommentObject" };
	private LinkedList<Object[]> data = new LinkedList<Object[]>();
	public boolean isCellEditable(int x, int y) {
		return false;
	}
	
	@Override
	public void addRow(Object[] rowData) {
		data.add(rowData);
		setColumnIdentifiers(columns);
		fireTableDataChanged();
	}
	
	/**
	 * Gets the row.
	 *
	 * @param rowNum the row num
	 * @return the row
	 */
	public Object[] getRow(int rowNum) {
		return data.get(rowNum);
	}
	

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getRowCount()
	 */
	public int getRowCount() {
		if (data != null && data.size() > 0) {
			return data.size();
		} else {
			return 0;
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		return columns[col];
	}
	
	/**
	 * Clear table.
	 */
	public void clearTable() {
		data = new LinkedList<Object[]>();
		fireTableDataChanged();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		if (data != null && !data.isEmpty()) {
			return data.get(row)[col];
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#removeRow(int)
	 */
	@Override
	public void removeRow(int row) {
		data.remove(row);
		fireTableRowsDeleted(row, row);
	}

	@Override
	public int getColumnCount() {
		return columns.length - 1;
	}

	public Class getColumnClass(int column) {
		Class returnValue;
		if ((column >= 0) && (column < getColumnCount()) && getRowCount() > 0) {
			returnValue = getValueAt(0, column).getClass();
		} else {
			returnValue = Object.class;
		}
		return returnValue;
	}
	
	/*
	 * Don't need to implement this method unless your table's data can change.
	 */
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object value, int row, int col) {
		data.get(row)[col] = value;
		fireTableCellUpdated(row, col);
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableModel#setRowCount(int)
	 */
	@Override
	public void setRowCount(int rowCount) {
		super.setRowCount(data.size());
	}
}