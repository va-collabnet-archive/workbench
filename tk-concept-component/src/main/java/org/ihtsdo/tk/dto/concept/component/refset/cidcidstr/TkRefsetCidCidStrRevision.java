package org.ihtsdo.tk.dto.concept.component.refset.cidcidstr;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

public class TkRefsetCidCidStrRevision extends TkRevision {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID   c1Uuid;
   public UUID   c2Uuid;
   public String stringValue;

   //~--- constructors --------------------------------------------------------

   public TkRefsetCidCidStrRevision() {
      super();
   }

   public TkRefsetCidCidStrRevision(DataInput in, int dataVersion)
           throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetCidCidStrRevision(TkRefsetCidCidStrRevision another, Map<UUID, UUID> conversionMap,
                                    long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.c1Uuid      = conversionMap.get(another.c1Uuid);
         this.c2Uuid      = conversionMap.get(another.c2Uuid);
         this.stringValue = another.stringValue;
      } else {
         this.c1Uuid      = another.c1Uuid;
         this.c2Uuid      = another.c2Uuid;
         this.stringValue = another.stringValue;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidCidStrVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidCidStrVersion</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same;
    *         <code>false</code> otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (TkRefsetCidCidStrRevision.class.isAssignableFrom(obj.getClass())) {
         TkRefsetCidCidStrRevision another = (TkRefsetCidCidStrRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.c1Uuid.equals(another.c1Uuid)) {
            return false;
         }

         // Compare c2Uuid
         if (!this.c2Uuid.equals(another.c2Uuid)) {
            return false;
         }

         // Compare stringValue
         if (!this.stringValue.equals(another.stringValue)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public TkRefsetCidCidStrRevision makeConversion(Map<UUID, UUID> conversionMap, long offset,
           boolean mapAll) {
      return new TkRefsetCidCidStrRevision(this, conversionMap, offset, mapAll);
   }

   @Override
   public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      c1Uuid      = new UUID(in.readLong(), in.readLong());
      c2Uuid      = new UUID(in.readLong(), in.readLong());
      stringValue = UtfHelper.readUtfV7(in, dataVersion);
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" c1:");
      buff.append(informAboutUuid(this.c1Uuid));
      buff.append(" c2:");
      buff.append(informAboutUuid(this.c2Uuid));
      buff.append(" str:");
      buff.append("'").append(this.stringValue).append("' ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(c1Uuid.getMostSignificantBits());
      out.writeLong(c1Uuid.getLeastSignificantBits());
      out.writeLong(c2Uuid.getMostSignificantBits());
      out.writeLong(c2Uuid.getLeastSignificantBits());
      UtfHelper.writeUtf(out, stringValue);
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getC1Uuid() {
      return c1Uuid;
   }

   public UUID getC2Uuid() {
      return c2Uuid;
   }

   public String getStringValue() {
      return stringValue;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Uuid(UUID c1Uuid) {
      this.c1Uuid = c1Uuid;
   }

   public void setC2Uuid(UUID c2Uuid) {
      this.c2Uuid = c2Uuid;
   }

   public void setStringValue(String stringValue) {
      this.stringValue = stringValue;
   }
}
