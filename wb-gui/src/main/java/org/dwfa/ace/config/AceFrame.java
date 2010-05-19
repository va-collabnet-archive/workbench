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
package org.dwfa.ace.config;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.MarshalledObject;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import net.jini.config.ConfigurationException;

import org.dwfa.ace.ACE;
import org.dwfa.ace.WizardPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.TerminologyException;

import com.sun.jini.start.LifeCycle;

public class AceFrame extends ComponentFrame {

    private ACE cdePanel;
    private AceFrameConfig frameConfig;

    private class AceWindowActionListener implements WindowListener {

        public void windowActivated(WindowEvent e) {
            doWindowActivation();
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {

        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
            doWindowActivation();
        }

    }

    public static String pluginRoot = "plugins";

    public static String getPluginRoot() {
        return pluginRoot;
    }

    public static void setPluginRoot(String pluginRoot) {
        AceFrame.pluginRoot = pluginRoot;
    }

    public static String getAdminPluginRoot() {
        return adminPluginRoot;
    }

    private static String adminPluginRoot = "plugins" + File.separator + "admin";

    public AceFrame(String[] args, LifeCycle lc, I_ConfigAceFrame frameConfig, boolean executeStartupProcesses)
            throws Exception {
        super(args, lc);
        this.frameConfig = (AceFrameConfig) frameConfig;
        setTitle(getFrameName());
        ((AceFrameConfig) frameConfig).setAceFrame(this);

        JLayeredPane layers = getRootPane().getLayeredPane();
        WizardPanel wizPanel = new WizardPanel(getCdePanel());
        layers.add(wizPanel, JLayeredPane.MODAL_LAYER);
        wizPanel.setLocation(350, 0);

        setContentPane(getCdePanel());

        getCdePanel().setWorkflowDetailsSheet(wizPanel.getWfDetailsPanel());
        getCdePanel().setWorkflowPanel(wizPanel.getWfPanel());
        getCdePanel().setWorfklowDetailSheetVisible(false);
        getCdePanel().setup(frameConfig);

        Rectangle defaultBounds = getDefaultFrameSize();
        Rectangle bounds = frameConfig.getBounds();
        bounds.width = Math.min(bounds.width, defaultBounds.width);
        bounds.height = Math.min(bounds.height, defaultBounds.height);
        setBounds(bounds);
        doWindowActivation();
        getQuitList().add(getCdePanel());
        this.addWindowListener(new AceWindowActionListener());
        MasterWorker worker = new MasterWorker(config);
        cdePanel.getAceFrameConfig().setWorker(worker);
        if (executeStartupProcesses) {
            File configFile = ((AceFrameConfig) frameConfig).getMasterConfig().getProfileFile();
            File startupFolder = new File(configFile.getParentFile().getParentFile(), "startup");
            executeStartupProcesses(worker, startupFolder);
            startupFolder = new File(configFile.getParentFile(), "startup");
            executeStartupProcesses(worker, startupFolder);
        }
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private void executeStartupProcesses(MasterWorker worker, File startupFolder) {
        if (startupFolder.exists()) {
            AceLog.getAppLog().info("Startup folder exists: " + startupFolder);
            File[] startupFiles = startupFolder.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".bp");
                }
            });
            if (startupFiles != null) {
                Arrays.sort(startupFiles);
                for (int i = 0; i < startupFiles.length; i++) {
                    try {
                        AceLog.getAppLog().info("Executing startup business process: " + startupFiles[i]);
                        FileInputStream fis = new FileInputStream(startupFiles[i]);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        ObjectInputStream ois = new ObjectInputStream(bis);
                        I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
                        worker.execute(process);
                        AceLog.getAppLog().info("Finished startup business process: " + startupFiles[i]);
                    } catch (Throwable e1) {
                        AceLog.getAppLog().alertAndLog(Level.SEVERE, e1.getMessage() + " thrown by " + startupFiles[i],
                            e1);
                    }
                }
            } else {
                AceLog.getAppLog().info("No startup processes found. Folder exists: " + startupFolder.exists());
            }
        } else {
            AceLog.getAppLog().info("NO startup folder exists: " + startupFolder);
        }
    }

    private void doWindowActivation() {
        try {
            Terms.get().setActiveAceFrameConfig(getCdePanel().getAceFrameConfig());
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @Override
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        getCdePanel().addToMenuBar(mainMenuBar, cfb.getEditMenu(), this);
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
     */
    public JMenu getQuitMenu() {
        return getCdePanel().getFileMenu();
    }

    public void addInternalFrames(JMenu menu) {

    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getCount()
     */
    public int getCount() {
        return count;
    }

    private static int count = 0;

    public class NewAceAdminFrame implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                MarshalledObject<I_ConfigAceFrame> marshalledFrame = new MarshalledObject<I_ConfigAceFrame>(cdePanel.getAceFrameConfig());
                AceFrameConfig newFrameConfig = (AceFrameConfig) marshalledFrame.get();
                newFrameConfig.setMasterConfig((AceConfig) cdePanel.getAceFrameConfig().getDbConfig());
                newFrameConfig.getMasterConfig().aceFrames.add(newFrameConfig);
                SvnPrompter prompter = new SvnPrompter();
                boolean tryAgain = true;
                while (tryAgain) {
                    prompter.prompt("Please authenticate as an administrative user:", newFrameConfig.getAdminUsername());
                    if (newFrameConfig.getAdminUsername().equals(prompter.getUsername())
                        && newFrameConfig.getAdminPassword().equals(prompter.getPassword())) {
                        String regularPluginRoot = pluginRoot;
                        pluginRoot = adminPluginRoot;
                        AceFrame newFrame = new AceFrame(getArgs(), getLc(), newFrameConfig, false);
                        pluginRoot = regularPluginRoot;
                        newFrameConfig.setSubversionToggleVisible(true);
                        newFrameConfig.setAdministrative(true);
                        newFrame.setTitle(newFrame.getTitle().replace("Editor", "Administrator"));
                        newFrame.setVisible(true);
                        tryAgain = false;
                        prompter.setPassword("");
                    } else {
                        int n = JOptionPane.showConfirmDialog(null, "Would you like to try again?",
                            "Administrative authentication failed", JOptionPane.YES_NO_OPTION);
                        if (n == JOptionPane.YES_OPTION) {
                            tryAgain = true;
                        } else {
                            tryAgain = false;
                        }
                    }
                }
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }
    }

    public class NewAceFrame implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                MarshalledObject<I_ConfigAceFrame> marshalledFrame = new MarshalledObject<I_ConfigAceFrame>(cdePanel.getAceFrameConfig());
                AceFrameConfig newFrameConfig = (AceFrameConfig) marshalledFrame.get();
                newFrameConfig.setMasterConfig((AceConfig) cdePanel.getAceFrameConfig().getDbConfig());
                newFrameConfig.getMasterConfig().aceFrames.add(newFrameConfig);

                AceFrame newFrame = new AceFrame(getArgs(), getLc(), newFrameConfig, false);
                newFrame.setVisible(true);
            } catch (Exception e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }

        }

    }

    public JMenuItem[] getNewWindowMenu() {
        if (ACE.editMode) {
            JMenuItem newViewer = new JMenuItem("Editor Window");
            newViewer.addActionListener(new NewAceFrame());
            JMenuItem newAdministrator = new JMenuItem("Administrator Window");
            newAdministrator.addActionListener(new NewAceAdminFrame());
            return new JMenuItem[] { newViewer, newAdministrator };
        } else {
            JMenuItem newViewer = new JMenuItem("Viewer Window");
            newViewer.addActionListener(new NewAceFrame());
            return new JMenuItem[] { newViewer };
        }
    }

    public String getNextFrameName() throws ConfigurationException {
        String title = (String) config.getEntry(this.getClass().getName(), "frameName", String.class, "Viewer");
        if (ACE.editMode) {
            title.replace("Viewer", "Editor");
            title.replace("viewer", "editor");
        }
        if (frameConfig != null) {
            if (count > 0) {
                title = title + "; User: " + this.frameConfig.getUsername() + "; # " + count;
            } else {
                title = title + "; User: " + this.frameConfig.getUsername();
            }
            count++;
        }
        return title;
    }

    public String getFrameName() {
        String title = "Viewer Window";
        if (ACE.editMode) {
            title = "Editor Window";
            if (count > 0) {
                title = title + "; User: " + this.frameConfig.getUsername() + "; # " + count;
            } else {
                title = title + "; User: " + this.frameConfig.getUsername();
            }
        } else {
            if (count > 0) {
                title = title + "; " + this.frameConfig.getUsername() + "; # " + count;
            } else {
                title = title + "; " + this.frameConfig.getUsername();
            }
        }
        if (title.equals(this.getTitle())) {
            // no increment, redundant call
        } else {
            count++;
        }
        return title;
    }

    public ACE getCdePanel() {
        if (cdePanel == null) {
            cdePanel = new ACE(config, pluginRoot);
        }
        return cdePanel;
    }

    public JList getBatchConceptList() {
        return getCdePanel().getBatchConceptList();
    }

    public void performLuceneSearch(String query, List<I_TestSearchResults> extraCriterion) {
        getCdePanel().performLuceneSearch(query, extraCriterion);
    }

    public void setShowAddresses(boolean show) {
        getCdePanel().setShowAddresses(show);
    }

    public void setShowComponentView(boolean show) {
        getCdePanel().setShowComponentView(show);
    }

    public void setShowHierarchyView(boolean show) {
        getCdePanel().setShowHierarchyView(show);
    }

    public void setShowHistory(boolean show) {
        getCdePanel().setShowHistory(show);
    }

    public void setShowPreferences(boolean show) {
        getCdePanel().setShowPreferences(show);
    }

    public void setShowSearch(boolean show) {
        getCdePanel().setShowSearch(show);
    }

    public void showListView() {
        getCdePanel().showListView();
    }

    public void setupSvn() {
        getCdePanel().setupSvn();
    }

    public void setShowProcessBuilder(boolean show) {
        getCdePanel().setShowProcessBuilder(show);
    }

    public void setShowQueueViewer(boolean show) {
        getCdePanel().setShowQueueViewer(show);
    }

    public JPanel getSignpostPanel() {
        return getCdePanel().getSignpostPanel();
    }

    public void setShowSignpostPanel(boolean show) {
        getCdePanel().setShowSignpostPanel(show);
    }

    public void setShowSignpostToggleVisible(boolean visible) {
        getCdePanel().setShowSignpostToggleVisible(visible);
    }

    public void setShowSignpostToggleEnabled(boolean enabled) {
        getCdePanel().setShowSignpostToggleEnabled(enabled);
    }

    public void setSignpostToggleIcon(ImageIcon icon) {
        getCdePanel().setSignpostToggleIcon(icon);
    }

    public I_HostConceptPlugins getListConceptViewer() {
        return getCdePanel().getListConceptViewer();
    }

    @Override
    public boolean okToClose() {
        if (cdePanel.okToClose() == false) {
            return false;
        }
        if (frameConfig.getMasterConfig() != null) {
            if ((frameConfig.getMasterConfig().aceFrames.size() > 1) || (AceConfig.config != frameConfig.getDbConfig())) {
                frameConfig.getMasterConfig().aceFrames.remove(this.frameConfig);
                getQuitList().remove(cdePanel);
                return true;
            }
            if (cfb.quit()) {
                this.setVisible(false);
                System.exit(0);
            } 
            return false;
        }
        return true;
    }

}
