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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
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
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HoldRefsetData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.RefsetSpecTreeCellRenderer;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.timer.UpdateAlertsTimer;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.IntList;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public abstract class ReflexiveTableModel extends AbstractTableModel implements PropertyChangeListener,
        I_HoldRefsetData {

    protected BitSet checkedRows = new BitSet();

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
        private JTextField textField;
        private ReflexiveRefsetFieldData field;

        public StringExtFieldEditor(ReflexiveRefsetFieldData field) {
            super(new JTextField());
            textField = new JTextField();
            textField.addFocusListener(new TextFieldFocusListener());
            editorComponent = textField;
            this.field = field;

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
            try {
                return (String) field.getReadMethod().invoke(swet.getTuple());
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

        public ConceptFieldEditor(I_ConfigAceFrame config, IntList popupIds, ReflexiveRefsetFieldData field)
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

                public void setValue(Object value) {
                    combo.setSelectedItem(getSelectedItem(value));
                }

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
            if (field.getType() == REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER) {
                try {
                    return Terms.get().getConcept((Integer) field.getReadMethod().invoke(swet.getTuple()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            throw new UnsupportedOperationException("Can't do concept combobox on " + field);
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

    protected class ReferencedConceptsSwingWorker extends SwingWorker<Map<Integer, I_GetConceptData>> {

        private boolean stopWork = false;

        @Override
        protected Map<Integer, I_GetConceptData> construct() throws Exception {
            getProgress().setActive(true);
            Map<Integer, I_GetConceptData> concepts = new HashMap<Integer, I_GetConceptData>();
            setupConceptCache();
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

        public void setStopWork(boolean b) {
            stopWork = b;
        }
    }
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected ReflexiveRefsetFieldData[] columns;
    protected SmallProgressPanel progress = new SmallProgressPanel();
    protected I_HostConceptPlugins host;
    protected List<I_ExtendByRefVersion> allTuples;
    protected Map<Integer, ConceptChronicleBI> conceptCache;
    protected ArrayList<I_ExtendByRef> allExtensions;
    protected ConcurrentHashMap<Long, Object> values;
    protected Map<Integer, I_GetConceptData> referencedConcepts = new ConcurrentHashMap<Integer, I_GetConceptData>();
    protected Set<Integer> conceptsToFetch = new HashSet<Integer>();
    protected I_ChangeTableInSwing tableChangeWorker;
    protected ReferencedConceptsSwingWorker refConWorker;
    protected int tableComponentId = Integer.MIN_VALUE;
    protected Integer promotionFilterId = null;
    protected JButton addButton = new JButton();
    private boolean useConceptCache = false;

    public static long rowColumnToLong(int row, int column) {
        long key = row;
        key = key & 0x00000000FFFFFFFFL;
        long columnLong = column;
        columnLong = columnLong & 0x00000000FFFFFFFFL;
        key = key << 32;
        key = key | columnLong;
        return key;
    }

    public void addToValueCache(int row, int column, Object value) {
        if (values != null) {
            long key = rowColumnToLong(row, column);
            values.put(key, value);
        }
    }

    public ReflexiveTableModel(I_HostConceptPlugins host, ReflexiveRefsetFieldData[] columns) {
        super();
        this.columns = columns;
        this.host = host;
        this.host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
    }

    public SmallProgressPanel getProgress() {
        return progress;
    }

    public void setProgress(SmallProgressPanel progress) {
        this.progress = progress;
    }

    @Override
    public int getColumnCount() {
        if (checkBoxColumn) {
            return columns.length + 1;
        }
        return columns.length;
    }

    public int getFixedColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        if (col >= columns.length) {
            return "";
        }
        return columns[col].getColumnName();
    }

    @Override
    public void setComponentId(int componentId) throws Exception {
        this.tableComponentId = componentId;
        this.allTuples = null;
        this.allExtensions = null;
        this.checkedRows.clear();
        if (ACE.editMode) {
            this.addButton.setEnabled(this.tableComponentId != Integer.MIN_VALUE);
        }
        propertyChange(null);
    }

    public void setPromotionFilterId(Integer promotionFilterId) {
        // clear any checked rows if we've switched to a different promotion filter
        if ((this.promotionFilterId == null && promotionFilterId != null)
                || (this.promotionFilterId != null && promotionFilterId == null)
                || (this.promotionFilterId != null && promotionFilterId != null && !this.promotionFilterId.equals(promotionFilterId))) {
            this.checkedRows.clear();
        }
        this.promotionFilterId = promotionFilterId;
    }

    @Override
    public int getRowCount() {
        List<I_ExtendByRefVersion> localAllTuples = allTuples;
        if (localAllTuples == null) {
            localAllTuples = new ArrayList<I_ExtendByRefVersion>();
            allTuples = localAllTuples;
            values = new ConcurrentHashMap<Long, Object>();
            if (tableChangeWorker != null) {
                tableChangeWorker.stop();
            }
            conceptsToFetch.clear();
            referencedConcepts.clear();
            if (tableComponentId != Integer.MIN_VALUE) {
                tableChangeWorker = getTableChangedSwingWorker(tableComponentId, promotionFilterId);
                tableChangeWorker.start();
            }
            return 0;
        }
        return localAllTuples.size();
    }

    public void setupConceptCache() throws IOException {
        NidBitSetBI conceptNids = Terms.get().getEmptyIdSet();
        for (int rowIndex = 0; rowIndex < allTuples.size(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
                try {
                    if (columns[columnIndex] != null && columns[columnIndex].invokeOnObjectType != null
                            && allTuples.get(rowIndex) != null) {
                        I_ExtendByRefVersion tuple = allTuples.get(rowIndex);
                        switch (columns[columnIndex].invokeOnObjectType) {
                            case CONCEPT_COMPONENT:
                                if (columns[columnIndex].readParamaters != null) {
                                    if (Terms.get().hasConcept(tuple.getComponentId())) {
                                        conceptNids.setMember(tuple.getComponentId());
                                    } else {
                                        int cNid = Ts.get().getConceptNidForNid(tuple.getComponentId());
                                        if (cNid != Integer.MAX_VALUE) {
                                            conceptNids.setMember(cNid);
                                        }
                                    }
                                } else {
                                    conceptNids.setMember(Ts.get().getConceptNidForNid(tuple.getComponentId()));
                                }
                                break;
                            case COMPONENT:
                                conceptNids.setMember(Ts.get().getConceptNidForNid(tuple.getComponentId()));
                                break;
                            case CONCEPT:
                                throw new UnsupportedOperationException();
                            case PART:
                                I_ExtendByRefPart part = tuple.getMutablePart();
                                if (I_ExtendByRefPartCid.class.isAssignableFrom(part.getClass())) {
                                    int cNid = Ts.get().getConceptNidForNid(((I_ExtendByRefPartCid) part).getC1id());
                                    if (cNid != Integer.MAX_VALUE) {
                                        conceptNids.setMember(cNid);
                                    }
                                } else if (I_ExtendByRefPartCidCid.class.isAssignableFrom(part.getClass())) {
                                    int cNid = Ts.get().getConceptNidForNid(((I_ExtendByRefPartCidCid) part).getC2id());
                                    if (cNid != Integer.MAX_VALUE) {
                                        conceptNids.setMember(cNid);
                                    }
                                } else if (I_ExtendByRefPartCidCidCid.class.isAssignableFrom(part.getClass())) {
                                    int cNid = Ts.get().getConceptNidForNid(((I_ExtendByRefPartCidCidCid) part).getC3id());
                                    if (cNid != Integer.MAX_VALUE) {
                                        conceptNids.setMember(cNid);
                                    }
                                }
                                break;

                            case PROMOTION_REFSET_PART:
                                break;
                            default:
                                throw new UnsupportedOperationException("Don't know how to handle: "
                                        + columns[columnIndex].invokeOnObjectType);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    AceLog.getAppLog().info("ReflexiveTableModel: " + e.toString());
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        }
        if (useConceptCache) {
            conceptCache = Ts.get().getConcepts(conceptNids);
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (values != null) {
            Object value = values.get(rowColumnToLong(rowIndex, columnIndex));
            if (value != null) {
                return value;
            }
        }
        I_TermFactory tf = Terms.get();
        if (allTuples == null || tableComponentId == Integer.MIN_VALUE) {
            return " ";
        }
        if (allTuples.isEmpty() && rowIndex == 0) {
            return " ";
        }
        if (rowIndex < 0) {
            return " ";
        }
        if (rowIndex >= allTuples.size()) {
            return " "; // TODO check
        }

        // Test to see if this is the extra boolean column for approving/denying members.
        if (columnIndex >= columns.length) {
            return checkedRows.get(rowIndex);
        }

        if (columns[columnIndex].type == REFSET_FIELD_TYPE.ROW) {
            return rowIndex + 1;
        }
        try {
            I_ExtendByRefVersion tuple = allTuples.get(rowIndex);
            Object value = null;
            int id = Integer.MIN_VALUE;
            switch (columns[columnIndex].invokeOnObjectType) {
                case CONCEPT_COMPONENT:
                    id = tuple.getComponentId();
                    if (columns[columnIndex].readParamaters != null) {
                        if (tf.hasConcept(id)) {
                            value =
                                    columns[columnIndex].getReadMethod().invoke(Terms.get().getConcept(tuple.getComponentId()),
                                    columns[columnIndex].readParamaters);
                        } else {
                            try {
                                I_DescriptionVersioned desc = tf.getDescription(id);
                                if (desc != null) {
                                    value = desc.getLastTuple().getText();
                                }
                            } catch (Exception e) {
                                value = "No description available.";
                            }
                        }

                    } else {
                        value = columns[columnIndex].getReadMethod().invoke(Terms.get().getConcept(tuple.getComponentId()));
                    }
                    break;
                case COMPONENT:
                    value = tuple.getComponentId();
                    break;
                case CONCEPT:
                    throw new UnsupportedOperationException();
                case IMMUTABLE:
                    if (columns[columnIndex].readParamaters != null) {
                        value = columns[columnIndex].getReadMethod().invoke(tuple, columns[columnIndex].readParamaters);
                    } else {
                        value = columns[columnIndex].getReadMethod().invoke(tuple);
                    }
                    break;
                case PART:
                    if (columns[columnIndex].readParamaters != null) {
                        value =
                                columns[columnIndex].getReadMethod().invoke(tuple.getMutablePart(),
                                columns[columnIndex].readParamaters);
                    } else {
                        try {
                            I_ExtendByRefPart part = tuple.getMutablePart();
                            if (columns[columnIndex].getReadMethod().getDeclaringClass().isAssignableFrom(part.getClass())) {
                                value = columns[columnIndex].getReadMethod().invoke(part);
                            } else {
                                if (I_ExtendByRefPartBoolean.class.isAssignableFrom(part.getClass())) {
                                    value = ((I_ExtendByRefPartBoolean) part).getBooleanValue();
                                } else if (I_ExtendByRefPartStr.class.isAssignableFrom(part.getClass())) {
                                    value = ((I_ExtendByRefPartStr) part).getStringValue();
                                } else if (I_ExtendByRefPartInt.class.isAssignableFrom(part.getClass())) {
                                    value = ((I_ExtendByRefPartInt) part).getIntValue();
                                } else {
                                    value = tuple.getMutablePart().toString();
                                }
                            }
                        } catch (Exception e) {
                            value = tuple.getMutablePart().toString();
                            AceLog.getAppLog().warning("ReflexiveTableModel_1: " + e.getMessage() + ": " + value);
                        }
                    }
                    break;
                case PROMOTION_REFSET_PART:
                    value = getPromotionRefsetValue(tuple.getCore(), columns[columnIndex]);
                    break;

                default:
                    throw new UnsupportedOperationException("Don't know how to handle: "
                            + columns[columnIndex].invokeOnObjectType);
            }
            if (value == null) {
                return new StringWithExtTuple(null, tuple, id);
            }
            switch (columns[columnIndex].getType()) {
                case CONCEPT_IDENTIFIER:
                    if (Integer.class.isAssignableFrom(value.getClass())) {
                        id = (Integer) value;
                        int conceptId = (Integer) value;
                        if (referencedConcepts.containsKey(conceptId)) {
                            return new StringWithExtTuple(getPrefText(conceptId), tuple, id);
                        }
                        return new StringWithExtTuple(Integer.toString(conceptId), tuple, id);
                    } else if (I_DescriptionTuple.class.isAssignableFrom(value.getClass())) {
                        I_DescriptionTuple descTuple = (I_DescriptionTuple) value;
                        return new StringWithExtTuple(descTuple.getText(), tuple, descTuple.getConceptNid());
                    }
                    addToValueCache(rowIndex, columnIndex, new StringWithExtTuple(value.toString(), tuple, id));
                    return values.get(rowColumnToLong(rowIndex, columnIndex));
                case COMPONENT_IDENTIFIER:
                    if (Integer.class.isAssignableFrom(value.getClass())) {
                        id = (Integer) value;
                        if (tf.hasConcept(id)) {

                            I_DescriptionTuple desc =
                                    tf.getConcept(id).getDescTuple(host.getConfig().getTableDescPreferenceList(),
                                    host.getConfig());
                            if (desc != null) {
                                addToValueCache(rowIndex, columnIndex, new StringWithExtTuple(desc.getText(), tuple, id));
                                return values.get(rowColumnToLong(rowIndex, columnIndex));
                            }
                            addToValueCache(rowIndex, columnIndex, new StringWithExtTuple(Integer.toString(id), tuple, id));
                            return values.get(rowColumnToLong(rowIndex, columnIndex));

                        } else if (tf.hasExtension(id)) {
                            I_ExtendByRef ext = tf.getExtension(id);
                            I_ConfigAceFrame config = (I_ConfigAceFrame) columns[columnIndex].readParamaters[1];
                            List<I_ExtendByRefVersion> tuples =
                                    (List<I_ExtendByRefVersion>) ext.getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
                            if (tuples.size() > 0) {
                                I_ExtendByRefVersion obj = tuples.iterator().next();
                                I_GetConceptData componentRefset = Terms.get().getConcept(obj.getRefsetId());
                                I_DescriptionTuple refsetDesc =
                                        componentRefset.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
                                StringBuffer buff = new StringBuffer();
                                buff.append("<html>");
                                buff.append(refsetDesc.getText());
                                buff.append(" member: ");
                                // @TODO replace this test with a call to determine
                                // "refset purpose" once the purpose is available.
                                if (refsetDesc.getText().toLowerCase().endsWith("refset spec")) {
                                    RefsetSpecTreeCellRenderer renderer = new RefsetSpecTreeCellRenderer(host.getConfig());
                                    buff.append(renderer.getHtmlRendering(obj));
                                } else {
                                    buff.append(obj.getMutablePart().toString());
                                }

                                addToValueCache(rowIndex, columnIndex, new StringWithExtTuple(buff.toString(), tuple, id));
                                return values.get(rowColumnToLong(rowIndex, columnIndex));
                            } else {
                                tuples =
                                        (List<I_ExtendByRefVersion>) ext.getTuples(null, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
                                if (tuples.size() > 0) {
                                    I_ExtendByRefVersion obj = tuples.iterator().next();
                                    I_GetConceptData componentRefset = Terms.get().getConcept(obj.getRefsetId());
                                    I_DescriptionTuple refsetDesc =
                                            componentRefset.getDescTuple(host.getConfig().getTableDescPreferenceList(),
                                            host.getConfig());
                                    StringBuilder buff = new StringBuilder();
                                    buff.append("<html>");
                                    buff.append(refsetDesc.getText());
                                    buff.append(" member: ");
                                    // @TODO replace this test with a call to determine
                                    // "refset purpose" once the purpose is available.
                                    if (refsetDesc.getText().toLowerCase().endsWith("refset spec")) {
                                        RefsetSpecTreeCellRenderer renderer =
                                                new RefsetSpecTreeCellRenderer(host.getConfig());
                                        buff.append(renderer.getHtmlRendering(obj));
                                    } else {
                                        buff.append(obj.getMutablePart().toString());
                                    }

                                    addToValueCache(rowIndex, columnIndex, new StringWithExtTuple(buff.toString(), tuple, id));
                                    return values.get(rowColumnToLong(rowIndex, columnIndex));
                                }
                            }
                        } else {
                            try {
                                I_DescriptionVersioned desc = tf.getDescription(id);
                                if (desc != null) {
                                    String text = desc.getLastTuple().getText();
                                    addToValueCache(rowIndex, columnIndex, new StringWithExtTuple(text, tuple, id));
                                    return values.get(rowColumnToLong(rowIndex, columnIndex));
                                }
                            } catch (TerminologyException e) {
                                return new StringWithExtTuple("No description available for id:" + id, tuple, id);
                            }

                        }
                    }
                    addToValueCache(rowIndex, columnIndex, value);
                    return value;
                case TIME:
                    if (tuple.getTime() == Long.MAX_VALUE) {
                        return new StringWithExtTuple(ThinVersionHelper.uncommittedHtml(), tuple, id);
                    }
                    return new StringWithExtTuple(ThinVersionHelper.format(tuple.getVersion()), tuple, id);

                // String extension
                case STRING:
                    return new StringWithExtTuple((String) value, tuple, id, false, true);
            }

            AceLog.getAppLog().alertAndLogException(new Exception("Can't handle column type: " + columns[columnIndex]));
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }

    protected abstract Object getPromotionRefsetValue(I_ExtendByRef extension,
            ReflexiveRefsetFieldData reflexiveRefsetFieldData) throws IOException, IllegalAccessException,
            InvocationTargetException, TerminologyException;

    public abstract I_GetConceptData getPromotionRefsetIdentityConcept();

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

    public ReflexiveRefsetFieldData[] getColumns() {
        return columns;
    }

    public ReflexiveRefsetFieldData[] getFieldsForPopup() {
        return columns;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (ACE.editMode == false || allTuples == null) {
            return false;
        }
        if (col >= columns.length) {
            return true;
        }
        if (columns[col].isCreationEditable() == false) {
            return false;
        }
        if (allTuples.isEmpty()) {
            return false;
        }
        if (allTuples.get(row).getTime() == Long.MAX_VALUE) {
            if (columns[col].isUpdateEditable() == false) {
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

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col >= columns.length) {
            if (Boolean.class.isAssignableFrom(value.getClass())) {
                Boolean set = (Boolean) value;
                if (set) {
                    checkedRows.set(row);
                } else {
                    checkedRows.clear(row);
                }
                return;
            }
            AceLog.getAppLog().warning("Can't handle value: " + value + " row: " + row + " col: " + col);
            return;
        }
        if (columns[col].isCreationEditable() || columns[col].isUpdateEditable()) {
            I_ExtendByRefVersion extTuple = allTuples.get(row);
            boolean changed = false;
            if (extTuple.getTime() == Long.MAX_VALUE) {
                try {
                    switch (columns[col].getType()) {
                        case CONCEPT_IDENTIFIER:
                            Integer identifier = (Integer) value;
                            referencedConcepts.put(identifier, Terms.get().getConcept(identifier));
                        default:
                            switch (columns[col].invokeOnObjectType) {
                                case COMPONENT:
                                case CONCEPT:
                                case CONCEPT_COMPONENT:
                                    break;
                                case IMMUTABLE:
                                    columns[col].getWriteMethod().invoke(extTuple, value);
                                    changed = true;
                                    break;
                                case PART:
                                    columns[col].getWriteMethod().invoke(extTuple.getMutablePart(), value);
                                    changed = true;
                                    break;
                                default:
                                    throw new UnsupportedOperationException("Can't handle type: "
                                            + columns[col].invokeOnObjectType);

                            }
                    }
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
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
            if (isActive()) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (isActive()) {
                            I_ExtendByRefVersion tuple = allTuples.get(row);
                            Terms.get().addUncommitted(tuple.getCore());
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
    private boolean checkBoxColumn = false;

    private void updateDataAlerts(int row) {
        if (alertUpdater != null) {
            alertUpdater.setActive(false);
        }
        alertUpdater = new UpdateDataAlertsTimerTask(row);
        UpdateAlertsTimer.schedule(alertUpdater, 2000);

    }

    @Override
    public Class<?> getColumnClass(int c) {
        if (c >= columns.length) {
            return Boolean.class;
        }
        return columns[c].getFieldClass();
    }

    public void removeRow(int rowIndex) {
        allTuples.remove(rowIndex);
    }

    protected abstract I_ChangeTableInSwing getTableChangedSwingWorker(int tableComponentId2, Integer promotionFilterId);

    public void enableCheckBoxColumn(boolean b) {
        this.checkBoxColumn = b;

    }
}
