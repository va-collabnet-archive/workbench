package org.ihtsdo.tk.dto.concept.component.refset.cidcidstr;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_str.RefexCnidCnidStrVersionBI;
import org.ihtsdo.tk.dto.concept.UtfHelper;
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

public class TkRefsetCidCidStrMember extends TkRefsetAbstractMember<TkRefsetCidCidStrRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID   c1Uuid;
   public UUID   c2Uuid;
   public String strValue;

   //~--- constructors --------------------------------------------------------

   public TkRefsetCidCidStrMember(RefexChronicleBI another) throws IOException {
      super((RefexVersionBI) another.getPrimordialVersion());

      TerminologyStoreDI                               ts        = Ts.get();
      Collection<? extends RefexCnidCnidStrVersionBI> rels      = another.getVersions();
      int                                              partCount = rels.size();
      Iterator<? extends RefexCnidCnidStrVersionBI>   relItr    = rels.iterator();
      RefexCnidCnidStrVersionBI                       rv        = relItr.next();

      this.c1Uuid = ts.getUuidPrimordialForNid(rv.getCnid1());
      this.c2Uuid = ts.getUuidPrimordialForNid(rv.getCnid2());
      this.strValue = rv.getStr1();

      if (partCount > 1) {
         revisions = new ArrayList<TkRefsetCidCidStrRevision>(partCount - 1);

         while (relItr.hasNext()) {
            rv = relItr.next();
            revisions.add(new TkRefsetCidCidStrRevision(rv));
         }
      }
   }


   public TkRefsetCidCidStrMember() {
      super();
   }

   public TkRefsetCidCidStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetCidCidStrMember(TkRefsetCidCidStrMember another, Map<UUID, UUID> conversionMap,
                                  long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.c1Uuid   = conversionMap.get(another.c1Uuid);
         this.c2Uuid   = conversionMap.get(another.c2Uuid);
         this.strValue = another.strValue;
      } else {
         this.c1Uuid   = another.c1Uuid;
         this.c2Uuid   = another.c2Uuid;
         this.strValue = another.strValue;
      }
   }

   public TkRefsetCidCidStrMember(RefexCnidCnidStrVersionBI another, NidBitSetBI exclusions,
                                  Map<UUID, UUID> conversionMap, long offset, boolean mapAll,
                                  ViewCoordinate vc)
           throws IOException, ContraditionException {
      super(another, exclusions, conversionMap, offset, mapAll, vc);

      if (mapAll) {
         this.c1Uuid = conversionMap.get(Ts.get().getComponent(another.getCnid1()).getPrimUuid());
         this.c2Uuid = conversionMap.get(Ts.get().getComponent(another.getCnid2()).getPrimUuid());
      } else {
         this.c1Uuid = Ts.get().getComponent(another.getCnid1()).getPrimUuid();
         this.c2Uuid = Ts.get().getComponent(another.getCnid2()).getPrimUuid();
      }

      this.strValue = another.getStr1();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidCidStrMember</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidCidStrMember</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same;
    *         <code>false</code> otherwise.
    */
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (TkRefsetCidCidStrMember.class.isAssignableFrom(obj.getClass())) {
         TkRefsetCidCidStrMember another = (TkRefsetCidCidStrMember) obj;

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
    * Returns a hash code for this <code>ERefsetCidCidStrMember</code>.
    *
    * @return a hash code value for this <tt>ERefsetCidCidStrMember</tt>.
    */
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TkRefsetCidCidStrMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetCidCidStrMember(this, conversionMap, offset, mapAll);
   }

   @Override
   public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      c1Uuid   = new UUID(in.readLong(), in.readLong());
      c2Uuid   = new UUID(in.readLong(), in.readLong());
      strValue = UtfHelper.readUtfV7(in, dataVersion);

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<TkRefsetCidCidStrRevision>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TkRefsetCidCidStrRevision(in, dataVersion));
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
      buff.append(" c2:");
      buff.append(informAboutUuid(this.c2Uuid));
      buff.append(" str:");
      buff.append("'" + this.strValue + "'");
      buff.append(" ");
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
      UtfHelper.writeUtf(out, strValue);

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TkRefsetCidCidStrRevision rmv : revisions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getC1Uuid() {
      return c1Uuid;
   }

   public UUID getC2Uuid() {
      return c2Uuid;
   }

   public List<TkRefsetCidCidStrRevision> getRevisionList() {
      return revisions;
   }

   public String getStrValue() {
      return strValue;
   }

   @Override
   public TK_REFSET_TYPE getType() {
      return TK_REFSET_TYPE.CID_CID_STR;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Uuid(UUID c1Uuid) {
      this.c1Uuid = c1Uuid;
   }

   public void setC2Uuid(UUID c2Uuid) {
      this.c2Uuid = c2Uuid;
   }

   public void setStrValue(String strValue) {
      this.strValue = strValue;
   }
}
