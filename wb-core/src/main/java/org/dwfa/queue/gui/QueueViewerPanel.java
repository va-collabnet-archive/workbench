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



/*
* Created on Apr 25, 2005
 */
package org.dwfa.queue.gui;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.gui.ProcessPanel;
import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_DescribeQueueEntry;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.util.SortClickListener;
import org.dwfa.queue.ObjectServerCore;
import org.dwfa.swing.SwingWorker;

import org.ihtsdo.ttk.lookup.InstanceWrapper;
import org.ihtsdo.ttk.lookup.LookupService;

import org.openide.util.Lookup;

//~--- JDK imports ------------------------------------------------------------

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.rmi.RemoteException;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author kec
 *
 */
public class QueueViewerPanel extends JPanel {

    /**
     *
     */
    private static final long            serialVersionUID       = 1L;
    private static final Logger          logger                 = Logger.getLogger(QueueViewerPanel.class.getName());;
    MoveToDiskActionListener             moveListener           = new MoveToDiskActionListener();
    JSplitPane                           splitPane              = new JSplitPane();
    JSplitPane                           queueContentsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private JButton                      execute                = new JButton("execute");
    private JButton                      delete                = new JButton("delete");
    private JButton                      refresh                = new JButton("<html><font color='#006400'>refresh");
    private JLabel                       statusMessage          = new JLabel();
    private I_Work                       worker;
    I_EncodeBusinessProcess              process;
    EntryID                              processEntryID;
    private JTable                       tableOfQueues;
    private ListOfQueuesTableModel       tableOfQueuesModel;
    QueueTableModel                      tableOfQueueEntriesModel;
    JTable                               tableOfQueueEntries;
    private ExecuteProcessActionListener executeActionListener;
    private DeleteProcessActionListener deleteActionListener;
    private I_QueueProcesses             selectedQueue;

    public QueueViewerPanel(I_Work worker) throws Exception {
        super(new GridBagLayout());
        this.worker = worker;
        this.worker.addPropertyChangeListener("executing", new ExecutionPropertyChangeListener());
        this.worker.addPropertyChangeListener("executing", new DeletePropertyChangeListener());
        this.splitPane.setLeftComponent(createTableOfQueues());
        this.splitPane.setRightComponent(this.queueContentsSplitPane);
        this.splitPane.setDividerLocation(250);
        this.splitPane.setOneTouchExpandable(true);
        this.queueContentsSplitPane.setTopComponent(new JPanel());
        this.queueContentsSplitPane.setBottomComponent(new JLabel("No process selected."));
        this.queueContentsSplitPane.setDividerLocation(225);
        this.queueContentsSplitPane.setOneTouchExpandable(true);
        this.refresh.addActionListener(new RefreshListener());

        // splitPane.setDividerLocation(0);
        GridBagConstraints c = new GridBagConstraints();

        c.anchor  = GridBagConstraints.NORTHWEST;
        c.fill    = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx   = 0;
        c.gridy   = 0;
        this.add(this.splitPane, c);
        this.execute.setEnabled(false);
        this.executeActionListener = new ExecuteProcessActionListener(UUID.randomUUID(), worker);
        this.execute.addActionListener(executeActionListener);
        this.delete.setEnabled(false);
        this.deleteActionListener = new DeleteProcessActionListener(worker);
        this.delete.addActionListener(deleteActionListener);

        JPanel statusPanel = makeStatusPanel(this.statusMessage, refresh, execute, delete);

        c.weighty = 0;
        c.gridy   = 1;
        c.fill    = GridBagConstraints.HORIZONTAL;
        this.add(statusPanel, c);
        getQueues();
        ObjectServerCore.addCommitListener(new CommitListener());
    }

    private void setupExecuteButton() {
        if (worker.isExecuting()) {
            execute.setText("execute");
            execute.setEnabled(false);
        } else {
            if (process != null) {
                execute.setText("<html><font color='#006400'>execute");
                execute.setEnabled(true);
            }
        }
    }
    
