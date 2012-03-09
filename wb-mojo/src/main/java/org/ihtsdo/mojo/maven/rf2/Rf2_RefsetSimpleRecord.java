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
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class Rf2_RefsetSimpleRecord implements Comparable<Rf2_RefsetSimpleRecord> {

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
    final String uuidNormalMember; // For Language refset uuidNormalMember is acceptibilityId

    public Rf2_RefsetSimpleRecord(String id, String dateStr, boolean active, String path,
            long refsetIdL, long referencedComponentIdL, String uuid) throws ParseException {
        this.id = id;
        this.effDateStr = dateStr;
        this.timeL = Rf2x.convertDateToTime(dateStr);
        this.isActive = active;

        /* this.pathStr = path; */
        this.pathStr = "8c230474-9f11-30ce-9cad-185a96fd03a2"; // SNOMED Core

        this.refsetIdL = refsetIdL;
        this.referencedComponentIdL = referencedComponentIdL;
        this.uuidNormalMember = uuid;
    }

    static Rf2_RefsetSimpleRecord[] parseRefset(Rf2File f)
            throws IOException, ParseException, IOException, TerminologyException {
        String uuidNormalMember = RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid().toString();

        int count = Rf2File.countFileLines(f);
        Rf2_RefsetSimpleRecord[] a = new Rf2_RefsetSimpleRecord[count];

        // DATA COLUMNS
        int ID = 0;// id
        int EFFECTIVE_TIME = 1; // effectiveTime
        int ACTIVE = 2; // active
        int MODULE_ID = 3; // moduleId
        int REFSET_ID = 4; // refSetId
        int REFERENCED_COMPONENT_ID = 5; // referencedComponentId

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f.file),
                "UTF-8"));
        Set idSet = new HashSet<Long>();

        int idx = 0;
        br.readLine(); // Header row
        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            Long refsetIdL = Long.parseLong(line[REFSET_ID]);
            idSet.add(refsetIdL);

            a[idx] = new Rf2_RefsetSimpleRecord(line[ID],
                    Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]),
                    Rf2x.convertStringToBoolean(line[ACTIVE]),
                    Rf2x.convertIdToUuidStr(line[MODULE_ID]),
                    Long.parseLong(line[REFSET_ID]),
                    Long.parseLong(line[REFERENCED_COMPONENT_ID]),
                    uuidNormalMember);
            idx++;
        }

        Long[] aLongs = (Long[]) idSet.toArray(new Long[0]);
        StringBuilder sb = new StringBuilder();
        sb.append("Simple Refset SCT IDs:\r\n");
        sb.append(f.file.getName());
        sb.append("\r\n");
        for (Long l : aLongs) {
            sb.append(l.toString());
            sb.append("\t");
            sb.append(Rf2x.convertIdToUuidStr(l));
            sb.append("\r\n");
        }
        Logger.getLogger(Rf2_CrossmapRecord.class.getName()).info(sb.toString());

        return a;
    }

    public void writeArf(BufferedWriter writer) throws IOException, TerminologyException {

        // Refset UUID
        writer.append(Rf2x.convertIdToUuidStr(refsetIdL) + TAB_CHARACTER);

        // Member UUID
        if (id.length() == 36) {
            writer.append(id + TAB_CHARACTER);
        } else {
            writer.append(id.substring(0,8) + '-');
            writer.append(id.substring(8,12) + '-');
            writer.append(id.substring(12,16) + '-');
            writer.append(id.substring(16,20) + '-');
            writer.append(id.substring(20,32) + TAB_CHARACTER);
        }

        // Status UUID
        writer.append(Rf2x.convertActiveToStatusUuid(isActive) + TAB_CHARACTER);

        // Component UUID
        writer.append(Rf2x.convertIdToUuidStr(referencedComponentIdL) + TAB_CHARACTER);

        // Effective Date
        writer.append(effDateStr + TAB_CHARACTER);

        // Path UUID
        writer.append(pathStr + TAB_CHARACTER);

        // Concept Extension Value UUID
        writer.append(uuidNormalMember + LINE_TERMINATOR);
    }

    @Override
    public int compareTo(Rf2_RefsetSimpleRecord t) {
        if (this.referencedComponentIdL < t.referencedComponentIdL) {
            return -1; // instance less than received
        } else if (this.referencedComponentIdL > t.referencedComponentIdL) {
            return 1; // instance greater than received
        }
        return 0; // instance == received
    }
}