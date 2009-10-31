package org.dwfa.ace.task.refset.spec.comment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.spec.SpecRefsetHelper;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Creates a comment extension on the refset currently in the refset spec panel.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class CreateCommentExtTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 2;
    private I_TermFactory termFactory;

    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String refsetSpecUuidPropName = ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(commentsPropName);
        out.writeObject(refsetSpecUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                commentsPropName = (String) in.readObject();
            } else {
                commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
            }
            if (objDataVersion >= 2) {
                refsetSpecUuidPropName = (String) in.readObject();
            } else {
                refsetSpecUuidPropName = ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            termFactory = LocalVersionedTerminology.get();
            UUID refsetSpecUuid = (UUID) process.readProperty(refsetSpecUuidPropName);
            I_GetConceptData refsetSpecConcept = termFactory.getConcept(refsetSpecUuid);
            String comments = (String) process.readProperty(commentsPropName);
            UUID currentUuid = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next();
            if (refsetSpecConcept != null) {
                RefsetSpec refsetSpec = new RefsetSpec(refsetSpecConcept);
                I_GetConceptData commentsRefset = refsetSpec.getCommentsRefsetConcept();
                I_GetConceptData memberRefset = refsetSpec.getMemberRefsetConcept();
                if (commentsRefset != null && memberRefset != null) {
                    SpecRefsetHelper specRefsetHelper = new SpecRefsetHelper();
                    for (I_Path path : termFactory.getActiveAceFrameConfig().getEditingPathSet()) {
                        specRefsetHelper.newStringRefsetExtension(commentsRefset.getConceptId(), memberRefset
                            .getConceptId(), comments, UUID.randomUUID(), termFactory.getConcept(path.getConceptId())
                            .getUids().iterator().next(), currentUuid, Integer.MAX_VALUE);
                    }
                }
            }

            return Condition.CONTINUE;
        } catch (Exception e) {

        }
        return Condition.CONTINUE;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getCommentsPropName() {
        return commentsPropName;
    }

    public void setCommentsPropName(String commentsPropName) {
        this.commentsPropName = commentsPropName;
    }

    public String getRefsetSpecUuidPropName() {
        return refsetSpecUuidPropName;
    }

    public void setRefsetSpecUuidPropName(String refsetSpecUuidPropName) {
        this.refsetSpecUuidPropName = refsetSpecUuidPropName;
    }
}