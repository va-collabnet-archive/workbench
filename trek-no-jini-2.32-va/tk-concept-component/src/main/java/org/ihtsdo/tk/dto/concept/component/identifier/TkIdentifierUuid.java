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
import org.ihtsdo.tk.api.id.UuidIdBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * The Class TkIdentifierUuid represents a version of a uuid identifier in the
 * eConcept format and contains methods specific to interacting with a version
 * of a uuid identifier. Further discussion of the eConcept format can be found
 * on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkIdentifierUuid extends TkIdentifier {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    /**
     * The uuid representing the authority associated with the TK Identifier Uuid.
     */
    public static UUID generatedUuid = UUID.fromString("2faa9262-8fb2-11db-b606-0800200c9a66");
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid denotation associated with this TK Identifier Uuid.
     */
    public UUID denotation;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Identifier Uuid.
     */
    public TkIdentifierUuid() {
        super();
    }

    /**
     * Instantiates a new TK Identifier Uuid based on the given
     * <code>uuid</code>.
     *
     * @param uuid the uuid specifying how to construct this TK
     * Identifier Uuid
     */
    public TkIdentifierUuid(UUID uuid) {
        super();
        this.denotation = uuid;
        this.authorityUuid = generatedUuid;
    }

    /**
     * Instantiates a new TK Identifier Uuid based on the given
     * <code>id</code>.
     *
     * @param id the uuid identifier specifying how to construct this TK
     * Identifier Uuid
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkIdentifierUuid(UuidIdBI id) throws IOException {
        super(id);
        denotation = id.getDenotation();
    }

    /**
     * Instantiates a new TK Identifier Uuid based on the specified data input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Identifier Uuid
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkIdentifierUuid(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
        denotation = new UUID(in.readLong(), in.readLong());
    }

    /**
     * Instantiates a new TK Identifier Uuid based on
     * <code>another</code> TK Identifier Uuid and allows for uuid conversion.
     *
     * @param another the TK Identifier Uuid specifying how to construct this TK
     * Identifier Uuid
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Identifier Uuid
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Identifier Uuid based on the conversion map
     */
    public TkIdentifierUuid(TkIdentifierUuid another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.denotation = conversionMap.get(another.denotation);
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EIdentifierVersionUuid</tt> object, and contains the same values,
     * field by field, as this <tt>EIdentifierVersionUuid</tt>.
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

        if (TkIdentifierUuid.class.isAssignableFrom(obj.getClass())) {
            TkIdentifierUuid another = (TkIdentifierUuid) obj;

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
     * <code>EIdentifierVersionUuid</code>.
     *
     * @return a hash code value for this <tt>EIdentifierVersionUuid</tt>.
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
     * TK Identifier Uuid
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Identifier Uuid based on the conversion map
     * @return the converted TK Identifier Uuid
     */
    @Override
    public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkIdentifierUuid(this, conversionMap, offset, mapAll);
    }

    /**
     * Returns a string representation of this TK Identifier Uuid object.
     *
     * @return a string representation of this TK Identifier Uuid object
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
     * Writes the value of this TK Identifier Uuid to an external source.
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public void writeDenotation(DataOutput out) throws IOException {
        out.writeLong(denotation.getMostSignificantBits());
        out.writeLong(denotation.getLeastSignificantBits());
    }

    //~--- get methods ---------------------------------------------------------

    /**
     *
     * @return the uuid denotation associated with this identifier
     */
    @Override
    public UUID getDenotation() {
        return denotation;
    }

    /**
     *
     * @return IDENTIFIER_PART_TYPES.UUID
     */
    @Override
    public IDENTIFIER_PART_TYPES getIdType() {
        return IDENTIFIER_PART_TYPES.UUID;
    }

    //~--- set methods ---------------------------------------------------------

    /**
     *
     * @param denotation the uuid denotation associated with this TK Identifier
     * Uuid
     */
    @Override
    public void setDenotation(Object denotation) {
        this.denotation = (UUID) denotation;
    }
}
