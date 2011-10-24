package org.ihtsdo.tk.dto.concept.component.refset.integer;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;

public class TkRefsetIntMember extends TkRefsetAbstractMember<TkRefsetIntRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public int intValue;

   //~--- constructors --------------------------------------------------------

   public TkRefsetIntMember() {
      super();
   }

   public TkRefsetIntMember(RefexChronicleBI another) throws IOException {
      super((RefexVersionBI) another.getPrimordialVersion());

      TerminologyStoreDI                      ts        = Ts.get();
      Collection<? extends RefexIntVersionBI> refexes   = another.getVersions();
      int                                     partCount = refexes.size();
      Iterator<? extends RefexIntVersionBI>   itr       = refexes.iterator();
      RefexIntVersionBI                       rv        = itr.next();

      this.intValue = rv.getInt1();

      if (partCount > 1) {
         revisions = new ArrayList<TkRefsetIntRevision>(partCount - 1);

         while (itr.hasNext()) {
            rv = itr.next();
            revisions.add(new TkRefsetIntRevision(rv));
         }
      }
   }

   public TkRefsetIntMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetIntMember(TkRefsetIntMember another, Map<UUID, UUID> conversionMap, long offset,
                            boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.intValue = another.intValue;
   }

   public TkRefsetIntMember(RefexIntVersionBI another, NidBitSetBI exclusions, Map<UUID, UUID> conversionMap,
                            long offset, boolean mapAll, ViewCoordinate vc)
           throws IOException, ContraditionException {
      super(another, exclusions, conversionMap, offset, mapAll, vc);
      this.intValue = another.getInt1();
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetIntMember</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetIntMember</tt>.
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

      if (TkRefsetIntMember.class.isAssignableFrom(obj.getClass())) {
         TkRefsetIntMember another = (TkRefsetIntMember) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare intValue
         if (this.intValue != another.intValue) {
            return false;
         }

         // Compare extraVersions
         if (this.revisions == null) {
            if (another.revisions == null) {             // Equal!
            } else if (another.revisions.isEmpty()) {    // Equal!
            } else {
               return false;
            }
         } else if (!this.revisions.equals(another.revisions)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERefsetIntMember</code>.
    *
    * @return a hash code value for this <tt>ERefsetIntMember</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TkRefsetIntMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetIntMember(this, conversionMap, offset, mapAll);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      intValue = in.readInt();

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<TkRefsetIntRevision>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TkRefsetIntRevision(in, dataVersion));
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
      buff.append(" int: ");
      buff.append(this.intValue);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeInt(intValue);

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TkRefsetIntRevision rmv : revisions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public int getIntValue() {
      return intValue;
   }

   @Override
   public List<TkRefsetIntRevision> getRevisionList() {
      return revisions;
   }

   @Override
   public TK_REFSET_TYPE getType() {
      return TK_REFSET_TYPE.INT;
   }

   //~--- set methods ---------------------------------------------------------

   public void setIntValue(int intValue) {
      this.intValue = intValue;
   }
}
