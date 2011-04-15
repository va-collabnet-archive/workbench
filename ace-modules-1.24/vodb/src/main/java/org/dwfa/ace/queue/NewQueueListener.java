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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;

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

                    File queueDirectory = new File(dialog.getDirectory(), dialog.getFile());
                    String username = ace.getAceFrameConfig().getUsername();
                    if (queueDirectory.getName().startsWith(username) == false) {
                        queueDirectory = new File(queueDirectory.getParent(), username + "." + dialog.getFile());
                    }

                    queueDirectory.mkdirs();

                    String nodeInboxAddress = queueDirectory.getName().toLowerCase().replace(' ', '.');
                    nodeInboxAddress = nodeInboxAddress.replace("....", ".");
                    nodeInboxAddress = nodeInboxAddress.replace("...", ".");
                    nodeInboxAddress = nodeInboxAddress.replace("..", ".");

                    Map<String, String> substutionMap = new TreeMap<String, String>();
                    substutionMap.put("**queueName**", queueDirectory.getName());
                    substutionMap.put("**directory**", FileIO.getRelativePath(queueDirectory).replace('\\', '/'));
                    substutionMap.put("**nodeInboxAddress**", nodeInboxAddress);

                    String fileName = "template.queue.config";
                    if (queueType.equals("aging")) {
                        fileName = "template.queueAging.config";
                    } else if (queueType.equals("archival")) {
                        fileName = "template.queueArchival.config";
                    } else if (queueType.equals("compute")) {
                        fileName = "template.queueCompute.config";
                    } else if (queueType.equals("inbox")) {
                        substutionMap.put("**mailPop3Host**", "**mailPop3Host**");
                        substutionMap.put("**mailUsername**", "**mailUsername**");
                        fileName = "template.queueInbox.config";
                    } else if (queueType.equals("launcher")) {
                        fileName = "template.queueLauncher.config";
                    } else if (queueType.equals("outbox")) {
                        substutionMap.put("//**allGroups**mailHost", "//**allGroups**mailHost");
                        substutionMap.put("//**outbox**mailHost", "//**outbox**mailHost");
                        substutionMap.put("**mailHost**", "**mailHost**");
                        fileName = "template.queueOutbox.config";
                    }

                    File queueConfigTemplate = new File("config", fileName);
                    String configTemplateString = FileIO.readerToString(new FileReader(queueConfigTemplate));

                    for (String key : substutionMap.keySet()) {
                        configTemplateString = configTemplateString.replace(key, substutionMap.get(key));
                    }

                    File newQueueConfig = new File(queueDirectory, "queue.config");
                    FileWriter fw = new FileWriter(newQueueConfig);
                    fw.write(configTemplateString);
                    fw.close();

                    ace.getAceFrameConfig().getDbConfig().getQueues().add(FileIO.getRelativePath(newQueueConfig));
                    Configuration queueConfig = ConfigurationProvider.getInstance(new String[] { newQueueConfig.getAbsolutePath() });
                    Entry[] entries = (Entry[]) queueConfig.getEntry("org.dwfa.queue.QueueServer", "entries",
                        Entry[].class, new Entry[] {});
                    for (Entry entry : entries) {
                        if (ElectronicAddress.class.isAssignableFrom(entry.getClass())) {
                            ElectronicAddress ea = (ElectronicAddress) entry;
                            ace.getAceFrameConfig().getQueueAddressesToShow().add(ea.address);
                            break;
                        }
                    }
                    if (QueueServer.started(newQueueConfig)) {
                        AceLog.getAppLog().info(
                            "Queue already started: " + newQueueConfig.toURI().toURL().toExternalForm());
                    } else {
                        new QueueServer(new String[] { newQueueConfig.getCanonicalPath() }, null);
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
        String[] QueueTypes = new String[] { "aging", "archival", "compute", "inbox", "launcher", "outbox" };

        queueTypePopup = new JPopupMenu();
        for (String type : QueueTypes) {
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
