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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.ace.tree.ExpandNodeSwingWorker;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.queue.QueueServer;
import org.dwfa.swing.SwingWorker;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.io.FileIO;
import org.dwfa.vodb.VodbEnv;

import com.sun.jini.start.LifeCycle;

public class AceRunner {
    private static final String SVN_PROPERTY_PREFIX = "SVN_";
    private static final String USE_RELATIVE_SVN_REPO_URL = "USE_RELATIVE_SVN_REPO_URL";
    private static final String USER_CHANGE_SET_ROOT = "USER_CHANGE_SET_ROOT";

    static {
        VodbEnv.setHeadless(false);
    }

    private class CheckIpAddressForChanges implements ActionListener {
        InetAddress startupLocalHost;

        public CheckIpAddressForChanges() throws UnknownHostException {
            super();
            startupLocalHost = InetAddress.getLocalHost();
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                InetAddress currentLocalHost = InetAddress.getLocalHost();
                if (currentLocalHost.equals(startupLocalHost)) {
                    // all ok
                } else {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "<html>Your ip address ("
                        + currentLocalHost.toString() + ") <br> has changed since startup ("
                        + startupLocalHost.toString() + ") <br> please restart your application.");
                }
            } catch (UnknownHostException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

    }

    public static String[] args;
    public static LifeCycle lc;
    private static boolean firstStartup = true;
    /**
     * The jini configuration provider
     */
    public Configuration jiniConfig;
    public File aceConfigFile;
    public Properties aceProperties;
    public Boolean initializeFromSubversion = false;
    public String[] svnUpdateOnStart = null;

