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
package org.dwfa.ace.task.wfpanel;

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
import java.util.concurrent.ExecutionException;
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
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.db.HasUncommittedChanges;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.swing.SwingWorker;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Instruct with buttons for Done and Todo (todo moves to the user's todo box).
 * 
 * @author Christine Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/instruct", type = BeanType.TASK_BEAN),
                   @Spec(directory = "tasks/ide/wfpanel", type = BeanType.TASK_BEAN) })
public class InstructWithDoneAndTodo extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String instruction = "<html>Instruction";

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    protected transient Condition returnCondition;

    protected transient boolean done;

    private transient I_EncodeBusinessProcess process;

    protected transient I_ConfigAceFrame config;
    protected transient boolean builderVisible;
    protected transient boolean progressPanelVisible;
    protected transient boolean subversionButtonVisible;
    protected transient boolean inboxButtonVisible;
    protected transient JPanel workflowPanel;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(instruction);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            profilePropName = (String) in.readObject();
            instruction = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private class DoneActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (Terms.get().getUncommitted().size() > 0) {
                for (I_Transact c : Terms.get().getUncommitted()) {
                    AceLog.getAppLog().warning("Uncommitted changes to: " + ((I_GetConceptData) c).toLongString());

                }
                HasUncommittedChanges.askToCommit(process);
            }
            if (Terms.get().getUncommitted().size() > 0) {
                if (!DwfaEnv.isHeadless()) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "There are uncommitted changes - please cancel or commit before continuing.", "",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                returnCondition = Condition.CONTINUE;
                done = true;
                synchronized (InstructWithDoneAndTodo.this) {
                    InstructWithDoneAndTodo.this.notifyAll();
                }
            }
        }
    }

    private class SaveActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (Terms.get().getUncommitted().size() > 0) {
                for (I_Transact c : Terms.get().getUncommitted()) {
                    AceLog.getAppLog().warning("Uncommitted changes to: " + ((I_GetConceptData) c).toLongString());

                }
                HasUncommittedChanges.askToCommit(process);
            }
            if (Terms.get().getUncommitted().size() > 0) {
                if (!DwfaEnv.isHeadless()) {
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "There are uncommitted changes - please cancel or commit before continuing.", "",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                returnCondition = Condition.ITEM_CANCELED;
                done = true;
                synchronized (InstructWithDoneAndTodo.this) {
                    InstructWithDoneAndTodo.this.notifyAll();
                }
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
    }

    public boolean isDone() {
        return this.done;
    }

    protected void setupDoneAndTodoButtons(final JPanel workflowPanel, GridBagConstraints c) {
        c.gridx++;
        workflowPanel.add(new JLabel("  "), c);
        c.gridx++;
        c.anchor = GridBagConstraints.SOUTHWEST;

        // JButton doneButton = new JButton("Done");
        JButton doneButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getStepForwardImage())));
        doneButton.setToolTipText("Done");
        workflowPanel.add(doneButton, c);
        doneButton.addActionListener(new DoneActionListener());
        c.gridx++;
        // JButton saveButton = new JButton("Save");
        JButton saveButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getToDoImage())));
        saveButton.setToolTipText("Save in todo queue");
        workflowPanel.add(saveButton, c);
        saveButton.addActionListener(new SaveActionListener());
        c.gridx++;
        workflowPanel.add(new JLabel("     "), c);
        workflowPanel.validate();
        workflowPanel.setVisible(true);
        Container cont = workflowPanel;
        while (cont != null) {
            cont.validate();
            cont = cont.getParent();
        }
        workflowPanel.repaint();
        saveButton.requestFocusInWindow();
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
            InvocationTargetException {
        this.done = false;
        config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());

        builderVisible = config.isBuilderToggleVisible();
        config.setBuilderToggleVisible(false);
        subversionButtonVisible = config.isSubversionToggleVisible();
        config.setSubversionToggleVisible(false);
        inboxButtonVisible = config.isInboxToggleVisible();
        config.setInboxToggleVisible(false);
        workflowPanel = config.getWorkflowPanel();
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CANCEL;
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
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            c.weighty = 0;
            c.anchor = GridBagConstraints.EAST;
            workflowPanel.add(new JLabel(instruction), c);
            c.weightx = 0.0;
            setupDoneAndTodoButtons(workflowPanel, c);
        }

    }

    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        this.process = process;
        try {
            DoSwing swinger = new DoSwing(process);
            swinger.start();
            swinger.get();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }
            restore();
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (ExecutionException e) {
            throw new TaskFailedException(e);
        }
        return returnCondition;
    }

    protected static String getDoneImage() {
        return "/24x24/plain/navigate_check.png";
    }

    public static String getToDoImage() {
        return "/24x24/plain/inbox_into.png";
    }

    protected static String getStepForwardImage() {
        return "/24x24/plain/media_step_forward.png";
    }

    protected static String getPreviousImage() {
        return "/16x16/plain/navigate_left.png";
    }

    protected static String getContinueImage() {
        return "/16x16/plain/navigate_right.png";
    }

    protected static String getCancelImage() {
        return "/16x16/plain/navigate_cross.png";
    }

}
