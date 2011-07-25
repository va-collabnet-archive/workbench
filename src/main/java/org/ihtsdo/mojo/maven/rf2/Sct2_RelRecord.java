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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

class Sct2_RelRecord implements Comparable<Sct2_RelRecord>, Serializable {

    private static final long serialVersionUID = 1L;
    private static final String LINE_TERMINATOR = "\r\n";
    private static final String TAB_CHARACTER = "\t";
    private static final String uuidUserStr = "f7495b58-6630-3499-a44e-2052b5fcf06c";
    private static final String uuidUserSnorocketStr = "7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9";
    // RELATIONSHIP FIELDS
    long relSnoId; // SNOMED RELATIONSHIPID, if applicable
    String effDateStr;
    long timeL;
    boolean isActive; // status is computed for relationships
    long statusConceptL; // extended from AttributeValue file
    String pathStr;
    long c1SnoId; // CONCEPTID1
    long roleTypeSnoId; // RELATIONSHIPTYPE .. SNOMED ID
    long c2SnoId; // CONCEPTID2
    int group; // RELATIONSHIPGROUP
    long characteristicL; // CHARACTERISTICTYPE
    long refinabilityL; // REFINABILITY
    boolean isInferred;

    public Sct2_RelRecord(long relID, String dateStr, boolean active, String path,
            long cOneID, long roleTypeSnoId, long cTwoID, int grp,
            long characterType, long refinibility,
            boolean inferredB,
            long statusConceptL) throws ParseException {

        this.relSnoId = relID; // RELATIONSHIPID

        this.effDateStr = dateStr;
        this.timeL = Rf2x.convertDateToTime(dateStr);
        this.isActive = active;
        /* this.pathStr = path; */
        this.pathStr = "8c230474-9f11-30ce-9cad-185a96fd03a2";

        this.c1SnoId = cOneID; // CONCEPTID1
        this.roleTypeSnoId = roleTypeSnoId; // RELATIONSHIPTYPE (SNOMED ID)
        this.c2SnoId = cTwoID; // CONCEPTID2
        this.group = grp; // RELATIONSHIPGROUP

        this.characteristicL = characterType; // CHARACTERISTICTYPE
        this.refinabilityL = refinibility; // REFINABILITY

        this.isInferred = inferredB;

        this.statusConceptL = statusConceptL;
    }

    public Sct2_RelRecord(Sct2_RelRecord in, long time, long status) throws ParseException {

        this.relSnoId = in.relSnoId; // RELATIONSHIPID

        this.effDateStr = in.effDateStr;
        this.timeL = time;
        this.isActive = in.isActive;
        this.pathStr = in.pathStr;

        this.c1SnoId = in.c1SnoId; // CONCEPTID1
        this.roleTypeSnoId = in.roleTypeSnoId; // RELATIONSHIPTYPE (SNOMED ID)
        this.c2SnoId = in.c2SnoId; // CONCEPTID2
        this.group = in.group; // RELATIONSHIPGROUP

        this.characteristicL = in.characteristicL; // CHARACTERISTICTYPE
        this.refinabilityL = in.refinabilityL; // REFINABILITY

        this.isInferred = in.isInferred;

        this.statusConceptL = status;
    }

