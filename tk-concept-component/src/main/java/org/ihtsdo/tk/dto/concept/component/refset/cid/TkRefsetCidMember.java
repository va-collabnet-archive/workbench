package org.ihtsdo.tk.dto.concept.component.refset.cid;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
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

public class TkRefsetCidMember extends TkRefsetAbstractMember<TkRefsetCidRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID c1Uuid;

   //~--- constructors --------------------------------------------------------
   public TkRefsetCidMember(RefexChronicleBI another) throws IOException {
      super((RefexVersionBI) another.getPrimordialVersion());

      TerminologyStoreDI                               ts        = Ts.get();
      Collection<? extends RefexCnidVersionBI> refexes   = another.getVersions();
      int                                              partCount = refexes.size();
      Iterator<? extends RefexCnidVersionBI>   itr       = refexes.iterator();
      RefexCnidVersionBI                       rv        = itr.next();

      this.c1Uuid = ts.getUuidPrimordialForNid(rv.getCnid1());

      if (partCount > 1) {
         revisions = new ArrayList<TkRefsetCidRevision>(partCount - 1);

         while (itr.hasNext()) {
            rv = itr.next();
            revisions.add(new TkRefsetCidRevision(rv));
         }
      }
   }

   public TkRefsetCidMember() {
      super();
   }

   public TkRefsetCidMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetCidMember(TkRefsetCidMember another, Map<UUID, UUID> conversionMap, long offset,
                            boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.c1Uuid = conversionMap.get(another.c1Uuid);
      } else {
         this.c1Uuid = another.c1Uuid;
      }
   }

   public TkRefsetCidMember(RefexCnidVersionBI another, NidBitSetBI exclusions,
                            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate vc)
           throws IOException, ContraditionException {
      super(another, exclusions, conversionMap, offset, mapAll, vc);

      if (mapAll) {
         this.c1Uuid = conversionMap.get(Ts.get().getComponent(another.getCnid1()).getPrimUuid());
      } else {
         this.c1Uuid = Ts.get().getComponent(another.getCnid1()).getPrimUuid();
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidMember</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidMember</tt>.
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

      if (TkRefsetCidMember.class.isAssignableFrom(obj.getClass())) {
         TkRefsetCidMember another = (TkRefsetCidMember) obj;

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

   /**
    * Returns a hash code for this <code>ERefsetCidMember</code>.
    *
    * @return a hash code value for this <tt>ERefsetCidMember</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TkRefsetCidMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetCidMember(this, conversionMap, offset, mapAll);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      c1Uuid = new UUID(in.readLong(), in.readLong());

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<TkRefsetCidRevision>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TkRefsetCidRevision(in, dataVersion));
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
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(c1Uuid.getMostSignificantBits());
      out.writeLong(c1Uuid.getLeastSignificantBits());

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TkRefsetCidRevision rmv : revisions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getC1Uuid() {
      return c1Uuid;
   }

   @Override
   public List<TkRefsetCidRevision> getRevisionList() {
      return revisions;
   }

   @Override
   public TK_REFSET_TYPE getType() {
      return TK_REFSET_TYPE.CID;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Uuid(UUID c1Uuid) {
      this.c1Uuid = c1Uuid;
   }
}
