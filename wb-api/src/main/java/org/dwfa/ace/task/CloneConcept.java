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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

@BeanList(specs = {
    @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN)})
public class CloneConcept extends AbstractTask {

    /*
     *See notes in NewConcept first.
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
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            ViewCoordinate vc = config.getViewCoordinate();
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), vc);
            I_HostConceptPlugins host = (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());

            I_GetConceptData c = (I_GetConceptData) host.getTermComponent();
            ConceptVersionBI original = Ts.get().getConceptVersion(vc, c.getNid());
            if (original == null) {
                throw new TaskFailedException("There is no concept in the component view to clone...");
            }
            
            /*
             * Make blueprint from the orginal concept. Copies exactly.
             */
            ConceptCB conceptBp = original.makeBlueprint();
            
            /*
             * Add clone of to the text for descriptons. Update fsn or pref term will also
             * recompute UUID to match based on the new hash.
             */
            List<DescriptionCAB> fsnCABs = conceptBp.getFullySpecifiedNameCABs();
            for(DescriptionCAB fsnBp : fsnCABs){
                String text = fsnBp.getText();
                conceptBp.updateFullySpecifiedName("Clone of " + text, fsnBp, null);
            }
            List<DescriptionCAB> prefCABs = conceptBp.getPreferredNameCABs();
            for(DescriptionCAB prefBp : prefCABs){
                String text = prefBp.getText();
                conceptBp.updatePreferredName("Clone of " + text, prefBp, null);
            }
            /*
             * Set to random since text will be generated with 'Clone of'
             */
            conceptBp.setComponentUuid(UUID.randomUUID());
            
            newConcept = builder.construct(conceptBp);
            host.unlink();
            I_AmTermComponent newTerm = (I_AmTermComponent) newConcept;
            host.setTermComponent(newTerm);
            Ts.get().addUncommitted(newConcept);

            return Condition.CONTINUE;
        } catch (NoSuchAlgorithmException e) {
            undoEdits(newConcept);
            throw new TaskFailedException(e);
        } catch (UnsupportedEncodingException e) {
            undoEdits(newConcept);
            throw new TaskFailedException(e);
        } catch (InvalidCAB e) {
            undoEdits(newConcept);
            throw new TaskFailedException(e);
        } catch (ContradictionException e) {
            undoEdits(newConcept);
            throw new TaskFailedException(e);
        } catch (IOException e) {
            undoEdits(newConcept);
            throw new TaskFailedException(e);
        }
    }

    private void undoEdits(ConceptChronicleBI concept) {
        try {
            Ts.get().forget(concept);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }
}
