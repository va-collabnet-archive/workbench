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
package org.dwfa.ace.task.tree.filter;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_FilterTaxonomyRels;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/taxonomy", type = BeanType.TASK_BEAN) })
public class AddNoInitialOsFilter extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

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

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            I_ConfigAceFrame profile = (I_ConfigAceFrame) process.readProperty(profilePropName);
            if (profile == null) {
                profile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            }
            profile.getTaxonomyRelFilterList().add(new NoInitialOsFilter());
            return Condition.CONTINUE;

        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    private static class NoInitialOsFilter implements I_FilterTaxonomyRels {

        public void filter(I_GetConceptData node, List<I_RelTuple> srcRels, List<I_RelTuple> destRels,
                I_ConfigAceFrame frameConfig) throws TerminologyException, IOException {
            List<I_RelTuple> relsToRemove = new ArrayList<I_RelTuple>();
            AceLog.getAppLog().info("Filtering srcRels: " + srcRels + " destRels: " + destRels + " for: " + node);
            for (I_RelTuple rt : srcRels) {
                if (hasInitialO(rt.getC2Id(), frameConfig)) {
                    relsToRemove.add(rt);
                }
            }
            srcRels.removeAll(relsToRemove);
            relsToRemove.clear();
            for (I_RelTuple rt : destRels) {
                if (hasInitialO(rt.getC1Id(), frameConfig)) {
                    relsToRemove.add(rt);
                }
            }
            destRels.removeAll(relsToRemove);
        }

        private boolean hasInitialO(int conceptNid, I_ConfigAceFrame frameConfig) throws TerminologyException,
                IOException {
            I_GetConceptData concept = LocalVersionedTerminology.get().getConcept(conceptNid);
            boolean initialO = false;
            AceLog.getAppLog().info("Testing: " + concept);
            for (I_DescriptionTuple dt : concept.getDescriptionTuples(frameConfig.getAllowedStatus(), null,
                frameConfig.getViewPositionSet())) {
                AceLog.getAppLog().info("Testing tuple for supression: " + dt.getText());
                if (dt.getText().toLowerCase().startsWith("o")) {
                    initialO = true;
                    break;
                }
            }
            return initialO;
        }

    }

}
