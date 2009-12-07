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
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
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
 * Meta data created includes :
 * concepts (member refset, refset spec, marked parent, comments refset,
 * promotion refset)
 * descriptions (FSN and PT for each concept)
 * relationships (member refset -> refset identity, remaining concepts ->
 * supporting refset)
 * 
 * Required input to this task is the name of the refset spec being created.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class CreateRefsetMetaDataTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 2;
    private String newRefsetPropName = ProcessAttachmentKeys.WORKING_REFSET.getAttachmentKey();
    private TermEntry statusTermEntry = new TermEntry(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids());
    private String reviewerUuidPropName = ProcessAttachmentKeys.REVIEWER_UUID.getAttachmentKey();
    private String ownerUuidPropName = ProcessAttachmentKeys.OWNER_UUID.getAttachmentKey();
    private String editorUuidPropName = ProcessAttachmentKeys.EDITOR_UUID.getAttachmentKey();
    private I_TermFactory termFactory;
    private I_GetConceptData status;

    private transient Exception ex = null;
    private transient Condition returnCondition = Condition.ITEM_COMPLETE;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(newRefsetPropName);
        out.writeObject(statusTermEntry);
        out.writeObject(reviewerUuidPropName);
        out.writeObject(ownerUuidPropName);
        out.writeObject(editorUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            newRefsetPropName = (String) in.readObject();
        } else if (objDataVersion == 2) {
            newRefsetPropName = (String) in.readObject();
            statusTermEntry = (TermEntry) in.readObject();
            reviewerUuidPropName = (String) in.readObject();
            ownerUuidPropName = (String) in.readObject();
            editorUuidPropName = (String) in.readObject();
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

            String name = (String) process.readProperty(newRefsetPropName);
            status = termFactory.getConcept(statusTermEntry.getIds());
            UUID[] reviewerUuids = (UUID[]) process.readProperty(reviewerUuidPropName);
            I_GetConceptData owner = termFactory.getConcept((UUID[]) process.readProperty(ownerUuidPropName));
            I_GetConceptData editor = termFactory.getConcept((UUID[]) process.readProperty(editorUuidPropName));

            I_GetConceptData parent =
                    termFactory.getConcept(new UUID[] { (UUID) process
                        .readProperty(ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey()) });

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
            I_GetConceptData memberRefset = newConcept();
            I_GetConceptData refsetSpec = newConcept();
            I_GetConceptData markedParent = newConcept();
            I_GetConceptData promotionRefset = newConcept();
            I_GetConceptData commentsRefset = newConcept();

            // create FSN and PT for each
            newDescription(memberRefset, fsnConcept, memberRefsetName);
            newDescription(memberRefset, ptConcept, memberRefsetName);

            newDescription(refsetSpec, fsnConcept, refsetSpecName);
            newDescription(refsetSpec, ptConcept, refsetSpecName);

            newDescription(markedParent, fsnConcept, markedParentName);
            newDescription(markedParent, ptConcept, markedParentName);

            newDescription(promotionRefset, fsnConcept, promotionName);
            newDescription(promotionRefset, ptConcept, promotionName);

            newDescription(commentsRefset, fsnConcept, commentsName);
            newDescription(commentsRefset, ptConcept, commentsName);

            // create relationships
            newRelationship(memberRefset, markedParentRel, markedParent);
            I_GetConceptData isADestination = null;
            if (termFactory.hasId(SNOMED.Concept.IS_A.getUids())) {
                isADestination = termFactory.getConcept(SNOMED.Concept.IS_A.getUids());
            } else {
                isADestination = termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
            }
            newRelationship(memberRefset, markedParentIsATypeRel, isADestination);
            newRelationship(refsetSpec, specifiesRefsetRel, memberRefset);

            newRelationship(memberRefset, isA, parent);
            newRelationship(refsetSpec, isA, supportingRefset);
            newRelationship(markedParent, isA, supportingRefset);
            newRelationship(promotionRefset, isA, supportingRefset);
            newRelationship(commentsRefset, isA, supportingRefset);

            newRelationship(memberRefset, refsetOwnerRel, owner);
            newRelationship(memberRefset, refsetEditorRel, editor);
            for (UUID reviewerUuid : reviewerUuids) {
                I_GetConceptData reviewer = termFactory.getConcept(new UUID[] { reviewerUuid });
                newRelationship(memberRefset, refsetReviewerRel, reviewer);
            }

            process.setProperty(ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey(), memberRefset.getUids().iterator()
                .next());

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

    private I_GetConceptData newConcept() throws Exception {
        try {
            boolean isDefined = true;

            List<I_Path> paths = termFactory.getPaths();

            UUID conceptUuid = UUID.randomUUID();
            termFactory.uuidToNativeWithGeneration(conceptUuid, ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
                .localize().getNid(), paths, Integer.MAX_VALUE);

            I_GetConceptData newConcept =
                    termFactory.newConcept(conceptUuid, isDefined, termFactory.getActiveAceFrameConfig());

            // edit the existing part's status
            I_ConceptAttributeVersioned v = newConcept.getConceptAttributes();
            newConcept.getConceptAttributes();
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

    private void newDescription(I_GetConceptData concept, I_GetConceptData descriptionType, String description)
            throws Exception {

        try {

            List<I_Path> paths = termFactory.getPaths();
            UUID descUuid = UUID.randomUUID();
            termFactory.uuidToNativeWithGeneration(descUuid, ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
                .getNid(), paths, Integer.MAX_VALUE);

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
            I_GetConceptData destination) throws Exception {
        try {
            int statusId = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId();

            List<I_Path> paths = termFactory.getPaths();
            UUID relUuid = UUID.randomUUID();

            I_GetConceptData charConcept =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            I_GetConceptData refConcept =
                    termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            int group = 0;

            termFactory.uuidToNativeWithGeneration(relUuid, ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
                .getNid(), paths, Integer.MAX_VALUE);

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
}
