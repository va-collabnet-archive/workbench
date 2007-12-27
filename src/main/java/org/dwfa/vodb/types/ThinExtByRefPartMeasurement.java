package org.dwfa.vodb.types;

import java.io.IOException;

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

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#getUnitsOfMeasureId()
    */
   public int getUnitsOfMeasureId() {
      return unitsOfMeasureId;
   }

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#setUnitsOfMeasureId(int)
    */
   public void setUnitsOfMeasureId(int conceptId) {
      this.unitsOfMeasureId = conceptId;
   }
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtByRefPartMeasurement.class.isAssignableFrom(obj.getClass())) {
            ThinExtByRefPartMeasurement another = (ThinExtByRefPartMeasurement) obj;
            return unitsOfMeasureId == another.unitsOfMeasureId && 
               measurementValue == another.measurementValue;
         }
      }
      return false;
   }

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#getMeasurementValue()
    */
   public double getMeasurementValue() {
      return measurementValue;
   }

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#setMeasurementValue(double)
    */
   public void setMeasurementValue(double measurementValue) {
      this.measurementValue = measurementValue;
   }
   
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#getUniversalPart()
    */
   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
      I_TermFactory tf = LocalVersionedTerminology.get();
      UniversalAceExtByRefPartMeasurement universalPart = new UniversalAceExtByRefPartMeasurement();
      universalPart.setMeasurementValue(getMeasurementValue());
      universalPart.setUnitsOfMeasureUids(tf.getUids(getUnitsOfMeasureId()));
      universalPart.setPathUid(tf.getUids(getPathId()));
      universalPart.setStatusUid(tf.getUids(getStatus()));
      universalPart.setTime(ThinVersionHelper.convert(getVersion()));
      return universalPart;
   }

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPartMeasurement#duplicatePart()
    */
   @Override
   public I_ThinExtByRefPart duplicatePart() {
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
