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

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class Rf2_RefsetCUuidRecord implements Comparable<Rf2_RefsetCUuidRecord> {

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
    final String valueUuid; // SCT_ID. For Language refset valueIdL is acceptibilityId

    public Rf2_RefsetCUuidRecord(String id, String dateStr, boolean active, String path,
            long refsetIdL, long referencedComponentIdL, String valueUuid) throws ParseException {
        this.id = id;
        this.effDateStr = dateStr;
        this.timeL = Rf2x.convertDateToTime(dateStr);
        this.isActive = active;

        /* this.pathStr = path; */
        this.pathStr = "8c230474-9f11-30ce-9cad-185a96fd03a2"; // SNOMED Core

        this.refsetIdL = refsetIdL;
        this.referencedComponentIdL = referencedComponentIdL;
        this.valueUuid = valueUuid;
    }

    static Rf2_RefsetCUuidRecord[] parseRefset(Rf2File f, Long[] exclusions)
            throws IOException, ParseException {

        int count = Rf2File.countFileLines(f);
        int countExludedMembers = 0;
        int currentCount = 0;
        ArrayList<Rf2_RefsetCUuidRecord> a = new ArrayList<Rf2_RefsetCUuidRecord>();

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
        Set idSet = new HashSet<Long>();

        br.readLine(); // Header row
        currentCount++;
        try {
			while (br.ready()) {
			    String[] line = br.readLine().split(TAB_CHARACTER);
			    currentCount++;

			    Long refsetIdL = Long.parseLong(line[REFSET_ID]);
			    boolean found = false;
			    if (exclusions != null) {
			        for (Long excludedId : exclusions) {
			            if (excludedId.compareTo(refsetIdL) == 0) {
			                found = true;
			            }
			        }
			    }
			    if (found) {
			        countExludedMembers++;
			        continue;
			    }
			    idSet.add(refsetIdL);

			    a.add(new Rf2_RefsetCUuidRecord(line[ID],
			            Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]),
			            Rf2x.convertStringToBoolean(line[ACTIVE]),
			            Rf2x.convertIdToUuidStr(line[MODULE_ID]),
			            Long.parseLong(line[REFSET_ID]),
			            Long.parseLong(line[REFERENCED_COMPONENT_ID]),
			            line[VALUE_ID]));
			}
		} catch (NumberFormatException e) {
			AceLog.getAppLog().severe("Error parsing Refset recors: File=" + f.file.getName() + " Line=" + currentCount);
			throw e;
		}

        Long[] aLongs = (Long[]) idSet.toArray(new Long[0]);
        StringBuilder sb = new StringBuilder();
        sb.append("Concept Refset SCT IDs kept:\r\n");
        sb.append(f.file.getName());
        sb.append("\r\n");
        for (Long l : aLongs) {
            sb.append(l.toString());
            sb.append("\t");
            sb.append(Rf2x.convertIdToUuidStr(l));
            sb.append("\r\n");
        }
        Logger.getLogger(Rf2_RefsetCUuidRecord.class.getName()).info(sb.toString());

        sb = new StringBuilder();
        sb.append("Concept Refset SCT IDs excluded:\r\n");
        sb.append(f.file.getName());
        sb.append("\r\n");
        if (exclusions != null) {
            for (Long l : exclusions) {
                sb.append(l.toString());
                sb.append("\t");
                sb.append(Rf2x.convertIdToUuidStr(l));
                sb.append("\r\n");
            }
        } else {
            sb.append("none.\r\n");
        }
        Logger.getLogger(Rf2_RefsetCUuidRecord.class.getName()).info(sb.toString());

        sb = new StringBuilder();
        sb.append("Filter Stats\r\n");
        sb.append(f.file.getName());
        sb.append("\r\nTotal members viewed   =\t");
        sb.append(count);
        sb.append("\r\nTotal members kept     =\t");
        sb.append(a.size());
        sb.append("\r\nTotal members excluded =\t");
        sb.append(countExludedMembers);
        sb.append("\r\n");
        Logger.getLogger(Rf2_RefsetCUuidRecord.class.getName()).info(sb.toString());

        Rf2_RefsetCUuidRecord[] b = new Rf2_RefsetCUuidRecord[a.size()];
        int idx = 0;
        for (Rf2_RefsetCUuidRecord rec : a) {
            b[idx] = rec;
            idx++;
        }

        return b;
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
        writer.append(valueUuid + LINE_TERMINATOR);
    }

    @Override
    public int compareTo(Rf2_RefsetCUuidRecord t) {
        if (this.referencedComponentIdL < t.referencedComponentIdL) {
            return -1; // instance less than received
        } else if (this.referencedComponentIdL > t.referencedComponentIdL) {
            return 1; // instance greater than received
        }
        return 0; // instance == received
    }
}
