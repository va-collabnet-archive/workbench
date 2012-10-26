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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

/**
 * Simple case: single date (no versioning). all active. one id schema. Handle
 * remapping of non-computed primorial UUIDs.
 *
 * @author marc campbell
 */
public class Sct2_IdRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    // RECORD FILES
    // long identifierScheme; // IDENTIFIER_SCHEME_ID column 0;
    UUID primordialUuid; // ALTERNATE_IDENTIFIER column 1;
    long revTime; // EFFECTIVE_TIME column 2; e.g. date time of "20020131"
    int active; // ACTIVE column 3; value 1 is active
    long moduleId; // MODULE_ID column 4; value 900000000000207008
    long referencedComponent; // REFERENCED_COMPONENT_ID column 5 SCTID
    // PATH
    // int pathIdx;
    // USER
    // int userIdx;

    public Sct2_IdRecord(UUID pUuid, long revTime, int active, long moduleId, long referencedComponent) {
        this.primordialUuid = pUuid; // CONCEPTID/PRIMARYID
        this.revTime = revTime;
        this.active = active;
        this.moduleId = moduleId;
        this.referencedComponent = referencedComponent;
    }

    /**
     *
     * prepare id lookup cache file.
     *
     * @param f
     * @return
     * @throws Exception
     */
    static void createIdsJbinFile(List<Rf2File> fList, String outputPathString)
            throws Exception {
        // SNOMED CT UUID scheme
        long sctUuidSchemeIdL = Long.parseLong("900000000000002006");
        long countNonActiveL;
        long countNonComputedIdsL;
        long nonParsableLinesL;
        long totalParsedLinesL;
        Set<Long> idSchemeSet = new HashSet<>();
        Set<Long> dateTimeSet = new HashSet<>();
        Set<Long> moduleIdSet = new HashSet<>();
        try (ObjectOutputStream oos = new ObjectOutputStream(
                        new BufferedOutputStream(
                        new FileOutputStream(outputPathString)))) {
            int IDENTIFIER_SCHEME_ID = 0;
            int ALTERNATE_IDENTIFIER = 1;
            int EFFECTIVE_TIME = 2;
            int ACTIVE = 3;
            int MODULE_ID = 4;
            int REFERENCED_COMPONENT_ID = 5;
            for (Rf2File f : fList) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                        new FileInputStream(f.file), "UTF-8"));
                br.readLine();
                countNonActiveL = 0;
                countNonComputedIdsL = 0;
                totalParsedLinesL = 0;
                nonParsableLinesL = 0;
                while (br.ready()) {
                    String tempLine = br.readLine();
                    String[] line = tempLine.split(TAB_CHARACTER);
                    if (line.length < REFERENCED_COMPONENT_ID + 1) {
                        System.err.println("not parsed: " + tempLine);
                        nonParsableLinesL++;
                        continue;
                    } else {
                        totalParsedLinesL++;
                    }
                    // IDENTIFIER_SCHEME_ID
                    long identifierScheme = Long.parseLong(line[IDENTIFIER_SCHEME_ID]);
                    if (identifierScheme != sctUuidSchemeIdL) {
                        idSchemeSet.add(identifierScheme);
                        continue;
                    }
                    // ACTIVE
                    int activeData = Integer.parseInt(line[ACTIVE]);
                    if (activeData != 1) {
                        countNonActiveL++;
                    }
                    // EFFECTIVE_TIME
                    String eTimeStr = Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]);
                    long eTime = Rf2x.convertDateToTime(eTimeStr);
                    dateTimeSet.add(eTime);

                    // MODULE_ID
                    Long moduleIdL = Long.parseLong(line[MODULE_ID]);
                    moduleIdSet.add(moduleIdL);

                    // ALTERNATE_IDENTIFIER and REFERENCED_COMPONENT_ID
                    // Assigned uuid
                    UUID aUuid = UUID.fromString(line[ALTERNATE_IDENTIFIER]);
                    // Computed uuid
                    long sctIdL = Long.parseLong(line[REFERENCED_COMPONENT_ID]);
                    UUID cUuid = UUID.fromString(Rf2x.convertSctIdToUuidStr(sctIdL));
                    if (aUuid.compareTo(cUuid) != 0) {
                        countNonComputedIdsL++;
                    }

                    Sct2_IdCompact tempIdCompact = new Sct2_IdCompact(
                            aUuid.getMostSignificantBits(),
                            aUuid.getLeastSignificantBits(),
                            sctIdL);
                    // Write to JBIN file
                    oos.writeUnshared(tempIdCompact);
                }
                StringBuilder sb = new StringBuilder();
                sb.append("\n::: PARSED: ");
                sb.append(f.file.toURI().toString());
                if (idSchemeSet.size() > 0) {
                    Long[] idSchemeArray = idSchemeSet.toArray(new Long[0]);
                    for (Long long1 : idSchemeArray) {
                        sb.append("\n::: WARNING unsupported id scheme: ");
                        sb.append(long1.toString());
                    }
                } else {
                    sb.append("\n::: Schema OK (900000000000002006)");
                }
                sb.append("\n::: countNonActive=");
                sb.append(countNonActiveL);
                sb.append("\n::: countNonComputedIDs=");
                sb.append(countNonComputedIdsL);
                sb.append("\n::: totalParsedLinesL=");
                sb.append(totalParsedLinesL);
                sb.append("\n::: nonParsableLines=");
                sb.append(nonParsableLinesL);
                sb.append("\n::: dateTimeCount=");
                sb.append(dateTimeSet.size());
                Long[] moduleIdArray = moduleIdSet.toArray(new Long[0]);
                for (Long moduleIdLong : moduleIdArray) {
                    sb.append("\n::: ModuleID: ");
                    sb.append(moduleIdLong.toString());
                }
                Long[] dateTimeArray = dateTimeSet.toArray(new Long[0]);
                for (Long dateLong : dateTimeArray) {
                    sb.append("\n:::     ");
                    sb.append(Rf2x.convertTimeToDate(dateLong));
                }
                sb.append("\n::: \n");
                AceLog.getAppLog().info(sb.toString());
            }
            oos.flush();
        }
    }

    /**
     *
     * :NYI: SCTIDs UUID pairing are not versioned.
     *
     * @param f
     * @return
     * @throws Exception
     */
    static ArrayList<Sct2_IdRecord> parseIds(Rf2File f,
            boolean returnIdRecordListB,
            BufferedWriter arfWriter,
            ObjectOutputStream compactIdStream)
            throws Exception {
        // SNOMED CT UUID scheme
        long sctUuidSchemeId = Long.parseLong("900000000000002006");

        ArrayList<Sct2_IdRecord> idRecordList = null;
        if (returnIdRecordListB) {
            idRecordList = new ArrayList();
        }

        // DATA COLUMNS
        int IDENTIFIER_SCHEME_ID = 0;
        int ALTERNATE_IDENTIFIER = 1; // UUID of interest
        int EFFECTIVE_TIME = 2; // effectiveTime 20020131
        int ACTIVE = 3; // active 1
        int MODULE_ID = 4; // moduleId 900000000000207008
        int REFERENCED_COMPONENT_ID = 5; // referencedComponentId 100000000

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                new FileInputStream(f.file), "UTF-8"));

        // Header row
        br.readLine();

        while (br.ready()) {
            String tempLine = br.readLine();
            String[] line = tempLine.split(TAB_CHARACTER);
            if (line.length < REFERENCED_COMPONENT_ID + 1) {
                System.err.println("not parsed: " + tempLine);
                continue;
            }
            // Check IDENTIFIER_SCHEME_ID
            long identifierScheme = Long.parseLong(line[IDENTIFIER_SCHEME_ID]);
            if (identifierScheme != sctUuidSchemeId) {
                throw new UnsupportedOperationException("ID scheme not supported");
            }

            int activeData = Integer.parseInt(line[ACTIVE]);
            String eTimeStr = Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]);
            long eTime = Rf2x.convertDateToTime(eTimeStr);
            UUID aUuid = UUID.fromString(line[ALTERNATE_IDENTIFIER]);
            long sctIdL = Long.parseLong(line[REFERENCED_COMPONENT_ID]);

            Sct2_IdRecord tempIdRecord = new Sct2_IdRecord(aUuid,
                    eTime,
                    activeData,
                    Long.parseLong(line[MODULE_ID]),
                    sctIdL);
            // Add to Sct2_IdRecord List
            if (idRecordList != null) {
                idRecordList.add(tempIdRecord);
            }
            // Write to ARF file
            if (arfWriter != null) {
                tempIdRecord.writeArf(arfWriter);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n::: FILE: ");
        sb.append(f.file.toURI().toString());
        sb.append("::: completed parseIds()\n");
        AceLog.getAppLog().info(sb.toString());
        return idRecordList;
    }

    // writeSctSnomedLongId
    // BufferedWriter writer, long sctId, String date, String path
    public void writeArf(BufferedWriter writer)
            throws IOException, TerminologyException {
        // PRIMARY_UUID = 0;
        writer.append(this.primordialUuid.toString() + TAB_CHARACTER);
        // SOURCE_SYSTEM_UUID = 1;
        //writer.append(uuidSourceSnomedLongStr + TAB_CHARACTER);
        writer.append(":!!!:REPLACE_THIS" + TAB_CHARACTER);
        // ID_FROM_SOURCE_SYSTEM = 2;
        writer.append(Long.toString(this.referencedComponent) + TAB_CHARACTER);
        // STATUS_UUID = 3; // :!!!:NYI: always true :???:
        writer.append(Rf2x.convertActiveToStatusUuid(true) + TAB_CHARACTER);
        // EFFECTIVE_DATE = 4; // yyyy-MM-dd HH:mm:ss
        // writer.append(date + TAB_CHARACTER);
        writer.append(":!!!:REPLACE_THIS" + TAB_CHARACTER);
        // PATH_UUID = 5;
        // writer.append(path + LINE_TERMINATOR);
        writer.append(":!!!:REPLACE_THIS" + LINE_TERMINATOR);
        // :!!!: module & author
    }
}