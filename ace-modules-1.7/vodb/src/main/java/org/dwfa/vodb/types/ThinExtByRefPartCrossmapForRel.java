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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartCrossmapForRel;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartCrossmap;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartCrossmapForRel extends ThinExtByRefPart implements I_ThinExtByRefPartCrossmapForRel {

    int refineFlagId;
    int additionalCodeId;
    int elementNo;
    int blockNo;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(4);
        partComponentNids.add(getPathId());
        partComponentNids.add(getStatusId());
        partComponentNids.add(refineFlagId);
        partComponentNids.add(additionalCodeId);
        return partComponentNids;
    }

    public ThinExtByRefPartCrossmapForRel(ThinExtByRefPartCrossmapForRel another) {
        super(another);
        this.refineFlagId = another.refineFlagId;
        this.additionalCodeId = another.additionalCodeId;
        this.elementNo = another.elementNo;
        this.blockNo = another.blockNo;
    }

    public ThinExtByRefPartCrossmapForRel() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartCrossmapForRel#getRefineFlagId()
     */
    public int getRefineFlagId() {
        return refineFlagId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartCrossmapForRel#setRefineFlagId(int)
     */
    public void setRefineFlagId(int refineFlagId) {
        this.refineFlagId = refineFlagId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartCrossmapForRel#getAdditionalCodeId
     * ()
     */
    public int getAdditionalCodeId() {
        return additionalCodeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartCrossmapForRel#setAdditionalCodeId
     * (int)
     */
    public void setAdditionalCodeId(int additionalCodeId) {
        this.additionalCodeId = additionalCodeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartCrossmapForRel#getElementNo()
     */
    public int getElementNo() {
        return elementNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartCrossmapForRel#setElementNo(int)
     */
    public void setElementNo(int elementNo) {
        this.elementNo = elementNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartCrossmapForRel#getBlockNo()
     */
    public int getBlockNo() {
        return blockNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartCrossmapForRel#setBlockNo(int)
     */
    public void setBlockNo(int blockNo) {
        this.blockNo = blockNo;
    }

    public int compareTo(I_ThinExtByRefPart o) {
        if (this.getClass().isAssignableFrom(o.getClass())) {
            ThinExtByRefPartCrossmapForRel otherPart = (ThinExtByRefPartCrossmapForRel) o;
            return this.getVersion() - otherPart.getVersion() + this.getPathId() - otherPart.getPathId();
        }
        return 1;
    }

    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartCrossmapForRel(this);
    }

    @Override
    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        UniversalAceExtByRefPartCrossmap universalPart = new UniversalAceExtByRefPartCrossmap();
        universalPart.setRefineFlagUid(tf.getUids(refineFlagId));
        universalPart.setAdditionalCodeUid(tf.getUids(additionalCodeId));
        universalPart.setElementNo(elementNo);
        universalPart.setBlockNo(blockNo);
        universalPart.setPathUid(tf.getUids(getPathId()));
        universalPart.setStatusUid(tf.getUids(getStatusId()));
        universalPart.setTime(ThinVersionHelper.convert(getVersion()));
        return universalPart;
    }

}
