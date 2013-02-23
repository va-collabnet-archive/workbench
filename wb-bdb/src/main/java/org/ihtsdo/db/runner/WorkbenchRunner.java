package org.ihtsdo.db.runner;

import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.commitlog.CommitLog;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.config.AceLoginDialog;
import org.dwfa.ace.config.AceProtocols;
import org.dwfa.ace.dnd.DragMonitor;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.ComponentFrameBean;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.queue.MultiQueueStarter;
import org.dwfa.svn.Svn;
import org.dwfa.swing.SwingWorker;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.io.FileIO;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.arena.contradiction.ContradictionEditorGenerator;
import org.ihtsdo.arena.promotion.PromotionEditorGenerator;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.custom.statics.CustomStatics;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.objectCache.ObjectCache;
import org.ihtsdo.objectCache.ObjectCacheClassHandler;
import org.ihtsdo.taxonomy.model.NodeFactory;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.ttk.preferences.EnumBasedPreferences;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class WorkbenchRunner {
    private static final String WB_PROPERTIES            = "wb.properties";
    private static boolean      firstStartup             = true;
    public static Boolean       initializeFromSubversion = false;
    public static Boolean       isaCacheCreateOnStartUp  = false;
    public static Boolean       persistIsaCache          = false;
    public static String[]      svnUpdateOnStart         = null;
    public static String[]      args;
    public static File          userProfile;
    public static File          wbConfigFile;
    public static Properties    wbProperties;

    static {
        DwfaEnv.setHeadless(false);
    }

    public WorkbenchRunner(String[] args) {
        try {
            AceProtocols.setupExtraProtocols();
            WorkbenchRunner.args = args;
            AceLog.getAppLog().info("\n*******************\n" + "\n Starting " + this.getClass().getSimpleName()
                                    + "\n with config file: " + getArgString(args) + "\n\n******************\n");
            wbConfigFile = new File("config/wb.config");

            UIManager.LookAndFeelInfo[] lookAndFeels  = UIManager.getInstalledLookAndFeels();
            boolean                     windowsSystem = false;

            for (UIManager.LookAndFeelInfo lookAndFeel : lookAndFeels) {
                if (lookAndFeel.getClassName().contains("Windows")) {
                    windowsSystem = true;
                }
            }

            DragMonitor.setup();

            /*
             * from http://lists.apple.com/archives/java-dev/2004/oct/msg00591.html
             *
             * The problem is that a ProgressMonitor would cause some kind of background thread
             * which would throw up junk events which had to be processed, a memory and CPU drain.
             * Specifically, a ProgressMonitor contains a JProgressBar and a JPBar on Mac OS X uses
             * AquaProgressBarUI as its look-and-feel. APBarUI is buggy in that even though the
             * progress bar may no longer be visible, the thread used to animate the progress
             * bar never stops doing its thing. Each displayed JPBar has its own thread, all
             * of which get stuck in memory, which can slow things down to a crawl and cause,
             * as with my application, OutOfMemoryExceptions.
             *
             * The solution is to not use AquaProgressBarUI. I have put the following lines in
             * my code so that my application will use the BasicProgressBarUI instead.
             * It's ugly but that's a secondary concern.
             *
             * javax.swing.UIManager.put( "ProgressBarUI", "javax.swing.plaf.basic.BasicProgressBarUI" );
             * javax.swing.UIManager.put( "javax.swing.plaf.basic.BasicProgressBarUI", Class.forName("javax.swing.plaf.basic.BasicProgressBarUI") );
             *
             */
            if (ComponentFrameBean.MAC_OS_X) {

                // javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                // javax.swing.UIManager.put( "ProgressBarUI", "javax.swing.plaf.synth.SynthProgressBarUI");
                // javax.swing.UIManager.put( "javax.swing.plaf.basic.BasicProgressBarUI", Class.forName("javax.swing.plaf.synth.SynthProgressBarUI") );
                // System.getProperties().setProperty("swing.defaultlaf", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                javax.swing.UIManager.put("ProgressBarUI", "javax.swing.plaf.basic.BasicProgressBarUI");
                javax.swing.UIManager.put("ProgressBar.foreground", Color.GREEN);
                javax.swing.UIManager.put("ProgressBar.background", Color.GRAY);
                javax.swing.UIManager.put("ProgressBar.border", BorderFactory.createRaisedBevelBorder());
                javax.swing.UIManager.put("javax.swing.plaf.basic.BasicProgressBarUI",
                                          Class.forName("javax.swing.plaf.basic.BasicProgressBarUI"));
            }

            OpenFrames.addNewWindowMenuItemGenerator(new ContradictionEditorGenerator());
            OpenFrames.addNewWindowMenuItemGenerator(new PromotionEditorGenerator());
            System.setProperty("javax.net.ssl.trustStore", "config/cacerts");

            long          startTime = System.currentTimeMillis();
            ActivityPanel activity  = new ActivityPanel(null, true);

            activity.setIndeterminate(true);
            activity.setProgressInfoUpper("Loading the database");
            activity.setProgressInfoLower("Setting up the environment...");
            activity.addRefreshActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("System.exit from activity action listener: " + e.getActionCommand());
                    System.exit(0);
                }
            });
            ActivityViewer.addActivity(activity);

            if ((System.getProperty("viewer") != null) && System.getProperty("viewer").toLowerCase().startsWith("t")) {
                ACE.editMode = false;
            }

            File    wbPropertiesFile        = new File("config", WB_PROPERTIES);
            boolean acePropertiesFileExists = wbPropertiesFile.exists();

            wbProperties = new Properties();

            SvnPrompter prompter    = new SvnPrompter();
            boolean     initialized = false;

            if (acePropertiesFileExists) {
                wbProperties.loadFromXML(new FileInputStream(wbPropertiesFile));
                initialized = Boolean.parseBoolean((String) wbProperties.get("initialized"));
            }

            SvnHelper svnHelper = new SvnHelper(WorkbenchRunner.class);

            if ((acePropertiesFileExists == false) || (initialized == false)) {
                try {
                    svnHelper.initialSubversionOperationsAndChangeSetImport(wbPropertiesFile, prompter);
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                    AceLog.getAppLog().info("### exit from WorkbenchRunner.");
                    System.exit(0);
                }
            }

            wbProperties.put("initialized", "true");

            // Check to see if there is a custom Properties file
            checkCustom();

            File profileDir = new File("profiles");

            if (((profileDir.exists() == false) && initializeFromSubversion) || (svnUpdateOnStart != null)) {
                Svn.setConnectedToSvn(true);
                svnHelper.initialSubversionOperationsAndChangeSetImport(new File("config", WB_PROPERTIES), prompter);
            }

            if ((wbConfigFile == null) || !wbConfigFile.exists()) {
                if (acePropertiesFileExists) {
                    wbProperties.loadFromXML(new FileInputStream(wbPropertiesFile));
                }

                String lastProfileDirStr = "profiles";

                if (wbProperties.getProperty("last-profile-dir") != null) {
                    lastProfileDirStr = wbProperties.getProperty("last-profile-dir");
                }

                File lastProfileDir = new File(lastProfileDirStr);

                if (lastProfileDir.isFile()) {
                    wbConfigFile = lastProfileDir;
                } else {
                    String[] profileFiles = lastProfileDir.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".ace") || name.endsWith(".wb") || name.endsWith(".wbp");
                        }
                    });

                    if ((profileFiles != null) && (profileFiles.length == 1)) {
                        wbConfigFile = new File(lastProfileDir, profileFiles[0]).getCanonicalFile();
                    } else if ((profileFiles != null) && (profileFiles.length > 1)) {
                        AceLog.getAppLog().warning(
                            "Profile from jini configuration does not exist and more than one profile file found in "
                            + "last profile directory " + lastProfileDir + ", unable to determine profile to use.");
                    }
                }
            }

            File berkeleyDbDir = new File("berkeley-db");

            Bdb.selectJeProperties(berkeleyDbDir, berkeleyDbDir);
            Bdb.setup(berkeleyDbDir.getName(), activity);
            activity.setProgressInfoLower("complete");
            activity.complete();

            long loadTime = System.currentTimeMillis() - startTime;

            AceLog.getAppLog().info("### Load time: " + loadTime + " ms");
            AceLog.getAppLog().info("Adding bdb shutdown hook. ");
            Runtime.getRuntime().addShutdownHook(new Thread(this.getClass().getCanonicalName()) {

                /**
                 * TODO For some reason, this thread does not seem to run in
                 * normal shutdown (only on ^c). Need to figure out why and make
                 * sure that we never fail to gracefully shutdown the database.
                 */
                @Override
                public void run() {
                    try {
                        System.out.println("Starting bdb shutdown from shutdown hook...");
                        System.out.flush();
                        NodeFactory.close();
                        Bdb.close();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Finished bdb shutdown from shutdown hook...");
                    System.out.flush();
                }
            });

            if (System.getProperty("newprofile") != null) {
                File dbFolder = new File("berkeley-db");

                AceConfig.config = new AceConfig(dbFolder);

                String username    = "username";
                File   profileFile = new File("profiles/" + username, username + ".wb");

                AceConfig.config.setProfileFile(profileFile);
                AceConfig.setupAceConfig(AceConfig.config, null, null, false);

                File startupFolder = new File(profileFile.getParent(), "startup");

                startupFolder.mkdirs();

                File shutdownFolder = new File(profileFile.getParent(), "shutdown");

                shutdownFolder.mkdirs();
            }

            if (svnHelper != null) {
                svnHelper.doChangeSetImport();
            }

            if ((wbConfigFile != null) && wbConfigFile.exists() && wbPropertiesFile.exists()) {

                // Put up a dialog to select the configuration file...
                CountDownLatch   latch    = new CountDownLatch(1);
                GetProfileWorker profiler = new GetProfileWorker(latch);

                profiler.start();
                latch.await();

                File jeUserPropertiesFile = new File(userProfile.getParentFile(), "je.properties");

                if (jeUserPropertiesFile.exists()) {
                    File jeDbPropertiesFile = new File(AceConfig.config.getDbFolder(), "je.properties");

                    FileIO.copyFile(jeUserPropertiesFile, jeDbPropertiesFile);
                }

                File test = userProfile;

                System.out.println("** User profile file on start up: " + test.getAbsolutePath());

                ObjectInputStream ois =
                    new ObjectInputStream(new BufferedInputStream(new FileInputStream(userProfile)));

                try {
                    AceConfig.config = (AceConfig) ois.readObject();
                    AceConfig.config.setProfileFile(userProfile);
                    prompter.setUsername(AceConfig.config.getUsername());
                    prompter.setPassword(profiler.getPassword());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                                  "Unable to open user file. Is it corrupt?");
                    AceLog.getAppLog().log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    AceLog.getAppLog().info("### exit from WorkbenchRunner 2.");
                    System.exit(-1);
                } finally {
                    ois.close();
                }
            } else {
                if (initializeFromSubversion) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                                  "Unable to initialize from subversion. Is the network connected?");
                } else {
                    File dbFolder = new File("berkeley-db");

                    AceConfig.config = new AceConfig(dbFolder);
                    AceConfig.config.setProfileFile(wbConfigFile);
                    AceConfig.setupAceConfig(AceConfig.config, null, null, false);
                }
            }

            wbProperties.storeToXML(new FileOutputStream(wbPropertiesFile), null);
            ACE.setAceConfig(AceConfig.config);

            String writerName = AceConfig.config.getChangeSetWriterFileName();

            if (!writerName.endsWith(".eccs")) {
                String firstPart = writerName.substring(0, writerName.lastIndexOf('.'));

                writerName = firstPart.concat(".eccs");
                AceConfig.config.setChangeSetWriterFileName(writerName);
            }

            String userName = AceConfig.config.getUsername();
            File changeSetRoot = AceConfig.config.getChangeSetRoot();
            ChangeSetWriterHandler.addWriter(userName + ".eccs",
                    new EConceptChangeSetWriter(
                            new File(changeSetRoot, writerName),
                            new File(changeSetRoot, "." + writerName),
                            ChangeSetGenerationPolicy.INCREMENTAL, true));
            ChangeSetWriterHandler.addWriter(userName + ".commitLog.xls",
                    new CommitLog(new File(changeSetRoot, "commitLog.xls"),
                            new File(changeSetRoot, "." + "commitLog.xls")));

            // Start user queues. 
            new MultiQueueStarter(loadUserPreferences(userName));
            
            int successCount = 0;
            int frameCount   = 0;

            for (final I_ConfigAceFrame ace : AceConfig.config.aceFrames) {
                frameCount++;

                if (ace.isActive()) {
                    AceFrameConfig afc = (AceFrameConfig) ace;

                    afc.setMasterConfig(AceConfig.config);

                    boolean login = true;

                    while (login) {
                        if (frameCount == 1) {
                            if (ace.getPassword().equals(prompter.getPassword())) {
                                if (ace.getUsername().equals(prompter.getUsername()) == false) {
                                    AceConfig.config.setUsername(ace.getUsername());
                                    prompter.setUsername(ace.getUsername());
                                }
                            } else {
                                prompter.prompt("Please authenticate for: " + ace.getFrameName(), ace.getUsername());

                                if (ace.getUsername().equals(prompter.getUsername()) == false) {
                                    AceConfig.config.setUsername(ace.getUsername());
                                    prompter.setUsername(ace.getUsername());
                                }
                            }
                        } else if ((ace.getUsername().equals(prompter.getUsername()) == false)
                                   || (ace.getPassword().equals(prompter.getPassword()) == false)) {
                            prompter.prompt("Please authenticate for: " + ace.getFrameName(), ace.getUsername());
                        }

                        if (ace.getUsername().equals(prompter.getUsername())
                                && ace.getPassword().equals(prompter.getPassword())) {
                            if (ace.isAdministrative()) {
                                login = false;
                                successCount++;
                                handleAdministrativeFrame(prompter, ace);
                            } else {
                                login = false;
                                successCount++;

                                if ((successCount == 1) && (svnHelper != null)) {
                                    ace.getSubversionMap().putAll(svnHelper.getSubversionMap());
                                }

                                HandleNormalFrame handler = new HandleNormalFrame(ace);

                                new Thread(handler, "Frame setup").start();
                            }
                        } else {
                            login = false;

                            int n = JOptionPane.showConfirmDialog(null, "Would you like to try again?", "Login failed",
                                        JOptionPane.YES_NO_OPTION);

                            if (n == JOptionPane.YES_OPTION) {
                                login = true;
                            }
                        }
                    }
                }
            }

            if (successCount == 0) {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                              "No frames where opened. Now exiting.", "No successful logins...",
                                              JOptionPane.ERROR_MESSAGE);
                AceLog.getAppLog().info("### exit from WorkbenchRunner 3.");
                System.exit(0);
            }

            File directory = AceConfig.config.getProfileFile().getParentFile();

            AceConfig.config.save();
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            AceLog.getAppLog().info("### exit from WorkbenchRunner 4.");
            System.exit(0);
        }
    }

    private EnumBasedPreferences loadUserPreferences(String userName) throws BackingStoreException,
            FileNotFoundException, IOException,
            InvalidPreferencesFormatException, Exception {
        
        // Load app info properties from XML.
        Properties appInfoProperties = new Properties();
        File profileRoot = new File("profiles");
        File appInfoPropertiesFile = new File(profileRoot, "appinfo.properties");
        appInfoProperties.loadFromXML(new FileInputStream(appInfoPropertiesFile));
        
        // Load new-style user preferences.        
        String appPrefix = EnumBasedPreferences.getDefaultAppPrefix(appInfoProperties, userName);
        EnumBasedPreferences prefs = new EnumBasedPreferences(appPrefix);
        
        // If user preferences don't already exist, load defaults from file.
        if (prefs.childrenNames().length == 0) {
            File preferencesFile = new File(userProfile.getParentFile(), "Preferences.xml");
            InputStream is = new FileInputStream(preferencesFile);
            Preferences.importPreferences(is);
        }
        
        // Bring up to date.
        prefs.sync();

        return prefs;
    }

    private void checkCustom() {
        String custPropFN = null;

        try {
            if (wbProperties.getProperty(CustomStatics.CUSTOMPROPS) != null) {
                custPropFN = wbProperties.getProperty(CustomStatics.CUSTOMPROPS);
                AceLog.getAppLog().info("checkCustom custPropFN = " + custPropFN);

                File custPropertiesFile = new File("config", custPropFN);

                if (custPropertiesFile.exists() && custPropertiesFile.canRead()) {
                    String     cpfn      = custPropertiesFile.getCanonicalPath();
                    Properties custProps = new Properties();

                    custProps.loadFromXML(new FileInputStream(custPropertiesFile));

                    if (custProps.getProperty(CustomStatics.CUSTOM_UI_CLASSNAME) != null) {
                        String custCN = custProps.getProperty(CustomStatics.CUSTOM_UI_CLASSNAME);

                        AceLog.getAppLog().info("checkCustom custCN = " + custCN);

                        Object obj = ObjectCacheClassHandler.getInstClass(custCN);

                        if (obj != null) {
                            ObjectCache.INSTANCE.put(CustomStatics.CUSTOMPROPSFN, cpfn);
                            ObjectCache.INSTANCE.put(CustomStatics.CUSTOM_UI_CLASS, custCN);
                            ObjectCache.INSTANCE.put(CustomStatics.CUSTOMPROPS, custProps);
                        }
                    }
                }
            }
        } catch (Exception E) {
            AceLog.getAppLog().severe("checkCustom threw an error trying to get " + custPropFN, E);
        }
    }

    private void handleAdministrativeFrame(SvnPrompter prompter, final I_ConfigAceFrame ace) {
        String  username = prompter.getUsername();
        String  password = prompter.getPassword();
        boolean tryAgain = true;

        prompter.setUsername(ace.getAdminUsername());
        prompter.setPassword("");

        while (tryAgain) {
            prompter.prompt("Please authenticate as an administrative user:", ace.getAdminUsername());

            if (ace.getAdminUsername().equals(prompter.getUsername())
                    && ace.getAdminPassword().equals(prompter.getPassword())) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean startup = firstStartup;

                            firstStartup = false;

                            String regularPluginRoot = AceFrame.getPluginRoot();

                            AceFrame.setPluginRoot(AceFrame.getAdminPluginRoot());

                            AceFrame newFrame = new AceFrame(args, ace, startup);

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

    private String getArgString(final String[] args) {
        String argsStr;

        if (args == null) {
            argsStr = "null";
        } else {
            argsStr = Arrays.asList(args).toString();
        }

        return argsStr;
    }

    private static class GetProfileWorker extends SwingWorker<Boolean> {
        StartupFrameListener   fl          = new StartupFrameListener();
        JFrame                 parentFrame = new JFrame();
        boolean                newFrame    = false;
        Dimension              d           = Toolkit.getDefaultToolkit().getScreenSize();
        private File           lastProfileDir;
        CountDownLatch         latch;
        private AceLoginDialog loginDialog;
        private String         password;

        public GetProfileWorker(CountDownLatch latch) {
            super();
            parentFrame = new JFrame();

            if (OpenFrames.getNumOfFrames() > 0) {
                parentFrame = OpenFrames.getFrames().iterator().next();
                AceLog.getAppLog().info("### Adding an existing frame");
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            parentFrame.setContentPane(new JLabel("The Terminology IDE is starting..."));
                            parentFrame.pack();
                            parentFrame.setVisible(true);
                            parentFrame.setLocation((d.width / 2) - (parentFrame.getWidth() / 2),
                                                    (d.height / 2) - (parentFrame.getHeight() / 2));
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

            loginDialog = new AceLoginDialog(parentFrame);
            loginDialog.setLocation((d.width / 2) - (loginDialog.getWidth() / 2),
                                    (d.height / 2) - (loginDialog.getHeight() / 2));
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

            if (wbProperties.getProperty("last-profile-dir") != null) {
                lastProfileDir = new File(wbProperties.getProperty("last-profile-dir"));
            }

            OpenFrames.addFrameListener(fl);

            if (OpenFrames.getNumOfFrames() > 0) {
                parentFrame = OpenFrames.getFrames().iterator().next();
                AceLog.getAppLog().info("### Using an existing frame 1");
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
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
                userProfile = loginDialog.getUserProfile(lastProfileDir);
                password    = new String(loginDialog.getPassword());
                wbProperties.setProperty("last-profile-dir", FileIO.getRelativePath(userProfile));

                if (newFrame) {
                    OpenFrames.removeFrame(parentFrame);
                    parentFrame.setVisible(false);
                }

                OpenFrames.removeFrameListener(fl);
            } catch (TaskFailedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } finally {
                latch.countDown();
            }
        }

        public String getPassword() {
            return password;
        }
    }


    class HandleNormalFrame extends javax.swing.SwingWorker<Object, Object> {
        boolean          startup = firstStartup;
        ;
        I_ConfigAceFrame ace;

        public HandleNormalFrame(I_ConfigAceFrame ace) {
            this.ace = ace;
        }

        @Override
        protected Object doInBackground() throws Exception {
            firstStartup = false;

            if ((ace.getViewPositionSet() == null) || ace.getViewPositionSet().isEmpty()) {
                Set<PositionBI> viewPositions = new HashSet<PositionBI>();

                viewPositions.add(
                    new Position(
                        Long.MAX_VALUE,
                        Bdb.getPathManager().get(ArchitectonicAuxiliary.Concept.SNOMED_CORE.localize().getNid())));
                viewPositions.add(
                    new Position(
                        Long.MAX_VALUE,
                        Bdb.getPathManager().get(
                            ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.localize().getNid())));
                ace.setViewPositions(viewPositions);
            }

            return null;
        }

        @Override
        protected void done() {
            try {
                super.get();

                AceFrame af = new AceFrame(args, ace, startup);

                af.setVisible(true);
            } catch (Exception exception) {
                AceLog.getAppLog().alertAndLogException(exception);
            }
        }
    }


    private static class StartupFrameListener implements ListDataListener {
        @Override
        public void contentsChanged(ListDataEvent arg0) {
            AceLog.getAppLog().info("Contents changed: " + arg0);
        }

        @Override
        public void intervalAdded(ListDataEvent arg0) {
            AceLog.getAppLog().info("intervalAdded: " + arg0);
        }

        @Override
        public void intervalRemoved(ListDataEvent arg0) {
            AceLog.getAppLog().info("intervalRemoved: " + arg0);
        }
    }

    public static void main(String[] args) {
        new WorkbenchRunner(args);
    }
}
