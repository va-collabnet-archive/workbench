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
package org.dwfa.ace;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.swing.*;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_HostConceptPlugins.LINK_TYPE;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.file.ConceptListReader;
import org.dwfa.ace.file.ConceptListWriter;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.list.TerminologyTable;
import org.dwfa.ace.list.TerminologyTableModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.gui.button.Button32x32;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.arena.Arena;
import org.ihtsdo.batch.BatchActionEditorPanel;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class CollectionEditorContainer extends JPanel {
    public JButton addUncommittedToListButton;
    private TerminologyTable table;

    public class ImportListButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            FileDialog dialog = new FileDialog(new Frame(), "Open file: ");
            dialog.setMode(FileDialog.LOAD);
            dialog.setDirectory(System.getProperty("user.dir"));
            dialog.setVisible(true);
            if (dialog.getFile() != null) {
                ConceptListReader reader = new ConceptListReader();
                reader.setSourceFile(new File(dialog.getDirectory(), dialog.getFile()));

                I_ModelTerminologyList model = (I_ModelTerminologyList) list.getModel();

                for (I_GetConceptData concept : reader) {
                    model.addElement(concept);
                }
            }
        }
    }

    public class ExportListButtonListener implements ActionListener {

        private static final String EXTENSION = ".txt";

        @Override
        public void actionPerformed(ActionEvent arg0) {
            FileDialog dialog = new FileDialog(new Frame(), "Enter file name: ");
            dialog.setMode(FileDialog.SAVE);
            dialog.setDirectory(System.getProperty("user.dir"));
            dialog.setVisible(true);
            if (dialog.getFile() != null) {
                if (dialog.getFile().toLowerCase().endsWith(EXTENSION) == false) {
                    dialog.setFile(dialog.getFile() + EXTENSION);
                }

                ConceptListWriter writer = new ConceptListWriter();
                try {
                    writer.open(new File(dialog.getDirectory(), dialog.getFile()), false);

                    ListModel model = list.getModel();

                    for (int i = 0; i < model.getSize(); i++) {
                        writer.write((I_GetConceptData) model.getElementAt(i));
                    }

                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();

                    final JDialog alert = new JDialog();
                    JPanel panel = new JPanel(new GridLayout(2, 1));
                    panel.add(new JLabel("Failed to write to file " + dialog.getFile() + " due to "
                            + e.getLocalizedMessage()));
                    JButton button = new JButton("OK");
                    button.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            alert.dispose();
                        }
                    });

                    panel.add(button);
                    alert.add(panel);
                }
            }
        }
    }

    public class AddUncommittedToListButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (Ts.get().hasUncommittedChanges()) {
                if (list.getModel().getSize() > 0) {
                    int option = JOptionPane.showConfirmDialog(CollectionEditorContainer.this,
                            "Do you want to erase existing components from the list?", "Erase the list?", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        ((TerminologyListModel) list.getModel()).clear();
                    }
                }
                I_ModelTerminologyList model = (I_ModelTerminologyList) list.getModel();

                for (ConceptChronicleBI concept : Ts.get().getUncommittedConcepts()) {
                    model.addElement((I_GetConceptData) concept);
                }
            } else {
                JOptionPane.showMessageDialog(CollectionEditorContainer.this, "There are no uncommitted concepts");
            }
        }
    }

    public class EraseListActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int option = JOptionPane.showConfirmDialog(CollectionEditorContainer.this,
                    "Are you sure you want to erase the list?", "Erase the list?", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                ((TerminologyListModel) list.getModel()).clear();
            }
        }
    }

 
     int lastDividerLocation = -1;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JComponent listArena;
    private JSplitPane listDetailSplit;
    private JSplitPane listActionSplit;
    private ACE ace;
    private ConceptPanel conceptPanel;
    private TerminologyList list;
    private JTabbedPane bottomTabs = new JTabbedPane();
    private JScrollPane batchResultsScroller;
    private JTextPane batchResults;
    private BatchActionEditorPanel batchActionPanelMain;

    public I_ConfigAceFrame getConfig() {
        return ace.getAceFrameConfig();
    }

    public CollectionEditorContainer(TerminologyList list, ACE ace)
            throws IOException, ClassNotFoundException, NoSuchAlgorithmException, TerminologyException {
        super(new GridBagLayout());
        this.ace = ace;
        this.list = list;


        // SET UP BATCH ACTION PANELS
        batchResults = new JTextPane();
        batchResults.setEditable(false);
        batchResults.setContentType("text/html");
        batchResults.setText("<html>Batch Action Task results will show here.");
        batchActionPanelMain = new BatchActionEditorPanel(ace, list, batchResults);

        batchResultsScroller = new JScrollPane(batchResults);
        conceptPanel = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_LIST_VIEW, ace.aceFrameConfig,
                LINK_TYPE.LIST_LINK, true,
                Integer.MIN_VALUE, ace.getPluginRoot());
        conceptPanel.setAce(ace, LINK_TYPE.LIST_LINK);
        conceptPanel.setLinkedList(list);
        conceptPanel.changeLinkListener(LINK_TYPE.LIST_LINK);
        bottomTabs.addTab("classic view", new ImageIcon(ACE.class.getResource("/16x16/plain/component.png")), 
                conceptPanel, "view list selection using the classic view");
        bottomTabs.addTab("batch action results", new ImageIcon(ACE.class.getResource("/16x16/plain/navigate_check.png")), 
                batchResultsScroller, "view batch action results");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(getListEditorTopPanel(), c);
        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(getListSplit(list, ace), c);
    }
    
