package org.dwfa.mojo.memrefset.mojo.builder;

import org.dwfa.mojo.memrefset.mojo.ChangeSet;
import org.dwfa.mojo.memrefset.mojo.RefSetNameRetriever;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CmrscsChangeSetBuilder implements ChangeSetBuilder {

    private final List<CmrscsRefSetBuilder> refSetBuilderList;
    private final CmrscsResultBuilder parent;
    private final RefSetNameRetriever refSetNameRetriever;
    private ChangeSet changeSet;

    public CmrscsChangeSetBuilder(final CmrscsResultBuilder parent, final RefSetNameRetriever refSetNameRetriever) {
        refSetBuilderList = new ArrayList<CmrscsRefSetBuilder>();
        changeSet = new ChangeSet();
        this.parent = parent;
        this.refSetNameRetriever = refSetNameRetriever;
    }

    public CmrscsChangeSetBuilder withPathUUID(final UUID uuid) {
        changeSet.setPathUUID(uuid);
        return this;
    }

    public CmrscsChangeSetBuilder withRefsetUUID(final UUID uuid) {
        changeSet.setRefsetUUID(uuid);
        changeSet.setRefsetName(refSetNameRetriever.retrieveName(uuid));
        return this;
    }

    public CmrscsRefSetBuilder openRefset() {
        return new CmrscsRefSetBuilder(this);
    }

    public CmrscsResultBuilder closeChangeSet() {
        for (CmrscsRefSetBuilder rs : refSetBuilderList) {
            changeSet.add(rs.build());
        }

        parent.addChangeSetBuilder(this);
        return parent;
    }

    public CmrscsChangeSetBuilder withTime(final Long time) {
        changeSet.setTime(time);
        return this;
    }

    public void addRefSetBuilder(final CmrscsRefSetBuilder refSetBuilder) {
        refSetBuilderList.add(refSetBuilder);
    }

    public ChangeSet build() {
        return changeSet;
    }
}
