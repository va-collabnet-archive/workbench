package org.dwfa.ace.config;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.commitlog.CommitLog;
import org.dwfa.ace.cs.BinaryChangeSetWriter;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.ace.url.tiuid.ExtendedUrlStreamHandlerFactory;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.fd.FileDialogUtil;
import org.dwfa.log.HtmlHandler;
import org.dwfa.log.LogViewerFrame;
import org.dwfa.svn.SvnPrompter;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;
import org.dwfa.util.io.JarExtractor;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;

import com.sleepycat.je.DatabaseException;

public class AceConfig implements I_ConfigAceDb, Serializable {

	private static File dbFolderOverride = null;

	public static AceConfig config;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 6;

	private static String DEFAULT_LOGGER_CONFIG_FILE = "logViewer.config";

	private static String DEFAULT_ACE_CONFIG_FILE = "ace.config";


	private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(
			this);

	private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(
			this);

	public List<I_ConfigAceFrame> aceFrames = new ArrayList<I_ConfigAceFrame>();

	private File dbFolder = new File("../test/berkeley-db");

	private String loggerRiverConfigFile = DEFAULT_LOGGER_CONFIG_FILE;

	private String aceRiverConfigFile = DEFAULT_ACE_CONFIG_FILE;

	private boolean readOnly = false;

	private Long cacheSize = null;

	// 4
	private String username;

    
    // 5
    private File changeSetRoot;
    private String changeSetWriterFileName;
    
    // 6
    private Collection<String> queueFolders = new HashSet<String>();
    
    // transient
    private transient File profileFile;

	public AceConfig() throws DatabaseException {
		super();
		if (LocalVersionedTerminology.get() == null) {
			LocalVersionedTerminology.set(new VodbEnv());
		}
	}

	public AceConfig(File dbFolder) throws DatabaseException {
		this();
		this.dbFolder = dbFolder;
	}


