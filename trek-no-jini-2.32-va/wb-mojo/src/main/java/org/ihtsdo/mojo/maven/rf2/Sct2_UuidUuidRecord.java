/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.log.AceLog;
import org.dwfa.util.id.Type3UuidFactory;

/**
 *
 * @author logger
 */
public class Sct2_UuidUuidRecord
        implements Comparable<Sct2_UuidUuidRecord>, Serializable {

    private static final long serialVersionUID = 1L;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    UUID uuidComputed;
    UUID uuidDeclared;

    public Sct2_UuidUuidRecord(UUID uuidComputed, UUID uuidDeclared) {
        this.uuidComputed = uuidComputed;
        this.uuidDeclared = uuidDeclared;
    }

    /**
     * Sort order: SCTID, UUID long
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Sct2_UuidUuidRecord o) {
        if (this.uuidComputed.compareTo(o.uuidComputed) < 0) {
            return -1; // instance less than received
        } else if (this.uuidComputed.compareTo(o.uuidComputed) > 0) {
            return 1; // instance greater than received
        } else {
            if (this.uuidDeclared.compareTo(o.uuidDeclared) < 0) {
                return -1; // instance less than received
            } else if (this.uuidDeclared.compareTo(o.uuidDeclared) > 0) {
                return 1; // instance greater than received
            } else {
                return 0; // instance == received
            }
        }
    }
    
        /**
     * 
     * @param fList
     * @param parseToUuidRemapCacheFile
     * @throws Exception 
     */
    static void parseToUuidRemapCacheFile(List<Rf2File> fList, String idCacheOutputPathFnameStr)
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
                        new FileOutputStream(idCacheOutputPathFnameStr)))) {
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
                    UUID cUuid = UUID.fromString(
                            Type3UuidFactory.fromSNOMED(sctIdL).toString());
                    // UUID cUuid = UUID.fromString(Rf2x.convertSctIdToUuidStr(sctIdL));
                    if (aUuid.compareTo(cUuid) != 0) {
                        countNonComputedIdsL++;
                    }

                    Sct2_UuidUuidRecord tempIdCompact = new Sct2_UuidUuidRecord(
                            cUuid,
                            aUuid);
                    // Write to JBIN file
                    oos.writeUnshared(tempIdCompact);
                }
                StringBuilder sb = new StringBuilder();
                sb.append("\n::: parseToUuidRemapCacheFile(..) ");
                sb.append("\n::: PARSED & WRITTEN TO ID CACHE: ");
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


}