    static Sct2_RelRecord[] attachStatus(Sct2_RelRecord[] a, Rf2_RefsetCRecord[] b) throws ParseException, MojoFailureException {
        ArrayList<Sct2_RelRecord> addedRecords = new ArrayList<Sct2_RelRecord>();
        Rf2_RefsetCRecord zeroB = new Rf2_RefsetCRecord("ZERO", "2000-01-01 00:00:00", false,
                null, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);

        Arrays.sort(a);
        Arrays.sort(b);

        int idxA = 0;
        int idxB = 0;
        long currentId = a[0].relSnoId;
        while (idxA < a.length) {
            ArrayList<Sct2_RelRecord> listA = new ArrayList<Sct2_RelRecord>();
            ArrayList<Rf2_RefsetCRecord> listB = new ArrayList<Rf2_RefsetCRecord>();
            while (idxA < a.length && a[idxA].relSnoId == currentId) {
                listA.add(a[idxA]);
                idxA++;
            }
            while (idxB < b.length && b[idxB].referencedComponentIdL == currentId) {
                listB.add(b[idxB]);
                idxB++;
            }

            // PROCESS ID
            if (listB.size() > 0) {
                if (listA.get(0).timeL < listB.get(0).timeL) {
                    listB.add(0, zeroB);
                }
                int idxAA = 0;
                int idxBB = 0;
                boolean moreToDo = true;
                while (moreToDo) {
                    // determine time range
                    long timeInAA = listA.get(idxAA).timeL;
                    long timeOutAA = Long.MAX_VALUE;
                    if (idxAA + 1 < listA.size()) {
                        timeOutAA = listA.get(idxAA + 1).timeL;
                    }
                    long timeInBB = listB.get(idxBB).timeL;
                    long timeOutBB = Long.MAX_VALUE;
                    if (idxBB + 1 < listB.size()) {
                        timeOutBB = listB.get(idxBB + 1).timeL;
                    }

                    // UPDATE VALUES
                    if (timeInAA >= timeInBB) {
                        if (listB.get(idxBB).isActive) {
                            listA.get(idxAA).statusConceptL = listB.get(idxBB).valueIdL;
                        } else {
                            // no change
                        }
                    } else {
                        if (listB.get(idxBB).isActive) {
                            addedRecords.add(new Sct2_RelRecord(listA.get(idxAA),
                                    listB.get(idxBB).timeL, listB.get(idxBB).valueIdL));
                        } else {
                            addedRecords.add(new Sct2_RelRecord(listA.get(idxAA),
                                    listB.get(idxBB).timeL, Long.MAX_VALUE));
                        }
                    }

                    // DETERMINE NEXT TO PROCESS
                    if (timeOutAA == Long.MAX_VALUE && timeOutBB == Long.MAX_VALUE) {
                        moreToDo = false;
                    } else if (timeOutAA < timeOutBB) {
                        idxAA++;
                    } else if (timeOutAA == timeOutBB) {
                        idxAA++;
                        idxBB++;
                    } else if (timeOutAA > timeOutBB) {
                        idxBB++;
                    }
                }
            }

            // NEXT ID
            if (idxA < a.length) {
                currentId = a[idxA].relSnoId;
            }
            if (idxB < b.length) {
                while (idxB < b.length && b[idxB].referencedComponentIdL < currentId) {
                    idxB++;
                }
            }
        }

        if (addedRecords.size() > 0) {
            int offsetI = a.length;
            a = Arrays.copyOf(a, a.length + addedRecords.size());
            for (int i = 0; i < addedRecords.size(); i++) {
                a[offsetI + i] = addedRecords.get(i);
            }
        }

        // REMOVE DUPLICATES
        a = removeDuplicates(a);

        return a;
    }

    static private Sct2_RelRecord[] removeDuplicates(Sct2_RelRecord[] a) throws MojoFailureException {
        Arrays.sort(a);

        // REMOVE DUPLICATES
        int lenA = a.length;
        ArrayList<Integer> duplIdxList = new ArrayList<Integer>();
        for (int idx = 0; idx < lenA - 2; idx++) {
            if ((a[idx].relSnoId == a[idx + 1].relSnoId)
                    && (a[idx].c1SnoId == a[idx + 1].c1SnoId)
                    && (a[idx].roleTypeSnoId == a[idx + 1].roleTypeSnoId)
                    && (a[idx].c2SnoId == a[idx + 1].c2SnoId)
                    && (a[idx].group == a[idx + 1].group)
                    && (a[idx].statusConceptL == a[idx + 1].statusConceptL)
                    && (a[idx].isInferred == a[idx + 1].isInferred)
                    && (a[idx].characteristicL == a[idx + 1].characteristicL)
                    && (a[idx].refinabilityL == a[idx + 1].refinabilityL)) {
                if (a[idx].statusConceptL == Long.MAX_VALUE) {
                    if (a[idx].isActive == a[idx + 1].isActive) {
                        duplIdxList.add(Integer.valueOf(idx + 1));
                    }
                } else {
                    duplIdxList.add(Integer.valueOf(idx + 1));
                }
            }
        }
        if (duplIdxList.size() > 0) {
            Sct2_RelRecord[] b = new Sct2_RelRecord[lenA - duplIdxList.size()];
            int aPos = 0;
            int bPos = 0;
            int len;
            for (int dropIdx : duplIdxList) {
                len = dropIdx - aPos;
                System.arraycopy(a, aPos, b, bPos, len);
                bPos = bPos + len;
                aPos = aPos + len + 1;
            }
            len = lenA - aPos;
            System.arraycopy(a, aPos, b, bPos, len);
            return b;
        } else {
            return a;
        }
    }

