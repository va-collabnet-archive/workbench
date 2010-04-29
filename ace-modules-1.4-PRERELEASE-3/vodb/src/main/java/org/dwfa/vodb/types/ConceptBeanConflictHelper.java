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
package org.dwfa.vodb.types;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.task.conflict.detector.AttrTupleConflictComparator;
import org.dwfa.ace.task.conflict.detector.DescriptionTupleConflictComparator;
import org.dwfa.ace.task.conflict.detector.RelTupleConflictComparator;

public class ConceptBeanConflictHelper {

    public static Set<I_ConceptAttributeTuple> getCommonConceptAttributeTuples(ConceptBean cb, I_ConfigAceFrame config)
            throws IOException {
        Set<I_ConceptAttributeTuple> commonTuples = null;
        for (I_Position p : config.getViewPositionSet()) {
            Set<I_Position> positionSet = new HashSet<I_Position>();
            positionSet.add(p);
            List<I_ConceptAttributeTuple> tuplesForPosition = cb.getConceptAttributeTuples(config.getAllowedStatus(),
                positionSet, false);
            if (commonTuples == null) {
                commonTuples = new TreeSet<I_ConceptAttributeTuple>(new AttrTupleConflictComparator());
                commonTuples.addAll(tuplesForPosition);
            } else {
                commonTuples.retainAll(tuplesForPosition);
            }
        }
        if (commonTuples == null) {
            commonTuples = new TreeSet<I_ConceptAttributeTuple>(new AttrTupleConflictComparator());
        }
        return commonTuples;
    }

    public static Set<I_RelTuple> getCommonRelTuples(ConceptBean cb, I_ConfigAceFrame config) throws IOException {
        Set<I_RelTuple> commonTuples = null;
        for (I_Position p : config.getViewPositionSet()) {
            Set<I_Position> positionSet = new HashSet<I_Position>();
            positionSet.add(p);
            List<I_RelTuple> tuplesForPosition = cb.getSourceRelTuples(config.getAllowedStatus(), null, positionSet,
                false);
            if (commonTuples == null) {
                commonTuples = new TreeSet<I_RelTuple>(new RelTupleConflictComparator());
                commonTuples.addAll(tuplesForPosition);
            } else {
                commonTuples.retainAll(tuplesForPosition);
            }
        }
        if (commonTuples == null) {
            commonTuples = new TreeSet<I_RelTuple>(new RelTupleConflictComparator());
        }
        return commonTuples;
    }

    public static Set<I_DescriptionTuple> getCommonDescTuples(ConceptBean cb, I_ConfigAceFrame config)
            throws IOException {
        Set<I_DescriptionTuple> commonTuples = null;
        for (I_Position p : config.getViewPositionSet()) {
            Set<I_Position> positionSet = new HashSet<I_Position>();
            positionSet.add(p);
            List<I_DescriptionTuple> tuplesForPosition = cb.getDescriptionTuples(config.getAllowedStatus(), null,
                positionSet, false);
            if (commonTuples == null) {
                commonTuples = new TreeSet<I_DescriptionTuple>(new DescriptionTupleConflictComparator());
                commonTuples.addAll(tuplesForPosition);
            } else {
                commonTuples.retainAll(tuplesForPosition);
            }
        }
        if (commonTuples == null) {
            commonTuples = new TreeSet<I_DescriptionTuple>(new DescriptionTupleConflictComparator());
        }
        return commonTuples;
    }

}
