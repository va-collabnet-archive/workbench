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
import java.util.ArrayList;
import java.util.UUID;

import org.dwfa.util.id.Type3UuidFactory;

class SctYRelRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAB_CHARACTER = "\t";
        
    // :yyy: private UUID uuid; // COMPUTED RELATIONSHIPID
    long relSnoId; // SNOMED RELATIONSHIPID, if applicable
    long relUuidMsb;
    long relUuidLsb;
    // List<EIdentifier> additionalIds;
    ArrayList<SctYIdRecord> addedIds;
    int status; // status is computed for relationships
    long c1SnoId; // CONCEPTID1
    long c1UuidMsb;
    long c1UuidLsb;
    long roleTypeSnoId; // RELATIONSHIPTYPE .. SNOMED ID
    int roleTypeIdx; // RELATIONSHIPTYPE .. index
    long c2SnoId; // CONCEPTID2
    long c2UuidMsb;
    long c2UuidLsb;
    int characteristic; // CHARACTERISTICTYPE
    int refinability; // REFINABILITY
    int group; // RELATIONSHIPGROUP
    boolean exceptionFlag; // to handle Concept ID change exception
    int yPath;
    int yRevision;

    public SctYRelRecord(long relID, int st, long cOneID, long roleTypeSnoId, int roleTypeIdx, long cTwoID,
            int characterType, int r, int grp) {
        this.relSnoId = relID; // RELATIONSHIPID
        UUID tmpUUID = Type3UuidFactory.fromSNOMED(relSnoId);
        this.relUuidMsb = tmpUUID.getMostSignificantBits();
        this.relUuidLsb = tmpUUID.getLeastSignificantBits();

        // additionalIds = null;
        addedIds = null;
        this.status = st; // status is computed for relationships
        this.c1SnoId = cOneID; // CONCEPTID1
        
        tmpUUID = Type3UuidFactory.fromSNOMED(c1SnoId);
        this.c1UuidMsb = tmpUUID.getMostSignificantBits();
        this.c1UuidLsb = tmpUUID.getLeastSignificantBits();
        
        this.roleTypeSnoId = roleTypeSnoId; // RELATIONSHIPTYPE (SNOMED ID) 
        this.roleTypeIdx = roleTypeIdx; // RELATIONSHIPTYPE  <-- INDEX (NOT SNOMED ID) 
        
        this.c2SnoId = cTwoID; // CONCEPTID2
        tmpUUID = Type3UuidFactory.fromSNOMED(c2SnoId);
        this.c2UuidMsb = tmpUUID.getMostSignificantBits();
        this.c2UuidLsb = tmpUUID.getLeastSignificantBits();

        this.characteristic = characterType; // CHARACTERISTICTYPE
        this.refinability = r; // REFINABILITY
        this.group = grp; // RELATIONSHIPGROUP
        this.exceptionFlag = false;
    }

    public SctYRelRecord(UUID uuidRelId, int status, UUID uuidC1, int roleTypeIdx, UUID uuidC2,
            int characteristic, int refinability, int group, int revDate, int pathIdx) {
        
        this.relSnoId = Long.MAX_VALUE; // SNOMED RELATIONSHIPID, if applicable
        this.relUuidMsb = uuidRelId.getMostSignificantBits();
        this.relUuidLsb = uuidRelId.getLeastSignificantBits();
        // additionalIds = null;
        addedIds = null;
        this.status = status; // status is computed for relationships
        this.c1SnoId = Long.MAX_VALUE; // CONCEPTID1
        this.c1UuidMsb = uuidC1.getMostSignificantBits();
        this.c1UuidLsb = uuidC1.getLeastSignificantBits();
        this.roleTypeSnoId = Long.MAX_VALUE; // max not assigned or unknown
        this.roleTypeIdx = roleTypeIdx; // RELATIONSHIPTYPE
        this.c2SnoId = Long.MAX_VALUE; // CONCEPTID2
        this.c2UuidMsb = uuidC2.getMostSignificantBits();
        this.c2UuidLsb = uuidC2.getLeastSignificantBits();
        this.characteristic = characteristic; // CHARACTERISTICTYPE
        this.refinability = refinability; // REFINABILITY
        this.group = group; // RELATIONSHIPGROUP
        this.exceptionFlag = false; // to handle Concept ID change exception
        this.yPath = pathIdx;
        this.yRevision = revDate;
    }

    // method required for object to be sortable (comparable) in arrays
    public int compareTo(Object obj) {
        SctYRelRecord tmp = (SctYRelRecord) obj;
        // :yyy: return this.uuid.compareTo(tmp.uuid);
        int thisMore = 1;
        int thisLess = -1;
        if (relUuidMsb > tmp.relUuidMsb) {
            return thisMore;
        } else if (relUuidMsb < tmp.relUuidMsb) {
            return thisLess;
        } else {
            if (relUuidLsb > tmp.relUuidLsb) {
                return thisMore;
            } else if (relUuidLsb < tmp.relUuidLsb) {
                return thisLess;
            } else {
                if (this.yPath > tmp.yPath) {
                    return thisMore;
                } else if (this.yPath < tmp.yPath) {
                    return thisLess;
                } else {
                    if (this.yRevision > tmp.yRevision) {
                        return thisMore;
                    } else if (this.yRevision < tmp.yRevision) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            }
        }
    }

    // Create string to show some input fields for exception reporting
    public String toString() {
        UUID uuid = new UUID(relUuidMsb, relUuidLsb); // :yyy:
        return uuid + TAB_CHARACTER + relSnoId + TAB_CHARACTER + status + TAB_CHARACTER
                + c1SnoId + TAB_CHARACTER + roleTypeIdx + TAB_CHARACTER + c2SnoId + TAB_CHARACTER + group;
    }
}
