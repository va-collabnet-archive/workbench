package org.ihtsdo.db.runner;

// ~--- non-JDK imports --------------------------------------------------------
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;

import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.commitlog.CommitLog;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.config.AceLoginDialog;
import org.dwfa.ace.config.AceProtocols;
import org.dwfa.ace.config.UIAuthenticator;
import org.dwfa.ace.dnd.DragMonitor;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.ace.tree.ExpandNodeSwingWorker;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.ComponentFrameBean;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.queue.QueueServer;
import org.dwfa.svn.Svn;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.io.FileIO;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.arena.contradiction.ContradictionEditorGenerator;
import org.ihtsdo.batch.BatchActionEditorPanel;
import org.ihtsdo.config.ConfigServiceBase;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.kindof.IsaCache;
import org.ihtsdo.db.bdb.computer.kindof.KindOfComputer;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.objectCache.ObjectCache;
import org.ihtsdo.objectCache.ObjectCacheClassHandler;
import org.ihtsdo.statics.CustomStatics;
import org.ihtsdo.taxonomy.model.NodeFactory;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.coordinate.IsaCoordinate;
import org.intsdo.tk.drools.manager.DroolsExecutionManager;

import com.sun.jini.start.LifeCycle;

public class WorkbenchRunner {

    private static final String WB_PROPERTIES = "wb.properties";
    private static boolean firstStartup = true;
    public static Boolean initializeFromSubversion = false;
    public static Boolean isaCacheCreateOnStartUp = false;
    public static Boolean persistIsaCache = false;
    public static String[] svnUpdateOnStart = null;
    public static String[] args;
    /**
     * The jini configuration provider
     */
    public static Configuration jiniConfig;
    public static LifeCycle lc;
    public static File userProfile;
    public static File wbConfigFile;
    public static Properties wbProperties;

    // TODO Switch?
    public static boolean SSO = false;
    public static String SSO_Key = "SSO";

    public static boolean isSSO() {
        return SSO;
    }

    public static void setSSO(boolean sSO) {
        SSO = sSO;
    }

    public static void setSSO(String[] svnUpdateAr) {

        if (svnUpdateAr != null && svnUpdateAr.length > 0) {

            for (String s : svnUpdateAr) {
                if (s.endsWith("mutable")) {
                    AceLog.getAppLog().info(
                        "svnUpdateOnStart contains a mutable setting to SSO");
                    setSSO(true);
                }
            }

        }

    }

    // ~--- static initializers
    // -------------------------------------------------
    static {
        DwfaEnv.setHeadless(false);
    }

    public static UIAuthenticator auth = new UIAuthenticator();

    // public static SvnPrompter prompt;

    private String authenticate(SvnPrompter prompt, String baseURL) {
        // String result = auth.authenticate(svnH);
        // SvnPrompter prompt = new SvnPrompter();
        String result = auth.authenticate(prompt, baseURL);
        if (result != null) {
            if (result.equals(UIAuthenticator.ERR_NO_PROFILE_S)) {
                AceLog.getAppLog().info(
                    "Problem Authenticating . Problem was " + result);
            }
        }
        userProfile = auth.getProfile();
        if (userProfile == null) {
            // AceLog.getAppLog().info("up is null "+auth.getApm().getProfile().getAbsolutePath());
            AceLog.getAppLog()
                .info("up is null " + auth.getApm().getUserName());
            // here
        }

        /*
         * if(result != null && result.length() > 0){
         * AceLog.getAppLog().info("authenticate Called result = "+result);
         * }
         * 
         * else{
         * AceLog.getAppLog().info(
         * "authenticate Called everything OK prompt User = "
         * +prompt.getUsername()+" pw = "+prompt.getPassword());
         * AceLog.getAppLog().info("authenticate Called profile = "
         * +auth.getProfile().getName());
         * AceLog.getAppLog().info("authenticate debug here");
         * }
         */

        return result;
    }

