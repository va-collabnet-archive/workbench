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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkRefexUuidUuidUuidRevision a version of a uuid-uuid-uuid type
 * refex member in the eConcept format and contains methods specific for
 * interacting with this version. Further discussion of the eConcept format can
 * be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexUuidUuidUuidRevision extends TkRevision {

    /**
     * The Constant serialVersionUID.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The first uuid associated with this TK Refex Uuid Uuid Uuid Revision.
     */
    public UUID uuid1;
    /**
     * The second uuid associated with this TK Refex Uuid Uuid Uuid Revision.
     */
    public UUID uuid2;
    /**
     * The third uuid associated with this TK Refex Uuid Uuid Uuid Revision.
     */
    public UUID uuid3;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Uuid Uuid Uuid Revision.
     */
    public TkRefexUuidUuidUuidRevision() {
        super();
    }

    /**
     * Instantiates a new TK Refex Uuid Uuid Uuid Revision based on the
     * <code>refexNidNidNidVersion</code>.
     *
     * @param refexNidNidNidVersion the refex nid nid nid version specifying how to
     * construct this TK Refex Uuid Uuid Uuid Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidUuidUuidRevision(RefexNidNidNidVersionBI refexNidNidNidVersion) throws IOException {
        super(refexNidNidNidVersion);

        TerminologyStoreDI ts = Ts.get();

        this.uuid1 = ts.getUuidPrimordialForNid(refexNidNidNidVersion.getNid1());
        this.uuid2 = ts.getUuidPrimordialForNid(refexNidNidNidVersion.getNid2());
        this.uuid3 = ts.getUuidPrimordialForNid(refexNidNidNidVersion.getNid3());
    }

    /**
     * Instantiates a new TK Refex Uuid Uuid Uuid Revision based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * Uuid Uuid Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexUuidUuidUuidRevision(DataInput in, int dataVersion)
            throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Uuid Uuid Uuid Revision based on
     * <code>another</code> TK Refex Uuid Uuid Uuid Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid Uuid Uuid Revision specifying how to construct
     * this TK Refex Uuid Uuid Uuid Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Uuid Uuid Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Uuid Uuid Revision based on the conversion map
     */
    public TkRefexUuidUuidUuidRevision(TkRefexUuidUuidUuidRevision another, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.uuid2 = conversionMap.get(another.uuid2);
            this.uuid3 = conversionMap.get(another.uuid3);
        } else {
            this.uuid1 = another.uuid1;
            this.uuid2 = another.uuid2;
            this.uuid3 = another.uuid3;
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidCidCidVersion</tt> object, and contains the same values,
     * field by field, as this <tt>ERefsetCidCidCidVersion</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; <code>false</code>
     * otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkRefexUuidUuidUuidRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidUuidUuidRevision another = (TkRefexUuidUuidUuidRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

            // Compare uuid2
            if (!this.uuid2.equals(another.uuid2)) {
                return false;
            }

            // Compare uuid3
            if (!this.uuid3.equals(another.uuid3)) {
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
     * TK Refex Uuid Uuid Uuid Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Uuid Uuid Revision based on the conversion map
     * @return the converted TK Refex Uuid Uuid Uuid Revision
     */
    @Override
    public TkRefexUuidUuidUuidRevision makeConversion(Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        return new TkRefexUuidUuidUuidRevision(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Uuid Uuid Uuid Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        uuid2 = new UUID(in.readLong(), in.readLong());
        uuid3 = new UUID(in.readLong(), in.readLong());
    }

    /**
     * Returns a string representation of this TK Refex Uuid Uuid Uuid Revision object.
     *
     * @return a string representation of this TK Refex Uuid Uuid Uuid Revision object
     * including the concepts represented by the uuids.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1:");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" c2:");
        buff.append(informAboutUuid(this.uuid2));
        buff.append(" c3:");
        buff.append(informAboutUuid(this.uuid3));
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
        out.writeLong(uuid2.getMostSignificantBits());
        out.writeLong(uuid2.getLeastSignificantBits());
        out.writeLong(uuid3.getMostSignificantBits());
        out.writeLong(uuid3.getLeastSignificantBits());
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the first uuid associated with this TK Refex Uuid Uuid Uuid Revision.
     *
     * @return the first uuid associated with this TK Refex Uuid Uuid Uuid Revision
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the second uuid associated with this TK Refex Uuid Uuid Uuid Revision.
     *
     * @return the second uuid associated with this TK Refex Uuid Uuid Uuid Revision
     */
    public UUID getUuid2() {
        return uuid2;
    }

    /**
     * Gets the third uuid associated with this TK Refex Uuid Uuid Uuid Revision.
     *
     * @return the third uuid associated with this TK Refex Uuid Uuid Uuid Revision
     */
    public UUID getUuid3() {
        return uuid3;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the first uuid associated with this TK Refex Uuid Uuid Uuid Revision.
     *
     * @param uuid1 the first uuid associated with this TK Refex Uuid Uuid Uuid Revision
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

     /**
     * Sets the second uuid associated with this TK Refex Uuid Uuid Uuid Revision.
     *
     * @param uuid2 the second uuid associated with this TK Refex Uuid Uuid Uuid Revision
     */
    public void setUuid2(UUID uuid2) {
        this.uuid2 = uuid2;
    }

    /**
     * Sets the third uuid associated with this TK Refex Uuid Uuid Uuid Revision.
     *
     * @param uuid2 the third uuid associated with this TK Refex Uuid Uuid Uuid Revision
     */
    public void setUuid3(UUID uuid3) {
        this.uuid3 = uuid3;
    }
}
