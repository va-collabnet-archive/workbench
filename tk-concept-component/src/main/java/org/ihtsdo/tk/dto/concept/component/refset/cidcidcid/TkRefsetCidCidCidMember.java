package org.ihtsdo.tk.dto.concept.component.refset.cidcidcid;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_cnid.RefexCnidCnidCnidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;

public class TkRefsetCidCidCidMember extends TkRefsetAbstractMember<TkRefsetCidCidCidRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID c1Uuid;
   public UUID c2Uuid;
   public UUID c3Uuid;

   //~--- constructors --------------------------------------------------------

   public TkRefsetCidCidCidMember() {
      super();
   }

   public TkRefsetCidCidCidMember(RefexChronicleBI another) throws IOException {
      super((RefexVersionBI) another.getPrimordialVersion());

      TerminologyStoreDI                               ts        = Ts.get();
      Collection<? extends RefexCnidCnidCnidVersionBI> refexes   = another.getVersions();
      int                                              partCount = refexes.size();
      Iterator<? extends RefexCnidCnidCnidVersionBI>   itr       = refexes.iterator();
      RefexCnidCnidCnidVersionBI                       rv        = itr.next();

      this.c1Uuid = ts.getUuidPrimordialForNid(rv.getCnid1());
      this.c2Uuid = ts.getUuidPrimordialForNid(rv.getCnid2());
      this.c3Uuid = ts.getUuidPrimordialForNid(rv.getCnid3());

      if (partCount > 1) {
         revisions = new ArrayList<TkRefsetCidCidCidRevision>(partCount - 1);

         while (itr.hasNext()) {
            rv = itr.next();
            revisions.add(new TkRefsetCidCidCidRevision(rv));
         }
      }
   }

   public TkRefsetCidCidCidMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetCidCidCidMember(TkRefsetCidCidCidMember another, Map<UUID, UUID> conversionMap,
                                  long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.c1Uuid = conversionMap.get(another.c1Uuid);
         this.c2Uuid = conversionMap.get(another.c2Uuid);
         this.c3Uuid = conversionMap.get(another.c3Uuid);
      } else {
         this.c1Uuid = another.c1Uuid;
         this.c2Uuid = another.c2Uuid;
         this.c3Uuid = another.c3Uuid;
      }
   }

   public TkRefsetCidCidCidMember(RefexCnidCnidCnidVersionBI another, NidBitSetBI exclusions,
                                  Map<UUID, UUID> conversionMap, long offset, boolean mapAll,
                                  ViewCoordinate vc)
           throws IOException, ContradictionException {
      super(another, exclusions, conversionMap, offset, mapAll, vc);

      if (mapAll) {
         this.c1Uuid = conversionMap.get(Ts.get().getComponent(another.getCnid1()).getPrimUuid());
         this.c2Uuid = conversionMap.get(Ts.get().getComponent(another.getCnid2()).getPrimUuid());
         this.c3Uuid = conversionMap.get(Ts.get().getComponent(another.getCnid3()).getPrimUuid());
      } else {
         this.c1Uuid = Ts.get().getComponent(another.getCnid1()).getPrimUuid();
         this.c2Uuid = Ts.get().getComponent(another.getCnid2()).getPrimUuid();
         this.c3Uuid = Ts.get().getComponent(another.getCnid3()).getPrimUuid();
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidCidCidMember</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidCidCidMember</tt>.
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

      if (TkRefsetCidCidCidMember.class.isAssignableFrom(obj.getClass())) {
         TkRefsetCidCidCidMember another = (TkRefsetCidCidCidMember) obj;

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

         // Compare c3Uuid
         if (!this.c3Uuid.equals(another.c3Uuid)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERefsetCidCidCidMember</code>.
    *
    * @return a hash code value for this <tt>ERefsetCidCidCidMember</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TkRefsetCidCidCidMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetCidCidCidMember(this, conversionMap, offset, mapAll);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      c1Uuid = new UUID(in.readLong(), in.readLong());
      c2Uuid = new UUID(in.readLong(), in.readLong());
      c3Uuid = new UUID(in.readLong(), in.readLong());

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<TkRefsetCidCidCidRevision>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TkRefsetCidCidCidRevision(in, dataVersion));
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
      buff.append(" c3:");
      buff.append(informAboutUuid(this.c3Uuid));
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
      out.writeLong(c3Uuid.getMostSignificantBits());
      out.writeLong(c3Uuid.getLeastSignificantBits());

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TkRefsetCidCidCidRevision rmv : revisions) {
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

   public UUID getC3Uuid() {
      return c3Uuid;
   }

   @Override
   public List<TkRefsetCidCidCidRevision> getRevisionList() {
      return revisions;
   }

   @Override
   public TK_REFSET_TYPE getType() {
      return TK_REFSET_TYPE.CID_CID_CID;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Uuid(UUID c1Uuid) {
      this.c1Uuid = c1Uuid;
   }

   public void setC2Uuid(UUID c2Uuid) {
      this.c2Uuid = c2Uuid;
   }

   public void setC3Uuid(UUID c3Uuid) {
      this.c3Uuid = c3Uuid;
   }
}
