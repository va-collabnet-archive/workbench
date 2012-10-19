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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkRefexUuidFloatRevision represents a version of a uuid-int type
 * refex member in the eConcept format and contains methods specific for
 * interacting with this version. Further discussion of the eConcept format can
 * be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexUuidFloatRevision extends TkRevision {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public UUID uuid1;
    public float float1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Uuid Float Revision.
     */
    public TkRefexUuidFloatRevision() {
        super();
    }

    /**
     * Instantiates a new TK Refex Uuid Float Revision based on the
     * <code>refexNidFloatVersion</code>.
     *
     * @param refexNidFloatVersion the refex nid float version specifying how to construct
     * this TK Refex Uuid Float Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidFloatRevision(RefexNidFloatVersionBI refexNidFloatVersion) throws IOException {
        super(refexNidFloatVersion);

        TerminologyStoreDI ts = Ts.get();

        this.uuid1 = ts.getUuidPrimordialForNid(refexNidFloatVersion.getNid1());
        this.float1 = refexNidFloatVersion.getFloat1();
    }

    /**
     * Instantiates a new TK Refex Uuid Float Revision based on the specified data
     * input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * Float Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexUuidFloatRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Uuid Float Revision based on
     * <code>another</code> TK Refex Uuid Float Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid Float Revision specifying how to construct
     * this TK Refex Uuid Float Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Float Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Float Revision based on the conversion map
     */
    public TkRefexUuidFloatRevision(TkRefexUuidFloatRevision another, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.float1 = another.float1;
        } else {
            this.uuid1 = another.uuid1;
            this.float1 = another.float1;
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidFloatVersion</tt> object, and contains the same values,
     * field by field, as this <tt>ERefsetCidFloatVersion</tt>.
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

        if (TkRefexUuidFloatRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidFloatRevision another = (TkRefexUuidFloatRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

            // Compare float1
            if (this.float1 != another.float1) {
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
     * TK Refex Uuid Float Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Float Revision based on the conversion map
     * @return the converted TK Refex Uuid Float Revision
     */
    @Override
    public TkRefexUuidFloatRevision makeConversion(Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        return new TkRefexUuidFloatRevision(this, conversionMap, offset, mapAll);
    }

    /**
     * 
     * @param in the data input specifying how to construct this TK
     * Refex Uuid Float Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        float1 = in.readFloat();
    }

     /**
     * Returns a string representation of this TK Refex Uuid Float Revision object.
     *
     * @return a string representation of this TK Refex Uuid Float Revision object
     * including the concept represented by the uuid and the float.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1:");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" flt:");
        buff.append(this.float1);
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
        out.writeFloat(float1);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the uuid associated with this TK Refex Uuid Float Revision.
     *
     * @return the uuid associated with this TK Refex Uuid Float Revision.
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the float associated with this TK Refex Uuid Float Revision.
     *
     * @return the float associated with this TK Refex Uuid Float Revision.
     */
    public float getFloat1() {
        return float1;
    }

    //~--- set methods ---------------------------------------------------------
   /**
     * Sets the uuid associated with this TK Refex Uuid Float Revision.
     *
     * @param uuid1 the uuid associated with this TK Refex Uuid Float Revision
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    /**
     * Sets the float associated with this TK Refex Uuid Float Revision.
     *
     * @param float1 the float associated with this TK Refex Uuid Float Revision
     */
    public void setFloat1(float float1) {
        this.float1 = float1;
    }
}
