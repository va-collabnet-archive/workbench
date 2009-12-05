/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        return new CmrscsRefSetBuilder(refSetNameRetriever, this);
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
