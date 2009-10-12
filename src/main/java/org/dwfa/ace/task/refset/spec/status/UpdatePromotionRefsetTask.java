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
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.RefsetHelper;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
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
    private I_GetConceptData promotionConcept;
    private I_GetConceptData memberRefsetConcept;
    private I_GetConceptData unreviewedAdditionStatus;
    private I_GetConceptData unreviewedDeletionStatus;
    private I_GetConceptData reviewedApprovedStatus;
    private I_GetConceptData reviewedRejectedStatus;

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

            UUID refsetSpecUuid = (UUID) process.readProperty(refsetSpecUuidPropName);
            I_GetConceptData refsetSpecConcept = termFactory.getConcept(new UUID[] { refsetSpecUuid });
            RefsetSpec refsetSpec = new RefsetSpec(refsetSpecConcept);

            memberRefsetConcept = refsetSpec.getMemberRefsetConcept();
            if (memberRefsetConcept == null) {
                throw new Exception("Member refset is null");
            }
            promotionConcept = refsetSpec.getPromotionRefsetConcept();
            if (promotionConcept == null) {
                throw new Exception("Promotion refset is null");
            }

            unreviewedAdditionStatus =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.getUids());
            unreviewedDeletionStatus =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION.getUids());
            reviewedApprovedStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED.getUids());
            reviewedRejectedStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_REJECTED.getUids());

            List<I_ThinExtByRefVersioned> memberExtensions =
                    termFactory.getRefsetExtensionMembers(memberRefsetConcept.getConceptId());
            List<I_ThinExtByRefVersioned> promotionExtensions =
                    termFactory.getRefsetExtensionMembers(promotionConcept.getConceptId());

            updatePromotionsRefset(memberExtensions, promotionExtensions);

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
        for (I_ThinExtByRefVersioned memberExtension : memberExtensions) {
            I_ThinExtByRefPart latestMemberPart = getLatestPart(memberExtension);
            I_ThinExtByRefVersioned promotionExtension =
                    getExtensionByComponent(memberExtension.getComponentId(), promotionExtensions);
            I_GetConceptData promotionStatus = null;
            if (promotionExtension != null) {
                promotionStatus = getPromotionStatus(promotionExtension);
            }
            if (latestMemberPart == null) {
                throw new Exception("Member extension exists with no parts.");
            } else {
                if (latestMemberPart.getStatusId() == currentStatusConcept.getConceptId()) {
                    if (promotionStatus == null) {
                        // add a new promotion refset member with value
                        // unreviewed addition
                        newConceptExtension(promotionConcept.getConceptId(), memberExtension.getComponentId(),
                            unreviewedAdditionStatus.getConceptId());
                    }
                } else if (latestMemberPart.getStatusId() == retiredStatusConcept.getConceptId()) {
                    if (promotionStatus == null) {
                        // add a new promotion refset member with value
                        // unreviewed deletion
                        newConceptExtension(promotionConcept.getConceptId(), memberExtension.getComponentId(),
                            unreviewedDeletionStatus.getConceptId());
                    }
                } else {
                    throw new Exception("Don't know how to handle status : "
                        + termFactory.getConcept(latestMemberPart.getStatusId()).getInitialText());
                }
            }
        }
    }

    private boolean newConceptExtension(int refsetId, int componentId, int c1Id) {

        try {
            UUID memberUuid =
                    new RefsetHelper().generateUuid(termFactory.getUids(refsetId).iterator().next(), termFactory
                        .getUids(componentId).iterator().next(), termFactory.getUids(c1Id).iterator().next());
            if (memberUuid == null) {
                memberUuid = UUID.randomUUID();
            }

            int newMemberId =
                    termFactory.uuidToNativeWithGeneration(memberUuid, ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
                        .localize().getNid(), termFactory.getActiveAceFrameConfig().getEditingPathSet(),
                        Integer.MAX_VALUE);

            I_ThinExtByRefVersioned newExtension =
                    termFactory.newExtensionNoChecks(refsetId, newMemberId, componentId,
                        RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid());

            for (I_Path editPath : termFactory.getActiveAceFrameConfig().getEditingPathSet()) {

                I_ThinExtByRefPartConcept conceptExtension = termFactory.newConceptExtensionPart();

                conceptExtension.setPathId(editPath.getConceptId());
                conceptExtension.setStatusId(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
                conceptExtension.setVersion(Integer.MAX_VALUE);
                conceptExtension.setC1id(c1Id);

                newExtension.addVersion(conceptExtension);
            }

            termFactory.addUncommittedNoChecks(newExtension);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
