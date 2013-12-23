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
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.*;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.task.classify.SnoQuery;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.tapi.ComputationCanceled;
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
        Comparator<String> comparatorIsAdded = new ComparatorIsAdded();
        sorter.setComparator(0, comparatorIsAdded);
        Comparator<I_GetConceptData> comparatorName = new ComparatorName();
        sorter.setComparator(1, comparatorName);
        sorter.setComparator(2, comparatorName);
        sorter.setComparator(3, comparatorName);
        sorter.setSortable(4, false); // not sortable by group
        sorter.addRowSorterListener(new rsl());
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
                // conceptListModel.clear();

                ArrayList<SnoRel> conceptsToBeAdded = new ArrayList<SnoRel>(SnoQuery.getIsaAdded());
                conceptsToBeAdded.addAll(SnoQuery.getIsaDropped());
                conceptsToBeAdded.addAll(SnoQuery.getRoleAdded());
                conceptsToBeAdded.addAll(SnoQuery.getRoleDropped());
                Collections.sort(conceptsToBeAdded);

                for (int i = 0; i < conceptsToBeAdded.size(); i++) {
                    if (i < conceptsToBeAdded.size() - 1) {
                        if (conceptsToBeAdded.get(i).c1Id != conceptsToBeAdded.get(i + 1).c1Id) {
                            I_GetConceptData cb = Terms.get().getConcept(conceptsToBeAdded.get(i).c1Id);
                            conceptListModel.addElement(cb);
                        }
                    } else {
                        I_GetConceptData cb = Terms.get().getConcept(conceptsToBeAdded.get(i).c1Id);
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
                SnoRelReport srr = srl.get(i);
                if (j == 0) {
                    if (srr.isAdded) {
                        tableStrings[i][j] = "added";
                    } else {
                        tableStrings[i][j] = "retired";
                    }
                } else if (j == 1) {
                    tableStrings[i][j] = Terms.get().getConcept(srr.snoRel.c1Id);
                } else if (j == 2) {
                    tableStrings[i][j] = Terms.get().getConcept(srr.snoRel.typeId);
                } else if (j == 3) {
                    tableStrings[i][j] = Terms.get().getConcept(srr.snoRel.c2Id);
                } else if (j == 4) {
                    tableStrings[i][j] = (Integer) srr.snoRel.group;
                }
            }
        }

        return tableStrings;
    }

    private static class ComparatorName implements Comparator<I_GetConceptData> {

        public ComparatorName() {
        }

        @Override
        public int compare(I_GetConceptData cb1, I_GetConceptData cb2) {
            String strings1 = cb1.toUserString();
            String strings2 = cb2.toUserString();
            return strings1.compareTo(strings2);
        }
    }

    private static class ComparatorIsAdded implements Comparator<String> {

        public ComparatorIsAdded() {
        }

        @Override
        public int compare(String sr1, String sr2) {
            return sr1.compareTo(sr2);
        }
    }

    I_ShowActivity sortActivityPanel = null;
    long sortStartTime;

    private class rsl implements RowSorterListener {

        @Override
        public void sorterChanged(RowSorterEvent e) {
            // Show in Activity Viewer
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                sortActivityPanel = tf.newActivityPanel(true, config, "Sort classifier differences report", true);
                sortActivityPanel.setIndeterminate(false);
                sortActivityPanel.setValue(0);
                sortActivityPanel.setProgressInfoLower("Sorting column ...");
                sortStartTime = System.currentTimeMillis();
            } else if (sortActivityPanel != null) {
                // e.getType() == RowSorterEvent.Type.SORTED
                    sortActivityPanel.setProgressInfoLower("Sort completed. time = "
                            + toStringLapseSec(sortStartTime));
                try {
                    sortActivityPanel.complete();
                } catch (ComputationCanceled ex) {
                    Logger.getLogger(DiffReportPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
        private String toStringLapseSec(long startTime) {
        StringBuilder s = new StringBuilder();
        long stopTime = System.currentTimeMillis();
        long lapseTime = stopTime - startTime;
        s.append((float) lapseTime / 1000).append(" (seconds)");
        return s.toString();
    }
    
}
