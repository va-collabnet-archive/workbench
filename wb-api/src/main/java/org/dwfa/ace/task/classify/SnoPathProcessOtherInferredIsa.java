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
package org.dwfa.ace.task.classify;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.ihtsdo.task.ExcludeFromIteration;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.helper.descriptionlogic.DescriptionLogic;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 *
 * @author marc
 */

public class SnoPathProcessOtherInferredIsa implements I_ProcessConcepts {

    private List<SnoRel> snorels;
    private List<SnoCon> snocons;
    // Do not filter 'Is a' on classifier path
    // The classifier itself is a "filter" what gets dropped.
    // STATISTICS COUNTERS
    private int skippedForPath;
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
    private int skipThisAuthorNid = Integer.MIN_VALUE;
    private I_IntSet roleTypeSet;
    private I_IntSet statusSet;
    private I_IntSet statusSetInactive = null; // NOTE: Concept may have been retired as a stated edit
    private PositionSetReadOnly fromPathPos;
    // GUI
    I_ShowActivity gui;
    private Logger logger;
    private Precedence precedence;
    private ContradictionManagerBI contradictionMgr;

    public SnoPathProcessOtherInferredIsa(Logger logger, List<SnoRel> snorels, I_IntSet roleSet,
            I_IntSet statSet, PositionSetReadOnly pathPos,
            I_ShowActivity gui, Precedence precedence, ContradictionManagerBI contradictionMgr,
            int skipThisAuthorNid)
            throws Exception {
        this.logger = logger;
        this.snorels = snorels;
        this.fromPathPos = pathPos;
        this.roleTypeSet = roleSet;
        this.statusSet = statSet;
        this.gui = gui;
        this.precedence = precedence;
        this.contradictionMgr = contradictionMgr;

        this.skipThisAuthorNid = skipThisAuthorNid;

        // STATISTICS COUNTERS
        countConSeen = 0;
        countConRoot = 0;
        countConDuplVersion = 0;
        countConAdded = 0; // ADDED TO SNOROCKET
        countRelAdded = 0; // ADDED TO SNOROCKET

        countRelCharInferred = 0;

        setupCoreNids();

        statusSetInactive = Terms.get().newIntSet();
        statusSetInactive.add(SnomedMetadataRfx.getSTATUS_RETIRED_NID());
    }

    private void setupCoreNids() throws Exception {
        I_TermFactory tf = Terms.get();

        // SETUP CORE NATIVES IDs
        isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
        rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());

        // Characteristic
        isCh_INFERRED_RELATIONSHIP = SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID();
    }

    @Override
    public void processConcept(I_GetConceptData concept) throws Exception {
        // processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc)
        int cNid = concept.getNid();
        if (++countConSeen % 25000 == 0 && logger != null) {
            logger.log(Level.INFO, "::: [SnoPathProcessCondor] Concepts viewed:\t{0}", countConSeen);
        }
        
        if (ExcludeFromIteration.exclude(concept))
        {
        	skippedForPath++;
        	return;
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
                statusSet, fromPathPos, precedence, contradictionMgr);

        if (attribs.size() >= 1) {
            passToCompare = true;
        } else {
            attribs = concept.getConceptAttributeTuples(statusSetInactive, fromPathPos,
                    precedence, contradictionMgr);
            if (attribs.size() >= 1) {
                passToCompare = true;
            }
        }

        if (passToCompare) {
            List<? extends I_RelTuple> relTupList = concept.getSourceRelTuples(statusSet,
                    roleTypeSet, fromPathPos, precedence, contradictionMgr);

            countConAdded++;

            for (I_RelTuple rt : relTupList) {
                int authorNid = rt.getAuthorNid();
                if (authorNid == skipThisAuthorNid) {
                    continue; // SKIP IF NOT INFERRED BY "OTHER" CLASSIFIER
                }
                int charId = rt.getCharacteristicId();
                boolean keep = false;
                if (charId == isCh_INFERRED_RELATIONSHIP &&
                        rt.getTypeNid() == isaNid) {
                    keep = true;
                    countRelCharInferred++;
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
        s.append("\r\n::: [SnoPathProcessCondor] ProcessPath()");
        if (startTime > 0) {
            long lapseTime = System.currentTimeMillis() - startTime;
            s.append("\r\n::: [Time] get vodb data: \t").append(lapseTime).append("\t(mS)\t");
            s.append(((float) lapseTime / 1000) / 60).append("\t(min)");
            s.append("\r\n:::");
        }

        s.append("\r\n::: concepts viewed:       \t").append(countConSeen);
        s.append("\r\n::: concepts added:        \t").append(countConAdded);
        s.append("\r\n::: concepts skipped for path  \t").append(skippedForPath);
        s.append("\r\n::: relationships added:   \t").append(countRelAdded);
        s.append("\r\n:::");

        s.append("\r\n::: ");
        s.append("\r\n::: concept root added:  \t").append(countConRoot);
        s.append("\r\n::: con version conflict:\t").append(countConDuplVersion);
        s.append("\t # attribs.size() > 1");
        s.append("\r\n::: ");
        s.append("\r\n::: Other Inferred:     \t").append(countRelCharInferred);

        s.append("\r\n::: ");
        s.append("\r\n");

        return s.toString();
    }
}
