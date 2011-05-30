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

class Sct1_ConRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final String TAB_CHARACTER = "\t";

    // RECORD FIELDS
    long conSnoId; //  CONCEPTID
    long conUuidMsb; // CONCEPTID
    long conUuidLsb; // CONCEPTID
    int status; // CONCEPTSTATUS
    // ArrayList<EIdentifier> additionalIds;
    ArrayList<Sct1_IdRecord> addedIds;
    String ctv3id; // CTV3ID
    String snomedrtid; // SNOMEDID (SNOMED RT ID)
    int isprimitive; // ISPRIMITIVE
    int path;
    long revTime;

    public Sct1_ConRecord(long csId, int s, String ctv, String rt, int p) {
        conSnoId = csId;
        UUID tmpUUID = Type3UuidFactory.fromSNOMED(conSnoId);
        conUuidMsb = tmpUUID.getMostSignificantBits();
        conUuidLsb = tmpUUID.getLeastSignificantBits();
        status = s;
        // additionalIds = null;
        addedIds = null;
        ctv3id = ctv;
        snomedrtid = rt;
        isprimitive = p;
    }
    
    public Sct1_ConRecord(UUID cUuid, int s, int p,
            long revDate, int pathIdx) {
        conSnoId = Long.MAX_VALUE;
        conUuidMsb = cUuid.getMostSignificantBits();
        conUuidLsb = cUuid.getLeastSignificantBits();
        status = s;
        // additionalIds = null;
        addedIds = null;
        ctv3id = null;
        snomedrtid = null;
        isprimitive = p;
        revTime = revDate;
        path = pathIdx;
    }

    // method required for object to be sortable (comparable) in arrays
    @Override
    public int compareTo(Object obj) {
        Sct1_ConRecord tmp = (Sct1_ConRecord) obj;
        int thisMore = 1;
        int thisLess = -1;
        if (this.conUuidMsb < tmp.conUuidMsb) {
            return thisLess; // instance less than received
        } else if (this.conUuidMsb > tmp.conUuidMsb) {
            return thisMore; // instance greater than received
        } else {
            if (conUuidLsb < tmp.conUuidLsb) {
                return thisLess;
            } else if (conUuidLsb > tmp.conUuidLsb) {
                return thisMore;
            } else {
                if (this.path < tmp.path) {
                    return thisLess; // instance less than received
                } else if (this.path > tmp.path) {
                    return thisMore; // instance greater than received
                } else {
                    if (this.revTime < tmp.revTime) {
                        return thisLess; // instance less than received
                    } else if (this.revTime > tmp.revTime) {
                        return thisMore; // instance greater than received
                    } else {
                        return 0; // instance == received
                    }
                }
            }
        }
    }

    // Create string to show some input fields for exception reporting
    @Override
    public String toString() {
        UUID uuid = new UUID(conUuidMsb, conUuidLsb); // :yyy:
        return uuid + TAB_CHARACTER + status + TAB_CHARACTER + isprimitive;
    }

}
