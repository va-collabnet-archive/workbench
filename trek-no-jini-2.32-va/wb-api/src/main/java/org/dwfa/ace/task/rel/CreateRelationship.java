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
package org.dwfa.ace.task.rel;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@AllowDataCheckSuppression
@BeanList(specs = { @Spec(directory = "tasks/ide/relationship", type = BeanType.TASK_BEAN) })
public class CreateRelationship extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT.getAttachmentKey();

    private String relParentPropName = ProcessAttachmentKeys.REL_PARENT.getAttachmentKey();

    private TermEntry relType = new TermEntry(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
    private TermEntry relCharacteristic = new TermEntry(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
    private TermEntry relRefinability = new TermEntry(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
    private TermEntry relStatus = new TermEntry(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(relParentPropName);
        out.writeObject(activeConceptPropName);
        out.writeObject(relType);
        out.writeObject(relCharacteristic);
        out.writeObject(relRefinability);
        out.writeObject(relStatus);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            relParentPropName = (String) in.readObject();
            activeConceptPropName = (String) in.readObject();
            relType = (TermEntry) in.readObject();
            relCharacteristic = (TermEntry) in.readObject();
            relRefinability = (TermEntry) in.readObject();
            relStatus = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());

            I_GetConceptData concept = (I_GetConceptData) process.getProperty(activeConceptPropName);
            if (config.getEditingPathSet().size() == 0) {
                throw new TaskFailedException("You must select at least one editing path. ");
            }

            I_GetConceptData relParentConcept = (I_GetConceptData) process.getProperty(relParentPropName);

            Terms.get().newRelationship(UUID.randomUUID(), concept,
                Terms.get().getConcept(relType.ids), relParentConcept,
                Terms.get().getConcept(relCharacteristic.ids),
                Terms.get().getConcept(relRefinability.ids),
                Terms.get().getConcept(relStatus.ids), 0, config);
            Terms.get().addUncommitted(concept);
            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getActiveConceptPropName() {
        return activeConceptPropName;
    }

    public void setActiveConceptPropName(String propName) {
        this.activeConceptPropName = propName;
    }

    public String getRelParentPropName() {
        return relParentPropName;
    }

    public void setRelParentPropName(String newStatusPropName) {
        this.relParentPropName = newStatusPropName;
    }

    public TermEntry getRelCharacteristic() {
        return relCharacteristic;
    }

    public void setRelCharacteristic(TermEntry relCharacteristic) {
        this.relCharacteristic = relCharacteristic;
    }

    public TermEntry getRelRefinability() {
        return relRefinability;
    }

    public void setRelRefinability(TermEntry relRefinability) {
        this.relRefinability = relRefinability;
    }

    public TermEntry getRelType() {
        return relType;
    }

    public void setRelType(TermEntry relType) {
        this.relType = relType;
    }

    public TermEntry getRelStatus() {
        return relStatus;
    }

    public void setRelStatus(TermEntry relStatus) {
        this.relStatus = relStatus;
    }

}
