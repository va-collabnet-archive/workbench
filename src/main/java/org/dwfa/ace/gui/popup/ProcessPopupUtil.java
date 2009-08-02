package org.dwfa.ace.gui.popup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.worker.MasterWorker;

public class ProcessPopupUtil {

	public static class ProcessMenuActionListener implements ActionListener {
		private class MenuProcessThread implements Runnable {

			private String action;

			/**
			 * @param action
			 */
			public MenuProcessThread(String action) {
				super();
				this.action = action;
			}

			public void run() {
				try {
					ObjectInputStream ois = new ObjectInputStream(
							new BufferedInputStream(new FileInputStream(
									processFile)));
					I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
							.readObject();
					ois.close();
					if (worker.isExecuting()) {
						worker = worker.getTransactionIndependentClone();
					}
					process.execute(worker);
					worker.commitTransactionIfActive();
				} catch (Exception ex) {

					worker.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
					JOptionPane.showMessageDialog(null,
							"<html>Exception processing action: " + action
									+ "<p><p>" + ex.getMessage()
									+ "<p><p>See log for details.");
				}
			}
		};

		private File processFile;
		private I_Work worker;

		public ProcessMenuActionListener(File processFile, I_Work worker) {
			super();
			this.processFile = processFile;
			this.worker = worker;
		}

		public void actionPerformed(ActionEvent e) {
			new Thread(new MenuProcessThread(e.getActionCommand()),
					"Menu Process Execution").start();
		}
	}

	public static void addProcessMenus(JMenuBar menuBar, String pluginRoot, MasterWorker menuWorker)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		File menuDir = new File(pluginRoot + File.separator + "menu");
		if (menuDir.listFiles() != null) {
			addProcessMenuItems(menuBar, menuDir, menuWorker);
		}
	}

	private static Collection<File> getSortedFiles(File f) {
		TreeSet<File> sortedFiles = new TreeSet<File>();
		for (File child: f.listFiles()) {
			sortedFiles.add(child);
		}
		return sortedFiles;
	}
	
	public static void addProcessMenuItems(JMenuBar menuBar, File menuDir, MasterWorker menuWorker)
			throws IOException, FileNotFoundException, ClassNotFoundException {
		for (File f : getSortedFiles(menuDir)) {
			JMenu newMenu = null;
			if (f.isDirectory()) {
				if (f.getName().equals("File")) {
					for (int i = 0; i < menuBar.getMenuCount(); i++) {
						if (menuBar.getMenu(i).getText().equalsIgnoreCase("File")) {
							newMenu = menuBar.getMenu(i);
							break;
						}
					}
					if (newMenu == null) {
						menuBar.add(newMenu);
					}
					newMenu.addSeparator();
				} else {
					String menuName = f.getName();
					String regex = "_";
					if (menuName.contains(regex)) {
						String[] parts = menuName.split(regex, 2);
						menuName = parts[1];
					}	
					newMenu = new JMenu(menuName);
					menuBar.add(newMenu);
				}
				if (f.listFiles() != null) {
					for (File processFile : getSortedFiles(f)) {
						if (processFile.isDirectory()) {
							JMenu submenu = new JMenu(processFile.getName());
							newMenu.add(submenu);
							addSubmenMenuItems(submenu, processFile, menuWorker);
						} else if (processFile.getName().toLowerCase()
								.endsWith(".bp")) {
							try {
								ActionListener processMenuListener = new ProcessMenuActionListener(
										processFile, menuWorker);
								ObjectInputStream ois = new ObjectInputStream(
										new BufferedInputStream(
												new FileInputStream(processFile)));
								I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
										.readObject();
								ois.close();
								JMenuItem processMenuItem = new JMenuItem(
										process.getName());
								processMenuItem
										.addActionListener(processMenuListener);
								newMenu.add(processMenuItem);
							} catch (IOException e) {
								AceLog.getAppLog().alertAndLog(null,
										Level.SEVERE,
										"processing: " + processFile, e);
							} catch (ClassNotFoundException e) {
								AceLog.getAppLog().alertAndLog(null,
										Level.SEVERE,
										"processing: " + processFile, e);
							}
						} else if (processFile.getName().toLowerCase()
								.endsWith("separator")) {
							newMenu.addSeparator();
						}
					}
				}
			}
		}
	}

	public static void addSubmenMenuItems(JComponent subMenu, File menuDir,
			MasterWorker menuWorker) throws IOException, FileNotFoundException,
			ClassNotFoundException {
		if ((menuDir != null) && menuDir.exists() && (menuDir.listFiles() != null)) {
			for (File f : getSortedFiles(menuDir)) {
				if (f.isDirectory()) {
					JMenu newSubMenu = new JMenu(f.getName());
					subMenu.add(newSubMenu);
					addSubmenMenuItems(newSubMenu, f, menuWorker);
				} else {
					if (f.getName().toLowerCase().endsWith(".bp")) {
						ActionListener processMenuListener = new ProcessMenuActionListener(
								f, menuWorker);
						ObjectInputStream ois = new ObjectInputStream(
								new BufferedInputStream(new FileInputStream(f)));
						I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
								.readObject();
						ois.close();
						JMenuItem processMenuItem = new JMenuItem(process.getName());
						processMenuItem.addActionListener(processMenuListener);
						subMenu.add(processMenuItem);
					}
				}
			}
		}
	}

}