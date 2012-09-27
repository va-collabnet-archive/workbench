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
package org.ihtsdo.mojo.mojo.compare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.mojo.mojo.CompareComponents;

public class MonitorComponents {

    List<I_Position> positions = new LinkedList<I_Position>();
    I_TermFactory termFactory;

    public MonitorComponents() {
        termFactory = Terms.get();
    }

    public List<Match> checkConcept(I_GetConceptData concept, List<Integer> acceptedStatusIds) throws Exception {

        // TODO replace with passed in config...
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        // get latest concept attributes/descriptions/relationships
        boolean attributesMatch = true;
        boolean descriptionsMatch = true;
        boolean relationshipsMatch = true;

        List<? extends I_ConceptAttributeTuple> conceptAttributeTuples1 = new LinkedList<I_ConceptAttributeTuple>();
        List<? extends I_ConceptAttributeTuple> conceptAttributeTuples2 = new LinkedList<I_ConceptAttributeTuple>();

        List<? extends I_DescriptionTuple> descriptionTuples1 = new LinkedList<I_DescriptionTuple>();
        List<? extends I_DescriptionTuple> descriptionTuples2 = new LinkedList<I_DescriptionTuple>();

        List<? extends I_RelTuple> relationshipTuples1 = new LinkedList<I_RelTuple>();
        List<? extends I_RelTuple> relationshipTuples2 = new LinkedList<I_RelTuple>();

        List<Match> matches = new ArrayList<Match>();

        /*
         * If there is only one position here, then only compare the concept
         * attributes
         * to the required status.
         */
        if (positions.size() == 1) {
            Set<I_Position> firstPosition = new HashSet<I_Position>();
            firstPosition.add(positions.get(0));
            conceptAttributeTuples1 = concept.getConceptAttributeTuples(null, new PositionSetReadOnly(firstPosition), 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            descriptionTuples1 = concept.getDescriptionTuples(null, null, new PositionSetReadOnly(firstPosition), 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            relationshipTuples1 = concept.getSourceRelTuples(null, null, new PositionSetReadOnly(firstPosition), 
                config.getPrecedence(), config.getConflictResolutionStrategy());

            /*
             * Is the status correct??
             */
            attributesMatch = false;
            for (int tuple = 0; tuple < conceptAttributeTuples1.size(); tuple++) {
                if ((acceptedStatusIds.size() == 0 || (acceptedStatusIds.size() != 0 && acceptedStatusIds.contains(conceptAttributeTuples1.get(
                    tuple)
                    .getStatusId())))) {

                    attributesMatch = true;
                }
            }
            descriptionsMatch = false;
            for (int tuple = 0; tuple < descriptionTuples1.size(); tuple++) {
                if ((acceptedStatusIds.size() == 0 || (acceptedStatusIds.size() != 0 && acceptedStatusIds.contains(descriptionTuples1.get(
                    tuple)
                    .getStatusId())))) {
                    descriptionsMatch = true;
                }
            }
            relationshipsMatch = false;
            if (relationshipTuples1.size() == 0) {
                relationshipsMatch = true;
            }
            for (int tuple = 0; tuple < relationshipTuples1.size(); tuple++) {
                if ((acceptedStatusIds.size() == 0 || (acceptedStatusIds.size() != 0 && acceptedStatusIds.contains(relationshipTuples1.get(
                    tuple)
                    .getStatusId())))) {
                    relationshipsMatch = true;
                }
            }

            if (attributesMatch && descriptionsMatch && relationshipsMatch) {
                Match match = new Match(positions.get(0), positions.get(0));
                match.matchConceptAttributeTuples.addAll(conceptAttributeTuples1);
                match.matchDescriptionTuples.addAll(descriptionTuples1);
                match.matchRelationshipTuples.addAll(relationshipTuples1);
                matches.add(match);
            }
        }

        for (int j = 0; j < positions.size() - 1; j++) {
            Set<I_Position> firstPosition = new HashSet<I_Position>();
            firstPosition.add(positions.get(j));
            conceptAttributeTuples1 = concept.getConceptAttributeTuples(null, new PositionSetReadOnly(firstPosition), 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            descriptionTuples1 = concept.getDescriptionTuples(null, null, new PositionSetReadOnly(firstPosition), 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            relationshipTuples1 = concept.getSourceRelTuples(null, null, new PositionSetReadOnly(firstPosition), 
                config.getPrecedence(), config.getConflictResolutionStrategy());

            for (int i = j; i < positions.size() - 1; i++) {

                Set<I_Position> secondPosition = new HashSet<I_Position>();
                secondPosition.add(positions.get(i + 1));

                conceptAttributeTuples2 = concept.getConceptAttributeTuples(null, new PositionSetReadOnly(secondPosition), 
                    config.getPrecedence(), config.getConflictResolutionStrategy());
                descriptionTuples2 = concept.getDescriptionTuples(null, null, new PositionSetReadOnly(secondPosition), 
                    config.getPrecedence(), config.getConflictResolutionStrategy());
                relationshipTuples2 = concept.getSourceRelTuples(null, null, new PositionSetReadOnly(secondPosition), 
                    config.getPrecedence(), config.getConflictResolutionStrategy());

                if (!CompareComponents.attributeListsEqual(conceptAttributeTuples1, conceptAttributeTuples2)) {
                    attributesMatch = false;
                } else {

                    /*
                     * There is a match, but is the status correct??
                     */
                    attributesMatch = false;
                    for (int tuple = 0; tuple < conceptAttributeTuples1.size(); tuple++) {
                        if ((acceptedStatusIds.size() == 0 || (acceptedStatusIds.size() != 0 && acceptedStatusIds.contains(conceptAttributeTuples1.get(
                            tuple)
                            .getStatusId())))) {
                            attributesMatch = true;
                        }
                    }
                }
                if (!CompareComponents.descriptionListsEqual(descriptionTuples1, descriptionTuples2)) {
                    descriptionsMatch = false;
                } else {
                    /*
                     * There is a match, but is the status correct??
                     */
                    descriptionsMatch = false;
                    for (int tuple = 0; tuple < descriptionTuples1.size(); tuple++) {
                        if ((acceptedStatusIds.size() == 0 || (acceptedStatusIds.size() != 0 && acceptedStatusIds.contains(descriptionTuples1.get(
                            tuple)
                            .getStatusId())))) {
                            descriptionsMatch = true;
                        }
                    }
                }

                if (!CompareComponents.relationshipListsEqual(relationshipTuples1, relationshipTuples2)) {
                    relationshipsMatch = false;
                } else {
                    /*
                     * There is a match, but is the status correct??
                     */
                    relationshipsMatch = false;
                    if (relationshipTuples1.size() == 0) {
                        relationshipsMatch = true;
                    }
                    for (int tuple = 0; tuple < relationshipTuples1.size(); tuple++) {
                        if ((acceptedStatusIds.size() == 0 || (acceptedStatusIds.size() != 0 && acceptedStatusIds.contains(relationshipTuples1.get(
                            tuple)
                            .getStatusId())))) {
                            relationshipsMatch = true;
                        }
                    }
                }

                if (descriptionsMatch && relationshipsMatch && attributesMatch) {
                    Match match = new Match(positions.get(j), positions.get(i + 1));
                    match.matchConceptAttributeTuples.addAll(conceptAttributeTuples1);
                    match.matchDescriptionTuples.addAll(descriptionTuples1);
                    match.matchRelationshipTuples.addAll(relationshipTuples1);
                    matches.add(match);
                }

            }

        }
        return matches;
    }

    public List<I_Position> getPositions() {
        return positions;
    }

    public void setPositions(List<I_Position> positions) {
        this.positions = positions;
    }
}
