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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptConceptConcept;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartConceptConceptConcept extends ThinExtByRefPart implements
        I_ThinExtByRefPartConceptConceptConcept {

    private int c1id;
    private int c2id;
    private int c3id;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(5);
        partComponentNids.add(getPathId());
        partComponentNids.add(getStatusId());
        partComponentNids.add(c1id);
        partComponentNids.add(c2id);
        partComponentNids.add(c3id);
        return partComponentNids;
    }

    public ThinExtByRefPartConceptConceptConcept(ThinExtByRefPartConceptConceptConcept another) {
        super(another);
        this.c1id = another.c1id;
        this.c2id = another.c2id;
        this.c3id = another.c3id;
    }

    public ThinExtByRefPartConceptConceptConcept() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (ThinExtByRefPartConceptConceptConcept.class.isAssignableFrom(obj.getClass())) {
                ThinExtByRefPartConceptConceptConcept another = (ThinExtByRefPartConceptConceptConcept) obj;
                return c1id == another.c1id && c2id == another.c2id && c3id == another.c3id;
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
        UniversalAceExtByRefPartConceptConceptConcept universalPart = new UniversalAceExtByRefPartConceptConceptConcept();
        universalPart.setC1UuidCollection(tf.getUids(getC1id()));
        universalPart.setC2UuidCollection(tf.getUids(getC2id()));
        universalPart.setC3UuidCollection(tf.getUids(getC3id()));
        universalPart.setPathUid(tf.getUids(getPathId()));
        universalPart.setStatusUid(tf.getUids(getStatusId()));
        universalPart.setTime(ThinVersionHelper.convert(getVersion()));
        return universalPart;
    }

    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartConceptConceptConcept(this);
    }

    public int compareTo(I_ThinExtByRefPart o) {
        if (ThinExtByRefPartConceptConceptConcept.class.isAssignableFrom(o.getClass())) {
            ThinExtByRefPartConceptConceptConcept otherPart = (ThinExtByRefPartConceptConceptConcept) o;
            if (c1id != otherPart.c1id) {
                return c1id - otherPart.c1id;
            }
            if (c2id != otherPart.c2id) {
                return c2id - otherPart.c2id;
            }
            return c3id - otherPart.c3id;
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

    public int getC3id() {
        return c3id;
    }

    public void setC3id(int c3id) {
        this.c3id = c3id;
    }

    public int getConceptId() {
        return getC1id();
    }

    public void setConceptId(int conceptId) {
        setC1id(conceptId);
    }

}
