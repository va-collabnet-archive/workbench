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
package org.dwfa.ace.classifier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.TableColumn;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.task.classify.SnoQuery;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.tapi.TerminologyException;

public class DiffPathPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    // ** WORKBENCH PARTICULARS **
    private I_TermFactory tf;
    I_ConfigAceFrame config;

    String typeFont = "<font face='Dialog' size='3' color='blue'>";
    String valueFont = "<font face='Dialog' size='3' color='green'>";

    public DiffPathPanel(I_ConfigAceFrame caf) {
        super();
        tf = Terms.get();
        config = caf;
        this.setLayout(new GridBagLayout());
    }

    public void update() {
        JTable table;
        this.removeAll();
        int rowHeight = 18;
        int tWide = 700;

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;

        // ADDED ISAs
        this.add(new JLabel("Added ISAs:"), c);
        if (SnoQuery.getIsaAdded() != null && SnoQuery.getIsaAdded().size() > 0) {

            table = updateTable(SnoQuery.getIsaAdded());
            table.setDragEnabled(false);
            table.setTransferHandler(new TerminologyTransferHandler(table));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // table.setSize(tWide, (SnoQuery.getIsaAdded().size() + 1) *
            // tHeight);
            table.setRowHeight(rowHeight);
            table.setPreferredScrollableViewportSize(new Dimension(tWide, rowHeight * table.getRowCount()));

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;
            c.gridy += 1;
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            JScrollPane jscrollpane = new JScrollPane(table);
            jscrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            jscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.add(jscrollpane, c);
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.weighty = 0;
        } else {
            c.gridy += 1;
            this.add(new JLabel("<HTML><FONT COLOR='gray'><I> -- No ISAs added. --"), c);
        }

        // RETIRED (DROPPED) ISAs
        c.gridy += 1;
        this.add(new JLabel("Retired ISAs:"), c); // Dropped
        if (SnoQuery.getIsaDropped() != null && SnoQuery.getIsaDropped().size() > 0) {
            table = updateTable(SnoQuery.getIsaDropped());
            table.setDragEnabled(false);
            table.setTransferHandler(new TerminologyTransferHandler(table));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // table.setSize(tWide, (SnoQuery.getIsaDropped().size() + 1) *
            // tHeight);
            table.setRowHeight(rowHeight);
            table.setPreferredScrollableViewportSize(new Dimension(tWide, rowHeight * table.getRowCount()));

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;
            c.gridy += 1;
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            JScrollPane jscrollpane = new JScrollPane(table);
            jscrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            jscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.add(jscrollpane, c);
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.weighty = 0;
        } else {
            c.gridy += 1;
            this.add(new JLabel("<HTML><FONT COLOR='gray'><I> -- No ISAs retired. --"), c);
        }
        // ADDED ROLES
        c.gridy += 1;
        this.add(new JLabel("Added Roles:"), c);
        if (SnoQuery.getRoleAdded() != null && SnoQuery.getRoleAdded().size() > 0) {
            table = updateTable(SnoQuery.getRoleAdded());
            table.setDragEnabled(false);
            table.setTransferHandler(new TerminologyTransferHandler(table));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // table.setSize(tWide, (SnoQuery.getRoleAdded().size() + 1) *
            // tHeight);
            table.setRowHeight(rowHeight);
            table.setPreferredScrollableViewportSize(new Dimension(tWide, rowHeight * table.getRowCount()));

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;
            c.gridy += 1;
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            JScrollPane jscrollpane = new JScrollPane(table);
            jscrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            jscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.add(jscrollpane, c);
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.weighty = 0;
        } else {
            c.gridy += 1;
            this.add(new JLabel("<HTML><FONT COLOR='gray'><I> -- No roles added. --"), c);
        }

        // RETIRED (DROPPED) ROLES
        c.gridy += 1;
        this.add(new JLabel("Retired Roles:"), c);
        if (SnoQuery.getRoleDropped() != null && SnoQuery.getRoleDropped().size() > 0) {
            c.gridy += 1;
            c.weightx = 0.5;
            c.weighty = 0.5;
            table = updateTable(SnoQuery.getRoleDropped());
            table.setDragEnabled(false);
            table.setTransferHandler(new TerminologyTransferHandler(table));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // table.setSize(tWide, (SnoQuery.getRoleDropped().size() + 1) *
            // tHeight);
            table.setRowHeight(rowHeight);
            table.setPreferredScrollableViewportSize(new Dimension(tWide, rowHeight * table.getRowCount()));

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1;
            c.weighty = 1;
            c.gridy += 1;
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            JScrollPane jscrollpane = new JScrollPane(table);
            jscrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            jscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.add(jscrollpane, c);
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.weighty = 0;
        } else {
            c.gridy += 1;
            c.weightx = 0.5;
            c.weighty = 0.5;
            this.add(new JLabel("<HTML><FONT COLOR='gray'><I> -- No roles retired. --"), c);
        }

    }

    private JTable updateTable(ArrayList<SnoRel> srl) {
        Object[][] theTableData;
        try {
            theTableData = getTableData(srl);
        } catch (TerminologyException ex) {
            Logger.getLogger(DiffPathPanel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(DiffPathPanel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        DiffTableModel theTableModel = new DiffTableModel(theTableData, srl);
        JTable table = new JTableWithDragImage(theTableModel);

        DiffTableRenderer renderer = new DiffTableRenderer(config);
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);

        // set column widths
        TableColumn tc = table.getColumnModel().getColumn(0);
        // tc.setPreferredWidth(400 + 368);
        tc.setPreferredWidth(3200);

        int totalRowHeight = 18;
        table.setPreferredScrollableViewportSize(new Dimension(900, totalRowHeight));
        return table;
    }

    private Object[][] getTableData(ArrayList<SnoRel> srl) throws TerminologyException, IOException {
        int totalCol = 1;
        int totalRows = srl.size();
        Object tableStrings[][] = new Object[totalRows][totalCol];

        for (int i = 0; i < totalRows; i++) {

            tableStrings[i][0] = srl.get(i);
        }

        return tableStrings;
    }

}
