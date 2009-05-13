package org.dwfa.ace.api.ebr;


public interface I_ThinExtByRefPartMeasurement extends I_ThinExtByRefPart {

   public int getUnitsOfMeasureId();

   public void setUnitsOfMeasureId(int conceptId);

   public double getMeasurementValue();

   public void setMeasurementValue(double measurementValue);

}