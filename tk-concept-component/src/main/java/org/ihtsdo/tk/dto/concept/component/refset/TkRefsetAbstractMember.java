package org.ihtsdo.tk.dto.concept.component.refset;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

public abstract class TkRefsetAbstractMember<V extends TkRevision> extends TkComponent<V> {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public UUID componentUuid;
   public UUID refsetUuid;

   //~--- constructors --------------------------------------------------------

   public TkRefsetAbstractMember() {
      super();
   }

   public TkRefsetAbstractMember(RefexVersionBI another) throws IOException {
      super(another);
      this.componentUuid = Ts.get().getComponent(another.getReferencedComponentNid()).getPrimUuid();
      this.refsetUuid    = Ts.get().getComponent(another.getCollectionNid()).getPrimUuid();
   }

   public TkRefsetAbstractMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetAbstractMember(TkRefsetAbstractMember another, Map<UUID, UUID> conversionMap, long offset,
                                 boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.componentUuid = conversionMap.get(another.componentUuid);
         this.refsetUuid    = conversionMap.get(another.refsetUuid);
      } else {
         this.componentUuid = another.componentUuid;
         this.refsetUuid    = another.refsetUuid;
      }
   }

   public TkRefsetAbstractMember(RefexVersionBI another, NidBitSetBI exclusions,
                                 Map<UUID, UUID> conversionMap, long offset, boolean mapAll,
                                 ViewCoordinate vc)
           throws IOException, ContraditionException {
      super(another, exclusions, conversionMap, offset, mapAll, vc);

      if (mapAll) {
         this.componentUuid =
            conversionMap.get(Ts.get().getComponent(another.getReferencedComponentNid()).getPrimUuid());
         this.refsetUuid = conversionMap.get(Ts.get().getComponent(another.getCollectionNid()).getPrimUuid());
      } else {
         this.componentUuid = Ts.get().getComponent(another.getReferencedComponentNid()).getPrimUuid();
         this.refsetUuid    = Ts.get().getComponent(another.getCollectionNid()).getPrimUuid();
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefset</tt> object, and contains the same values, field by field,
    * as this <tt>ERefset</tt>.
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

      if (TkRefsetAbstractMember.class.isAssignableFrom(obj.getClass())) {
         TkRefsetAbstractMember<?> another = (TkRefsetAbstractMember<?>) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare refsetUuid
         if (!this.refsetUuid.equals(another.refsetUuid)) {
            return false;
         }

         // Compare componentUuid
         if (!this.componentUuid.equals(another.componentUuid)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>ERefset</code>.
    *
    * @return a hash code value for this <tt>ERefset</tt>.
    */
   @Override
   public int hashCode() {
      return this.primordialUuid.hashCode();
   }

   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      refsetUuid    = new UUID(in.readLong(), in.readLong());
      componentUuid = new UUID(in.readLong(), in.readLong());
   }

   /**
    * Returns a string representation of the object.
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(" refex:");
      buff.append(informAboutUuid(this.refsetUuid));
      buff.append(" component:");
      buff.append(informAboutUuid(this.componentUuid));
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(refsetUuid.getMostSignificantBits());
      out.writeLong(refsetUuid.getLeastSignificantBits());
      out.writeLong(componentUuid.getMostSignificantBits());
      out.writeLong(componentUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   public UUID getComponentUuid() {
      return componentUuid;
   }

   public UUID getRefsetUuid() {
      return refsetUuid;
   }

   public abstract TK_REFSET_TYPE getType();

   //~--- set methods ---------------------------------------------------------

   public void setComponentUuid(UUID componentUuid) {
      this.componentUuid = componentUuid;
   }

   public void setRefsetUuid(UUID refsetUuid) {
      this.refsetUuid = refsetUuid;
   }
}
