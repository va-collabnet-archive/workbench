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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.logging.Level;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.AceTableRenderer;
import org.dwfa.ace.task.classify.SnoConSer;
import org.dwfa.ace.task.classify.SnoDL;
import org.dwfa.ace.task.classify.SnoDLSet;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;

/**
 * Classifier Description Logic Edit Panel
 * 
 * 
 * @author Marc Campbell
 * 
 */

public class DLPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    // ** WORKBENCH PARTICULARS **
    private I_TermFactory tf;
    I_ConfigAceFrame config;

    // BUTTONS
    private JButton btnReadFromFile;
    private JButton btnSaveToFile;
    private JButton btnAddDLList;
    private JButton btnAddDLEdit;
    private JButton btnAddNeverGrp;
    private Dimension btnDim_32x32;
    private Dimension btnDim_24x24;
    private Dimension btnDim_16x16;

    // Table Data Models
    private TableModel_DLEdit_Lhs tableModelDLEditLhs;
    private TableModel_DLEdit_Rhs tableModelDLEditRhs;
    private TableModel_DLList tableModelDLList;
    private TableModel_NeverGrp tableModelNeverGrp;
    
    //
    private JTable tableDLEditLhs;
    private JTable tableDLEditRhs;
    private JTable tableNeverGrp;

    // EDIT & LIST TABLE COORDINATION
    private SnoDL editDL; // current DL being edited
    private int editRowIdx = -1; // current DL row being edited
    private JTable tableDLList;
    private JLabel labelDL;
    private JLabel labelKRSS;

    private static final int FORMAT_DL = 0;
    private static final int FORMAT_KRSS = 1;
    private int dlFormat = FORMAT_DL;

    // INTERNAL
    private static final boolean debug = false; // :DEBUG:

    public DLPanel(I_ConfigAceFrame caf) {
        super();
        tf = Terms.get();
        config = caf;
        this.setLayout(new GridBagLayout());
        btnDim_16x16 = new Dimension(16, 16);
        btnDim_24x24 = new Dimension(24, 24);
        btnDim_32x32 = new Dimension(32, 32);
        dlFormat = FORMAT_DL;

        // 
        SnoDLSet.initCheck();

        // SETUP TABLES
        tableModelDLEditLhs = new TableModel_DLEdit_Lhs();
        tableModelDLEditRhs = new TableModel_DLEdit_Rhs();
        tableModelDLList = new TableModel_DLList();
        tableModelNeverGrp = new TableModel_NeverGrp();

        labelDL = new JLabel();
        labelKRSS = new JLabel();
        try {
			if (AceConfig.config.getProperty("dlogic") != null) {
				readDlFile(new File((String) AceConfig.config.getProperty("dlogic")));
			}
		} catch (FileNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ClassNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
    }

	@SuppressWarnings("unchecked")
	private void readDlFile(File theFile) throws IOException,
			FileNotFoundException, ClassNotFoundException {
		AceConfig.config.setProperty("dlogic", FileIO
				.getNormalizedRelativePath(theFile));
		FileInputStream fis = new FileInputStream(theFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);
		ArrayList<SnoDL> dll = (ArrayList<SnoDL>) (ois.readObject());
		ArrayList<SnoConSer> ng = (ArrayList<SnoConSer>) (ois.readObject());
		SnoDLSet.init();
		SnoDLSet.setDLList(dll);
		SnoDLSet.setNeverGroup(ng);
		ois.close();
	}

    private void updateEdit(int index) {
        if (SnoDLSet.getDLList() != null) {
            editRowIdx = index;
            editDL = SnoDLSet.getDLList().get(index);
            tableModelDLEditRhs.select(index);
            tableModelDLEditLhs.select(index);
            labelDL.setText("<html><font face='Dialog' size='3' color='black'>DL: "
                    + "<font face='Dialog' size='3' color='black'>" + editDL.toStringDl());
            labelKRSS.setText("<html><font face='Dialog' size='3' color='black'>KRSS: "
                    + "<font face='Dialog' size='3' color='black'>" + editDL.toStringKrss());
        }
    }

    public void update() {
        this.removeAll();
        int widthDescPref = 250;
        int widthDescMin = 150;
        int widthDescMax = 2000;

        int widthBtn = 32;
        int heightRow = 24;

        // BUTTONS: READ FROM FILE, WRITE TO FILE
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.NONE; // do not resize component
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        btnReadFromFile = new JButton(new ImageIcon(ACE.class
                .getResource("/24x24/plain/read_from_disk.png")));
        btnReadFromFile.setToolTipText("Read Description Logic from file.");
        btnReadFromFile.addActionListener(new Listener_BtnFileRead());
        btnReadFromFile.setMaximumSize(btnDim_32x32);
        btnReadFromFile.setPreferredSize(btnDim_32x32);
        this.add(btnReadFromFile, c);

        c.weightx = 0.5;
        c.gridx += 1;
        btnSaveToFile = new JButton(new ImageIcon(ACE.class
                .getResource("/24x24/plain/save_to_disk.png")));
        btnSaveToFile.setToolTipText("Write Description Logic to file.");
        btnSaveToFile.addActionListener(new Listener_BtnFileWrite());
        btnSaveToFile.setMaximumSize(btnDim_32x32);
        btnSaveToFile.setPreferredSize(btnDim_32x32);
        this.add(btnSaveToFile, c);

        // LABEL: DESCRIPTION LOGIC EDITOR (DL Edit) **SECTION**
        c.gridy += 1;
        c.weightx = 0;
        c.gridx = 0;
        c.gridwidth = 2;
        JLabel label = new JLabel();
        label.setText("Declaration Terms: LHS[0] o ... o LHS[k] -> RHS");
        this.add(label, c);

        // TABLE: RHS TABLE (VIEW ONLY)
        c.fill = GridBagConstraints.HORIZONTAL; // resize horizontally
        c.gridy += 1;
        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = 0.5;

        JTable table = new JTable(tableModelDLEditRhs);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(heightRow);
        table.setMinimumSize(new Dimension(widthDescMin, heightRow * 1));
        table.setPreferredSize(new Dimension(widthDescPref, heightRow * 1));
        table.setMaximumSize(new Dimension(widthDescMax, heightRow * 1));
        TableColumn tc = table.getColumnModel().getColumn(0);
        // tc.setPreferredWidth(widthDescPref);
        tc.setResizable(true);
        tc.setCellRenderer(new Renderer_SnoCon());
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(widthDescPref, heightRow * 2));
        this.add(sp, c);

        // BUTTON: ADD LHS TERMS
        c.fill = GridBagConstraints.NONE; // do not resize
        c.gridy += 1;
        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        btnAddDLEdit = new JButton(new ImageIcon(ACE.class
                .getResource("/24x24/plain/row_add_after.png")));
        btnAddDLEdit.setToolTipText("add new LHS role from taxonomy selection");
        btnAddDLEdit.addActionListener(new Listener_BtnAdd_DLLhs(null, config));
        btnAddDLEdit.setMaximumSize(btnDim_32x32);
        btnAddDLEdit.setPreferredSize(btnDim_32x32);
        this.add(btnAddDLEdit, c);

        // TABLE: LHS LIST
        c.fill = GridBagConstraints.HORIZONTAL; // resize horizontally
        c.gridx = 1;
        c.weightx = 0.5;
        table = new JTable(tableModelDLEditLhs);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(heightRow);
        int rows = tableModelDLEditLhs.getRowCount();
        rows = rows <= 2 ? 2 : rows;
        table.setMinimumSize(new Dimension(widthDescMin + 4 * widthBtn, heightRow * rows));
        table.setPreferredSize(new Dimension(widthDescPref + 4 * widthBtn, heightRow * rows));
        table.setMaximumSize(new Dimension(widthDescMax + 4 * widthBtn, heightRow * rows));
        tc = table.getColumnModel().getColumn(0);
        tc.setCellRenderer(new Renderer_SnoCon());
        setUpActButtons(table);
        sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(widthDescPref + 4 * widthBtn, heightRow * (rows + 1)));
        this.add(sp, c);

        // LABELS: DISPLAY DL & KRSS
        c.fill = GridBagConstraints.NONE;
        c.gridy += 1;
        c.weightx = 0;
        c.gridx = 1;
        c.gridwidth = 1;
        this.add(labelDL, c);
        c.gridy += 1;
        this.add(labelKRSS, c);

        // LABEL: "DL LIST" **SECTION**
        c.fill = GridBagConstraints.NONE;
        c.gridy += 1;
        c.weightx = 0;
        c.gridx = 0;
        c.gridwidth = 2;
        label = new JLabel();
        label.setText("Declarations List: ");
        this.add(label, c);

        // BUTTON: ADD "DL LIST" BUTTON
        c.fill = GridBagConstraints.NONE;
        c.gridy += 1;
        c.gridx = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        btnAddDLList = new JButton(new ImageIcon(ACE.class
                .getResource("/24x24/plain/row_add_after.png")));
        btnAddDLList.setToolTipText("add new DL (RHS) from taxonomy selection");
        btnAddDLList.addActionListener(new Listener_BtnAdd_DLList(null, config));
        btnAddDLList.setMaximumSize(btnDim_32x32);
        btnAddDLList.setPreferredSize(btnDim_32x32);
        this.add(btnAddDLList, c);

        // TABLE: "DL LIST"
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        table = new JTable(tableModelDLList);
        tableDLList = table;
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new Listener_Row());
        table.setRowHeight(heightRow);
        //rows = tableModelDLList.getRowCount();
        //rows = rows <= 2 ? 2 : rows;
        //table.setMinimumSize(new Dimension(widthDescMin + 4 * widthBtn, heightRow * rows));
        //table.setPreferredSize(new Dimension(widthDescPref + 4 * widthBtn, heightRow * rows));
        //table.setMaximumSize(new Dimension(widthDescMax + 4 * widthBtn, heightRow * rows));

        tc = table.getColumnModel().getColumn(0);
        tc.setCellRenderer(new Renderer_SnoDL());

        // :!!!:NYI: DL, KRSS ComboBox
        if (true) {
            TableColumn_SelectSyntax col;
            String[] items = {
                    "<html><font face='Dialog' size='3' color='black'>DL Format",
                    "<html><font face='Dialog' size='3' color='black'>KRSS Format",
                    "<html><font face='Dialog' size='3' color='black'>DL Format;</font>"
                            + "<font face='Dialog' size='3' color='black'>KRSS Format" };
            JComboBox combo = new JComboBox();
            for (int i = 0; i < items.length; i++) {
                combo.addItem(items[i]);
            }
            tc.setHeaderValue(combo.getItemAt(dlFormat));
        }

        setUpActButtons(table);
        sp = new JScrollPane(table);
        // sp.setPreferredSize(new Dimension(widthDescPref + 4 * widthBtn, heightRow * (rows + 1)));
        this.add(sp, c);

        // LABEL: "NEVER GROUP" ROLES **SECTION**
        c.fill = GridBagConstraints.NONE;
        c.gridy += 1;
        c.weightx = 0;
        c.gridx = 0;
        c.gridwidth = 2;
        label = new JLabel();
        label.setText("Never-Group Role List: ");
        this.add(label, c);

        // ADD "NEVER GROUP" BUTTON
        c.fill = GridBagConstraints.NONE;
        c.gridy += 1;
        c.gridx = 0;
        c.weightx = 0;
        c.weighty = 0.5;
        c.gridwidth = 1;
        btnAddNeverGrp = new JButton(new ImageIcon(ACE.class
                .getResource("/24x24/plain/row_add_after.png")));
        btnAddNeverGrp.setToolTipText("add 'Never-Grouped' Role from taxonomy selection");
        btnAddNeverGrp.addActionListener(new Listener_BtnAdd_NeverGrp(null, config));
        btnAddNeverGrp.setMaximumSize(btnDim_32x32);
        btnAddNeverGrp.setPreferredSize(btnDim_32x32);
        this.add(btnAddNeverGrp, c);

        // ADD "NEVER GROUP" TABLE
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.gridx = 1;
        table = new JTable(tableModelNeverGrp);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(heightRow);
        //rows = tableModelNeverGrp.getRowCount();
        //rows = rows <= 2 ? 2 : rows;
        //table.setMinimumSize(new Dimension(widthDescMin + 1 * widthBtn, heightRow * rows));
        //table.setPreferredSize(new Dimension(widthDescPref + 1 * widthBtn, heightRow * rows));
        //table.setMaximumSize(new Dimension(widthDescMax + 1 * widthBtn, heightRow * rows));

        tc = table.getColumnModel().getColumn(0);
        tc.setCellRenderer(new Renderer_SnoCon());
        Renderer_BtnAct_Del ngbr = new Renderer_BtnAct_Del();
        tc = table.getColumnModel().getColumn(1);
        tc.setCellRenderer(ngbr);
        tc.setCellEditor(new Editor_BtnAct(new JButton()));
        tc.setMaxWidth(32);
        tc.setResizable(false);
        
        sp = new JScrollPane(table);
        // sp.setPreferredSize(new Dimension(widthDescPref + 1 * widthBtn, heightRow * (rows + 1)));
        this.add(sp, c);

        //
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy += 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.gridwidth = 2;
        label = new JLabel();
        label.setText("  ");
        this.add(label, c);

    }

    void setUpActButtons(JTable table) {
        int width = 32;

        TableColumn tc = table.getColumnModel().getColumn(1);
        tc.setCellRenderer(new Renderer_BtnAct_Dup());
        tc.setMaxWidth(width);
        tc.setResizable(false);
        tc.setCellEditor(new Editor_BtnAct(new JButton()));

        tc = table.getColumnModel().getColumn(2);
        tc.setCellRenderer(new Renderer_BtnAct_Down());
        tc.setMaxWidth(width);
        tc.setResizable(false);
        tc.setCellEditor(new Editor_BtnAct(new JButton()));

        tc = table.getColumnModel().getColumn(3);
        tc.setCellRenderer(new Renderer_BtnAct_Up());
        tc.setMaxWidth(width);
        tc.setResizable(false);
        tc.setCellEditor(new Editor_BtnAct(new JButton()));

        tc = table.getColumnModel().getColumn(4);
        tc.setCellRenderer(new Renderer_BtnAct_Del());
        tc.setMaxWidth(width);
        tc.setResizable(false);
        tc.setCellEditor(new Editor_BtnAct(new JButton()));
    }

    private class Editor_BtnAct implements TableCellEditor, PropertyChangeListener, ActionListener {
        protected EventListenerList listeners = new EventListenerList();
        protected ChangeEvent changeEvent;
        protected EventObject editingEvent;
        protected JButton editor;

        // CONSTRUCTOR: BUTTON EDITOR
        public Editor_BtnAct(JButton button) {
            this.editor = button;
            editor.addPropertyChangeListener(this);
            button.addActionListener(this);
        }

        // *** ActionListener ***
        public void actionPerformed(ActionEvent e) {
            stopCellEditing();
        }

        // *** CellEditor ***
        public void addCellEditorListener(CellEditorListener l) {
            listeners.add(CellEditorListener.class, l);
        }

        // *** CellEditor ***
        public void cancelCellEditing() {
            Object[] l = listeners.getListenerList();
            for (int i = l.length - 2; i >= 0; i -= 2) {
                if (l[i] == CellEditorListener.class) {
                    if (changeEvent == null) {
                        changeEvent = new ChangeEvent(this);
                    }
                    ((CellEditorListener) l[i + 1]).editingCanceled(changeEvent);
                }
            }
        }

        // *** CellEditor ***
        public Object getCellEditorValue() {
            return null;
        }

        // *** TableCellEditor ***
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            if (column >= 1)
                return editor;
            else
                return null;
        }

        // *** CellEditor ***
        public boolean isCellEditable(EventObject anEvent) {
            editingEvent = anEvent;
            return true;
        }

        // *** PropertyChangeListener ***
        public void propertyChange(PropertyChangeEvent evt) {
        }

        // *** CellEditor ***
        public void removeCellEditorListener(CellEditorListener l) {
            listeners.remove(CellEditorListener.class, l);
        }

        // *** CellEditor ***
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        // *** CellEditor ***
        public boolean stopCellEditing() {
            Object[] l = listeners.getListenerList();
            for (int i = l.length - 2; i >= 0; i -= 2) {
                if (l[i] == CellEditorListener.class) {
                    if (changeEvent == null) {
                        changeEvent = new ChangeEvent(this);
                    }
                    ((CellEditorListener) l[i + 1]).editingStopped(changeEvent);
                }
            }
            return true;
        }
    }

    private class Listener_BtnAdd_DLLhs implements ActionListener {
        private I_ConfigAceFrame config; // :!!!:

        public Listener_BtnAdd_DLLhs(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
            super();
            this.config = config;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (config.getClassifierInputPath() == null) {

                    JOptionPane.showMessageDialog(new JFrame(),
                            "Please set the Classifier Input (Stated) Path in the preferences.");
                    return;
                }
                doEdit(e, config);
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }

        void doEdit(ActionEvent e, I_ConfigAceFrame config) {
            if (editRowIdx < 0) {
                String errStr = "Right Hand Term (RHS) must be added to the Declarations List first.";
                AceLog.getAppLog().alertAndLog(Level.INFO, errStr, new Exception(errStr));
                return;
            }

            int cNid = Integer.MAX_VALUE;
            if (config.getHierarchySelection() != null) {
                cNid = config.getHierarchySelection().getConceptId();
                int isRole = SnoTable.testIsRole(cNid);
                if (isRole == 1) {
                    AceLog.getAppLog().log(Level.INFO,
                            "::: TAXONOMY NEW DL RHS: " + SnoTable.toString(cNid));

                    tableModelDLEditLhs.append(cNid);
                    updateEdit(editRowIdx);
                    tableModelDLList.update();
                    tableModelDLList.fireTableDataChanged();
                } else if (isRole == 0) {
                    String errStr = "A role must be selected in the SNOMED taxomony.";
                    AceLog.getAppLog().alertAndLog(Level.INFO, errStr, new Exception(errStr));
                }
            }

        }
    }

    private class Listener_BtnAdd_DLList implements ActionListener {
        private I_ConfigAceFrame config; // :!!!:

        public Listener_BtnAdd_DLList(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
            super();
            this.config = config;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (config.getClassifierInputPath() == null) {

                    JOptionPane.showMessageDialog(new JFrame(),
                            "Please set the Classifier Input (Stated) Path in the preferences.");
                    return;
                }
                doEdit(e, config);
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }

        void doEdit(ActionEvent e, I_ConfigAceFrame config) {

            int cNid = Integer.MAX_VALUE;
            if (config.getHierarchySelection() != null) {
                cNid = config.getHierarchySelection().getConceptId();
                int isRole = SnoTable.testIsRole(cNid);
                if (isRole == 1) {
                    AceLog.getAppLog().log(Level.INFO,
                            "::: TAXONOMY NEW DL RHS: " + SnoTable.toString(cNid));

                    tableModelDLList.append(cNid);
                    editRowIdx = tableDLList.getRowCount() - 1;
                    tableDLList.setRowSelectionInterval(editRowIdx, editRowIdx);
                    tableModelDLEditLhs.select(editRowIdx);

                } else if (isRole == 0) {
                    String errStr = "A role must be selected in the SNOMED taxomony.";
                    AceLog.getAppLog().alertAndLog(Level.INFO, errStr, new Exception(errStr));
                }

            }

        }
    }

    private class Listener_BtnAdd_NeverGrp implements ActionListener {
        private I_ConfigAceFrame config; // :!!!:

        public Listener_BtnAdd_NeverGrp(I_ContainTermComponent termContainer,
                I_ConfigAceFrame config) {
            super();
            this.config = config;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (config.getClassifierInputPath() == null) {

                    JOptionPane.showMessageDialog(new JFrame(),
                            "Please set the Classifier Input (Stated) Path in the preferences.");
                    return;
                }
                int cNid = Integer.MAX_VALUE;
                if (config.getHierarchySelection() != null) {
                    cNid = config.getHierarchySelection().getConceptId();
                    int isRole = SnoTable.testIsRole(cNid);
                    if (isRole == 1) {
                        AceLog.getAppLog().log(Level.INFO,
                                "::: TAXONOMY - NEVER GROUPED: " + SnoTable.toString(cNid));
                        tableModelNeverGrp.append(cNid);
                    } else if (isRole == 0) {
                        String errStr = "A role must be selected in the SNOMED taxomony.";
                        AceLog.getAppLog().alertAndLog(Level.INFO, errStr, new Exception(errStr));
                    }
                }
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }
    }

    private class Listener_BtnFileRead implements ActionListener {

        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {
            try {
                FileDialog dialog = new FileDialog((Frame) DLPanel.this.getTopLevelAncestor(),
                        "Open Logic", FileDialog.LOAD);
                //
                dialog.setDirectory(System.getProperty("user.dir")
                        + System.getProperty("file.separator") + "logic");
                dialog.setFilenameFilter(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".dlogic");
                    }
                });
                // Display dialog and wait for response
                dialog.setVisible(true);
                // Check response
                if (dialog.getFile() != null) {
                    File theFile = new File(dialog.getDirectory(), dialog.getFile());
                    readDlFile(theFile);
                }
                // Cleanup
                dialog.dispose();
            } catch (FileNotFoundException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            } catch (IOException e2) { // from ObjectInputStream
                AceLog.getAppLog().alertAndLogException(e2);
            } catch (ClassNotFoundException e3) {
                AceLog.getAppLog().alertAndLogException(e3);
            }
            update();
        }
    }

    private class Listener_BtnFileWrite implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                FileDialog dialog = new FileDialog((Frame) DLPanel.this.getTopLevelAncestor(),
                        "Save Logic (.dl)", FileDialog.SAVE);
                //
                dialog.setDirectory(AceConfig.config.getProfileFile().getParent());
                dialog.setName("kb.dlogic");
                // Display dialog and wait for response
                dialog.setVisible(true);
                // Check response
                if (dialog.getFile() != null) {
                    String fileName = dialog.getFile();
                    if (fileName.endsWith(".dlogic") == false) {
                        fileName = fileName + ".dlogic";
                    }
                    File binaryFile = new File(dialog.getDirectory(), fileName);
                    if (AceConfig.config.getProperty("dlogic") != null) {
                		AceConfig.config.setProperty("dlogic", FileIO
                				.getNormalizedRelativePath(binaryFile));
                    }

                    FileOutputStream fos = new FileOutputStream(binaryFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(SnoDLSet.getDLList());
                    oos.writeObject(SnoDLSet.getNeverGroup());
                    oos.flush();
                    oos.close();
                }
                // Cleanup
                dialog.dispose();
            } catch (FileNotFoundException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            } catch (IOException e2) { // from ObjectOutputStream
                AceLog.getAppLog().alertAndLogException(e2);
            }

        }

    }

    private class Listener_Row implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                // true if event is one of multiple change events
                return;
            }

            int row = tableDLList.getSelectedRow();
            if (row >= 0) {
                updateEdit(row);
            }

        }

    }

    private class Renderer_BtnAct_Del extends JButton implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        Renderer_BtnAct_Del() {
            this.setOpaque(true);
            this.setIcon(new ImageIcon(this.getClass().getResource("/16x16/plain/delete2.png")));
            this.setToolTipText("Delete");
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class Renderer_BtnAct_Dup extends JButton implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        Renderer_BtnAct_Dup() {
            this.setOpaque(true);
            this.setIcon(new ImageIcon(this.getClass()
                    .getResource("/16x16/plain/navigate_plus.png"))); // add2.png
            this.setToolTipText("Duplicate");
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class Renderer_BtnAct_Down extends JButton implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        Renderer_BtnAct_Down() {
            this.setOpaque(true);
            this.setIcon(new ImageIcon(this.getClass()
                    .getResource("/16x16/plain/navigate_down.png"))); // navigate_down2.png
            this.setToolTipText("Move down");
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class Renderer_BtnAct_Up extends JButton implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        Renderer_BtnAct_Up() {
            this.setOpaque(true);
            this
                    .setIcon(new ImageIcon(this.getClass().getResource(
                            "/16x16/plain/navigate_up.png"))); // navigate_up2.png
            this.setToolTipText("Move up");
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class Renderer_SnoCon extends AceTableRenderer implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        boolean renderInactive = false;

        public Renderer_SnoCon() {
            super();
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderComponent = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (isSelected == false) {
                if (renderInactive) {
                    renderComponent.setBackground(UIManager.getColor("Table.background"));
                    renderComponent.setForeground(UIManager.getColor("Table.foreground"));
                } else {
                    renderComponent.setBackground(colorForRow(row));
                    renderComponent.setForeground(UIManager.getColor("Table.foreground"));
                }
            } else {
                renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
                renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));
            }

            setBorder(column, this, false, false, false); // .., same, uncommitted
            // if (renderInactive)
            // renderComponent.setBackground(Color.LIGHT_GRAY);

            // value is the object from data[row][column]
            SnoConSer sc = (SnoConSer) value;
            String s = null;
            try {
                I_GetConceptData cb = tf.getConcept(sc.id);
                s = new String(cb.getInitialText());
                if (debug)
                    s = String.valueOf(sc.id) + " - " + s;
            } catch (TerminologyException e) {
                s = new String("error: concept not found");
                e.printStackTrace();
            } catch (IOException e) {
                s = new String("error: concept not found");
                e.printStackTrace();
            }

            this.setText(s);
            return this;
        }

    }

    private class Renderer_SnoDL extends AceTableRenderer implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        boolean renderInactive = false;

        public Renderer_SnoDL() {
            super();
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderComponent = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);

            if (isSelected == false) {
                if (renderInactive) {
                    renderComponent.setBackground(UIManager.getColor("Table.background"));
                    renderComponent.setForeground(UIManager.getColor("Table.foreground"));
                } else {
                    renderComponent.setBackground(colorForRow(row));
                    renderComponent.setForeground(UIManager.getColor("Table.foreground"));
                }

                // value is the object from data[row][column]
                SnoDL sdl = (SnoDL) value;
                if (dlFormat == FORMAT_DL) {
                    setText("<html><font face='Dialog' size='3' color='black'>" + sdl.toStringDl());
                } else if (dlFormat == FORMAT_KRSS) {
                    setText("<html><font face='Dialog' size='3' color='black'>"
                            + sdl.toStringKrss());
                } else {
                    setText(sdl.toStringHtml());
                }
            } else {
                renderComponent.setBackground(UIManager.getColor("Table.selectionBackground"));
                renderComponent.setForeground(UIManager.getColor("Table.selectionForeground"));

                // value is the object from data[row][column]
                SnoDL sdl = (SnoDL) value;
                if (dlFormat == FORMAT_DL) {
                    setText("<html><font face='Dialog' size='3' color='white'>" + sdl.toStringDl());
                } else if (dlFormat == FORMAT_KRSS) {
                    setText("<html><font face='Dialog' size='3' color='white'>"
                            + sdl.toStringKrss());
                } else {
                    setText(sdl.toStringHtml());
                }
            }

            setBorder(column, this, false, false, false); // .., same, uncommitted
            // if (renderInactive)
            // renderComponent.setBackground(Color.LIGHT_GRAY);

            return this;
        }
    }

    private class TableModel_DLEdit_Rhs extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        protected static final int CONCEPT_COLUMN = 0;
        protected static final int COLUMN_COUNT = 1;

        protected String[] columnNames = { "<html><font face='Dialog' size='3' color='black'>"
            + "RHS (Right Hand Side)" };
        protected Object[][] data; // {concept}

        public TableModel_DLEdit_Rhs() {
            super();
            update();
        }

        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public int getRowCount() {
            return data.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        public void select(int index) {
            // keeps track of which DL is begin edited
            editDL = SnoDLSet.getDLList().get(index);
            select();
        }

        public void select() {
            // keeps track of which DL is begin edited
            update();
            fireTableDataChanged();
        }

        public void update() {
            if (editDL != null) {
                SnoConSer sc = editDL.getRhs();
                data = new Object[1][COLUMN_COUNT];
                data[0][CONCEPT_COLUMN] = sc;
            } else {
                data = new Object[0][COLUMN_COUNT];
            }
        }

    }

    private class TableColumn_SelectSyntax extends TableColumn {
        private static final long serialVersionUID = 1L;

        protected TableCellEditor headerEditor;

        protected boolean isHeaderEditable;

        public TableColumn_SelectSyntax() {
            setHeaderEditor(createDefaultHeaderEditor());
            isHeaderEditable = true;
        }

        public void setHeaderEditor(TableCellEditor headerEditor) {
            this.headerEditor = headerEditor;
        }

        public TableCellEditor getHeaderEditor() {
            return headerEditor;
        }

        public void setHeaderEditable(boolean isEditable) {
            isHeaderEditable = isEditable;
        }

        public boolean isHeaderEditable() {
            return isHeaderEditable;
        }

        public void copyValues(TableColumn base) {
            modelIndex = base.getModelIndex();
            identifier = base.getIdentifier();
            width = base.getWidth();
            minWidth = base.getMinWidth();
            setPreferredWidth(base.getPreferredWidth());
            maxWidth = base.getMaxWidth();
            headerRenderer = base.getHeaderRenderer();
            headerValue = base.getHeaderValue();
            cellRenderer = base.getCellRenderer();
            cellEditor = base.getCellEditor();
            isResizable = base.getResizable();
        }

        protected TableCellEditor createDefaultHeaderEditor() {
            return new DefaultCellEditor(new JTextField());
        }
    }

    private class TableModel_DLEdit_Lhs extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        protected static final int CONCEPT_COLUMN = 0;
        protected static final int DUPL_BTN_COLUMN = 1;
        protected static final int UP_BTN_COLUMN = 2;
        protected static final int DOWN_BTN_COLUMN = 3;
        protected static final int DEL_BTN_COLUMN = 4;
        protected static final int COLUMN_COUNT = 5;

        protected String[] columnNames = {
                "<html><font face='Dialog' size='3' color='black'>LHS[k] (Left Hand Side)", "", "",
                "", "" };
        protected Object[][] data; // {concept, null-delete button}

        public TableModel_DLEdit_Lhs() {
            super();
            update();
        }

        // append DL rhs from taxonomy selection
        public void append(int nid) {
            if (editDL != null) {
                editDL.addLhs(new SnoConSer(nid, false));
                update();
                fireTableDataChanged();
            }
        }

        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public int getRowCount() {
            return data.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        // OVERRIDE: make table editable
        public boolean isCellEditable(int row, int column) {
            return column == DEL_BTN_COLUMN || column == DUPL_BTN_COLUMN || column == UP_BTN_COLUMN
                || column == DOWN_BTN_COLUMN;
        }

        // OVERRIDE: make table editable
        public void setValueAt(Object value, int row, int column) {
            if (column == DEL_BTN_COLUMN) {
                editDL.del(row);
                update();
                fireTableDataChanged();
            } else if (column == DUPL_BTN_COLUMN) {
                editDL.dupl(row);
                update();
                fireTableDataChanged();
            } else if (column == UP_BTN_COLUMN) {
                editDL.move(row, 1);
                update();
                fireTableDataChanged();
            } else if (column == DOWN_BTN_COLUMN) {
                editDL.move(row, -1);
                update();
                fireTableDataChanged();
            }
        }

        public void select(int index) {
            // keeps track of which DL is begin edited
            editDL = SnoDLSet.getDLList().get(index);
            select();
        }

        public void select() {
            // keeps track of which DL is begin edited
            update();
            fireTableDataChanged();
        }

        public void update() {
            if (editDL != null) {
                List<SnoConSer> scl = editDL.getLhs();
                int max = scl.size();
                data = new Object[max][COLUMN_COUNT];
                for (int i = 0; i < max; i++) {
                    data[i][CONCEPT_COLUMN] = scl.get(i);
                    data[i][DUPL_BTN_COLUMN] = null;
                    data[i][UP_BTN_COLUMN] = null;
                    data[i][DOWN_BTN_COLUMN] = null;
                    data[i][DEL_BTN_COLUMN] = null;
                }
            } else {
                data = new Object[0][COLUMN_COUNT];
            }
        }
    }

    private class TableModel_DLList extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        protected static final int DESCR_COLUMN = 0;
        protected static final int DUPL_BTN_COLUMN = 1;
        protected static final int UP_BTN_COLUMN = 2;
        protected static final int DOWN_BTN_COLUMN = 3;
        protected static final int DEL_BTN_COLUMN = 4;
        protected static final int COLUMN_COUNT = 5;

        protected String[] columnNames = {
                "<html><font face='Dialog' size='3' color='black'>DL Format; </font>"
                        + "<font face='Dialog' size='3' color='black'>KRSS Format", "", "", "", "" };
        protected Object[][] data; // {concept, null-delete button}

        public TableModel_DLList() {
            super();
            update();
        }

        // append DL rhs from taxonomy selection
        public void append(int nid) {
            SnoDLSet.addDL(nid);
            update();
            fireTableDataChanged();
        }

        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public int getRowCount() {
            return data.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        // OVERRIDE: make table editable
        public boolean isCellEditable(int row, int column) {
            return column == DEL_BTN_COLUMN || column == DUPL_BTN_COLUMN || column == UP_BTN_COLUMN
                || column == DOWN_BTN_COLUMN;
        }

        // OVERRIDE: make table editable
        public void setValueAt(Object value, int row, int column) {
            if (column == DEL_BTN_COLUMN) {
                SnoDLSet.deleteDL(row);
                update();
                fireTableDataChanged();
            } else if (column == DUPL_BTN_COLUMN) {
                SnoDLSet.duplicateDL(row);
                update();
                fireTableDataChanged();
            } else if (column == UP_BTN_COLUMN) {
                SnoDLSet.moveDL(row, 1);
                update();
                fireTableDataChanged();
            } else if (column == DOWN_BTN_COLUMN) {
                SnoDLSet.moveDL(row, -1);
                update();
                fireTableDataChanged();
            }
        }

        public void update() {
            int max = SnoDLSet.sizeDLList();
            data = new Object[max][COLUMN_COUNT];
            List<SnoDL> sdll = SnoDLSet.getDLList();
            for (int i = 0; i < max; i++) {
                data[i][DESCR_COLUMN] = sdll.get(i);
                data[i][DUPL_BTN_COLUMN] = null;
                data[i][UP_BTN_COLUMN] = null;
                data[i][DOWN_BTN_COLUMN] = null;
                data[i][DEL_BTN_COLUMN] = null;
            }
        }
    }

    private class TableModel_NeverGrp extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        protected static final int SNOCON_COLUMN = 0;
        protected static final int DEL_BTN_COLUMN = 1;
        protected static final int COLUMN_COUNT = 2;

        protected String[] columnNames = {
                "<html><font face='Dialog' size='3' color='black'>Never-Group Roles", "" };
        protected Object[][] data; // {concept, null-delete button}

        public TableModel_NeverGrp() {
            super();
            updateDataModelArray();
        }

        // append current selection from taxonomy selection
        public void append(int nid) {
            SnoDLSet.addNeverGroup(nid);
            updateDataModelArray();
            fireTableDataChanged();
        }

        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public int getRowCount() {
            return data.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        // OVERRIDE: make table editable
        public boolean isCellEditable(int row, int column) {
            return column == DEL_BTN_COLUMN;
        }

        // OVERRIDE: make table editable
        public void setValueAt(Object value, int row, int column) {
            if (column == DEL_BTN_COLUMN) {
                SnoDLSet.delNeverGroup(row);
                updateDataModelArray();
                fireTableDataChanged();
            }
        }

        public void updateDataModelArray() {
            int max = SnoDLSet.sizeNeverGroup();
            data = new Object[max][COLUMN_COUNT];
            List<SnoConSer> srl = SnoDLSet.getNeverGroup();
            for (int i = 0; i < max; i++) {
                data[i][SNOCON_COLUMN] = srl.get(i);
                data[i][DEL_BTN_COLUMN] = null;
            }
        }
    }

}
