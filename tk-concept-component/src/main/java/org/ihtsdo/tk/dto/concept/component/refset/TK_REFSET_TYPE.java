package org.ihtsdo.tk.dto.concept.component.refset;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public enum TK_REFSET_TYPE {
    /**
     * CID = Component IDentifier
     * 
     * @author kec
     * 
     */
        MEMBER(1), 
        CID(2), 
        CID_CID(3), 
        CID_CID_CID(4), 
        CID_CID_STR(5), 
        STR(6), 
        INT(7), 
        CID_INT(8), 
        BOOLEAN(9), 
        CID_STR(10), 
        CID_FLOAT(11),
        CID_LONG(12), 
        LONG(13); 

        private int externalizedToken;
        
        TK_REFSET_TYPE(int externalizedToken) {
            this.externalizedToken = externalizedToken;
        }
        
        public void writeType(DataOutput output) throws IOException {
            output.writeByte(externalizedToken);
        }

        public static TK_REFSET_TYPE readType(DataInput input) throws IOException {
            switch (input.readByte()) {
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
            throw new UnsupportedOperationException();
        }
}
