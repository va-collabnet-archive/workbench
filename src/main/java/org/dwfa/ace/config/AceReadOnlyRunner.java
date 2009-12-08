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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.url.tiuid.ExtendedUrlStreamHandlerFactory;
import org.dwfa.jini.JiniManager;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.VodbEnv;

import com.sun.jini.start.LifeCycle;

public class AceReadOnlyRunner {

    static {
        VodbEnv.setHeadless(false);
    }

    @SuppressWarnings("unused")
    private String[] args;

    protected Configuration config;

    private LifeCycle lc;

    private static boolean firstStartup = true;

    public AceReadOnlyRunner(final String[] args, final LifeCycle lc) {
        AceProtocols.setupExtraProtocols();
        ACE.editMode = false;
        JiniManager.setLocalOnly(true);
        try {
            this.args = args;
            this.lc = lc;
            String argsStr;
            if (args == null) {
                argsStr = "null";
            } else {
                argsStr = Arrays.asList(args).toString();
            }
            AceLog.getAppLog().info(
                "\n*******************\n\n" + "Starting AceReadOnlyRunner with config file args: " + argsStr
                    + "\n\n******************\n");
            config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());

            String lookAndFeelClassName = (String) config.getEntry(this.getClass().getName(), "lookAndFeelClassName",
                String.class, UIManager.getSystemLookAndFeelClassName());

            String userNameForWindow = (String) config.getEntry(this.getClass().getName(), "userNameForWindow",
                String.class, "AMT Viewer");

            UIManager.setLookAndFeel(lookAndFeelClassName);

            File aceConfigFile = (File) config.getEntry(this.getClass().getName(), "aceConfigFile", File.class,
                new File("profiles/bootstrap.ace"));

            if (aceConfigFile.exists()) {
                File profileDir = new File("profiles" + File.separator + "users");
                if (profileDir.exists() == false) {
                    profileDir = new File("profiles");
                    if (profileDir.exists() == false) {
                        profileDir.mkdirs();
                    }
                }

                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(aceConfigFile)));
                AceConfig.config = (AceConfig) ois.readObject();
                AceConfig.config.setProfileFile(aceConfigFile);
                setupDatabase(AceConfig.config);
            } else {
                File dbFolder = (File) config.getEntry(this.getClass().getName(), "dbFolder", File.class, new File(
                    "target/berkeley-db"));
                Long cacheSize = (Long) config.getEntry(this.getClass().getName(), "cacheSize", Long.class, null);
                AceLog.getAppLog().info("Cache size in config file: " + cacheSize);
                AceConfig.config = new AceConfig(dbFolder);
                AceConfig.config.setProfileFile(aceConfigFile);
                setupDatabase(AceConfig.config);
                AceConfig.setupAceConfig(AceConfig.config, aceConfigFile, cacheSize, false);
            }
            ACE.setAceConfig(AceConfig.config);
            AceConfig.config.addChangeSetWriters();
            AceConfig.config.setUsername(userNameForWindow);
            int successCount = 0;
            int frameCount = 0;
            for (final I_ConfigAceFrame ace : AceConfig.config.aceFrames) {
                frameCount++;
                if (ace.isActive()) {
                    AceFrameConfig afc = (AceFrameConfig) ace;
                    afc.setUsername(userNameForWindow);
                    afc.setMasterConfig(AceConfig.config);
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

                }
            }
            // Execute startup processes here...

            if (successCount == 0) {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    "No frames where opened. Now exiting.", "No successful logins...", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
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
}
