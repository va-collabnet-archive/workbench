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
package org.dwfa.ace.task.input;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task prompts the user to enter a number. Non-valid characters cannot be
 * entered.
 * The task can be configured to include/exclude negative numbers, zeroes and
 * floating
 * point numbers.
 * Based on PromptForUserInput task
 * 
 * @author Christine Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class PromptForNumericInput extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private String outputPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String instruction = "<html>Instruction";
    public boolean allowNegative = false;
    public boolean allowZero = false;
    public boolean allowDouble = false;
    private Number userInputtedNumber;
    private Condition returnCondition;
    private transient boolean done;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(outputPropName);
        out.writeObject(instruction);
        out.writeObject(allowNegative);
        out.writeObject(allowZero);
        out.writeObject(allowDouble);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            outputPropName = (String) in.readObject();
            instruction = (String) in.readObject();
            allowNegative = (Boolean) in.readObject();
            allowZero = (Boolean) in.readObject();
            allowDouble = (Boolean) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

        // record which buttons were visible so they can be reset appropriately
        // when
        // finished
        done = false;
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
                    c.anchor = GridBagConstraints.EAST;
                    workflowPanel.add(new JLabel(instruction), c);
                    c.weightx = 0.0;
                    c.gridx++;
                    workflowPanel.add(new JLabel("  "), c);
                    c.gridx++;

                    // set up formatted text field that will only accept valid
                    // numbers
                    DecimalFormat decimalFormat = new DecimalFormat();
                    decimalFormat.setMaximumFractionDigits(0);
                    NumberFormatterImpl formatter = new NumberFormatterImpl(decimalFormat, allowNegative, allowZero,
                        allowDouble);
                    JFormattedTextField nameField = new JFormattedTextField(formatter);
                    nameField.setPreferredSize(new Dimension(200, nameField.getHeight()));
                    workflowPanel.add(nameField, c);

                    // step image
                    c.gridx++;
                    c.anchor = GridBagConstraints.SOUTHWEST;
                    JButton stepButton = new JButton(new ImageIcon(
                        InstructAndWait.class.getResource("/16x16/plain/media_step_forward.png")));
                    workflowPanel.add(stepButton, c);

                    // stop image
                    c.gridx++;
                    stepButton.addActionListener(new StepActionListener());
                    JButton stopButton = new JButton(new ImageIcon(
                        InstructAndWait.class.getResource("/16x16/plain/media_stop_red.png")));
                    workflowPanel.add(stopButton, c);
                    stopButton.addActionListener(new StopActionListener());
                    c.gridx++;
                    workflowPanel.add(new JLabel("     "), c);
                    workflowPanel.validate();
                    Container cont = workflowPanel;
                    while (cont != null) {
                        cont.validate();
                        cont = cont.getParent();
                    }
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
                }
            });

            // zeroes are allowed to be entered at the start of the text field
            // for
            // floating point numbers (e.g. 0.000001 is valid) however we need
            // to double check
            // that the user hasn't entered something along the lines of 0.0000
            // and hit step
            // when a value of zero isn't allowed
            if (allowDouble && !allowZero && new Double(0).equals(userInputtedNumber)) {
                userInputtedNumber = null;
            }

            process.setProperty(outputPropName, userInputtedNumber);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        // reset button visibility as they were before this task executed
        config.setBuilderToggleVisible(builderVisible);
        config.setSubversionToggleVisible(subversionButtonVisible);
        config.setInboxToggleVisible(inboxButtonVisible);

        return returnCondition;

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

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getOutputPropName() {
        return outputPropName;
    }

    public void setOutputPropName(String outputPropName) {
        this.outputPropName = outputPropName;
    }

    public boolean isAllowDouble() {
        return allowDouble;
    }

    public void setAllowDouble(boolean allowDouble) {
        this.allowDouble = allowDouble;
    }

    public boolean isAllowNegative() {
        return allowNegative;
    }

    public void setAllowNegative(boolean allowNegative) {
        this.allowNegative = allowNegative;
    }

    public boolean isAllowZero() {
        return allowZero;
    }

    public void setAllowZero(boolean allowZero) {
        this.allowZero = allowZero;
    }

    private class StepActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {

            JButton stepButton = (JButton) e.getSource();
            JPanel workflowPanel = (JPanel) stepButton.getParent();

            Component[] components = workflowPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof JFormattedTextField) {
                    userInputtedNumber = ((Number) ((JFormattedTextField) components[i]).getValue());
                }
            }

            returnCondition = Condition.ITEM_COMPLETE;
            done = true;
            synchronized (PromptForNumericInput.this) {
                PromptForNumericInput.this.notifyAll();
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
            synchronized (PromptForNumericInput.this) {
                PromptForNumericInput.this.notifyAll();
            }

        }
    }
}
