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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartMeasurement;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartMeasurement extends ThinExtByRefPart implements I_ThinExtByRefPartMeasurement {
    private int unitsOfMeasureId;
    private double measurementValue;

    public ArrayIntList getPartComponentNids() {
        ArrayIntList partComponentNids = new ArrayIntList(3);
        partComponentNids.add(getPathId());
        partComponentNids.add(getStatusId());
        partComponentNids.add(unitsOfMeasureId);
        return partComponentNids;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#getUnitsOfMeasureId()
     */
    public int getUnitsOfMeasureId() {
        return unitsOfMeasureId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#setUnitsOfMeasureId
     * (int)
     */
    public void setUnitsOfMeasureId(int conceptId) {
        this.unitsOfMeasureId = conceptId;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (ThinExtByRefPartMeasurement.class.isAssignableFrom(obj.getClass())) {
                ThinExtByRefPartMeasurement another = (ThinExtByRefPartMeasurement) obj;
                return unitsOfMeasureId == another.unitsOfMeasureId && measurementValue == another.measurementValue;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#getMeasurementValue()
     */
    public double getMeasurementValue() {
        return measurementValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#setMeasurementValue
     * (double)
     */
    public void setMeasurementValue(double measurementValue) {
        this.measurementValue = measurementValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#getUniversalPart()
     */
    @Override
    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        UniversalAceExtByRefPartMeasurement universalPart = new UniversalAceExtByRefPartMeasurement();
        universalPart.setMeasurementValue(getMeasurementValue());
        universalPart.setUnitsOfMeasureUids(tf.getUids(getUnitsOfMeasureId()));
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
        return new ThinExtByRefPartMeasurement(this);
    }

    public ThinExtByRefPartMeasurement(ThinExtByRefPartMeasurement another) {
        super(another);
        this.unitsOfMeasureId = another.unitsOfMeasureId;
        this.measurementValue = another.measurementValue;
    }

    public ThinExtByRefPartMeasurement() {
        super();
    }

    public int compareTo(I_ThinExtByRefPart o) {
        if (ThinExtByRefPartMeasurement.class.isAssignableFrom(o.getClass())) {
            ThinExtByRefPartMeasurement otherPart = (ThinExtByRefPartMeasurement) o;
            return Double.compare(this.measurementValue, otherPart.measurementValue);
        }
        return 1;
    }

}
