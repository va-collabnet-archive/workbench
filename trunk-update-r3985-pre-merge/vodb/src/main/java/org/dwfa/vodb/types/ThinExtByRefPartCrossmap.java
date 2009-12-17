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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartCrossmap;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartCrossmap;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartCrossmap extends ThinExtByRefPartCrossmapForRel implements I_ThinExtByRefPartCrossmap {

    int mapStatusId;
    int targetCodeId;

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartCrossmap#getReadCodeId()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartCrossmap#getMapStatusId()
     */
    public int getMapStatusId() {
        return mapStatusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartCrossmap#setMapStatusId(int)
     */
    public void setMapStatusId(int mapStatusId) {
        this.mapStatusId = mapStatusId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartCrossmap#getTargetCodeId()
     */
    public int getTargetCodeId() {
        return targetCodeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartCrossmap#setTargetCodeId(int)
     */
    public void setTargetCodeId(int targetCodeId) {
        this.targetCodeId = targetCodeId;
    }

    public ThinExtByRefPartCrossmap(ThinExtByRefPartCrossmap another) {
        super(another);
        this.mapStatusId = another.mapStatusId;
        this.targetCodeId = another.targetCodeId;
    }

    public ThinExtByRefPartCrossmap() {
        super();
    }

    public I_ThinExtByRefPart duplicate() {
        return new ThinExtByRefPartCrossmap(this);
    }

    @Override
    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        UniversalAceExtByRefPartCrossmap universalPart = new UniversalAceExtByRefPartCrossmap();
        universalPart.setMapStatusUid(tf.getUids(mapStatusId));
        universalPart.setTargetCodeUid(tf.getUids(targetCodeId));
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
