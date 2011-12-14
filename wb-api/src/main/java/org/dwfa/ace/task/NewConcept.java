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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.*;
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
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescCAB;
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

        try {@SuppressWarnings("unused")
            // here to demo how to get the configuration.
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());

            I_HostConceptPlugins host = (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
            ConceptChronicleBI concept = Ts.get().getConcept(host.getTermComponent().getConceptNid());
            
            ConceptCB conceptBp = new ConceptCB("new concept (tag)",
                    "new concept",
                    LANG_CODE.EN,
                    Snomed.IS_A.getLenient().getPrimUuid(),
                    concept.getPrimUuid());
            conceptBp.setComponentUuid(UUID.randomUUID());
            List<DescCAB> fsnCABs = conceptBp.getFsnCABs();
            List<DescCAB> prefCABs = conceptBp.getPrefCABs();
            
            for(DescCAB fsn : fsnCABs){
                conceptBp.addFsn(fsn, LANG_CODE.EN);
            }
            
            for(DescCAB pref : prefCABs){
                conceptBp.addFsn(pref, LANG_CODE.EN);
            }
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
