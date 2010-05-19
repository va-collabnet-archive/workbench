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
package org.dwfa.ace.task.conflict.detector;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.tapi.TerminologyException;

public class ConflictDetector {
    /**
     * @todo implement conflict for images.
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException 
     */
    public static boolean conflict(I_GetConceptData concept, I_ConfigAceFrame profileForConflictDetection)
            throws IOException, TerminologyException {
        Set<I_ConceptAttributeTuple> attributeTuples = null;
        Set<I_DescriptionTuple> descTuples = null;
        Set<I_RelTuple> relTuples = null;
        for (I_Position viewPos : profileForConflictDetection.getViewPositionSet()) {
            Set<I_Position> positionSet = new HashSet<I_Position>();
            positionSet.add(viewPos);
            PositionSetReadOnly viewPositionSet = new PositionSetReadOnly(positionSet);
            if (attributeTuples == null) {
                attributeTuples = new TreeSet<I_ConceptAttributeTuple>(new AttrTupleConflictComparator());
                attributeTuples.addAll(concept.getConceptAttributeTuples(null, viewPositionSet, 
                    profileForConflictDetection.getPrecedence(), 
                    profileForConflictDetection.getConflictResolutionStrategy()));
            } else {
                TreeSet<I_ConceptAttributeTuple> positionTupleSet = new TreeSet<I_ConceptAttributeTuple>(
                    new AttrTupleConflictComparator());
                positionTupleSet.addAll(concept.getConceptAttributeTuples(null, viewPositionSet,
                    profileForConflictDetection.getPrecedence(),
                    profileForConflictDetection.getConflictResolutionStrategy()));
                if (positionTupleSet.containsAll(attributeTuples) == false) {
                    return true;
                }
                if (attributeTuples.containsAll(positionTupleSet) == false) {
                    return true;
                }
            }

            if (descTuples == null) {
                descTuples = new TreeSet<I_DescriptionTuple>(new DescriptionTupleConflictComparator());
                descTuples.addAll(concept.getDescriptionTuples(null, null, viewPositionSet,
                    profileForConflictDetection.getPrecedence(),
                    profileForConflictDetection.getConflictResolutionStrategy()));
            } else {
                TreeSet<I_DescriptionTuple> positionTupleSet = new TreeSet<I_DescriptionTuple>(
                    new DescriptionTupleConflictComparator());
                positionTupleSet.addAll(concept.getDescriptionTuples(null, null, viewPositionSet,
                    profileForConflictDetection.getPrecedence(),
                    profileForConflictDetection.getConflictResolutionStrategy()));
                if (positionTupleSet.containsAll(descTuples) == false) {
                    return true;
                }
                if (descTuples.containsAll(positionTupleSet) == false) {
                    return true;
                }
            }

            if (relTuples == null) {
                relTuples = new TreeSet<I_RelTuple>(new RelTupleConflictComparator());
                relTuples.addAll(concept.getSourceRelTuples(null, null, viewPositionSet,
                    profileForConflictDetection.getPrecedence(),
                    profileForConflictDetection.getConflictResolutionStrategy()));
            } else {
                TreeSet<I_RelTuple> positionTupleSet = new TreeSet<I_RelTuple>(new RelTupleConflictComparator());
                positionTupleSet.addAll(concept.getSourceRelTuples(null, null, viewPositionSet,
                    profileForConflictDetection.getPrecedence(),
                    profileForConflictDetection.getConflictResolutionStrategy()));
                if (positionTupleSet.containsAll(relTuples) == false) {
                    return true;
                }
                if (relTuples.containsAll(positionTupleSet) == false) {
                    return true;
                }
            }
        }

        return false;
    }
}
