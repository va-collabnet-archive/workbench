package org.dwfa.mojo.refset.scrub.markedparents;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

import java.util.ArrayList;
import java.util.List;

public final class ComponentRefsetMembers {

    private final ComponentRefsetKey componentRefsetKey;
    private final List<I_ThinExtByRefVersioned> members;

    public ComponentRefsetMembers(final ComponentRefsetKey componentRefsetKey) {
        this.componentRefsetKey = componentRefsetKey;
        members = new ArrayList<I_ThinExtByRefVersioned>();
    }

    public ComponentRefsetKey getComponentRefsetKey() {
        return componentRefsetKey;
    }

    public void addMember(final I_ThinExtByRefVersioned member) {
        members.add(member);
    }

    public void removeMember(final I_ThinExtByRefVersioned member) {
        members.remove(member);
    }

    public List<I_ThinExtByRefVersioned> getMembers() {
        //return a copy.
        return new ArrayList<I_ThinExtByRefVersioned>(members);
    }

    public int getMemberCount() {
        return members.size();
    }

}

