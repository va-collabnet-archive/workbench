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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.profile.CreateUserPathAndQueues;
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
 * This task collects the data on the CollabNetQueuePanel currently displayed in
 * the Workflow Details Sheet and verifies that the required data has been
 * filled in.
 * 
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf/sme", type = BeanType.TASK_BEAN) })
public class GetCollabNetQueuePanelDataTask extends AbstractTask {

    // Serialization Properties
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    // Task Attribute Properties
    private String profilePropName = ProcessAttachmentKeys.CURRENT_PROFILE.getAttachmentKey();

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
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
                profilePropName = (String) in.readObject();
            }

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * Handles actions required by the task after normal task completion (such
     * as moving a process to another user's input queue).
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
     * and validate data that has been entered by the user on the Workflow
     * Details
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
            termFactory = Terms.get();

            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();

            for (Component c : workflowDetailsSheet.getComponents()) {
                if (NewCollabNetQueuePanel.class.isAssignableFrom(c.getClass())) {
                    NewCollabNetQueuePanel panel = (NewCollabNetQueuePanel) c;

                    I_GetConceptData workbenchUser = panel.getWorkbenchUser();
                    String collabnetUserPassword = panel.getCollabnetUserPassword();
                    String collabnetUserName = panel.getCollabnetUserName();

                    // -----------------------------------------
                    // workbenchUser is required
                    // -----------------------------------------
                    if (workbenchUser == null) {
                        // Warn the user that Refset is required.
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select a Workbench User. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }

                    // -----------------------------------------
                    // collabnetUserName Field is required
                    // -----------------------------------------
                    if (collabnetUserName == null) {
                        // Warn the user that Refset is required.
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must select a Collabnet user. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }

                    // -----------------------------------------
                    // collabnetUserPassword
                    // -----------------------------------------
                    if (collabnetUserPassword == null) {
                        // Warn the user that Refset is required.
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must enter a Collabnet user password. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }

                    CreateUserPathAndQueues queueCreator = new CreateUserPathAndQueues();
                    File userQueueRoot = new File("queues");

                    // Create CollabNet inbox
                    boolean validInbox =
                            queueCreator.createCollabnetInbox(config, workbenchUser.getInitialText() + "-collabnet.inbox",
                                userQueueRoot, workbenchUser.getInitialText() + "-collabnet.inbox", workbenchUser
                                    .getInitialText()
                                    + "-collabnet.inbox", collabnetUserName, collabnetUserPassword);
                    if (!validInbox) {
                        return Condition.ITEM_CANCELED;
                    }

                    // Create CollabNet outbox
                    boolean validOutbox =
                            queueCreator.createCollabnetOutbox(config, workbenchUser.getInitialText() + "-collabnet.outbox",
                                userQueueRoot, workbenchUser.getInitialText() + "-collabnet.outbox", workbenchUser
                                    .getInitialText()
                                    + "-collabnet.outbox", collabnetUserName, collabnetUserPassword);

                    if (!validOutbox) {
                        return Condition.ITEM_CANCELED;
                    }
                    String userDirStr = "profiles" + File.separator + workbenchUser.getInitialText();
                    File userDir = new File(userDirStr);

                    File profileFile = new File(userDir, workbenchUser.getInitialText() + ".ace");

                    // read old profile from disk
                    FileInputStream fis = new FileInputStream(profileFile);

                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    I_ConfigAceDb modifiedProfile = (I_ConfigAceDb) ois.readObject();
                    ois.close();

                    List<I_ConfigAceFrame> configs = modifiedProfile.getAceFrames();

                    for (I_ConfigAceFrame modifiedProfileConfig : configs) {
                        modifiedProfileConfig.getQueueAddressesToShow().add(
                            workbenchUser.getInitialText() + "-collabnet.inbox");
                        modifiedProfileConfig.getQueueAddressesToShow().add(
                            workbenchUser.getInitialText() + "-collabnet.outbox");

                    }

                    if (configs.size() >= 1) {
                        process.setProperty(profilePropName, configs.get(0));
                    }

                    return Condition.ITEM_COMPLETE;

                }
            }

            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "Could not locate the 'NewCollabNetQueuePanel' panel. \n " + "Canceling the task. ", "",
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

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }
}