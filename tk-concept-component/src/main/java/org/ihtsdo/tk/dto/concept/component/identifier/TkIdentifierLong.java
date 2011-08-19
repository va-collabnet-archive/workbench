package org.ihtsdo.tk.dto.concept.component.identifier;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;

public class TkIdentifierLong extends TkIdentifier {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public long denotation;

   //~--- constructors --------------------------------------------------------

   public TkIdentifierLong() {
      super();
   }

   public TkIdentifierLong(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
      denotation = in.readLong();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EIdentifierVersionLong</tt> object, and contains the same values, field by field,
    * as this <tt>EIdentifierVersionLong</tt>.
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

      if (TkIdentifierLong.class.isAssignableFrom(obj.getClass())) {
         TkIdentifierLong another = (TkIdentifierLong) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare denotation
         if (this.denotation != another.denotation) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>EIdentifierVersionLong</code>.
    *
    * @return a hash code value for this <tt>EIdentifierVersionLong</tt>.
    */
    @Override
   public int hashCode() {
      return Arrays.hashCode(new int[] {
         (int) denotation, (int) (denotation >>> 32), statusUuid.hashCode(), pathUuid.hashCode(), (int) time,
         (int) (time >>> 32)
      });
   }

   /**
    * Returns a string representation of the object.
    */
    @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" denotation:");
      buff.append(this.denotation);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeDenotation(DataOutput out) throws IOException {
      out.writeLong(denotation);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Long getDenotation() {
      return denotation;
   }

   @Override
   public IDENTIFIER_PART_TYPES getIdType() {
      return IDENTIFIER_PART_TYPES.LONG;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDenotation(Object denotation) {
      this.denotation = (Long) denotation;
   }
}
