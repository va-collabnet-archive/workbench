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
package org.dwfa.ace.gui.popup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LineageHelper;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;

public class ProcessPopupUtil {

    public static class ProcessMenuActionListener implements ActionListener {
        private class MenuProcessThread implements Runnable {

            private String action;

            /**
             * @param action
             */
            public MenuProcessThread(String action) {
                super();
                this.action = action;
            }

            public void run() {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                        processFile)));
                    I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
                    if (conceptUuid != null) {
                        process.writeAttachment(ProcessAttachmentKeys.ACTIVE_CONCEPT_UUID.getAttachmentKey(),
                            conceptUuid);
                    }
                    ois.close();
                    if (worker.isExecuting()) {
                        worker = worker.getTransactionIndependentClone();
                    }
                    process.execute(worker);
                    worker.commitTransactionIfActive();
                } catch (Exception ex) {

                    worker.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "<html>Exception processing action: " + action + "<p><p>" + ex.getMessage()
                            + "<p><p>See log for details.");
                }
            }
        };

        private File processFile;
        private I_Work worker;
        private UUID conceptUuid = null;

        public ProcessMenuActionListener(File processFile, I_Work worker) {
            super();
            this.processFile = processFile;
            this.worker = worker;
        }

        public ProcessMenuActionListener(File processFile, I_Work worker, UUID conceptUuid) {
            this(processFile, worker);
            this.conceptUuid = conceptUuid;
        }

        public void actionPerformed(ActionEvent e) {
            new Thread(new MenuProcessThread(e.getActionCommand()), "Menu Process Execution").start();
        }
    }

    public static void addProcessMenus(JMenuBar menuBar, String pluginRoot, MasterWorker menuWorker)
            throws TerminologyException {
        File menuDir = new File(pluginRoot + File.separator + "menu");
        if (menuDir.listFiles() != null) {
            addProcessMenuItems(menuBar, menuDir, menuWorker);
        }
    }

    private static Collection<File> getSortedFiles(File f) {
        TreeSet<File> sortedFiles = new TreeSet<File>();
        for (File child : f.listFiles()) {
            sortedFiles.add(child);
        }
        return sortedFiles;
    }

    public static void addProcessMenuItems(JMenuBar menuBar, File menuDir, MasterWorker menuWorker)
            throws TerminologyException {
        for (File f : getSortedFiles(menuDir)) {
            JMenu newMenu = null;
            if (f.isDirectory()) {
                if (f.getName().equals("File")) {
                    for (int i = 0; i < menuBar.getMenuCount(); i++) {
                        if (menuBar.getMenu(i).getText().equalsIgnoreCase("File")) {
                            newMenu = menuBar.getMenu(i);
                            break;
                        }
                    }
                    if (newMenu == null) {
                        menuBar.add(newMenu);
                    }
                    newMenu.addSeparator();
                } else {
                    String menuName = f.getName();
                    String regex = "_";
                    if (menuName.contains(regex)) {
                        String[] parts = menuName.split(regex, 2);
                        menuName = parts[1];
                    }
                    newMenu = new JMenu(menuName);
                    menuBar.add(newMenu);
                }
                if (f.listFiles() != null) {
                    for (File processFile : getSortedFiles(f)) {
                        if (processFile.isDirectory()) {
                            JMenu submenu = new JMenu(processFile.getName());
                            newMenu.add(submenu);
                            addSubMenuItems(submenu, processFile, menuWorker, null);
                        } else if (processFile.getName().toLowerCase().endsWith(".bp")) {
                            try {
                                ActionListener processMenuListener = new ProcessMenuActionListener(processFile,
                                    menuWorker);
                                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                                    new FileInputStream(processFile)));
                                I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
                                ois.close();
                                JMenuItem processMenuItem = new JMenuItem(process.getName());
                                processMenuItem.addActionListener(processMenuListener);
                                newMenu.add(processMenuItem);
                            } catch (IOException e) {
                                AceLog.getAppLog().alertAndLog(null, Level.SEVERE, "processing: " + processFile, e);
                            } catch (ClassNotFoundException e) {
                                AceLog.getAppLog().alertAndLog(null, Level.SEVERE, "processing: " + processFile, e);
                            }
                        } else if (processFile.getName().toLowerCase().endsWith("separator")) {
                            newMenu.addSeparator();
                        }
                    }
                }
            }
        }
    }

    public static void addSubMenuItems(JComponent subMenu, File menuDir, I_ConfigAceFrame frameConfig) throws Exception {
        addSubMenuItems(subMenu, menuDir, frameConfig.getWorker(), frameConfig.getHierarchySelection());
    }

    public static void addSubMenuItems(JComponent subMenu, File menuDir, MasterWorker worker,
            I_GetConceptData contextSubject) throws TerminologyException {
        try {
            if ((menuDir != null) && menuDir.exists() && (menuDir.listFiles() != null)) {

                boolean useContextValidation = false;
                Properties contextProps = new Properties();
                if (contextSubject != null) {
                    // Check for context definitions
                    File contextFile = new File(menuDir, "context.properties");
                    if (contextFile.exists()) {
                        contextProps.load(new FileReader(contextFile));
                        useContextValidation = true;
                    }
                }

                ArrayList<JMenuItem> menuItems = new ArrayList<JMenuItem>();
                ITERATE_FILES: for (File f : getSortedFiles(menuDir)) {
                    if (f.isDirectory()) {
                        JMenu newSubMenu = new JMenu(f.getName());
                        addSubMenuItems(newSubMenu, f, worker, contextSubject);
                        if (newSubMenu.getItemCount() > 0) {
                            subMenu.add(newSubMenu);
                        }
                    } else {
                        String filename = f.getName().toLowerCase();
                        if (filename.endsWith(".bp")) {

                            if (useContextValidation) {
                                try {
                                    // Check context
                                    LineageHelper lineage = new LineageHelper();
                                    String key = filename + ".context";
                                    if (contextProps.containsKey(key)) {
                                        UUID uuid = UUID.fromString(contextProps.getProperty(key).trim());
                                        I_GetConceptData context = LocalVersionedTerminology.get().getConcept(uuid);
                                        if (!lineage.hasAncestor(contextSubject, context)) {
                                            // Failed context test so skip
                                            continue ITERATE_FILES;
                                        }
                                    }
                                } catch (TerminologyException e) {
                                    // A context is defined put we can't validate it as the specified
                                    // uuid is invalid or does not exist
                                    continue ITERATE_FILES;
                                }
                            }

                            try {
                                ActionListener processMenuListener = new ProcessMenuActionListener(f, worker);
                                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                                    new FileInputStream(f)));
                                I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
                                ois.close();
                                JMenuItem processMenuItem = new JMenuItem(process.getName());
                                processMenuItem.addActionListener(processMenuListener);
                                menuItems.add(processMenuItem);
                            } catch (StreamCorruptedException sce) {
                                AceLog.getAppLog().warning("Error reading: " + f.getCanonicalPath());
                                AceLog.getAppLog().warning(sce.getMessage());
                            }
                        }
                    }
                }
                Collections.sort(menuItems, new MenuTextComparitor());
                for (JMenuItem menuItem : menuItems) {
                    subMenu.add(menuItem);
                }
            }
        } catch (Exception e) {
            throw new TerminologyException(e);
        }
    }

    static class MenuTextComparitor implements Comparator<JMenuItem> {
        @Override
        public int compare(JMenuItem o1, JMenuItem o2) {
            return o1.getText().compareTo(o2.getText());
        }
    }

}
