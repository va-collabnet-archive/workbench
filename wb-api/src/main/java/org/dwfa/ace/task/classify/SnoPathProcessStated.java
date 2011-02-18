package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.Precedence;

public class SnoPathProcessStated implements I_ProcessConcepts {
    private List<SnoRel> snorels;
    private List<SnoCon> snocons;

    // STATISTICS COUNTERS
    private int countConSeen;
    private int countConRoot;
    private int countConDuplVersion;
    private int countConAdded; // ADDED TO LIST
    public int countRelAdded; // ADDED TO LIST

    private int countRelCharStated;
    private int countRelCharDefining;
    private int countRelCharStatedInferred;
    private int countRelCharStatedSubsumed;
    private int countRelCharInferred;

    // CORE CONSTANTS
    private int rootNid;
    private int isaNid;

    private static int isCh_STATED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_DEFINING_CHARACTERISTIC = Integer.MIN_VALUE;
    private static int isCh_STATED_AND_INFERRED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_STATED_AND_SUBSUMED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_INFERRED_RELATIONSHIP = Integer.MIN_VALUE;

    private static int snorocketAuthorNid = Integer.MIN_VALUE;

    private I_IntSet roleTypeSet;
    private I_IntSet statusSet;
    private PositionSetReadOnly fromPathPos;
    private PositionSetReadOnly fromPathPosPriority;

    // GUI
    I_ShowActivity gui;
    private Logger logger;
    private Precedence precedence;
    private I_ManageContradiction contradictionMgr;

    public SnoPathProcessStated(Logger logger, List<SnoCon> snocons, List<SnoRel> snorels,
            I_IntSet roleSet, I_IntSet statSet, PositionSetReadOnly pathPos,
            PositionSetReadOnly pathOverridePos, I_ShowActivity gui, Precedence precedence,
            I_ManageContradiction contradictionMgr) throws TerminologyException, IOException {

        this.fromPathPosPriority = pathOverridePos;

        this.logger = logger;
        this.snocons = snocons;
        this.snorels = snorels;
        this.fromPathPos = pathPos;
        this.roleTypeSet = roleSet;
        this.statusSet = statSet;
        //this.doNotCareIfHasSnomedIsa = doNotCareIfHasIsa;
        this.gui = gui;
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
        countRelCharStatedInferred = 0;
        countRelCharStatedSubsumed = 0;
        countRelCharInferred = 0;

        setupCoreNids();
    }

    public SnoPathProcessStated(Logger logger, List<SnoCon> snocons, List<SnoRel> snorels,
            I_IntSet roleSet, I_IntSet statSet, PositionSetReadOnly pathPos, I_ShowActivity gui,
            Precedence precedence, I_ManageContradiction contradictionMgr)
            throws TerminologyException, IOException {
        this.fromPathPosPriority = null;

        this.logger = logger;
        this.snocons = snocons;
        this.snorels = snorels;
        this.fromPathPos = pathPos;
        this.roleTypeSet = roleSet;
        this.statusSet = statSet;
        //this.doNotCareIfHasSnomedIsa = doNotCareIfHasIsa;
        this.gui = gui;
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
        countRelCharStatedInferred = 0;
        countRelCharStatedSubsumed = 0;
        countRelCharInferred = 0;

        setupCoreNids();
    }

    private void setupCoreNids() throws TerminologyException, IOException {
        I_TermFactory tf = Terms.get();

        // SETUP CORE NATIVES IDs
        isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
        rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());

