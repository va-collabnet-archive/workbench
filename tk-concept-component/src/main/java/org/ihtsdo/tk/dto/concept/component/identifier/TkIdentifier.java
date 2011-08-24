package org.ihtsdo.tk.dto.concept.component.identifier;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public abstract class TkIdentifier extends TkRevision {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID authorityUuid;

   //~--- constructors --------------------------------------------------------

   public TkIdentifier() {
      super();
   }

   public TkIdentifier(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkIdentifier(TkIdentifier another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.authorityUuid = conversionMap.get(another.authorityUuid);
      } else {
         this.authorUuid = another.authorUuid;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EIdentifierVersion</tt> object, and contains the same values, field by field,
    * as this <tt>EIdentifierVersion</tt>.
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

      if (TkIdentifier.class.isAssignableFrom(obj.getClass())) {
         TkIdentifier another = (TkIdentifier) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare authorityUuid
         if (!this.authorityUuid.equals(another.authorityUuid)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>EIdentifierVersion</code>.
    *
    * @return a hash code value for this <tt>EIdentifierVersion</tt>.
    */
   @Override
   public int hashCode() {
      return Arrays.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time,
                                         (int) (time >>> 32) });
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      authorityUuid = new UUID(in.readLong(), in.readLong());
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(" authority:");
      buff.append(informAboutUuid(this.authorityUuid));
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   public abstract void writeDenotation(DataOutput out) throws IOException;

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(authorityUuid.getMostSignificantBits());
      out.writeLong(authorityUuid.getLeastSignificantBits());
      writeDenotation(out);
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getAuthorityUuid() {
      return authorityUuid;
   }

   public abstract Object getDenotation();

   public abstract IDENTIFIER_PART_TYPES getIdType();

   //~--- set methods ---------------------------------------------------------

   public void setAuthorityUuid(UUID authorityUuid) {
      this.authorityUuid = authorityUuid;
   }

   public abstract void setDenotation(Object denotation);
}
