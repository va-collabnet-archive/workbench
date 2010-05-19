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

public class SctXRelDestRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;

    // private UUID uuid; // COMPUTED RELATIONSHIPID
    long uuidMostSigBits;
    long uuidLeastSigBits;
    long conceptTwoID; // CONCEPTID2
    long roleType; // RELATIONSHIPTYPE
    long uuidTypeMsb; // RELATIONSHIPTYPE
    long uuidTypeLsb; // RELATIONSHIPTYPE
    
    public SctXRelDestRecord(long uuidMostSigBits, long uuidLeastSigBits, long conceptTwoID,
            long roleType, long uuidTypeMsb, long uuidTypeLsb) {
        super();
        this.uuidMostSigBits = uuidMostSigBits;
        this.uuidLeastSigBits = uuidLeastSigBits;
        this.conceptTwoID = conceptTwoID;
        this.roleType = roleType;
        this.uuidTypeMsb = uuidTypeMsb;
        this.uuidTypeLsb = uuidTypeLsb;
    }

    // method required for object to be sortable (comparable) in arrays
    public int compareTo(Object obj) {
        SctXRelDestRecord tmp = (SctXRelDestRecord) obj;
        int thisMore = 1;
        int thisLess = -1;
        if (uuidTypeMsb > tmp.uuidTypeMsb) {
            return thisMore;
        } else if (uuidTypeMsb < tmp.uuidTypeMsb) {
            return thisLess;
        } else {
            if (uuidTypeLsb > tmp.uuidTypeLsb) {
                return thisMore;
            } else if (uuidTypeLsb < tmp.uuidTypeLsb) {
                return thisLess;
            } else {
                return 0; // EQUAL
            }
        }
    }
}
