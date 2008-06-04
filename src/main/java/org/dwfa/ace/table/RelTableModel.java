package org.dwfa.ace.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_DoConceptDrop;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.log.AceLog;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;

public abstract class RelTableModel extends AbstractTableModel implements PropertyChangeListener, I_DoConceptDrop {
    enum FieldToChange { REFINABILITY, CHARACTERISTIC, TYPE, STATUS};

    List<I_RelTuple> allTuples;

    // protected ConceptPanel parentPanel;

    protected I_GetConceptData tableBean = null;

    private Set<Integer> conceptsToFetch = Collections.synchronizedSet(new HashSet<Integer>());

    Map<Integer, ConceptBean> referencedConcepts = Collections
            .synchronizedMap(new HashMap<Integer, ConceptBean>());

    private TableChangedSwingWorker tableChangeWorker;

    private SmallProgressPanel progress = new SmallProgressPanel();

    public class ReferencedConceptsSwingWorker extends SwingWorker<Map<Integer, ConceptBean>> {
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
                        progress.setProgressInfo("   " + progressBar.getValue() + "/" + progressBar.getMaximum()
                                + "   ");
                        fireTableDataChanged();
                        if (progressBar.getValue() == progressBar.getMaximum()) {
                            normalCompletionForUpdator();
                        }
                    }
                } else {
                    updateTimer.stop();
                }
            }

            public void normalCompletionForUpdator() {
                normalCompletion();
            }

        }

        private boolean stopWork = false;

        ProgressUpdator updator = new ProgressUpdator();

        @Override
        protected Map<Integer, ConceptBean> construct() throws Exception {
            referencedConcepts = Collections.synchronizedMap(new HashMap<Integer, ConceptBean>());
            Set<Integer> fetchSet = null;
            synchronized (conceptsToFetch) {
                fetchSet = new HashSet<Integer>(conceptsToFetch);
            }
            for (Integer id : fetchSet) {
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
                updator.normalCompletionForUpdator();
                fireTableDataChanged();
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
            updator.normalCompletionForUpdator();
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
            List<I_RelTuple> rels = getRels(cb, host.getUsePrefs(), getShowHistory());
            for (I_RelTuple r : rels) {
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
                    progress.getProgressBar().setMaximum(conceptsToFetch.size());
                }
            }
            if (stopWork) {
                fireTableDataChanged();
                return;
            }
            try {
                get();
                normalCompletion();
            } catch (InterruptedException e) {
                ;
            } catch (ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
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
        REL_ID("rid", 5, 100, 100), SOURCE_ID("origin", 5, 300, 1000), REL_TYPE("type", 5, 120, 500), DEST_ID(
                "destination", 5, 300, 1000), GROUP("group", 5, 36, 46), REFINABILITY("refinability", 5, 80, 180), CHARACTERISTIC(
                "char", 5, 70, 70), STATUS("status", 5, 50, 250), VERSION("version", 5, 140, 140), PATH("path", 5,
                90, 180);

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
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return 0;
    }

    public abstract List<I_RelTuple> getRels(I_GetConceptData cb, boolean usePrefs, boolean showHistory)
            throws IOException;

    public Object getValueAt(int rowIndex, int columnIndex) {
        I_GetConceptData cb = (I_GetConceptData) host.getTermComponent();
        if (cb == null) {
            return null;
        }
        REL_FIELD field = columns[columnIndex];
        try {
            I_RelTuple rel;
            if (rowIndex >= allTuples.size() || rowIndex < 0) {
                return null;
            }
            rel = allTuples.get(rowIndex);

            switch (field) {
            case REL_ID:
                return new StringWithRelTuple(Integer.toString(rel.getRelId()), rel);
            case SOURCE_ID:
                if (referencedConcepts.containsKey(rel.getC1Id())) {
                    return new StringWithRelTuple(getPrefText(rel.getC1Id()), rel);
                }
                return new StringWithRelTuple(Integer.toString(rel.getC1Id()), rel);
            case REL_TYPE:
                if (referencedConcepts.containsKey(rel.getRelTypeId())) {
                    return new StringWithRelTuple(getPrefText(rel.getRelTypeId()), rel);
                }
                return new StringWithRelTuple(Integer.toString(rel.getRelTypeId()), rel);
            case DEST_ID:
                if (referencedConcepts.containsKey(rel.getC2Id())) {
                    return new StringWithRelTuple(getPrefText(rel.getC2Id()), rel);
                }
                return new StringWithRelTuple(Integer.toString(rel.getC2Id()), rel);
            case GROUP:
                return new StringWithRelTuple(Integer.toString(rel.getGroup()), rel);
            case REFINABILITY:
                if (referencedConcepts.containsKey(rel.getRefinabilityId())) {
                    return new StringWithRelTuple(getPrefText(rel.getRefinabilityId()), rel);
                }
                return new StringWithRelTuple(Integer.toString(rel.getRefinabilityId()), rel);
            case CHARACTERISTIC:
                if (referencedConcepts.containsKey(rel.getCharacteristicId())) {
                    return new StringWithRelTuple(getPrefText(rel.getCharacteristicId()), rel);
                }
                return new StringWithRelTuple(Integer.toString(rel.getCharacteristicId()), rel);
            case STATUS:
                if (referencedConcepts.containsKey(rel.getStatusId())) {
                    return new StringWithRelTuple(getPrefText(rel.getStatusId()), rel);
                }
                return new StringWithRelTuple(Integer.toString(rel.getStatusId()), rel);
            case VERSION:
                if (rel.getVersion() == Integer.MAX_VALUE) {
                    return new StringWithRelTuple(ThinVersionHelper.uncommittedHtml(), rel);
                }
                return new StringWithRelTuple(ThinVersionHelper.format(rel.getVersion()), rel);
            case PATH:
                if (referencedConcepts.containsKey(rel.getPathId())) {
                    try {
						return new StringWithRelTuple(getPrefText(rel.getPathId()), rel);
					} catch (Exception e) {
						new StringWithRelTuple(Integer.toString(rel.getPathId()) + " no pref text...", rel);;
					}
                }
                return new StringWithRelTuple(Integer.toString(rel.getPathId()), rel);
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return "No case found for: " + field;
    }

    private String getPrefText(int id) throws IOException {
        ConceptBean cb = referencedConcepts.get(id);
        I_DescriptionTuple desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
        if (desc != null) {
            return desc.getText();
        }
        cb = referencedConcepts.get(id);
        desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
        return cb.getInitialText();
    }

    public String getColumnName(int col) {
        return columns[col].getColumnName();
    }

    public boolean isCellEditable(int row, int col) {
       if (ACE.editMode == false) {
          return false;
       }
        if (row >= allTuples.size()) {
            return false;
        }
        I_RelTuple rel = allTuples.get(row);
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
            /*
             * if (rel.getFixedPart().getTuples().size() == 1) { return true; }
             * else { return allUncommitted(rel); }
             */
        case DEST_ID:
            return true;
            /*
             * if (rel.getFixedPart().getTuples().size() == 1) { return true; }
             * else { return allUncommitted(rel); }
             */
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
        case PATH:
            return false;
        }
        return false;
    }


    public void setValueAt(Object value, int row, int col) {
        I_RelTuple rel = allTuples.get(row);
        REL_FIELD field = columns[col];
		boolean changed = false;
        switch (field) {
        case REL_ID:
            break;
        case SOURCE_ID:
            break;
        case REL_TYPE:
            Integer typeId = (Integer) value;
            rel.setRelTypeId(typeId);
            referencedConcepts.put(typeId, ConceptBean.get(typeId));
			changed = true;
            break;
        case DEST_ID:
            Integer destId = (Integer) value;
            rel.getFixedPart().setC2Id(destId);
            referencedConcepts.put(destId, ConceptBean.get(destId));
			changed = true;
            break;
        case GROUP:
            if (String.class.isAssignableFrom(value.getClass())) {
                String valueStr = (String) value;
                rel.setGroup(Integer.parseInt(valueStr));
            } else {
                rel.setGroup((Integer) value);
            }
			changed = true;
            break;
        case REFINABILITY:
            Integer refinabilityId = (Integer) value;
            rel.setRefinabilityId(refinabilityId);
            referencedConcepts.put(refinabilityId, ConceptBean.get(refinabilityId));
			changed = true;
            break;
        case CHARACTERISTIC:
            Integer characteristicId = (Integer) value;
            rel.setCharacteristicId(characteristicId);
            referencedConcepts.put(characteristicId, ConceptBean.get(characteristicId));
			changed = true;
            break;
        case STATUS:
            Integer statusId = (Integer) value;
            rel.setStatusId(statusId);
            referencedConcepts.put(statusId, ConceptBean.get(statusId));
			changed = true;
            break;
        case VERSION:
            break;
        case PATH:
            break;
        }
        fireTableDataChanged();
		if (changed) {
			AceLog.getAppLog().info("Rel table changed");
			updateDataAlerts(row);
		}
    }

	java.util.Timer timer = new java.util.Timer("updateDataAlertsTimer");
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
							I_RelTuple rel = allTuples.get(row);
							ACE.addUncommitted(ConceptBean.get(rel.getC1Id()));
						}
					}});
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
		timer.schedule(alertUpdater, 2000);
		
	}

    public Class<?> getColumnClass(int c) {
        return StringWithRelTuple.class;
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
        I_GetConceptData tableConcept = (I_GetConceptData) evt.getNewValue();
        updateTable(tableConcept);
    }

    void updateTable(I_GetConceptData tableConcept) {
        tableChangeWorker = new TableChangedSwingWorker(tableConcept);
        tableChangeWorker.start();
    }

    public SmallProgressPanel getProgress() {
        return progress;
    }

    public void setProgress(SmallProgressPanel progress) {
        this.progress = progress;
    }

    public static class StringWithRelTuple implements Comparable<StringWithRelTuple>, I_CellTextWithTuple {
        String cellText;

        I_RelTuple tuple;

        public StringWithRelTuple(String cellText, I_RelTuple tuple) {
            super();
            this.cellText = cellText;
            this.tuple = tuple;
        }

        public String getCellText() {
            return cellText;
        }

        public I_RelTuple getTuple() {
            return tuple;
        }

        public String toString() {
            return cellText;
        }

        public int compareTo(StringWithRelTuple another) {
            return cellText.compareTo(another.cellText);
        }
    }

    public REL_FIELD[] getColumnEnums() {
        return columns;
    }

    public static class RelStatusFieldEditor extends AbstractPopupFieldEditor {

        private static final long serialVersionUID = 1L;

        public RelStatusFieldEditor(I_ConfigAceFrame config) {
            super(config);
        }

        @Override
        public int[] getPopupValues() {
            return config.getEditStatusTypePopup().getListArray();
        }

        @Override
        public ConceptBean getSelectedItem(Object value) {
            StringWithRelTuple swdt = (StringWithRelTuple) value;
            return ConceptBean.get(swdt.getTuple().getStatusId());
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

    public static class RelRefinabilityFieldEditor extends AbstractPopupFieldEditor {

        private static final long serialVersionUID = 1L;

        public RelRefinabilityFieldEditor(I_ConfigAceFrame config) {
            super(config);
        }

        @Override
        public int[] getPopupValues() {
            return config.getEditRelRefinabiltyPopup().getListArray();
        }

        @Override
        public ConceptBean getSelectedItem(Object value) {
            StringWithRelTuple swdt = (StringWithRelTuple) value;
            return ConceptBean.get(swdt.getTuple().getRefinabilityId());
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

	public static class RelGroupFieldEditor extends DefaultCellEditor {

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
		public RelGroupFieldEditor() {
			super(new JTextField());
			textField = new JTextField();
			textField.addFocusListener(new TextFieldFocusListener());
			editorComponent = textField;

			delegate = new EditorDelegate() {
				private static final long serialVersionUID = 1L;

				public void setValue(Object value) {
					if (StringWithRelTuple.class.isAssignableFrom(value
							.getClass())) {
						StringWithRelTuple swrt = (StringWithRelTuple) value;
						textField.setText((value != null) ? swrt.cellText : "");
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
	}

    
    public static class RelCharactisticFieldEditor extends AbstractPopupFieldEditor {

        private static final long serialVersionUID = 1L;

        public RelCharactisticFieldEditor(I_ConfigAceFrame config) {
            super(config);
        }

        @Override
        public int[] getPopupValues() {
            return config.getEditRelCharacteristicPopup().getListArray();
        }

        @Override
        public ConceptBean getSelectedItem(Object value) {
            StringWithRelTuple swdt = (StringWithRelTuple) value;
            return ConceptBean.get(swdt.getTuple().getCharacteristicId());
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

    public static class RelTypeFieldEditor extends AbstractPopupFieldEditor {

        private static final long serialVersionUID = 1L;

        public RelTypeFieldEditor(I_ConfigAceFrame config) {
            super(config);
        }

        @Override
        public int[] getPopupValues() {
            return config.getEditRelTypePopup().getListArray();
        }

        @Override
        public ConceptBean getSelectedItem(Object value) {
            StringWithRelTuple swdt = (StringWithRelTuple) value;
            return ConceptBean.get(swdt.getTuple().getRelTypeId());
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

    public RelPopupListener makePopupListener(JTable table, I_ConfigAceFrame config) {
        return new RelPopupListener(table, config, this);
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

    public void normalCompletion() {
        if (progress != null) {
            progress.setProgressInfo("   " + getRowCount() + "   ");
            progress.setActive(false);
        }
    }

    public boolean getShowHistory() {
        return host.getShowHistory();
    }

}