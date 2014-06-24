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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkRefexUuidLongRevision a version of a uuid-long type refex member
 * in the eConcept format and contains methods specific for interacting with
 * this version. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexUuidLongRevision extends TkRevision {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid associated with this TK Refex Uuid Long Revision.
     */
    public UUID uuid1;
    /**
     * The long associated with this TK Refex Uuid Long Revision.
     */
    public long long1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Uuid Long Revision.
     */
    public TkRefexUuidLongRevision() {
        super();
    }

    /**
     * Instantiates a new TK Refex Uuid Long Revision based on the
     * <code>refexNidLongVersion</code>.
     *
     * @param refexNidLongVersion the refex nid long version specifying how to construct
     * this TK Refex Uuid Long Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidLongRevision(RefexNidLongVersionBI refexNidLongVersion) throws IOException {
        super(refexNidLongVersion);

        TerminologyStoreDI ts = Ts.get();

        this.uuid1 = ts.getUuidPrimordialForNid(refexNidLongVersion.getNid1());
        this.long1 = refexNidLongVersion.getLong1();
    }

    /**
     * Instantiates a new TK Refex Uuid Long Revision based on the specified data
     * input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * Long Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexUuidLongRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Uuid Long Revision based on
     * <code>another</code> TK Refex Uuid Long Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid Long Revision specifying how to construct
     * this TK Refex Uuid Long Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Long Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Long Revision based on the conversion map
     */
    public TkRefexUuidLongRevision(TkRefexUuidLongRevision another, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.long1 = another.long1;
        } else {
            this.uuid1 = another.uuid1;
            this.long1 = another.long1;
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidLongVersion</tt> object, and contains the same values,
     * field by field, as this <tt>ERefsetCidLongVersion</tt>.
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

        if (TkRefexUuidLongRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidLongRevision another = (TkRefexUuidLongRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

            // Compare long1
            if (this.long1 != another.long1) {
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
     * TK Refex Uuid Long Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Long Revision based on the conversion map
     * @return the converted TK Refex Uuid Long Revision
     */
    @Override
    public TkRefexUuidLongRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidLongRevision(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Uuid Long Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        long1 = in.readLong();
    }

    /**
     * Returns a string representation of this TK Refex Uuid Long Revision object.
     *
     * @return a string representation of this TK Refex Uuid Long Revision object
     * including the concept represented by the uuid and the long.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1: ");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" long: ");
        buff.append(this.long1);
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
        out.writeLong(long1);
    }

    //~--- get methods ---------------------------------------------------------
     /**
     * Gets the uuid associated with this TK Refex Uuid Long Revision.
     *
     * @return the uuid associated with this TK Refex Uuid Long Revision
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the long associated with this TK Refex Uuid Long Revision.
     *
     * @return the long associated with this TK Refex Uuid Long Revision
     */
    public long getLong1() {
        return long1;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid associated with this TK Refex Uuid Long Revision.
     *
     * @param uuid1 the uuid associated with this TK Refex Uuid Long Revision
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    /**
     * Sets the long associated with this TK Refex Uuid Long Revision.
     *
     * @param long1 the long associated with this TK Refex Uuid Long Revision
     */
    public void setLong1(long long1) {
        this.long1 = long1;
    }
}
