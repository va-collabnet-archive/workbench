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
package org.ihtsdo.mojo.maven.sct;

import java.io.Serializable;
import java.util.UUID;

import org.dwfa.util.id.Type3UuidFactory;

public class SctYRelDestRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;

    // private UUID uuid; // COMPUTED RELATIONSHIPID
    long relUuidMsb; // RELATIONSHIPID
    long relUuidLsb; // RELATIONSHIPID
    long c2SnoId; // CONCEPTID2
    long c2UuidMsb;
    long c2UuidLsb;
    long roleType; // RELATIONSHIPTYPE
    long roleTypeUuidMsb; // RELATIONSHIPTYPE
    long roleTypeUuidLsb; // RELATIONSHIPTYPE
    
    public SctYRelDestRecord(long uuidMostSigBits, long uuidLeastSigBits, long conceptTwoID,
            long roleType, long uuidTypeMsb, long uuidTypeLsb) {
        super();
        this.relUuidMsb = uuidMostSigBits;
        this.relUuidLsb = uuidLeastSigBits;
        this.c2SnoId = conceptTwoID;
        UUID tmpUUID = Type3UuidFactory.fromSNOMED(c2SnoId);
        c2UuidMsb = tmpUUID.getMostSignificantBits();
        c2UuidLsb = tmpUUID.getLeastSignificantBits();
        this.roleType = roleType;
        this.roleTypeUuidMsb = uuidTypeMsb;
        this.roleTypeUuidLsb = uuidTypeLsb;
    }

    // method required for object to be sortable (comparable) in arrays
    public int compareTo(Object obj) {
        SctYRelDestRecord tmp = (SctYRelDestRecord) obj;
        int thisMore = 1;
        int thisLess = -1;
        if (roleTypeUuidMsb > tmp.roleTypeUuidMsb) {
            return thisMore;
        } else if (roleTypeUuidMsb < tmp.roleTypeUuidMsb) {
            return thisLess;
        } else {
            if (roleTypeUuidLsb > tmp.roleTypeUuidLsb) {
                return thisMore;
            } else if (roleTypeUuidLsb < tmp.roleTypeUuidLsb) {
                return thisLess;
            } else {
                return 0; // EQUAL
            }
        }
    }
}
