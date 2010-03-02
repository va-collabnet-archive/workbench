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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
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
        JTable table = null;
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
            table.setDragEnabled(true);
            table.setTransferHandler(new TerminologyTransferHandler(table));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // table.setSize(tWide, (SnoQuery.getIsaAdded().size() + 1) *
            // tHeight);
            table.setRowHeight(rowHeight);
            table.setPreferredScrollableViewportSize(new Dimension(tWide, rowHeight * table.getRowCount()));

            c.gridy += 1;
            this.add(new JScrollPane(table), c);
        } else {
            c.gridy += 1;
            this.add(new JLabel("<HTML><FONT COLOR='gray'><I> -- No ISAs added. --"), c);
        }

        // DROPPED ISAs
        c.gridy += 1;
        this.add(new JLabel("Dropped ISAs:"), c);
        if (SnoQuery.getIsaDropped() != null && SnoQuery.getIsaDropped().size() > 0) {
            table = updateTable(SnoQuery.getIsaDropped());
            table.setDragEnabled(true);
            table.setTransferHandler(new TerminologyTransferHandler(table));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // table.setSize(tWide, (SnoQuery.getIsaDropped().size() + 1) *
            // tHeight);
            table.setRowHeight(rowHeight);
            table.setPreferredScrollableViewportSize(new Dimension(tWide, rowHeight * table.getRowCount()));

            c.gridy += 1;
            this.add(new JScrollPane(table), c);
        } else {
            c.gridy += 1;
            this.add(new JLabel("<HTML><FONT COLOR='gray'><I> -- No ISAs dropped. --"), c);
        }
        // ADDED ROLES
        c.gridy += 1;
        this.add(new JLabel("Added Roles:"), c);
        if (SnoQuery.getRoleAdded() != null && SnoQuery.getRoleAdded().size() > 0) {
            table = updateTable(SnoQuery.getRoleAdded());
            table.setDragEnabled(true);
            table.setTransferHandler(new TerminologyTransferHandler(table));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // table.setSize(tWide, (SnoQuery.getRoleAdded().size() + 1) *
            // tHeight);
            table.setRowHeight(rowHeight);
            table.setPreferredScrollableViewportSize(new Dimension(tWide, rowHeight * table.getRowCount()));

            c.gridy += 1;
            this.add(new JScrollPane(table), c);
        } else {
            c.gridy += 1;
            this.add(new JLabel("<HTML><FONT COLOR='gray'><I> -- No roles added. --"), c);
        }

        // DROPPED ROLES
        c.gridy += 1;
        this.add(new JLabel("Dropped Roles:"), c);
        if (SnoQuery.getRoleDropped() != null && SnoQuery.getRoleDropped().size() > 0) {
            c.gridy += 1;
            c.weightx = 0.5;
            c.weighty = 0.5;
            table = updateTable(SnoQuery.getRoleDropped());
            table.setDragEnabled(true);
            table.setTransferHandler(new TerminologyTransferHandler(table));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // table.setSize(tWide, (SnoQuery.getRoleDropped().size() + 1) *
            // tHeight);
            table.setRowHeight(rowHeight);
            table.setPreferredScrollableViewportSize(new Dimension(tWide, rowHeight * table.getRowCount()));

            this.add(new JScrollPane(table), c);
        } else {
            c.gridy += 1;
            c.weightx = 0.5;
            c.weighty = 0.5;
            this.add(new JLabel("<HTML><FONT COLOR='gray'><I> -- No roles dropped. --"), c);
        }

    }

    private JTable updateTable(ArrayList<SnoRel> srl) {
        String[][] theTableStr = null;

        try {
            theTableStr = getTableStrings(srl);
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        DiffTableModel theTableModel = new DiffTableModel(theTableStr, srl);
        JTable table = new JTable(theTableModel);

        DiffTableRenderer renderer = new DiffTableRenderer(config);
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);

        // set column widths
        TableColumn tc = table.getColumnModel().getColumn(0);
        tc.setPreferredWidth(400 + 368);

        int totalRowHeight = 18;
        table.setPreferredScrollableViewportSize(new Dimension(900, totalRowHeight));
        return table;
    }

    private String[][] getTableStrings(ArrayList<SnoRel> srl) throws TerminologyException, IOException {
        int totalCol = 1;
        int totalRows = srl.size();
        String tableStrings[][] = new String[totalRows][totalCol];

        for (int i = 0; i < totalRows; i++) {
            SnoRel sr = srl.get(i);
            StringBuilder str = new StringBuilder("<html>");
            // CONCEPT_1
            I_GetConceptData c1Bean = tf.getConcept(sr.c1Id);
            str.append(valueFont + c1Bean.getInitialText());
            // ROLE TYPE
            I_GetConceptData typeBean = tf.getConcept(sr.typeId);
            str.append(" - </font>" + typeFont + typeBean.getInitialText());
            // CONCEPT_2
            I_GetConceptData c2Bean = tf.getConcept(sr.c2Id);
            str.append(" - </font>" + valueFont + c2Bean.getInitialText());
            tableStrings[i][0] = str.toString();
        }

        return tableStrings;
    }

}
