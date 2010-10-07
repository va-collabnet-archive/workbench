/*
 *  Copyright 2010 International Health Terminology Standards Development  *  Organisation..
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.dwfa.mojo.export.amt;

import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.mojo.export.AbstractComponentDtoUpdater;
import org.dwfa.tapi.TerminologyException;

/**
 * {@code AmtComponentDtoUpdater} is an implementation of {@link AbstractComponentDtoUpdater} for performing AMT
 * specific DTO updating functions.
 * @author Matthew Edwards
 */
public final class AmtComponentDtoUpdater extends AbstractComponentDtoUpdater {

    /**
     * Creates an instance of {@code AmtComponentDtoUpdater} for performing DTO update functions for AMT.
     * @param defaultNamespace the Name space of the data.
     * @param defaultProject the Name space of the data.
     * @throws Exception if there is an error accessing the terminology from the Internal Term Factory.
     */
    public AmtComponentDtoUpdater(final NAMESPACE defaultNamespace, final PROJECT defaultProject) throws Exception {
        super(defaultNamespace, defaultProject);

        I_GetConceptData erroneous = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getUids().iterator().next());

        I_GetConceptData conceptRetired = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getUids().iterator().next());

         this.check = new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired);
    }

    @Override
    public ComponentDto updateComponentDto(final ComponentDto componentDto, final I_ConceptAttributeTuple tuple, Collection<I_DescriptionTuple> descriptionTuples,
            final boolean latest) throws Exception {
           ConceptDto conceptDto = new ConceptDto();
        I_GetConceptData conceptData = termFactory.getConcept(tuple.getConId());
        conceptDto.setConceptId(getIdMap(tuple, tuple.getConId()));

        this.getBaseConceptDto(conceptDto, tuple, conceptData.getId().getVersions(), latest);

        this.addUuidSctIdIndentifierToConceptDto(
                conceptDto, tuple, conceptData.getId().getVersions(), TYPE.CONCEPT, tuple.getConId(), latest);

        conceptDto.setFullySpecifiedName(getFsn(descriptionTuples));

        conceptDto.setPrimative(!tuple.isDefined());
        conceptDto.setDefinitionStatusUuid(getDefinitionStatusUuid(tuple.isDefined()));
        conceptDto.setType(TYPE.CONCEPT);

        componentDto.getConceptDtos().add(conceptDto);

        return componentDto;
    }

    @Override
    public void updateComponentDto(final ComponentDto componentDto, final I_RelTuple tuple, final boolean latest)
            throws Exception, TerminologyException {
          RelationshipDto relationshipDto = new RelationshipDto();
        List<I_IdPart> idParts = termFactory.getId(tuple.getRelId()).getVersions();

        this.getBaseConceptDto(relationshipDto, tuple, idParts, latest);

        this.addUuidSctIdIndentifierToConceptDto(relationshipDto, tuple, idParts, TYPE.RELATIONSHIP, tuple.getRelId(),
                latest);

        int snomedCharacter = ArchitectonicAuxiliary.getSnomedCharacteristicTypeId(termFactory.getUids(tuple.
                getCharacteristicId()));
        relationshipDto.setCharacteristicTypeCode(Character.forDigit(snomedCharacter, 10));
        relationshipDto.setCharacteristicTypeId(termFactory.getUids(tuple.getCharacteristicId()).iterator().next());
        relationshipDto.setConceptId(getIdMap(tuple, tuple.getRelId()));
        relationshipDto.setDestinationId(getIdMap(tuple, tuple.getC2Id()));
        relationshipDto.setModifierId(ConceptConstants.MODIFIER_SOME.getUuids()[0]);
        relationshipDto.setRefinabilityId(termFactory.getUids(tuple.getRefinabilityId()).iterator().next());
        int refinableChar = ArchitectonicAuxiliary.getSnomedRefinabilityTypeId(termFactory.getUids(tuple.
                getRefinabilityId()));
        relationshipDto.setRefinable(Character.forDigit(refinableChar, 10));
        relationshipDto.setRelationshipGroup(tuple.getGroup());
        relationshipDto.setSourceId(termFactory.getUids(tuple.getC1Id()).iterator().next());
        relationshipDto.setType(TYPE.RELATIONSHIP);
        relationshipDto.setTypeId(termFactory.getUids(tuple.getTypeId()).iterator().next());

        componentDto.getRelationshipDtos().add(relationshipDto);
    }
}
