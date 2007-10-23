package org.dwfa.ace.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.cs.BinaryChangeSetReader;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.cs.ImportAllChangeSets;
import org.dwfa.ace.tree.ExpandNodeSwingWorker;
import org.dwfa.ace.url.tiuid.ExtendedUrlStreamHandlerFactory;
import org.dwfa.fd.FileDialogUtil;
import org.dwfa.queue.QueueServer;
import org.dwfa.svn.Svn;
import org.dwfa.svn.SvnPrompter;
import org.dwfa.vodb.VodbEnv;
import org.tigris.subversion.javahl.Revision;

import com.sun.jini.start.LifeCycle;

public class AceRunner {

   private class CheckIpAddressForChanges implements ActionListener {

      public void actionPerformed(ActionEvent arg0) {
         try {
            InetAddress currentLocalHost = InetAddress.getLocalHost();
            if (currentLocalHost.equals(startupLocalHost)) {
               // all ok
            } else {
               JOptionPane.showMessageDialog(null, "<html>Your ip address (" + currentLocalHost.toString()
                     + ") <br> has changed since startup (" + startupLocalHost.toString()
                     + ") <br> please restart your application.");
            }
         } catch (UnknownHostException e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }

   }

   @SuppressWarnings("unused")
   private String[] args;

   @SuppressWarnings("unused")
   private LifeCycle lc;

   protected Configuration config;

   private InetAddress startupLocalHost;

   private static boolean firstStartup = true;

   CheckIpAddressForChanges ipChangeListener = new CheckIpAddressForChanges();

   @SuppressWarnings("unused")
   private Timer ipChangeTimer;

   public AceRunner(final String[] args, final LifeCycle lc) {
      try {
         this.args = args;
         this.lc = lc;
         AceLog.getAppLog().info("java.protocol.handler.pkgs: " + System.getProperty("java.protocol.handler.pkgs"));
         URL.setURLStreamHandlerFactory(new ExtendedUrlStreamHandlerFactory());
         String argsStr;
         if (args == null) {
            argsStr = "null";
         } else {
            argsStr = Arrays.asList(args).toString();
         }
         AceLog.getAppLog().info(
               "\n*******************\n\n" + "Starting " + this.getClass().getSimpleName() + " with config file: " + argsStr
                     + "\n\n******************\n");
         startupLocalHost = InetAddress.getLocalHost();
         
         config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());

         Boolean preloadRels = (Boolean) config.getEntry(this.getClass().getName(), "preloadRels",
                                               Boolean.class, null);
         AceLog.getAppLog().info("preloadRels " + preloadRels);
         if (preloadRels != null) {
             VodbEnv.setPreloadRels(preloadRels);
         }
         
         Boolean preloadDescriptions = (Boolean) config.getEntry(this.getClass().getName(), "preloadDescriptions",
                                                                 Boolean.class, null);
         AceLog.getAppLog().info("preloadDescriptions " + preloadDescriptions);
         if (preloadRels != null) {
             VodbEnv.setPreloadDescriptions(preloadDescriptions);
         }
                   
         Boolean logTimingInfo = (Boolean) config.getEntry(this.getClass().getName(), "logTimingInfo",
                                                                 Boolean.class, null);
         if (logTimingInfo != null) {
             ExpandNodeSwingWorker.setLogTimingInfo(logTimingInfo);
         }
         AceLog.getAppLog().info("logTimingInfo " + logTimingInfo);
         
         Long cacheSize = (Long) config.getEntry(this.getClass().getName(), "cacheSize", Long.class, null);
         AceLog.getAppLog().info("cacheSize " + cacheSize);
         if (cacheSize != null) {
             VodbEnv.setCacheSize(cacheSize);
         }

                   
         ipChangeTimer = new Timer(2 * 60 * 1000, ipChangeListener);
         ipChangeTimer.start();
 
         String lookAndFeelClassName = (String) config.getEntry(this.getClass().getName(), "lookAndFeelClassName",
               String.class, UIManager.getSystemLookAndFeelClassName());

         UIManager.setLookAndFeel(lookAndFeelClassName);

         String[] svnCheckoutOnStart = (String[]) config.getEntry(this.getClass().getName(), "svnCheckoutOnStart",
               String[].class, new String[] {});
         if (svnCheckoutOnStart != null) {
            for (String svnSpec : svnCheckoutOnStart) {
               AceLog.getAppLog().info("Got svn spec: " + svnSpec);
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
                  int n = JOptionPane.showConfirmDialog(null,
                        "Would you like to connect over the network to Subversion?", "Confirm network operation",
                        JOptionPane.YES_NO_OPTION);

                  if (n == JOptionPane.YES_OPTION) {
                     try {
                        // do the checkout...
                        AceLog.getAppLog().info("svn checkout " + specParts[server] + " to: " + specParts[local]);
                        Svn.getSvnClient().checkout(specParts[server], specParts[local], Revision.HEAD, true);
                        
                        // import any change sets that may be downloaded from svn...
                        File dbFolder = (File) config.getEntry(this.getClass().getName(), "dbFolder", File.class, new File(
                        "target/berkeley-db"));
                        
                        VodbEnv stealthVodb = new VodbEnv(true);
                        AceConfig.stealthVodb = stealthVodb;
                        LocalVersionedTerminology.setStealthfactory(stealthVodb);
                        stealthVodb.setup(dbFolder, false, cacheSize);
                        
                        List<File> changeSetFiles = new ArrayList<File>();
                        ImportAllChangeSets.addAllChangeSetFiles(checkoutLocation, changeSetFiles);
                        
                        TreeSet<I_ReadChangeSet> readerSet = ImportAllChangeSets.getSortedReaderSet();
                        
                        for (File csf : changeSetFiles) {
                            BinaryChangeSetReader csr = new BinaryChangeSetReader();
                            csr.setChangeSetFile(csf);
                            csr.setVodb(stealthVodb);
                            readerSet.add(csr);
                            AceLog.getAppLog().info("Adding reader: " + csf.getAbsolutePath());
                        }

                        while (readerSet.size() > 0) {
                            ImportAllChangeSets.readNext(readerSet);
                        }
                        stealthVodb.close();
                        AceConfig.stealthVodb = null;
                        LocalVersionedTerminology.setStealthfactory(null);
                        
                     } catch (Exception e) {
                         AceConfig.stealthVodb = null;
                         LocalVersionedTerminology.setStealthfactory(null);
                        AceLog.getAppLog().alertAndLogException(e);
                     }
                  }
               }
            }
            
            
         }

