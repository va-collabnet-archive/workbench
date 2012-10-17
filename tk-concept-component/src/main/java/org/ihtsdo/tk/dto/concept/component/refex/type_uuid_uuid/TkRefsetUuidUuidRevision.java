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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkRefsetUuidUuidRevision represents a version of a uuid-uuid type refex member
 * in the eConcept format and contains methods specific for interacting with
 * this version. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefsetUuidUuidRevision extends TkRevision {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The first uuid associated with this TK Refex Uuid Uuid Revision.
     */
    public UUID uuid1;
    /**
     * The second uuid associated with this TK Refex Uuid Uuid Revision.
     */
    public UUID uuid2;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refset Uuid Uuid Revision.
     */
    public TkRefsetUuidUuidRevision() {
        super();
    }

    /**
     * Instantiates a new TK Refset Uuid Uuid Revision based on the
     * <code>refexNidUuidVersion</code>.
     *
     * @param refexNidNidVersion the refex nid nid version pecifying how to
     * construct this TK Refex Uuid Uuid Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefsetUuidUuidRevision(RefexNidNidVersionBI refexNidNidVersion) throws IOException {
        super(refexNidNidVersion);

        TerminologyStoreDI ts = Ts.get();

        this.uuid1 = ts.getUuidPrimordialForNid(refexNidNidVersion.getNid1());
        this.uuid2 = ts.getUuidPrimordialForNid(refexNidNidVersion.getNid2());
    }

    /**
     * Instantiates a new TK Refset Uuid Uuid Revision based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * Uuid Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefsetUuidUuidRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refset Uuid Uuid Revision based on
     * <code>another</code> TK Refex Uuid Uuid Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid Uuid Revision specifying how to construct
     * this TK Refex Uuid Uuid Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Uuid Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Uuid Revision based on the conversion map
     */
    public TkRefsetUuidUuidRevision(TkRefsetUuidUuidRevision another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.uuid2 = conversionMap.get(another.uuid2);
        } else {
            this.uuid1 = another.uuid1;
            this.uuid2 = another.uuid2;
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidCidVersion</tt> object, and contains the same values, field
     * by field, as this <tt>ERefsetCidCidVersion</tt>.
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

        if (TkRefsetUuidUuidRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefsetUuidUuidRevision another = (TkRefsetUuidUuidRevision) obj;

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
     * TK Refex Uuid Uuid Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Uuid Revision based on the conversion map
     * @return the converted TK Refex Uuid Uuid Revision
     */
    @Override
    public TkRefsetUuidUuidRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefsetUuidUuidRevision(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Uuid Uuid Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        uuid2 = new UUID(in.readLong(), in.readLong());
    }

    /**
     * Returns a string representation of this TK Refex Uuid Uuid Revision object.
     *
     * @return a string representation of this TK Refex Uuid Uuid Revision object
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
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the first uuid associated with this TK Refex Uuid Uuid Revision.
     *
     * @return the first uuid associated with this TK Refex Uuid Uuid Revision
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the second uuid associated with this TK Refex Uuid Uuid Revision.
     *
     * @return the second uuid associated with this TK Refex Uuid Uuid Revision
     */
    public UUID getUuid2() {
        return uuid2;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the first uuid associated with this TK Refex Uuid Uuid Revision.
     *
     * @param c1Uuid the first uuid associated with this TK Refex Uuid Uuid Revision
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    /**
     * Sets the second uuid associated with this TK Refex Uuid Uuid Revision.
     *
     * @param c2Uuid the second uuid associated with this TK Refex Uuid Uuid Revision
     */
    public void setUuid2(UUID uuid2) {
        this.uuid2 = uuid2;
    }
}
