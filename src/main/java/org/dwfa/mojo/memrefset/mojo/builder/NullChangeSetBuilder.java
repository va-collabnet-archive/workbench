package org.dwfa.mojo.memrefset.mojo.builder;

import org.dwfa.mojo.memrefset.mojo.ChangeSet;

import java.util.UUID;

/**
 * This class follows the null object pattern. We can use this class whereever a ChangeSetBuilder is required
 * without the use of null values.
 */
public final class NullChangeSetBuilder implements ChangeSetBuilder {

    public CmrscsChangeSetBuilder withPathUUID(final UUID uuid) {
        return null;
    }

    public CmrscsChangeSetBuilder withRefsetUUID(final UUID uuid) {
        return null;
    }

    public CmrscsRefSetBuilder openRefset() {
        return null;
    }

    public CmrscsResultBuilder closeChangeSet() {
        return null;
    }

    public CmrscsChangeSetBuilder withTime(final Long time) {
        return null;
    }

    public ChangeSet build() {
        return null;
    }
}
