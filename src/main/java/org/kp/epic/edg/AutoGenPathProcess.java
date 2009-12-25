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
package org.kp.epic.edg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.classify.SnoCon;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * AutoGenPathProcess object is passed to TermFactory.iterateConcepts() to
 * retrieve all current concepts visible to the path positions which
 * have at least one description which is extended by a EDG Clinical Item 2 refset.
 * 
 * @author Marc E. Campbell
 * 
 */

public class AutoGenPathProcess implements I_ProcessConcepts {
    private I_TermFactory tf = null;
    private List<I_Position> fromPathPos;
    private Logger logger;
    private List<SnoRel> snorels;
    private List<SnoCon> snocons;
    private int[] nidsEDGClinicalItem_2;

    // STATISTICS COUNTERS
    private int countConSeen = 0;
    private int countConDuplVersion = 0;
    private int countRelDuplVersion = 0;
    private int countSnoCon = 0;
    private int countSnoRel = 0;

    // CORE NID CONSTANTS
    private static int isaNid = Integer.MIN_VALUE;
    private static int isCURRENT = Integer.MIN_VALUE;
    private static int isRETIRED = Integer.MIN_VALUE;
    private static int isOPTIONAL_REFINABILITY = Integer.MIN_VALUE;
    private static int isNOT_REFINABLE = Integer.MIN_VALUE;
    private static int isMANDATORY_REFINABILITY = Integer.MIN_VALUE;
    private static int isCh_STATED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_DEFINING_CHARACTERISTIC = Integer.MIN_VALUE;
    private static int isCh_STATED_AND_INFERRED_RELATIONSHIP = Integer.MIN_VALUE;
    private static int isCh_STATED_AND_SUBSUMED_RELATIONSHIP = Integer.MIN_VALUE;

    public AutoGenPathProcess(Logger logger, List<SnoCon> snocons, List<SnoRel> snorels,
            List<I_Position> fromPathPos, int[] nids) throws TerminologyException, IOException {
        this.logger = logger;
        this.snocons = snocons;
        this.snorels = snorels;
        this.fromPathPos = fromPathPos;
        tf = LocalVersionedTerminology.get();
        setupCoreNids();
        nidsEDGClinicalItem_2 = nids;
    }

