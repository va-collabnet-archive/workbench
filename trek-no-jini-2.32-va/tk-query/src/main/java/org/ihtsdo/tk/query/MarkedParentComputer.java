/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.query;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 * The class MarkedParentComputer provides methods for computing the marked
 * parents of a refset.
 */
public class MarkedParentComputer implements ProcessUnfetchedConceptDataBI {

    NidBitSetBI newMembers;
    ConcurrentSkipListSet<Integer> resultNids = new ConcurrentSkipListSet<>();
    ViewCoordinate vc;

    /**
     * Creates a new marked parent computer based on the given
     * <code>refsetMembers</code> and
     * <code>viewCoordinate</code>.
     *
     * @param refsetMembers a nid set representing the refset members
     * @param viewCoordinate specifies active/inactive versions
     */
    public MarkedParentComputer(NidBitSetBI refsetMembers, ViewCoordinate viewCoordinate) {
        this.newMembers = refsetMembers;
        this.vc = viewCoordinate;
    }

    @Override
    public void processUnfetchedConceptData(int conceptNid, ConceptFetcherBI conceptFetcher) throws Exception {
        if (newMembers.isMember(conceptNid)) {
            Set<Integer> parents = Ts.get().getAncestors(conceptNid, vc);
            resultNids.addAll(parents);
        }
    }

    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return newMembers;
    }

    @Override
    public boolean continueWork() {
        return true;
    }

    /**
     * Returns a set of nids representing the marked parents.
     * @return a <code>NidBitSetBI</code> representing the marked parent nids
     * @throws IOException indicates and I/O exception has occurred
     */
    public NidBitSetBI getMarkedParentNids() throws IOException {
        NidBitSetBI result = Ts.get().getEmptyNidSet();
        for (int nid : resultNids) {
            result.setMember(nid);
        }
        return result;
    }
}