public CollectionEditorContainer(TerminologyTable table, ACE ace)
            throws IOException, ClassNotFoundException, NoSuchAlgorithmException, TerminologyException {
        super(new GridBagLayout());
        this.ace = ace;
        this.table = table;
        this.list = table.getList();


        // SET UP BATCH ACTION PANELS
        batchResults = new JTextPane();
        batchResults.setEditable(false);
        batchResults.setContentType("text/html");
        batchResults.setText("<html>Batch Action Task results will show here.");
        batchActionPanelMain = new BatchActionEditorPanel(ace, table.getList(), batchResults);

        batchResultsScroller = new JScrollPane(batchResults);
        conceptPanel = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_LIST_VIEW, ace.aceFrameConfig,
                LINK_TYPE.LIST_LINK, true,
                Integer.MIN_VALUE, ace.getPluginRoot());
        conceptPanel.setAce(ace, LINK_TYPE.LIST_LINK);
        conceptPanel.setLinkedTable(table);
        conceptPanel.changeLinkListener(LINK_TYPE.TABLE_LINK);
        bottomTabs.addTab("classic view", new ImageIcon(ACE.class.getResource("/16x16/plain/component.png")), 
                conceptPanel, "view list selection using the classic view");
        bottomTabs.addTab("batch action results", new ImageIcon(ACE.class.getResource("/16x16/plain/navigate_check.png")), 
                batchResultsScroller, "view batch action results");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(getListEditorTopPanel(), c);
        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(getListSplit(table, ace), c);
    }

    public void setupArena() throws IOException {
        this.listArena = new Arena(ace.getAceFrameConfig(), new File("arena/listView.mxe"));
        bottomTabs.insertTab("arena view", new ImageIcon(ACE.class.getResource("/16x16/plain/eye.png")), 
                listArena, "view list selection using the arena view", 1);
    }

    private JSplitPane getListSplit(JList list, ACE ace) throws IOException, ClassNotFoundException {
        listDetailSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        listDetailSplit.setOneTouchExpandable(true);

        listActionSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        listActionSplit.setLeftComponent(new JScrollPane(list));
        listActionSplit.setDividerLocation(400);
        listActionSplit.setRightComponent(batchActionPanelMain);

        listDetailSplit.setTopComponent(listActionSplit);
        listDetailSplit.setBottomComponent(bottomTabs);
        listDetailSplit.setDividerLocation(3000);
        return listDetailSplit;
    }
    
    private JSplitPane getListSplit(JTable table, ACE ace) throws IOException, ClassNotFoundException {
        listDetailSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        listDetailSplit.setOneTouchExpandable(true);

        listActionSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JScrollPane scroller = new JScrollPane(table);
        listActionSplit.setLeftComponent(scroller);
        listActionSplit.setDividerLocation(400);
        listActionSplit.setRightComponent(batchActionPanelMain);

        listDetailSplit.setTopComponent(listActionSplit);
        listDetailSplit.setBottomComponent(bottomTabs);
        listDetailSplit.setDividerLocation(3000);
        return listDetailSplit;
    }

    private JPanel getListEditorTopPanel() throws IOException, ClassNotFoundException {
        JPanel listEditorTopPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;

        JButton eraseListButton = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/notebook_delete.png")));
        eraseListButton.setVisible(ACE.editMode);
        eraseListButton.addActionListener(new EraseListActionListener());
        eraseListButton.setToolTipText("clear the list");
        listEditorTopPanel.add(eraseListButton, c);

        c.gridx++;

        JButton exportListButton = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/notebook_save.png")));
        exportListButton.setVisible(ACE.editMode);
        exportListButton.addActionListener(new ExportListButtonListener());
        exportListButton.setToolTipText("save the list to a file");
        listEditorTopPanel.add(exportListButton, c);

        c.gridx++;

        JButton importListButton = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/notebook_read.png")));
        importListButton.setVisible(ACE.editMode);
        importListButton.addActionListener(new ImportListButtonListener());
        importListButton.setToolTipText("read a list from a file");
        listEditorTopPanel.add(importListButton, c);

        c.gridx++;

        addUncommittedToListButton = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/question_and_answer.png")));
        addUncommittedToListButton.setVisible(ACE.editMode);
        addUncommittedToListButton.addActionListener(new AddUncommittedToListButtonListener());
        addUncommittedToListButton.setToolTipText("Add concepts with uncommitted components to the list view");
        listEditorTopPanel.add(addUncommittedToListButton, c);

        c.gridx++;

        c.weightx = 1.0;
        listEditorTopPanel.add(new JLabel(" "), c);
        c.gridx++;
        c.weightx = 0.0;

        File componentPluginDir = new File(ace.getPluginRoot() + File.separator + "list");
        File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String fileName) {
                return fileName.toLowerCase().endsWith(".bp");
            }
        });

        if (plugins != null) {
            c.weightx = 0.0;
            c.weightx = 0.0;
            c.fill = GridBagConstraints.NONE;
            for (File f : plugins) {
                try {
                    FileInputStream fis = new FileInputStream(f);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    try {
                        BusinessProcess bp = (BusinessProcess) ois.readObject();
                        byte[] iconBytes = (byte[]) bp.readAttachement("button_icon");
                        if (iconBytes != null) {
                            ImageIcon icon = new ImageIcon(iconBytes);
                            JButton pluginButton = new Button32x32(icon);
                            pluginButton.setToolTipText(bp.getSubject());
                            pluginButton.addActionListener(new PluginListener(f));
                            c.gridx++;
                            listEditorTopPanel.add(pluginButton, c);
                        } else {
                            JButton pluginButton = new Button32x32(bp.getName());
                            pluginButton.setToolTipText(bp.getSubject());
                            pluginButton.addActionListener(new PluginListener(f));
                            c.gridx++;
                            listEditorTopPanel.add(pluginButton, c);
                        }
                    } catch (Exception ex) {
                        TaskFailedException ex2 = new TaskFailedException("Exception processing plugin file: " + f, ex);
                        AceLog.getAppLog().alertAndLogException(ex2);
                    }
                    ois.close();
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        }

        return listEditorTopPanel;

    }

    private class PluginListener implements ActionListener {

        File pluginProcessFile;
        String exceptionMessage;

        private PluginListener(File pluginProcessFile) {
            super();
            this.pluginProcessFile = pluginProcessFile;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                FileInputStream fis = new FileInputStream(pluginProcessFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis);
                final BusinessProcess bp = (BusinessProcess) ois.readObject();
                ois.close();
                getConfig().setStatusMessage("Executing: " + bp.getName());
                final MasterWorker worker = getConfig().getWorker();
                I_GetConceptData concept = null;
                if(table != null){
                    if(table.getSelectedRow()!= -1){
                        int index = table.convertRowIndexToModel(table.getSelectedRow());
                        JList conceptList = getConfig().getBatchConceptList();
                        I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
                        concept = (I_GetConceptData) model.getElementAt(index);
                    }
                }else{
                    // Set concept bean
                    // Set config
                    JList conceptList = getConfig().getBatchConceptList();
                    I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();

                    if (conceptList.getSelectedIndex() != -1) {
                        concept = (I_GetConceptData) model.getElementAt(conceptList.getSelectedIndex());
                    }
                }
                
                worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), getConfig());
                bp.writeAttachment(ProcessAttachmentKeys.I_GET_CONCEPT_DATA.name(), concept);
                worker.writeAttachment(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name(), conceptPanel);
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        I_EncodeBusinessProcess process = bp;
                        try {
                            worker.getLogger().log(
                                    Level.INFO, "Worker: {0} ({1}) executing process: {2}", new Object[]{worker.getWorkerDesc(), worker.getId(), process.getName()});
                            worker.execute(process);
                            SortedSet<ExecutionRecord> sortedRecords = new TreeSet<ExecutionRecord>(
                                    process.getExecutionRecords());
                            Iterator<ExecutionRecord> recordItr = sortedRecords.iterator();
                            StringBuilder buff = new StringBuilder();
                            while (recordItr.hasNext()) {
                                ExecutionRecord rec = recordItr.next();
                                buff.append("\n");
                                buff.append(rec.toString());
                            }
                            worker.getLogger().info(buff.toString());
                            exceptionMessage = "";
                        } catch (Throwable e1) {
                            worker.getLogger().log(Level.WARNING, e1.toString(), e1);
                            exceptionMessage = e1.toString();
                        }
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                getConfig().setStatusMessage("<html><font color='#006400'>execute");
                                I_GetConceptData conceptInPanel = (I_GetConceptData) conceptPanel.getTermComponent();
                                conceptPanel.setTermComponent(null);
                                conceptPanel.setTermComponent(conceptInPanel);
                                if (exceptionMessage.equals("")) {
                                    getConfig().setStatusMessage(
                                            "<html>Execution of <font color='blue'>" + bp.getName() + "</font> complete.");
                                } else {
                                    getConfig().setStatusMessage(
                                            "<html><font color='blue'>Process complete: <font color='red'>"
                                            + exceptionMessage);
                                }
                            }
                        });
                    }
                };
                new Thread(r, "Collection editor container").start();
            } catch (Exception e1) {
                getConfig().setStatusMessage("Exception during execution.");
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }
    }

    public ConceptPanel getConceptPanel() {
        return conceptPanel;
    }
}
