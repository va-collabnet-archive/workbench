package org.ihtsdo.translation.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.workflow.api.WorkflowSearcher;
import org.ihtsdo.project.workflow.filters.WfSearchFilterBI;
import org.ihtsdo.project.workflow.model.WfInstance;

public class InboxTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -3295746462823927132L;

	private static final Integer ROW_NUMBER = 0;
	private static final Integer COMPONENT = 1;
	private static final Integer TARGET = 2;
	private static final Integer WORKLIST = 3;
	private static final Integer DESTINATION = 4;
	private static final Integer STATE = 5;

	private String[] columnNames = { "#", "Component", "Target", "Worklist", "Destination", "State" };
	private Object[][] data = { { "", "", "", "", "", "" } };
	private WorkflowSearcher searcher;
	private ArrayList<String> ids;
	private I_TermFactory tf;

	public InboxTableModel() {
		super();
		this.tf = Terms.get();
		this.searcher = new WorkflowSearcher();
	}

	public void updateTable(Object[][] data) {
		int i = 0;
		this.data = new Object[data.length][columnNames.length];
		for (Object[] objects : data) {
			Object[] row = new Object[columnNames.length];
			row[0] = i + 1;
			int j = 1;
			for (Object obj : objects) {
				row[j] = obj;
				j++;
			}
			this.data[i] = row;
			i++;
		}
		fireTableDataChanged();
	}

	public boolean updatePage(HashMap<String, WfSearchFilterBI> filterList) {
		boolean morePages = false;
		try {
			List<WfInstance> wfInstances = searcher.searchWfInstances(filterList.values());
			this.data = new Object[wfInstances.size()][columnNames.length];
			int i = 0;
			for (WfInstance wfInstance : wfInstances) {
				Object[] row = new Object[columnNames.length];
				row[ROW_NUMBER] = i + 1;
				row[COMPONENT] = tf.getConcept(wfInstance.getComponentId()).getInitialText();
				row[TARGET] = "";
				row[WORKLIST] = tf.getConcept(wfInstance.getWorkListId()).getInitialText();
				row[DESTINATION] = wfInstance.getDestination().getUsername();
				row[STATE] = wfInstance.getState().getName();
				this.data[i] = row;
				i++;
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fireTableDataChanged();
		return morePages;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		if (data != null) {
			return data.length;
		} else {
			return 0;
		}
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then the last column would
	 * contain text ("true"/"false"), rather than a check box.
	 */
	public Class getColumnClass(int c) {
		if (getValueAt(0, c) != null) {
			return getValueAt(0, c).getClass();
		} else {
			return null;
		}
	}

	/*
	 * Don't need to implement this method unless your table's editable.
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
	 * Don't need to implement this method unless your table's data can change.
	 */
	public void setValueAt(Object value, int row, int col) {
		if (col == 0) {
			data[row][col] = new Integer(row);
			fireTableCellUpdated(row, col);
		} else {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}
	}
}