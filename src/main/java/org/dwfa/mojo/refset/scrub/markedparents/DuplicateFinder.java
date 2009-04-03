package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class DuplicateFinder {

    private final Map<ComponentRefsetKey, ComponentRefsetMembers> memberMap;
    private final int currentStatusId;

    public DuplicateFinder(final int currentStatusId) {
        this.currentStatusId = currentStatusId;
        memberMap = new HashMap<ComponentRefsetKey, ComponentRefsetMembers>();
    }

    public void put(final I_ThinExtByRefVersioned member) {
        ComponentRefsetKey key = new ComponentRefsetKey(member);

        if (!memberMap.containsKey(key)) {
            memberMap.put(key, new ComponentRefsetMembers(key));
        }

        memberMap.get(key).addMember(member);
    }

    public List<ComponentRefsetMembers> getDuplicates() {
        List<ComponentRefsetMembers> duplicateList = new ArrayList<ComponentRefsetMembers>();

        for (ComponentRefsetMembers componentRefsetMembers : memberMap.values()) {
            if (hasDuplicateMarkedParents(componentRefsetMembers)) {
                removeRetiredMembers(componentRefsetMembers);
                duplicateList.add(componentRefsetMembers);
            }
        }

        return duplicateList;
    }

    private boolean hasDuplicateMarkedParents(final ComponentRefsetMembers componentRefsetMembers) {
        return componentRefsetMembers.getMemberCount() > 1;
    }

    private void removeRetiredMembers(final ComponentRefsetMembers componentRefsetMembers) {
        for (I_ThinExtByRefVersioned member : componentRefsetMembers.getMembers()) {
            TreeSet<I_ThinExtByRefPart> sortedVersionsSet = new TreeSet<I_ThinExtByRefPart>(new LatestVersionComparator());
            sortedVersionsSet.addAll(member.getVersions());
            if (sortedVersionsSet.last().getStatus() != currentStatusId) { //ignore non-current statuses
                componentRefsetMembers.removeMember(member);
            }
        }
    }

    private final class LatestVersionComparator implements Comparator<I_ThinExtByRefPart> {

        public int compare(final I_ThinExtByRefPart o1, final I_ThinExtByRefPart o2) {
            return o1.getVersion() - o2.getVersion();
        }
    }    
}
