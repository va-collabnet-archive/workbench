package org.dwfa.ace.task.refset.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
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
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Creates meta data required for a refset spec. This consists of a refset spec,
 * marked parent and refset components. Required input to this task is the name
 * of the refset spec being created.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class CreateRefsetMetaDataTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String newRefsetPropName = ProcessAttachmentKeys.MESSAGE
            .getAttachmentKey();
    private I_TermFactory termFactory;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(newRefsetPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            newRefsetPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

        try {
            String name = (String) process.readProperty(newRefsetPropName);
            termFactory = LocalVersionedTerminology.get();
            I_GetConceptData fsnConcept = termFactory
                    .getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
                            .getUids());
            I_GetConceptData ptConcept = termFactory
                    .getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
                            .getUids());
            I_GetConceptData isA = termFactory
                    .getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
                            .getUids());
            I_GetConceptData refsetParent = termFactory
                    .getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY
                            .getUids());
            I_GetConceptData markedParentRel = termFactory
                    .getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET
                            .getUids());
            I_GetConceptData markedParentIsATypeRel = termFactory
                    .getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_IS_A_TYPE
                            .getUids());
            I_GetConceptData specifiesRefsetRel = termFactory
                    .getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET
                            .getUids());

            // check that the name isn't null or empty etc
            if (name == null || name.trim().equals("")) {
                throw new TaskFailedException("Refset name cannot be empty.");
            }

            String refsetName = name + " refset";
            String refsetSpecName = name + " refset spec";
            String markedParentName = name + " marked parent";

            // create 3 new concepts
            I_GetConceptData refset = newConcept();
            I_GetConceptData refsetSpec = newConcept();
            I_GetConceptData markedParent = newConcept();

            // create FSN and PT for each
            newDescription(refset, fsnConcept, refsetName);
            newDescription(refset, ptConcept, refsetName);

            newDescription(refsetSpec, fsnConcept, refsetSpecName);
            newDescription(refsetSpec, ptConcept, refsetSpecName);

            newDescription(markedParent, fsnConcept, markedParentName);
            newDescription(markedParent, ptConcept, markedParentName);

            // create relationships
            newRelationship(refset, isA, refsetParent);
            newRelationship(refset, markedParentRel, markedParent);
            I_GetConceptData isADestination = null;
            if (termFactory.hasId(SNOMED.Concept.IS_A.getUids())) {
                isADestination = termFactory.getConcept(SNOMED.Concept.IS_A
                        .getUids());
            } else {
                isADestination = termFactory
                        .getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
                                .getUids());
            }
            newRelationship(refset, markedParentIsATypeRel, isADestination);

            newRelationship(refsetSpec, isA, refsetParent);
            newRelationship(refsetSpec, specifiesRefsetRel, refset);

            newRelationship(markedParent, isA, refsetParent);

            // commit
            termFactory.commit();

            // set new spec as focus
            termFactory.getActiveAceFrameConfig().setRefsetInSpecEditor(refset);

            return Condition.CONTINUE;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TaskFailedException(ex);
        }
    }

    private I_GetConceptData newConcept() throws Exception {
        try {
            boolean isDefined = true;

            List<I_Path> paths = termFactory.getPaths();

            UUID conceptUuid = UUID.randomUUID();
            termFactory.uuidToNativeWithGeneration(conceptUuid,
                    ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
                            .getNid(), paths, Integer.MAX_VALUE);

            I_GetConceptData newConcept = termFactory.newConcept(conceptUuid,
                    isDefined, termFactory.getActiveAceFrameConfig());
            newConcept.getConceptAttributes();

            return newConcept;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    private void newDescription(I_GetConceptData concept,
            I_GetConceptData descriptionType, String description)
            throws Exception {

        try {

            List<I_Path> paths = termFactory.getPaths();
            UUID descUuid = UUID.randomUUID();
            termFactory.uuidToNativeWithGeneration(descUuid,
                    ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
                            .getNid(), paths, Integer.MAX_VALUE);

            termFactory.newDescription(descUuid, concept, "en", description,
                    descriptionType, termFactory.getActiveAceFrameConfig());

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public void newRelationship(I_GetConceptData concept,
            I_GetConceptData relationshipType, I_GetConceptData destination)
            throws Exception {
        try {
            int statusId = termFactory.getConcept(
                    ArchitectonicAuxiliary.Concept.CURRENT.getUids())
                    .getConceptId();

            List<I_Path> paths = termFactory.getPaths();
            UUID relUuid = UUID.randomUUID();

            I_GetConceptData charConcept = termFactory
                    .getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP
                            .getUids());
            I_GetConceptData refConcept = termFactory
                    .getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY
                            .getUids());
            int group = 0;

            termFactory.uuidToNativeWithGeneration(relUuid,
                    ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
                            .getNid(), paths, Integer.MAX_VALUE);

            termFactory.newRelationship(relUuid, concept, relationshipType,
                    destination, charConcept, refConcept, termFactory
                            .getConcept(statusId), group, termFactory
                            .getActiveAceFrameConfig());

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getNewRefsetPropName() {
        return newRefsetPropName;
    }

    public void setNewRefsetPropName(String newRefsetPropName) {
        this.newRefsetPropName = newRefsetPropName;
    }
}
