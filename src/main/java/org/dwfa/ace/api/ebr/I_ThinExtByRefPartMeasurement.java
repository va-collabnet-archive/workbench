package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPartMeasurement extends I_ThinExtByRefPart {

   public int getUnitsOfMeasureId();

   public void setUnitsOfMeasureId(int conceptId);

   public double getMeasurementValue();

   public void setMeasurementValue(double measurementValue);

   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

   public I_ThinExtByRefPart duplicatePart();

}