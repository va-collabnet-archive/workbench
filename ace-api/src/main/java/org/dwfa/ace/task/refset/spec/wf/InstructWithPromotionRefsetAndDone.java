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
package org.dwfa.ace.task.refset.spec.wf;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.MarshalledObject;
import java.util.Collection;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * 
 * 
 * @author Christine Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class InstructWithPromotionRefsetAndDone extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String instruction = "<html>Instruction";

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String processPropName = ProcessAttachmentKeys.PROCESS_TO_LAUNCH.getAttachmentKey();

    protected transient Condition returnCondition;

    protected transient boolean done;

    protected transient I_ConfigAceFrame config;
    protected transient boolean builderVisible;
    protected transient boolean progressPanelVisible;
    protected transient boolean subversionButtonVisible;
    protected transient boolean inboxButtonVisible;
    protected transient JPanel workflowPanel;
    protected transient I_TermFactory termFactory;
    protected transient I_EncodeBusinessProcess originalProcess;
    private String bpFileString;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(instruction);
        out.writeObject(processPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            profilePropName = (String) in.readObject();
            instruction = (String) in.readObject();
            processPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private class DoneActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.CONTINUE;
            done = true;
            synchronized (InstructWithPromotionRefsetAndDone.this) {
                InstructWithPromotionRefsetAndDone.this.notifyAll();
            }
        }
    }

    private class PromoteActionListener implements ActionListener {

        public PromoteActionListener() {
            super();
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {

            final JButton button = (JButton) e.getSource();
            Runnable r = new Runnable() {

                public void run() {
                    try {

                        button.setEnabled(false);
                        final I_Work worker;
                        if (config.getWorker().isExecuting()) {
                            worker = config.getWorker().getTransactionIndependentClone();
                        } else {
                            worker = config.getWorker();
                        }

                        MarshalledObject marshalledProcess = (MarshalledObject) originalProcess.readProperty(processPropName);
                        if (marshalledProcess == null) {
                            marshalledProcess = (MarshalledObject) originalProcess.readAttachement(processPropName);
                        }
                        I_EncodeBusinessProcess processToLaunch = (I_EncodeBusinessProcess) marshalledProcess.get();
                        Stack<I_EncodeBusinessProcess> ps = worker.getProcessStack();
                        worker.setProcessStack(new Stack<I_EncodeBusinessProcess>());
                        processToLaunch.execute(worker);
                        worker.setProcessStack(ps);
                    } catch (Exception e) {
                        e.printStackTrace();
                        button.setEnabled(true);
                    }
                }

            };
            new Thread(r).start();

            synchronized (InstructWithPromotionRefsetAndDone.this) {
                InstructWithPromotionRefsetAndDone.this.notifyAll();
            }
        }
    }

    protected void waitTillDone(Logger l) {
        while (!this.isDone()) {
            try {
                wait();
            } catch (InterruptedException e) {
                l.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        config.setBuilderToggleVisible(true);
        config.setSubversionToggleVisible(subversionButtonVisible);
        config.setInboxToggleVisible(true);
    }

    public boolean isDone() {
        return this.done;
    }

    protected void setupButtons(final JPanel workflowPanel, GridBagConstraints c) {
        c.gridy++;
        c.insets = new Insets(0, 5, 0, 5); // padding

        JButton promoteButton = new JButton("Run promotion BP");
        promoteButton.setToolTipText("Run promotion BP");
        workflowPanel.add(promoteButton, c);
        promoteButton.addActionListener(new PromoteActionListener());
        c.gridx++;

        JButton doneButton = new JButton("Done");
        doneButton.setToolTipText("Done");
        workflowPanel.add(doneButton, c);
        doneButton.addActionListener(new DoneActionListener());
        c.gridx++;
        c.gridy++;

        // filler
        c.weighty = 1.0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.LINE_END;
        workflowPanel.add(Box.createGlue(), c);

        workflowPanel.validate();
        workflowPanel.setVisible(true);
        Container cont = workflowPanel;
        while (cont != null) {
            cont.validate();
            cont = cont.getParent();
        }
        workflowPanel.repaint();
        doneButton.requestFocusInWindow();
    }

    protected void restore() throws InterruptedException, InvocationTargetException {
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
        config.setBuilderToggleVisible(builderVisible);
        config.setSubversionToggleVisible(subversionButtonVisible);
        config.setInboxToggleVisible(inboxButtonVisible);
    }

    protected void setup(I_EncodeBusinessProcess process) throws IntrospectionException, IllegalAccessException,
            InvocationTargetException, IllegalArgumentException, TerminologyException, IOException {
        termFactory = LocalVersionedTerminology.get();
        this.done = false;
        config = (I_ConfigAceFrame) process.readProperty(getProfilePropName());
        System.out.println(bpFileString);

        builderVisible = config.isBuilderToggleVisible();
        config.setBuilderToggleVisible(false);
        subversionButtonVisible = config.isBuilderToggleVisible();
        config.setSubversionToggleVisible(false);
        inboxButtonVisible = config.isInboxToggleVisible();
        config.setInboxToggleVisible(false);
        workflowPanel = config.getWorkflowPanel();
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1) throws TaskFailedException {
        // nothing to do
    }

    private class DoSwing extends SwingWorker<Boolean> {

        I_EncodeBusinessProcess process;

        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
        }

        @Override
        protected Boolean construct() throws Exception {
            setup(process);
            return true;
        }

        @Override
        protected void finished() {
            Component[] components = workflowPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                workflowPanel.remove(components[i]);
            }
            workflowPanel.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.weighty = 0.0;
            c.weightx = 0.0;
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            workflowPanel.add(new JLabel(instruction), c);
            setupButtons(workflowPanel, c);
        }

    }

    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            originalProcess = process;
            DoSwing swinger = new DoSwing(process);
            swinger.start();
            swinger.get();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }
            restore();
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        return returnCondition;
    }

    public String getProcessPropName() {
        return processPropName;
    }

    public void setProcessPropName(String processPropName) {
        this.processPropName = processPropName;
    }

}
