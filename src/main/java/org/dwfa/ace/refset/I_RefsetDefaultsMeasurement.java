package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;

public interface I_RefsetDefaultsMeasurement extends I_RefsetDefaults {   
   public double getDefaultMeasurementValueForMeasurementRefset();
   public Double[] getMeasurementValuePopupItems();
   
   public I_GetConceptData getDefaultUnitsOfMeasureForMeasurementRefset();
   public I_IntList getUnitsOfMeasurePopupIds();

}
