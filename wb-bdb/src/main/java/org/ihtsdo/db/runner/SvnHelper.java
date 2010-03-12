package org.ihtsdo.db.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.cs.ChangeSetImporter;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.SelectObjectDialog;
import org.dwfa.svn.Svn;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.cs.econcept.EConceptChangeSetReader;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Depth;
import org.tigris.subversion.javahl.Revision;

/**
 * Common SVN helper methods used by the Ace Runner and AceLoginDialog.
 * 
 * @author ean dungey
 * @author kec
 */

public class SvnHelper {
	   /**
     * The jini configuration provider
     */
    private Configuration jiniConfig;
    private String svnCheckoutProfileOnStart = null;
    private String[] svnCheckoutOnStart = null;
    private String[] svnUpdateOnStart = null;
    private String[] csImportOnStart = null;
    private List<File> changeLocations = new ArrayList<File>();
    private Class<?> jiniClass;
    private static boolean connectToSubversion = false;

    public SvnHelper(Class<?> jiniClassToSet, Configuration jiniConfigToSet) throws ConfigurationException {
        jiniClass = jiniClassToSet;
        jiniConfig = jiniConfigToSet;
        if (jiniConfig != null) {
            svnCheckoutProfileOnStart = (String) jiniConfig.getEntry(jiniClass.getName(), "svnCheckoutProfileOnStart",
                String.class, "");

            svnCheckoutOnStart = (String[]) jiniConfig.getEntry(jiniClass.getName(), "svnCheckoutOnStart",
                String[].class, new String[] {});
            svnUpdateOnStart = (String[]) jiniConfig.getEntry(jiniClass.getName(), "svnUpdateOnStart", String[].class,
                new String[] {});
            csImportOnStart = (String[]) jiniConfig.getEntry(jiniClass.getName(), "csImportOnStart", String[].class,
                new String[] {});
            if (csImportOnStart != null) {
                for (String importLoc : csImportOnStart) {
                    changeLocations.add(new File(importLoc));
                }
            }
        }
    }

    void initialSubversionOperationsAndChangeSetImport(File acePropertiesFile, boolean connectToSubversion)
            throws ConfigurationException, FileNotFoundException, IOException, TaskFailedException, ClientException {
        Properties aceProperties = new Properties();
        aceProperties.setProperty("initial-svn-checkout", "true");

        if ((svnCheckoutOnStart != null && svnCheckoutOnStart.length > 0)
            || (svnUpdateOnStart != null && svnUpdateOnStart.length > 0)
            || (svnCheckoutProfileOnStart != null && svnCheckoutProfileOnStart.length() > 0)) {

            if (connectToSubversion) {
                if (svnCheckoutProfileOnStart != null && svnCheckoutProfileOnStart.length() > 0) {
                    handleSvnProfileCheckout(aceProperties);
                }

                if (svnCheckoutOnStart != null && svnCheckoutOnStart.length > 0) {
                    for (String svnSpec : svnCheckoutOnStart) {
                        handleSvnCheckout(changeLocations, svnSpec);
                    }
                }

                if (svnUpdateOnStart != null && svnUpdateOnStart.length > 0) {
                    for (String svnSpec : svnUpdateOnStart) {
                        handleSvnUpdate(changeLocations, svnSpec);
                    }
                }

                if (changeLocations.size() > 0) {
                    doChangeSetImport(changeLocations);
                }
                aceProperties.storeToXML(new FileOutputStream(acePropertiesFile), null);
            } else {
                if (new File("profiles").exists() == false) {
                    throw new TaskFailedException("User did not want to connect to Subversion.");
                }
            }
        } else if (changeLocations.size() > 0) {
            doChangeSetImport(changeLocations);
        }
    }

