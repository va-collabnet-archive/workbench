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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
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
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Instruct with buttons for Review Selected List, Save (to queue) and Done
 * button.
 * 
 * @author Christine Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class ReviewSelectedSaveAndDone extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    protected transient Condition returnCondition;

    protected transient boolean done;

    protected transient I_TermFactory termFactory;
    protected transient I_ConfigAceFrame config;
    protected transient boolean builderVisible;
    protected transient boolean progressPanelVisible;
    protected transient boolean subversionButtonVisible;
    protected transient boolean inboxButtonVisible;
    protected transient JPanel workflowPanel;
    protected transient I_EncodeBusinessProcess process;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            profilePropName = (String) in.readObject();
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
            synchronized (ReviewSelectedSaveAndDone.this) {
                ReviewSelectedSaveAndDone.this.notifyAll();
            }
        }
    }

    private class SaveActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.ITEM_CANCELED;
            try {
                process.setProperty(ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey(), new UUID[] { config
                    .getDbConfig().getUserConcept().getUids().iterator().next() });
                I_GetConceptData refsetConcept = config.getRefsetInSpecEditor();
                if (refsetConcept != null) {
                    process.setProperty(ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey(), refsetConcept.getUids()
                        .iterator().next());
                }
                done = true;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            synchronized (ReviewSelectedSaveAndDone.this) {
                ReviewSelectedSaveAndDone.this.notifyAll();
            }
        }
    }

    private class ReviewActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            returnCondition = Condition.PREVIOUS;
            done = true;
            synchronized (ReviewSelectedSaveAndDone.this) {
                ReviewSelectedSaveAndDone.this.notifyAll();
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

    protected void setupDoneAndTodoButtons(final JPanel workflowPanel, GridBagConstraints c) {
        c.gridx++;
        workflowPanel.add(new JLabel("  "), c);
        c.gridx++;
        c.anchor = GridBagConstraints.SOUTHWEST;

        JButton reviewButton = new JButton("Review selected list");
        reviewButton
            .setToolTipText("Review selected list - opens up the list view and lets you approve or disapprove of specific refset members");
        workflowPanel.add(reviewButton, c);
        reviewButton.addActionListener(new ReviewActionListener());
        c.gridx++;

        JButton saveButton =
                new JButton(new ImageIcon(InstructAndWait.class.getResource("/24x24/plain/inbox_into.png")));
        saveButton
            .setToolTipText("Save to TODO queue - choose this option if you want to finish this work at a later date");
        workflowPanel.add(saveButton, c);
        saveButton.addActionListener(new SaveActionListener());
        c.gridx++;

        JButton doneButton = new JButton("Done");
        doneButton
            .setToolTipText("Done - choose this option if you've finished checking the promotion status of the refset");
        workflowPanel.add(doneButton, c);
        doneButton.addActionListener(new DoneActionListener());
        c.gridx++;

        try {
            if (allPromotionStatusesReviewed()) {
                doneButton.setEnabled(true);
            } else {
                doneButton.setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "Promotion wizard cannot be completed. Error : " + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            doneButton.setEnabled(false);
        }

        workflowPanel.add(new JLabel("     "), c);
        workflowPanel.validate();
        workflowPanel.setVisible(true);
        Container cont = workflowPanel;
        while (cont != null) {
            cont.validate();
            cont = cont.getParent();
        }
        workflowPanel.repaint();
        reviewButton.requestFocusInWindow();
        workflowPanel.revalidate();
    }

    private boolean allPromotionStatusesReviewed() throws Exception {
        UUID memberRefsetUuid = (UUID) process.getProperty(ProcessAttachmentKeys.PROMOTION_UUID.getAttachmentKey());
        I_GetConceptData memberRefsetConcept = termFactory.getConcept(new UUID[] { memberRefsetUuid });
        I_GetConceptData unreviewedAdditionStatus =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.getUids());
        I_GetConceptData unreviewedDeletionStatus =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION.getUids());

        I_HelpSpecRefset refsetHelper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
        List<I_GetConceptData> newAdditions =
                refsetHelper.filterListByConceptType(termFactory.getRefsetExtensionMembers(memberRefsetConcept
                    .getConceptId()), unreviewedAdditionStatus);
        List<I_GetConceptData> newDeletions =
                refsetHelper.filterListByConceptType(termFactory.getRefsetExtensionMembers(memberRefsetConcept
                    .getConceptId()), unreviewedDeletionStatus);

        if (newAdditions.size() != 0) {
            return false;
        }
        if (newDeletions.size() != 0) {
            return false;
        }

        return true;
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
        termFactory = Terms.get();
        this.process = process;

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
        return AbstractTask.PREVIOUS_CONTINUE_CANCEL;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
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
            c.weightx = 0.0;
            setupDoneAndTodoButtons(workflowPanel, c);
        }

    }

    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
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
}
