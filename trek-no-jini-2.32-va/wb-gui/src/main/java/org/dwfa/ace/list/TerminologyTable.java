/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dwfa.ace.list;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.list.TerminologyTableModel.MODEL_FIELD;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class TerminologyTable extends JTable {

    TerminologyTableModel tableModel;
    I_ConfigAceFrame config;
    private boolean confirmDelete = false;

    public TerminologyTable(TerminologyTableModel dataModel, I_ConfigAceFrame config) {
        super(dataModel);
        this.tableModel = dataModel;
        this.config = config;
        init(true);
    }

    public TerminologyList getList() {
        return tableModel.getList();
    }

    private void init(boolean allowDelete) {

        TableColumn column = null;
        for(int i = 0; i < 3; i++){
            column = getColumnModel().getColumn(i);
            if(i == 0){
                column.setPreferredWidth(230);
            }else{
                column.setPreferredWidth(50);
            }
        }
        TableRowSorter<TerminologyTableModel> sorter = 
                new TableRowSorter<TerminologyTableModel>((TerminologyTableModel) getModel());
        sorter.setSortsOnUpdates(false);
        setRowSorter(sorter);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setTransferHandler(new TerminologyTransferHandler(this));
        setDragEnabled(true);
        setShowVerticalLines(false);
        if (allowDelete) {
            TerminologyTable.DeleteAction delete = new TerminologyTable.DeleteAction();
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                    delete.getValue(Action.NAME));
            getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), delete.getValue(Action.NAME));
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                    delete.getValue(Action.NAME));
            getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), delete.getValue(Action.NAME));

            ActionMap map = this.getActionMap();
            map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
            map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
            map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
            map.put(delete.getValue(Action.NAME), delete);
        }
    }

    //to fix known issue in java with jtable not expaning to viewport size
    @Override
    public boolean getScrollableTracksViewportHeight() {
        Container viewport = getParent();
        if (!(viewport instanceof JViewport)) {
            return false;
        }
        return getPreferredSize().height < viewport.getHeight();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return new TerminologyTableRenderer(config);
    }

    private class DeleteAction extends AbstractAction {

        public DeleteAction() {
            super("delete");
        }
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (confirmDelete) {
                int index = getSelectedRow();
                if (index >= 0) {
                    TerminologyTableModel tm = (TerminologyTableModel) getModel();
                    ConceptChronicleBI element = tm.getElementAt(index);
                    if (element.isUncommitted()) {
                        JOptionPane.showMessageDialog(TerminologyTable.this,
                                "<html>Uncommitted items cannot be removed from the list.<br><br>"
                                + "Either cancel or commit changes prior to removal.");
                    } else {
                        int option = JOptionPane.showConfirmDialog(TerminologyTable.this,
                                "<html>Are you sure you want to erase item <font color='red'>" + element
                                + "</font> from the list?", "Erase the list?", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            delete();
                        }
                    }
                }
            } else {
                delete();
            }
        }

        private void delete() {
            int index = getSelectedRow();
            if (index >= 0) {
                TerminologyTableModel tm = (TerminologyTableModel) getModel();
                tm.removeElement(index);
            }
        }
    }
}
