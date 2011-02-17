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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguageScoped;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.I_RefsetDefaults;
import org.dwfa.ace.table.refset.RefsetMemberTableModel.REFSET_FIELDS;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.ThinExtByRefVersioned;

public class RefsetPopupListener extends MouseAdapter {

    private RefsetMemberTableModel model;

    private class ChangeActionListener implements ActionListener {

        public ChangeActionListener() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (selectedObject.getTuple().getPart().getVersion() != Integer.MAX_VALUE) {
                    for (I_Path p : config.getEditingPathSet()) {
                        I_ThinExtByRefPart newPart = selectedObject.getTuple().getPart().duplicate();
                        newPart.setPathId(p.getConceptId());
                        newPart.setVersion(Integer.MAX_VALUE);
                        setProperStatus(newPart);
                        model.referencedConcepts.put(newPart.getStatusId(), ConceptBean.get(newPart.getStatusId()));
                        selectedObject.getTuple().addVersion(newPart);
                    }
                    ACE.addUncommitted(ExtensionByReferenceBean.get(selectedObject.getTuple().getMemberId()));
                    model.allTuples = null;
                    model.fireTableDataChanged();
                    model.propertyChange(null);
                }
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private class UndoActionListener implements ActionListener {

        public UndoActionListener() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            ExtensionByReferenceBean sourceBean = ExtensionByReferenceBean.get(selectedObject.getTuple().getMemberId());
            I_ThinExtByRefTuple tuple = selectedObject.getTuple();
            ThinExtByRefVersioned versioned = (ThinExtByRefVersioned) tuple.getCore();
            versioned.getVersions().remove(tuple.getPart());
            if (versioned.getVersions().size() == 0) {

            }
            ACE.addUncommitted(sourceBean);
            model.allTuples = null;
            model.fireTableDataChanged();
        }
    }

    private class ChangeFieldActionListener implements ActionListener {
        private Collection<UUID> ids;

        private REFSET_FIELDS field;

        public ChangeFieldActionListener(Collection<UUID> ids, REFSET_FIELDS field) {
            super();
            this.ids = ids;
            this.field = field;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                for (I_Path p : config.getEditingPathSet()) {
                    I_ThinExtByRefPart newPart;
                    if (selectedObject.getTuple().getPart().getVersion() != Integer.MAX_VALUE) {
                        newPart = selectedObject.getTuple().getPart().duplicate();
                    } else {
                        newPart = selectedObject.getTuple().getPart();
                    }

                    newPart.setPathId(p.getConceptId());
                    newPart.setVersion(Integer.MAX_VALUE);
                    setProperStatus(newPart);
                    switch (field) {
                    case STATUS:
                        newPart.setStatusId((AceConfig.getVodb().uuidToNative(ids)));
                        model.referencedConcepts.put(newPart.getStatusId(), ConceptBean.get(newPart.getStatusId()));
                        break;
                    case CONCEPT_ID:
                        ((I_ThinExtByRefPartConcept) newPart).setConceptId((AceConfig.getVodb().uuidToNative(ids)));
                        break;
                    case ACCEPTABILITY:
                        ((I_ThinExtByRefPartLanguage) newPart).setAcceptabilityId((AceConfig.getVodb().uuidToNative(ids)));
                        break;
                    case CORRECTNESS:
                        ((I_ThinExtByRefPartLanguage) newPart).setCorrectnessId((AceConfig.getVodb().uuidToNative(ids)));
                        break;
                    case DEGREE_OF_SYNONYMY:
                        ((I_ThinExtByRefPartLanguage) newPart).setDegreeOfSynonymyId((AceConfig.getVodb().uuidToNative(ids)));
                        break;
                    case TAG:
                        ((I_ThinExtByRefPartLanguageScoped) newPart).setTagId((AceConfig.getVodb().uuidToNative(ids)));
                        break;
                    case SCOPE:
                        ((I_ThinExtByRefPartLanguageScoped) newPart).setScopeId((AceConfig.getVodb().uuidToNative(ids)));
                        break;
                    case MEASUREMENT_UNITS_ID:
                        ((I_ThinExtByRefPartMeasurement) newPart).setUnitsOfMeasureId((AceConfig.getVodb().uuidToNative(ids)));
                        break;

                    case INTEGER_VALUE:
                    case PRIORITY:
                    case BOOLEAN_VALUE:
                    case MEASUREMENT_VALUE:
                    case STRING_VALUE:
                    default:
                        throw new Exception("Don't know how to handle: " + field);
                    }

                    model.referencedConcepts.put(AceConfig.getVodb().uuidToNative(ids),
                        ConceptBean.get((AceConfig.getVodb().uuidToNative(ids))));
                    model.referencedConcepts.put(newPart.getStatusId(), ConceptBean.get(newPart.getStatusId()));
                    if (selectedObject.getTuple().getPart().getVersion() != Integer.MAX_VALUE) {
                        selectedObject.getTuple().addVersion(newPart);
                    }
                }
                ACE.addUncommitted(ExtensionByReferenceBean.get(selectedObject.getTuple().getMemberId()));
                model.allTuples = null;
                model.fireTableDataChanged();
                model.propertyChange(null);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

    }

