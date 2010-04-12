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
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForIsa extends AbstractConceptTest {

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
            List<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();

            if (isRootConcept(concept)) {
                return alertList;
            }

            if (hasNoIsaRelationships(concept)) {
                addWarning(concept, alertList);
            }

            return alertList;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private boolean hasNoIsaRelationships(final I_GetConceptData concept) throws Exception {
        return getSourceRelationships(concept).isEmpty();
    }

    private I_TermFactory getTermFactory() {
        return Terms.get();
    }

    private boolean isRootConcept(final I_GetConceptData concept) throws Exception {
        return getActiveProfile().getRoots().contains(concept.getConceptId());
    }

    private I_ConfigAceFrame getActiveProfile() throws TerminologyException, IOException {
        return getTermFactory().getActiveAceFrameConfig();
    }

    private I_IntSet getIsARelationshipList(final I_TermFactory termFactory) throws Exception {
        I_GetConceptData snomed_isa = getConceptSafe(termFactory, SNOMED.Concept.IS_A.getUids());
        I_GetConceptData aux_isa = getConceptSafe(termFactory, ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());

        if (snomed_isa == null && aux_isa == null) {
            throw new IllegalStateException("Could not find SNOMED 'IS A' or ArchitectonicAuxiliary 'is a'.");
        }

        I_IntSet isARelationships = getTermFactory().newIntSet();
        addRelationship(isARelationships, snomed_isa);
        addRelationship(isARelationships, aux_isa);
        return isARelationships;
    }

    private void addRelationship(final I_IntSet relationships, final I_GetConceptData isaConcept) {
        if (isaConcept != null) {
            relationships.add(isaConcept.getConceptId());
        }
    }

    private void addWarning(final I_GetConceptData concept, final List<AlertToDataConstraintFailure> alertList) {
        alertList.add(new AlertToDataConstraintFailure(AlertToDataConstraintFailure.ALERT_TYPE.WARNING,
            "<html>No IS_A relationship", concept));
    }

    private List<? extends I_RelTuple> getSourceRelationships(final I_GetConceptData concept) throws Exception {
        I_IntSet isARelationships = getIsARelationshipList(getTermFactory());
        PositionSetReadOnly allPositions = null;

        return concept.getSourceRelTuples(getActiveProfile().getAllowedStatus(), isARelationships, allPositions,
            getFrameConfig().getPrecedence(), getFrameConfig().getConflictResolutionStrategy());
    }
}
