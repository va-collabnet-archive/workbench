/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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

/**
 * The Class TkConceptAttributesRevision represents a version of a concept
 * attribute in the eConcept format and contains methods for interacting with
 * version of a concept attribute. Further discussion of the eConcept format can
 * be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkConceptAttributesRevision extends TkRevision implements I_ConceptualizeExternally {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing its
     * own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The boolean value indicating if the concept associated with these concept
     * attributes is defined.
     */
    public boolean defined;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Concept Attributes Revision.
     */
    public TkConceptAttributesRevision() {
        super();
    }

    /**
     * Instantiates a new TK Concept Attributes Revision based on the
     * <code>conceptAttributeVersion</code>.
     *
     * @param conceptAttributeVersion the concept attribute version specifying
     * how to construct this TK Concept Attributes Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkConceptAttributesRevision(ConceptAttributeVersionBI conceptAttributeVersion) throws IOException {
        super(conceptAttributeVersion);
        this.defined = conceptAttributeVersion.isDefined();
    }

    /**
     * Instantiates a new TK Concept Attributes Revision based on the specified data
     * input,
     * <code>in</code>..
     *
     * @param in the data input specifying how to construct this TK Concept
     * Attributes Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkConceptAttributesRevision(DataInput in, int dataVersion)
            throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Concept Attributes Revision based on
     * <code>another</code> TK Concept Attributes Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Concept Attributes Revision specifying how to construct this
     * TK Concept Attributes Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Concept Attributes Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Concept Attributes based on the conversion map
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
     * <tt>EConceptAttributesVersion</tt> object, and contains the same values,
     * field by field, as this <tt>EConceptAttributesVersion</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; <code>false</code>
     * otherwise.
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

    /**
     * 
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Concept Attributes Revision
     * @param mapAll to <code>true</code> to map all the uuids in this TK Concept Attributes
     * Revision based on the conversion map
     * @return the converted TK Concept Attributes Revision
     */
    @Override
    public TkConceptAttributesRevision makeConversion(Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        return new TkConceptAttributesRevision(this, conversionMap, offset, mapAll);
    }

    /**
     * 
     * @param in the data input specifying how to construct this TK Concept Attributes Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        defined = in.readBoolean();
    }

    /**
     * Returns a string representation of this TK Concept Attributes Revision object.
     *
     * @return a string representation of this TK Concept Attributes Revision object
     * including if the concept is defined or not
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

    /**
     * 
     * @param out the data output object that writes to the external source
     * @throws IOException 
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(defined);
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * 
     * @return <code>true</code> if associated concept is defined
     */
    @Override
    public boolean isDefined() {
        return defined;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Indicates the associated concept is defined.
     *
     * @param defined set to <code>true</code> to indicate the associated
     * concept is defined
     */
    public void setDefined(boolean defined) {
        this.defined = defined;
    }
}