    public void initialSubversionOperationsAndChangeSetImport(File acePropertiesFile) throws ConfigurationException,
            FileNotFoundException, IOException, TaskFailedException, ClientException {

        Properties wbProperties = new Properties();
        wbProperties.setProperty("initial-svn-checkout", "true");

        if ((svnCheckoutOnStart != null && svnCheckoutOnStart.length > 0)
            || (svnUpdateOnStart != null && svnUpdateOnStart.length > 0)
            || (svnCheckoutProfileOnStart != null && svnCheckoutProfileOnStart.length() > 0)) {
            if (connectToSubversion == false) {
                connectToSubversion = (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                    LogWithAlerts.getActiveFrame(null), "Would you like to connect over the network to Subversion?",
                    "Confirm network operation", JOptionPane.YES_NO_OPTION));
            }
            if (connectToSubversion) {
                if (svnCheckoutProfileOnStart != null && svnCheckoutProfileOnStart.length() > 0) {
                    handleSvnProfileCheckout(wbProperties);
                }

                if (svnCheckoutOnStart != null && svnCheckoutOnStart.length > 0) {
                    for (String svnSpec : svnCheckoutOnStart) {
                        handleSvnCheckout(changeLocations, svnSpec);
                    }
                }

                if (svnUpdateOnStart != null && svnUpdateOnStart.length > 0) {
                    for (String svnSpec : svnUpdateOnStart) {
                        handleSvnUpdate(changeLocations, svnSpec);
                    }
                }

                if (changeLocations.size() > 0) {
                    doChangeSetImport(changeLocations);
                }
                wbProperties.storeToXML(new FileOutputStream(acePropertiesFile), null);
            } else {
                if (new File("profiles").exists() == false) {
                    throw new TaskFailedException("User did not want to connect to Subversion.");
                }
            }
        } else if (changeLocations.size() > 0) {
            doChangeSetImport(changeLocations);
        }
    }

    void handleSvnProfileCheckout(Properties aceProperties) throws ClientException, TaskFailedException {
        SubversionData svd = new SubversionData(svnCheckoutProfileOnStart, null);
        List<String> listing = Svn.list(svd);
        Map<String, String> profileMap = new HashMap<String, String>();
        for (String item : listing) {
            if (item.endsWith(".ace")) {
                profileMap.put(item.substring(item.lastIndexOf("/") + 1).replace(".ace", ""), item);
            }
        }
        SortedSet<String> sortedProfiles = new TreeSet<String>(profileMap.keySet());
        JFrame emptyFrame = new JFrame();
        String selectedProfile = (String) SelectObjectDialog.showDialog(emptyFrame, emptyFrame,
            "Select profile to checkout:", "Checkout profile:", sortedProfiles.toArray(), null, null);
        String selectedPath = profileMap.get(selectedProfile);
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

    private void handleSvnCheckout(List<File> changeLocations, String svnSpec) throws TaskFailedException,
            ClientException {
        AceLog.getAppLog().info("Got svn checkout spec: " + svnSpec);
        String[] specParts = new String[] { svnSpec.substring(0, svnSpec.lastIndexOf("|")),
                                           svnSpec.substring(svnSpec.lastIndexOf("|") + 1) };
        int server = 0;
        int local = 1;
        specParts[local] = specParts[local].replace('/', File.separatorChar);
        File checkoutLocation = new File(specParts[local]);
        if (checkoutLocation.exists()) {
            // already checked out
            AceLog.getAppLog().info(specParts[server] + " already checked out to: " + specParts[local]);
        } else {

            // do the checkout...
            AceLog.getAppLog().info("svn checkout " + specParts[server] + " to: " + specParts[local]);
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
        }
    }

    private void handleSvnUpdate(List<File> changeLocations, String path) {
        AceLog.getAppLog().info("Got svn update spec: " + path);
        try {
            Revision revision = Revision.HEAD;
            int depth = Depth.unknown;
            boolean depthIsSticky = false;
            boolean ignoreExternals = false;
            boolean allowUnverObstructions = false;
            AceLog.getAppLog().info("Starting svn update for: " + path);
            Svn.getSvnClient().update(path, revision, depth, depthIsSticky, ignoreExternals, allowUnverObstructions);
            AceLog.getAppLog().info("Finished svn update for: " + path);
            changeLocations.add(new File(path));
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    public void doChangeSetImport(List<File> changeLocations) {
        // import any change sets that may be downloaded
        // from svn...
        try {
            Terms.get().suspendChangeSetWriters();
            AceLog.getAppLog().info("Starting eccs import");

        	// TODO Support Import here...
            ChangeSetImporter jcsImporter = new ChangeSetImporter() {

                @Override
                public I_ReadChangeSet getChangeSetReader(File csf) {
                	EConceptChangeSetReader csr = new EConceptChangeSetReader();
                    csr.setChangeSetFile(csf);
                    return csr;
                }
            };

            for (File checkoutLocation : changeLocations) {
                jcsImporter.importAllChangeSets(AceLog.getAppLog().getLogger(), null,
                    checkoutLocation.getAbsolutePath(), false, ".eccs", "bootstrap.init");
            }

            for (File checkoutLocation : changeLocations) {
                jcsImporter.importAllChangeSets(AceLog.getAppLog().getLogger(), null,
                    checkoutLocation.getAbsolutePath(), false, ".eccs");
            }

            AceLog.getAppLog().info("Finished eccs import");
            Terms.get().resumeChangeSetWriters();
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }
}
