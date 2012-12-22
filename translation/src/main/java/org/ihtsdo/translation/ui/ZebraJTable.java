/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation.ui;

import java.awt.Color;

import javax.swing.JTable;

/**
 * A JTable that draws a zebra striped background.
 */
public class ZebraJTable extends javax.swing.JTable {

	/** The row colors. */
	private java.awt.Color rowColors[] = new java.awt.Color[2];

	/** The draw stripes. */
	private boolean drawStripes = false;

	/**
	 * Instantiates a new zebra j table.
	 */
	public ZebraJTable() {
	}

	/**
	 * Instantiates a new zebra j table.
	 * 
	 * @param numRows
	 *            the num rows
	 * @param numColumns
	 *            the num columns
	 */
	public ZebraJTable(int numRows, int numColumns) {
		super(numRows, numColumns);
	}

	/**
	 * Instantiates a new zebra j table.
	 * 
	 * @param rowData
	 *            the row data
	 * @param columnNames
	 *            the column names
	 */
	public ZebraJTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
	}

	/**
	 * Instantiates a new zebra j table.
	 * 
	 * @param dataModel
	 *            the data model
	 */
	public ZebraJTable(javax.swing.table.TableModel dataModel) {
		super(dataModel);
	}

	/**
	 * Instantiates a new zebra j table.
	 * 
	 * @param dataModel
	 *            the data model
	 * @param columnModel
	 *            the column model
	 */
	public ZebraJTable(javax.swing.table.TableModel dataModel, javax.swing.table.TableColumnModel columnModel) {
		super(dataModel, columnModel);
	}

	/**
	 * Instantiates a new zebra j table.
	 * 
	 * @param dataModel
	 *            the data model
	 * @param columnModel
	 *            the column model
	 * @param selectionModel
	 *            the selection model
	 */
	public ZebraJTable(javax.swing.table.TableModel dataModel, javax.swing.table.TableColumnModel columnModel, javax.swing.ListSelectionModel selectionModel) {
		super(dataModel, columnModel, selectionModel);
	}

	/**
	 * Instantiates a new zebra j table.
	 * 
	 * @param rowData
	 *            the row data
	 * @param columnNames
	 *            the column names
	 */
	public ZebraJTable(java.util.Vector<?> rowData, java.util.Vector<?> columnNames) {
		super(rowData, columnNames);
	}

	/**
	 * Add stripes between cells and behind non-opaque cells.
	 * 
	 * @param g
	 *            the g
	 */
	public void paintComponent(java.awt.Graphics g) {
		if (!(drawStripes = isOpaque())) {
			super.paintComponent(g);
			return;
		}

		// Paint zebra background stripes
		updateZebraColors();
		final java.awt.Insets insets = getInsets();
		final int w = getWidth() - insets.left - insets.right;
		final int h = getHeight() - insets.top - insets.bottom;
		final int x = insets.left;
		int y = insets.top;
		int rowHeight = 16; // A default for empty tables
		final int nItems = getRowCount();
		for (int i = 0; i < nItems; i++, y += rowHeight) {
			rowHeight = getRowHeight(i);
			g.setColor(rowColors[i & 1]);
			g.fillRect(x, y, w, rowHeight);
		}
		// Use last row height for remainder of table area
		final int nRows = nItems + (insets.top + h - y) / rowHeight;
		for (int i = nItems; i < nRows; i++, y += rowHeight) {
			g.setColor(rowColors[i & 1]);
			g.fillRect(x, y, w, rowHeight);
		}
		final int remainder = insets.top + h - y;
		if (remainder > 0) {
			g.setColor(rowColors[nRows & 1]);
			g.fillRect(x, y, w, remainder);
		}

		// Paint component
		setOpaque(false);
		try {
			super.paintComponent(g);
		} catch (Exception e) {
		}
		setOpaque(true);
	}

	/**
	 * Add background stripes behind rendered cells.
	 * 
	 * @param renderer
	 *            the renderer
	 * @param row
	 *            the row
	 * @param col
	 *            the col
	 * @return the java.awt. component
	 */
	public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
		try {
			final java.awt.Component c = super.prepareRenderer(renderer, row, col);
			if (drawStripes && !isCellSelected(row, col)) {
				if (c.getBackground().equals(Color.GREEN)) {
					c.setBackground(Color.GREEN);
				} else {
					c.setBackground(rowColors[row & 1]);
				}
			}
			return c;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Add background stripes behind edited cells.
	 * 
	 * @param editor
	 *            the editor
	 * @param row
	 *            the row
	 * @param col
	 *            the col
	 * @return the java.awt. component
	 */
	public java.awt.Component prepareEditor(javax.swing.table.TableCellEditor editor, int row, int col) {
		final java.awt.Component c = super.prepareEditor(editor, row, col);
		if (drawStripes && !isCellSelected(row, col)) {
			if (c.getBackground().equals(Color.GREEN)) {
				c.setBackground(Color.GREEN);
			} else {
				c.setBackground(rowColors[row & 1]);
			}
		}
		JTable table = new JTable();
		return c;
	}

	/**
	 * Force the table to fill the viewport's height.
	 * 
	 * @return the scrollable tracks viewport height
	 */
	public boolean getScrollableTracksViewportHeight() {
		final java.awt.Component p = getParent();
		if (!(p instanceof javax.swing.JViewport))
			return false;
		return ((javax.swing.JViewport) p).getHeight() > getPreferredSize().height;
	}

	/** Compute zebra background stripe colors. */
	private void updateZebraColors() {
		if ((rowColors[0] = getBackground()) == null) {
			rowColors[0] = rowColors[1] = java.awt.Color.white;
			return;
		}
		final java.awt.Color sel = getSelectionBackground();
		if (sel == null) {
			rowColors[1] = rowColors[0];
			return;
		}
		final float[] bgHSB = java.awt.Color.RGBtoHSB(rowColors[0].getRed(), rowColors[0].getGreen(), rowColors[0].getBlue(), null);
		final float[] selHSB = java.awt.Color.RGBtoHSB(sel.getRed(), sel.getGreen(), sel.getBlue(), null);
		rowColors[1] = java.awt.Color.getHSBColor((selHSB[1] == 0.0 || selHSB[2] == 0.0) ? bgHSB[0] : selHSB[0], 0.1f * selHSB[1] + 0.9f * bgHSB[1], bgHSB[2] + ((bgHSB[2] < 0.5f) ? 0.05f : -0.05f));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JTable#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int column) {
		// System.out.println("ZebraJTable rowcount: " + getRowCount() +
		// " Row: " + row);
		try {
			if (row < getRowCount()) {
				return super.getValueAt(row, column);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// Do Nothing
		}
		return null;
	}
}
