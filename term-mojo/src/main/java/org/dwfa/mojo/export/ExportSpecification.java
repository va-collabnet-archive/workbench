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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
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
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.dwfa.vodb.types.ThinExtByRefTuple;
import org.dwfa.vodb.types.ThinExtByRefVersioned;
import org.dwfa.vodb.types.ThinIdPart;

/**
 * Contains the hierarchy to be exports and optionally not exported that are on the exportable positions.
 */
public class ExportSpecification {
    /**
     * Class logger.
     */
    private Logger logger = Logger.getLogger(ExportSpecification.class.getName());

    /** Export positions */
    public List<Position> positions;
    /** Included hierarchy */
    public List<I_GetConceptData> inclusions;
    /** Excluded hierarchy - within one or more included hierarchy */
    public List<I_GetConceptData> exclusions;
    /**The default namespace to use for export*/
    private NAMESPACE defaultNamespace;

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
    /** UUID Source concept. */
    private I_GetConceptData unspecifiedUuid;
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
    /** concept string extension type. */
    private int stringExtensionNid;
    /** concept extension type. */
    private int conceptExtensionNid;

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
    /** relationship refinablility reference set */
    int relationshipRefinabilityExtensionNid;

    int descriptionInactivationIndicatorNid;
    int relationshipInactivationIndicatorNid;
    int conceptInactivationIndicatorNid;

    /** Da factory. */
    private I_TermFactory termFactory;

    private ExtensionProcessor<I_ThinExtByRefPart> extensionProcessor;

