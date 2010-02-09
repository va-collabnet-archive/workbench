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
package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = {
        @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
        @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForIsa extends AbstractConceptTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public List<AlertToDataConstraintFailure> test(I_GetConceptData concept,
            boolean forCommit) throws TaskFailedException {
        try {
            ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            I_ConfigAceFrame activeProfile = termFactory
                    .getActiveAceFrameConfig();
            Set<I_Path> editingPaths = new HashSet<I_Path>(activeProfile
                    .getEditingPathSet());
            I_GetConceptData is_a = null;
            I_GetConceptData is_a_rel_aux = null;

            // check that the SNOMED is-a exists in the current database before using it
            if (termFactory.hasId(SNOMED.Concept.IS_A.getUids().iterator().next())) {
                is_a = termFactory.getConcept(SNOMED.Concept.IS_A.getUids());
            }

            // check that the Architectonic is-a exists before using it
            if (termFactory.hasId(ArchitectonicAuxiliary.Concept.IS_A_REL
                    .getUids().iterator().next())) {
                 is_a_rel_aux = termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
                         .getUids());
            }

            Set<I_Position> positions = new HashSet<I_Position>();
            for (I_Path path : editingPaths) {
                positions.add(termFactory.newPosition(path, Integer.MAX_VALUE));
            }
            for (I_RelTuple rel : concept.getSourceRelTuples(activeProfile
                    .getAllowedStatus(), null, positions, true)) {
                if ((is_a != null && rel.getRelTypeId() == is_a.getConceptId())
                        || (is_a_rel_aux != null && rel.getRelTypeId() == is_a_rel_aux.getConceptId()))
                    return alertList;
            }
            alertList.add(new AlertToDataConstraintFailure(
                    AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
                    "<html>No IS_A relationship", concept));
            return alertList;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

}
