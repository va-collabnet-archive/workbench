package org.ihtsdo.tk.dto.concept.component.refset.str;

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

public class TkRefsetStrMember extends TkRefsetAbstractMember<TkRefsetStrRevision> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public String strValue;

   //~--- constructors --------------------------------------------------------

   public TkRefsetStrMember() {
      super();
   }

   public TkRefsetStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetStrMember</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetStrMember</tt>.
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

      if (TkRefsetStrMember.class.isAssignableFrom(obj.getClass())) {
         TkRefsetStrMember another = (TkRefsetStrMember) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
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
    * Returns a hash code for this <code>ERefsetStrMember</code>.
    *
    * @return a hash code value for this <tt>ERefsetStrMember</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      strValue = UtfHelper.readUtfV6(in, dataVersion);

      int versionSize = in.readInt();

      if (versionSize > 0) {
         revisions = new ArrayList<TkRefsetStrRevision>(versionSize);

         for (int i = 0; i < versionSize; i++) {
            revisions.add(new TkRefsetStrRevision(in, dataVersion));
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
      buff.append(" str:");
      buff.append("'").append(this.strValue).append("'");
      buff.append("; ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      UtfHelper.writeUtf(out, strValue);

      if (revisions == null) {
         out.writeInt(0);
      } else {
         out.writeInt(revisions.size());

         for (TkRefsetStrRevision rmv : revisions) {
            rmv.writeExternal(out);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public List<TkRefsetStrRevision> getRevisionList() {
      return revisions;
   }

   public String getStrValue() {
      return strValue;
   }

   @Override
   public TK_REFSET_TYPE getType() {
      return TK_REFSET_TYPE.STR;
   }

   //~--- set methods ---------------------------------------------------------

   public void setStrValue(String strValue) {
      this.strValue = strValue;
   }
}
