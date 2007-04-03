package org.dwfa.ace.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.I_HostConceptPlugins;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;

import com.sleepycat.je.DatabaseException;

public class IdTableModel extends AbstractTableModel implements
		PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class StringWithIdTuple implements Comparable, I_CellTextWithTuple {
		String cellText;

		I_IdTuple tuple;

		public StringWithIdTuple(String cellText, I_IdTuple tuple) {
			super();
			this.cellText = cellText;
			this.tuple = tuple;
		}

		public String getCellText() {
			return cellText;
		}

		public I_IdTuple getTuple() {
			return tuple;
		}

		public String toString() {
			return cellText;
		}

		public int compareTo(Object o) {
			StringWithIdTuple another = (StringWithIdTuple) o;
			return cellText.compareTo(another.cellText);
		}
	}
	private List<I_IdTuple> allTuples;

	private TableChangedSwingWorker tableChangeWorker;

	private ReferencedConceptsSwingWorker refConWorker;

	private Set<Integer> conceptsToFetch = new HashSet<Integer>();

	private Map<Integer, ConceptBean> referencedConcepts = new HashMap<Integer, ConceptBean>();

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
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
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
			if (cb == null) {
				return 0;
			}
			I_IdVersioned id = cb.getId();
			for (I_IdPart part : id.getVersions()) {
				conceptsToFetch.add(part.getIdStatus());
				conceptsToFetch.add(part.getPathId());
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
				return;
			}
			try {
				get();
			} catch (InterruptedException e) {
				;
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			fireTableDataChanged();

		}

		public void stop() {
			stopWork = true;
		}
	}

	public ID_FIELD[] getColumnEnums() {
		return columns;
	}

	public enum ID_FIELD {
		LOCAL_ID("local id", 5, 100, 100), STATUS("status", 5, 50, 50), EXT_ID(
				"external id", 5, 85, 1550), VERSION("version", 5, 140, 140), BRANCH(
				"pathId", 5, 90, 150);

		private String columnName;

		private int min;

		private int pref;

		private int max;

		private ID_FIELD(String columnName, int min, int pref, int max) {
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
	private ID_FIELD[] columns;

	private SmallProgressPanel progress = new SmallProgressPanel();

	private I_HostConceptPlugins host;

	public IdTableModel(ID_FIELD[] columns, I_HostConceptPlugins host) {
		super();
		this.host = host;
		host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
		this.columns = columns;
	}
	public void setColumns(ID_FIELD[] columns) {
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
	public String getColumnName(int col) {
		return columns[col].getColumnName();
	}

	public int getColumnCount() {
		return columns.length;
	}
	private I_IdTuple getIdTuple(int rowIndex) throws DatabaseException {
		I_GetConceptData cb = (I_GetConceptData) host.getTermComponent();
		if (cb == null) {
			return null;
		}
		if (allTuples == null) {
			allTuples = cb.getId().getTuples();
		}
		return allTuples.get(rowIndex);
	}

	public int getRowCount() {
		if (allTuples == null) {
			try {
				getIdTuple(0);
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (allTuples == null) {
			return 0;
		}
		return allTuples.size();
	}
	private String getPrefText(int id) throws DatabaseException {
		ConceptBean cb = getReferencedConcepts().get(id);
		I_DescriptionTuple statusDesc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
		String text = statusDesc.getText();
		return text;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			if (rowIndex >= getRowCount()) {
				return null;
			}
			I_IdTuple idTuple = getIdTuple(rowIndex);
			if (idTuple == null) {
				return null;
			}

			switch (columns[columnIndex]) {
			case LOCAL_ID:
				return new StringWithIdTuple(Integer.toString(idTuple
						.getNativeId()), idTuple);
			case STATUS:
				if (getReferencedConcepts().containsKey(idTuple.getIdStatus())) {
					return new StringWithIdTuple(getPrefText(idTuple.getIdStatus()), idTuple);
				}
				return new StringWithIdTuple(Integer.toString(idTuple
						.getIdStatus()), idTuple);
			case EXT_ID:
				return new StringWithIdTuple(idTuple
						.getSourceId().toString(), idTuple);
			case VERSION:
				if (idTuple.getVersion() == Integer.MAX_VALUE) {
					return new StringWithIdTuple(ThinVersionHelper
							.uncommittedHtml(), idTuple);
				}
				new ThinVersionHelper();
				System.out.println("ID tuple version: " + idTuple.getVersion());
				System.out.println("ID tuple time: " + ThinVersionHelper.convert(idTuple.getVersion()));
				System.out.println("ID tuple formatted: " + ThinVersionHelper.format(idTuple
						.getVersion()));
				
				
				return new StringWithIdTuple(ThinVersionHelper.format(idTuple
						.getVersion()), idTuple);
			case BRANCH:
				if (getReferencedConcepts().containsKey(idTuple.getPathId())) {
					return new StringWithIdTuple(getPrefText(idTuple.getPathId()), idTuple);
				}
				return new StringWithIdTuple(Integer.toString(idTuple
						.getPathId()), idTuple);
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Class<?> getColumnClass(int c) {
		switch (columns[c]) {
		case LOCAL_ID:
			return Number.class;
		case EXT_ID:
			return StringWithIdTuple.class;
		case STATUS:
			return StringWithIdTuple.class;
		case VERSION:
			return Number.class;
		case BRANCH:
			return StringWithIdTuple.class;
		}
		return String.class;
	}

	public SmallProgressPanel getProgress() {
		return progress;
	}

	public void setProgress(SmallProgressPanel progress) {
		this.progress = progress;
	}
	public Map<Integer, ConceptBean> getReferencedConcepts() {
		return referencedConcepts;
	}
	public class PopupListener extends MouseAdapter {
		private class ChangeActionListener implements ActionListener {

			public ChangeActionListener() {
				super();
			}

			public void actionPerformed(ActionEvent e) {
				for (Path p : config.getEditingPathSet()) {
					I_IdPart newPart = selectedObject.getTuple()
							.duplicatePart();
					newPart.setPathId(p.getConceptId());
					newPart.setVersion(Integer.MAX_VALUE);
					selectedObject.getTuple().getIdVersioned().getVersions().add(
							newPart);
				}
				ACE.addUncommitted(ConceptBean.get(selectedObject.getTuple().getNativeId()));
				allTuples = null;
				IdTableModel.this.fireTableDataChanged();
			}
		}

		private class RetireActionListener implements ActionListener {

			public RetireActionListener() {
				super();
			}

			public void actionPerformed(ActionEvent e) {
				try {
					for (Path p : config.getEditingPathSet()) {
						I_IdPart newPart = selectedObject.getTuple()
								.duplicatePart();
						newPart.setPathId(p.getConceptId());
						newPart.setVersion(Integer.MAX_VALUE);
						newPart.setIdStatus(AceConfig.vodb
								.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED
										.getUids()));
						getReferencedConcepts().put(newPart.getIdStatus(), 
								ConceptBean.get(newPart.getIdStatus()));
						selectedObject.getTuple().getIdVersioned()
								.getVersions().add(newPart);
					}
					ACE.addUncommitted(ConceptBean.get(selectedObject.getTuple().getNativeId()));
					allTuples = null;
					IdTableModel.this.fireTableDataChanged();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		JPopupMenu popup;

		JTable table;

		ActionListener retire;

		ActionListener change;

		StringWithIdTuple selectedObject;

		AceFrameConfig config;

		public PopupListener(JTable table, AceFrameConfig config) {
			super();
			this.table = table;
			this.config = config;
			retire = new RetireActionListener();
			change = new ChangeActionListener();
		}

		private void makePopup(MouseEvent e) {
			popup = new JPopupMenu();
			JMenuItem noActionItem = new JMenuItem("");
			popup.add(noActionItem);
			int column = table.columnAtPoint(e.getPoint());
			int row = table.rowAtPoint(e.getPoint());
			selectedObject = (StringWithIdTuple) table
					.getValueAt(row, column);
			JMenuItem changeItem = new JMenuItem("Change");
			popup.add(changeItem);
			changeItem.addActionListener(change);
			JMenuItem retireItem = new JMenuItem("Retire");
			popup.add(retireItem);
			retireItem.addActionListener(retire);
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
					popup.show(e.getComponent(), e.getX(), e.getY());
				} else {
		            JOptionPane.showMessageDialog(table.getTopLevelAncestor(), "You must select at least one path to edit on...");
				}
			}
		}
	}
	public PopupListener makePopupListener(JTable table, AceFrameConfig config) {
		return new PopupListener(table, config);
	}
	public static class IdStatusFieldEditor extends AbstractPopupFieldEditor {

		private static final long serialVersionUID = 1L;

		public IdStatusFieldEditor(AceFrameConfig config) {
			super(config);
		}

		@Override
		public int[] getPopupValues() {
			return config.getEditStatusTypePopup().getSetValues();
		}

		@Override
		public ConceptBean getSelectedItem(Object value) {
			StringWithIdTuple swdt = (StringWithIdTuple) value;
			return ConceptBean.get(swdt.getTuple().getIdStatus());
		}
	}

}
