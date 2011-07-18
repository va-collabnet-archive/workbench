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
package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.example.binding.SnomedMetadataRfx;

/**
 * 
 * SnoPathProcess object is passed to TermFactory.iterateConcepts() to retrieve
 * all current concepts along the specified path.
 * 
 * @author Marc E. Campbell
 * 
 */
public class SnoPathProcess implements I_ProcessConcepts {

    private List<PositionBI> fromPathPos;
    private Logger logger;
    private List<SnoRel> snorels;
    private List<SnoCon> snocons;
    // Do not filter 'Is a' on classifier path
    // The classifier itself is a "filter" what gets dropped.
    private boolean doNotCareIfHasSnomedIsa;
    // STATISTICS COUNTERS
    private int countConSeen = 0;
    private int countConRoot = 0;
    private int countConDuplVersion = 0;
    private int countSnoCon = 0;
    private int countConAdded = 0; // ADDED TO SNOROCKET
    public int countRelAdded = 0; // ADDED TO SNOROCKET
    private int countRelAddedGroups = 0; // Count rels with non-zero group
    private int countRelDuplVersion = 0; // SAME PATH, SAME VERSION
    private int countRelCharStated = 0;
    private int countRelCharDefining = 0;
    private int countRelRefNot = 0;
    private int countRelRefOpt = 0;
    private int countRelRefMand = 0;
    // CORE NID CONSTANTS
    private static int isaNid = Integer.MIN_VALUE;
    private static int rootNid = Integer.MIN_VALUE;
    private static int isCURRENT = Integer.MIN_VALUE;
    private static int isRETIRED = Integer.MIN_VALUE;
    private static int isOPTIONAL_REFINABILITY = Integer.MIN_VALUE;
    private static int isNOT_REFINABLE = Integer.MIN_VALUE;
    private static int isMANDATORY_REFINABILITY = Integer.MIN_VALUE;
    private static int isCh_STATED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_DEFINING_CHARACTERISTIC = Integer.MIN_VALUE;
    private int[] allowedRoles;
    // GUI
    I_ShowActivity gui = null;

    public SnoPathProcess(Logger logger, List<SnoCon> snocons, List<SnoRel> snorels,
            int[] allowedRoles, List<PositionBI> fromPathPos, I_ShowActivity gui,
            boolean doNotCareIfHasIsa) throws Exception {
        this.logger = logger;
        this.snocons = snocons;
        this.snorels = snorels;
        this.fromPathPos = fromPathPos;
        this.gui = gui;
        setupCoreNids();
        this.allowedRoles = allowedRoles;
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

        countRelRefNot = 0;
        countRelRefOpt = 0;
        countRelRefMand = 0;
    }

    public void processConcept(I_GetConceptData concept) throws Exception {
        if (++countConSeen % 25000 == 0) {
            logger.log(Level.INFO, "::: [SnoPathProcess] Concepts viewed:\t{0}", countConSeen);
        }
        int cNid = concept.getNid();

        if (concept.getConceptNid() != cNid) {
            logger.log(Level.INFO, "::: [SnoPathProcess] concept.getConceptNid({0}) != concept.getNid({1})",
                    new Object[]{concept.getConceptNid(), cNid});
        }

        if (cNid == rootNid) {
            if (snocons != null) {
                snocons.add(new SnoCon(cNid, false));
            }

            countConAdded++;
            countConRoot++;
            return;
        }

        // GET LATEST CONCEPT PART
        List<? extends I_ConceptAttributePart> cParts;
        cParts = concept.getConceptAttributes().getMutableParts();
        I_ConceptAttributePart cPart1 = null;
        for (PositionBI pos : fromPathPos) { // FOR PATHS_IN_PRIORITY_ORDER
            for (I_ConceptAttributePart cPart : cParts) {
                if (pos.getPath().getConceptNid() == cPart.getPathId()) {
                    if (cPart1 == null) {
                        cPart1 = cPart; // ...... KEEP FIRST INSTANCE
                    } else if (cPart1.getVersion() < cPart.getVersion()) {
                        cPart1 = cPart; // ...... KEEP MORE RECENT PART
                    } else if (cPart1.getVersion() == cPart.getVersion()) {
                        countConDuplVersion++; // COUNT DUPLICATE CASE
                    }
                } // if ON_THIS_PATH
            } // for EACH_CONCEPT_PART
            if (cPart1 != null) {
                break; // IF FOUND ON THIS PATH, STOP SEARCHING
            }
        } // for PATHS_IN_PRIORITY_ORDER

        if (cPart1 == null) {
            return; // not relevant to the "visible path"
        }
        if (cPart1.getStatusId() != isCURRENT) {
            return; // IF (NOT_CURRENT) RETURN; "active" in "visible path"
        }
        if (snocons != null) {
            snocons.add(new SnoCon(cNid, cPart1.isDefined()));
            countSnoCon++;
        }

        // PROCESS CURRENT RELATIONSHIPS
        List<SnoRel> rels = findRelationships(concept);
        if (rels != null) {
            // Add Concept to Snorocket
            countConAdded++;

            for (SnoRel x : rels) {
                countRelAdded++;
                if (gui != null && countRelAdded % 25000 == 0) {
                    // ** GUI: ProcessPath
                    gui.setValue(countRelAdded);
                    gui.setProgressInfoLower("rels processed " + countRelAdded);
                }
                if (x.group >= 0) {
                    countRelAddedGroups++;
                }
                if (snorels != null) {
                    snorels.add(x); // Add to master input set
                }
            }
        }
    }

