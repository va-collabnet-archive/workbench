package org.dwfa.ace.cs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.jar.JarInputStream;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import net.jini.config.Configuration;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.fd.FileDialogUtil;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;

public class ImportChangeSetReader implements ActionListener, I_Count {



	JarInputStream input;

	ThinDescVersionedBinding descBinding = new ThinDescVersionedBinding();

	boolean continueWork = true;

	String upperProgressMessage = "Reading Java Change Set";

	String lowerProgressMessage = "counting";

	int max = -1;

	int concepts = -1;

	int descriptions = -1;

	int relationships = -1;

	int ids = -1;

	int images = -1;

	int total = -1;

	int processed = 0;

	private CountDownLatch latch;

	private AceConfig config;

	private class ProgressUpdator implements I_UpdateProgress {
		Timer updateTimer;

		boolean firstUpdate = true;

		ActivityPanel activity = new ActivityPanel(true, true);

		public ProgressUpdator() {
			super();
			updateTimer = new Timer(300, this);
			updateTimer.start();
		}

		public void actionPerformed(ActionEvent e) {
			if (firstUpdate) {
				firstUpdate = false;
				try {
					ActivityViewer.addActivity(activity);
				} catch (Exception e1) {
					AceLog.getAppLog().alertAndLogException(e1);
				}
			}
			activity.setIndeterminate(total == -1);
			activity.setValue(processed);
			activity.setMaximum(total);
			activity.setProgressInfoUpper(upperProgressMessage);
			if (latch != null) {
				activity.setProgressInfoLower(lowerProgressMessage + processed +
						" latch: " + latch.getCount());				
			} else {
				activity.setProgressInfoLower(lowerProgressMessage + processed);
			}
			if (!continueWork) {
				activity.complete();
				updateTimer.stop();
			}
		}

		public void normalCompletion() {
			activity.complete();
			updateTimer.stop();
		}

	}

	public ImportChangeSetReader(final Configuration riverConfig) {
		try {
			final File csFile = FileDialogUtil
					.getExistingFile("Select Java Change Set to Import...", new FilenameFilter() {

						public boolean accept(File dir, String name) {
							return name.toLowerCase().endsWith(".jcs");
						}});
			ProgressUpdator updater = new ProgressUpdator();
			updater.activity.addActionListener(this);
			ACE.threadPool.execute(new Runnable() {
				public void run() {
					try {
						importChangeSet(csFile, riverConfig);
						ACE.commit();
					} catch (TaskFailedException ex) {
						AceLog.getAppLog().alertAndLogException(ex);
					} catch (IOException ex) {
						AceLog.getAppLog().alertAndLogException(ex);
					}
				}

			});
		} catch (TaskFailedException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
	}

	@SuppressWarnings("unchecked")
    protected void importChangeSet(File csFile, final Configuration riverConfig) throws TaskFailedException {
		try {

			lowerProgressMessage = "Processing change set";
			AceLog.getEditLog().info("Importing change set: " + csFile.getAbsolutePath());
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(csFile));
			Class<I_ReadChangeSet> readerClass = (Class<I_ReadChangeSet>) ois.readObject();
			ois.close();
			I_ReadChangeSet reader = (I_ReadChangeSet) readerClass.newInstance();
			processed = 0;
			reader.setCounter(this);
			reader.setChangeSetFile(csFile);
			reader.read();
			
			lowerProgressMessage = "Starting sync ";
			AceConfig.getVodb().sync();
			upperProgressMessage = "Import complete";
			lowerProgressMessage = "Finished sync. Components imported: ";

			continueWork = false;
			if (config != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						for (I_ConfigAceFrame ace: config.aceFrames) {
							if (ace.isActive()) {
								ACE cdePanel;
								try {
									cdePanel = new ACE(riverConfig);
									cdePanel.setup(ace);
									JFrame cdeFrame = new JFrame(ace.getFrameName());
									cdeFrame.setContentPane(cdePanel);
									cdeFrame.setJMenuBar(cdePanel.createMenuBar());

									cdeFrame.setBounds(ace.getBounds());
									cdeFrame.setVisible(true);
								} catch (Exception e) {
									AceLog.getEditLog().alertAndLog(Level.SEVERE, e.getLocalizedMessage(), e);
								}
							}
						}
					}
					
				});
			}
		} catch (Exception e) {
			continueWork = false;
			throw new TaskFailedException(e);
		}
		
	}


	public void actionPerformed(ActionEvent e) {
		continueWork = false;
		lowerProgressMessage = "User stopped action";
	}

	public void increment() {
		processed++;
	}

}
