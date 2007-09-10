package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.refset.I_RefsetDefaultsMeasurement;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsMeasurement extends RefsetDefaults implements I_RefsetDefaultsMeasurement {

   private I_GetConceptData defaultUnitsOfMeasureForMeasurementRefset;
   private I_IntList unitsOfMeasurePopupIds = new IntList();

   private double defaultMeasurementValueForMeasurementRefset;
   private Double[] measurementValuePopupItems;

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(defaultUnitsOfMeasureForMeasurementRefset.getUids());
      IntList.writeIntList(out, unitsOfMeasurePopupIds);
      out.writeDouble(defaultMeasurementValueForMeasurementRefset);
      out.writeObject(measurementValuePopupItems);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         defaultUnitsOfMeasureForMeasurementRefset = readConcept(in);
         unitsOfMeasurePopupIds = IntList.readIntListIgnoreMapErrors(in);
         defaultMeasurementValueForMeasurementRefset = in.readDouble();
         measurementValuePopupItems = (Double[]) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   public RefsetDefaultsMeasurement() throws TerminologyException, IOException {
      super();
      defaultUnitsOfMeasureForMeasurementRefset = ConceptBean.get(RefsetAuxiliary.Concept.REFSET_AUXILIARY.getUids());
      unitsOfMeasurePopupIds.add(defaultUnitsOfMeasureForMeasurementRefset.getConceptId());
              
      defaultMeasurementValueForMeasurementRefset = 1.0;
      measurementValuePopupItems = new Double[] { 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0 };
   }

   
   public double getDefaultMeasurementValueForMeasurementRefset() {
      return defaultMeasurementValueForMeasurementRefset;
   }

   public void setDefaultMeasurementValueForMeasurementRefset(double defaultMeasurementValueForMeasurementRefset) {
      this.defaultMeasurementValueForMeasurementRefset = defaultMeasurementValueForMeasurementRefset;
   }

   public I_GetConceptData getDefaultUnitsOfMeasureForMeasurementRefset() {
      return defaultUnitsOfMeasureForMeasurementRefset;
   }

   public void setDefaultUnitsOfMeasureForMeasurementRefset(I_GetConceptData defaultUnitsOfMeasureForMeasurementRefset) {
      this.defaultUnitsOfMeasureForMeasurementRefset = defaultUnitsOfMeasureForMeasurementRefset;
   }

   public Double[] getMeasurementValuePopupItems() {
      return measurementValuePopupItems;
   }

   public void setMeasurementValuePopupItems(Double[] measurementValuePopupItems) {
      this.measurementValuePopupItems = measurementValuePopupItems;
   }

   public I_IntList getUnitsOfMeasurePopupIds() {
      return unitsOfMeasurePopupIds;
   }


}
