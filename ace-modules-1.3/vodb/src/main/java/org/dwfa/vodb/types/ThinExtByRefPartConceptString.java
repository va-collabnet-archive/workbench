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
package org.dwfa.vodb.types;

import java.io.IOException;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptString;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartConceptString extends ThinExtByRefPartConcept implements I_ThinExtByRefPartConceptString {

    private String str;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(3);
        partComponentNids.add(getPathId());
        partComponentNids.add(getStatusId());
        partComponentNids.add(getC1id());
        return partComponentNids;
    }

    public ThinExtByRefPartConceptString(ThinExtByRefPartConceptString another) {
        super(another);
        this.str = another.str;
    }

    public ThinExtByRefPartConceptString() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (ThinExtByRefPartConceptString.class.isAssignableFrom(obj.getClass())) {
                ThinExtByRefPartConceptString another = (ThinExtByRefPartConceptString) obj;
                return str.equals(another.str);
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartConcept#getUniversalPart()
     */
    @Override
    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        UniversalAceExtByRefPartConceptString universalPart = new UniversalAceExtByRefPartConceptString();
        universalPart.setC1UuidCollection(tf.getUids(getC1id()));
        universalPart.setStr(getStr());
        universalPart.setPathUid(tf.getUids(getPathId()));
        universalPart.setStatusUid(tf.getUids(getStatusId()));
        universalPart.setTime(ThinVersionHelper.convert(getVersion()));
        return universalPart;
    }

    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartConceptString(this);
    }

    public int compareTo(I_ThinExtByRefPart o) {
        if (ThinExtByRefPartConceptString.class.isAssignableFrom(o.getClass())) {
            ThinExtByRefPartConceptString otherPart = (ThinExtByRefPartConceptString) o;
            return str.compareTo(otherPart.str);
        }
        return 1;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
}
