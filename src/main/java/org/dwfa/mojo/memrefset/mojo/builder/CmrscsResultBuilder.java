package org.dwfa.mojo.memrefset.mojo.builder;

import org.dwfa.mojo.memrefset.mojo.ChangeSet;
import org.dwfa.mojo.memrefset.mojo.CmrscsResult;
import org.dwfa.mojo.memrefset.mojo.CmrscsResultImpl;
import org.dwfa.mojo.memrefset.mojo.RefSetNameRetriever;
import org.dwfa.mojo.memrefset.mojo.RefSetNameRetrieverImpl;

import java.util.ArrayList;
import java.util.List;

public final class CmrscsResultBuilder {

    private final List<CmrscsChangeSetBuilder> changeSetBuilderList;
    private final RefSetNameRetriever refSetNameRetriever;

    public CmrscsResultBuilder() {
        changeSetBuilderList = new ArrayList<CmrscsChangeSetBuilder>();
        refSetNameRetriever = new RefSetNameRetrieverImpl();
    }

    public CmrscsChangeSetBuilder openChangeSet() {
        return new CmrscsChangeSetBuilder(this, refSetNameRetriever);
    }

    public CmrscsResult build() {
        List<ChangeSet> changeSetList = new ArrayList<ChangeSet>();

        for (CmrscsChangeSetBuilder builder : changeSetBuilderList) {
            changeSetList.add(builder.build());
        }

        return new CmrscsResultImpl(changeSetList);
    }

    void addChangeSetBuilder(final CmrscsChangeSetBuilder cmrscsChangeSetBuilder) {
        changeSetBuilderList.add(cmrscsChangeSetBuilder);
    }
}