    private class ChangeFieldIntegerActionListener implements ActionListener {
        private Integer value;

        private REFSET_FIELDS field;

        public ChangeFieldIntegerActionListener(Integer value, REFSET_FIELDS field) {
            super();
            this.value = value;
            this.field = field;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                for (I_Path p : config.getEditingPathSet()) {
                    I_ThinExtByRefPart newPart;
                    if (selectedObject.getTuple().getPart().getVersion() != Integer.MAX_VALUE) {
                        newPart = selectedObject.getTuple().getPart().duplicate();
                    } else {
                        newPart = selectedObject.getTuple().getPart();
                    }

                    newPart.setPathId(p.getConceptId());
                    newPart.setVersion(Integer.MAX_VALUE);
                    setProperStatus(newPart);
                    switch (field) {
                    case INTEGER_VALUE:
                        ((I_ThinExtByRefPartInteger) newPart).setValue(value);
                        break;
                    case PRIORITY:
                        ((I_ThinExtByRefPartLanguageScoped) newPart).setPriority(value);
                        break;
                    case CONCEPT_ID:
                    case ACCEPTABILITY:
                    case CORRECTNESS:
                    case DEGREE_OF_SYNONYMY:
                    case TAG:
                    case SCOPE:
                    case STATUS:
                    case BOOLEAN_VALUE:
                    case MEASUREMENT_VALUE:
                    case MEASUREMENT_UNITS_ID:
                    case STRING_VALUE:
                    default:
                        throw new Exception("Don't know how to handle: " + field);
                    }
                    model.referencedConcepts.put(newPart.getStatusId(), ConceptBean.get(newPart.getStatusId()));
                    if (selectedObject.getTuple().getPart().getVersion() != Integer.MAX_VALUE) {
                        selectedObject.getTuple().addVersion(newPart);
                    }
                }
                ACE.addUncommitted(ExtensionByReferenceBean.get(selectedObject.getTuple().getMemberId()));
                model.allTuples = null;
                model.fireTableDataChanged();
                model.propertyChange(null);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

    }

    private class ChangeFieldDoubleActionListener implements ActionListener {
        private Double value;

        private REFSET_FIELDS field;

        public ChangeFieldDoubleActionListener(Double value, REFSET_FIELDS field) {
            super();
            this.value = value;
            this.field = field;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                for (I_Path p : config.getEditingPathSet()) {
                    I_ThinExtByRefPart newPart;
                    if (selectedObject.getTuple().getPart().getVersion() != Integer.MAX_VALUE) {
                        newPart = selectedObject.getTuple().getPart().duplicate();
                    } else {
                        newPart = selectedObject.getTuple().getPart();
                    }

                    newPart.setPathId(p.getConceptId());
                    newPart.setVersion(Integer.MAX_VALUE);
                    setProperStatus(newPart);
                    switch (field) {
                    case MEASUREMENT_VALUE:
                        ((I_ThinExtByRefPartMeasurement) newPart).setMeasurementValue(value);
                        break;

                    case PRIORITY:
                    case CONCEPT_ID:
                    case ACCEPTABILITY:
                    case CORRECTNESS:
                    case DEGREE_OF_SYNONYMY:
                    case TAG:
                    case SCOPE:
                    case STATUS:
                    case BOOLEAN_VALUE:
                    case INTEGER_VALUE:
                    case MEASUREMENT_UNITS_ID:
                    case STRING_VALUE:
                    default:
                        throw new Exception("Don't know how to handle: " + field);
                    }
                    model.referencedConcepts.put(newPart.getStatusId(), ConceptBean.get(newPart.getStatusId()));

                    if (selectedObject.getTuple().getPart().getVersion() != Integer.MAX_VALUE) {
                        selectedObject.getTuple().addVersion(newPart);
                    }
                }
                ACE.addUncommitted(ExtensionByReferenceBean.get(selectedObject.getTuple().getMemberId()));
                model.allTuples = null;
                model.fireTableDataChanged();
                model.propertyChange(null);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

    }

