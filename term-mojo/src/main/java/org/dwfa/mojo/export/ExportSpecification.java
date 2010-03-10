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
package org.dwfa.mojo.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.dto.BaseConceptDto;
import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.ExtensionDto;
import org.dwfa.dto.IdentifierDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.sctid.SctIdValidator;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartConceptString;
import org.dwfa.vodb.types.ThinExtByRefTuple;
import org.dwfa.vodb.types.ThinExtByRefVersioned;
import org.dwfa.vodb.types.ThinIdPart;

/**
 * Contains the hierarchy to be exports and optionally not exported that are on the exportable positions.
 */
public class ExportSpecification {
    /** Class logger. */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /** Export positions */
    public List<Position> positions;
    /** Included hierarchy */
    public List<I_GetConceptData> inclusions;
    /** Excluded hierarchy - within one or more included hierarchy */
    public List<I_GetConceptData> exclusions;

    /** The active concept. */
    private I_GetConceptData activeConcept;
    /** The active concept. */
    private I_GetConceptData inActiveConcept;
    /** The active concept. */
    private I_GetConceptData currentConcept;
    /** International release path. */
    private I_GetConceptData snomedReleasePath;
    /** SNOMED RT Id concept. */
    private I_GetConceptData snomedRtId;
    /** SNOMED Id concept. */
    private I_GetConceptData snomedIntId;
    /** CTV3 Id concept. */
    private I_GetConceptData ctv3Id;
    /** SNOMED UUID concept. */
    private I_GetConceptData snomedT3Uuid;
    /** Initial case not sensitive concepts. */
    private I_GetConceptData initialCharacterNotCaseSensitive;
    /** Initial all characters sensitive concepts. */
    private I_GetConceptData allCharactersCaseSensitive;
    /** Fully specified name desctiption type. */
    private I_GetConceptData fullySpecifiedDescriptionType;
    /** Int set of fsn type. */
    private I_IntSet fullySpecifiedDescriptionTypeIIntSet = new IntSet();
    /** CTV3 reference set. */
    private I_GetConceptData ctv3IdMapExtension;
    /** Snomed Rt Id reference set. */
    private I_GetConceptData snomedRtIdMapExtension;
    /** concept extension type. */
    private int conceptExtensionNid;
    /** concept string extension type. */
    private int conceptStringExtensionNid;

    /** Ace Workbench inactive status. */
    int aceDuplicateStatusNId;
    int aceAmbiguousStatusNId;
    int aceErroneousStatusNId;
    int aceOutdatedStatusNId;
    int aceInappropriateStatusNId;
    int aceMovedElsewhereStatusNId;
    /** RF2 inactive status. */
    int duplicateStatusNId;
    int ambiguousStatusNId;
    int erroneousStatusNId;
    int outdatedStatusNId;
    int inappropriateStatusNId;
    int movedElsewhereStatusNId;
    /** History relationship types to History relationship reference set map. */
    Map<Integer, Integer> historyStatusRefsetMap = new HashMap<Integer, Integer>();

    int descriptionInactivationIndicatorNid;
    int relationshipInactivationIndicatorNid;
    int conceptInactivationIndicatorNid;

    /** Da factory. */
    private I_TermFactory termFactory;

    private ExtensionProcessor<I_ThinExtByRefPart> extensionProcessor = new ExtensionProcessor<I_ThinExtByRefPart>();

