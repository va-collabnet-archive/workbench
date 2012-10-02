/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.dto.concept.component.refex;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
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

// TODO: Auto-generated Javadoc
/**
 * The Class TkRefexAbstractMember.
 *
 * @param <V> the value type
 */
public abstract class TkRefexAbstractMember<V extends TkRevision> extends TkComponent<V> {
   
   /** The Constant serialVersionUID. */
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   /** The component uuid. */
   public UUID componentUuid;
   
   /** The refset uuid. */
   public UUID refsetUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk refex abstract member.
    */
   public TkRefexAbstractMember() {
      super();
   }

   /**
    * Instantiates a new tk refex abstract member.
    *
    * @param refexVersion the refex version
    * @throws IOException signals that an I/O exception has occurred.
    */
   public TkRefexAbstractMember(RefexVersionBI refexVersion) throws IOException {
      super(refexVersion);
      this.componentUuid = Ts.get().getComponent(refexVersion.getReferencedComponentNid()).getPrimUuid();
      this.refsetUuid    = Ts.get().getComponent(refexVersion.getRefexNid()).getPrimUuid();
   }

   /**
    * Instantiates a new tk refex abstract member.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException signals that an I/O exception has occurred.
    * @throws ClassNotFoundException the class not found exception
    */
   public TkRefexAbstractMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   /**
    * Instantiates a new tk refex abstract member.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkRefexAbstractMember(TkRefexAbstractMember another, Map<UUID, UUID> conversionMap, long offset,
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

   /**
    * Instantiates a new tk refex abstract member.
    *
    * @param refexVersion the refex version
    * @param excludedNids the excluded nids
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    * @param viewCoordinate the view coordinate
    * @throws IOException signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   public TkRefexAbstractMember(RefexVersionBI refexVersion, NidBitSetBI excludedNids,
                                 Map<UUID, UUID> conversionMap, long offset, boolean mapAll,
                                 ViewCoordinate viewCoordinate)
           throws IOException, ContradictionException {
      super(refexVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

      if (mapAll) {
         this.componentUuid =
            conversionMap.get(Ts.get().getComponent(refexVersion.getReferencedComponentNid()).getPrimUuid());
         this.refsetUuid = conversionMap.get(Ts.get().getComponent(refexVersion.getRefexNid()).getPrimUuid());
      } else {
         this.componentUuid = Ts.get().getComponent(refexVersion.getReferencedComponentNid()).getPrimUuid();
         this.refsetUuid    = Ts.get().getComponent(refexVersion.getRefexNid()).getPrimUuid();
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

      if (TkRefexAbstractMember.class.isAssignableFrom(obj.getClass())) {
         TkRefexAbstractMember<?> another = (TkRefexAbstractMember<?>) obj;

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

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkComponent#readExternal(java.io.DataInput, int)
    */
   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      refsetUuid    = new UUID(in.readLong(), in.readLong());
      componentUuid = new UUID(in.readLong(), in.readLong());
   }

   /**
    * Returns a string representation of the object.
    *
    * @return the string
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

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkComponent#writeExternal(java.io.DataOutput)
    */
   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(refsetUuid.getMostSignificantBits());
      out.writeLong(refsetUuid.getLeastSignificantBits());
      out.writeLong(componentUuid.getMostSignificantBits());
      out.writeLong(componentUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the component uuid.
    *
    * @return the component uuid
    */
   public UUID getComponentUuid() {
      return componentUuid;
   }

   /**
    * Gets the refex uuid.
    *
    * @return the refex uuid
    */
   public UUID getRefexUuid() {
      return refsetUuid;
   }

   /**
    * Gets the type.
    *
    * @return the type
    */
   public abstract TK_REFEX_TYPE getType();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the component uuid.
    *
    * @param componentUuid the new component uuid
    */
   public void setComponentUuid(UUID componentUuid) {
      this.componentUuid = componentUuid;
   }

   /**
    * Sets the refset uuid.
    *
    * @param refsetUuid the new refset uuid
    */
   public void setRefsetUuid(UUID refsetUuid) {
      this.refsetUuid = refsetUuid;
   }
}
