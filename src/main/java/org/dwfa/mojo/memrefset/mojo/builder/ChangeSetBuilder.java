package org.dwfa.mojo.memrefset.mojo.builder;

import org.dwfa.mojo.memrefset.mojo.ChangeSet;

import java.util.UUID;

public interface ChangeSetBuilder {
    
    CmrscsChangeSetBuilder withPathUUID(UUID uuid);

    CmrscsChangeSetBuilder withRefsetUUID(UUID uuid);

    CmrscsRefSetBuilder openRefset();

    CmrscsResultBuilder closeChangeSet();

    CmrscsChangeSetBuilder withTime(Long time);

    ChangeSet build();
}
