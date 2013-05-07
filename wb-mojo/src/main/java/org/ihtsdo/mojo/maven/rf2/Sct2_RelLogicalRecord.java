/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marc
 */
public class Sct2_RelLogicalRecord
        implements Comparable<Sct2_RelLogicalRecord>, Serializable {

    // class fields
    private static final long serialVersionUID = 1L;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    // instance fields
    long relSctId;
    long c1SnoId;
    long c2SnoId;
    long roleTypeSnoId;
    int group;
    long time;

    private Sct2_RelLogicalRecord(long relSctId,
            String dateStr,
            long sourceSctId,
            long typeSctId,
            long destSctId,
            int group) throws ParseException {

        this.relSctId = relSctId;
        this.c1SnoId = sourceSctId;
        this.roleTypeSnoId = typeSctId;
        this.c2SnoId = destSctId;
        this.group = group;

        this.time = Rf2x.convertDateToTime(dateStr);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static ArrayList<Sct2_RelLogicalRecord> parseRelationships(List<Rf2File> fList)
            throws IOException, ParseException {

        ArrayList<Sct2_RelLogicalRecord> a = new ArrayList<>();

        int ID = 0; // id
        int EFFECTIVE_TIME = 1; // effectiveTime
        int ACTIVE = 2; // active
        // int MODULE_ID = 3; // moduleId
        int SOURCE_ID = 4; // sourceId
        int DESTINATION_ID = 5; // destinationId
        int RELATIONSHIP_GROUP = 6; // relationshipGroup
        int TYPE_ID = 7; // typeId
        // int CHARACTERISTIC_TYPE = 8; // characteristicTypeId
        // int MODIFIER_ID = 9; // modifierId

        for (Rf2File f : fList) {

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                    new FileInputStream(f.file), "UTF-8"));

            br.readLine(); // Header row
            while (br.ready()) {
                String[] line = br.readLine().split(TAB_CHARACTER);

                if (Rf2x.convertStringToBoolean(line[ACTIVE])) {
                    Sct2_RelLogicalRecord rel;
                    rel = new Sct2_RelLogicalRecord(
                            Long.parseLong(line[ID]),
                            Rf2x.convertEffectiveTimeToDate(line[EFFECTIVE_TIME]),
                            Long.parseLong(line[SOURCE_ID]),
                            Long.parseLong(line[TYPE_ID]),
                            Long.parseLong(line[DESTINATION_ID]),
                            Integer.parseInt(line[RELATIONSHIP_GROUP]));
                    a.add(rel);
                }
            }
        }

        return a;
    }

    public static int checkRelSctIdTimeErrors(ArrayList<Sct2_RelLogicalRecord> rels) {
        int errorCount = 0;

        // SORT BY [Rel-Time]
        Comparator<Sct2_RelLogicalRecord> compBySctidTime =
                new Comparator<Sct2_RelLogicalRecord>() {
            @Override
            public int compare(Sct2_RelLogicalRecord o1, Sct2_RelLogicalRecord o2) {
                int more = 1;
                int less = -1;
                // relationship assigned sct id
                if (o1.relSctId > o2.relSctId) {
                    return more;
                } else if (o1.relSctId < o2.relSctId) {
                    return less;
                } else {
                    // time
                    if (o1.time > o2.time) {
                        return more;
                    } else if (o1.time < o2.time) {
                        return less;
                    } else {
                        return 0; // equal
                    }
                }
            }
        };

        Collections.sort(rels, compBySctidTime);

        // active rel sctids should be unique
        for (int i = 0; i < rels.size() - 1; i++) {
            Sct2_RelLogicalRecord a = rels.get(i);
            Sct2_RelLogicalRecord b = rels.get(i + 1);
            if (a.relSctId == b.relSctId) {
                // just log the first 10
                if (errorCount < 10) {
                    Logger log = Logger.getLogger(Sct2_RelLogicalRecord.class.getName());
                    log.log(Level.INFO, "::: Sct2_RelLogicalRecord.checkRelSctIdTimeErrors {0}",
                            a.relSctId);
                }
            }

        }

        return errorCount;
    }

    public static int checkRelGroupTime(ArrayList<Sct2_RelLogicalRecord> rels) {
        int errorCount = 0;

        Collections.sort(rels);
        // active rel sctids should be unique
        Sct2_RelLogicalRecord prevRel = rels.get(0);
        Sct2_RelLogicalRecord thisRel = rels.get(1);
        for (int i = 1; i < rels.size(); i++) {

            // same role group in same c1 must have the same time
            if (thisRel.group != 0
                    && thisRel.c1SnoId == prevRel.c1SnoId
                    && thisRel.group == prevRel.group
                    && thisRel.time != prevRel.time) {
                if (errorCount < 10) {
                    Logger log = Logger.getLogger(Sct2_RelLogicalRecord.class.getName());
                    log.log(Level.INFO, "::: Sct2_RelLogicalRecord.checkRelGroupTime {0}",
                            thisRel.relSctId);
                }
            }
            prevRel = thisRel;
            thisRel = rels.get(i);
        }

        return errorCount;
    }

    // SORT BY [C1-Group-RoleType-C2]
    @Override
    public int compareTo(Sct2_RelLogicalRecord o2) {
        int thisMore = 1;
        int thisLess = -1;
        // C1
        if (this.c1SnoId > o2.c1SnoId) {
            return thisMore;
        } else if (this.c1SnoId < o2.c1SnoId) {
            return thisLess;
        } else {
            // GROUP
            if (this.group > o2.group) {
                return thisMore;
            } else if (this.group < o2.group) {
                return thisLess;
            } else {
                // ROLE TYPE
                if (this.roleTypeSnoId > o2.roleTypeSnoId) {
                    return thisMore;
                } else if (this.roleTypeSnoId < o2.roleTypeSnoId) {
                    return thisLess;
                } else {
                    // C2
                    if (this.c2SnoId > o2.c2SnoId) {
                        return thisMore;
                    } else if (this.c2SnoId < o2.c2SnoId) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            }
        }
    }
}
