package org.ihtsdo.tk.dto.concept.component.refex.type_boolean;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

public class TkRefexBooleanRevision extends TkRevision {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public boolean boolean1;

   //~--- constructors --------------------------------------------------------

   public TkRefexBooleanRevision() {
      super();
   }

   public TkRefexBooleanRevision(RefexBooleanVersionBI refexBooleanVersion) throws IOException {
      super(refexBooleanVersion);
      this.boolean1 = refexBooleanVersion.getBoolean1();
   }

   public TkRefexBooleanRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefexBooleanRevision(TkRefexBooleanRevision another, Map<UUID, UUID> conversionMap,
                                  long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.boolean1 = another.boolean1;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetBooleanVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetBooleanVersion</tt>.
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

      if (TkRefexBooleanRevision.class.isAssignableFrom(obj.getClass())) {
         TkRefexBooleanRevision another = (TkRefexBooleanRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare boolean1
         if (this.boolean1 != another.boolean1) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public TkRefexBooleanRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefexBooleanRevision(this, conversionMap, offset, mapAll);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      boolean1 = in.readBoolean();
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(this.boolean1);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeBoolean(boolean1);
   }

   //~--- get methods ---------------------------------------------------------

   public boolean getBoolean1() {
      return boolean1;
   }

   public boolean isBooleanValue() {
      return boolean1;
   }

   //~--- set methods ---------------------------------------------------------

   public void setBoolean1(boolean boolean1) {
      this.boolean1 = boolean1;
   }
}
