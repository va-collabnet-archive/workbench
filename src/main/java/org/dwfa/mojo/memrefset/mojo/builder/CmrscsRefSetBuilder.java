package org.dwfa.mojo.memrefset.mojo.builder;

import org.dwfa.mojo.memrefset.mojo.RefSet;
import org.dwfa.mojo.memrefset.mojo.RefSetNameRetriever;

import java.util.UUID;

public final class CmrscsRefSetBuilder {

    private final RefSetNameRetriever refSetNameRetriever;
    private final CmrscsChangeSetBuilder parent;
    private RefSet refSet;

    public CmrscsRefSetBuilder(final RefSetNameRetriever refSetNameRetriever, final CmrscsChangeSetBuilder parent) {
        this.parent = parent;
        this.refSetNameRetriever = refSetNameRetriever;
        refSet = new RefSet();
    }

    public CmrscsRefSetBuilder withComponentUUID(final UUID uuid) {
        refSet.setComponentUUID(uuid);
        refSet.setComponentDescription(refSetNameRetriever.retrieveName(uuid));
        return this;
    }

    public CmrscsRefSetBuilder withStatusUUID(final UUID uuid) {
        refSet.setStatusUUID(uuid);
        refSet.setStatusDescription(refSetNameRetriever.retrieveName(uuid));
        return this;
    }

    public CmrscsRefSetBuilder withConceptUUID(final UUID uuid) {
        refSet.setConceptUUID(uuid);
        refSet.setConceptDescription(refSetNameRetriever.retrieveName(uuid));
        return this;
    }

    public CmrscsRefSetBuilder withMemberUUID(final UUID memberUUID) {
        refSet.setMemberUUID(memberUUID);
        return this;
    }

    public CmrscsChangeSetBuilder closeRefset() {
        parent.addRefSetBuilder(this);
        return parent;
    }

    public RefSet build() {
        return refSet;
    }
}
