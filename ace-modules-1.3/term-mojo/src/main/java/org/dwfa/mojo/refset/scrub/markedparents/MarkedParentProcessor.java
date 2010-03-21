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
package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.refset.scrub.util.CandidateWriter;
import org.dwfa.mojo.refset.scrub.util.TerminologyFactoryUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Puts "marked parents" and "normal members" into logical structures that can
 * be used later.
 */
public final class MarkedParentProcessor {

    private static final String NORMAL_MEMBER_UUID = "cc624429-b17d-4ac5-a69e-0b32448aaf3c";
    private static final String NORMAL_MEMBER_DESC = "normal member";

    private final List<ComponentRefsetKey> normalMemberList;
    private final DuplicateMarkedParentMarker duplicateMarkedParentMarker;
    private final CandidateWriter candidateWriter;
    private final List<Integer> validTypeIds;
    private final int currentStatusId;
    private final int retiredStatusId;
    private final int normalMemberId;

    public MarkedParentProcessor(final CandidateWriter candidateWriter, final List<Integer> validTypeIds)
            throws Exception {
        this.candidateWriter = candidateWriter;
        this.validTypeIds = validTypeIds;

        normalMemberList = new ArrayList<ComponentRefsetKey>();
        duplicateMarkedParentMarker = new DuplicateMarkedParentMarker();
        normalMemberId = new ConceptDescriptor(NORMAL_MEMBER_UUID, NORMAL_MEMBER_DESC).getVerifiedConcept()
            .getId()
            .getNativeId();

        TerminologyFactoryUtil terminologyFactoryUtil = new TerminologyFactoryUtil();
        retiredStatusId = terminologyFactoryUtil.getNativeConceptId(ArchitectonicAuxiliary.Concept.RETIRED);
        currentStatusId = terminologyFactoryUtil.getNativeConceptId(ArchitectonicAuxiliary.Concept.CURRENT);
    }

    public void process(final String memberRefsetName, final List<I_ThinExtByRefVersioned> refsetMembers)
            throws Exception {
        for (I_ThinExtByRefVersioned member : refsetMembers) {
            List<? extends I_ThinExtByRefPart> versions = member.getVersions();
            for (I_ThinExtByRefPart version : versions) {
                if (version instanceof I_ThinExtByRefPartConcept && isCurrentOrRetired(version)) {
                    int inclusionType = ((I_ThinExtByRefPartConcept) version).getConceptId();
                    processType(memberRefsetName, member, version, inclusionType);
                }
            }
        }
    }

    private void processType(final String memberRefsetName, final I_ThinExtByRefVersioned member,
            final I_ThinExtByRefPart version, final int inclusionType) throws Exception {
        if (isMarkedParent(inclusionType)) {
            duplicateMarkedParentMarker.put(member);
            candidateWriter.logCandidate(memberRefsetName, member);
            return;
        }

        if (isCurrent(version) && isNormalMember(inclusionType)) { // only care
                                                                   // about
                                                                   // current
                                                                   // normal
                                                                   // members
            normalMemberList.add(new ComponentRefsetKey(member));
            candidateWriter.logCandidate(memberRefsetName, member);
        }
    }

    public List<ComponentRefsetMembers> getDuplicateMarkedParentMarker() {
        return duplicateMarkedParentMarker.getDuplicates();
    }

    public List<ComponentRefsetKey> getNormalMembers() {
        return normalMemberList;
    }

    private boolean isCurrentOrRetired(final I_ThinExtByRefPart version) {
        return isCurrent(version) || version.getStatus() == retiredStatusId;
    }

    private boolean isMarkedParent(final int inclusionType) throws Exception {
        return validTypeIds.contains(Integer.valueOf(inclusionType));
    }

    private boolean isNormalMember(final int inclusionType) throws Exception {
        return normalMemberId == inclusionType;
    }

    public boolean isCurrent(final I_ThinExtByRefPart version) {
        return version.getStatus() == currentStatusId;
    }
}
