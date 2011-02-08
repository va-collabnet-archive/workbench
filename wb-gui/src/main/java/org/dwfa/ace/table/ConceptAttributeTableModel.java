/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.table;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.timer.UpdateAlertsTimer;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ConceptAttributeTableModel extends AbstractTableModel implements PropertyChangeListener {

    enum FieldToChange {
        DEFINED, STATUS
    };

    public static class ConceptStatusFieldEditor extends AbstractPopupFieldEditor {

        private static final long serialVersionUID = 1L;

        public ConceptStatusFieldEditor(I_ConfigAceFrame config) throws TerminologyException, IOException {
            super(config);
        }

        @Override
        public int[] getPopupValues() {
            return config.getEditStatusTypePopup().getListArray();
        }

        @Override
        public I_GetConceptData getSelectedItem(Object value) throws TerminologyException, IOException {
            StringWithConceptTuple swdt = (StringWithConceptTuple) value;
            return Terms.get().getConcept(swdt.getTuple().getStatusNid());
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

    List<? extends I_ConceptAttributeTuple> allTuples;

    private TableChangedSwingWorker tableChangeWorker;

    private ReferencedConceptsSwingWorker refConWorker;

    private Set<Integer> conceptsToFetch = new ConcurrentSkipListSet<Integer>();

    Map<Integer, I_GetConceptData> referencedConcepts = new HashMap<Integer, I_GetConceptData>();

    public class ReferencedConceptsSwingWorker extends SwingWorker<Boolean> {
        private boolean stopWork = false;
        private HashMap<Integer, I_GetConceptData> concepts;

        @Override
        protected Boolean construct() throws Exception {
            getProgress().setActive(true);
            concepts = new HashMap<Integer, I_GetConceptData>();
            Set<Integer> fetchSet = null;
            synchronized (conceptsToFetch) {
                fetchSet = new ConcurrentSkipListSet<Integer>(conceptsToFetch);
            }
            for (Integer id : fetchSet) {
                if (stopWork) {
                    return false;
                }
                I_GetConceptData b = Terms.get().getConcept(id);
                b.getDescriptions();
                concepts.put(id, b);
            }
            if (stopWork) {
                return false;
            }
            return true;
        }

        @Override
        protected void finished() {
            super.finished();
            try {
                if (get()) {
                    if (stopWork) {
                        return;
                    }
                    if (getProgress() != null) {
                        getProgress().getProgressBar().setIndeterminate(false);
                        if (conceptsToFetch.size() == 0) {
                            getProgress().getProgressBar().setValue(1);
                        } else {
                            getProgress().getProgressBar().setValue(conceptsToFetch.size());
                        }
                    }
                    referencedConcepts = concepts;
                    fireTableDataChanged();
                    if (getProgress() != null) {
                        getProgress().setProgressInfo("   " + getRowCount() + "   ");
                        getProgress().setActive(false);
                    }
                }
            } catch (InterruptedException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

        public void stop() {
            stopWork = true;
        }

    }

    public class TableChangedSwingWorker extends SwingWorker<Boolean> {
        I_GetConceptData cb;

        private boolean stopWork = false;

        public TableChangedSwingWorker(I_GetConceptData cb) {
            super();
            this.cb = cb;
        }

        @Override
        protected Boolean construct() throws Exception {
            if (refConWorker != null) {
                refConWorker.stop();
            }
            if ((cb == null) || (cb.getConceptAttributes() == null)) {
                return true;
            }
            List<? extends I_ConceptAttributeTuple> tuples = getConceptTuples(cb);
            for (I_ConceptAttributeTuple conVersion : tuples) {
                conceptsToFetch.add(conVersion.getStatusNid());
                conceptsToFetch.add(conVersion.getPathNid());
                if (stopWork) {
                    return false;
                }
            }

            refConWorker = new ReferencedConceptsSwingWorker();
            refConWorker.start();
            return true;
        }

        @Override
        protected void finished() {
            super.finished();
            try {
                if (get()) {
                    if (getProgress() != null) {
                        getProgress().getProgressBar().setIndeterminate(false);
                        if (conceptsToFetch.size() == 0) {
                            getProgress().getProgressBar().setValue(1);
                            getProgress().getProgressBar().setMaximum(1);
                        } else {
                            getProgress().getProgressBar().setValue(1);
                            getProgress().getProgressBar().setMaximum(conceptsToFetch.size());
                        }
                    }
                    fireTableDataChanged();
                }
            } catch (InterruptedException e) {
                ;
            } catch (ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

        public void stop() {
            stopWork = true;
        }

    }

    public enum CONCEPT_FIELD {
        CON_ID("cid", 5, 100, 100), 
        STATUS("status", 5, 50, 250),
        DEFINED("defined", 5, 85, 1550),
        AUTHOR("author", 5, 90, 150),
        VERSION("time", 5, 140, 140),
        PATH("path", 5, 90, 150);

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

            I_ConfigAceFrame config = host.getConfig();
            boolean inConflict = config.getHighlightConflictsInComponentPanel()
                && config.getConflictResolutionStrategy().isInConflict(
                    (I_ConceptAttributeVersioned) conTuple.getFixedPart());

            switch (columns[columnIndex]) {
            case CON_ID:
                return new StringWithConceptTuple(Integer.toString(conTuple.getNid()), conTuple, inConflict);
            case STATUS:
                if (getReferencedConcepts().containsKey(conTuple.getStatusId())) {
                    return new StringWithConceptTuple(getPrefText(conTuple.getStatusId()), conTuple, inConflict);
                }
                return new StringWithConceptTuple(Integer.toString(conTuple.getStatusId()), conTuple, inConflict);
            case DEFINED:
                return new StringWithConceptTuple(Boolean.toString(conTuple.isDefined()), conTuple, inConflict);
            case VERSION:
                if (conTuple.getTime() == Long.MAX_VALUE) {
                    return new StringWithConceptTuple(ThinVersionHelper.uncommittedHtml(), conTuple, inConflict);
                } else if (conTuple.getTime() == Long.MIN_VALUE) {
                    return new StringWithConceptTuple(ThinVersionHelper.canceledHtml(), conTuple, inConflict);
                }
                return new StringWithConceptTuple(ThinVersionHelper.format(conTuple.getVersion()), conTuple, inConflict);
            case PATH:
                if (getReferencedConcepts().containsKey(conTuple.getPathId())) {
                    try {
                        return new StringWithConceptTuple(getPrefText(conTuple.getPathId()), conTuple, inConflict);
                    } catch (Exception e) {
                        return new StringWithConceptTuple(Integer.toString(conTuple.getPathId()), conTuple, inConflict);
                    }
                }
                return new StringWithConceptTuple(Integer.toString(conTuple.getPathId()), conTuple, inConflict);
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }

    private String getPrefText(int id) throws IOException {
        I_GetConceptData cb = getReferencedConcepts().get(id);
        I_DescriptionTuple desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
        if (desc != null) {
            return desc.getText();
        }
        cb = getReferencedConcepts().get(id);
        desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
        return cb.getInitialText();
    }

    private I_ConceptAttributeTuple getConceptTuple(int rowIndex) throws IOException {
        I_GetConceptData cb = (I_GetConceptData) host.getTermComponent();
        if ((cb == null) || (cb.getConceptAttributes() == null)) {
            return null;
        }
        if (allTuples == null) {
            allTuples = getConceptTuples(cb);
        }
        if (rowIndex < allTuples.size()) {
            return allTuples.get(rowIndex);
        }
        return null;
    }

    private List<? extends I_ConceptAttributeTuple> getConceptTuples(I_GetConceptData cb) throws IOException {
    	PositionSetReadOnly positions = null;
        positions = host.getConfig().getViewPositionSetReadOnly();
        if (host.getShowHistory()) {
            positions = null;
        }
        try {
            return cb.getConceptAttributeTuples(null, positions, 
                host.getConfig().getPrecedence(), host.getConfig().getConflictResolutionStrategy());
        } catch (TerminologyException e) {
            throw new ToIoException(e);
        }
    }

    public String getColumnName(int col) {
        return columns[col].getColumnName();
    }

    public boolean isCellEditable(int row, int col) {
        if (ACE.editMode == false) {
            return false;
        }
        try {
            if (row < 0 || row >= allTuples.size()) {
                return false;
            }
            if (getConceptTuple(row).getTime() != Long.MAX_VALUE) {
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
        case PATH:
            return false;
        }
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        boolean changed = false;
        try {
            switch (columns[col]) {
            case CON_ID:
                break;
            case STATUS:
                Integer statusId = (Integer) value;
                getConceptTuple(row).setStatusId(statusId);
                getReferencedConcepts().put(statusId, Terms.get().getConcept(statusId));
                changed = true;
                break;
            case DEFINED:
                getConceptTuple(row).setDefined((Boolean) value);
                changed = true;
                break;
            case VERSION:
                break;
            case PATH:
                break;
            }
            fireTableCellUpdated(row, col);
            if (changed) {
                AceLog.getAppLog().info("Attributes table changed");
                updateDataAlerts(row);
                Terms.get().addUncommitted(Terms.get().getConcept(getConceptTuple(row).getNid()));
            }
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
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
            if (isActive()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (isActive()) {
                            I_ConceptAttributeTuple tuple = allTuples.get(row);
                            try {
								Terms.get().addUncommitted(Terms.get().getConcept(tuple.getNid()));
							} catch (TerminologyException e) {
								throw new RuntimeException(e);
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
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

    public void propertyChange(PropertyChangeEvent evt) {
        allTuples = null;
        if (getProgress() != null) {
            getProgress().setVisible(true);
            getProgress().getProgressBar().setValue(0);
            getProgress().getProgressBar().setIndeterminate(true);
        }
        if (tableChangeWorker != null) {
            tableChangeWorker.stop();
        }
        conceptsToFetch.clear();
        referencedConcepts.clear();
        tableChangeWorker = new TableChangedSwingWorker((I_GetConceptData) evt.getNewValue());
        tableChangeWorker.start();
        fireTableDataChanged();
    }

    public Map<Integer, I_GetConceptData> getReferencedConcepts() {
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
        case PATH:
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

    public static class StringWithConceptTuple extends StringWithTuple<StringWithConceptTuple> {
        String cellText;

        I_ConceptAttributeTuple tuple;

        public StringWithConceptTuple(String cellText, I_ConceptAttributeTuple tuple, boolean inConflict) {
            super(cellText, inConflict);
            this.tuple = tuple;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.dwfa.ace.table.I_CellTextWithTuple#getTuple()
         */
        public I_ConceptAttributeTuple getTuple() {
            return tuple;
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
