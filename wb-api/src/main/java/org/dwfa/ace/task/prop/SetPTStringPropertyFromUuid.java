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
package org.dwfa.ace.task.prop;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.ProcessAttachmentKeys;
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
 * Sets a string property derived from the inputted UUID's PT.
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/prop", type = BeanType.TASK_BEAN) })
public class SetPTStringPropertyFromUuid extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String uuidPropName = ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey();
    private String ptPropName = ProcessAttachmentKeys.REFSET_NAME.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(uuidPropName);
        out.writeObject(ptPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion > dataVersion) {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
        uuidPropName = (String) in.readObject();
        ptPropName = (String) in.readObject();
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_TermFactory termFactory = Terms.get();
            UUID uuid = (UUID) process.getProperty(uuidPropName);
            I_GetConceptData concept = termFactory.getConcept(uuid);
            String pt = getLatestPreferredTerm(concept);
            if (pt != null) {
                process.setProperty(ptPropName, pt);
            }

            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private String getLatestPreferredTerm(I_GetConceptData concept) throws Exception {
        I_TermFactory termFactory = Terms.get();
        I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());

        String latestDescription = null;
        int latestVersion = Integer.MIN_VALUE;

        List<? extends I_DescriptionTuple> descriptionResults =
                concept.getDescriptionTuples(helper.getCurrentStatusIntSet(), allowedTypes, termFactory
                    .getActiveAceFrameConfig().getViewPositionSetReadOnly(), 
                    Terms.get().getActiveAceFrameConfig().getPrecedence(),
                    Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy());

        // find the latest tuple, so that the latest edited version of the
        // description is always used
        for (I_DescriptionTuple descriptionTuple : descriptionResults) {
            if (descriptionTuple.getVersion() > latestVersion) {
                latestVersion = descriptionTuple.getVersion();
                latestDescription = descriptionTuple.getText();
            }
        }

        return latestDescription;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getUuidPropName() {
        return uuidPropName;
    }

    public void setUuidPropName(String uuidPropName) {
        this.uuidPropName = uuidPropName;
    }

    public String getPtPropName() {
        return ptPropName;
    }

    public void setPtPropName(String ptPropName) {
        this.ptPropName = ptPropName;
    }
}
