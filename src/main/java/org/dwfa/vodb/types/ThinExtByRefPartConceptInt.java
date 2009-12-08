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

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptInt;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartConceptInt extends ThinExtByRefPartConcept implements I_ThinExtByRefPartConceptInt {

    private int intValue;

    public ThinExtByRefPartConceptInt(ThinExtByRefPartConceptInt another) {
        super(another);
        this.intValue = another.intValue;
    }

    public ThinExtByRefPartConceptInt() {
        super();
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (ThinExtByRefPartConceptInt.class.isAssignableFrom(obj.getClass())) {
                ThinExtByRefPartConceptInt another = (ThinExtByRefPartConceptInt) obj;
                return intValue == another.intValue;
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
        UniversalAceExtByRefPartConceptInt universalPart = new UniversalAceExtByRefPartConceptInt();
        universalPart.setConceptUid(tf.getUids(getConceptId()));
        universalPart.setIntValue(getIntValue());
        universalPart.setPathUid(tf.getUids(getPathId()));
        universalPart.setStatusUid(tf.getUids(getStatusId()));
        universalPart.setTime(ThinVersionHelper.convert(getVersion()));
        return universalPart;
    }

    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartConceptInt(this);
    }

    public int compareTo(ThinExtByRefPart o) {
        if (ThinExtByRefPartConceptInt.class.isAssignableFrom(o.getClass())) {
            ThinExtByRefPartConceptInt otherPart = (ThinExtByRefPartConceptInt) o;
            return this.intValue - otherPart.intValue;
        }
        return 1;
    }

}
