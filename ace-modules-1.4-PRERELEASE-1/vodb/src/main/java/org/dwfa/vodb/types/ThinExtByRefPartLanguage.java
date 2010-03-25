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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartLanguage;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartLanguage extends ThinExtByRefPart implements I_ThinExtByRefPartLanguage {
    private int acceptabilityId;
    private int correctnessId;
    private int degreeOfSynonymyId;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(5);
        partComponentNids.add(getPathId());
        partComponentNids.add(getStatusId());
        partComponentNids.add(acceptabilityId);
        partComponentNids.add(correctnessId);
        partComponentNids.add(degreeOfSynonymyId);
        return partComponentNids;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#getAcceptabilityId()
     */
    public int getAcceptabilityId() {
        return acceptabilityId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#setAcceptabilityId(int)
     */
    public void setAcceptabilityId(int acceptabilityId) {
        this.acceptabilityId = acceptabilityId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#getCorrectnessId()
     */
    public int getCorrectnessId() {
        return correctnessId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#setCorrectnessId(int)
     */
    public void setCorrectnessId(int correctnessId) {
        this.correctnessId = correctnessId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#getDegreeOfSynonymyId()
     */
    public int getDegreeOfSynonymyId() {
        return degreeOfSynonymyId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#setDegreeOfSynonymyId(int)
     */
    public void setDegreeOfSynonymyId(int degreeOfSynonymyId) {
        this.degreeOfSynonymyId = degreeOfSynonymyId;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (ThinExtByRefPartLanguage.class.isAssignableFrom(obj.getClass())) {
                ThinExtByRefPartLanguage another = (ThinExtByRefPartLanguage) obj;
                return acceptabilityId == another.acceptabilityId && correctnessId == another.correctnessId
                    && degreeOfSynonymyId == another.degreeOfSynonymyId;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartLanguage#getUniversalPart()
     */
    @Override
    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        UniversalAceExtByRefPartLanguage universalPart = new UniversalAceExtByRefPartLanguage();
        universalPart.setAcceptabilityUids(tf.getUids(getAcceptabilityId()));
        universalPart.setCorrectnessUids(tf.getUids(getCorrectnessId()));
        universalPart.setDegreeOfSynonymyUids(tf.getUids(getDegreeOfSynonymyId()));
        universalPart.setPathUid(tf.getUids(getPathId()));
        universalPart.setStatusUid(tf.getUids(getStatusId()));
        universalPart.setTime(ThinVersionHelper.convert(getVersion()));
        return universalPart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.ace.api.ebr.I_ThinExtByRefPart#duplicate()
     */
    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartLanguage(this);
    }

    public ThinExtByRefPartLanguage(ThinExtByRefPartLanguage another) {
        super(another);
        this.acceptabilityId = another.acceptabilityId;
        this.correctnessId = another.correctnessId;
        this.degreeOfSynonymyId = another.degreeOfSynonymyId;
    }

    public ThinExtByRefPartLanguage() {
        super();
    }

    public int compareTo(I_ThinExtByRefPart o) {
        if (ThinExtByRefPartLanguage.class.isAssignableFrom(o.getClass())) {
            ThinExtByRefPartLanguage otherPart = (ThinExtByRefPartLanguage) o;
            return this.acceptabilityId - otherPart.acceptabilityId;
        }
        return 1;
    }

}
