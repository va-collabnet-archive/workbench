package org.ihtsdo.tk.dto.concept.component.refset.cidstr;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TkRefsetCidStrMember extends TkRefsetAbstractMember<TkRefsetCidStrRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID   c1Uuid;
   public String strValue;

   //~--- constructors --------------------------------------------------------

   public TkRefsetCidStrMember() {
      super();
   }

   public TkRefsetCidStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetCidStrMember(TkRefsetCidStrMember another, Map<UUID, UUID> conversionMap, long offset,
                               boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.c1Uuid   = conversionMap.get(another.c1Uuid);
         this.strValue = another.strValue;
      } else {
         this.c1Uuid   = another.c1Uuid;
         this.strValue = another.strValue;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidStrMember</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidStrMember</tt>.
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

      if (TkRefsetCidStrMember.class.isAssignableFrom(obj.getClass())) {
         TkRefsetCidStrMember another = (TkRefsetCidStrMember) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.c1Uuid.equals(another.c1Uuid)) {
            return false;
         }

         // Compare strValue
         if (!this.strValue.equals(another.strValue)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERefsetCidStrMember</code>.
    *
    * @return a hash code value for this <tt>ERefsetCidStrMember</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TkRefsetCidStrMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetCidStrMember(this, conversionMap, offset, mapAll);
   }

   @Override
   public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      c1Uuid   = new UUID(in.readLong(), in.readLong());
      strValue = UtfHelper.readUtfV7(in, dataVersion);

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<TkRefsetCidStrRevision>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TkRefsetCidStrRevision(in, dataVersion));
         }
      }
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
      buff.append(" str:");
      buff.append("'").append(this.strValue).append("'");
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(c1Uuid.getMostSignificantBits());
      out.writeLong(c1Uuid.getLeastSignificantBits());
      UtfHelper.writeUtf(out, strValue);

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TkRefsetCidStrRevision rmv : revisions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getC1Uuid() {
      return c1Uuid;
   }

   @Override
   public List<TkRefsetCidStrRevision> getRevisionList() {
      return revisions;
   }

   @Override
   public List<TkRefsetCidStrRevision> getRevisions() {
      return revisions;
   }

   public String getStrValue() {
      return strValue;
   }

   @Override
   public TK_REFSET_TYPE getType() {
      return TK_REFSET_TYPE.CID_STR;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Uuid(UUID c1Uuid) {
      this.c1Uuid = c1Uuid;
   }

   public void setStrValue(String strValue) {
      this.strValue = strValue;
   }
}
