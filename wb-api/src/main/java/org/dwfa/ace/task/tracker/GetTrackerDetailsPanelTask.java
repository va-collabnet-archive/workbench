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
package org.dwfa.ace.task.tracker;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
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
 * This task collects the Tracker details data entered on the
 * TrackerDetails panel currently displayed in the Workflow
 * Details Sheet and verifies that the required data has been filled in.
 * 
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf/sme", type = BeanType.TASK_BEAN) })
public class GetTrackerDetailsPanelTask extends AbstractTask {

    // Serialization Properties
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    // Task Attribute Properties
    private String profilePropName = ProcessAttachmentKeys.CURRENT_PROFILE.getAttachmentKey();
    private String artfIDPropName = ProcessAttachmentKeys.TRACKER_ID.getAttachmentKey();
    private String name1PropName = ProcessAttachmentKeys.NAME1.getAttachmentKey();
    private String name2PropName = ProcessAttachmentKeys.NAME2.getAttachmentKey();

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
        out.writeObject(artfIDPropName);
        out.writeObject(name1PropName);
        out.writeObject(name2PropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
                profilePropName = (String) in.readObject();
                artfIDPropName = (String) in.readObject();
                name1PropName = (String) in.readObject();
                name2PropName = (String) in.readObject();
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
                if (TrackerDetailsPanel.class.isAssignableFrom(c.getClass())) {
                    TrackerDetailsPanel panel = (TrackerDetailsPanel) c;

                    String artfID = panel.getArtfID();
                    String name1 = panel.getName1();
                    String name2 = panel.getName2();

                    // -----------------------------------------
                    // artf ID is required
                    // -----------------------------------------
                    if (artfID == null) {
                        // Warn the user that Refset is required.
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must enter an ARTF ID. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    } else {
                        // Set the artf ID property
                        process.setProperty(artfIDPropName, artfID);
                    }

                    // -----------------------------------------
                    // name1 is required
                    // -----------------------------------------
                    if (name1 == null) {
                        // Warn the user that Refset is required.
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must enter all name data. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    } else {
                        // Set the name1 property
                        process.setProperty(name1PropName, name1);
                    }

                    // -----------------------------------------
                    // name2 is required
                    // -----------------------------------------
                    if (name2 == null) {
                        // Warn the user that Refset is required.
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "You must enter all name data. ", "", JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    } else {
                        // Set the name2 property
                        process.setProperty(name2PropName, name2);
                    }

                    // Under normal conditions this is where we should return from
                    return Condition.ITEM_COMPLETE;

                }
            }

            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "Could not locate the 'TrackerDetailsPanel' panel. \n " + "Canceling the task. ", "",
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

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getArtfIDPropName() {
        return artfIDPropName;
    }

    public void setArtfIDPropName(String artfIDPropName) {
        this.artfIDPropName = artfIDPropName;
    }

    public String getName1PropName() {
        return name1PropName;
    }

    public void setName1PropName(String name1PropName) {
        this.name1PropName = name1PropName;
    }

    public String getName2PropName() {
        return name2PropName;
    }

    public void setName2PropName(String name2PropName) {
        this.name2PropName = name2PropName;
    }

}