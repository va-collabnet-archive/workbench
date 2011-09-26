package org.ihtsdo.tk.dto.concept.component.refset.member;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.dto.concept.component.TkRevision;
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

public class TkRefsetMember extends TkRefsetAbstractMember<TkRefsetRevision> {
   public static final long serialVersionUID = 1;

   //~--- constructors --------------------------------------------------------

   public TkRefsetMember() {
      super();
   }

   public TkRefsetMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetMember(TkRefsetMember another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetMember</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetMember</tt>.
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

      if (TkRefsetMember.class.isAssignableFrom(obj.getClass())) {

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERefsetMember</code>.
    *
    * @return a hash code value for this <tt>ERefsetMember</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetMember(this, conversionMap, offset, mapAll);
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<TkRefsetRevision>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TkRefsetRevision(in, dataVersion));
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
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);

      if (revisions == null) {
         out.writeInt(0);
      } else {
    	 checkListInt(revisions.size()); 
         out.writeInt(revisions.size());

         for (TkRefsetRevision rmv : revisions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public List<TkRefsetRevision> getRevisionList() {
      return revisions;
   }

   @Override
   public TK_REFSET_TYPE getType() {
      return TK_REFSET_TYPE.MEMBER;
   }
}
