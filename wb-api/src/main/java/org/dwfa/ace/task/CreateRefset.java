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
package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.PathBI;

@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class CreateRefset extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * The refset name. Property key.
     */
    private String refsetNamePropertyKey = "A: REFSET_NAME";

    public String getRefsetNamePropertyKey() {
        return refsetNamePropertyKey;
    }

    public void setRefsetNamePropertyKey(String refsetName) {
        this.refsetNamePropertyKey = refsetName;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.refsetNamePropertyKey);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            this.refsetNamePropertyKey = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    protected I_GetConceptData createRefsetConcept(String refsetName) throws Exception {
        I_TermFactory termFactory = Terms.get();
        I_GetConceptData fully_specified_description_type = termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
        I_GetConceptData preferred_description_type = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
        I_ConfigAceFrame config = termFactory.newAceFrameConfig();
        PathBI path = termFactory.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
        config.addEditingPath(path);
        config.setDefaultStatus(termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
        UUID uuid = UUID.randomUUID();
        // UUID uuid = UUID.fromString(this.refsetUuid);
        I_GetConceptData newConcept = termFactory.newConcept(uuid, false, config);
        // Install the FSN
        termFactory.newDescription(UUID.randomUUID(), newConcept, "en", refsetName, fully_specified_description_type,
            config);
        // Install the preferred term
        termFactory.newDescription(UUID.randomUUID(), newConcept, "en", refsetName, preferred_description_type, config);
        termFactory.newRelationship(UUID.randomUUID(), newConcept,
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()),
            termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, config);
        termFactory.newRelationship(UUID.randomUUID(), newConcept,
            termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids()),
            termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()), 0, config);
        termFactory.commit();
        return newConcept;
    }

    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            String refsetName = (String) process.getProperty(this.refsetNamePropertyKey);
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            I_GetConceptData con = createRefsetConcept(refsetName);
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1) throws TaskFailedException {
        // TODO Auto-generated method stub

    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }
}
