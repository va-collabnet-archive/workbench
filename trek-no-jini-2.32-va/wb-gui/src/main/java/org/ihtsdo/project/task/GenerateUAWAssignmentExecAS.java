/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The Class GenerateUAWAssignmentExecAS.
 *
 * @author ALO
 * @version 1.0, June 2010
 */
@BeanList(specs = {
    @Spec(directory = "tasks/translation tasks", type = BeanType.TASK_BEAN)})
public class GenerateUAWAssignmentExecAS extends AbstractTask {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    // Serialization Properties
    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The Constant dataVersion.
     */
    private static final int dataVersion = 2;
    // Task Attribute Properties
    /**
     * The profile prop name.
     */
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    /**
     * The member prop name.
     */
    private String memberPropName = ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey();
    /**
     * The project prop name.
     */
    private String projectPropName = ProcessAttachmentKeys.TERMINOLOGY_PROJECT.getAttachmentKey();
    /**
     * The process prop name.
     */
    private String processPropName = ProcessAttachmentKeys.PROCESS_TO_LAUNCH.getAttachmentKey();
    /**
     * The translator inbox prop name.
     */
    private String translatorInboxPropName = ProcessAttachmentKeys.TRANSLATOR_ROLE_INBOX.getAttachmentKey();
    /**
     * The fast track translator inbox prop name.
     */
    private String fastTrackTranslatorInboxPropName = ProcessAttachmentKeys.FAST_TRACK_TRANSLATOR_ROLE_INBOX.getAttachmentKey();
    /**
     * The reviewer1 inbox prop name.
     */
    private String reviewer1InboxPropName = ProcessAttachmentKeys.REVIEWER_1_ROLE_INBOX.getAttachmentKey();
    /**
     * The reviewer2 inbox prop name.
     */
    private String reviewer2InboxPropName = ProcessAttachmentKeys.REVIEWER_2_ROLE_INBOX.getAttachmentKey();
    /**
     * The sme inbox prop name.
     */
    private String smeInboxPropName = ProcessAttachmentKeys.SME_ROLE_INBOX.getAttachmentKey();
    /**
     * The super sme inbox prop name.
     */
    private String superSmeInboxPropName = ProcessAttachmentKeys.SUPER_SME_ROLE_INBOX.getAttachmentKey();
    /**
     * The editorial board inbox prop name.
     */
    private String editorialBoardInboxPropName = ProcessAttachmentKeys.EDITORIAL_BOARD_ROLE_INBOX.getAttachmentKey();
    // Other Properties
    /**
     * The term factory.
     */
    private I_TermFactory termFactory;
    /**
     * The SEL f_ assig n_ key.
     */
    private String SELF_ASSIGN_KEY = "SELF_ASSIGN";
    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */

    /**
     * Write object.
     *
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(memberPropName);
        out.writeObject(projectPropName);
        out.writeObject(processPropName);
        out.writeObject(translatorInboxPropName);
        out.writeObject(fastTrackTranslatorInboxPropName);
        out.writeObject(reviewer1InboxPropName);
        out.writeObject(reviewer2InboxPropName);
        out.writeObject(smeInboxPropName);
        out.writeObject(superSmeInboxPropName);
        out.writeObject(editorialBoardInboxPropName);
    }

    /**
     * Read object.
     *
     * @param in the in
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
                profilePropName = (String) in.readObject();
                memberPropName = (String) in.readObject();
                projectPropName = (String) in.readObject();
                processPropName = (String) in.readObject();
                translatorInboxPropName = (String) in.readObject();
                fastTrackTranslatorInboxPropName = (String) in.readObject();
                reviewer1InboxPropName = (String) in.readObject();
                reviewer2InboxPropName = (String) in.readObject();
                smeInboxPropName = (String) in.readObject();
                superSmeInboxPropName = (String) in.readObject();
                editorialBoardInboxPropName = (String) in.readObject();
            }
            // Initialize transient properties
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * Handles actions required by the task after normal task completion (such
     * as moving a process to another user's input queue).
     *
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @return void
     * @throws TaskFailedException Thrown if a task fails for any reason.
     * @see
     * org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     * org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    /**
     * Performs the primary action of the task, which in this case is to gather
     * and validate data that has been entered by the user on the Workflow
     * Details Sheet.
     *
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @return The exit condition of the task
     * @throws TaskFailedException Thrown if a task fails for any reason.
     * @see
     * org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     * org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
       throw new UnsupportedOperationException("TODO: Jini Removal");
 
        
    }

    /**
     * This method overrides: getDataContainerIds() in AbstractTask.
     *
     * @return The data container identifiers used by this task.
     */
    public int[] getDataContainerIds() {
        return new int[]{};
    }

