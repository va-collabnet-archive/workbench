package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

import java.util.ArrayList;
import java.util.List;

/**
 * Sifts through the list of supplied "normal members" and "marked parents". Applies 1 of 2 strategies:
 *
 * 1. If the "marked parent" is a normal member then, return all "marked parents".
 * 2. If the "market parent" is not a normal member then return all but 1 "marked parent".
 */
public final class DuplicateMarketParentSifter {

    public List<I_ThinExtByRefVersioned> sift(final List<ComponentRefsetKey> normalMemberList,
                                              final List<ComponentRefsetMembers> componentRefsetMembersList) {

        List<I_ThinExtByRefVersioned> sortedMembersList = new ArrayList<I_ThinExtByRefVersioned>();
        SiftingStrategy duplicateMarkedParentStrategy = new DuplicateMarkedParentStrategy(sortedMembersList);
        SiftingStrategy normalMemberStrategy = new NormalMemberStrategy(sortedMembersList);

        for (ComponentRefsetMembers componentRefsetMembers : componentRefsetMembersList) {

            if (isNormalMember(normalMemberList, componentRefsetMembers)) {
                normalMemberStrategy.sift(componentRefsetMembers);
                continue;
            }
            
            duplicateMarkedParentStrategy.sift(componentRefsetMembers);
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


    private final class NormalMemberStrategy implements SiftingStrategy {

        private final List<I_ThinExtByRefVersioned> sortedMembersList;

        private NormalMemberStrategy(final List<I_ThinExtByRefVersioned> sortedMembersList) {
            this.sortedMembersList = sortedMembersList;
        }

        public void sift(final ComponentRefsetMembers componentRefsetMembers) {
            sortedMembersList.addAll(componentRefsetMembers.getMembers());
        }
    }

    private final class DuplicateMarkedParentStrategy implements SiftingStrategy {

        private final List<I_ThinExtByRefVersioned> sortedMembersList;

        private DuplicateMarkedParentStrategy(final List<I_ThinExtByRefVersioned> sortedMembersList) {
            this.sortedMembersList = sortedMembersList;
        }

        public void sift(final ComponentRefsetMembers componentRefsetMembers) {
            List<I_ThinExtByRefVersioned> affectedMembers = componentRefsetMembers.getMembers();
            for (int x = 0; x < affectedMembers.size() - 1; x++) {
                sortedMembersList.add(affectedMembers.get(x));//add all but the last one
            }
        }
    }
}
