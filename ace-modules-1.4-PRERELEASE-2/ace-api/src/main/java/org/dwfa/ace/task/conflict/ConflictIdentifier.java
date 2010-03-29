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
package org.dwfa.ace.task.conflict;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class ConflictIdentifier implements I_ProcessConcepts {

    private I_IntSet conflictsNids;

    private int conceptsProcessed = 0;
    private int conceptsProcessedWithNoTuples = 0;
    private int conceptsProcessedWithOneTuple = 0;
    private int conceptsProcessedWithTwoTuples = 0;
    private int conceptsProcessedWithThreeTuples = 0;
    private int conceptsProcessedWithFourTuples = 0;

    I_ConfigAceFrame profileForConflictDetection;

    HashMap<Integer, Integer> statusCount = new HashMap<Integer, Integer>();

    public ConflictIdentifier(I_IntSet conflictsNids, I_ConfigAceFrame profileForConflictDetection) {
        super();
        this.conflictsNids = conflictsNids;
        this.profileForConflictDetection = profileForConflictDetection;
    }

    public int getConceptsProcessed() {
        return conceptsProcessed;
    }

    public I_IntSet getConflictsNids() {
        return conflictsNids;
    }

    public void processConcept(I_GetConceptData concept) throws Exception {
        conceptsProcessed++;
        List<I_ConceptAttributeTuple> attrTupels;
        if (profileForConflictDetection == null) {
            attrTupels = concept.getConceptAttributeTuples(null, null);
        } else {
            attrTupels = concept.getConceptAttributeTuples(profileForConflictDetection.getAllowedStatus(),
                profileForConflictDetection.getViewPositionSet());
        }
        int tupleListSize = attrTupels.size();
        if (tupleListSize > 1) {
            AceLog.getAppLog().info(concept.getInitialText() + " has multiple tuples: " + attrTupels);
        }
        for (I_ConceptAttributeTuple tuple : attrTupels) {
            if (statusCount.containsKey(tuple.getConceptStatus())) {
                Integer count = statusCount.get(tuple.getConceptStatus());
                statusCount.put(tuple.getConceptStatus(), count.intValue() + 1);
            } else {
                statusCount.put(tuple.getConceptStatus(), 1);
            }
        }
        switch (attrTupels.size()) {
        case 0:
            conceptsProcessedWithNoTuples++;
            break;
        case 1:
            conceptsProcessedWithOneTuple++;
            break;
        case 2:
            conceptsProcessedWithTwoTuples++;
            conflictsNids.add(concept.getConceptId());
            break;
        case 3:
            conceptsProcessedWithThreeTuples++;
            conflictsNids.add(concept.getConceptId());
            break;
        case 4:
            conceptsProcessedWithFourTuples++;
            conflictsNids.add(concept.getConceptId());
            break;

        default:
            conflictsNids.add(concept.getConceptId());
            break;
        }
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        if (profileForConflictDetection != null) {
            for (I_Position view : profileForConflictDetection.getViewPositionSet()) {
                buff.append("\nview: " + view);
            }
            for (int statusNid : profileForConflictDetection.getAllowedStatus().getSetValues()) {
                try {
                    I_GetConceptData status = LocalVersionedTerminology.get().getConcept(statusNid);
                    buff.append("\nallowed status: " + status.getInitialText() + " (" + status.getConceptId() + "): ");
                } catch (TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        } else {
            buff.append("\n null profile. ");
        }
        for (Integer key : statusCount.keySet()) {
            try {
                I_GetConceptData status = LocalVersionedTerminology.get().getConcept(key);
                Integer count = statusCount.get(key);
                buff.append("\nstatus: " + status.getInitialText() + " (" + status.getConceptId() + "); count: "
                    + count);
            } catch (TerminologyException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

        return "conceptsProcessed: " + conceptsProcessed + " conflicts: " + conflictsNids.getSetValues().length
            + " conceptsProcessedWithNoTuples: " + conceptsProcessedWithNoTuples + " conceptsProcessedWithOneTuple: "
            + conceptsProcessedWithOneTuple + " conceptsProcessedWithTwoTuples: " + conceptsProcessedWithTwoTuples
            + " conceptsProcessedWithThreeTuples: " + conceptsProcessedWithThreeTuples
            + " conceptsProcessedWithFourTuples: " + conceptsProcessedWithFourTuples + buff.toString();
    }

}
