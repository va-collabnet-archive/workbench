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
package org.ihtsdo.tk.dto.concept.component.identifier;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.id.StringIdBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * The Class TkIdentifierString represents a version of a String identifier in
 * the eConcept format and contains methods specific to interacting with a
 * version of a String identifier. Further discussion of the eConcept format can
 * be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkIdentifierString extends TkIdentifier {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The String denotation associated with this TK Identifier String.
     */
    public String denotation;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Identifier String.
     */
    public TkIdentifierString() {
        super();
    }

    /**
     * Instantiates a new TK Identifier String based on the given
     * <code>id</code>.
     *
     * @param id the String identifier specifying how to construct this TK
     * Identifier String
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkIdentifierString(StringIdBI id) throws IOException {
        super(id);
        denotation = id.getDenotation();
    }

    /**
     * Instantiates a new TK Identifier String based on the specified data
     * input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Identifier
     * String
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkIdentifierString(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
        denotation = in.readUTF();
    }

    /**
     * Instantiates a new TK Identifier String based on
     * <code>another</code> TK Identifier String and allows for uuid conversion.
     *
     * @param another the TK Identifier String specifying how to construct this
     * TK Identifier String
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Identifier String
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Identifier String based on the conversion map
     */
    public TkIdentifierString(TkIdentifierString another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.denotation = another.denotation;
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EIdentifierVersionString</tt> object, and contains the same values,
     * field by field, as this <tt>EIdentifierVersionString</tt>.
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

        if (TkIdentifierString.class.isAssignableFrom(obj.getClass())) {
            TkIdentifierString another = (TkIdentifierString) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare denotation
            if (!this.denotation.equals(another.denotation)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>EIdentifierVersionString</code>.
     *
     * @return a hash code value for this <tt>EIdentifierVersionString</tt>.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{denotation.hashCode(), statusUuid.hashCode(), pathUuid.hashCode(),
                    (int) time, (int) (time >>> 32)});
    }

    /**
     *
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Identifier String
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Identifier String based on the conversion map
     * @return the converted TK Identifier String
     */
    @Override
    public TkIdentifierString makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkIdentifierString(this, conversionMap, offset, mapAll);
    }

    /**
     * Returns a string representation of this TK Identifier String object.
     *
     * @return a string representation of this TK Identifier String object
     * including the denotation.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" denotation:");
        buff.append("'").append(this.denotation).append("'");
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    /**
     * Writes the value of this TK Identifier String to an external source.
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public void writeDenotation(DataOutput out) throws IOException {
        out.writeUTF(denotation);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     *
     * @return the String denotation associated with this identifier
     */
    @Override
    public String getDenotation() {
        return denotation;
    }

    /**
     *
     * @return IDENTIFIER_PART_TYPES.STRING
     */
    @Override
    public IDENTIFIER_PART_TYPES getIdType() {
        return IDENTIFIER_PART_TYPES.STRING;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     *
     * @param denotation the String denotation associated with this TK
     * Identifier String
     */
    @Override
    public void setDenotation(Object denotation) {
        this.denotation = (String) denotation;
    }
}
