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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartConcept;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartConcept extends ThinExtByRefPart implements I_ThinExtByRefPartConcept {
    private int conceptId;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(3);
        partComponentNids.add(getPathId());
        partComponentNids.add(getStatusId());
        partComponentNids.add(conceptId);
        return partComponentNids;
    }

    public ThinExtByRefPartConcept(ThinExtByRefPartConcept another) {
        super(another);
        this.conceptId = another.conceptId;
    }

    public ThinExtByRefPartConcept() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartConcept#getConceptId()
     */
    public int getConceptId() {
        return getC1id();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartConcept#setConceptId(int)
     */
    public void setConceptId(int conceptId) {
        setC1id(conceptId);
    }

    public int getC1id() {
        return conceptId;
    }

    public void setC1id(int c1id) {
        this.conceptId = c1id;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (ThinExtByRefPartConcept.class.isAssignableFrom(obj.getClass())) {
                ThinExtByRefPartConcept another = (ThinExtByRefPartConcept) obj;
                return conceptId == another.conceptId;
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
        UniversalAceExtByRefPartConcept universalPart = new UniversalAceExtByRefPartConcept();
        universalPart.setConceptUid(tf.getUids(getConceptId()));
        universalPart.setPathUid(tf.getUids(getPathId()));
        universalPart.setStatusUid(tf.getUids(getStatusId()));
        universalPart.setTime(ThinVersionHelper.convert(getVersion()));
        return universalPart;
    }

    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartConcept(this);
    }

    public int compareTo(I_ThinExtByRefPart o) {
        if (ThinExtByRefPartConcept.class.isAssignableFrom(o.getClass())) {
            ThinExtByRefPartConcept otherPart = (ThinExtByRefPartConcept) o;
            return this.conceptId - otherPart.conceptId;
        }
        return 1;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(super.toString());
        try {
            buff.append(LocalVersionedTerminology.get().getConcept(conceptId).toString());
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
            buff.append(" cid: " + conceptId);
        }
        return buff.toString();
    }

}
