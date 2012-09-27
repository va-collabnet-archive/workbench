/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dwfa.ace.task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.dwfa.ace.api.*;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.binding.snomed.Snomed;

@BeanList(specs = {
    @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN)})
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

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        ConceptChronicleBI newConcept = null;

        try {
            @SuppressWarnings("unused")
            /*
             * Get config from worker.
             */
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            
            /*
             * Get builder to create concepts from blueprints.
             */
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            
            /*
             * Get concept which is displayed in the classic view when NewConcept task
             * is executed. This will be the parent concept for the new concept.
             */
            I_HostConceptPlugins host = (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
            ConceptChronicleBI parentConcept = Ts.get().getConcept(host.getTermComponent().getConceptNid());

            /*
             * Create the bluprint for the new concept. Provide fsn and pref
             * term text. Language is EN. Use Snomed Is a (not: is a).
             */
            ConceptCB conceptBp = new ConceptCB("new concept (tag)",
                    "new concept",
                    LANG_CODE.EN,
                    Snomed.IS_A.getLenient().getPrimUuid(),
                    parentConcept.getPrimUuid());
            
            /*
             * Set UUID to be random. Normally computed with a hash of fsn, pref
             * term and parents. Since text is alwasy 'new concept' for a clone,
             * this would result in concepts with the same UUID. No need to set
             * to random if creating a unique concept.
             */
            conceptBp.setComponentUuid(UUID.randomUUID());

            /*
             * Get fsn and pref blueprints. This creates the blueprints if they
             * don't already exist.
             */
            List<DescriptionCAB> fsnCABs = conceptBp.getFullySpecifiedNameCABs();
            List<DescriptionCAB> prefCABs = conceptBp.getPreferredNameCABs();

            /*
             * Add fsn and add pref term. This adds them with the appropriate
             * dialect annotations.
             */
            for (DescriptionCAB fsn : fsnCABs) {
                conceptBp.addFullySpecifiedName(fsn, LANG_CODE.EN);
            }

            for (DescriptionCAB pref : prefCABs) {
                conceptBp.addFullySpecifiedName(pref, LANG_CODE.EN);
            }

            /*
             * Construct new concept. This will construct all of the blueprints
             * within the concept as well.
             */
            newConcept = builder.construct(conceptBp);

            host.unlink();
            I_AmTermComponent newTerm = (I_AmTermComponent) newConcept;
            host.setTermComponent(newTerm);
            Ts.get().addUncommitted(newConcept);

            return Condition.CONTINUE;
        } catch (NoSuchAlgorithmException e) {
            undoEdits(newConcept, Terms.get());
            throw new TaskFailedException(e);
        } catch (InvalidCAB e) {
            undoEdits(newConcept, Terms.get());
            throw new TaskFailedException(e);
        } catch (ContradictionException e) {
            undoEdits(newConcept, Terms.get());
            throw new TaskFailedException(e);
        } catch (IOException e) {
            undoEdits(newConcept, Terms.get());
            throw new TaskFailedException(e);
        }
    }

    private void undoEdits(ConceptChronicleBI newConcept, I_TermFactory termFactory) {
        if (termFactory != null) {
            if (newConcept != null) {
                try {
                    termFactory.forget((I_GetConceptData) newConcept);
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
        return new int[]{};
    }
}
