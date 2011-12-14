package org.ihtsdo.tk.dto.concept.component.refset.cidflt;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_float.RefexCnidFloatVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;

public class TkRefsetCidFloatMember extends TkRefsetAbstractMember<TkRefsetCidFloatRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID  c1Uuid;
   public float floatValue;

   //~--- constructors --------------------------------------------------------

   public TkRefsetCidFloatMember() {
      super();
   }

   public TkRefsetCidFloatMember(RefexChronicleBI another) throws IOException {
      super((RefexVersionBI) another.getPrimordialVersion());

      TerminologyStoreDI                            ts        = Ts.get();
      Collection<? extends RefexCnidFloatVersionBI> refexes   = another.getVersions();
      int                                           partCount = refexes.size();
      Iterator<? extends RefexCnidFloatVersionBI>   itr       = refexes.iterator();
      RefexCnidFloatVersionBI                       rv        = itr.next();

      this.c1Uuid     = ts.getUuidPrimordialForNid(rv.getCnid1());
      this.floatValue = rv.getFloat1();

      if (partCount > 1) {
         revisions = new ArrayList<TkRefsetCidFloatRevision>(partCount - 1);

         while (itr.hasNext()) {
            rv = itr.next();
            revisions.add(new TkRefsetCidFloatRevision(rv));
         }
      }
   }

   public TkRefsetCidFloatMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetCidFloatMember(TkRefsetCidFloatMember another, Map<UUID, UUID> conversionMap, long offset,
                                 boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.c1Uuid     = conversionMap.get(another.c1Uuid);
         this.floatValue = another.floatValue;
      } else {
         this.c1Uuid     = another.c1Uuid;
         this.floatValue = another.floatValue;
      }
   }

   public TkRefsetCidFloatMember(RefexCnidFloatVersionBI another, NidBitSetBI exclusions,
                                 Map<UUID, UUID> conversionMap, long offset, boolean mapAll,
                                 ViewCoordinate vc)
           throws IOException, ContradictionException {
      super(another, exclusions, conversionMap, offset, mapAll, vc);

      if (mapAll) {
         this.c1Uuid = conversionMap.get(Ts.get().getComponent(another.getCnid1()).getPrimUuid());
      } else {
         this.c1Uuid = Ts.get().getComponent(another.getCnid1()).getPrimUuid();
      }

      this.floatValue = another.getFloat1();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidFloatMember</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidFloatMember</tt>.
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

      if (TkRefsetCidFloatMember.class.isAssignableFrom(obj.getClass())) {
         TkRefsetCidFloatMember another = (TkRefsetCidFloatMember) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare c1Uuid
         if (!this.c1Uuid.equals(another.c1Uuid)) {
            return false;
         }

         // Compare floatValue
         if (this.floatValue != another.floatValue) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERefsetCidFloatMember</code>.
    *
    * @return a hash code value for this <tt>ERefsetCidFloatMember</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TkRefsetCidFloatMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetCidFloatMember(this, conversionMap, offset, mapAll);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      c1Uuid     = new UUID(in.readLong(), in.readLong());
      floatValue = in.readFloat();

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<TkRefsetCidFloatRevision>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TkRefsetCidFloatRevision(in, dataVersion));
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
      buff.append(" flt:");
      buff.append(this.floatValue);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(c1Uuid.getMostSignificantBits());
      out.writeLong(c1Uuid.getLeastSignificantBits());
      out.writeFloat(floatValue);

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TkRefsetCidFloatRevision rmv : revisions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getC1Uuid() {
      return c1Uuid;
   }

   public float getFloatValue() {
      return floatValue;
   }

   @Override
   public List<TkRefsetCidFloatRevision> getRevisionList() {
      return revisions;
   }

   @Override
   public List<TkRefsetCidFloatRevision> getRevisions() {
      return revisions;
   }

   @Override
   public TK_REFSET_TYPE getType() {
      return TK_REFSET_TYPE.CID_FLOAT;
   }

   //~--- set methods ---------------------------------------------------------

   public void setC1Uuid(UUID c1Uuid) {
      this.c1Uuid = c1Uuid;
   }

   public void setFloatValue(float floatValue) {
      this.floatValue = floatValue;
   }
}
