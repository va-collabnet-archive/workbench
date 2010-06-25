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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class NewConcept extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    /**
     * @TODO use a type 1 uuid generator instead of a random uuid...
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        I_GetConceptData newConcept = null;
        try {
            @SuppressWarnings("unused")
            // here to demo how to get the configuration.
            I_GetConceptData concept = (I_GetConceptData) worker.readAttachement(ProcessAttachmentKeys.I_GET_CONCEPT_DATA.name());

            @SuppressWarnings("unused")
            // here to demo how to get the configuration.
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            I_HostConceptPlugins host = (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());

            newConcept = LocalVersionedTerminology.get().newConcept(UUID.randomUUID(), false, config);

            LocalVersionedTerminology.get().newDescription(UUID.randomUUID(), newConcept, "en",
                "New Fully Specified Description",
                ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), config);

            LocalVersionedTerminology.get().newDescription(UUID.randomUUID(), newConcept, "en",
                "New Preferred Description", ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(),
                config);

            LocalVersionedTerminology.get().newRelationship(UUID.randomUUID(), newConcept, config);

            host.setTermComponent(newConcept);

            return Condition.CONTINUE;
        } catch (TerminologyException e) {
            undoEdits(newConcept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        } catch (IOException e) {
            undoEdits(newConcept, LocalVersionedTerminology.get());
            throw new TaskFailedException(e);
        }
    }

    private void undoEdits(I_GetConceptData newConcept, I_TermFactory termFactory) {
        if (termFactory != null) {
            if (newConcept != null) {
                termFactory.forget(newConcept);
            }
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
