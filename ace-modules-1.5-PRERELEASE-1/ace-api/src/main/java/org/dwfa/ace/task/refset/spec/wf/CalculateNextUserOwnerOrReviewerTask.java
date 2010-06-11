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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.spec.SpecRefsetHelper;
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
 * Decides the next person the BP will go to. If a reviewer was selected earlier in the BP, then this will be the next
 * person. Otherwise, the BP will return to the owner.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class CalculateNextUserOwnerOrReviewerTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String nextUserPropName = ProcessAttachmentKeys.NEXT_USER.getAttachmentKey();
    private String reviewerUuidPropName = ProcessAttachmentKeys.REVIEWER_UUID.getAttachmentKey();
    private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
    private I_TermFactory termFactory;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(nextUserPropName);
        out.writeObject(reviewerUuidPropName);
        out.writeObject(ownerUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            nextUserPropName = (String) in.readObject();
            reviewerUuidPropName = (String) in.readObject();
            ownerUuidPropName = (String) in.readObject();
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
            I_GetConceptData nextUserConcept = null;

            UUID[] reviewerUuids = (UUID[]) process.getProperty(reviewerUuidPropName);
            I_GetConceptData owner = termFactory.getConcept((UUID[]) process.getProperty(ownerUuidPropName));

            HashSet<UUID> uniqueReviewerUuids = new HashSet<UUID>();
            if (reviewerUuids != null) {
                for (UUID reviewerUuid : reviewerUuids) {
                    uniqueReviewerUuids.add(reviewerUuid);
                }
            }
            uniqueReviewerUuids.remove(owner.getUids().iterator().next());
            if (uniqueReviewerUuids.size() == 0) {
                nextUserConcept = owner;
            } else {
                for (UUID reviewerUuid : uniqueReviewerUuids) {
                    nextUserConcept = termFactory.getConcept(new UUID[] { reviewerUuid });
                    break; // for now, there is only a single reviewer allowed
                }
            }

            SpecRefsetHelper helper = new SpecRefsetHelper();
            String inboxAddress = helper.getInbox(nextUserConcept);

            if (inboxAddress == null) {
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    "Refset wizard cannot be completed. The selected user has no assigned inbox.", "",
                    JOptionPane.ERROR_MESSAGE);
                return Condition.ITEM_CANCELED;
            }
            process.setDestination(inboxAddress);
            process.setProperty(nextUserPropName, inboxAddress);

            if (nextUserConcept.equals(owner)) {
                return Condition.PREVIOUS;
            } else {
                return Condition.CONTINUE;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new TaskFailedException(e.getMessage());
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.PREVIOUS_CONTINUE_CANCEL;
    }

    public String getReviewerUuidPropName() {
        return reviewerUuidPropName;
    }

    public void setReviewerUuidPropName(String reviewerUuidPropName) {
        this.reviewerUuidPropName = reviewerUuidPropName;
    }

    public String getOwnerUuidPropName() {
        return ownerUuidPropName;
    }

    public void setOwnerUuidPropName(String ownerUuidPropName) {
        this.ownerUuidPropName = ownerUuidPropName;
    }

    public String getNextUserPropName() {
        return nextUserPropName;
    }

    public void setNextUserPropName(String nextUserPropName) {
        this.nextUserPropName = nextUserPropName;
    }
}
