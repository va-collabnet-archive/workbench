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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
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
import org.dwfa.ace.api.I_HoldRefsetPreferences;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartCrossmap;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguageScoped;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.I_RefsetDefaults;
import org.dwfa.ace.refset.RefsetHelper;
import org.dwfa.ace.timer.UpdateAlertsTimer;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.IntList;
import org.dwfa.vodb.types.ThinExtByRefPart;
import org.dwfa.vodb.types.ThinExtByRefPartBoolean;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptConceptString;
import org.dwfa.vodb.types.ThinExtByRefPartConceptInt;
import org.dwfa.vodb.types.ThinExtByRefPartCrossmap;
import org.dwfa.vodb.types.ThinExtByRefPartCrossmapForRel;
import org.dwfa.vodb.types.ThinExtByRefPartInteger;
import org.dwfa.vodb.types.ThinExtByRefPartLanguage;
import org.dwfa.vodb.types.ThinExtByRefPartLanguageScoped;
import org.dwfa.vodb.types.ThinExtByRefPartMeasurement;
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.dwfa.vodb.types.ThinExtByRefPartTemplate;
import org.dwfa.vodb.types.ThinExtByRefPartTemplateForRel;
import org.dwfa.vodb.types.ThinExtByRefTuple;
import org.dwfa.vodb.types.ThinExtByRefVersioned;