         File aceConfigFile = (File) config.getEntry(this.getClass().getName(), "aceConfigFile", File.class, new File(
               "src/main/config/config.ace"));

         if (aceConfigFile.exists()) {
             
             
             
            File profileDir = new File("profiles" + File.separator + "users");
            if (profileDir.exists() == false) {
               profileDir = new File("profiles");
               if (profileDir.exists() == false) {
                  profileDir.mkdirs();
               }
            }
            // Put up a dialog to select the config file...
            aceConfigFile = FileDialogUtil.getExistingFile("Please select your user profile:", new FilenameFilter() {

               public boolean accept(File dir, String name) {
                  return name.toLowerCase().endsWith(".ace");
               }
            }, profileDir);

            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(aceConfigFile)));
            AceConfig.config = (AceConfig) ois.readObject();
            AceConfig.config.setConfigFile(aceConfigFile);
            setupDatabase(AceConfig.config);
         } else {
            File dbFolder = (File) config.getEntry(this.getClass().getName(), "dbFolder", File.class, new File(
                  "target/berkeley-db"));
            AceLog.getAppLog().info("Cache size in config file: " + cacheSize);
            AceConfig.config = new AceConfig(dbFolder);
            AceConfig.config.setConfigFile(aceConfigFile);
            setupDatabase(AceConfig.config);
            AceConfig.setupAceConfig(AceConfig.config, aceConfigFile, cacheSize, false);
         }
         ACE.setAceConfig(AceConfig.config);
         AceConfig.config.addChangeSetWriters();
         int successCount = 0;
         int frameCount = 0;
         SvnPrompter prompter = new SvnPrompter();
         for (final I_ConfigAceFrame ace : AceConfig.config.aceFrames) {
            frameCount++;
            if (ace.isActive()) {
               AceFrameConfig afc = (AceFrameConfig) ace;
               afc.setMasterConfig(AceConfig.config);
               boolean login = true;
               while (login) {
                  if (ace.getUsername().equals(prompter.getUsername()) == false
                        || ace.getPassword().equals(prompter.getPassword()) == false) {
                     prompter.prompt("Please authenticate for: " + ace.getFrameName(), ace.getUsername());
                  }
                  if (ace.getUsername().equals(prompter.getUsername())
                        && ace.getPassword().equals(prompter.getPassword())) {
                     login = false;
                     successCount++;
                     SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                           try {
                              boolean startup = firstStartup;
                              firstStartup = false;
                              AceFrame af = new AceFrame(args, lc, ace, startup);
                              af.setVisible(true);
                           } catch (Exception e) {
                              AceLog.getAppLog().alertAndLogException(e);
                           }
                        }

                     });
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
         // Execute startup processes here...

         if (successCount == 0) {
            JOptionPane.showMessageDialog(null, "No frames where opened. Now exiting.", "No successful logins...",
                  JOptionPane.ERROR_MESSAGE);
            System.exit(0);
         }
         File directory = AceConfig.config.getConfigFile().getParentFile();

         if (directory.listFiles() != null) {
            for (File dir : directory.listFiles()) {
               processFile(dir, lc);
            }
         }
      } catch (Exception e) {
         AceLog.getAppLog().alertAndLogException(e);
         System.exit(0);
      }
   }

   private void setupDatabase(AceConfig aceConfig) throws IOException {
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
