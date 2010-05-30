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

class SctXRelRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAB_CHARACTER = "\t";
    
    
    // :yyy: private UUID uuid; // COMPUTED RELATIONSHIPID
    long uuidMostSigBits;
    long uuidLeastSigBits;
    long id; // SNOMED RELATIONSHIPID, if applicable
    int status; // status is computed for relationships
    long conceptOneID; // CONCEPTID1
    long roleType; // RELATIONSHIPTYPE
    long conceptTwoID; // CONCEPTID2
    int characteristic; // CHARACTERISTICTYPE
    int refinability; // REFINABILITY
    int group; // RELATIONSHIPGROUP
    boolean exceptionFlag; // to handle Concept ID change exception
    int xPath;
    int xRevision;

    public SctXRelRecord(long relID, int st, long cOneID, long relType, long cTwoID,
            int characterType, int r, int grp) {
        id = relID; // RELATIONSHIPID
        status = st; // status is computed for relationships
        conceptOneID = cOneID; // CONCEPTID1
        roleType = relType; // RELATIONSHIPTYPE
        conceptTwoID = cTwoID; // CONCEPTID2
        characteristic = characterType; // CHARACTERISTICTYPE
        refinability = r; // REFINABILITY
        group = grp; // RELATIONSHIPGROUP
        exceptionFlag = false;
    }

    // method required for object to be sortable (comparable) in arrays
    public int compareTo(Object obj) {
        SctXRelRecord tmp = (SctXRelRecord) obj;
        // :yyy: return this.uuid.compareTo(tmp.uuid);
        int thisMore = 1;
        int thisLess = -1;
        if (uuidMostSigBits > tmp.uuidMostSigBits) {
            return thisMore;
        } else if (uuidMostSigBits < tmp.uuidMostSigBits) {
            return thisLess;
        } else {
            if (uuidLeastSigBits > tmp.uuidLeastSigBits) {
                return thisMore;
            } else if (uuidLeastSigBits < tmp.uuidLeastSigBits) {
                return thisLess;
            } else {
                if (this.xPath > tmp.xPath) {
                    return thisMore;
                } else if (this.xPath < tmp.xPath) {
                    return thisLess;
                } else {
                    if (this.xRevision > tmp.xRevision) {
                        return thisMore;
                    } else if (this.xRevision < tmp.xRevision) {
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
        UUID uuid = new UUID(uuidMostSigBits, uuidLeastSigBits); // :yyy:
        return uuid + TAB_CHARACTER + id + TAB_CHARACTER + status + TAB_CHARACTER
                + conceptOneID + TAB_CHARACTER + roleType + TAB_CHARACTER + conceptTwoID + TAB_CHARACTER + group;
    }

}
