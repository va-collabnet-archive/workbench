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
package org.dwfa.ace.table.refset;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
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
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_HoldRefsetData;
import org.dwfa.ace.api.I_HoldRefsetPreferences;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidFloat;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.I_RefsetDefaults;
import org.dwfa.ace.refset.I_RefsetDefaultsBoolean;
import org.dwfa.ace.refset.I_RefsetDefaultsConInt;
import org.dwfa.ace.refset.I_RefsetDefaultsConcept;
import org.dwfa.ace.refset.I_RefsetDefaultsInteger;
import org.dwfa.ace.refset.I_RefsetDefaultsString;
import org.dwfa.ace.refset.I_RefsetsDefaultsConConCon;
import org.dwfa.ace.timer.UpdateAlertsTimer;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.IntList;

public class RefsetMemberTableModel extends AbstractTableModel implements PropertyChangeListener, I_HoldRefsetData,
        ActionListener {

    public static class StringExtFieldEditor extends DefaultCellEditor {

        private static final long serialVersionUID = 1L;

        private class TextFieldFocusListener implements FocusListener {

            @Override
            public void focusGained(FocusEvent e) {
                // nothing to do
            }

            @Override
            public void focusLost(FocusEvent e) {
                delegate.stopCellEditing();
            }
        }
        JTextField textField;
        int row;
        int column;
        REFSET_FIELDS field = REFSET_FIELDS.STRING_VALUE;

        public StringExtFieldEditor() {
            super(new JTextField());
            textField = new JTextField();
            textField.addFocusListener(new TextFieldFocusListener());
            editorComponent = textField;

            delegate = new EditorDelegate() {

                private static final long serialVersionUID = 1L;

                @Override
                public void setValue(Object value) {
                    if (StringWithExtTuple.class.isAssignableFrom(value.getClass())) {
                        StringWithExtTuple swet = (StringWithExtTuple) value;
                        textField.setText((value != null) ? swet.getCellText() : "");
                    } else {
                        textField.setText((value != null) ? value.toString() : "");
                    }
                }

                @Override
                public Object getCellEditorValue() {
                    return textField.getText();
                }
            };
            textField.addActionListener(delegate);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            this.column = column;
            ((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
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

            switch (field) {
                case STRING_VALUE:
                    return ((I_ExtendByRefPartStr) swet.getTuple().getMutablePart()).getStringValue();
                default:
                    throw new UnsupportedOperationException("Can't do string combobox on " + field);

            }
        }
    }

    public static class ConceptFieldEditor extends DefaultCellEditor {

        private static final long serialVersionUID = 1L;
        private JComboBox combo;
        I_ConfigAceFrame config;
        REFSET_FIELDS field;
        private IntList popupIds;

        public ConceptFieldEditor(I_ConfigAceFrame config, IntList popupIds, REFSET_FIELDS field)
                throws TerminologyException, IOException {
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

                @Override
                public void setValue(Object value) {
                    combo.setSelectedItem(getSelectedItem(value));
                }

                @Override
                public Object getCellEditorValue() {
                    return ((I_GetConceptData) combo.getSelectedItem()).getConceptNid();
                }
            };
            combo.addActionListener(delegate);
        }

        private void populatePopup() throws TerminologyException, IOException {
            combo.removeAllItems();
            for (int id : getPopupValues()) {
                combo.addItem(Terms.get().getConcept(id));
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            try {
                populatePopup();
            } catch (TerminologyException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        public int[] getPopupValues() {
            return popupIds.getListArray();
        }

        public I_GetConceptData getSelectedItem(Object value) {
            StringWithExtTuple swet = (StringWithExtTuple) value;

            try {
                switch (field) {
                    case REFSET_ID:
                        return Terms.get().getConcept(swet.getTuple().getRefsetId());
                    case MEMBER_ID:
                        throw new UnsupportedOperationException("Can't do concept combobox on " + field);
                    case COMPONENT_ID:
                        throw new UnsupportedOperationException("Can't do concept combobox on " + field);
                    case STATUS:
                        return Terms.get().getConcept(swet.getTuple().getStatusNid());
                    case VERSION:
                        throw new UnsupportedOperationException("Can't do concept combobox on " + field);
                    case PATH:
                        throw new UnsupportedOperationException("Can't do concept combobox on " + field);
                    case BOOLEAN_VALUE:
                        throw new UnsupportedOperationException("Can't do concept combobox on " + field);
                    case CONCEPT_ID:
                        return Terms.get().getConcept(((I_ExtendByRefPartCid) swet.getTuple().getMutablePart()).getC1id());
                    case CONCEPT_ID2:
                        return Terms.get().getConcept(((I_ExtendByRefPartCidCid) swet.getTuple().getMutablePart()).getC2id());
                    case CONCEPT_ID3:
                        return Terms.get().getConcept(((I_ExtendByRefPartCidCidCid) swet.getTuple().getMutablePart()).getC3id());
                    case INTEGER_VALUE:
                        throw new UnsupportedOperationException("Can't do concept combobox on " + field);
                    default:
                        throw new UnsupportedOperationException("Can't do concept combobox on " + field);

                }
            } catch (TerminologyException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    public enum REFSET_FIELDS {
        // All extensions

        REFSET_ID("refset", 5, 75, 1000),
        MEMBER_ID("member id", 5, 100, 100),
        COMPONENT_ID("component id", 5, 100, 100),
        STATUS("status", 5, 50, 250),
        VERSION("time", 5, 140, 140),
        PATH("path", 5, 90, 180),
        // Boolean extension
        BOOLEAN_VALUE("boolean value", 5, 100, 500),
        // String extension
        STRING_VALUE("string value", 75, 250, 1000),
        // Concept extension
        CONCEPT_ID("concept", 5, 300, 1000),
        CONCEPT_ID2("concept", 5, 300, 1000),
        CONCEPT_ID3("concept", 5, 300, 1000),
        // Integer extension
        INTEGER_VALUE("integer value", 5, 100, 500);
        private String columnName;
        private int min;
        private int pref;
        private int max;

        private REFSET_FIELDS(String columnName, int min, int pref, int max) {
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
    private static REFSET_FIELDS[] booleanRefsetFields =
            new REFSET_FIELDS[]{
        REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.BOOLEAN_VALUE, REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION,
        REFSET_FIELDS.PATH};
    private static REFSET_FIELDS[] booleanRefsetFieldsNoHistory =
            new REFSET_FIELDS[]{REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.BOOLEAN_VALUE, REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };
    private static REFSET_FIELDS[] stringRefsetFields =
            new REFSET_FIELDS[]{
        REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.STRING_VALUE, REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION,
        REFSET_FIELDS.PATH};
    private static REFSET_FIELDS[] stringRefsetFieldsNoHistory =
            new REFSET_FIELDS[]{REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.STRING_VALUE, REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.PATH
    };
    private static REFSET_FIELDS[] conceptRefsetFields =
            new REFSET_FIELDS[]{
        REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.CONCEPT_ID, REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION,
        REFSET_FIELDS.PATH};
    private static REFSET_FIELDS[] cnidCnidCnidRefsetFields =
            new REFSET_FIELDS[]{
        REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.CONCEPT_ID, 
        REFSET_FIELDS.CONCEPT_ID2, 
        REFSET_FIELDS.CONCEPT_ID3, 
        REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION,
        REFSET_FIELDS.PATH};
    private static REFSET_FIELDS[] cnidCnidCnidRefsetFieldsNoHistory =
            new REFSET_FIELDS[]{
        REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.CONCEPT_ID, 
        REFSET_FIELDS.CONCEPT_ID2, 
        REFSET_FIELDS.CONCEPT_ID3, 
        REFSET_FIELDS.STATUS};
    private static REFSET_FIELDS[] conceptRefsetFieldsNoHistory =
            new REFSET_FIELDS[]{REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.CONCEPT_ID, REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };
    private static REFSET_FIELDS[] conIntRefsetFields =
            new REFSET_FIELDS[]{
        REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.CONCEPT_ID, REFSET_FIELDS.INTEGER_VALUE, REFSET_FIELDS.STATUS,
        REFSET_FIELDS.VERSION, REFSET_FIELDS.PATH};
    private static REFSET_FIELDS[] conIntRefsetFieldsNoHistory =
            new REFSET_FIELDS[]{REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.CONCEPT_ID, REFSET_FIELDS.INTEGER_VALUE, REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };
    private static REFSET_FIELDS[] integerRefsetFields =
            new REFSET_FIELDS[]{
        REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.INTEGER_VALUE, REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION,
        REFSET_FIELDS.PATH};
    private static REFSET_FIELDS[] integerRefsetFieldsNoHistory =
            new REFSET_FIELDS[]{REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.INTEGER_VALUE, REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };

    public static REFSET_FIELDS[] getRefsetColumns(I_HostConceptPlugins host, REFSET_TYPES type) {
        if (host.getShowHistory()) {
            switch (type) {
                case BOOLEAN:
                    return booleanRefsetFields;
                case STRING:
                    return stringRefsetFields;
                case CONCEPT:
                    return conceptRefsetFields;
                case CON_INT:
                    return conIntRefsetFields;
                case INTEGER:
                    return integerRefsetFields;
                case CID_CID_CID:
                    return cnidCnidCnidRefsetFields;
                default:
                    throw new UnsupportedOperationException("Can't handle type: " + type);
            }
        } else {
            switch (type) {
                case BOOLEAN:
                    return booleanRefsetFieldsNoHistory;
                case STRING:
                    return stringRefsetFieldsNoHistory;
                case CONCEPT:
                    return conceptRefsetFieldsNoHistory;
                case CON_INT:
                    return conIntRefsetFieldsNoHistory;
                case INTEGER:
                    return integerRefsetFieldsNoHistory;
                case CID_CID_CID:
                    return cnidCnidCnidRefsetFieldsNoHistory;
                default:
                    throw new UnsupportedOperationException("Can't handle type: " + type);
            }
        }
    }
    private REFSET_FIELDS[] columns;
    private SmallProgressPanel progress = new SmallProgressPanel();
    I_HostConceptPlugins host;
    List<I_ExtendByRefVersion> allTuples;
    ArrayList<I_ExtendByRef> allExtensions;
    Map<Integer, I_GetConceptData> referencedConcepts = new ConcurrentHashMap<Integer, I_GetConceptData>();
    private Set<Integer> conceptsToFetch = new ConcurrentSkipListSet<Integer>();
    private TableChangedSwingWorker tableChangeWorker;
    private ReferencedConceptsSwingWorker refConWorker;
    private int tableComponentId = Integer.MIN_VALUE;
    private JButton addButton;
    private REFSET_TYPES refsetType;
    private TOGGLES toggle;

    protected Class<? extends I_ExtendByRefPart> getExtPartClass() {
        switch (refsetType) {
            case BOOLEAN:
                return I_ExtendByRefPartBoolean.class;
            case STRING:
                return I_ExtendByRefPartStr.class;
            case CONCEPT:
                return I_ExtendByRefPartCid.class;
            case CON_INT:
                return I_ExtendByRefPartCidInt.class;
            case INTEGER:
                return I_ExtendByRefPartInt.class;
            case CID_CID_CID:
                return I_ExtendByRefPartCidCidCid.class;
            default:
                throw new UnsupportedOperationException("Can't handle type: " + refsetType);
        }
    }

    public class ReferencedConceptsSwingWorker extends SwingWorker<Map<Integer, I_GetConceptData>> {

        private boolean stopWork = false;

        @Override
        protected Map<Integer, I_GetConceptData> construct() throws Exception {
            getProgress().setActive(true);
            Map<Integer, I_GetConceptData> concepts = new ConcurrentHashMap<Integer, I_GetConceptData>();
            for (Integer id : new HashSet<Integer>(conceptsToFetch)) {
                if (stopWork) {
                    break;
                }
                I_GetConceptData b = Terms.get().getConcept(id);
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
                if (conceptsToFetch.isEmpty()) {
                    getProgress().getProgressBar().setValue(1);
                } else {
                    getProgress().getProgressBar().setValue(conceptsToFetch.size());
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

        Integer componentId;
        private boolean stopWork = false;

        public TableChangedSwingWorker(Integer componentId) {
            super();
            this.componentId = componentId;
        }

        @Override
        protected Boolean construct() throws Exception {
            if (refConWorker != null) {
                refConWorker.stop();
            }
            if (componentId == null || componentId == Integer.MIN_VALUE) {
                return true;
            }
            List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(componentId);
            I_IntSet allowedStatus = host.getConfig().getAllowedStatus();
            for (I_ExtendByRef ext : extensions) {
                if (stopWork) {
                    return false;
                }
                List<? extends I_ExtendByRefVersion> parts = new ArrayList<I_ExtendByRefVersion>();
                if (!host.getShowHistory()) {
                    parts = ext.getTuples(allowedStatus, host.getConfig().getViewPositionSetReadOnly(),
                            host.getConfig().getPrecedence(), host.getConfig().getConflictResolutionStrategy());
                } else {
                    parts = ext.getTuples();
                }
                for (I_ExtendByRefVersion tuple : parts) {

                    if (!getExtPartClass().isAssignableFrom(tuple.getClass())) {
                        break;
                    }

                    if (I_ExtendByRefPartBoolean.class.isAssignableFrom(tuple.getClass())) {
                        conceptsToFetch.add(tuple.getStatusNid());
                        conceptsToFetch.add(tuple.getPathNid());
                    } else if (I_ExtendByRefPartStr.class.isAssignableFrom(tuple.getClass())) {
                        conceptsToFetch.add(tuple.getStatusNid());
                        conceptsToFetch.add(tuple.getPathNid());
                    } else if (I_ExtendByRefPartCidCidCid.class.isAssignableFrom(tuple.getClass())) {
                        I_ExtendByRefPartCidCidCid conceptPart = (I_ExtendByRefPartCidCidCid) tuple;
                        conceptsToFetch.add(conceptPart.getC1id());
                        conceptsToFetch.add(conceptPart.getC2id());
                        conceptsToFetch.add(conceptPart.getC3id());
                        conceptsToFetch.add(tuple.getStatusNid());
                        conceptsToFetch.add(tuple.getPathNid());
                    } else if (I_ExtendByRefPartCidCid.class.isAssignableFrom(tuple.getClass())) {
                        I_ExtendByRefPartCidCid conceptPart = (I_ExtendByRefPartCidCid) tuple;
                        conceptsToFetch.add(conceptPart.getC1id());
                        conceptsToFetch.add(conceptPart.getC2id());
                        conceptsToFetch.add(tuple.getStatusNid());
                        conceptsToFetch.add(tuple.getPathNid());
                    } else if (I_ExtendByRefPartCid.class.isAssignableFrom(tuple.getClass())) {
                        I_ExtendByRefPartCid conceptPart = (I_ExtendByRefPartCid) tuple;
                        conceptsToFetch.add(conceptPart.getC1id());
                        conceptsToFetch.add(tuple.getStatusNid());
                        conceptsToFetch.add(tuple.getPathNid());
                    } else if (I_ExtendByRefPartCidInt.class.isAssignableFrom(tuple.getClass())) {
                        I_ExtendByRefPartCidInt conceptPart = (I_ExtendByRefPartCidInt) tuple;
                        conceptsToFetch.add(conceptPart.getC1id());
                        conceptsToFetch.add(tuple.getStatusNid());
                        conceptsToFetch.add(tuple.getPathNid());
                    } else if (I_ExtendByRefPartCidFloat.class.isAssignableFrom(tuple.getClass())) {
                        I_ExtendByRefPartCidFloat conceptPart = (I_ExtendByRefPartCidFloat) tuple;
                        conceptsToFetch.add(conceptPart.getUnitsOfMeasureId());
                        conceptsToFetch.add(tuple.getStatusNid());
                        conceptsToFetch.add(tuple.getPathNid());
                    } else if (I_ExtendByRefPartInt.class.isAssignableFrom(tuple.getClass())) {
                        conceptsToFetch.add(tuple.getStatusNid());
                        conceptsToFetch.add(tuple.getPathNid());
                    }
                    if (stopWork) {
                        return false;
                    }
                    if (allTuples == null) {
                        AceLog.getAppLog().info("all tuples for RefsetMemberTableModel is  null");
                        return false;
                    }
                    conceptsToFetch.add(ext.getRefsetId());
                    allTuples.add(tuple);
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
                if (getProgress() != null) {
                    getProgress().getProgressBar().setIndeterminate(false);
                    if (conceptsToFetch.isEmpty()) {
                        getProgress().getProgressBar().setValue(1);
                        getProgress().getProgressBar().setMaximum(1);
                    } else {
                        getProgress().getProgressBar().setValue(1);
                        getProgress().getProgressBar().setMaximum(conceptsToFetch.size());
                    }
                }
                if (get()) {
                    tableComponentId = componentId;
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

    public RefsetMemberTableModel(I_HostConceptPlugins host, REFSET_FIELDS[] columns, REFSET_TYPES refsetType,
            I_HostConceptPlugins.TOGGLES toggle) {
        super();
        this.columns = columns;
        this.host = host;
        this.refsetType = refsetType;
        this.toggle = toggle;
        this.host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
    }

    public I_RefsetDefaults getRefsetPreferences() throws Exception {
        switch (refsetType) {
            case BOOLEAN:
                return host.getConfig().getRefsetPreferencesForToggle(toggle).getBooleanPreferences();
            case STRING:
                return host.getConfig().getRefsetPreferencesForToggle(toggle).getStringPreferences();
            case CONCEPT:
                return host.getConfig().getRefsetPreferencesForToggle(toggle).getConceptPreferences();
            case CON_INT:
                return host.getConfig().getRefsetPreferencesForToggle(toggle).getConIntPreferences();
            case INTEGER:
                return host.getConfig().getRefsetPreferencesForToggle(toggle).getIntegerPreferences();
            case CID_CID_CID:
                return host.getConfig().getRefsetPreferencesForToggle(toggle).getCidCidCidPreferences();
            default:
                throw new Exception("Can't handle refset type: " + refsetType);
        }
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
            this.addButton.setEnabled(this.tableComponentId != Integer.MIN_VALUE);
        }
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            if (componentId == Integer.MIN_VALUE) {
                AceLog.getAppLog().fine("Set component id to NULL for " + refsetType + " in " + toggle);
            } else {
                AceLog.getAppLog().fine("Set component id to: " + componentId + " for " + refsetType + " in " + toggle);
            }
        }
        propertyChange(null);
    }

    public int getRowCount() {
        if (tableComponentId == Integer.MIN_VALUE) {
            return 0;
        }
        if (allTuples == null) {
            allTuples = new ArrayList<I_ExtendByRefVersion>();
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

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (allTuples == null) {
            return null;
        }
        try {
            I_ExtendByRefVersion tuple = allTuples.get(rowIndex);

            boolean inConflict =
                    (host.getConfig().getHighlightConflictsInComponentPanel() && host.getConfig().getConflictResolutionStrategy().isInConflict(tuple.getCore()));

            switch (columns[columnIndex]) {

                case REFSET_ID:
                    if (referencedConcepts.containsKey(tuple.getRefsetId())) {
                        return new StringWithExtTuple(getPrefText(tuple.getRefsetId()), tuple, tuple.getRefsetId(),
                                inConflict);
                    }
                    return new StringWithExtTuple(Integer.toString(tuple.getRefsetId()), tuple, tuple.getRefsetId(),
                            inConflict);

                case MEMBER_ID:
                    return new StringWithExtTuple(Integer.toString(tuple.getMemberId()), tuple, tuple.getMemberId(),
                            inConflict);

                case COMPONENT_ID:
                    return new StringWithExtTuple(Integer.toString(tuple.getComponentId()), tuple, tuple.getComponentId(),
                            inConflict);

                case STATUS:
                    if (referencedConcepts.containsKey(tuple.getStatusNid())) {
                        return new StringWithExtTuple(getPrefText(tuple.getStatusNid()), tuple, tuple.getStatusNid(),
                                inConflict);
                    }
                    return new StringWithExtTuple(Integer.toString(tuple.getStatusNid()), tuple, tuple.getStatusNid(),
                            inConflict);

                case VERSION:
                    if (tuple.getTime() == Long.MAX_VALUE) {
                        return new StringWithExtTuple(ThinVersionHelper.uncommittedHtml(), tuple, tuple.getMemberId(),
                                inConflict);
                    }
                    return new StringWithExtTuple(ThinVersionHelper.format(tuple.getVersion()), tuple, tuple.getMemberId(),
                            inConflict);

                case PATH:
                    if (referencedConcepts.containsKey(tuple.getPathNid())) {
                        return new StringWithExtTuple(getPrefText(tuple.getPathNid()), tuple, tuple.getPathNid(), inConflict);
                    }
                    return new StringWithExtTuple(Integer.toString(tuple.getPathNid()), tuple, tuple.getPathNid(), inConflict);

                case BOOLEAN_VALUE:
                    return new StringWithExtTuple(Boolean.toString(((I_ExtendByRefPartBoolean) tuple.getMutablePart()).getBooleanValue()), tuple, tuple.getMemberId(), inConflict);

                case STRING_VALUE:
                    return new StringWithExtTuple(((I_ExtendByRefPartStr) tuple.getMutablePart()).getStringValue(), tuple,
                            tuple.getMemberId(), inConflict);

                case CONCEPT_ID:
                    if (referencedConcepts.containsKey(((I_ExtendByRefPartCid) tuple.getMutablePart()).getC1id())) {
                        return new StringWithExtTuple(
                                getPrefText(((I_ExtendByRefPartCid) tuple.getMutablePart()).getC1id()), tuple,
                                ((I_ExtendByRefPartCid) tuple.getMutablePart()).getC1id(), inConflict);
                    }
                    return new StringWithExtTuple(Integer.toString(((I_ExtendByRefPartCid) tuple.getMutablePart()).getC1id()), tuple, ((I_ExtendByRefPartCid) tuple.getMutablePart()).getC1id(), inConflict);

                case CONCEPT_ID2:
                    if (referencedConcepts.containsKey(((I_ExtendByRefPartCidCid) tuple.getMutablePart()).getC2id())) {
                        return new StringWithExtTuple(
                                getPrefText(((I_ExtendByRefPartCidCid) tuple.getMutablePart()).getC2id()), tuple,
                                ((I_ExtendByRefPartCidCid) tuple.getMutablePart()).getC2id(), inConflict);
                    }
                    return new StringWithExtTuple(Integer.toString(((I_ExtendByRefPartCidCid) tuple.getMutablePart()).getC2id()), tuple, ((I_ExtendByRefPartCidCid) tuple.getMutablePart()).getC2id(), inConflict);

                case CONCEPT_ID3:
                    if (referencedConcepts.containsKey(((I_ExtendByRefPartCidCidCid) tuple.getMutablePart()).getC3id())) {
                        return new StringWithExtTuple(
                                getPrefText(((I_ExtendByRefPartCidCidCid) tuple.getMutablePart()).getC3id()), tuple,
                                ((I_ExtendByRefPartCidCidCid) tuple.getMutablePart()).getC3id(), inConflict);
                    }
                    return new StringWithExtTuple(Integer.toString(((I_ExtendByRefPartCidCidCid) tuple.getMutablePart()).getC3id()), tuple, ((I_ExtendByRefPartCidCidCid) tuple.getMutablePart()).getC3id(), inConflict);

                case INTEGER_VALUE:
                    if (I_ExtendByRefPartCidInt.class.isAssignableFrom(tuple.getMutablePart().getClass())) {
                        int value = ((I_ExtendByRefPartCidInt) tuple.getMutablePart()).getIntValue();
                        if (Terms.get().getRefsetHelper(host.getConfig()).hasPurpose(tuple.getRefsetId(),
                                RefsetAuxiliary.Concept.REFSET_PURPOSE_POSITION)) {
                            return new StringWithExtTuple((value == Integer.MAX_VALUE) ? "latest" : ThinVersionHelper.format(value), tuple, tuple.getMemberId(), inConflict);
                        } else {
                            return new StringWithExtTuple(Integer.toString(value), tuple, tuple.getMemberId(), inConflict);
                        }
                    } else {
                        int value = ((I_ExtendByRefPartInt) tuple.getMutablePart()).getIntValue();
                        return new StringWithExtTuple(Integer.toString(value), tuple, tuple.getMemberId(), inConflict);
                    }

            }

            AceLog.getAppLog().alertAndLogException(new Exception("Can't handle column type: " + columns[columnIndex]));
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }

    private String getPrefText(int id) throws IOException {
        I_GetConceptData cb = referencedConcepts.get(id);
        I_DescriptionTuple desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
        if (desc != null) {
            return desc.getText();
        }
        cb = referencedConcepts.get(id);
        desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
        return cb.getInitialText();
    }

    public REFSET_FIELDS[] getColumns() {
        return columns;
    }

    public REFSET_FIELDS[] getFieldsForPopup() {
        return columns;
    }

    public void setAddButton(JButton addButton) {
        if (this.addButton != null) {
            this.addButton.removeActionListener(this);
        }
        this.addButton = addButton;
        if (this.addButton != null) {
            this.addButton.addActionListener(this);
        }
    }

    public void actionPerformed(ActionEvent arg0) {
        try {
            I_HoldRefsetPreferences preferences = host.getConfig().getRefsetPreferencesForToggle(toggle);
            I_RefsetDefaults refsetDefaults = null;
            RefsetPropertyMap extProps = new RefsetPropertyMap();
            I_HelpRefsets refsetHelper = Terms.get().getRefsetHelper(host.getConfig());
            switch (refsetType) {
                case BOOLEAN:
                    extProps.setMemberType(org.ihtsdo.etypes.EConcept.REFSET_TYPES.BOOLEAN);
                    refsetDefaults = preferences.getBooleanPreferences();
                    extProps.put(REFSET_PROPERTY.STATUS, refsetDefaults.getDefaultStatusForRefset().getConceptNid());
                    extProps.put(REFSET_PROPERTY.BOOLEAN_VALUE, ((I_RefsetDefaultsBoolean) refsetDefaults).getDefaultForBooleanRefset());
                    break;
                case STRING:
                    extProps.setMemberType(org.ihtsdo.etypes.EConcept.REFSET_TYPES.STR);
                    refsetDefaults = preferences.getStringPreferences();
                    extProps.put(REFSET_PROPERTY.STATUS, refsetDefaults.getDefaultStatusForRefset().getConceptNid());
                    extProps.put(REFSET_PROPERTY.STRING_VALUE, ((I_RefsetDefaultsString) refsetDefaults).getDefaultForStringRefset());
                    break;
                case CONCEPT:
                    extProps.setMemberType(org.ihtsdo.etypes.EConcept.REFSET_TYPES.CID);
                    refsetDefaults = preferences.getConceptPreferences();
                    extProps.put(REFSET_PROPERTY.STATUS, refsetDefaults.getDefaultStatusForRefset().getConceptNid());
                    extProps.put(REFSET_PROPERTY.CID_ONE, ((I_RefsetDefaultsConcept) refsetDefaults).getDefaultForConceptRefset().getConceptNid());
                    break;
                case CON_INT:
                    extProps.setMemberType(org.ihtsdo.etypes.EConcept.REFSET_TYPES.CID_INT);
                    refsetDefaults = preferences.getConIntPreferences();
                    extProps.put(REFSET_PROPERTY.STATUS, refsetDefaults.getDefaultStatusForRefset().getConceptNid());
                    extProps.put(REFSET_PROPERTY.CID_ONE, ((I_RefsetDefaultsConcept) refsetDefaults).getDefaultForConceptRefset().getConceptNid());
                    extProps.put(REFSET_PROPERTY.INTEGER_VALUE, ((I_RefsetDefaultsConInt) refsetDefaults).getDefaultForIntegerValue());
                    break;
                case CID_CID_CID:
                    extProps.setMemberType(org.ihtsdo.etypes.EConcept.REFSET_TYPES.CID_CID_CID);
                    refsetDefaults = preferences.getCidCidCidPreferences();
                    extProps.put(REFSET_PROPERTY.STATUS, refsetDefaults.getDefaultStatusForRefset().getConceptNid());
                    extProps.put(REFSET_PROPERTY.CID_ONE, ((I_RefsetsDefaultsConConCon) refsetDefaults).getDefaultForCnid1().getConceptNid());
                    extProps.put(REFSET_PROPERTY.CID_TWO, ((I_RefsetsDefaultsConConCon) refsetDefaults).getDefaultForCnid1().getConceptNid());
                    extProps.put(REFSET_PROPERTY.CID_THREE, ((I_RefsetsDefaultsConConCon) refsetDefaults).getDefaultForCnid1().getConceptNid());
                    break;
                case INTEGER:
                    extProps.setMemberType(org.ihtsdo.etypes.EConcept.REFSET_TYPES.INT);
                    refsetDefaults = preferences.getIntegerPreferences();
                    refsetDefaults = preferences.getConIntPreferences();
                    extProps.put(REFSET_PROPERTY.STATUS, refsetDefaults.getDefaultStatusForRefset().getConceptNid());
                    if (I_RefsetDefaultsInteger.class.isAssignableFrom(refsetDefaults.getClass())) {
                        extProps.put(REFSET_PROPERTY.INTEGER_VALUE, ((I_RefsetDefaultsInteger) refsetDefaults).getDefaultForIntegerRefset());
                    } else if (I_RefsetDefaultsConInt.class.isAssignableFrom(refsetDefaults.getClass())) {
                        extProps.put(REFSET_PROPERTY.INTEGER_VALUE, ((I_RefsetDefaultsConInt) refsetDefaults).getDefaultForIntegerValue());
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Can't handle ref set type: " + refsetType);
            }
            I_ExtendByRef extension =
                    refsetHelper.getOrCreateRefsetExtension(refsetDefaults.getDefaultRefset().getConceptNid(),
                    tableComponentId, extProps.getMemberType(), extProps, UUID.randomUUID());
            Terms.get().addUncommitted(extension);
            propertyChange(null);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (RuntimeException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    public boolean isCellEditable(int row, int col) {
        if (ACE.editMode == false || row < 0 || row >= allTuples.size()) {
            return false;
        }
        if (allTuples.get(row).getTime() == Long.MAX_VALUE) {
            if (columns[col] == REFSET_FIELDS.REFSET_ID) {
                if (allTuples.get(row).getVersions().size() > 1) {
                    return false;
                }
            }
            if (AceLog.getAppLog().isLoggable(Level.FINER)) {
                AceLog.getAppLog().finer("Cell is editable: " + row + " " + col);
            }
            return true;
        }
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        I_ExtendByRefVersion extTuple = allTuples.get(row);
        boolean changed = false;
        if (extTuple.getTime() == Long.MAX_VALUE) {
            try {
                switch (columns[col]) {
                    case REFSET_ID:
                        Integer refsetId = (Integer) value;
                        if (refsetId != extTuple.getCore().getRefsetId()) {
                            extTuple.getCore().setRefsetId(refsetId);
                            referencedConcepts.put(refsetId, Terms.get().getConcept(refsetId));
                            changed = true;
                        }
                        break;
                    case MEMBER_ID:
                        break;
                    case COMPONENT_ID:
                        break;
                    case STATUS:
                        Integer statusId = (Integer) value;
                        if (statusId != extTuple.getStatusNid()) {
                            extTuple.setStatusNid(statusId);
                            referencedConcepts.put(statusId, Terms.get().getConcept(statusId));
                            changed = true;
                        }
                        break;
                    case VERSION:
                        break;
                    case PATH:
                        break;
                    case BOOLEAN_VALUE:
                        Boolean booleanValue = (Boolean) value;
                        if (booleanValue != ((I_ExtendByRefPartBoolean) extTuple.getMutablePart()).getBooleanValue()) {
                            ((I_ExtendByRefPartBoolean) extTuple.getMutablePart()).setBooleanValue(booleanValue);
                            changed = true;
                        }
                        break;
                    case STRING_VALUE:
                        String stringValue = (String) value;
                        if (stringValue.equals(((I_ExtendByRefPartStr) extTuple.getMutablePart()).getStringValue()) == false) {
                            ((I_ExtendByRefPartStr) extTuple.getMutablePart()).setStringValue(stringValue);
                            changed = true;
                        }
                        break;
                    case CONCEPT_ID:
                        Integer conceptId = (Integer) value;
                        ((I_ExtendByRefPartCid) extTuple.getMutablePart()).setC1id(conceptId);
                        referencedConcepts.put(conceptId, Terms.get().getConcept(conceptId));
                        changed = true;
                        break;
                    case INTEGER_VALUE:
                        Integer intValue = (Integer) value;
                        if (I_ExtendByRefPartCidInt.class.isAssignableFrom(extTuple.getMutablePart().getClass())) {
                            ((I_ExtendByRefPartCidInt) extTuple.getMutablePart()).setIntValue(intValue);
                        } else {
                            ((I_ExtendByRefPartInt) extTuple.getMutablePart()).setIntValue(intValue);
                        }
                        changed = true;
                        break;
                }
            } catch (TerminologyException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (changed) {
                fireTableDataChanged();
                AceLog.getAppLog().info("refset table changed");
                updateDataAlerts(row);
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
                        if (active && row > -1 && row < allTuples.size()) {
                            I_ExtendByRefVersion tuple = allTuples.get(row);
                            Terms.get().addUncommitted(tuple.getCore());
                        }
                    }
                });
            }
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
        switch (columns[c]) {
            case REFSET_ID:
                return Number.class;
            case MEMBER_ID:
                return Number.class;
            case COMPONENT_ID:
                return Number.class;
            case STATUS:
                return Number.class;
            case VERSION:
                return Number.class;
            case PATH:
                return Number.class;
            case BOOLEAN_VALUE:
                return Boolean.class;
            case STRING_VALUE:
                return String.class;
            case CONCEPT_ID:
                return Number.class;
            case INTEGER_VALUE:
                return Integer.class;
        }
        return String.class;
    }

    public RefsetPopupListener makePopupListener(JTable table, I_ConfigAceFrame config) throws Exception {
        return new RefsetPopupListener(table, config, this.getRefsetPreferences(), this);
    }

    public List<REFSET_FIELDS> getPopupFields() {
        ArrayList<REFSET_FIELDS> returnValues = new ArrayList<REFSET_FIELDS>();
        for (REFSET_FIELDS f : columns) {
            switch (f) {
                case MEMBER_ID:
                case COMPONENT_ID:
                case PATH:
                    break;

                default:
                    returnValues.add(f);
            }
        }

        return returnValues;
    }
}