    public static Sct2_RelRecord[] parseRelationships(Rf2File f, boolean inferredB) throws IOException, ParseException {

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

        // Refinibility UUID
        // notRefinable	RF2==900000000000007000, RF1="0"
        // optional     RF2==900000000000216007, RF1="1"
        // mandatory    RF2==900000000000218008, RF1="2"
        long refinibilityId = Long.parseLong("900000000000216007");

        int idx = 0;
        br.readLine(); // Header row
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
                    refinibilityId,
                    inferredB,
                    Long.MAX_VALUE);
            idx++;
        }

        return a;
    }

    // Create string to show some input fields for exception reporting
    @Override
    public String toString() {
        return relSnoId + TAB_CHARACTER + isActive + TAB_CHARACTER
                + c1SnoId + TAB_CHARACTER + roleTypeSnoId + TAB_CHARACTER + c2SnoId + TAB_CHARACTER + group;
    }

    public void writeArf(BufferedWriter writer) throws IOException, TerminologyException, ParseException {
        // Relationship UUID
        writer.append(Rf2x.convertIdToUuidStr(relSnoId) + TAB_CHARACTER);

        // Status UUID
        if (statusConceptL < Long.MAX_VALUE) {
            writer.append(Rf2x.convertIdToUuidStr(statusConceptL) + TAB_CHARACTER);
        } else {
            writer.append(Rf2x.convertActiveToStatusUuid(isActive) + TAB_CHARACTER);
        }

        // Source Concept UUID
        writer.append(Rf2x.convertIdToUuidStr(c1SnoId) + TAB_CHARACTER);

        // Relationship Type UUID
        writer.append(Rf2x.convertIdToUuidStr(roleTypeSnoId) + TAB_CHARACTER);

        // Destination Concept UUID
        writer.append(Rf2x.convertIdToUuidStr(c2SnoId) + TAB_CHARACTER);

        // Characteristic Type UUID
        if (characteristicL >= 0) {
            writer.append(Rf2x.convertIdToUuidStr(characteristicL) + TAB_CHARACTER);
        } else { // -1 becomes  ==> (2) historical relationship
            writer.append(ArchitectonicAuxiliary.getSnomedCharacteristicType(2).getPrimoridalUid().toString()
                    + TAB_CHARACTER);
        }

        // Refinibility UUID
        // notRefinable	RF2==900000000000007000, RF1="0"
        // optional     RF2==900000000000216007, RF1="1" <--
        // mandatory    RF2==900000000000218008, RF1="2"
        writer.append(Rf2x.convertIdToUuidStr(refinabilityL) + TAB_CHARACTER);

        // Relationship Group
        writer.append(group + TAB_CHARACTER);

        // Effective Date
        writer.append(Rf2x.convertTimeToDate(timeL) + TAB_CHARACTER);

        // Path UUID
        writer.append(pathStr + TAB_CHARACTER);

        // Author UUID
        // SCT ID Is-a == 116680003L
        if (isInferred) {
            writer.append(uuidUserSnorocketStr + LINE_TERMINATOR);
        } else {
            writer.append(uuidUserStr + LINE_TERMINATOR);
        }

    }

    @Override
    public int compareTo(Sct2_RelRecord t) {
        if (this.relSnoId < t.relSnoId) {
            return -1; // instance less than received
        } else if (this.relSnoId > t.relSnoId) {
            return 1; // instance greater than received
        } else {
            if (this.timeL < t.timeL) {
                return -1; // instance less than received
            } else if (this.timeL > t.timeL) {
                return 1; // instance greater than received
            }
        }
        return 0; // instance == received
    }
}