    // ~--- constructors
    // --------------------------------------------------------
    public WorkbenchRunner(String[] args, LifeCycle lc) {
        try {
            AceProtocols.setupExtraProtocols();
            WorkbenchRunner.args = args;
            WorkbenchRunner.lc = lc;
            AceLog.getAppLog().info(
                "\n*******************\n" + "\n Starting "
                    + this.getClass().getSimpleName() + "\n with config file: "
                    + getArgString(args) + "\n\n******************\n");

            if (new File(args[0]).exists()) {
                jiniConfig =
                    ConfigurationProvider.getInstance(args,
                        getClass().getClassLoader());
            }

            if (jiniConfig != null) {
                wbConfigFile =
                    (File) jiniConfig.getEntry(this.getClass().getName(),
                        "wbConfigFile", File.class,
                        new File("config/wb.config"));
                initializeFromSubversion =
                    (Boolean) jiniConfig.getEntry(this.getClass().getName(),
                        "initFromSubversion", Boolean.class, Boolean.FALSE);
                svnUpdateOnStart =
                    (String[]) jiniConfig.getEntry(this.getClass().getName(),
                        "svnUpdateOnStart", String[].class, null);
                setSSO(svnUpdateOnStart);
                System.out.println("is_SSO: " + isSSO());
                DroolsExecutionManager.drools_dialect_java_compiler =
                    (String) jiniConfig.getEntry(this.getClass().getName(),
                        "drools_dialect_java_compiler", String.class, null);
                ACE.refsetOnly =
                    (Boolean) jiniConfig.getEntry(this.getClass().getName(),
                        "refsetOnly", Boolean.class, Boolean.FALSE);
                ACE.editMode =
                    (Boolean) jiniConfig.getEntry(this.getClass().getName(),
                        "allowEdit", Boolean.class, Boolean.TRUE);
                isaCacheCreateOnStartUp =
                    (Boolean) jiniConfig.getEntry(this.getClass().getName(),
                        "isaCacheCreateOnStartUp", Boolean.class, Boolean.TRUE);
                persistIsaCache =
                    (Boolean) jiniConfig.getEntry(this.getClass().getName(),
                        "persistIsaCache", Boolean.class, Boolean.FALSE);
                EConceptChangeSetWriter.writeDebugFiles =
                    (Boolean) jiniConfig.getEntry(this.getClass().getName(),
                        "writeDebugFiles", Boolean.class, Boolean.FALSE);
                EConceptChangeSetWriter.validateAfterWrite =
                    (Boolean) jiniConfig.getEntry(this.getClass().getName(),
                        "validateAfterWrite", Boolean.class, Boolean.FALSE);

                BatchActionEditorPanel.batchEditingDisabled =
                    (Boolean) jiniConfig.getEntry(this.getClass().getName(),
                        "batchEditingDisabled", Boolean.class, Boolean.FALSE);
            } else {
                wbConfigFile = new File("config/wb.config");
            }

            UIManager.LookAndFeelInfo[] lookAndFeels =
                UIManager.getInstalledLookAndFeels();
            boolean windowsSystem = false;

            for (UIManager.LookAndFeelInfo lookAndFeel : lookAndFeels) {
                if (lookAndFeel.getClassName().contains("Windows")) {
                    windowsSystem = true;
                }
            }

            String lookAndFeelStr = null;

            if (windowsSystem
                && (jiniConfig.getEntry(this.getClass().getName(),
                    "lookAndFeelStr", String.class, null) != null)) {
                lookAndFeelStr =
                    (String) jiniConfig.getEntry(this.getClass().getName(),
                        "lookAndFeelStr", String.class, null);
                System.out.println("LAF: " + lookAndFeelStr);
            } else if (windowsSystem && (lookAndFeelStr == null)) {
                for (UIManager.LookAndFeelInfo lookAndFeel : lookAndFeels) {
                    System.out.println(lookAndFeel.getClassName());

                    if (lookAndFeel.getClassName().contains("WindowsClassic")) {
                        lookAndFeelStr = lookAndFeel.getClassName();
                        System.out.println("Changing LAF to: " + lookAndFeelStr);
                    }
                }
            } else {
                lookAndFeelStr = UIManager.getSystemLookAndFeelClassName();
                System.out.println("Default LAF: " + lookAndFeelStr);
            }

            UIManager.setLookAndFeel(lookAndFeelStr);
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
                // javax.swing.UIManager.put( "ProgressBarUI",
                // "javax.swing.plaf.synth.SynthProgressBarUI");
                // javax.swing.UIManager.put(
                // "javax.swing.plaf.basic.BasicProgressBarUI",
                // Class.forName("javax.swing.plaf.synth.SynthProgressBarUI") );
                // System.getProperties().setProperty("swing.defaultlaf",
                // "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                javax.swing.UIManager.put("ProgressBarUI",
                    "javax.swing.plaf.basic.BasicProgressBarUI");
                javax.swing.UIManager.put("ProgressBar.foreground", Color.GREEN);
                javax.swing.UIManager.put("ProgressBar.background", Color.GRAY);
                javax.swing.UIManager.put("ProgressBar.border",
                    BorderFactory.createRaisedBevelBorder());
                javax.swing.UIManager.put(
                    "javax.swing.plaf.basic.BasicProgressBarUI",
                    Class.forName("javax.swing.plaf.basic.BasicProgressBarUI"));
            }

