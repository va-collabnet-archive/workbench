package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

import java.util.ArrayList;
import java.util.List;

public final class DuplicateMarkerParentSorter {

    public List<I_ThinExtByRefVersioned> sort(final List<ComponentRefsetMembers> componentRefsetMembersList) {

        List<I_ThinExtByRefVersioned> sortedMembersList = new ArrayList<I_ThinExtByRefVersioned>();

        for (ComponentRefsetMembers componentRefsetMembers : componentRefsetMembersList) {
            List<I_ThinExtByRefVersioned> affectedMembers = componentRefsetMembers.getMembers();

            for (int x = 0; x < affectedMembers.size() - 1; x++) {
                sortedMembersList.add(affectedMembers.get(x));//add all but one
            }
        }

        return sortedMembersList;
    }
}
