/*
 *  Copyright 2010 matt.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.util.TupleVersionComparator;
import org.dwfa.ace.util.TupleVersionPart;
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
 *
 * @author matt
 */
public final class AmtComponentDtoUpdater extends AbstractComponentDtoUpdater {

    public AmtComponentDtoUpdater(NAMESPACE defaultNamespace, PROJECT defaultProject) throws Exception {
        super(defaultNamespace, defaultProject);

        I_GetConceptData erroneous = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getUids().iterator().next());

        I_GetConceptData conceptRetired = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getUids().iterator().next());
        
         this.check = new AmtStatusChecker(activeConcept, currentConcept, erroneous, conceptRetired);
    }

    @Override
    public ComponentDto updateComponentDto(ComponentDto componentDto, I_ConceptAttributeTuple tuple, boolean latest) throws Exception {
           ConceptDto conceptDto = new ConceptDto();
        I_GetConceptData conceptData = termFactory.getConcept(tuple.getConId());
        conceptDto.setConceptId(getIdMap(tuple, tuple.getConId()));

        this.getBaseConceptDto(conceptDto, tuple, conceptData.getId().getVersions(), latest);

        this.addUuidSctIdIndentifierToConceptDto(conceptDto, tuple, conceptData.getId().getVersions(), TYPE.CONCEPT, tuple.getConId(), latest);

        List<I_DescriptionTuple> descriptionTuples = new ArrayList<I_DescriptionTuple>();
        descriptionTuples.addAll(TupleVersionPart.getLatestMatchingTuples(conceptData.getDescriptionTuples(null, fullySpecifiedDescriptionTypeIntSet, null, true)));
        Collections.sort(descriptionTuples, new TupleVersionComparator());

        String fsn = "NO FSN!!!";
        I_DescriptionTuple fsnTuple = null;
        if (!descriptionTuples.isEmpty()) {
            for (I_DescriptionTuple iDescriptionTuple : descriptionTuples) {
                if (check.isDescriptionActive(iDescriptionTuple.getStatusId()) || iDescriptionTuple.getStatusId() == aceLimitedStatusNId) {
                    if (fsnTuple == null || fsnTuple.getVersion() < iDescriptionTuple.getVersion()) {
                        fsnTuple = iDescriptionTuple;
                    }
                }
            }

            //If no active FSN get the latest inactive FSN
            if (fsnTuple != null) {
                fsn = fsnTuple.getText();
            } else {
                fsn = descriptionTuples.iterator().next().getText();
            }
        } else {
            logger.severe(String.format("No FSN for: %1$s concept %2$s",
                    tuple.getVersion(), termFactory.getConcept(conceptData.getNid())));
        }
        conceptDto.setFullySpecifiedName(fsn);

        conceptDto.setPrimative(!tuple.isDefined());
        conceptDto.setDefinitionStatusUuid(getDefinitionStatusUuid(tuple.isDefined()));
        conceptDto.setType(TYPE.CONCEPT);

        componentDto.getConceptDtos().add(conceptDto);

        return componentDto;
    }

    @Override
    public void updateComponentDto(ComponentDto componentDto, I_RelTuple tuple, boolean latest) throws Exception, TerminologyException {
          RelationshipDto relationshipDto = new RelationshipDto();
        List<I_IdPart> idParts = termFactory.getId(tuple.getRelId()).getVersions();

        this.getBaseConceptDto(relationshipDto, tuple, idParts, latest);

        this.addUuidSctIdIndentifierToConceptDto(relationshipDto, tuple, idParts, TYPE.RELATIONSHIP, tuple.getRelId(), latest);

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
