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
package org.dwfa.ace.task.refset.refresh;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpecWizardTask;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.bpa.tasks.util.FileContent;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This task collects the Refresh Refset Spec Params data entered on the
 * PanelRefsetAndParameters panel currently displayed in the Workflow
 * Details Sheet and verifies that the required data has been filled in.
 *
 * @author Perry Reid
 * @version 1.0, November 2009
 *
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class GetRefreshRefsetSpecParamsPanelDataTask extends AbstractTask {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 3;

    // Task Attribute Properties
    private String profilePropName = ProcessAttachmentKeys.CURRENT_PROFILE.getAttachmentKey();
    private String editorInboxPropName = ProcessAttachmentKeys.EDITOR_INBOX.getAttachmentKey();
    private String refsetUuidPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String editorUuidPropName = ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey();
    private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
    private String fileAttachmentsPropName = ProcessAttachmentKeys.FILE_ATTACHMENTS.getAttachmentKey();
    private String ownerInboxPropName = ProcessAttachmentKeys.OWNER_INBOX.getAttachmentKey();
    private String reviewerUuidPropName = ProcessAttachmentKeys.REVIEWER_UUID.getAttachmentKey();
    private String reviewerInboxPropName = ProcessAttachmentKeys.REVIEWER_INBOX.getAttachmentKey();

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
        out.writeObject(editorInboxPropName);
        out.writeObject(commentsPropName);
        out.writeObject(refsetUuidPropName);
        out.writeObject(editorUuidPropName);
        out.writeObject(ownerUuidPropName);
        out.writeObject(fileAttachmentsPropName);
        out.writeObject(ownerInboxPropName);
        out.writeObject(reviewerUuidPropName);
        out.writeObject(reviewerInboxPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
                profilePropName = (String) in.readObject();
                editorInboxPropName = (String) in.readObject();
                commentsPropName = (String) in.readObject();
                refsetUuidPropName = (String) in.readObject();
                editorUuidPropName = (String) in.readObject();
                ownerUuidPropName = (String) in.readObject();
                fileAttachmentsPropName = (String) in.readObject();
            }
            if (objDataVersion >= 2) {
                ownerInboxPropName = (String) in.readObject();
            }
            if (objDataVersion >= 3) {
                reviewerUuidPropName = (String) in.readObject();
                reviewerInboxPropName = (String) in.readObject();
            }
            // Initialize transient properties

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * Handles actions required by the task after normal task completion (such
     * as moving a
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
     * Performs the primary action of the task, which in this case is to gather
     * and
     * validate data that has been entered by the user on the Workflow Details
     * Sheet.
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
            termFactory = LocalVersionedTerminology.get();

            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();

            for (Component c : workflowDetailsSheet.getComponents()) {
                if (PanelRefsetAndParameters.class.isAssignableFrom(c.getClass())) {
                    PanelRefsetAndParameters panel = (PanelRefsetAndParameters) c;

                    // ---------------------------------------------
                    // Retrieve values from the panel / environment
                    // ---------------------------------------------
                    I_GetConceptData refset = panel.getRefset();
                    String comments = panel.getComments();
                    HashSet<File> fileAttachments = panel.getAttachments();
                    I_GetConceptData owner = config.getDbConfig().getUserConcept();

                    // -------------------------------------------------------------------------
                    // VERIFY ALL REQUIRED FIELDS AND STORE THE ENTERED DATA
                    // INTO PROPERTY KEYS
                    // -------------------------------------------------------------------------

                    // -----------------------------------------
                    // Refset Field is required!
                    // -----------------------------------------
                    if (refset == null) {
                        // Warn the user that Refset is required.
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "You must select a refset. ",
                            "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    } else {
                        // Set the Refset property
                        process.setSubject("Refresh Refset : " + refset.getInitialText());
                        process.setName("Refresh Refset : " + refset.getInitialText());
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
                    // Originator
                    // -----------------------------------------
                    process.setOriginator(config.getUsername());

                    // -----------------------------------------
                    // Owner
                    // -----------------------------------------
                    process.setProperty(ownerUuidPropName, owner.getUids().iterator().next());

                    // Set the Owner's Inbox for future reference
                    RefsetSpecWizardTask wizard = new RefsetSpecWizardTask();
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
                    // File attachments
                    // -----------------------------------------
                    process.setProperty(fileAttachmentsPropName, fileAttachments);

                    // Under normal conditions this is where we should return
                    // from
                    return Condition.ITEM_COMPLETE;

                }
            }

            // If we got here we could not find the PanelRefsetAndParameters
            // panel
            // so warn the user and cancel the task.
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "Could not locate the 'PanelRefsetAndParameters' panel. \n " + "Canceling the task. ", "",
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
     * This method implements the interface method specified by: getConditions()
     * in I_DefineTask
     *
     * @return The possible evaluation conditions for this task.
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public String getEditorInboxPropName() {
        return editorInboxPropName;
    }

    public void setEditorInboxPropName(String editorInboxPropName) {
        this.editorInboxPropName = editorInboxPropName;
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

    public String getEditorUuidPropName() {
        return editorUuidPropName;
    }

    public void setEditorUuidPropName(String editorUuidPropName) {
        this.editorUuidPropName = editorUuidPropName;
    }

    public String getOwnerUuidPropName() {
        return ownerUuidPropName;
    }

    public void setOwnerUuidPropName(String ownerUuidPropName) {
        this.ownerUuidPropName = ownerUuidPropName;
    }

    public String getFileAttachmentsPropName() {
        return fileAttachmentsPropName;
    }

    public void setFileAttachmentsPropName(String fileAttachmentsPropName) {
        this.fileAttachmentsPropName = fileAttachmentsPropName;
    }

    public String getOwnerInboxPropName() {
        return ownerInboxPropName;
    }

    public void setOwnerInboxPropName(String ownerInboxPropName) {
        this.ownerInboxPropName = ownerInboxPropName;
    }

}