    private List<SnoRel> findRelationships(I_GetConceptData concept) throws IOException {
        // STATISTICS VARIABLES
        int tmpCountRelCharStated = 0;
        int tmpCountRelCharDefining = 0;
        int tmpCountRelRefNot = 0;
        int tmpCountRelRefOpt = 0;
        int tmpCountRelRefMand = 0;

        // OUTPUT VARIABLES
        boolean isSnomedConcept = false;
        List<SnoRel> keepRels = new ArrayList<SnoRel>();

        // FOR ALL SOURCE RELS
        for (I_RelVersioned<?> rel : concept.getSourceRels()) {
            // FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
            I_RelPart rPart1 = null;
            for (PositionBI pos : fromPathPos) { // PATHS_IN_PRIORITY_ORDER
                for (I_RelPart rPart : rel.getMutableParts()) {
                    if (pos.getPath().getConceptNid() == rPart.getPathId()) {
                        if (rPart1 == null) {
                            rPart1 = rPart; // ... KEEP FIRST_INSTANCE
                        } else if (rPart1.getVersion() < rPart.getVersion()) {
                            rPart1 = rPart; // ... KEEP MORE_RECENT PART
                        } else if (rPart1.getVersion() == rPart.getVersion()) {
                            countRelDuplVersion++;
                            if (rPart.getStatusId() == isCURRENT) {
                                rPart1 = rPart; // KEEP CURRENT PART
                            }
                        }
                    }
                }
                if (rPart1 != null) {
                    break; // IF FOUND ON THIS PATH, STOP SEARCHING
                }
            }

            if ((rPart1 != null) && (rPart1.getStatusId() == isCURRENT)) {
                // SET UP STATED FORMS LOOP
                // NOTE: inferred_only, descriptive and historic relationships
                // are not included
                int p1c = rPart1.getCharacteristicId();
                boolean keep = false;
                if (p1c == isCh_DEFINING_CHARACTERISTIC) {
                    keep = true;
                    tmpCountRelCharDefining++;
                } else if (p1c == isCh_STATED_RELATIONSHIP) {
                    keep = true;
                    tmpCountRelCharStated++;
                }

                // must FIND at least one SNOMED IS-A relationship
                int typeNid = rPart1.getTypeId();
                if (typeNid == isaNid) {
                    isSnomedConcept = true;
                } else if (allowedRoles != null) {
                    // must be an allowed role type
                    boolean isAllowed = false;
                    int i = 0;
                    while (isAllowed == false && i < allowedRoles.length) {
                        if (typeNid == allowedRoles[i]) {
                            isAllowed = true;
                        }
                        i++;
                    }
                    if (isAllowed == false) {
                        keep = false;
                    }
                }

                if (keep) {
                    // UPDATE STATS
                    int p1rfn = rPart1.getRefinabilityId();
                    if (p1rfn == isOPTIONAL_REFINABILITY) {
                        tmpCountRelRefOpt++;
                    } else if (p1rfn == isNOT_REFINABLE) {
                        tmpCountRelRefNot++;
                    } else if (p1rfn == isMANDATORY_REFINABILITY) {
                        tmpCountRelRefMand++;
                    }

                    // ADD TO STATED
                    SnoRel relationship = new SnoRel(rel.getC1Id(), rel.getC2Id(), rPart1.getTypeId(), rPart1.getGroup(), rel.getNid());
                    keepRels.add(relationship);
                }
            }
        }

        if (isSnomedConcept || doNotCareIfHasSnomedIsa) {
            countRelCharStated += tmpCountRelCharStated;
            countRelCharDefining += tmpCountRelCharDefining;

            countRelRefNot += tmpCountRelRefNot;
            countRelRefOpt += tmpCountRelRefOpt;
            countRelRefMand += tmpCountRelRefMand;

            return keepRels;
        } else {
            return null;
        }
    }

