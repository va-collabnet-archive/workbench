package org.ihtsdo.tk.dto.concept.component.refset.cid;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;

public class TkRefsetCidRevision extends TkRevision {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID c1Uuid;

   //~--- constructors --------------------------------------------------------

   public TkRefsetCidRevision() {
      super();
   }

   public TkRefsetCidRevision(RefexCnidVersionBI another) throws IOException {
      super(another);

      TerminologyStoreDI ts = Ts.get();

      this.c1Uuid = ts.getUuidPrimordialForNid(another.getCnid1());
   }

   public TkRefsetCidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetCidRevision(TkRefsetCidRevision another, Map<UUID, UUID> conversionMap, long offset,
                              boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.c1Uuid = conversionMap.get(another.c1Uuid);
      } else {
         this.c1Uuid = another.c1Uuid;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidVersion</tt>.
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

      if (TkRefsetCidRevision.class.isAssignableFrom(obj.getClass())) {
         TkRefsetCidRevision another = (TkRefsetCidRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.c1Uuid.equals(another.c1Uuid)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   @Override
   public TkRefsetCidRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetCidRevision(this, conversionMap, offset, mapAll);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      c1Uuid = new UUID(in.readLong(), in.readLong());
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
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(c1Uuid.getMostSignificantBits());
      out.writeLong(c1Uuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getC1Uuid() {
      return c1Uuid;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Uuid(UUID c1Uuid) {
      this.c1Uuid = c1Uuid;
   }
}