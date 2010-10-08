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
package org.dwfa.ace.task.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/refset", type = BeanType.TASK_BEAN) })
public class RemoveAllConceptsFromRefset extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    /**
     * Property name for the term component to test.
     */
    private String componentPropName = ProcessAttachmentKeys.SEARCH_TEST_ITEM.getAttachmentKey();

    transient private I_TermFactory termFactory;

    transient private int retiredConceptId;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.componentPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            this.componentPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_GetConceptData refset = (I_GetConceptData) process.readProperty(componentPropName);
            int refsetId = refset.getConceptId();

            termFactory = LocalVersionedTerminology.get();
            retiredConceptId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids()
                .iterator()
                .next());

            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            List<I_ThinExtByRefVersioned> extVersions = termFactory.getRefsetExtensionMembers(refsetId);
            for (I_ThinExtByRefVersioned thinExtByRefVersioned : extVersions) {
                List<I_ThinExtByRefTuple> extensions = thinExtByRefVersioned.getTuples(config.getAllowedStatus(),
                    config.getViewPositionSet(), true);
                for (I_ThinExtByRefTuple thinExtByRefTuple : extensions) {
                    if (thinExtByRefTuple.getRefsetId() == refsetId) {
                        I_ThinExtByRefPart part = thinExtByRefTuple.getPart();
                        I_ThinExtByRefPart clone = part.duplicatePart();

                        clone.setStatus(retiredConceptId);
                        clone.setVersion(Integer.MAX_VALUE);
                        thinExtByRefVersioned.addVersion(clone);

                        termFactory.addUncommitted(thinExtByRefVersioned);
                    }
                }
            }

            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getComponentPropName() {
        return componentPropName;
    }

    public void setComponentPropName(String componentPropName) {
        this.componentPropName = componentPropName;
    }

}