        // Characteristic
        isCh_STATED_RELATIONSHIP = tf
                .uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
        isCh_DEFINING_CHARACTERISTIC = tf
                .uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
        isCh_STATED_AND_INFERRED_RELATIONSHIP = tf
                .uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP
                        .getUids());
        isCh_STATED_AND_SUBSUMED_RELATIONSHIP = tf
                .uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP
                        .getUids());
        isCh_INFERRED_RELATIONSHIP = tf
                .uuidToNative(ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.getUids());

        snorocketAuthorNid = tf.uuidToNative(ArchitectonicAuxiliary.Concept.USER.SNOROCKET
                .getUids());
    }

    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {
        // processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc)
        int cNid = concept.getNid();
        if (++countConSeen % 25000 == 0) {
            logger.info("::: [SnoPathProcess] Concepts viewed:\t" + countConSeen);
        }
        if (cNid == rootNid) {
            if (snocons != null)
                snocons.add(new SnoCon(cNid, false));

            countConAdded++;
            countConRoot++;
            return;
        }

        boolean passToCompare = false;
        List<? extends I_ConceptAttributeTuple> attribs = concept.getConceptAttributeTuples(
                statusSet, fromPathPos, precedence, contradictionMgr);

        if (attribs.size() == 1)
            passToCompare = true;
        else if (attribs.size() == 0 && fromPathPosPriority != null) {
            // check to see if attribute is only on edit path
            attribs = concept.getConceptAttributeTuples(statusSet, fromPathPosPriority, precedence,
                    contradictionMgr);
            if (attribs.size() == 1)
                passToCompare = true;
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
                if (snocons != null)
                    snocons.add(new SnoCon(cNid, attribs.get(0).isDefined()));
                countConAdded++;

                if (snorels != null)
                    snorels.addAll(snorelListA);

                countRelAdded += snorelListA.size();

                if (gui != null && countRelAdded % 25000 < snorels.size()) {
                    // ** GUI: ProcessPath
                    gui.setValue(countRelAdded);
                    gui.setProgressInfoLower("rels processed " + countRelAdded);
                }
            } // isaFound
        } // pass to compare
    }

    private List<SnoRel> tupleToSnoRel(List<? extends I_RelTuple> relTupList) {
        List<SnoRel> snoRelList = new ArrayList<SnoRel>();

        boolean isaFound = false;
        for (I_RelTuple rt : relTupList) {
            if (rt.getAuthorNid() == snorocketAuthorNid) // filter out classifier as user
                continue;

            if (rt.getTypeNid() == isaNid)
                isaFound = true;

            int charId = rt.getCharacteristicId();
            boolean keep = false;
            if (charId == isCh_DEFINING_CHARACTERISTIC) {
                keep = true;
                countRelCharDefining++;
            } else if (charId == isCh_STATED_RELATIONSHIP) {
                keep = true;
                countRelCharStated++;
            } else if (charId == isCh_STATED_AND_INFERRED_RELATIONSHIP) {
                keep = true;
                countRelCharStatedInferred++;
            } else if (charId == isCh_STATED_AND_SUBSUMED_RELATIONSHIP) {
                keep = true;
                countRelCharStatedSubsumed++;
            } else if (charId == isCh_INFERRED_RELATIONSHIP) {
                keep = true;
                countRelCharInferred++;
            }

            if (keep)
                snoRelList.add(new SnoRel(rt.getC1Id(), rt.getC2Id(), rt.getTypeNid(), rt
                        .getGroup(), rt.getNid()));
        }

        if (!isaFound)
            snoRelList.clear();

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
        
        if (snorelA.size() == 0 && snorelB_Priority.size() == 0)
            return false; // implies keep A
        if (snorelA.size() >= 0 && snorelB_Priority.size() == 0)
            return true; // implies use B priority
        if (snorelA.size() == 0 && snorelB_Priority.size() >= 0)
            return true; // implies use B priority
            
        // STATISTICS COUNTERS
        int countConSeen = 0;
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
                        if (rel_A.typeId == isaNid)
                            countSameISA++;
                        // NOTHING TO WRITE IN THIS CASE
                        if (itA.hasNext())
                            rel_A = itA.next();
                        else
                            done_A = true;
                        if (itB.hasNext())
                            rel_B = itB.next();
                        else
                            done_B = true;
                        break;

                    case 2: // REL_A > REL_B -- B has extra stuff
                        // WRITEBACK REL_B (Classifier Results) AS CURRENT
                        countB_Diff++;
                        countB_Total++;
                        if (rel_B.typeId == isaNid)
                            countB_DiffISA++;

                        if (itB.hasNext())
                            rel_B = itB.next();
                        else
                            done_B = true;
                        break;

                    case 3: // REL_A < REL_B -- A has extra stuff
                        // WRITEBACK REL_A (Classifier Input) AS RETIRED
                        // GATHER STATISTICS
                        countA_Diff++;
                        countA_Total++;
                        if (rel_A.typeId == isaNid)
                            countA_DiffISA++;

                        if (itA.hasNext())
                            rel_A = itA.next();
                        else
                            done_A = true;
                        break;
                    } // switch
                }

                // REMAINDER LIST_A GROUP 0 FOR C1
                while (rel_A.c1Id == thisC1 && rel_A.group == 0 && !done_A) {
                    countA_Diff++;
                    countA_Total++;
                    if (rel_A.typeId == isaNid)
                        countA_DiffISA++;
                    if (itA.hasNext())
                        rel_A = itA.next();
                    else
                        done_A = true;
                    break;
                }

                // REMAINDER LIST_B GROUP 0 FOR C1
                while (rel_B.c1Id == thisC1 && rel_B.group == 0 && !done_B) {
                    countB_Diff++;
                    countB_Total++;
                    if (rel_B.typeId == isaNid)
                        countB_DiffISA++;
                    if (itB.hasNext())
                        rel_B = itB.next();
                    else
                        done_B = true;
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
                    if (itA.hasNext())
                        rel_A = itA.next();
                    else
                        done_A = true;
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
                    if (itB.hasNext())
                        rel_B = itB.next();
                    else
                        done_B = true;
                }

                // FIND GROUPS IN GROUPLIST_A WITHOUT AN EQUAL IN GROUPLIST_B
                // WRITE THESE GROUPED RELS AS "RETIRED"
                SnoGrpList groupList_NotEqual;
                if (groupList_A.size() > 0) {
                    groupList_NotEqual = groupList_A.whichNotEqual(groupList_B);
                    for (SnoGrp sg : groupList_NotEqual)
                        for (SnoRel sr_A : sg)
                            countA_Total += groupList_A.countRels();
                    countA_Diff += groupList_NotEqual.countRels();
                }

                // FIND GROUPS IN GROUPLIST_B WITHOUT AN EQUAL IN GROUPLIST_A
                // WRITE THESE GROUPED RELS AS "NEW, CURRENT"
                if (groupList_B.size() > 0) {
                    groupList_NotEqual = groupList_B.whichNotEqual(groupList_A);
                    for (SnoGrp sg : groupList_NotEqual)
                        for (SnoRel sr_B : sg)
                            countB_Total += groupList_A.countRels();
                    countB_Diff += groupList_NotEqual.countRels();
                }
            } else if (rel_A.c1Id > rel_B.c1Id) {
                // CASE 2: LIST_B HAS CONCEPT NOT IN LIST_A
                // COMPLETELY *ADD* ALL THIS C1 FOR REL_B AS NEW, CURRENT
                int thisC1 = rel_B.c1Id;
                while (rel_B.c1Id == thisC1) {
                    countB_Diff++;
                    countB_Total++;
                    if (rel_B.typeId == isaNid)
                        countB_DiffISA++;
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
                    if (rel_A.typeId == isaNid)
                        countA_DiffISA++;
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
            if (rel_A.typeId == isaNid)
                countA_DiffISA++;
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
            if (rel_B.typeId == isaNid)
                countB_DiffISA++;
            // COMPLETELY UPDATE ALL REMAINING REL_B AS NEW, CURRENT
            if (itB.hasNext()) {
                rel_B = itB.next();
            } else {
                done_B = true;
                break;
            }
        }

        if (countA_Diff > 0 || countA_DiffISA > 0 || countB_DiffISA > 0 || countB_DiffISA > 0)
            return true; // Use B... priority
        else
            return false; // Use A... baseline
    }

    // STATS FROM PROCESS CONCEPTS (CLASSIFIER INPUT)
    public String getStats(long startTime) {
        StringBuffer s = new StringBuffer(1500);
        s.append("\r\n::: [SnoPathProcess] ProcessPath()");
        if (startTime > 0) {
            long lapseTime = System.currentTimeMillis() - startTime;
            s.append("\r\n::: [Time] get vodb data: \t" + lapseTime + "\t(mS)\t"
                    + (((float) lapseTime / 1000) / 60) + "\t(min)");
            s.append("\r\n:::");
        }

        s.append("\r\n::: concepts viewed:       \t" + countConSeen);
        s.append("\r\n::: concepts added:        \t" + countConAdded);
        s.append("\r\n::: relationships added:   \t" + countRelAdded);
        s.append("\r\n:::");

        s.append("\r\n::: ");
        s.append("\r\n::: concept root added:  \t" + countConRoot);
        s.append("\r\n::: con version conflict:\t" + countConDuplVersion
                + "\t # attribs.size() > 1");
        s.append("\r\n::: ");
        s.append("\r\n::: Defining:         \t" + countRelCharDefining);
        s.append("\r\n::: Stated:           \t" + countRelCharStated);
        s.append("\r\n::: Stated & Inferred:\t" + countRelCharStatedInferred);
        s.append("\r\n::: Stated & Subsumed:\t" + countRelCharStatedSubsumed);
        s.append("\r\n::: Inferred:         \t" + countRelCharInferred);
        int total = countRelCharStated + countRelCharDefining + countRelCharStatedInferred
                + countRelCharStatedSubsumed + countRelCharInferred;
        s.append("\r\n:::            TOTAL=\t" + total);

        s.append("\r\n::: ");
        s.append("\r\n");
        return s.toString();
    }
}
