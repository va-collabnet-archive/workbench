package org.ihtsdo.project.view.details;

/** 
 * @(#)TextAreaRenderer.java 
 */

import java.awt.Component;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * The Class TextAreaRenderer.
 */

public class TextAreaRenderer extends JTextArea implements TableCellRenderer {

	/** The renderer. */
	protected final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

	// Column heights are placed in this Map
	/** The tablecell sizes. */
	protected final Map<JTable, Map<Object, Map<Object, Integer>>> tablecellSizes = new HashMap<JTable, Map<Object, Map<Object, Integer>>>();

	/**
	 * Instantiates a new text area renderer.
	 */
	public TextAreaRenderer() {
		setLineWrap(true);
		setWrapStyleWord(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
	 * .swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		// set the Font, Color, etc.
		renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setBackground(renderer.getBackground());
		setForeground(renderer.getForeground());
		setBorder(renderer.getBorder());
		setFont(renderer.getFont());
		setText(renderer.getText());

		if (value != null && !value.toString().equals("") && value.toString().contains("\n")) {
			int newlines = value.toString().split("\n").length;
			table.setRowHeight(row, newlines*30);
		}else{
			table.setRowHeight(30);
		}

		return this;
	}

	/**
	 * Adds the size.
	 * 
	 * @param table
	 *            the table
	 * @param row
	 *            the row
	 * @param column
	 *            the column
	 * @param height
	 *            the height
	 */
	protected void addSize(JTable table, int row, int column, int height) {
		Map<Object, Map<Object, Integer>> rowsMap = tablecellSizes.get(table);
		if (rowsMap == null) {
			tablecellSizes.put(table, rowsMap = new HashMap<Object, Map<Object, Integer>>());
		}
		Map<Object, Integer> rowheightsMap = rowsMap.get(row);
		if (rowheightsMap == null) {
			rowsMap.put(row, rowheightsMap = new HashMap<Object, Integer>());
		}
		rowheightsMap.put(column, height);
	}

	/**
	 * Find total maximum row size.
	 * 
	 * @param table
	 *            the table
	 * @param row
	 *            the row
	 * 
	 * @return the int
	 */
	protected int findTotalMaximumRowSize(JTable table, int row) {
		int maximum_height = 0;
		Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			TableColumn tc = columns.nextElement();
			TableCellRenderer cellRenderer = tc.getCellRenderer();
			if (cellRenderer instanceof TextAreaRenderer) {
				TextAreaRenderer tar = (TextAreaRenderer) cellRenderer;
				maximum_height = Math.max(maximum_height, tar.findMaximumRowSize(table, row));
			}
		}
		return maximum_height;
	}

	/**
	 * Find maximum row size.
	 * 
	 * @param table
	 *            the table
	 * @param row
	 *            the row
	 * 
	 * @return the int
	 */
	protected int findMaximumRowSize(JTable table, int row) {
		Map<Object, Map<Object, Integer>> rows = tablecellSizes.get(table);
		if (rows == null)
			return 0;
		Map<Object, Integer> rowheights = rows.get(row);
		if (rowheights == null)
			return 0;
		int maximum_height = 0;
		for (Map.Entry<Object, Integer> entry : rowheights.entrySet()) {
			int cellHeight = entry.getValue();
			maximum_height = Math.max(maximum_height, cellHeight);
		}
		return maximum_height;
	}
}
