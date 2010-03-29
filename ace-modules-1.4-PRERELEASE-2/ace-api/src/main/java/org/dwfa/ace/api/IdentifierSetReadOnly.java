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

import org.apache.lucene.util.OpenBitSet;

public class IdentifierSetReadOnly extends IdentifierSet {

    private IdentifierSetReadOnly() {
        super();
    }

    private IdentifierSetReadOnly(int numBits) {
        super(numBits);
    }

    public IdentifierSetReadOnly(IdentifierSet set) {
        this.bitSet = (OpenBitSet) set.bitSet.clone();
    }

    @Override
    public void and(I_RepresentIdSet other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void or(I_RepresentIdSet other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMember(int nid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNotMember(int nid) {
        throw new UnsupportedOperationException();
    }

}
