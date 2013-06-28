/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dwfa.ace.classifier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.*;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.task.classify.SnoQuery;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.tapi.TerminologyException;

public class DiffReportPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    // ** WORKBENCH PARTICULARS **
    private I_TermFactory tf;
    I_ConfigAceFrame config;
    String typeFont = "<font face='Dialog' size='3' color='blue'>";
    String valueFont = "<font face='Dialog' size='3' color='green'>";

    public DiffReportPanel(I_ConfigAceFrame caf) {
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

        // REPORT
        this.add(new JLabel(""), c);
        if (SnoQuery.getIsaAdded() != null && SnoQuery.getIsaAdded().size() > 0) {
            c.gridx = 1;
            JButton jbtn = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/notebook_add.png")));
            jbtn.addActionListener(new AddToList());
            jbtn.setToolTipText("add concepts to list view");
            this.add(jbtn, c);

            ArrayList<SnoRelReport> srrl = new ArrayList<>();
            ArrayList<SnoRel> asd = SnoQuery.getIsaAdded();
            for (SnoRel snoRel : SnoQuery.getIsaAdded()) {
                srrl.add(new SnoRelReport(snoRel, true));
            }
            for (SnoRel snoRel : SnoQuery.getIsaDropped()) {
                srrl.add(new SnoRelReport(snoRel, false));
            }
            for (SnoRel snoRel : SnoQuery.getRoleAdded()) {
                srrl.add(new SnoRelReport(snoRel, true));
            }
            for (SnoRel snoRel : SnoQuery.getRoleDropped()) {
                srrl.add(new SnoRelReport(snoRel, false));
            }
            table = updateTable(srrl);
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
            c.gridx = 0;
            c.gridy += 1;
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            JScrollPane jscrollpane = new JScrollPane(table);
            jscrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            jscrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.add(jscrollpane, c);
            c.fill = GridBagConstraints.NONE;
            c.weightx = 0;
            c.weighty = 0;
        } else {
            c.gridy += 1;
            this.add(new JLabel("<HTML><FONT COLOR='gray'><I> -- no differences reported --"), c);
        }

    }

    private JTable updateTable(ArrayList<SnoRelReport> srl) {
        Object[][] theTableData;
        try {
            theTableData = getTableData(srl);
        } catch (TerminologyException | IOException ex) {
            Logger.getLogger(DiffReportPanel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        DiffReportTableModel theTableModel = new DiffReportTableModel(theTableData, srl);
        JTable table = new JTableWithDragImage(theTableModel);

        DiffReportTableRenderer renderer = new DiffReportTableRenderer(config);
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);

        // sorter
        TableRowSorter<DiffReportTableModel> sorter = new TableRowSorter<>();
        sorter.setModel(theTableModel);
        Comparator<SnoRelReport> comparatorIsAdded = new ComparatorIsAdded();
        sorter.setComparator(0, comparatorIsAdded);
        Comparator<SnoRelReport> comparatorC1 = new ComparatorC1();
        sorter.setComparator(1, comparatorC1);
        Comparator<SnoRelReport> comparatorType = new ComparatorType();
        sorter.setComparator(2, comparatorType);
        Comparator<SnoRelReport> comparatorC2 = new ComparatorC2();
        sorter.setComparator(3, comparatorC2);
        sorter.setSortable(4, false); // not sortable by group
        table.setRowSorter(sorter);

        // set column widths
        // add drop column
        TableColumn tc = table.getColumnModel().getColumn(0);
        tc.setMinWidth(32);
        tc.setPreferredWidth(64);
        tc.setMaxWidth(128);
        // c1 source concept
        tc = table.getColumnModel().getColumn(1);
        tc.setPreferredWidth(200);
        // type concept
        tc = table.getColumnModel().getColumn(2);
        tc.setPreferredWidth(200);
        // c2 concept
        tc = table.getColumnModel().getColumn(3);
        tc.setPreferredWidth(200);
        // group
        tc = table.getColumnModel().getColumn(4);
        tc.setMinWidth(18);
        tc.setPreferredWidth(48);
        tc.setMaxWidth(64);

        int totalRowHeight = 18;
        table.setPreferredScrollableViewportSize(new Dimension(900, totalRowHeight));
        return table;
    }

    public class AddToList implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                JList conceptList = config.getBatchConceptList();
                I_ModelTerminologyList conceptListModel = (I_ModelTerminologyList) conceptList.getModel();

                ArrayList<SnoRel> added = SnoQuery.getIsaAdded();
                Collections.sort(added);

                for (int i = 0; i < added.size(); i++) {
                    if (i < added.size() - 1) {
                        if (added.get(i).c1Id != added.get(i + 1).c1Id) {
                            I_GetConceptData cb = Terms.get().getConcept(added.get(i).c1Id);
                            conceptListModel.addElement(cb);
                        }
                    } else {
                        I_GetConceptData cb = Terms.get().getConcept(added.get(i).c1Id);
                        conceptListModel.addElement(cb);
                    }
                }

            } catch (TerminologyException | IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private Object[][] getTableData(ArrayList<SnoRelReport> srl) throws TerminologyException, IOException {
        int totalCol = 5; // add drop, c1 source, type, c2 destination, group
        int totalRows = srl.size();
        Object tableStrings[][] = new Object[totalRows][totalCol];

        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < totalCol; j++) {
                tableStrings[i][j] = srl.get(i);
            }
        }

        return tableStrings;
    }

    private static class ComparatorC1 implements Comparator<SnoRelReport> {

        public ComparatorC1() {
        }

        @Override
        public int compare(SnoRelReport sr1, SnoRelReport sr2) {
            String strings1 = sr1.snoRel.toStringC1();
            String strings2 = sr2.snoRel.toStringC1();
            return strings1.compareTo(strings2);
        }
    }

    private static class ComparatorType implements Comparator<SnoRelReport> {

        public ComparatorType() {
        }

        @Override
        public int compare(SnoRelReport sr1, SnoRelReport sr2) {
            String strings1 = sr1.snoRel.toStringType();
            String strings2 = sr2.snoRel.toStringType();
            return strings1.compareTo(strings2);
        }
    }

    private static class ComparatorC2 implements Comparator<SnoRelReport> {

        public ComparatorC2() {
        }

        @Override
        public int compare(SnoRelReport sr1, SnoRelReport sr2) {
            String strings1 = sr1.snoRel.toStringC2();
            String strings2 = sr2.snoRel.toStringC2();
            return strings1.compareTo(strings2);
        }
    }

    private static class ComparatorIsAdded implements Comparator<SnoRelReport> {

        public ComparatorIsAdded() {
        }

        @Override
        public int compare(SnoRelReport sr1, SnoRelReport sr2) {
            boolean boolean1 = sr1.isAdded;
            boolean boolean2 = sr2.isAdded;
            if (boolean1 == true && boolean2 == false) {
                return 1;
            } else if (boolean1 == false && boolean2 == true) {
                return -1;
            }
            return 0;
        }
    }
}
