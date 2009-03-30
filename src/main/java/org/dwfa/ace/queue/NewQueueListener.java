/**
 * 
 */
package org.dwfa.ace.queue;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;

import org.dwfa.ace.ACE;
import org.dwfa.ace.log.AceLog;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.queue.QueueServer;
import org.dwfa.util.io.FileIO;

public class NewQueueListener implements ActionListener {

	/**
	 * 
	 */
	private final ACE ace;

	/**
	 * @param ace
	 */
	public NewQueueListener(ACE ace) {
		this.ace = ace;
	}

	public void actionPerformed(ActionEvent evt) {

		try {

			File queueDir = new File("queues", "dynamic");
			if (queueDir.exists() == false) {
				queueDir.mkdirs();
				File staticQueueDir = new File("queues", "static");
				staticQueueDir.mkdirs();
			}

			FileDialog dialog = new FileDialog(new Frame(), "Specify new Queue");
			dialog.setMode(FileDialog.SAVE);
			dialog.setDirectory(queueDir.getAbsolutePath());
			dialog.setVisible(true);
			if (dialog.getFile() != null) {

				File queueDirectory = new File(dialog.getDirectory(), dialog
						.getFile());
				queueDirectory.mkdirs();
				File queueConfigTemplate = new File("config", "queue.config");
				File newQueueConfig = new File(queueDirectory, "queue.config");
				FileIO.copyFile(queueConfigTemplate, newQueueConfig);

				this.ace.getAceFrameConfig().getDbConfig().getQueues().add(
						FileIO.getRelativePath(newQueueConfig));
				Configuration queueConfig = ConfigurationProvider
						.getInstance(new String[] { newQueueConfig.getAbsolutePath() });
				Entry[] entries = (Entry[]) queueConfig.getEntry(
						"org.dwfa.queue.QueueServer", "entries", Entry[].class,
						new Entry[] {});
				for (Entry entry : entries) {
					if (ElectronicAddress.class.isAssignableFrom(entry
							.getClass())) {
						ElectronicAddress ea = (ElectronicAddress) entry;
						this.ace.getAceFrameConfig().getQueueAddressesToShow()
								.add(ea.address);
						break;
					}
				}
				if (QueueServer.started(newQueueConfig)) {
					AceLog.getAppLog().info(
							"Queue already started: "
									+ newQueueConfig.toURI().toURL()
											.toExternalForm());
				} else {
					new QueueServer(
							new String[] { newQueueConfig.getCanonicalPath() }, null);
				}

			}

			this.ace.getQueueViewer().refreshQueues();
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}
}