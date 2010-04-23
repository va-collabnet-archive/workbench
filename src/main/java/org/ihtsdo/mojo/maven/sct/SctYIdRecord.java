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

import java.util.UUID;

public class SctYIdRecord {

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
    int yRevision;
   
    // PATH UUID
    int yPath;

    public SctYIdRecord(UUID uuidPrimaryId, int sourceSystemIdx, String idFromSourceSystem,
            int status, int revDate, int pathIdx) {
        this.primaryUuidMsb = uuidPrimaryId.getMostSignificantBits(); // CONCEPTID/PRIMARYID
        this.primaryUuidLsb = uuidPrimaryId.getLeastSignificantBits(); // CONCEPTID/PRIMARYID
        this.srcSystemIdx = sourceSystemIdx;
        this.denotation = idFromSourceSystem;
        this.status = status;
        this.yRevision = revDate;
        this.yPath = pathIdx;
    }
}
