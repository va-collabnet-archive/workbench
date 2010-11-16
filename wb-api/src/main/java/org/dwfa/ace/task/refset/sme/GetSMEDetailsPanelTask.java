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
package org.dwfa.ace.task.refset.sme;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpecWizardTask;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task collects the Refset and SME data entered on the
 * SMEDetails panel currently displayed in the Workflow
 * Details Sheet and verifies that the required data has been filled in.
 *
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf/sme", type = BeanType.TASK_BEAN) })
public class GetSMEDetailsPanelTask extends AbstractTask {

    // Serialization Properties
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 4;

    // Task Attribute Properties
    private String profilePropName = ProcessAttachmentKeys.CURRENT_PROFILE.getAttachmentKey();
    private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
    private String ownerInboxPropName = ProcessAttachmentKeys.OWNER_INBOX.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private String commentsPropName = ProcessAttachmentKeys.SEND_COMMENT.getAttachmentKey();
    private String descriptionPropName = ProcessAttachmentKeys.DESCRIPTION.getAttachmentKey();
    private String sendToUserPropName = ProcessAttachmentKeys.SEND_TO_USER.getAttachmentKey();

    // Other Properties
    private I_TermFactory termFactory;

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(commentsPropName);
        out.writeObject(refsetUuidPropName);
        out.writeObject(ownerUuidPropName);
        out.writeObject(ownerInboxPropName);
        out.writeObject(descriptionPropName);
        out.writeObject(sendToUserPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
                profilePropName = (String) in.readObject();
                commentsPropName = (String) in.readObject();
                refsetUuidPropName = (String) in.readObject();
                ownerUuidPropName = (String) in.readObject();
                ownerInboxPropName = (String) in.readObject();
            }

            if (objDataVersion >= 3) {
                descriptionPropName = (String) in.readObject();
            } else {
                descriptionPropName = ProcessAttachmentKeys.DESCRIPTION.getAttachmentKey();
            }

            if (objDataVersion >= 4) {
                sendToUserPropName = (String) in.readObject();
            } else {
                sendToUserPropName = ProcessAttachmentKeys.SEND_TO_USER.getAttachmentKey();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * Handles actions required by the task after normal task completion (such as moving a
     * process to another user's input queue).
     *
     * @return void
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    /**
     * Performs the primary action of the task, which in this case is to gather and
     * validate data that has been entered by the user on the Workflow Details Sheet.
     *
     * @return The exit condition of the task
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            termFactory = Terms.get();

            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();

            for (Component c : workflowDetailsSheet.getComponents()) {
                if (SMEDetailsPanel.class.isAssignableFrom(c.getClass())) {
                    SMEDetailsPanel panel = (SMEDetailsPanel) c;

                    I_GetConceptData refset = panel.getRefset();
                    String comments = panel.getComments();
                    String smeNameRequest = panel.getSmeNameRequest();
                    I_GetConceptData owner = config.getDbConfig().getUserConcept();
                    RefsetSpecWizardTask wizard = new RefsetSpecWizardTask();

                    // Set owner as the originator
                    process.setOriginator(config.getUsername());

                    // Remember the owner's UUID
                    process.setProperty(ownerUuidPropName, new UUID[] { owner.getUids().iterator().next() });

                    // Set the Owner's Inbox for future reference
                    String ownerInboxAddress = wizard.getInbox(owner);
                    if (ownerInboxAddress == null) {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "Refresh Refset process cannot continue... The Owner has no assigned inbox : " + owner, "",
                            JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    } else {
                        process.setProperty(ownerInboxPropName, ownerInboxAddress);
                    }

                    // -----------------------------------------
                    // smeNameRequest is required
                    // -----------------------------------------
                    if (smeNameRequest == null) {
                        // Warn the user that Refset is required.
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must enter a SME Name. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    } else {
                        // Set the SME Name request property
                        process.setProperty(sendToUserPropName, smeNameRequest);
                    }

                    // -----------------------------------------
                    // Refset Field is required
                    // -----------------------------------------
                    if (refset == null) {
                        // Warn the user that Refset is required.
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "You must select a refset. ",
                            "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    } else {
                        // Set the Refset property
                        process.setSubject("SME Review " + smeNameRequest);
                        process.setName("SME Review " + smeNameRequest);
                        process.setProperty(refsetUuidPropName, refset.getUids().iterator().next());
                    }

                    // -----------------------------------------
                    // Comments
                    // -----------------------------------------
                    if (comments != null) {
                        process.setProperty(commentsPropName, comments);
                    } else {
                        process.setProperty(commentsPropName, "");
                    }

                    // -----------------------------------------
                    // Description
                    // -----------------------------------------
                    String description = refset.getInitialText() + " " + config.getEditingPathSet();
                    if (description != null) {
                        process.setProperty(descriptionPropName, description);
                    } else {
                        process.setProperty(descriptionPropName, "");
                    }

                    // Under normal conditions this is where we should return from
                    return Condition.ITEM_COMPLETE;

                }
            }

            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "Could not locate the 'SMEDetailsPanel' panel. \n " + "Canceling the task. ", "",
                JOptionPane.ERROR_MESSAGE);
            return Condition.ITEM_CANCELED;

        } catch (Exception e) {
            e.printStackTrace();
            throw new TaskFailedException(e.getMessage());
        }
    }

    /**
     * This method overrides: getDataContainerIds() in AbstractTask
     *
     * @return The data container identifiers used by this task.
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * This method implements the interface method specified by: getConditions() in I_DefineTask
     *
     * @return The possible evaluation conditions for this task.
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public String getCommentsPropName() {
        return commentsPropName;
    }

    public void setCommentsPropName(String commentsPropName) {
        this.commentsPropName = commentsPropName;
    }

    public String getRefsetUuidPropName() {
        return refsetUuidPropName;
    }

    public void setRefsetUuidPropName(String refsetUuidPropName) {
        this.refsetUuidPropName = refsetUuidPropName;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getOwnerUuidPropName() {
        return ownerUuidPropName;
    }

    public void setOwnerUuidPropName(String ownerUuidPropName) {
        this.ownerUuidPropName = ownerUuidPropName;
    }

    public String getOwnerInboxPropName() {
        return ownerInboxPropName;
    }

    public void setOwnerInboxPropName(String ownerInboxPropName) {
        this.ownerInboxPropName = ownerInboxPropName;
    }

    public String getDescriptionPropName() {
        return descriptionPropName;
    }

    public void setDescriptionPropName(String descriptionPropName) {
        this.descriptionPropName = descriptionPropName;
    }

    public String getSendToUserPropName() {
        return sendToUserPropName;
    }

    public void setSendToUserPropName(String sendToUserPropName) {
        this.sendToUserPropName = sendToUserPropName;
    }
}