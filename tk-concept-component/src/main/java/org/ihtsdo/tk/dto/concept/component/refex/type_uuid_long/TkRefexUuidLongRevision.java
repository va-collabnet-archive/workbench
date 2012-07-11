package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

public class TkRefexUuidLongRevision extends TkRevision {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID uuid1;
   public long long1;

   //~--- constructors --------------------------------------------------------

   public TkRefexUuidLongRevision() {
      super();
   }

   public TkRefexUuidLongRevision(RefexNidLongVersionBI refexNidLongVersion) throws IOException {
      super(refexNidLongVersion);

      TerminologyStoreDI ts = Ts.get();

      this.uuid1    = ts.getUuidPrimordialForNid(refexNidLongVersion.getNid1());
      this.long1 = refexNidLongVersion.getLong1();
   }

   public TkRefexUuidLongRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefexUuidLongRevision(TkRefexUuidLongRevision another, Map<UUID, UUID> conversionMap,
                                  long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.uuid1    = conversionMap.get(another.uuid1);
         this.long1 = another.long1;
      } else {
         this.uuid1    = another.uuid1;
         this.long1 = another.long1;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidLongVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidLongVersion</tt>.
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

      if (TkRefexUuidLongRevision.class.isAssignableFrom(obj.getClass())) {
         TkRefexUuidLongRevision another = (TkRefexUuidLongRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare uuid1
         if (!this.uuid1.equals(another.uuid1)) {
            return false;
         }

         // Compare long1
         if (this.long1 != another.long1) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public TkRefexUuidLongRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefexUuidLongRevision(this, conversionMap, offset, mapAll);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      uuid1    = new UUID(in.readLong(), in.readLong());
      long1 = in.readLong();
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" c1: ");
      buff.append(informAboutUuid(this.uuid1));
      buff.append(" long: ");
      buff.append(this.long1);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(uuid1.getMostSignificantBits());
      out.writeLong(uuid1.getLeastSignificantBits());
      out.writeLong(long1);
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getUuid1() {
      return uuid1;
   }

   public long getLong1() {
      return long1;
   }

   //~--- set methods ---------------------------------------------------------

   public void setUuid1(UUID uuid1) {
      this.uuid1 = uuid1;
   }

   public void setLong1(long long1) {
      this.long1 = long1;
   }
}
