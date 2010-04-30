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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.cs.ChangeSetImporter;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;
import org.dwfa.util.io.JarCreator;
import org.dwfa.util.io.JarExtractor;

@BeanList(specs = { @Spec(directory = "tasks/ide/ssync/", type = BeanType.TASK_BEAN) })
public class CentralToUserSyncPkg extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private transient TaskFailedException ex = null;

    private transient File remoteProfile = null;

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

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            // Select the remote profile
            getRemoteDbProfile();

            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(remoteProfile)));
            I_ConfigAceDb remoteDbConfig = (I_ConfigAceDb) ois.readObject();

            int sequence = 1;
            String dir = "ssync";
            String prefix = "central-" + remoteDbConfig.getUsername() + "-";
            String sequenceString = Terms.get().getProperty(prefix);
            if (sequenceString != null) {
                sequence = Integer.parseInt(sequenceString) + 1;
            }
            String suffix = ".cus";
            File jarFile = new File(dir, prefix + sequence + suffix);
            Manifest manifest = new Manifest();
            Attributes a = manifest.getMainAttributes();
            a.putValue(Name.MANIFEST_VERSION.toString(), "1.0");
            a.putValue(Name.MAIN_CLASS.toString(), JarExtractor.class.getCanonicalName());
            Map<String, Attributes> entries = manifest.getEntries();

            jarFile.getParentFile().mkdirs();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(jarFile));
            JarOutputStream output = new JarOutputStream(bos, manifest);
            JarCreator.addToZip(Class.forName(JarExtractor.class.getCanonicalName()), output);

            // Inbox and outbox
            for (String queueFile : remoteDbConfig.getQueues()) {
                String queueConfigString = FileIO.readerToString(new FileReader(queueFile));
                if (queueConfigString.contains("getInboxQueueType")) {
                    putInboxInJar(queueFile, output);
                } else if (queueConfigString.contains("getOutboxQueueType")) {
                    putOutboxInJar(queueFile, output);
                }
            }

            // change sets
            Properties csProperties = new Properties();
            File csPropsFile = new File(remoteDbConfig.getProfileFile().getParentFile(), ".csp");
            if (csPropsFile.exists()) {
                InputStream fis = new FileInputStream(csPropsFile);
                csProperties.loadFromXML(fis);
                fis.close();
            }

            List<File> changeSetFiles = new ArrayList<File>();
            ChangeSetImporter.addAllChangeSetFiles(new File("profiles"), changeSetFiles, ".jcs");
            for (File csf : changeSetFiles) {
                String lastImportSize = csProperties.getProperty(FileIO.getNormalizedRelativePath(csf));
                if (lastImportSize != null) {
                    if (csf.length() != Long.parseLong(lastImportSize)) {
                        // change set not previously sent, need to send it.
                        addChangeSet(csProperties, output, csf);
                    } else {
                        // change set previously sent, no need to send again
                    }
                } else {
                    addChangeSet(csProperties, output, csf);
                }
            }
            OutputStream fos = new FileOutputStream(csPropsFile);
            csProperties.storeToXML(fos, "");
            fos.close();

            JarCreator.addToZip("profiles/", remoteDbConfig.getProfileFile(), output, "");
            JarCreator.addToZip("profiles/", new File(remoteDbConfig.getProfileFile().getParentFile(), ".csp"), output,
                "");

            output.close();
            Terms.get().setProperty(prefix, Integer.toString(sequence + 1));

            return Condition.CONTINUE;
        } catch (Exception ex) {
            throw new TaskFailedException(ex);
        }
    }

    private void getRemoteDbProfile() throws Exception {
        // Get the file
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                FileDialog dialog = new FileDialog(new Frame(), "Select a profile to prepare sync pkg for:");
                dialog.setDirectory(System.getProperty("user.dir"));
                dialog.setFilenameFilter(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".ace");
                    }
                });
                dialog.setVisible(true);
                if (dialog.getFile() == null) {
                    ex = new TaskFailedException("User canceled operation");
                } else {
                    remoteProfile = new File(dialog.getDirectory(), dialog.getFile());
                }
            }
        });
        if (ex != null) {
            throw ex;
        }
    }

    private void addChangeSet(Properties props, JarOutputStream output, File csf) throws IOException {
        props.setProperty(FileIO.getNormalizedRelativePath(csf), Long.toString(csf.length()));
        JarCreator.addToZip("changeSets/", csf, output, "");
    }

    private void putInboxInJar(String queueFileStr, JarOutputStream output) throws IOException {
        AceLog.getAppLog().info("Processing inbox: " + queueFileStr);
        File queueDir = new File(queueFileStr).getParentFile();
        String prefix = "inboxes/";
        JarCreator.recursiveAddToZip(output, queueDir, prefix);
    }

    private void putOutboxInJar(String queueFileStr, JarOutputStream output) throws IOException {
        AceLog.getAppLog().info("Processing outbox: " + queueFileStr);
        File queueDir = new File(queueFileStr).getParentFile();
        File queueLlog = new File(queueDir, ".llog");
        File[] logItems = queueLlog.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.getName().endsWith(".bp");
            }
        });
        if (logItems != null) {
            String prefix = "outboxes/";
            for (File llogEntry : logItems) {
                File clogEntry = new File(FileIO.getRelativePath(queueDir) + "/.clog/" + llogEntry.getName());
                JarCreator.addToZip(prefix, clogEntry, output, "");
            }
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

}