    private void setupDeleteButton() {
        if (worker.isExecuting()) {
            delete.setText("delete");
            delete.setEnabled(false);
        } else {
            if (process != null) {
                delete.setText("<html><font color='#006400'>delete");
                delete.setEnabled(true);
            }
        }
    }

    private JPanel makeStatusPanel(JLabel statusMessage, JButton refresh, JButton execute, JButton delete) {
        JPanel             panel = new JPanel(new GridBagLayout());
        GridBagConstraints c     = new GridBagConstraints();

        c.anchor  = GridBagConstraints.WEST;
        c.fill    = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;
        c.gridy   = 0;
        c.gridx   = 7;
        panel.add(new JLabel("    "), c);    // filler for grow box.
        c.gridx = 6;
        panel.add(refresh, c);
        c.gridx = 5;
        panel.add(delete, c);
        c.gridx = 4;
        panel.add(execute, c);
        c.gridx   = 3;
        c.weightx = 1;
        c.fill    = GridBagConstraints.HORIZONTAL;
        panel.add(statusMessage, c);
        c.weightx = 0;
        c.gridx   = 2;
        c.weightx = 0;
        c.fill    = GridBagConstraints.NONE;
        panel.add(new JLabel("   "), c);
        c.gridx = 1;

        // ButtonGroup group = new ButtonGroup();
        // JRadioButton taskButton = new JRadioButton("task");
        // taskButton.setSelected(true);
        // taskButton.addActionListener(new ChangeToTaskActionListener());
        // group.add(taskButton);
        panel.add(new JPanel(), c);
        c.gridx = 0;

        // JRadioButton dataButton = new JRadioButton("data");
        // dataButton.setSelected(false);
        // group.add(dataButton);
        // dataButton.addActionListener(new ChangeToDataActionListener());
        panel.add(new JPanel(), c);

        return panel;
    }

    private JScrollPane createTableOfQueues() {
        tableOfQueuesModel = new ListOfQueuesTableModel();
        tableOfQueues      = new JTable(tableOfQueuesModel);
        SortClickListener.setupSorter(tableOfQueues);

        // Set up tool tips for column headers.
        tableOfQueues.getTableHeader().setToolTipText("Click to specify sorting");

        // Create a tableOfQueues that allows one selection at a time.
        QueueSelectionListener qmsl = new QueueSelectionListener();

        tableOfQueues.getSelectionModel().addListSelectionListener(qmsl);
        tableOfQueues.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tableOfQueues.getRowSorter().addRowSorterListener(qmsl);
        ToolTipManager.sharedInstance().registerComponent(tableOfQueues);

        return new JScrollPane(tableOfQueues);
    }

    /**
     * @param queue
     * @param queueName
     */
    public void addQueue(I_QueueProcesses queue, String queueName, UUID id) {
        QueueAdaptor qAdaptor = new QueueAdaptor(queue, queueName, id);

        this.tableOfQueuesModel.addQueue(qAdaptor);
    }

    public void refreshQueues() {
        this.tableOfQueues.clearSelection();
        this.tableOfQueuesModel.clear();
        getQueues();
    }

    /**
     * @return Returns the moveListener.
     */
    public MoveToDiskActionListener getMoveListener() {
        return moveListener;
    }

    public I_Work getWorker() {
        return worker;
    }

