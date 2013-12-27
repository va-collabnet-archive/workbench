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
package org.ihtsdo.translation.model;

import java.awt.Container;
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
import java.util.concurrent.CancellationException;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.filter.WfCompletionFilter;
import org.ihtsdo.project.filter.WfCompletionFilter.CompletionOption;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.I_TerminologyProject.Type;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.util.WorkflowSearcher;
import org.ihtsdo.project.view.event.EventMediator;
import org.ihtsdo.project.view.event.GenericEvent.EventType;
import org.ihtsdo.project.view.tag.InboxTag;
import org.ihtsdo.project.view.tag.TagManager;
import org.ihtsdo.project.view.tag.WfTagFilter;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;
import org.ihtsdo.translation.ui.config.event.InboxColumnsChangedEvent;
import org.ihtsdo.translation.ui.config.event.InboxColumnsChangedEventHandler;

/**
 * The Class InboxTableModel.
 */
public class InboxTableModel extends DefaultTableModel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3295746462823927132L;

	/** The column count. */
	private int columnCount = InboxColumn.values().length;

	/** The data. */
	private LinkedList<Object[]> data = new LinkedList<Object[]>();

	/** The searcher. */
	private WorkflowSearcher searcher;

	/** The p bar. */
	private JProgressBar pBar;

	/** The tag cache. */
	private HashMap<String, InboxTag> tagCache = new HashMap<String, InboxTag>();

	/** The preferred. */
	private I_GetConceptData preferred;

	/** The synonym. */
	private I_GetConceptData synonym;

	/** The fsn. */
	private I_GetConceptData fsn;

	/** The inbox worker. */
	private InboxWorker inboxWorker;

	/** The tags. */
	public List<InboxTag> tags;

	/** The columns. */
	protected LinkedHashSet<InboxColumn> columns;

	/** The config. */
	private I_ConfigAceFrame config;

	/**
	 * Instantiates a new inbox table model.
	 * 
	 * @param pBar
	 *            the bar
	 */
	public InboxTableModel(JProgressBar pBar) {
		super();
		this.pBar = pBar;
		this.searcher = new WorkflowSearcher();
		try {
			config = Terms.get().getActiveAceFrameConfig();
			preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
			fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			synonym = Terms.get().getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		refreshColumnsStruct();
		initEventListeners();
	}

	/**
	 * Fire table struct changed after calling this method.
	 */
	public void refreshColumnsStruct() {
		ConfigTranslationModule cfg = null;
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		columns = cfg.getColumnsDisplayedInInbox();
	}

	/**
	 * Inits the event listeners.
	 */
	private void initEventListeners() {
		EventMediator.getInstance().suscribe(EventType.INBOX_COLUMNS_CHANGED, new InboxColumnsChangedEventHandler<InboxColumnsChangedEvent>(this) {
			@Override
			public void handleEvent(InboxColumnsChangedEvent event) {
				columns = event.getInboxColumnComponents();
				fireTableStructureChanged();
			}
		});
	}

	/**
	 * Gets the tag by uuid.
	 * 
	 * @param uuid
	 *            the uuid
	 * @return the tag by uuid
	 */
	public InboxTag getTagByUuid(String[] uuid) {
		return tagCache.get(uuid[InboxTag.TERM_WORKLIST_UUID_INDEX] + uuid[InboxTag.TERM_UUID_INDEX]);
	}

	/**
	 * Adds the tag to cache.
	 * 
	 * @param uuid
	 *            the uuid
	 * @param tag
	 *            the tag
	 */
	public void addTagToCache(String[] uuid, InboxTag tag) {
		tagCache.put(uuid[InboxTag.TERM_WORKLIST_UUID_INDEX] + uuid[InboxTag.TERM_UUID_INDEX], tag);
	}

	/**
	 * Removes the tag from cache.
	 * 
	 * @param uuid
	 *            the uuid
	 */
	public void removeTagFromCache(String[] uuid) {
		tagCache.remove(uuid[InboxTag.TERM_WORKLIST_UUID_INDEX] + uuid[InboxTag.TERM_UUID_INDEX]);
	}

	/**
	 * Gets the row.
	 * 
	 * @param rowNum
	 *            the row num
	 * @return the row
	 */
	public Object[] getRow(int rowNum) {
		return data.get(rowNum);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#addRow(java.lang.Object[])
	 */
	@Override
	public void addRow(Object[] rowData) {
		data.add(rowData);
		fireTableDataChanged();
	}

	/**
	 * Gets the wf instance.
	 * 
	 * @param rowNum
	 *            the row num
	 * @return the wf instance
	 */
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

	/**
	 * Update table.
	 * 
	 * @param data
	 *            the data
	 */
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

	/**
	 * Clear table.
	 */
	public void clearTable() {
		data = new LinkedList<Object[]>();
		fireTableDataChanged();
	}

	/**
	 * Update page.
	 * 
	 * @param filterList
	 *            the filter list
	 * @return true, if successful
	 */
	public boolean updatePage(HashMap<String, WfFilterBI> filterList) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#getColumnCount()
	 */
	public int getColumnCount() {
		this.columnCount = columns.size();
		return columnCount;
	}

	/**
	 * Sets the columns.
	 * 
	 * @param columns
	 *            the new columns
	 */
	public void setColumns(LinkedHashSet<InboxColumn> columns) {
		this.columns = columns;
		fireTableStructureChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#setColumnCount(int)
	 */
	@Override
	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#getRowCount()
	 */
	public int getRowCount() {
		if (data != null && data.size() > 0) {
			return data.size();
		} else {
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		int i = 0;
		for (InboxColumn inboxColumn : columns) {
			if (i == col) {
				return inboxColumn.getColumnName();
			}
			i++;
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		int i = 0;
		if (data != null && !data.isEmpty())
			for (InboxColumn inboxColumn : columns) {
				if (i == col) {
					return data.get(row)[inboxColumn.getColumnNumber()];
				} else if (col == InboxColumn.values().length) {
					return data.get(row)[InboxColumn.values().length];
				}
				i++;
			}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#removeRow(int)
	 */
	@Override
	public void removeRow(int row) {
		if (!data.isEmpty()) {
			data.remove(row);
			fireTableRowsDeleted(row, row);
		}
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then the last column would
	 * contain text ("true"/"false"), rather than a check box.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
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
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/*
	 * Don't need to implement this method unless your table's data can change.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object,
	 * int, int)
	 */
	public void setValueAt(Object value, int row, int col) {
		data.get(row)[col] = value;
		fireTableCellUpdated(row, col);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#setRowCount(int)
	 */
	@Override
	public void setRowCount(int rowCount) {
		super.setRowCount(data.size());
	}

	/**
	 * The Class InboxWorker.
	 */
	class InboxWorker extends SwingWorker<String, Object[]> {

		/** The filter list. */
		private HashMap<String, WfFilterBI> filterList;

		/** The special tag. */
		private boolean specialTag;

		/**
		 * Instantiates a new inbox worker.
		 * 
		 * @param filterList
		 *            the filter list
		 */
		public InboxWorker(HashMap<String, WfFilterBI> filterList) {
			super();
			this.filterList = filterList;
			Set<String> keys = filterList.keySet();

			for (String key : keys) {
				WfFilterBI filter = filterList.get(key);
				if (filter instanceof WfTagFilter) {
					WfTagFilter tf = (WfTagFilter) filter;
					if (tf.getTag() != null && tf.getTag().getTagName().equals(TagManager.OUTBOX) || tf.getTag().getTagName().equals(TagManager.TODO)) {
						specialTag = true;
					} else {
						specialTag = false;
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected String doInBackground() throws Exception {
			data = new LinkedList<Object[]>();
			List<WfInstance> wfInstances = new ArrayList<WfInstance>();
			try {
				ConfigTranslationModule cfg = null;
				cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
				List<WfFilterBI> filters = new ArrayList<WfFilterBI>();
				if (cfg.getCompletionMode().equals(ConfigTranslationModule.CompletionMode.COMPLETE_INSTANCES)) {
					filters .add(new WfCompletionFilter(CompletionOption.COMPLETE_INSTANCES));
				} else if (cfg.getCompletionMode().equals(ConfigTranslationModule.CompletionMode.INCOMPLETE_INSTACES)) {
					filters.add(new WfCompletionFilter(CompletionOption.INCOMPLETE_INSTACES));
				}
				filters.addAll(filterList.values());
				wfInstances = searcher.searchWfInstances(filters);
			} catch (Exception e) {
				if (!(e instanceof InterruptedException)) {
					e.printStackTrace();
				}
			}

			tags = TagManager.getInstance().getAllTagsContent();
			for (WfInstance wfInstance : wfInstances) {
				Object[] row = createRow(wfInstance, specialTag);
				publish(row);
			}
			return "DONE";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<Object[]> rows) {
			try {
				if (!isCancelled()) {
					for (Object[] row : rows) {
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#done()
		 */
		@Override
		public void done() {
			try {
				get();
				fireTableDataChanged();
			} catch (Exception ignore) {
				if (!(ignore instanceof CancellationException)) {
					ignore.printStackTrace();
				}
			}
		}

	}

	/**
	 * Update row.
	 * 
	 * @param currentRow
	 *            the current row
	 * @param modelRowNum
	 *            the model row num
	 * @param specialTag
	 *            the special tag
	 */
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

	/**
	 * Creates the row.
	 * 
	 * @param wfInstance
	 *            the wf instance
	 * @param specialTag
	 *            the special tag
	 * @return the object[]
	 */
	private Object[] createRow(WfInstance wfInstance, boolean specialTag) {
		Object[] row = null;
		String tagStr = "";
		try {
			tags = TagManager.getInstance().getAllTagsContent();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (tags != null) {
			for (InboxTag tag : tags) {
				boolean contains = false;
				String[] tagWorklistConceptUuids = TagManager.getTagWorklistConceptUuids(wfInstance);
				for (String[] uuidlist : tag.getUuidList()) {
					String[] wlanduuid = tagWorklistConceptUuids;
					if (uuidlist[InboxTag.TERM_WORKLIST_UUID_INDEX].equals(wlanduuid[InboxTag.TERM_WORKLIST_UUID_INDEX])
							&& uuidlist[InboxTag.TERM_UUID_INDEX].equals(wlanduuid[InboxTag.TERM_UUID_INDEX])) {
						contains = true;
						break;
					}
				}
				if (contains) {
					// wfInstance is tagged
					if ((tag.getTagName().equals(TagManager.OUTBOX) || tag.getTagName().equals(TagManager.TODO)) && !specialTag) {
						// Item tag is special, and tree item selected is not
						// specialtag
						return null;
					} else {
						// Item tag isnot special or tree item selected is not
						// outbox o todo
						tagStr = TagManager.getInstance().getHeader(tag.getTagName(), tag.getColor(), tag.getTextColor());
						tagCache.put(tagWorklistConceptUuids[InboxTag.TERM_WORKLIST_UUID_INDEX] + tagWorklistConceptUuids[InboxTag.TERM_UUID_INDEX],
								tag);
					}
				}
			}
		}
		String sourcePreferred = "";
		String defaultDescription = "";
		TranslationProject translationProject = null;
		I_TerminologyProject projectConcept = null;
		ConceptChronicleBI concept = null;
		try {
			concept = Ts.get().getConcept(wfInstance.getComponentId());
			defaultDescription = concept.getVersion(config.getViewCoordinate()).getDescriptionPreferred().getText();
			List<I_GetConceptData> langRefsets = null;
			List<ContextualizedDescription> descriptions = new ArrayList<ContextualizedDescription>();
			projectConcept = TerminologyProjectDAO.getProjectForWorklist(wfInstance.getWorkList(), config);
			if (projectConcept.getProjectType().equals(Type.TRANSLATION)) {
				translationProject = TerminologyProjectDAO.getTranslationProject(projectConcept.getConcept(), config);
				langRefsets = translationProject.getSourceLanguageRefsets();
				for (I_GetConceptData langRefset : langRefsets) {
					descriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), langRefset.getConceptNid(), true);
					for (I_ContextualizeDescription description : descriptions) {
						if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
							if (description.getAcceptabilityId() == preferred.getConceptNid() && description.getTypeId() == synonym.getConceptNid()) {
								sourcePreferred = description.getText();
								if (!sourcePreferred.equals("")) {
									break;
								}
							}
						}
					}
					if (!sourcePreferred.equals("")) {
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String targetFSN = "";
		String targetPreferred = "";

		try {
			I_GetConceptData langRefset = null;
			List<ContextualizedDescription> descriptions = new ArrayList<ContextualizedDescription>();
			if (projectConcept != null && projectConcept.getProjectType().equals(Type.TRANSLATION)) {

				langRefset = translationProject.getTargetLanguageRefset();
				descriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), langRefset.getConceptNid(), true);
				for (I_ContextualizeDescription description : descriptions) {
					if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {

						if (description.getTypeId() == fsn.getConceptNid()) {
							targetFSN = description.getText();
						} else if (description.getAcceptabilityId() == preferred.getConceptNid()) {
							targetPreferred = description.getText();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		row = new Object[InboxColumn.values().length + 1];
		row[InboxColumn.SOURCE_PREFERRED.getColumnNumber()] = sourcePreferred;
		row[InboxColumn.TARGET_FSN.getColumnNumber()] = targetFSN;
		// row[InboxColumn.STATUS_DATE.getColumnNumber()] = targetPreferred;
		row[InboxColumn.TARGET_PREFERRED.getColumnNumber()] = targetPreferred;
		row[InboxColumn.WORKLIST.getColumnNumber()] = wfInstance.getWorkList().getName();
		row[InboxColumn.DESTINATION.getColumnNumber()] = wfInstance.getDestination().getUsername();
		row[InboxColumn.STATUS.getColumnNumber()] = wfInstance.getState().getName();
		row[InboxColumn.DEFAULT_DESCRIPTION.getColumnNumber()] = defaultDescription;
		row[InboxColumn.values().length] = wfInstance;
		row[columns.iterator().next().getColumnNumber()] = tagStr + row[columns.iterator().next().getColumnNumber()]; 
		return row;
	}
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
		return ascending ? c1[columnToSort].toString().compareTo(c2[columnToSort].toString()) : c1[columnToSort].toString().compareTo(
				c2[columnToSort].toString())
				* -1;
	}
}
