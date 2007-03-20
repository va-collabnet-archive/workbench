package org.dwfa.ace.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.ace.I_DoConceptDrop;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.I_HostConceptPlugins;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_GetConceptData;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinDescTuple;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelTuple;

import com.sleepycat.je.DatabaseException;

public abstract class RelTableModel extends AbstractTableModel implements
		PropertyChangeListener, I_DoConceptDrop {
	private List<ThinRelTuple> allTuples;

	//protected ConceptPanel parentPanel;

	protected I_GetConceptData tableBean = null;

	private Set<Integer> conceptsToFetch = Collections
			.synchronizedSet(new HashSet<Integer>());

	private Map<Integer, ConceptBean> referencedConcepts = Collections
			.synchronizedMap(new HashMap<Integer, ConceptBean>());

	private TableChangedSwingWorker tableChangeWorker;

	private SmallProgressPanel progress = new SmallProgressPanel();

	public class ReferencedConceptsSwingWorker extends
			SwingWorker<Map<Integer, ConceptBean>> {
		private class ProgressUpdator implements I_UpdateProgress {
			Timer updateTimer;

			public ProgressUpdator() {
				super();
				getProgress().setActive(true);
				updateTimer = new Timer(100, this);
				updateTimer.start();
				if (progress != null) {
					progress.setActive(true);
					progress.setVisible(true);
				}
			}

			public void actionPerformed(ActionEvent e) {
				if (!stopWork) {
					if (progress != null) {
						JProgressBar progressBar = progress.getProgressBar();
						progressBar.setValue(referencedConcepts.size());
						progress.setProgressInfo("   " + progressBar.getValue()
								+ "/" + progressBar.getMaximum() + "   ");
						fireTableDataChanged();
						if (progressBar.getValue() == progressBar.getMaximum()) {
							normalCompletion();
						}
					}
				} else {
					updateTimer.stop();
				}
			}

			public void normalCompletion() {
				if (progress != null) {
					progress.setProgressInfo("   " + getRowCount() + "   ");
					progress.setActive(false);
				}
			}

		}

		private boolean stopWork = false;

		ProgressUpdator updator = new ProgressUpdator();

		@Override
		protected Map<Integer, ConceptBean> construct() throws Exception {
			referencedConcepts = Collections
					.synchronizedMap(new HashMap<Integer, ConceptBean>());
			for (Integer id : new HashSet<Integer>(conceptsToFetch)) {
				if (stopWork) {
					break;
				}
				ConceptBean b = ConceptBean.get(id);
				b.getDescriptions();
				referencedConcepts.put(id, b);
			}
			return referencedConcepts;
		}

		@Override
		protected void finished() {
			super.finished();
			if (progress != null) {
				progress.getProgressBar().setIndeterminate(false);
				if (conceptsToFetch.size() == 0) {
					progress.getProgressBar().setValue(1);
				} else {
					progress.getProgressBar().setValue(conceptsToFetch.size());
				}
				progress.setEnabled(false);
			}
			if (stopWork) {
				updator.normalCompletion();
				fireTableDataChanged();
				return;
			}
			try {
				referencedConcepts = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			fireTableDataChanged();
			updator.normalCompletion();
			stopWork = true;
		}

		public void stop() {
			stopWork = true;
		}

	}

	public class TableChangedSwingWorker extends SwingWorker<Integer> {
		I_GetConceptData cb;

		private boolean stopWork = false;

		private ReferencedConceptsSwingWorker refConWorker;

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
			if (cb == null) {
				return 0;
			}
			List<ThinRelTuple> rels = getRels(cb, host.getUsePrefs(),
					getShowHistory());
			for (ThinRelTuple r : rels) {
				if (stopWork) {
					return -1;
				}
				conceptsToFetch.add(r.getC1Id());
				conceptsToFetch.add(r.getC2Id());
				conceptsToFetch.add(r.getCharacteristicId());
				conceptsToFetch.add(r.getRefinabilityId());
				conceptsToFetch.add(r.getRelTypeId());
				conceptsToFetch.add(r.getStatusId());
				conceptsToFetch.add(r.getPathId());

			}

			refConWorker = new ReferencedConceptsSwingWorker();
			refConWorker.start();
			return rels.size();
		}

		@Override
		protected void finished() {
			super.finished();
			if (progress != null) {
				progress.getProgressBar().setIndeterminate(false);
				if (conceptsToFetch.size() == 0) {
					progress.getProgressBar().setValue(1);
					progress.getProgressBar().setMaximum(1);
				} else {
					progress.getProgressBar().setValue(1);
					progress.getProgressBar()
							.setMaximum(conceptsToFetch.size());
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
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			tableBean = cb;
			fireTableDataChanged();
		}

		public void stop() {
			stopWork = true;
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum REL_FIELD {
		REL_ID("rid", 5, 100, 100), SOURCE_ID("origin", 5, 300, 1000), REL_TYPE(
				"type", 5, 120, 500), DEST_ID("destination", 5, 300, 1000), GROUP(
				"group", 5, 36, 36), REFINABILITY("refinability", 5, 80, 80), CHARACTERISTIC(
				"char", 5, 70, 70), STATUS("status", 5, 50, 50), VERSION(
				"version", 5, 140, 140), BRANCH("pathId", 5, 90, 180);

		private String columnName;

		private int min;

		private int pref;

		private int max;

		private REL_FIELD(String columnName, int min, int pref, int max) {
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

	private REL_FIELD[] columns;

	protected I_HostConceptPlugins host;

	public RelTableModel(I_HostConceptPlugins host, REL_FIELD[] columns) {
		super();
		this.columns = columns;
		this.host = host;
		this.host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
	}

	public int getColumnCount() {
		return columns.length;
	}

	public int getRowCount() {
		if (tableBean == null) {
			return 0;
		}
		try {
			allTuples = getRels(tableBean, host.getUsePrefs(), getShowHistory());
			return allTuples.size();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public abstract List<ThinRelTuple> getRels(I_GetConceptData cb,
			boolean usePrefs, boolean showHistory) throws DatabaseException;

	public Object getValueAt(int rowIndex, int columnIndex) {
		I_GetConceptData cb = (I_GetConceptData) host.getTermComponent();
		if (cb == null) {
			return null;
		}
		REL_FIELD field = columns[columnIndex];
		try {
			ThinRelTuple rel;
			if (rowIndex >= allTuples.size()) {
				return null;
			}
			rel = allTuples.get(rowIndex);

			switch (field) {
			case REL_ID:
				return new StringWithRelTuple(Integer.toString(rel.getRelId()),
						rel);
			case SOURCE_ID:
				if (referencedConcepts.containsKey(rel.getC1Id())) {
					return new StringWithRelTuple(getPrefText(rel.getC1Id()), rel);
				}
				return new StringWithRelTuple(Integer.toString(rel.getC1Id()),
						rel);
			case REL_TYPE:
				if (referencedConcepts.containsKey(rel.getRelTypeId())) {
					return new StringWithRelTuple(getPrefText(rel.getRelTypeId()), rel);
				}
				return new StringWithRelTuple(Integer.toString(rel
						.getRelTypeId()), rel);
			case DEST_ID:
				if (referencedConcepts.containsKey(rel.getC2Id())) {
					return new StringWithRelTuple(getPrefText(rel.getC2Id()), rel);
				}
				return new StringWithRelTuple(Integer.toString(rel.getC2Id()),
						rel);
			case GROUP:
				return new StringWithRelTuple(Integer.toString(rel.getGroup()),
						rel);
			case REFINABILITY:
				if (referencedConcepts.containsKey(rel.getRefinabilityId())) {
					return new StringWithRelTuple(getPrefText(rel.getRefinabilityId()), rel);
				}
				return new StringWithRelTuple(Integer.toString(rel
						.getRefinabilityId()), rel);
			case CHARACTERISTIC:
				if (referencedConcepts.containsKey(rel.getCharacteristicId())) {
					return new StringWithRelTuple(getPrefText(rel.getCharacteristicId()), rel);
				}
				return new StringWithRelTuple(Integer.toString(rel
						.getCharacteristicId()), rel);
			case STATUS:
				if (referencedConcepts.containsKey(rel.getStatusId())) {
					return new StringWithRelTuple(getPrefText(rel.getStatusId()), rel);
				}
				return new StringWithRelTuple(Integer.toString(rel
						.getStatusId()), rel);
			case VERSION:
				if (rel.getVersion() == Integer.MAX_VALUE) {
					return new StringWithRelTuple(ThinVersionHelper
							.uncommittedHtml(), rel);
				}
				return new StringWithRelTuple(ThinVersionHelper.format(rel
						.getVersion()), rel);
			case BRANCH:
				if (referencedConcepts.containsKey(rel.getPathId())) {
					return new StringWithRelTuple(getPrefText(rel.getPathId()), rel);
				}
				return new StringWithRelTuple(
						Integer.toString(rel.getPathId()), rel);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return "No case found for: " + field;
	}
	private String getPrefText(int id) throws DatabaseException {
		ConceptBean cb = referencedConcepts.get(id);
		ThinDescTuple desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
		if (desc != null) {
			return desc.getText();
		}
		cb = referencedConcepts.get(id);
		desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
		return "null pref desc: " + cb.getInitialText();
	}

	public String getColumnName(int col) {
		return columns[col].getColumnName();
	}

	public boolean isCellEditable(int row, int col) {
		if (row >= allTuples.size()) {
			return false;
		}
		ThinRelTuple rel = allTuples.get(row);
		if (rel.getVersion() != Integer.MAX_VALUE) {
			return false;
		}
		REL_FIELD field = columns[col];
		switch (field) {
		case REL_ID:
			return false;
		case SOURCE_ID:
			return false;
		case REL_TYPE:
			return true;
		case DEST_ID:
			return false;
		case GROUP:
			return true;
		case REFINABILITY:
			return true;
		case CHARACTERISTIC:
			return true;
		case STATUS:
			return true;
		case VERSION:
			return false;
		case BRANCH:
			return false;
		}
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
		ThinRelTuple rel = allTuples.get(row);
		REL_FIELD field = columns[col];
		switch (field) {
		case REL_ID:
			break;
		case SOURCE_ID:
			break;
		case REL_TYPE:
			Integer typeId = (Integer) value;
			rel.setRelTypeId(typeId);
			referencedConcepts.put(typeId, ConceptBean.get(typeId));
			break;
		case DEST_ID:
			break;
		case GROUP:
			if (String.class.isAssignableFrom(value.getClass())) {
				String valueStr = (String) value;
				rel.setGroup(Integer.parseInt(valueStr));
			} else {
				rel.setGroup((Integer) value);
			}
			break;
		case REFINABILITY:
			Integer refinabilityId = (Integer) value;
			rel.setRefinabilityId(refinabilityId);
			referencedConcepts.put(refinabilityId, ConceptBean
					.get(refinabilityId));
			break;
		case CHARACTERISTIC:
			Integer characteristicId = (Integer) value;
			rel.setCharacteristicId(characteristicId);
			referencedConcepts.put(characteristicId, ConceptBean
					.get(characteristicId));
			break;
		case STATUS:
			Integer statusId = (Integer) value;
			rel.setStatusId(statusId);
			referencedConcepts.put(statusId, ConceptBean.get(statusId));
			break;
		case VERSION:
			break;
		case BRANCH:
			break;
		}
		fireTableCellUpdated(row, col);
	}

	public Class<?> getColumnClass(int c) {
		return String.class;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		allTuples = null;
		if (progress != null) {
			progress.getProgressBar().setValue(0);
			progress.getProgressBar().setIndeterminate(true);
			progress.setActive(true);
			progress.setVisible(true);
		}
		tableBean = null;
		fireTableDataChanged();

		if (tableChangeWorker != null) {
			tableChangeWorker.stop();
		}
		tableChangeWorker = new TableChangedSwingWorker((I_GetConceptData) evt
				.getNewValue());
		tableChangeWorker.start();
	}

	public SmallProgressPanel getProgress() {
		return progress;
	}

	public void setProgress(SmallProgressPanel progress) {
		this.progress = progress;
	}

	public static class StringWithRelTuple implements Comparable, I_CellTextWithTuple {
		String cellText;

		ThinRelTuple tuple;

		public StringWithRelTuple(String cellText, ThinRelTuple tuple) {
			super();
			this.cellText = cellText;
			this.tuple = tuple;
		}

		public String getCellText() {
			return cellText;
		}

		public ThinRelTuple getTuple() {
			return tuple;
		}

		public String toString() {
			return cellText;
		}

		public int compareTo(Object o) {
			StringWithRelTuple another = (StringWithRelTuple) o;
			return cellText.compareTo(another.cellText);
		}
	}

	public REL_FIELD[] getColumnEnums() {
		return columns;
	}

	public static class RelStatusFieldEditor extends AbstractPopupFieldEditor {

		private static final long serialVersionUID = 1L;

		public RelStatusFieldEditor(AceFrameConfig config) {
			super(config);
		}

		@Override
		public int[] getPopupValues() {
			return config.getEditStatusTypePopup().getSetValues();
		}

		@Override
		public ConceptBean getSelectedItem(Object value) {
			StringWithRelTuple swdt = (StringWithRelTuple) value;
			return ConceptBean.get(swdt.getTuple().getStatusId());
		}
	}

	public static class RelRefinabilityFieldEditor extends
			AbstractPopupFieldEditor {

		private static final long serialVersionUID = 1L;

		public RelRefinabilityFieldEditor(AceFrameConfig config) {
			super(config);
		}

		@Override
		public int[] getPopupValues() {
			return config.getEditRelRefinabiltyPopup().getSetValues();
		}

		@Override
		public ConceptBean getSelectedItem(Object value) {
			StringWithRelTuple swdt = (StringWithRelTuple) value;
			return ConceptBean.get(swdt.getTuple().getRefinabilityId());
		}
	}

	public static class RelCharactisticFieldEditor extends
			AbstractPopupFieldEditor {

		private static final long serialVersionUID = 1L;

		public RelCharactisticFieldEditor(AceFrameConfig config) {
			super(config);
		}

		@Override
		public int[] getPopupValues() {
			return config.getEditRelCharacteristicPopup().getSetValues();
		}

		@Override
		public ConceptBean getSelectedItem(Object value) {
			StringWithRelTuple swdt = (StringWithRelTuple) value;
			return ConceptBean.get(swdt.getTuple().getCharacteristicId());
		}
	}

	public static class RelTypeFieldEditor extends AbstractPopupFieldEditor {

		private static final long serialVersionUID = 1L;

		public RelTypeFieldEditor(AceFrameConfig config) {
			super(config);
		}

		@Override
		public int[] getPopupValues() {
			return config.getEditRelTypePopup().getSetValues();
		}

		@Override
		public ConceptBean getSelectedItem(Object value) {
			StringWithRelTuple swdt = (StringWithRelTuple) value;
			return ConceptBean.get(swdt.getTuple().getRelTypeId());
		}
	}

	public PopupListener makePopupListener(JTable table, AceFrameConfig config) {
		return new PopupListener(table, config);
	}

	public class PopupListener extends MouseAdapter {
		private class ChangeActionListener implements ActionListener {

			public ChangeActionListener() {
				super();
			}

			public void actionPerformed(ActionEvent e) {
				try {
					ConceptBean sourceBean = ConceptBean.get(selectedObject
							.getTuple().getC1Id());
					ConceptBean destBean = ConceptBean.get(selectedObject
							.getTuple().getC2Id());
					for (Path p : config.getEditingPathSet()) {
						ThinRelPart newPart = selectedObject.getTuple()
								.duplicatePart();
						newPart.setPathId(p.getConceptId());
						newPart.setVersion(Integer.MAX_VALUE);
						selectedObject.getTuple().getRelVersioned()
								.getVersions().add(newPart);
						sourceBean.getSourceRel(
								selectedObject.getTuple().getRelId())
								.addVersion(newPart);
						destBean
								.getDestRel(selectedObject.getTuple().getRelId())
								.addVersion(newPart);
					}
					ACE.addUncommitted(sourceBean);
					ACE.addUncommitted(destBean);
					allTuples = null;
					RelTableModel.this.fireTableDataChanged();
				} catch (DatabaseException e1) {
					e1.printStackTrace();
				}
			}
		}

		private class RetireActionListener implements ActionListener {

			public RetireActionListener() {
				super();
			}

			public void actionPerformed(ActionEvent e) {
				try {
					ConceptBean sourceBean = ConceptBean.get(selectedObject
							.getTuple().getC1Id());
					ConceptBean destBean = ConceptBean.get(selectedObject
							.getTuple().getC2Id());
					for (Path p : config.getEditingPathSet()) {
						ThinRelPart newPart = selectedObject.getTuple()
								.duplicatePart();
						newPart.setPathId(p.getConceptId());
						newPart.setVersion(Integer.MAX_VALUE);
						newPart.setStatusId((AceConfig.vodb
								.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED
										.getUids())));
						referencedConcepts.put(newPart.getStatusId(),
								ConceptBean.get(newPart.getStatusId()));
						sourceBean.getSourceRel(
								selectedObject.getTuple().getRelId())
								.addVersion(newPart);
						destBean
								.getDestRel(selectedObject.getTuple().getRelId())
								.addVersion(newPart);

						selectedObject.getTuple().getRelVersioned()
								.getVersions().add(newPart);
					}
					ACE.addUncommitted(sourceBean);
					ACE.addUncommitted(destBean);
					allTuples = null;
					RelTableModel.this.fireTableDataChanged();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		JPopupMenu popup;

		JTable table;

		ActionListener retire;

		ActionListener change;

		StringWithRelTuple selectedObject;

		AceFrameConfig config;

		public PopupListener(JTable table, AceFrameConfig config) {
			super();
			this.table = table;
			this.config = config;
			retire = new RetireActionListener();
			change = new ChangeActionListener();
		}

		private void makePopup(MouseEvent e) {
			popup = null;
			int column = table.columnAtPoint(e.getPoint());
			int row = table.rowAtPoint(e.getPoint());
			if ((row != -1) && (column != -1)) {
				popup = new JPopupMenu();
				JMenuItem noActionItem = new JMenuItem("");
				popup.add(noActionItem);
				selectedObject = (StringWithRelTuple) table.getValueAt(row, column);
				JMenuItem changeItem = new JMenuItem("Change");
				popup.add(changeItem);
				changeItem.addActionListener(change);
				JMenuItem retireItem = new JMenuItem("Retire");
				popup.add(retireItem);
				retireItem.addActionListener(retire);
			}
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				if (config.getEditingPathSet().size() > 0) {
					makePopup(e);
					if (popup != null) {
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				} else {
					JOptionPane.showMessageDialog(table.getTopLevelAncestor(),
							"You must select at least one path to edit on...");
				}
			}
		}
	}

	public REL_FIELD[] getColumns() {
		return columns;
	}

	public void setColumns(REL_FIELD[] columns) {
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

	public boolean getShowHistory() {
		return host.getShowHistory();
	}

}