    /**
     * @param queue
     * @throws RemoteException
     * @throws IOException
     */
    private void updateQueueModelAndDisplay(final I_QueueProcesses queue) throws RemoteException, IOException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (selectedQueue != queue) {
                        selectedQueue            = queue;
                        tableOfQueueEntriesModel = new QueueTableModel(queue);
                        tableOfQueueEntries      = new JTable(tableOfQueueEntriesModel);
                        SortClickListener.setupSorter(tableOfQueueEntries);
                        tableOfQueueEntries.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                "executeTask");
                        tableOfQueueEntries.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                "executeTask");
                        tableOfQueueEntries.getActionMap().put("executeTask", new ExecuteAction());

                        // Set up tool tips for column headers.
                        tableOfQueueEntries.getTableHeader().setToolTipText("Click to specify sorting");
                        tableOfQueueEntries.getSelectionModel().addListSelectionListener(
                            new ProcessSelectionActionListener(tableOfQueueEntries));
                        queueContentsSplitPane.setTopComponent(new JScrollPane(tableOfQueueEntries));

                        int dividerLoc = queueContentsSplitPane.getDividerLocation();

                        queueContentsSplitPane.setBottomComponent(new JLabel("No process is selected"));
                        queueContentsSplitPane.setDividerLocation(dividerLoc);
                    } else {
                        EntryID selectedEntry = null;
                        int     selectedRow   = tableOfQueueEntries.getSelectedRow();

                        if (selectedRow > -1) {
                            try {
                                int convertedRow =
                                    tableOfQueueEntries.getRowSorter().convertRowIndexToModel(selectedRow);

                                if ((convertedRow >= 0) && (convertedRow < tableOfQueueEntries.getRowCount())) {
                                    selectedEntry = (EntryID) tableOfQueueEntries.getModel().getValueAt(convertedRow,
                                            6);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                logger.log(Level.WARNING, "{0} selected row: {1}", new Object[] { e.toString(),
                                        selectedRow });
                            }
                        }

                        tableOfQueueEntriesModel.updateQueueData();

                        if (selectedEntry != null) {
                            boolean entryFound = false;

                            for (int row = 0; row < tableOfQueueEntries.getRowCount(); row++) {
                                EntryID entry = (EntryID) tableOfQueueEntries.getModel().getValueAt(
                                                    tableOfQueueEntries.getRowSorter().convertRowIndexToModel(row), 6);

                                if (entry.equals(selectedEntry)) {
                                    tableOfQueueEntries.getSelectionModel().addSelectionInterval(row, row);
                                    entryFound = true;

                                    break;
                                }
                            }

                            if (entryFound == false) {
                                tableOfQueueEntries.getSelectionModel().setSelectionInterval(0, 0);
                            } else {
                                execute.setEnabled(worker.isExecuting() == false);
                            }
                        } else {
                            tableOfQueueEntries.getSelectionModel().setSelectionInterval(0, 0);
                        }

                        tableOfQueueEntries.requestFocusInWindow();
                    }
                } catch (Exception e) {

                    // TODO Auto-generated catch block
                    logger.log(Level.SEVERE, e.toString(), e);
                }
            }
        });
    }

    public void requestFocusOnEntry() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (tableOfQueueEntries != null) {
                    tableOfQueueEntries.requestFocusInWindow();
                }
            }
        });
    }

    public JTable getTableOfQueues() {
        return tableOfQueues;
    }

    public final void getQueues() {
        Lookup.Template lt           = new Lookup.Template<>(I_QueueProcesses.class, null, null);
        Lookup.Result   lookupResult = LookupService.get().lookup(lt);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.INFO, "Found {0} matching I_QueueProcesses services...", lookupResult.allItems().size());
        }

        for (Object itemObject : lookupResult.allItems()) {
            InstanceWrapper item = (InstanceWrapper) itemObject;

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "found: {0}", item.toString());
            }

            I_QueueProcesses queue = (I_QueueProcesses) item.getInstance();

            QueueViewerPanel.this.addQueue(queue, item.getDisplayName(), UUID.fromString(item.getId()));
        }
    }

    private class CommitListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent arg0) {
            QueueAdaptor qAdaptor = null;

            if (tableOfQueues.getSelectedRow() >= 0) {
                ListOfQueuesTableModel loqtm = (ListOfQueuesTableModel) tableOfQueues.getModel();

                qAdaptor = loqtm.getQueueAt(
                    tableOfQueues.getRowSorter().convertRowIndexToModel(tableOfQueues.getSelectedRow()));
            }

            // System.out.println(" Transaction committed. Performing queue refresh. ");
            if (qAdaptor == null) {

                // nothing to do...
            } else {
                try {
                    updateQueueModelAndDisplay(qAdaptor.queue);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    public class ExecuteAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent evt) {
            executeActionListener.actionPerformed(evt);
        }
    }


    private class ExecuteProcessActionListener implements ActionListener {
        String       exceptionMessage = "";
        List<I_Work> cloneList        = new ArrayList<>();
        @SuppressWarnings("unused")
        UUID         id;
        I_Work       worker;
        boolean      indexOutOfBoundsExceptionThrown;

        /**
         * @param config
         * @param id
         * @param worker
         * @param frames
         */
        public ExecuteProcessActionListener(UUID id, I_Work worker) {
            super();
            this.id     = id;
            this.worker = worker;
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            execute.setText("execute");
            execute.setEnabled(false);
            statusMessage.setText("<html><font color='red'>Executing process");

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    ListSelectionModel lsm              = tableOfQueueEntries.getSelectionModel();
                    int                firstSelectedRow = lsm.getMinSelectionIndex();
                    int                lastSelectedRow  = lsm.getMaxSelectionIndex();

                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "firstSelectedRow: {0} lastSelectedRow: {1}",
                                   new Object[] { firstSelectedRow,
                                                  lastSelectedRow });
                    }

                    List<I_DescribeQueueEntry> selectedProcesses = new ArrayList<>();

                    indexOutOfBoundsExceptionThrown = false;

                    for (int i = firstSelectedRow; i <= lastSelectedRow; i++) {
                        if (lsm.isSelectedIndex(i)) {
                            try {
                                I_DescribeQueueEntry processMeta = tableOfQueueEntriesModel.getRowMetaData(i);

                                selectedProcesses.add(processMeta);

                                if (logger.isLoggable(Level.FINE)) {
                                    logger.log(Level.FINE, "selectedProcesses: {0}", processMeta);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                logger.log(Level.WARNING, e.toString(), e);
                                indexOutOfBoundsExceptionThrown = true;
                            }
                        }
                    }

                    Iterator<I_DescribeQueueEntry> selectionItr = selectedProcesses.iterator();

                    while (selectionItr.hasNext()) {
                        I_DescribeQueueEntry processMeta = selectionItr.next();
                        final String         message     = "<html><font color='red'>Executing process: "
                                                           + "<font color='blue'>" + processMeta.getName();

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                statusMessage.setText(message);
                            }
                        });

                        try {
                            I_EncodeBusinessProcess processToExecute =
                                tableOfQueueEntriesModel.getQueue().take(processMeta.getEntryID(),
                                    worker.getActiveTransaction());

                            if (worker.isExecuting()) {
                                I_Work altWorker = null;

                                for (I_Work alt : cloneList) {
                                    if (alt.isExecuting() == false) {
                                        altWorker = alt;

                                        break;
                                    }
                                }

                                if (altWorker == null) {
                                    altWorker = worker.getTransactionIndependentClone();
                                    cloneList.add(altWorker);
                                }

                                altWorker.execute(processToExecute);
                            } else {
                                worker.execute(processToExecute);
                            }

                            if (logger.isLoggable(Level.FINE)) {
                                SortedSet<ExecutionRecord> sortedRecords =
                                    new TreeSet<>(processToExecute.getExecutionRecords());
                                Iterator<ExecutionRecord> recordItr = sortedRecords.iterator();
                                StringBuilder             buff      = new StringBuilder();

                                while (recordItr.hasNext()) {
                                    ExecutionRecord rec = recordItr.next();

                                    buff.append("\n");
                                    buff.append(rec.toString());
                                }

                                logger.fine(buff.toString());
                            }

                            exceptionMessage = "";
                        } catch (Throwable e1) {
                            logger.log(Level.WARNING, e1.toString(), e1);
                            exceptionMessage = e1.toString();
                        }
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            setupExecuteButton();
                            setupDeleteButton();

                            if (exceptionMessage.equals("")) {
                                statusMessage.setText("<html><font color='blue'>Process complete");
                            } else {
                                statusMessage.setText("<html><font color='blue'>Process complete: <font color='red'>"
                                                      + exceptionMessage);
                            }

                            refresh.doClick();

                            if (indexOutOfBoundsExceptionThrown) {
                                try {
                                    updateQueueModelAndDisplay(tableOfQueueEntriesModel.getQueue());
                                } catch (Exception e) {
                                    logger.log(Level.WARNING, e.toString(), e);
                                }
                            }

                            ListSelectionModel lsm = tableOfQueueEntries.getSelectionModel();

                            lsm.setSelectionInterval(0, 0);
                            execute.requestFocusInWindow();
                        }
                    });
                }
            };

            new Thread(r, this.getClass().getCanonicalName()).start();
        }
    }
    
    
    private class ExecutionPropertyChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setupExecuteButton();
        }
    }
    
    private class DeletePropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setupDeleteButton();
        }
    }

    private class DeleteProcessActionListener implements ActionListener {

        String exceptionMessage = "";
        List<I_Work> cloneList = new ArrayList<>();
        @SuppressWarnings("unused")
        UUID id;
        I_Work worker;
        boolean indexOutOfBoundsExceptionThrown;

        /**
         * @param config
         * @param id
         * @param worker
         * @param frames
         */
        public DeleteProcessActionListener(I_Work worker) {
            super();
            this.id = id;
            this.worker = worker;
        }

        /**
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            
            int option = JOptionPane.showConfirmDialog(queueContentsSplitPane, "Are you sure you want to delete this item from the inbox?", "Delete?", JOptionPane.YES_NO_OPTION);

            switch (option) {
                case JOptionPane.YES_OPTION:
                    delete.setText("delete");
                    delete.setEnabled(false);
                    indexOutOfBoundsExceptionThrown = false;
                    ListSelectionModel lsm = tableOfQueueEntries.getSelectionModel();
                    int firstSelectedRow = lsm.getMinSelectionIndex();
                    int lastSelectedRow = lsm.getMaxSelectionIndex();

                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "firstSelectedRow: {0} lastSelectedRow: {1}",
                                new Object[]{firstSelectedRow,
                            lastSelectedRow});
                    }

                    List<I_DescribeQueueEntry> selectedProcesses = new ArrayList<>();
                    for (int i = firstSelectedRow; i <= lastSelectedRow; i++) {
                        if (lsm.isSelectedIndex(i)) {
                            try {
                                I_DescribeQueueEntry processMeta = tableOfQueueEntriesModel.getRowMetaData(i);

                                selectedProcesses.add(processMeta);

                                if (logger.isLoggable(Level.FINE)) {
                                    logger.log(Level.FINE, "selectedProcesses: {0}", processMeta);
                                }
                            } catch (IndexOutOfBoundsException ex) {
                                logger.log(Level.WARNING, ex.toString(), ex);
                                indexOutOfBoundsExceptionThrown = true;
                            }
                        }
                    }

                    Iterator<I_DescribeQueueEntry> selectionItr = selectedProcesses.iterator();

                    while (selectionItr.hasNext()) {
                        I_DescribeQueueEntry processMeta = selectionItr.next();

                        try {
                            tableOfQueueEntriesModel.getQueue().delete(processMeta.getEntryID(),
                                    worker.getActiveTransaction());

                        } catch (Throwable e1) {
                            logger.log(Level.WARNING, e1.toString(), e1);
                            exceptionMessage = e1.toString();
                        }
                    }
                    refresh.doClick();
                    lsm.setSelectionInterval(0, 0);
                    break;
                case JOptionPane.NO_OPTION:
                    return;
            }
        }
    }
    
    public class MoveToDiskActionListener implements ActionListener {

        /**
         * @param config
         * @param id
         * @param worker
         * @param frames
         */
        public MoveToDiskActionListener() {
            super();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            // Create a file dialog box to prompt for a new file to display
            JFileChooser chooser = new JFileChooser();

            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select directory to put selected processes");

            int returnVal = chooser.showDialog(QueueViewerPanel.this.getTopLevelAncestor(), "Select Directory");

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                final File directory = chooser.getSelectedFile();

                execute.setEnabled(false);

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        ListSelectionModel         lsm               = tableOfQueueEntries.getSelectionModel();
                        int                        firstSelectedRow  = lsm.getMinSelectionIndex();
                        int                        lastSelectedRow   = lsm.getMaxSelectionIndex();
                        List<I_DescribeQueueEntry> selectedProcesses = new ArrayList<>();

                        for (int i = firstSelectedRow; i <= lastSelectedRow; i++) {
                            if (lsm.isSelectedIndex(i)) {
                                I_DescribeQueueEntry processMeta =
                                    tableOfQueueEntriesModel.getRowMetaData(
                                        tableOfQueueEntries.getRowSorter().convertRowIndexToModel(i));

                                selectedProcesses.add(processMeta);
                            }
                        }

                        Iterator<I_DescribeQueueEntry> selectionItr = selectedProcesses.iterator();

                        while (selectionItr.hasNext()) {
                            I_DescribeQueueEntry processMeta = selectionItr.next();
                            final String         message     = "<html><font color='red'>Moving process to disk: "
                                                               + "<font color='blue'>" + processMeta.getName();

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    statusMessage.setText(message);
                                }
                            });

                            try {
                                I_EncodeBusinessProcess processToExecute =
                                    tableOfQueueEntriesModel.getQueue().take(processMeta.getEntryID(),
                                        worker.getActiveTransaction());

                                // write to disk here
                                File processFile = new File(directory,
                                                            processMeta.getName() + " - " + processMeta.getProcessID()
                                                            + "." + processMeta.getEntryID() + ".bp");
                                FileOutputStream     fos = new FileOutputStream(processFile);
                                BufferedOutputStream bos = new BufferedOutputStream(fos);

                                try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                                    oos.writeObject(processToExecute);
                                }

                                worker.commitActiveTransaction();
                            } catch (Throwable e1) {
                                logger.log(Level.WARNING, e1.toString(), e1);
                            }
                        }

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setupExecuteButton();
                                setupDeleteButton();
                                statusMessage.setText("<html><font color='blue'>Take complete");
                                refresh.doClick();
                            }
                        });
                    }
                };

                new Thread(r, this.getClass().getCanonicalName()).start();
            }
        }
    }


    private class ProcessSelectionActionListener implements ListSelectionListener {
        private JTable table;

        public ProcessSelectionActionListener(JTable table) {
            this.table = table;
        }

        /**
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        @Override
        public void valueChanged(ListSelectionEvent ev) {

            // Ignore extra messages.
            if (ev.getValueIsAdjusting()) {
                return;
            }

            ListSelectionModel lsm = (ListSelectionModel) ev.getSource();

            if (lsm.isSelectionEmpty()) {

                // no rows are selected
                int dividerLoc = queueContentsSplitPane.getDividerLocation();

                queueContentsSplitPane.setBottomComponent(new JLabel("No process is selected"));
                queueContentsSplitPane.setDividerLocation(dividerLoc);
                execute.setText("execute");
                execute.setEnabled(false);
                process = null;
            } else {
                try {
                    tableOfQueueEntriesModel.updateQueueData();

                    if ((lsm.getMinSelectionIndex() > -1) && (lsm.getMinSelectionIndex() < table.getRowCount())) {
                        int firstSelectedRow = lsm.getMinSelectionIndex();
                        int modelIndex       = table.getRowSorter().convertRowIndexToModel(firstSelectedRow);

                        if ((modelIndex > -1) && (modelIndex < tableOfQueueEntriesModel.getRowCount())) {
                            I_DescribeQueueEntry processMeta = tableOfQueueEntriesModel.getRowMetaData(modelIndex);

                            if (processMeta != null) {
                                processEntryID = processMeta.getEntryID();

                                try {
                                    process = tableOfQueueEntriesModel.getQueue().read(processMeta.getEntryID(), null);

                                    ProcessPanel processPanel = new ProcessPanel(process, worker, null);
                                    int          dividerLoc   = queueContentsSplitPane.getDividerLocation();

                                    queueContentsSplitPane.setBottomComponent(new JScrollPane(processPanel));
                                    queueContentsSplitPane.setDividerLocation(dividerLoc);
                                    setupExecuteButton();
                                    setupDeleteButton();
                                } catch (NoMatchingEntryException ex) {
                                    logger.log(Level.INFO, " NoMatchingEntry: {0}", ex);
                                    lsm.clearSelection();

                                    int dividerLoc = queueContentsSplitPane.getDividerLocation();

                                    queueContentsSplitPane.setBottomComponent(new JLabel("No matching entry"));
                                    queueContentsSplitPane.setDividerLocation(dividerLoc);
                                    execute.setText("execute");
                                    execute.setEnabled(false);
                                    process = null;
                                }
                            }
                        }
                    } else {
                        lsm.clearSelection();

                        int dividerLoc = queueContentsSplitPane.getDividerLocation();

                        queueContentsSplitPane.setBottomComponent(new JLabel("No selected entry"));
                        queueContentsSplitPane.setDividerLocation(dividerLoc);
                        execute.setText("execute");
                        execute.setEnabled(false);
                        process = null;
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);

                    int dividerLoc = queueContentsSplitPane.getDividerLocation();

                    queueContentsSplitPane.setBottomComponent(new JLabel(ex.getMessage()));
                    queueContentsSplitPane.setDividerLocation(dividerLoc);
                }
            }
        }
    }


    private class QueueSelectionListener implements ListSelectionListener, RowSorterListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            handleChange();
        }

        @Override
        public void sorterChanged(RowSorterEvent e) {
            handleChange();
        }

        private void handleChange() {
            if (tableOfQueues.getSelectedRow() >= 0) {
                ListOfQueuesTableModel tableModel = (ListOfQueuesTableModel) tableOfQueues.getModel();
                QueueAdaptor           qAdaptor   = tableModel.getQueueAt(
                                                        tableOfQueues.getRowSorter().convertRowIndexToModel(
                                                            tableOfQueues.getSelectedRow()));

                if (qAdaptor == null) {
                    clearQueuePanel();
                } else {
                    try {
                        updateQueueModelAndDisplay(qAdaptor.queue);
                    } catch (Exception e1) {
                        int dividerLoc = queueContentsSplitPane.getDividerLocation();

                        queueContentsSplitPane.setTopComponent(new JPanel());
                        queueContentsSplitPane.setBottomComponent(
                            new JLabel("<html>No process is selected secondary to exception: <br>" + e1.toString()));
                        queueContentsSplitPane.setDividerLocation(dividerLoc);
                        logger.log(Level.WARNING, e1.toString(), e1);
                    }
                }
            } else {
                clearQueuePanel();
            }
        }

        private void clearQueuePanel() {
            int dividerLoc = queueContentsSplitPane.getDividerLocation();

            queueContentsSplitPane.setTopComponent(new JPanel());
            queueContentsSplitPane.setBottomComponent(new JLabel("No process is selected"));
            queueContentsSplitPane.setDividerLocation(dividerLoc);
        }
    }


    private class RefreshListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent arg0) {
            RefreshObjectServers ros = new RefreshObjectServers();

            ros.start();
        }
    }


    public class RefreshObjectServers extends SwingWorker<Boolean> {
        @Override
        protected Boolean construct() throws Exception {
            ObjectServerCore.refreshServers();

            return true;
        }

        @Override
        protected void finished() {
            try {
                get();

                QueueAdaptor qAdaptor = null;

                if (tableOfQueues.getSelectedRow() >= 0) {
                    ListOfQueuesTableModel loqtm = (ListOfQueuesTableModel) tableOfQueues.getModel();

                    qAdaptor = loqtm.getQueueAt(
                        tableOfQueues.getRowSorter().convertRowIndexToModel(tableOfQueues.getSelectedRow()));
                }

                if (qAdaptor == null) {

                    // nothing to do...
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Refresh, but qAdaptor is null");
                    }
                } else {
                    try {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("Starting refresh for qAdaptor");
                        }

                        updateQueueModelAndDisplay(qAdaptor.queue);
                    } catch (Exception e1) {
                        logger.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
                    }
                }
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            } catch (ExecutionException e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
    }
}
