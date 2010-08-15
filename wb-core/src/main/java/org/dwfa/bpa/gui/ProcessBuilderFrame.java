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
 * Created on Mar 17, 2005
 */
package org.dwfa.bpa.gui;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import net.jini.config.ConfigurationException;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.NoMappingException;

import com.sun.jini.start.LifeCycle;

/**
 * @author kec
 * 
 */
public class ProcessBuilderFrame extends ComponentFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected static Logger logger = Logger.getLogger(ProcessBuilderFrame.class.getName());
    protected JMenu fileMenu;

    protected JMenuItem newProcessMI, readProcessMI, takeProcessNoTranMI, takeProcessTranMI, saveProcessMI,
            saveForLauncherQueueMI, saveAsXmlMI;

    private boolean firstStartup = true;

    private ProcessBuilderPanel processBuilderPanel;

    private MasterWorker processWorker;

    /**
     * @param title
     * @throws Exception
     */
    public ProcessBuilderFrame(String[] args, LifeCycle lc) throws Exception {
        super(args, lc);

        this.getContentPane().setLayout(new GridLayout(1, 1));
        this.getContentPane().add(processBuilderPanel);

        // ;

        this.setBounds(getDefaultFrameSize());
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        if (this.firstStartup) {
            this.firstStartup = false;
            // Execute startup processes here...
            File[] startupFiles = new File("startup").listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".bp");
                }
            });
            if (startupFiles != null) {
                for (int i = 0; i < startupFiles.length; i++) {
                    try {
                        logger.info("Executing business process: " + startupFiles[i]);
                        FileInputStream fis = new FileInputStream(startupFiles[i]);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
                        processWorker.execute(process);
                        logger.info("Finished business process: " + startupFiles[i]);
                    } catch (Throwable e1) {
                        logger.log(Level.SEVERE, e1.getMessage() + " thrown by " + startupFiles[i], e1);
                    }
                }
            }
        }
    }

    /**
     * @throws ConfigurationException
     * @throws NoSuchMethodException
     * @throws QueryException
     * @throws ClassNotFoundException
     * @throws PropertyVetoException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IntrospectionException
     * @throws SecurityException
     * @throws InvalidComponentException
     * @throws ValidationException
     * @throws IdentifierIsNotNativeException
     * @throws NoMappingException
     * @throws RemoteException
     * @see org.dwfa.bpa.util.ComponentFrame#addAppMenus(javax.swing.JMenuBar)
     */
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        processWorker = new MasterWorker(config);
        this.processBuilderPanel = new ProcessBuilderPanel(config, processWorker);
        mainMenuBar.add(fileMenu = new JMenu("File"));
        fileMenu.add(newProcessMI = new JMenuItem("New Process"));
        newProcessMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_MASK));
        newProcessMI.addActionListener(this.processBuilderPanel.getNewProcessActionListener());
        fileMenu.add(readProcessMI = new JMenuItem("Read Process"));
        readProcessMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, MENU_MASK));
        readProcessMI.addActionListener(this.processBuilderPanel.getReadProcessActionListener());

        fileMenu.add(takeProcessNoTranMI = new JMenuItem("Take Process (no transaction)"));
        takeProcessNoTranMI.addActionListener(this.processBuilderPanel.getTakeNoTranProcessActionListener());

        fileMenu.addSeparator();
        fileMenu.add(saveProcessMI = new JMenuItem("Save Process"));
        saveProcessMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MENU_MASK));
        saveProcessMI.addActionListener(this.processBuilderPanel.getSaveProcessActionListener());

        fileMenu.add(saveForLauncherQueueMI = new JMenuItem("Save for Launcher Queue"));
        saveForLauncherQueueMI.addActionListener(this.processBuilderPanel.getSaveForLauncherQueueActionListener());

        fileMenu.add(saveAsXmlMI = new JMenuItem("Save as XML"));
        saveAsXmlMI.addActionListener(this.processBuilderPanel.getSaveAsXmlActionListener());

    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
     */
    public JMenu getQuitMenu() {
        return fileMenu;
    }

    public void addInternalFrames(JMenu menu) {

    }

    public JMenuItem[] getNewWindowMenu() {
        JMenuItem newWindow = new JMenuItem("Process Builder");
        newWindow.addActionListener(new NewFrame(this.getArgs(), this.getLc()));
        return new JMenuItem[] { newWindow };
    }

    /**
     * @throws ConfigurationException
     * @see org.dwfa.bpa.util.ComponentFrame#getNextFrameName()
     */
    public String getNextFrameName() throws ConfigurationException {
        String title = (String) config.getEntry(this.getClass().getName(), "frameName", String.class, "Process Builder");
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
