package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.tapi.TerminologyException;

public class SnoPathProcessConcepts implements I_ProcessConcepts {
    private int rootNid;
    private int isaNid;
    private I_IntSet roleTypeSet;
    private I_IntSet statusSet;
    private PositionSetReadOnly fromPathPos;
    private Logger logger;
    private List<SnoRel> snorels;
    private List<SnoCon> snocons;
    // Do not filter 'Is a' on classifier path
    // The classifier itself is a "filter" what gets dropped.
    private boolean doNotCareIfHasSnomedIsa;

    // STATISTICS COUNTERS
    private int countConSeen = Integer.MIN_VALUE;
    private int countConRoot = Integer.MIN_VALUE;
    private int countConDuplVersion = Integer.MIN_VALUE;
    private int countSnoCon = Integer.MIN_VALUE;
    private int countConAdded = Integer.MIN_VALUE; // ADDED TO SNOROCKET
    public int countRelAdded = Integer.MIN_VALUE; // ADDED TO SNOROCKET
    private int countRelAddedGroups = Integer.MIN_VALUE; // Count rels with
    // non-zero group
    private int countRelDuplVersion = Integer.MIN_VALUE; // SAME PATH, SAME
    // VERSION

    private int countRelCharStated = Integer.MIN_VALUE;
    private int countRelCharDefining = Integer.MIN_VALUE;
    private int countRelCharStatedInferred = Integer.MIN_VALUE;
    private int countRelCharStatedSubsumed = Integer.MIN_VALUE;

    private int countRelRefNot = Integer.MIN_VALUE;
    private int countRelRefOpt = Integer.MIN_VALUE;
    private int countRelRefMand = Integer.MIN_VALUE;

    // CORE NID CONSTANTS

    public SnoPathProcessConcepts(Logger logger, List<SnoCon> snocons, List<SnoRel> snorels,
            int rootNid, int isaNid, I_IntSet roleSet, I_IntSet statSet,
            PositionSetReadOnly pathPos, I_ShowActivity gui, boolean doNotCareIfHasIsa)
            throws TerminologyException, IOException {
        this.logger = logger;
        this.snocons = snocons;
        this.snorels = snorels;
        this.rootNid = rootNid;
        this.isaNid = isaNid;
        this.fromPathPos = pathPos;
        this.roleTypeSet = roleSet;
        this.statusSet = statSet;
        this.doNotCareIfHasSnomedIsa = doNotCareIfHasIsa;

        // STATISTICS COUNTERS
        countConSeen = 0;
        countConRoot = 0;
        countConDuplVersion = 0;
        countSnoCon = 0;
        countConAdded = 0; // ADDED TO SNOROCKET
        countRelAdded = 0; // ADDED TO SNOROCKET
        countRelAddedGroups = 0; // Count rels with
        // non-zero group
        countRelDuplVersion = 0; // SAME PATH, SAME
        // VERSION

        countRelCharStated = 0;
        countRelCharDefining = 0;
        countRelCharStatedInferred = 0;
        countRelCharStatedSubsumed = 0;

        countRelRefNot = 0;
        countRelRefOpt = 0;
        countRelRefMand = 0;
    }

    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {
        // processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc)
        // Concept concept = fcfc.fetch();
        // I_GetConceptData concept = fcfc.fetch();
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

        // status, position, addUncommitted, returnConflictResolvedLatestState
        // List<Version> attribs = concept.getConceptAttributeTuples(statusSet,
        // fromPathPos, false, true);
        List<? extends I_ConceptAttributeTuple> attribs = concept.getConceptAttributeTuples(
                statusSet, fromPathPos, false, true);

        if (attribs.size() == 1) {
            // status, types, positions, addUncommitted,
            // returnConflictResolvedLatestState
            List<? extends I_RelTuple> relTupList = concept.getSourceRelTuples(statusSet,
                    roleTypeSet, fromPathPos, false, true);

            // ComponentList<Relationship> rels = concept.getSourceRels();
            boolean isaFound = false;
            if (doNotCareIfHasSnomedIsa)
                isaFound = true;
            else
                for (I_RelTuple rt : relTupList)
                    if (rt.getTypeId() == isaNid)
                        isaFound = true;

            if (isaFound || doNotCareIfHasSnomedIsa) {
                if (snocons != null)
                    snocons.add(new SnoCon(cNid, false));
                countConAdded++;

                for (I_RelTuple rt : relTupList) {
                    if (snorels != null)
                        snorels.add(new SnoRel(rt.getC1Id(), rt.getC2Id(), rt.getTypeId(), rt
                                .getGroup(), countRelAdded));
                    countRelAdded++;
                }
            }
        } else if (attribs.size() > 1) {
            countConDuplVersion++;
        }
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

        s.append("\r\n::: concepts viewed:      \t" + countConSeen);
        s.append("\r\n::: concepts total current:\t" + countSnoCon);
        s.append("\r\n::: concepts w/rel added:  \t" + countConAdded);
        s.append("\r\n::: relationships added:   \t" + countRelAdded);
        s.append("\r\n:::");
        s.append("\r\n::: rel group TOTAL:\t" + countRelAddedGroups);

        s.append("\r\n::: ");
        s.append("\r\n::: concept root added:  \t" + countConRoot);
        s.append("\r\n::: con version conflict:\t" + countConDuplVersion);
        s.append("\r\n::: rel version conflict:\t" + countRelDuplVersion);
        s.append("\r\n::: ");
        s.append("\r\n::: Defining:         \t" + countRelCharDefining);
        s.append("\r\n::: Stated:           \t" + countRelCharStated);
        s.append("\r\n::: Stated & Inferred:\t" + countRelCharStatedInferred);
        s.append("\r\n::: Stated & Subsumed:\t" + countRelCharStatedSubsumed);
        int total = countRelCharStated + countRelCharDefining + countRelCharStatedInferred
                + countRelCharStatedSubsumed;
        s.append("\r\n:::            TOTAL=\t" + total);
        s.append("\r\n::: ");
        s.append("\r\n::: Optional Refinability: \t" + countRelRefOpt);
        s.append("\r\n::: Not Refinable:         \t" + countRelRefNot);
        s.append("\r\n::: Mandatory Refinability:\t" + countRelRefMand);
        total = countRelRefNot + countRelRefOpt + countRelRefMand;
        s.append("\r\n:::                  TOTAL=\t" + total);
        s.append("\r\n::: ");
        s.append("\r\n");
        return s.toString();
    }
}
