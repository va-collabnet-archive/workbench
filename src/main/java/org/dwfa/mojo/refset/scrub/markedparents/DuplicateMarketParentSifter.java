package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

import java.util.ArrayList;
import java.util.List;

/**
 * Sifts through the list of supplied "marked parents" and returns all but the last "marked parent" member for removal.
 */
public final class DuplicateMarketParentSifter {

    public List<I_ThinExtByRefVersioned> sift(final List<ComponentRefsetMembers> componentRefsetMembersList) {

        List<I_ThinExtByRefVersioned> sortedMembersList = new ArrayList<I_ThinExtByRefVersioned>();

        for (ComponentRefsetMembers componentRefsetMembers : componentRefsetMembersList) {
            List<I_ThinExtByRefVersioned> affectedMembers = componentRefsetMembers.getMembers();

            for (int x = 0; x < affectedMembers.size() - 1; x++) {
                sortedMembersList.add(affectedMembers.get(x));//add all but the last one
            }
        }

        return sortedMembersList;
    }
}
