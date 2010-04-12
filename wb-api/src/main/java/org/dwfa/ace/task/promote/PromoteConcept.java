package org.dwfa.ace.task.promote;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/refset/promote", type = BeanType.TASK_BEAN) })
public class PromoteConcept extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
    private String conceptPropName = ProcessAttachmentKeys.CONCEPT_UUID.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
        out.writeObject(conceptPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            profilePropName = (String) in.readObject();
            conceptPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(profilePropName);
            Terms.get().commit();
            Set<I_Position> viewPositionSet = config.getViewPositionSet();
            PathSetReadOnly promotionPaths = new PathSetReadOnly(config.getPromotionPathSet());
            if (viewPositionSet.size() != 1 || promotionPaths.size() != 1) {
                throw new TaskFailedException(
                    "There must be only one view position, and one promotion path: viewPaths " + viewPositionSet
                        + " promotionPaths: " + promotionPaths);
            }
            I_GetConceptData conceptToPromote = AceTaskUtil.getConceptFromProperty(process, conceptPropName);
            if (conceptToPromote == null) {
                throw new TaskFailedException("The conceptToPromote is null. ");
            }

            I_Position viewPosition = viewPositionSet.iterator().next();
            conceptToPromote.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
                config.getAllowedStatus(), config.getPrecedence());
            Terms.get().addUncommittedNoChecks(conceptToPromote);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        return Condition.CONTINUE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getConceptPropName() {
        return conceptPropName;
    }

    public void setConceptPropName(String conceptPropName) {
        this.conceptPropName = conceptPropName;
    }

}
