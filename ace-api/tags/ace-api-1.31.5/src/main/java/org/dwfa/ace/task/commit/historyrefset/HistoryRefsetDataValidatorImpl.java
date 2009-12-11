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
/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.ace.task.commit.historyrefset;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.cement.ArchitectonicAuxiliary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class HistoryRefsetDataValidatorImpl implements HistoryRefsetDataValidator {

    private static final List<UUID> HISTORY_REFSET_UUIDS =
            Arrays.asList(UUID.fromString("0c325625-6250-47d9-a370-246779b69fd4"),
                          UUID.fromString("e1703d6a-7016-4185-afaa-e2f72d0dbad4"),
                          UUID.fromString("f44bd22c-1869-4e29-b6c9-262840fcfc45"));

    private final I_TermFactory termFactory;
    private final boolean forCommit;
    private I_GetConceptData retiredStatus;
    private ConstraintFailureChooser constraintFailureChooser;

    public HistoryRefsetDataValidatorImpl(final I_TermFactory termFactory,
          final ConstraintFailureChooser constraintFailureChooser, final boolean forCommit) throws Exception {
        this.termFactory = termFactory;
        this.forCommit = forCommit;
        retiredStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
        this.constraintFailureChooser = constraintFailureChooser;
    }

    public List<AlertToDataConstraintFailure> validate(final I_GetConceptData concept)
            throws Exception {
        List<AlertToDataConstraintFailure> failures = new ArrayList<AlertToDataConstraintFailure>();
        List<I_ThinExtByRefVersioned> refsetsForConcept = getUncommittedRefsetExtensions(concept);
        for (I_ThinExtByRefVersioned refset : refsetsForConcept) {
            List<UUID> refsetUUID = getRefsetUUID(refset);
            if (isCurrentMemberOfHistoryRefset(refset, refsetUUID)) {
                addAnAlertIfTheConceptIsNotRetired(concept, failures, refset);
            }
        }

        return failures;
    }

    private boolean isCurrentMemberOfHistoryRefset(final I_ThinExtByRefVersioned refset,
                                                       final List<UUID> refsetUUID) throws Exception {
        return isMemberOfHistoryRefset(refsetUUID) && !isRefsetMembershipRetired(refset);
    }

    private boolean isRefsetMembershipRetired(final I_ThinExtByRefVersioned refset) throws Exception {
        return isRefsetRetired(refset);
    }

    private List<UUID> getRefsetUUID(final I_ThinExtByRefVersioned refset)
            throws Exception {
        return termFactory.getConcept(refset.getRefsetId()).getUids();
    }

    private void addAnAlertIfTheConceptIsNotRetired(final I_GetConceptData concept,
        final List<AlertToDataConstraintFailure> failures, final I_ThinExtByRefVersioned refset) throws Exception {
        if (!isConceptRetired(concept)) {
            constraintFailureChooser.constrain(forCommit, concept, refset, failures);
        }
    }

    private boolean isConceptRetired(final I_GetConceptData concept) throws Exception {
        return isVersionRetired(concept.getConceptAttributes().getVersions());
    }

    private boolean isRefsetRetired(final I_ThinExtByRefVersioned refset) throws Exception {
        return isVersionRetired(refset.getVersions());
    }

    private boolean isVersionRetired(final List<? extends I_AmPart> versions) throws Exception {
        I_AmPart latestVersion = getLatestVersion(versions);
        I_GetConceptData conceptStatus = getConceptForStatus(latestVersion, termFactory);
        return conceptStatusIsEqualToRetiredStatus(conceptStatus, retiredStatus);
    }

    private I_AmPart getLatestVersion(final List<? extends I_AmPart> versions) {
        I_AmPart latestVersion = null;

        for (I_AmPart versionPart : versions) {
            if (latestVersion == null) {
                latestVersion = versionPart;
                continue;
            }

            latestVersion = isVersionAfterOrEqualToLatestVersion(versionPart, latestVersion)
                    ? versionPart : latestVersion;
        }

        return latestVersion;
    }

    private boolean isVersionAfterOrEqualToLatestVersion(final I_AmPart versionPart,
                                                         final I_AmPart latestVersion) {
        return latestVersion.getVersion() <= versionPart.getVersion();
    }

    private boolean conceptStatusIsEqualToRetiredStatus(final I_GetConceptData conceptStatus,
        final I_GetConceptData retiredStatus) {
        return conceptStatus.getConceptId() == retiredStatus.getConceptId();
    }

    private I_GetConceptData getConceptForStatus(final I_AmPart versionPart,
        final I_TermFactory termFactory) throws Exception {
        return termFactory.getConcept(versionPart.getStatusId());
    }

    private boolean isMemberOfHistoryRefset(final List<UUID> refsetUUIDList) {
        for (UUID refsetUUID : refsetUUIDList) {
            if (HISTORY_REFSET_UUIDS.contains(refsetUUID)) {
                return true;
            }
        }

        return false;
    }

    private List<I_ThinExtByRefVersioned> getUncommittedRefsetExtensions(final I_GetConceptData concept)
            throws Exception {
        return termFactory.getAllExtensionsForComponent(concept.getConceptId(), true);
    }
}
