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
package org.ihtsdo.tk.dto.concept.component.identifier;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.id.LongIdBI;
import org.ihtsdo.tk.api.id.StringIdBI;
import org.ihtsdo.tk.api.id.UuidIdBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class TkIdentifier.
 */
public abstract class TkIdentifier extends TkRevision {
   
   /** The Constant serialVersionUID. */
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   /** The authority uuid. */
   public UUID authorityUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk identifier.
    */
   public TkIdentifier() {
      super();
   }

   /**
    * Instantiates a new tk identifier.
    *
    * @param id the id
    * @throws IOException signals that an I/O exception has occurred.
    */
   public TkIdentifier(IdBI id) throws IOException {
      super(id);
      this.authorityUuid = Ts.get().getComponent(id.getAuthorityNid()).getPrimUuid();
   }

   /**
    * Instantiates a new tk identifier.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException signals that an I/O exception has occurred.
    * @throws ClassNotFoundException the class not found exception
    */
   public TkIdentifier(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   /**
    * Instantiates a new tk identifier.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkIdentifier(TkIdentifier another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.authorityUuid = conversionMap.get(another.authorityUuid);
      } else {
         this.authorUuid = another.authorUuid;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Convert id.
    *
    * @param id the id
    * @return the tk identifier
    * @throws IOException signals that an I/O exception has occurred.
    */
   public static TkIdentifier convertId(IdBI id) throws IOException {
      Object denotation = id.getDenotation();

      switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
      case LONG :
         return new TkIdentifierLong((LongIdBI) id);

      case STRING :
         return new TkIdentifierString((StringIdBI) id);

      case UUID :
         return new TkIdentifierUuid((UuidIdBI) id);

      default :
         throw new UnsupportedOperationException();
      }
   }

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EIdentifierVersion</tt> object, and contains the same values, field by field,
    * as this <tt>EIdentifierVersion</tt>.
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

      if (TkIdentifier.class.isAssignableFrom(obj.getClass())) {
         TkIdentifier another = (TkIdentifier) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare authorityUuid
         if (!this.authorityUuid.equals(another.authorityUuid)) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>EIdentifierVersion</code>.
    *
    * @return a hash code value for this <tt>EIdentifierVersion</tt>.
    */
   @Override
   public int hashCode() {
      return Arrays.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time,
                                         (int) (time >>> 32) });
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#readExternal(java.io.DataInput, int)
    */
   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      authorityUuid = new UUID(in.readLong(), in.readLong());
   }

   /**
    * Returns a string representation of the object.
    *
    * @return the string
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();

      buff.append(" authority:");
      buff.append(informAboutUuid(this.authorityUuid));
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   /**
    * Write denotation.
    *
    * @param out the out
    * @throws IOException signals that an I/O exception has occurred.
    */
   public abstract void writeDenotation(DataOutput out) throws IOException;

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#writeExternal(java.io.DataOutput)
    */
   @Override
   public void writeExternal(DataOutput out) throws IOException {
      super.writeExternal(out);
      out.writeLong(authorityUuid.getMostSignificantBits());
      out.writeLong(authorityUuid.getLeastSignificantBits());
      writeDenotation(out);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the authority uuid.
    *
    * @return the authority uuid
    */
   public UUID getAuthorityUuid() {
      return authorityUuid;
   }

   /**
    * Gets the denotation.
    *
    * @return the denotation
    */
   public abstract Object getDenotation();

   /**
    * Gets the id type.
    *
    * @return the id type
    */
   public abstract IDENTIFIER_PART_TYPES getIdType();

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the authority uuid.
    *
    * @param authorityUuid the new authority uuid
    */
   public void setAuthorityUuid(UUID authorityUuid) {
      this.authorityUuid = authorityUuid;
   }

   /**
    * Sets the denotation.
    *
    * @param denotation the new denotation
    */
   public abstract void setDenotation(Object denotation);
}