            OpenFrames.addNewWindowMenuItemGenerator(new ContradictionEditorGenerator());
            System.setProperty("javax.net.ssl.trustStore", "config/cacerts");

            long startTime = System.currentTimeMillis();
            ActivityPanel activity = new ActivityPanel(null, true);

            activity.setIndeterminate(true);
            activity.setProgressInfoUpper("Loading the database");
            activity.setProgressInfoLower("Setting up the environment...");
            activity.addRefreshActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("System.exit from activity action listener: "
                        + e.getActionCommand());
                    System.exit(0);
                }
            });
            ActivityViewer.addActivity(activity);

            if ((System.getProperty("viewer") != null)
                && System.getProperty("viewer").toLowerCase().startsWith("t")) {
                ACE.editMode = false;
            }

            File wbPropertiesFile = new File("config", WB_PROPERTIES);
            boolean acePropertiesFileExists = wbPropertiesFile.exists();

            wbProperties = new Properties();

            SvnPrompter prompter = new SvnPrompter();
            boolean initialized = false;

            if (acePropertiesFileExists) {
                wbProperties.loadFromXML(new FileInputStream(wbPropertiesFile));
                initialized =
                    Boolean.parseBoolean((String) wbProperties.get("initialized"));
                if (wbProperties != null) {
                    String SSOVal = wbProperties.getProperty(SSO_Key);
                    AceLog.getAppLog().info("SSOVal = "+SSOVal);
                    if (SSOVal != null && SSOVal.length() > 0
                        && SSOVal.equalsIgnoreCase("yes")) {
                        setSSO(true);
                    }
                }

            }

            SvnHelper svnHelper =
                new SvnHelper(WorkbenchRunner.class, jiniConfig);

            if (SSO) {
                String testSVNURL = svnHelper.getSvnCheckoutProfileOnStart();
                // AceLog.getAppLog().info("About to open the init svn dialog svnCheckoutProfileOnStart = "+testSVNURL);
                // TODO throw some sort of error if url is empty or null
                String auth_e_msg = authenticate(Svn.getPrompter(), testSVNURL);

            }

            if ((acePropertiesFileExists == false) || (initialized == false)) {
                try {
                    if (!SSO) {
                        svnHelper.initialSubversionOperationsAndChangeSetImport(
                            wbPropertiesFile, prompter);
                    } else {
                        boolean ok =
                            svnHelper.initialSubversionOperationsAndChangeSetImport(
                                wbPropertiesFile, Svn.getPrompter());
                    }
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                    System.exit(0);
                }
            }

            wbProperties.put("initialized", "true");

            File profileDir = new File("profiles");

            if (((profileDir.exists() == false) && initializeFromSubversion)
                || (svnUpdateOnStart != null)) {
                Svn.setConnectedToSvn(true);
                if (!SSO) {
                    svnHelper.initialSubversionOperationsAndChangeSetImport(
                        new File("config", WB_PROPERTIES), prompter);
                } else {
                    svnHelper.initialSubversionOperationsAndChangeSetImport(
                        new File("config", WB_PROPERTIES), Svn.getPrompter());
                }
            }

            // Check to see if there is a custom Properties file
            checkCustom();

            if ((wbConfigFile == null) || !wbConfigFile.exists()) {
                if (acePropertiesFileExists) {
                    wbProperties.loadFromXML(new FileInputStream(
                        wbPropertiesFile));
                }

                String lastProfileDirStr = "profiles";

                if (wbProperties.getProperty("last-profile-dir") != null) {
                    lastProfileDirStr =
                        wbProperties.getProperty("last-profile-dir");
                }

                File lastProfileDir = new File(lastProfileDirStr);

                if (lastProfileDir.isFile()) {
                    wbConfigFile = lastProfileDir;
                } else {
                    String[] profileFiles =
                        lastProfileDir.list(new FilenameFilter() {

                            @Override
                            public boolean accept(File dir, String name) {
                                return name.endsWith(".ace")
                                    || name.endsWith(".wb")
                                    || name.endsWith(".wbp");
                            }
                        });

                    if ((profileFiles != null) && (profileFiles.length == 1)) {
                        wbConfigFile =
                            new File(lastProfileDir, profileFiles[0]).getCanonicalFile();
                    } else if ((profileFiles != null)
                        && (profileFiles.length > 1)) {
                        AceLog.getAppLog()
                            .warning(
                                "Profile from jini configuration does not exist and more than one profile file found in "
                                    + "last profile directory "
                                    + lastProfileDir
                                    + ", unable to determine profile to use.");
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
            Runtime.getRuntime().addShutdownHook(
                new Thread(this.getClass().getCanonicalName()) {

                    /**
                     * TODO For some reason, this thread does not seem to run in
                     * normal shutdown (only on ^c). Need to figure out why and
                     * make
                     * sure that we never fail to gracefully shutdown the
                     * database.
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
            setupSwingExpansionTimerLogging();

            if (System.getProperty("newprofile") != null) {
                File dbFolder = new File("berkeley-db");

                if (jiniConfig != null) {
                    dbFolder =
                        (File) jiniConfig.getEntry(this.getClass().getName(),
                            "dbFolder", File.class, new File(
                                "target/berkeley-db"));
                }

                AceConfig.config = new AceConfig(dbFolder);

                String username = "username";
                File profileFile =
                    new File("profiles/" + username, username + ".wb");

                AceConfig.config.setProfileFile(profileFile);
                AceConfig.setupAceConfig(AceConfig.config, null, null, false);

                File startupFolder =
                    new File(profileFile.getParent(), "startup");

                startupFolder.mkdirs();

                File shutdownFolder =
                    new File(profileFile.getParent(), "shutdown");

                shutdownFolder.mkdirs();
            }

            if (svnHelper != null) {
                svnHelper.doChangeSetImport();
            }

            if (SSO) {
                if (userProfile == null) {

                    AceLog.getAppLog().info(
                        "up is null " + auth.getApm().getUserName());
                    auth.getApm().processProfiles();
                    userProfile = auth.getApm().getProfile();

                    if (userProfile == null) {
                        JOptionPane.showMessageDialog(
                            LogWithAlerts.getActiveFrame(null),
                            "Unable to find a profile for user = "
                                + auth.getApm().getUserName());
                        return;
                    }
                }
            }
            if ((wbConfigFile != null) && wbConfigFile.exists()
                && wbPropertiesFile.exists()) {

                // Put up a dialog to select the configuration file...
                CountDownLatch latch = new CountDownLatch(1);
                GetProfileWorker profiler = new GetProfileWorker(latch);

                profiler.start();
                latch.await();

                File jeUserPropertiesFile =
                    new File(userProfile.getParentFile(), "je.properties");

                if (jeUserPropertiesFile.exists()) {
                    File jeDbPropertiesFile =
                        new File(AceConfig.config.getDbFolder(),
                            "je.properties");

                    FileIO.copyFile(jeUserPropertiesFile, jeDbPropertiesFile);
                }

                ObjectInputStream ois =
                    new ObjectInputStream(new BufferedInputStream(
                        new FileInputStream(userProfile)));

                try {
                    AceConfig.config = (AceConfig) ois.readObject();
                    AceConfig.config.setProfileFile(userProfile);

                    if (!SSO) {
                        AceConfig.config.getUsername();
                        prompter.setUsername(AceConfig.config.getUsername());
                        prompter.setPassword(profiler.getPassword());
                    } else {
                        AceLog.getAppLog().info(
                            "AceConfig UserName = "
                                + AceConfig.config.getUsername()
                                + " prompt.getUsername() = "
                                + Svn.getPrompter().getUsername());

                        if (!Svn.getPrompter()
                            .getUsername()
                            .equalsIgnoreCase(AceConfig.config.getUsername())) {
                            AceLog.getAppLog().info(
                                "AceConfig UserName not the same so setting");
                            AceConfig.config.setUsername(Svn.getPrompter()
                                .getUsername());
                            AceConfig.config.save();

                            ObjectInputStream ois2 =
                                new ObjectInputStream(new BufferedInputStream(
                                    new FileInputStream(userProfile)));
                            AceConfig.config = (AceConfig) ois2.readObject();
                            AceConfig.config.setProfileFile(userProfile);
                            AceLog.getAppLog().info(
                                "AceConfig UserName2 = "
                                    + AceConfig.config.getUsername());
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        LogWithAlerts.getActiveFrame(null),
                        "Unable to open user file. Is it corrupt?");
                    AceLog.getAppLog().log(Level.SEVERE,
                        ex.getLocalizedMessage(), ex);
                    System.exit(-1);
                } finally {
                    ois.close();
                }
            } else {
                if (initializeFromSubversion) {
                    JOptionPane.showMessageDialog(
                        LogWithAlerts.getActiveFrame(null),
                        "Unable to initialize from subversion. Is the network connected?");
                } else {
                    File dbFolder = new File("berkeley-db");

                    if (jiniConfig != null) {
                        dbFolder =
                            (File) jiniConfig.getEntry(this.getClass()
                                .getName(), "dbFolder", File.class, new File(
                                "target/berkeley-db"));
                    }

                    AceConfig.config = new AceConfig(dbFolder);
                    AceConfig.config.setProfileFile(wbConfigFile);
                    AceConfig.setupAceConfig(AceConfig.config, null, null,
                        false);
                }
            }

            wbProperties.storeToXML(new FileOutputStream(wbPropertiesFile),
                null);
            ACE.setAceConfig(AceConfig.config);

            String writerName = AceConfig.config.getChangeSetWriterFileName();

            if (!writerName.endsWith(".eccs")) {
                String firstPart =
                    writerName.substring(0, writerName.lastIndexOf('.'));

                writerName = firstPart.concat(".eccs");
                AceConfig.config.setChangeSetWriterFileName(writerName);
            }

            ChangeSetWriterHandler.addWriter(
                AceConfig.config.getUsername() + ".eccs",
                new EConceptChangeSetWriter(new File(
                    AceConfig.config.getChangeSetRoot(),
                    AceConfig.config.getChangeSetWriterFileName()), new File(
                    AceConfig.config.getChangeSetRoot(), "."
                        + AceConfig.config.getChangeSetWriterFileName()),
                    ChangeSetGenerationPolicy.INCREMENTAL, true));
            ChangeSetWriterHandler.addWriter(
                AceConfig.config.getUsername() + ".commitLog.xls",
                new CommitLog(new File(AceConfig.config.getChangeSetRoot(),
                    "commitLog.xls"), new File(
                    AceConfig.config.getChangeSetRoot(), "." + "commitLog.xls")));

            int successCount = 0;
            int frameCount = 0;

            for (final I_ConfigAceFrame ace : AceConfig.config.aceFrames) {
                frameCount++;

                if (ace.isActive()) {
                    AceFrameConfig afc = (AceFrameConfig) ace;

                    afc.setMasterConfig(AceConfig.config);

                    boolean login = true;
                    if (!SSO) {
                        while (login) {
                            if (frameCount == 1) {
                                if (ace.getPassword().equals(
                                    prompter.getPassword())) {
                                    if (ace.getUsername().equals(
                                        prompter.getUsername()) == false) {
                                        AceConfig.config.setUsername(ace.getUsername());
                                        prompter.setUsername(ace.getUsername());
                                    }
                                } else {
                                    prompter.prompt("Please authenticate for: "
                                        + ace.getFrameName(), ace.getUsername());

                                    if (ace.getUsername().equals(
                                        prompter.getUsername()) == false) {
                                        AceConfig.config.setUsername(ace.getUsername());
                                        prompter.setUsername(ace.getUsername());
                                    }
                                }
                            } else if ((ace.getUsername().equals(
                                prompter.getUsername()) == false)
                                || (ace.getPassword().equals(
                                    prompter.getPassword()) == false)) {
                                prompter.prompt("Please authenticate for: "
                                    + ace.getFrameName(), ace.getUsername());
                            }

                            if (ace.getUsername()
                                .equals(prompter.getUsername())
                                && ace.getPassword().equals(
                                    prompter.getPassword())) {
                                if (ace.isAdministrative()) {
                                    login = false;
                                    successCount++;
                                    handleAdministrativeFrame(prompter, ace);
                                } else {
                                    login = false;
                                    successCount++;

                                    if ((successCount == 1)
                                        && (svnHelper != null)) {
                                        ace.getSubversionMap().putAll(
                                            svnHelper.getSubversionMap());
                                    }

                                    HandleNormalFrame handler =
                                        new HandleNormalFrame(ace);

                                    new Thread(handler, "Frame setup").start();
                                }
                            } else {
                                login = false;

                                int n =
                                    JOptionPane.showConfirmDialog(null,
                                        "Would you like to try again?",
                                        "Login failed",
                                        JOptionPane.YES_NO_OPTION);

                                if (n == JOptionPane.YES_OPTION) {
                                    login = true;
                                }
                            }
                        }
                    } else {
                        if (ace.isAdministrative()) {
                            successCount++;
                            AceLog.getAppLog().info(
                                "About to handleAdministrativeFrame");
                            handleAdministrativeFrame(Svn.getPrompter(), ace);
                        } else {
                            successCount++;
                            // AceLog.getAppLog().info("Workbench runner 517 successCount = "+successCount);
                            if (successCount == 1 && svnHelper != null) {
                                /*
                                 * AceLog.getAppLog().info(
                                 * "Workbench runner 519 svnHelper.getSubversionMap().size = "
                                 * +svnHelper.getSubversionMap().size());
                                 * for (String key :
                                 * svnHelper.getSubversionMap().keySet()) {
                                 * SubversionData svd =
                                 * svnHelper.getSubversionMap().get(key);
                                 * AceLog.getAppLog().info(
                                 * " Printing svnHelper.getSubversionMap key = "
                                 * + key+" SubversionData = "+svd);
                                 * }
                                 * 
                                 * AceLog.getAppLog().info(
                                 * "Workbench runner 520 ace.getSubversionMap().size() 1 = "
                                 * +ace.getSubversionMap().size());
                                 * for (String key :
                                 * ace.getSubversionMap().keySet()) {
                                 * SubversionData svd =
                                 * ace.getSubversionMap().get(key);
                                 * AceLog.getAppLog().info(" Printing ace key = "
                                 * + key+" SubversionData = "+svd);
                                 * }
                                 */

                                ace.getSubversionMap().clear();
                                ace.getSubversionMap().putAll(
                                    svnHelper.getSubversionMap());
                                /*
                                 * AceLog.getAppLog().info(
                                 * "Workbench runner 534 ace.getSubversionMap().size() = "
                                 * +ace.getSubversionMap().size());
                                 * for (String key :
                                 * ace.getSubversionMap().keySet()) {
                                 * SubversionData svd =
                                 * ace.getSubversionMap().get(key);
                                 * AceLog.getAppLog().info(
                                 * " Printing ace svnMap info key = " +
                                 * key+" SubversionData = "+svd);
                                 * }
                                 */

                            }
                            // AceLog.getAppLog().info("About to handleNormalFrame");
                            // handleNormalFrame(ace);
                            HandleNormalFrame handler =
                                new HandleNormalFrame(ace);

                            new Thread(handler, "Frame setup").start();

                        }
                    }

                }
            }

            if (successCount == 0) {
                JOptionPane.showMessageDialog(
                    LogWithAlerts.getActiveFrame(null),
                    "No frames where opened. Now exiting.",
                    "No successful logins...", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            File directory = AceConfig.config.getProfileFile().getParentFile();

            if (directory.listFiles() != null) {
                for (File dir : directory.listFiles()) {
                    processFile(dir, lc);
                }
            }

            AceConfig.config.save();

            // Startup other queues here...
            List<String> queuesToRemove = new ArrayList<String>();

            for (String queue : AceConfig.config.getQueues()) {
                queue = queue.replace('\\', '/');

                File queueFile = new File(queue);

                if (queueFile.exists()) {
                    AceLog.getAppLog().info(
                        "Found queue: "
                            + queueFile.toURI().toURL().toExternalForm());

                    if (QueueServer.started(queueFile)) {
                        AceLog.getAppLog().info(
                            "Queue already started: "
                                + queueFile.toURI().toURL().toExternalForm());
                    } else {
                        new QueueServer(
                            new String[] { queueFile.getCanonicalPath() }, lc);
                    }
                } else {
                    queuesToRemove.add(queue);
                }
            }

            if (queuesToRemove.size() > 0) {
                AceConfig.config.getQueues().removeAll(queuesToRemove);

                StringBuilder buff = new StringBuilder();

                buff.append("<html><body>Removing queues that are not accessible: <br>");

                for (String queue : queuesToRemove) {
                    buff.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                    buff.append(queue);
                    buff.append("<br>");
                }

                buff.append("</body></html>");
                AceLog.getAppLog().alertAndLog(
                    Level.WARNING,
                    buff.toString(),
                    new Exception("Removing queues that are not accessable: "
                        + queuesToRemove));
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            System.exit(0);
        }
    }

    // ~--- methods
    // -------------------------------------------------------------
    private void checkCustom() {
        String custPropFN = null;

        try {
            if (wbProperties.getProperty(CustomStatics.CUSTOMPROPS) != null) {
                custPropFN =
                    wbProperties.getProperty(CustomStatics.CUSTOMPROPS);
                AceLog.getAppLog().info(
                    "checkCustom custPropFN = " + custPropFN);

                File custPropertiesFile = new File("config", custPropFN);

                if (custPropertiesFile.exists() && custPropertiesFile.canRead()) {
                    String cpfn = custPropertiesFile.getCanonicalPath();
                    Properties custProps = new Properties();

                    custProps.loadFromXML(new FileInputStream(
                        custPropertiesFile));

                    ObjectCache.INSTANCE.put(CustomStatics.CUSTOMPROPSFN, cpfn);
                    ObjectCache.INSTANCE.put(CustomStatics.CUSTOMPROPS,
                        custProps);

                    // Add the UI key if there
                    String uiClass =
                        custProps.getProperty(CustomStatics.CUSTOM_UI_CLASS);
                    AceLog.getAppLog().info("checkCustom uiClass = " + uiClass);
                    if (uiClass != null) {
                        ObjectCache.INSTANCE.put(CustomStatics.CUSTOM_UI_CLASS,
                            uiClass);
                    }

                    if (custProps.getProperty(CustomStatics.CONFIG_SERVICE_CLASSNAME) != null) {
                        String custCN =
                            custProps.getProperty(CustomStatics.CONFIG_SERVICE_CLASSNAME);

                        AceLog.getAppLog().info(
                            "checkCustom custCN = " + custCN);

                        ConfigServiceBase csb =
                            (ConfigServiceBase) ObjectCacheClassHandler.getInstClass(custCN);

                        if (csb != null) {
                            csb.init();
                            ObjectCache.INSTANCE.put(
                                CustomStatics.CONFIG_SERVICE, csb);
                        }
                    }
                }

            }
        } catch (Exception E) {
            AceLog.getAppLog().severe(
                "checkCustom threw an error trying to get " + custPropFN, E);
        }
    }

    private void generateCache(Collection<IsaCoordinate> isaCoordinates,
            List<CountDownLatch> latches) throws InterruptedException,
            IOException {
        // not persisted
        for (IsaCoordinate isac : isaCoordinates) {
            IsaCache loopCache = (IsaCache) Terms.get().setupIsaCache(isac);
            latches.add(loopCache.getLatch());
        }

        // Await isa cache finalization
        for (CountDownLatch latch : latches) {
            latch.await();
        }
    }

    private void handleAdministrativeFrame(SvnPrompter prompter,
            final I_ConfigAceFrame ace) {
        String username = prompter.getUsername();
        String password = prompter.getPassword();
        boolean tryAgain = true;

        prompter.setUsername(ace.getAdminUsername());
        prompter.setPassword("");

        while (tryAgain) {
            prompter.prompt("Please authenticate as an administrative user:",
                ace.getAdminUsername());

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

                            AceFrame newFrame =
                                new AceFrame(args, lc, ace, startup);

                            AceFrame.setPluginRoot(regularPluginRoot);
                            ace.setSubversionToggleVisible(true);
                            newFrame.setTitle(newFrame.getTitle().replace(
                                "Editor", "Administrator"));
                            newFrame.setVisible(true);
                        } catch (Exception e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }
                    }
                });
                tryAgain = false;
                prompter.setPassword("");
            } else {
                int n =
                    JOptionPane.showConfirmDialog(null,
                        "Would you like to try again?",
                        "Administrative authentication failed",
                        JOptionPane.YES_NO_OPTION);

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

    private void processFile(File file, LifeCycle lc) throws Exception {
        if (file.isDirectory() == false) {
            if (file.getName().equalsIgnoreCase("queue.config")
                && (QueueServer.started(file) == false)) {
                AceLog.getAppLog().info(
                    "Found user queue: " + file.getCanonicalPath());
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

    private void setupIsaCache(Collection<IsaCoordinate> isaCoordinates)
            throws Exception, ComputationCanceled, InterruptedException,
            IOException {

        // Startup queues in profile sub-directories here...
        // Isa Cache Setup start
        long isaStartTime = System.currentTimeMillis();
        ActivityPanel activityIsa = new ActivityPanel(null, true);

        activityIsa.setIndeterminate(true);
        activityIsa.setStopButtonVisible(false);
        activityIsa.setProgressInfoUpper("Isa Cache preparation");
        activityIsa.setProgressInfoLower("Setting up is-a cache...");
        ActivityViewer.addActivity(activityIsa);

        List<CountDownLatch> latches = new ArrayList<CountDownLatch>();

        if (isaCacheCreateOnStartUp) {
            if (!persistIsaCache) {
                File cacheFile = new File("berkeley-db/isa-cache.oos");
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
                activityIsa.setProgressInfoLower("Generating is-a cache...");
                generateCache(isaCoordinates, latches);
            } else {
                KindOfComputer.persistIsaCache = true;
                // persisted
                activityIsa.setProgressInfoLower("Reading is-a cache...");
                if (!KindOfComputer.loadIsaCacheFromFile(new File(
                    "berkeley-db/isa-cache.oos"), isaCoordinates)) {
                    KindOfComputer.clearIsaCache();
                    activityIsa.setProgressInfoLower("Failed to load, regenerating is-a cache...");
                    generateCache(isaCoordinates, latches);
                }
            }

            long isaLoadTime = System.currentTimeMillis() - isaStartTime;
            String elapsedStr = TimeHelper.getElapsedTimeString(isaLoadTime);

            activityIsa.setProgressInfoLower("Elapsed: " + elapsedStr);
            activityIsa.complete();
        } else {
            activityIsa.setProgressInfoLower("Configured to not generate Isa Cache on startup");
            activityIsa.complete();
        }
    }

    private void setupSwingExpansionTimerLogging()
            throws ConfigurationException {
        if (jiniConfig != null) {
            Boolean logTimingInfo =
                (Boolean) jiniConfig.getEntry(this.getClass().getName(),
                    "logTimingInfo", Boolean.class, null);

            if (logTimingInfo != null) {
                ExpandNodeSwingWorker.setLogTimingInfo(logTimingInfo);
            }

            AceLog.getAppLog().info(
                "Swing expansion logTimingInfo " + logTimingInfo);
        }
    }

    // ~--- get methods
    // ---------------------------------------------------------
    private String getArgString(final String[] args) {
        String argsStr;

        if (args == null) {
            argsStr = "null";
        } else {
            argsStr = Arrays.asList(args).toString();
        }

        return argsStr;
    }

    // ~--- inner classes
    // -------------------------------------------------------
    private static class GetProfileWorker extends SwingWorker<Boolean> {

        StartupFrameListener fl = new StartupFrameListener();
        JFrame parentFrame = new JFrame();
        boolean newFrame = false;
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        private File lastProfileDir;
        CountDownLatch latch;
        private AceLoginDialog loginDialog;
        private String password;

        // ~--- constructors
        // -----------------------------------------------------
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
                            parentFrame.setContentPane(new JLabel(
                                "The Terminology IDE is starting..."));
                            parentFrame.pack();
                            parentFrame.setVisible(true);
                            parentFrame.setLocation((d.width / 2)
                                - (parentFrame.getWidth() / 2), (d.height / 2)
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
            if (!SSO) {
                loginDialog = new AceLoginDialog(parentFrame);
                loginDialog.setLocation((d.width / 2)
                    - (loginDialog.getWidth() / 2), (d.height / 2)
                    - (loginDialog.getHeight() / 2));
            }
            this.latch = latch;
        }

        // ~--- methods
        // ----------------------------------------------------------
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
                lastProfileDir =
                    new File(wbProperties.getProperty("last-profile-dir"));
            }

            OpenFrames.addFrameListener(fl);

            if (OpenFrames.getNumOfFrames() > 0) {
                parentFrame = OpenFrames.getFrames().iterator().next();
                AceLog.getAppLog().info("### Using an existing frame 1");
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        parentFrame.setContentPane(new JLabel(
                            "The Terminology IDE is starting..."));
                        parentFrame.pack();

                        Dimension d =
                            Toolkit.getDefaultToolkit().getScreenSize();

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
                if (!isSSO()) {
                    // shows the AceLoginDialog
                    userProfile = loginDialog.getUserProfile(lastProfileDir);
                    password = new String(loginDialog.getPassword());
                    wbProperties.setProperty("last-profile-dir",
                        FileIO.getRelativePath(userProfile));
                }
                if (newFrame) {
                    OpenFrames.removeFrame(parentFrame);
                    parentFrame.setVisible(false);
                }

                OpenFrames.removeFrameListener(fl);
                latch.countDown();
            } catch (TaskFailedException e) {
                latch.countDown();
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

        // ~--- get methods
        // ------------------------------------------------------
        public String getPassword() {
            return password;
        }
    }

    class HandleNormalFrame extends javax.swing.SwingWorker<Object, Object> {

        boolean startup = firstStartup;;
        I_ConfigAceFrame ace;

        // ~--- constructors
        // -----------------------------------------------------
        public HandleNormalFrame(I_ConfigAceFrame ace) {
            this.ace = ace;
        }

        // ~--- methods
        // ----------------------------------------------------------
        @Override
        protected Object doInBackground() throws Exception {
            firstStartup = false;

            if ((ace.getViewPositionSet() == null)
                || ace.getViewPositionSet().isEmpty()) {
                Set<PositionBI> viewPositions = new HashSet<PositionBI>();

                viewPositions.add(new Position(Long.MAX_VALUE,
                    Bdb.getPathManager().get(
                        ArchitectonicAuxiliary.Concept.SNOMED_CORE.localize()
                            .getNid())));
                viewPositions.add(new Position(
                    Long.MAX_VALUE,
                    Bdb.getPathManager()
                        .get(
                            ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.localize()
                                .getNid())));
                ace.setViewPositions(viewPositions);
            }

            setupIsaCache(ace.getViewCoordinate().getIsaCoordinates());

            return null;
        }

        @Override
        protected void done() {
            try {
                super.get();

                AceFrame af = new AceFrame(args, lc, ace, startup);

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

    public static SvnPrompter getPrompt() {
        return Svn.getPrompter();
    }

}
