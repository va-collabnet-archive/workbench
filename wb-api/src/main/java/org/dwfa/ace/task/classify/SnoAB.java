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
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * Determines if Concept A subsumes Concept B.
 * 
 * SnoAB is a utility to support SnoGrp role value groups subsumption and
 * differentiation methods.
 * 
 * @author Marc E. Campbell
 * 
 */

public class SnoAB {
    public static int isCURRENT = Integer.MIN_VALUE;
    public static int isaNid = Integer.MIN_VALUE;
    public static List<I_Position> posList = null; //
    I_TermFactory tf;

    public SnoAB() {
        tf = Terms.get();
        try {
            I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
            isaNid = config.getClassifierIsaType().getConceptId();
            isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setPathPosList(List<I_Position> posList) {
        SnoAB.posList = posList;
    }

    public boolean aSubsumesB(int nid_A, int nid_B) {
        try {
            I_GetConceptData con_B = tf.getConcept(nid_B);

            List<SnoRel> isaSnoRelProx = findIsaProximal(con_B);
            while (isaSnoRelProx.size() > 0) {
                for (SnoRel isaRel : isaSnoRelProx) {
                    if (isaRel.c2Id == nid_A)
                        return true;
                }

                List<SnoRel> isaSnoRelProxNext = new ArrayList<SnoRel>();
                for (SnoRel isaRel : isaSnoRelProx) {
                    I_GetConceptData con_tmp = tf.getConcept(isaRel.c2Id);
                    isaSnoRelProxNext.addAll(findIsaProximal(con_tmp));
                }
                isaSnoRelProx = isaSnoRelProxNext;
            }

        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<SnoRel> findIsaProximal(I_GetConceptData cBean) {
        List<SnoRel> returnSnoRels = new ArrayList<SnoRel>();
        try {
            Collection<? extends I_RelVersioned> relList = cBean.getSourceRels();
            for (I_RelVersioned rel : relList) { // FOR EACH [C1, C2] PAIR
                // FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
                I_RelPart rp1 = null;
                for (I_Position pos : posList) { // FOR EACH PATH POSITION
                    // FIND MOST CURRENT
                    int tmpCountDupl = 0;
                    for (I_RelPart rp : rel.getMutableParts()) {
                        if (rp.getPathId() == pos.getPath().getConceptId()) {
                            if (rp1 == null) {
                                rp1 = rp; // ... KEEP FIRST_INSTANCE PART
                            } else if (rp1.getVersion() < rp.getVersion()) {
                                rp1 = rp; // ... KEEP MORE_RECENT PART
                            } else if (rp1.getVersion() == rp.getVersion()) {
                                // DUPLICATE PART SHOULD NEVER HAPPEN
                                tmpCountDupl++;
                            }
                        }
                    }
                    if (rp1 != null) {
                        if (rp1.getStatusId() == isCURRENT && rp1.getTypeId() == isaNid) {
                            returnSnoRels.add(new SnoRel(rel.getC1Id(), rel.getC2Id(), rp1.getTypeId(), rp1.getGroup(), rel.getNid()));
                        }
                        break; // IF FOUND ON THIS PATH, STOP SEARCHING
                    }
                } // FOR EACH PATH POSITION

            } // FOR EACH [C1, C2] PAIR
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return returnSnoRels;
    }

}
