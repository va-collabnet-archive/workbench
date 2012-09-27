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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.jini.config.ConfigurationException;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;

import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.bpa.util.SelectObjectDialog;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.jini.SelectServiceDialog;

import com.sun.jini.start.LifeCycle;

/**
 * @author kec
 * 
 */
public class LogViewerFrame extends ComponentFrame {
    protected static Logger logger = Logger.getLogger(LogViewerFrame.class.getName());

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @author kec
     * 
     */
    public class PrintSourceAndMethodListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ev) {
            if (printSourceAndMethod.isSelected()) {
                logViewer.setPrintSourceAndMethodEnabled(true);
            } else {
                logViewer.setPrintSourceAndMethodEnabled(false);
            }
        }

    }

    public class ClearLogListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ev) {
            logViewer.clearLog();
        }

    }

    /**
     * @author kec
     * 
     */
    public class RefreshAvailableLogsListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0) {
            try {
                logViewer.updateAvailableLogsOnSelectedTab();
            } catch (RemoteException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        }

    }

    /**
     * @author kec
     * 
     */
    public class AddRemoteLogListener implements ActionListener {

        /**
         *  
         */
        public AddRemoteLogListener() {
            super();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0) {
            try {
                ServiceTemplate template = new ServiceTemplate(null, new Class[] { I_ManageLogs.class }, null);
                ServiceItem[] logServices;
                logServices = worker.lookup(template, 1, 20, null, 1000 * 60);
                ServiceItem item =
                        (ServiceItem) this.selectFromList(logServices, "Select Log Manager Service",
                    "Select the log manager service you want to subscribe to:");
                if (item != null) {
                    String loggerName = "Anonymous Remote";

                    I_ManageLogs logger = (I_ManageLogs) item.service;
                    for (int i = 0; i < item.attributeSets.length; i++) {
                        if (Name.class.isAssignableFrom(item.attributeSets[i].getClass())) {
                            Name nameEntry = (Name) item.attributeSets[i];
                            loggerName = nameEntry.name;
                        }
                    }
                    logViewer.addLoggerTab(logger, loggerName,
                        "Configure and view log output for a network accessable JVM. ");
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }

        }

        /**
         * @see org.dwfa.bpa.process.I_Work#selectService(net.jini.core.lookup.ServiceItem[])
         */
        public Object selectFromList(Object[] list, String title, String labelText) {
            Iterator<JFrame> frameItr = OpenFrames.getFrames().iterator();
            JFrame activeFrame = null;
            while (frameItr.hasNext()) {
                JFrame aFrame = frameItr.next();
                if (aFrame.isActive()) {
                    activeFrame = aFrame;
                    break;
                }
            }
            if (ServiceItem[].class.isAssignableFrom(list.getClass())) {
                return SelectServiceDialog.showDialog(activeFrame, activeFrame, labelText, title, (ServiceItem[]) list,
                    (ServiceItem) list[0], null);
            } else {
                return SelectObjectDialog.showDialog(activeFrame, activeFrame, labelText, title, list, list[0], null);
            }
        }
    }

    protected JMenu logMenu;

    protected JMenuItem addRemoteLoggerMI, refreshAvailableLogs, clearLog;
    protected JCheckBoxMenuItem printSourceAndMethod;

    private LogViewerPanel logViewer;
    private MasterWorker worker;

    /**
     * @param title
     * @throws Exception
     */
    public LogViewerFrame(String[] args, LifeCycle lc) throws Exception {
        super(args, lc);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        worker = new MasterWorker(config);
        this.logViewer = new LogViewerPanel(args, lc);
        this.getContentPane().setLayout(new GridLayout(1, 1));
        this.getContentPane().add(this.logViewer);

        this.setBounds(getDefaultFrameSize());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LogViewerFrame.this.setVisible(true);
            }
        });
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#addAppMenus(javax.swing.JMenuBar)
     */
    public void addAppMenus(JMenuBar mainMenuBar) {
        mainMenuBar.add(logMenu = new JMenu("Log"));
        logMenu.add(addRemoteLoggerMI = new JMenuItem("Add Remote Logger"));
        addRemoteLoggerMI.addActionListener(new AddRemoteLogListener());
        logMenu.add(refreshAvailableLogs = new JMenuItem("Refresh available logs"));
        refreshAvailableLogs.addActionListener(new RefreshAvailableLogsListener());
        logMenu.add(printSourceAndMethod = new JCheckBoxMenuItem("Print source and method"));
        printSourceAndMethod.addActionListener(new PrintSourceAndMethodListener());
        logMenu.add(clearLog = new JMenuItem("Clear log"));
        clearLog.addActionListener(new ClearLogListener());
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
     */
    public JMenu getQuitMenu() {
        return null;
    }

    public void addInternalFrames(JMenu menu) {

    }

    public JMenuItem[] getNewWindowMenu() {
        JMenuItem newWindow = new JMenuItem("Log Viewer");
        newWindow.addActionListener(new NewFrame(this.getArgs(), this.getLc()));
        return new JMenuItem[] { newWindow };
    }

    /**
     * @throws ConfigurationException
     * @see org.dwfa.bpa.util.ComponentFrame#getNextFrameName()
     */
    public String getNextFrameName() throws ConfigurationException {
        String title = (String) config.getEntry(this.getClass().getName(), "frameName", String.class, "Log Viewer");
        if (count > 0) {
            return title + " " + count++;
        }
        count++;
        return title;
    }

    private static int count = 0;

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getCount()
     */
    public int getCount() {
        return count;
    }

}