    /**
     * Setup member variables/meta data
     *
     * @throws Exception cannot load meta data concepts
     */
    public ExportSpecification(List<Position> positions, List<I_GetConceptData> inclusions,
            List<I_GetConceptData> exclusions, NAMESPACE defaultNamespace) throws Exception {
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
        unspecifiedUuid = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getUids().iterator().next());
        initialCharacterNotCaseSensitive = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.INITIAL_CHARACTER_NOT_CASE_SENSITIVE.localize().getUids().iterator().next());
        allCharactersCaseSensitive = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.ALL_CHARACTERS_CASE_SENSITIVE.localize().getUids().iterator().next());
        fullySpecifiedDescriptionType = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getUids().iterator().next());
        fullySpecifiedDescriptionTypeIIntSet.add(fullySpecifiedDescriptionType.getConceptId());
        ctv3IdMapExtension = termFactory.getConcept(ConceptConstants.CTV3_ID_MAP_EXTENSION.localize().getNid());
        snomedRtIdMapExtension = termFactory.getConcept(ConceptConstants.SNOMED_ID_MAP_EXTENSION.localize().getNid());
        stringExtensionNid = RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid();
        conceptExtensionNid = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid();
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
        relationshipRefinabilityExtensionNid = ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize().getNid();

        setPositions(positions);
        setInclusions(inclusions);
        setExclusions(exclusions);

        this.defaultNamespace = defaultNamespace;

        extensionProcessor = new ExtensionProcessor<I_ThinExtByRefPart>();
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
    public ComponentDto getDataForExport(I_GetConceptData concept) throws Exception {
        ComponentDto componentDto = null;
        boolean exportableConcept = false;

        if (isIncluded(concept) && !isExcluded(concept)) {
            componentDto = new ComponentDto();
            for (Position position : positions) {
                List<I_ConceptAttributeTuple> matchingConceptTuples = position.getMatchingTuples(concept.getConceptAttributeTuples(null, null, false, true));
                for (I_ConceptAttributeTuple tuple : matchingConceptTuples) {
                    exportableConcept = true;
                    updateComponentDto(componentDto, tuple);
                    for (I_ThinExtByRefVersioned thinExtByRefVersioned : termFactory.getAllExtensionsForComponent(concept.getConceptId())) {
                        for (Position extPosition : positions) {
                            List<I_ThinExtByRefTuple> conceptExtensionTuples = extPosition.getMatchingTuples(
                                thinExtByRefVersioned.getTuples(null, null, false, true));
                            componentDto.getConceptExtensionDtos().addAll(
                                extensionProcessor.processList(thinExtByRefVersioned, conceptExtensionTuples, TYPE.CONCEPT, true));
                        }
                    }
                }

                List<I_DescriptionTuple> matchingDescriptionTuples = position.getMatchingTuples(concept.getDescriptionTuples(null, null, null, true));
                for (I_DescriptionTuple tuple : matchingDescriptionTuples) {
                    updateComponentDto(componentDto, tuple);
                    for (I_ThinExtByRefVersioned thinExtByRefVersioned : termFactory.getAllExtensionsForComponent(tuple.getDescId())) {
                        for (Position extPosition : positions) {
                            List<I_ThinExtByRefTuple> descriptionExtensionTuples = extPosition.getMatchingTuples(thinExtByRefVersioned.getTuples(
                                null, null, false, true));
                            componentDto.getDescriptionExtensionDtos().addAll(
                                extensionProcessor.processList(thinExtByRefVersioned, descriptionExtensionTuples, TYPE.DESCRIPTION, true));
                        }
                    }
                }

                List<I_RelTuple> matchingRelationshipTuples = position.getMatchingTuples(
                    concept.getSourceRelTuples(null, null, null, false, true));
                for (I_RelTuple tuple : matchingRelationshipTuples) {
                    I_GetConceptData destinationConcept = termFactory.getConcept(tuple.getC2Id());
                    if (isIncluded(destinationConcept) && !isExcluded(destinationConcept)) {
                        updateComponentDto(componentDto, tuple);
                        for (I_ThinExtByRefVersioned thinExtByRefVersioned : termFactory.getAllExtensionsForComponent(tuple.getRelId())) {
                            for (Position extPosition : positions) {
                                List<I_ThinExtByRefTuple> relationshipExtensionTuples = extPosition.getMatchingTuples(thinExtByRefVersioned.getTuples(
                                    null, null, false, true));

                                componentDto.getRelationshipExtensionDtos().addAll(
                                    extensionProcessor.processList(thinExtByRefVersioned, relationshipExtensionTuples, TYPE.RELATIONSHIP, true));
                            }
                        }
                    }
                }
            }
        }

        return (exportableConcept) ? componentDto : null;
    }

    /**
     * Update the ComponentDto with the relationship details.
     *
     * Adds history reference set members.
     *
     * Adds relationship inactivation members.
     *
     * Adds relationship refinability members.
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

        setComponentInactivationReferenceSet(componentDto.getRelationshipExtensionDtos(), tuple.getRelId(), tuple,
            conceptInactivationIndicatorNid, TYPE.RELATIONSHIP);
        setConceptHistory(componentDto, tuple.getRelVersioned());
        setRelationshipRefinabilityReferenceSet(componentDto.getRelationshipExtensionDtos(), tuple);

        setUuidSctIdIdentifier(relationshipDto, tuple, idParts, TYPE.CONCEPT);

        int snomedCharacter = ArchitectonicAuxiliary.getSnomedCharacteristicTypeId(termFactory.getUids(tuple.getCharacteristicId()));
        relationshipDto.setCharacteristicTypeCode(Character.forDigit(snomedCharacter, 10));
        relationshipDto.setCharacteristicTypeId(termFactory.getUids(tuple.getCharacteristicId()).iterator().next());
        relationshipDto.setConceptId(termFactory.getUids(tuple.getRelId()).iterator().next());
        relationshipDto.setDestinationId(termFactory.getUids(tuple.getC2Id()).iterator().next());
        relationshipDto.setModifierId(ConceptConstants.MODIFIER_SOME.getUuids()[0]);
        relationshipDto.setRefinabilityId(termFactory.getUids(tuple.getRefinabilityId()).iterator().next());
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
     * Adds description inactivation members.
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
     * Adds ctv3 and SNOMED RT id map members.
     *
     * Adds concept inactivation members.
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

        List<I_DescriptionTuple> descriptionTuples = conceptData.getDescriptionTuples(null, fullySpecifiedDescriptionTypeIIntSet, null, true);

        String fsn = "NO FSN!!!";
        if (!descriptionTuples.isEmpty()) {
            I_DescriptionTuple fsnTuple = getTupleVersion(descriptionTuples, tuple);
            if (fsnTuple != null) {
                fsn = fsnTuple.getText();
            } else {
                logger.severe("No FSN for the tuple version: " + tuple.getVersion() + " concept "
                    + termFactory.getConcept(conceptData.getNid()));
                fsn = descriptionTuples.get(0).getText();
            }
        } else {
            logger.severe("No FSN for: " + tuple.getVersion() + " concept "
                + termFactory.getConcept(conceptData.getNid()));
        }
        componentDto.getConceptDto().setFullySpecifiedName(fsn);

        componentDto.getConceptDto().setPrimative(tuple.isDefined());
        componentDto.getConceptDto().setType(TYPE.CONCEPT);

        return componentDto;
    }

    /**
     * Checks if the I_RelVersioned a history relationship type, if so export
     * the details to corresponding history type reference set.
     *
     * All versions of the relationship are exported.
     *
     * @param versionedRel I_RelVersioned to check for history reference set match.
     * @throws Exception
     */
    private void setConceptHistory(ComponentDto componentDto, I_RelVersioned versionedRel) throws Exception {
        for (I_RelPart versionPart : versionedRel.getVersions()) {
            if (historyStatusRefsetMap.containsKey(versionPart.getTypeId())) {
                List<I_ThinExtByRefTuple> list = new ArrayList<I_ThinExtByRefTuple>();

                I_ThinExtByRefTuple extensionTuple = createThinExtByRefTuple(historyStatusRefsetMap.get(versionPart.getTypeId()), 0,
                    versionedRel.getC1Id(), versionedRel.getC2Id(), versionPart);
                list.add(extensionTuple);

                componentDto.getRelationshipExtensionDtos().addAll(
                    extensionProcessor.processList(extensionTuple.getCore(), list, TYPE.CONCEPT, false));
            }
        }
    }

    /**
     * Adds the component to the <code>inactivationIndicatorRefsetNid</code> if the component is inactive.
     *
     * @param componentDto ComponentDto to add the member to
     * @param tuple I_ConceptAttributeTuple
     * @param inactivationIndicatorRefsetNid int the inactivation refset id (concept, description or relationship)
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
                    componentNid, rf2InactiveStatus, tuple);
                list.add(extensionTuple);

                extensionDtos.addAll(extensionProcessor.processList(extensionTuple.getCore(), list, type, false));
            }
        }
    }

    /**
     * Export relationship refinability (relationships refinability reference) for all relationships.
     *
     * @param extensionDtos to add the refinability to
     * @param relationshipTuple I_RelTuple
     * @throws Exception
     */
    private void setRelationshipRefinabilityReferenceSet(List<ExtensionDto> extensionDtos,
            I_RelTuple relationshipTuple) throws Exception {
        List<I_ThinExtByRefTuple> list = new ArrayList<I_ThinExtByRefTuple>();

        I_ThinExtByRefTuple extensionTuple = createThinExtByRefTuple(relationshipRefinabilityExtensionNid, 0, relationshipTuple.getRelId(),
            relationshipTuple.getRefinabilityId(), relationshipTuple);

        list.add(extensionTuple);

        extensionDtos.addAll(extensionProcessor.processList(extensionTuple.getCore(), list, TYPE.RELATIONSHIP, false));
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
            extensionProcessor.processList(ctv3tuple.getCore(), list, TYPE.CONCEPT, false));
    }

    /**
     * Adds the SNOMED RT id to the SNOMED RT id reference set.
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
            extensionProcessor.processList(snomedIdtuple.getCore(), list, TYPE.CONCEPT, false));
    }

    /**
     * Create a I_ThinExtByRefTuple for a concept reference set member.
     *
     * Path version and status are copied from amPart.
     *
     * @param refsetNid int
     * @param memberNid int
     * @param referencedComponentNid int
     * @param amPart I_AmPart int
     * @return I_ThinExtByRefTuple
     */
    private I_ThinExtByRefTuple createThinExtByRefTuple(int refsetNid, int memberNid, int referencedComponentNid,
            int component1, I_AmPart amPart) {
        ThinExtByRefVersioned thinExtByRefVersioned = new ThinExtByRefVersioned(refsetNid, memberNid, referencedComponentNid,
            conceptExtensionNid);

        I_ThinExtByRefPartConcept conceptExtension = new ThinExtByRefPartConcept();
        conceptExtension.setC1id(component1);
        conceptExtension.setPathId(amPart.getPathId());
        conceptExtension.setStatusId(amPart.getStatusId());
        conceptExtension.setVersion(amPart.getVersion());

        I_ThinExtByRefTuple ctv3tuple = new ThinExtByRefTuple(thinExtByRefVersioned, conceptExtension);

        return ctv3tuple;
    }

    /**
     * Create a I_ThinExtByRefTuple for a string reference set member.
     *
     * Path version and status are copied from amPart.
     *
     * @param refsetNid int
     * @param memberNid int
     * @param componentNid int
     * @param amPart I_AmPart
     * @param string String
     * @return
     */
    private I_ThinExtByRefTuple createThinExtByRefTuple(int refsetNid, int memberNid, int componentNid,
            I_AmPart amPart, String string) {
        ThinExtByRefVersioned thinExtByRefVersioned = new ThinExtByRefVersioned(refsetNid, memberNid, componentNid,
            stringExtensionNid);

        I_ThinExtByRefPartString conceptExtension = new ThinExtByRefPartString();
        conceptExtension.setStringValue(string);
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
        baseConceptDto.setStatusCode(ArchitectonicAuxiliary.getSnomedConceptStatusId(
            termFactory.getConcept(tuple.getStatusId()).getUids()) + "");

        return baseConceptDto;
    }

    /**
     * Adds an identifier row for UUID (type 3 and 5) to SCTID to the ConceptDto.
     *
     * If not SCTID then no identifier row is added
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

        I_IdPart type5UuidPart = getLatesIdtVersion(idVersions, unspecifiedUuid.getConceptId(), tuple);
        I_IdPart sctIdPart = getLatesIdtVersion(idVersions, snomedIntId.getConceptId(), tuple);
        I_IdPart type3UidPart = getLatesIdtVersion(idVersions, snomedT3Uuid.getConceptId(), tuple);

        if (type5UuidPart != null && sctIdPart != null) {
            setIdentifier(conceptDto, tuple, idVersions, type, type5UuidPart, sctIdPart);
        }
        if (type3UidPart != null && sctIdPart != null) {
            setIdentifier(conceptDto, tuple, idVersions, type, type3UidPart, sctIdPart);
        }
    }

    /**
     * Sets the id mapping for the UUID to SCTID
     *
     * @param conceptDto ConceptDto to add the identifier to.
     * @param tuple I_AmPart
     * @param idVersions List of I_IdPart
     * @param type TYPE
     * @param uuidPart I_IdPart
     * @param sctIdPart I_IdPart
     * @throws IOException
     * @throws TerminologyException
     */
    private void setIdentifier(ConceptDto conceptDto, I_AmPart tuple, List<I_IdPart> idVersions, TYPE type,
            I_IdPart uuidPart, I_IdPart sctIdPart) throws IOException, TerminologyException {
        IdentifierDto identifierDto = new IdentifierDto();
        getBaseConceptDto(identifierDto, tuple, idVersions);
        identifierDto.setType(type);

        identifierDto.setActive(isActive(sctIdPart.getStatusId()));
        identifierDto.setConceptId(UUID.fromString(uuidPart.getSourceId().toString()));
        identifierDto.setReferencedSctId(Long.valueOf(sctIdPart.getSourceId().toString()));
        identifierDto.setIdentifierSchemeUuid(snomedIntId.getUids().get(0));

        conceptDto.getIdentifierDtos().add(identifierDto);
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
     * or if no SCTID then use the tuple path.
     *
     * NB currently if no SCTID and not the international path then the
     * defaultNamespace is returned.
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
                namespace = defaultNamespace;
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

        if (snomedReleasePath.isParentOf(pathConcept, null, null, null, false)) {
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
     * @param statusNid int
     * @return String 1 if the statusNid is active otherwise 0;
     * @throws TerminologyException DB error
     * @throws IOException DB error
     */
    private boolean isActive(final int statusNid) throws IOException, TerminologyException {
        boolean activate = false;
        I_GetConceptData statusConcept = termFactory.getConcept(statusNid);

        if (activeConcept.isParentOf(statusConcept, null, null, null, false)) {
            activate = true;
        } else if (activeConcept.getNid() == statusConcept.getNid()) {
            activate = true;
        }

        return activate;
    }

    /**
     * covert the snomed CT component status to rf2 meta-data status
     *
     * @param statusNid int
     * @return int rf2 status or -1 if no map found.
     */
    private int getRf2Status(int statusNid) {
        int rf2Status = -1;

        if (statusNid == aceDuplicateStatusNId) {
            rf2Status = duplicateStatusNId;
        } else if (statusNid == aceAmbiguousStatusNId) {
            rf2Status = ambiguousStatusNId;
        } else if (statusNid == aceErroneousStatusNId) {
            rf2Status = erroneousStatusNId;
        } else if (statusNid == aceOutdatedStatusNId) {
            rf2Status = outdatedStatusNId;
        } else if (statusNid == aceInappropriateStatusNId) {
            rf2Status = inappropriateStatusNId;
        } else if (statusNid == aceMovedElsewhereStatusNId) {
            rf2Status = movedElsewhereStatusNId;
        }

        return rf2Status;
    }

    /**
     * Gets the latest tuple part version that is not newer than <code>tuple</code>.
     *
     * @param conceptTuples list of I_AmTuple
     * @param tuple I_AmTuple
     * @return I_AmTuple
     * @throws IOException DB error
     * @throws TerminologyException DB error
     */
    private <T extends I_AmTuple> T getTupleVersion(List<T> conceptTuples, I_AmTuple tuple) throws IOException,
            TerminologyException {
        T tuplePart = null;

        for (T curTupleVersion : conceptTuples) {
            if (curTupleVersion.getVersion() <= tuple.getVersion()) {
                if (tuplePart == null || tuplePart.getVersion() < curTupleVersion.getVersion()) {
                    tuplePart = curTupleVersion;
                }
            }
        }

        return tuplePart;
    }

    /**
     * Get the latest id part for the list of parts with the source id concept
     * <code>sourceConcept</code> and is not newer than the attributeTuple
     * version
     *
     * @param sourceIdConceptNid int CTV3_ID, SNOMED_RT_ID etc
     * @param idParts the List of I_IdPart
     * @param attributeTuple I_AmPart to check the part version against
     * @return I_IdPart latest Id version for the sourceConcept.
     * @throws IOException DB errors
     */
    private I_IdPart getLatesIdtVersion(List<I_IdPart> idParts, int sourceIdConceptNid, I_AmPart attributeTuple)
            throws TerminologyException, IOException, NoMappingException {
        I_IdPart latestIdPart = null;

        for (I_IdPart iIdPart : idParts) {
            if (iIdPart.getSource() == sourceIdConceptNid
                && ((latestIdPart == null && iIdPart.getVersion() <= attributeTuple.getVersion()) || (latestIdPart != null
                    && iIdPart.getVersion() > latestIdPart.getVersion() && latestIdPart.getVersion() <= attributeTuple.getVersion()))) {
                latestIdPart = iIdPart;
            }
        }

        return latestIdPart;
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
            if (includeConceptData.isParentOf(concept, null, null, null, false)) {
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
            if (excludedConceptData.isParentOf(concept, null, null, null, false)) {
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
     * Extension processing interface.
     *
     * @param <T> extends I_ThinExtByRefPart
     */
    private interface I_AmExtensionProcessor<T extends I_ThinExtByRefPart> {
        /**
         * Create a ExtensionDto for the reference set member version
         *
         * @param thinExtByRefVersioned I_ThinExtByRefVersioned
         * @param tuple <T>
         * @param type TYPE
         * @return ExtensionDto
         * @throws IOException ye'old DB errors
         * @throws TerminologyException ye'old DB errors
         */
        ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned, T tuple, TYPE type)  throws IOException, TerminologyException;
    }

    /**
     * Processes each of the extension types that extend I_ThinExtByRefPart.
     *
     * @param <T> the extension types
     */
    private class ExtensionProcessor<T extends I_ThinExtByRefPart> {

        /** Map of extension handlers for each extension type. */
        private Map<Integer, I_AmExtensionProcessor<T>> extensionMap =
                new HashMap<Integer, I_AmExtensionProcessor<T>>();

        /**
         * Create the <code>extensionMap</code>.
         *
         * @throws Exception error creating the extensionMap
         */
        @SuppressWarnings("unchecked")
        public ExtensionProcessor() throws Exception {
            extensionMap.put(RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid(), new StringExtensionProcessor());
            extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid(), new ConceptExtensionProcessor());
            extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.localize().getNid(), new ConceptStringExtensionProcessor());
            extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.localize().getNid(), new ConceptIntegerExtensionProcessor());
            extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid(), new ConceptConceptExtensionProcessor());
            extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.localize().getNid(), new ConceptConceptStringExtensionProcessor());
            extensionMap.put(RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize().getNid(), new ConceptConceptConceptExtensionProcessor());
        }

        /**
         * For each member tuple part of the reference set create an ExtensionDto
         *
         * @param thinExtByRefVersioned I_ThinExtByRefVersioned the reference set member
         * @param list List of I_ThinExtByRefPart the member parts
         * @return List of ExtensionDto
         * @throws IOException DB errors
         * @throws TerminologyException DB errors
         */
        @SuppressWarnings("unchecked")
        public List<ExtensionDto> processList(I_ThinExtByRefVersioned thinExtByRefVersioned, List<I_ThinExtByRefTuple> list, TYPE type, boolean isClinical) throws IOException, TerminologyException {
            List<ExtensionDto> extensionDtos = new ArrayList<ExtensionDto>();

            for (I_ThinExtByRefTuple t : list) {
                I_GetConceptData refsetConcept = termFactory.getConcept(t.getRefsetId());
                if (isIncluded(refsetConcept) && !isExcluded(refsetConcept)) {
                    I_AmExtensionProcessor<T> extensionProcessor = extensionMap.get(t.getTypeId());
                    if(extensionProcessor != null){
                        ExtensionDto extensionDto = extensionProcessor.getExtensionDto(thinExtByRefVersioned, (T) t.getPart(), type);
                        extensionDto.setIsClinical(isClinical);
                        extensionDtos.add(extensionDto);
                    } else {
                        logger.severe("No extension processor for refset " + termFactory.getConcept(t.getRefsetId()).getInitialText());
                        logger.severe("No extension processor for concept " + termFactory.getConcept(t.getComponentId()).getInitialText());
                        logger.severe("No extension processor for type " + termFactory.getConcept(t.getTypeId()).getInitialText());
                    }
                }
            }

            return extensionDtos;
        }
    }

    /**
     * Extension processor for  I_ThinExtByRefPartConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConcept
     */
    private class BaseExtensionProcessor<T extends I_ThinExtByRefPart> implements
            I_AmExtensionProcessor<I_ThinExtByRefPart> {

        /**
         * @see org.dwfa.mojo.export.ExportSpecification.I_AmExtensionProcessor#getExtensionDto(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, org.dwfa.maven.transform.SctIdGenerator.TYPE)
         *
         * Creates the ExtensionDto for a Concept extension.
         */
        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPart tuple, TYPE type) throws IOException, TerminologyException {
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
            extensionDto.setReferencedConceptId(termFactory.getUids(thinExtByRefVersioned.getComponentId()).iterator().next());
            extensionDto.setMemberId(getUuid(thinExtByRefVersioned));
            extensionDto.setNamespace(getNamespace(idParts, tuple));
            extensionDto.setType(type);
            extensionDto.setFullySpecifiedName(termFactory.getConcept(thinExtByRefVersioned.getRefsetId()).getInitialText());

            return extensionDto;
        }

        /**
         * Create a UUID id part for export. The member UUID will based on the refset id and concept 1 id.
         *
         * @param thinExtByRefVersioned I_ThinExtByRefVersioned
         * @param tuple I_ThinExtByRefPartConcept
         * @return I_IdPart containing the UUID
         * @throws TerminologyException
         * @throws IOException
         */
        private I_IdPart getIdUuidSctIdPart(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPart tuple) throws TerminologyException, IOException {
            I_IdPart sctUuidPart = new ThinIdPart();
            sctUuidPart.setStatusId(currentConcept.getNid());
            sctUuidPart.setPathId(tuple.getPathId());
            sctUuidPart.setSource(ctv3IdMapExtension.getNid());
            sctUuidPart.setVersion(tuple.getVersion());
            sctUuidPart.setSourceId(getUuid(thinExtByRefVersioned));

            return sctUuidPart;
        }

        /**
         * Gets the UUID for the member. If member id is 0 a new UUID is create.
         *
         * @param thinExtByRefVersioned I_ThinExtByRefVersioned
         * @return UUID
         * @throws TerminologyException
         * @throws IOException
         */
        private UUID getUuid(I_ThinExtByRefVersioned thinExtByRefVersioned)
                throws TerminologyException, IOException {
            UUID uuid = null;

            if (thinExtByRefVersioned.getMemberId() != 0) {
                uuid = termFactory.getUids(thinExtByRefVersioned.getMemberId()).iterator().next();
            } else {
                uuid = UUID.nameUUIDFromBytes(("org.dwfa." + termFactory.getUids(thinExtByRefVersioned.getComponentId())
                        + termFactory.getUids(thinExtByRefVersioned.getRefsetId())).getBytes("8859_1"));
            }
            return uuid;
        }
    }

    /**
     * Extension processor for  I_ThinExtByRefPartConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConcept
     */
    private class ConceptExtensionProcessor<T extends I_ThinExtByRefPartConcept> implements
            I_AmExtensionProcessor<I_ThinExtByRefPartConcept> {

        BaseExtensionProcessor<T> extensionProcessor = new BaseExtensionProcessor<T>();

        /**
         * @see org.dwfa.mojo.export.ExportSpecification.I_AmExtensionProcessor#getExtensionDto(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, org.dwfa.maven.transform.SctIdGenerator.TYPE)
         *
         * Creates the ExtensionDto for a Concept extension.
         */
        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPartConcept tuple, TYPE type) throws IOException, TerminologyException {
            ExtensionDto extensionDto = extensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type);

            extensionDto.setConcept1Id(termFactory.getUids(tuple.getC1id()).iterator().next());

            return extensionDto;
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

        /**
         * @see org.dwfa.mojo.export.ExportSpecification.I_AmExtensionProcessor#getExtensionDto(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, org.dwfa.maven.transform.SctIdGenerator.TYPE)
         *
         * Concept String extensions
         */
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
    private class StringExtensionProcessor<T extends I_ThinExtByRefPartString> implements
            I_AmExtensionProcessor<I_ThinExtByRefPartString> {
        BaseExtensionProcessor<T> extensionProcessor = new BaseExtensionProcessor<T>();

        /**
         * @see org.dwfa.mojo.export.ExportSpecification.I_AmExtensionProcessor#getExtensionDto(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, org.dwfa.maven.transform.SctIdGenerator.TYPE)
         *
         * Concept String extensions
         */
        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPartString tuple, TYPE type) throws IOException, TerminologyException {
            ExtensionDto extensionDto = extensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type);

            extensionDto.setValue(tuple.getStringValue());

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

        /**
         * @see org.dwfa.mojo.export.ExportSpecification.I_AmExtensionProcessor#getExtensionDto(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, org.dwfa.maven.transform.SctIdGenerator.TYPE)
         *
         * Concept Integer extensions
         */
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

        /**
         * @see org.dwfa.mojo.export.ExportSpecification.I_AmExtensionProcessor#getExtensionDto(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, org.dwfa.maven.transform.SctIdGenerator.TYPE)
         *
         * Concept Concept Extension
         */
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

        /**
         * @see org.dwfa.mojo.export.ExportSpecification.I_AmExtensionProcessor#getExtensionDto(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, org.dwfa.maven.transform.SctIdGenerator.TYPE)
         *
         * Concept Concept String Extension
         */
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

        /**
         * @see org.dwfa.mojo.export.ExportSpecification.I_AmExtensionProcessor#getExtensionDto(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, org.dwfa.maven.transform.SctIdGenerator.TYPE)
         *
         * Concept Concept Concept Extension
         */
        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPartConceptConceptConcept tuple, TYPE type) throws IOException, TerminologyException {
            ExtensionDto extensionDto = conceptConceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type);

            extensionDto.setConcept3Id(termFactory.getConcept(tuple.getC3id()).getUids().get(0));

            return extensionDto;
        }
    }
}
