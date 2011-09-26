package org.ihtsdo.tk.dto.concept.component.refset.Long;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

public class TkRefsetLongRevision extends TkRevision {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public long longValue;

   //~--- constructors --------------------------------------------------------

   public TkRefsetLongRevision() {
      super();
   }

   public TkRefsetLongRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetLongRevision(TkRefsetLongRevision another, Map<UUID, UUID> conversionMap, long offset,
                               boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.longValue = another.longValue;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetLongVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetLongVersion</tt>.
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

      if (TkRefsetLongRevision.class.isAssignableFrom(obj.getClass())) {
         TkRefsetLongRevision another = (TkRefsetLongRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare longValue
         if (this.longValue != another.longValue) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public TkRefsetLongRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetLongRevision(this, conversionMap, offset, mapAll);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      longValue = in.readLong();
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" long: ");
      buff.append(this.longValue);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(longValue);
   }

   //~--- get methods ---------------------------------------------------------

   public long getLongValue() {
      return longValue;
   }

   //~--- set methods ---------------------------------------------------------

   public void setLongValue(long longValue) {
      this.longValue = longValue;
   }
}
