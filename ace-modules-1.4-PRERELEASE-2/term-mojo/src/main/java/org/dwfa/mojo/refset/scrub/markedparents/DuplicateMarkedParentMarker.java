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
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.mojo.refset.scrub.util.TerminologyFactoryUtil;
import org.dwfa.cement.ArchitectonicAuxiliary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Members <code>I_ThinExtByRefVersioned</code> supplied through the
 * {@link #put(I_ThinExtByRefVersioned)} mehtod are sorted by component and
 * referenceset.
 * 
 * This list of members is then processed when {@link #getDuplicates()} is
 * called to return a list of duplicate members having
 * which are "marked parents" having a status of "current". Any "retired"
 * members are removed.
 */
public final class DuplicateMarkedParentMarker {

    private final Map<ComponentRefsetKey, ComponentRefsetMembers> memberMap;
    private final int currentStatusId;

    public DuplicateMarkedParentMarker() throws Exception {
        currentStatusId = new TerminologyFactoryUtil().getNativeConceptId(ArchitectonicAuxiliary.Concept.CURRENT);
        memberMap = new HashMap<ComponentRefsetKey, ComponentRefsetMembers>();
    }

    /**
     * Call this for each member that has a "marked parent" concept and which
     * has a status of "current" or "retired".
     * 
     * @param member The marked parent.
     */
    public void put(final I_ThinExtByRefVersioned member) {
        ComponentRefsetKey key = new ComponentRefsetKey(member);

        if (!memberMap.containsKey(key)) {
            memberMap.put(key, new ComponentRefsetMembers(key));
        }

        memberMap.get(key).addMember(member);
    }

    /**
     * Returns a <code>List<ComponentRefsetMembers></code> which identify the
     * "marked parents" that are currently
     * active and are duplicates.
     * 
     * @return A <code>List<ComponentRefsetMembers></code> of "marked parents".
     */
    public List<ComponentRefsetMembers> getDuplicates() {
        List<ComponentRefsetMembers> duplicateList = new ArrayList<ComponentRefsetMembers>();

        for (ComponentRefsetMembers componentRefsetMembers : memberMap.values()) {
            removeRetiredMembers(componentRefsetMembers);
            duplicateList.add(componentRefsetMembers);
        }

        return duplicateList;
    }

    private void removeRetiredMembers(final ComponentRefsetMembers componentRefsetMembers) {
        for (I_ThinExtByRefVersioned member : componentRefsetMembers.getMembers()) {
            TreeSet<I_ThinExtByRefPart> sortedVersionsSet = new TreeSet<I_ThinExtByRefPart>(
                new LatestVersionComparator());
            sortedVersionsSet.addAll(member.getVersions());
            if (sortedVersionsSet.last().getStatus() != currentStatusId) { // ignore
                                                                           // non-current
                                                                           // statuses
                componentRefsetMembers.removeMember(member);
            }
        }
    }
}
