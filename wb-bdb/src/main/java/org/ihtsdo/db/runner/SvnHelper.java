package org.ihtsdo.db.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.cs.ChangeSetImporter;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.SelectObjectDialog;
import org.dwfa.svn.Svn;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.cs.econcept.EConceptChangeSetReader;
import org.ihtsdo.cs.econcept.workflow.WfRefsetChangeSetReader;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbPathManager;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.Ts;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Depth;
import org.tigris.subversion.javahl.Revision;
import org.tmatesoft.svn.core.SVNErrorCode;

/**
 * Common SVN helper methods used by the Ace Runner and AceLoginDialog.
 * 
 * @author ean dungey
 * @author kec
 */
public class SvnHelper {

    private String svnCheckoutProfileOnStart = null;
    private String[] svnCheckoutOnStart = null;
    private String[] svnUpdateOnStart = null;
    private String[] csImportOnStart = null;
    private List<File> changeLocations = new ArrayList<File>();
    private Class<?> jiniClass;
    private static boolean connectToSubversion = false;
    private Map<String, SubversionData> subversionMap = new HashMap<String, SubversionData>();
    private int numberOfNonUserDirsInProfilesDir = 2; // startup and shutdown

    public Map<String, SubversionData> getSubversionMap() {
        return subversionMap;
    }

    public void setSubversionMap(Map<String, SubversionData> subversionMap) {
        this.subversionMap = subversionMap;
    }

    public SvnHelper(Class<?> jiniClassToSet, Properties svnConfigProperties) throws ConfigurationException {
        jiniClass = jiniClassToSet;
        Properties svnProperties = svnConfigProperties;
        if (svnProperties != null) {

            svnCheckoutProfileOnStart = svnProperties.getProperty("svnCheckoutProfileOnStart");
            
            String svnCheckoutOnStartStr = svnProperties.getProperty("svnCheckoutOnStart");
            if (svnCheckoutOnStartStr != null) {
                svnCheckoutOnStart = svnCheckoutOnStartStr.split(";");
            }
            
            String svnUpdateOnStartStr = svnProperties.getProperty("svnUpdateOnStart");
            if (svnUpdateOnStartStr != null) {
                svnUpdateOnStart = svnUpdateOnStartStr.split(";");
            }
            
            String csImportOnStartStr = svnProperties.getProperty("csImportOnStart");
            if (csImportOnStartStr != null) {
                csImportOnStart = csImportOnStartStr.split(";");
                if (csImportOnStart != null) {
                    for (String importLoc : csImportOnStart) {
                        changeLocations.add(new File(importLoc));
                    }
                }
            }
        }

    }

