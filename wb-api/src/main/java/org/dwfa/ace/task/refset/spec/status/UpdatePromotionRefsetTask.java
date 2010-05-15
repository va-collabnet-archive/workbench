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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.time.TimeUtil;

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
    private I_GetConceptData promotionRefsetConcept;
    private I_GetConceptData memberRefsetConcept;
    private I_GetConceptData unreviewedAdditionStatus;
    private I_GetConceptData unreviewedDeletionStatus;
    private I_GetConceptData reviewedApprovedDeletionStatus;
    private I_GetConceptData reviewedApprovedAdditionStatus;
    private I_GetConceptData reviewedRejectedDeletionStatus;
    private I_GetConceptData reviewedRejectedAdditionStatus;
    private I_ConfigAceFrame activeFrameConfig;

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
        AceLog.getAppLog().info("Starting " + this.getIdAndName());
        termFactory = Terms.get();

        try {
            ex = null;
            if (SwingUtilities.isEventDispatchThread()) {
                throw new TaskFailedException("This task cannot be run on the event dispatch thread. ");
            } else {
                doRun(process, worker);
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
            //TODO replace getActiveFrameConfig with a passed in value. 
            activeFrameConfig = Terms.get().getActiveAceFrameConfig();
            UUID refsetSpecUuid = (UUID) process.getProperty(refsetSpecUuidPropName);
            if (refsetSpecUuid == null) {
                throw new Exception("No refset spec currently in refset spec panel.");
            }
            I_GetConceptData refsetSpecConcept = termFactory.getConcept(new UUID[] { refsetSpecUuid });
            RefsetSpec refsetSpec = new RefsetSpec(refsetSpecConcept, activeFrameConfig);
            I_ShowActivity activity = Terms.get().newActivityPanel(true, activeFrameConfig, "Updating refset: " + 
                refsetSpec.getPromotionRefsetConcept().toString(), false);
            activity.setIndeterminate(true);
            long start = System.currentTimeMillis();

            currentStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            retiredStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
            unreviewedStatusConcept =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids());
            readyToPromoteStatusConcept =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE.getUids());
            promotedStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PROMOTED.getUids());
            activeStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());


            memberRefsetConcept = refsetSpec.getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                throw new Exception("Unable to find member refset.");
            }
            promotionRefsetConcept = refsetSpec.getPromotionRefsetConcept();
            if (promotionRefsetConcept == null) {
                throw new Exception("Unable to find promotion refset.");
            }

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

            Collection<? extends I_ExtendByRef> memberExtensions =
                    termFactory.getRefsetExtensionMembers(memberRefsetConcept.getConceptId());
            Collection<? extends I_ExtendByRef> promotionExtensions =
                    termFactory.getRefsetExtensionMembers(promotionRefsetConcept.getConceptId());

            activity.setValue(0);
            activity.setMaximum(memberExtensions.size());
            activity.setIndeterminate(false);

            updatePromotionsRefset(memberExtensions, promotionExtensions, activity, start);

            process.setProperty(ProcessAttachmentKeys.PROMOTION_UUID.getAttachmentKey(), termFactory.getUids(
                promotionRefsetConcept.getConceptId()).iterator().next());

            termFactory.commit();
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - start;
            String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
            activity.setProgressInfoLower("Elapsed: " + elapsedStr + " Processed: " + memberExtensions.size());
            activity.complete();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "Promotion wizard cannot be completed. Error : " + e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
            returnCondition = Condition.ITEM_CANCELED;
            return;
        }

        returnCondition = Condition.ITEM_COMPLETE;
    }

    // TODO make this process run in parallel...
    private void updatePromotionsRefset(Collection<? extends I_ExtendByRef> memberExtensions,
                                                   Collection<? extends I_ExtendByRef> promotionExtensions, 
                                                              I_ShowActivity activity, 
                                                              long start) throws Exception {
        I_HelpSpecRefset refsetHelper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
        refsetHelper.setAutocommitActive(false);
        int processed = 0;
        int size = memberExtensions.size();
        Map<Integer, I_ExtendByRef> promotionExtMap = new HashMap<Integer, I_ExtendByRef>(promotionExtensions.size());
        for (I_ExtendByRef promotionExtension: promotionExtensions) {
            promotionExtMap.put(promotionExtension.getComponentId(), promotionExtension);
        }

        for (I_ExtendByRef memberExtension : memberExtensions) {
            I_ExtendByRefPart latestMemberPart = getLatestPart(memberExtension);
            I_ExtendByRef promotionExtension = promotionExtMap.get(memberExtension.getNid());
            I_GetConceptData promotionStatus = null;
            if (promotionExtension != null) {
                promotionStatus = getPromotionStatus(promotionExtension);
            }
            if (latestMemberPart == null) {
                AceLog.getAppLog().warning("Member extension exists with no parts: " + memberExtension);
            } else {
                if (latestMemberPart.getStatusId() == currentStatusConcept.getConceptId()
                    || latestMemberPart.getStatusId() == unreviewedStatusConcept.getConceptId()
                    || latestMemberPart.getStatusId() == readyToPromoteStatusConcept.getConceptId()
                    || latestMemberPart.getStatusId() == promotedStatusConcept.getConceptId()
                    || latestMemberPart.getStatusId() == activeStatusConcept.getConceptId()) {
                    if (promotionStatus == null) {
                        // add a new promotion refset member with value
                        // unreviewed addition
                        refsetHelper.newRefsetExtension(promotionRefsetConcept.getConceptId(), 
                            memberExtension.getNid(), 
                            unreviewedAdditionStatus.getConceptId());
                    } else if (promotionStatus.equals(unreviewedAdditionStatus)
                        || promotionStatus.equals(reviewedApprovedAdditionStatus)
                        || promotionStatus.equals(reviewedRejectedAdditionStatus)) {
                        // no change
                    } else if (promotionStatus.equals(unreviewedDeletionStatus)
                        || promotionStatus.equals(reviewedApprovedDeletionStatus)
                        || promotionStatus.equals(reviewedRejectedDeletionStatus)) {
                        refsetHelper.newConceptExtensionPart(promotionRefsetConcept.getConceptId(), 
                            memberExtension.getNid(), 
                            unreviewedAdditionStatus.getConceptId(), 
                            currentStatusConcept.getConceptId());
                    }

                } else if (latestMemberPart.getStatusId() == retiredStatusConcept.getConceptId()) {
                    if (promotionStatus == null) {
                        // add a new promotion refset member with value
                        // unreviewed deletion
                        refsetHelper.newRefsetExtension(promotionRefsetConcept.getConceptId(), 
                            memberExtension.getNid(), 
                            unreviewedDeletionStatus.getConceptId());
                    } else if (promotionStatus.equals(unreviewedAdditionStatus)
                        || promotionStatus.equals(reviewedApprovedAdditionStatus)
                        || promotionStatus.equals(reviewedRejectedAdditionStatus)) {
                        refsetHelper.newConceptExtensionPart(promotionRefsetConcept.getConceptId(), 
                            memberExtension.getNid(), 
                            unreviewedDeletionStatus.getConceptId(), 
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
            if (processed % 50 == 0) {
                activity.setValue(processed);
                long endTime = System.currentTimeMillis();
                long elapsed = endTime - start;
                String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
                String remainingStr = TimeUtil.getRemainingTimeString(processed, size, elapsed);
                activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";  Remaining: " + remainingStr + ".");
            }
            processed++;
       }
        Terms.get().addUncommittedNoChecks(promotionRefsetConcept);
        Terms.get().addUncommittedNoChecks(memberRefsetConcept);
    }

    private I_GetConceptData getPromotionStatus(I_ExtendByRef promotionExtension) throws Exception {
        I_ExtendByRefPart latestPart = getLatestPart(promotionExtension);
        if (latestPart == null) {
            return null;
        } else {
            if (latestPart instanceof I_ExtendByRefPartCid) {
                I_ExtendByRefPartCid latestConceptPart = (I_ExtendByRefPartCid) latestPart;
                return termFactory.getConcept(latestConceptPart.getC1id());
            } else {
                throw new Exception("Don't know how to handle promotion ext of type : " + latestPart);
            }
        }
    }

    private I_ExtendByRefPart getLatestPart(I_ExtendByRef memberExtension) throws TerminologyException, IOException {
        List<? extends I_ExtendByRefVersion> versions = memberExtension.getTuples(null, 
            activeFrameConfig.getViewPositionSetReadOnly(), 
            activeFrameConfig.getPrecedence(), 
            activeFrameConfig.getConflictResolutionStrategy());
        if (versions.size() == 0) {
            return null;
        }
        if (versions.size() > 1) {
            throw new IOException("Contradiction identified in member extension:\n" +
                memberExtension + "\n\ncontradiction: " + versions);
        }
        return versions.get(0);
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
