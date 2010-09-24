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
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;

public class DescPopupListener extends MouseAdapter {

    enum FieldToChange {
        TYPE,
        STATUS
    };

    private DescriptionsForConceptTableModel model;

    private class ChangeActionListener implements ActionListener {

        public ChangeActionListener() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
            I_GetConceptData sourceBean;
			try {
				sourceBean = Terms.get().getConcept(selectedObject.getTuple().getConceptNid());
			} catch (TerminologyException e1) {
				throw new RuntimeException(e1);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
            for (PathBI p : config.getEditingPathSet()) {
                I_DescriptionPart current = (I_DescriptionPart) selectedObject.getTuple().getMutablePart();
                I_DescriptionPart newPart =
                        (I_DescriptionPart) current.makeAnalog(current.getStatusId(), p.getConceptNid(), Long.MAX_VALUE);
                selectedObject.getTuple().getDescVersioned().addVersion(newPart);
            }
            Terms.get().addUncommitted(sourceBean);
            model.allTuples = null;
            model.fireTableDataChanged();
        }
    }

    private class UndoActionListener implements ActionListener {

        public UndoActionListener() {
            super();
        }

        public void actionPerformed(ActionEvent e) {
			try {
			    I_GetConceptData sourceBean = Terms.get().getConcept(selectedObject.getTuple().getConceptNid());
	            I_DescriptionTuple tuple = selectedObject.getTuple();
	            I_DescriptionVersioned versioned = tuple.getDescVersioned();
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
        private int nid = Integer.MIN_VALUE;
        
        private FieldToChange field;

        public ChangeFieldActionListener(Collection<UUID> ids, FieldToChange field) throws TerminologyException,
                IOException {
            super();
            this.nid = AceConfig.getVodb().uuidToNative(ids);
            this.field = field;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                I_GetConceptData concept = Terms.get().getConcept(selectedObject.getTuple().getConceptNid());
                for (PathBI p : config.getEditingPathSet()) {
                    I_DescriptionPart newPart = selectedObject.getTuple().getMutablePart();
                    if (newPart.getTime() != Long.MAX_VALUE) {
                        I_DescriptionPart currentPart = (I_DescriptionPart) 
                    			selectedObject.getTuple().getMutablePart();
                        newPart = (I_DescriptionPart) 
                    		currentPart.makeAnalog(nid, p.getConceptNid(), Long.MAX_VALUE);
                        selectedObject.getTuple().getDescVersioned().addVersion(newPart);
                	}
                    switch (field) {
                    case STATUS:
                		newPart.setStatusId(nid);
                        break;
                    case TYPE:
                        newPart.setTypeId(nid);
                        newPart.setStatusId(config.getDefaultStatus().getConceptNid());
                        break;
                    default:
                    }

                    model.referencedConcepts.put(newPart.getStatusId(), Terms.get().getConcept(newPart.getStatusId()));
                    model.referencedConcepts.put(newPart.getTypeId(), Terms.get().getConcept(newPart.getTypeId()));
                }
                Terms.get().addUncommitted(concept);
                model.allTuples = null;
                model.fireTableDataChanged();
                model.propertyChange(new PropertyChangeEvent(this, I_ContainTermComponent.TERM_COMPONENT, null,
                		model.host.getTermComponent()));
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    JPopupMenu popup;

    JTable table;

    ActionListener change;

    StringWithDescTuple selectedObject;

    I_ConfigAceFrame config;

    public DescPopupListener(JTable table, I_ConfigAceFrame config, DescriptionsForConceptTableModel model) {
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
                selectedObject = (StringWithDescTuple) table.getValueAt(row, column);
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
                addSubmenuItems(changeType, FieldToChange.TYPE, model.host.getConfig().getEditDescTypePopup());
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
                    selectedObject = (StringWithDescTuple) table.getValueAt(row, column);
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
