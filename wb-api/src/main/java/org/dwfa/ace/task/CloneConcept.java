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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

@BeanList(specs = { @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN) })
public class CloneConcept extends AbstractTask {

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
            I_TermFactory tf = Terms.get();
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            Set<PositionBI> positionSet = new HashSet<PositionBI>();
            for (PathBI path : config.getEditingPathSet()) {
            	positionSet.add(tf.newPosition(path, Long.MAX_VALUE));
            }
            PositionSetReadOnly clonePositions = new PositionSetReadOnly(positionSet);
            I_HostConceptPlugins host = (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());

            I_GetConceptData conceptToClone = (I_GetConceptData) host.getTermComponent();
            if (conceptToClone == null) {
                throw new TaskFailedException("There is no concept in the component view to clone...");
            }

            newConcept = Terms.get().newConcept(UUID.randomUUID(), false, config);

            for (I_DescriptionTuple desc : conceptToClone.getDescriptionTuples(config.getAllowedStatus(), null,
                clonePositions, config.getPrecedence(), config.getConflictResolutionStrategy())) {
                tf.newDescription(UUID.randomUUID(), newConcept, desc.getLang(), "Clone of " + desc.getText(),
                    tf.getConcept(desc.getTypeId()), config);
            }

            for (I_RelTuple rel : conceptToClone.getSourceRelTuples(config.getAllowedStatus(), null, clonePositions,
                config.getPrecedence(), config.getConflictResolutionStrategy())) {
                tf.newRelationship(UUID.randomUUID(), newConcept, tf.getConcept(rel.getTypeId()),
                    tf.getConcept(rel.getC2Id()), tf.getConcept(rel.getCharacteristicId()),
                    tf.getConcept(rel.getRefinabilityId()), tf.getConcept(rel.getStatusId()), rel.getGroup(), config);
            }

            host.unlink();
            host.setTermComponent(newConcept);
            Terms.get().addUncommitted(newConcept);

            return Condition.CONTINUE;
        } catch (TerminologyException e) {
            undoEdits(newConcept, Terms.get());
            throw new TaskFailedException(e);
        } catch (IOException e) {
            undoEdits(newConcept, Terms.get());
            throw new TaskFailedException(e);
        }
    }

    private void undoEdits(I_GetConceptData newConcept, I_TermFactory termFactory) {
        if (termFactory != null) {
            if (newConcept != null) {
                try {
					termFactory.forget(newConcept);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
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
