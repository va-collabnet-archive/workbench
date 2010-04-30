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
    long c2UuidMsb;
    long c2UuidLsb;
    int roleTypeIdx; // RELATIONSHIPTYPE  IDX !!!

    public SctYRelDestRecord(long uuidRelMsb, long uuidRelLsb, long uuidC2Msb, long uuidC2Lsb,
            int roleTypeIdx) {
        super();
        this.relUuidMsb = uuidRelMsb;
        this.relUuidLsb = uuidRelLsb;
        c2UuidMsb = uuidC2Msb;
        c2UuidLsb = uuidC2Lsb;
        this.roleTypeIdx = roleTypeIdx;
    }

    // method required for object to be sortable (comparable) in arrays
    public int compareTo(Object obj) {
        SctYRelDestRecord tmp = (SctYRelDestRecord) obj;
        int thisMore = 1;
        int thisLess = -1;
        if (roleTypeIdx > tmp.roleTypeIdx) {
            return thisMore;
        } else if (roleTypeIdx < tmp.roleTypeIdx) {
            return thisLess;
        } else {
            return 0; // EQUAL
        }
    }

}
