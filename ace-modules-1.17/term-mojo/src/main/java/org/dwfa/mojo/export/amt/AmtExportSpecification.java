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
package org.dwfa.mojo.export.amt;

import org.dwfa.mojo.export.ExportSpecification;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.util.TupleVersionPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.dto.ComponentDto;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.export.AbstractExportSpecification;
import org.dwfa.mojo.export.Position;

/**
 * Contains the hierarchy to be exports and optionally not exported that are on the exportable positions.
 */
public class AmtExportSpecification extends AbstractExportSpecification implements ExportSpecification {

    public AmtExportSpecification(final List<Position> positions, final List<I_GetConceptData> inclusions,
            final List<I_GetConceptData> exclusions, final NAMESPACE defaultNamespace, final PROJECT defaultProject)
            throws Exception {
        super(positions, inclusions, exclusions, defaultNamespace, defaultProject);
        this.updater = new AmtComponentDtoUpdater(defaultNamespace, defaultProject);
        this.utility = new AmtExportUtility();

        I_GetConceptData erroneous = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getUids().iterator().next());

        I_GetConceptData conceptRetired = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getUids().iterator().next());


        this.check = new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired);

        snomedIsATypeIntSet.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).
                getConceptId());
    }

    /**
     * It all starts hear, return a populated ComponentDto from the I_GetConceptData details.
     * Includes all extensions that this concepts/ description and relationship
     * is a member of.
     *
     * @param concept I_GetConceptData
     *
     * @return ComponentDto
     *
     * @throws Exception DB errors/missing concepts
     */
    @Override
    public ComponentDto getDataForExport(I_GetConceptData concept) throws Exception {
        ComponentDto componentDto = null;

        Set<I_ConceptAttributeTuple> matchingConceptTuples = new HashSet<I_ConceptAttributeTuple>();
        Set<I_DescriptionTuple> matchingDescriptionTuples = new HashSet<I_DescriptionTuple>();
        Set<I_RelTuple> matchingRelationshipTuples = new HashSet<I_RelTuple>();

        Set<I_ConceptAttributeTuple> latestPostionMatchingConceptTuples = new HashSet<I_ConceptAttributeTuple>();
        Set<I_DescriptionTuple> latestPostionMatchingDescriptionTuples = new HashSet<I_DescriptionTuple>();
        Set<I_RelTuple> latestPostionMatchingRelationshipTuples = new HashSet<I_RelTuple>();

        if (isExportableConcept(concept)) {
            componentDto = new ComponentDto();
            for (Position position : positions) {
                if (position.isLastest()) {
                    utility.addComponentTuplesToMaps(concept, latestPostionMatchingConceptTuples,
                            latestPostionMatchingDescriptionTuples, latestPostionMatchingRelationshipTuples, position);
                } else if (fullExport) {
                    utility.addComponentTuplesToMaps(concept, matchingConceptTuples, matchingDescriptionTuples,
                            matchingRelationshipTuples, position);
                }
            }

            matchingConceptTuples.addAll(TupleVersionPart.getLatestMatchingTuples(latestPostionMatchingConceptTuples));
            matchingDescriptionTuples.addAll(TupleVersionPart.getLatestMatchingTuples(latestPostionMatchingDescriptionTuples));
            matchingRelationshipTuples.addAll(TupleVersionPart.getLatestMatchingTuples(latestPostionMatchingRelationshipTuples));

            Set<I_ConceptAttributeTuple> latestConceptTuples = new HashSet<I_ConceptAttributeTuple>();
            latestConceptTuples.addAll(TupleVersionPart.getLatestMatchingTuples(matchingConceptTuples));
            for (I_ConceptAttributeTuple tuple : matchingConceptTuples) {
                setConceptDto(componentDto, tuple, latestConceptTuples.contains(tuple));
            }

            setExtensionDto(componentDto.getConceptExtensionDtos(), concept.getConceptId(), TYPE.CONCEPT);

            Set<I_DescriptionTuple> latestDescriptionTuples = new HashSet<I_DescriptionTuple>();
            latestDescriptionTuples.addAll(TupleVersionPart.getLatestMatchingTuples(matchingDescriptionTuples));
            for (I_DescriptionTuple tuple : matchingDescriptionTuples) {
                setDescriptionDto(componentDto, tuple, latestDescriptionTuples.contains(tuple));
            }


            Set<I_RelTuple> latestRelationshipTuples = new HashSet<I_RelTuple>();
            latestRelationshipTuples.addAll(TupleVersionPart.getLatestMatchingTuples(matchingRelationshipTuples));
            for (I_RelTuple tuple : matchingRelationshipTuples) {
                setRelationshipDto(componentDto, tuple, latestRelationshipTuples.contains(tuple));
            }
        }
        return (!matchingConceptTuples.isEmpty()) ? componentDto : null;
    }
}
