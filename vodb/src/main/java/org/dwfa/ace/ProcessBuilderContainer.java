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
import java.beans.PropertyVetoException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.jini.config.Configuration;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.gui.I_HandleDoubleClickInTaskProcess;
import org.dwfa.bpa.gui.ProcessBuilderPanel;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.util.FrameWithOpenFramesListener;
import org.dwfa.bpa.worker.MasterWorker;

public class ProcessBuilderContainer extends JPanel implements I_HandleDoubleClickInTaskProcess {

    public enum ContainerType {
        TOP_LEVEL, EMBEDDED_TASK
    };

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Configuration config;

    private I_ConfigAceFrame aceFrameConfig;

    private MasterWorker processWorker;

    private ProcessBuilderPanel processBuilderPanel;

    public ProcessBuilderContainer(Configuration config, I_ConfigAceFrame aceFrameConfig) throws Exception {
        this(config, aceFrameConfig, ContainerType.TOP_LEVEL);
    }

    public ProcessBuilderContainer(Configuration config, I_ConfigAceFrame aceFrameConfig, ContainerType type)
            throws Exception {
        super(new GridBagLayout());
        this.config = config;
        this.aceFrameConfig = aceFrameConfig;
        processWorker = new MasterWorker(config);
        if (aceFrameConfig == null) {
            throw new NullPointerException("aceFrameConfig cannot be null...");
        }
        processWorker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), aceFrameConfig);

        processBuilderPanel = new ProcessBuilderPanel(config, processWorker);
        processBuilderPanel.setDoubleClickHandler(this);
        processBuilderPanel.newProcess();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(getProcessBuilderTopPanel(processBuilderPanel, type), c);
        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(processBuilderPanel, c);

    }

    private static JPanel getProcessBuilderTopPanel(ProcessBuilderPanel processBuilderPanel, ContainerType type) {
        JPanel listEditorTopPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        if (type.equals(ContainerType.TOP_LEVEL)) {
            addActionButton(processBuilderPanel.getNewProcessActionListener(), "/24x24/plain/cube_molecule_new.png",
                "new process", listEditorTopPanel, c);
            addActionButton(processBuilderPanel.getReadProcessActionListener(), "/24x24/plain/read_from_disk.png",
                "read process", listEditorTopPanel, c);

            addActionButton(processBuilderPanel.getTakeNoTranProcessActionListener(), "/24x24/plain/outbox_out.png",
                "take process (no transaction)", listEditorTopPanel, c);
        }
        addActionButton(processBuilderPanel.getSaveProcessActionListener(), "/24x24/plain/save_to_disk.png",
            "save process", listEditorTopPanel, c);
        if (type.equals(ContainerType.TOP_LEVEL)) {

            addActionButton(processBuilderPanel.getSaveForLauncherQueueActionListener(), "/24x24/plain/inbox_into.png",
                "save for queue", listEditorTopPanel, c);

        }
        if (type.equals(ContainerType.EMBEDDED_TASK)) {
            addActionButton(processBuilderPanel.getWriteAttachmentActionListener(), "/24x24/plain/mail_attachment.png",
                "write attachment", listEditorTopPanel, c);
        }
        addActionButton(processBuilderPanel.getClearExecutionRecordsActionListener(),
            "/24x24/plain/history_delete.png", "remove history", listEditorTopPanel, c);
        /*
         * addActionButton(processBuilderPanel.getSaveAsXmlActionListener(),
         * "/24x24/plain/save_as_xml.png",
         * "save as XML",
         * listEditorTopPanel, c);
         */
        c.weightx = 1.0;
        listEditorTopPanel.add(new JLabel(" "), c); // filler
        c.gridx++;
        c.weightx = 0.0;
        listEditorTopPanel.add(new JLabel(" "), c); // right sided buttons
        return listEditorTopPanel;

    }

    private static JButton addActionButton(ActionListener actionListener, String resource, String tooltipText,
            JPanel topPanel, GridBagConstraints c) {
        JButton button = new JButton(new ImageIcon(ACE.class.getResource(resource)));
        button.setToolTipText(tooltipText);
        button.addActionListener(actionListener);
        topPanel.add(button, c);
        c.gridx++;
        return button;
    }

    public void handle(I_EncodeBusinessProcess process, I_Work worker, I_EncodeBusinessProcess parent) {
        try {
            ProcessBuilderContainer pbc = new ProcessBuilderContainer(config, aceFrameConfig,
                ContainerType.EMBEDDED_TASK);
            pbc.processBuilderPanel.setExecutionButtonsVisible(false);
            pbc.processBuilderPanel.setDoubleClickHandler(pbc);
            pbc.processBuilderPanel.setProcess(process);
            pbc.processBuilderPanel.setParentProcess(parent);
            new FrameWithOpenFramesListener("Embedded Process Editor: " + process.getName(), "Workflow Bundle",
                new JScrollPane(pbc));
        } catch (PropertyVetoException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

}
