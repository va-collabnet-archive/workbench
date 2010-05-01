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
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class PromptUserForInput extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String instruction = "<html>Instruction";
    private String newRefsetPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String refsetName = "";
    private transient Condition returnCondition;

    private transient boolean done;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(instruction);
        out.writeObject(newRefsetPropName);
    }// End method writeObject

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            // nothing to read...
            instruction = (String) in.readObject();
            newRefsetPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }// End method readObject

    private class StepActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {

            JButton stepButton = (JButton) e.getSource();
            JPanel workflowPanel = (JPanel) stepButton.getParent();

            Component[] components = workflowPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof JTextField) {
                    refsetName = ((JTextField) components[i]).getText();
                }
            }

            returnCondition = Condition.ITEM_COMPLETE;
            done = true;
            synchronized (PromptUserForInput.this) {
                PromptUserForInput.this.notifyAll();
            }

        }// End method actionPerformed

    }// End nested class StepActionListener

    private class StopActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.ITEM_CANCELED;
            done = true;
            synchronized (PromptUserForInput.this) {
                PromptUserForInput.this.notifyAll();
            }

        }// End method actionPerformed

    }// End nested class StopActionListener

    private void waitTillDone(Logger l) {
        while (!this.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                l.log(Level.SEVERE, e.getMessage(), e);
            }
        }

    }// End method waitTilDone

    public boolean isDone() {
        return this.done;
    }// End method isDone

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        this.done = false;
        I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
        boolean builderVisible = config.isBuilderToggleVisible();
        config.setBuilderToggleVisible(false);
        boolean subversionButtonVisible = config.isBuilderToggleVisible();
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
                    
                    workflowPanel.add(new JLabel(instruction), c);
                    c.insets = new java.awt.Insets(0, 10, 0, 0);

                    JTextField nameField = new JTextField(15);
                    workflowPanel.add(nameField, c);
 
                    JButton stepButton = new JButton(new ImageIcon(
                        InstructAndWait.class.getResource("/16x16/plain/media_step_forward.png")));
                    stepButton.setToolTipText("Next step");
                    workflowPanel.add(stepButton, c);
                    stepButton.addActionListener(new StepActionListener());

                    JButton stopButton = new JButton(new ImageIcon(
                        InstructAndWait.class.getResource("/16x16/plain/media_stop_red.png")));
                    stopButton.setToolTipText("Cancel");
                    workflowPanel.add(stopButton, c);
                    stopButton.addActionListener(new StopActionListener());

                    workflowPanel.validate();
                    Container cont = workflowPanel;
                    while (cont != null) {                        
                        cont.validate();
                        cont = cont.getParent();                        
                    }
                    workflowPanel.repaint();
                    nameField.requestFocusInWindow(); 
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

            /*
             * Set process property value
             */

            process.setProperty(newRefsetPropName, refsetName);

        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        }

        config.setBuilderToggleVisible(builderVisible);
        config.setSubversionToggleVisible(subversionButtonVisible);
        config.setInboxToggleVisible(inboxButtonVisible);
        return returnCondition;
    }// End method evaluate

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }// End method complete

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }// End method getConditions

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }// End method getDataContainerIds

    public String getInstruction() {
        return instruction;
    }// End method getInstruction

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }// End method setInstruction

    public String getNewRefsetPropName() {
        return newRefsetPropName;
    }// End method getInstruction

    public void setNewRefsetPropName(String newRefsetPropName) {
        this.newRefsetPropName = newRefsetPropName;
    }// End method setInstruction

}// End class PromptUserForInput
