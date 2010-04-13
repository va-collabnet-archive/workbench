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
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.RelTableModel.FieldToChange;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.tapi.TerminologyException;

public class RelPopupListener extends MouseAdapter {

    private RelTableModel model;

    private class ChangeActionListener implements ActionListener {

        public ChangeActionListener() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            try {
                I_GetConceptData sourceBean = Terms.get().getConcept(selectedObject.getTuple().getC1Id());
                I_GetConceptData destBean = Terms.get().getConcept(selectedObject.getTuple().getC2Id());
                for (I_Path p : config.getEditingPathSet()) {
                    I_RelPart currentPart = (I_RelPart) selectedObject.getTuple().getMutablePart();
                    I_RelPart newPart =
                            (I_RelPart) currentPart.makeAnalog(currentPart.getStatusId(), p.getConceptId(),
                                Long.MAX_VALUE);
                    selectedObject.getTuple().getRelVersioned().addVersion(newPart);

                    I_RelVersioned srcRel = sourceBean.getSourceRel(selectedObject.getTuple().getRelId());
                    I_RelVersioned destRel = destBean.getDestRel(selectedObject.getTuple().getRelId());
                    if ((srcRel != null) && (destRel != null)) {
                        srcRel.addVersion(newPart);
                        destRel.addVersion(newPart);
                    } else {
                        AceLog.getAppLog().alertAndLogException(
                            new Exception("srcRel: " + srcRel + " destRel: " + destRel + " cannot be null"));
                    }
                }
                Terms.get().addUncommitted(sourceBean);
                Terms.get().addUncommitted(destBean);
                model.allTuples = null;
                model.fireTableDataChanged();
            } catch (IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (TerminologyException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
			}
        }
    }

    private class UndoActionListener implements ActionListener {

        public UndoActionListener() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
			try {
			    I_GetConceptData sourceBean = Terms.get().getConcept(selectedObject.getTuple().getC1Id());
	            I_RelTuple tuple = selectedObject.getTuple();
	            I_RelVersioned versioned = (I_RelVersioned) tuple.getRelVersioned();
	            Terms.get().forget(versioned);
	            Terms.get().addUncommitted(sourceBean);
	            model.allTuples = null;
	            model.fireTableDataChanged();
			} catch (TerminologyException e1) {
				throw new RuntimeException(e1);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
        }
    }

    private class ChangeFieldActionListener implements ActionListener {
        private Collection<UUID> ids;

        private FieldToChange field;

