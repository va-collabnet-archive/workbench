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
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.id.LongIdBI;
import org.ihtsdo.tk.api.id.StringIdBI;
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
 * The Class TkIdentifier represents a version of an identifier in the eConcept
 * format and contains methods general to interacting with a version of an
 * identifier. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public abstract class TkIdentifier extends TkRevision {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid representing the authority associated with TK Identifier.
     */
    public UUID authorityUuid;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Identifier.
     */
    public TkIdentifier() {
        super();
    }

    /**
     * Instantiates a new TK Identifier based on the given
     * <code>id</code>.
     *
     * @param id the identifier specifying how to construct this TK Identifier
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkIdentifier(IdBI id) throws IOException {
        super(id);
        this.authorityUuid = Ts.get().getComponent(id.getAuthorityNid()).getPrimUuid();
    }

    /**
     * Instantiates a new TK Identifier based on the specified data input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Identifier
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkIdentifier(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Identifier based on
     * <code>another</code> TK Identifier and allows for uuid conversion.
     *
     * @param another the TK Identifier specifying how to construct this TK
     * Identifier
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Identifier
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Identifier based on the conversion map
     */
    public TkIdentifier(TkIdentifier another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.authorityUuid = conversionMap.get(another.authorityUuid);
        } else {
            this.authorUuid = another.authorUuid;
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Converts an
     * <code>IdBI</code> object to a
     * <code>TkIdentifier</code> object.
     *
     * @param id the id
     * @return the TK Identifier
     * @throws IOException signals that an I/O exception has occurred
     */
    public static TkIdentifier convertId(IdBI id) throws IOException {
        Object denotation = id.getDenotation();

        switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
            case LONG:
                return new TkIdentifierLong((LongIdBI) id);

            case STRING:
                return new TkIdentifierString((StringIdBI) id);

            case UUID:
                return new TkIdentifierUuid((UuidIdBI) id);

            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EIdentifierVersion</tt> object, and contains the same values, field
     * by field, as this <tt>EIdentifierVersion</tt>.
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

        if (TkIdentifier.class.isAssignableFrom(obj.getClass())) {
            TkIdentifier another = (TkIdentifier) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare authorityUuid
            if (!this.authorityUuid.equals(another.authorityUuid)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>EIdentifierVersion</code>.
     *
     * @return a hash code value for this <tt>EIdentifierVersion</tt>.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{statusUuid.hashCode(), pathUuid.hashCode(), (int) time,
                    (int) (time >>> 32)});
    }

    /**
     * Reads a TK Identifier from an external source using the specified data
     * input
     * <code>in</code> for the given
     * <code>dataVersion</code> of the external source.
     *
     * @param in the data input specifying how to construct this TK Identifier
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        authorityUuid = new UUID(in.readLong(), in.readLong());
    }

    /**
     * Returns a string representation of this TK Identifier object.
     *
     * @return a string representation of this TK Identifier object including
     * the authority.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(" authority:");
        buff.append(informAboutUuid(this.authorityUuid));
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    /**
     * Writes the value of this TK Identifier to an external source.
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    public abstract void writeDenotation(DataOutput out) throws IOException;

    /**
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(authorityUuid.getMostSignificantBits());
        out.writeLong(authorityUuid.getLeastSignificantBits());
        writeDenotation(out);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the uuid representing the authority associated with this TK
     * Identifier.
     *
     * @return the authority uuid associated with this identifier
     */
    public UUID getAuthorityUuid() {
        return authorityUuid;
    }

    /**
     * Gets the denotation associated with this identifier. This is the
     * actual identifier value.
     *
     * @return the denotation associated with this identifier
     */
    public abstract Object getDenotation();

    /**
     * Gets the type of identifier: <code>Long</code, <code>String</code>, or
     * <code>UUID</code>.
     *
     * @return the IDENTIFIER_PART_TYPES representing the type of identifier
     */
    public abstract IDENTIFIER_PART_TYPES getIdType();

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets uuid representing the authority associated with this TK Identifier.
     *
     * @param authorityUuid uuid representing the authority associated with this TK Identifier
     */
    public void setAuthorityUuid(UUID authorityUuid) {
        this.authorityUuid = authorityUuid;
    }

    /**
     * Sets the denotation associated with this identifier. This is the
     * actual identifier value.
     *
     * @param denotation the denotation associated with this TK Identifier
     */
    public abstract void setDenotation(Object denotation);
}
