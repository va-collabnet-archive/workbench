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
package org.ihtsdo.tk.dto.concept.component.refex.type_boolean;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkRefexBooleanRevision represents a version of a boolean type refex
 * member in the eConcept format and contains methods specific for interacting
 * with this version. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexBooleanRevision extends TkRevision {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The boolean value associated with this TK Refex Boolean Revision.
     */
    public boolean boolean1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Boolean Revision.
     */
    public TkRefexBooleanRevision() {
        super();
    }

    /**
     * Instantiates a new TK Refex Boolean Revision based on the
     * <code>refexBooleanVersion</code>.
     *
     * @param refexBooleanVersion the refex boolean version specifying how to
     * construct this TK Refex Boolean Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexBooleanRevision(RefexBooleanVersionBI refexBooleanVersion) throws IOException {
        super(refexBooleanVersion);
        this.boolean1 = refexBooleanVersion.getBoolean1();
    }

    /**
     * Instantiates a new TK Refex Boolean Revision based on the specified data
     * input, <code>in</code>.
     *
     * @param in the the data input specifying how to construct this TK Refex Boolean Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexBooleanRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Boolean Revision based on
     * <code>another</code> TK Refex Boolean Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Boolean Revision specifying how to construct
     * this TK Refex Boolean Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Boolean Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Boolean Revision based on the conversion map
     */
    public TkRefexBooleanRevision(TkRefexBooleanRevision another, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.boolean1 = another.boolean1;
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetBooleanVersion</tt> object, and contains the same values,
     * field by field, as this <tt>ERefsetBooleanVersion</tt>.
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

        if (TkRefexBooleanRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefexBooleanRevision another = (TkRefexBooleanRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare boolean1
            if (this.boolean1 != another.boolean1) {
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
     * TK Refex Boolean Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Boolean Revision based on the conversion map
     * @return the converted TK Refex Boolean Revision
     */
    @Override
    public TkRefexBooleanRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexBooleanRevision(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Boolean Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        boolean1 = in.readBoolean();
    }

    /**
     * Returns a string representation of this TK Refex Boolean Revision object.
     *
     * @return a string representation of this TK Refex Boolean Revision object
     * including the boolean value
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(this.boolean1);
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
        out.writeBoolean(boolean1);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the boolean associated with this TK Refex Boolean Revision.
     *
     * @return the boolean associated with this TK Refex Boolean Revision
     */
    public boolean getBoolean1() {
        return boolean1;
    }

    /**
     * Checks if the associated value is a boolean value.
     *
     * @return <code>true</code>, the associated value is a boolean value.
     */
    public boolean isBooleanValue() {
        return boolean1;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the boolean associated with this TK Refex Boolean Revision.
     *
     * @param boolean1 the new boolean associated with this TK Refex Boolean Revision
     */
    public void setBoolean1(boolean boolean1) {
        this.boolean1 = boolean1;
    }
}
