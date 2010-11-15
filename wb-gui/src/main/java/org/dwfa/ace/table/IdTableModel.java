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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
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

import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.tk.api.PathBI;

public class IdTableModel extends AbstractTableModel implements PropertyChangeListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public static class StringWithIdTuple extends StringWithTuple<StringWithIdTuple>  {
        String cellText;

        I_IdVersion tuple;

        public StringWithIdTuple(String cellText, I_IdVersion tuple, boolean isInConflict) {
            super(cellText, isInConflict);
            this.tuple = tuple;
        }

        public I_IdVersion getTuple() {
            return tuple;
        }
    }

    private List<? extends I_IdVersion> allTuples;

    private TableChangedSwingWorker tableChangeWorker;

    private Set<Integer> conceptsToFetch = new HashSet<Integer>();

    private Map<Integer, I_GetConceptData> referencedConcepts = new HashMap<Integer, I_GetConceptData>();

    public class ReferencedConceptsSwingWorker extends SwingWorker<Boolean> {
        private boolean stopWork = false;

        Map<Integer, I_GetConceptData> concepts;

        @Override
        protected Boolean construct() throws Exception {
            getProgress().setActive(true);
            concepts = new HashMap<Integer, I_GetConceptData>();
            HashSet<Integer> idSetToFetch = null;
            synchronized (conceptsToFetch) {
                idSetToFetch = new HashSet<Integer>(conceptsToFetch);
            }
            for (Integer id : idSetToFetch) {
                if (stopWork) {
                    return false;
                }
                I_GetConceptData b = Terms.get().getConcept(id);
                b.getDescriptions();
                concepts.put(id, b);

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

        public String toString() {
            return "ReferencedConceptsSwingWorker stopWork: " + stopWork + " concepts: " + concepts;
        }
    }

    public class TableChangedSwingWorker extends SwingWorker<Boolean> {
        I_AmTermComponent tc;

        private boolean workStopped = false;

        ReferencedConceptsSwingWorker refConWorker;

        public TableChangedSwingWorker(I_AmTermComponent tc) {
            super();
            this.tc = tc;
        }

        @Override
        protected Boolean construct() throws Exception {
            if (tc == null) {
                return false;
            }
            int nid = getNidFromTermComponent(tc);

            I_Identify id = Terms.get().getId(nid);
            for (I_IdPart part : id.getMutableIdParts()) {
                if (workStopped) {
                    return false;
                }
                conceptsToFetch.add(part.getStatusNid());
                conceptsToFetch.add(part.getPathNid());
                conceptsToFetch.add(part.getAuthorityNid());
                conceptsToFetch.add(part.getAuthorNid());
            }

            if (workStopped) {
                return false;
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
                        getProgress().getProgressBar().setMaximum(conceptsToFetch.size());
                    }
                }
                get();
                fireTableDataChanged();
            } catch (InterruptedException e) {
                ;
            } catch (ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

        public void stop() {
            workStopped = true;
            if (refConWorker != null) {
                refConWorker.stop();
            }
        }

        public String toString() {
            return "TableChangedSwingWorker: " + tc + " workStopped: " + workStopped + "\n" + refConWorker;
        }
    }

    public ID_FIELD[] getColumnEnums() {
        return columns;
    }

    public enum ID_FIELD {
        LOCAL_ID("local id", 5, 100, 100),
        STATUS("status", 5, 50, 250),
        EXT_ID("id", 5, 85, 1550),
        VERSION("time", 5, 140, 140),
        AUTHOR("author", 5, 90, 150),
        PATH("path", 5, 90, 150),
        SOURCE("id source", 5, 50, 250);

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
        if (tableChangeWorker != null) {
            tableChangeWorker.stop();
        }
        conceptsToFetch = new HashSet<Integer>();
        referencedConcepts = new HashMap<Integer, I_GetConceptData>();
        if (getProgress() != null) {
            getProgress().setVisible(true);
            getProgress().getProgressBar().setValue(0);
            getProgress().getProgressBar().setIndeterminate(true);
        }
        tableChangeWorker = new TableChangedSwingWorker((I_AmTermComponent) evt.getNewValue());
        tableChangeWorker.start();
        fireTableDataChanged();
    }

    public String getColumnName(int col) {
        return columns[col].getColumnName();
    }

    public int getColumnCount() {
        return columns.length;
    }

    private I_IdVersion getIdTuple(int rowIndex) throws IOException {
        I_AmTermComponent tc = (I_AmTermComponent) host.getTermComponent();
        if (tc == null) {
            return null;
        }
        if (allTuples == null) {
            try {
                allTuples = Terms.get().getId(getNidFromTermComponent(tc)).getIdVersions();
            } catch (TerminologyException e) {
                throw new ToIoException(e);
            }
        }
        return allTuples.get(rowIndex);
    }

    public int getRowCount() {
        if (allTuples == null) {
            try {
                getIdTuple(0);
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
        if (allTuples == null) {
            return 0;
        }
        return allTuples.size();
    }

    private String getPrefText(int id) throws IOException {
    	I_GetConceptData cb = referencedConcepts.get(id);
        I_DescriptionTuple desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
        if (desc != null) {
            String text = desc.getText();
            return text;
        }
        return "null desc for " + id;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            if (rowIndex >= getRowCount()) {
                return null;
            }
            I_IdVersion idTuple = getIdTuple(rowIndex);
            if (idTuple == null) {
                return null;
            }

            I_ConfigAceFrame config = host.getConfig();
            boolean inConflict = config.getHighlightConflictsInComponentPanel()
                && config.getConflictResolutionStrategy().isInConflict((I_Identify) idTuple.getFixedIdPart());

            switch (columns[columnIndex]) {
            case LOCAL_ID:
                return new StringWithIdTuple(Integer.toString(idTuple.getNid()), idTuple, inConflict);
            case STATUS:
                if (referencedConcepts.containsKey(idTuple.getStatusId())) {
                    return new StringWithIdTuple(getPrefText(idTuple.getStatusId()), idTuple, inConflict);
                }
                return new StringWithIdTuple(Integer.toString(idTuple.getStatusId()), idTuple, inConflict);
            case EXT_ID:
                return new StringWithIdTuple(idTuple.getDenotation().toString(), idTuple, inConflict);
            case VERSION:
                if (idTuple.getVersion() == Integer.MAX_VALUE) {
                    return new StringWithIdTuple(ThinVersionHelper.uncommittedHtml(), idTuple, inConflict);
                }
                return new StringWithIdTuple(ThinVersionHelper.format(idTuple.getVersion()), idTuple, inConflict);
            case PATH:
                if (referencedConcepts.containsKey(idTuple.getPathId())) {
                    return new StringWithIdTuple(getPrefText(idTuple.getPathId()), idTuple, inConflict);
                }
                return new StringWithIdTuple(Integer.toString(idTuple.getPathId()), idTuple, inConflict);
            case SOURCE:
                if (referencedConcepts.containsKey(idTuple.getAuthorityNid())) {
                    return new StringWithIdTuple(getPrefText(idTuple.getAuthorityNid()), idTuple, inConflict);
                }
                return new StringWithIdTuple(Integer.toString(idTuple.getAuthorityNid()), idTuple, inConflict);
            case AUTHOR:
                if (referencedConcepts.containsKey(idTuple.getAuthorNid())) {
                    return new StringWithIdTuple(getPrefText(idTuple.getAuthorNid()), idTuple, inConflict);
                }
                return new StringWithIdTuple(Integer.toString(idTuple.getAuthorNid()), idTuple, inConflict);

            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
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
        case PATH:
            return StringWithIdTuple.class;
        }
        return String.class;
    }

    private int getNidFromTermComponent(I_AmTermComponent tc) {
        int nid = Integer.MIN_VALUE;
        if (I_DescriptionVersioned.class.isAssignableFrom(tc.getClass())) {
            I_DescriptionVersioned dv = (I_DescriptionVersioned) tc;
            nid = dv.getDescId();
        } else if (I_GetConceptData.class.isAssignableFrom(tc.getClass())) {
            I_GetConceptData cb = (I_GetConceptData) tc;
            nid = cb.getConceptNid();
        } else if (I_RelVersioned.class.isAssignableFrom(tc.getClass())) {
            I_RelVersioned rel = (I_RelVersioned) tc;
            nid = rel.getRelId();
        }
        return nid;
    }

    public SmallProgressPanel getProgress() {
        return progress;
    }

    public void setProgress(SmallProgressPanel progress) {
        this.progress = progress;
    }

    public class PopupListener extends MouseAdapter {
        private class ChangeActionListener implements ActionListener {

            public ChangeActionListener() {
                super();
            }

            public void actionPerformed(ActionEvent e) {
                try {
					for (PathBI p : config.getEditingPathSet()) {
					    I_IdPart newPart = selectedObject.getTuple().duplicateIdPart();
					    newPart.setPathId(p.getConceptNid());
					    newPart.setVersion(Integer.MAX_VALUE);
					    selectedObject.getTuple().getIdentifier().addMutableIdPart(newPart);
					}
					Terms.get().addUncommitted(Terms.get().getConcept(selectedObject.getTuple().getNid()));
					allTuples = null;
					IdTableModel.this.fireTableDataChanged();
				} catch (TerminologyException e1) {
					throw new RuntimeException(e1);
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
            }
        }

        private class RetireActionListener implements ActionListener {

            public RetireActionListener() {
                super();
            }

            public void actionPerformed(ActionEvent e) {
                try {
                    for (PathBI p : config.getEditingPathSet()) {
                        I_IdPart newPart = selectedObject.getTuple().duplicateIdPart();
                        newPart.setPathId(p.getConceptNid());
                        newPart.setVersion(Integer.MAX_VALUE);
                        newPart.setStatusId(Terms.get().uuidToNative(
                            ArchitectonicAuxiliary.Concept.RETIRED.getUids()));
                        referencedConcepts.put(newPart.getStatusId(), Terms.get().getConcept(newPart.getStatusId()));
                        selectedObject.getTuple().getIdentifier().addMutableIdPart(newPart);
                    }
                    Terms.get().addUncommitted(Terms.get().getConcept(selectedObject.getTuple().getNid()));
                    allTuples = null;
                    IdTableModel.this.fireTableDataChanged();
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        }

        JPopupMenu popup;

        JTable table;

        ActionListener retire;

        ActionListener change;

        StringWithIdTuple selectedObject;

        I_ConfigAceFrame config;

        public PopupListener(JTable table, I_ConfigAceFrame config) {
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
            selectedObject = (StringWithIdTuple) table.getValueAt(row, column);
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
                    JOptionPane.showMessageDialog(table.getTopLevelAncestor(),
                        "You must select at least one path to edit on...");
                }
            }
            e.consume();
        }
    }

    public PopupListener makePopupListener(JTable table, I_ConfigAceFrame config) {
        return new PopupListener(table, config);
    }

    public static class IdStatusFieldEditor extends AbstractPopupFieldEditor {

        private static final long serialVersionUID = 1L;

        public IdStatusFieldEditor(I_ConfigAceFrame config) throws TerminologyException, IOException {
            super(config);
        }

        @Override
        public int[] getPopupValues() {
            return config.getEditStatusTypePopup().getListArray();
        }

        @Override
        public I_GetConceptData getSelectedItem(Object value) throws TerminologyException, IOException {
            StringWithIdTuple swdt = (StringWithIdTuple) value;
            return Terms.get().getConcept(swdt.getTuple().getStatusId());
        }
    }

}