    /**
     * This method implements the interface method specified by: getConditions()
     * in I_DefineTask.
     *
     * @return The possible evaluation conditions for this task.
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * Gets the profile prop name.
     *
     * @return the profile prop name
     */
    public String getProfilePropName() {
        return profilePropName;
    }

    /**
     * Sets the profile prop name.
     *
     * @param profilePropName the new profile prop name
     */
    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    /**
     * Gets the member prop name.
     *
     * @return the member prop name
     */
    public String getMemberPropName() {
        return memberPropName;
    }

    /**
     * Sets the member prop name.
     *
     * @param memberPropName the new member prop name
     */
    public void setMemberPropName(String memberPropName) {
        this.memberPropName = memberPropName;
    }

    /**
     * Gets the process prop name.
     *
     * @return the process prop name
     */
    public String getProcessPropName() {
        return processPropName;
    }

    /**
     * Sets the process prop name.
     *
     * @param processPropName the new process prop name
     */
    public void setProcessPropName(String processPropName) {
        this.processPropName = processPropName;
    }

    /**
     * Gets the translator inbox prop name.
     *
     * @return the translator inbox prop name
     */
    public String getTranslatorInboxPropName() {
        return translatorInboxPropName;
    }

    /**
     * Sets the translator inbox prop name.
     *
     * @param translatorInboxPropName the new translator inbox prop name
     */
    public void setTranslatorInboxPropName(String translatorInboxPropName) {
        this.translatorInboxPropName = translatorInboxPropName;
    }

    /**
     * Gets the reviewer1 inbox prop name.
     *
     * @return the reviewer1 inbox prop name
     */
    public String getReviewer1InboxPropName() {
        return reviewer1InboxPropName;
    }

    /**
     * Sets the reviewer1 inbox prop name.
     *
     * @param reviewer1InboxPropName the new reviewer1 inbox prop name
     */
    public void setReviewer1InboxPropName(String reviewer1InboxPropName) {
        this.reviewer1InboxPropName = reviewer1InboxPropName;
    }

    /**
     * Gets the reviewer2 inbox prop name.
     *
     * @return the reviewer2 inbox prop name
     */
    public String getReviewer2InboxPropName() {
        return reviewer2InboxPropName;
    }

    /**
     * Sets the reviewer2 inbox prop name.
     *
     * @param reviewer2InboxPropName the new reviewer2 inbox prop name
     */
    public void setReviewer2InboxPropName(String reviewer2InboxPropName) {
        this.reviewer2InboxPropName = reviewer2InboxPropName;
    }

    /**
     * Gets the sme inbox prop name.
     *
     * @return the sme inbox prop name
     */
    public String getSmeInboxPropName() {
        return smeInboxPropName;
    }

    /**
     * Sets the sme inbox prop name.
     *
     * @param smeInboxPropName the new sme inbox prop name
     */
    public void setSmeInboxPropName(String smeInboxPropName) {
        this.smeInboxPropName = smeInboxPropName;
    }

    /**
     * Gets the editorial board inbox prop name.
     *
     * @return the editorial board inbox prop name
     */
    public String getEditorialBoardInboxPropName() {
        return editorialBoardInboxPropName;
    }

    /**
     * Sets the editorial board inbox prop name.
     *
     * @param editorialBoardInboxPropName the new editorial board inbox prop
     * name
     */
    public void setEditorialBoardInboxPropName(String editorialBoardInboxPropName) {
        this.editorialBoardInboxPropName = editorialBoardInboxPropName;
    }

    /**
     * Gets the project prop name.
     *
     * @return the project prop name
     */
    public String getProjectPropName() {
        return projectPropName;
    }

    /**
     * Sets the project prop name.
     *
     * @param projectPropName the new project prop name
     */
    public void setProjectPropName(String projectPropName) {
        this.projectPropName = projectPropName;
    }

    /**
     * Gets the fast track translator inbox prop name.
     *
     * @return the fast track translator inbox prop name
     */
    public String getFastTrackTranslatorInboxPropName() {
        return fastTrackTranslatorInboxPropName;
    }

    /**
     * Sets the fast track translator inbox prop name.
     *
     * @param fastTrackTranslatorInboxPropName the new fast track translator
     * inbox prop name
     */
    public void setFastTrackTranslatorInboxPropName(
            String fastTrackTranslatorInboxPropName) {
        this.fastTrackTranslatorInboxPropName = fastTrackTranslatorInboxPropName;
    }

    /**
     * Gets the super sme inbox prop name.
     *
     * @return the super sme inbox prop name
     */
    public String getSuperSmeInboxPropName() {
        return superSmeInboxPropName;
    }

    /**
     * Sets the super sme inbox prop name.
     *
     * @param superSmeInboxPropName the new super sme inbox prop name
     */
    public void setSuperSmeInboxPropName(String superSmeInboxPropName) {
        this.superSmeInboxPropName = superSmeInboxPropName;
    }
}