package org.dwfa.ace.table;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.log.AceLog;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;

public class ConceptAttributeTableModel extends AbstractTableModel implements
		PropertyChangeListener {
	
    enum FieldToChange { DEFINED, STATUS};

	public static class ConceptStatusFieldEditor extends AbstractPopupFieldEditor {

		private static final long serialVersionUID = 1L;

		public ConceptStatusFieldEditor(I_ConfigAceFrame config) {
			super(config);
		}

		@Override
		public int[] getPopupValues() {
			return config.getEditStatusTypePopup().getListArray();
		}

		@Override
		public ConceptBean getSelectedItem(Object value) {
			StringWithConceptTuple swdt = (StringWithConceptTuple) value;
			return ConceptBean.get(swdt.getTuple().getConceptStatus());
		}
		@Override
		public boolean isCellEditable(EventObject evt) {
			if (evt instanceof MouseEvent) {
				int clickCount;
				// For double-click activation
				clickCount = 2;
				return ((MouseEvent) evt).getClickCount() >= clickCount;
			}
			return true;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<I_ConceptAttributeTuple> allTuples;

	private TableChangedSwingWorker tableChangeWorker;

	private ReferencedConceptsSwingWorker refConWorker;

	private Set<Integer> conceptsToFetch = new HashSet<Integer>();

	Map<Integer, ConceptBean> referencedConcepts = new HashMap<Integer, ConceptBean>();

	public class ReferencedConceptsSwingWorker extends
			SwingWorker<Map<Integer, ConceptBean>> {
		private boolean stopWork = false;

		@Override
		protected Map<Integer, ConceptBean> construct() throws Exception {
			getProgress().setActive(true);
			Map<Integer, ConceptBean> concepts = new HashMap<Integer, ConceptBean>();
			for (Integer id : new HashSet<Integer>(conceptsToFetch)) {
				if (stopWork) {
					break;
				}
				ConceptBean b = ConceptBean.get(id);
				b.getDescriptions();
				concepts.put(id, b);

			}
			return concepts;
		}

		@Override
		protected void finished() {
			super.finished();
			if (getProgress() != null) {
				getProgress().getProgressBar().setIndeterminate(false);
				if (conceptsToFetch.size() == 0) {
					getProgress().getProgressBar().setValue(1);
				} else {
					getProgress().getProgressBar().setValue(
							conceptsToFetch.size());
				}
			}
			if (stopWork) {
				return;
			}
			try {
				referencedConcepts = get();
			} catch (InterruptedException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			} catch (ExecutionException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
			fireTableDataChanged();
			if (getProgress() != null) {
				getProgress().setProgressInfo("   " + getRowCount() + "   ");
				getProgress().setActive(false);
			}

		}

		public void stop() {
			stopWork = true;
		}

	}

	public class TableChangedSwingWorker extends SwingWorker<Object> {
		I_GetConceptData cb;

		private boolean stopWork = false;

		public TableChangedSwingWorker(I_GetConceptData cb) {
			super();
			this.cb = cb;
		}

		@Override
		protected Integer construct() throws Exception {
			if (refConWorker != null) {
				refConWorker.stop();
			}
			conceptsToFetch.clear();
			referencedConcepts.clear();
			if ((cb == null) || (cb.getConceptAttributes() == null)) {
				return 0;
			}
			I_ConceptAttributeVersioned concept = cb.getConceptAttributes();
			for (I_ConceptAttributePart conVersion : concept.getVersions()) {
				conceptsToFetch.add(conVersion.getConceptStatus());
				conceptsToFetch.add(conVersion.getPathId());
			}

			refConWorker = new ReferencedConceptsSwingWorker();
			refConWorker.start();
			return null;
		}

		@Override
		protected void finished() {
			super.finished();
			if (getProgress() != null) {
				getProgress().getProgressBar().setIndeterminate(false);
				if (conceptsToFetch.size() == 0) {
					getProgress().getProgressBar().setValue(1);
					getProgress().getProgressBar().setMaximum(1);
				} else {
					getProgress().getProgressBar().setValue(1);
					getProgress().getProgressBar().setMaximum(
							conceptsToFetch.size());
				}
			}
			if (stopWork) {
            fireTableDataChanged();
				return;
			}
			try {
				get();
			} catch (InterruptedException e) {
				;
			} catch (ExecutionException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
			fireTableDataChanged();

		}

		public void stop() {
			stopWork = true;
		}

	}

	public enum CONCEPT_FIELD {
		CON_ID("cid", 5, 100, 100), STATUS("status", 5, 50, 150), DEFINED(
				"defined", 5, 85, 1550), VERSION("version", 5, 140, 140), BRANCH(
				"path", 5, 90, 150);

		private String columnName;

		private int min;

		private int pref;

		private int max;

		private CONCEPT_FIELD(String columnName, int min, int pref, int max) {
			this.columnName = columnName;
			this.min = min;
			this.pref = pref;
			this.max = max;
		}

		public String getColumnName() {
			return columnName;
		}

		public int getMax() {
			return max;
		}

		public int getMin() {
			return min;
		}

		public int getPref() {
			return pref;
		}

	}

	private CONCEPT_FIELD[] columns;

	private SmallProgressPanel progress;

	I_HostConceptPlugins host;

	public ConceptAttributeTableModel(CONCEPT_FIELD[] columns, I_HostConceptPlugins host) {
		super();
		this.columns = columns;
		this.host = host;
		host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
	}

	public void setColumns(CONCEPT_FIELD[] columns) {
		if (this.columns.length != columns.length) {
			this.columns = columns;
			fireTableStructureChanged();
			return;
		}
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals(this.columns[i]) == false) {
				this.columns = columns;
				fireTableStructureChanged();
				return;
			}
		}
	}

	public int getColumnCount() {
		return columns.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			if (rowIndex >= getRowCount()) {
				return null;
			}
			I_ConceptAttributeTuple conTuple = getConceptTuple(rowIndex);
			if (conTuple == null) {
				return null;
			}

			switch (columns[columnIndex]) {
			case CON_ID:
				return new StringWithConceptTuple(Integer.toString(conTuple
						.getConId()), conTuple);
			case STATUS:
				if (getReferencedConcepts().containsKey(conTuple.getConceptStatus())) {
					return new StringWithConceptTuple(getPrefText(conTuple.getConceptStatus()), conTuple);
				}
				return new StringWithConceptTuple(Integer.toString(conTuple
						.getConceptStatus()), conTuple);
			case DEFINED:
				return new StringWithConceptTuple(Boolean.toString(conTuple
						.isDefined()), conTuple);
			case VERSION:
				if (conTuple.getVersion() == Integer.MAX_VALUE) {
					return new StringWithConceptTuple(ThinVersionHelper
							.uncommittedHtml(), conTuple);
				}
				return new StringWithConceptTuple(ThinVersionHelper.format(conTuple
						.getVersion()), conTuple);
			case BRANCH:
				if (getReferencedConcepts().containsKey(conTuple.getPathId())) {
					return new StringWithConceptTuple(getPrefText(conTuple.getPathId()), conTuple);
				}
				return new StringWithConceptTuple(Integer.toString(conTuple
						.getPathId()), conTuple);
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return null;
	}

	private String getPrefText(int id) throws IOException {
		ConceptBean cb = getReferencedConcepts().get(id);
		I_DescriptionTuple desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
		if (desc != null) {
			return desc.getText();
		}
		cb = getReferencedConcepts().get(id);
		desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
		return "null pref desc: " + cb.getInitialText();
	}

	private I_ConceptAttributeTuple getConceptTuple(int rowIndex) throws IOException {
		I_GetConceptData cb = (I_GetConceptData) host.getTermComponent();
		if ((cb == null) || (cb.getConceptAttributes() == null))  {
			return null;
		}
		if (allTuples == null) {
         allTuples = new ArrayList<I_ConceptAttributeTuple>();
         Set<I_Position> positions = null;
         if (host.getUsePrefs()) {
            positions = host.getConfig().getViewPositionSet();
         }
         if (host.getShowHistory()) {
            positions = null;
          }
			cb.getConceptAttributes().addTuples(null, positions, allTuples);
		}
		return allTuples.get(rowIndex);
	}

	public String getColumnName(int col) {
		return columns[col].getColumnName();
	}

	public boolean isCellEditable(int row, int col) {
        try {
			if (getConceptTuple(row).getVersion() != Integer.MAX_VALUE) {
				return false;
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
			return false;
		}
        switch (columns[col]) {
        case CON_ID:
        	return false;
        case STATUS:
        	return true;
        case DEFINED:
        	return true;
        case VERSION:
        	return false;
        case BRANCH:
        	return false;
        }
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
		try {
			switch (columns[col]) {
			case CON_ID:
				break;
			case STATUS:
				Integer statusId = (Integer) value;
				getConceptTuple(row).setStatusId(statusId);
				getReferencedConcepts()
						.put(statusId, ConceptBean.get(statusId));
				break;
			case DEFINED:
				getConceptTuple(row).setDefined((Boolean) value);
				break;
			case VERSION:
				break;
			case BRANCH:
				break;
			}
			fireTableCellUpdated(row, col);
		} catch (Exception ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		allTuples = null;
		if (getProgress() != null) {
			getProgress().setVisible(true);
			getProgress().getProgressBar().setValue(0);
			getProgress().getProgressBar().setIndeterminate(true);
		}
		fireTableDataChanged();
		if (tableChangeWorker != null) {
			tableChangeWorker.stop();
		}
		tableChangeWorker = new TableChangedSwingWorker((I_GetConceptData) evt
				.getNewValue());
		tableChangeWorker.start();
	}

	public Map<Integer, ConceptBean> getReferencedConcepts() {
		return referencedConcepts;
	}

	public Class<?> getColumnClass(int c) {
		switch (columns[c]) {
		case CON_ID:
			return StringWithConceptTuple.class;
		case STATUS:
			return StringWithConceptTuple.class;
		case DEFINED:
			return StringWithConceptTuple.class;
		case VERSION:
			return StringWithConceptTuple.class;
		case BRANCH:
			return StringWithConceptTuple.class;
		}
		return StringWithConceptTuple.class;
	}

	public SmallProgressPanel getProgress() {
		return progress;
	}

	public void setProgress(SmallProgressPanel progress) {
		this.progress = progress;
	}

	public static class StringWithConceptTuple implements Comparable, I_CellTextWithTuple {
		String cellText;

		I_ConceptAttributeTuple tuple;

		public StringWithConceptTuple(String cellText, I_ConceptAttributeTuple tuple) {
			super();
			this.cellText = cellText;
			this.tuple = tuple;
		}

		/* (non-Javadoc)
		 * @see org.dwfa.ace.table.I_CellTextWithTuple#getCellText()
		 */
		public String getCellText() {
			return cellText;
		}

		/* (non-Javadoc)
		 * @see org.dwfa.ace.table.I_CellTextWithTuple#getTuple()
		 */
		public I_ConceptAttributeTuple getTuple() {
			return tuple;
		}

		public String toString() {
			return cellText;
		}

		public int compareTo(Object o) {
			StringWithConceptTuple another = (StringWithConceptTuple) o;
			return cellText.compareTo(another.cellText);
		}
	}

	public CONCEPT_FIELD[] getColumnEnums() {
		return columns;
	}

	public int getRowCount() {
		if (allTuples == null) {
			try {
				getConceptTuple(0);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
		if (allTuples == null) {
			return 0;
		}
		return allTuples.size();
	}

	public AttributePopupListener makePopupListener(JTable table, I_ConfigAceFrame config) {
		return new AttributePopupListener(table, config, this);
	}
}