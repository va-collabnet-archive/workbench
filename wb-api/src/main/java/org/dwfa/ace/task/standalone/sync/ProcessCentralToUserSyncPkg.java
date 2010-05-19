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
package org.dwfa.ace.task.standalone.sync;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;
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
public class ProcessCentralToUserSyncPkg extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private transient TaskFailedException ex;

    private transient File jarFile;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            // nothing to read...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            // Get the file
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    FileDialog dialog = new FileDialog(new Frame(), "Select a sync file to process");
                    dialog.setDirectory(System.getProperty("user.dir"));
                    dialog.setFilenameFilter(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".cus");
                        }
                    });
                    dialog.setVisible(true);
                    if (dialog.getFile() == null) {
                        ex = new TaskFailedException("User canceled operation");
                    } else {
                        jarFile = new File(dialog.getDirectory(), dialog.getFile());
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
            if (Terms.get().getProperty(jarFile.getName()) != null) {
                AceLog.getAppLog().alertAndLogException(
                    new Exception(jarFile.getName() + " has already been processed on: "
                        + Terms.get().getProperty(jarFile.getName())));
                return Condition.CONTINUE;
            }

            // Extract the contents
            File destDir = new File("tmp", UUID.randomUUID().toString());
            JarExtractor.execute(jarFile, destDir);
            boolean authenticated = false;

            // Process Profile
            File profilesDir = new File(destDir, "profiles");
            if (profilesDir.exists()) {
                File[] profiles = profilesDir.listFiles(new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.isDirectory() && (pathname.isHidden() == false);
                    }
                });
                if (profiles != null) {
                    // do a recursive search for .ace
                    for (File profile : profiles) {
                        String prefix = null;
                        String suffix = ".ace";
                        boolean excludeHidden = true;

                        for (File profileFile : FileIO.recursiveGetFiles(profile, prefix, suffix, excludeHidden)) {
                            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                                profileFile)));
                            I_ConfigAceDb remoteDbConfig = (I_ConfigAceDb) ois.readObject();
                            SvnPrompter prompter = new SvnPrompter();
                            prompter.prompt("Please authenticate for: " + jarFile.getName(),
                                remoteDbConfig.getUsername());

                            for (final I_ConfigAceFrame ace : remoteDbConfig.getAceFrames()) {
                                if (ace.isActive()) {
                                    if (ace.getUsername().equals(prompter.getUsername())
                                        && ace.getPassword().equals(prompter.getPassword())) {
                                        authenticated = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (authenticated) {
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
                        for (File inbox : inboxes) {
                            String prefix = null;
                            String suffix = ".bp";
                            boolean excludeHidden = false;
                            // Delete inbox items that are in the .dlog
                            List<File> bpList = FileIO.recursiveGetFiles(inbox, prefix, suffix, excludeHidden);
                            for (File bpFile : bpList) {
                                if (bpFile.getParentFile().getName().equals(".dlog")) {
                                    File dlogFileRelative = new File(bpFile.getAbsolutePath().substring(
                                        absolutePathPortion));
                                    File llogFile = new File(new File(dlogFileRelative.getParentFile().getParentFile(),
                                        ".llog"), dlogFileRelative.getName());
                                    if (llogFile.exists()) {
                                        llogFile.delete();
                                    }
                                } else if (bpFile.getParentFile().getName().startsWith(".") == false) {
                                    File bpFileRelative = new File(bpFile.getAbsolutePath().substring(
                                        absolutePathPortion));
                                    File llogFile = new File(new File(bpFileRelative.getParentFile(), ".llog"),
                                        bpFileRelative.getName());
                                    if ((bpFileRelative.exists() == false) && (llogFile.exists() == false)) {
                                        FileIO.copyFile(bpFile, bpFileRelative);
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
                        // do a recursive search for .bp
                        for (File outbox : outboxes) {
                            String prefix = null;
                            String suffix = ".bp";

                            // Delete each entry from the central .clog that is
                            // in
                            // the jar, and create a .dlog folder
                            boolean excludeHidden = false;

                            // do a recursive search for .dlog
                            List<File> cFileList = FileIO.recursiveGetFiles(outbox, prefix, suffix, excludeHidden);
                            for (File cFile : cFileList) {
                                if (cFile.getParentFile().getName().equals(".clog")) {
                                    File cFileRelative = new File(cFile.getAbsolutePath()
                                        .substring(absolutePathPortion));
                                    File dlogFileRelative = new File(new File(cFileRelative.getParent(), ".dlog"),
                                        cFileRelative.getName());
                                    File llogFileRelative = new File(new File(cFileRelative.getParent(), ".llog"),
                                        cFileRelative.getName());
                                    File bpFileRelative = new File(cFileRelative.getParent(), cFileRelative.getName());
                                    if (bpFileRelative.exists() == false && dlogFileRelative.exists() == false) {
                                        dlogFileRelative.createNewFile();
                                        if (llogFileRelative.exists()) {
                                            llogFileRelative.delete();
                                        }
                                    }
                                    if (bpFileRelative.exists()) {
                                        bpFileRelative.delete();
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
                        // do a recursive search for change sets
                        for (File changeSet : changeSets) {
                            String prefix = null;
                            String[] suffixes = { ".eccs", ".cmrscs" };
                            boolean excludeHidden = true;
                            List<File> changeSetFiles = new ArrayList<File>();
                            for (String suffix : suffixes) {
                                changeSetFiles.addAll(FileIO.recursiveGetFiles(changeSet, prefix, suffix, excludeHidden));
                            }

                            for (File csFile : changeSetFiles) {

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
                QueueServer.refreshServers();

                Terms.get().setProperty(jarFile.getName(), new Date().toString());
            } else {
                AceLog.getAppLog().alertAndLogException(new Exception("Authentication failed"));
            }
            // Cleanup
            FileIO.recursiveDelete(destDir);

        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (ClassNotFoundException e) {
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