        public ChangeFieldActionListener(Collection<UUID> ids, FieldToChange field) {
            super();
            this.ids = ids;
            this.field = field;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                I_GetConceptData sourceBean = Terms.get().getConcept(selectedObject.getTuple().getC1Id());
                I_GetConceptData destBean = Terms.get().getConcept(selectedObject.getTuple().getC2Id());
                I_RelVersioned srcRel = sourceBean.getSourceRel(selectedObject.getTuple().getRelId());
                for (I_Path p : config.getEditingPathSet()) {
                    I_RelPart newPart = selectedObject.getTuple().getMutablePart();
                    if (newPart.getTime() != Long.MAX_VALUE) {
                        I_RelPart currentPart = (I_RelPart) selectedObject.getTuple().getMutablePart();
                        newPart =
                                (I_RelPart) currentPart.makeAnalog(currentPart.getStatusId(), currentPart.getPathId(),
                                    Long.MAX_VALUE);
                        srcRel.addVersion(newPart);
                    } else {
                        newPart.setPathId(p.getConceptId());
                    }
                    switch (field) {
                    case STATUS:
                        newPart.setStatusId((AceConfig.getVodb().uuidToNative(ids)));
                        break;
                    case CHARACTERISTIC:
                        newPart.setCharacteristicId((AceConfig.getVodb().uuidToNative(ids)));
                        newPart.setStatusId(config.getDefaultStatus().getConceptId());
                        break;
                    case REFINABILITY:
                        newPart.setRefinabilityId((AceConfig.getVodb().uuidToNative(ids)));
                        newPart.setStatusId(config.getDefaultStatus().getConceptId());
                        break;
                    case TYPE:
                        newPart.setTypeId((AceConfig.getVodb().uuidToNative(ids)));
                        newPart.setStatusId(config.getDefaultStatus().getConceptId());
                        break;

                    default:
                    }

                    model.referencedConcepts.put(newPart.getStatusId(), Terms.get().getConcept(newPart.getStatusId()));
                    model.referencedConcepts.put(newPart.getCharacteristicId(), Terms.get().getConcept(
                        newPart.getCharacteristicId()));
                    model.referencedConcepts.put(newPart.getRefinabilityId(), Terms.get().getConcept(
                        newPart.getRefinabilityId()));
                    model.referencedConcepts.put(newPart.getTypeId(), Terms.get().getConcept(newPart.getTypeId()));

                    I_RelVersioned destRel = destBean.getDestRel(selectedObject.getTuple().getRelId());

                    selectedObject.getTuple().getRelVersioned().addVersion(newPart);
                    if (destRel != null && srcRel != destRel) {
                        destRel.addVersion(newPart);
                    }
                }
                Terms.get().addUncommitted(sourceBean);
                Terms.get().addUncommitted(destBean);
                model.allTuples = null;
                model.fireTableDataChanged();
                model.updateTable(model.tableBean);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    JPopupMenu popup;

    JTable table;

    ActionListener change;

    StringWithRelTuple selectedObject;

    I_ConfigAceFrame config;

    public RelPopupListener(JTable table, I_ConfigAceFrame config, RelTableModel model) {
        super();
        this.table = table;
        this.config = config;
        this.model = model;
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
                selectedObject = (StringWithRelTuple) table.getValueAt(row, column);
                if (selectedObject.getTuple().getVersion() == Integer.MAX_VALUE) {
                    JMenuItem undoActonItem = new JMenuItem("Undo");
                    undoActonItem.addActionListener(new UndoActionListener());
                    popup.add(undoActonItem);
                }
                JMenuItem changeItem = new JMenuItem("Change");
                popup.add(changeItem);
                changeItem.addActionListener(change);
                /*
                 * JMenuItem retireItem = new JMenuItem("Retire");
                 * retireItem.addActionListener(new ChangeFieldActionListener(
                 * ArchitectonicAuxiliary.Concept.RETIRED.getUids(),
                 * FieldToChange.STATUS)); popup.add(retireItem);
                 */
                JMenu changeType = new JMenu("Change Type");
                popup.add(changeType);
                addSubmenuItems(changeType, FieldToChange.TYPE, model.host.getConfig().getEditRelTypePopup());
                JMenu changeRefinability = new JMenu("Change Refinability");
                popup.add(changeRefinability);
                addSubmenuItems(changeRefinability, FieldToChange.REFINABILITY, model.host.getConfig()
                    .getEditRelRefinabiltyPopup());
                JMenu changeCharacteristic = new JMenu("Change Characteristic");
                popup.add(changeCharacteristic);
                addSubmenuItems(changeCharacteristic, FieldToChange.CHARACTERISTIC, model.host.getConfig()
                    .getEditRelCharacteristicPopup());
                JMenu changeStatus = new JMenu("Change Status");
                popup.add(changeStatus);
                addSubmenuItems(changeStatus, FieldToChange.STATUS, model.host.getConfig().getEditStatusTypePopup());
            }
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    private void addSubmenuItems(JMenu menu, FieldToChange field, I_IntList possibleValues)
            throws TerminologyException, IOException {
        for (int id : possibleValues.getListValues()) {
            I_GetConceptData possibleValue = Terms.get().getConcept(id);
            JMenuItem changeStatusItem = new JMenuItem(possibleValue.toString());
            changeStatusItem.addActionListener(new ChangeFieldActionListener(possibleValue.getUids(), field));
            menu.add(changeStatusItem);
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
                if (row >= 0) {
                    selectedObject = (StringWithRelTuple) table.getValueAt(row, column);
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
