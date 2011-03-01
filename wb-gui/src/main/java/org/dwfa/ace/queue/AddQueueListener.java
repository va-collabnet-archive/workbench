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
/**
 * 
 */
package org.dwfa.ace.queue;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;
import net.jini.core.entry.Entry;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.jini.ElectronicAddress;
import org.dwfa.queue.QueueServer;
import org.dwfa.util.io.FileIO;

public class AddQueueListener implements ActionListener {

    /**
     * 
     */
    private final ACE ace;

    /**
     * @param ace
     */
    public AddQueueListener(ACE ace) {
        this.ace = ace;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        try {
            FileDialog dialog = new FileDialog(new Frame(), "Select a queue");
            dialog.setDirectory(System.getProperty("user.dir"));
            dialog.setFilenameFilter(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("queue.config");
                }
            });
            dialog.setVisible(true);
            if (dialog.getFile() != null) {
                File queueFile = new File(dialog.getDirectory(), dialog.getFile());
                String workingCopy = FileIO.getRelativePath(queueFile.getParentFile().getAbsoluteFile());

                File queueSvnDir = new File(dialog.getDirectory(), ".svn");

                this.ace.getAceFrameConfig().getDbConfig().getQueues().add(FileIO.getRelativePath(queueFile));

                Configuration queueConfig = ConfigurationProvider.getInstance(new String[] { queueFile.getAbsolutePath() });
                Entry[] entries = (Entry[]) queueConfig.getEntry("org.dwfa.queue.QueueServer", "entries",
                    Entry[].class, new Entry[] {});
                for (Entry entry : entries) {
                    if (ElectronicAddress.class.isAssignableFrom(entry.getClass())) {
                        ElectronicAddress ea = (ElectronicAddress) entry;
                        this.ace.getAceFrameConfig().getQueueAddressesToShow().add(ea.address);
                        if (queueSvnDir.exists()) {
                            SubversionData svd = new SubversionData(null, workingCopy);
                            this.ace.getAceFrameConfig().svnCompleteRepoInfo(svd);
                            this.ace.getAceFrameConfig().getSubversionMap().put(ea.address, svd);
                        }
                        break;
                    }
                }
                if (QueueServer.started(queueFile)) {
                    AceLog.getAppLog().info("Queue already started: " + queueFile.toURI().toURL().toExternalForm());
                } else {
                    new QueueServer(new String[] { queueFile.getCanonicalPath() }, null);
                }
            }

            this.ace.getQueueViewer().refreshQueues();
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }
}
