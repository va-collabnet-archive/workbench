package org.ihtsdo.tk.dto.concept.component.refset.cidlong;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_cnid_long.RefexCnidLongVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

public class TkRefsetCidLongMember extends TkRefsetAbstractMember<TkRefsetCidLongRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID                          c1Uuid;
   public List<TkRefsetCidLongRevision> extraVersions;
   public long                          longValue;

   //~--- constructors --------------------------------------------------------
   public TkRefsetCidLongMember(RefexChronicleBI another) throws IOException {
      super((RefexVersionBI) another.getPrimordialVersion());

      TerminologyStoreDI                               ts        = Ts.get();
      Collection<? extends RefexCnidLongVersionBI> refexes   = another.getVersions();
      int                                              partCount = refexes.size();
      Iterator<? extends RefexCnidLongVersionBI>   itr       = refexes.iterator();
      RefexCnidLongVersionBI                       rv        = itr.next();

      this.c1Uuid = ts.getUuidPrimordialForNid(rv.getCnid1());
      this.longValue = rv.getLong1();

      if (partCount > 1) {
         revisions = new ArrayList<TkRefsetCidLongRevision>(partCount - 1);

         while (itr.hasNext()) {
            rv = itr.next();
            revisions.add(new TkRefsetCidLongRevision(rv));
         }
      }
   }


   public TkRefsetCidLongMember() {
      super();
   }

   public TkRefsetCidLongMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetCidLongMember(TkRefsetCidLongMember another, Map<UUID, UUID> conversionMap, long offset,
                                boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.c1Uuid    = conversionMap.get(another.c1Uuid);
         this.longValue = another.longValue;
      } else {
         this.c1Uuid    = another.c1Uuid;
         this.longValue = another.longValue;
      }
   }

   public TkRefsetCidLongMember(RefexCnidLongVersionBI another, NidBitSetBI exclusions,
                                Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate vc)
           throws IOException, ContradictionException {
      super(another, exclusions, conversionMap, offset, mapAll, vc);

      if (mapAll) {
         this.c1Uuid = conversionMap.get(Ts.get().getComponent(another.getCnid1()).getPrimUuid());
      } else {
         this.c1Uuid = Ts.get().getComponent(another.getCnid1()).getPrimUuid();
      }

      this.longValue = another.getLong1();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidLongMember</tt> object, and contains the same values,
    * field by field, as this <tt>ERefsetCidLongMember</tt>.
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

      if (TkRefsetCidLongMember.class.isAssignableFrom(obj.getClass())) {
         TkRefsetCidLongMember another = (TkRefsetCidLongMember) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.c1Uuid.equals(another.c1Uuid)) {
            return false;
         }

         // Compare longValue
         if (this.longValue != another.longValue) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERefsetCidLongMember</code>.
    *
    * @return  a hash code value for this <tt>ERefsetCidLongMember</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TkRefsetCidLongMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetCidLongMember(this, conversionMap, offset, mapAll);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      c1Uuid    = new UUID(in.readLong(), in.readLong());
      longValue = in.readLong();

      int versionSize = in.readInt();

      if (versionSize > 0) {
         extraVersions = new ArrayList<TkRefsetCidLongRevision>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            extraVersions.add(new TkRefsetCidLongRevision(in, dataVersion));
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
      buff.append(" c1: ");
      buff.append(informAboutUuid(this.c1Uuid));
      buff.append(" long:");
      buff.append(this.longValue);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(c1Uuid.getMostSignificantBits());
      out.writeLong(c1Uuid.getLeastSignificantBits());
      out.writeLong(longValue);

      if (extraVersions == null) {
         out.writeInt(0);
      } else {
    	 checkListInt(extraVersions.size()); 
         out.writeInt(extraVersions.size());

         for (TkRefsetCidLongRevision rmv : extraVersions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getC1Uuid() {
      return c1Uuid;
   }

   public long getLongValue() {
      return longValue;
   }

   @Override
   public List<TkRefsetCidLongRevision> getRevisionList() {
      return extraVersions;
   }

   @Override
   public List<TkRefsetCidLongRevision> getRevisions() {
      return extraVersions;
   }

   @Override
   public TK_REFSET_TYPE getType() {
      return TK_REFSET_TYPE.CID_LONG;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Uuid(UUID c1Uuid) {
      this.c1Uuid = c1Uuid;
   }

   public void setLongValue(long longValue) {
      this.longValue = longValue;
   }
}
