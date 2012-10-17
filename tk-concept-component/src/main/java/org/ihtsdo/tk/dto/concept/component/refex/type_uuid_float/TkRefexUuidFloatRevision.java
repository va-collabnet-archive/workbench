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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkRefexUuidFloatRevision.
 */
public class TkRefexUuidFloatRevision extends TkRevision {
   
   /** The Constant serialVersionUID. */
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   /** The uuid1. */
   public UUID  uuid1;
   
   /** The float1. */
   public float float1;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk refex uuid float revision.
    */
   public TkRefexUuidFloatRevision() {
      super();
   }

   /**
    * Instantiates a new tk refex uuid float revision.
    *
    * @param refexNidFloatVersion the refex nid float version
    * @throws IOException signals that an I/O exception has occurred
    */
   public TkRefexUuidFloatRevision(RefexNidFloatVersionBI refexNidFloatVersion) throws IOException {
      super(refexNidFloatVersion);

      TerminologyStoreDI ts = Ts.get();

      this.uuid1     = ts.getUuidPrimordialForNid(refexNidFloatVersion.getNid1());
      this.float1 = refexNidFloatVersion.getFloat1();
   }

   /**
    * Instantiates a new tk refex uuid float revision.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException signals that an I/O exception has occurred
    * @throws ClassNotFoundException indicates a specified class was not found
    */
   public TkRefexUuidFloatRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   /**
    * Instantiates a new tk refex uuid float revision.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkRefexUuidFloatRevision(TkRefexUuidFloatRevision another, Map<UUID, UUID> conversionMap,
                                   long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.uuid1     = conversionMap.get(another.uuid1);
         this.float1 = another.float1;
      } else {
         this.uuid1     = another.uuid1;
         this.float1 = another.float1;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidFloatVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidFloatVersion</tt>.
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

      if (TkRefexUuidFloatRevision.class.isAssignableFrom(obj.getClass())) {
         TkRefexUuidFloatRevision another = (TkRefexUuidFloatRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare uuid1
         if (!this.uuid1.equals(another.uuid1)) {
            return false;
         }

         // Compare float1
         if (this.float1 != another.float1) {
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
   public TkRefexUuidFloatRevision makeConversion(Map<UUID, UUID> conversionMap, long offset,
           boolean mapAll) {
      return new TkRefexUuidFloatRevision(this, conversionMap, offset, mapAll);
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#readExternal(java.io.DataInput, int)
    */
   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      uuid1     = new UUID(in.readLong(), in.readLong());
      float1 = in.readFloat();
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
      buff.append(" c1:");
      buff.append(informAboutUuid(this.uuid1));
      buff.append(" flt:");
      buff.append(this.float1);
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
      out.writeLong(uuid1.getMostSignificantBits());
      out.writeLong(uuid1.getLeastSignificantBits());
      out.writeFloat(float1);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the uuid1.
    *
    * @return the uuid1
    */
   public UUID getUuid1() {
      return uuid1;
   }

   /**
    * Gets the float1.
    *
    * @return the float1
    */
   public float getFloat1() {
      return float1;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the uuid1.
    *
    * @param uuid1 the new uuid1
    */
   public void setUuid1(UUID uuid1) {
      this.uuid1 = uuid1;
   }

   /**
    * Sets the float1.
    *
    * @param float1 the new float1
    */
   public void setFloat1(float float1) {
      this.float1 = float1;
   }
}