	public AceConfig(File dbFolder, boolean readOnly, Long cacheSize) {
        super();
        this.dbFolder = dbFolder;
        this.readOnly = readOnly;
        this.cacheSize = cacheSize;
    }

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(username);
		out.writeObject(null); //for historic password...
		out.writeObject(dbFolder);
		out.writeBoolean(readOnly);
		out.writeObject(cacheSize);
		out.writeObject(aceFrames);
        out.writeObject(loggerRiverConfigFile);
        out.writeObject(changeSetRoot);
        out.writeObject(changeSetWriterFileName);
        out.writeObject(queueFolders);
	}

	private static final String authFailureMsg = "Username and password do not match.";

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
			if (objDataVersion >= 4) {
				username = (String) in.readObject();
				in.readObject(); // for historic password
			} else {
				username = null;
			}
			if (objDataVersion >= 1) {
				dbFolder = (File) in.readObject();
				if (dbFolderOverride != null) {
					dbFolder = dbFolderOverride;
				}
				readOnly = in.readBoolean();
				if (objDataVersion >= 3) {
					cacheSize = (Long) in.readObject();
				} else {
					cacheSize = null;
				}
				try {
					if (AceConfig.getVodb() == null) {
						new VodbEnv();
					}
					AceConfig.getVodb().setup(dbFolder, readOnly, cacheSize);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
				aceFrames = (List<I_ConfigAceFrame>) in.readObject();
				for (I_ConfigAceFrame icaf: aceFrames) {
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
                changeSetRoot = new File("profiles" + File.separator + "users" + File.separator + username);
                changeSetWriterFileName = username + "." + "#" + 0 + "#" + UUID.randomUUID().toString() + ".jcs";
            }
            if (objDataVersion >= 6) {
            	queueFolders = (Collection<String>) in.readObject();
            } else {
            	queueFolders = new HashSet<String>();
            }
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
		this.vetoSupport = new VetoableChangeSupport(this);
		this.changeSupport = new PropertyChangeSupport(this);
	}

	public static void main(String[] args) throws SecurityException,
			IOException {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		URL.setURLStreamHandlerFactory(new ExtendedUrlStreamHandlerFactory());
		AceLog.getEditLog().setLevel(Level.FINER);
		HtmlHandler h = new HtmlHandler(null, "edits");
		h.setLevel(Level.FINER);
		AceLog.getEditLog().addHandler(h);
		AceLog.getEditLog().info("Setting up for editing.");
		try {
			String fileStr;
			if (args.length == 0) {
				fileStr = "config.ace";
			} else {
				fileStr = args[0];
				if (args.length > 1) {
					dbFolderOverride = new File(args[1]);
				}
			}
			if (dbFolderOverride != null) {
				config = new AceConfig(dbFolderOverride);
			} else {
				config = new AceConfig();
			}

			if (config.isDbCreated() == false) {

				AceLog.getAppLog().info("DB not created");
				int n = JOptionPane
						.showConfirmDialog(
								new JFrame(),
								"Would you like to extract the db from your maven repository?",
								"DB does not exist", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					extractMavenLib(config);
				} else {
					AceLog
							.getAppLog()
							.info(
									"Exiting, user did not want to extract the DB from maven.");
					return;
				}
			}
			long defaultCacheSize = 600000000L;
			File configFile = new File(fileStr);
			if (configFile.exists() == false) {
				setupAceConfig(config, configFile, defaultCacheSize, false);
			} else {
				FileInputStream fis = new FileInputStream(configFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				try {
					config = (AceConfig) ois.readObject();
				} catch (IOException e) {
					if (e.getMessage().equalsIgnoreCase(authFailureMsg)) {
						IOException ioe = e;
						while (ioe != null) {
							ioe = null;
							ioe = loopOpenOrCreateConfig(defaultCacheSize, ioe);
						}

					} else {
						throw e;
					}
				}
			}
			File logConfigFile = new File(configFile.getParent(),
					config.loggerRiverConfigFile);
			if (logConfigFile.exists() == false) {
				URL logConfigUrl = AceConfig.class
						.getResource("/org/dwfa/resources/core/config/logViewer.config");
				AceLog.getAppLog().info(
						"Config file does not exist... " + logConfigUrl);
				InputStream is = logConfigUrl.openStream();
				FileOutputStream fos = new FileOutputStream(logConfigFile);
				FileIO.copyFile(is, fos, true);
				is.close();
			}
			new LogViewerFrame(
					new String[] { logConfigFile.getCanonicalPath() }, null);

			File aceRiverConfigFile = new File(configFile.getParent(), config
					.getAceRiverConfigFile());
			if (aceRiverConfigFile.exists() == false) {
				URL configUrl = AceConfig.class
						.getResource("/org/dwfa/ace/config/ace.config");
				AceLog.getAppLog().info(
						"Config file does not exist... " + configUrl);
				InputStream is = configUrl.openStream();
				FileOutputStream fos = new FileOutputStream(aceRiverConfigFile);
				FileIO.copyFile(is, fos, true);
				is.close();
			}

			ACE.setAceConfig(config);
			for (I_ConfigAceFrame ace : config.aceFrames) {
				if (ace.isActive()) {
					AceFrame af = new AceFrame(
							new String[] { aceRiverConfigFile.getAbsolutePath() },
							null, ace, false);
					af.setVisible(true);
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	private static IOException loopOpenOrCreateConfig(long defaultCacheSize,
			IOException ioe) throws TaskFailedException,
			ClassNotFoundException, DatabaseException, ParseException,
			TerminologyException, IOException {
		try {
			// Custom button text
			Object[] options = { "Select Config", "New Config", "Cancel" };
			int n = JOptionPane.showOptionDialog(null,
					"Would you like to select or "
							+ "\ncreate a configuration file?",
					"Authorization failure", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			switch (n) {
			case 0:
				// for reading existing
				File inFile = FileDialogUtil.getExistingFile(
						"Select a configuration file", new FilenameFilter() {
							public boolean accept(File dir, String name) {
								return name.endsWith(".ace");
							}
						});
				ObjectInputStream configStream = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(inFile)));
				config = (AceConfig) configStream.readObject();

				break;
			case 1:
				// for creating new
				File outFile = FileDialogUtil
						.getNewFile("Save environment to config file...");
				if (outFile.getName().endsWith(".ace") == false) {
					outFile = new File(outFile.getParentFile(), outFile
							.getName()
							+ ".ace");
				}
				config = new AceConfig();
				setupAceConfig(config, outFile, defaultCacheSize, false);
				break;
			case 2:
			default:
				System.out.println("System.exit from AceConfig ");
				System.exit(0);

			}
		} catch (IOException innerIoe) {
			if (innerIoe.getMessage().equalsIgnoreCase(authFailureMsg)) {
				ioe = innerIoe;
			} else {
				throw innerIoe;
			}
		}
		return ioe;
	}

	public static void setupAceConfig(AceConfig config, File configFile,
			Long cacheSize, boolean includeSnomed) throws DatabaseException, ParseException,
			TerminologyException, IOException, FileNotFoundException {
		try {
			AceConfig.getVodb().setup(config.dbFolder, config.readOnly, cacheSize);
		} catch (Exception e) {
			throw new ToIoException(e);
		}
		SvnPrompter prompter = new SvnPrompter();
		prompter.prompt("config file", "username");

		I_ConfigAceFrame profile = NewDefaultProfile.newProfile(prompter.getUsername(), prompter.getPassword(), 
				"admin", "visit.bend");
		config.aceFrames.add(profile);

		if (config.getUsername() == null) {
			config.setChangeSetWriterFileName("nullUser."
					+ UUID.randomUUID().toString() + ".jcs");
		} else {
			config.setChangeSetWriterFileName(config.getUsername() + "."
					+ UUID.randomUUID().toString() + ".jcs");
		}
        config.changeSetRoot = new File("profiles" + File.separator + "users" + File.separator + config.getUsername());
        config.addChangeSetWriters();

        if (configFile == null) {
        	File profileDir = new File("profiles");
        	File userDir = new File(profileDir, profile.getUsername());
        	userDir.mkdirs();
        	configFile = new File(userDir, profile.getUsername() + ".ace");
        }
		FileOutputStream fos = new FileOutputStream(configFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(config);
		oos.close();
	}
    
    public void addChangeSetWriters() {
        ACE.getCsWriters().add(new BinaryChangeSetWriter(new File(changeSetRoot, 
                                                                  getChangeSetWriterFileName()), 
                                                                  new File(changeSetRoot, 
                                                                           "." + getChangeSetWriterFileName())));
        ACE.getCsWriters().add(new CommitLog(new File(changeSetRoot, 
                "commitLog.xls"), 
                new File(changeSetRoot, 
                         "." + "commitLog.xls")));
    }

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}

	public void addVetoableChangeListener(String propertyName,
			VetoableChangeListener listener) {
		vetoSupport.addVetoableChangeListener(propertyName, listener);
	}

	public void addVetoableChangeListener(VetoableChangeListener listener) {
		vetoSupport.addVetoableChangeListener(listener);
	}

	public void removeVetoableChangeListener(String propertyName,
			VetoableChangeListener listener) {
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

		File srcJarFile = new File(fileProtocolParts[1].replace("foundation",
				"ace-bdb").replace("dwfa", "jehri"));
		File targetDir = config.dbFolder.getParentFile();
		AceLog.getAppLog().info("Jar file: " + srcJarFile);
		if (targetDir.exists()
				&& targetDir.lastModified() == srcJarFile.lastModified()) {
			AceLog.getAppLog().info("ace-db is current...");
		} else {
			AceLog.getAppLog().info("ace-db needs update...");
			targetDir.mkdirs();
			AceLog.getAppLog().info(
					"Now extracting into: " + targetDir.getCanonicalPath());
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

    public Long getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(Long cacheSize) {
        this.cacheSize = cacheSize;
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
              String[] nameParts = getChangeSetWriterFileName().split("#");
              int sequence = Integer.parseInt(nameParts[1]);
              sequence++;
              setChangeSetWriterFileName(nameParts[0] + '#' + sequence + "#" + nameParts[2]);
              AceLog.getAppLog().info("change set exceeds " + maxSize + 
                    " bytes. Incrementing file to: " + getChangeSetWriterFileName());
           }
        }
        FileOutputStream fos = new FileOutputStream(profileFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.close();
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
}
