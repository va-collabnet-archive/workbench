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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Instruct with buttons for Approve, Reject and Done. Pressing the approve
 * button will update the promotion status of the concepts currently selected in
 * the list view, as will reject. Done causes the workflow to continue.
 * 
 * @author Christine Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class InstructWithApproveRejectDone extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String instruction = "<html>Instruction";

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String memberRefsetUuidPropName = ProcessAttachmentKeys.PROMOTION_UUID.getAttachmentKey();
    private String statusUuidPropName = ProcessAttachmentKeys.STATUS_UUID.getAttachmentKey();

    private I_HelpSpecRefset refsetHelper;
    private int refsetId;
    private I_GetConceptData initialPromotionStatus;
    private int approveId;
    private int rejectId;

    private I_GetConceptData unreviewedAdditionStatus;
    private I_GetConceptData unreviewedDeletionStatus;
    private I_GetConceptData reviewedApprovedDeletionStatus;
    private I_GetConceptData reviewedApprovedAdditionStatus;
    private I_GetConceptData reviewedRejectedDeletionStatus;
    private I_GetConceptData reviewedRejectedAdditionStatus;

    protected transient Condition returnCondition;

    protected transient boolean done;

    protected transient I_ConfigAceFrame config;
    protected transient boolean builderVisible;
    protected transient boolean progressPanelVisible;
    protected transient boolean subversionButtonVisible;
    protected transient boolean inboxButtonVisible;
    protected transient JPanel workflowPanel;
    protected transient Box workflowBox;
    protected transient I_TermFactory termFactory;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(instruction);
        out.writeObject(memberRefsetUuidPropName);
        out.writeObject(statusUuidPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            profilePropName = (String) in.readObject();
            instruction = (String) in.readObject();
            memberRefsetUuidPropName = (String) in.readObject();
            statusUuidPropName = (String) in.readObject();
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
            synchronized (InstructWithApproveRejectDone.this) {
                InstructWithApproveRejectDone.this.notifyAll();
            }
        }
    }

    private class ApproveActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                JList conceptList = config.getBatchConceptList();
                I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
                int[] indices = conceptList.getSelectedIndices();
                Set<I_GetConceptData> conceptsToCheck = new HashSet<I_GetConceptData>();
                for (int index : indices) {
                    conceptsToCheck.add((I_GetConceptData) model.getElementAt(index));
                }

                ArrayList<I_GetConceptData> temporaryList = new ArrayList<I_GetConceptData>();

                for (int i = 0; i < model.getSize(); i++) {
                    temporaryList.add(((I_GetConceptData) model.getElementAt(i)));
                }

                for (I_GetConceptData concept : conceptsToCheck) {

                    if (refsetHelper.hasConceptRefsetExtensionWithAnyPromotionStatus(refsetId, concept.getConceptId())) {
                        if (refsetHelper.hasCurrentRefsetExtension(refsetId, concept.getConceptId(), approveId)) {
                            // nothing to do
                        } else {
                            refsetHelper.newConceptExtensionPart(refsetId, concept.getConceptId(), approveId);
                            temporaryList.remove(concept);
                        }
                    } else {
                        // description extension

                        Collection<? extends I_DescriptionVersioned> descriptions = concept.getDescriptions();
                        for (I_DescriptionVersioned desc : descriptions) {
                            if (refsetHelper.hasCurrentRefsetExtension(refsetId, desc.getDescId(), approveId)) {
                                // nothing to do
                            } else {
                                refsetHelper.newConceptExtensionPart(refsetId, desc.getDescId(), approveId);
                                temporaryList.remove(concept);
                            }
                        }
                    }
                }

                termFactory.commit();

                model.clear();
                for (I_GetConceptData c : temporaryList) {
                    model.addElement(c);
                }

                if (temporaryList.size() == 0) {
                    returnCondition = Condition.CONTINUE;
                    done = true;
                    synchronized (InstructWithApproveRejectDone.this) {
                        InstructWithApproveRejectDone.this.notifyAll();
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            synchronized (InstructWithApproveRejectDone.this) {
                InstructWithApproveRejectDone.this.notifyAll();
            }
        }
    }

    private class RejectActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                JList conceptList = config.getBatchConceptList();
                I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
                int[] indices = conceptList.getSelectedIndices();
                Set<I_GetConceptData> conceptsToCheck = new HashSet<I_GetConceptData>();
                for (int index : indices) {
                    conceptsToCheck.add((I_GetConceptData) model.getElementAt(index));
                }

                ArrayList<I_GetConceptData> temporaryList = new ArrayList<I_GetConceptData>();

                for (int i = 0; i < model.getSize(); i++) {
                    temporaryList.add(((I_GetConceptData) model.getElementAt(i)));
                }

                for (I_GetConceptData concept : conceptsToCheck) {
                    if (refsetHelper.hasConceptRefsetExtensionWithAnyPromotionStatus(refsetId, concept.getConceptId())) {
                        if (refsetHelper.hasCurrentRefsetExtension(refsetId, concept.getConceptId(), rejectId)) {
                            // nothing to do
                        } else {
                            refsetHelper.newConceptExtensionPart(refsetId, concept.getConceptId(), rejectId);
                            temporaryList.remove(concept);
                        }
                    } else {
                        // description refset
                        Collection<? extends I_DescriptionVersioned> descriptions = concept.getDescriptions();
                        for (I_DescriptionVersioned desc : descriptions) {
                            if (refsetHelper.hasCurrentRefsetExtension(refsetId, desc.getDescId(), rejectId)) {
                                // nothing to do
                            } else {
                                refsetHelper.newConceptExtensionPart(refsetId, desc.getDescId(), rejectId);
                                temporaryList.remove(concept);
                            }
                        }
                    }
                }

                termFactory.commit();

                model.clear();
                for (I_GetConceptData c : temporaryList) {
                    model.addElement(c);
                }

                if (temporaryList.size() == 0) {
                    returnCondition = Condition.CONTINUE;
                    done = true;
                    synchronized (InstructWithApproveRejectDone.this) {
                        InstructWithApproveRejectDone.this.notifyAll();
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            synchronized (InstructWithApproveRejectDone.this) {
                InstructWithApproveRejectDone.this.notifyAll();
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

    protected void setupButtons(final JPanel workflowPanel) {

        Box buttonBox = new Box(BoxLayout.X_AXIS);
        buttonBox.add(Box.createHorizontalGlue());

        JButton approveButton = new JButton("Approve selected");
        approveButton.setToolTipText("Approve selected");
        buttonBox.add(approveButton);
        approveButton.addActionListener(new ApproveActionListener());
        buttonBox.add(Box.createHorizontalGlue());

        if (initialPromotionStatus.equals(reviewedApprovedAdditionStatus)
            || initialPromotionStatus.equals(reviewedApprovedDeletionStatus)) {
            approveButton.setEnabled(false);
        } else {
            approveButton.setEnabled(true);
        }

        JButton rejectButton = new JButton("Disapprove selected");
        rejectButton.setToolTipText("Disapprove selected");
        buttonBox.add(rejectButton);
        rejectButton.addActionListener(new RejectActionListener());
        buttonBox.add(Box.createHorizontalGlue());
        if (initialPromotionStatus.equals(reviewedRejectedAdditionStatus)
            || initialPromotionStatus.equals(reviewedRejectedDeletionStatus)) {
            rejectButton.setEnabled(false);
        } else {
            rejectButton.setEnabled(true);
        }

        JButton doneButton = new JButton("Return to list menu");
        doneButton.setToolTipText("Return to list menu");
        buttonBox.add(doneButton);
        doneButton.addActionListener(new DoneActionListener());
        buttonBox.add(Box.createHorizontalGlue());

        // filler
        workflowBox.add(Box.createVerticalStrut(5));
        workflowBox.add(buttonBox);

        workflowPanel.add(workflowBox);
        workflowPanel.validate();
        workflowPanel.setVisible(true);

        workflowPanel.setPreferredSize(new Dimension(0, 0));
        workflowPanel.setMaximumSize(new Dimension(0, 0));
        workflowPanel.setMinimumSize(new Dimension(0, 0));
        workflowPanel.revalidate();

        workflowPanel.setPreferredSize(null);
        workflowPanel.setMaximumSize(null);
        workflowPanel.setMinimumSize(null);
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
        termFactory = Terms.get();
        this.done = false;
        config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
        refsetId =
                termFactory.getConcept(new UUID[] { (UUID) process.getProperty(memberRefsetUuidPropName) })
                    .getConceptId();
        initialPromotionStatus = termFactory.getConcept(new UUID[] { (UUID) process.getProperty(statusUuidPropName) });

        unreviewedAdditionStatus =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.getUids());
        unreviewedDeletionStatus =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION.getUids());
        reviewedApprovedAdditionStatus =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION.getUids());
        reviewedApprovedDeletionStatus =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION.getUids());
        reviewedRejectedAdditionStatus =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION.getUids());
        reviewedRejectedDeletionStatus =
                termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION.getUids());

        if (initialPromotionStatus.equals(unreviewedAdditionStatus)
            || initialPromotionStatus.equals(reviewedApprovedAdditionStatus)
            || initialPromotionStatus.equals(reviewedRejectedAdditionStatus)) {
            approveId = reviewedApprovedAdditionStatus.getConceptId();
            rejectId = reviewedRejectedAdditionStatus.getConceptId();
        } else if (initialPromotionStatus.equals(unreviewedDeletionStatus)
            || initialPromotionStatus.equals(reviewedApprovedDeletionStatus)
            || initialPromotionStatus.equals(reviewedRejectedDeletionStatus)) {
            approveId = reviewedApprovedDeletionStatus.getConceptId();
            rejectId = reviewedRejectedDeletionStatus.getConceptId();
        }

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

            workflowBox = new Box(BoxLayout.Y_AXIS);
            Box labelBox = new Box(BoxLayout.X_AXIS);
            try {
                labelBox.add(new JLabel(instruction + " " + initialPromotionStatus.getInitialText() + "(s)"));
            } catch (IOException e) {
                labelBox.add(new JLabel(instruction));
            }
            labelBox.add(Box.createHorizontalGlue());
            workflowBox.add(labelBox);

            setupButtons(workflowPanel);
        }

    }

    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            refsetHelper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
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

    public String getMemberRefsetUuidPropName() {
        return memberRefsetUuidPropName;
    }

    public void setMemberRefsetUuidPropName(String memberRefsetUuidPropName) {
        this.memberRefsetUuidPropName = memberRefsetUuidPropName;
    }

    public String getStatusUuidPropName() {
        return statusUuidPropName;
    }

    public void setStatusUuidPropName(String statusUuidPropName) {
        this.statusUuidPropName = statusUuidPropName;
    }
}
