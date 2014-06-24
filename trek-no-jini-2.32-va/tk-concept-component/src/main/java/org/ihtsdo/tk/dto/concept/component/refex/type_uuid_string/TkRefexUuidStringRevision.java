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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_string;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringVersionBI;

/**
 * The Class TkRefexUuidStringRevision a version of a uuid-string type refex
 * member in the eConcept format and contains methods specific for interacting
 * with this version. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexUuidStringRevision extends TkRevision {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid associated with this TK Refex Uuid String Revision.
     */
    public UUID uuid1;
    /**
     * The string associated with this TK Refex Uuid String Revision.
     */
    public String string1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Uuid String Revision.
     */
    public TkRefexUuidStringRevision() {
        super();
    }

    /**
     * Instantiates a new TK Refex Uuid String Revision based on the
     * <code>refexNidStringVersion</code>.
     *
     * @param refexNidStringVersion the refex nid string version specifying how to
     * construct this TK Refex Uuid String Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidStringRevision(RefexNidStringVersionBI refexNidStringVersion) throws IOException {
        super(refexNidStringVersion);

        TerminologyStoreDI ts = Ts.get();

        this.uuid1 = ts.getUuidPrimordialForNid(refexNidStringVersion.getNid1());
        this.string1 = refexNidStringVersion.getString1();
    }

    /**
     * Instantiates a new TK Refex Uuid String Revision based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * String Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexUuidStringRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Uuid String Revision based on
     * <code>another</code> TK Refex Uuid String Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid String Revision specifying how to construct
     * this TK Refex Uuid String Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid String Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid String Revision based on the conversion map
     */
    public TkRefexUuidStringRevision(TkRefexUuidStringRevision another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.string1 = another.string1;
        } else {
            this.uuid1 = another.uuid1;
            this.string1 = another.string1;
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidStrVersion</tt> object, and contains the same values, field
     * by field, as this <tt>ERefsetCidStrVersion</tt>.
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

        if (TkRefexUuidStringRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidStringRevision another = (TkRefexUuidStringRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

            // Compare string1
            if (!this.string1.equals(another.string1)) {
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
     * TK Refex Uuid String Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid String Revision based on the conversion map
     * @return the converted TK Refex Uuid String Revision
     */
    @Override
    public TkRefexUuidStringRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidStringRevision(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Uuid String Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        string1 = UtfHelper.readUtfV7(in, dataVersion);
    }

    /**
     * Returns a string representation of this TK Refex Uuid String Revision object.
     *
     * @return a string representation of this TK Refex Uuid String Revision object
     * including the concept represented by the uuid and the string.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1: ");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" str: ");
        buff.append("'").append(this.string1).append("'");
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
        out.writeUTF(string1);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the uuid associated with this TK Refex Uuid String Revision.
     *
     * @return the uuid associated with this TK Refex Uuid String Revision
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the string associated with this TK Refex Uuid String Revision.
     *
     * @return the string associated with this TK Refex Uuid String Revision
     */
    public String getString1() {
        return string1;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid associated with this TK Refex Uuid String Revision.
     *
     * @param uuid1 the uuid associated with this TK Refex Uuid String Revision
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    /**
     * Sets the string associated with this TK Refex Uuid String Revision.
     *
     * @param string1 the new string associated with this TK Refex Uuid String Revision
     */
    public void setString1(String string1) {
        this.string1 = string1;
    }
}