    private class ChangeFieldBooleanActionListener implements ActionListener {
        private Boolean value;

        private REFSET_FIELDS field;

        public ChangeFieldBooleanActionListener(Boolean value, REFSET_FIELDS field) {
            super();
            this.value = value;
            this.field = field;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                for (I_Path p : config.getEditingPathSet()) {
                    I_ThinExtByRefPart newPart;
                    if (selectedObject.getTuple().getPart().getVersion() != Integer.MAX_VALUE) {
                        newPart = selectedObject.getTuple().getPart().duplicate();
                    } else {
                        newPart = selectedObject.getTuple().getPart();
                    }

                    newPart.setPathId(p.getConceptId());
                    newPart.setVersion(Integer.MAX_VALUE);
                    setProperStatus(newPart);
                    switch (field) {
                    case BOOLEAN_VALUE:
                        ((I_ThinExtByRefPartBoolean) newPart).setValue(value);
                        break;

                    case PRIORITY:
                    case CONCEPT_ID:
                    case ACCEPTABILITY:
                    case CORRECTNESS:
                    case DEGREE_OF_SYNONYMY:
                    case TAG:
                    case SCOPE:
                    case STATUS:
                    case MEASUREMENT_VALUE:
                    case INTEGER_VALUE:
                    case MEASUREMENT_UNITS_ID:
                    case STRING_VALUE:
                    default:
                        throw new Exception("Don't know how to handle: " + field);
                    }
                    model.referencedConcepts.put(newPart.getStatusId(), ConceptBean.get(newPart.getStatusId()));

                    if (selectedObject.getTuple().getPart().getVersion() != Integer.MAX_VALUE) {
                        selectedObject.getTuple().addVersion(newPart);
                    }
                }
                ACE.addUncommitted(ExtensionByReferenceBean.get(selectedObject.getTuple().getMemberId()));
                model.allTuples = null;
                model.fireTableDataChanged();
                model.propertyChange(null);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

    }

    private JPopupMenu popup;

    private JTable table;

    private ActionListener change;

    private StringWithExtTuple selectedObject;

    private I_ConfigAceFrame config;

    private I_RefsetDefaults preferences;

    public RefsetPopupListener(JTable table, I_ConfigAceFrame config, I_RefsetDefaults defaults,
            RefsetMemberTableModel model) {
        super();
        this.table = table;
        this.config = config;
        this.model = model;
        this.preferences = defaults;

        change = new ChangeActionListener();
    }

