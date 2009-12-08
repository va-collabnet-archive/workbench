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
package org.dwfa.ace.task.promote;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.RefsetHelper;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/refset/promote", type = BeanType.TASK_BEAN) })
public class PromoteRefset extends AbstractTask {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            profilePropName = (String) in.readObject();
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
            Set<I_Position> viewPositionSet = config.getViewPositionSet();
            Set<I_Path> promotionPaths = config.getPromotionPathSet();
            if (viewPositionSet.size() != 1 || promotionPaths.size() != 1) {
                throw new TaskFailedException(
                    "There must be only one view position, and one promotion path: viewPaths " + viewPositionSet
                        + " promotionPaths: " + promotionPaths);
            }
            I_GetConceptData refsetToPromote = config.getRefsetInSpecEditor();
            if (refsetToPromote == null) {
                throw new TaskFailedException("The refset in the spec editor is null. ");
            }

            I_TermFactory tf = LocalVersionedTerminology.get();

            I_Position viewPosition = viewPositionSet.iterator().next();
            promoteRefset(config, viewPosition, promotionPaths, tf, refsetToPromote);

            for (I_GetConceptData memberRefsetIdentity : RefsetHelper.getSpecificationRefsetForRefset(refsetToPromote,
                config)) {
                promoteRefset(config, viewPosition, promotionPaths, tf, memberRefsetIdentity);
            }
            for (I_GetConceptData promotionRefsetIdentity : RefsetHelper.getPromotionRefsetForRefset(refsetToPromote,
                config)) {
                promoteRefset(config, viewPosition, promotionPaths, tf, promotionRefsetIdentity);
            }
            for (I_GetConceptData markedParentRefsetIdentity : RefsetHelper.getMarkedParentRefsetForRefset(
                refsetToPromote, config)) {
                promoteRefset(config, viewPosition, promotionPaths, tf, markedParentRefsetIdentity);
            }
            for (I_GetConceptData commentsRefsetIdentity : RefsetHelper.getCommentsRefsetForRefset(refsetToPromote,
                config)) {
                promoteRefset(config, viewPosition, promotionPaths, tf, commentsRefsetIdentity);
            }

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        return Condition.CONTINUE;
    }

    private void promoteRefset(I_ConfigAceFrame config, I_Position viewPosition, Set<I_Path> promotionPaths,
            I_TermFactory tf, I_GetConceptData refsetIdentity) throws TerminologyException, IOException {
        refsetIdentity.promote(viewPosition, promotionPaths, config.getAllowedStatus());
        tf.addUncommittedNoChecks(refsetIdentity);
        promoteMembers(config, viewPosition, promotionPaths,
            tf.getRefsetExtensionMembers(refsetIdentity.getConceptId()));
    }

    private void promoteMembers(I_ConfigAceFrame config, I_Position viewPosition, Set<I_Path> promitionSets,
            List<I_ThinExtByRefVersioned> refsetMembers) throws TerminologyException, IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        Set<I_Position> positionSet = new HashSet<I_Position>();
        positionSet.add(viewPosition);
        for (I_ThinExtByRefVersioned ext : refsetMembers) {
            for (I_ThinExtByRefTuple tuple : ext.getTuples(config.getAllowedStatus(), positionSet, false, false)) {
                for (I_Path path : promitionSets) {
                    if (tuple.promote(path)) {
                        tf.addUncommittedNoChecks(tuple.getCore());
                    }
                }
            }
        }
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

}
