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
    long denotationLong;
    
    // STATUS UUID
    // ArchitectonicAuxiliary.Concept.CURRENT.getUids().get(0)
    int status;
    
    // EFFECTIVE DATE
    long revTime;
   
    // PATH
    int pathIdx;
    
    // USER
    int userIdx;    

    // :NYI:HACK:
    // UUID allocation called only from ARF input
    // so far, only String identifiers are supported for ARF ids.
    public Sct1_IdRecord(UUID uuidPrimaryId, int sourceSystemIdx, String idFromSourceSystem,
            int status, long revDateTime, int pathIdx, int userIdx) {
        this.primaryUuidMsb = uuidPrimaryId.getMostSignificantBits(); // CONCEPTID/PRIMARYID
        this.primaryUuidLsb = uuidPrimaryId.getLeastSignificantBits(); // CONCEPTID/PRIMARYID
        this.srcSystemIdx = sourceSystemIdx;
        this.denotation = idFromSourceSystem;
        this.denotationLong = Long.MAX_VALUE;
        this.status = status;
        this.revTime = revDateTime;
        this.pathIdx = pathIdx;
        this.userIdx = userIdx;
    }

    // :NYI:HACK:
    // long long UUID called only from SCT1 input
    // in this case, the denotation is always long for the SNOMED_ID 
    public Sct1_IdRecord(long uuidPrimaryMsb, long uuidPrimaryLsb, int sourceSystemIdx, long idFromSourceSystem,
            int status, long revDateTime, int pathIdx, int uIdx) {
        this.primaryUuidMsb = uuidPrimaryMsb; // MSB CONCEPTID/PRIMARYID
        this.primaryUuidLsb = uuidPrimaryLsb; // LSB CONCEPTID/PRIMARYID
        this.srcSystemIdx = sourceSystemIdx;
        this.denotation = null;
        this.denotationLong = idFromSourceSystem;
        this.status = status;
        this.revTime = revDateTime;
        this.pathIdx = pathIdx;
        this.userIdx = uIdx;
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
                if (this.pathIdx > o.pathIdx) {
                    return thisMore;
                } else if (this.pathIdx < o.pathIdx) {
                    return thisLess;
                } else {
                    if (this.userIdx > o.userIdx) {
                        return thisMore;
                    } else if (this.userIdx < o.userIdx) {
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
    
}
