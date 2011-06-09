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

/**
 *
 * @author marc
 */
public class Rf2_CrossmapRecord {

    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    // RECORD FIELDS
    final String id; // UUID
    final String effDateStr;
    final long timeL;
    final boolean isActive;
    final String pathStr;
    final long refsetIdL;
    final long referencedComponentIdL;
    final String mapValueStr;

    static String uuidSourceSnomedRtStr = null;
    static String uuidSourceCtv3Str = null;

    public Rf2_CrossmapRecord(String id, String dateStr, boolean active, String path,
            long refsetIdL, long referencedComponentIdL, String valueIdL) throws ParseException {
        this.id = id;
        this.effDateStr = dateStr;
        this.timeL = Rf2x.convertDateToTime(dateStr);
        this.isActive = active;

        this.pathStr = path;

        this.refsetIdL = refsetIdL;
        this.referencedComponentIdL = referencedComponentIdL;
        this.mapValueStr = valueIdL;
    }

    static Rf2_CrossmapRecord[] parseCrossmapFile(Rf2File f)
            throws IOException, ParseException {

        int count = Rf2File.countFileLines(f);
        Rf2_CrossmapRecord[] a = new Rf2_CrossmapRecord[count];

        // DATA COLUMNS
        int ID = 0;// id
        int EFFECTIVE_TIME = 1; // effectiveTime
        int ACTIVE = 2; // active
        int MODULE_ID = 3; // moduleId
        int REFSET_ID = 4; // refSetId
        int REFERENCED_COMPONENT_ID = 5; // referencedComponentId
        int MAP_TARGET_ID = 6; // For Language refset VALUE_ID is ACCEPTIBILITY_ID

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f.file),
                "UTF-8"));
        Set m = new HashSet<Long>();

        int idx = 0;
        br.readLine(); // Header row
        while (br.ready()) {
            String[] line = br.readLine().split(TAB_CHARACTER);

            Long refsetIdL = Long.parseLong(line[REFSET_ID]);
            m.add(refsetIdL);

            a[idx] = new Rf2_CrossmapRecord(line[ID],
                    Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]),
                    Rf2x.convertStringToBoolean(line[ACTIVE]),
                    Rf2x.convertIdToUuidStr(line[MODULE_ID]),
                    refsetIdL,
                    Long.parseLong(line[REFERENCED_COMPONENT_ID]),
                    line[MAP_TARGET_ID]);
            idx++;
        }

        Long[] aLongs = (Long[]) m.toArray(new Long[0]);
        StringBuilder sb = new StringBuilder();
        for (Long l : aLongs) {
            sb.append(l.toString());
        }
        Logger.getLogger(Rf2_CrossmapRecord.class.getName()).info(sb.toString());

        return a;
    }

    public void writeArfRefset(BufferedWriter writer) throws IOException, TerminologyException {
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
        writer.append(mapValueStr + LINE_TERMINATOR);
    }

    public void writeArfId(BufferedWriter writer) throws IOException, TerminologyException {
        // PRIMARY_UUID = 0;
        writer.append(id + TAB_CHARACTER);
        // SOURCE_SYSTEM_UUID = 1;
        // 446608001 ICD-O
        // 900000000000498005 SNOMED RT
        // 900000000000497000 CTV3
        if (refsetIdL == 900000000000498005L) {
            writer.append(uuidSourceSnomedRtStr + TAB_CHARACTER);
        } else if (refsetIdL == 900000000000497000L) {
            writer.append(uuidSourceCtv3Str + TAB_CHARACTER);
        } else {
            throw new UnsupportedOperationException();
        }
        // ID_FROM_SOURCE_SYSTEM = 2;
        writer.append(mapValueStr + TAB_CHARACTER);
        // STATUS_UUID = 3;
        writer.append(Rf2x.convertActiveToStatusUuid(true) + TAB_CHARACTER);
        // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        writer.append(effDateStr + TAB_CHARACTER);
        // PATH_UUID = 5;
        writer.append(pathStr + LINE_TERMINATOR);
    }
}
