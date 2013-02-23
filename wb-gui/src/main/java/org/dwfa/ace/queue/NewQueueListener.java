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
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.dwfa.ace.ACE;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.no_jini.ElectronicAddress;
import org.dwfa.ace.no_jini.Entry;
import org.dwfa.queue.QueueServer;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.ttk.preferences.TtkPreferences;
import org.ihtsdo.ttk.queue.QueueAddress;
import org.ihtsdo.ttk.queue.QueueList;
import org.ihtsdo.ttk.queue.QueuePreferences;
import org.ihtsdo.ttk.queue.QueueType;

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
                    String workingCopy = FileIO.getRelativePath(queueDirectory.getAbsoluteFile());

                    QueueType queuePrefType = new QueueType(QueueType.Types.INBOX);
                    boolean readInsteadOfTake = false;
                    if (queueType.equals("aging")) {
                        throw new UnsupportedOperationException();
                    } else if (queueType.equals("archival")) {
                        readInsteadOfTake = true;
                    } else if (queueType.equals("compute")) {
                        throw new UnsupportedOperationException();
                    } else if (queueType.equals("inbox")) {
                       
                    } else if (queueType.equals("launcher")) {
                        readInsteadOfTake = true;
                    } else if (queueType.equals("outbox")) {
                        queuePrefType = new QueueType(QueueType.Types.OUTBOX);
                    }


                    ace.getAceFrameConfig().getDbConfig().getQueues().add(FileIO.getRelativePath(queueDirectory));
                    QueuePreferences queuePreferences = new QueuePreferences(queueDirectory.getName(), UUID.randomUUID().toString(), 
                                                                         queueDirectory, readInsteadOfTake, queuePrefType);
                    queuePreferences.getServiceItemProperties().add(new QueueAddress(workingCopy));
                    ace.getAceFrameConfig().getQueueAddressesToShow().add(workingCopy);


                    if (QueueServer.started(queuePreferences)) {
                        AceLog.getAppLog().info(
                            "Queue already started: " + queueDirectory.toURI().toURL().toExternalForm());
                    } else {
                        // TODO: Replace with real logic.
                        // This just here so class can compile.
                        //new QueueServer(new String[] { queueFile.getCanonicalPath() }, null);
                        new QueueServer(queuePreferences);
                    // add to queue list...
                    QueueList queueList = new QueueList(TtkPreferences.get());
                    queueList.getQueuePreferences().add(queuePreferences);
                    TtkPreferences.get().write(queuePreferences);
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
        if (location == null) {
            location = new Point(0, 0);
        }
        queueTypePopup.show(newQueueButton, location.x, location.y);
    }
}
