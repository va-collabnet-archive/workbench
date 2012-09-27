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
package org.ihtsdo.tk.dto.concept.component.refex.type_int;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class TkRefexIntRevision.
 */
public class TkRefexIntRevision extends TkRevision {
   
   /** The Constant serialVersionUID. */
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   /** The int1. */
   public int int1;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk refex int revision.
    */
   public TkRefexIntRevision() {
      super();
   }

   /**
    * Instantiates a new tk refex int revision.
    *
    * @param refexIntVersion the refex int version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public TkRefexIntRevision(RefexIntVersionBI refexIntVersion) throws IOException {
      super(refexIntVersion);
      this.int1 = refexIntVersion.getInt1();
   }

   /**
    * Instantiates a new tk refex int revision.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ClassNotFoundException the class not found exception
    */
   public TkRefexIntRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   /**
    * Instantiates a new tk refex int revision.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkRefexIntRevision(TkRefexIntRevision another, Map<UUID, UUID> conversionMap, long offset,
                              boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.int1 = another.int1;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetIntVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetIntVersion</tt>.
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

      if (TkRefexIntRevision.class.isAssignableFrom(obj.getClass())) {
         TkRefexIntRevision another = (TkRefexIntRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare int1
         if (this.int1 != another.int1) {
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
   public TkRefexIntRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefexIntRevision(this, conversionMap, offset, mapAll);
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#readExternal(java.io.DataInput, int)
    */
   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      int1 = in.readInt();
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
      buff.append(" int: ");
      buff.append(this.int1);
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
      out.writeInt(int1);
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the int1.
    *
    * @return the int1
    */
   public int getInt1() {
      return int1;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the int1.
    *
    * @param int1 the new int1
    */
   public void setInt1(int int1) {
      this.int1 = int1;
   }
}
