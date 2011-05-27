/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.maven.rf2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import org.dwfa.tapi.TerminologyException;

class Sct2_RelRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    private static final String uuidUserStr = "f7495b58-6630-3499-a44e-2052b5fcf06c";
    private static final String uuidUserSnorocketStr = "7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9";
    // RELATIONSHIP FIELDS
    long relSnoId; // SNOMED RELATIONSHIPID, if applicable
    String effDateStr;
    boolean isActive; // status is computed for relationships
    String pathStr;
    long c1SnoId; // CONCEPTID1
    long roleTypeSnoId; // RELATIONSHIPTYPE .. SNOMED ID
    long c2SnoId; // CONCEPTID2
    int group; // RELATIONSHIPGROUP
    long characteristicL; // CHARACTERISTICTYPE
    long refinability; // REFINABILITY
    boolean isInferred;

    public Sct2_RelRecord(long relID, String dateStr, boolean active, String path,
            long cOneID, long roleTypeSnoId, long cTwoID, int grp,
            long characterType, long refinibility,
            boolean inferredB) {

        this.relSnoId = relID; // RELATIONSHIPID

        this.effDateStr = dateStr;
        this.isActive = active;
        this.pathStr = path;

        this.c1SnoId = cOneID; // CONCEPTID1
        this.roleTypeSnoId = roleTypeSnoId; // RELATIONSHIPTYPE (SNOMED ID)
        this.c2SnoId = cTwoID; // CONCEPTID2
        this.group = grp; // RELATIONSHIPGROUP

        this.characteristicL = characterType; // CHARACTERISTICTYPE
        this.refinability = refinibility; // REFINABILITY

        this.isInferred = inferredB;
    }

    public static Sct2_RelRecord[] parseRelationships(Rf2File f, boolean inferredB) throws IOException {

        int count = Rf2File.countFileLines(f);
        Sct2_RelRecord[] a = new Sct2_RelRecord[count];

        int ID = 0; // id
        int EFFECTIVE_TIME = 1; // effectiveTime
        int ACTIVE = 2; // active
        int MODULE_ID = 3; // moduleId
        int SOURCE_ID = 4; // sourceId
        int DESTINATION_ID = 5; // destinationId
        int RELATIONSHIP_GROUP = 6; // relationshipGroup
        int TYPE_ID = 7; // typeId
        int CHARACTERISTIC_TYPE = 8; // characteristicTypeId
        int MODIFIER_ID = 9; // modifierId

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f.file),
                "UTF-8"));

        // Header row
        br.readLine();

        int idx = 0;
        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            a[idx] = new Sct2_RelRecord(Long.parseLong(line[ID]),
                    Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]),
                    Rf2x.convertStringToBoolean(line[ACTIVE]),
                    Rf2x.convertIdToUuidStr(line[MODULE_ID]),
                    Long.parseLong(line[SOURCE_ID]),
                    Long.parseLong(line[TYPE_ID]),
                    Long.parseLong(line[DESTINATION_ID]),
                    Integer.parseInt(line[RELATIONSHIP_GROUP]),
                    Long.parseLong(line[CHARACTERISTIC_TYPE]),
                    0,
                    inferredB);
        }

        return a;
    }

    // Create string to show some input fields for exception reporting
    @Override
    public String toString() {
        return relSnoId + TAB_CHARACTER + isActive + TAB_CHARACTER
                + c1SnoId + TAB_CHARACTER + roleTypeSnoId + TAB_CHARACTER + c2SnoId + TAB_CHARACTER + group;
    }

    public void writeArf(BufferedWriter writer) throws IOException, TerminologyException {
        // Relationship UUID
        writer.append(Rf2x.convertIdToUuidStr(relSnoId) + TAB_CHARACTER);

        // Status UUID
        writer.append(Rf2x.convertActiveToStatusUuid(isActive) + TAB_CHARACTER);

        // Source Concept UUID
        writer.append(Rf2x.convertIdToUuidStr(c1SnoId) + TAB_CHARACTER);

        // Relationship Type UUID
        writer.append(Rf2x.convertIdToUuidStr(roleTypeSnoId) + TAB_CHARACTER);

        // Destination Concept UUID
        writer.append(Rf2x.convertIdToUuidStr(c2SnoId) + TAB_CHARACTER);

        // Relationship Group
        writer.append(group + TAB_CHARACTER);

        // Characteristic Type UUID
        writer.append(Rf2x.convertIdToUuidStr(characteristicL) + TAB_CHARACTER);

        // Refinibility UUID
        // notRefinable	RF2==900000000000007000, RF1="0"
        // optional     RF2==900000000000216007, RF1="1"
        // mandatory    RF2==900000000000218008, RF1="2"
        writer.append("900000000000216007" + TAB_CHARACTER);

        // Effective Date
        writer.append(effDateStr + TAB_CHARACTER);

        // Path UUID
        writer.append(pathStr + TAB_CHARACTER);

        // Author UUID
        if (isInferred) {
            writer.append(uuidUserSnorocketStr + LINE_TERMINATOR);
        } else {
            writer.append(uuidUserStr + LINE_TERMINATOR);
        }

    }
}