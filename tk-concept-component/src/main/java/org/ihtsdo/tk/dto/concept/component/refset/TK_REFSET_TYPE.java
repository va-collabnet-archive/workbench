package org.ihtsdo.tk.dto.concept.component.refset;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_cnid.RefexCnidCnidCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_str.RefexCnidCnidStrVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_float.RefexCnidFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_int.RefexCnidIntVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_long.RefexCnidLongVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_str.RefexCnidStrVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;

public enum TK_REFSET_TYPE {

    /**
     * CID = Component IDentifier
     * 
     * @author kec
     * 
     */
    MEMBER(1, RefexVersionBI.class),
    CID(2, RefexCnidVersionBI.class),
    CID_CID(3, RefexCnidCnidVersionBI.class),
    CID_CID_CID(4, RefexCnidCnidCnidVersionBI.class),
    CID_CID_STR(5, RefexCnidCnidStrVersionBI.class),
    STR(6, RefexStrVersionBI.class),
    INT(7, RefexIntVersionBI.class),
    CID_INT(8, RefexCnidIntVersionBI.class),
    BOOLEAN(9, RefexBooleanVersionBI.class),
    CID_STR(10, RefexCnidStrVersionBI.class),
    CID_FLOAT(11, RefexCnidFloatVersionBI.class),
    CID_LONG(12, RefexCnidLongVersionBI.class),
    LONG(13, RefexLongVersionBI.class),
    UNKNOWN(Byte.MAX_VALUE, null);
    private int externalizedToken;
    private Class<? extends RefexVersionBI> rxc;

    TK_REFSET_TYPE(int externalizedToken, Class<? extends RefexVersionBI> rxc) {
        this.externalizedToken = externalizedToken;
        this.rxc = rxc;
    }

    public Class<? extends RefexVersionBI> getRefexClass() {
        return rxc;
    }

    public void writeType(DataOutput output) throws IOException {
        output.writeByte(externalizedToken);
    }

    public static TK_REFSET_TYPE classToType(Class<?> c) {
        if (RefexCnidCnidCnidVersionBI.class.isAssignableFrom(c)) {
            return CID_CID_CID;
        }
        if (RefexCnidCnidStrVersionBI.class.isAssignableFrom(c)) {
            return CID_CID_STR;
        }
        if (RefexCnidCnidVersionBI.class.isAssignableFrom(c)) {
            return CID_CID;
        }
        if (RefexCnidIntVersionBI.class.isAssignableFrom(c)) {
            return CID_INT;
        }
        if (RefexCnidStrVersionBI.class.isAssignableFrom(c)) {
            return CID_STR;
        }
        if (RefexCnidFloatVersionBI.class.isAssignableFrom(c)) {
            return CID_FLOAT;
        }
        if (RefexCnidLongVersionBI.class.isAssignableFrom(c)) {
            return CID_LONG;
        }
        if (RefexBooleanVersionBI.class.isAssignableFrom(c)) {
            return BOOLEAN;
        }
        if (RefexCnidVersionBI.class.isAssignableFrom(c)) {
            return CID;
        }
        if (RefexStrVersionBI.class.isAssignableFrom(c)) {
            return STR;
        }
        if (RefexIntVersionBI.class.isAssignableFrom(c)) {
            return INT;
        }
        if (RefexLongVersionBI.class.isAssignableFrom(c)) {
            return LONG;
        }
        if (RefexVersionBI.class.isAssignableFrom(c)) {
            return MEMBER;
        }
        return UNKNOWN;
    }

    public static TK_REFSET_TYPE readType(DataInput input) throws IOException {
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
        }
        throw new UnsupportedOperationException("Can't handle type: " + type);
    }
}
