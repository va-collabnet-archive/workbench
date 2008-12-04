package org.dwfa.mojo.memrefset.mojo;

import java.util.List;

public final class CmrscsResultImpl implements CmrscsResult {

    private final List<ChangeSet> changeSets;

    public CmrscsResultImpl(final List<ChangeSet> changeSets) {
        this.changeSets = changeSets;
    }

    public List<ChangeSet> getChangeSets() {
        return changeSets;
    }

    @Override
    public String toString() {
        return "CmrscsResultImpl{" +
                "changeSets=" + changeSets +
                '}';
    }
}
