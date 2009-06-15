package org.dwfa.ace.table.refset;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_HoldRefsetData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.timer.UpdateAlertsTimer;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.ThinExtByRefTuple;
import org.dwfa.vodb.types.ThinExtByRefVersioned;

public class ReflexiveRefsetMemberTableModel extends AbstractTableModel implements
		PropertyChangeListener, I_HoldRefsetData {

	public static class StringExtFieldEditor extends DefaultCellEditor {

		private static final long serialVersionUID = 1L;

		private class TextFieldFocusListener implements FocusListener {

			public void focusGained(FocusEvent e) {
				// nothing to do
			}

			public void focusLost(FocusEvent e) {
				delegate.stopCellEditing();
			}

		}

		JTextField textField;
		int row;
		int column;
		ReflexiveRefsetFieldData field;

		public StringExtFieldEditor(ReflexiveRefsetFieldData field) {
			super(new JTextField());
			textField = new JTextField();
			textField.addFocusListener(new TextFieldFocusListener());
			editorComponent = textField;
			this.field = field;

			delegate = new EditorDelegate() {
				private static final long serialVersionUID = 1L;

				public void setValue(Object value) {
					if (StringWithExtTuple.class.isAssignableFrom(value
							.getClass())) {
						StringWithExtTuple swet = (StringWithExtTuple) value;
						textField.setText((value != null) ? swet.getCellText()
								: "");
					} else {
						textField.setText((value != null) ? value.toString()
								: "");
					}
				}

				public Object getCellEditorValue() {
					return textField.getText();
				}
			};
			textField.addActionListener(delegate);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			this.row = row;
			this.column = column;
			((JComponent) getComponent())
					.setBorder(new LineBorder(Color.black));
			return super.getTableCellEditorComponent(table, value, isSelected,
					row, column);
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

		public String getSelectedItem(Object value) {
			StringWithExtTuple swet = (StringWithExtTuple) value;
			try {
				return (String) field.getReadMethod().invoke(swet.tuple);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class ConceptFieldEditor extends DefaultCellEditor {

		private static final long serialVersionUID = 1L;
		private JComboBox combo;

		I_ConfigAceFrame config;

		ReflexiveRefsetFieldData field;
		private IntList popupIds;

		public ConceptFieldEditor(I_ConfigAceFrame config, IntList popupIds,
				ReflexiveRefsetFieldData field) {
			super(new JComboBox());
			this.popupIds = popupIds;
			this.field = field;
			combo = new JComboBox();
			combo.setMaximumRowCount(20);
			this.config = config;
			populatePopup();
			editorComponent = combo;

			delegate = new EditorDelegate() {
				private static final long serialVersionUID = 1L;

				public void setValue(Object value) {
					combo.setSelectedItem(getSelectedItem(value));
				}

				public Object getCellEditorValue() {
					return ((ConceptBean) combo.getSelectedItem())
							.getConceptId();
				}
			};
			combo.addActionListener(delegate);
		}

		private void populatePopup() {
			combo.removeAllItems();
			for (int id : getPopupValues()) {
				combo.addItem(ConceptBean.get(id));
			}
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			populatePopup();
			return super.getTableCellEditorComponent(table, value, isSelected,
					row, column);
		}

		public int[] getPopupValues() {
			return popupIds.getListArray();
		}

		public ConceptBean getSelectedItem(Object value) {
			StringWithExtTuple swet = (StringWithExtTuple) value;
			if (field.getType() == REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER) {
				try {
					return ConceptBean.get((Integer) field.getReadMethod().invoke(swet.tuple));
				} catch (Exception e) {
					throw new RuntimeException(e);
				} 
			}
			throw new UnsupportedOperationException(
					"Can't do concept combobox on " + field);
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
		
	private ReflexiveRefsetFieldData[] columns;

	private SmallProgressPanel progress = new SmallProgressPanel();

	I_HostConceptPlugins host;

	List<ThinExtByRefTuple> allTuples;

	ArrayList<ThinExtByRefVersioned> allExtensions;

	Map<Integer, ConceptBean> referencedConcepts = new HashMap<Integer, ConceptBean>();

	private Set<Integer> conceptsToFetch = new HashSet<Integer>();

	private TableChangedSwingWorker tableChangeWorker;

	private ReferencedConceptsSwingWorker refConWorker;

	private int tableComponentId = Integer.MIN_VALUE;

	private JButton addButton = new JButton();

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

	public class TableChangedSwingWorker extends SwingWorker<Boolean> {
		Integer memberId;

		private boolean stopWork = false;

		public TableChangedSwingWorker(Integer componentId) {
			super();
			this.memberId = componentId;
		}

		@Override
		protected Boolean construct() throws Exception {
			if (refConWorker != null) {
				refConWorker.stop();
			}
			if (memberId == null || memberId == Integer.MIN_VALUE) {
				return true;
			}
			I_ThinExtByRefVersioned extension = null;
			if (AceConfig.getVodb().hasExtension(memberId)) {
				extension = AceConfig.getVodb().getExtension(memberId);
			} else {
				extension = ExtensionByReferenceBean.getNewExtensionMember(memberId);
			}

			if (stopWork || extension == null) {
				return false;
			}
			for (I_ThinExtByRefPart part : extension.getVersions()) {
				ThinExtByRefTuple ebrTuple = new ThinExtByRefTuple(extension, part);
				for (ReflexiveRefsetFieldData col: columns) {
					if (col.getType() == REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER) {
						if (col.invokedOnPart) {
							conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple.getPart()));
						} else {
							conceptsToFetch.add((Integer) col.getReadMethod().invoke(ebrTuple));
						}
					}

				}
				if (stopWork) {
					return false;
				}
				if (allTuples == null) {
					AceLog.getAppLog()
					.info("all tuples for RefsetMemberTableModel is  null");
					return false;
				}
				allTuples.add(ebrTuple);
			}


			refConWorker = new ReferencedConceptsSwingWorker();
			refConWorker.start();
			return true;
		}

		@Override
		protected void finished() {
			super.finished();
			try {
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
				if (get()) {
					tableComponentId = memberId;
				}
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

	public ReflexiveRefsetMemberTableModel(I_HostConceptPlugins host,
			ReflexiveRefsetFieldData[] columns) {
		super();
		this.columns = columns;
		this.host = host;
		this.host.addPropertyChangeListener(
				I_ContainTermComponent.TERM_COMPONENT, this);
	}

	public SmallProgressPanel getProgress() {
		return progress;
	}

	public void setProgress(SmallProgressPanel progress) {
		this.progress = progress;
	}

	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int col) {
		return columns[col].getColumnName();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		if (tableChangeWorker != null) {
			tableChangeWorker.stopWork = true;
		}
		allTuples = null;
		allExtensions = null;
		if (getProgress() != null) {
			getProgress().setVisible(true);
			getProgress().getProgressBar().setValue(0);
			getProgress().getProgressBar().setIndeterminate(true);
		}
		fireTableDataChanged();
	}

	public void setComponentId(int componentId) throws Exception {
		this.tableComponentId = componentId;
		if (ACE.editMode) {
			this.addButton
					.setEnabled(this.tableComponentId != Integer.MIN_VALUE);
		}
		propertyChange(null);
	}

	public int getRowCount() {
		if (tableComponentId == Integer.MIN_VALUE) {
			return 0;
		}
		if (allTuples == null) {
			allTuples = new ArrayList<ThinExtByRefTuple>();
			if (tableChangeWorker != null) {
				tableChangeWorker.stop();
			}
			conceptsToFetch.clear();
			referencedConcepts.clear();
			tableChangeWorker = new TableChangedSwingWorker(tableComponentId);
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					tableChangeWorker.start();
				}

			});
			return 0;
		}
		return allTuples.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (allTuples == null) {
			return null;
		}
		try {
			I_ThinExtByRefTuple tuple = allTuples.get(rowIndex);
			Object value = null;
			if (columns[columnIndex].invokedOnPart) {
				value = columns[columnIndex].getReadMethod().invoke(tuple.getPart());
			} else {
				value = columns[columnIndex].getReadMethod().invoke(tuple);
			}
			switch (columns[columnIndex].getType()) {
			case CONCEPT_IDENTIFIER:
				int conceptId = (Integer) value;
				if (referencedConcepts.containsKey(conceptId)) {
					return new StringWithExtTuple(getPrefText(conceptId), tuple);
				}
				return new StringWithExtTuple(Integer.toString(conceptId), tuple);
			case COMPONENT_IDENTIFIER:
				int componentId = (Integer) value;
				return new StringWithExtTuple(Integer.toString(componentId), tuple);

			case VERSION:
				if (tuple.getVersion() == Integer.MAX_VALUE) {
					return new StringWithExtTuple(ThinVersionHelper
							.uncommittedHtml(), tuple);
				}
				return new StringWithExtTuple(ThinVersionHelper.format(tuple
						.getVersion()), tuple);

				// String extension
			case STRING:
				return new StringWithExtTuple((String) value, tuple);
			}
			
				
			AceLog.getAppLog().alertAndLogException(
					new Exception("Can't handle column type: "
							+ columns[columnIndex]));
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return null;
	}

	private String getPrefText(int id) throws IOException {
		ConceptBean cb = referencedConcepts.get(id);
		I_DescriptionTuple desc = cb.getDescTuple(host.getConfig()
				.getTableDescPreferenceList(), host.getConfig());
		if (desc != null) {
			return desc.getText();
		}
		cb = referencedConcepts.get(id);
		desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(),
				host.getConfig());
		return cb.getInitialText();
	}

	public ReflexiveRefsetFieldData[] getColumns() {
		return columns;
	}

	public ReflexiveRefsetFieldData[] getFieldsForPopup() {
		return columns;
	}

	public boolean isCellEditable(int row, int col) {
		if (ACE.editMode == false) {
			return false;
		}
		if (columns[col].isCreationEditable() == false) {
			return false;
		}
		if (allTuples.get(row).getVersion() == Integer.MAX_VALUE) {
			if (columns[col].isUpdateEditable() == false) {
				if (allTuples.get(row).getVersions().size() > 1) {
					return false;
				}
			}
			if (AceLog.getAppLog().isLoggable(Level.FINER)) {
				AceLog.getAppLog()
						.finer("Cell is editable: " + row + " " + col);
			}
			return true;
		}
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
		if (columns[col].isCreationEditable() || columns[col].isUpdateEditable()) {
			I_ThinExtByRefTuple extTuple = allTuples.get(row);
			boolean changed = false;
			if (extTuple.getVersion() == Integer.MAX_VALUE) {
				switch (columns[col].getType()) {
				case CONCEPT_IDENTIFIER:
					Integer identifier = (Integer) value;					
					referencedConcepts.put(identifier, ConceptBean.get(identifier));
				default:
					try {
						if (columns[col].invokedOnPart) {
							columns[col].getWriteMethod().invoke(extTuple.getPart(), value);
						} else {
							columns[col].getWriteMethod().invoke(extTuple, value);
						}
						changed = true;
					} catch (Exception e) {
						AceLog.getAppLog().alertAndLogException(e);
					} 
				}
				if (changed) {
					fireTableDataChanged();
					AceLog.getAppLog().info("refset table changed");
					updateDataAlerts(row);
				}
			}
		}
	}

	private class UpdateDataAlertsTimerTask extends TimerTask {
		boolean active = true;
		final int row;

		public UpdateDataAlertsTimerTask(int row) {
			super();
			this.row = row;
		}

		@Override
		public void run() {
			if (active) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (active) {
							ThinExtByRefTuple tuple = allTuples.get(row);
							ACE.addUncommitted(ExtensionByReferenceBean
									.get(tuple.getMemberId()));
						}
					}
				});
			}
		}
		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

	}

	UpdateDataAlertsTimerTask alertUpdater;

	private void updateDataAlerts(int row) {
		if (alertUpdater != null) {
			alertUpdater.setActive(false);
		}
		alertUpdater = new UpdateDataAlertsTimerTask(row);
		UpdateAlertsTimer.schedule(alertUpdater, 2000);

	}

	public Class<?> getColumnClass(int c) {
		return columns[c].getFieldClass();
	}
}
