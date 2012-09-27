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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class TkRefsetUuidUuidRevision.
 */
public class TkRefsetUuidUuidRevision extends TkRevision {
   
   /** The Constant serialVersionUID. */
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   /** The uuid1. */
   public UUID uuid1;
   
   /** The uuid2. */
   public UUID uuid2;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk refset uuid uuid revision.
    */
   public TkRefsetUuidUuidRevision() {
      super();
   }

   /**
    * Instantiates a new tk refset uuid uuid revision.
    *
    * @param refexNidNidVersion the refex nid nid version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public TkRefsetUuidUuidRevision(RefexNidNidVersionBI refexNidNidVersion) throws IOException {
      super(refexNidNidVersion);

      TerminologyStoreDI ts = Ts.get();

      this.uuid1 = ts.getUuidPrimordialForNid(refexNidNidVersion.getNid1());
      this.uuid2 = ts.getUuidPrimordialForNid(refexNidNidVersion.getNid2());
   }

   /**
    * Instantiates a new tk refset uuid uuid revision.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ClassNotFoundException the class not found exception
    */
   public TkRefsetUuidUuidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   /**
    * Instantiates a new tk refset uuid uuid revision.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkRefsetUuidUuidRevision(TkRefsetUuidUuidRevision another, Map<UUID, UUID> conversionMap, long offset,
                                 boolean mapAll) {
      super(another, conversionMap, offset, mapAll);

      if (mapAll) {
         this.uuid1 = conversionMap.get(another.uuid1);
         this.uuid2 = conversionMap.get(another.uuid2);
      } else {
         this.uuid1 = another.uuid1;
         this.uuid2 = another.uuid2;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetCidCidVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetCidCidVersion</tt>.
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

      if (TkRefsetUuidUuidRevision.class.isAssignableFrom(obj.getClass())) {
         TkRefsetUuidUuidRevision another = (TkRefsetUuidUuidRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare uuid1
         if (!this.uuid1.equals(another.uuid1)) {
            return false;
         }

         // Compare uuid2
         if (!this.uuid2.equals(another.uuid2)) {
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
   public TkRefsetUuidUuidRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetUuidUuidRevision(this, conversionMap, offset, mapAll);
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#readExternal(java.io.DataInput, int)
    */
   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      uuid1 = new UUID(in.readLong(), in.readLong());
      uuid2 = new UUID(in.readLong(), in.readLong());
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
      buff.append(" c2:");
      buff.append(informAboutUuid(this.uuid2));
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
      out.writeLong(uuid2.getMostSignificantBits());
      out.writeLong(uuid2.getLeastSignificantBits());
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
    * Gets the uuid2.
    *
    * @return the uuid2
    */
   public UUID getUuid2() {
      return uuid2;
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
    * Sets the uuid2.
    *
    * @param uuid2 the new uuid2
    */
   public void setUuid2(UUID uuid2) {
      this.uuid2 = uuid2;
   }
}
