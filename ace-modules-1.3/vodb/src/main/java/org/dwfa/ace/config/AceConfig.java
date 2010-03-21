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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JFrame;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.commitlog.CommitLog;
import org.dwfa.ace.cs.BinaryChangeSetWriter;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.JarExtractor;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.types.ConceptBean;

import com.sleepycat.je.DatabaseException;

public class AceConfig implements I_ConfigAceDb, Serializable {

    private static File dbFolderOverride;
    
    static {
        String property = System.getProperty("db.folder.override"); 
        if (property != null) {
            dbFolderOverride = new File(property);
        }
    }

    public static AceConfig config;

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 9;

    private static String DEFAULT_LOGGER_CONFIG_FILE = "logViewer.config";

    private static String DEFAULT_ACE_CONFIG_FILE = "ace.config";

    private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);

    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public List<I_ConfigAceFrame> aceFrames = new ArrayList<I_ConfigAceFrame>();

    private File dbFolder = new File("berkeley-db");

    private String loggerRiverConfigFile = DEFAULT_LOGGER_CONFIG_FILE;

    private String aceRiverConfigFile = DEFAULT_ACE_CONFIG_FILE;

    private boolean readOnly = false;

    // private Long cacheSize = null; removed cacheSize...

    // 4
    private String username;

    // 5
    private File changeSetRoot;
    private String changeSetWriterFileName;

    // 6
    private Collection<String> queueFolders = new HashSet<String>();

    // 7
    private Map<String, Object> properties = new HashMap<String, Object>();

    // 8
    private I_GetConceptData userConcept;

    // 9
    private I_GetConceptData userPath;
    private String fullName;

    // transient
    private transient File profileFile;

    public AceConfig() throws DatabaseException, TerminologyException, IOException {
        super();
    }

    public AceConfig(File dbFolder) throws DatabaseException, TerminologyException, IOException {
        this();
        this.dbFolder = dbFolder;
    }

    public AceConfig(File dbFolder, boolean readOnly) {
        super();
        this.dbFolder = dbFolder;
        this.readOnly = readOnly;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        if (username == null || username.equals("null")) {
            username = aceFrames.get(0).getUsername();
        }
        out.writeObject(username);
        out.writeObject(null); // for historic password...
        out.writeObject(dbFolder);
        out.writeBoolean(readOnly);
        out.writeObject(null); // was cacheSize
        out.writeObject(aceFrames);
        out.writeObject(loggerRiverConfigFile);
        out.writeObject(changeSetRoot);
        out.writeObject(changeSetWriterFileName);
        out.writeObject(queueFolders);
        out.writeObject(properties);
        try {

            if (userConcept == null) {
                userConcept = ConceptBean.get(ArchitectonicAuxiliary.Concept.USER.getUids());
            }
            if (userPath == null) {
                userPath = ConceptBean.get(ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids());
            }
            out.writeObject(AceConfig.getVodb().nativeToUuid(userConcept.getConceptId()));
            out.writeObject(AceConfig.getVodb().nativeToUuid(userPath.getConceptId()));
            out.writeObject(fullName);
        } catch (DatabaseException e) {
            IOException newEx = new IOException();
            newEx.initCause(e);
            throw newEx;
        } catch (TerminologyException e) {
            IOException newEx = new IOException();
            newEx.initCause(e);
            throw newEx;
        }

    }

    private static final String authFailureMsg = "Username and password do not match.";

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (Boolean.getBoolean("skipReadObject")) {
            if (objDataVersion >= 4) {
                username = (String) in.readObject();
                in.readObject(); // for historic password
            } else {
                username = null;
            }
            if (objDataVersion >= 1) {
                
                // Ignore the dbFolder from the profile 
                in.readObject();
                
                dbFolder = new File("berkeley-db");
                if (dbFolderOverride != null) {
                    dbFolder = dbFolderOverride;
                }
                readOnly = in.readBoolean();
                if (objDataVersion >= 3) {
                    in.readObject(); // was cacheSize
                }
                aceFrames = (List<I_ConfigAceFrame>) in.readObject();
                for (I_ConfigAceFrame icaf : aceFrames) {
                    AceFrameConfig afc = (AceFrameConfig) icaf;
                    afc.setMasterConfig(this);
                }
            }
            if (objDataVersion >= 2) {
                loggerRiverConfigFile = (String) in.readObject();
            } else {
                loggerRiverConfigFile = DEFAULT_LOGGER_CONFIG_FILE;
            }
            if (objDataVersion >= 5) {
                changeSetRoot = (File) in.readObject();
                changeSetWriterFileName = (String) in.readObject();
                if (changeSetWriterFileName.contains("#") == false) {
                    changeSetWriterFileName = username + "." + "#" + 0 + "#" + UUID.randomUUID().toString() + ".jcs";
                }
            } else {
                changeSetRoot = new File("profiles" + File.separator + username);
                changeSetWriterFileName = username + "." + "#" + 0 + "#" + UUID.randomUUID().toString() + ".jcs";
            }
            if (objDataVersion >= 6) {
                queueFolders = (Collection<String>) in.readObject();
            } else {
                queueFolders = new HashSet<String>();
            }
            if (objDataVersion >= 7) {
                properties = (Map<String, Object>) in.readObject();
            } else {
                properties = new HashMap<String, Object>();
            }
            try {
                if (objDataVersion >= 8) {
                    in.readObject();
                } else {
                    userConcept = ConceptBean.get(ArchitectonicAuxiliary.Concept.USER.getUids());
                }
                if (objDataVersion >= 9) {
                    userPath = ConceptBean.get(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
                    fullName = (String) in.readObject();
                } else {
                    userPath = ConceptBean.get(ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids());
                    fullName = username;
                }
            } catch (Exception e) {
                IOException newEx = new IOException();
                newEx.initCause(e);
                throw newEx;
            }
        }

        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 4) {
                username = (String) in.readObject();
                in.readObject(); // for historic password
            } else {
                username = null;
            }
            if (objDataVersion >= 1) {
                
                // ignore the dbFolder from the profile
                in.readObject();
                
                dbFolder = new File("berkeley-db");
                if (dbFolderOverride != null) {
                    dbFolder = dbFolderOverride;
                }
                readOnly = in.readBoolean();
                if (objDataVersion >= 3) {
                    in.readObject(); // was cacheSize
                }
                try {
                    VodbEnv vodbEnv;
                    if (AceConfig.getVodb() == null) {
                        vodbEnv = new VodbEnv();
                    } else {
                        vodbEnv = AceConfig.getVodb();
                    }
                    vodbEnv.setup(dbFolder, readOnly);
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
                aceFrames = (List<I_ConfigAceFrame>) in.readObject();
                for (I_ConfigAceFrame icaf : aceFrames) {
                    AceFrameConfig afc = (AceFrameConfig) icaf;
                    afc.setMasterConfig(this);
                }
            }
            if (username == null || username.equals("null")) {
                username = aceFrames.get(0).getUsername();
            }
            if (objDataVersion >= 2) {
                loggerRiverConfigFile = (String) in.readObject();
            } else {
                loggerRiverConfigFile = DEFAULT_LOGGER_CONFIG_FILE;
            }
            if (objDataVersion >= 5) {
                changeSetRoot = (File) in.readObject();
                changeSetWriterFileName = (String) in.readObject();
                if (changeSetWriterFileName.contains("#") == false) {
                    changeSetWriterFileName = username + "." + "#" + 0 + "#" + UUID.randomUUID().toString() + ".jcs";
                }
            } else {
                changeSetRoot = new File("profiles" + File.separator + username);
                changeSetWriterFileName = username + "." + "#" + 0 + "#" + UUID.randomUUID().toString() + ".jcs";
            }
            if (objDataVersion >= 6) {
                queueFolders = (Collection<String>) in.readObject();
            } else {
                queueFolders = new HashSet<String>();
            }
            if (objDataVersion >= 7) {
                properties = (Map<String, Object>) in.readObject();
            } else {
                properties = new HashMap<String, Object>();
            }
            try {
                if (objDataVersion >= 8) {
                    userConcept = ConceptBean.get(AceConfig.getVodb().uuidToNative((List<UUID>) in.readObject()));
                } else {
                    userConcept = ConceptBean.get(ArchitectonicAuxiliary.Concept.USER.getUids());
                }
                if (objDataVersion >= 9) {
                    List<UUID> uuids = (List<UUID>) in.readObject();
                    if (uuids != null) {
                        userPath = ConceptBean.get(AceConfig.getVodb().uuidToNative(uuids));
                        fullName = (String) in.readObject();
                    } else {
                        fullName = username;
                    }

                } else {
                    fullName = username;
                }
            } catch (Exception e) {
                IOException newEx = new IOException();
                newEx.initCause(e);
                throw newEx;
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
        AceLog.getAppLog().info("changeSetWriterFileName: " + changeSetWriterFileName);
        renameChangeSetFile();
        AceLog.getAppLog().info("changeSetWriterFileName: " + changeSetWriterFileName);

        this.vetoSupport = new VetoableChangeSupport(this);
        this.changeSupport = new PropertyChangeSupport(this);
    }

    public static void setupAceConfig(AceConfig config, File configFile, Long cacheSize, boolean includeSnomed)
            throws DatabaseException, ParseException, TerminologyException, IOException, FileNotFoundException {
        try {
            VodbEnv vodbEnv = new VodbEnv();
            vodbEnv.setup(config.dbFolder, config.readOnly, cacheSize);
            LocalVersionedTerminology.set(vodbEnv);
            if (config.userConcept == null) {
                config.userConcept = ConceptBean.get(ArchitectonicAuxiliary.Concept.USER.getUids());
            }
            if (config.userPath == null) {
                config.userPath = ConceptBean.get(ArchitectonicAuxiliary.Concept.DEVELOPMENT.getUids());
            }

        } catch (Exception e) {
            throw new ToIoException(e);
        }

        SvnPrompter prompter = new SvnPrompter();
        prompter.prompt("config file", "username");

        I_ConfigAceFrame profile = NewDefaultProfile.newProfile(prompter.getUsername(), prompter.getUsername(),
            prompter.getPassword(), "admin", "visit.bend");
        config.setUsername(profile.getUsername());
        config.aceFrames.add(profile);

        if (config.getUsername() == null) {
            config.setChangeSetWriterFileName("nullUser." + UUID.randomUUID().toString() + ".jcs");
        } else {
            config.setChangeSetWriterFileName(config.getUsername() + "." + UUID.randomUUID().toString() + ".jcs");
        }
        config.changeSetRoot = new File("profiles" + File.separator + config.getUsername());

        if (configFile == null) {
            File profileDir = new File("profiles");
            File userDir = new File(profileDir, profile.getUsername());
            userDir.mkdirs();
            configFile = new File(userDir, profile.getUsername() + ".ace");
            config.setProfileFile(configFile);
        }
        FileOutputStream fos = new FileOutputStream(configFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(config);
        oos.close();
    }

    public void addChangeSetWriters() {
        ACE.getCsWriters().add(
            new BinaryChangeSetWriter(new File(changeSetRoot, getChangeSetWriterFileName()), new File(changeSetRoot,
                "." + getChangeSetWriterFileName())));
        ACE.getCsWriters().add(
            new CommitLog(new File(changeSetRoot, "commitLog.xls"), new File(changeSetRoot, "." + "commitLog.xls")));
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        vetoSupport.addVetoableChangeListener(propertyName, listener);
    }

    public void addVetoableChangeListener(VetoableChangeListener listener) {
        vetoSupport.addVetoableChangeListener(listener);
    }

    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        vetoSupport.removeVetoableChangeListener(propertyName, listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        vetoSupport.removeVetoableChangeListener(listener);
    }

    public boolean isDbCreated() {

        File[] dbFiles = dbFolder.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jdb");
            }

        });
        return (dbFiles != null && dbFiles.length > 0);
    }

    public static void extractMavenLib(AceConfig config) throws IOException {
        URL dbUrl = AceConfig.class.getClassLoader().getResource("locator.txt");

        AceLog.getAppLog().info(" url: " + dbUrl);
        String[] pathParts = dbUrl.getPath().split("!");
        String[] fileProtocolParts = pathParts[0].split(":");

        File srcJarFile = new File(fileProtocolParts[1].replace("foundation", "ace-bdb").replace("dwfa", "jehri"));
        File targetDir = config.dbFolder.getParentFile();
        AceLog.getAppLog().info("Jar file: " + srcJarFile);
        if (targetDir.exists() && targetDir.lastModified() == srcJarFile.lastModified()) {
            AceLog.getAppLog().info("ace-db is current...");
        } else {
            AceLog.getAppLog().info("ace-db needs update...");
            targetDir.mkdirs();
            AceLog.getAppLog().info("Now extracting into: " + targetDir.getCanonicalPath());
            JarExtractor.execute(srcJarFile, targetDir);
            targetDir.setLastModified(srcJarFile.lastModified());
        }
    }

    public String getLoggerRiverConfigFile() {
        return loggerRiverConfigFile;
    }

    public void setLoggerRiverConfigFile(String loggerConfigFile) {
        this.loggerRiverConfigFile = loggerConfigFile;
    }

    public String getAceRiverConfigFile() {
        if (aceRiverConfigFile == null) {
            aceRiverConfigFile = DEFAULT_ACE_CONFIG_FILE;
        }
        return aceRiverConfigFile;
    }

    public void setAceRiverConfigFile(String aceRiverConfigFile) {
        this.aceRiverConfigFile = aceRiverConfigFile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        Object old = this.username;
        this.username = username;
        if (this.username == null) {
            this.setChangeSetWriterFileName("nullUser." + UUID.randomUUID().toString() + ".jcs");
        } else {
            this.setChangeSetWriterFileName(this.username + "." + UUID.randomUUID().toString() + ".jcs");
        }

        this.changeSetRoot = new File("profiles" + File.separator + username);
        this.changeSupport.firePropertyChange("username", old, username);
    }

    public static VodbEnv stealthVodb;

    public static VodbEnv getVodb() {
        if (stealthVodb != null) {
            return stealthVodb;
        }
        return (VodbEnv) LocalVersionedTerminology.get();
    }

    public List<I_ConfigAceFrame> getAceFrames() {
        return aceFrames;
    }

    public File getDbFolder() {
        return dbFolder;
    }

    public void setDbFolder(File dbFolder) {
        this.dbFolder = dbFolder;
    }

    public void save() throws IOException {
        if (profileFile == null) {
            throw new IOException("configFile is null. Please set before saving. ");
        }
        File changeSetFile = new File(getChangeSetRoot(), getChangeSetWriterFileName());
        if (changeSetFile.exists()) {
            int maxSize = 512000;
            if (changeSetFile.length() > maxSize) {
                renameChangeSetFile();
                AceLog.getAppLog().info(
                    "change set exceeds " + maxSize + " bytes. Incrementing file to: " + getChangeSetWriterFileName());
            }
        }
        FileOutputStream fos = new FileOutputStream(profileFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.close();
    }

    private void renameChangeSetFile() {
        String[] nameParts = getChangeSetWriterFileName().split("#");
        int sequence = Integer.parseInt(nameParts[1]);
        sequence++;
        setChangeSetWriterFileName(getUsername() + '#' + sequence + "#" + UUID.randomUUID() + ".jcs");
    }

    public File getChangeSetRoot() {
        return changeSetRoot;
    }

    public void setChangeSetRoot(File changeSetRoot) {
        this.changeSetRoot = changeSetRoot;
    }

    public String getChangeSetWriterFileName() {
        return changeSetWriterFileName;
    }

    public void setChangeSetWriterFileName(String changeSetWriterFileName) {
        this.changeSetWriterFileName = changeSetWriterFileName;
    }

    public File getProfileFile() {
        return profileFile;
    }

    public void setProfileFile(File profileFile) {
        this.profileFile = profileFile;
    }

    public Collection<String> getQueues() {
        return queueFolders;
    }

    /**
     * Currently non-functional stub.
     * 
     * @deprecated
     */
    public Long getCacheSize() {
        return null;
    }

    /**
     * Currently non-functional stub.
     * 
     * @deprecated
     */
    public void setCacheSize(Long cacheSize) {
    }

    public Map<String, Object> getProperties() throws IOException {
        return properties;
    }

    public Object getProperty(String key) throws IOException {
        return properties.get(key);
    }

    public void setProperty(String key, Object value) throws IOException {
        properties.put(key, value);
    }

    public JFrame getActiveFrame() {
        for (I_ConfigAceFrame f : aceFrames) {
            if (f.isActive()) {
                return ((AceFrameConfig) f).getAceFrame();
            }
        }
        return null;
    }

    public I_GetConceptData getUserConcept() {
        return userConcept;
    }

    public void setUserConcept(I_GetConceptData userConcept) {
        this.userConcept = userConcept;
    }

    public I_GetConceptData getUserPath() {
        return userPath;
    }

    public void setUserPath(I_GetConceptData userPath) {
        this.userPath = userPath;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
