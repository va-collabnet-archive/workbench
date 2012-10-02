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
package org.ihtsdo.tk.dto.concept.component.description;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.ext.I_DescribeExternally;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class TkDescriptionRevision.
 */
public class TkDescriptionRevision extends TkRevision implements I_DescribeExternally {
   
   /** The Constant serialVersionUID. */
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   /** The initial case significant. */
   public boolean initialCaseSignificant;
   
   /** The lang. */
   public String  lang;
   
   /** The text. */
   public String  text;
   
   /** The type uuid. */
   public UUID    typeUuid;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk description revision.
    */
   public TkDescriptionRevision() {
      super();
   }

   /**
    * Instantiates a new tk description revision.
    *
    * @param descriptionVersion the description version
    * @throws IOException signals that an I/O exception has occurred.
    */
   public TkDescriptionRevision(DescriptionVersionBI descriptionVersion) throws IOException {
      super(descriptionVersion);
      this.initialCaseSignificant = descriptionVersion.isInitialCaseSignificant();
      this.lang                   = descriptionVersion.getLang();
      this.text                   = descriptionVersion.getText();
      this.typeUuid               = Ts.get().getUuidPrimordialForNid(descriptionVersion.getTypeNid());
   }

   /**
    * Instantiates a new tk description revision.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException signals that an I/O exception has occurred.
    * @throws ClassNotFoundException the class not found exception
    */
   public TkDescriptionRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   /**
    * Instantiates a new tk description revision.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkDescriptionRevision(TkDescriptionRevision another, Map<UUID, UUID> conversionMap, long offset,
                                boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.initialCaseSignificant = another.initialCaseSignificant;
      this.lang                   = another.lang;
      this.text                   = another.text;

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
    * <tt>EDescriptionVersion</tt> object, and contains the same values, field by field,
    * as this <tt>EDescriptionVersion</tt>.
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

      if (TkDescriptionRevision.class.isAssignableFrom(obj.getClass())) {
         TkDescriptionRevision another = (TkDescriptionRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare initialCaseSignificant
         if (this.initialCaseSignificant != another.initialCaseSignificant) {
            return false;
         }

         // Compare lang
         if (!this.lang.equals(another.lang)) {
            return false;
         }

         // Compare text
         if (!this.text.equals(another.text)) {
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
   public TkDescriptionRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkDescriptionRevision(this, conversionMap, offset, mapAll);
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#readExternal(java.io.DataInput, int)
    */
   @Override
   public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      initialCaseSignificant = in.readBoolean();
      lang                   = in.readUTF();

      if (dataVersion < 7) {
         text = in.readUTF();
      } else {
         int textlength = in.readInt();

         if (textlength > 32000) {
            int    textBytesLength = in.readInt();
            byte[] textBytes       = new byte[textBytesLength];

            in.readFully(textBytes);
            text = new String(textBytes, "UTF-8");
         } else {
            text = in.readUTF();
         }
      }

      typeUuid = new UUID(in.readLong(), in.readLong());
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
      buff.append(" ics:");
      buff.append(this.initialCaseSignificant);
      buff.append(" lang:");
      buff.append("'").append(this.lang).append("'");
      buff.append(" text:");
      buff.append("'").append(this.text).append("'");
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
      out.writeBoolean(initialCaseSignificant);
      out.writeUTF(lang);
      out.writeInt(text.length());

      if (text.length() > 32000) {
         byte[] textBytes = text.getBytes("UTF-8");

         out.writeInt(textBytes.length);
         out.write(textBytes);
      } else {
         out.writeUTF(text);
      }

      out.writeLong(typeUuid.getMostSignificantBits());
      out.writeLong(typeUuid.getLeastSignificantBits());
   }

   //~--- get methods ---------------------------------------------------------

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_DescribeExternally#getLang()
    */
   @Override
   public String getLang() {
      return lang;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_DescribeExternally#getText()
    */
   @Override
   public String getText() {
      return text;
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.api.ext.I_DescribeExternally#getTypeUuid()
    */
   @Override
   public UUID getTypeUuid() {
      return typeUuid;
   }

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_DescribeExternally#isInitialCaseSignificant()
    */
   @Override
   public boolean isInitialCaseSignificant() {
      return initialCaseSignificant;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the initial case significant.
    *
    * @param initialCaseSignificant the new initial case significant
    */
   public void setInitialCaseSignificant(boolean initialCaseSignificant) {
      this.initialCaseSignificant = initialCaseSignificant;
   }

   /**
    * Sets the lang.
    *
    * @param lang the new lang
    */
   public void setLang(String lang) {
      this.lang = lang;
   }

   /**
    * Sets the text.
    *
    * @param text the new text
    */
   public void setText(String text) {
      this.text = text;
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
