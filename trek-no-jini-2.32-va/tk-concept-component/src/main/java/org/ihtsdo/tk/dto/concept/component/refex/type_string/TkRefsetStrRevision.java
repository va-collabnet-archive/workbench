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
package org.ihtsdo.tk.dto.concept.component.refex.type_string;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkRefsetStrRevision represents a version of a string type refex
 * member in the eConcept format and contains methods specific for interacting
 * with this version. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefsetStrRevision extends TkRevision {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The string value associated with this TK Refset String Revision.
     */
    public String string1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refset String Revision.
     */
    public TkRefsetStrRevision() {
        super();
    }

    /**
     * Instantiates a new TK Refset String Revision based on the
     * <code>refexStringVersion</code>
     *
     * @param refexStringVersion the refex string version specifying how to
     * construct this TK Refset String Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefsetStrRevision(RefexStringVersionBI refexStringVersion) throws IOException {
        super(refexStringVersion);
        this.string1 = refexStringVersion.getString1();
    }

    /**
     * Instantiates a new TK Refset String Revision based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refset String Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefsetStrRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refset String Revision based on
     * <code>another</code> TK Refset String Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Refset String Revision specifying how to construct
     * this TK Refset String Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refset String Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refset String Revision based on the conversion map
     */
    public TkRefsetStrRevision(TkRefsetStrRevision another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.string1 = another.string1;
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetStrVersion</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetStrVersion</tt>.
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

        if (TkRefsetStrRevision.class.isAssignableFrom(obj.getClass())) {
            TkRefsetStrRevision another = (TkRefsetStrRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
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
     * TK Refset String Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refset String Revision based on the conversion map
     * @return the converted TK Refset String Revision
     */
    @Override
    public TkRefsetStrRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefsetStrRevision(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Refset String Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        string1 = UtfHelper.readUtfV7(in, dataVersion);
    }

    /**
     * Returns a string representation of this TK Refset String Revision object.
     *
     * @return a string representation of this TK Refset String Revision object
     * including the string value
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" str:");
        buff.append("'").append(this.string1).append("' ");
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
        UtfHelper.writeUtf(out, string1);
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the string associated with this TK Refset String Revision.
     *
     * @return the string associated with this TK Refset String Revision
     */
    public String getString1() {
        return string1;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the string associated with this TK Refset String Revision.
     *
     * @param string1 associated with this TK Refset String Revision
     */
    public void setString1(String string1) {
        this.string1 = string1;
    }
}
