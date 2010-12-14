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

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.I_RefsetDefaults;
import org.dwfa.ace.table.refset.RefsetMemberTableModel.REFSET_FIELDS;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;

public class RefsetPopupListener extends MouseAdapter {

    private RefsetMemberTableModel model;

    private class ChangeActionListener implements ActionListener {

        public ChangeActionListener() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (selectedObject.getTuple().getMutablePart().getTime() != Long.MAX_VALUE) {
                    for (PathBI p : config.getEditingPathSet()) {
                    	I_ExtendByRefPart currentPart = selectedObject.getTuple();
                        I_ExtendByRefPart newPart = (I_ExtendByRefPart) selectedObject.getTuple().getMutablePart().makeAnalog(currentPart.getStatusNid(),
                        		p.getConceptNid(), Long.MAX_VALUE);
                        setProperStatus(newPart);
                        model.referencedConcepts.put(newPart.getStatusNid(), Terms.get().getConcept(newPart.getStatusNid()));
                        selectedObject.getTuple().addVersion(newPart);
                    }
                    Terms.get().addUncommitted(selectedObject.getTuple().getCore());
                    model.allTuples = null;
                    model.fireTableDataChanged();
                    model.propertyChange(null);
                }
            } catch (Throwable ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private class UndoActionListener implements ActionListener {

        public UndoActionListener() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            I_ExtendByRefVersion tuple = selectedObject.getTuple();
            I_ExtendByRef versioned = (I_ExtendByRef) tuple.getCore();
            versioned.getMutableParts().remove(tuple.getMutablePart());
            if (versioned.getMutableParts().size() == 0) {

            }
            Terms.get().addUncommitted(tuple.getCore());
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
                for (PathBI p : config.getEditingPathSet()) {
                    I_ExtendByRefPart newPart;
                    if (selectedObject.getTuple().getMutablePart().getTime() != Long.MAX_VALUE) {
                    	I_ExtendByRefPart currentPart = selectedObject.getTuple();
                        newPart = (I_ExtendByRefPart) selectedObject.getTuple().getMutablePart().makeAnalog(currentPart.getStatusNid(), 
                        		currentPart.getPathNid(), Long.MAX_VALUE);
                    } else {
                        newPart = selectedObject.getTuple().getMutablePart();
                    }
                    newPart.setPathNid(p.getConceptNid());
                    setProperStatus(newPart);
                    switch (field) {
                    case STATUS:
                        newPart.setStatusNid((Terms.get().uuidToNative(ids)));
                        model.referencedConcepts.put(newPart.getStatusNid(), Terms.get().getConcept(newPart.getStatusNid()));
                        break;
                    case CONCEPT_ID:
                        ((I_ExtendByRefPartCid) newPart).setC1id((Terms.get().uuidToNative(ids)));
                        break;

                    case INTEGER_VALUE:
                    case BOOLEAN_VALUE:
                    case STRING_VALUE:
                    	break;
                    default:
                        throw new Exception("Don't know how to handle: " + field);
                    }

                    model.referencedConcepts.put(Terms.get().uuidToNative(ids),
                        Terms.get().getConcept((Terms.get().uuidToNative(ids))));
                    model.referencedConcepts.put(newPart.getStatusNid(), Terms.get().getConcept(newPart.getStatusNid()));
                    if (selectedObject.getTuple().getMutablePart().getTime() != Long.MAX_VALUE) {
                        selectedObject.getTuple().addVersion(newPart);
                    }
                }
                Terms.get().addUncommitted(selectedObject.getTuple().getCore());
                model.allTuples = null;
                model.fireTableDataChanged();
                model.propertyChange(null);
            } catch (Throwable ex) {
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
                for (PathBI p : config.getEditingPathSet()) {
                    I_ExtendByRefPart newPart;
                    if (selectedObject.getTuple().getMutablePart().getTime() != Long.MAX_VALUE) {
                    	I_ExtendByRefPart currentPart = selectedObject.getTuple();
                        newPart = (I_ExtendByRefPart) selectedObject.getTuple().getMutablePart().makeAnalog(currentPart.getStatusNid(), p.getConceptNid(), Long.MAX_VALUE);
                    } else {
                        newPart = selectedObject.getTuple().getMutablePart();
                    }

                    setProperStatus(newPart);
                    switch (field) {
                    case INTEGER_VALUE:
                        ((I_ExtendByRefPartInt) newPart).setIntValue(value);
                        break;
                    case CONCEPT_ID:
                    case STATUS:
                    case BOOLEAN_VALUE:
                    case STRING_VALUE:
                    default:
                        throw new Exception("Don't know how to handle: " + field);
                    }
                    model.referencedConcepts.put(newPart.getStatusNid(), Terms.get().getConcept(newPart.getStatusNid()));
                    if (selectedObject.getTuple().getMutablePart().getTime() != Long.MAX_VALUE) {
                        selectedObject.getTuple().addVersion(newPart);
                    }
                }
                Terms.get().addUncommitted(selectedObject.getTuple().getCore());
                model.allTuples = null;
                model.fireTableDataChanged();
                model.propertyChange(null);
            } catch (Throwable ex) {
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
                for (PathBI p : config.getEditingPathSet()) {
                    I_ExtendByRefPart newPart;
                    if (selectedObject.getTuple().getMutablePart().getTime() != Long.MAX_VALUE) {
                    	I_ExtendByRefPart currentPart = selectedObject.getTuple();
                        newPart = (I_ExtendByRefPart) selectedObject.getTuple().getMutablePart().makeAnalog(currentPart.getStatusNid(), currentPart.getPathNid(), Long.MAX_VALUE);
                    } else {
                        newPart = selectedObject.getTuple().getMutablePart();
                    }

                    newPart.setPathNid(p.getConceptNid());
                    newPart.setTime(Long.MAX_VALUE);
                    setProperStatus(newPart);
                    switch (field) {
                    case BOOLEAN_VALUE:
                        ((I_ExtendByRefPartBoolean) newPart).setBooleanValue(value);
                        break;

                    case CONCEPT_ID:
                    case STATUS:
                    case INTEGER_VALUE:
                    case STRING_VALUE:
                    default:
                        throw new Exception("Don't know how to handle: " + field);
                    }
                    model.referencedConcepts.put(newPart.getStatusNid(), Terms.get().getConcept(newPart.getStatusNid()));

                    if (selectedObject.getTuple().getMutablePart().getTime() != Long.MAX_VALUE) {
                        selectedObject.getTuple().addVersion(newPart);
                    }
                }
                Terms.get().addUncommitted(selectedObject.getTuple().getCore());
                model.allTuples = null;
                model.fireTableDataChanged();
                model.propertyChange(null);
            } catch (Throwable ex) {
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
                if (selectedObject.getTuple().getTime() == Long.MAX_VALUE) {
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
                        case INTEGER_VALUE:
                            popup.add(changeMenu);
                            addSubmenuObjects(changeMenu, field,
                                ((RefsetDefaultsInteger) preferences).getIntegerPopupItems());
                            break;
                        case BOOLEAN_VALUE:
                            popup.add(changeMenu);
                            addSubmenuObjects(changeMenu, field,
                                ((RefsetDefaultsBoolean) preferences).getBooleanPopupItems());
                            break;
                        case STRING_VALUE:
                            break;
                        default:
                            popup.add(changeMenu);
                        }

                    }

                }
            }
        } catch (Throwable e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    private void setProperStatus(I_ExtendByRefPart newPart) throws Exception {
        newPart.setStatusNid(preferences.getDefaultStatusForRefset().getConceptNid());
    }

    private void addSubmenuItems(JMenu menu, REFSET_FIELDS field, I_IntList possibleValues)
            throws TerminologyException, IOException {
        for (int id : possibleValues.getListValues()) {
            I_GetConceptData possibleValue = Terms.get().getConcept(id);
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
