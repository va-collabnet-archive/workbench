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
 * Created on Mar 10, 2005
 */
package org.dwfa.bpa.gui;

import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import net.jini.config.ConfigurationException;

import org.dwfa.bpa.gui.GridBagPanel.GridBagPanelConstraints;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.I_Workspace;
import org.dwfa.bpa.util.ComponentFrame;

/**
 * @author kec
 * 
 */
public class WorkspaceFrame extends ComponentFrame implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    protected JMenu fileMenu;

    protected JMenuItem saveMI;

    protected JMenuItem loadMI;

    protected JCheckBoxMenuItem showInInternalFrameMI;

    private WorkspacePanel workspace;

    private String title;

    private I_Work worker;

    /**
     * @param title
     * @param menuDir
     * @throws Exception
     */
    public WorkspaceFrame(String title, I_ManageUserTransactions transactionInterface, File menuDir, I_Work worker)
            throws Exception {
        super(new String[] {}, null, menuDir);
        this.title = title;
        this.menuDir = menuDir;
        this.worker = worker;
        this.workspace = new WorkspacePanel(new ArrayList<GridBagPanel>(), this, transactionInterface);
        this.getContentPane().setLayout(new GridLayout(1, 1));
        this.getContentPane().add(workspace);
        this.setTitle(title);
    }

    public void addGridBagPanel(GridBagPanel panel) {
        this.workspace.addGridBagPanel(panel);
        this.redoWindowMenu();
    }

    public void redoWindowMenu() {
        this.cfb.intervalAdded(null);
    }

    public static int getMenuMask() {
        return MENU_MASK;
    }

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.saveMI) {
            try {
                Map<String, GridBagPanelConstraints> constraintMap = new HashMap<String, GridBagPanelConstraints>();
                for (GridBagPanel gbp : this.workspace.getPanelList()) {
                    constraintMap.put(gbp.getTitle(), gbp.getConstraints());
                }
                // Create a file dialog box to prompt for a new file to display

                FileDialog f = new FileDialog(this, "Save Workspace Configuration", FileDialog.SAVE);
                f.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + "wsconfig");
                f.setVisible(true); // Display dialog and wait for response
                if (f.getFile() != null) {
                    File processBinaryFile = new File(f.getDirectory(), f.getFile() + ".wsc");
                    FileOutputStream fos = new FileOutputStream(processBinaryFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(constraintMap);
                    oos.close();
                }
                f.dispose(); // Get rid of the dialog box

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == this.loadMI) {
            try {
                FileDialog f = new FileDialog(this, "Load Workspace Configuration", FileDialog.LOAD);
                f.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + "wsconfig");
                f.setVisible(true); // Display dialog and wait for response
                f.setFilenameFilter(new FilenameFilter() {

                    public boolean accept(File dir, String name) {
                        return name.endsWith(".wsc");
                    }
                });
                if (f.getFile() != null) {
                    File processBinaryFile = new File(f.getDirectory(), f.getFile());
                    FileInputStream fis = new FileInputStream(processBinaryFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    Map<String, GridBagPanelConstraints> constraintMap = (Map<String, GridBagPanelConstraints>) ois.readObject();
                    ois.close();

                    for (GridBagPanel gbp : this.workspace.getPanelList()) {
                        GridBagPanelConstraints c = constraintMap.get(gbp.getTitle());
                        if (c != null) {
                            gbp.setConstraints(c);
                        }
                    }

                }
                f.dispose(); // Get rid of the dialog box
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == this.showInInternalFrameMI) {
            if (this.workspace.isShownInInternalFrame()) {
                this.workspace.setShownInInternalFrame(false);
                this.showInInternalFrameMI.setSelected(false);
            } else {
                this.workspace.setShownInInternalFrame(true);
                this.showInInternalFrameMI.setSelected(true);
            }
            redoWindowMenu();
        }
    }

    public void addInternalFrames(JMenu menu) {
        if (this.workspace != null) {
            /*
             * menu.addSeparator();
             * menu.add(showInInternalFrameMI);
             * if (this.workspace.isShownInInternalFrame()) {
             * menu.addSeparator();
             * Iterator<GridBagPanel> panelListItr =
             * this.workspace.getPanelList().iterator();
             * while (panelListItr.hasNext()) {
             * GridBagPanel gbp = panelListItr.next();
             * JMenuItem menuItem = new JMenuItem(gbp.getTitle());
             * menuItem.addActionListener(new BringInternalFrameToFront(gbp));
             * menu.add(menuItem);
             * }
             * }
             */
        }
    }

    /**
     * @return
     */
    public I_Workspace getWorkspace() {
        return this.workspace;
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#addAppMenus(javax.swing.JMenuBar)
     */
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        if (mainMenuBar.getMenuCount() == 0) {
            mainMenuBar.add(fileMenu = new JMenu("File"));
        } else {
            for (int i = 0; i < mainMenuBar.getMenuCount(); i++) {
                JMenu menuItem = mainMenuBar.getMenu(i);
                if (menuItem.getText().toLowerCase().startsWith("edit")) {
                    mainMenuBar.add(fileMenu = new JMenu("File"), i);
                    break;
                }
            }
        }
        System.out.println("Adding app menus: " + this.menuDir);
        if (this.menuDir != null) {
            System.out.println(this.menuDir.getName());
            System.out.println(this.menuDir.getAbsolutePath());
            if (this.menuDir.listFiles() != null) {
                for (File f : this.menuDir.listFiles()) {
                    JMenu newMenu;
                    if (f.isDirectory()) {
                        if (f.getName().equals("File")) {
                            newMenu = this.fileMenu;
                        } else {
                            newMenu = new JMenu(f.getName());
                            mainMenuBar.add(newMenu);
                        }
                        if (f.listFiles() != null) {
                            for (File processFile : f.listFiles()) {
                                ActionListener processMenuListener = new ProcessMenuActionListener(processFile, this);
                                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                                    new FileInputStream(processFile)));
                                I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
                                ois.close();
                                JMenuItem processMenuItem = new JMenuItem(process.getName());
                                processMenuItem.addActionListener(processMenuListener);
                                newMenu.add(processMenuItem);

                            }
                        }
                        if (newMenu == fileMenu) {
                            fileMenu.addSeparator();
                        }
                    }
                }
            }
        }
        saveMI = new JMenuItem("Save Workspace Configuration");
        // fileMenu.add(saveMI);
        saveMI.addActionListener(this);

        loadMI = new JMenuItem("Load Workspace Configuration");
        // fileMenu.add(loadMI);
        loadMI.addActionListener(this);

        showInInternalFrameMI = new JCheckBoxMenuItem("Internal Frames", false);
        showInInternalFrameMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, MENU_MASK));
        showInInternalFrameMI.addActionListener(this);
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
     */
    public JMenu getQuitMenu() {
        return this.fileMenu;
    }

    public JMenuItem[] getNewWindowMenu() {
        return null;
    }

    /**
     * @throws ConfigurationException
     * @see org.dwfa.bpa.util.ComponentFrame#getNextFrameName()
     */
    public String getNextFrameName() throws ConfigurationException {
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

    public JMenu getFileMenu() {
        return fileMenu;
    }

    public I_Work getWorker() {
        return worker;
    }

}