public class RefsetMemberTableModel extends AbstractTableModel implements PropertyChangeListener, I_HoldRefsetData,
        ActionListener {

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
        REFSET_FIELDS field = REFSET_FIELDS.STRING_VALUE;

        public StringExtFieldEditor() {
            super(new JTextField());
            textField = new JTextField();
            textField.addFocusListener(new TextFieldFocusListener());
            editorComponent = textField;

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
                return ((I_ThinExtByRefPartString) swet.getTuple().getPart()).getStringValue();
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

        public ConceptFieldEditor(I_ConfigAceFrame config, IntList popupIds, REFSET_FIELDS field) {
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

            switch (field) {
            case REFSET_ID:
                return ConceptBean.get(swet.getTuple().getRefsetId());
            case MEMBER_ID:
                throw new UnsupportedOperationException("Can't do concept combobox on " + field);
            case COMPONENT_ID:
                throw new UnsupportedOperationException("Can't do concept combobox on " + field);
            case STATUS:
                return ConceptBean.get(swet.getTuple().getStatusId());
            case VERSION:
                throw new UnsupportedOperationException("Can't do concept combobox on " + field);
            case PATH:
                throw new UnsupportedOperationException("Can't do concept combobox on " + field);
            case BOOLEAN_VALUE:
                throw new UnsupportedOperationException("Can't do concept combobox on " + field);
            case CONCEPT_ID:
                return ConceptBean.get(((I_ThinExtByRefPartConcept) swet.getTuple().getPart()).getC1id());
            case CONCEPT_2_ID:
                return ConceptBean.get(((I_ThinExtByRefPartConceptConcept) swet.getTuple().getPart()).getC2id());
            case INTEGER_VALUE:
                throw new UnsupportedOperationException("Can't do concept combobox on " + field);
            case ACCEPTABILITY:
                return ConceptBean.get(((I_ThinExtByRefPartLanguage) swet.getTuple().getPart()).getAcceptabilityId());
            case CORRECTNESS:
                return ConceptBean.get(((I_ThinExtByRefPartLanguage) swet.getTuple().getPart()).getCorrectnessId());
            case DEGREE_OF_SYNONYMY:
                return ConceptBean.get(((I_ThinExtByRefPartLanguage) swet.getTuple().getPart()).getDegreeOfSynonymyId());
            case TAG:
                return ConceptBean.get(((I_ThinExtByRefPartLanguageScoped) swet.getTuple().getPart()).getTagId());
            case SCOPE:
                return ConceptBean.get(((I_ThinExtByRefPartLanguageScoped) swet.getTuple().getPart()).getScopeId());
            case PRIORITY:
                throw new UnsupportedOperationException("Can't do concept combobox on " + field);
            case MEASUREMENT_UNITS_ID:
                return ConceptBean.get(((I_ThinExtByRefPartMeasurement) swet.getTuple().getPart()).getUnitsOfMeasureId());
            case MEASUREMENT_VALUE:
            default:
                throw new UnsupportedOperationException("Can't do concept combobox on " + field);

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
        REFSET_ID("refset", 5, 75, 1000), MEMBER_ID("member id", 5, 100, 100), COMPONENT_ID("component id", 5, 100, 100), STATUS("status", 5, 50, 250), VERSION("version", 5, 140, 140), PATH("path", 5, 90, 180),

        // Boolean extension
        BOOLEAN_VALUE("boolean value", 5, 100, 500),

        // String extension
        STRING_VALUE("string value", 75, 250, 1000),

        // String extension
        CONCEPT_CONCEPT_STRING_VALUE("string value", 75, 250, 1000),

        // Concept extension
        CONCEPT_ID("concept", 5, 300, 1000),

        // Concept extension
        CONCEPT_2_ID("concept 2", 5, 300, 1000),

        // Integer extension
        INTEGER_VALUE("integer value", 5, 100, 500),

        // Language extension
        ACCEPTABILITY("acceptability", 5, 125, 1000), CORRECTNESS("correctness", 5, 125, 1000), DEGREE_OF_SYNONYMY("synonymy", 5, 125, 1000),

        // Scoped language extension
        TAG("tag", 5, 100, 1000), SCOPE("scope", 5, 100, 1000), PRIORITY("priority", 5, 50, 100),

        // Measurement extension
        MEASUREMENT_VALUE("measurement value", 75, 100, 1000), MEASUREMENT_UNITS_ID("units of measure", 75, 100, 1000),

        // Template for rel extension
        T_VALUE_TYPE("value type", 5, 100, 1000), T_CARDINALITY("cardinality", 5, 50, 100), T_SEMANTIC_STATUS("semantic status", 5, 100, 1000), T_BROWSE_ATTRIBUTE_ORDER("attribute order", 5, 50, 100), T_BROWSE_VALUE_ORDER("value order", 5, 50, 100), T_NOTES_SCREEN_ORDER("screen order", 5, 50, 100), T_DISPLAY_STATUS("display status", 5, 100, 1000), T_CHARACTERISTIC_STATUS("characteristic status", 5, 100, 1000),

        // Template extension

        T_ATTRIBUTE("attribute", 5, 100, 1000), T_TARGET("value", 5, 100, 1000),

        // Cross mapping for rel extension
        MAP_REFINABILITY("map refinability", 5, 100, 1000), MAP_ADDITIONAL_CODE("additional code", 5, 100, 1000), MAP_ELEMENT_NO("element", 5, 50, 100), MAP_BLOCK_NO("block", 5, 50, 100),

        // Cross mapping extension
        MAP_STATUS("map status", 5, 100, 1000), MAP_TARGET("target", 5, 100, 1000)

        ;
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

    private static REFSET_FIELDS[] booleanRefsetFields = new REFSET_FIELDS[] {
                                                                              REFSET_FIELDS.REFSET_ID,
                                                                              // REFSET_FIELDS.MEMBER_ID,
                                                                              // REFSET_FIELDS.COMPONENT_ID,
                                                                              REFSET_FIELDS.BOOLEAN_VALUE,
                                                                              REFSET_FIELDS.STATUS,
                                                                              REFSET_FIELDS.VERSION, REFSET_FIELDS.PATH };

    private static REFSET_FIELDS[] booleanRefsetFieldsNoHistory = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
                                                                                       // REFSET_FIELDS.MEMBER_ID,
                                                                                       // REFSET_FIELDS.COMPONENT_ID,
                                                                                       REFSET_FIELDS.BOOLEAN_VALUE,
                                                                                       REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };

    private static REFSET_FIELDS[] stringRefsetFields = new REFSET_FIELDS[] {
                                                                             REFSET_FIELDS.REFSET_ID,
                                                                             // REFSET_FIELDS.MEMBER_ID,
                                                                             // REFSET_FIELDS.COMPONENT_ID,
                                                                             REFSET_FIELDS.STRING_VALUE,
                                                                             REFSET_FIELDS.STATUS,
                                                                             REFSET_FIELDS.VERSION, REFSET_FIELDS.PATH };

    private static REFSET_FIELDS[] stringRefsetFieldsNoHistory = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
                                                                                      // REFSET_FIELDS.MEMBER_ID,
                                                                                      // REFSET_FIELDS.COMPONENT_ID,
                                                                                      REFSET_FIELDS.STRING_VALUE,
                                                                                      REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.PATH
    };

    private static REFSET_FIELDS[] conceptRefsetFields = new REFSET_FIELDS[] {
                                                                              REFSET_FIELDS.REFSET_ID,
                                                                              // REFSET_FIELDS.MEMBER_ID,
                                                                              // REFSET_FIELDS.COMPONENT_ID,
                                                                              REFSET_FIELDS.CONCEPT_ID,
                                                                              REFSET_FIELDS.STATUS,
                                                                              REFSET_FIELDS.VERSION, REFSET_FIELDS.PATH };

    private static REFSET_FIELDS[] conceptRefsetFieldsNoHistory = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
                                                                                       // REFSET_FIELDS.MEMBER_ID,
                                                                                       // REFSET_FIELDS.COMPONENT_ID,
                                                                                       REFSET_FIELDS.CONCEPT_ID,
                                                                                       REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };

    private static REFSET_FIELDS[] conIntRefsetFields = new REFSET_FIELDS[] {
                                                                             REFSET_FIELDS.REFSET_ID,
                                                                             // REFSET_FIELDS.MEMBER_ID,
                                                                             // REFSET_FIELDS.COMPONENT_ID,
                                                                             REFSET_FIELDS.CONCEPT_ID,
                                                                             REFSET_FIELDS.INTEGER_VALUE,
                                                                             REFSET_FIELDS.STATUS,
                                                                             REFSET_FIELDS.VERSION, REFSET_FIELDS.PATH };

    private static REFSET_FIELDS[] conIntRefsetFieldsNoHistory = new REFSET_FIELDS[] {
                                                                                      REFSET_FIELDS.REFSET_ID,
                                                                                      // REFSET_FIELDS.MEMBER_ID,
                                                                                      // REFSET_FIELDS.COMPONENT_ID,
                                                                                      REFSET_FIELDS.CONCEPT_ID,
                                                                                      REFSET_FIELDS.INTEGER_VALUE,
                                                                                      REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };
    private static REFSET_FIELDS[] conConStrRefsetFieldsNoHistory = new REFSET_FIELDS[] {
        REFSET_FIELDS.REFSET_ID,
        REFSET_FIELDS.CONCEPT_ID,
        REFSET_FIELDS.CONCEPT_2_ID,
        REFSET_FIELDS.CONCEPT_CONCEPT_STRING_VALUE,
        REFSET_FIELDS.STATUS
    };
    private static REFSET_FIELDS[] conceptConceptStrRefsetFields = new REFSET_FIELDS[] {
        REFSET_FIELDS.REFSET_ID,
        // REFSET_FIELDS.MEMBER_ID,
        // REFSET_FIELDS.COMPONENT_ID,
        REFSET_FIELDS.CONCEPT_ID,
        REFSET_FIELDS.CONCEPT_2_ID,
        REFSET_FIELDS.CONCEPT_CONCEPT_STRING_VALUE,
        REFSET_FIELDS.STATUS,
        REFSET_FIELDS.VERSION,
        REFSET_FIELDS.PATH
    };

    private static REFSET_FIELDS[] measurementRefsetFields = new REFSET_FIELDS[] {
                                                                                  REFSET_FIELDS.REFSET_ID,
                                                                                  // REFSET_FIELDS.MEMBER_ID,
                                                                                  // REFSET_FIELDS.COMPONENT_ID,
                                                                                  REFSET_FIELDS.MEASUREMENT_VALUE,
                                                                                  REFSET_FIELDS.MEASUREMENT_UNITS_ID,
                                                                                  REFSET_FIELDS.STATUS,
                                                                                  REFSET_FIELDS.VERSION,
                                                                                  REFSET_FIELDS.PATH };

    private static REFSET_FIELDS[] measurementRefsetFieldsNoHistory = new REFSET_FIELDS[] {
                                                                                           REFSET_FIELDS.REFSET_ID,
                                                                                           // REFSET_FIELDS.MEMBER_ID,
                                                                                           // REFSET_FIELDS.COMPONENT_ID,
                                                                                           REFSET_FIELDS.MEASUREMENT_VALUE,
                                                                                           REFSET_FIELDS.MEASUREMENT_UNITS_ID,
                                                                                           REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };

    private static REFSET_FIELDS[] integerRefsetFields = new REFSET_FIELDS[] {
                                                                              REFSET_FIELDS.REFSET_ID,
                                                                              // REFSET_FIELDS.MEMBER_ID,
                                                                              // REFSET_FIELDS.COMPONENT_ID,
                                                                              REFSET_FIELDS.INTEGER_VALUE,
                                                                              REFSET_FIELDS.STATUS,
                                                                              REFSET_FIELDS.VERSION, REFSET_FIELDS.PATH };

    private static REFSET_FIELDS[] integerRefsetFieldsNoHistory = new REFSET_FIELDS[] { REFSET_FIELDS.REFSET_ID,
                                                                                       // REFSET_FIELDS.MEMBER_ID,
                                                                                       // REFSET_FIELDS.COMPONENT_ID,
                                                                                       REFSET_FIELDS.INTEGER_VALUE,
                                                                                       REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };

    private static REFSET_FIELDS[] languageRefsetFields = new REFSET_FIELDS[] {
                                                                               REFSET_FIELDS.REFSET_ID,
                                                                               // REFSET_FIELDS.MEMBER_ID,
                                                                               // REFSET_FIELDS.COMPONENT_ID,
                                                                               REFSET_FIELDS.ACCEPTABILITY,
                                                                               REFSET_FIELDS.CORRECTNESS,
                                                                               REFSET_FIELDS.DEGREE_OF_SYNONYMY,
                                                                               REFSET_FIELDS.STATUS,
                                                                               REFSET_FIELDS.VERSION,
                                                                               REFSET_FIELDS.PATH };

    private static REFSET_FIELDS[] languageRefsetFieldsNoHistory = new REFSET_FIELDS[] {
                                                                                        REFSET_FIELDS.REFSET_ID,
                                                                                        // REFSET_FIELDS.MEMBER_ID,
                                                                                        // REFSET_FIELDS.COMPONENT_ID,
                                                                                        REFSET_FIELDS.ACCEPTABILITY,
                                                                                        REFSET_FIELDS.CORRECTNESS,
                                                                                        REFSET_FIELDS.DEGREE_OF_SYNONYMY,
                                                                                        REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };

    private static REFSET_FIELDS[] scopedLanguageRefsetFields = new REFSET_FIELDS[] {
                                                                                     REFSET_FIELDS.REFSET_ID,
                                                                                     // REFSET_FIELDS.MEMBER_ID,
                                                                                     // REFSET_FIELDS.COMPONENT_ID,
                                                                                     REFSET_FIELDS.ACCEPTABILITY,
                                                                                     REFSET_FIELDS.CORRECTNESS,
                                                                                     REFSET_FIELDS.DEGREE_OF_SYNONYMY,
                                                                                     REFSET_FIELDS.TAG,
                                                                                     REFSET_FIELDS.SCOPE,
                                                                                     REFSET_FIELDS.PRIORITY,
                                                                                     REFSET_FIELDS.STATUS,
                                                                                     REFSET_FIELDS.VERSION,
                                                                                     REFSET_FIELDS.PATH };

    private static REFSET_FIELDS[] scopedLanguageRefsetFieldsNoHistory = new REFSET_FIELDS[] {
                                                                                              REFSET_FIELDS.REFSET_ID,
                                                                                              // REFSET_FIELDS.MEMBER_ID,
                                                                                              // REFSET_FIELDS.COMPONENT_ID,
                                                                                              REFSET_FIELDS.ACCEPTABILITY,
                                                                                              REFSET_FIELDS.CORRECTNESS,
                                                                                              REFSET_FIELDS.DEGREE_OF_SYNONYMY,
                                                                                              REFSET_FIELDS.TAG,
                                                                                              REFSET_FIELDS.SCOPE,
                                                                                              REFSET_FIELDS.PRIORITY,
                                                                                              REFSET_FIELDS.STATUS, // REFSET_FIELDS.VERSION,
    // REFSET_FIELDS.BRANCH
    };

    private static REFSET_FIELDS[] templateForRel = new REFSET_FIELDS[] {
                                                                         REFSET_FIELDS.REFSET_ID,
                                                                         // REFSET_FIELDS.MEMBER_ID,
                                                                         // REFSET_FIELDS.COMPONENT_ID,
                                                                         REFSET_FIELDS.T_VALUE_TYPE,
                                                                         REFSET_FIELDS.T_CARDINALITY,
                                                                         REFSET_FIELDS.T_SEMANTIC_STATUS,
                                                                         REFSET_FIELDS.T_BROWSE_ATTRIBUTE_ORDER,
                                                                         REFSET_FIELDS.T_BROWSE_VALUE_ORDER,
                                                                         REFSET_FIELDS.T_NOTES_SCREEN_ORDER,
                                                                         REFSET_FIELDS.T_DISPLAY_STATUS,
                                                                         REFSET_FIELDS.T_CHARACTERISTIC_STATUS,
                                                                         REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION,
                                                                         REFSET_FIELDS.PATH

    };

    private static REFSET_FIELDS[] templateForRelNoHistory = new REFSET_FIELDS[] {
                                                                                  REFSET_FIELDS.REFSET_ID,
                                                                                  // REFSET_FIELDS.MEMBER_ID,
                                                                                  // REFSET_FIELDS.COMPONENT_ID,
                                                                                  REFSET_FIELDS.T_VALUE_TYPE,
                                                                                  REFSET_FIELDS.T_CARDINALITY,
                                                                                  REFSET_FIELDS.T_SEMANTIC_STATUS,
                                                                                  REFSET_FIELDS.T_BROWSE_ATTRIBUTE_ORDER,
                                                                                  REFSET_FIELDS.T_BROWSE_VALUE_ORDER,
                                                                                  REFSET_FIELDS.T_NOTES_SCREEN_ORDER,
                                                                                  REFSET_FIELDS.T_DISPLAY_STATUS,
                                                                                  REFSET_FIELDS.T_CHARACTERISTIC_STATUS,
                                                                                  REFSET_FIELDS.STATUS };

    private static REFSET_FIELDS[] template = new REFSET_FIELDS[] {
                                                                   REFSET_FIELDS.REFSET_ID,
                                                                   // REFSET_FIELDS.MEMBER_ID,
                                                                   // REFSET_FIELDS.COMPONENT_ID,
                                                                   REFSET_FIELDS.T_ATTRIBUTE, REFSET_FIELDS.T_TARGET,
                                                                   REFSET_FIELDS.T_VALUE_TYPE,
                                                                   REFSET_FIELDS.T_CARDINALITY,
                                                                   REFSET_FIELDS.T_SEMANTIC_STATUS,
                                                                   REFSET_FIELDS.T_BROWSE_ATTRIBUTE_ORDER,
                                                                   REFSET_FIELDS.T_BROWSE_VALUE_ORDER,
                                                                   REFSET_FIELDS.T_NOTES_SCREEN_ORDER,
                                                                   REFSET_FIELDS.T_DISPLAY_STATUS,
                                                                   REFSET_FIELDS.T_CHARACTERISTIC_STATUS,
                                                                   REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION,
                                                                   REFSET_FIELDS.PATH };
    private static REFSET_FIELDS[] templateNoHistory = new REFSET_FIELDS[] {
                                                                            REFSET_FIELDS.REFSET_ID,
                                                                            // REFSET_FIELDS.MEMBER_ID,
                                                                            // REFSET_FIELDS.COMPONENT_ID,
                                                                            REFSET_FIELDS.T_ATTRIBUTE,
                                                                            REFSET_FIELDS.T_TARGET,
                                                                            REFSET_FIELDS.T_VALUE_TYPE,
                                                                            REFSET_FIELDS.T_CARDINALITY,
                                                                            REFSET_FIELDS.T_SEMANTIC_STATUS,
                                                                            REFSET_FIELDS.T_BROWSE_ATTRIBUTE_ORDER,
                                                                            REFSET_FIELDS.T_BROWSE_VALUE_ORDER,
                                                                            REFSET_FIELDS.T_NOTES_SCREEN_ORDER,
                                                                            REFSET_FIELDS.T_DISPLAY_STATUS,
                                                                            REFSET_FIELDS.T_CHARACTERISTIC_STATUS,
                                                                            REFSET_FIELDS.STATUS };

    private static REFSET_FIELDS[] crossMapForRelFields = new REFSET_FIELDS[] {
                                                                               REFSET_FIELDS.REFSET_ID,
                                                                               // REFSET_FIELDS.MEMBER_ID,
                                                                               // REFSET_FIELDS.COMPONENT_ID,
                                                                               REFSET_FIELDS.MAP_REFINABILITY,
                                                                               REFSET_FIELDS.MAP_ADDITIONAL_CODE,
                                                                               REFSET_FIELDS.MAP_ELEMENT_NO,
                                                                               REFSET_FIELDS.MAP_BLOCK_NO,
                                                                               REFSET_FIELDS.STATUS,
                                                                               REFSET_FIELDS.VERSION,
                                                                               REFSET_FIELDS.PATH };
    private static REFSET_FIELDS[] crossMapForRelFieldsNoHistory = new REFSET_FIELDS[] {
                                                                                        REFSET_FIELDS.REFSET_ID,
                                                                                        // REFSET_FIELDS.MEMBER_ID,
                                                                                        // REFSET_FIELDS.COMPONENT_ID,
                                                                                        REFSET_FIELDS.MAP_REFINABILITY,
                                                                                        REFSET_FIELDS.MAP_ADDITIONAL_CODE,
                                                                                        REFSET_FIELDS.MAP_ELEMENT_NO,
                                                                                        REFSET_FIELDS.MAP_BLOCK_NO,
                                                                                        REFSET_FIELDS.STATUS };

    private static REFSET_FIELDS[] crossMapFields = new REFSET_FIELDS[] {
                                                                         REFSET_FIELDS.REFSET_ID,
                                                                         // REFSET_FIELDS.MEMBER_ID,
                                                                         // REFSET_FIELDS.COMPONENT_ID,
                                                                         REFSET_FIELDS.MAP_STATUS,
                                                                         REFSET_FIELDS.MAP_TARGET,
                                                                         REFSET_FIELDS.MAP_REFINABILITY,
                                                                         REFSET_FIELDS.MAP_ADDITIONAL_CODE,
                                                                         REFSET_FIELDS.MAP_ELEMENT_NO,
                                                                         REFSET_FIELDS.MAP_BLOCK_NO,
                                                                         REFSET_FIELDS.STATUS, REFSET_FIELDS.VERSION,
                                                                         REFSET_FIELDS.PATH

    };
    private static REFSET_FIELDS[] crossMapFieldsNoHistory = new REFSET_FIELDS[] {
                                                                                  REFSET_FIELDS.REFSET_ID,
                                                                                  // REFSET_FIELDS.MEMBER_ID,
                                                                                  // REFSET_FIELDS.COMPONENT_ID,
                                                                                  REFSET_FIELDS.MAP_STATUS,
                                                                                  REFSET_FIELDS.MAP_TARGET,
                                                                                  REFSET_FIELDS.MAP_REFINABILITY,
                                                                                  REFSET_FIELDS.MAP_ADDITIONAL_CODE,
                                                                                  REFSET_FIELDS.MAP_ELEMENT_NO,
                                                                                  REFSET_FIELDS.MAP_BLOCK_NO,
                                                                                  REFSET_FIELDS.STATUS

    };

    public static REFSET_FIELDS[] getRefsetColumns(I_HostConceptPlugins host, EXT_TYPE type) {
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
            case LANGUAGE:
                return languageRefsetFields;
            case SCOPED_LANGUAGE:
                return scopedLanguageRefsetFields;
            case MEASUREMENT:
                return measurementRefsetFields;
            case CROSS_MAP:
                return crossMapFields;
            case CROSS_MAP_FOR_REL:
                return crossMapForRelFields;
            case TEMPLATE:
                return template;
            case TEMPLATE_FOR_REL:
                return templateForRel;
            case CONCEPT_CONCEPT_STRING:
                return conceptConceptStrRefsetFields;
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
            case LANGUAGE:
                return languageRefsetFieldsNoHistory;
            case SCOPED_LANGUAGE:
                return scopedLanguageRefsetFieldsNoHistory;
            case MEASUREMENT:
                return measurementRefsetFieldsNoHistory;
            case CROSS_MAP:
                return crossMapFieldsNoHistory;
            case CROSS_MAP_FOR_REL:
                return crossMapForRelFieldsNoHistory;
            case TEMPLATE:
                return templateNoHistory;
            case TEMPLATE_FOR_REL:
                return templateForRelNoHistory;
            case CONCEPT_CONCEPT_STRING:
                return conConStrRefsetFieldsNoHistory;
            default:
                throw new UnsupportedOperationException("Can't handle type: " + type);
            }
        }
    }

    private REFSET_FIELDS[] columns;

    private SmallProgressPanel progress = new SmallProgressPanel();

    I_HostConceptPlugins host;

    List<ThinExtByRefTuple> allTuples;

    ArrayList<ThinExtByRefVersioned> allExtensions;

    Map<Integer, ConceptBean> referencedConcepts = new HashMap<Integer, ConceptBean>();

    private Set<Integer> conceptsToFetch = new HashSet<Integer>();

    private TableChangedSwingWorker tableChangeWorker;

    private ReferencedConceptsSwingWorker refConWorker;

    private int tableComponentId = Integer.MIN_VALUE;

    private JButton addButton;

    private EXT_TYPE refsetType;

    private TOGGLES toggle;

    private RefsetHelper refsetHelper = new RefsetHelper();

    protected Class<? extends ThinExtByRefPart> getExtPartClass() {
        switch (refsetType) {
        case BOOLEAN:
            return ThinExtByRefPartBoolean.class;
        case STRING:
            return ThinExtByRefPartString.class;
        case CONCEPT:
            return ThinExtByRefPartConcept.class;
        case CON_INT:
            return ThinExtByRefPartConceptInt.class;
        case CONCEPT_CONCEPT_STRING:
            return ThinExtByRefPartConceptConceptString.class;
        case INTEGER:
            return ThinExtByRefPartInteger.class;
        case LANGUAGE:
            return ThinExtByRefPartLanguage.class;
        case SCOPED_LANGUAGE:
            return ThinExtByRefPartLanguageScoped.class;
        case MEASUREMENT:
            return ThinExtByRefPartMeasurement.class;
        case CROSS_MAP:
            return ThinExtByRefPartCrossmap.class;
        case CROSS_MAP_FOR_REL:
            return ThinExtByRefPartCrossmapForRel.class;
        case TEMPLATE:
            return ThinExtByRefPartTemplate.class;
        case TEMPLATE_FOR_REL:
            return ThinExtByRefPartTemplateForRel.class;
        default:
            throw new UnsupportedOperationException("Can't handle type: " + refsetType);
        }
    }

    public class ReferencedConceptsSwingWorker extends SwingWorker<Map<Integer, ConceptBean>> {
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
            List<I_GetExtensionData> extensions = AceConfig.getVodb().getExtensionsForComponent(componentId);
            extensions.addAll(ExtensionByReferenceBean.getNewExtensions(componentId));
            I_IntSet allowedStatus = host.getConfig().getAllowedStatus();
            for (I_GetExtensionData ebrBean : extensions) {
                if (stopWork) {
                    return false;
                }
                conceptsToFetch.add(ebrBean.getExtension().getRefsetId());
                List<I_ThinExtByRefPart> parts = new ArrayList<I_ThinExtByRefPart>();
                if (!host.getShowHistory()) {
                    for (I_ThinExtByRefTuple tuple : ebrBean.getExtension().getTuples(allowedStatus, null, true, true)) {
                        parts.add(tuple.getPart());
                    }
                } else {
                    parts.addAll(ebrBean.getExtension().getVersions());
                }
                for (I_ThinExtByRefPart part : parts) {
                    if (getExtPartClass().equals(part.getClass()) == false) {
                        break;
                    }
                    if (ThinExtByRefPartBoolean.class.equals(part.getClass())) {
                        conceptsToFetch.add(part.getStatusId());
                        conceptsToFetch.add(part.getPathId());
                    } else if (ThinExtByRefPartString.class.equals(part.getClass())) {
                        conceptsToFetch.add(part.getStatusId());
                        conceptsToFetch.add(part.getPathId());
                    } else if (ThinExtByRefPartConcept.class.equals(part.getClass())) {
                        I_ThinExtByRefPartConcept conceptPart = (I_ThinExtByRefPartConcept) part;
                        conceptsToFetch.add(conceptPart.getC1id());
                        conceptsToFetch.add(part.getStatusId());
                        conceptsToFetch.add(part.getPathId());
                    } else if (ThinExtByRefPartConceptInt.class.equals(part.getClass())) {
                        I_ThinExtByRefPartConceptInt conceptPart = (I_ThinExtByRefPartConceptInt) part;
                        conceptsToFetch.add(conceptPart.getC1id());
                        conceptsToFetch.add(part.getStatusId());
                        conceptsToFetch.add(part.getPathId());
                    } else if (ThinExtByRefPartConceptConceptString.class.equals(part.getClass())) {
                        ThinExtByRefPartConceptConceptString conceptPart = (ThinExtByRefPartConceptConceptString) part;
                        conceptsToFetch.add(conceptPart.getC1id());
                        conceptsToFetch.add(conceptPart.getC2id());
                        conceptsToFetch.add(part.getStatusId());
                        conceptsToFetch.add(part.getPathId());
                    } else if (ThinExtByRefPartMeasurement.class.equals(part.getClass())) {
                        I_ThinExtByRefPartMeasurement conceptPart = (I_ThinExtByRefPartMeasurement) part;
                        conceptsToFetch.add(conceptPart.getUnitsOfMeasureId());
                        conceptsToFetch.add(part.getStatusId());
                        conceptsToFetch.add(part.getPathId());
                    } else if (ThinExtByRefPartInteger.class.equals(part.getClass())) {
                        conceptsToFetch.add(part.getStatusId());
                        conceptsToFetch.add(part.getPathId());
                    } else if (ThinExtByRefPartLanguage.class.equals(part.getClass())) {
                        I_ThinExtByRefPartLanguage conceptPart = (I_ThinExtByRefPartLanguage) part;
                        conceptsToFetch.add(conceptPart.getAcceptabilityId());
                        conceptsToFetch.add(conceptPart.getCorrectnessId());
                        conceptsToFetch.add(conceptPart.getDegreeOfSynonymyId());
                        conceptsToFetch.add(part.getStatusId());
                        conceptsToFetch.add(part.getPathId());
                    } else if (ThinExtByRefPartLanguageScoped.class.equals(part.getClass())) {
                        ThinExtByRefPartLanguageScoped conceptPart = (ThinExtByRefPartLanguageScoped) part;
                        conceptsToFetch.add(conceptPart.getScopeId());
                        conceptsToFetch.add(conceptPart.getTagId());
                        conceptsToFetch.add(conceptPart.getAcceptabilityId());
                        conceptsToFetch.add(conceptPart.getCorrectnessId());
                        conceptsToFetch.add(conceptPart.getDegreeOfSynonymyId());
                        conceptsToFetch.add(part.getStatusId());
                        conceptsToFetch.add(part.getPathId());
                    }
                    if (stopWork) {
                        return false;
                    }
                    if (allTuples == null) {
                        AceLog.getAppLog().info("all tuples for RefsetMemberTableModel is  null");
                        return false;
                    }
                    allTuples.add(new ThinExtByRefTuple(ebrBean.getExtension(), part));
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
                    if (conceptsToFetch.size() == 0) {
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

    public RefsetMemberTableModel(I_HostConceptPlugins host, REFSET_FIELDS[] columns, EXT_TYPE refsetType,
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
        case CONCEPT_CONCEPT_STRING:
            return host.getConfig().getRefsetPreferencesForToggle(toggle).getConceptConceptStringPreferences();
        case INTEGER:
            return host.getConfig().getRefsetPreferencesForToggle(toggle).getIntegerPreferences();
        case LANGUAGE:
            return host.getConfig().getRefsetPreferencesForToggle(toggle).getLanguagePreferences();
        case SCOPED_LANGUAGE:
            return host.getConfig().getRefsetPreferencesForToggle(toggle).getLanguageScopedPreferences();
        case MEASUREMENT:
            return host.getConfig().getRefsetPreferencesForToggle(toggle).getMeasurementPreferences();
        case CROSS_MAP:
            return host.getConfig().getRefsetPreferencesForToggle(toggle).getCrossMapPreferences();
        case CROSS_MAP_FOR_REL:
            return host.getConfig().getRefsetPreferencesForToggle(toggle).getCrossMapForRelPreferences();
        case TEMPLATE:
            return host.getConfig().getRefsetPreferencesForToggle(toggle).getTemplatePreferences();
        case TEMPLATE_FOR_REL:
            return host.getConfig().getRefsetPreferencesForToggle(toggle).getTemplateForRelPreferences();
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

            boolean inConflict = (host.getConfig().getHighlightConflictsInComponentPanel() && host.getConfig()
                .getConflictResolutionStrategy()
                .isInConflict(tuple.getCore()));

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
                if (referencedConcepts.containsKey(tuple.getStatusId())) {
                    return new StringWithExtTuple(getPrefText(tuple.getStatusId()), tuple, tuple.getStatusId(),
                        inConflict);
                }
                return new StringWithExtTuple(Integer.toString(tuple.getStatusId()), tuple, tuple.getStatusId(),
                    inConflict);

            case VERSION:
                if (tuple.getVersion() == Integer.MAX_VALUE) {
                    return new StringWithExtTuple(ThinVersionHelper.uncommittedHtml(), tuple, tuple.getMemberId(),
                        inConflict);
                }
                return new StringWithExtTuple(ThinVersionHelper.format(tuple.getVersion()), tuple, tuple.getMemberId(),
                    inConflict);

            case PATH:
                if (referencedConcepts.containsKey(tuple.getPathId())) {
                    return new StringWithExtTuple(getPrefText(tuple.getPathId()), tuple, tuple.getPathId(), inConflict);
                }
                return new StringWithExtTuple(Integer.toString(tuple.getPathId()), tuple, tuple.getPathId(), inConflict);

            case BOOLEAN_VALUE:
                return new StringWithExtTuple(
                    Boolean.toString(((I_ThinExtByRefPartBoolean) tuple.getPart()).getValue()), tuple,
                    tuple.getMemberId(), inConflict);

            case STRING_VALUE:
                return new StringWithExtTuple(((I_ThinExtByRefPartString) tuple.getPart()).getStringValue(), tuple,
                    tuple.getMemberId(), inConflict);

            case CONCEPT_CONCEPT_STRING_VALUE:
                return new StringWithExtTuple(((I_ThinExtByRefPartConceptConceptString) tuple.getPart()).getStringValue().replaceAll("\t", " "), tuple,
                    tuple.getMemberId(), inConflict);

            case CONCEPT_ID:
                if (referencedConcepts.containsKey(((I_ThinExtByRefPartConcept) tuple.getPart()).getC1id())) {
                    return new StringWithExtTuple(getPrefText(((I_ThinExtByRefPartConcept) tuple.getPart()).getC1id()),
                        tuple, ((I_ThinExtByRefPartConcept) tuple.getPart()).getC1id(), inConflict);
                }
                return new StringWithExtTuple(
                    Integer.toString(((I_ThinExtByRefPartConcept) tuple.getPart()).getC1id()), tuple,
                    ((I_ThinExtByRefPartConcept) tuple.getPart()).getC1id(), inConflict);

            case CONCEPT_2_ID:
                if (referencedConcepts.containsKey(((I_ThinExtByRefPartConceptConcept) tuple.getPart()).getC1id())) {
                    return new StringWithExtTuple(getPrefText(((I_ThinExtByRefPartConceptConcept) tuple.getPart()).getC2id()),
                        tuple, ((I_ThinExtByRefPartConceptConcept) tuple.getPart()).getC2id(), inConflict);
                }
                return new StringWithExtTuple(
                    Integer.toString(((I_ThinExtByRefPartConceptConcept) tuple.getPart()).getC2id()), tuple,
                    ((I_ThinExtByRefPartConceptConcept) tuple.getPart()).getC2id(), inConflict);

            case INTEGER_VALUE:
                if (ThinExtByRefPartConceptInt.class.isAssignableFrom(tuple.getPart().getClass())) {
                    int value = ((ThinExtByRefPartConceptInt) tuple.getPart()).getIntValue();
                    if (refsetHelper.hasPurpose(tuple.getRefsetId(), RefsetAuxiliary.Concept.REFSET_PURPOSE_POSITION)) {
                        return new StringWithExtTuple((value == Integer.MAX_VALUE) ? "latest"
                                                                                  : ThinVersionHelper.format(value),
                            tuple, tuple.getMemberId(), inConflict);
                    } else {
                        return new StringWithExtTuple(Integer.toString(value), tuple, tuple.getMemberId(), inConflict);
                    }
                } else {
                    int value = ((I_ThinExtByRefPartInteger) tuple.getPart()).getIntValue();
                    return new StringWithExtTuple(Integer.toString(value), tuple, tuple.getMemberId(), inConflict);
                }

            case ACCEPTABILITY:
                if (referencedConcepts.containsKey(((I_ThinExtByRefPartLanguage) tuple.getPart()).getAcceptabilityId())) {
                    return new StringWithExtTuple(
                        getPrefText(((I_ThinExtByRefPartLanguage) tuple.getPart()).getAcceptabilityId()), tuple,
                        ((I_ThinExtByRefPartLanguage) tuple.getPart()).getAcceptabilityId(), inConflict);
                }
                return new StringWithExtTuple(
                    Integer.toString(((I_ThinExtByRefPartLanguage) tuple.getPart()).getAcceptabilityId()), tuple,
                    ((I_ThinExtByRefPartLanguage) tuple.getPart()).getAcceptabilityId(), inConflict);

            case CORRECTNESS:
                if (referencedConcepts.containsKey(((I_ThinExtByRefPartLanguage) tuple.getPart()).getCorrectnessId())) {
                    return new StringWithExtTuple(
                        getPrefText(((I_ThinExtByRefPartLanguage) tuple.getPart()).getCorrectnessId()), tuple,
                        ((I_ThinExtByRefPartLanguage) tuple.getPart()).getCorrectnessId(), inConflict);
                }
                return new StringWithExtTuple(
                    Integer.toString(((I_ThinExtByRefPartLanguage) tuple.getPart()).getCorrectnessId()), tuple,
                    ((I_ThinExtByRefPartLanguage) tuple.getPart()).getCorrectnessId(), inConflict);

            case DEGREE_OF_SYNONYMY:
                if (referencedConcepts.containsKey(((I_ThinExtByRefPartLanguage) tuple.getPart()).getDegreeOfSynonymyId())) {
                    return new StringWithExtTuple(
                        getPrefText(((I_ThinExtByRefPartLanguage) tuple.getPart()).getDegreeOfSynonymyId()), tuple,
                        ((I_ThinExtByRefPartLanguage) tuple.getPart()).getDegreeOfSynonymyId(), inConflict);
                }
                return new StringWithExtTuple(
                    Integer.toString(((I_ThinExtByRefPartLanguage) tuple.getPart()).getDegreeOfSynonymyId()), tuple,
                    ((I_ThinExtByRefPartLanguage) tuple.getPart()).getDegreeOfSynonymyId(), inConflict);

            case TAG:
                if (referencedConcepts.containsKey(((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getTagId())) {
                    return new StringWithExtTuple(
                        getPrefText(((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getTagId()), tuple,
                        ((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getTagId(), inConflict);
                }
                return new StringWithExtTuple(
                    Integer.toString(((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getTagId()), tuple,
                    ((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getTagId(), inConflict);

            case SCOPE:
                if (referencedConcepts.containsKey(((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getScopeId())) {
                    return new StringWithExtTuple(
                        getPrefText(((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getScopeId()), tuple,
                        ((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getScopeId(), inConflict);
                }
                return new StringWithExtTuple(
                    Integer.toString(((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getScopeId()), tuple,
                    ((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getScopeId(), inConflict);

            case PRIORITY:
                return new StringWithExtTuple(
                    Integer.toString(((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getPriority()), tuple,
                    ((I_ThinExtByRefPartLanguageScoped) tuple.getPart()).getPriority(), inConflict);

            case MEASUREMENT_UNITS_ID:
                if (referencedConcepts.containsKey(((I_ThinExtByRefPartMeasurement) tuple.getPart()).getUnitsOfMeasureId())) {
                    return new StringWithExtTuple(
                        getPrefText(((I_ThinExtByRefPartMeasurement) tuple.getPart()).getUnitsOfMeasureId()), tuple,
                        ((I_ThinExtByRefPartMeasurement) tuple.getPart()).getUnitsOfMeasureId(), inConflict);
                }
                return new StringWithExtTuple(
                    Integer.toString(((I_ThinExtByRefPartMeasurement) tuple.getPart()).getUnitsOfMeasureId()), tuple,
                    ((I_ThinExtByRefPartMeasurement) tuple.getPart()).getUnitsOfMeasureId(), inConflict);

            case MEASUREMENT_VALUE:
                return new StringWithExtTuple(
                    Double.toString(((I_ThinExtByRefPartMeasurement) tuple.getPart()).getMeasurementValue()), tuple,
                    tuple.getMemberId(), inConflict);

            case MAP_REFINABILITY:
                return new StringWithExtTuple(
                    Double.toString(((I_ThinExtByRefPartCrossmap) tuple.getPart()).getRefineFlagId()), tuple,
                    ((I_ThinExtByRefPartCrossmap) tuple.getPart()).getRefineFlagId(), inConflict);

            case MAP_STATUS:
                return new StringWithExtTuple(
                    Double.toString(((I_ThinExtByRefPartCrossmap) tuple.getPart()).getMapStatusId()), tuple,
                    ((I_ThinExtByRefPartCrossmap) tuple.getPart()).getMapStatusId(), inConflict);
            }

            AceLog.getAppLog().alertAndLogException(new Exception("Can't handle column type: " + columns[columnIndex]));
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
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
            switch (refsetType) {
            case BOOLEAN:
                refsetDefaults = preferences.getBooleanPreferences();
                break;
            case STRING:
                refsetDefaults = preferences.getStringPreferences();
                break;
            case CONCEPT:
                refsetDefaults = preferences.getConceptPreferences();
                break;
            case CON_INT:
                refsetDefaults = preferences.getConIntPreferences();
                break;
            case CONCEPT_CONCEPT_STRING:
                refsetDefaults = preferences.getConceptConceptStringPreferences();
                break;
            case INTEGER:
                refsetDefaults = preferences.getIntegerPreferences();
                break;
            case MEASUREMENT:
                refsetDefaults = preferences.getMeasurementPreferences();
                break;
            case LANGUAGE:
                refsetDefaults = preferences.getLanguagePreferences();
                break;
            case SCOPED_LANGUAGE:
                refsetDefaults = preferences.getLanguageScopedPreferences();
                break;
            case CROSS_MAP:
                refsetDefaults = preferences.getCrossMapPreferences();
                break;
            case CROSS_MAP_FOR_REL:
                refsetDefaults = preferences.getCrossMapForRelPreferences();
                break;
            case TEMPLATE:
                refsetDefaults = preferences.getTemplatePreferences();
                break;
            case TEMPLATE_FOR_REL:
                refsetDefaults = preferences.getTemplateForRelPreferences();
                break;
            default:
                throw new UnsupportedOperationException("Can't handle ref set type: " + refsetType);
            }
            int refsetId = refsetDefaults.getDefaultRefset().getConceptId();
            int memberId = LocalVersionedTerminology.get().uuidToNativeWithGeneration(UUID.randomUUID(),
                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
                host.getConfig().getEditingPathSet(), Integer.MAX_VALUE);

            I_ThinExtByRefVersioned extension = new ThinExtByRefVersioned(refsetId, memberId, tableComponentId,
                ThinExtBinder.getExtensionTypeNid(refsetType));
            switch (refsetType) {
            case BOOLEAN:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartBoolean booleanPart = new ThinExtByRefPartBoolean();
                    booleanPart.setPathId(editPath.getConceptId());
                    booleanPart.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    booleanPart.setValue(preferences.getBooleanPreferences().getDefaultForBooleanRefset());
                    booleanPart.setVersion(Integer.MAX_VALUE);
                    extension.addVersion(booleanPart);
                }
                break;
            case STRING:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartString stringPart = new ThinExtByRefPartString();
                    stringPart.setPathId(editPath.getConceptId());
                    stringPart.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    stringPart.setStringValue(preferences.getStringPreferences().getDefaultForStringRefset());
                    stringPart.setVersion(Integer.MAX_VALUE);
                    extension.addVersion(stringPart);
                }
                break;
            case CONCEPT:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartConcept conceptPart = new ThinExtByRefPartConcept();
                    conceptPart.setPathId(editPath.getConceptId());
                    conceptPart.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    conceptPart.setConceptId(preferences.getConceptPreferences()
                        .getDefaultForConceptRefset()
                        .getConceptId());
                    conceptPart.setVersion(Integer.MAX_VALUE);
                    extension.addVersion(conceptPart);
                }
                break;
            case CON_INT:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartConceptInt conceptPart = new ThinExtByRefPartConceptInt();
                    conceptPart.setPathId(editPath.getConceptId());
                    conceptPart.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    conceptPart.setConceptId(preferences.getConIntPreferences()
                        .getDefaultForConceptRefset()
                        .getConceptId());
                    conceptPart.setIntValue(preferences.getConIntPreferences().getDefaultForIntegerValue());
                    conceptPart.setVersion(Integer.MAX_VALUE);
                    extension.addVersion(conceptPart);
                }
                break;
            case CONCEPT_CONCEPT_STRING:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartConceptConceptString conceptPart = new ThinExtByRefPartConceptConceptString();
                    conceptPart.setPathId(editPath.getConceptId());
                    conceptPart.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    conceptPart.setConceptId(preferences.getConIntPreferences()
                        .getDefaultForConceptRefset()
                        .getConceptId());
                    conceptPart.setStringValue(preferences.getConceptConceptStringPreferences().getDefaultForStringValue());
                    conceptPart.setVersion(Integer.MAX_VALUE);
                    extension.addVersion(conceptPart);
                }
                break;
            case INTEGER:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartInteger integerPart = new ThinExtByRefPartInteger();
                    integerPart.setPathId(editPath.getConceptId());
                    integerPart.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    integerPart.setValue(preferences.getIntegerPreferences().getDefaultForIntegerRefset());
                    integerPart.setVersion(Integer.MAX_VALUE);
                    extension.addVersion(integerPart);
                }
                break;
            case MEASUREMENT:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartMeasurement measurementPart = new ThinExtByRefPartMeasurement();
                    measurementPart.setPathId(editPath.getConceptId());
                    measurementPart.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    measurementPart.setUnitsOfMeasureId(preferences.getMeasurementPreferences()
                        .getDefaultUnitsOfMeasureForMeasurementRefset()
                        .getConceptId());
                    measurementPart.setMeasurementValue(preferences.getMeasurementPreferences()
                        .getDefaultMeasurementValueForMeasurementRefset());
                    measurementPart.setVersion(Integer.MAX_VALUE);
                    extension.addVersion(measurementPart);
                }
                break;
            case LANGUAGE:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartLanguage laguagePart = new ThinExtByRefPartLanguage();
                    laguagePart.setPathId(editPath.getConceptId());
                    laguagePart.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    laguagePart.setAcceptabilityId(preferences.getLanguagePreferences()
                        .getDefaultAcceptabilityForLanguageRefset()
                        .getConceptId());
                    laguagePart.setCorrectnessId(preferences.getLanguagePreferences()
                        .getDefaultCorrectnessForLanguageRefset()
                        .getConceptId());
                    laguagePart.setDegreeOfSynonymyId(preferences.getLanguagePreferences()
                        .getDefaultDegreeOfSynonymyForLanguageRefset()
                        .getConceptId());
                    laguagePart.setVersion(Integer.MAX_VALUE);
                    extension.addVersion(laguagePart);
                }
                break;
            case SCOPED_LANGUAGE:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartLanguageScoped scopedLaguagePart = new ThinExtByRefPartLanguageScoped();
                    scopedLaguagePart.setPathId(editPath.getConceptId());
                    scopedLaguagePart.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    scopedLaguagePart.setAcceptabilityId(preferences.getLanguageScopedPreferences()
                        .getDefaultAcceptabilityForLanguageRefset()
                        .getConceptId());
                    scopedLaguagePart.setCorrectnessId(preferences.getLanguageScopedPreferences()
                        .getDefaultCorrectnessForLanguageRefset()
                        .getConceptId());
                    scopedLaguagePart.setDegreeOfSynonymyId(preferences.getLanguageScopedPreferences()
                        .getDefaultDegreeOfSynonymyForLanguageRefset()
                        .getConceptId());
                    scopedLaguagePart.setScopeId(preferences.getLanguageScopedPreferences()
                        .getDefaultScopeForScopedLanguageRefset()
                        .getConceptId());
                    scopedLaguagePart.setTagId(preferences.getLanguageScopedPreferences()
                        .getDefaultTagForScopedLanguageRefset()
                        .getConceptId());
                    scopedLaguagePart.setPriority(preferences.getLanguageScopedPreferences()
                        .getDefaultPriorityForScopedLanguageRefset());
                    scopedLaguagePart.setVersion(Integer.MAX_VALUE);
                    extension.addVersion(scopedLaguagePart);
                }
                break;
            case CROSS_MAP:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartCrossmap part = new ThinExtByRefPartCrossmap();
                    part.setPathId(editPath.getConceptId());
                    part.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    part.setVersion(Integer.MAX_VALUE);
                    part.setAdditionalCodeId(preferences.getCrossMapPreferences().getAdditionalCode().getConceptId());
                    part.setBlockNo(preferences.getCrossMapPreferences().getDefaultBlockNo());
                    part.setElementNo(preferences.getCrossMapPreferences().getDefaultElementNo());
                    part.setRefineFlagId(preferences.getCrossMapPreferences().getRefineFlag().getConceptId());
                    if (host.getConfig().getHierarchySelection() != null) {
                        part.setTargetCodeId(host.getConfig().getHierarchySelection().getConceptId());
                    } else {
                        part.setTargetCodeId(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.localize()
                            .getNid());
                    }
                    part.setMapStatusId(preferences.getCrossMapPreferences().getMapStatus().getConceptId());
                    extension.addVersion(part);
                }
                break;
            case CROSS_MAP_FOR_REL:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartCrossmapForRel part = new ThinExtByRefPartCrossmapForRel();
                    part.setPathId(editPath.getConceptId());
                    part.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    part.setVersion(Integer.MAX_VALUE);
                    part.setAdditionalCodeId(preferences.getCrossMapForRelPreferences()
                        .getAdditionalCode()
                        .getConceptId());
                    part.setBlockNo(preferences.getCrossMapForRelPreferences().getDefaultBlockNo());
                    part.setElementNo(preferences.getCrossMapForRelPreferences().getDefaultElementNo());
                    part.setRefineFlagId(preferences.getCrossMapForRelPreferences().getRefineFlag().getConceptId());
                    extension.addVersion(part);
                }
                break;
            case TEMPLATE:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartTemplate part = new ThinExtByRefPartTemplate();
                    part.setPathId(editPath.getConceptId());
                    part.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    part.setVersion(Integer.MAX_VALUE);
                    part.setAttributeDisplayStatusId(preferences.getTemplatePreferences()
                        .getAttributeDisplayStatus()
                        .getConceptId());
                    part.setBrowseAttributeOrder(preferences.getTemplatePreferences().getBrowseAttributeOrder());
                    part.setBrowseValueOrder(preferences.getTemplatePreferences().getBrowseValueOrder());
                    part.setCardinality(preferences.getTemplatePreferences().getCardinality());
                    part.setCharacteristicStatusId(preferences.getTemplatePreferences()
                        .getCharacteristicStatus()
                        .getConceptId());
                    part.setNotesScreenOrder(preferences.getTemplatePreferences().getNotesScreenOrder());
                    part.setSemanticStatusId(preferences.getTemplatePreferences().getSemanticStatus().getConceptId());
                    part.setValueTypeId(preferences.getTemplatePreferences().getValueType().getConceptId());
                    if (host.getConfig().getHierarchySelection() != null) {
                        part.setTargetCodeId(host.getConfig().getHierarchySelection().getConceptId());
                    } else {
                        part.setTargetCodeId(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.localize()
                            .getNid());
                    }
                    part.setAttributeId(preferences.getTemplatePreferences().getAttribute().getConceptId());
                    extension.addVersion(part);
                }
                break;
            case TEMPLATE_FOR_REL:
                for (I_Path editPath : host.getConfig().getEditingPathSet()) {
                    ThinExtByRefPartTemplateForRel part = new ThinExtByRefPartTemplateForRel();
                    part.setPathId(editPath.getConceptId());
                    part.setStatusId(refsetDefaults.getDefaultStatusForRefset().getConceptId());
                    part.setVersion(Integer.MAX_VALUE);
                    part.setAttributeDisplayStatusId(preferences.getTemplateForRelPreferences()
                        .getAttributeDisplayStatus()
                        .getConceptId());
                    part.setBrowseAttributeOrder(preferences.getTemplateForRelPreferences().getBrowseAttributeOrder());
                    part.setBrowseValueOrder(preferences.getTemplateForRelPreferences().getBrowseValueOrder());
                    part.setCardinality(preferences.getTemplateForRelPreferences().getCardinality());
                    part.setCharacteristicStatusId(preferences.getTemplateForRelPreferences()
                        .getCharacteristicStatus()
                        .getConceptId());
                    part.setNotesScreenOrder(preferences.getTemplateForRelPreferences().getNotesScreenOrder());
                    part.setSemanticStatusId(preferences.getTemplateForRelPreferences()
                        .getSemanticStatus()
                        .getConceptId());
                    part.setValueTypeId(preferences.getTemplateForRelPreferences().getValueType().getConceptId());
                    extension.addVersion(part);
                }
                break;
            default:
                throw new UnsupportedOperationException("Can't handle ref set type: " + refsetType);
            }
            ExtensionByReferenceBean ebrBean = ExtensionByReferenceBean.makeNew(extension.getMemberId(), extension);
            ACE.addUncommitted(ebrBean);
            propertyChange(null);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (RuntimeException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    public boolean isCellEditable(int row, int col) {
        if (ACE.editMode == false) {
            return false;
        }
        if (allTuples.get(row).getVersion() == Integer.MAX_VALUE) {
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
        I_ThinExtByRefTuple extTuple = allTuples.get(row);
        boolean changed = false;
        if (extTuple.getVersion() == Integer.MAX_VALUE) {
            switch (columns[col]) {
            case REFSET_ID:
                Integer refsetId = (Integer) value;
                if (refsetId != extTuple.getCore().getRefsetId()) {
                    extTuple.getCore().setRefsetId(refsetId);
                    referencedConcepts.put(refsetId, ConceptBean.get(refsetId));
                    changed = true;
                }
                break;
            case MEMBER_ID:
                break;
            case COMPONENT_ID:
                break;
            case STATUS:
                Integer statusId = (Integer) value;
                if (statusId != extTuple.getStatusId()) {
                    extTuple.setStatusId(statusId);
                    referencedConcepts.put(statusId, ConceptBean.get(statusId));
                    changed = true;
                }
                break;
            case VERSION:
                break;
            case PATH:
                break;
            case BOOLEAN_VALUE:
                Boolean booleanValue = (Boolean) value;
                if (booleanValue != ((I_ThinExtByRefPartBoolean) extTuple.getPart()).getValue()) {
                    ((I_ThinExtByRefPartBoolean) extTuple.getPart()).setValue(booleanValue);
                    changed = true;
                }
                break;
            case STRING_VALUE:
                String stringValue = (String) value;
                if (stringValue.equals(((I_ThinExtByRefPartString) extTuple.getPart()).getStringValue()) == false) {
                    ((I_ThinExtByRefPartString) extTuple.getPart()).setStringValue(stringValue);
                    changed = true;
                }
                break;
            case CONCEPT_ID:
                Integer conceptId = (Integer) value;
                ((I_ThinExtByRefPartConcept) extTuple.getPart()).setConceptId(conceptId);
                referencedConcepts.put(conceptId, ConceptBean.get(conceptId));
                changed = true;
                break;
            case CONCEPT_2_ID:
                Integer concept2Id = (Integer) value;
                ((I_ThinExtByRefPartConceptConcept) extTuple.getPart()).setC2id(concept2Id);
                referencedConcepts.put(concept2Id, ConceptBean.get(concept2Id));
                changed = true;
                break;
            case INTEGER_VALUE:
                Integer intValue = (Integer) value;
                if (I_ThinExtByRefPartConceptInt.class.isAssignableFrom(extTuple.getPart().getClass())) {
                    ((I_ThinExtByRefPartConceptInt) extTuple.getPart()).setIntValue(intValue);
                } else {
                    ((I_ThinExtByRefPartInteger) extTuple.getPart()).setValue(intValue);
                }
                changed = true;
                break;
            case ACCEPTABILITY:
                Integer acceptabilityId = (Integer) value;
                // TODO finish the conditional tests...
                ((I_ThinExtByRefPartLanguage) extTuple.getPart()).setAcceptabilityId(acceptabilityId);
                referencedConcepts.put(acceptabilityId, ConceptBean.get(acceptabilityId));
                changed = true;
                break;
            case CORRECTNESS:
                Integer correctnessId = (Integer) value;
                ((I_ThinExtByRefPartLanguage) extTuple.getPart()).setCorrectnessId(correctnessId);
                referencedConcepts.put(correctnessId, ConceptBean.get(correctnessId));
                changed = true;
                break;
            case DEGREE_OF_SYNONYMY:
                Integer dosId = (Integer) value;
                ((I_ThinExtByRefPartLanguage) extTuple.getPart()).setDegreeOfSynonymyId(dosId);
                referencedConcepts.put(dosId, ConceptBean.get(dosId));
                changed = true;
                break;
            case TAG:
                Integer tagId = (Integer) value;
                ((I_ThinExtByRefPartLanguageScoped) extTuple.getPart()).setTagId(tagId);
                referencedConcepts.put(tagId, ConceptBean.get(tagId));
                changed = true;
                break;
            case SCOPE:
                Integer scopeId = (Integer) value;
                ((I_ThinExtByRefPartLanguageScoped) extTuple.getPart()).setScopeId(scopeId);
                referencedConcepts.put(scopeId, ConceptBean.get(scopeId));
                changed = true;
                break;
            case PRIORITY:
                Integer priority = (Integer) value;
                ((I_ThinExtByRefPartLanguageScoped) extTuple.getPart()).setPriority(priority);
                changed = true;
                break;
            case MEASUREMENT_UNITS_ID:
                Integer unitsId = (Integer) value;
                ((I_ThinExtByRefPartMeasurement) extTuple.getPart()).setUnitsOfMeasureId(unitsId);
                referencedConcepts.put(unitsId, ConceptBean.get(unitsId));
                changed = true;
                break;
            case MEASUREMENT_VALUE:
                Double measurementValue = (Double) value;
                ((I_ThinExtByRefPartMeasurement) extTuple.getPart()).setMeasurementValue(measurementValue);
                changed = true;
                break;
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
        case ACCEPTABILITY:
            return Number.class;
        case CORRECTNESS:
            return Number.class;
        case DEGREE_OF_SYNONYMY:
            return Number.class;
        case TAG:
            return Number.class;
        case SCOPE:
            return Number.class;
        case PRIORITY:
            return Integer.class;
        case MEASUREMENT_UNITS_ID:
            return Number.class;
        case MEASUREMENT_VALUE:
            return Double.class;
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