    void initialSubversionOperationsAndChangeSetImport(File acePropertiesFile, boolean connectToSubversion,
            SvnPrompter prompter) throws FileNotFoundException, IOException, TaskFailedException,
            ClientException {
        Properties aceProperties = new Properties();
        aceProperties.setProperty("initial-svn-checkout", "true");

        if ((svnCheckoutOnStart != null && svnCheckoutOnStart.length > 0)
                || (svnUpdateOnStart != null && svnUpdateOnStart.length > 0)
                || (svnCheckoutProfileOnStart != null && svnCheckoutProfileOnStart.length() > 0)) {

            if (connectToSubversion) {
                long startTime = System.currentTimeMillis();
                I_ShowActivity activity = Svn.setupActivityPanel("Subversion startup operation");
                Svn.rwl.acquireUninterruptibly(Svn.SEMAPHORE_PERMITS);
                try {
                    if (svnCheckoutProfileOnStart != null && svnCheckoutProfileOnStart.length() > 0) {
                        handleSvnProfileCheckout(aceProperties);
                    }

                    if (svnCheckoutOnStart != null && svnCheckoutOnStart.length > 0) {
                        for (String svnSpec : svnCheckoutOnStart) {
                            activity.setProgressInfoLower("Checkout: " + svnSpec.substring(0, svnSpec.indexOf('|')));
                            handleSvnCheckout(changeLocations, svnSpec);
                        }
                    }

                    if (svnUpdateOnStart != null && svnUpdateOnStart.length > 0) {
                        for (String svnSpec : svnUpdateOnStart) {
                            activity.setProgressInfoLower("Update: " + svnSpec);
                            handleSvnUpdate(changeLocations, svnSpec, prompter);
                        }
                    }

                } catch (ClientException e) {
                    if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                "Unable to connect to Subversion - please check network connection and try again.",
                                "Unable to connect", JOptionPane.INFORMATION_MESSAGE);
                        connectToSubversion = false;
                        Svn.setConnectedToSvn(connectToSubversion);
                        AceLog.getAppLog().info(
                                "### Unable to connect to Subversion - please check network connection and try again.");
                        System.exit(0);
                    } else {
                        AceLog.getAppLog().info("### User cancelled Subversion log in.");
                        System.exit(0);
                    }
                } finally {
                    Svn.rwl.release(Svn.SEMAPHORE_PERMITS);
                    try {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        String elapsed = "Elapsed time: " + TimeUtil.getElapsedTimeString(elapsedTime);
                        activity.setProgressInfoLower(elapsed);
                        activity.complete();
                    } catch (ComputationCanceled e) {
                        throw new TaskFailedException(e);
                    }
                }
                if (changeLocations.size() > 0) {
                    doChangeSetImport();
                }
                aceProperties.storeToXML(new FileOutputStream(acePropertiesFile), null);
            } else {
                if (new File("profiles").exists() == false) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "No profiles are available. Please re-run bundle and checkout profiles from Subversion.",
                            "No profiles", JOptionPane.INFORMATION_MESSAGE);
                    connectToSubversion = false;
                    Svn.setConnectedToSvn(connectToSubversion);
                    System.out.println("Exiting from initialSubversionOperationsAndChangeSetImport");
                    System.exit(0);
                }
            }
        } else if (changeLocations.size() > 0) {
            doChangeSetImport();
        }
    }

    public void initialSubversionOperationsAndChangeSetImport(File acePropertiesFile, SvnPrompter prompter)
            throws FileNotFoundException, IOException, TaskFailedException, ClientException {

        Properties wbProperties = new Properties();
        wbProperties.setProperty("initial-svn-checkout", "true");

        if ((svnCheckoutOnStart != null && svnCheckoutOnStart.length > 0)
                || (svnUpdateOnStart != null && svnUpdateOnStart.length > 0)
                || (svnCheckoutProfileOnStart != null && svnCheckoutProfileOnStart.length() > 0)) {
            if (connectToSubversion == false) {
                connectToSubversion =
                        (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(LogWithAlerts.getActiveFrame(null),
                        "Would you like to connect over the network to Subversion?", "Confirm network operation",
                        JOptionPane.YES_NO_OPTION));
            }
            Svn.setConnectedToSvn(connectToSubversion);
            try {
                if (connectToSubversion) {
                    long startTime = System.currentTimeMillis();
                    I_ShowActivity activity = Svn.setupActivityPanel("Subversion startup operation");
                    Svn.rwl.acquireUninterruptibly(Svn.SEMAPHORE_PERMITS);
                    try {
                        if (svnCheckoutProfileOnStart != null && svnCheckoutProfileOnStart.length() > 0) {
                            handleSvnProfileCheckout(wbProperties);
                        }

                        if (svnCheckoutOnStart != null && svnCheckoutOnStart.length > 0) {
                            for (String svnSpec : svnCheckoutOnStart) {
                                activity.setProgressInfoLower("Checkout: " + svnSpec);
                                handleSvnCheckout(changeLocations, svnSpec);
                            }
                        }

                        if (svnUpdateOnStart != null && svnUpdateOnStart.length > 0) {
                            for (String svnSpec : svnUpdateOnStart) {
                                activity.setProgressInfoLower("Update: " + svnSpec);
                                handleSvnUpdate(changeLocations, svnSpec, prompter);
                            }
                        }
                    } finally {
                        Svn.rwl.release(Svn.SEMAPHORE_PERMITS);
                        try {
                            long elapsedTime = System.currentTimeMillis() - startTime;
                            String elapsed = "Elapsed time: " + TimeUtil.getElapsedTimeString(elapsedTime);
                            activity.setProgressInfoLower(elapsed);
                            activity.complete();
                        } catch (ComputationCanceled e) {
                            throw new TaskFailedException(e);
                        }
                    }
                    if (changeLocations.size() > 0) {
                        doChangeSetImport();
                    }
                    wbProperties.storeToXML(new FileOutputStream(acePropertiesFile), null);
                } else {
                    if (new File("profiles").exists() == false) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                "No profiles are available. Please re-run bundle and checkout profiles from Subversion.",
                                "No profiles", JOptionPane.INFORMATION_MESSAGE);
                        connectToSubversion = false;
                        Svn.setConnectedToSvn(connectToSubversion);
                        AceLog.getAppLog().info("### User cancelled Subversion log in.");
                        System.exit(0);
                    }
                }
            } catch (ClientException e) {
                if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "Unable to connect to Subversion - please check network connection and try again.",
                            "Unable to connect", JOptionPane.INFORMATION_MESSAGE);
                    connectToSubversion = false;
                    Svn.setConnectedToSvn(connectToSubversion);
                    AceLog.getAppLog().info(
                            "### Unable to connect to Subversion - please check network connection and try again.");
                    System.exit(0);
                } else {
                    AceLog.getAppLog().info("### exit from initialSubversionOperationsAndChangeSetImport.");
                    System.exit(0);
                }
            }
        } else if (changeLocations.size() > 0) {
            doChangeSetImport();
        }
    }

    void handleSvnProfileCheckout(Properties aceProperties) throws ClientException, TaskFailedException {
        if (new File("profiles").exists()) {
            if (aceProperties.getProperty("last-profile-dir") != null) {
                // A checkout has previously completed. 
                return;
            }
            File profilesFolder = new File("profiles");
            File[] subFolders = profilesFolder.listFiles();

            if (subFolders.length > numberOfNonUserDirsInProfilesDir) {
                // a checkout has previously completed
                return;
            }
        }
        try {
            SubversionData svd = new SubversionData(svnCheckoutProfileOnStart, null);
            List<String> listing = Svn.list(svd);
            if (listing != null) {
                Map<String, String> profileMap = new HashMap<String, String>();
                for (String item : listing) {
                    if (item.endsWith(".ace")) {
                        profileMap.put(item.substring(item.lastIndexOf("/") + 1).replace(".ace", ""), item);
                    }
                }
                SortedSet<String> sortedProfiles = new TreeSet<String>(profileMap.keySet());
                JFrame emptyFrame = new JFrame();
                if (sortedProfiles.size() == 0) {
                    return;
                }
                String selectedProfile =
                        (String) SelectObjectDialog.showDialog(emptyFrame, emptyFrame, "Select profile to checkout:",
                        "Checkout profile:", sortedProfiles.toArray(), null, null);

                String selectedPath = profileMap.get(selectedProfile);
                if (selectedPath == null) {
                    AceLog.getAppLog().info("### No profile selected - shutting down.");
                    connectToSubversion = false;
                    Svn.setConnectedToSvn(connectToSubversion);
                    AceLog.getAppLog().info("### exit from handleSvnProfileCheckout.");
                    System.exit(0);
                }
                if (selectedPath.startsWith("/")) {
                    selectedPath = selectedPath.substring(1);
                }
                String[] pathParts = selectedPath.split("/");
                String[] specParts = svnCheckoutProfileOnStart.split("/");
                int matchStart = 0;
                for (int i = 0; i < specParts.length; i++) {
                    if (specParts[i].equals(pathParts[i - matchStart])) {
                    } else {
                        matchStart = i + 1;
                    }
                }
                List<String> specList = new ArrayList<String>();
                for (int i = 0; i < matchStart; i++) {
                    specList.add(specParts[i]);
                }
                for (String pathPart : pathParts) {
                    specList.add(pathPart);
                }
                StringBuffer checkoutBuffer = new StringBuffer();
                for (int i = 0; i < specList.size() - 1; i++) {
                    checkoutBuffer.append(specList.get(i));
                    checkoutBuffer.append("/");
                }
                String svnProfilePath = checkoutBuffer.toString();
                SubversionData svnCheckoutData = new SubversionData(svnProfilePath, "profiles/" + selectedProfile);
                subversionMap.put(svnCheckoutData.getWorkingCopyStr(), svnCheckoutData);
                aceProperties.setProperty("last-profile-dir", "profiles/" + selectedProfile);

                String moduleName = svnCheckoutData.getRepositoryUrlStr();
                String destPath = svnCheckoutData.getWorkingCopyStr();
                Revision revision = Revision.HEAD;
                Revision pegRevision = Revision.HEAD;
                int depth = Depth.infinity;
                boolean ignoreExternals = false;
                boolean allowUnverObstructions = false;
                Svn.getSvnClient().checkout(moduleName, destPath, revision, pegRevision, depth, ignoreExternals,
                        allowUnverObstructions);
                changeLocations.add(new File(destPath));
            }
        } catch (ClientException e) {
            if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "Unable to connect to Subversion - please check network connection and try again.", "Unable to connect",
                        JOptionPane.INFORMATION_MESSAGE);
                connectToSubversion = false;
                Svn.setConnectedToSvn(connectToSubversion);
                AceLog.getAppLog().info(
                        "### Unable to connect to Subversion - please check network connection and try again.");
                System.exit(0);
            } else {
                AceLog.getAppLog().info("### User cancelled Subversion log in.");
                System.exit(0);
            }
        }
    }

    private void handleSvnCheckout(List<File> changeLocations, String svnSpec) throws TaskFailedException, ClientException {
        AceLog.getAppLog().info("Got svn checkout spec: " + svnSpec);
        String[] specParts =
                new String[]{svnSpec.substring(0, svnSpec.lastIndexOf("|")),
            svnSpec.substring(svnSpec.lastIndexOf("|") + 1)};
        int server = 0;
        int local = 1;
        specParts[local] = specParts[local].replace('/', File.separatorChar);
        File checkoutLocation = new File(specParts[local]);
        if (checkoutLocation.exists()) {
            // already checked out
            AceLog.getAppLog().info(specParts[server] + " already checked out to: " + specParts[local]);
            subversionMap.put(specParts[local], new SubversionData(specParts[server], specParts[local]));
        } else {

            try {
                // do the checkout...
                AceLog.getAppLog().info("svn checkout " + specParts[server] + " to: " + specParts[local]);
                subversionMap.put(specParts[local], new SubversionData(specParts[server], specParts[local]));
                String moduleName = specParts[server];
                String destPath = specParts[local];
                Revision revision = Revision.HEAD;
                Revision pegRevision = Revision.HEAD;
                int depth = Depth.infinity;
                boolean ignoreExternals = false;
                boolean allowUnverObstructions = false;
                Svn.getSvnClient().checkout(moduleName, destPath, revision, pegRevision, depth, ignoreExternals,
                        allowUnverObstructions);
                changeLocations.add(checkoutLocation);
            } catch (ClientException e) {
                if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "Unable to connect to Subversion - please check network connection and try again.",
                            "Unable to connect", JOptionPane.INFORMATION_MESSAGE);
                    connectToSubversion = false;
                    Svn.setConnectedToSvn(connectToSubversion);
                    AceLog.getAppLog().info(
                            "### Unable to connect to Subversion - please check network connection and try again.");
                    System.exit(0);
                } else {
                    AceLog.getAppLog().info("### User cancelled Subversion log in.");
                    System.exit(0);
                }
            }
        }
    }

    private void handleSvnUpdate(List<File> changeLocations, String path, SvnPrompter prompter) throws ClientException,
            TaskFailedException {
        AceLog.getAppLog().info("Got svn update spec: " + path);
        try {
            Revision revision = Revision.HEAD;
            int depth = Depth.unknown;
            boolean depthIsSticky = false;
            boolean ignoreExternals = false;
            boolean allowUnverObstructions = false;
            if (path.replace('\\', '/').equalsIgnoreCase(I_ConfigAceDb.MUTABLE_DB_LOC)) {
                AceLog.getAppLog().info("Starting svn revert for: " + path);
                SubversionData svd = new SubversionData(null, path);
                Svn.revertNoLock(svd, prompter);
                changeLocations.add(new File("profiles"));

            }
            AceLog.getAppLog().info("Starting svn update for: " + path);
            Svn.getSvnClient().update(path, revision, depth, depthIsSticky, ignoreExternals, allowUnverObstructions);
            AceLog.getAppLog().info("Finished svn update for: " + path);
            changeLocations.add(new File(path));
        } catch (ClientException e) {
            if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "Unable to connect to Subversion - please check network connection and try again.", "Unable to connect",
                        JOptionPane.INFORMATION_MESSAGE);
                connectToSubversion = false;
                Svn.setConnectedToSvn(connectToSubversion);
                AceLog.getAppLog().info(
                        "### Unable to connect to Subversion - please check network connection and try again.");
                System.exit(0);
            } else {
                AceLog.getAppLog().info("### User cancelled Subversion log in.");
                System.exit(0);
            }
        }
    }

    public void doChangeSetImport() {
        if (Terms.get() == null) {
            AceLog.getAppLog().info("Database not setup for eccs import: " + changeLocations);
            return;
        }
        doChangeSetImport(changeLocations);
        changeLocations.clear();
    }

    public static void doChangeSetImport(List<File> changeLocations) {
        if (Terms.get() == null) {
            AceLog.getAppLog().info("Database not setup for eccs import: " + changeLocations);
            return;
        }
        // import any change sets that may be downloaded
        // from svn...
        changeLocations = new ArrayList<File>(new HashSet<File>(changeLocations));
        Ts.get().suspendChangeNotifications();
        try {
            Terms.get().suspendChangeSetWriters();
            AceLog.getAppLog().info("Starting eccs import: " + changeLocations);

            ChangeSetImporter jcsImporter = new ChangeSetImporter() {

                @Override
                public I_ReadChangeSet getChangeSetReader(File csf) {
                    EConceptChangeSetReader csr = new EConceptChangeSetReader();
                    csr.setChangeSetFile(csf);
                    return csr;
                }
                @Override
                public I_ReadChangeSet getChangeSetWfHxReader(File csf) {
                    WfRefsetChangeSetReader wcsr = new WfRefsetChangeSetReader();
                    wcsr.setChangeSetFile(csf);
                    return wcsr;
                }
            };

            for (File checkoutLocation : changeLocations) {
                jcsImporter.importAllChangeSets(AceLog.getAppLog().getLogger(), null, checkoutLocation.getAbsolutePath(),
                        false, ".eccs", "bootstrap.init");
            }

            for (File checkoutLocation : changeLocations) {
                jcsImporter.importAllChangeSets(AceLog.getAppLog().getLogger(), null, checkoutLocation.getAbsolutePath(),
                        false, ".eccs");
            }

            AceLog.getAppLog().info("Finished eccs import");
            BdbPathManager.get().resetPathMap();
            Bdb.sync();
            Terms.get().resumeChangeSetWriters();
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        } finally {
            Ts.get().resumeChangeNotifications();
        }
    }
}
