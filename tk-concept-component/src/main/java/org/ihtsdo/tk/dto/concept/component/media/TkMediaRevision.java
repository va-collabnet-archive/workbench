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
package org.ihtsdo.tk.dto.concept.component.media;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class TkMediaRevision.
 */
public class TkMediaRevision extends TkRevision {
   
   /** The Constant serialVersionUID. */
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   /** The text description. */
   public String textDescription;
   
   /** The type uuid. */
   public UUID   typeUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk media revision.
    */
   public TkMediaRevision() {
      super();
   }

   /**
    * Instantiates a new tk media revision.
    *
    * @param mediaVersion the media version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public TkMediaRevision(MediaVersionBI mediaVersion) throws IOException {
      super(mediaVersion);
      this.textDescription = mediaVersion.getTextDescription();
      this.typeUuid        = Ts.get().getUuidPrimordialForNid(mediaVersion.getTypeNid());
   }

   /**
    * Instantiates a new tk media revision.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ClassNotFoundException the class not found exception
    */
   public TkMediaRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   /**
    * Instantiates a new tk media revision.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkMediaRevision(TkMediaRevision another, Map<UUID, UUID> conversionMap, long offset,
                          boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.textDescription = another.textDescription;

      if (mapAll) {
         this.typeUuid = conversionMap.get(another.typeUuid);
      } else {
         this.typeUuid = another.typeUuid;
      }
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EImageVersion</tt> object, and contains the same values, field by field,
    * as this <tt>EImageVersion</tt>.
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

      if (TkMediaRevision.class.isAssignableFrom(obj.getClass())) {
         TkMediaRevision another = (TkMediaRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare textDescription
         if (!this.textDescription.equals(another.textDescription)) {
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
   public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkMediaRevision(this, conversionMap, offset, mapAll);
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#readExternal(java.io.DataInput, int)
    */
   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      textDescription = in.readUTF();
      typeUuid        = new UUID(in.readLong(), in.readLong());
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
      buff.append(" desc:");
      buff.append("'").append(this.textDescription).append("'");
      buff.append(" type:");
      buff.append(informAboutUuid(this.typeUuid));
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
      out.writeUTF(textDescription);
      out.writeLong(typeUuid.getMostSignificantBits());
      out.writeLong(typeUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the text description.
    *
    * @return the text description
    */
   public String getTextDescription() {
      return textDescription;
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
    * Sets the text description.
    *
    * @param textDescription the new text description
    */
   public void setTextDescription(String textDescription) {
      this.textDescription = textDescription;
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
