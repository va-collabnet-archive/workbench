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
package org.dwfa.ace.task.refset.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Creates meta data required for a new refset.
 * Meta data created includes:
 * concepts (member refset, refset spec, marked parent, comments refset,
 * promotion refset)
 * descriptions (FSN and PT for each concept)
 * relationships (member refset -> refset identity, remaining concepts ->
 * supporting refset)
 * This task also sets the destination inbox.
 * 
 * Required input to this task is the name of the refset being created.
 * 
 * @author Chrissy Hill
 * @author Perry Reid
 * @version 3, October 2009
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class CreateRefsetMetaDataTask extends AbstractTask {

    /*
     * -----------------------
     * Properties
     * -----------------------
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 3;
    private String newRefsetPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private TermEntry statusTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids());
    private String reviewerUuidPropName = ProcessAttachmentKeys.REVIEWER_UUID.getAttachmentKey();
    private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
    private String editorUuidPropName = ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey();
    private String newRefsetUUIDPropName = ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey();
    private String newRefsetSpecUUIDPropName = ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey();

    private I_TermFactory termFactory;
    private I_GetConceptData status;

    private transient Exception ex = null;
    private transient Condition returnCondition = Condition.ITEM_COMPLETE;

    /*
     * -----------------------
     * Serialization Methods
     * -----------------------
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(newRefsetPropName);
        out.writeObject(statusTermEntry);
        out.writeObject(reviewerUuidPropName);
        out.writeObject(ownerUuidPropName);
        out.writeObject(editorUuidPropName);
        out.writeObject(newRefsetUUIDPropName);
        out.writeObject(newRefsetSpecUUIDPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields
                newRefsetPropName = (String) in.readObject();
            } else {
                // Set version 1 default values
                newRefsetPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
            }
            if (objDataVersion >= 2) {
                // Read data fields added in version 2 (should have read version
                // 1 already)
                statusTermEntry = (TermEntry) in.readObject();
                reviewerUuidPropName = (String) in.readObject();
                ownerUuidPropName = (String) in.readObject();
                editorUuidPropName = (String) in.readObject();
            } else {
                // Set version 2 default values
                statusTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids());
                reviewerUuidPropName = ProcessAttachmentKeys.REVIEWER_UUID.getAttachmentKey();
                ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
                editorUuidPropName = ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey();
            }
            if (objDataVersion >= 3) {
                // Read data fields added in version 3 (should have read version
                // 1 & 2 already)
                newRefsetUUIDPropName = (String) in.readObject();
                newRefsetSpecUUIDPropName = (String) in.readObject();
            } else {
                // The following field were not in this version, so set default
                // values for them.
                newRefsetUUIDPropName = ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey();
                newRefsetSpecUUIDPropName = ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey();
            }

            // Initialize transient properties
            ex = null;
            returnCondition = Condition.ITEM_COMPLETE;

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
     * Performs the primary action of the task, which in this case is to present
     * a small user interface to the user which allows them to specify the
     * characteristics
     * of this refset to be created.
     * 
     * @return The exit condition of the task
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
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

    /**
     * Creates the new refset.
     * 
     * @return void
     * @param process The currently executing Workflow process
     * @param worker The worker currently executing this task
     * @exception TaskFailedException Thrown if a task fails for any reason.
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void doRun(final I_EncodeBusinessProcess process, I_Work worker) {

        try {

            termFactory = LocalVersionedTerminology.get();
            I_ConfigAceFrame aceConfig = termFactory.getActiveAceFrameConfig();

            String name = (String) process.getProperty(newRefsetPropName);
            status = termFactory.getConcept(statusTermEntry.getIds());
            UUID[] reviewerUuids = (UUID[]) process.getProperty(reviewerUuidPropName);
            I_GetConceptData owner = termFactory.getConcept((UUID[]) process.getProperty(ownerUuidPropName));
            I_GetConceptData editor = termFactory.getConcept((UUID[]) process.getProperty(editorUuidPropName));

            I_GetConceptData parent =
                    termFactory.getConcept(new UUID[] { (UUID) process
                        .getProperty(ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey()) });

            I_GetConceptData fsnConcept =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
            I_GetConceptData ptConcept =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
            I_GetConceptData isA = termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
            I_GetConceptData supportingRefset =
                    termFactory.getConcept(RefsetAuxiliary.Concept.SUPPORTING_REFSETS.getUids());
            I_GetConceptData markedParentRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids());
            I_GetConceptData markedParentIsATypeRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_IS_A_TYPE.getUids());
            I_GetConceptData specifiesRefsetRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
            I_GetConceptData refsetReviewerRel =
                    termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_REVIEWER.getUids());
            I_GetConceptData refsetOwnerRel = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_OWNER.getUids());
            I_GetConceptData refsetEditorRel = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_EDITOR.getUids());
            I_GetConceptData promotionRel = termFactory.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
            I_GetConceptData commentsRel = termFactory.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
            I_GetConceptData purposeRel = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
            I_GetConceptData stringAnnotation =
                    termFactory.getConcept(RefsetAuxiliary.Concept.STRING_ANNOTATION_PURPOSE.getUids());
            I_GetConceptData markedParentAnnotation =
                    termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PARENT_MEMBER_PURPOSE.getUids());
            I_GetConceptData enumeratedAnnotation =
                    termFactory.getConcept(RefsetAuxiliary.Concept.ENUMERATED_ANNOTATION_PURPOSE.getUids());
            I_GetConceptData specAnnotation =
                    termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_SPECIFICATION.getUids());

            // check that the name isn't null or empty etc
            if (name == null || name.trim().equals("")) {
                throw new TaskFailedException("Refset name cannot be empty.");
            }

            String memberRefsetName = name + " refset";
            String refsetSpecName = name + " refset spec";
            String markedParentName = name + " marked parent";
            String promotionName = name + " promotion refset";
            String commentsName = name + " comments refset";

            // create new concepts
            I_GetConceptData memberRefset = newConcept(aceConfig);
            I_GetConceptData refsetSpec = newConcept(aceConfig);
            I_GetConceptData markedParent = newConcept(aceConfig);
            I_GetConceptData promotionRefset = newConcept(aceConfig);
            I_GetConceptData commentsRefset = newConcept(aceConfig);

            // create FSN and PT for each
            newDescription(memberRefset, fsnConcept, memberRefsetName, aceConfig);
            newDescription(memberRefset, ptConcept, memberRefsetName, aceConfig);

            newDescription(refsetSpec, fsnConcept, refsetSpecName, aceConfig);
            newDescription(refsetSpec, ptConcept, refsetSpecName, aceConfig);

            newDescription(markedParent, fsnConcept, markedParentName, aceConfig);
            newDescription(markedParent, ptConcept, markedParentName, aceConfig);

            newDescription(promotionRefset, fsnConcept, promotionName, aceConfig);
            newDescription(promotionRefset, ptConcept, promotionName, aceConfig);

            newDescription(commentsRefset, fsnConcept, commentsName, aceConfig);
            newDescription(commentsRefset, ptConcept, commentsName, aceConfig);

            // create relationships
            newRelationship(memberRefset, markedParentRel, markedParent, aceConfig);
            I_GetConceptData isADestination = null;
            if (termFactory.hasId(SNOMED.Concept.IS_A.getUids())) {
                isADestination = termFactory.getConcept(SNOMED.Concept.IS_A.getUids());
            } else {
                isADestination = termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
            }
            newRelationship(memberRefset, markedParentIsATypeRel, isADestination, aceConfig);
            newRelationship(refsetSpec, specifiesRefsetRel, memberRefset, aceConfig);

            newRelationship(memberRefset, isA, parent, aceConfig);
            newRelationship(refsetSpec, isA, supportingRefset, aceConfig);
            newRelationship(markedParent, isA, supportingRefset, aceConfig);
            newRelationship(promotionRefset, isA, supportingRefset, aceConfig);
            newRelationship(commentsRefset, isA, supportingRefset, aceConfig);

            newRelationship(memberRefset, refsetOwnerRel, owner, aceConfig);
            newRelationship(memberRefset, refsetEditorRel, editor, aceConfig);
            for (UUID reviewerUuid : reviewerUuids) {
                I_GetConceptData reviewer = termFactory.getConcept(new UUID[] { reviewerUuid });
                newRelationship(memberRefset, refsetReviewerRel, reviewer, aceConfig);
            }

            newRelationship(memberRefset, promotionRel, promotionRefset, aceConfig);
            newRelationship(memberRefset, commentsRel, commentsRefset, aceConfig);

            // supporting refsets purpose relationships
            newRelationship(commentsRefset, purposeRel, stringAnnotation, aceConfig);
            newRelationship(markedParent, purposeRel, markedParentAnnotation, aceConfig);
            newRelationship(promotionRefset, purposeRel, enumeratedAnnotation, aceConfig);
            newRelationship(refsetSpec, purposeRel, specAnnotation, aceConfig);

            process.setProperty(ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey(), memberRefset.getUids().iterator()
                .next());
            process.setProperty(ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey(), refsetSpec.getUids()
                .iterator().next());

            termFactory.getActiveAceFrameConfig().setBuilderToggleVisible(true);
            termFactory.getActiveAceFrameConfig().setInboxToggleVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "Refset wizard cannot be completed. Error creating refset meta data: " + e.getMessage(), "",
                JOptionPane.ERROR_MESSAGE);
            returnCondition = Condition.ITEM_CANCELED;
            return;
        }

        returnCondition = Condition.ITEM_COMPLETE;

    }

    private I_GetConceptData newConcept(I_ConfigAceFrame aceConfig) throws Exception {
        try {
            boolean isDefined = true;

 
            UUID conceptUuid = UUID.randomUUID();
            termFactory.uuidToNativeWithGeneration(conceptUuid, ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
                .localize().getNid(), aceConfig.getEditingPathSet(), Integer.MAX_VALUE);

            I_GetConceptData newConcept =
                    termFactory.newConcept(conceptUuid, isDefined, aceConfig);

            // edit the existing part's status
            I_ConceptAttributeVersioned v = newConcept.getConceptAttributes();

            int index = v.getVersions().size() - 1;
            I_ConceptAttributePart part;
            if (index >= 0) {
                part = (I_ConceptAttributePart) v.getVersions().get(index);
            } else {
                part = termFactory.newConceptAttributePart();
            }
            part.setStatusId(status.getConceptId());
            v.addVersion(part);
            termFactory.addUncommittedNoChecks(newConcept);

            return newConcept;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    private void newDescription(I_GetConceptData concept, I_GetConceptData descriptionType, 
    		String description, I_ConfigAceFrame aceConfig)
            throws Exception {

        try {
            UUID descUuid = UUID.randomUUID();
            termFactory.uuidToNativeWithGeneration(descUuid, ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
                .getNid(), aceConfig.getEditingPathSet(), Integer.MAX_VALUE);

            I_DescriptionVersioned descVersioned =
                    termFactory.newDescription(descUuid, concept, "en", description, descriptionType, termFactory
                        .getActiveAceFrameConfig());

            I_DescriptionPart part = descVersioned.getLastTuple().getPart();
            part.setStatusId(status.getConceptId());

            descVersioned.addVersion(part);
            termFactory.addUncommittedNoChecks(concept);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public void newRelationship(I_GetConceptData concept, I_GetConceptData relationshipType,
            I_GetConceptData destination, I_ConfigAceFrame aceConfig) throws Exception {
        try {
            int statusId = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId();
            UUID relUuid = UUID.randomUUID();

            I_GetConceptData charConcept =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            I_GetConceptData refConcept =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            int group = 0;

            termFactory.uuidToNativeWithGeneration(relUuid, ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
                .getNid(), aceConfig.getEditingPathSet(), Integer.MAX_VALUE);

            I_RelVersioned relVersioned =
                    termFactory.newRelationship(relUuid, concept, relationshipType, destination, charConcept,
                        refConcept, termFactory.getConcept(statusId), group, termFactory.getActiveAceFrameConfig());

            I_RelPart newPart = relVersioned.getLastTuple().getPart();
            newPart.setStatusId(status.getConceptId());

            relVersioned.addVersion(newPart);
            termFactory.addUncommittedNoChecks(concept);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.ITEM_SKIPPED_OR_COMPLETE;
    }

    public String getNewRefsetPropName() {
        return newRefsetPropName;
    }

    public void setNewRefsetPropName(String newRefsetPropName) {
        this.newRefsetPropName = newRefsetPropName;
    }

    public TermEntry getStatusTermEntry() {
        return statusTermEntry;
    }

    public void setStatusTermEntry(TermEntry statusTermEntry) {
        this.statusTermEntry = statusTermEntry;
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

    public String getEditorUuidPropName() {
        return editorUuidPropName;
    }

    public void setEditorUuidPropName(String editorUuidPropName) {
        this.editorUuidPropName = editorUuidPropName;
    }

    public String getNewRefsetUUIDPropName() {
        return newRefsetUUIDPropName;
    }

    public void setNewRefsetUUIDPropName(String newRefsetUUIDPropName) {
        this.newRefsetUUIDPropName = newRefsetUUIDPropName;
    }

    public String getNewRefsetSpecUUIDPropName() {
        return newRefsetSpecUUIDPropName;
    }

    public void setNewRefsetSpecUUIDPropName(String newRefsetSpecUUIDPropName) {
        this.newRefsetSpecUUIDPropName = newRefsetSpecUUIDPropName;
    }

}
