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
package org.dwfa.ace.task;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.db.HasUncommittedChanges;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/instruct", type = BeanType.TASK_BEAN),
                   @Spec(directory = "tasks/ide/wfpanel", type = BeanType.TASK_BEAN) })
public class InstructAndWaitStepOrCancel extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String instruction = "<html>Instruction";

    private transient Condition returnCondition;

    private transient boolean done;
    
    private transient I_EncodeBusinessProcess process;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(instruction);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            // nothing to read...
            instruction = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    private class StepActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (Terms.get().getUncommitted().size() > 0) {
            	if (Terms.get().getUncommitted().size() > 0) {
            		for (I_Transact c: Terms.get().getUncommitted()) {
            			AceLog.getAppLog().warning("Uncommitted changes to: " 
            					+ ((I_GetConceptData) c).toLongString());
            			
            		}
            		HasUncommittedChanges.askToCommit(process);
            	}
                if (!DwfaEnv.isHeadless()) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "There are uncommitted changes - please cancel or commit before continuing.", "",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                returnCondition = Condition.ITEM_COMPLETE;
                done = true;
                synchronized (InstructAndWaitStepOrCancel.this) {
                    InstructAndWaitStepOrCancel.this.notifyAll();
                }
            }
        }

    }

    private class StopActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.ITEM_CANCELED;
            done = true;
            synchronized (InstructAndWaitStepOrCancel.this) {
                InstructAndWaitStepOrCancel.this.notifyAll();
            }

        }

    }

    private void waitTillDone(Logger l) {
        while (!this.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                l.log(Level.SEVERE, e.getMessage(), e);
            }
        }

    }

    public boolean isDone() {
        return this.done;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        this.done = false;
        this.process = process;
        I_ConfigAceFrame config =
                (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
        boolean builderVisible = config.isBuilderToggleVisible();
        config.setBuilderToggleVisible(false);
        boolean subversionButtonVisible = config.isSubversionToggleVisible();
        config.setSubversionToggleVisible(false);
        boolean inboxButtonVisible = config.isInboxToggleVisible();
        config.setInboxToggleVisible(false);
        try {
            final JPanel workflowPanel = config.getWorkflowPanel();
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    Component[] components = workflowPanel.getComponents();
                    for (int i = 0; i < components.length; i++) {
                        workflowPanel.remove(components[i]);
                    }
                    workflowPanel.setLayout(new GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints();
                    c.fill = GridBagConstraints.BOTH;
                    c.gridx = 0;
                    c.gridy = 0;
                    c.weightx = 1.0;
                    c.weighty = 0;
                    c.anchor = GridBagConstraints.WEST;
                    c.weightx = 0.0;
                    workflowPanel.add(new JLabel(instruction), c);
                    c.gridx++;
                    workflowPanel.add(new JLabel("  "), c);
                    c.gridx++;
                    c.anchor = GridBagConstraints.SOUTHWEST;
                    JButton stepButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getTrueImage())));
                    stepButton.setToolTipText("Step");
                    workflowPanel.add(stepButton, c);

                    c.gridx++;
                    stepButton.addActionListener(new StepActionListener());
                    JButton stopButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getFalseImage())));
                    workflowPanel.add(stopButton, c);
                    stopButton.addActionListener(new StopActionListener());
                    stopButton.setToolTipText("Cancel");
                    c.gridx++;
                    workflowPanel.add(new JLabel("  "), c);
                    workflowPanel.validate();
                    Container cont = workflowPanel;
                    while (cont != null) {
                        cont.validate();
                        cont = cont.getParent();
                    }
                    workflowPanel.setVisible(true);
                    workflowPanel.repaint();
                    stepButton.requestFocusInWindow();
                }
            });
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    Component[] components = workflowPanel.getComponents();
                    for (int i = 0; i < components.length; i++) {
                        workflowPanel.remove(components[i]);
                    }
                    workflowPanel.validate();
                    Container cont = workflowPanel;

                    while (cont != null) {
                        cont.validate();
                        cont = cont.getParent();
                    }
                    workflowPanel.repaint();
                    workflowPanel.setVisible(false);
                }

            });
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        }
        config.setBuilderToggleVisible(builderVisible);
        config.setSubversionToggleVisible(subversionButtonVisible);
        config.setInboxToggleVisible(inboxButtonVisible);
        return returnCondition;
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
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    protected String getTrueImage() {
        return "/16x16/plain/media_step_forward.png";
    }

    protected String getFalseImage() {
        return "/16x16/plain/media_stop_red.png";
    }
}
