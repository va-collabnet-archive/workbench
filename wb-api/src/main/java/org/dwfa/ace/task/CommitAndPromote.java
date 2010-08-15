package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.PositionBI;

@BeanList(specs = { @Spec(directory = "tasks/ide/db", type = BeanType.TASK_BEAN) })
public class CommitAndPromote extends AbstractTask {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.CURRENT_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            profilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) process.getProperty(profilePropName);
            Set<? extends PositionBI> viewPositionSet = config.getViewPositionSet();
            PathSetReadOnly promotionPaths = new PathSetReadOnly(config.getPromotionPathSet());
            if (viewPositionSet.size() != 1 || promotionPaths.size() != 1) {
                throw new TaskFailedException(
                    "There must be only one view position, and one promotion path: viewPaths " + viewPositionSet
                        + " promotionPaths: " + promotionPaths);
            }

            PositionBI viewPosition = viewPositionSet.iterator().next();
            List<I_GetConceptData> conceptsToPromote = new ArrayList<I_GetConceptData>();
        	for (I_Transact t: Terms.get().getUncommitted()) {
        		if (I_GetConceptData.class.isAssignableFrom(t.getClass())) {
        			conceptsToPromote.add((I_GetConceptData) t);
        		}
        	}
            Terms.get().commit();
            
            // Commit complete, now promote...
            for (I_GetConceptData concept: conceptsToPromote) {
            	concept.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
                		config.getAllowedStatus(), config.getPrecedence());
            	Terms.get().addUncommittedNoChecks(concept);
            }
            
            Terms.get().commit();
            
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

	public String getProfilePropName() {
		return profilePropName;
	}

	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

}
