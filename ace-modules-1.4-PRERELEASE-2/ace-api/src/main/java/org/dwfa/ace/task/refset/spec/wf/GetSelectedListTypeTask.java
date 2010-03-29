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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
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
 * Gets the selected list type from the WF panel.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class GetSelectedListTypeTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String memberRefsetUuidPropName = ProcessAttachmentKeys.PROMOTION_UUID.getAttachmentKey();
    private String statusUuidPropName = ProcessAttachmentKeys.STATUS_UUID.getAttachmentKey();
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private I_TermFactory termFactory;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(memberRefsetUuidPropName);
        out.writeObject(statusUuidPropName);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            memberRefsetUuidPropName = (String) in.readObject();
            statusUuidPropName = (String) in.readObject();
            profilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            termFactory = LocalVersionedTerminology.get();

            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
            for (Component c : workflowDetailsSheet.getComponents()) {
                if (SelectPromotionListPanel.class.isAssignableFrom(c.getClass())) {
                    SelectPromotionListPanel panel = (SelectPromotionListPanel) c;

                    I_GetConceptData selectedPromotionStatus = panel.getSelectedPromotionStatus();
                    if (selectedPromotionStatus != null) {
                        process.setProperty(statusUuidPropName, selectedPromotionStatus.getUids().iterator().next());
                    } else {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "The current refset has no members and therefore no promotion members to review.", "",
                            JOptionPane.INFORMATION_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }

                    I_GetConceptData refsetSpecConcept = termFactory.getActiveAceFrameConfig()
                        .getRefsetSpecInSpecEditor();

                    if (refsetSpecConcept != null) {
                        RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpecConcept);
                        I_GetConceptData promotionsRefset = refsetSpecHelper.getPromotionRefsetConcept();
                        if (promotionsRefset != null) {
                            process.setProperty(memberRefsetUuidPropName, promotionsRefset.getUids().iterator().next());
                        } else {
                            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                "Promotion wizard cannot be completed. Error : promotions refset is null", "",
                                JOptionPane.ERROR_MESSAGE);
                            return Condition.ITEM_CANCELED;
                        }
                    } else {
                        JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                            "Promotion wizard cannot be completed. Error : refset spec concept is null", "",
                            JOptionPane.ERROR_MESSAGE);
                        return Condition.ITEM_CANCELED;
                    }

                }
            }
            return Condition.ITEM_COMPLETE;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TaskFailedException(e.getMessage());
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
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

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

}
