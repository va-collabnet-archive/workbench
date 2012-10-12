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
import org.ihtsdo.tk.api.id.LongIdBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * The Class TkIdentifierLong represents a version of a Long identifier in the
 * eConcept format and contains methods specific to interacting with a version
 * of a Long identifier. Further discussion of the eConcept format can be found
 * on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkIdentifierLong extends TkIdentifier {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The long denotation associated with this TK Identifier Long.
     */
    public long denotation;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Identifier Long.
     */
    public TkIdentifierLong() {
        super();
    }

    /**
     * Instantiates a new TK Identifier Long based on the given
     * <code>id</code>.
     *
     * @param id the long identifier specifying how to construct this TK
     * Identifier Long
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkIdentifierLong(LongIdBI id) throws IOException {
        super(id);
        denotation = id.getDenotation();
    }

    /**
     * Instantiates a new TK Identifier Long based on the specified data input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Identifier
     * Long
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkIdentifierLong(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
        denotation = in.readLong();
    }

    /**
     * Instantiates a new TK Identifier Long based on
     * <code>another</code> TK Identifier Long and allows for uuid conversion.
     *
     * @param another the TK Identifier Long specifying how to construct this TK
     * Identifier Long
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Identifier Long
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Identifier Long based on the conversion map
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
     * <tt>EIdentifierVersionLong</tt> object, and contains the same values,
     * field by field, as this <tt>EIdentifierVersionLong</tt>.
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
     * Returns a hash code for this
     * <code>EIdentifierVersionLong</code>.
     *
     * @return a hash code value for this <tt>EIdentifierVersionLong</tt>.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{
                    (int) denotation, (int) (denotation >>> 32), statusUuid.hashCode(), pathUuid.hashCode(), (int) time,
                    (int) (time >>> 32)
                });
    }

    /**
     *
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Identifier Long
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Identifier Long based on the conversion map
     * @return the converted TK Identifier Long
     */
    @Override
    public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkIdentifierLong(this, conversionMap, offset, mapAll);
    }

    /**
     * Returns a string representation of this TK Identifier Long object.
     *
     * @return a string representation of this TK Identifier Long object
     * including the denotation.
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

    /**
     * Writes the value of this TK Identifier Long to an external source.
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public void writeDenotation(DataOutput out) throws IOException {
        out.writeLong(denotation);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     *
     * @return the Long denotation associated with this identifier
     */
    @Override
    public Long getDenotation() {
        return denotation;
    }

    /**
     *
     * @return IDENTIFIER_PART_TYPES.LONG
     */
    @Override
    public IDENTIFIER_PART_TYPES getIdType() {
        return IDENTIFIER_PART_TYPES.LONG;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     *
     * @param denotation the Long denotation associated with this TK Identifier
     * Long
     */
    @Override
    public void setDenotation(Object denotation) {
        this.denotation = (Long) denotation;
    }
}
