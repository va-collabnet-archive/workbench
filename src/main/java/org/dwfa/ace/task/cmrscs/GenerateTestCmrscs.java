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
package org.dwfa.ace.task.cmrscs;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.ComponentValidator;
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

@BeanList(specs = { @Spec(directory = "tasks/ide/cmrscs", type = BeanType.TASK_BEAN) })
public class GenerateTestCmrscs extends AbstractTask {

    private String rootDirStr = "profiles";

    private Boolean validateChangeSets = true;

    private String validators = ComponentValidator.class.getName();

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 3;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(rootDirStr);
        out.writeBoolean(validateChangeSets);
        out.writeObject(validators);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            rootDirStr = (String) in.readObject();
            if (objDataVersion > 1) {
                validateChangeSets = in.readBoolean();
            } else {
                validateChangeSets = true;
            }

            if (objDataVersion > 2) {
                validators = (String) in.readObject();
            } else {
                validators = ComponentValidator.class.getName();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    private static class ConceptIterator implements I_ProcessConcepts {
        int count = 0;
        DataOutputStream dos;
        UUID currentStatus = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next();
        UUID conceptValue = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next();

        public ConceptIterator(DataOutputStream dos) {
            super();
            this.dos = dos;
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            if (count < 70000) {
                writeUuid(UUID.randomUUID(), dos);
                writeUuid(concept.getUids(), dos);
                writeUuid(conceptValue, dos);
                writeUuid(currentStatus, dos);
                count++;
            }
        }

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        File testCsFile = new File(rootDirStr, "test." + UUID.randomUUID().toString() + ".cmrscs");
        try {
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(testCsFile)));

            // commit time
            dos.writeLong(System.currentTimeMillis());
            // path
            writeUuid(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids(), dos);
            // refset
            writeUuid(RefsetAuxiliary.Concept.REFSET_AUXILIARY.getUids(), dos);

            LocalVersionedTerminology.get().iterateConcepts(new ConceptIterator(dos));

            UUID endUid = new UUID(0, 0);

            writeUuid(endUid, dos);

            dos.close();

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public static void writeUuid(Collection<UUID> uuidList, DataOutputStream dos) throws IOException {
        UUID uuid = uuidList.iterator().next();
        dos.writeLong(uuid.getMostSignificantBits());
        dos.writeLong(uuid.getLeastSignificantBits());
    }

    public static void writeUuid(UUID uuid, DataOutputStream dos) throws IOException {
        dos.writeLong(uuid.getMostSignificantBits());
        dos.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do.

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * @return Returns the message.
     */
    public String getRootDirStr() {
        return rootDirStr;
    }

    /**
     * @param message
     *            The message to set.
     */
    public void setRootDirStr(String rootDirStr) {
        this.rootDirStr = rootDirStr;
    }

    public Boolean getValidateChangeSets() {
        return validateChangeSets;
    }

    public void setValidateChangeSets(Boolean validateChangeSets) {
        this.validateChangeSets = validateChangeSets;
    }

    public String getValidators() {
        return validators;
    }

    public void setValidators(String validators) {
        this.validators = validators;
    }
}
