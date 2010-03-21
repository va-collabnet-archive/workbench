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
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HoldRefsetData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.RefsetSpecTreeCellRenderer;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.timer.UpdateAlertsTimer;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.ThinDescTuple;
import org.dwfa.vodb.types.ThinExtByRefTuple;
import org.dwfa.vodb.types.ThinExtByRefVersioned;

public abstract class ReflexiveTableModel extends AbstractTableModel implements PropertyChangeListener,
        I_HoldRefsetData {

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

                public void setValue(Object value) {
                    if (StringWithExtTuple.class.isAssignableFrom(value.getClass())) {
                        StringWithExtTuple swet = (StringWithExtTuple) value;
                        textField.setText((value != null) ? swet.getCellText() : "");
                    } else {
                        textField.setText((value != null) ? value.toString() : "");
                    }
                }

                public Object getCellEditorValue() {
                    return textField.getText();
                }
            };
            textField.addActionListener(delegate);
        }

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

        public ConceptFieldEditor(I_ConfigAceFrame config, IntList popupIds, ReflexiveRefsetFieldData field) {
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
                    return ((ConceptBean) combo.getSelectedItem()).getConceptId();
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

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            populatePopup();
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        public int[] getPopupValues() {
            return popupIds.getListArray();
        }

        public ConceptBean getSelectedItem(Object value) {
            StringWithExtTuple swet = (StringWithExtTuple) value;
            if (field.getType() == REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER) {
                try {
                    return ConceptBean.get((Integer) field.getReadMethod().invoke(swet.getTuple()));
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

    protected class ReferencedConceptsSwingWorker extends SwingWorker<Map<Integer, ConceptBean>> {
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

    protected List<ThinExtByRefTuple> allTuples;

    protected ArrayList<ThinExtByRefVersioned> allExtensions;

    protected Map<Integer, ConceptBean> referencedConcepts = new HashMap<Integer, ConceptBean>();

    protected Set<Integer> conceptsToFetch = new HashSet<Integer>();

    protected I_ChangeTableInSwing tableChangeWorker;

    protected ReferencedConceptsSwingWorker refConWorker;

    protected int tableComponentId = Integer.MIN_VALUE;

    protected JButton addButton = new JButton();

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

    public int getColumnCount() {
        return columns.length;
    }

    public String getColumnName(int col) {
        return columns[col].getColumnName();
    }

    public void setComponentId(int componentId) throws Exception {
        this.tableComponentId = componentId;
        this.allTuples = null;
        this.allExtensions = null;
        if (ACE.editMode) {
            this.addButton.setEnabled(this.tableComponentId != Integer.MIN_VALUE);
        }
        propertyChange(null);
    }

    public int getRowCount() {
        if (allTuples == null) {
            allTuples = new ArrayList<ThinExtByRefTuple>();
            if (tableChangeWorker != null) {
                tableChangeWorker.stop();
            }
            conceptsToFetch.clear();
            referencedConcepts.clear();
            tableChangeWorker = getTableChangedSwingWorker(tableComponentId);
            tableChangeWorker.start();
            return 0;
        }
        return allTuples.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        I_TermFactory tf = LocalVersionedTerminology.get();
        if (allTuples == null || tableComponentId == Integer.MIN_VALUE) {
            return " ";
        }
        if (allTuples.size() == 0 && rowIndex == 0) {
            return " ";
        }
        if (rowIndex < 0) {
            return " ";
        }
        try {
            I_ThinExtByRefTuple tuple = allTuples.get(rowIndex);
            Object value = null;
            int id = Integer.MIN_VALUE;
            switch (columns[columnIndex].invokeOnObjectType) {
            case CONCEPT_COMPONENT:
                id = tuple.getComponentId();
                if (columns[columnIndex].readParamaters != null) {
                    value = columns[columnIndex].getReadMethod().invoke(ConceptBean.get(tuple.getComponentId()),
                        columns[columnIndex].readParamaters);
                } else {
                    value = columns[columnIndex].getReadMethod().invoke(ConceptBean.get(tuple.getComponentId()));
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
                    value = columns[columnIndex].getReadMethod().invoke(tuple.getPart(),
                        columns[columnIndex].readParamaters);
                } else {
                    try {
                        value = columns[columnIndex].getReadMethod().invoke(tuple.getPart());
                    } catch (Exception e) {
                        value = tuple.getPart().toString();
                        AceLog.getAppLog().warning(e.getMessage() + ": " + value);
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
                } else if (ThinDescTuple.class.isAssignableFrom(value.getClass())) {
                    ThinDescTuple descTuple = (ThinDescTuple) value;
                    return new StringWithExtTuple(descTuple.getText(), tuple, descTuple.getConceptId());
                }
                return new StringWithExtTuple(value.toString(), tuple, id);
            case COMPONENT_IDENTIFIER:
                if (Integer.class.isAssignableFrom(value.getClass())) {
                    id = (Integer) value;
                    if (tf.hasConcept(id)) {
                        I_GetConceptData concept = tf.getConcept(id);
                        I_DescriptionTuple obj = concept.getDescTuple(
                            (I_IntList) columns[columnIndex].readParamaters[0],
                            (I_ConfigAceFrame) columns[columnIndex].readParamaters[1]);
                        return new StringWithExtTuple(obj.getText(), tuple, id);
                    } else if (tf.hasExtension(id)) {
                        I_ThinExtByRefVersioned ext = tf.getExtension(id);
                        I_ConfigAceFrame config = (I_ConfigAceFrame) columns[columnIndex].readParamaters[1];
                        List<I_ThinExtByRefTuple> tuples = ext.getTuples(config.getAllowedStatus(),
                            config.getViewPositionSet(), false);
                        if (tuples.size() > 0) {
                            I_ThinExtByRefTuple obj = tuples.iterator().next();
                            ConceptBean componentRefset = ConceptBean.get(obj.getRefsetId());
                            I_DescriptionTuple refsetDesc = componentRefset.getDescTuple(host.getConfig()
                                .getTableDescPreferenceList(), host.getConfig());
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
                                buff.append(obj.getPart().toString());
                            }

                            return new StringWithExtTuple(buff.toString(), tuple, id);
                        }
                    } else {
                        throw new UnsupportedOperationException("Can't find component for id: " + id);
                    }
                }
                return value;
            case VERSION:
                if (tuple.getVersion() == Integer.MAX_VALUE) {
                    return new StringWithExtTuple(ThinVersionHelper.uncommittedHtml(), tuple, id);
                }
                return new StringWithExtTuple(ThinVersionHelper.format(tuple.getVersion()), tuple, id);

                // String extension
            case STRING:
                return new StringWithExtTuple((String) value, tuple, id);
            }

            AceLog.getAppLog().alertAndLogException(new Exception("Can't handle column type: " + columns[columnIndex]));
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }

    protected abstract Object getPromotionRefsetValue(I_ThinExtByRefVersioned extension,
            ReflexiveRefsetFieldData reflexiveRefsetFieldData) throws IOException, IllegalAccessException,
            InvocationTargetException;

    public abstract I_GetConceptData getPromotionRefsetIdentityConcept();

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

    public ReflexiveRefsetFieldData[] getColumns() {
        return columns;
    }

    public ReflexiveRefsetFieldData[] getFieldsForPopup() {
        return columns;
    }

    public boolean isCellEditable(int row, int col) {
        if (ACE.editMode == false || allTuples == null) {
            return false;
        }
        if (columns[col].isCreationEditable() == false) {
            return false;
        }
        if (allTuples.size() == 0) {
            return false;
        }
        if (allTuples.get(row).getVersion() == Integer.MAX_VALUE) {
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
                            columns[col].getWriteMethod().invoke(extTuple.getPart(), value);
                            changed = true;
                            break;
                        default:
                            throw new UnsupportedOperationException("Can't handle type: "
                                + columns[col].invokeOnObjectType);

                        }
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
                            ACE.addUncommitted(ExtensionByReferenceBean.get(tuple.getMemberId()));
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

    protected abstract I_ChangeTableInSwing getTableChangedSwingWorker(int tableComponentId2);

}
