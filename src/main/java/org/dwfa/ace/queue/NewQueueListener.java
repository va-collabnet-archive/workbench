/**
 * 
 */
package org.dwfa.ace.queue;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;

import org.dwfa.ace.ACE;
import org.dwfa.ace.log.AceLog;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.queue.QueueServer;
import org.dwfa.util.io.FileIO;

public class NewQueueListener implements ActionListener {
	
	private class CreateNewQueueActionListener implements ActionListener {
		String queueType;

		private CreateNewQueueActionListener(String queueType) {
			super();
			this.queueType = queueType;
		}

		public void actionPerformed(ActionEvent arg0) {
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
				dialog.setFile(ace.getAceFrameConfig().getUsername() + " " + queueType);
				dialog.setVisible(true);
				if (dialog.getFile() != null) {

					File queueDirectory = new File(dialog.getDirectory(), dialog
							.getFile());
					String username = ace.getAceFrameConfig().getUsername();
					if (queueDirectory.getName().startsWith(username) == false) {
						queueDirectory = new File(queueDirectory.getParent(), username + "." + dialog.getFile());
					}
					
					queueDirectory.mkdirs();
					String fileName = "queue.config";
					if (queueType.equals("Aging")) {
						fileName = "queueAging.config";
					} else if (queueType.equals("Archival")) {
						fileName = "queueArchival.config";
					} else if (queueType.equals("Compute")) {
						fileName = "queueCompute.config";
					} else if (queueType.equals("Inbox")) {
						fileName = "queueInbox.config";
					} else if (queueType.equals("Launcher")) {
						fileName = "queueLauncher.config";
					} else if (queueType.equals("Outbox")) {
						fileName = "queueOutbox.config";
					} 
					
					File queueConfigTemplate = new File("config", fileName);
					String configTemplateString = FileIO.readerToString(new FileReader(queueConfigTemplate));
					
					configTemplateString = configTemplateString.replaceFirst("username",
							queueDirectory.getName());
					configTemplateString = configTemplateString.replaceFirst("username", 
							queueDirectory.getName());
					
					
					File newQueueConfig = new File(queueDirectory, "queue.config");
					FileWriter fw = new FileWriter(newQueueConfig);
					fw.write(configTemplateString);
					fw.close();

					ace.getAceFrameConfig().getDbConfig().getQueues().add(
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
							ace.getAceFrameConfig().getQueueAddressesToShow()
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

				ace.getQueueViewer().refreshQueues();
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
	}

	/**
	 * 
	 */
	private final ACE ace;
	
	private JPopupMenu queueTypePopup;

	/**
	 * @param ace
	 */
	public NewQueueListener(ACE ace) {
		this.ace = ace;
		String[] QueueTypes = new String[] {"Aging", "Archival", "Compute", "Inbox", "Launcher", "Outbox" };
		
		queueTypePopup = new JPopupMenu();
		for (String type: QueueTypes) {
			JMenuItem item = new JMenuItem(type);
			queueTypePopup.add(item);
			item.addActionListener(new CreateNewQueueActionListener(type));
		}
	}

	public void actionPerformed(ActionEvent evt) {

		  JButton newQueueButton = (JButton) evt.getSource();
	      Point location = newQueueButton.getMousePosition();
	      queueTypePopup.show(newQueueButton, location.x, location.y);
	}
}