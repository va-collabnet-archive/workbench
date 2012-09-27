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
package org.ihtsdo.tk.dto.concept.component.attribute;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.ext.I_ConceptualizeExternally;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class TkConceptAttributesRevision.
 */
public class TkConceptAttributesRevision extends TkRevision implements I_ConceptualizeExternally {
   
   /** The Constant serialVersionUID. */
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   /** The defined. */
   public boolean defined;

   //~--- constructors --------------------------------------------------------

   /**
    * Instantiates a new tk concept attributes revision.
    */
   public TkConceptAttributesRevision() {
      super();
   }

   /**
    * Instantiates a new tk concept attributes revision.
    *
    * @param conceptAttributeVersion the concept attribute version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public TkConceptAttributesRevision(ConceptAttributeVersionBI conceptAttributeVersion) throws IOException {
      super(conceptAttributeVersion);
      this.defined = conceptAttributeVersion.isDefined();
   }

   /**
    * Instantiates a new tk concept attributes revision.
    *
    * @param in the in
    * @param dataVersion the data version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ClassNotFoundException the class not found exception
    */
   public TkConceptAttributesRevision(DataInput in, int dataVersion)
           throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   /**
    * Instantiates a new tk concept attributes revision.
    *
    * @param another the another
    * @param conversionMap the conversion map
    * @param offset the offset
    * @param mapAll the map all
    */
   public TkConceptAttributesRevision(TkConceptAttributesRevision another, Map<UUID, UUID> conversionMap,
                                      long offset, boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.defined = another.defined;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>EConceptAttributesVersion</tt> object, and contains the same values, field by field,
    * as this <tt>EConceptAttributesVersion</tt>.
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

      if (TkConceptAttributesRevision.class.isAssignableFrom(obj.getClass())) {
         TkConceptAttributesRevision another = (TkConceptAttributesRevision) obj;

         // =========================================================
         // Compare properties of 'this' class to the 'another' class
         // =========================================================
         // Compare defined
         if (this.defined != another.defined) {
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
   public TkConceptAttributesRevision makeConversion(Map<UUID, UUID> conversionMap, long offset,
           boolean mapAll) {
      return new TkConceptAttributesRevision(this, conversionMap, offset, mapAll);
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.dto.concept.component.TkRevision#readExternal(java.io.DataInput, int)
    */
   @Override
   public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super.readExternal(in, dataVersion);
      defined = in.readBoolean();
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
      buff.append(" defined:");
      buff.append(this.defined);
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
      out.writeBoolean(defined);
   }

   //~--- get methods ---------------------------------------------------------

   /*
    * (non-Javadoc)
    *
    * @see org.ihtsdo.etypes.I_ConceptualizeExternally#isDefined()
    */
   @Override
   public boolean isDefined() {
      return defined;
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the defined.
    *
    * @param defined the new defined
    */
   public void setDefined(boolean defined) {
      this.defined = defined;
   }
}
