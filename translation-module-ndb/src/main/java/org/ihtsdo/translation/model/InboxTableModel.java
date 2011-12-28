package org.ihtsdo.translation.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.model.WorkSetMember;
import org.ihtsdo.project.workflow.api.WorkflowSearcher;
import org.ihtsdo.project.workflow.filters.WfSearchFilterBI;
import org.ihtsdo.project.workflow.model.WfInstance;

public class InboxTableModel extends DefaultTableModel {

	private static final long serialVersionUID = -3295746462823927132L;

	public static final Integer COMPONENT = 0;
	public static final Integer TARGET = 1;
	public static final Integer WORKLIST = 2;
	public static final Integer DESTINATION = 3;
	public static final Integer STATE = 4;
	public static final Integer WORKFLOW_ITEM = 5;

	private static String[] columnNames = { "Component", "Target", "Worklist", "Destination", "State", "wf item" };
	private LinkedList<Object[]> data = new LinkedList<Object[]>();
	private WorkflowSearcher searcher;
	private ArrayList<String> ids;
	private I_TermFactory tf;
	private JProgressBar pBar;

	private InboxWorker inboxWorker;

	public InboxTableModel(JProgressBar pBar) {
		super();
		this.tf = Terms.get();
		this.pBar = pBar;
		this.searcher = new WorkflowSearcher();
	}

	public void sortArray(int col, boolean ascending) {
		Collections.sort(data, new ArrayComparator(col, ascending));
		System.out.println("data sorted");
		for (Object[] d : data) {
			System.out.println(d[0] + " " + d[1] + " " + d[2] + " " + d[3] + " " + d[4]);
		}
	}

	public Object[] getRow(int rowNum) {
		return data.get(rowNum);
	}

	@Override
	public void addRow(Object[] rowData) {
		data.add(rowData);
		fireTableDataChanged();
	}

	public void updateTable(Object[][] data) {
		int i = 0;
		this.data = new LinkedList<Object[]>();
		for (Object[] objects : data) {
			Object[] row = new Object[columnNames.length];
			row[0] = i + 1;
			int j = 1;
			for (Object obj : objects) {
				row[j] = obj;
				j++;
			}
			this.data.add(row);
			i++;
		}
		fireTableDataChanged();
	}

	public boolean updatePage(HashMap<String, WfSearchFilterBI> filterList) {
		boolean morePages = false;
		if (inboxWorker != null && !inboxWorker.isDone()) {
			inboxWorker.cancel(true);
			inboxWorker = null;
		}
		inboxWorker = new InboxWorker(filterList);
		inboxWorker.addPropertyChangeListener(new ProgressListener(pBar));
		inboxWorker.execute();
		return morePages;
	}

	public int getColumnCount() {
		return columnNames.length - 1;
	}

	public int getRowCount() {
		if (data != null && data.size() > 0) {
			return data.size();
		} else {
			return 0;
		}
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		return data.get(row)[col];
	}

	@Override
	public void removeRow(int row) {
		data.remove(row);
		fireTableRowsDeleted(row, row);
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
		data.get(row)[col] = value;
		fireTableCellUpdated(row, col);
	}

	@Override
	public void setRowCount(int rowCount) {
		super.setRowCount(data.size());
	}

	class InboxWorker extends SwingWorker<List<WfInstance>, WorkSetMember> {
		private HashMap<String, WfSearchFilterBI> filterList;
		private ExecutorService executor;

		public InboxWorker(HashMap<String, WfSearchFilterBI> filterList) {
			super();
			this.filterList = filterList;
		}

		@Override
		protected List<WfInstance> doInBackground() throws Exception {
			List<WfInstance> wfInstances = new ArrayList<WfInstance>();
			executor = Executors.newFixedThreadPool(1);
			FutureTask<List<WfInstance>> future = new FutureTask<List<WfInstance>>(new Callable<List<WfInstance>>() {
				@Override
				public List<WfInstance> call() throws Exception {
					return searcher.searchWfInstances(filterList.values());
				}
			});
			executor.execute(future);
			try {
				// try every 10 seconds
				while (!future.isDone()) {
					System.out.println("Task not yet completed.");
					Thread.sleep(500);
				}
				if (!future.isCancelled()) {
					wfInstances = future.get();
				}
			} catch (CancellationException e) {
			}
			executor.shutdown();
			return wfInstances;
		}

		@Override
		public void done() {
			List<WfInstance> wfInstances = null;
			try {
				wfInstances = get();
				if (!isCancelled()) {
					data = new LinkedList<Object[]>();
					int i = 0;
					for (WfInstance wfInstance : wfInstances) {
						Object[] row = new Object[columnNames.length];
						row[COMPONENT] = tf.getConcept(wfInstance.getComponentId()).getInitialText();
						row[TARGET] = "";
						row[WORKLIST] = tf.getConcept(wfInstance.getWorkListId()).getInitialText();
						row[DESTINATION] = wfInstance.getDestination().getUsername();
						row[STATE] = wfInstance.getState().getName();
						row[WORKFLOW_ITEM] = wfInstance;
						data.add(row);
						i++;
					}
					fireTableDataChanged();
				}
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
		}

	};
}

class ProgressListener implements PropertyChangeListener {
	// Prevent creation without providing a progress bar.
	private ProgressListener() {
	}

	public ProgressListener(JProgressBar progressBar) {
		this.progressBar = progressBar;
		this.progressBar.setVisible(true);
		this.progressBar.setIndeterminate(true);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
			progressBar.setIndeterminate(false);
			progressBar.setVisible(false);
		}
	}

	private JProgressBar progressBar;
}

class ArrayComparator implements Comparator<Object[]> {
	private final int columnToSort;
	private final boolean ascending;

	public ArrayComparator(int columnToSort, boolean ascending) {
		this.columnToSort = columnToSort;
		this.ascending = ascending;
	}

	public int compare(Object[] c1, Object[] c2) {
		return ascending ? c1[columnToSort].toString().compareTo(c2[columnToSort].toString()) : c1[columnToSort].toString().compareTo(c2[columnToSort].toString()) * -1;
	}
}