    /**
     * Setup member variables/meta data
     *
     * @throws TerminologyException cannot load meta data concepts
     * @throws IOException cannot load meta data concepts
     */
    public ExportSpecification(List<Position> positions, List<I_GetConceptData> inclusions,
            List<I_GetConceptData> exclusions) throws TerminologyException, IOException {
        termFactory = LocalVersionedTerminology.get();

        activeConcept = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.ACTIVE.localize().getUids().iterator().next());
        inActiveConcept = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.INACTIVE.localize().getUids().iterator().next());
        currentConcept = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.CURRENT.localize().getUids().iterator().next());
        snomedReleasePath = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.SNOMED_CORE.localize().getUids().iterator().next());
        snomedRtId = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.localize().getUids().iterator().next());
        snomedIntId = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getUids().iterator().next());
        ctv3Id = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CTV3_ID.localize().getUids().iterator().next());
        snomedT3Uuid = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.SNOMED_T3_UUID.localize().getUids().iterator().next());
        initialCharacterNotCaseSensitive = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.INITIAL_CHARACTER_NOT_CASE_SENSITIVE.localize().getUids().iterator().next());
        allCharactersCaseSensitive = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.ALL_CHARACTERS_CASE_SENSITIVE.localize().getUids().iterator().next());
        fullySpecifiedDescriptionType = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getUids().iterator().next());
        fullySpecifiedDescriptionTypeIIntSet.add(fullySpecifiedDescriptionType.getConceptId());
        ctv3IdMapExtension = termFactory.getConcept(ConceptConstants.CTV3_ID_MAP_EXTENSION.localize().getNid());
        snomedRtIdMapExtension = termFactory.getConcept(ConceptConstants.SNOMED_ID_MAP_EXTENSION.localize().getNid());
        conceptExtensionNid = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid();
        conceptStringExtensionNid = RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.localize().getNid();
        duplicateStatusNId = ConceptConstants.DUPLICATE_STATUS.localize().getNid();
        ambiguousStatusNId = ConceptConstants.AMBIGUOUS_STATUS.localize().getNid();
        erroneousStatusNId = ConceptConstants.ERRONEOUS_STATUS.localize().getNid();
        outdatedStatusNId = ConceptConstants.OUTDATED_STATUS.localize().getNid();
        inappropriateStatusNId = ConceptConstants.INAPPROPRIATE_STATUS.localize().getNid();
        movedElsewhereStatusNId = ConceptConstants.MOVED_ELSEWHERE_STATUS.localize().getNid();
        aceDuplicateStatusNId = Concept.DUPLICATE.localize().getNid();
        aceAmbiguousStatusNId = Concept.AMBIGUOUS.localize().getNid();
        aceErroneousStatusNId = Concept.ERRONEOUS.localize().getNid();
        aceOutdatedStatusNId = Concept.OUTDATED.localize().getNid();
        aceInappropriateStatusNId = Concept.INAPPROPRIATE.localize().getNid();
        aceMovedElsewhereStatusNId = Concept.MOVED_ELSEWHERE.localize().getNid();
        descriptionInactivationIndicatorNid = ConceptConstants.DESCRIPTION_INACTIVATION_INDICATOR.localize().getNid();
        relationshipInactivationIndicatorNid = ConceptConstants.RELATIONSHIP_INACTIVATION_INDICATOR.localize().getNid();
        conceptInactivationIndicatorNid = ConceptConstants.CONCEPT_INACTIVATION_INDICATOR.localize().getNid();
        historyStatusRefsetMap.put(ConceptConstants.MOVED_FROM_HISTORY.localize().getNid(),
            ConceptConstants.MOVED_FROM_HISTORY_REFSET.localize().getNid());
        historyStatusRefsetMap.put(ConceptConstants.MOVED_TO_HISTORY.localize().getNid(),
            ConceptConstants.MOVED_TO_HISTORY_REFSET.localize().getNid());
        historyStatusRefsetMap.put(ConceptConstants.REPLACED_BY_HISTORY.localize().getNid(),
            ConceptConstants.REPLACED_BY_HISTORY_REFSET.localize().getNid());
        historyStatusRefsetMap.put(ConceptConstants.SAME_AS_HISTORY.localize().getNid(),
            ConceptConstants.SAME_AS_HISTORY_REFSET.localize().getNid());
        historyStatusRefsetMap.put(ConceptConstants.WAS_A_HISTORY.localize().getNid(),
            ConceptConstants.WAS_A_HISTORY_REFSET.localize().getNid());

        setPositions(positions);
        setInclusions(inclusions);
        setExclusions(exclusions);
    }

    /**
     * Return a populated ComponentDto from the I_GetConceptData details.
     * Includes all extensions that this concepts/ description and relationship
     * is a member of.
     *
     * @param concept I_GetConceptData
     *
     * @return ComponentDto
     *
     * @throws Exception DB errors/missing concepts
     */
    public ComponentDto getDataForExport(I_GetConceptData concept) throws Exception {
        ComponentDto componentDto = null;

        if (isIncluded(concept) && !isExcluded(concept)) {
            componentDto = new ComponentDto();
            for (Position position : positions) {
                List<I_ConceptAttributeTuple> matchingConceptTuples = position.getMatchingTuples(concept.getConceptAttributeTuples(true));
                for (I_ConceptAttributeTuple tuple : matchingConceptTuples) {
                    updateComponentDto(componentDto, tuple);
                    for (I_ThinExtByRefVersioned thinExtByRefVersioned : termFactory.getAllExtensionsForComponent(concept.getConceptId())) {
                        List<I_ThinExtByRefTuple> conceptExtensionTuples = position.getMatchingTuples(thinExtByRefVersioned.getTuples(
                            false, true));
                        componentDto.getConceptExtensionDtos().addAll(
                            extensionProcessor.processList(thinExtByRefVersioned, conceptExtensionTuples, TYPE.CONCEPT));
                    }
                }

                List<I_DescriptionTuple> matchingDescriptionTuples = position.getMatchingTuples(concept.getDescriptionTuples(true));
                for (I_DescriptionTuple tuple : matchingDescriptionTuples) {
                    updateComponentDto(componentDto, tuple);
                    for (I_ThinExtByRefVersioned thinExtByRefVersioned : termFactory.getAllExtensionsForComponent(tuple.getDescId())) {
                        List<I_ThinExtByRefTuple> descriptionExtensionTuples = position.getMatchingTuples(thinExtByRefVersioned.getTuples(
                            false, true));
                        componentDto.getDescriptionExtensionDtos().addAll(
                            extensionProcessor.processList(thinExtByRefVersioned, descriptionExtensionTuples, TYPE.DESCRIPTION));
                    }
                }

                List<I_RelTuple> matchingRelationshipTuples = position.getMatchingTuples(concept.getSourceRelTuples(
                    null, false, true));
                for (I_RelTuple tuple : matchingRelationshipTuples) {
                    I_GetConceptData destinationConcept = termFactory.getConcept(tuple.getC2Id());
                    if (isIncluded(destinationConcept) && !isExcluded(destinationConcept)) {
                        updateComponentDto(componentDto, tuple);
                        for (I_ThinExtByRefVersioned thinExtByRefVersioned : termFactory.getAllExtensionsForComponent(tuple.getRelId())) {
                            List<I_ThinExtByRefTuple> relationshipExtensionTuples = position.getMatchingTuples(thinExtByRefVersioned.getTuples(
                                false, true));

                            componentDto.getRelationshipExtensionDtos().addAll(
                                extensionProcessor.processList(thinExtByRefVersioned, relationshipExtensionTuples, TYPE.RELATIONSHIP));
                        }
                    }
                }
            }
        }

        return componentDto;
    }

    /**
     * Update the ComponentDto with the relationship details.
     *
     * @param componentDto ComponentDto - updated with a new relationship
     * @param tuple I_RelTuple - to add to the ComponentDto
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private void updateComponentDto(ComponentDto componentDto, I_RelTuple tuple) throws Exception,
            TerminologyException {
        RelationshipDto relationshipDto = new RelationshipDto();
        List<I_IdPart> idParts = termFactory.getId(tuple.getRelId()).getVersions();

        getBaseConceptDto(relationshipDto, tuple, idParts);

        setComponentInactivationReferenceSet(componentDto.getDescriptionExtensionDtos(), tuple.getRelId(), tuple,
            conceptInactivationIndicatorNid, TYPE.RELATIONSHIP);
        setConceptHistory(componentDto, tuple.getRelVersioned());

        setUuidSctIdIdentifier(relationshipDto, tuple, idParts, TYPE.CONCEPT);

        int snomedCharacter = ArchitectonicAuxiliary.getSnomedCharacteristicTypeId(termFactory.getUids(tuple.getCharacteristicId()));
        relationshipDto.setCharacteristicTypeCode(Character.forDigit(snomedCharacter, 10));
        relationshipDto.setCharacteristicTypeId(termFactory.getUids(tuple.getCharacteristicId()).iterator().next());
        relationshipDto.setConceptId(termFactory.getUids(tuple.getRelId()).iterator().next());
        relationshipDto.setDestinationId(termFactory.getUids(tuple.getC2Id()).iterator().next());
        relationshipDto.setModifierId(ConceptConstants.MODIFIER_SOME.getUuids()[0]);
        int refinableChar = ArchitectonicAuxiliary.getSnomedRefinabilityTypeId(termFactory.getUids(tuple.getRefinabilityId()));
        relationshipDto.setRefinable(Character.forDigit(refinableChar, 10));
        relationshipDto.setRelationshipGroupCode(Character.forDigit(tuple.getGroup(), 10));
        relationshipDto.setSourceId(termFactory.getUids(tuple.getC1Id()).iterator().next());
        relationshipDto.setType(TYPE.RELATIONSHIP);
        relationshipDto.setTypeId(termFactory.getUids(tuple.getTypeId()).iterator().next());

        componentDto.getRelationshipDtos().add(relationshipDto);
    }

    /**
     * Update the ComponentDto with the description details.
     *
     * @param componentDto ComponentDto - updated with the description details
     * @param tuple I_DescriptionTuple - to add to the ComponentDto
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private void updateComponentDto(ComponentDto componentDto, I_DescriptionTuple tuple) throws IOException,
            TerminologyException {
        DescriptionDto descriptionDto = new DescriptionDto();
        List<I_IdPart> idParts = termFactory.getId(tuple.getDescId()).getVersions();

        getBaseConceptDto(descriptionDto, tuple, idParts);

        setComponentInactivationReferenceSet(componentDto.getDescriptionExtensionDtos(), tuple.getDescId(), tuple,
            conceptInactivationIndicatorNid, TYPE.DESCRIPTION);

        setUuidSctIdIdentifier(descriptionDto, tuple, idParts, TYPE.CONCEPT);

        descriptionDto.setCaseSignificanceId(getInitialCaseSignificant(tuple.getInitialCaseSignificant()));
        descriptionDto.setConceptId(termFactory.getUids(tuple.getConceptId()).iterator().next());
        descriptionDto.setDescription(tuple.getText());
        descriptionDto.setDescriptionId(termFactory.getUids(tuple.getDescId()).iterator().next());
        descriptionDto.setDescriptionTypeCode(Character.forDigit(
            ArchitectonicAuxiliary.getSnomedDescriptionTypeId(termFactory.getUids(tuple.getTypeId())), 10));
        descriptionDto.setInitialCapitalStatusCode(tuple.getInitialCaseSignificant() ? '1' : '0');
        descriptionDto.setLanguageCode(tuple.getLang());
        descriptionDto.setLanguageId(ArchitectonicAuxiliary.getLanguageConcept(tuple.getLang()).getUids().iterator().next());
        descriptionDto.setNamespace(getNamespace(idParts, tuple));
        descriptionDto.setType(TYPE.DESCRIPTION);
        descriptionDto.setTypeId(termFactory.getConcept(tuple.getTypeId()).getUids().get(0));

        componentDto.getDescriptionDtos().add(descriptionDto);
    }

    /**
     * Update the ComponentDto with the concept details.
     *
     * @param componentDto ComponentDto - updated with the concept details
     * @param tuple I_ConceptAttributeTuple - to add to the ComponentDto
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private ComponentDto updateComponentDto(ComponentDto componentDto, I_ConceptAttributeTuple tuple)
            throws IOException, TerminologyException {
        I_GetConceptData conceptData = termFactory.getConcept(tuple.getConId());
        componentDto.getConceptDto().setConceptId(conceptData.getUids().get(0));

        getBaseConceptDto(componentDto.getConceptDto(), tuple, conceptData.getId().getVersions());

        I_IdPart ctv3IdPart = getLatesIdtVersion(conceptData.getId().getVersions(), ctv3Id.getConceptId(), tuple);
        if (ctv3IdPart != null) {
            componentDto.getConceptDto().setCtv3Id(ctv3IdPart.getSourceId().toString());
            setCtv3ReferenceSet(componentDto, tuple, ctv3IdPart);
        } else {
            componentDto.getConceptDto().setCtv3Id("");
        }

        I_IdPart snomedIdPart = getLatesIdtVersion(conceptData.getId().getVersions(), snomedRtId.getConceptId(), tuple);
        if(snomedIdPart != null){
            componentDto.getConceptDto().setSnomedId(snomedIdPart.getSourceId().toString());
            setSnomedRtIdReferenceSet(componentDto, tuple, snomedIdPart);
        } else {
            componentDto.getConceptDto().setSnomedId("");
        }

        setComponentInactivationReferenceSet(componentDto.getConceptExtensionDtos(), tuple.getConId(), tuple,
            conceptInactivationIndicatorNid, TYPE.CONCEPT);

        setUuidSctIdIdentifier(componentDto.getConceptDto(), tuple, conceptData.getId().getVersions(), TYPE.CONCEPT);

        componentDto.getConceptDto().setFullySpecifiedName(
            getTupleVersion(conceptData.getDescriptionTuples(null, fullySpecifiedDescriptionTypeIIntSet, null, true),
                tuple).getText());
        componentDto.getConceptDto().setPrimative(tuple.isDefined());
        componentDto.getConceptDto().setType(TYPE.CONCEPT);

        return componentDto;
    }

    /**
     * Checks if the I_RelVersioned a history relationship type, if so export
     * the details to corresponding history type refset.
     *
     * All versions of the relationship are exported.
     *
     * @param versionedRel I_RelVersioned to create the history refset for.
     * @throws Exception
     */
    private void setConceptHistory(ComponentDto componentDto, I_RelVersioned versionedRel) throws Exception {
        for (I_RelPart versionPart : versionedRel.getVersions()) {
            if (historyStatusRefsetMap.containsKey(versionPart.getTypeId())) {
                List<I_ThinExtByRefTuple> list = new ArrayList<I_ThinExtByRefTuple>();

                I_ThinExtByRefTuple extensionTuple = createThinExtByRefTuple(historyStatusRefsetMap.get(versionPart.getTypeId()), 0,
                    versionedRel.getC2Id(), versionPart);
                list.add(extensionTuple);

                componentDto.getRelationshipExtensionDtos().addAll(
                    extensionProcessor.processList(extensionTuple.getCore(), list, TYPE.CONCEPT));
            }
        }
    }

    /**
     * Adds the component to the <code>inactivationIndicatorRefsetNid</code> if the component is inactive.
     *
     * @param componentDto ComponentDto
     * @param tuple I_ConceptAttributeTuple
     * @param inactivationIndicatorRefsetNid int
     * @param type TYPE
     * @throws IOException
     * @throws TerminologyException
     */
    private void setComponentInactivationReferenceSet(List<ExtensionDto> extensionDtos, int componentNid, I_AmTuple tuple,
            int inactivationIndicatorRefsetNid, TYPE type) throws IOException,TerminologyException {
        int rf2InactiveStatus = getRf2Status(tuple.getStatusId());
        if (rf2InactiveStatus != -1) {
            // if the status is INACTIVE or ACTIVE there is no need for a
            // reason. For simplicity, CURRENT will be treated this way too,
            if (tuple.getStatusId() != activeConcept.getNid()
                && tuple.getStatusId() != inActiveConcept.getNid()
                && tuple.getStatusId() != currentConcept.getNid()) {
                List<I_ThinExtByRefTuple> list = new ArrayList<I_ThinExtByRefTuple>();

                I_ThinExtByRefTuple extensionTuple = createThinExtByRefTuple(inactivationIndicatorRefsetNid, 0,
                    componentNid, tuple);
                list.add(extensionTuple);

                extensionDtos.addAll(extensionProcessor.processList(extensionTuple.getCore(), list, type));
            }
        }
    }

    /**
     * Adds the ctv3 to the ctv3 reference set.
     *
     * @param componentDto ComponentDto
     * @param tuple I_ConceptAttributeTuple
     * @param ctv3IdPart I_IdPart
     * @throws IOException
     * @throws TerminologyException
     */
    private void setCtv3ReferenceSet(ComponentDto componentDto, I_ConceptAttributeTuple tuple, I_IdPart ctv3IdPart)
            throws IOException, TerminologyException {
        List<I_ThinExtByRefTuple> list = new ArrayList<I_ThinExtByRefTuple>();

        I_ThinExtByRefTuple ctv3tuple = createThinExtByRefTuple(ctv3IdMapExtension.getConceptId(), 0, tuple.getConId(),
            ctv3IdPart, ctv3IdPart.getSourceId().toString());
        list.add(ctv3tuple);

        componentDto.getConceptExtensionDtos().addAll(
            extensionProcessor.processList(ctv3tuple.getCore(), list, TYPE.CONCEPT));
    }

    /**
     * Adds the snomed id to the snomed id reference set.
     *
     * @param componentDto ComponentDto
     * @param tuple I_ConceptAttributeTuple
     * @param snomedIdPart I_IdPart
     * @throws IOException
     * @throws TerminologyException
     */
    private void setSnomedRtIdReferenceSet(ComponentDto componentDto, I_ConceptAttributeTuple tuple, I_IdPart snomedIdPart)
            throws IOException, TerminologyException {
        List<I_ThinExtByRefTuple> list = new ArrayList<I_ThinExtByRefTuple>();

        I_ThinExtByRefTuple snomedIdtuple = createThinExtByRefTuple(snomedRtIdMapExtension.getConceptId(), 0,
            tuple.getConId(), snomedIdPart, snomedIdPart.getSourceId().toString());
        list.add(snomedIdtuple);

        componentDto.getConceptExtensionDtos().addAll(
            extensionProcessor.processList(snomedIdtuple.getCore(), list, TYPE.CONCEPT));
    }

    /**
     *
     * @param refsetId
     * @param memberId
     * @param componentId
     * @param amPart
     * @return
     */
    private I_ThinExtByRefTuple createThinExtByRefTuple(int refsetId, int memberId, int componentId,
            I_AmPart amPart) {
        ThinExtByRefVersioned thinExtByRefVersioned = new ThinExtByRefVersioned(refsetId, memberId, componentId,
            conceptExtensionNid);

        I_ThinExtByRefPartConcept conceptExtension = new ThinExtByRefPartConcept();
        conceptExtension.setC1id(componentId);
        conceptExtension.setPathId(amPart.getPathId());
        conceptExtension.setPathId(amPart.getPathId());
        conceptExtension.setStatusId(amPart.getStatusId());
        conceptExtension.setVersion(amPart.getVersion());

        I_ThinExtByRefTuple ctv3tuple = new ThinExtByRefTuple(thinExtByRefVersioned, conceptExtension);

        return ctv3tuple;
    }

    private I_ThinExtByRefTuple createThinExtByRefTuple(int refsetId, int memberId, int componentId,
            I_AmPart amPart, String string) {
        ThinExtByRefVersioned thinExtByRefVersioned = new ThinExtByRefVersioned(refsetId, memberId, componentId,
            conceptStringExtensionNid);

        I_ThinExtByRefPartConceptString conceptExtension = new ThinExtByRefPartConceptString();
        conceptExtension.setC1id(componentId);
        conceptExtension.setStr(string);
        conceptExtension.setPathId(amPart.getPathId());
        conceptExtension.setPathId(amPart.getPathId());
        conceptExtension.setStatusId(amPart.getStatusId());
        conceptExtension.setVersion(amPart.getVersion());

        I_ThinExtByRefTuple tuple = new ThinExtByRefTuple(thinExtByRefVersioned, conceptExtension);

        return tuple;
    }

    /**
     * Gets the active status, date time, namespace, path and status id for a
     * component (concept, description, relationship or extension).
     *
     * @param baseConceptDto ConceptDto
     * @param tuple I_AmTuple
     * @return ConceptDto
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private BaseConceptDto getBaseConceptDto(BaseConceptDto baseConceptDto, I_AmPart tuple, List<I_IdPart> idVersions) throws IOException, TerminologyException {
        baseConceptDto.setActive(isActive(tuple.getStatusId()));
        baseConceptDto.setDateTime(new Date(tuple.getTime()));
        baseConceptDto.setNamespace(getNamespace(idVersions,tuple));
        baseConceptDto.setPathId(termFactory.getConcept(tuple.getPathId()).getUids().get(0));
        baseConceptDto.setStatusId(termFactory.getConcept(tuple.getStatusId()).getUids().get(0));

        return baseConceptDto;
    }

    /**
     * Adds an identifier to the ConceptDto.
     *
     * @param conceptDto ConceptDto
     * @param tuple I_ConceptAttributeTuple
     * @param conceptData I_GetConceptData
     * @param type

     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private void setUuidSctIdIdentifier(ConceptDto conceptDto, I_AmPart tuple,
            List<I_IdPart> idVersions, TYPE type) throws TerminologyException, IOException {
        I_IdPart uuidPart = getLatesIdtVersion(idVersions, snomedT3Uuid.getConceptId(), tuple);
        I_IdPart sctIdPart = getLatesIdtVersion(idVersions, snomedIntId.getConceptId(), tuple);

        if (uuidPart != null && sctIdPart != null) {
            IdentifierDto identifierDto = new IdentifierDto();
            getBaseConceptDto(identifierDto, tuple, idVersions);
            identifierDto.setType(type);

            identifierDto.setActive(isActive(sctIdPart.getStatusId()));
            identifierDto.setConceptId(UUID.fromString(uuidPart.getSourceId().toString()));
            identifierDto.setReferencedSctId(Long.valueOf(sctIdPart.getSourceId().toString()));
            identifierDto.setIdentifierSchemeUuid(snomedIntId.getUids().get(0));

            conceptDto.getIdentifierDtos().add(identifierDto);
        }
    }

    /**
     * Get the UUID for case significances.
     *
     * @param isInitialCaseSignificant boolean
     * @return UUID
     * @throws IOException DB errors
     */
    private UUID getInitialCaseSignificant(boolean isInitialCaseSignificant) throws IOException {
        if (isInitialCaseSignificant) {
            return allCharactersCaseSensitive.getUids().get(0);
        } else {
            return initialCharacterNotCaseSensitive.getUids().get(0);
        }
    }

    /**
     * Get the SNOMED name space for the tuple based on the SCTID of the concept
     * or if no SCTID the use the path.
     *
     * @param uuid UUID of the concept
     * @param tuple I_AmPart
     *
     * @return NAMESPACE
     *
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private NAMESPACE getNamespace(List<I_IdPart> idVersions, I_AmPart tuple) throws IOException, TerminologyException {
        NAMESPACE namespace;
        I_IdPart sctIdPart = getLatesIdtVersion(idVersions, snomedIntId.getConceptId(),
            tuple);
        if (sctIdPart != null) {
            namespace = SctIdValidator.getInstance().getSctIdNamespace(sctIdPart.getSourceId().toString());
        } else {
            if (isInternationalPath(tuple.getPathId())) {
                namespace = NAMESPACE.SNOMED_META_DATA;
            } else {
                namespace = NAMESPACE.NEHTA;
            }
        }

        return namespace;
    }

    /**
     * Is the path the SNOMED international path
     * @param pathNid int
     *
     * @return true if path is snomedReleasePath or child of
     *
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private boolean isInternationalPath(int pathNid) throws IOException, TerminologyException {
        boolean internationPath = false;
        I_GetConceptData pathConcept = termFactory.getConcept(pathNid);

        if (snomedReleasePath.isParentOf(pathConcept, false)) {
            internationPath = true;
        } else if (snomedReleasePath.equals(pathConcept)) {
            internationPath = true;
        }

        return internationPath;
    }

    /**
     * Checks is the uuidStatus equals the Concept.ACTIVE uuid.
     * or is a child of Concept.ACTIVE
     *
     * @param uuid String
     * @return String 1 if the uuidStatus is active otherwise 0;
     * @throws TerminologyException DB error
     * @throws IOException DB error
     */
    private boolean isActive(final int statusNid) throws IOException, TerminologyException {
        boolean activate = false;
        I_GetConceptData statusConcept = termFactory.getConcept(statusNid);

        if (activeConcept.isParentOf(statusConcept, false)) {
            activate = true;
        } else if (activeConcept.getNid() == statusConcept.getNid()) {
            activate = true;
        }

        return activate;
    }

    /**
     * covert the snomed CT component status to rf2 meta-data status
     *
     * @param statusId int
     * @return int rf2 status or -1 if no map found.
     */
    private int getRf2Status(int statusId) {
        int rf2Status = -1;

        if (statusId == aceDuplicateStatusNId) {
            rf2Status = duplicateStatusNId;
        } else if (statusId == aceAmbiguousStatusNId) {
            rf2Status = ambiguousStatusNId;
        } else if (statusId == aceErroneousStatusNId) {
            rf2Status = erroneousStatusNId;
        } else if (statusId == aceOutdatedStatusNId) {
            rf2Status = outdatedStatusNId;
        } else if (statusId == aceInappropriateStatusNId) {
            rf2Status = inappropriateStatusNId;
        } else if (statusId == aceMovedElsewhereStatusNId) {
            rf2Status = movedElsewhereStatusNId;
        }

        return rf2Status;
    }

    /**
     * Gets the latest tuple version that is not newer than <code>tuple</code>.
     *
     * @param conceptTuples list of I_AmTuple
     * @param tuple I_AmTuple
     * @return I_AmTuple
     * @throws IOException DB error
     * @throws TerminologyException DB error
     */
    private <T extends I_AmTuple> T getTupleVersion(List<T> conceptTuples, I_AmTuple tuple) throws IOException,
            TerminologyException {
        T tupleVersion = null;

        for (T curTupleVersion : conceptTuples) {
            if (curTupleVersion.getVersion() <= tuple.getVersion()) {
                if (tupleVersion == null || tupleVersion.getVersion() < curTupleVersion.getVersion()) {
                    tupleVersion = curTupleVersion;
                }
            }
        }

        return tupleVersion;
    }

    /**
     * Get the latest version for the list of parts with the source id concept
     * <code>sourceConcept</code>
     *
     * @param sourceConcept Concept eg CTV3_ID, SNOMED_RT_ID etc
     * @return I_IdPart latest Id version for the sourceConcept.
     * @throws IOException DB errors
     */
    private I_IdPart getLatesIdtVersion(List<I_IdPart> idParts, int sourceIdConceptNid, I_AmPart attributeTuple)
            throws TerminologyException, IOException, NoMappingException {
        I_IdPart latestVersion = null;

        for (I_IdPart iIdPart : idParts) {
            if (iIdPart.getSource() == sourceIdConceptNid
                && ((latestVersion == null && iIdPart.getVersion() <= attributeTuple.getVersion()) || (latestVersion != null
                    && iIdPart.getVersion() > latestVersion.getVersion() && latestVersion.getVersion() <= attributeTuple.getVersion()))) {
                latestVersion = iIdPart;
            }
        }

        return latestVersion;
    }

    /**
     * Is the concept an included hierarchy or a child element of.
     * @param concept I_GetConceptData
     *
     * @return boolean
     *
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private boolean isIncluded(I_GetConceptData concept) throws IOException, TerminologyException {
        boolean includedConcept = false;

        for (I_GetConceptData includeConceptData : inclusions) {
            if (includeConceptData.getNid() == concept.getNid()) {
                includedConcept = true;
                break;
            }
            if (includeConceptData.isParentOf(concept, false)) {
                includedConcept = true;
                break;
            }
        }

        return includedConcept;
    }

    /**
     * Is the concept an exclusions hierarchy or a child element of.
     *
     * @param concept I_GetConceptData
     *
     * @return boolean
     *
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private boolean isExcluded(I_GetConceptData concept) throws IOException, TerminologyException {
        boolean excludedConcept = false;

        for (I_GetConceptData excludedConceptData : exclusions) {
            if (excludedConceptData.getNid() == concept.getNid()) {
                excludedConcept = true;
                break;
            }
            if (excludedConceptData.isParentOf(concept, false)) {
                excludedConcept = true;
                break;
            }
        }

        return excludedConcept;
    }

    /**
     * @param positions the positions to set
     */
    public final void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    /**
     * @param inclusions the inclusions to set
     */
    public final void setInclusions(List<I_GetConceptData> inclusions) {
        this.inclusions = inclusions;
    }

    /**
     * @param exclusions the exclusions to set
     */
    public final void setExclusions(List<I_GetConceptData> exclusions) {
        this.exclusions = exclusions;
    }

    /**
     * Processes each of the extension types that extend I_ThinExtByRefPart.
     *
     * @param <T>
     */
    private class ExtensionProcessor<T extends I_ThinExtByRefPart> {

        /** Map of extension handlers for each extension type. */
        private Map<Integer, I_AmExtensionProcessor<T>> extensionMap =
                new HashMap<Integer, I_AmExtensionProcessor<T>>();

        /**
         * Create the <code>extensionMap</code>.
         */
        public ExtensionProcessor() {
            try {
                extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid(), new ConceptExtensionProcessor());
                extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.localize().getNid(), new ConceptStringExtensionProcessor());
                extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.localize().getNid(), new ConceptIntegerExtensionProcessor());
                extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid(), new ConceptConceptExtensionProcessor());
                extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.localize().getNid(), new ConceptConceptStringExtensionProcessor());
                extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize().getNid(), new ConceptConceptConceptExtensionProcessor());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TerminologyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * For each part of the reference set member get a ExtensionDto
         *
         * @param thinExtByRefVersioned I_ThinExtByRefVersioned the reference set member
         * @param list List of I_ThinExtByRefPart
         * @return List of ExtensionDto
         * @throws IOException DB errors
         * @throws TerminologyException DB errors
         */
        public List<ExtensionDto> processList(I_ThinExtByRefVersioned thinExtByRefVersioned, List<I_ThinExtByRefTuple> list, TYPE type) throws IOException, TerminologyException {
            List<ExtensionDto> extensionDtos = new ArrayList<ExtensionDto>();

            for (I_ThinExtByRefTuple t : list) {
                I_GetConceptData refsetConcept = termFactory.getConcept(t.getRefsetId());
                if (isIncluded(refsetConcept) && !isExcluded(refsetConcept)) {
                    ExtensionDto extensionDto = extensionMap.get(t.getTypeId()).getExtensionDto(thinExtByRefVersioned, (T) t.getPart(), type);

                    extensionDtos.add(extensionDto);
                }
            }

            return extensionDtos;
        }
    }

    /**
     * Extension processing interface.
     *
     * @param <T> extends I_ThinExtByRefPart
     */
    private interface I_AmExtensionProcessor<T extends I_ThinExtByRefPart> {
        ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned, T tuple, TYPE type)  throws IOException, TerminologyException;
    }

    /**
     * Extension processor for  I_ThinExtByRefPartConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConcept
     */
    private class ConceptExtensionProcessor<T extends I_ThinExtByRefPartConcept> implements
            I_AmExtensionProcessor<I_ThinExtByRefPartConcept> {

        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPartConcept tuple, TYPE type) throws IOException, TerminologyException {
            ExtensionDto extensionDto = new ExtensionDto();
            List<I_IdPart> idParts;

            if(thinExtByRefVersioned.getMemberId() != 0){
                idParts = termFactory.getId(thinExtByRefVersioned.getMemberId()).getVersions();
            } else {
                idParts = new ArrayList<I_IdPart>(1);
                idParts.add(getIdUuidSctIdPart(thinExtByRefVersioned, tuple));
            }

            getBaseConceptDto(extensionDto, tuple, idParts);

            setUuidSctIdIdentifier(extensionDto, tuple, idParts, TYPE.REFSET);

            extensionDto.setConceptId(termFactory.getUids(thinExtByRefVersioned.getRefsetId()).iterator().next());
            extensionDto.setConcept1Id(termFactory.getUids(tuple.getC1id()).iterator().next());
            extensionDto.setMemberId(getUuid(tuple.getC1id(), thinExtByRefVersioned));
            extensionDto.setNamespace(getNamespace(idParts, tuple));
            extensionDto.setType(type);

            return extensionDto;
        }

        /**
         * Create a UUID id part for export. The UUID will be the same for the refset id and concept 1 id.
         *
         * @param thinExtByRefVersioned I_ThinExtByRefVersioned
         * @param tuple I_ThinExtByRefPartConcept
         * @return I_IdPart containing the UUID
         * @throws TerminologyException
         * @throws IOException
         */
        private I_IdPart getIdUuidSctIdPart(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPartConcept tuple) throws TerminologyException, IOException {
            I_IdPart sctUuidPart = new ThinIdPart();
            sctUuidPart.setStatusId(currentConcept.getNid());
            sctUuidPart.setPathId(tuple.getPathId());
            sctUuidPart.setSource(ctv3IdMapExtension.getNid());
            sctUuidPart.setVersion(tuple.getVersion());
            sctUuidPart.setSourceId(getUuid(tuple.getC1id(), thinExtByRefVersioned));

            return sctUuidPart;
        }

        /**
         * Gets the UUID for the member. If member id is 0 a new UUID is create.
         *
         * @param componentId member component
         * @param thinExtByRefVersioned I_ThinExtByRefVersioned
         * @return UUID
         * @throws TerminologyException
         * @throws IOException
         */
        private UUID getUuid(int componentId, I_ThinExtByRefVersioned thinExtByRefVersioned)
                throws TerminologyException, IOException {
            UUID uuid = null;

            if (thinExtByRefVersioned.getMemberId() != 0) {
                uuid = termFactory.getUids(thinExtByRefVersioned.getMemberId()).iterator().next();
            } else {
                uuid = UUID.nameUUIDFromBytes(("org.dwfa." + termFactory.getUids(componentId)
                        + termFactory.getUids(thinExtByRefVersioned.getRefsetId())).getBytes("8859_1"));
            }
            return uuid;
        }
    }

    /**
     * Extension processor for  I_ThinExtByRefPartConceptConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConceptConcept
     */
    private class ConceptStringExtensionProcessor<T extends I_ThinExtByRefPartConceptString> implements
            I_AmExtensionProcessor<I_ThinExtByRefPartConceptString> {
        ConceptExtensionProcessor<T> conceptExtensionProcessor = new ConceptExtensionProcessor<T>();

        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPartConceptString tuple, TYPE type) throws IOException, TerminologyException {
            ExtensionDto extensionDto = conceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type);

            extensionDto.setValue(tuple.getStr());

            return extensionDto;
        }
    }

    /**
     * Extension processor for  I_ThinExtByRefPartConceptConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConceptConcept
     */
    private class ConceptIntegerExtensionProcessor<T extends I_ThinExtByRefPartConceptInt> implements
            I_AmExtensionProcessor<I_ThinExtByRefPartConceptInt> {
        ConceptExtensionProcessor<T> conceptExtensionProcessor = new ConceptExtensionProcessor<T>();

        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPartConceptInt tuple, TYPE type) throws IOException, TerminologyException {
            ExtensionDto extensionDto = conceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type);

            extensionDto.setValue(tuple.getIntValue() + "");

            return extensionDto;
        }
    }

    /**
     * Extension processor for  I_ThinExtByRefPartConceptConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConceptConcept
     */
    private class ConceptConceptExtensionProcessor<T extends I_ThinExtByRefPartConceptConcept> implements
            I_AmExtensionProcessor<I_ThinExtByRefPartConceptConcept> {
        ConceptExtensionProcessor<T> conceptExtensionProcessor = new ConceptExtensionProcessor<T>();

        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPartConceptConcept tuple, TYPE type) throws IOException, TerminologyException {
            ExtensionDto extensionDto = conceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type);

            extensionDto.setConcept2Id(termFactory.getConcept(tuple.getC2id()).getUids().get(0));

            return extensionDto;
        }
    }

    /**
     * Extension processor for  I_ThinExtByRefPartConceptConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConceptConcept
     */
    private class ConceptConceptStringExtensionProcessor<T extends I_ThinExtByRefPartConceptConceptString> implements
            I_AmExtensionProcessor<I_ThinExtByRefPartConceptConceptString> {
        ConceptConceptExtensionProcessor<T> conceptConceptExtensionProcessor = new ConceptConceptExtensionProcessor<T>();

        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPartConceptConceptString tuple, TYPE type) throws IOException, TerminologyException {
            ExtensionDto extensionDto = conceptConceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type);

            extensionDto.setValue(tuple.getStringValue());

            return extensionDto;
        }
    }

    /**
     * Extension processor for  I_ThinExtByRefPartConceptConceptConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConceptConceptConcept
     */
    private class ConceptConceptConceptExtensionProcessor<T extends I_ThinExtByRefPartConceptConceptConcept> implements
            I_AmExtensionProcessor<I_ThinExtByRefPartConceptConceptConcept> {
        ConceptConceptExtensionProcessor<T> conceptConceptExtensionProcessor = new ConceptConceptExtensionProcessor<T>();

        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPartConceptConceptConcept tuple, TYPE type) throws IOException, TerminologyException {
            ExtensionDto extensionDto = conceptConceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type);

            extensionDto.setConcept3Id(termFactory.getConcept(tuple.getC3id()).getUids().get(0));

            return extensionDto;
        }
    }
}
