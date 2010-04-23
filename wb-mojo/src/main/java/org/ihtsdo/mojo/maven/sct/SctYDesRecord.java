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
import java.util.List;
import java.util.UUID;

import org.dwfa.util.id.Type3UuidFactory;
import org.ihtsdo.etypes.EIdentifier;

class SctYDesRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAB_CHARACTER = "\t";

    long desSnoId; // DESCRIPTIONID
    long desUuidMsb;
    long desUuidLsb;    
    int status; // DESCRIPTIONSTATUS
    List<EIdentifier> additionalIds;
    long conSnoId; // CONCEPTID
    long conUuidMsb; // CONCEPTID
    long conUuidLsb; // CONCEPTID    
    String termText; // TERM
    int capStatus; // INITIALCAPITALSTATUS -- capitalization
    int descriptionType; // DESCRIPTIONTYPE
    String languageCode; // LANGUAGECODE
    int yPath;
    int yRevision;

    public SctYDesRecord(long dId, int s, long cId, String text, int cStat, int typeInt, String lang) {
        desSnoId = dId;
        UUID tmpUUID = Type3UuidFactory.fromSNOMED(desSnoId);
        desUuidMsb = tmpUUID.getMostSignificantBits();
        desUuidLsb = tmpUUID.getLeastSignificantBits();
        
        status = s;
        additionalIds = null;
        
        conSnoId = cId;
        tmpUUID = Type3UuidFactory.fromSNOMED(conSnoId);
        conUuidMsb = tmpUUID.getMostSignificantBits();
        conUuidLsb = tmpUUID.getLeastSignificantBits();
        
        termText = new String(text);
        capStatus = cStat;
        descriptionType = typeInt;
        languageCode = new String(lang);
    }

    public SctYDesRecord(UUID desUuid, int status, UUID uuidCon, String termStr,
            int capitalization, int desTypeIdx, String langCodeStr, int revDate, int pathIdx) {
         this.desSnoId = Long.MAX_VALUE; // DESCRIPTIONID
         this.desUuidMsb = desUuid.getMostSignificantBits();
         this.desUuidLsb = desUuid.getLeastSignificantBits();    
         this.status = status; // DESCRIPTIONSTATUS
         this.additionalIds = null;
         this.conSnoId = Long.MAX_VALUE; // CONCEPTID
         this.conUuidMsb = uuidCon.getMostSignificantBits(); // CONCEPTID
         this.conUuidLsb = uuidCon.getLeastSignificantBits(); // CONCEPTID    
         this.termText = termStr; // TERM
         this.capStatus = capitalization; // INITIALCAPITALSTATUS -- capitalization
         this.descriptionType = desTypeIdx; // DESCRIPTIONTYPE
         this.languageCode = langCodeStr; // LANGUAGECODE
         this.yPath = pathIdx;
         this.yRevision = revDate;
    }

    // method required for object to be sortable (comparable) in arrays
    public int compareTo(Object obj) {
        SctYDesRecord tmp = (SctYDesRecord) obj;
        if (this.desSnoId < tmp.desSnoId) {
            return -1; // instance less than received
        } else if (this.desSnoId > tmp.desSnoId) {
            return 1; // instance greater than received
        } else {
            if (this.yPath < tmp.yPath) {
                return -1; // instance less than received
            } else if (this.yPath > tmp.yPath) {
                return 1; // instance greater than received
            } else {
                if (this.yRevision < tmp.yRevision) {
                    return -1; // instance less than received
                } else if (this.yRevision > tmp.yRevision) {
                    return 1; // instance greater than received
                } else {
                    return 0; // instance == received
                }
            }
        }
    }

    // Create string to show some input fields for exception reporting
    public String toString() {
        return desSnoId + TAB_CHARACTER + status + TAB_CHARACTER + conSnoId + TAB_CHARACTER + termText
                + TAB_CHARACTER + capStatus + TAB_CHARACTER + descriptionType + TAB_CHARACTER
                + languageCode;
    }

}
