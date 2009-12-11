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
package org.dwfa.ace;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;

import javax.security.auth.login.LoginException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.gui.ProcessBuilderPanel;
import org.dwfa.bpa.worker.MasterWorker;

public class ProcessBuilderContainer extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ProcessBuilderContainer(Configuration config,
            I_ConfigAceFrame aceFrameConfig) throws ConfigurationException,
            LoginException, IOException, PrivilegedActionException,
            IntrospectionException, InvocationTargetException,
            IllegalAccessException, PropertyVetoException,
            ClassNotFoundException, NoSuchMethodException {
        super(new GridBagLayout());
        MasterWorker processWorker = new MasterWorker(config);
        if (aceFrameConfig == null) {
            throw new NullPointerException("aceFrameConfig cannot be null...");
        }
        processWorker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG
            .name(), aceFrameConfig);

        ProcessBuilderPanel processBuilderPanel =
                new ProcessBuilderPanel(config, processWorker);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(getProcessBuilderTopPanel(processBuilderPanel), c);
        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(processBuilderPanel, c);

    }

    private static JPanel getProcessBuilderTopPanel(
            ProcessBuilderPanel processBuilderPanel) {
        JPanel listEditorTopPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        addActionButton(processBuilderPanel.getNewProcessActionListener(),
            "/24x24/plain/cube_molecule_new.png", "new process",
            listEditorTopPanel, c);
        addActionButton(processBuilderPanel.getReadProcessActionListener(),
            "/24x24/plain/read_from_disk.png", "read process",
            listEditorTopPanel, c);

        addActionButton(processBuilderPanel
            .getTakeNoTranProcessActionListener(),
            "/24x24/plain/outbox_out.png", "take process (no transaction)",
            listEditorTopPanel, c);

        addActionButton(processBuilderPanel.getSaveProcessActionListener(),
            "/24x24/plain/save_to_disk.png", "save process",
            listEditorTopPanel, c);

        addActionButton(processBuilderPanel
            .getSaveForLauncherQueueActionListener(),
            "/24x24/plain/inbox_into.png", "save for queue",
            listEditorTopPanel, c);
        /*
        addActionButton(processBuilderPanel.getSaveAsXmlActionListener(), 
        		"/24x24/plain/save_as_xml.png",
        		"save as XML",
        		listEditorTopPanel, c);
         */
        c.weightx = 1.0;
        listEditorTopPanel.add(new JLabel(" "), c); //filler
        c.gridx++;
        c.weightx = 0.0;
        listEditorTopPanel.add(new JLabel(" "), c); //right sided buttons
        return listEditorTopPanel;

    }

    private static void addActionButton(ActionListener actionListener,
            String resource, String tooltipText, JPanel topPanel,
            GridBagConstraints c) {
        JButton newProcess =
                new JButton(new ImageIcon(ACE.class.getResource(resource)));
        newProcess.setToolTipText(tooltipText);
        newProcess.addActionListener(actionListener);
        topPanel.add(newProcess, c);
        c.gridx++;
    }

}
