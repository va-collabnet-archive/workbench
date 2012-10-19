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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TKRefexUuidIntRevision represents a version of a uuid-int type refex member in the
 * eConcept format and contains methods specific for interacting with this
 * version. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexUuidIntRevision extends TkRevision {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid associated with this TK Refex Uuid Int Revision.
     */
    public UUID uuid1;
    /**
     * The int associated with this TK Refex Uuid Int Revision.
     */
    public int int1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Uuid Int Revision.
     */
    public TkRefexUuidIntRevision() {
        super();
    }

    /**
     * Instantiates a new TK Refex Uuid Int Revision based on the
     * <code>refexNidIntVersion</code>.
     *
     * @param refexNidIntVersion the refex nid int version specifying how to construct
     * this TK Refex Uuid Int Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidIntRevision(RefexNidIntVersionBI refexNidIntVersion) throws IOException {
        super(refexNidIntVersion);

        TerminologyStoreDI ts = Ts.get();

        this.uuid1 = ts.getUuidPrimordialForNid(refexNidIntVersion.getNid1());
        this.int1 = refexNidIntVersion.getInt1();
    }

    /**
     * Instantiates a new TK Refex Uuid Int Revision based on the specified data
     * input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * Int Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexUuidIntRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Uuid Int Revision based on
     * <code>another</code> TK Refex Uuid Int Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid Int Revision specifying how to construct
     * this TK Refex Uuid Int Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Int Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Int Revisionr based on the conversion map
     */
    public TkRefexUuidIntRevision(TkRefexUuidIntRevision another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.int1 = another.int1;
        } else {
            this.uuid1 = another.uuid1;
            this.int1 = another.int1;
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidIntVersion</tt> object, and contains the same values, field
     * by field, as this <tt>ERefsetCidIntVersion</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; <code>false</code>
     * otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkRefexUuidIntRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidIntRevision another = (TkRefexUuidIntRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

            // Compare int1
            if (this.int1 != another.int1) {
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
     * TK Refex Uuid Int Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Int Revision based on the conversion map
     * @return the converted TK Refex Uuid Int Revision
     */
    @Override
    public TkRefexUuidIntRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidIntRevision(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Uuid Int Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        int1 = in.readInt();
    }

    /**
     * Returns a string representation of this TK Refex Uuid Int Revision object.
     *
     * @return a string representation of this TK Refex Uuid Int Revision object
     * including the concept represented by the uuid and the integer.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1:");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" int: ");
        buff.append(this.int1);
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    /**
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(uuid1.getMostSignificantBits());
        out.writeLong(uuid1.getLeastSignificantBits());
        out.writeInt(int1);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the uuid associated with this TK Refex Uuid Int Revision.
     *
     * @return the uuid associated with this TK Refex Uuid Int Revision
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the int associated with this TK Refex Uuid Int Revision.
     *
     * @return the int1 associated with this TK Refex Uuid Int Revision
     */
    public int getInt1() {
        return int1;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid associated with this TK Refex Uuid Int Revision.
     *
     * @param uuid1 the uuid associated with this TK Refex Uuid Int Revision
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    /**
     * Sets the int associated with this TK Refex Uuid Int Revision.
     *
     * @param int1 the int associated with this TK Refex Uuid Int Revision
     */
    public void setInt1(int int1) {
        this.int1 = int1;
    }
}
