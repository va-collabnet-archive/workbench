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
package org.dwfa.ace.task.refset.spec.status;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
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
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Takes a promotion refset and promotion status as input, and adds all of the
 * associated members to the list view.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class AddPromotionConceptsToListViewTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String memberRefsetUuidPropName = ProcessAttachmentKeys.PROMOTION_UUID.getAttachmentKey();
    private String statusUuidPropName = ProcessAttachmentKeys.STATUS_UUID.getAttachmentKey();
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private I_TermFactory termFactory;

    private transient Exception ex = null;
    private transient Condition returnCondition = Condition.ITEM_COMPLETE;

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

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

        try {
            ex = null;
            if (SwingUtilities.isEventDispatchThread()) {
                doRun(process, worker);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        doRun(process, worker);
                    }
                });
            }
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        if (ex != null) {
            throw new TaskFailedException(ex);
        }
        return returnCondition;
    }

    public void doRun(final I_EncodeBusinessProcess process, I_Work worker) {

        try {

            termFactory = Terms.get();

            UUID memberRefsetUuid = (UUID) process.getProperty(memberRefsetUuidPropName);
            I_GetConceptData memberRefsetConcept = termFactory.getConcept(new UUID[] { memberRefsetUuid });

            UUID promotionStatusUuid = (UUID) process.getProperty(statusUuidPropName);
            I_GetConceptData promotionStatusConcept = termFactory.getConcept(new UUID[] { promotionStatusUuid });

            I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(getProfilePropName());
            I_HelpSpecRefset refsetHelper = Terms.get().getSpecRefsetHelper(config);
            List<I_GetConceptData> filteredConcepts =
                    refsetHelper.filterListByConceptType(termFactory.getRefsetExtensionMembers(memberRefsetConcept
                        .getConceptId()), promotionStatusConcept);

             JList conceptList = config.getBatchConceptList();
            I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
            model.clear();

            for (I_GetConceptData concept : filteredConcepts) {
                model.addElement(concept);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "Promotion wizard cannot be completed. Error : " + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            returnCondition = Condition.ITEM_CANCELED;
            return;
        }

        returnCondition = Condition.ITEM_COMPLETE;
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
