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
 * Created on Apr 18, 2005
 */
package org.dwfa.bpa.worker;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.TableColumn;

import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.lookup.ServiceItemFilter;
import net.jini.space.JavaSpace05;

import org.dwfa.bpa.space.DistributedSemaphore;
import org.dwfa.bpa.space.SemaphoreEntry;
import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.util.StopThreadException;

import com.sun.jini.start.LifeCycle;

/**
 * @author kec
 * 
 */
public class GenericWorkerManager extends ComponentFrame implements Runnable, WindowListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final String GENERIC_WORKER_LIST = "Generic worker list";

    private static Logger logger = Logger.getLogger(GenericWorkerManager.class.getName());

    private JMenu managerMenu;

    private Vector<GenericWorker> workers = new Vector<GenericWorker>();

    private WorkerTableModel model;

    private MasterWorker worker;

    private boolean stop = false;

    /**
     * @throws ConfigurationException
     * @throws TransactionException
     * @throws UnusableEntryException
     * @throws RemoteException
     * @throws InterruptedException
     * @throws IOException
     * @throws LeaseDeniedException
     * 
     */
    public GenericWorkerManager(String[] args, LifeCycle lc) throws Exception {
        super(args, lc);
        this.config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());
        int numOfCpus = Runtime.getRuntime().availableProcessors();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Started GenericWorkerManager: " + this);
            logger.info("Number of CPUs: " + numOfCpus);
        }
        this.worker = new MasterWorker(config);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(this);
        new Thread(this, "GenericWorkerManager").start();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    model = new WorkerTableModel(workers);
                    JTable table = new JTable(model);
                    setPreferredWidth(table);
                    setContentPane(new JScrollPane(table));
                    setBounds(getDefaultFrameSize());
                    setVisible(true);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        });

    }

    /**
     * @param table
     */
    private void setPreferredWidth(JTable table) {
        TableColumn column = null;
        for (int i = 0; i < 7; i++) {
            column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(40);
            } else if (i == 2) {
                column.setPreferredWidth(40);
            } else if (i == 3) {
                column.setPreferredWidth(10);
            } else if (i == 4) {
                column.setPreferredWidth(40);
            } else if (i == 5) {
                column.setPreferredWidth(10);
            } else if (i == 6) {
                column.setPreferredWidth(200);
            } else {
                column.setPreferredWidth(25);
            }
        }
    }

    /**
     * @param space
     * @param se
     * @throws LeaseDeniedException
     * @throws RemoteException
     * @throws InterruptedException
     * @throws IOException
     * @throws UnusableEntryException
     * @throws TransactionException
     * @throws UnknownTransactionException
     * @throws CannotAbortException
     * @throws PrivilegedActionException
     */
    public static int countAndLogGenericWorkers(MasterWorker worker, JavaSpace05 space, SemaphoreEntry se)
            throws LeaseDeniedException, RemoteException, InterruptedException, IOException, UnusableEntryException,
            TransactionException, UnknownTransactionException, CannotAbortException, PrivilegedActionException {
        Transaction t = worker.createTransaction(1000 * 60);
        // Take the semaphore for exclusive access to the GENERIC_WORKER_LIST
        Entry semaphore = space.takeIfExists(se, t, 1000 * 60);

        if (semaphore != null) {
            // Find all the recorded workers...
            GenericWorkerEntry matchAllGwe = new GenericWorkerEntry();
            Entry gwEntry = space.takeIfExists(matchAllGwe, t, 1000 * 60);
            int genericWorkerCount = 0;
            while (gwEntry != null) {
                genericWorkerCount++;
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Found generic worker record: " + gwEntry);
                }
                gwEntry = space.takeIfExists(matchAllGwe, t, 1000 * 60);
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Found " + genericWorkerCount + " generic worker entries");
            }
            t.abort();
            return genericWorkerCount;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Semaphore not found for generic worker entries");
        }
        return 0;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @SuppressWarnings("unchecked")
    public void run() {
        this.worker.doAsPrivileged(new PrivilegedAction() {

            public Object run() {
                try {
                    while (true) {
                        try {
                            checkStop();
                            int numOfCpus = Runtime.getRuntime().availableProcessors();
                            ServiceTemplate tmpl = new ServiceTemplate(null, new Class[] { JavaSpace05.class }, null);
                            ServiceItemFilter filter = null;
                            long waitDur = 1000 * 60 * 3;
                            ServiceItem service = worker.lookup(tmpl, filter, waitDur);
                            checkStop();
                            if (service != null) {
                                if (logger.isLoggable(Level.INFO)) {
                                    StringBuffer buff = new StringBuffer();
                                    if (service.attributeSets != null) {
                                        for (int i = 0; i < service.attributeSets.length; i++) {
                                            buff.append(service.attributeSets[i] + " ");
                                        }
                                    } else {
                                        buff.append("null attributeSets ");
                                    }
                                    buff.append(service.serviceID);
                                    logger.info("Found java space: " + buff.toString());
                                }
                                checkStop();
                                JavaSpace05 space = (JavaSpace05) service.service;
                                SemaphoreEntry se = new SemaphoreEntry(GENERIC_WORKER_LIST);
                                // See if the entry already exists...
                                SemaphoreEntry e = (SemaphoreEntry) space.readIfExists(se, null, 1000 * 60);
                                if (e == null) {
                                    DistributedSemaphore ds = new DistributedSemaphore(space, GENERIC_WORKER_LIST);
                                    ds.create(1);
                                    if (logger.isLoggable(Level.INFO)) {
                                        logger.info("Created DistributedSemaphore for " + GENERIC_WORKER_LIST);
                                    }
                                }

                                String workerName = (String) config.getEntry(GenericWorkerManager.this.getClass()
                                    .getName(), "workerName", String.class, "w");
                                InetAddress localaddr = InetAddress.getLocalHost();
                                Integer numOfWorkers = (Integer) GenericWorkerManager.this.config.getEntry(
                                    GenericWorkerManager.this.getClass().getName(), "numOfWorkers", Integer.class,
                                    new Integer(numOfCpus));
                                if (logger.isLoggable(Level.INFO)) {
                                    logger.info("Creating " + numOfWorkers + "  generic workers.");
                                }
                                checkStop();
                                for (int i = 0; i < numOfWorkers.intValue(); i++) {
                                    String workerDesc = workerName + " " + (i + 1);
                                    if (logger.isLoggable(Level.INFO)) {
                                        logger.info("Creating generic worker: " + workerDesc);
                                    }
                                    GenericWorker gw = new GenericWorker(config, UUID.randomUUID(), workerDesc,
                                        localaddr, space, model, i);
                                    workers.add(gw);
                                    if (model != null) {
                                        model.fireTableDataChanged();
                                    }
                                    new Thread(gw, gw.getWorkerDesc()).start();
                                }
                                countAndLogGenericWorkers(worker, space, se);
                                return null;
                            }
                            logger.warning("Found  NULL service searching for java space.");
                            checkStop();
                        } catch (StopThreadException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, ex.getMessage(), ex);
                        }
                        try {
                            Thread.sleep(1000 * 60 * 2);
                        } catch (InterruptedException e) {
                        }
                    }
                } catch (StopThreadException ex) {
                    for (GenericWorker w : workers) {
                        w.stop();
                    }
                    return null;
                }
            }

            /**
             * @throws StopThreadException
             */
            private void checkStop() throws StopThreadException {
                if (stop) {
                    throw new StopThreadException();
                }
            }
        }, null);

    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#addAppMenus(javax.swing.JMenuBar)
     */
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        mainMenuBar.add(this.managerMenu = new JMenu("Manager"));
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
     */
    public JMenu getQuitMenu() {
        return this.managerMenu;
    }

    /**
     * @see org.dwfa.bpa.util.I_InitComponentMenus#addInternalFrames(javax.swing.JMenu)
     */
    public void addInternalFrames(JMenu menu) {
        ; // Nothing to do

    }

    public JMenuItem[] getNewWindowMenu() {
        JMenuItem newWindow = new JMenuItem("Generic Worker Manager");
        newWindow.addActionListener(new NewFrame(this.getArgs(), this.getLc()));
        return new JMenuItem[] { newWindow };
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getNextFrameName()
     */
    public String getNextFrameName() {
        String title = "Generic Worker Manager";
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

    public void windowOpened(WindowEvent e) {
        // Nothing to do...

    }

    public void windowClosing(WindowEvent e) {
        this.stop = true;

    }

    public void windowClosed(WindowEvent e) {
        this.stop = true;

    }

    public void windowIconified(WindowEvent e) {
        // Nothing to do...

    }

    public void windowDeiconified(WindowEvent e) {
        // Nothing to do...

    }

    public void windowActivated(WindowEvent e) {
        // Nothing to do...

    }

    public void windowDeactivated(WindowEvent e) {
        // Nothing to do...

    }

}
