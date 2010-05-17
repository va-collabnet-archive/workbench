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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.jini.config.ConfigurationException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.entry.Name;

import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.worker.MasterWorker;

import com.sun.jini.start.LifeCycle;

/**
 * @author kec
 * 
 */
public class QueueViewerFrame extends ComponentFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected static Logger logger = Logger.getLogger("org.dwfa.queue.gui.QueueViewer");

    public class MoveListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            viewer.getMoveListener().actionPerformed(evt);

        }

    }

    /**
     * @author kec
     * 
     */
    public class AddRemoteQueue implements ActionListener, Runnable {
        String queueName;

        I_QueueProcesses queue;

        ServiceID sid;

        I_Work worker;

        ServiceItem[] authorityServices;

        /**
         * 
         */
        public AddRemoteQueue() {
            super();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent arg0) {
            try {
                worker = viewer.getWorker();
                new Thread(this).start();
            } catch (Exception ex) {
                logger.log(Level.WARNING, ex.toString(), ex);
            }

        }

        private class GetSelectedQueueAsWorker implements PrivilegedExceptionAction {
            public Object run() throws Exception {
                ServiceTemplate template = new ServiceTemplate(null, new Class[] { I_QueueProcesses.class }, null);

                authorityServices = worker.lookup(template, 1, 20, null, 1000 * 60);
                if ((authorityServices == null) || (authorityServices.length == 0)) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JOptionPane.showMessageDialog(QueueViewerFrame.this, "No queues found...");
                        }
                    });
                    return null;
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            ServiceItem item = (ServiceItem) worker.selectFromList(authorityServices, "Select Queue",
                                "Select the queue you want to examine:");
                            if (item != null) {
                                queueName = "Anonymous I_QueueProcesses";
                                sid = item.serviceID;
                                queue = (I_QueueProcesses) item.service;
                                queue = (I_QueueProcesses) worker.prepareProxy(queue, I_QueueProcesses.class);
                                for (int i = 0; i < item.attributeSets.length; i++) {
                                    if (Name.class.isAssignableFrom(item.attributeSets[i].getClass())) {
                                        Name nameEntry = (Name) item.attributeSets[i];
                                        queueName = nameEntry.name;
                                    }
                                }
                                viewer.addQueue(queue, queueName, sid);
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, e.toString(), e);
                        }
                    }
                });

                return null;
            }
        }

        public void run() {
            GetSelectedQueueAsWorker getAction = new GetSelectedQueueAsWorker();
            try {
                viewer.getWorker().doAsPrivileged(getAction, null);
            } catch (PrivilegedActionException e) {
                logger.log(Level.WARNING, e.toString(), e);
            }
        }
    }

    protected JMenu queueMenu;

    protected JMenuItem addQueueMI, moveToDiskMI;

    private QueueViewerPanel viewer;

    /**
     * @param title
     * @throws Exception
     */
    public QueueViewerFrame(String[] args, LifeCycle lc) throws Exception {
        super(args, lc);
        // ;
        MasterWorker worker = new MasterWorker(config);
        viewer = new QueueViewerPanel(config, worker);
        getContentPane().setLayout(new GridLayout(1, 1));
        getContentPane().add(viewer);

        setBounds(getDefaultFrameSize());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            new QueueViewerFrame(args, null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#addAppMenus(javax.swing.JMenuBar)
     */
    public void addAppMenus(JMenuBar mainMenuBar) {
        mainMenuBar.add(queueMenu = new JMenu("Queue"));
        queueMenu.add(addQueueMI = new JMenuItem("Add Queue"));
        addQueueMI.addActionListener(new AddRemoteQueue());
        queueMenu.addSeparator();
        queueMenu.add(moveToDiskMI = new JMenuItem("Take Selected Processes and Save To Disk"));
        moveToDiskMI.addActionListener(new MoveListener());
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
     */
    public JMenu getQuitMenu() {
        return queueMenu;
    }

    public void addInternalFrames(JMenu menu) {

    }

    public JMenuItem[] getNewWindowMenu() {
        JMenuItem newWindow = new JMenuItem("Queue Viewer");
        newWindow.addActionListener(new NewFrame(this.getArgs(), this.getLc()));
        return new JMenuItem[] { newWindow };
    }

    /**
     * @throws ConfigurationException
     * @see org.dwfa.bpa.util.ComponentFrame#getNextFrameName()
     */
    public String getNextFrameName() throws ConfigurationException {
        String title = (String) config.getEntry(this.getClass().getName(), "frameName", String.class, "Queue Viewer");
        if (count > 0) {
            return title + " " + count++;
        }
        count++;
        return title;
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getCount()
     */
    public int getCount() {
        return count;
    }

    private static int count = 0;

}
