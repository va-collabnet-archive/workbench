package org.ihtsdo.db.runner;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;

import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.commitlog.CommitLog;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.config.AceLoginDialog;
import org.dwfa.ace.config.AceProtocols;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.ace.tree.ExpandNodeSwingWorker;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.queue.QueueServer;
import org.dwfa.svn.Svn;
import org.dwfa.swing.SwingWorker;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.io.FileIO;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.custom.statics.CustomStatics;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.objectCache.ObjectCache;
import org.ihtsdo.objectCache.ObjectCacheClassHandler;

import com.sun.jini.start.LifeCycle;

public class WorkbenchRunner {
	private static final String WB_PROPERTIES = "wb.properties";

	static {
		DwfaEnv.setHeadless(false);
	}

	public static String[] args;
	public static LifeCycle lc;
	private static boolean firstStartup = true;
	/**
	 * The jini configuration provider
	 */
	public static Configuration jiniConfig;
	public static File wbConfigFile;
	public static Properties wbProperties;
	public static Boolean initializeFromSubversion = false;
	public static String[] svnUpdateOnStart = null;
	public static File userProfile;

	public WorkbenchRunner(String[] args, LifeCycle lc) {
		try {
			AceProtocols.setupExtraProtocols();

			WorkbenchRunner.args = args;
			WorkbenchRunner.lc = lc;

			AceLog.getAppLog().info(
					"\n*******************\n" + "\n Starting "
							+ this.getClass().getSimpleName()
							+ "\n with config file: " + getArgString(args)
							+ "\n\n******************\n");
			if (new File(args[0]).exists()) {
				jiniConfig = ConfigurationProvider.getInstance(args, getClass()
						.getClassLoader());
			}

			System.setProperty("javax.net.ssl.trustStore", "config/cacerts");
			long startTime = System.currentTimeMillis();
			ActivityPanel activity = new ActivityPanel(true, null, null);
			activity.setIndeterminate(true);
			activity.setProgressInfoUpper("Loading the database");
			activity.setProgressInfoLower("Setting up the environment...");
			activity.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("System.exit from activity action listener: "
									+ e.getActionCommand());
					System.exit(0);
				}
			});
			ActivityViewer.addActivity(activity);

			Bdb.setup("berkeley-db", activity);
			activity.setProgressInfoLower("complete");
			activity.complete();
			long loadTime = System.currentTimeMillis() - startTime;
			AceLog.getAppLog().info("### Load time: " + loadTime + " ms");
			AceLog.getAppLog().info("Adding bdb shutdown hook. ");
			Runtime.getRuntime().addShutdownHook(new Thread() {

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

			setupLookAndFeel();
			setupSwingExpansionTimerLogging();
			
			if (System.getProperty("viewer") != null && System.getProperty("viewer").toLowerCase().startsWith("t")) {
			    ACE.editMode = false;
			}
			
			if (System.getProperty("newprofile") != null) {
                File dbFolder = new File("berkeley-db");
                if (jiniConfig != null) {
                    dbFolder = (File) jiniConfig.getEntry(this.getClass().getName(), "dbFolder", File.class,
                        new File("target/berkeley-db"));
                }
                AceConfig.config = new AceConfig(dbFolder);
                String username = "username";
                File profileFile = new File("profiles/" + username, username + ".ace");
                AceConfig.config.setProfileFile(profileFile);
                AceConfig.setupAceConfig(AceConfig.config, null, null, false);

	            File startupFolder = new File(profileFile.getParent(), "startup");
	            startupFolder.mkdirs();

	            File shutdownFolder = new File(profileFile.getParent(), "shutdown");
	            shutdownFolder.mkdirs();
			}

			File wbPropertiesFile = new File("config", WB_PROPERTIES);
			boolean acePropertiesFileExists = wbPropertiesFile.exists();
			wbProperties = new Properties();
			
			boolean initialized = false;
			if (acePropertiesFileExists) {
				wbProperties.loadFromXML(new FileInputStream(wbPropertiesFile));
				initialized = Boolean.parseBoolean((String) wbProperties
						.get("initialized"));
			}
			if (acePropertiesFileExists == false || initialized == false) {
				try {
					new SvnHelper(WorkbenchRunner.class, jiniConfig)
							.initialSubversionOperationsAndChangeSetImport(wbPropertiesFile);
				} catch (Exception ex) {
					AceLog.getAppLog().alertAndLogException(ex);
					System.exit(0);
				}
			}

			wbProperties.put("initialized", "true");
			//Check to see if there is a custom Properties file
			checkCustom();

			if (jiniConfig != null) {
				wbConfigFile = (File) jiniConfig.getEntry(this.getClass()
						.getName(), "wbConfigFile", File.class, new File(
						"config/wb.config"));
				initializeFromSubversion = (Boolean) jiniConfig.getEntry(this
						.getClass().getName(), "initFromSubversion",
						Boolean.class, Boolean.FALSE);
				svnUpdateOnStart = (String[]) jiniConfig.getEntry(this
						.getClass().getName(), "svnUpdateOnStart",
						String[].class, null);
			} else {
				wbConfigFile = new File("config/wb.config");
			}

			SvnPrompter prompter = new SvnPrompter();
			File profileDir = new File("profiles");
			if ((profileDir.exists() == false && initializeFromSubversion)
					|| (svnUpdateOnStart != null)) {
	            Svn.setConnectedToSvn(true);
				new SvnHelper(WorkbenchRunner.class, jiniConfig)
						.initialSubversionOperationsAndChangeSetImport(new File(
								"config", WB_PROPERTIES));
			} else if (profileDir.exists()) {
				ArrayList<File> profileLoc = new ArrayList<File>();
				profileLoc.add(profileDir);
				new SvnHelper(WorkbenchRunner.class, jiniConfig)
						.doChangeSetImport(profileLoc);
			}

			if (wbConfigFile == null || !wbConfigFile.exists()) {
				if (acePropertiesFileExists) {
					wbProperties.loadFromXML(new FileInputStream(
							wbPropertiesFile));
				}
				String lastProfileDirStr = "profiles";
				if (wbProperties.getProperty("last-profile-dir") != null) {
					lastProfileDirStr = wbProperties
							.getProperty("last-profile-dir");
				}
				File lastProfileDir = new File(lastProfileDirStr);

				if (lastProfileDir.isFile()) {
					wbConfigFile = lastProfileDir;
				} else {
					String[] profileFiles = lastProfileDir.list(new FilenameFilter() {
								public boolean accept(File dir, String name) {
									return name.endsWith(".ace");
								}
							});

					if (profileFiles != null && profileFiles.length == 1) {
						wbConfigFile = new File(lastProfileDir, profileFiles[0])
								.getCanonicalFile();
					} else if (profileFiles != null && profileFiles.length > 1) {
						AceLog.getAppLog().warning(
										"Profile from jini configuration does not exist and more than one profile file found in "
												+ "last profile directory "
												+ lastProfileDir
												+ ", unable to determine profile to use.");
					}
				}
			}

			if (wbConfigFile != null && wbConfigFile.exists()
					&& wbPropertiesFile.exists()) {

				// Put up a dialog to select the configuration file...
				CountDownLatch latch = new CountDownLatch(1);
				GetProfileWorker profiler = new GetProfileWorker(latch);
				profiler.start();
				latch.await();

				File jeUserPropertiesFile = new File(userProfile
						.getParentFile(), "je.properties");
				if (jeUserPropertiesFile.exists()) {
					File jeDbPropertiesFile = new File(AceConfig.config
							.getDbFolder(), "je.properties");
					FileIO.copyFile(jeUserPropertiesFile, jeDbPropertiesFile);
				}
				ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(
								new FileInputStream(userProfile)));
				AceConfig.config = (AceConfig) ois.readObject();
				AceConfig.config.setProfileFile(userProfile);
				AceConfig.config.getUsername();
				prompter.setUsername(AceConfig.config.getUsername());
				prompter.setPassword(profiler.getPassword());
			} else {
				if (initializeFromSubversion) {
					JOptionPane
							.showMessageDialog(LogWithAlerts
									.getActiveFrame(null),
									"Unable to initialize from subversion. Is the network connected?");
				} else {
					File dbFolder = new File("berkeley-db");
					if (jiniConfig != null) {
						dbFolder = (File) jiniConfig.getEntry(this.getClass()
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
			// TODO. Get config to work with new change sets AceConfig.config.addChangeSetWriters();
			String writerName = AceConfig.config.getChangeSetWriterFileName();
			if (!writerName.endsWith(".eccs")) {
				String firstPart = writerName.substring(0, writerName.lastIndexOf('.'));
				writerName = firstPart.concat(".eccs");
				AceConfig.config.setChangeSetWriterFileName(writerName);
			}

			ChangeSetWriterHandler.addWriter(new EConceptChangeSetWriter(
					new File(AceConfig.config.getChangeSetRoot(),
							AceConfig.config.getChangeSetWriterFileName()),
					new File(AceConfig.config.getChangeSetRoot(), "."
							+ AceConfig.config.getChangeSetWriterFileName()),
							ChangeSetPolicy.MUTABLE_ONLY, true));
			ChangeSetWriterHandler.addWriter(new CommitLog(new File(
					AceConfig.config.getChangeSetRoot(), "commitLog.xls"),
					new File(AceConfig.config.getChangeSetRoot(), "."
							+ "commitLog.xls")));

			
			int successCount = 0;
			int frameCount = 0;

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
				JOptionPane.showMessageDialog(LogWithAlerts
						.getActiveFrame(null),
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
			List<String> queuesToRemove = new ArrayList<String>();
			for (String queue : AceConfig.config.getQueues()) {
				queue = queue.replace('\\', '/');
				File queueFile = new File(queue);
				if (queueFile.exists()) {
					AceLog.getAppLog().info(
							"Found queue: "
									+ queueFile.toURI().toURL()
											.toExternalForm());
					if (QueueServer.started(queueFile)) {
						AceLog.getAppLog().info(
								"Queue already started: "
										+ queueFile.toURI().toURL()
												.toExternalForm());
					} else {
						new QueueServer(new String[] { queueFile
								.getCanonicalPath() }, lc);
					}
				} else {
					queuesToRemove.add(queue);
				}
			}
			if (queuesToRemove.size() > 0) {
				AceConfig.config.getQueues().removeAll(queuesToRemove);
				StringBuffer buff = new StringBuffer();
				buff
						.append("<html><body>Removing queues that are not accessible: <br>");
				for (String queue : queuesToRemove) {
					buff.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
					buff.append(queue);
					buff.append("<br>");
				}
				buff.append("</body></html>");

				AceLog.getAppLog().alertAndLog(
						Level.WARNING,
						buff.toString(),
						new Exception(
								"Removing queues that are not accessable: "
										+ queuesToRemove));
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
			System.exit(0);
		}
	}

	private static class GetProfileWorker extends SwingWorker<Boolean> {
		StartupFrameListener fl = new StartupFrameListener();
		JFrame parentFrame = new JFrame();
		boolean newFrame = false;
		private File lastProfileDir;
		private String password;
		private AceLoginDialog loginDialog;
		CountDownLatch latch;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

		public GetProfileWorker(CountDownLatch latch) {
			super();
			parentFrame = new JFrame();
			if (OpenFrames.getNumOfFrames() > 0) {
				parentFrame = OpenFrames.getFrames().iterator().next();
				AceLog.getAppLog().info("### Adding an existing frame");
			} else {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						public void run() {
							parentFrame.setContentPane(new JLabel(
									"The Terminology IDE is starting..."));
							parentFrame.pack();

							parentFrame.setVisible(true);
							parentFrame.setLocation((d.width / 2)
									- (parentFrame.getWidth() / 2),
									(d.height / 2)
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
			loginDialog = new AceLoginDialog(parentFrame);
			loginDialog.setConnectToSvn(initializeFromSubversion);
			loginDialog.setLocation((d.width / 2)
					- (loginDialog.getWidth() / 2), (d.height / 2)
					- (loginDialog.getHeight() / 2));
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
				lastProfileDir = new File(wbProperties
						.getProperty("last-profile-dir"));
			}
			OpenFrames.addFrameListener(fl);
			if (OpenFrames.getNumOfFrames() > 0) {
				parentFrame = OpenFrames.getFrames().iterator().next();
				AceLog.getAppLog().info("### Using an existing frame 1");
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {

					public void run() {
						parentFrame.setContentPane(new JLabel(
								"The Terminology IDE is starting..."));
						parentFrame.pack();
						Dimension d = Toolkit.getDefaultToolkit()
								.getScreenSize();
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
				password = new String(loginDialog.getPassword());
		        Svn.setConnectedToSvn(loginDialog.connectToSvn());

				wbProperties.setProperty("last-profile-dir", FileIO
						.getRelativePath(userProfile));

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
					if (ace.getViewPositionSet() == null
							|| ace.getViewPositionSet().size() == 0) {
						Set<I_Position> viewPositions = new HashSet<I_Position>();

						viewPositions
								.add(new Position(
										Integer.MAX_VALUE,
										Bdb
												.getPathManager()
												.get(
														ArchitectonicAuxiliary.Concept.SNOMED_CORE
																.localize()
																.getNid())));
						viewPositions
								.add(new Position(
										Integer.MAX_VALUE,
										Bdb
												.getPathManager()
												.get(
														ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
																.localize()
																.getNid())));
						ace.setViewPositions(viewPositions);
					}
					AceFrame af = new AceFrame(args, lc, ace, startup);
					af.setVisible(true);
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
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
			prompter.prompt("Please authenticate as an administrative user:",
					ace.getAdminUsername());
			if (ace.getAdminUsername().equals(prompter.getUsername())
					&& ace.getAdminPassword().equals(prompter.getPassword())) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							boolean startup = firstStartup;
							firstStartup = false;
							String regularPluginRoot = AceFrame.getPluginRoot();
							AceFrame.setPluginRoot(AceFrame
									.getAdminPluginRoot());
							AceFrame newFrame = new AceFrame(args, lc, ace,
									startup);
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
				int n = JOptionPane.showConfirmDialog(null,
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

	private void setupLookAndFeel() throws ConfigurationException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		if (jiniConfig != null) {
			String lookAndFeelClassName = (String) jiniConfig.getEntry(this
					.getClass().getName(), "lookAndFeelClassName",
					String.class, UIManager.getSystemLookAndFeelClassName());

			UIManager.setLookAndFeel(lookAndFeelClassName);
		} else {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
	}

	private void setupSwingExpansionTimerLogging()
			throws ConfigurationException {
		if (jiniConfig != null) {
			Boolean logTimingInfo = (Boolean) jiniConfig
					.getEntry(this.getClass().getName(), "logTimingInfo",
							Boolean.class, null);
			if (logTimingInfo != null) {
				ExpandNodeSwingWorker.setLogTimingInfo(logTimingInfo);
			}
			AceLog.getAppLog().info(
					"Swing expansion logTimingInfo " + logTimingInfo);
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
	
	private void checkCustom() {
		String custPropFN = null;
		try {
		if(wbProperties.getProperty(CustomStatics.CUSTOMPROPS) != null) {
			custPropFN = wbProperties.getProperty(CustomStatics.CUSTOMPROPS);
			AceLog.getAppLog().info("checkCustom custPropFN = "+custPropFN);
			File custPropertiesFile = new File("config",custPropFN );
			if(custPropertiesFile.exists() && custPropertiesFile.canRead()) {
				String cpfn = custPropertiesFile.getCanonicalPath();
				Properties custProps = new Properties();
				custProps.loadFromXML(new FileInputStream(custPropertiesFile));
				if(custProps.getProperty(CustomStatics.CUSTOM_UI_CLASSNAME) != null) {
					String custCN = custProps.getProperty(CustomStatics.CUSTOM_UI_CLASSNAME);
					AceLog.getAppLog().info("checkCustom custCN = "+custCN);
					Object obj = ObjectCacheClassHandler.getInstClass(custCN);
					if(obj != null) {
						ObjectCache.put(CustomStatics.CUSTOMPROPSFN, cpfn);
						ObjectCache.put(CustomStatics.CUSTOM_UI_CLASS, custCN);	
						ObjectCache.put(CustomStatics.CUSTOMPROPS, custProps);
					}	
				}	
			}
		}
	}
		catch(Exception E) {
			AceLog.getAppLog().severe("checkCustom threw an error trying to get "+custPropFN,E);
		}
	}

}
