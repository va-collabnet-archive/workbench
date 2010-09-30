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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConceptConceptString;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartConceptConceptString extends ThinExtByRefPartConceptConcept implements
        I_ThinExtByRefPartConceptConceptString {

    private String str;

    public ThinExtByRefPartConceptConceptString(ThinExtByRefPartConceptConceptString another) {
        super(another);
        this.str = another.str;
    }

    public ThinExtByRefPartConceptConceptString() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (ThinExtByRefPartConceptConceptString.class.isAssignableFrom(obj.getClass())) {
                ThinExtByRefPartConceptConceptString another = (ThinExtByRefPartConceptConceptString) obj;
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
        UniversalAceExtByRefPartConceptConceptString universalPart = new UniversalAceExtByRefPartConceptConceptString();
        universalPart.setC1UuidCollection(tf.getUids(getC1id()));
        universalPart.setC2UuidCollection(tf.getUids(getC2id()));
        universalPart.setStr(getStr());
        universalPart.setPathUid(tf.getUids(getPathId()));
        universalPart.setStatusUid(tf.getUids(getStatusId()));
        universalPart.setTime(ThinVersionHelper.convert(getVersion()));
        return universalPart;
    }

    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartConceptConceptString(this);
    }

    public int compareTo(I_ThinExtByRefPart o) {
        if (ThinExtByRefPartConceptConceptString.class.isAssignableFrom(o.getClass())) {
            ThinExtByRefPartConceptConceptString otherPart = (ThinExtByRefPartConceptConceptString) o;
            if (getC1id() != otherPart.getC1id()) {
                return getC1id() - otherPart.getC1id();
            }
            if (getC2id() != otherPart.getC2id()) {
                return getC2id() - otherPart.getC2id();
            }
            return str.compareTo(otherPart.str);
        }
        return 1;
    }

    @Deprecated
    public String getStr() {
        return getStringValue();
    }

    @Deprecated
    public void setStr(String str) {
        setStringValue(str);
    }

    public String getStringValue() {
        return str;
    }

    public void setStringValue(String str) {
        this.str = str;
    }
}
