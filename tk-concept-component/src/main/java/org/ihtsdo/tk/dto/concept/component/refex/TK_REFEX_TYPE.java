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
package org.ihtsdo.tk.dto.concept.component.refex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_string.RefexNidNidStringVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.api.refex.type_string_string.RefexStringStringVersionBI;

/**
 * The Enum TK_REFEX_TYPE lists all the types of supported refexes. A
 * <code>TkRefexAbstractMember</code> will be of ones of these types. Each field
 * is associated with an identifier and the specific RefexVerisonBI
 * implementation class for the type represented.
 */
public enum TK_REFEX_TYPE {

    /**
     * A membership refex. This is standard refex where each member is
     * associated only with a referenced component. Returns the refex class:
     * <code>RefexVersionBI</code>.
     */
    MEMBER(1, RefexVersionBI.class),
    /**
     * A concept refex. Returns the refex class:
     * <code>RefexNidVersionBI</code>
     */
    CID(2, RefexNidVersionBI.class),
    /**
     * A concept-concept refex. Returns the refex class:
     * <code>RefexNidNidVersionBI</code>
     */
    CID_CID(3, RefexNidNidVersionBI.class),
    /**
     * A concept-concept-concept refex. Returns the refex class:
     * <code>RefexNidNidNidVersionBI</code>
     */
    CID_CID_CID(4, RefexNidNidNidVersionBI.class),
    /**
     * A concept-concept-string refex. Returns the refex class:
     * <code>RefexNidNidStringVersionBI</code>
     */
    CID_CID_STR(5, RefexNidNidStringVersionBI.class),
    /**
     * A string refex. Returns the refex class:
     * <code>RefexStringVersionBI</code>
     */
    STR(6, RefexStringVersionBI.class),
    /**
     * An integer refex. Returns the refex class:
     * <code>RefexIntVersionBI</code>
     */
    INT(7, RefexIntVersionBI.class),
    /**
     * A concept-integer refex. Returns the refex class:
     * <code>RefexNidIntVersionBI</code>
     */
    CID_INT(8, RefexNidIntVersionBI.class),
    /**
     * A boolean refex. Returns the refex class:
     * <code>RefexBooleanVersionBI</code>
     */
    BOOLEAN(9, RefexBooleanVersionBI.class),
    /**
     * A concept-string refex. Returns the refex class:
     * <code>RefexNidStringVersionBI</code>
     */
    CID_STR(10, RefexNidStringVersionBI.class),
    /**
     * A concept-float refex. Returns the refex class:
     * <code>RefexNidFloatVersionBI</code>
     */
    CID_FLOAT(11, RefexNidFloatVersionBI.class),
    /**
     * A concept-long refex. Returns the refex class:
     * <code>RefexNidLongVersionBI</code>
     */
    CID_LONG(12, RefexNidLongVersionBI.class),
    /**
     * A long refex. Returns the refex class:
     * <code>RefexLongVersionBI</code>
     */
    LONG(13, RefexLongVersionBI.class),
    /**
     * An array of byte array refex. Returns the refex class:
     * <code>RefexArrayOfBytearrayVersionBI</code>
     */
    ARRAY_BYTEARRAY(14, RefexArrayOfBytearrayVersionBI.class),
    /**
     * A string string refex. Returns the refex class:
     * <code>RefexStringVersionBI</code>
     */
    STR_STR(15, RefexStringStringVersionBI.class),
    /**
     * An unknown type. Has an identifier of Byte.MAX_VALUE and returns
     * <code>null</code> for the refex class.
     */
    UNKNOWN(Byte.MAX_VALUE, null);
    /**
     * The
     * <code>int</code> identifier associated with each field.
     */
    private int externalizedToken;
    /**
     * The refex class.
     */
    private Class<? extends RefexVersionBI> rxc;

