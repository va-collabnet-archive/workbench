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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptConcept;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartConceptConcept extends ThinExtByRefPart implements I_ThinExtByRefPartConceptConcept {
    private int c1id;
    private int c2id;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(4);
        partComponentNids.add(getPathId());
        partComponentNids.add(getStatusId());
        partComponentNids.add(c1id);
        partComponentNids.add(c2id);
        return partComponentNids;
    }

    public ThinExtByRefPartConceptConcept(ThinExtByRefPartConceptConcept another) {
        super(another);
        this.c1id = another.c1id;
        this.c2id = another.c2id;
    }

    public ThinExtByRefPartConceptConcept() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (ThinExtByRefPartConceptConcept.class.isAssignableFrom(obj.getClass())) {
                ThinExtByRefPartConceptConcept another = (ThinExtByRefPartConceptConcept) obj;
                return c1id == another.c1id && c2id == another.c2id;
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
        UniversalAceExtByRefPartConceptConcept universalPart = new UniversalAceExtByRefPartConceptConcept();
        universalPart.setC1UuidCollection(tf.getUids(getC1id()));
        universalPart.setC2UuidCollection(tf.getUids(getC2id()));
        universalPart.setPathUid(tf.getUids(getPathId()));
        universalPart.setStatusUid(tf.getUids(getStatusId()));
        universalPart.setTime(ThinVersionHelper.convert(getVersion()));
        return universalPart;
    }

    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartConceptConcept(this);
    }

    public int compareTo(I_ThinExtByRefPart o) {
        if (ThinExtByRefPartConceptConcept.class.isAssignableFrom(o.getClass())) {
            ThinExtByRefPartConceptConcept otherPart = (ThinExtByRefPartConceptConcept) o;
            if (c1id != otherPart.c1id) {
                return c1id - otherPart.c1id;
            }
            return c2id - otherPart.c2id;
        }
        return 1;
    }

    public int getC1id() {
        return c1id;
    }

    public void setC1id(int c1id) {
        this.c1id = c1id;
    }

    public int getC2id() {
        return c2id;
    }

    public void setC2id(int c2id) {
        this.c2id = c2id;
    }

    public int getConceptId() {
        return getC1id();
    }

    public void setConceptId(int conceptId) {
        setC1id(conceptId);
    }

}
