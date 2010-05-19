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
import java.util.Set;

import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.time.TimeUtil;

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
            I_TermFactory tf = Terms.get();

            while (config.getViewPositionSet().size() != 1 || config.getPromotionPathSet().size() != 1) {
                if (config.getViewPositionSet().size() != 1) {
                    config.setShowPreferences(true);
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "There must only be ONE view position. Please fix this in the preferences panel before "
                            + "continuing. \n Current view positions: " + config.getViewPositionSet(), "",
                        JOptionPane.ERROR_MESSAGE);
                    InstructAndWait instruct = new InstructAndWait();
                    instruct.setInstruction("Update preferences - ONE view position required.");
                    instruct.evaluate(process, worker);
                }
                if (config.getPromotionPathSet().size() != 1) {
                    config.setShowPreferences(true);
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "There must only be ONE promotion path. Please fix this in the preferences panel before "
                            + "continuing. \n Current promotion paths: " + config.getPromotionPathSet(), "",
                        JOptionPane.ERROR_MESSAGE);
                    InstructAndWait instruct = new InstructAndWait();
                    instruct.setInstruction("Update preferences - ONE promotion path required.");
                    instruct.evaluate(process, worker);
                }
            }
            config.setShowPreferences(false);

            Set<I_Position> viewPositionSet = config.getViewPositionSet();
            PathSetReadOnly promotionPaths = new PathSetReadOnly(config.getPromotionPathSet());

            I_GetConceptData refsetToPromote = config.getRefsetInSpecEditor();
            if (refsetToPromote == null) {
                throw new TaskFailedException("The refset in the spec editor is null. ");
            }

            tf.commit();

            I_Position viewPosition = viewPositionSet.iterator().next();
            promoteRefset(config, viewPosition, promotionPaths, tf, refsetToPromote);

            for (I_GetConceptData specificationRefsetIdentity : Terms.get().getRefsetHelper(config)
                .getSpecificationRefsetForRefset(refsetToPromote, config)) {
                promoteRefset(config, viewPosition, promotionPaths, tf, specificationRefsetIdentity);
            }
            for (I_GetConceptData promotionRefsetIdentity : Terms.get().getRefsetHelper(config)
                .getPromotionRefsetForRefset(refsetToPromote, config)) {
                promoteRefset(config, viewPosition, promotionPaths, tf, promotionRefsetIdentity);
            }
            for (I_GetConceptData markedParentRefsetIdentity : Terms.get().getRefsetHelper(config)
                .getMarkedParentRefsetForRefset(refsetToPromote, config)) {
                promoteRefset(config, viewPosition, promotionPaths, tf, markedParentRefsetIdentity);
            }
            for (I_GetConceptData commentsRefsetIdentity : Terms.get().getRefsetHelper(config)
                    .getCommentsRefsetForRefset(refsetToPromote, config)) {
                promoteRefset(config, viewPosition, promotionPaths, tf, commentsRefsetIdentity);
           }
            tf.commit();

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        return Condition.CONTINUE;
    }

    private void promoteRefset(I_ConfigAceFrame config, I_Position viewPosition, PathSetReadOnly promotionPaths,
            I_TermFactory tf, I_GetConceptData refsetIdentity) throws TerminologyException, IOException {
        I_ShowActivity activity = Terms.get().newActivityPanel(true, config, "Promoting refset: " + refsetIdentity.toString(), false);
        activity.setIndeterminate(true);
        long start = System.currentTimeMillis();
        Collection<? extends I_ExtendByRef> extensions = tf.getRefsetExtensionMembers(refsetIdentity.getConceptId());
        int completedCount = 1;
        int size = extensions.size();
        activity.setValue(completedCount);
        activity.setMaximum(extensions.size());
        activity.setIndeterminate(false);
        refsetIdentity.promote(viewPosition, promotionPaths, null, config.getPrecedence());
        for (I_ExtendByRef ext : extensions) {
            ext.promote(viewPosition, new PathSetReadOnly(promotionPaths), null, config.getPrecedence());
            if (completedCount % 50 == 0) {
                activity.setValue(completedCount);
                long endTime = System.currentTimeMillis();
                long elapsed = endTime - start;
                String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
                String remainingStr = TimeUtil.getRemainingTimeString(completedCount, size, elapsed);
                activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";  Remaining: " + remainingStr + ".");
            }
            completedCount++;
        }
        tf.addUncommittedNoChecks(refsetIdentity);
        activity.setValue(size);
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - start;
        String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
        activity.setProgressInfoLower("Elapsed: " + elapsedStr);
        activity.complete();
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