    /**
     * Instantiates a new TK_REFEX_TYPE.
     *
     * @param externalizedToken the <code>int</code> identifier associated with
     * the type
     * @param refexVersion the <code>RefexVersionBI</code> class associated with
     * the type
     */
    TK_REFEX_TYPE(int externalizedToken, Class<? extends RefexVersionBI> refexVersion) {
        this.externalizedToken = externalizedToken;
        this.rxc = refexVersion;
    }

    /**
     * Gets the
     * <code>RefexVersionBI</code> class associated with the type.
     *
     * @return the <code>RefexVersionBI</code> class for the type
     */
    public Class<? extends RefexVersionBI> getRefexClass() {
        return rxc;
    }

    /**
     * Writes this TK_REFEX_TYPE to an external source.
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    public void writeType(DataOutput output) throws IOException {
        output.writeByte(externalizedToken);
    }

    /**
     * Converts a given
     * <code>RefexVersionBI</code> type class to a TK_REFEX_TYPE.
     *
     * @param c the specific <code>RefexVersionBI</code> class
     * @return the associated TK_REFEX_TYPE
     */
    public static TK_REFEX_TYPE classToType(Class<?> c) {
        if (RefexNidNidNidVersionBI.class.isAssignableFrom(c)) {
            return CID_CID_CID;
        }
        if (RefexNidNidStringVersionBI.class.isAssignableFrom(c)) {
            return CID_CID_STR;
        }
        if (RefexNidNidVersionBI.class.isAssignableFrom(c)) {
            return CID_CID;
        }
        if (RefexNidIntVersionBI.class.isAssignableFrom(c)) {
            return CID_INT;
        }
        if (RefexNidStringVersionBI.class.isAssignableFrom(c)) {
            return CID_STR;
        }
        if (RefexNidFloatVersionBI.class.isAssignableFrom(c)) {
            return CID_FLOAT;
        }
        if (RefexNidLongVersionBI.class.isAssignableFrom(c)) {
            return CID_LONG;
        }
        if (RefexBooleanVersionBI.class.isAssignableFrom(c)) {
            return BOOLEAN;
        }
        if (RefexNidVersionBI.class.isAssignableFrom(c)) {
            return CID;
        }
        if (RefexStringStringVersionBI.class.isAssignableFrom(c)) {
            return STR_STR;
        }
        if (RefexStringVersionBI.class.isAssignableFrom(c)) {
            return STR;
        }
        if (RefexIntVersionBI.class.isAssignableFrom(c)) {
            return INT;
        }
        if (RefexLongVersionBI.class.isAssignableFrom(c)) {
            return LONG;
        }
        if (RefexArrayOfBytearrayVersionBI.class.isAssignableFrom(c)) {
            return ARRAY_BYTEARRAY;
        }
        if (RefexVersionBI.class.isAssignableFrom(c)) {
            return MEMBER;
        }
        return UNKNOWN;
    }

    /**
     * Converts a given
     * <code>input</code> to a TK_REFEX_TYPE.
     *
     * @param input the data input specifying the refex type by an externalized
     * token
     * @return the associated TK_REFEX_TYPE
     * @throws IOException signals that an I/O exception has occurred
     */
    public static TK_REFEX_TYPE readType(DataInput input) throws IOException {
        int type = input.readByte();
        switch (type) {
            case 1:
                return MEMBER;
            case 2:
                return CID;
            case 3:
                return CID_CID;
            case 4:
                return CID_CID_CID;
            case 5:
                return CID_CID_STR;
            case 6:
                return STR;
            case 7:
                return INT;
            case 8:
                return CID_INT;
            case 9:
                return BOOLEAN;
            case 10:
                return CID_STR;
            case 11:
                return CID_FLOAT;
            case 12:
                return CID_LONG;
            case 13:
                return LONG;
            case 14:
                return ARRAY_BYTEARRAY;
            case 15:
                return STR_STR;
        }
        throw new UnsupportedOperationException("Can't handle type: " + type);
    }
}