    private void setupCoreNids() throws Exception {
        I_TermFactory tf = Terms.get();

        // SETUP CORE NATIVES IDs
        isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
        rootNid = tf.uuidToNative(SNOMED.Concept.ROOT.getUids());
        // 0 CURRENT, 1 RETIRED
        isCURRENT = SnomedMetadataRfx.getCURRENT_NID();
        isRETIRED = SnomedMetadataRfx.getRETIRED_NID();
        // NOT_REFINABLE | OPTIONAL_REFINABILITY | MANDATORY_REFINABILITY
        isOPTIONAL_REFINABILITY = SnomedMetadataRfx.getOPTIONAL_REFINABILITY_NID();
        isNOT_REFINABLE = SnomedMetadataRfx.getNOT_REFINABLE_NID();
        isMANDATORY_REFINABILITY = SnomedMetadataRfx.getMANDATORY_REFINABILITY_NID();

        // Characteristic
        isCh_STATED_RELATIONSHIP = SnomedMetadataRfx.getCh_STATED_RELATIONSHIP_NID();
        isCh_DEFINING_CHARACTERISTIC = SnomedMetadataRfx.getCh_DEFINING_CHARACTERISTIC_NID();
    }

    // STATS FROM PROCESS CONCEPTS (CLASSIFIER INPUT)
    public String getStats(long startTime) {
        StringBuilder s = new StringBuilder(1500);
        s.append("\r\n::: [SnoPathProcess] ProcessPath()");
        if (startTime > 0) {
            long lapseTime = System.currentTimeMillis() - startTime;
            s.append("\r\n::: [Time] get vodb data: \t").append(lapseTime);
            s.append("\t(mS)\t").append(((float) lapseTime / 1000) / 60);
            s.append("\t(min)");
            s.append("\r\n:::");
        }

        s.append("\r\n::: concepts viewed:      \t").append(countConSeen);
        s.append("\r\n::: concepts total current:\t").append(countSnoCon);
        s.append("\r\n::: concepts w/rel added:  \t").append(countConAdded);
        s.append("\r\n::: relationships added:   \t").append(countRelAdded);
        s.append("\r\n:::");
        s.append("\r\n::: rel group TOTAL:\t").append(countRelAddedGroups);

        s.append("\r\n::: ");
        s.append("\r\n::: concept root added:  \t").append(countConRoot);
        s.append("\r\n::: con version conflict:\t").append(countConDuplVersion);
        s.append("\r\n::: rel version conflict:\t").append(countRelDuplVersion);
        s.append("\r\n::: ");
        s.append("\r\n::: Defining/Inferred: \t").append(countRelCharDefining);
        s.append("\r\n::: Stated:            \t").append(countRelCharStated);
        int total = countRelCharStated + countRelCharDefining;
        s.append("\r\n:::            TOTAL=\t").append(total);
        s.append("\r\n::: ");
        s.append("\r\n::: Optional Refinability: \t").append(countRelRefOpt);
        s.append("\r\n::: Not Refinable:         \t").append(countRelRefNot);
        s.append("\r\n::: Mandatory Refinability:\t").append(countRelRefMand);
        total = countRelRefNot + countRelRefOpt + countRelRefMand;
        s.append("\r\n:::                  TOTAL=\t").append(total);
        s.append("\r\n::: ");
        s.append("\r\n");
        return s.toString();
    }
}
