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
/*
 * Created on Apr 28, 2005
 */
package org.dwfa.queue.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.jini.core.lookup.ServiceID;

import org.dwfa.bpa.process.I_DescribeQueueEntry;
import org.dwfa.bpa.process.I_QueueProcesses;

/**
 * @author kec
 * 
 */
public class QueueSelectorPanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public class QueueAdaptor {
        I_QueueProcesses queue;

        String queueName;

        ServiceID id;

        /**
         * @param queue
         * @param queueName
         * @param id
         */
        public QueueAdaptor(I_QueueProcesses queue, String queueName, ServiceID id) {
            super();
            this.queue = queue;
            this.queueName = queueName;
            this.id = id;
        }

        public String toString() {
            return this.queueName;
        }
    }

    private JButton selectButton = new JButton("<html><font color='#006400'>select");

    private JLabel statusMessage = new JLabel();

    private JTable queueTable;
    QueueTableModel model;

    public QueueSelectorPanel(I_QueueProcesses queue) throws RemoteException, InterruptedException, IOException {
        super(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        model = new QueueTableModel(queue);
        this.queueTable = new JTable(model);
        this.add(new JScrollPane(this.queueTable), c);
        JPanel statusPanel = makeStatusPanel(this.statusMessage, selectButton);
        c.weighty = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(statusPanel, c);
    }

    private JPanel makeStatusPanel(JLabel statusMessage, JButton execute) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;
        c.gridy = 0;
        c.gridx = 6;
        panel.add(new JLabel("    "), c); // filler for grow box.

        c.gridx = 5;
        panel.add(execute, c);
        c.gridx = 4;
        // panel.add(cancel, c);
        c.gridx = 3;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(statusMessage, c);
        c.weightx = 0;
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("   "), c);
        c.gridx = 1;
        // ButtonGroup group = new ButtonGroup();
        // JRadioButton taskButton = new JRadioButton("task");
        // taskButton.setSelected(true);
        // taskButton.addActionListener(new ChangeToTaskActionListener());
        // group.add(taskButton);
        panel.add(new JPanel(), c);
        c.gridx = 0;
        // JRadioButton dataButton = new JRadioButton("data");
        // dataButton.setSelected(false);
        // group.add(dataButton);
        // dataButton.addActionListener(new ChangeToDataActionListener());
        panel.add(new JPanel(), c);

        return panel;
    }

    /**
     * @return Returns the selectButton.
     */
    public JButton getSelectButton() {
        return selectButton;
    }

    public I_DescribeQueueEntry getSelectedEntryMetaData() {
        return model.getRowMetaData(this.queueTable.getSelectedRow());
    }
}
