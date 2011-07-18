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
package org.ihtsdo.mojo.maven.classifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.maven.plugin.logging.Log;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.classify.SnoGrp;
import org.dwfa.ace.task.classify.SnoGrpList;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.example.binding.SnomedMetadataRfx;

public class SnoPathProcessStatedCycleCheck implements I_ProcessConcepts {

    private List<SnoRel> snorels;
    // STATISTICS COUNTERS
    private int countConSeen;
    private int countConRoot;
    private int countConDuplVersion;
    private int countConAdded; // ADDED TO LIST
    public int countRelAdded; // ADDED TO LIST
    private int countRelCharStated;
    private int countRelCharDefining;
    private int countRelCharInferred;
    // CORE CONSTANTS
    private int rootNid;
    private int isaNid;
    private static int isCh_STATED_RELATIONSHIP = Integer.MAX_VALUE;
    private static int isCh_DEFINING_CHARACTERISTIC = Integer.MAX_VALUE;
    private static int isCh_INFERRED_RELATIONSHIP = Integer.MAX_VALUE;
    private static int snorocketAuthorNid = Integer.MAX_VALUE;
    private I_IntSet roleTypeSet;
    private I_IntSet statusSet;
    private PositionSetReadOnly fromPathPos;
    private PositionSetReadOnly fromPathPosPriority;
    // WORKBENCH
    private Log logger;
    private Precedence precedence;
    private I_ManageContradiction contradictionMgr;
    private ConcurrentHashMap<Integer, UUID> watchList = null;

    public void addWatchConcept(UUID uuid) throws TerminologyException, IOException {
        if (watchList == null) {
            watchList = new ConcurrentHashMap<Integer, UUID>();
        }
        I_TermFactory tf = Terms.get();
        Integer nid = Integer.valueOf(tf.uuidToNative(uuid));
        watchList.put(nid, uuid);
    }

    public SnoPathProcessStatedCycleCheck(
            Log logger,
            List<SnoRel> snorels,
            I_IntSet roleSet,
            I_IntSet statSet,
            PositionSetReadOnly pathPos,
            Precedence precedence,
            I_ManageContradiction contradictionMgr)
            throws Exception {
        this.logger = logger;
        this.snorels = snorels;
        this.fromPathPosPriority = null;

        this.fromPathPos = pathPos;
        this.roleTypeSet = roleSet;
        this.statusSet = statSet;
        //this.doNotCareIfHasSnomedIsa = doNotCareIfHasIsa;
        this.precedence = precedence;
        this.contradictionMgr = contradictionMgr;

        // STATISTICS COUNTERS
        countConSeen = 0;
        countConRoot = 0;
        countConDuplVersion = 0;
        countConAdded = 0; // ADDED TO SNOROCKET
        countRelAdded = 0; // ADDED TO SNOROCKET

        countRelCharStated = 0;
        countRelCharDefining = 0;
        countRelCharInferred = 0;

        setupCoreNids();
        SnoTable.updatePrefs(false);
    }

    private void setupCoreNids() throws Exception {
        I_TermFactory tf = Terms.get();

        // SETUP CORE NATIVES IDs
        isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
        rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());

        // Characteristic
        isCh_STATED_RELATIONSHIP = SnomedMetadataRfx.getCh_STATED_RELATIONSHIP_NID();
        isCh_DEFINING_CHARACTERISTIC = SnomedMetadataRfx.getCh_DEFINING_CHARACTERISTIC_NID();
        isCh_INFERRED_RELATIONSHIP = SnomedMetadataRfx.getCh_INFERRED_RELATIONSHIP_NID();

        snorocketAuthorNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.USER.SNOROCKET.getUids());
    }

    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {
        // processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc)
        int cNid = concept.getNid();

        if (++countConSeen % 25000 == 0) {
            if (logger != null) {
                logger.info("::: [SnoPathProcessStatedCycleCheck] Concepts viewed:\t" + countConSeen);
            }
        }
        if (cNid == rootNid) {
            countConAdded++;
            countConRoot++;
            return;
        }

        boolean passToCompare = false;
        List<? extends I_ConceptAttributeTuple> attribs = concept.getConceptAttributeTuples(
                statusSet, fromPathPos, precedence, contradictionMgr);

        if (attribs.size() == 1) {
            passToCompare = true;
        } else if (attribs.isEmpty() && fromPathPosPriority != null) {
            // check to see if attribute is only on edit path
            attribs = concept.getConceptAttributeTuples(statusSet, fromPathPosPriority, precedence,
                    contradictionMgr);
            if (attribs.size() == 1) {
                passToCompare = true;
            }
        }

        if (passToCompare) {
            List<? extends I_RelTuple> relTupList = concept.getSourceRelTuples(statusSet,
                    roleTypeSet, fromPathPos, precedence, contradictionMgr);

            List<SnoRel> snorelListA = tupleToSnoRel(relTupList);

            if (fromPathPosPriority != null) {
                List<? extends I_RelTuple> relTupListPriority = concept.getSourceRelTuples(
                        statusSet, roleTypeSet, fromPathPosPriority, precedence, contradictionMgr);
                List<SnoRel> snorelListB = tupleToSnoRel(relTupListPriority);

                boolean usePriority = usePriorityListTest(snorelListA, snorelListB);
                if (usePriority) {
                    snorelListA = snorelListB;
                    relTupList = relTupListPriority;
                }
                relTupListPriority = null;
                snorelListB = null;
            }

            if (snorelListA.size() > 0) { // "Is a" found if size > 0
                countConAdded++;
                countRelAdded += snorelListA.size();

                for (int i = 0; i < snorelListA.size(); i++) {
                    if (snorelListA.get(i).typeId == isaNid) {
                        if (SnoTable.findIsaCycle(cNid, isaNid, snorelListA.get(i).c2Id, true)) {
                            snorels.add(snorelListA.get(i));
                        }
                    }
                }

            } // isaFound
        } // pass to compare
    }

    private List<SnoRel> tupleToSnoRel(List<? extends I_RelTuple> relTupList) throws Exception {
        List<SnoRel> snoRelList = new ArrayList<SnoRel>();

        boolean isaFound = false;
        int c1 = Integer.MAX_VALUE;
        for (I_RelTuple rt : relTupList) {
            c1 = rt.getC1Id();
            if (rt.getAuthorNid() == snorocketAuthorNid) // filter out classifier as user
            {
                continue;
            }

            if (rt.getTypeNid() == isaNid) {
                isaFound = true;
            }

            int charId = rt.getCharacteristicId();
            boolean keep = false;
            if (charId == isCh_DEFINING_CHARACTERISTIC) {
                keep = true;
                countRelCharDefining++;
            } else if (charId == isCh_STATED_RELATIONSHIP) {
                keep = true;
                countRelCharStated++;
            } else if (charId == isCh_INFERRED_RELATIONSHIP) {
                keep = true;
                countRelCharInferred++;
            }

            if (keep) {
                snoRelList.add(new SnoRel(rt.getC1Id(), rt.getC2Id(), rt.getTypeNid(), rt.getGroup(), rt.getNid()));
            }
        }

        if (!isaFound) {
            snoRelList.clear();

            if (watchList != null) {
                if (watchList.containsKey(Integer.valueOf(c1))) {
                    throw new Exception("::: Relationship Exception -- 'Is a' relationship not found");
                }
            }
        }

        return snoRelList;
    }

    private int compareSnoRel(SnoRel inR, SnoRel outR) {
        if ((inR.c1Id == outR.c1Id) && (inR.group == outR.group) && (inR.typeId == outR.typeId)
                && (inR.c2Id == outR.c2Id)) {
            return 1; // SAME
        } else if (inR.c1Id > outR.c1Id) {
            return 2; // ADDED
        } else if ((inR.c1Id == outR.c1Id) && (inR.group > outR.group)) {
            return 2; // ADDED
        } else if ((inR.c1Id == outR.c1Id) && (inR.group == outR.group)
                && (inR.typeId > outR.typeId)) {
            return 2; // ADDED
        } else if ((inR.c1Id == outR.c1Id) && (inR.group == outR.group)
                && (inR.typeId == outR.typeId) && (inR.c2Id > outR.c2Id)) {
            return 2; // ADDED
        } else {
            return 3; // DROPPED
        }
    } // compareSnoRel

    private boolean usePriorityListTest(List<SnoRel> snorelA, List<SnoRel> snorelB_Priority)
            throws TerminologyException, IOException {

        if (snorelA.isEmpty() && snorelB_Priority.isEmpty()) {
            return false; // implies keep A
        }
        if (snorelA.size() >= 0 && snorelB_Priority.isEmpty()) {
            return true; // implies use B priority
        }
        if (snorelA.isEmpty() && snorelB_Priority.size() >= 0) {
            return true; // implies use B priority
        }
        // STATISTICS COUNTERS
        int countSame = 0;
        int countSameISA = 0;
        int countA_Diff = 0;
        int countA_DiffISA = 0;
        int countA_Total = 0;
        int countB_Diff = 0;
        int countB_DiffISA = 0;
        int countB_Total = 0;

        Collections.sort(snorelA);
        Collections.sort(snorelB_Priority);

        // Typically, A is the Classifier Path (for previously inferred)
        // Typically, B is the SnoRocket Results Set (for newly inferred)
        Iterator<SnoRel> itA = snorelA.iterator();
        Iterator<SnoRel> itB = snorelB_Priority.iterator();
        SnoRel rel_A = itA.next();
        SnoRel rel_B = itB.next();
        boolean done_A = false;
        boolean done_B = false;

        // BY SORT ORDER, LOWER NUMBER ADVANCES FIRST
        while (!done_A && !done_B) {

            if (rel_A.c1Id == rel_B.c1Id) {
                // COMPLETELY PROCESS ALL C1 FOR BOTH IN & OUT
                // PROCESS C1 WITH GROUP == 0
                int thisC1 = rel_A.c1Id;

                // PROCESS WHILE BOTH HAVE GROUP 0
                while (rel_A.c1Id == thisC1 && rel_B.c1Id == thisC1 && rel_A.group == 0
                        && rel_B.group == 0 && !done_A && !done_B) {

                    // PROGESS GROUP ZERO
                    switch (compareSnoRel(rel_A, rel_B)) {
                        case 1: // SAME
                            // GATHER STATISTICS
                            countSame++;
                            countA_Total++;
                            countB_Total++;
                            if (rel_A.typeId == isaNid) {
                                countSameISA++;
                            }
                            // NOTHING TO WRITE IN THIS CASE
                            if (itA.hasNext()) {
                                rel_A = itA.next();
                            } else {
                                done_A = true;
                            }
                            if (itB.hasNext()) {
                                rel_B = itB.next();
                            } else {
                                done_B = true;
                            }
                            break;

                        case 2: // REL_A > REL_B -- B has extra stuff
                            // WRITEBACK REL_B (Classifier Results) AS CURRENT
                            countB_Diff++;
                            countB_Total++;
                            if (rel_B.typeId == isaNid) {
                                countB_DiffISA++;
                            }

                            if (itB.hasNext()) {
                                rel_B = itB.next();
                            } else {
                                done_B = true;
                            }
                            break;

                        case 3: // REL_A < REL_B -- A has extra stuff
                            // WRITEBACK REL_A (Classifier Input) AS RETIRED
                            // GATHER STATISTICS
                            countA_Diff++;
                            countA_Total++;
                            if (rel_A.typeId == isaNid) {
                                countA_DiffISA++;
                            }

                            if (itA.hasNext()) {
                                rel_A = itA.next();
                            } else {
                                done_A = true;
                            }
                            break;
                    } // switch
                }

                // REMAINDER LIST_A GROUP 0 FOR C1
                while (rel_A.c1Id == thisC1 && rel_A.group == 0 && !done_A) {
                    countA_Diff++;
                    countA_Total++;
                    if (rel_A.typeId == isaNid) {
                        countA_DiffISA++;
                    }
                    if (itA.hasNext()) {
                        rel_A = itA.next();
                    } else {
                        done_A = true;
                    }
                    break;
                }

                // REMAINDER LIST_B GROUP 0 FOR C1
                while (rel_B.c1Id == thisC1 && rel_B.group == 0 && !done_B) {
                    countB_Diff++;
                    countB_Total++;
                    if (rel_B.typeId == isaNid) {
                        countB_DiffISA++;
                    }
                    if (itB.hasNext()) {
                        rel_B = itB.next();
                    } else {
                        done_B = true;
                    }
                    break;
                }

                // ** SEGMENT GROUPS **
                SnoGrpList groupList_A = new SnoGrpList();
                SnoGrpList groupList_B = new SnoGrpList();
                SnoGrp groupA = null;
                SnoGrp groupB = null;

                // SEGMENT GROUPS IN LIST_A
                int prevGroup = Integer.MIN_VALUE;
                while (rel_A.c1Id == thisC1 && !done_A) {
                    if (rel_A.group != prevGroup) {
                        groupA = new SnoGrp();
                        groupList_A.add(groupA);
                    }

                    groupA.add(rel_A);

                    prevGroup = rel_A.group;
                    if (itA.hasNext()) {
                        rel_A = itA.next();
                    } else {
                        done_A = true;
                    }
                }
                // SEGMENT GROUPS IN LIST_B
                prevGroup = Integer.MIN_VALUE;
                while (rel_B.c1Id == thisC1 && !done_B) {
                    if (rel_B.group != prevGroup) {
                        groupB = new SnoGrp();
                        groupList_B.add(groupB);
                    }

                    groupB.add(rel_B);

                    prevGroup = rel_B.group;
                    if (itB.hasNext()) {
                        rel_B = itB.next();
                    } else {
                        done_B = true;
                    }
                }

                // FIND GROUPS IN GROUPLIST_A WITHOUT AN EQUAL IN GROUPLIST_B
                // WRITE THESE GROUPED RELS AS "RETIRED"
                SnoGrpList groupList_NotEqual;
                if (groupList_A.size() > 0) {
                    groupList_NotEqual = groupList_A.whichNotEqual(groupList_B);
                    for (SnoGrp sg : groupList_NotEqual) {
                        for (SnoRel sr_A : sg) {
                            countA_Total += groupList_A.countRels();
                        }
                    }
                    countA_Diff += groupList_NotEqual.countRels();
                }

                // FIND GROUPS IN GROUPLIST_B WITHOUT AN EQUAL IN GROUPLIST_A
                // WRITE THESE GROUPED RELS AS "NEW, CURRENT"
                if (groupList_B.size() > 0) {
                    groupList_NotEqual = groupList_B.whichNotEqual(groupList_A);
                    for (SnoGrp sg : groupList_NotEqual) {
                        for (SnoRel sr_B : sg) {
                            countB_Total += groupList_A.countRels();
                        }
                    }
                    countB_Diff += groupList_NotEqual.countRels();
                }
            } else if (rel_A.c1Id > rel_B.c1Id) {
                // CASE 2: LIST_B HAS CONCEPT NOT IN LIST_A
                // COMPLETELY *ADD* ALL THIS C1 FOR REL_B AS NEW, CURRENT
                int thisC1 = rel_B.c1Id;
                while (rel_B.c1Id == thisC1) {
                    countB_Diff++;
                    countB_Total++;
                    if (rel_B.typeId == isaNid) {
                        countB_DiffISA++;
                    }
                    if (itB.hasNext()) {
                        rel_B = itB.next();
                    } else {
                        done_B = true;
                        break;
                    }
                }

            } else {
                // CASE 3: LIST_A HAS CONCEPT NOT IN LIST_B
                // COMPLETELY *RETIRE* ALL THIS C1 FOR REL_A
                int thisC1 = rel_A.c1Id;
                while (rel_A.c1Id == thisC1) {
                    countA_Diff++;
                    countA_Total++;
                    if (rel_A.typeId == isaNid) {
                        countA_DiffISA++;
                    }
                    if (itA.hasNext()) {
                        rel_A = itA.next();
                    } else {
                        done_A = true;
                        break;
                    }
                }
            }
        }

        // AT THIS POINT, THE PREVIOUS C1 HAS BE PROCESSED COMPLETELY
        // AND, EITHER REL_A OR REL_B HAS BEEN COMPLETELY PROCESSED
        // AND, ANY REMAINDER IS ONLY ON REL_LIST_A OR ONLY ON REL_LIST_B
        // AND, THAT REMAINDER HAS A "STANDALONE" C1 VALUE
        // THEREFORE THAT REMAINDER WRITEBACK COMPLETELY
        // AS "NEW CURRENT" OR "OLD RETIRED"
        //
        // LASTLY, IF .NOT.DONE_A THEN THE NEXT REL_A IN ALREADY IN PLACE
        while (!done_A) {
            countA_Diff++;
            countA_Total++;
            if (rel_A.typeId == isaNid) {
                countA_DiffISA++;
            }
            // COMPLETELY UPDATE ALL REMAINING REL_A AS RETIRED
            if (itA.hasNext()) {
                rel_A = itA.next();
            } else {
                done_A = true;
                break;
            }
        }

        while (!done_B) {
            countB_Diff++;
            countB_Total++;
            if (rel_B.typeId == isaNid) {
                countB_DiffISA++;
            }
            // COMPLETELY UPDATE ALL REMAINING REL_B AS NEW, CURRENT
            if (itB.hasNext()) {
                rel_B = itB.next();
            } else {
                done_B = true;
                break;
            }
        }

        if (countA_Diff > 0 || countA_DiffISA > 0 || countB_DiffISA > 0 || countB_DiffISA > 0) {
            return true; // Use B... priority
        } else {
            return false; // Use A... baseline
        }
    }

    // STATS FROM PROCESS CONCEPTS (CLASSIFIER INPUT)
    public String getStats(long startTime) {
        StringBuilder s = new StringBuilder(1500);
        s.append("\r\n::: [SnoPathProcessStatedCycleCheck] getStats()");
        if (startTime > 0) {
            long lapseTime = System.currentTimeMillis() - startTime;
            s.append("\r\n::: [Time] get vodb data: \t").append(lapseTime).append("\t(mS)\t");
            s.append(((float) lapseTime / 1000) / 60).append("\t(min)");
            s.append("\r\n:::");
        }

        s.append("\r\n::: concepts viewed:       \t").append(countConSeen);
        s.append("\r\n::: concepts added:        \t").append(countConAdded);
        s.append("\r\n::: relationships added:   \t").append(countRelAdded);
        s.append("\r\n:::");

        s.append("\r\n::: ");
        s.append("\r\n::: concept root added:  \t").append(countConRoot);
        s.append("\r\n::: con version conflict:\t").append(countConDuplVersion);
        s.append("\t # attribs.size() > 1");
        s.append("\r\n::: ");
        s.append("\r\n::: Defining/Inferred: \t").append(countRelCharDefining);
        s.append("\r\n::: Stated:            \t").append(countRelCharStated);
        s.append("\r\n::: Inferred:         \t").append(countRelCharInferred);
        int total = countRelCharStated + countRelCharDefining + countRelCharInferred;
        s.append("\r\n:::            TOTAL=\t").append(total);

        s.append("\r\n::: ");
        s.append("\r\n");
        return s.toString();
    }
}
