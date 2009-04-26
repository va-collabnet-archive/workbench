package org.dwfa.ace.task.standalone.sync;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.SwingUtilities;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.queue.QueueServer;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;
import org.dwfa.util.io.JarExtractor;

@BeanList(specs = { @Spec(directory = "tasks/ide/ssync/", type = BeanType.TASK_BEAN) })
public class ProcessUserToCentralSyncPackage extends AbstractTask {

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	private transient TaskFailedException ex;

	private transient File jarFile;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			// nothing to read...
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do...
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			// Get the file
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					FileDialog dialog = new FileDialog(new Frame(),
							"Select a sync file to process");
					dialog.setDirectory(System.getProperty("user.dir"));
					dialog.setFilenameFilter(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".ucs");
						}
					});
					dialog.setVisible(true);
					if (dialog.getFile() == null) {
						ex = new TaskFailedException("User canceled operation");
					} else {
						jarFile = new File(dialog.getDirectory(), dialog
								.getFile());
					}
				}
			});
		} catch (InterruptedException e) {
			throw new TaskFailedException(e);
		} catch (InvocationTargetException e) {
			throw new TaskFailedException(e);
		}
		if (ex != null) {
			throw ex;
		}
		try {
			// Extract the contents

			File destDir = new File("tmp", UUID.randomUUID().toString());
			JarExtractor.execute(jarFile, destDir);
			// Process Inboxes
			File inboxesDir = new File(destDir, "inboxes");
			if (inboxesDir.exists()) {
				int absolutePathPortion = getAbsolutePathPortion(inboxesDir);
				File[] inboxes = inboxesDir.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isDirectory() && (pathname.isHidden() == false);
					}
				});
				if (inboxes != null) {
					for (File inbox: inboxes) {
						String prefix = null; 
						String suffix = ".bp"; 
						boolean excludeHidden = true;
						//Delete inbox items that are in the .dlog
						List<File> dFileList = FileIO.recursiveGetFiles(inbox, prefix, suffix, excludeHidden);
						for (File dFile: dFileList) {
							if (dFile.getParentFile().getName().equals(".dlog")) {
								File dlogFileRelative = new File(dFile.getAbsolutePath().substring(absolutePathPortion));
								File bpFileRelative = new File(dlogFileRelative.getParentFile().getParentFile(), 
										dlogFileRelative.getName());
								if (bpFileRelative.exists()) {
									bpFileRelative.delete();
								}
							}
						}
					}
				}
			}

			// Process Outboxes
			File outboxesDir = new File(destDir, "outboxes");
			if (outboxesDir.exists()) {
				int absolutePathPortion = getAbsolutePathPortion(outboxesDir);
				File[] outboxes = outboxesDir.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isDirectory() && (pathname.isHidden() == false);
					}
				});
				if (outboxes != null) {
					//do a recursive search for .bp
					for (File outbox: outboxes) {
						String prefix = null; 
						String suffix = ".bp"; 
						boolean excludeHidden = true;

						for (File bpFile: FileIO.recursiveGetFiles(outbox, prefix, suffix, excludeHidden)) {
							// See if each process is in the central .llog 

							File bpFileRelative = new File(bpFile.getAbsolutePath().substring(absolutePathPortion));
							File bpFileLlogRelative = new File(new File(bpFileRelative.getParent(), ".llog"), bpFileRelative.getName());
							if (bpFileLlogRelative.exists() == false) {
								// If not in the central .llog, copy the file in, and add to .llog
								FileIO.copyFile(bpFile, bpFileRelative);
								bpFileLlogRelative.getParentFile().mkdirs();
								bpFileLlogRelative.createNewFile();
							}
						}
						
						
						// Delete each entry from the central .llog that is in the jar .dlog folder
						excludeHidden = false;
						
						//do a recursive search for .dlog
						List<File> dFileList = FileIO.recursiveGetFiles(outbox, prefix, suffix, excludeHidden);
						for (File dFile: dFileList) {
							if (dFile.getParentFile().getName().equals(".dlog")) {
								File bpFileRelative = new File(dFile.getAbsolutePath().substring(absolutePathPortion));
								File llogFileRelative = new File(new File(bpFileRelative.getParent(), ".llog"), bpFileRelative.getName());
								if (llogFileRelative.exists()) {
									llogFileRelative.delete();
								}
							}
						}
					}
				}
			}

			// Process Change sets
			File changeSetsDir = new File(destDir, "changeSets");
			if (changeSetsDir.exists()) {
				int absolutePathPortion = getAbsolutePathPortion(changeSetsDir);
				File[] changeSets = changeSetsDir.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isDirectory() && (pathname.isHidden() == false);
					}
				});
				if (changeSets != null) {
					//do a recursive search for change sets
					for (File changeSet: changeSets) {
						String prefix = null; 
						String[] suffixes = { ".jcs", ".cmrscs" }; 
						boolean excludeHidden = true;
						List<File> changeSetFiles = new ArrayList<File>();
						for (String suffix: suffixes) {
							changeSetFiles.addAll(FileIO.recursiveGetFiles(changeSet, prefix, suffix, excludeHidden));
						}
						
						for (File csFile: changeSetFiles) {

							File csFileRelative = new File(csFile.getAbsolutePath().substring(absolutePathPortion));
							if (csFileRelative.exists() == false) {
								FileIO.copyFile(csFile, csFileRelative);
							} else {
								if (csFile.length() > csFileRelative.length()) {
									FileIO.copyFile(csFile, csFileRelative);
								}
							}
						}
					}
				}
				
			}

			// Process Profile
			File profilesDir = new File(destDir, "profiles");
			if (profilesDir.exists()) {
				int absolutePathPortion = getAbsolutePathPortion(profilesDir);
				File[] profiles = profilesDir.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isDirectory() && (pathname.isHidden() == false);
					}
				});
				if (profiles != null) {
					//do a recursive search for .ace
					for (File profile: profiles) {
						String prefix = null; 
						String suffix = ".ace"; 
						boolean excludeHidden = true;

						for (File profileFile: FileIO.recursiveGetFiles(profile, prefix, suffix, excludeHidden)) {

							File profileFileRelative = new File(profileFile.getAbsolutePath().substring(absolutePathPortion));
							FileIO.copyFile(profileFile, profileFileRelative);
						}
						
						//do a recursive search for .csp
						suffix = ".csp"; 
						for (File profileFile: FileIO.recursiveGetFiles(profile, prefix, suffix, excludeHidden)) {
							File profileFileRelative = new File(profileFile.getAbsolutePath().substring(absolutePathPortion));
							FileIO.copyFile(profileFile, profileFileRelative);
						}

					
					}
					
				}
			}

			QueueServer.refreshServers();
			
			
			// Cleanup
			FileIO.recursiveDelete(destDir);

			
		} catch (IOException e) {
			throw new TaskFailedException(e);
		}
		
		return Condition.CONTINUE;
	}

	private int getAbsolutePathPortion(File inbox) {
		int absolutePathPortion = inbox.getAbsolutePath().length();
		if (inbox.getAbsolutePath().endsWith(File.separator) == false) {
			absolutePathPortion++;
		}
		return absolutePathPortion;
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}
}
