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
package org.dwfa.mojo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;

import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/refsets", type = BeanType.TASK_BEAN) })
public class CalculateMemberSetTask extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private TermEntry root;
    private TermEntry refset;
    private TermEntry memberset;
    private TermEntry membersetPath;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(root);
        out.writeObject(refset);
        out.writeObject(memberset);
        out.writeObject(membersetPath);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            root = (TermEntry) in.readObject();
            refset = (TermEntry) in.readObject();
            memberset = (TermEntry) in.readObject();
            membersetPath = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            VodbCalculateMemberSet calculator = new VodbCalculateMemberSet();

            // set up root descriptor
            ConceptDescriptor rootDescriptor = new ConceptDescriptor();
            I_GetConceptData rootConcept = LocalVersionedTerminology.get().getConcept(root.ids);
            if (rootConcept == null) {
                throw new TaskFailedException("rootConcept is null. Ids: " + Arrays.asList(root.ids));
            }
            rootDescriptor.setUuid((rootConcept.getUids().iterator().next()).toString());
            rootDescriptor.setDescription(calculator.getFsnFromConceptId(rootConcept.getConceptId())); // TODO
            calculator.setRootDescriptor(rootDescriptor);

            // set up ref set (spec) descriptor
            ConceptDescriptor refSetSpecDescriptor = new ConceptDescriptor();
            I_GetConceptData refsetConcept = LocalVersionedTerminology.get().getConcept(refset.ids);
            if (refsetConcept == null) {
                throw new TaskFailedException("refsetConcept is null. Ids: " + Arrays.asList(refset.ids));
            }
            refSetSpecDescriptor.setUuid((refsetConcept.getUids().iterator().next()).toString());
            refSetSpecDescriptor.setDescription(calculator.getFsnFromConceptId(refsetConcept.getConceptId())); // TODO
            calculator.setRootDescriptor(refSetSpecDescriptor);

            // set up member set descriptor
            ConceptDescriptor memberSetSpecDescriptor = new ConceptDescriptor();
            I_GetConceptData membersetConcept = LocalVersionedTerminology.get().getConcept(memberset.ids);
            if (membersetConcept == null) {
                throw new TaskFailedException("membersetConcept is null. Ids: " + Arrays.asList(memberset.ids));
            }
            memberSetSpecDescriptor.setUuid((membersetConcept.getUids().iterator().next()).toString());
            memberSetSpecDescriptor.setDescription(calculator.getFsnFromConceptId(membersetConcept.getConceptId())); // TODO
            calculator.setRootDescriptor(memberSetSpecDescriptor);

            // set up member set path descriptor
            ConceptDescriptor memberSetPathDescriptor = new ConceptDescriptor();
            I_GetConceptData membersetPathConcept = LocalVersionedTerminology.get().getConcept(membersetPath.ids);
            if (membersetPathConcept == null) {
                throw new TaskFailedException("membersetPathConcept is null. Ids: " + Arrays.asList(membersetPath.ids));
            }
            memberSetPathDescriptor.setUuid((membersetPathConcept.getUids().iterator().next()).toString());
            memberSetPathDescriptor.setDescription(calculator.getFsnFromConceptId(membersetPathConcept.getConceptId())); // TODO
            calculator.setRootDescriptor(memberSetPathDescriptor);

            // execute calculate member set plugin

            calculator.execute();

            return Condition.CONTINUE;
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }
}
