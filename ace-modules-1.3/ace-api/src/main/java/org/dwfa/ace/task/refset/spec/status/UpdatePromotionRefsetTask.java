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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.spec.SpecRefsetHelper;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Takes a refset spec as input and creates/updates an associated promotion
 * refset, used to track the refset member's status over time.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class UpdatePromotionRefsetTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String refsetSpecUuidPropName = ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey();
    private I_TermFactory termFactory;
    private I_GetConceptData currentStatusConcept;
    private I_GetConceptData retiredStatusConcept;
    private I_GetConceptData unreviewedStatusConcept;
    private I_GetConceptData promotedStatusConcept;
    private I_GetConceptData readyToPromoteStatusConcept;
    private I_GetConceptData activeStatusConcept;
    private I_GetConceptData promotionConcept;
    private I_GetConceptData memberRefsetConcept;
    private I_GetConceptData unreviewedAdditionStatus;
    private I_GetConceptData unreviewedDeletionStatus;
    private I_GetConceptData reviewedApprovedDeletionStatus;
    private I_GetConceptData reviewedApprovedAdditionStatus;
    private I_GetConceptData reviewedRejectedDeletionStatus;
    private I_GetConceptData reviewedRejectedAdditionStatus;

    private transient Exception ex = null;
    private transient Condition returnCondition = Condition.ITEM_COMPLETE;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(refsetSpecUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            refsetSpecUuidPropName = (String) in.readObject();
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

            termFactory = LocalVersionedTerminology.get();
            currentStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            retiredStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
            unreviewedStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids());
            readyToPromoteStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE.getUids());
            promotedStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PROMOTED.getUids());
            activeStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());

            UUID refsetSpecUuid = (UUID) process.readProperty(refsetSpecUuidPropName);
            if (refsetSpecUuid == null) {
                throw new Exception("No refset spec currently in refset spec panel.");
            }
            I_GetConceptData refsetSpecConcept = termFactory.getConcept(new UUID[] { refsetSpecUuid });
            RefsetSpec refsetSpec = new RefsetSpec(refsetSpecConcept);

            memberRefsetConcept = refsetSpec.getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                throw new Exception("Unable to find member refset.");
            }
            promotionConcept = refsetSpec.getPromotionRefsetConcept();
            if (promotionConcept == null) {
                throw new Exception("Unable to find promotion refset.");
            }

            unreviewedAdditionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.getUids());
            unreviewedDeletionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION.getUids());
            reviewedApprovedAdditionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION.getUids());
            reviewedApprovedDeletionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION.getUids());
            reviewedRejectedAdditionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION.getUids());
            reviewedRejectedDeletionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION.getUids());

            List<I_ThinExtByRefVersioned> memberExtensions = termFactory.getRefsetExtensionMembers(memberRefsetConcept.getConceptId());
            List<I_ThinExtByRefVersioned> promotionExtensions = termFactory.getRefsetExtensionMembers(promotionConcept.getConceptId());

            updatePromotionsRefset(memberExtensions, promotionExtensions);

            process.setProperty(ProcessAttachmentKeys.PROMOTION_UUID.getAttachmentKey(), termFactory.getUids(
                promotionConcept.getConceptId()).iterator().next());

            termFactory.commit();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "Promotion wizard cannot be completed. Error : " + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            returnCondition = Condition.ITEM_CANCELED;
            return;
        }

        returnCondition = Condition.ITEM_COMPLETE;
    }

    private void updatePromotionsRefset(List<I_ThinExtByRefVersioned> memberExtensions,
            List<I_ThinExtByRefVersioned> promotionExtensions) throws Exception {
        SpecRefsetHelper refsetHelper = new SpecRefsetHelper();
        for (I_ThinExtByRefVersioned memberExtension : memberExtensions) {
            I_ThinExtByRefPart latestMemberPart = getLatestPart(memberExtension);
            I_ThinExtByRefVersioned promotionExtension = getExtensionByComponent(memberExtension.getComponentId(),
                promotionExtensions);
            I_GetConceptData promotionStatus = null;
            if (promotionExtension != null) {
                promotionStatus = getPromotionStatus(promotionExtension);
            }
            if (latestMemberPart == null) {
                throw new Exception("Member extension exists with no parts.");
            } else {
                if (latestMemberPart.getStatusId() == currentStatusConcept.getConceptId()
                    || latestMemberPart.getStatusId() == unreviewedStatusConcept.getConceptId()
                    || latestMemberPart.getStatusId() == readyToPromoteStatusConcept.getConceptId()
                    || latestMemberPart.getStatusId() == promotedStatusConcept.getConceptId()
                    || latestMemberPart.getStatusId() == activeStatusConcept.getConceptId()) {
                    if (promotionStatus == null) {
                        // add a new promotion refset member with value
                        // unreviewed addition
                        refsetHelper.newRefsetExtension(promotionConcept.getConceptId(),
                            memberExtension.getComponentId(), unreviewedAdditionStatus.getConceptId());
                    } else if (promotionStatus.equals(unreviewedAdditionStatus)
                        || promotionStatus.equals(reviewedApprovedAdditionStatus)
                        || promotionStatus.equals(reviewedRejectedAdditionStatus)) {
                        // no change
                    } else if (promotionStatus.equals(unreviewedDeletionStatus)
                        || promotionStatus.equals(reviewedApprovedDeletionStatus)
                        || promotionStatus.equals(reviewedRejectedDeletionStatus)) {
                        // refsetHelper.retireConceptExtension(promotionConcept.getConceptId(),
                        // memberExtension
                        // .getComponentId());
                        refsetHelper.newConceptExtensionPart(promotionConcept.getConceptId(),
                            memberExtension.getComponentId(), unreviewedAdditionStatus.getConceptId(),
                            currentStatusConcept.getConceptId());
                    }

                } else if (latestMemberPart.getStatusId() == retiredStatusConcept.getConceptId()) {
                    if (promotionStatus == null) {
                        // add a new promotion refset member with value
                        // unreviewed deletion
                        refsetHelper.newRefsetExtension(promotionConcept.getConceptId(),
                            memberExtension.getComponentId(), unreviewedDeletionStatus.getConceptId());
                    } else if (promotionStatus.equals(unreviewedAdditionStatus)
                        || promotionStatus.equals(reviewedApprovedAdditionStatus)
                        || promotionStatus.equals(reviewedRejectedAdditionStatus)) {
                        // refsetHelper.retireConceptExtension(promotionConcept.getConceptId(),
                        // memberExtension
                        // .getComponentId());
                        refsetHelper.newConceptExtensionPart(promotionConcept.getConceptId(),
                            memberExtension.getComponentId(), unreviewedDeletionStatus.getConceptId(),
                            currentStatusConcept.getConceptId());
                    } else if (promotionStatus.equals(unreviewedDeletionStatus)
                        || promotionStatus.equals(reviewedApprovedDeletionStatus)
                        || promotionStatus.equals(reviewedRejectedDeletionStatus)) {
                        // no change
                    }
                } else {
                    throw new Exception("Don't know how to handle status : "
                        + termFactory.getConcept(latestMemberPart.getStatusId()).getInitialText());
                }
            }
        }
    }

    private I_GetConceptData getPromotionStatus(I_ThinExtByRefVersioned promotionExtension) throws Exception {
        I_ThinExtByRefPart latestPart = getLatestPart(promotionExtension);
        if (latestPart == null) {
            return null;
        } else {
            if (latestPart instanceof I_ThinExtByRefPartConcept) {
                I_ThinExtByRefPartConcept latestConceptPart = (I_ThinExtByRefPartConcept) latestPart;
                return termFactory.getConcept(latestConceptPart.getC1id());
            } else {
                throw new Exception("Don't know how to handle promotion ext of type : " + latestPart);
            }
        }
    }

    private I_ThinExtByRefVersioned getExtensionByComponent(int componentId, List<I_ThinExtByRefVersioned> extensions) {
        for (I_ThinExtByRefVersioned extension : extensions) {
            if (extension.getComponentId() == componentId) {
                return extension;
            }
        }
        return null;
    }

    private I_ThinExtByRefPart getLatestPart(I_ThinExtByRefVersioned memberExtension) {
        I_ThinExtByRefPart latestPart = null;
        for (I_ThinExtByRefPart part : memberExtension.getVersions()) {
            if ((latestPart == null) || (part.getVersion() >= latestPart.getVersion())) {
                latestPart = part;
            }
        }
        return latestPart;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_CANCELED_OR_COMPLETE;
    }

    public String getRefsetSpecUuidPropName() {
        return refsetSpecUuidPropName;
    }

    public void setRefsetSpecUuidPropName(String refsetSpecUuidPropName) {
        this.refsetSpecUuidPropName = refsetSpecUuidPropName;
    }
}
