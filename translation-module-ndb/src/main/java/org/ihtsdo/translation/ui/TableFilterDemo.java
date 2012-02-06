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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

/**
 * The Class TableFilterDemo.
 */
public class TableFilterDemo extends JPanel {
	
	/** The DEBUG. */
	private boolean DEBUG = false;
	
	/** The table. */
	private JTable table;
	
	/** The filter text. */
	private JTextField filterText;
	
	/** The status text. */
	private JTextField statusText;
	
	/** The sorter. */
	private TableRowSorter<MyTableModel> sorter;

	/**
	 * Instantiates a new table filter demo.
	 */
	public TableFilterDemo() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Create a table with a sorter.
		MyTableModel model = new MyTableModel();
		sorter = new TableRowSorter<MyTableModel>(model);
		table = new JTable(model);
		table.setRowSorter(sorter);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);

		// For the purposes of this example, better to have a single
		// selection.
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// When selection changes, provide user with row numbers for
		// both view and model.
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				int viewRow = table.getSelectedRow();
				if (viewRow < 0) {
					// Selection got filtered away.
					statusText.setText("");
				} else {
					int modelRow = table.convertRowIndexToModel(viewRow);
					statusText.setText(String.format("Selected Row in view: %d. " + "Selected Row in model: %d.", viewRow, modelRow));
				}
			}
		});

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);

		// Add the scroll pane to this panel.
		add(scrollPane);

		// Create a separate form for filterText and statusText
		JPanel form = new JPanel(new SpringLayout());
		JLabel l1 = new JLabel("Filter Text:", SwingConstants.TRAILING);
		form.add(l1);
		filterText = new JTextField();
		// Whenever filterText changes, invoke newFilter.
		filterText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				newFilter();
			}

			public void insertUpdate(DocumentEvent e) {
				newFilter();
			}

			public void removeUpdate(DocumentEvent e) {
				newFilter();
			}
		});
		l1.setLabelFor(filterText);
		form.add(filterText);
		JLabel l2 = new JLabel("Status:", SwingConstants.TRAILING);
		form.add(l2);
		statusText = new JTextField();
		l2.setLabelFor(statusText);
		form.add(statusText);
		SpringUtilities.makeCompactGrid(form, 2, 2, 6, 6, 6, 6);
		add(form);
	}

	/**
	 * Update the row filter regular expression from the expression in the text
	 * box.
	 */
	private void newFilter() {
		RowFilter<MyTableModel, Object> rf = null;
		RowFilter<MyTableModel, Object> rf2 = null;
		// If current expression doesn't parse, don't update.
		try {
			rf = RowFilter.regexFilter(filterText.getText(), 0);
			rf2= RowFilter.regexFilter(filterText.getText(), 1);
			List<RowFilter<MyTableModel, Object>> filters = new ArrayList<RowFilter<MyTableModel,Object>>();
			filters.add(rf);
			filters.add(rf2);
			rf.andFilter(filters);
		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}
		sorter.setRowFilter(rf);
		sorter.setRowFilter(rf2);
	}

	/**
	 * The Class MyTableModel.
	 */
	class MyTableModel extends AbstractTableModel {
		
		/** The column names. */
		private String[] columnNames = { "First Name", "Last Name", "Sport", "# of Years", "Vegetarian" };
		
		/** The data. */
		private Object[][] data = { { "Mary", "Campione", "Snowboarding", new Integer(5), new Boolean(false) }, { "Alison", "Huml", "Rowing", new Integer(3), new Boolean(true) },
				{ "Kathy", "Walrath", "Knitting", new Integer(2), new Boolean(false) }, { "Sharon", "Zakhour", "Speed reading", new Integer(20), new Boolean(true) },
				{ "Philip", "Milne", "Pool", new Integer(10), new Boolean(false) }, };

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return columnNames.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return data.length;
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		public String getColumnName(int col) {
			return columnNames[col];
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col < 2) {
				return false;
			} else {
				return true;
			}
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		/* (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
		 */
		public void setValueAt(Object value, int row, int col) {
			if (DEBUG) {
				System.out.println("Setting value at " + row + "," + col + " to " + value + " (an instance of " + value.getClass() + ")");
			}

			data[row][col] = value;
			fireTableCellUpdated(row, col);

			if (DEBUG) {
				System.out.println("New value of data:");
				printDebugData();
			}
		}

		/**
		 * Prints the debug data.
		 */
		private void printDebugData() {
			int numRows = getRowCount();
			int numCols = getColumnCount();

			for (int i = 0; i < numRows; i++) {
				System.out.print("    row " + i + ":");
				for (int j = 0; j < numCols; j++) {
					System.out.print("  " + data[i][j]);
				}
				System.out.println();
			}
			System.out.println("--------------------------");
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("TableFilterDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		TableFilterDemo newContentPane = new TableFilterDemo();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}

class SpringUtilities {
	/**
	 * A debugging utility that prints to stdout the component's minimum,
	 * preferred, and maximum sizes.
	 */
	public static void printSizes(Component c) {
		System.out.println("minimumSize = " + c.getMinimumSize());
		System.out.println("preferredSize = " + c.getPreferredSize());
		System.out.println("maximumSize = " + c.getMaximumSize());
	}

	/**
	 * Aligns the first <code>rows</code> * <code>cols</code> components of
	 * <code>parent</code> in a grid. Each component is as big as the maximum
	 * preferred width and height of the components. The parent is made just big
	 * enough to fit them all.
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param initialX
	 *            x location to start the grid at
	 * @param initialY
	 *            y location to start the grid at
	 * @param xPad
	 *            x padding between cells
	 * @param yPad
	 *            y padding between cells
	 */
	public static void makeGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			System.err.println("The first argument to makeGrid must use SpringLayout.");
			return;
		}

		Spring xPadSpring = Spring.constant(xPad);
		Spring yPadSpring = Spring.constant(yPad);
		Spring initialXSpring = Spring.constant(initialX);
		Spring initialYSpring = Spring.constant(initialY);
		int max = rows * cols;

		// Calculate Springs that are the max of the width/height so that all
		// cells have the same size.
		Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
		Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
		for (int i = 1; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));

			maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
			maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
		}

		// Apply the new width/height Spring. This forces all the
		// components to have the same size.
		for (int i = 0; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));

			cons.setWidth(maxWidthSpring);
			cons.setHeight(maxHeightSpring);
		}

		// Then adjust the x/y constraints of all the cells so that they
		// are aligned in a grid.
		SpringLayout.Constraints lastCons = null;
		SpringLayout.Constraints lastRowCons = null;
		for (int i = 0; i < max; i++) {
			SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
			if (i % cols == 0) { // start of new row
				lastRowCons = lastCons;
				cons.setX(initialXSpring);
			} else { // x position depends on previous component
				cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST), xPadSpring));
			}

			if (i / cols == 0) { // first row
				cons.setY(initialYSpring);
			} else { // y position depends on previous row
				cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH), yPadSpring));
			}
			lastCons = cons;
		}

		// Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, Spring.sum(Spring.constant(yPad), lastCons.getConstraint(SpringLayout.SOUTH)));
		pCons.setConstraint(SpringLayout.EAST, Spring.sum(Spring.constant(xPad), lastCons.getConstraint(SpringLayout.EAST)));
	}

	/* Used by makeCompactGrid. */
	private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
		SpringLayout layout = (SpringLayout) parent.getLayout();
		Component c = parent.getComponent(row * cols + col);
		return layout.getConstraints(c);
	}

	/**
	 * Aligns the first <code>rows</code> * <code>cols</code> components of
	 * <code>parent</code> in a grid. Each component in a column is as wide as
	 * the maximum preferred width of the components in that column; height is
	 * similarly determined for each row. The parent is made just big enough to
	 * fit them all.
	 * 
	 * @param rows
	 *            number of rows
	 * @param cols
	 *            number of columns
	 * @param initialX
	 *            x location to start the grid at
	 * @param initialY
	 *            y location to start the grid at
	 * @param xPad
	 *            x padding between cells
	 * @param yPad
	 *            y padding between cells
	 */
	public static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
		SpringLayout layout;
		try {
			layout = (SpringLayout) parent.getLayout();
		} catch (ClassCastException exc) {
			System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
			return;
		}

		// Align all cells in each column and make them the same width.
		Spring x = Spring.constant(initialX);
		for (int c = 0; c < cols; c++) {
			Spring width = Spring.constant(0);
			for (int r = 0; r < rows; r++) {
				width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
			}
			for (int r = 0; r < rows; r++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
		}

		// Align all cells in each row and make them the same height.
		Spring y = Spring.constant(initialY);
		for (int r = 0; r < rows; r++) {
			Spring height = Spring.constant(0);
			for (int c = 0; c < cols; c++) {
				height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
			}
			for (int c = 0; c < cols; c++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
		}

		// Set the parent's size.
		SpringLayout.Constraints pCons = layout.getConstraints(parent);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}
}