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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;

public abstract class AbstractExtensionTest extends AbstractDataConstraintTest {

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
    public final List<AlertToDataConstraintFailure> test(I_Transact component, boolean forCommit)
            throws TaskFailedException {
        return new ArrayList<AlertToDataConstraintFailure>();
    }

    @Override
    public abstract List<AlertToDataConstraintFailure> test(I_ExtendByRef extension, boolean forCommit)
            throws TaskFailedException;

    public I_GetConceptData getConceptSafe(I_TermFactory termFactory, Collection<UUID> concepts) throws Exception {
        if (termFactory.hasId(concepts)) {
            return termFactory.getConcept(concepts);
        }
        return null;
    }

    public PositionSetReadOnly getPositions(I_TermFactory termFactory) throws Exception {
        return termFactory.getActiveAceFrameConfig().getViewPositionSetReadOnly();
        /*
         * I_ConfigAceFrame activeProfile = termFactory.getActiveAceFrameConfig();
         * Set<PathBI> editingPaths = activeProfile.getEditingPathSet();
         * Set<I_Position> allPositions = new HashSet<I_Position>();
         * for (PathBI path : editingPaths) {
         * allPositions.add(termFactory.newPosition(path, Integer.MAX_VALUE));
         * for (I_Position position : path.getOrigins()) {
         * addOriginPositions(termFactory, position, allPositions);
         * }
         * }
         * return new PositionSetReadOnly(allPositions);
         */
    }

    public I_IntSet getActiveStatus(I_TermFactory termFactory) throws Exception {
        I_IntSet activeSet = Terms.get().newIntSet();
        I_ConfigAceFrame config = getFrameConfig();
        if (config == null) {
            config = NewDefaultProfile.newProfile("fullname", "username", "password", "adminUsername", "adminPassword");
        }
        activeSet.addAll(config.getAllowedStatus().getSetValues());
        for (ArchitectonicAuxiliary.Concept con : Arrays.asList(ArchitectonicAuxiliary.Concept.ACTIVE,
            ArchitectonicAuxiliary.Concept.CURRENT, ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED,
            ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED, ArchitectonicAuxiliary.Concept.LIMITED,
            ArchitectonicAuxiliary.Concept.PENDING_MOVE, ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE,
            ArchitectonicAuxiliary.Concept.PROMOTED)) {
            I_GetConceptData c = getConceptSafe(termFactory, con.getUids());
            if (c != null) {
                activeSet.add(c.getConceptNid());
            }
        }
        return activeSet;
    }

    /**
     * Gets the latest specified relationship's target.
     * 
     * @param relationshipType
     * @return
     * @throws Exception
     */
    public I_GetConceptData getLatestRelationshipTarget(I_GetConceptData concept, I_GetConceptData relationshipType)
            throws Exception {

        I_GetConceptData latestTarget = null;
        int latestVersion = Integer.MIN_VALUE;

        I_IntSet allowedTypes = Terms.get().newIntSet();
        allowedTypes.add(relationshipType.getConceptNid());

        I_TermFactory termFactory = Terms.get();
        PositionSetReadOnly allPositions = getPositions(termFactory);
        I_IntSet activeStatuses = getActiveStatus(termFactory);

        List<? extends I_RelTuple> relationships =
                concept.getSourceRelTuples(activeStatuses, allowedTypes, allPositions,
                getFrameConfig().getPrecedence(), getFrameConfig().getConflictResolutionStrategy());
        for (I_RelTuple rel : relationships) {
            if (rel.getVersion() > latestVersion) {
                latestVersion = rel.getVersion();
                latestTarget = Terms.get().getConcept(rel.getC2Id());
            }
        }

        return latestTarget;
    }
}
