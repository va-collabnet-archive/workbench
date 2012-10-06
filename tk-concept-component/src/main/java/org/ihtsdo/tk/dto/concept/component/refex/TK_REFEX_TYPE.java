/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.dto.concept.component.refex;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_string.RefexNidNidStringVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;

// TODO: Auto-generated Javadoc
/**
 * The Enum TK_REFEX_TYPE.
 */
public enum TK_REFEX_TYPE {

    /** CID = Component IDentifier. @author kec */
    MEMBER(1, RefexVersionBI.class),
    
    /** The cid. */
    CID(2, RefexNidVersionBI.class),
    
    /** The cid cid. */
    CID_CID(3, RefexNidNidVersionBI.class),
    
    /** The cid cid cid. */
    CID_CID_CID(4, RefexNidNidNidVersionBI.class),
    
    /** The cid cid str. */
    CID_CID_STR(5, RefexNidNidStringVersionBI.class),
    
    /** The str. */
    STR(6, RefexStringVersionBI.class),
    
    /** The int. */
    INT(7, RefexIntVersionBI.class),
    
    /** The cid int. */
    CID_INT(8, RefexNidIntVersionBI.class),
    
    /** The boolean. */
    BOOLEAN(9, RefexBooleanVersionBI.class),
    
    /** The cid str. */
    CID_STR(10, RefexNidStringVersionBI.class),
    
    /** The cid float. */
    CID_FLOAT(11, RefexNidFloatVersionBI.class),
    
    /** The cid long. */
    CID_LONG(12, RefexNidLongVersionBI.class),
    
    /** The long. */
    LONG(13, RefexLongVersionBI.class),
    
    /** The array bytearray. */
    ARRAY_BYTEARRAY(14, RefexArrayOfBytearrayVersionBI.class),
    
    /** The unknown. */
    UNKNOWN(Byte.MAX_VALUE, null);
    
    /** The externalized token. */
    private int externalizedToken;
    
    /** The rxc. */
    private Class<? extends RefexVersionBI> rxc;

    /**
     * Instantiates a new tk refex type.
     *
     * @param externalizedToken the externalized token
     * @param refexVersion the refex version
     */
    TK_REFEX_TYPE(int externalizedToken, Class<? extends RefexVersionBI> refexVersion) {
        this.externalizedToken = externalizedToken;
        this.rxc = refexVersion;
    }

    /**
     * Gets the refex class.
     *
     * @return the refex class
     */
    public Class<? extends RefexVersionBI> getRefexClass() {
        return rxc;
    }

    /**
     * Write type.
     *
     * @param output the output
     * @throws IOException signals that an I/O exception has occurred
     */
    public void writeType(DataOutput output) throws IOException {
        output.writeByte(externalizedToken);
    }

    /**
     * Class to type.
     *
     * @param c the c
     * @return the tk refex type
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
     * Read type.
     *
     * @param input the input
     * @return the tk refex type
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
        }
        throw new UnsupportedOperationException("Can't handle type: " + type);
    }
}