    public void processConcept(I_GetConceptData concept) throws Exception {
        if (++countConSeen % 25000 == 0) {
            logger.info("::: [AutoGenPathProcess] Concepts viewed:\t" + countConSeen);
        }

        // GET LATEST CONCEPT PART
        List<? extends I_ConceptAttributePart> cParts;
        cParts = concept.getConceptAttributes().getVersions();
        I_ConceptAttributePart cPart1 = null;
        for (I_Position pos : fromPathPos) { // FOR PATHS_IN_PRIORITY_ORDER
            for (I_ConceptAttributePart cPart : cParts) {
                if (pos.getPath().getConceptId() == cPart.getPathId()) {
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

        if (cPart1 == null)
            return; // not relevant to the "visible path"

        if (cPart1.getStatusId() != isCURRENT)
            return; // IF (NOT_CURRENT) RETURN; "active" in "visible path"

        if (snocons != null) {
            if (testForRefSetMember_Type2(concept.getConceptId())) {
                snocons.add(new SnoCon(concept.getConceptId(), cPart1.isDefined()));
                countSnoCon++;
            }
        }

        if (snorels == null)
            return;
        // PROCESS CURRENT RELATIONSHIPS
        List<SnoRel> rels = findRelationships(concept);
        if (rels != null) {
            for (SnoRel x : rels) {
                countSnoRel++;
                x.setCid(countSnoRel); // Update SnoRel sequential uid
                snorels.add(x); // Add to master input set
            }
        }
    }

    private List<SnoRel> findRelationships(I_GetConceptData concept) throws IOException {
        // STATISTICS VARIABLES
        int tmpCountRelCharStated = 0;
        int tmpCountRelCharDefining = 0;
        int tmpCountRelCharStatedInferred = 0;
        int tmpCountRelCharStatedSubsumed = 0;
        int tmpCountRelRefNot = 0;
        int tmpCountRelRefOpt = 0;
        int tmpCountRelRefMand = 0;

        // OUTPUT VARIABLES
        boolean isSnomedConcept = false;
        List<SnoRel> keepRels = new ArrayList<SnoRel>();

        // FOR ALL SOURCE RELS
        for (I_RelVersioned rel : concept.getSourceRels()) {
            // FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
            I_RelPart rPart1 = null;
            for (I_Position pos : fromPathPos) { // PATHS_IN_PRIORITY_ORDER
                for (I_RelPart rPart : rel.getVersions()) {
                    if (pos.getPath().getConceptId() == rPart.getPathId()) {
                        if (rPart1 == null) {
                            rPart1 = rPart; // ... KEEP FIRST_INSTANCE
                        } else if (rPart1.getVersion() < rPart.getVersion()) {
                            rPart1 = rPart; // ... KEEP MORE_RECENT PART
                        } else if (rPart1.getVersion() == rPart.getVersion()) {
                            countRelDuplVersion++;
                            if (rPart.getStatusId() == isCURRENT)
                                rPart1 = rPart; // KEEP CURRENT PART
                        }
                    }
                }
                if (rPart1 != null) {
                    break; // IF FOUND ON THIS PATH, STOP SEARCHING
                }
            }

            if ((rPart1 != null) && (rPart1.getStatusId() == isCURRENT)) {
                // must FIND at least one SNOMED IS-A relationship
                if (rPart1.getTypeId() == isaNid) {
                    isSnomedConcept = true;
                }

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
                } else if (p1c == isCh_STATED_AND_INFERRED_RELATIONSHIP) {
                    keep = true;
                    tmpCountRelCharStatedInferred++;
                } else if (p1c == isCh_STATED_AND_SUBSUMED_RELATIONSHIP) {
                    keep = true;
                    tmpCountRelCharStatedSubsumed++;
                }

                if (keep) {
                    // UPDATE STATS
                    int p1rfn = rPart1.getRefinabilityId();
                    if (p1rfn == isOPTIONAL_REFINABILITY)
                        tmpCountRelRefOpt++;
                    else if (p1rfn == isNOT_REFINABLE)
                        tmpCountRelRefNot++;
                    else if (p1rfn == isMANDATORY_REFINABILITY)
                        tmpCountRelRefMand++;

                    // ADD TO STATED
                    SnoRel relationship = new SnoRel(rel, rPart1, -1);
                    keepRels.add(relationship);
                }
            }
        }

        if (isSnomedConcept) {
            return keepRels;
        } else
            return null;
    }

    private void setupCoreNids() {
        // SETUP CORE NATIVES IDs
        try {
            isaNid = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            tf.uuidToNative(SNOMED.Concept.ROOT.getUids());
            // 0 CURRENT, 1 RETIRED
            isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            isRETIRED = tf.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
            // NOT_REFINABLE | OPTIONAL_REFINABILITY | MANDATORY_REFINABILITY
            isOPTIONAL_REFINABILITY = tf.uuidToNative(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
            isNOT_REFINABLE = tf.uuidToNative(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
            isMANDATORY_REFINABILITY = tf.uuidToNative(ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.getUids());

            // Characteristic
            isCh_STATED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
            isCh_DEFINING_CHARACTERISTIC = tf.uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
            isCh_STATED_AND_INFERRED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_INFERRED_RELATIONSHIP.getUids());
            isCh_STATED_AND_SUBSUMED_RELATIONSHIP = tf.uuidToNative(ArchitectonicAuxiliary.Concept.STATED_AND_SUBSUMED_RELATIONSHIP.getUids());
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean testForRefSetMember_Type2(int nid) throws TerminologyException, IOException {
        I_GetConceptData cb = tf.getConcept(nid);

        List<? extends I_DescriptionVersioned> dl = cb.getDescriptions();
        for (I_DescriptionVersioned d : dl) {
            // :@@@:!!!: d.? tf.get? HOW TO GET members extending description
            List<I_ThinExtByRefVersioned> extList = tf.getAllExtensionsForComponent(d.getNid());
            for (I_ThinExtByRefVersioned ext : extList) {
                int refsetNid = ext.getRefsetId();
                int l = nidsEDGClinicalItem_2.length;
                for (int i = 0; i < l; i++)
                    if (refsetNid == nidsEDGClinicalItem_2[i])
                        return true;
            }
        }
        return false;
    }

    // STATS FROM PROCESS CONCEPTS (CLASSIFIER INPUT)
    public String toStringStats(long startTime) {
        StringBuffer s = new StringBuffer(1500);
        s.append("\r\n::: [AutoGenPathProcess] ProcessPath()");
        if (startTime > 0) {
            long lapseTime = System.currentTimeMillis() - startTime;
            s.append("\r\n::: [Time] get vodb data: \t" + lapseTime + "\t(mS)\t"
                + (((float) lapseTime / 1000) / 60) + "\t(min)");
            s.append("\r\n:::");
        }

        s.append("\r\n::: concepts viewed:      \t" + countConSeen);
        s.append("\r\n::: concepts total current:\t" + countSnoCon);
        s.append("\r\n::: ");
        s.append("\r\n::: con version conflict:\t" + countConDuplVersion);
        s.append("\r\n::: rel version conflict:\t" + countRelDuplVersion);
        s.append("\r\n::: ");
        s.append("\r\n::: ");
        s.append("\r\n");
        return s.toString();
    }

}
