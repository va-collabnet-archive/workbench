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

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

import java.util.ArrayList;
import java.util.List;

/**
 * Sifts through the list of supplied "normal members" and "marked parents".
 * Applies 1 of 2 strategies:
 * 
 * 1. If the refset has "marked parents" and a normal member then, return all
 * "marked parents".
 * 2. If the refset has "market parents" but no normal members then return all
 * but 1 "marked parent".
 */
public final class DuplicateMarketParentSifter {

    public List<I_ThinExtByRefVersioned> sift(final MarkedParentProcessor markedParentProcessor) {
        List<I_ThinExtByRefVersioned> sortedMembersList = new ArrayList<I_ThinExtByRefVersioned>();
        SiftingStrategy duplicateMarkedParentStrategy = new DuplicateMarkedParentStrategy(sortedMembersList);
        SiftingStrategy normalMemberStrategy = new NormalMemberStrategy(sortedMembersList);

        List<ComponentRefsetKey> normalMemberList = markedParentProcessor.getNormalMembers();
        List<ComponentRefsetMembers> markedParentList = markedParentProcessor.getDuplicateMarkedParentMarker();

        for (ComponentRefsetMembers markedParentMember : markedParentList) {

            if (isNormalMember(normalMemberList, markedParentMember)) {
                normalMemberStrategy.sift(markedParentMember);
                continue;
            }

            duplicateMarkedParentStrategy.sift(markedParentMember);
        }

        return sortedMembersList;
    }

    private boolean isNormalMember(final List<ComponentRefsetKey> normalMemberList,
            final ComponentRefsetMembers componentRefsetMembers) {
        return normalMemberList.contains(componentRefsetMembers.getComponentRefsetKey());
    }

    private interface SiftingStrategy {

        void sift(ComponentRefsetMembers componentRefsetMembers);
    }

    /**
     * Adds all "marked parent" to the <code>sortedMemberList</code>.
     */
    private final class NormalMemberStrategy implements SiftingStrategy {

        private final List<I_ThinExtByRefVersioned> sortedMembersList;

        private NormalMemberStrategy(final List<I_ThinExtByRefVersioned> sortedMembersList) {
            this.sortedMembersList = sortedMembersList;
        }

        public void sift(final ComponentRefsetMembers markedParentMember) {
            sortedMembersList.addAll(markedParentMember.getMembers());
        }
    }

    /**
     * Adds all but one "marked parent" to the <code>sortedMemberList</code>.
     */
    private final class DuplicateMarkedParentStrategy implements SiftingStrategy {

        private final List<I_ThinExtByRefVersioned> sortedMembersList;

        private DuplicateMarkedParentStrategy(final List<I_ThinExtByRefVersioned> sortedMembersList) {
            this.sortedMembersList = sortedMembersList;
        }

        public void sift(final ComponentRefsetMembers markedParentMember) {
            List<I_ThinExtByRefVersioned> affectedMembers = markedParentMember.getMembers();
            for (int x = 0; x < affectedMembers.size() - 1; x++) {
                sortedMembersList.add(affectedMembers.get(x));// add all but the
                                                              // last one
            }
        }
    }
}
