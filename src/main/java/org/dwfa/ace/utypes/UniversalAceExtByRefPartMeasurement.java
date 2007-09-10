package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceExtByRefPartMeasurement extends UniversalAceExtByRefPart {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private Collection<UUID> unitsOfMeasureUids;
   private double measurementValue;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(unitsOfMeasureUids);
      out.writeDouble(measurementValue);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         unitsOfMeasureUids = (Collection<UUID>) in.readObject();
         measurementValue = in.readDouble();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   public double getMeasurementValue() {
      return measurementValue;
   }

   public void setMeasurementValue(double measurementValue) {
      this.measurementValue = measurementValue;
   }

   public Collection<UUID> getUnitsOfMeasureUids() {
      return unitsOfMeasureUids;
   }

   public void setUnitsOfMeasureUids(Collection<UUID> unitsOfMeasureUids) {
      this.unitsOfMeasureUids = unitsOfMeasureUids;
   }

}
