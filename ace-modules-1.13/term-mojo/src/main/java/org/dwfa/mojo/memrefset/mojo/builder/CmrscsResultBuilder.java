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
