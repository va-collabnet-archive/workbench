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

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_HostConceptPlugins.HOST_ENUM;
import org.dwfa.ace.api.I_HostConceptPlugins.LINK_TYPE;
import org.dwfa.ace.file.ConceptListReader;
import org.dwfa.ace.file.ConceptListWriter;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.gui.button.Button32x32;
import org.dwfa.gui.toggle.Toggle32x32;

import com.sleepycat.je.DatabaseException;

public class CollectionEditorContainer extends JPanel {

    public class ImportListButtonListener implements ActionListener {

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

    public class EraseListActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int option = JOptionPane.showConfirmDialog(CollectionEditorContainer.this,
                "Are you sure you want to erase the list?", "Erase the list?", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                ((TerminologyListModel) list.getModel()).clear();
            }
        }

    }

    private class ShowComponentActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (showComponentView.isSelected()) {
                showProcessBuilder.setSelected(false);
                listSplit.setBottomComponent(conceptPanel);
                if (lastDividerLocation > 0) {
                    listSplit.setDividerLocation(lastDividerLocation);
                } else {
                    listSplit.setDividerLocation(0.30);
                }
            }
            if (showOnlyList()) {
                showListOnly();
            }
        }

    }

    private class ShowProcessBuilderActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (showProcessBuilder.isSelected()) {
                showComponentView.setSelected(false);
                listSplit.setBottomComponent(processBuilder);
                if (lastDividerLocation > 0) {
                    listSplit.setDividerLocation(lastDividerLocation);
                } else {
                    listSplit.setDividerLocation(0.30);
                }
            }
            if (showOnlyList()) {
                showListOnly();
            }
        }

    }

    private void showListOnly() {
        int dividerLocation = listSplit.getDividerLocation();
        if (dividerLocation != 3000) {
            lastDividerLocation = dividerLocation;
            listSplit.setBottomComponent(new JPanel());
            listSplit.setDividerLocation(3000);
        }
    }

    private boolean showOnlyList() {
        return showComponentView.isSelected() == false && showProcessBuilder.isSelected() == false;
    }

    int lastDividerLocation = -1;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JComponent processBuilder;
    private JToggleButton showComponentView;
    private JToggleButton showProcessBuilder;
    private JSplitPane listSplit;
    private ACE ace;
    private ConceptPanel conceptPanel;
    private ShowComponentActionListener showComponentActionListener;
    private TerminologyList list;

    public I_ConfigAceFrame getConfig() {
        return ace.getAceFrameConfig();
    }

    public CollectionEditorContainer(TerminologyList list, ACE ace, JPanel descListProcessBuilderPanel)
            throws DatabaseException, IOException, ClassNotFoundException, NoSuchAlgorithmException {
        super(new GridBagLayout());
        this.ace = ace;
        this.list = list;
        this.processBuilder = descListProcessBuilderPanel;
        conceptPanel = new ConceptPanel(HOST_ENUM.CONCEPT_PANEL_LIST_VIEW, ace, LINK_TYPE.LIST_LINK, true,
            Integer.MIN_VALUE);
        conceptPanel.setLinkedList(list);
        conceptPanel.changeLinkListener(LINK_TYPE.LIST_LINK);
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
        showComponentView.setSelected(true);
        showComponentActionListener.actionPerformed(new ActionEvent(showComponentView, 0, "show"));
    }

    private JSplitPane getListSplit(JList list, ACE ace) throws DatabaseException, IOException, ClassNotFoundException {
        listSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        listSplit.setOneTouchExpandable(true);
        listSplit.setTopComponent(new JScrollPane(list));
        listSplit.setBottomComponent(conceptPanel);
        listSplit.setDividerLocation(3000);
        return listSplit;
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
        showComponentView = new Toggle32x32(new ImageIcon(ACE.class.getResource("/32x32/plain/component.png")));
        showComponentActionListener = new ShowComponentActionListener();
        showComponentView.setVisible(true);
        showComponentView.addActionListener(showComponentActionListener);
        showComponentView.setToolTipText("Show component view associated with list view");
        listEditorTopPanel.add(showComponentView, c);
        c.gridx++;
        showProcessBuilder = new Toggle32x32(new ImageIcon(ACE.class.getResource("/32x32/plain/cube_molecule.png")));
        listEditorTopPanel.add(showProcessBuilder, c);
        showProcessBuilder.setVisible(ACE.editMode);
        showProcessBuilder.setToolTipText("Show process builder associated with list view");
        showProcessBuilder.addActionListener(new ShowProcessBuilderActionListener());
        c.gridx++;

        JButton eraseListButton = new JButton(new ImageIcon(ACE.class.getResource("/32x32/plain/notebook_delete.png")));
        eraseListButton.setVisible(true);
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

        c.weightx = 1.0;
        listEditorTopPanel.add(new JLabel(" "), c);
        c.gridx++;
        c.weightx = 0.0;

        File componentPluginDir = new File(ace.getPluginRoot() + File.separator + "list");
        File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {
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
                            AceLog.getAppLog().info("adding collection plugin: " + f.getName());
                        } else {
                            JButton pluginButton = new Button32x32(bp.getName());
                            pluginButton.setToolTipText(bp.getSubject());
                            pluginButton.addActionListener(new PluginListener(f));
                            c.gridx++;
                            listEditorTopPanel.add(pluginButton, c);
                            AceLog.getAppLog().info("adding collection plugin: " + f.getName());
                        }
                    } catch (Exception ex) {
                        TaskFailedException ex2 = new TaskFailedException("Exception processing file: " + f, ex);
                        AceLog.getAppLog().alertAndLogException(ex2);
                    }
                    ois.close();
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        }

        listEditorTopPanel.add(new Toggle32x32(new ImageIcon(ACE.class.getResource("/32x32/plain/branch_delete.png"))),
            c);
        return listEditorTopPanel;

    }

    private class PluginListener implements ActionListener {
        File pluginProcessFile;
        String exceptionMessage;

        private PluginListener(File pluginProcessFile) {
            super();
            this.pluginProcessFile = pluginProcessFile;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                FileInputStream fis = new FileInputStream(pluginProcessFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis);
                final BusinessProcess bp = (BusinessProcess) ois.readObject();
                ois.close();
                getConfig().setStatusMessage("Executing: " + bp.getName());
                final MasterWorker worker = getConfig().getWorker();
                // Set concept bean
                // Set config
                JList conceptList = getConfig().getBatchConceptList();
                I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();

                I_GetConceptData concept = null;
                if (conceptList.getSelectedIndex() != -1) {
                    concept = (I_GetConceptData) model.getElementAt(conceptList.getSelectedIndex());
                }

                worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), getConfig());
                bp.writeAttachment(ProcessAttachmentKeys.I_GET_CONCEPT_DATA.name(), concept);
                worker.writeAttachment(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name(), conceptPanel);
                Runnable r = new Runnable() {
                    public void run() {
                        I_EncodeBusinessProcess process = bp;
                        try {
                            worker.getLogger().info(
                                "Worker: " + worker.getWorkerDesc() + " (" + worker.getId() + ") executing process: "
                                    + process.getName());
                            worker.execute(process);
                            SortedSet<ExecutionRecord> sortedRecords = new TreeSet<ExecutionRecord>(
                                process.getExecutionRecords());
                            Iterator<ExecutionRecord> recordItr = sortedRecords.iterator();
                            StringBuffer buff = new StringBuffer();
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
                new Thread(r).start();
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
