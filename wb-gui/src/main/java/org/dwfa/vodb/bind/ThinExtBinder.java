/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.vodb.bind;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidInt;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidFloat;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.vodb.I_MapIds;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinExtBinder extends TupleBinding {
    private static int BOOLEAN_ID = 1;
    private static int CONCEPT_ID = 2;
    private static int INTEGER_ID = 3;
    private static int MEASUREMENT_ID = 4;
    private static int STRING_ID = 7;
    private static int CON_INT_ID = 8;
    private static int CONCEPT_CONCEPT_ID = 13;
    private static int CONCEPT_CONCEPT_CONCEPT_ID = 14;
    private static int CONCEPT_CONCEPT_STRING_ID = 15;
    private static int CONCEPT_STRING_ID = 16;

    public static enum EXT_TYPE {
        BOOLEAN(BOOLEAN_ID, "boolean", I_ExtendByRefPartBoolean.class),
        CONCEPT(CONCEPT_ID, "concept", I_ExtendByRefPartCid.class),
        CON_INT(CON_INT_ID, "con int", I_ExtendByRefPartCidInt.class),
        STRING(STRING_ID, "string", I_ExtendByRefPartStr.class),
        INTEGER(INTEGER_ID, "integer", I_ExtendByRefPartInt.class),
        MEASUREMENT(MEASUREMENT_ID, "measurement", I_ExtendByRefPartCidFloat.class),
        CONCEPT_CONCEPT(CONCEPT_CONCEPT_ID, "concept-concept", I_ExtendByRefPartCidCid.class),
        CONCEPT_CONCEPT_CONCEPT(
                CONCEPT_CONCEPT_CONCEPT_ID,
                "concept-concept-concept",
                I_ExtendRefPartCidCidCid.class),
        CONCEPT_CONCEPT_STRING(
                CONCEPT_CONCEPT_STRING_ID,
                "concept-concept-string",
                I_ExtendByRefPartCidCidString.class),
        CONCEPT_STRING(CONCEPT_STRING_ID, "concept-string", I_ExtendByRefPartCidString.class), ;

        private int enumId;

        private String interfaceName;

        private Class<? extends I_ExtendByRefPart> partClass;

        private EXT_TYPE(int id, String interfaceName, Class<? extends I_ExtendByRefPart> partClass) {
            this.enumId = id;
            this.interfaceName = interfaceName;
            this.partClass = partClass;
        }

        public int getEnumId() {
            return enumId;
        }

        public static EXT_TYPE fromEnumId(int id) {
            switch (id) {
            case 1:
                return BOOLEAN;
            case 2:
                return CONCEPT;
            case 3:
                return INTEGER;
            case 4:
                return MEASUREMENT;
            case 7:
                return STRING;
            case 8:
                return CON_INT;
            case 13:
                return CONCEPT_CONCEPT;
            case 14:
                return CONCEPT_CONCEPT_CONCEPT;
            case 15:
                return CONCEPT_CONCEPT_STRING;
            case 16:
                return CONCEPT_STRING;

            default:
                throw new RuntimeException("Can't convert to EXT_TYPE: " + id);
            }
        }

        public String getInterfaceName() {
            return interfaceName;
        }

        public Class<? extends I_ExtendByRefPart> getPartClass() {
            return partClass;
        }

        public int getNid() {
            return getExtensionTypeNid(this);
        }
    }

    private boolean fixedOnly;

    public ThinExtBinder() {
        super();
        this.fixedOnly = false;
    }

    public ThinExtBinder(boolean fixedOnly) {
        super();
        this.fixedOnly = fixedOnly;
    }

    public I_ExtendByRef entryToObject(TupleInput ti) {
    	throw new UnsupportedOperationException();
    }

    public static int getExtensionTypeNid(EXT_TYPE type) {
    	throw new UnsupportedOperationException();
        }

    public static int getExtensionTypeNid(EXT_TYPE type, I_MapIds map, I_Path idPath, int version) {
    	throw new UnsupportedOperationException();
        }

    /*
     * private int refsetId; private int memberId; private int componentId;
     * private int typeId;
     */
    public void objectToEntry(Object obj, TupleOutput to) {
    	throw new UnsupportedOperationException();
        }

    public static EXT_TYPE getExtensionType(Class<? extends I_ExtendByRefPart> partType) {
        for (EXT_TYPE extType : EXT_TYPE.values()) {
            if (extType.getPartClass().equals(partType)) {
                return extType;
            }
        }
        throw new UnsupportedOperationException("Can't convert to type: " + partType);
    }

    public static EXT_TYPE getExtensionType(I_ExtendByRef versioned) {
    	throw new UnsupportedOperationException();
        }

}
