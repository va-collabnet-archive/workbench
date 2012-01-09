package org.ihtsdo.translation.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowSearcher;
import org.ihtsdo.project.workflow.filters.WfSearchFilterBI;
import org.ihtsdo.project.workflow.filters.WfTagFilter;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.tag.InboxTag;
import org.ihtsdo.project.workflow.tag.TagManager;

public class InboxTableModel extends DefaultTableModel {

	private static final long serialVersionUID = -3295746462823927132L;

	public static final Integer COMPONENT = 0;
	public static final Integer TARGET = 1;
	public static final Integer WORKLIST = 2;
	public static final Integer DESTINATION = 3;
	public static final Integer STATE = 4;
	public static final Integer WORKFLOW_ITEM = 5;

	private static String[] columnNames = { "Component", "Target", "Worklist", "Destination", "State", "wf item" };

	private int columnCount = columnNames.length - 1;
	private LinkedList<Object[]> data = new LinkedList<Object[]>();
	private WorkflowSearcher searcher;
	private JProgressBar pBar;
	private HashMap<String, InboxTag> tagCache = new HashMap<String, InboxTag>();

	private InboxWorker inboxWorker;

	public List<InboxTag> tags;

	public InboxTag getTagByUuid(String uuid) {
		return tagCache.get(uuid);
	}

	public void addTagToCache(String uuid, InboxTag tag) {
		tagCache.put(uuid, tag);
	}

	public void removeTagFromCache(String uuid) {
		tagCache.remove(uuid);
	}

	public InboxTableModel(JProgressBar pBar) {
		super();
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

	public int getRealColumnSize() {
		return columnNames.length - 1;
	}

	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
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

	class InboxWorker extends SwingWorker<List<WfInstance>, WfInstance> {
		private HashMap<String, WfSearchFilterBI> filterList;
		private boolean specialTag;

		public InboxWorker(HashMap<String, WfSearchFilterBI> filterList) {
			super();
			this.filterList = filterList;
			Set<String> keys = filterList.keySet();
			for (String key : keys) {
				WfSearchFilterBI filter = filterList.get(key);
				if (filter instanceof WfTagFilter) {
					WfTagFilter tf = (WfTagFilter) filter;
					if (tf.getTag().getTagName().equals(TagManager.OUTBOX) || tf.getTag().getTagName().equals(TagManager.TODO)) {
						specialTag = true;
					} else {
						specialTag = false;
					}
				}
			}
		}

		@Override
		protected List<WfInstance> doInBackground() throws Exception {
			List<WfInstance> wfInstances = new ArrayList<WfInstance>();
			wfInstances = searcher.searchWfInstances(filterList.values());

			tags = TagManager.getInstance().getAllTagsContent();
			for (WfInstance wfInstance : wfInstances) {
				publish(wfInstance);
			}
			return wfInstances;
		}

		@Override
		protected void process(List<WfInstance> wfInstances) {
			try {
				if (!isCancelled()) {
					data = new LinkedList<Object[]>();
					for (WfInstance wfInstance : wfInstances) {
						Object[] row = createRow(wfInstance, specialTag);
						if (row != null) {
							data.add(row);
						}
					}
					fireTableDataChanged();
				}
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
		}

		@Override
		public void done() {
			try {
				get();
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
		}

	}

	public void updateRow(Object[] currentRow, int modelRowNum, boolean specialTag) {
		Object[] rowUpdated = null;
		WfInstance wfInstance = (WfInstance) currentRow[currentRow.length - 1];
		WfInstance wfInstanceUpdated = WfComponentProvider.getWfInstance(wfInstance.getComponentId());
		rowUpdated = createRow(wfInstanceUpdated, specialTag);
		data.remove(modelRowNum);
		if (rowUpdated != null) {
			data.add(modelRowNum, rowUpdated);
		}
		fireTableDataChanged();
	}

	private Object[] createRow(WfInstance wfInstance, boolean specialTag) {
		Object[] row = null;
		String tagStr = "";
		if (tags != null) {
			for (InboxTag tag : tags) {
				if (tag.getUuidList().contains(wfInstance.getComponentId().toString())) {
					//wfInstance is tagged
					if ((tag.getTagName().equals(TagManager.OUTBOX) || tag.getTagName().equals(TagManager.TODO)) && !specialTag) {
						//Item tag is special, and tree item selected is not specialtag
						return null;
					}else{
						//Item tag isnot special or tree item selected is not outbox o todo
						tagStr = TagManager.getInstance().getHeader(tag.getTagName(), tag.getColor(), tag.getTextColor());
						tagCache.put(wfInstance.getComponentId().toString(), tag);
					}
				}
			}
		}
		String concept = wfInstance.getComponentName();
		row = new Object[columnNames.length];
		String componentStr = tagStr + concept;
		row[COMPONENT] = componentStr;
		row[TARGET] = "";
		row[WORKLIST] = wfInstance.getWorkList().getName();
		row[DESTINATION] = wfInstance.getDestination().getUsername();
		row[STATE] = wfInstance.getState().getName();
		row[WORKFLOW_ITEM] = wfInstance;
		return row;
	};
}

class ProgressListener implements PropertyChangeListener {
	// Prevent creation without providing a progress bar.
	@SuppressWarnings("unused")
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
