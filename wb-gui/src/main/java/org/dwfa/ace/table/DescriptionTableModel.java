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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.EventObject;
import java.util.TimerTask;
import java.util.logging.Level;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.timer.UpdateAlertsTimer;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public abstract class DescriptionTableModel extends AbstractTableModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public enum DESC_FIELD {
        SCORE("score", 5, 75, 75), DESC_ID("did", 5, 100, 100), CON_ID("cid", 5, 100, 100), TEXT("text", 5, 300, 2000), LANG("lang", 5, 35, 55), CASE_FIXED("case", 5, 35, 55), STATUS("status", 5, 50, 100), TYPE("type", 5, 85, 450), VERSION("time", 5, 140, 140), PATH("path", 5, 90, 150);

        private String columnName;

        private int min;

        private int pref;

        private int max;

        private DESC_FIELD(String columnName, int min, int pref, int max) {
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

    private DESC_FIELD[] columns;

    private SmallProgressPanel progress = new SmallProgressPanel();

    private I_ConfigAceFrame config;

    public DescriptionTableModel(DESC_FIELD[] columns, I_ConfigAceFrame config) {
        super();
        this.columns = columns;
        this.config = config;
    }

    public final void setColumns(DESC_FIELD[] columns) {
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

    private String getPrefText(int id) throws IOException, TerminologyException {
        I_GetConceptData cb = Terms.get().getConcept(id);
        I_DescriptionTuple desc = cb.getDescTuple(config.getTableDescPreferenceList(), config);
        if (desc != null) {
            return desc.getText();
        }
        return cb.getInitialText() + " null pref desc";
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            if (rowIndex >= getRowCount() || rowIndex < 0) {
                return null;
            }
            I_DescriptionTuple desc = getDescription(rowIndex);

            boolean inConflict = config.getHighlightConflictsInComponentPanel()
                && config.getConflictResolutionStrategy().isInConflict((I_DescriptionVersioned) desc.getFixedPart());

            if (desc == null) {
                return null;
            }

            switch (columns[columnIndex]) {
            case SCORE:
                return new StringWithDescTuple(getScore(rowIndex), desc, false, inConflict);
            case DESC_ID:
                return new StringWithDescTuple(Integer.toString(desc.getDescId()), desc, false, inConflict);
            case CON_ID:
                return new StringWithDescTuple(Integer.toString(desc.getConceptId()), desc, false, inConflict);
            case TEXT:
                if (BasicHTML.isHTMLString(desc.getText())) {
                    return new StringWithDescTuple(desc.getText(), desc, true, inConflict);
                } else {
                    return new StringWithDescTuple(desc.getText(), desc, true, inConflict);
                }
            case LANG:
                return new StringWithDescTuple(desc.getLang(), desc, false, inConflict);
            case CASE_FIXED:
                return new StringWithDescTuple(Boolean.toString(desc.isInitialCaseSignificant()), desc, false,
                    inConflict);
            case STATUS:
                     return new StringWithDescTuple(getPrefText(desc.getStatusId()), desc, false, inConflict);
             case TYPE:
                   return new StringWithDescTuple(getPrefText(desc.getTypeId()), desc, false, inConflict);
            case VERSION:
                if (desc.getVersion() == Integer.MAX_VALUE) {
                    return new StringWithDescTuple(ThinVersionHelper.uncommittedHtml(), desc, false, inConflict);
                }
                return new StringWithDescTuple(ThinVersionHelper.format(desc.getVersion()), desc, false, inConflict);
            case PATH:
                try {
                    return new StringWithDescTuple(getPrefText(desc.getPathId()), desc, false, inConflict);
                } catch (Exception e) {
                    return new StringWithDescTuple(Integer.toString(desc.getPathId()) + " no pref desc...", desc,
                        false, inConflict);
                }
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }

    protected abstract I_DescriptionTuple getDescription(int rowIndex) throws IOException;

    public String getColumnName(int col) {
        return columns[col].getColumnName();
    }

    public boolean isCellEditable(int row, int col) {
        if (ACE.editMode == false) {
            return false;
        }
        try {
            if (getDescription(row).getVersion() == Integer.MAX_VALUE) {
                if (AceLog.getAppLog().isLoggable(Level.FINER)) {
                    AceLog.getAppLog().finer("Cell is editable: " + row + " " + col);
                }
                return true;
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        try {
            I_DescriptionTuple desc = getDescription(row);
            boolean changed = false;
            if (desc.getVersion() == Integer.MAX_VALUE) {
                switch (columns[col]) {
                case DESC_ID:
                    break;
                case CON_ID:
                    break;
                case TEXT:
                    desc.setText(value.toString());
                    changed = true;
                    break;
                case LANG:
                    desc.setLang(value.toString());
                    changed = true;
                    break;
                case CASE_FIXED:
                    desc.setInitialCaseSignificant((Boolean) value);
                    changed = true;
                    break;
                case STATUS:
                    Integer statusId = (Integer) value;
                    desc.setStatusId(statusId);
                    changed = true;
                    break;
                case TYPE:
                    Integer typeId = (Integer) value;
                    desc.setTypeId(typeId);
                    changed = true;
                    break;
                case VERSION:
                    break;
                case PATH:
                    break;
                }
                fireTableDataChanged();
                if (changed) {
                    AceLog.getAppLog().info("Description table changed");
                    updateDataAlerts(row);
                    Terms.get().addUncommitted(Terms.get().getConcept(desc.getConceptId()));
               }
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
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
                            try {
                                I_DescriptionTuple desc = getDescription(row);
                                if (desc != null) {
                                    Terms.get().addUncommitted(Terms.get().getConcept(desc.getConceptId()));
                                }
                            } catch (IOException e) {
                                AceLog.getAppLog().alertAndLogException(e);
                            } catch (TerminologyException e) {
                                AceLog.getAppLog().alertAndLogException(e);
							}
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
        case DESC_ID:
            return Number.class;
        case CON_ID:
            return Number.class;
        case TEXT:
            return StringWithDescTuple.class;
        case LANG:
            return String.class;
        case CASE_FIXED:
            return Boolean.class;
        case STATUS:
            return Number.class;
        case TYPE:
            return Number.class;
        case VERSION:
            return Number.class;
        case PATH:
            return Number.class;
        case SCORE:
            return Number.class;

        }
        return String.class;
    }

    public SmallProgressPanel getProgress() {
        return progress;
    }

    public void setProgress(SmallProgressPanel progress) {
        this.progress = progress;
    }

    public static class StringWithDescTuple extends StringWithTuple<StringWithDescTuple>  {
        I_DescriptionTuple tuple;

        boolean wrapLines;

        public StringWithDescTuple(String cellText, I_DescriptionTuple tuple, boolean wrapLines, boolean inConflict) {
            super(cellText, inConflict);
            this.tuple = tuple;
            this.wrapLines = wrapLines;
        }

        public I_DescriptionTuple getTuple() {
            return tuple;
        }

        public boolean getWrapLines() {
            return wrapLines;
        }

        public void setWrapLines(boolean wrapLines) {
            this.wrapLines = wrapLines;
        }
    }

    public static class DescTextFieldEditor extends DefaultCellEditor {

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

        public DescTextFieldEditor() {
            super(new JTextField());
            textField = new JTextField();
            textField.addFocusListener(new TextFieldFocusListener());
            editorComponent = textField;

            delegate = new EditorDelegate() {
                private static final long serialVersionUID = 1L;

                public void setValue(Object value) {
                    if (StringWithDescTuple.class.isAssignableFrom(value.getClass())) {
                        StringWithDescTuple swdt = (StringWithDescTuple) value;
                        textField.setText((value != null) ? swdt.tuple.getText() : "");
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
    }

    public static class DescTypeFieldEditor extends AbstractPopupFieldEditor {

        private static final long serialVersionUID = 1L;

        public DescTypeFieldEditor(I_ConfigAceFrame config) throws TerminologyException, IOException {
            super(config);
        }

        @Override
        public int[] getPopupValues() {
            return config.getEditDescTypePopup().getListArray();
        }

        @Override
        public I_GetConceptData getSelectedItem(Object value) throws TerminologyException, IOException {
            StringWithDescTuple swdt = (StringWithDescTuple) value;
            return Terms.get().getConcept(swdt.getTuple().getTypeId());
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

    public static class DescStatusFieldEditor extends AbstractPopupFieldEditor {

        private static final long serialVersionUID = 1L;

        public DescStatusFieldEditor(I_ConfigAceFrame config) throws TerminologyException, IOException {
            super(config);
        }

        @Override
        public int[] getPopupValues() {
            return config.getEditStatusTypePopup().getListArray();
        }

        @Override
        public I_GetConceptData getSelectedItem(Object value) throws TerminologyException, IOException {
            StringWithDescTuple swdt = (StringWithDescTuple) value;
            return Terms.get().getConcept(swdt.getTuple().getStatusId());
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

    public DESC_FIELD[] getColumnEnums() {
        return columns;
    }

    public abstract String getScore(int rowIndex);

}
