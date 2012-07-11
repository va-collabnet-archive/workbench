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

public enum TK_REFEX_TYPE {

    /**
     * CID = Component IDentifier
     * 
     * @author kec
     * 
     */
    MEMBER(1, RefexVersionBI.class),
    CID(2, RefexNidVersionBI.class),
    CID_CID(3, RefexNidNidVersionBI.class),
    CID_CID_CID(4, RefexNidNidNidVersionBI.class),
    CID_CID_STR(5, RefexNidNidStringVersionBI.class),
    STR(6, RefexStringVersionBI.class),
    INT(7, RefexIntVersionBI.class),
    CID_INT(8, RefexNidIntVersionBI.class),
    BOOLEAN(9, RefexBooleanVersionBI.class),
    CID_STR(10, RefexNidStringVersionBI.class),
    CID_FLOAT(11, RefexNidFloatVersionBI.class),
    CID_LONG(12, RefexNidLongVersionBI.class),
    LONG(13, RefexLongVersionBI.class),
    ARRAY_BYTEARRAY(14, RefexArrayOfBytearrayVersionBI.class),
    UNKNOWN(Byte.MAX_VALUE, null);
    private int externalizedToken;
    private Class<? extends RefexVersionBI> rxc;

    TK_REFEX_TYPE(int externalizedToken, Class<? extends RefexVersionBI> refexVersion) {
        this.externalizedToken = externalizedToken;
        this.rxc = refexVersion;
    }

    public Class<? extends RefexVersionBI> getRefexClass() {
        return rxc;
    }

    public void writeType(DataOutput output) throws IOException {
        output.writeByte(externalizedToken);
    }

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
