package org.ihtsdo.tk.dto.concept.component.identifier;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class TkIdentifierString extends TkIdentifier {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public String denotation;

   //~--- constructors --------------------------------------------------------

   public TkIdentifierString() {
      super();
   }

   public TkIdentifierString(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
      denotation = in.readUTF();
   }

   public TkIdentifierString(TkIdentifierString another, Map<UUID, UUID> conversionMap, long offset,
                             boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.denotation = another.denotation;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EIdentifierVersionString</tt> object, and contains the same values, field by field,
    * as this <tt>EIdentifierVersionString</tt>.
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

      if (TkIdentifierString.class.isAssignableFrom(obj.getClass())) {
         TkIdentifierString another = (TkIdentifierString) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare denotation
         if (!this.denotation.equals(another.denotation)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>EIdentifierVersionString</code>.
    *
    * @return a hash code value for this <tt>EIdentifierVersionString</tt>.
    */
   @Override
   public int hashCode() {
      return Arrays.hashCode(new int[] { denotation.hashCode(), statusUuid.hashCode(), pathUuid.hashCode(),
                                         (int) time, (int) (time >>> 32) });
   }

   @Override
   public TkIdentifierString makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkIdentifierString(this, conversionMap, offset, mapAll);
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" denotation:");
      buff.append("'").append(this.denotation).append("'");
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeDenotation(DataOutput out) throws IOException {
      out.writeUTF(denotation);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getDenotation() {
      return denotation;
   }

   @Override
   public IDENTIFIER_PART_TYPES getIdType() {
      return IDENTIFIER_PART_TYPES.STRING;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setDenotation(Object denotation) {
      this.denotation = (String) denotation;
   }
}
