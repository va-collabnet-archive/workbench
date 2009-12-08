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
package org.dwfa.ace.api;

import java.io.IOException;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.OpenBitSet;

public class IdentifierSet implements I_RepresentIdSet {

    protected OpenBitSet bitSet;
    private int offset = Integer.MIN_VALUE;

    public IdentifierSet(OpenBitSet bitSet) {
        super();
        this.bitSet = bitSet;
    }

    public IdentifierSet() {
        bitSet = new OpenBitSet();
    }

    public IdentifierSet(int numBits) {
        bitSet = new OpenBitSet(numBits);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_RepresentIdSet#isMember(int)
     */
    public boolean isMember(int nid) {
        return bitSet.fastGet(nid + offset);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_RepresentIdSet#setMember(int)
     */
    public void setMember(int nid) {
        bitSet.fastSet(nid + offset);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_RepresentIdSet#setNotMember(int)
     */
    public void setNotMember(int nid) {
        bitSet.fastClear(nid + offset);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.ace.api.I_RepresentIdSet#and(org.dwfa.ace.api.IdentifierSet)
     */
    public void and(I_RepresentIdSet other) {
        bitSet.and(((IdentifierSet) other).bitSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.I_RepresentIdSet#or(org.dwfa.ace.api.IdentifierSet)
     */
    public void or(I_RepresentIdSet other) {
        bitSet.or(((IdentifierSet) other).bitSet);
    }

    public I_RepresentIdSet duplicate() {
        return new IdentifierSet((OpenBitSet) bitSet.clone());
    }

    public I_IterateIds iterator() {
        return new NidIterator(bitSet.iterator());
    }

    private class NidIterator implements I_IterateIds {
        private DocIdSetIterator docIterator;

        private NidIterator(DocIdSetIterator docIterator) {
            super();
            this.docIterator = docIterator;
        }

        public boolean next() throws IOException {
            return docIterator.next();
        }

        public int nid() {
            return docIterator.doc() + offset;
        }

        public boolean skipTo(int target) throws IOException {
            return docIterator.skipTo(target + offset);
        }
    }

    public int size() {
        return (int) bitSet.cardinality();
    }
}