    private void makePopup(MouseEvent e) {
        try {
            popup = null;
            int column = table.columnAtPoint(e.getPoint());
            int row = table.rowAtPoint(e.getPoint());
            if ((row != -1) && (column != -1)) {
                popup = new JPopupMenu();
                JMenuItem noActionItem = new JMenuItem("");
                popup.add(noActionItem);
                selectedObject = (StringWithExtTuple) table.getValueAt(row, column);
                if (selectedObject.getTuple().getVersion() == Integer.MAX_VALUE) {
                    JMenuItem undoActonItem = new JMenuItem("Undo");
                    undoActonItem.addActionListener(new UndoActionListener());
                    popup.add(undoActonItem);
                }
                JMenuItem changeItem = new JMenuItem("Change");
                popup.add(changeItem);
                changeItem.addActionListener(change);
                for (REFSET_FIELDS field : model.getPopupFields()) {
                    if (REFSET_FIELDS.REFSET_ID.equals(field)) {
                        // Not allowed to change the refset.
                    } else {
                        JMenu changeMenu = new JMenu("Change " + field.getColumnName());

                        switch (field) {
                        case REFSET_ID:
                            popup.add(changeMenu);
                            addSubmenuItems(changeMenu, field, preferences.getRefsetPopupIds());
                            break;
                        case STATUS:
                            popup.add(changeMenu);
                            addSubmenuItems(changeMenu, field, preferences.getStatusPopupIds());
                            break;
                        case CONCEPT_ID:
                            popup.add(changeMenu);
                            addSubmenuItems(changeMenu, field,
                                ((RefsetDefaultsConcept) preferences).getConceptPopupIds());
                            break;
                        case ACCEPTABILITY:
                            popup.add(changeMenu);
                            addSubmenuItems(changeMenu, field,
                                ((RefsetDefaultsLanguage) preferences).getAcceptabilityPopupIds());
                            break;
                        case CORRECTNESS:
                            popup.add(changeMenu);
                            addSubmenuItems(changeMenu, field,
                                ((RefsetDefaultsLanguage) preferences).getCorrectnessPopupIds());
                            break;
                        case DEGREE_OF_SYNONYMY:
                            popup.add(changeMenu);
                            addSubmenuItems(changeMenu, field,
                                ((RefsetDefaultsLanguage) preferences).getDegreeOfSynonymyPopupIds());
                            break;
                        case TAG:
                            popup.add(changeMenu);
                            addSubmenuItems(changeMenu, field,
                                ((RefsetDefaultsLanguageScoped) preferences).getTagPopupIds());
                            break;
                        case SCOPE:
                            popup.add(changeMenu);
                            addSubmenuItems(changeMenu, field,
                                ((RefsetDefaultsLanguageScoped) preferences).getScopePopupIds());
                            break;
                        case MEASUREMENT_UNITS_ID:
                            popup.add(changeMenu);
                            addSubmenuItems(changeMenu, field,
                                ((RefsetDefaultsMeasurement) preferences).getUnitsOfMeasurePopupIds());
                            break;
                        case INTEGER_VALUE:
                            popup.add(changeMenu);
                            addSubmenuObjects(changeMenu, field,
                                ((RefsetDefaultsInteger) preferences).getIntegerPopupItems());
                            break;
                        case PRIORITY:
                            popup.add(changeMenu);
                            addSubmenuObjects(changeMenu, field,
                                ((RefsetDefaultsLanguageScoped) preferences).getPriorityPopupItems());
                            break;
                        case BOOLEAN_VALUE:
                            popup.add(changeMenu);
                            addSubmenuObjects(changeMenu, field,
                                ((RefsetDefaultsBoolean) preferences).getBooleanPopupItems());
                            break;
                        case STRING_VALUE:
                            break;
                        case MEASUREMENT_VALUE:
                            popup.add(changeMenu);
                            addSubmenuObjects(changeMenu, field,
                                ((RefsetDefaultsMeasurement) preferences).getMeasurementValuePopupItems());
                            break;
                        default:
                            popup.add(changeMenu);
                        }

                    }

                }
            }
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    private void setProperStatus(I_ThinExtByRefPart newPart) throws Exception {
        newPart.setStatusId(preferences.getDefaultStatusForRefset().getConceptId());
    }

    private void addSubmenuItems(JMenu menu, REFSET_FIELDS field, I_IntList possibleValues)
            throws TerminologyException, IOException {
        for (int id : possibleValues.getListValues()) {
            I_GetConceptData possibleValue = LocalVersionedTerminology.get().getConcept(id);
            JMenuItem changeStatusItem = new JMenuItem(possibleValue.toString());
            changeStatusItem.addActionListener(new ChangeFieldActionListener(possibleValue.getUids(), field));
            menu.add(changeStatusItem);
        }
    }

    private void addSubmenuObjects(JMenu menu, REFSET_FIELDS field, Object[] values) {
        for (Object value : values) {
            JMenuItem menuItem = new JMenuItem(value.toString());
            if (Integer.class.equals(value.getClass())) {
                menuItem.addActionListener(new ChangeFieldIntegerActionListener((Integer) value, field));
                menu.add(menuItem);
            } else if (Double.class.equals(value.getClass())) {
                menuItem.addActionListener(new ChangeFieldDoubleActionListener((Double) value, field));
                menu.add(menuItem);
            } else if (Boolean.class.equals(value.getClass())) {
                menuItem.addActionListener(new ChangeFieldBooleanActionListener((Boolean) value, field));
                menu.add(menuItem);
            }
        }
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
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                selectedObject = (StringWithExtTuple) table.getValueAt(row, column);
                if (selectedObject != null) {
                    makePopup(e);
                    if (popup != null) {
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }

                }
            } else {
                JOptionPane.showMessageDialog(table.getTopLevelAncestor(),
                    "You must select at least one path to edit on...");
            }
            e.consume();
        }
    }
}
