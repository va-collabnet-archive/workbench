package org.dwfa.ace.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;

import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.cs.BinaryChangeSetReader;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.cs.ChangeSetImporter;
import org.dwfa.ace.tree.ExpandNodeSwingWorker;
import org.dwfa.ace.url.tiuid.ExtendedUrlStreamHandlerFactory;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.SelectObjectDialog;
import org.dwfa.fd.FileDialogUtil;
import org.dwfa.queue.QueueServer;
import org.dwfa.svn.Svn;
import org.dwfa.svn.SvnPrompter;
import org.dwfa.util.io.FileIO;
import org.dwfa.vodb.VodbEnv;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Depth;
import org.tigris.subversion.javahl.Revision;

import com.sun.jini.start.LifeCycle;

public class AceRunner {

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
					JOptionPane
							.showMessageDialog(null, "<html>Your ip address ("
									+ currentLocalHost.toString()
									+ ") <br> has changed since startup ("
									+ startupLocalHost.toString()
									+ ") <br> please restart your application.");
				}
			} catch (UnknownHostException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}

	}

	public static String[] args;
	public static LifeCycle lc;
	/**
	 * The jini configuration provider
	 */
	protected Configuration jiniConfig;

	private static boolean firstStartup = true;

	private File aceConfigFile;

	public AceRunner(String[] args, LifeCycle lc) {
		try {
			AceRunner.args = args;
			AceRunner.lc = lc;

			ActivityViewer.setHeadless(false);

			setupCustomProtocolHandler();

			AceLog.getAppLog().info(
					"\n*******************\n" + "\n Starting "
							+ this.getClass().getSimpleName()
							+ "\n with config file: " + getArgString(args)
							+ "\n\n******************\n");

			jiniConfig = ConfigurationProvider.getInstance(args, getClass()
					.getClassLoader());

			setupLookAndFeel();
			setupSwingExpansionTimerLogging();
			setupIpChangeListener();
			setBerkeleyDbAsTransactional();

			Long cacheSize = setBerkeleyDbCacheSize();

			File acePropertiesFile = new File("config","ace.properties");
			if (acePropertiesFile.exists() == false) {
				try {
					initialSubversionOperationsAndChangeSetImport(cacheSize, acePropertiesFile);
				} catch (Exception ex) {
					AceLog.getAppLog().alertAndLogException(ex);
					System.exit(0);
				}
			}
			final Properties aceProperties = new Properties();
			if (acePropertiesFile.exists()) {
				aceProperties.loadFromXML(new FileInputStream(acePropertiesFile));
			}

			aceConfigFile = (File) jiniConfig.getEntry(this.getClass()
					.getName(), "aceConfigFile", File.class, new File(
					"src/main/config/config.ace"));

			if (aceConfigFile.exists()) {

				// Put up a dialog to select the configuration file...
				SwingUtilities.invokeAndWait(new Runnable() {

					public void run() {
						File profileDir = new File("profiles" + File.separator
								+ "users");
						if (profileDir.exists() == false) {
							profileDir = new File("profiles");
							if (profileDir.exists() == false) {
								profileDir.mkdirs();
							}
						}
						File lastProfileDir = profileDir;
						if (aceProperties.getProperty("last-profile-dir") != null) {
							lastProfileDir = new File(aceProperties.getProperty("last-profile-dir"));
						}
						try {
							aceConfigFile = FileDialogUtil.getExistingFile(
									"Please select your user profile:",
									new FilenameFilter() {
										public boolean accept(File dir,
												String name) {
											return name.toLowerCase().endsWith(
													".ace");
										}
									}, lastProfileDir);
							aceProperties.setProperty("last-profile-dir", FileIO.getRelativePath(aceConfigFile));
						} catch (TaskFailedException e) {
							AceLog.getAppLog().alertAndLogException(e);
							System.exit(0);
						}
					}

				});

				ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(
								aceConfigFile)));
				AceConfig.config = (AceConfig) ois.readObject();
				AceConfig.config.setProfileFile(aceConfigFile);
				setupDatabase(AceConfig.config, aceConfigFile);
			} else {
				File dbFolder = (File) jiniConfig.getEntry(this.getClass()
						.getName(), "dbFolder", File.class, new File(
						"target/berkeley-db"));
				AceLog.getAppLog().info(
						"Cache size in config file: " + cacheSize);
				AceConfig.config = new AceConfig(dbFolder);
				AceConfig.config.setProfileFile(aceConfigFile);
				setupDatabase(AceConfig.config, aceConfigFile);
				AceConfig.setupAceConfig(AceConfig.config, aceConfigFile,
						cacheSize, false);
			}
			aceProperties.storeToXML(new FileOutputStream(acePropertiesFile), null);
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
								|| ace.getPassword().equals(
										prompter.getPassword()) == false) {
							prompter.prompt("Please authenticate for: "
									+ ace.getFrameName(), ace.getUsername());
						}
						if (ace.getUsername().equals(prompter.getUsername())
								&& ace.getPassword().equals(
										prompter.getPassword())) {
							if (ace.isAdministrative()) {
								login = false;
								successCount++;
								handleAdministrativeFrame(prompter, ace);
								
							} else {
								login = false;
								successCount++;
								handleNormalFrame(ace);
							}
						} else {
							login = false;
							int n = JOptionPane.showConfirmDialog(null,
									"Would you like to try again?",
									"Login failed", JOptionPane.YES_NO_OPTION);
							if (n == JOptionPane.YES_OPTION) {
								login = true;
							}
						}
					}
				}
			}

			if (successCount == 0) {
				JOptionPane.showMessageDialog(null,
						"No frames where opened. Now exiting.",
						"No successful logins...", JOptionPane.ERROR_MESSAGE);
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
           for (String queue: AceConfig.config.getQueues()) {
        	   File queueFile = new File(queue);
            	AceLog.getAppLog().info("Found queue: " + queueFile.toURI().toURL().toExternalForm());
            	if (QueueServer.started(queueFile)) {
                	AceLog.getAppLog().info("Queue already started: " + queueFile.toURI().toURL().toExternalForm());
            	} else {
                    new QueueServer(new String[] { queueFile.getCanonicalPath() }, lc);
            	}
            }			
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
			System.exit(0);
		}
	}

	private void handleNormalFrame(final I_ConfigAceFrame ace) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					boolean startup = firstStartup;
					firstStartup = false;
					AceFrame af = new AceFrame(
							AceRunner.args,
							AceRunner.lc, ace, startup);
					af.setVisible(true);
				} catch (Exception e) {
					AceLog.getAppLog()
							.alertAndLogException(e);
				}
			}

		});
	}

	private void handleAdministrativeFrame(SvnPrompter prompter,
			final I_ConfigAceFrame ace) {
		String username = prompter.getUsername();
		String password = prompter.getPassword();
		boolean tryAgain = true;
		prompter.setUsername(ace.getAdminUsername());
		prompter.setPassword("");
		while (tryAgain) {
			prompter.prompt(
							"Please authenticate as an administrative user:",
							ace.getAdminUsername());
			if (ace.getAdminUsername().equals(prompter.getUsername())
					&& ace.getAdminPassword().equals(prompter.getPassword())) {
				SwingUtilities
						.invokeLater(new Runnable() {
							public void run() {
								try {
									boolean startup = firstStartup;
									firstStartup = false;
									String regularPluginRoot = AceFrame.getPluginRoot();
									AceFrame.setPluginRoot(AceFrame.getAdminPluginRoot());
									AceFrame newFrame = new AceFrame(
											AceRunner.args,
											AceRunner.lc,
											ace,
											startup);
									AceFrame.setPluginRoot(regularPluginRoot);
									ace.setSubversionToggleVisible(true);
									newFrame.setTitle(newFrame.getTitle().replace(
															"Editor",
															"Administrator"));
									newFrame.setVisible(true);
								} catch (Exception e) {
									AceLog.getAppLog().alertAndLogException(e);
								}
							}
						});

				tryAgain = false;
				prompter.setPassword("");
			} else {
				int n = JOptionPane
						.showConfirmDialog(
								null,
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

	private void initialSubversionOperationsAndChangeSetImport(Long cacheSize, File acePropertiesFile)
			throws ConfigurationException, FileNotFoundException, IOException, TaskFailedException, ClientException {
		Properties aceProperties = new Properties();
		aceProperties.setProperty("initial-svn-checkout", "true");
		
		
		String svnCheckoutProfileOnStart = (String) jiniConfig.getEntry(this
				.getClass().getName(), "svnCheckoutProfileOnStart", String.class,
				"");
		
		
		String[] svnCheckoutOnStart = (String[]) jiniConfig.getEntry(this
				.getClass().getName(), "svnCheckoutOnStart", String[].class,
				new String[] {});
		String[] svnUpdateOnStart = (String[]) jiniConfig.getEntry(this
				.getClass().getName(), "svnUpdateOnStart", String[].class,
				new String[] {});
		List<File> changeLocations = new ArrayList<File>();
		if ((svnCheckoutOnStart != null && svnCheckoutOnStart.length > 0)
				|| (svnUpdateOnStart != null && svnUpdateOnStart.length > 0)
				|| (svnCheckoutProfileOnStart != null && svnCheckoutProfileOnStart.length() > 0)) {
			boolean connectToSubversion = (JOptionPane.YES_OPTION == JOptionPane
					.showConfirmDialog(
							null,
							"Would you like to connect over the network to Subversion?",
							"Confirm network operation",
							JOptionPane.YES_NO_OPTION));
			if (connectToSubversion) {
				if (svnCheckoutProfileOnStart != null && svnCheckoutProfileOnStart.length() > 0) {
					handleSvnProfileCheckout(changeLocations, svnCheckoutProfileOnStart, aceProperties);
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
					doStealthChangeSetImport(cacheSize, changeLocations);
				}
				aceProperties.storeToXML(new FileOutputStream(acePropertiesFile), null);
			} else {
				throw new TaskFailedException("User did not want to connect to Subversion.");
			}
		}
	}

	private void handleSvnProfileCheckout(List<File> changeLocations, String svnSpec, Properties aceProperties) throws ClientException, TaskFailedException {
		SubversionData svd = new SubversionData(svnSpec, null);
		List<String> listing = Svn.list(svd);
		Map<String, String> profileMap = new HashMap<String, String>();
		for (String item: listing) {
			if (item.endsWith(".ace")) {
				profileMap.put(item.substring(item.lastIndexOf("/") + 1).replace(".ace", ""), item);
			}
		}
		SortedSet<String> sortedProfiles = new TreeSet<String>(profileMap.keySet());
		JFrame emptyFrame = new JFrame();
		String selectedProfile = (String) SelectObjectDialog.showDialog(emptyFrame, emptyFrame, 
				"Select profile to checkout:",
		          "Checkout profile:", sortedProfiles.toArray(), null, null);
		String selectedPath = profileMap.get(selectedProfile);
		if (selectedPath.startsWith("/")) {
			selectedPath = selectedPath.substring(1);
		}
		String[] pathParts = selectedPath.split("/");
		String[] specParts = svnSpec.split("/");
		int matchStart = 0;
		for (int i = 0; i < specParts.length; i++) {
			if (specParts[i].equals(pathParts[i-matchStart])) {
				
			} else {
				matchStart = i+1;
			}
		}
		List<String> specList = new ArrayList<String>();
		for (int i = 0; i < matchStart; i++) {
			specList.add(specParts[i]);
		}
		for (String pathPart: pathParts) {
			specList.add(pathPart);
		}
		StringBuffer checkoutBuffer = new StringBuffer();
		for (int i = 0; i < specList.size() -1; i++) {
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
		Svn.getSvnClient().checkout(moduleName, destPath, revision,
				pegRevision, depth, ignoreExternals,
				allowUnverObstructions);
		changeLocations.add(new File(destPath));
	}
	
	private void handleSvnCheckout(List<File> changeLocations, String svnSpec) throws TaskFailedException, ClientException {
		AceLog.getAppLog().info("Got svn checkout spec: " + svnSpec);
		String[] specParts = new String[] {
				svnSpec.substring(0, svnSpec.lastIndexOf("|")),
				svnSpec.substring(svnSpec.lastIndexOf("|") + 1) };
		int server = 0;
		int local = 1;
		specParts[local] = specParts[local].replace('/', File.separatorChar);
		File checkoutLocation = new File(specParts[local]);
		if (checkoutLocation.exists()) {
			// already checked out
			AceLog.getAppLog().info(
					specParts[server] + " already checked out to: "
							+ specParts[local]);
		} else {

				// do the checkout...
				AceLog.getAppLog().info(
						"svn checkout " + specParts[server] + " to: "
								+ specParts[local]);
				String moduleName = specParts[server];
				String destPath = specParts[local];
				Revision revision = Revision.HEAD;
				Revision pegRevision = Revision.HEAD;
				int depth = Depth.infinity;
				boolean ignoreExternals = false;
				boolean allowUnverObstructions = false;
				Svn.getSvnClient().checkout(moduleName, destPath, revision,
						pegRevision, depth, ignoreExternals,
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
			Svn.getSvnClient().update(path, revision, depth, depthIsSticky,
					ignoreExternals, allowUnverObstructions);
			changeLocations.add(new File(path));
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	private void doStealthChangeSetImport(Long cacheSize,
			List<File> changeLocations) {
		// import any change sets that may be downloaded
		// from svn...
		try {
			File dbFolder = (File) jiniConfig.getEntry(this.getClass()
					.getName(), "dbFolder", File.class, new File(
					"target/berkeley-db"));

			final VodbEnv stealthVodb = new VodbEnv(true);
			AceConfig.stealthVodb = stealthVodb;
			LocalVersionedTerminology.setStealthfactory(stealthVodb);
			stealthVodb.setup(dbFolder, false, cacheSize);

			ChangeSetImporter jcsImporter = new ChangeSetImporter() {

				@Override
				public I_ReadChangeSet getChangeSetReader(File csf) {
					BinaryChangeSetReader csr = new BinaryChangeSetReader();
					csr.setChangeSetFile(csf);
					csr.setVodb(stealthVodb);
					return csr;
				}

			};

			for (File checkoutLocation : changeLocations) {
				jcsImporter
						.importAllChangeSets(AceLog.getAppLog().getLogger(),
								null, checkoutLocation.getAbsolutePath(),
								false, ".jcs");
			}

			stealthVodb.close();
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		AceConfig.stealthVodb = null;
		LocalVersionedTerminology.setStealthfactory(null);
	}

	private Long setBerkeleyDbCacheSize() throws ConfigurationException {
		Long cacheSize = (Long) jiniConfig.getEntry(this.getClass().getName(),
				"cacheSize", Long.class, null);
		AceLog.getAppLog().info("cacheSize " + cacheSize);
		if (cacheSize != null) {
			VodbEnv.setCacheSize(cacheSize);
		}
		return cacheSize;
	}

	private void setupLookAndFeel() throws ConfigurationException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		String lookAndFeelClassName = (String) jiniConfig.getEntry(this
				.getClass().getName(), "lookAndFeelClassName", String.class,
				UIManager.getSystemLookAndFeelClassName());

		UIManager.setLookAndFeel(lookAndFeelClassName);
	}

	private void setBerkeleyDbAsTransactional() {
		VodbEnv.setTransactional(true);
		VodbEnv.setTxnNoSync(false);
		VodbEnv.setDeferredWrite(false);
	}

	private void setupIpChangeListener() throws ConfigurationException,
			UnknownHostException {
		Boolean listenForIpChanges = (Boolean) jiniConfig.getEntry(this
				.getClass().getName(), "listenForIpChanges", Boolean.class,
				null);
		if (listenForIpChanges != null) {
			if (listenForIpChanges) {
				Timer ipChangeTimer = new Timer(2 * 60 * 1000,
						new CheckIpAddressForChanges());
				ipChangeTimer.start();
			}
		}
	}

	private void setupSwingExpansionTimerLogging()
			throws ConfigurationException {
		Boolean logTimingInfo = (Boolean) jiniConfig.getEntry(this.getClass()
				.getName(), "logTimingInfo", Boolean.class, null);
		if (logTimingInfo != null) {
			ExpandNodeSwingWorker.setLogTimingInfo(logTimingInfo);
		}
		AceLog.getAppLog().info(
				"Swing expansion logTimingInfo " + logTimingInfo);
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

	private void setupCustomProtocolHandler() {
		AceLog.getAppLog().info(
				"java.protocol.handler.pkgs: "
						+ System.getProperty("java.protocol.handler.pkgs"));
		URL.setURLStreamHandlerFactory(new ExtendedUrlStreamHandlerFactory());
	}

	private void setupDatabase(AceConfig aceConfig, File configFileFile) throws IOException {
		if (aceConfig.isDbCreated() == false) {
			int n = JOptionPane
					.showConfirmDialog(
							new JFrame(),
							"Would you like to extract the db from your maven repository?",
							"DB does not exist", JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION) {
				AceConfig.extractMavenLib(aceConfig);
			} else {
				AceLog
						.getAppLog()
						.info(
								"Exiting, user did not want to extract the DB from maven.");
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
			if (file.getName().equalsIgnoreCase("queue.config")
					&& QueueServer.started(file) == false) {
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

}
