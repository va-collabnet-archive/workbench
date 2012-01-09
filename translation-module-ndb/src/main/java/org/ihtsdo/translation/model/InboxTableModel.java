package org.ihtsdo.translation.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowSearcher;
import org.ihtsdo.project.workflow.event.EventMediator;
import org.ihtsdo.project.workflow.event.GenericEvent.EventType;
import org.ihtsdo.project.workflow.filters.WfSearchFilterBI;
import org.ihtsdo.project.workflow.filters.WfTagFilter;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.tag.InboxTag;
import org.ihtsdo.project.workflow.tag.TagManager;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;
import org.ihtsdo.translation.ui.config.event.InboxColumnsChangedEvent;
import org.ihtsdo.translation.ui.config.event.InboxColumnsChangedEventHandler;

public class InboxTableModel extends DefaultTableModel {

	private static final long serialVersionUID = -3295746462823927132L;

	private int columnCount = InboxColumn.values().length;
	private LinkedList<Object[]> data = new LinkedList<Object[]>();
	private WorkflowSearcher searcher;
	private JProgressBar pBar;
	private HashMap<String, InboxTag> tagCache = new HashMap<String, InboxTag>();

	private InboxWorker inboxWorker;

	public List<InboxTag> tags;

	public InboxTableModel(JProgressBar pBar) {
		super();
		this.pBar = pBar;
		this.searcher = new WorkflowSearcher();
		initEventListeners();
	}

	
	private void initEventListeners() {
		EventMediator.getInstance().suscribe(EventType.INBOX_COLUMNS_CHANGED, new InboxColumnsChangedEventHandler<InboxColumnsChangedEvent>(this) {
			@Override
			public void handleEvent(InboxColumnsChangedEvent event) {
				fireTableStructureChanged();
			}
		});
	}


	public InboxTag getTagByUuid(String uuid) {
		return tagCache.get(uuid);
	}

	public void addTagToCache(String uuid, InboxTag tag) {
		tagCache.put(uuid, tag);
	}

	public void removeTagFromCache(String uuid) {
		tagCache.remove(uuid);
	}

	public Object[] getRow(int rowNum) {
		return data.get(rowNum);
	}

	@Override
	public void addRow(Object[] rowData) {
		data.add(rowData);
		fireTableDataChanged();
	}

	public WfInstance getWfInstance(int rowNum) {
		ConfigTranslationModule cfg = new ConfigTranslationModule();
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return (WfInstance) data.get(rowNum)[cfg.getColumnsDisplayedInInbox().size() + 1];
	}

	public void updateTable(Object[][] data) {
		int i = 0;
		this.data = new LinkedList<Object[]>();
		for (Object[] objects : data) {
			Object[] row = new Object[InboxColumn.values().length + 1];
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
		return InboxColumn.values().length;
	}

	public int getColumnCount() {
		ConfigTranslationModule cfg = new ConfigTranslationModule();
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		LinkedHashSet<InboxColumn> columnsToDisplay = cfg.getColumnsDisplayedInInbox();
		this.columnCount = columnsToDisplay.size();
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
		ConfigTranslationModule cfg = null;
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		LinkedHashSet<InboxColumn> columnsToDisplay = cfg.getColumnsDisplayedInInbox();
		int i = 0;
		for (InboxColumn inboxColumn : columnsToDisplay) {
			if (i == col) {
				return inboxColumn.getColumnName();
			}
			i++;
		}
		return "";
	}

	public Object getValueAt(int row, int col) {
		ConfigTranslationModule cfg = null;
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		LinkedHashSet<InboxColumn> columnsToDisplay = cfg.getColumnsDisplayedInInbox();
		int i = 0;
		for (InboxColumn inboxColumn : columnsToDisplay) {
			if (i == col) {
				return data.get(row)[inboxColumn.getColumnNumber()];
			}
			i++;
		}
		return "";
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
		return false;
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
					// wfInstance is tagged
					if ((tag.getTagName().equals(TagManager.OUTBOX) || tag.getTagName().equals(TagManager.TODO)) && !specialTag) {
						// Item tag is special, and tree item selected is not
						// specialtag
						return null;
					} else {
						// Item tag isnot special or tree item selected is not
						// outbox o todo
						tagStr = TagManager.getInstance().getHeader(tag.getTagName(), tag.getColor(), tag.getTextColor());
						tagCache.put(wfInstance.getComponentId().toString(), tag);
					}
				}
			}
		}
		String concept = wfInstance.getComponentName();
		row = new Object[InboxColumn.values().length + 1];
		String componentStr = tagStr + concept;
		row[InboxColumn.SOURCE_PREFERRED.getColumnNumber()] = componentStr;
		row[InboxColumn.TARGET_PREFERRED.getColumnNumber()] = "";
		row[InboxColumn.WORKLIST.getColumnNumber()] = wfInstance.getWorkList().getName();
		row[InboxColumn.DESTINATION.getColumnNumber()] = wfInstance.getDestination().getUsername();
		row[InboxColumn.STATUS.getColumnNumber()] = wfInstance.getState().getName();
		row[InboxColumn.values().length] = wfInstance;
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
