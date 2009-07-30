package org.dwfa.ace.config;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;

import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.ace.tree.ExpandNodeSwingWorker;
import org.dwfa.ace.url.tiuid.ExtendedUrlStreamHandlerFactory;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.queue.QueueServer;
import org.dwfa.swing.SwingWorker;
import org.dwfa.util.io.FileIO;
import org.dwfa.vodb.VodbEnv;
import org.tigris.subversion.javahl.ClientException;

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
	private static boolean firstStartup = true;
	/**
	 * The jini configuration provider
	 */
	public Configuration jiniConfig;
	public File aceConfigFile;
	public Properties aceProperties;

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
			if (new File(args[0]).exists()) {
				jiniConfig = ConfigurationProvider.getInstance(args, getClass()
						.getClassLoader());
			}

			setupLookAndFeel();
			setupSwingExpansionTimerLogging();
			setupIpChangeListener();
			setBerkeleyDbAsTransactional();

			File acePropertiesFile = new File("config", "ace.properties");
			boolean acePropertiesFileExists = acePropertiesFile.exists();
			aceProperties = new Properties();

			if (acePropertiesFileExists) {
				aceProperties
						.loadFromXML(new FileInputStream(acePropertiesFile));
			}

			aceProperties.put("initialized", "true");
			if (jiniConfig != null) {
				aceConfigFile = (File) jiniConfig.getEntry(this.getClass()
						.getName(), "aceConfigFile", File.class, new File(
						"config/config.ace"));
			} else {
				aceConfigFile = new File("config/config.ace");
			}
			aceProperties.storeToXML(
					new FileOutputStream(acePropertiesFile), null);

			SvnPrompter prompter = new SvnPrompter();
			if (aceConfigFile != null && aceConfigFile.exists() && acePropertiesFile.exists()) {
				
				// Put up a dialog to select the configuration file...
				CountDownLatch latch = new CountDownLatch(1);
				GetProfileWorker profiler = new GetProfileWorker(aceConfigFile, aceProperties, jiniConfig, latch);
				profiler.start();
				latch.await();
				
				aceConfigFile = profiler.aceConfigFile;
				
				ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(
								aceConfigFile)));
				AceConfig.config = (AceConfig) ois.readObject();
				AceConfig.config.setProfileFile(aceConfigFile);
				setupDatabase(AceConfig.config, aceConfigFile);

				AceConfig.config.getUsername();
				prompter.setUsername(AceConfig.config.getUsername());
				prompter.setPassword(profiler.getPassword());
			} else {
				File dbFolder = new File("berkeley-db");
				if (jiniConfig != null) {
					dbFolder = (File) jiniConfig.getEntry(this.getClass()
							.getName(), "dbFolder", File.class, new File(
							"target/berkeley-db"));
				}
				AceConfig.config = new AceConfig(dbFolder);
				AceConfig.config.setProfileFile(aceConfigFile);
				AceConfig.setupAceConfig(AceConfig.config, null, null, false);
			}
			aceProperties.storeToXML(new FileOutputStream(acePropertiesFile),
					null);
			ACE.setAceConfig(AceConfig.config);
			AceConfig.config.addChangeSetWriters();
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
			for (String queue : AceConfig.config.getQueues()) {
				File queueFile = new File(queue);
				AceLog.getAppLog().info(
						"Found queue: "
								+ queueFile.toURI().toURL().toExternalForm());
				if (QueueServer.started(queueFile)) {
					AceLog.getAppLog().info(
							"Queue already started: "
									+ queueFile.toURI().toURL()
											.toExternalForm());
				} else {
					new QueueServer(
							new String[] { queueFile.getCanonicalPath() }, lc);
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
			System.exit(0);
		}
	}

	private static class GetProfileWorker extends SwingWorker<Boolean> {
		StartupFrameListener fl = new StartupFrameListener();
		JFrame parentFrame = new JFrame();
		boolean newFrame = true;
		private File aceConfigFile;
		private Properties aceProperties;
		private Configuration jiniConfig;
		private File lastProfileDir;
		private String password;
		private AceLoginDialog aceLoginDialog;
		CountDownLatch latch;

		public GetProfileWorker(File aceConfigFile, Properties aceProperties, Configuration jiniConfig, CountDownLatch latch) {
			super();
			parentFrame = new JFrame();
			boolean newFrame = true;
			if (OpenFrames.getNumOfFrames() > 0) {
				newFrame = false;
				parentFrame = OpenFrames.getFrames().iterator().next();
				AceLog.getAppLog().info("### Adding an existing frame");
			} else {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						public void run() {
							parentFrame.setContentPane(new JLabel("The Terminology IDE is starting..."));
							parentFrame.pack();
							Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
							parentFrame.setLocation(d.width/2, d.height/2);
							parentFrame.setVisible(true);
							OpenFrames.addFrame(parentFrame);
							AceLog.getAppLog().info("### Using a new frame");
						}
						
					});
				} catch (InterruptedException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (InvocationTargetException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
			aceLoginDialog = new AceLoginDialog(aceProperties, jiniConfig, parentFrame);

			this.aceConfigFile = aceConfigFile;
			this.jiniConfig = jiniConfig;
			this.aceProperties = aceProperties;
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
			if (aceProperties.getProperty("last-profile-dir") != null) {
				lastProfileDir = new File(aceProperties
						.getProperty("last-profile-dir"));
			}
			OpenFrames.addFrameListener(fl);
			if (OpenFrames.getNumOfFrames() > 0) {
				newFrame = false;
				parentFrame = OpenFrames.getFrames().iterator().next();
				AceLog.getAppLog().info("### Adding an existing frame");
			} else {
				SwingUtilities.invokeAndWait(new Runnable() {

					public void run() {
						parentFrame.setContentPane(new JLabel("The Terminology IDE is starting..."));
						parentFrame.pack();
						Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
						parentFrame.setLocation(d.width/2, d.height/2);
						parentFrame.setVisible(true);
						OpenFrames.addFrame(parentFrame);
						AceLog.getAppLog().info("### Using a new frame");
					}
					
				});
			}
			return true;
		}

		@Override
		protected void finished() {
			super.finished();
			try {
				//shows the AceLoginDialog
				aceConfigFile = aceLoginDialog.getUserProfile(lastProfileDir);
				password = new String(aceLoginDialog.getPassword());

				aceProperties.setProperty("last-profile-dir", FileIO
						.getRelativePath(aceConfigFile));
				
				new AceSvn(AceRunner.class, jiniConfig).initialSubversionOperationsAndChangeSetImport(
						new File("config", "ace.properties"), 
						aceLoginDialog.connectToSvn());
				
				if (newFrame) {
					OpenFrames.removeFrame(parentFrame);
					parentFrame.setVisible(false);
				}
				latch.countDown();
				OpenFrames.removeFrameListener(fl);
			} catch (TaskFailedException e) {
				AceLog.getAppLog().alertAndLogException(e);
				System.exit(0);
			} catch (ClientException e) {
				AceLog.getAppLog().alertAndLogException(e);
				System.exit(0);
			} catch (FileNotFoundException e) {
				AceLog.getAppLog().alertAndLogException(e);
				System.exit(0);
			} catch (ConfigurationException e) {
				AceLog.getAppLog().alertAndLogException(e);
				System.exit(0);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
				System.exit(0);
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
					AceFrame af = new AceFrame(AceRunner.args, AceRunner.lc,
							ace, startup);
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
							AceFrame newFrame = new AceFrame(AceRunner.args,
									AceRunner.lc, ace, startup);
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
					.getClass().getName(), "lookAndFeelClassName", String.class,
					UIManager.getSystemLookAndFeelClassName());

			UIManager.setLookAndFeel(lookAndFeelClassName);
		} else {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
	}

	private void setBerkeleyDbAsTransactional() {
		VodbEnv.setTransactional(true);
		VodbEnv.setTxnNoSync(false);
		VodbEnv.setDeferredWrite(false);
	}

	private void setupIpChangeListener() throws ConfigurationException,
			UnknownHostException {
		if (jiniConfig != null) {
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
	}

	private void setupSwingExpansionTimerLogging()
			throws ConfigurationException {
		if (jiniConfig != null) {
			Boolean logTimingInfo = (Boolean) jiniConfig.getEntry(this.getClass()
					.getName(), "logTimingInfo", Boolean.class, null);
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

	private void setupCustomProtocolHandler() {
		AceLog.getAppLog().info(
				"java.protocol.handler.pkgs: "
						+ System.getProperty("java.protocol.handler.pkgs"));
		URL.setURLStreamHandlerFactory(new ExtendedUrlStreamHandlerFactory());
	}

	private void setupDatabase(AceConfig aceConfig, File configFileFile)
			throws IOException {
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

		File jeUserPropertiesFile = new File(configFileFile.getParentFile(),
				"je.properties");
		if (jeUserPropertiesFile.exists()) {
			File jeDbPropertiesFile = new File(aceConfig.getDbFolder(),
					"je.properties");
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