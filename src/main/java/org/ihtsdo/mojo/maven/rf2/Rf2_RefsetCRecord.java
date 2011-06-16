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
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.dwfa.tapi.TerminologyException;

public class Rf2_RefsetCRecord implements Comparable<Rf2_RefsetCRecord> {

    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    // RECORD FIELDS
    final String id;
    final String effDateStr;
    final long timeL;
    final boolean isActive;
    final String pathStr;
    final long refsetIdL;
    final long referencedComponentIdL;
    final long valueIdL; // For Language refset valueIdL is acceptibilityId

    public Rf2_RefsetCRecord(String id, String dateStr, boolean active, String path,
            long refsetIdL, long referencedComponentIdL, long valueIdL) throws ParseException {
        this.id = id;
        this.effDateStr = dateStr;
        this.timeL = Rf2x.convertDateToTime(dateStr);
        this.isActive = active;

        /* this.pathStr = path; */
        this.pathStr = "2faa9260-8fb2-11db-b606-0800200c9a66";

        this.refsetIdL = refsetIdL;
        this.referencedComponentIdL = referencedComponentIdL;
        this.valueIdL = valueIdL;
    }

    static Rf2_RefsetCRecord[] parseRefset(Rf2File f) throws IOException, ParseException {

        int count = Rf2File.countFileLines(f);
        Rf2_RefsetCRecord[] a = new Rf2_RefsetCRecord[count];

        // DATA COLUMNS
        int ID = 0;// id
        int EFFECTIVE_TIME = 1; // effectiveTime
        int ACTIVE = 2; // active
        int MODULE_ID = 3; // moduleId
        int REFSET_ID = 4; // refSetId
        int REFERENCED_COMPONENT_ID = 5; // referencedComponentId
        int VALUE_ID = 6; // For Language refset VALUE_ID is ACCEPTIBILITY_ID

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f.file),
                "UTF-8"));
        Set m = new HashSet<Long>();

        int idx = 0;
        br.readLine(); // Header row
        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            Long refsetIdL = Long.parseLong(line[REFSET_ID]);
            m.add(refsetIdL);

            a[idx] = new Rf2_RefsetCRecord(line[ID],
                    Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]),
                    Rf2x.convertStringToBoolean(line[ACTIVE]),
                    Rf2x.convertIdToUuidStr(line[MODULE_ID]),
                    Long.parseLong(line[REFSET_ID]),
                    Long.parseLong(line[REFERENCED_COMPONENT_ID]),
                    Long.parseLong(line[VALUE_ID]));
            idx++;
        }

        Long[] aLongs = (Long[]) m.toArray(new Long[0]);
        StringBuilder sb = new StringBuilder();
        sb.append("Concept Refset SCT IDs:\r\n");
        for (Long l : aLongs) {
            sb.append(l.toString());
            sb.append("\r\n");
        }
        Logger.getLogger(Rf2_CrossmapRecord.class.getName()).info(sb.toString());

        return a;
    }

    public void writeArf(BufferedWriter writer) throws IOException, TerminologyException {

        // Refset UUID
        writer.append(Rf2x.convertIdToUuidStr(refsetIdL) + TAB_CHARACTER);

        // Member UUID
        writer.append(id + TAB_CHARACTER);

        // Status UUID
        writer.append(Rf2x.convertActiveToStatusUuid(isActive) + TAB_CHARACTER);

        // Component UUID
        writer.append(Rf2x.convertIdToUuidStr(referencedComponentIdL) + TAB_CHARACTER);

        // Effective Date
        writer.append(effDateStr + TAB_CHARACTER);

        // Path UUID
        writer.append(pathStr + TAB_CHARACTER);

        // Concept Extension Value UUID
        writer.append(Rf2x.convertIdToUuidStr(valueIdL) + LINE_TERMINATOR);
    }

    @Override
    public int compareTo(Rf2_RefsetCRecord t) {
        if (this.referencedComponentIdL < t.referencedComponentIdL) {
            return -1; // instance less than received
        } else if (this.referencedComponentIdL > t.referencedComponentIdL) {
            return 1; // instance greater than received
        }
        return 0; // instance == received
    }
}
