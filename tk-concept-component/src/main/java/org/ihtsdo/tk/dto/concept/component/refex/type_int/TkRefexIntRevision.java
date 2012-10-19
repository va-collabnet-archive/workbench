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
package org.ihtsdo.tk.dto.concept.component.refex.type_int;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkRefexIntRevision represents a version of a int type refex member
 * in the eConcept format and contains methods specific for interacting with
 * this version. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexIntRevision extends TkRevision {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The int value associated with this TK Refex Int Revision.
     */
    public int int1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Int Revision.
     */
    public TkRefexIntRevision() {
        super();
    }

    /**
     * Instantiates a new TK Refex Int Revision based on the
     * <code>refexIntVersion</code>.
     *
     * @param refexIntVersion the refex int version specifying how to
     * construct this TK Refex Int Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexIntRevision(RefexIntVersionBI refexIntVersion) throws IOException {
        super(refexIntVersion);
        this.int1 = refexIntVersion.getInt1();
    }

    /**
     * Instantiates a new TK Refex Int Revision based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Boolean Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexIntRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Int Revision based on
     * <code>another</code> TK Refex Int Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Int Revision specifying how to construct
     * this TK Refex Int Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Int Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refex Int Revision based on the conversion map
     */
    public TkRefexIntRevision(TkRefexIntRevision another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.int1 = another.int1;
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetIntVersion</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetIntVersion</tt>.
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

        if (TkRefexIntRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefexIntRevision another = (TkRefexIntRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
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
     * TK Refex Int Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refex Int Revision based on the conversion map
     * @return the converted TK Refex Int Revision
     */
    @Override
    public TkRefexIntRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexIntRevision(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Refex Int Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        int1 = in.readInt();
    }

    /**
     * Returns a string representation of this TK Refex Int Revision object.
     *
     * @return a string representation of this TK Refex Int Revision object
     * including the int value
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
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
        out.writeInt(int1);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the int associated with this TK Refex Int Revision.
     *
     * @return the int associated with this TK Refex Int Revision
     */
    public int getInt1() {
        return int1;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the int associated with this TK Refex Int Revision.
     *
     * @param int1 the int associated with this TK Refex Int Revision.
     */
    public void setInt1(int int1) {
        this.int1 = int1;
    }
}
