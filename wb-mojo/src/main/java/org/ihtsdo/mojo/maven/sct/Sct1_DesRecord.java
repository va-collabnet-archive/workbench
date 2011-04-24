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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.dwfa.util.id.Type3UuidFactory;

class Sct1_DesRecord implements Comparable<Object>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAB_CHARACTER = "\t";

    long desSnoId; // DESCRIPTIONID
    long desUuidMsb;
    long desUuidLsb;
    int status; // DESCRIPTIONSTATUS
    // ArrayList<EIdentifier> additionalIds;
    ArrayList<Sct1_IdRecord> addedIds;
    long conSnoId; // CONCEPTID
    long conUuidMsb; // CONCEPTID
    long conUuidLsb; // CONCEPTID    
    String termText; // TERM
    int capStatus; // INITIALCAPITALSTATUS -- capitalization
    int descriptionType; // DESCRIPTIONTYPE
    String languageCode; // LANGUAGECODE
    int pathIdx;
    long revTime;

    public Sct1_DesRecord(long dId, int s, long cId, String text, int cStat, int typeInt, String lang) {
        desSnoId = dId;
        UUID tmpUUID = Type3UuidFactory.fromSNOMED(desSnoId);
        desUuidMsb = tmpUUID.getMostSignificantBits();
        desUuidLsb = tmpUUID.getLeastSignificantBits();

        status = s;
        // additionalIds = null;
        addedIds = null;

        conSnoId = cId;
        tmpUUID = Type3UuidFactory.fromSNOMED(conSnoId);
        conUuidMsb = tmpUUID.getMostSignificantBits();
        conUuidLsb = tmpUUID.getLeastSignificantBits();

        termText = new String(text);
        capStatus = cStat;
        descriptionType = typeInt;
        languageCode = new String(lang);
    }

    public Sct1_DesRecord(UUID desUuid, int status, UUID uuidCon, String termStr,
            int capitalization, int desTypeIdx, String langCodeStr, long revTime, int pathIdx) {
        this.desSnoId = Long.MAX_VALUE; // DESCRIPTIONID
        this.desUuidMsb = desUuid.getMostSignificantBits();
        this.desUuidLsb = desUuid.getLeastSignificantBits();
        this.status = status; // DESCRIPTIONSTATUS
        // additionalIds = null;
        addedIds = null;
        this.conSnoId = Long.MAX_VALUE; // CONCEPTID
        this.conUuidMsb = uuidCon.getMostSignificantBits(); // CONCEPTID
        this.conUuidLsb = uuidCon.getLeastSignificantBits(); // CONCEPTID    
        this.termText = termStr; // TERM
        this.capStatus = capitalization; // INITIALCAPITALSTATUS -- capitalization
        this.descriptionType = desTypeIdx; // DESCRIPTIONTYPE
        this.languageCode = langCodeStr; // LANGUAGECODE
        this.pathIdx = pathIdx;
        this.revTime = revTime;
    }

    // method required for object to be sortable (comparable) in arrays
    @Override
    public int compareTo(Object obj) {
        Sct1_DesRecord o2 = (Sct1_DesRecord) obj;
        int thisMore = 1;
        int thisLess = -1;
        // DESCRIPTION UUID
        if (this.desUuidMsb > o2.desUuidMsb) {
            return thisMore;
        } else if (this.desUuidMsb < o2.desUuidMsb) {
            return thisLess;
        } else {
            if (this.desUuidLsb > o2.desUuidLsb) {
                return thisMore;
            } else if (this.desUuidLsb < o2.desUuidLsb) {
                return thisLess;
            } else {
                // Path
                if (this.pathIdx > o2.pathIdx) {
                    return thisMore;
                } else if (this.pathIdx < o2.pathIdx) {
                    return thisLess;
                } else {
                    // Revision
                    if (this.revTime > o2.revTime) {
                        return thisMore;
                    } else if (this.revTime < o2.revTime) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            }
        }
    }

    public static Sct1_DesRecord[] parseDescriptions(Sct1File sct1File) throws IOException {

        int count = Sct1File.countFileLines(sct1File);
        Sct1_DesRecord[] a = new Sct1_DesRecord[count];

        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(
                sct1File.file), "UTF-8"));
        int descriptions = 0;

        int DESCRIPTIONID = 0;
        int DESCRIPTIONSTATUS = 1;
        int CONCEPTID = 2;
        int TERM = 3;
        int INITIALCAPITALSTATUS = 4;
        int DESCRIPTIONTYPE = 5;
        int LANGUAGECODE = 6;

        // Header row
        r.readLine();

        while (r.ready()) {
            String[] line = r.readLine().split(TAB_CHARACTER);

            // DESCRIPTIONID
            long descriptionId = Long.parseLong(line[DESCRIPTIONID]);
            // DESCRIPTIONSTATUS
            int status = Integer.parseInt(line[DESCRIPTIONSTATUS]);
            // CONCEPTID
            long conSnoId = Long.parseLong(line[CONCEPTID]);
            // TERM
            String text = line[TERM];
            // INITIALCAPITALSTATUS
            int capStatus = Integer.parseInt(line[INITIALCAPITALSTATUS]);
            // DESCRIPTIONTYPE
            int typeInt = Integer.parseInt(line[DESCRIPTIONTYPE]);
            // LANGUAGECODE
            String lang = line[LANGUAGECODE];

            // Save to sortable array
            a[descriptions] = new Sct1_DesRecord(descriptionId, status, conSnoId, text, capStatus,
                    typeInt, lang);
            descriptions++;

        }
        Arrays.sort(a);
        
        return a;
    }
    
    // Create string to show some input fields for exception reporting
    // DESCRIPTIONID    DESCRIPTIONSTATUS   CONCEPTID   TERM    INITIALCAPITALSTATUS    DESCRIPTIONTYPE LANGUAGECODE
    public static String toStringHeader() {
        return "DESCRIPTIONID" + TAB_CHARACTER + "DESCRIPTIONSTATUS" + TAB_CHARACTER + "CONCEPTID"
                + TAB_CHARACTER + "TERM" + TAB_CHARACTER + "INITIALCAPITALSTATUS" + TAB_CHARACTER
                + "DESCRIPTIONTYPE" + TAB_CHARACTER + "LANGUAGECODE";
    }

    // Create string to show some input fields for exception reporting
    @Override
    public String toString() {
        return desSnoId + TAB_CHARACTER + status + TAB_CHARACTER + conSnoId + TAB_CHARACTER
                + termText + TAB_CHARACTER + capStatus + TAB_CHARACTER + descriptionType
                + TAB_CHARACTER + languageCode;
    }

}