    public AceRunner(String[] args, LifeCycle lc) {
        try {
            AceProtocols.setupExtraProtocols();

            AceRunner.args = args;
            AceRunner.lc = lc;

            AceLog.getAppLog().info(
                "\n*******************\n" + "\n Starting " + this.getClass().getSimpleName() + "\n with config file: "
                    + getArgString(args) + "\n\n******************\n");
            if (new File(args[0]).exists()) {
                jiniConfig = ConfigurationProvider.getInstance(args, getClass().getClassLoader());
            }

            setupLookAndFeel();
            setupSwingExpansionTimerLogging();
            setupIpChangeListener();
            // setBerkeleyDbAsTransactional();

            File acePropertiesFile = new File("config", "ace.properties");
            boolean acePropertiesFileExists = acePropertiesFile.exists();
            aceProperties = new Properties();

            boolean initialized = false;
            if (acePropertiesFileExists) {
                aceProperties.loadFromXML(new FileInputStream(acePropertiesFile));
                initialized = Boolean.parseBoolean((String) aceProperties.get("initialized"));
            }
            if (!acePropertiesFileExists || !initialized) {
                try {
                    new AceSvn(AceRunner.class, jiniConfig).initialSubversionOperationsAndChangeSetImport(aceProperties);
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                    System.exit(0);
                }
            }

            aceProperties.put("initialized", "true");
            // AceSvn aceSvn = new AceSvn(AceRunner.class, jiniConfig);
            // aceSvn.handleSvnUpdate("profiles");
            // aceSvn.handleSvnUpdate("changesets");

            if (jiniConfig != null) {
                aceConfigFile = (File) jiniConfig.getEntry(this.getClass().getName(), "aceConfigFile", File.class,
                    new File("config/config.ace"));
                initializeFromSubversion = (Boolean) jiniConfig.getEntry(this.getClass().getName(),
                    "initFromSubversion", Boolean.class, Boolean.FALSE);
                svnUpdateOnStart = (String[]) jiniConfig.getEntry(this.getClass().getName(), "svnUpdateOnStart",
                    String[].class, null);
            } else {
                aceConfigFile = new File("config/config.ace");
            }

            // new AceSvn(AceRunner.class,
            // jiniConfig).handleSvnProfileCheckout(aceProperties);

            SvnPrompter prompter = new SvnPrompter();
            File profileDir = new File("profiles");
            if (profileDir.exists() == false && initializeFromSubversion) {
                new AceSvn(AceRunner.class, jiniConfig).initialSubversionOperationsAndChangeSetImport(aceProperties,
                    true);
            }

            if (aceConfigFile == null || !aceConfigFile.exists()) {
                String lastProfileDirStr = "profiles";
                if (aceProperties.getProperty("last-profile-dir") != null) {
                    lastProfileDirStr = aceProperties.getProperty("last-profile-dir");
                }
                File lastProfileDir = new File(lastProfileDirStr);

                if (lastProfileDir.isFile() && lastProfileDir.exists()) {
                    aceConfigFile = lastProfileDir;
                } else {
                    String[] profileFiles = lastProfileDir.list(new FilenameFilter() {

                        public boolean accept(File dir, String name) {
                            return name.endsWith(".ace");
                        }
                    });

                    if (profileFiles != null && profileFiles.length == 1) {
                        aceConfigFile = new File(lastProfileDir, profileFiles[0]).getCanonicalFile();
                    } else if (profileFiles != null && profileFiles.length > 1) {
                        AceLog.getAppLog().warning(
                            "Profile from jini configuration does not exist and more than one profile file found in "
                                + "last profile directory " + lastProfileDir + ", unable to determine profile to use.");
                    }
                }
            }

            if (aceConfigFile != null && aceConfigFile.exists() && acePropertiesFile.exists()) {

                // Put up a dialog to select the configuration file...
                CountDownLatch latch = new CountDownLatch(1);
                GetProfileWorker profiler = new GetProfileWorker(aceConfigFile, aceProperties, jiniConfig, latch);
                profiler.start();
                latch.await();

                aceConfigFile = profiler.aceConfigFile;
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(aceConfigFile)));
                AceConfig.config = (AceConfig) ois.readObject();
                AceConfig.config.setProfileFile(aceConfigFile);

                // If using relative SVN from properties file (the real one)
                // if SVN entries in the SVN map
                // this profile is an old one not used this way yet???!!!???
                // take the username and password from the first SVN entry -
                // overwrite the profile username and password
                // update profile svn entries with details from ace properties
                // file
                // end
                // end

                // Check if SVN URLS are configured using ace.properties file
                if (Boolean.valueOf(aceProperties.getProperty(USE_RELATIVE_SVN_REPO_URL))) {

                    for (I_ConfigAceFrame configAceFrame : AceConfig.config.aceFrames) {

                        Map<String, SubversionData> profileSubversionMap = configAceFrame.getSubversionMap();
                        if (!profileSubversionMap.isEmpty()) {
                            String username = profileSubversionMap.values().iterator().next().getUsername();
                            String password = profileSubversionMap.values().iterator().next().getPassword();

                            if ("".equals(username)) {
                                do {
                                    prompter.prompt("", username);
                                    password = prompter.getPassword();
                                    username = prompter.getUsername();
                                } while ("".equals(username));
                                profileSubversionMap.values().iterator().next().setUsername(username);
                                profileSubversionMap.values().iterator().next().setPassword(password);
                            }

                            Map<String, SubversionData> relativeSubversionMap = new HashMap<String, SubversionData>();
                            Enumeration<String> propertyNames = (Enumeration<String>) aceProperties.propertyNames();
                            for (; propertyNames.hasMoreElements();) {
                                String name = propertyNames.nextElement();
                                if (name.startsWith(SVN_PROPERTY_PREFIX)) {
                                    String svnProperty = aceProperties.getProperty(name);
                                    SubversionData svnData = new SubversionData();
                                    svnData.setUsername(username);
                                    svnData.setPassword(password);
                                    svnData.setPreferredReadRepository(AceSvn.getRepositoryUrl(svnProperty));
                                    svnData.setRepositoryUrlStr(AceSvn.getRepositoryUrl(svnProperty));
                                    svnData.setWorkingCopyStr(AceSvn.getWorkingLocation(svnProperty));

                                    relativeSubversionMap.put(svnData.getWorkingCopyStr(), svnData);
                                } else if (name.equals(USER_CHANGE_SET_ROOT)) {
                                    configAceFrame.getDbConfig().setChangeSetRoot(
                                        new File(aceProperties.getProperty(USER_CHANGE_SET_ROOT) + File.separator
                                            + username));
                                }

                            }

                            if (!profileSubversionMap.values().equals(relativeSubversionMap.values())) {
                                AceLog.getAppLog()
                                    .warning(
                                        "Profile "
                                            + configAceFrame.getFrameName()
                                            + " SVN entries have been replaced with the ACE relative SVN properties. Replaced values were "
                                            + profileSubversionMap);
                                profileSubversionMap.clear();
                                profileSubversionMap.putAll(relativeSubversionMap);
                            }

                        }
                        configAceFrame.setClassifierIsaType(LocalVersionedTerminology.get().getConcept(SNOMED.Concept.IS_A.getUids()));
                        configAceFrame.setClassificationRoot(LocalVersionedTerminology.get().getConcept(SNOMED.Concept.ROOT.getUids()));
                        configAceFrame.setClassificationRoleRoot(LocalVersionedTerminology.get().getConcept(ArchitectonicAuxiliary.Concept.USER_ROLE.getUids()));
                        configAceFrame.setClassifierInputPath(LocalVersionedTerminology.get().getConcept(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()));
                        configAceFrame.setClassifierOutputPath(AceConfig.config.getUserPath());
                    }
                }

                setupDatabase(AceConfig.config, aceConfigFile);

                AceConfig.config.getUsername();
                prompter.setUsername(AceConfig.config.getUsername());
                prompter.setPassword(profiler.getPassword());
            } else {
                if (initializeFromSubversion) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "Unable to initialize from subversion. Is the network connected?");
                } else {
                    File dbFolder = new File("berkeley-db");
                    if (jiniConfig != null) {
                        dbFolder = (File) jiniConfig.getEntry(this.getClass().getName(), "dbFolder", File.class,
                            new File("target/berkeley-db"));
                    }
                    AceConfig.config = new AceConfig(dbFolder);
                    AceConfig.config.setProfileFile(aceConfigFile);
                    AceConfig.setupAceConfig(AceConfig.config, null, null, false);
                }
            }
            aceProperties.storeToXML(new FileOutputStream(acePropertiesFile), null);
            ACE.setAceConfig(AceConfig.config);
            AceConfig.config.addChangeSetWriters();
            int successCount = 0;
            int frameCount = 0;

            for (final I_ConfigAceFrame ace : AceConfig.config.aceFrames) {
                frameCount++;
                if (ace.isActive()) {
                    AceFrameConfig afc = (AceFrameConfig) ace;
                    afc.setMasterConfig(AceConfig.config);
                    if (ace.isAdministrative()) {
                        successCount++;
                        handleAdministrativeFrame(prompter, ace);

                    } else {
                        successCount++;
                        handleNormalFrame(ace);
                    }
                }
            }

            if (successCount == 0) {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    "No frames where opened. Now exiting.", "No successful logins...", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            // Startup queues in profile sub-directories here...

            File directory = AceConfig.config.getProfileFile().getParentFile();

            if (directory.listFiles() != null) {
                for (File dir : directory.listFiles()) {
                    processFile(dir, lc);
                }
            }

            // Startup other queues here...
            List<String> queuesToRemove = new ArrayList<String>();
            for (String queue : AceConfig.config.getQueues()) {
                File queueFile = new File(queue);
                if (queueFile.exists()) {
                    AceLog.getAppLog().info("Found queue: " + queueFile.toURI().toURL().toExternalForm());
                    if (QueueServer.started(queueFile)) {
                        AceLog.getAppLog().info("Queue already started: " + queueFile.toURI().toURL().toExternalForm());
                    } else {
                        new QueueServer(new String[] { queueFile.getCanonicalPath() }, lc);
                    }
                } else {
                    queuesToRemove.add(queue);
                }
            }
            if (queuesToRemove.size() > 0) {
                AceConfig.config.getQueues().removeAll(queuesToRemove);
                StringBuffer buff = new StringBuffer();
                buff.append("<html><body>Removing queues that are not accessible: <br>");
                for (String queue : queuesToRemove) {
                    buff.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                    buff.append(queue);
                    buff.append("<br>");
                }
                buff.append("</body></html>");

                AceLog.getAppLog().alertAndLog(Level.WARNING, buff.toString(),
                    new Exception("Removing queues that are not accessable: " + queuesToRemove));
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            System.exit(0);
        }
    }

    // /**
    // * Get a map of SubversionData keyed on the WorkingCopyStr
    // *
    // * @param subversionMap Collection of SubversionData
    // *
    // * @return Map<String, SubversionData>
    // */
    // private Map<String, SubversionData>
    // getSubversionWorkingLocationMap(Collection<SubversionData>
    // subversionDataList) {
    // Map<String, SubversionData> aceSvnData = new HashMap<String,
    // SubversionData>();
    //
    // for (SubversionData subversionData : subversionDataList) {
    // aceSvnData.put(subversionData.getWorkingCopyStr(), subversionData);
    // }
    //
    // return aceSvnData;
    // }

    private static class GetProfileWorker extends SwingWorker<Boolean> {
        StartupFrameListener fl = new StartupFrameListener();
        JFrame parentFrame = new JFrame();
        boolean newFrame = false;
        private File aceConfigFile;
        private Properties aceProperties;
        private Configuration jiniConfig;
        private File lastProfileDir;
        private String password;
        private AceLoginDialog aceLoginDialog;
        CountDownLatch latch;
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        public GetProfileWorker(File aceConfigFile, Properties aceProperties, Configuration jiniConfig,
                CountDownLatch latch) {
            super();
            parentFrame = new JFrame();
            if (OpenFrames.getNumOfFrames() > 0) {
                parentFrame = OpenFrames.getFrames().iterator().next();
                AceLog.getAppLog().info("### Adding an existing frame");
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        public void run() {
                            parentFrame.setContentPane(new JLabel("The Terminology IDE is starting..."));
                            parentFrame.pack();

                            parentFrame.setVisible(true);
                            parentFrame.setLocation((d.width / 2) - (parentFrame.getWidth() / 2), (d.height / 2)
                                - (parentFrame.getHeight() / 2));
                            OpenFrames.addFrame(parentFrame);
                            AceLog.getAppLog().info("### Using a new frame");
                            newFrame = true;
                        }

                    });
                } catch (InterruptedException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (InvocationTargetException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
            aceLoginDialog = new AceLoginDialog(parentFrame);
            aceLoginDialog.setLocation((d.width / 2) - (aceLoginDialog.getWidth() / 2), (d.height / 2)
                - (aceLoginDialog.getHeight() / 2));
            this.aceConfigFile = aceConfigFile;
            this.jiniConfig = jiniConfig;
            this.aceProperties = aceProperties;
            this.latch = latch;
        }

        @Override
        protected Boolean construct() throws Exception {

            File profileDir = new File("profiles" + File.separator + "users");
            if (profileDir.exists() == false) {
                profileDir = new File("profiles");
                if (profileDir.exists() == false) {
                    profileDir.mkdirs();
                }
            }
            lastProfileDir = profileDir;
            if (aceProperties.getProperty("last-profile-dir") != null) {
                lastProfileDir = new File(aceProperties.getProperty("last-profile-dir"));
            }
            OpenFrames.addFrameListener(fl);
            if (OpenFrames.getNumOfFrames() > 0) {
                parentFrame = OpenFrames.getFrames().iterator().next();
                AceLog.getAppLog().info("### Using an existing frame 1");
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        parentFrame.setContentPane(new JLabel("The Terminology IDE is starting..."));
                        parentFrame.pack();
                        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                        parentFrame.setLocation(d.width / 2, d.height / 2);
                        parentFrame.setVisible(true);
                        OpenFrames.addFrame(parentFrame);
                        AceLog.getAppLog().info("### Adding a new frame 1");
                        newFrame = true;
                    }

                });
            }
            return true;
        }

        @Override
        protected void finished() {
            super.finished();
            try {
                // shows the AceLoginDialog
                aceConfigFile = aceLoginDialog.getUserProfile(lastProfileDir);
                password = new String(aceLoginDialog.getPassword());

                aceProperties.setProperty("last-profile-dir", FileIO.getRelativePath(aceConfigFile));

                if (newFrame) {
                    OpenFrames.removeFrame(parentFrame);
                    parentFrame.setVisible(false);
                }
                latch.countDown();
                OpenFrames.removeFrameListener(fl);
            } catch (TaskFailedException e) {
                AceLog.getAppLog().alertAndLogException(e);
                System.exit(0);
            }
        }

        public String getPassword() {
            return password;
        }
    }

    private static class StartupFrameListener implements ListDataListener {

        public void contentsChanged(ListDataEvent arg0) {
            AceLog.getAppLog().info("Contents changed: " + arg0);

        }

        public void intervalAdded(ListDataEvent arg0) {
            AceLog.getAppLog().info("intervalAdded: " + arg0);

        }

        public void intervalRemoved(ListDataEvent arg0) {
            AceLog.getAppLog().info("intervalRemoved: " + arg0);

        }

    }

    private void handleNormalFrame(final I_ConfigAceFrame ace) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    boolean startup = firstStartup;
                    firstStartup = false;
                    AceFrame af = new AceFrame(AceRunner.args, AceRunner.lc, ace, startup);
                    af.setVisible(true);
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }

        });
    }

    private void handleAdministrativeFrame(SvnPrompter prompter, final I_ConfigAceFrame ace) {
        String username = prompter.getUsername();
        String password = prompter.getPassword();
        boolean tryAgain = true;
        prompter.setUsername(ace.getAdminUsername());
        prompter.setPassword("");
        while (tryAgain) {
            prompter.prompt("Please authenticate as an administrative user:", ace.getAdminUsername());
            if (ace.getAdminUsername().equals(prompter.getUsername())
                && ace.getAdminPassword().equals(prompter.getPassword())) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            boolean startup = firstStartup;
                            firstStartup = false;
                            String regularPluginRoot = AceFrame.getPluginRoot();
                            AceFrame.setPluginRoot(AceFrame.getAdminPluginRoot());
                            AceFrame newFrame = new AceFrame(AceRunner.args, AceRunner.lc, ace, startup);
                            AceFrame.setPluginRoot(regularPluginRoot);
                            ace.setSubversionToggleVisible(true);
                            newFrame.setTitle(newFrame.getTitle().replace("Editor", "Administrator"));
                            newFrame.setVisible(true);
                        } catch (Exception e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }
                    }
                });

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
        prompter.setUsername(username);
        prompter.setPassword(password);
    }

    private void setupLookAndFeel() throws ConfigurationException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, UnsupportedLookAndFeelException {
        if (jiniConfig != null) {
            String lookAndFeelClassName = (String) jiniConfig.getEntry(this.getClass().getName(),
                "lookAndFeelClassName", String.class, UIManager.getSystemLookAndFeelClassName());

            UIManager.setLookAndFeel(lookAndFeelClassName);
        } else {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
    }

    private void setBerkeleyDbAsTransactional() {
        VodbEnv.setTransactional(true);
        VodbEnv.setTxnNoSync(false);
        VodbEnv.setDeferredWrite(false);
    }

    private void setupIpChangeListener() throws ConfigurationException, UnknownHostException {
        if (jiniConfig != null) {
            Boolean listenForIpChanges = (Boolean) jiniConfig.getEntry(this.getClass().getName(), "listenForIpChanges",
                Boolean.class, null);
            if (listenForIpChanges != null) {
                if (listenForIpChanges) {
                    Timer ipChangeTimer = new Timer(2 * 60 * 1000, new CheckIpAddressForChanges());
                    ipChangeTimer.start();
                }
            }
        }
    }

    private void setupSwingExpansionTimerLogging() throws ConfigurationException {
        if (jiniConfig != null) {
            Boolean logTimingInfo = (Boolean) jiniConfig.getEntry(this.getClass().getName(), "logTimingInfo",
                Boolean.class, null);
            if (logTimingInfo != null) {
                ExpandNodeSwingWorker.setLogTimingInfo(logTimingInfo);
            }
            AceLog.getAppLog().info("Swing expansion logTimingInfo " + logTimingInfo);
        }
    }

    private String getArgString(final String[] args) {
        String argsStr;
        if (args == null) {
            argsStr = "null";
        } else {
            argsStr = Arrays.asList(args).toString();
        }
        return argsStr;
    }

    private void setupDatabase(AceConfig aceConfig, File configFileFile) throws IOException {
        if (aceConfig.isDbCreated() == false) {
            int n = JOptionPane.showConfirmDialog(new JFrame(),
                "Would you like to extract the db from your maven repository?", "DB does not exist",
                JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                AceConfig.extractMavenLib(aceConfig);
            } else {
                AceLog.getAppLog().info("Exiting, user did not want to extract the DB from maven.");
                return;
            }
        }

        File jeUserPropertiesFile = new File(configFileFile.getParentFile(), "je.properties");
        if (jeUserPropertiesFile.exists()) {
            File jeDbPropertiesFile = new File(aceConfig.getDbFolder(), "je.properties");
            FileIO.copyFile(jeUserPropertiesFile, jeDbPropertiesFile);
        }
    }

    private void processFile(File file, LifeCycle lc) throws Exception {
        if (file.isDirectory() == false) {
            if (file.getName().equalsIgnoreCase("queue.config") && QueueServer.started(file) == false) {
                AceLog.getAppLog().info("Found user queue: " + file.getCanonicalPath());
                new QueueServer(new String[] { file.getCanonicalPath() }, lc);
            }
        } else {
            String fileName = file.getName();
            if (fileName.equals("queues-maven")) {
                // ignore these queue directories.
            } else {
                for (File f : file.listFiles()) {
                    processFile(f, lc);
                }
            }
        }
    }

}
