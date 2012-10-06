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
package org.ihtsdo.tk.dto.concept.component.relationship;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class TkRelationshipRevision.
 */
public class TkRelationshipRevision extends TkRevision {
   
   /** The Constant serialVersionUID. */
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   /** The characteristic uuid. */
   public UUID characteristicUuid;
   
   /** The group. */
   public int  group;
   
   /** The refinability uuid. */
   public UUID refinabilityUuid;
   
   /** The type uuid. */
   public UUID typeUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk relationship revision.
    */
   public TkRelationshipRevision() {
      super();
   }

   /**
    * Instantiates a new tk relationship revision.
    *
    * @param relationshipVersion the relationship version
    * @throws IOException signals that an I/O exception has occurred
    */
   public TkRelationshipRevision(RelationshipVersionBI relationshipVersion) throws IOException {
       super(relationshipVersion);
      TerminologyStoreDI ts = Ts.get();

      characteristicUuid = ts.getUuidPrimordialForNid(relationshipVersion.getCharacteristicNid());
      refinabilityUuid   = ts.getUuidPrimordialForNid(relationshipVersion.getRefinabilityNid());
      group              = relationshipVersion.getGroup();
      typeUuid           = ts.getUuidPrimordialForNid(relationshipVersion.getTypeNid());
   }

   /**
    * Instantiates a new tk relationship revision.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException signals that an I/O exception has occurred
    * @throws ClassNotFoundException the class not found exception
    */
   public TkRelationshipRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   /**
    * Instantiates a new tk relationship revision.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkRelationshipRevision(TkRelationshipRevision another, Map<UUID, UUID> conversionMap, long offset,
                                 boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.characteristicUuid = conversionMap.get(another.characteristicUuid);
         this.refinabilityUuid   = conversionMap.get(another.refinabilityUuid);
         this.group              = another.group;
         this.typeUuid           = conversionMap.get(another.typeUuid);
      } else {
         this.characteristicUuid = another.characteristicUuid;
         this.refinabilityUuid   = another.refinabilityUuid;
         this.group              = another.group;
         this.typeUuid           = another.typeUuid;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERelationshipVersion</tt> object, and contains the same values,
    * field by field, as this <tt>ERelationshipVersion</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same;
    *         <code>false</code> otherwise.
    */
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (TkRelationshipRevision.class.isAssignableFrom(obj.getClass())) {
         TkRelationshipRevision another = (TkRelationshipRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare characteristicUuid
         if (!this.characteristicUuid.equals(another.characteristicUuid)) {
            return false;
         }

         // Compare refinabilityUuid
         if (!this.refinabilityUuid.equals(another.refinabilityUuid)) {
            return false;
         }

         // Compare group
         if (this.group != another.group) {
            return false;
         }

         // Compare typeUuid
         if (!this.typeUuid.equals(another.typeUuid)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
    */
   @Override
   public TkRelationshipRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRelationshipRevision(this, conversionMap, offset, mapAll);
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#readExternal(java.io.DataInput, int)
    */
   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      characteristicUuid = new UUID(in.readLong(), in.readLong());
      refinabilityUuid   = new UUID(in.readLong(), in.readLong());
      group              = in.readInt();
      typeUuid           = new UUID(in.readLong(), in.readLong());
   }

   /**
    * Returns a string representation of the object.
    *
    * @return the string
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(this.getClass().getSimpleName()).append(": ");
      buff.append(" type:");
      buff.append(informAboutUuid(this.typeUuid));
      buff.append(" grp:");
      buff.append(this.group);
      buff.append(" char:");
      buff.append(this.characteristicUuid);
      buff.append(" ref:");
      buff.append(this.refinabilityUuid);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#writeExternal(java.io.DataOutput)
    */
   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(characteristicUuid.getMostSignificantBits());
      out.writeLong(characteristicUuid.getLeastSignificantBits());
      out.writeLong(refinabilityUuid.getMostSignificantBits());
      out.writeLong(refinabilityUuid.getLeastSignificantBits());
      out.writeInt(group);
      out.writeLong(typeUuid.getMostSignificantBits());
      out.writeLong(typeUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the characteristic uuid.
    *
    * @return the characteristic uuid
    */
   public UUID getCharacteristicUuid() {
      return characteristicUuid;
   }

   /**
    * Gets the group.
    *
    * @return the group
    */
   public int getGroup() {
      return group;
   }

   /**
    * Gets the rel group.
    *
    * @return the rel group
    */
   public int getRelGroup() {
      return group;
   }

   /**
    * Gets the refinability uuid.
    *
    * @return the refinability uuid
    */
   public UUID getRefinabilityUuid() {
      return refinabilityUuid;
   }

   /**
    * Gets the type uuid.
    *
    * @return the type uuid
    */
   public UUID getTypeUuid() {
      return typeUuid;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the characteristic uuid.
    *
    * @param characteristicUuid the new characteristic uuid
    */
   public void setCharacteristicUuid(UUID characteristicUuid) {
      this.characteristicUuid = characteristicUuid;
   }

   /**
    * Sets the refinability uuid.
    *
    * @param refinabilityUuid the new refinability uuid
    */
   public void setRefinabilityUuid(UUID refinabilityUuid) {
      this.refinabilityUuid = refinabilityUuid;
   }

   /**
    * Sets the rel group.
    *
    * @param relGroup the new rel group
    */
   public void setRelGroup(int relGroup) {
      this.group = relGroup;
   }

   /**
    * Sets the type uuid.
    *
    * @param typeUuid the new type uuid
    */
   public void setTypeUuid(UUID typeUuid) {
      this.typeUuid = typeUuid;
   }
}
