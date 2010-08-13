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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForStatusForRefset extends AbstractConceptTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    @Override
    public List<AlertToDataConstraintFailure> test(I_GetConceptData concept, boolean forCommit)
            throws TaskFailedException {
        try {
            ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            I_ConfigAceFrame activeProfile = termFactory.getActiveAceFrameConfig();

            Set<I_Position> allPositions = getPositions(termFactory);

            I_IntSet actives = getActiveStatus(termFactory);

            AceLog.getAppLog().info("Testing for status for refset: " + concept);
            for (I_ConceptAttributeTuple rel : concept.getConceptAttributeTuples(activeProfile.getAllowedStatus(),
                allPositions, true, true)) {
                if (actives.contains(rel.getConceptStatus()))
                    return alertList;
            }

            I_GetConceptData refset_con = getConceptSafe(termFactory, RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());
            if (refset_con == null)
                return alertList;

            I_IntSet types = termFactory.newIntSet();
            I_GetConceptData isa_con;
            isa_con = getConceptSafe(termFactory, SNOMED.Concept.IS_A.getUids());
            if (isa_con != null)
                types.add(isa_con.getConceptId());
            isa_con = getConceptSafe(termFactory, ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
            if (isa_con != null)
                types.add(isa_con.getConceptId());

            for (I_GetConceptData refset : refset_con.getDestRelOrigins(activeProfile.getAllowedStatus(), types,
                allPositions, true, true)) {
                // System.out.println(refset.getInitialText());
                for (I_ThinExtByRefVersioned mem : termFactory.getRefsetExtensionMembers(refset.getConceptId())) {
                    // List<I_ThinExtByRefVersioned> extensions = termFactory
                    // .getAllExtensionsForComponent(refset.getConceptId(),
                    // true);
                    // for (I_ThinExtByRefVersioned ext : extensions) {
                    // System.out.println(ext.getComponentId() + " "
                    // + termFactory.getConcept(ext.getComponentId()));
                    if (mem.getComponentId() == concept.getConceptId()) {
                        alertList.add(new AlertToDataConstraintFailure(AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
                            "<html>Refset, but inactive", concept));
                        return alertList;
                    }
                }
            }
            return alertList;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

}
