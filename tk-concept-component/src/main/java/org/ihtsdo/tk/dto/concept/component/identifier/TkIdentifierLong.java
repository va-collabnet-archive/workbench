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

import org.ihtsdo.tk.api.id.LongIdBI;
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
 * The Class TkIdentifierLong.
 */
public class TkIdentifierLong extends TkIdentifier {
   
   /** The Constant serialVersionUID. */
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   /** The denotation. */
   public long denotation;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk identifier long.
    */
   public TkIdentifierLong() {
      super();
   }

   /**
    * Instantiates a new tk identifier long.
    *
    * @param id the id
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public TkIdentifierLong(LongIdBI id) throws IOException {
      super(id);
      denotation = id.getDenotation();
   }

   /**
    * Instantiates a new tk identifier long.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ClassNotFoundException the class not found exception
    */
   public TkIdentifierLong(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super(in, dataVersion);
      denotation = in.readLong();
   }

   /**
    * Instantiates a new tk identifier long.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkIdentifierLong(TkIdentifierLong another, Map<UUID, UUID> conversionMap, long offset,
                           boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.denotation = another.denotation;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EIdentifierVersionLong</tt> object, and contains the same values, field by field,
    * as this <tt>EIdentifierVersionLong</tt>.
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

      if (TkIdentifierLong.class.isAssignableFrom(obj.getClass())) {
         TkIdentifierLong another = (TkIdentifierLong) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare denotation
         if (this.denotation != another.denotation) {
            return false;
         }

         // Compare their parents
         return super.equals(obj);
      }

      return false;
   }

   /**
    * Returns a hash code for this <code>EIdentifierVersionLong</code>.
    *
    * @return a hash code value for this <tt>EIdentifierVersionLong</tt>.
    */
   @Override
   public int hashCode() {
      return Arrays.hashCode(new int[] {
         (int) denotation, (int) (denotation >>> 32), statusUuid.hashCode(), pathUuid.hashCode(), (int) time,
         (int) (time >>> 32)
      });
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
    */
   @Override
   public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkIdentifierLong(this, conversionMap, offset, mapAll);
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
      buff.append(" denotation:");
      buff.append(this.denotation);
      buff.append(" ");
      buff.append(super.toString());

      return buff.toString();
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier#writeDenotation(java.io.DataOutput)
    */
   @Override
   public void writeDenotation(DataOutput out) throws IOException {
      out.writeLong(denotation);
   }

   //~--- get methods ---------------------------------------------------------

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier#getDenotation()
    */
   @Override
   public Long getDenotation() {
      return denotation;
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier#getIdType()
    */
   @Override
   public IDENTIFIER_PART_TYPES getIdType() {
      return IDENTIFIER_PART_TYPES.LONG;
   }

   //~--- set methods ---------------------------------------------------------

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier#setDenotation(java.lang.Object)
    */
   @Override
   public void setDenotation(Object denotation) {
      this.denotation = (Long) denotation;
   }
}
