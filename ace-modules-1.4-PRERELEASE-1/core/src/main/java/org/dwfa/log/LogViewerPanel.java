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
 * Created on Apr 23, 2005
 */
package org.dwfa.log;

import java.awt.GridLayout;
import java.rmi.RemoteException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.security.TrustVerifier;

import com.sun.jini.start.LifeCycle;

/**
 * @author kec
 * 
 */
public class LogViewerPanel extends JPanel implements TableModelListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private HtmlHandler logHandler;
    JTabbedPane logViewerTabs;
    /** The server proxy, for use by getProxyVerifier */
    protected I_PublishLogRecord serverProxy;
    private static Logger logger = Logger.getLogger(LogViewerPanel.class.getName());
    /**
     * Cache of our <code>LifeCycle</code> object
     * TODO implement the lifeCycle
     * destroy methods. See TxnManagerImpl for an example.
     */
    @SuppressWarnings("unused")
    private LifeCycle lifeCycle = null;

    /** The configuration to use for configuring the server */
    protected final Configuration config;

    private List<JTable> logTables = new ArrayList<JTable>();

    /**
     * @throws Exception
     * 
     */
    public LogViewerPanel(String[] args, LifeCycle lc) throws Exception {
        super(new GridLayout(1, 1));
        logger.info("\n*******************\n\n" + "Starting " + this.getClass().getSimpleName() + " with config file: "
            + Arrays.asList(args) + "\n\n******************\n");
        this.config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());
        this.lifeCycle = lc;
        LogManager logManager = LogManager.getLogManager();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        logViewerTabs = new JTabbedPane();
        splitPane.setTopComponent(logViewerTabs);
        JEditorPane logOut = new JEditorPane("text/html", "<html>");
        logHandler = new HtmlHandler(logOut);
        splitPane.setBottomComponent(new JScrollPane(logOut));
        this.add(splitPane);
        this.init();
        LoggerTableModel model = this.addLoggerTab(new LogManagerAdaptor(logManager), "Local logs",
            "Configure and view log output for the local JVM. ");
        this.setHandler(model, "", new Boolean(true));
    }

    public LoggerTableModel addLoggerTab(I_ManageLogs logManager, String tabDesc, String toolTipText)
            throws RemoteException {
        LoggerTableModel model = new LoggerTableModel(logManager);
        JTable table = new JTable(model);
        setPreferredWidth(table);
        setupTableEditor(table);
        table.getModel().addTableModelListener(this);
        this.logTables.add(table);
        logViewerTabs.addTab(tabDesc, null, new JScrollPane(table), toolTipText);
        return model;
    }

    /**
     * @param table
     */
    private void setupTableEditor(JTable table) {
        TableColumn specifiedLevel = table.getColumnModel().getColumn(1);
        JComboBox comboBox = new JComboBox();
        comboBox.addItem(null);
        comboBox.addItem(Level.OFF);
        comboBox.addItem(Level.SEVERE);
        comboBox.addItem(Level.WARNING);
        comboBox.addItem(Level.INFO);
        comboBox.addItem(Level.CONFIG);
        comboBox.addItem(Level.FINE);
        comboBox.addItem(Level.FINER);
        comboBox.addItem(Level.FINEST);
        comboBox.addItem(Level.ALL);
        specifiedLevel.setCellEditor(new DefaultCellEditor(comboBox));
    }

    /**
     * @param table
     */
    private void setPreferredWidth(JTable table) {
        TableColumn column = null;
        for (int i = 0; i < 4; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(400);
            } else if (i == 2) {
                column.setPreferredWidth(50);
            } else if (i == 3) {
                column.setPreferredWidth(50);
            } else {
                column.setPreferredWidth(100);
            }
        }
    }

    public void updateAvailableLogsOnSelectedTab() throws RemoteException {
        JTable table = this.logTables.get(logViewerTabs.getSelectedIndex());
        LoggerTableModel model = (LoggerTableModel) table.getModel();
        model.removeTableModelListener(this);
        LoggerTableModel newModel = new LoggerTableModel(model.logManager);
        table.setModel(newModel);
        setPreferredWidth(table);
        setupTableEditor(table);
        table.getModel().addTableModelListener(this);
        Iterator<String> selectionItr = model.getSelectedLoggers().iterator();
        while (selectionItr.hasNext()) {
            String loggerName = selectionItr.next();
            newModel.listenToLogger(loggerName);
        }
    }

    /**
     * Initializes the server, including exporting it and storing its proxy in
     * the registry.
     * 
     * @throws Exception
     *             if a problem occurs
     */
    @SuppressWarnings("unchecked")
    protected void init() throws Exception {
        LoginContext loginContext = (LoginContext) config.getEntry(this.getClass().getName(), "loginContext",
            LoginContext.class, null);
        if (loginContext == null) {
            initAsSubject();
        } else {
            loginContext.login();
            Subject.doAsPrivileged(loginContext.getSubject(), new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    initAsSubject();
                    return null;
                }
            }, null);
        }
    }

    /**
     * Initializes the server, assuming that the appropriate subject is in
     * effect.
     */
    protected void initAsSubject() throws Exception {
        /* Export the server */
        logger.info("initAsSubject: " + this.getClass().getName());
        Exporter exporter = getExporter();
        serverProxy = (I_PublishLogRecord) exporter.export(this.logHandler);

        /* Create the smart proxy */
        serverProxy = PublishLogRecordProxy.create(serverProxy);
    }

    /**
     * Returns the exporter for exporting the server.
     * 
     * @throws ConfigurationException
     *             if a problem occurs getting the exporter from the
     *             configuration
     * @throws RemoteException
     *             if a remote communication problem occurs
     */
    protected Exporter getExporter() throws ConfigurationException, RemoteException {
        return (Exporter) config.getEntry(this.getClass().getName(), "exporter", Exporter.class, new BasicJeriExporter(
            TcpServerEndpoint.getInstance(0), new BasicILFactory()));
    }

    /**
     * Implement the ServerProxyTrust interface to provide a verifier for secure
     * smart proxies.
     */
    public TrustVerifier getProxyVerifier() {
        return new PublishLogRecordProxy.Verifier(serverProxy);
    }

    /**
     * Returns a proxy object for this remote object.
     * 
     * @return our proxy
     */
    public Object getProxy() {
        return serverProxy;
    }

    private class LoggerTableModel extends AbstractTableModel {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private String[] columnNames = { "Logger Name", "Specified Level", "Effective Level", "Listen" };

        private Object[][] rowData;

        private I_ManageLogs logManager;

        List<String> logNameList;

        /**
         * @throws RemoteException
         * 
         */
        public LoggerTableModel(I_ManageLogs logManager) throws RemoteException {
            super();
            this.logManager = logManager;
            logNameList = logManager.getLoggerNames();
            Collections.<String> sort(logNameList);

            this.rowData = new Object[logNameList.size()][4];
            for (int i = 0; i < logNameList.size(); i++) {
                String loggerName = (String) logNameList.get(i);
                rowData[i][0] = loggerName;
                rowData[i][1] = logManager.getLevel(loggerName);
                rowData[i][1] = getEffectiveLevel(loggerName);
                rowData[i][3] = new Boolean(false);

            }
        }

        /**
         * @param loggerName
         */
        public void listenToLogger(String loggerName) {
            int index = logNameList.indexOf(loggerName);
            this.setValueAt(new Boolean(true), index, 3);
        }

        /**
         * @param i
         * @param logger
         * @throws RemoteException
         */
        private Level getEffectiveLevel(String loggerName) throws RemoteException {
            if (logManager.isLoggable(loggerName, Level.ALL)) {
                return Level.ALL;
            } else if (logManager.isLoggable(loggerName, Level.FINEST)) {
                return Level.FINEST;
            } else if (logManager.isLoggable(loggerName, Level.FINER)) {
                return Level.FINER;
            } else if (logManager.isLoggable(loggerName, Level.FINE)) {
                return Level.FINE;
            } else if (logManager.isLoggable(loggerName, Level.CONFIG)) {
                return Level.CONFIG;
            } else if (logManager.isLoggable(loggerName, Level.INFO)) {
                return Level.INFO;
            } else if (logManager.isLoggable(loggerName, Level.WARNING)) {
                return Level.WARNING;
            } else if (logManager.isLoggable(loggerName, Level.SEVERE)) {
                return Level.SEVERE;
            }
            return null;
        }

        public String getColumnName(int col) {
            return columnNames[col].toString();
        }

        public int getRowCount() {
            return rowData.length;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public Object getValueAt(int row, int col) {
            try {
                String loggerName = (String) rowData[row][0];
                if (col == 1) {
                    return this.logManager.getLevel(loggerName);
                } else if (col == 2) {
                    return getEffectiveLevel(loggerName);
                }
                return rowData[row][col];
            } catch (RemoteException ex) {
                return ex.toString();
            }
        }

        public boolean isCellEditable(int row, int col) {
            return (col == 1 || col == 3);
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == 1) {
                Logger logger = Logger.getLogger((String) rowData[row][0]);
                logger.setLevel((Level) value);
                fireTableCellUpdated(row, col);
                for (int i = 0; i < this.getRowCount(); i++) {
                    fireTableCellUpdated(i, 2);
                }
            } else {
                rowData[row][col] = value;
                fireTableCellUpdated(row, col);
            }

        }

        public Class<?> getColumnClass(int c) {
            if (c == 0) {
                return String.class;
            } else if (c == 1) {
                return Level.class;
            } else if (c == 2) {
                return Level.class;
            } else if (c == 3) {
                return Boolean.class;
            }
            return getValueAt(0, c).getClass();
        }

        public List<String> getSelectedLoggers() {
            List<String> selected = new ArrayList<String>();
            for (int i = 0; i < rowData.length; i++) {
                Boolean isSelected = (Boolean) getValueAt(i, 3);
                if (isSelected.booleanValue()) {
                    String loggerName = (String) getValueAt(i, 0);
                    selected.add(loggerName);
                }
            }

            return selected;
        }

    }

    /**
     * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
     */
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        LoggerTableModel model = (LoggerTableModel) e.getSource();
        String columnName = model.getColumnName(column);
        Object data = model.getValueAt(row, column);
        if (columnName.equals("Listen")) {
            try {
                changeHandler(model.logManager, (String) model.getValueAt(row, 0), (Boolean) data);
            } catch (RemoteException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /**
     * @param logs
     * @param valueAt
     * @param data
     * @throws RemoteException
     */
    private void changeHandler(I_ManageLogs logs, String loggerName, Boolean listen) throws RemoteException {
        if (listen.booleanValue()) {
            if (logs.addRemoteHandler(loggerName, this.serverProxy)) {
                logger.info("Added HTML handler to: " + loggerName + ".");
            }
        } else {
            logger.info("Removing HTML handler from: " + loggerName + ".");
            logs.removeRemoteHandler(loggerName, logHandler.getId());
        }

    }

    /**
     * @param valueAt
     * @param data
     */
    public void setHandler(LoggerTableModel model, String loggerName, Boolean listen) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(loggerName)) {
                model.setValueAt(listen, i, 3);
            }
        }
    }

    /**
     * @see org.dwfa.log.HtmlHandler#isPrintSourceAndMethodEnabled()
     */
    public boolean isPrintSourceAndMethodEnabled() {
        return logHandler.isPrintSourceAndMethodEnabled();
    }

    public void clearLog() {
        logHandler.clearLog();
    }

    /**
     * @see org.dwfa.log.HtmlHandler#setPrintSourceAndMethodEnabled(boolean)
     */
    public void setPrintSourceAndMethodEnabled(boolean printSourceAndMethodEnabled) {
        logHandler.setPrintSourceAndMethodEnabled(printSourceAndMethodEnabled);
    }

}
