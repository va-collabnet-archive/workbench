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

public class Sct1_IdRecord implements Comparable<Sct1_IdRecord>, Serializable {
    private static final long serialVersionUID = 1L;

    long primaryUuidMsb; // CONCEPTID/PRIMARYID
    long primaryUuidLsb; // CONCEPTID/PRIMARYID
    
    // SOURCE UUID
    // ArchitectonicAuxiliary.Concept.ICD_9.getUids().get(0)
    int srcSystemIdx;
    
    // SOURCE ID -- DENOTATION
    String denotation;
    
    // STATUS UUID
    // ArchitectonicAuxiliary.Concept.CURRENT.getUids().get(0)
    int status;
    
    // EFFECTIVE DATE
    long revTime;
   
    // PATH UUID
    int path;

    public Sct1_IdRecord(UUID uuidPrimaryId, int sourceSystemIdx, String idFromSourceSystem,
            int status, long revDateTime, int pathIdx) {
        this.primaryUuidMsb = uuidPrimaryId.getMostSignificantBits(); // CONCEPTID/PRIMARYID
        this.primaryUuidLsb = uuidPrimaryId.getLeastSignificantBits(); // CONCEPTID/PRIMARYID
        this.srcSystemIdx = sourceSystemIdx;
        this.denotation = idFromSourceSystem;
        this.status = status;
        this.revTime = revDateTime;
        this.path = pathIdx;
    }

    @Override
    public int compareTo(Sct1_IdRecord o) {
        int thisMore = 1;
        int thisLess = -1;
        if (primaryUuidMsb > o.primaryUuidMsb) {
            return thisMore;
        } else if (primaryUuidMsb < o.primaryUuidMsb) {
            return thisLess;
        } else {
            if (primaryUuidLsb > o.primaryUuidLsb) {
                return thisMore;
            } else if (primaryUuidLsb < o.primaryUuidLsb) {
                return thisLess;
            } else {
                if (this.path > o.path) {
                    return thisMore;
                } else if (this.path < o.path) {
                    return thisLess;
                } else {
                    if (this.revTime > o.revTime) {
                        return thisMore;
                    } else if (this.revTime < o.revTime) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            }
        }
    }
    
    
}
