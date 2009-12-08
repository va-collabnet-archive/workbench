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
