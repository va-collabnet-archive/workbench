package org.dwfa.ace.task.classify;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

public class SnoPathProcessExInferred implements I_ProcessConcepts {

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
    private int countRelCharInferred;
    // CORE CONSTANTS
    private int rootNid;
    private int isaNid;
    private static int isCh_INFERRED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int snorocketAuthorNid = Integer.MIN_VALUE;
    private I_IntSet roleTypeSet;
    private I_IntSet statusSet;
    private I_IntSet statusSetPlusInactive = null; // NOTE: Concept may have been retired as a stated edit
    private PositionSetReadOnly fromPathPos;
    // GUI
    I_ShowActivity gui;
    private Logger logger;
    private Precedence precedence;
    private ContradictionManagerBI contradictionMgr;
    private LinkedHashMap<Integer, Integer> charMap;

    public SnoPathProcessExInferred(Logger logger, List<SnoRel> snorels, I_IntSet roleSet,
            I_IntSet statSet, PositionSetReadOnly pathPos,
            I_ShowActivity gui, Precedence precedence, ContradictionManagerBI contradictionMgr)
            throws Exception {
        this.logger = logger;
        this.snorels = snorels;
        this.fromPathPos = pathPos;
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

        countRelCharInferred = 0;

        setupCoreNids();

        charMap = new LinkedHashMap<Integer, Integer>(); // SCTID, COUNT

        this.statusSetPlusInactive = Terms.get().newIntSet();
        this.statusSetPlusInactive.add(SnomedMetadataRfx.getSTATUS_CURRENT_NID());
        this.statusSetPlusInactive.add(SnomedMetadataRfx.getSTATUS_LIMITED_NID());
        this.statusSetPlusInactive.add(SnomedMetadataRfx.getSTATUS_RETIRED_NID());
        this.statusSetPlusInactive.add(SnomedMetadataRfx.getSTATUS_AMBIGUOUS_NID());
        this.statusSetPlusInactive.add(SnomedMetadataRfx.getSTATUS_DUPLICATE_NID());
        this.statusSetPlusInactive.add(SnomedMetadataRfx.getSTATUS_ERRONEOUS_NID());
        this.statusSetPlusInactive.add(SnomedMetadataRfx.getSTATUS_LIMITED_NID());
        this.statusSetPlusInactive.add(SnomedMetadataRfx.getSTATUS_OUTDATED_NID());
    }

    private void setupCoreNids() throws Exception {
        I_TermFactory tf = Terms.get();

        // SETUP CORE NATIVES IDs
        isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
        rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());

        // Characteristic
        isCh_INFERRED_RELATIONSHIP = SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID();

        snorocketAuthorNid = tf.uuidToNative(Concept.SNOROCKET.getUids());
    }

    // :TODO: have concept attributes for user created concepts go on the common path.
    // :TODO: then, simple this routine to not look at both the stated and inferred.
    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {
        // processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc)
        int cNid = concept.getNid();
        if (++countConSeen % 25000 == 0 && logger != null) {
            logger.log(Level.INFO, "::: [SnoPathProcessEx] Concepts viewed:\t{0}", countConSeen);
        }
        if (cNid == rootNid) {
            if (snocons != null) {
                snocons.add(new SnoCon(cNid, false));
            }

            countConAdded++;
            countConRoot++;
            return;
        }

        boolean passToCompare = false;
        List<? extends I_ConceptAttributeTuple> attribs = concept.getConceptAttributeTuples(
                statusSetPlusInactive, fromPathPos, precedence, contradictionMgr);

        if (attribs.size() >= 1) {
            passToCompare = true;
        }

        if (passToCompare) {
            List<? extends I_RelTuple> relTupList = concept.getSourceRelTuples(statusSet,
                    roleTypeSet, fromPathPos, precedence, contradictionMgr);

            countConAdded++;

            for (I_RelTuple rt : relTupList) {
                // :SNOOWL: handle multiple classifier case.
                if (DescriptionLogic.isVisible()) {
                    int authorNid = rt.getAuthorNid();
                    if (authorNid != snorocketAuthorNid) {
                        continue; // SKIP IF NOT INFERRED BY SNOROCKET
                    }
                }
                int charId = rt.getCharacteristicId();
                boolean keep = false;
                if (charId == isCh_INFERRED_RELATIONSHIP) {
                    keep = true;
                    countRelCharInferred++;
                } else {
                    Integer count = charMap.get(charId);
                    if (charMap.get(charId) == null) {
                        charMap.put(charId, new Integer(0));
                    } else {
                        count += 1;
                        charMap.put(charId, count);
                    }
                }

                if (keep == true) {
                    if (snorels != null) {
                        snorels.add(new SnoRel(rt.getC1Id(), rt.getC2Id(), rt.getTypeNid(),
                                rt.getGroup(), rt.getNid()));
                    }
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
        StringBuilder s = new StringBuilder(1500);
        s.append("\r\n::: [SnoPathProcessEx] ProcessPath()");
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
        s.append("\r\n::: Inferred:           \t").append(countRelCharInferred);

        s.append("\r\n::: ");
        s.append("\r\n");

        Set<Integer> ks = charMap.keySet();
        for (Integer keyInteger : ks) {
            s.append("\r\n::: Other char type: \t").append(keyInteger);
            s.append("\tcount=\t").append(charMap.get(keyInteger));
        }
        s.append("\r\n");
        return s.toString();
    }
}
