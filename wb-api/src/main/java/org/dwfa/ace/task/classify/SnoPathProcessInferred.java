package org.dwfa.ace.task.classify;

import java.io.IOException;
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

public class SnoPathProcessInferred implements I_ProcessConcepts {
    private List<SnoRel> snorels;
    private List<SnoCon> snocons;
    // Do not filter 'Is a' on classifier path
    // The classifier itself is a "filter" what gets dropped.

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
    private PositionSetReadOnly fromPathPosEditSide;

    // GUI
    I_ShowActivity gui;
    private Logger logger;
    private Precedence precedence;
    private I_ManageContradiction contradictionMgr;

    public SnoPathProcessInferred(Logger logger, List<SnoRel> snorels, I_IntSet roleSet,
            I_IntSet statSet, PositionSetReadOnly pathPosEditSide, PositionSetReadOnly pathPos,
            I_ShowActivity gui, Precedence precedence, I_ManageContradiction contradictionMgr)
            throws TerminologyException, IOException {
        this.logger = logger;
        this.snorels = snorels;
        this.fromPathPos = pathPos;
        this.fromPathPosEditSide = pathPosEditSide;
        this.roleTypeSet = roleSet;
        this.statusSet = statSet;
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

    // :TODO: have concept attributes for user created concepts go on the common path.

    // :TODO: then, simple this routine to not look at both the stated and inferred.

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
        else if (attribs.size() == 0) {
            // check to see if attribute is only on edit path
            attribs = concept.getConceptAttributeTuples(statusSet, fromPathPosEditSide, precedence,
                    contradictionMgr);
            if (attribs.size() == 1)
                passToCompare = true;
        }

        if (passToCompare) {
            List<? extends I_RelTuple> relTupList = concept.getSourceRelTuples(statusSet,
                    roleTypeSet, fromPathPos, precedence, contradictionMgr);

            countConAdded++;

            for (I_RelTuple rt : relTupList) {
                int authorNid = rt.getAuthorNid();
                if (authorNid != snorocketAuthorNid) 
                    continue; // SKIP IF NOT INFERRED
                
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

                if (keep == true) {
                    if (snorels != null)
                        snorels.add(new SnoRel(rt.getC1Id(), rt.getC2Id(), rt.getTypeNid(), rt
                                .getGroup(), rt.getNid()));
                    countRelAdded++;

                    if (gui != null && countRelAdded % 25000 == 0) {
                        // ** GUI: ProcessPath
                        gui.setValue(countRelAdded);
                        gui.setProgressInfoLower("rels processed " + countRelAdded);
                    }
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
