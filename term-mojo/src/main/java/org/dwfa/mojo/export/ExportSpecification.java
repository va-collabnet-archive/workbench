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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.dwfa.ace.util.TupleVersionComparator;
import org.dwfa.ace.util.TupleVersionPart;
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
import org.dwfa.maven.sctid.UuidSnomedDbMapHandler;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.dwfa.vodb.types.ThinExtByRefVersioned;
import org.dwfa.vodb.types.ThinIdPart;

/**
 * Contains the hierarchy to be exports and optionally not exported that are on the exportable positions.
 */
public class ExportSpecification {
    /** Class logger. */
    private Logger logger = Logger.getLogger(ExportSpecification.class.getName());

    /** Export positions */
    public List<Position> positions;
    /** Included hierarchy */
    public List<I_GetConceptData> inclusions;
    /** Excluded hierarchy - within one or more included hierarchy */
    public List<I_GetConceptData> exclusions;
    /** The default namespace to use for export */
    private NAMESPACE defaultNamespace;
    /** Mapping of UUID's to SCT ids. */
    private UuidSnomedDbMapHandler uuidSnomedDbMapHandler;

    /** The RF2 active concept. */
    private I_GetConceptData rf2ActiveConcept;
    /** The active concept. */
    private I_GetConceptData activeConcept;
    /** The in active concept. */
    private I_GetConceptData inActiveConcept;
    /** The active concept. */
    private I_GetConceptData currentConcept;
    /** The active concept. */
    private I_GetConceptData conceptRetired;
    /** The active concept. */
    private I_GetConceptData pendingMove;
    /** Retired concept */
    private I_GetConceptData retiredConcept;
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
    /** Preferred desctiption type. */
    private I_GetConceptData unspecifiedDescriptionType;
    /** Preferred desctiption type. */
    private I_GetConceptData preferredDescriptionType;
    /** Synonym desctiption type. */
    private I_GetConceptData synonymDescriptionType;
    /** Int set of fsn type. */
    private I_IntSet fullySpecifiedDescriptionTypeIntSet = new IntSet();
    /** Int set of fsn type. */
    private I_IntSet snomedIsATypeIntSet = new IntSet();
    /** Int set of fsn type. */
    private I_IntSet currentActiveStatusIntSet = new IntSet();
    /** CTV3 reference set. */
    private I_GetConceptData ctv3IdMapExtension;
    /** Snomed Rt Id reference set. */
    private I_GetConceptData snomedRtIdMapExtension;
    /** concept string extension type. */
    private int stringExtensionNid;
    /** concept extension type. */
    private int conceptExtensionNid;
    /** RF2 acceptable description type */
    private int rf2AcceptableDescriptionTypeNid;
    /** RF2 preferred description type */
    private int rf2PreferredDescriptionTypeNid;


    /** Ace Workbench inactive status. */
    int aceDuplicateStatusNId;
    int aceAmbiguousStatusNId;
    int aceErroneousStatusNId;
    int aceOutdatedStatusNId;
    int aceInappropriateStatusNId;
    int aceMovedElsewhereStatusNId;
    int aceLimitedStatusNId;
    /** RF2 inactive status. */
    int duplicateStatusNId;
    int ambiguousStatusNId;
    int erroneousStatusNId;
    int outdatedStatusNId;
    int inappropriateStatusNId;
    int movedElsewhereStatusNId;
    int limitedStatusNId;
    /** History relationship types to History relationship reference set map. */
    Map<Integer, Integer> historyStatusRefsetMap = new HashMap<Integer, Integer>();

    /** relationship refinablility reference set */
    int relationshipRefinabilityExtensionNid;

    /** inactivation indicators. */
    int descriptionInactivationIndicatorNid;
    int relationshipInactivationIndicatorNid;
    int conceptInactivationIndicatorNid;

    /** ADRS TODO need to change this to be configurable */
    List<Integer> structuralRefsets = new ArrayList<Integer>();
    int adrsNid;
    int ctv3SimpleMapReferenceSetNid;
    int conceptInactivationIndicatorAttributeValueReferenceSet;
    int descriptionInactivationIndicatorAttributeValueReferenceSet;
    int snomedRtIdSimpleMap;
    int relationshipRefinabilityAttributeValueReferenceSet;

    /** release part for new content namely ADRS members */
    ThinConPart releasePart;

    /***/
    private PROJECT defaultProject;

    /** Da factory. */
    private I_TermFactory termFactory;

    /** Component extension processor. */
    private ExtensionProcessor<I_ThinExtByRefPart> extensionProcessor;

    private static final String EN_US = "en-US";
    private static final String EN = "en";
    private static final String EN_GB = "en-GB";
    private static final String EN_AU = "en-AU";

    /**
     * Setup member variables/meta data
     *
     * @throws Exception cannot load meta data concepts
     */
    public ExportSpecification(List<Position> positions, List<I_GetConceptData> inclusions,
            List<I_GetConceptData> exclusions, NAMESPACE defaultNamespace, PROJECT defaultProject) throws Exception {
        termFactory = LocalVersionedTerminology.get();

        rf2ActiveConcept = termFactory.getConcept(ConceptConstants.ACTIVE_VALUE.localize().getNid());
        activeConcept = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.ACTIVE.localize().getUids().iterator().next());
        inActiveConcept = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.INACTIVE.localize().getUids().iterator().next());
        currentConcept = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.CURRENT.localize().getUids().iterator().next());
        conceptRetired = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getUids().iterator().next());
        pendingMove = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getUids().iterator().next());
        retiredConcept = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.RETIRED.localize().getUids().iterator().next());
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
        synonymDescriptionType = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getUids().iterator().next());
        preferredDescriptionType = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getUids().iterator().next());
        unspecifiedDescriptionType = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE.localize().getUids().iterator().next());
        rf2AcceptableDescriptionTypeNid = termFactory.getConcept(
            org.dwfa.ace.refset.ConceptConstants.ACCEPTABLE.getUuids()).getNid();
        rf2PreferredDescriptionTypeNid = termFactory.getConcept(
            org.dwfa.ace.refset.ConceptConstants.PREFERRED.getUuids()).getNid();

        fullySpecifiedDescriptionTypeIntSet.add(fullySpecifiedDescriptionType.getConceptId());
        snomedIsATypeIntSet.add(termFactory.getConcept(org.dwfa.ace.refset.ConceptConstants.SNOMED_IS_A.getUuids()).getConceptId());
        currentActiveStatusIntSet.add(currentConcept.getNid());
        currentActiveStatusIntSet.add(activeConcept.getNid());
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
        aceLimitedStatusNId = Concept.LIMITED.localize().getNid();
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
        limitedStatusNId = ConceptConstants.LIMITED.localize().getNid();

        //TODO this needs to be re factored...
        adrsNid = termFactory.getConcept(UUID.fromString("e20f610b-fbc0-43fe-8130-8f9abca312d9")).getNid();

        structuralRefsets.add(adrsNid);
        structuralRefsets.add(termFactory.getConcept(UUID.fromString("6f1e56b5-c127-4f0b-97fa-cb72c76ad58a")).getNid());
        structuralRefsets.add(termFactory.getConcept(UUID.fromString("ef010cf1-cf06-4c8a-9684-a040e61b319d")).getNid());
        structuralRefsets.add(termFactory.getConcept(UUID.fromString("f8834d2f-4e2d-4793-a9e0-5190391ad277")).getNid());
        structuralRefsets.add(termFactory.getConcept(UUID.fromString("69eb6cad-441a-456e-93da-78520ba68a29")).getNid());
        structuralRefsets.add(termFactory.getConcept(UUID.fromString("c4367277-ed2a-4641-b35c-8e4c6d92a3c9")).getNid());
        structuralRefsets.add(termFactory.getConcept(UUID.fromString("856c8043-6890-42c2-bb4b-b97d127b7f5c")).getNid());
        structuralRefsets.add(termFactory.getConcept(UUID.fromString("aa1698ba-8bff-4e9d-abdf-a0cb47b7bc5e")).getNid());
        structuralRefsets.add(termFactory.getConcept(UUID.fromString("6dba82a0-c89d-4ee5-91e4-cb63787447fa")).getNid());
        structuralRefsets.add(termFactory.getConcept(UUID.fromString("622aa587-2e34-43b3-b3d4-53561aa3c7be")).getNid());
        structuralRefsets.add(termFactory.getConcept(UUID.fromString("6c441f26-ed8a-42ff-91b7-fcb27191f9f6")).getNid());


        setPositions(positions);
        setInclusions(inclusions);
        setExclusions(exclusions);

        this.defaultNamespace = defaultNamespace;
        this.defaultProject = defaultProject;

        uuidSnomedDbMapHandler = UuidSnomedDbMapHandler.getInstance();
        uuidSnomedDbMapHandler.updateNextSequenceMap();

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
                    setComponentTuples(concept, latestPostionMatchingConceptTuples,
                        latestPostionMatchingDescriptionTuples, latestPostionMatchingRelationshipTuples, position);
                } else {
                    setComponentTuples(concept, matchingConceptTuples, matchingDescriptionTuples,
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
            setComponentInactivationReferenceSet(componentDto.getConceptExtensionDtos(), concept.getNid(), matchingConceptTuples,
                conceptInactivationIndicatorNid, TYPE.CONCEPT);

            setExtensionDto(componentDto.getConceptExtensionDtos(), concept.getConceptId(), TYPE.CONCEPT);

            Set<I_DescriptionTuple> latestDescriptionTuples = new HashSet<I_DescriptionTuple>();
            latestDescriptionTuples.addAll(TupleVersionPart.getLatestMatchingTuples(matchingDescriptionTuples));
            for (I_DescriptionTuple tuple : matchingDescriptionTuples) {
                setDescriptionDto(componentDto, tuple, latestDescriptionTuples.contains(tuple));
            }
            for (I_DescriptionTuple latestDescription : latestDescriptionTuples) {
                setComponentInactivationReferenceSet(componentDto.getDescriptionExtensionDtos(), latestDescription.getDescId(), latestDescription,
                    descriptionInactivationIndicatorNid, TYPE.RELATIONSHIP);
            }
            updateAdrsComponentDto(componentDto, matchingDescriptionTuples);

            Set<I_RelTuple> latestRelationshipTuples = new HashSet<I_RelTuple>();
            latestRelationshipTuples.addAll(TupleVersionPart.getLatestMatchingTuples(matchingRelationshipTuples));
            for (I_RelTuple tuple : matchingRelationshipTuples) {
                setRelationshipDto(componentDto, tuple, latestRelationshipTuples.contains(tuple));
            }
            for (I_RelTuple latestRelationship : latestRelationshipTuples) {
                setComponentInactivationReferenceSet(componentDto.getRelationshipExtensionDtos(), latestRelationship.getRelId(), latestRelationship,
                    relationshipInactivationIndicatorNid, TYPE.RELATIONSHIP);

            }
        }

        return (!matchingConceptTuples.isEmpty()) ? componentDto : null;
    }

    /**
     * Set the matching tuples for concepts, descriptions and relationships
     *
     * @param concept I_GetConceptData
     * @param exportableConcept boolean
     * @param matchingConceptTuples Collection of I_ConceptAttributeTuple to update
     * @param matchingDescriptionTuples Collection of I_DescriptionTuple to update
     * @param matchingRelationshipTuples Collection of I_RelTuple to update
     * @param position to Export
     *
     * @return true if exportableConcept is true or there are matching concept tuples
     *
     * @throws IOException
     * @throws TerminologyException
     */
    private void setComponentTuples(I_GetConceptData concept,
            Collection<I_ConceptAttributeTuple> matchingConceptTuples,
            Collection<I_DescriptionTuple> matchingDescriptionTuples,
            Collection<I_RelTuple> matchingRelationshipTuples, Position position) throws IOException,
            TerminologyException {
        matchingConceptTuples.addAll(
            position.getMatchingTuples(concept.getConceptAttributeTuples(null, null, false, false)));

        matchingDescriptionTuples.addAll(
            position.getMatchingTuples(concept.getDescriptionTuples(null, null, null, false)));

        matchingRelationshipTuples.addAll(
            position.getMatchingTuples(concept.getSourceRelTuples(null, null, null, false, false)));
    }

    /**
     * Set the Concept and concept extensions on the componentDto
     *
     * @param componentDto ComponentDto
     * @param tuple I_ConceptAttributeTuple
     * @throws Exception
     * @throws IOException
     * @throws TerminologyException
     */
    private void setConceptDto(ComponentDto componentDto, I_ConceptAttributeTuple tuple, boolean latest)
            throws Exception, IOException, TerminologyException {
        updateComponentDto(componentDto, tuple, latest);
    }

    /**
     * Set the Description and description extensions on the componentDto
     *
     * @param componentDto ComponentDto
     * @param tuple I_DescriptionTuple
     * @throws Exception
     * @throws IOException
     * @throws TerminologyException
     */
    private void setDescriptionDto(ComponentDto componentDto, I_DescriptionTuple tuple, boolean latest) throws Exception, IOException,
            TerminologyException {
        updateComponentDto(componentDto, tuple, latest);
        if (latest) {
            setExtensionDto(componentDto.getDescriptionExtensionDtos(), tuple.getDescId(), TYPE.DESCRIPTION);
        }
    }

    /**
     * Set the relationship and relationship extensions on the componentDto
     *
     * @param componentDto ComponentDto
     * @param tuple I_RelTuple
     * @throws TerminologyException
     * @throws IOException
     * @throws Exception
     */
    private void setRelationshipDto(ComponentDto componentDto, I_RelTuple tuple, boolean latest) throws TerminologyException,
            IOException, Exception {
        I_GetConceptData destinationConcept = termFactory.getConcept(tuple.getC2Id());
        I_GetConceptData relationshipType = termFactory.getConcept(tuple.getTypeId());

        if (isExportableConcept(destinationConcept) && isExportableConcept(relationshipType)) {
            updateComponentDto(componentDto, tuple, latest);
            if (latest) {
                setExtensionDto(componentDto.getRelationshipExtensionDtos(), tuple.getRelId(), TYPE.RELATIONSHIP);
            }
        }
    }

    /**
     * Add the extensionDtos on the extension list.
     *
     * @param extensionList List of ExtensionDto
     * @param nid int
     * @throws Exception
     */
    private void setExtensionDto(List<ExtensionDto> extensionList, int nid, TYPE type) throws Exception {
        Set<I_ThinExtByRefTuple> extensionTuples = new HashSet<I_ThinExtByRefTuple>();
        List<I_ThinExtByRefPart> extensionPartList = new ArrayList<I_ThinExtByRefPart>();

        for (I_ThinExtByRefVersioned thinExtByRefVersioned : termFactory.getAllExtensionsForComponent(nid)) {
            for (Position position : positions) {
                if (!structuralRefsets.contains(thinExtByRefVersioned.getRefsetId())) {
                    extensionTuples.addAll(position.getMatchingTuples(thinExtByRefVersioned.getTuples(null, null, false, false)));
                }
            }

            if (!extensionTuples.isEmpty()) {
                for (I_ThinExtByRefTuple tuple : extensionTuples) {
                    extensionPartList.add(tuple.getPart());
                }
                extensionList.addAll(extensionProcessor.processList(extensionTuples.iterator().next().getCore(), extensionPartList, type, true));
            }
            extensionTuples.clear();
            extensionPartList.clear();
        }
    }

    /**
     * Is the concept in the include hierarchy or in the excluded hierarchy and also in the included hierarchy.
     *
     * @param concept I_GetConceptData
     * @return boolean
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean isExportableConcept(I_GetConceptData concept)
            throws IOException, TerminologyException {
        boolean exportable = false;

        if (isExcluded(concept) && isIncluded(concept)) {
            exportable = true;
        } else if (isIncluded(concept)) {
            exportable = true;
        }

        return exportable;
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
    private void updateComponentDto(ComponentDto componentDto, I_RelTuple tuple, boolean latest) throws Exception,
            TerminologyException {
        RelationshipDto relationshipDto = new RelationshipDto();
        List<I_IdPart> idParts = termFactory.getId(tuple.getRelId()).getVersions();

        getBaseConceptDto(relationshipDto, tuple, idParts, latest);

        if (latest) {
            setConceptHistory(componentDto, tuple.getRelVersioned() ,latest);
            setRelationshipRefinabilityReferenceSet(componentDto.getRelationshipExtensionDtos(), tuple ,latest);
        }

        setUuidSctIdIdentifier(relationshipDto, tuple, idParts, TYPE.RELATIONSHIP, tuple.getRelId(), latest);

        int snomedCharacter = ArchitectonicAuxiliary.getSnomedCharacteristicTypeId(termFactory.getUids(tuple.getCharacteristicId()));
        relationshipDto.setCharacteristicTypeCode(Character.forDigit(snomedCharacter, 10));
        relationshipDto.setCharacteristicTypeId(termFactory.getUids(tuple.getCharacteristicId()).iterator().next());
        relationshipDto.setConceptId(getIdMap(tuple, tuple.getRelId()));
        relationshipDto.setDestinationId(getIdMap(tuple, tuple.getC2Id()));
        relationshipDto.setModifierId(ConceptConstants.MODIFIER_SOME.getUuids()[0]);
        relationshipDto.setRefinabilityId(termFactory.getUids(tuple.getRefinabilityId()).iterator().next());
        int refinableChar = ArchitectonicAuxiliary.getSnomedRefinabilityTypeId(termFactory.getUids(tuple.getRefinabilityId()));
        relationshipDto.setRefinable(Character.forDigit(refinableChar, 10));
        relationshipDto.setRelationshipGroup(tuple.getGroup());
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
     * @throws Exception
     */
    private void updateComponentDto(ComponentDto componentDto, I_DescriptionTuple tuple, boolean latest) throws Exception {
        DescriptionDto descriptionDto = new DescriptionDto();
        List<I_IdPart> idParts = termFactory.getId(tuple.getDescId()).getVersions();

        getBaseConceptDto(descriptionDto, tuple, idParts, latest);
        descriptionDto.setActive(isDescriptionActive(tuple.getStatusId()));

        setUuidSctIdIdentifier(descriptionDto, tuple, idParts, TYPE.DESCRIPTION, tuple.getDescId(), latest);

        descriptionDto.setCaseSignificanceId(getInitialCaseSignificant(tuple.getInitialCaseSignificant()));
        descriptionDto.setConceptId(getIdMap(tuple, tuple.getConceptId()));
        descriptionDto.setDescription(tuple.getText());
        descriptionDto.setDescriptionId(termFactory.getUids(tuple.getDescId()).iterator().next());
        descriptionDto.setDescriptionTypeCode(Character.forDigit(
            ArchitectonicAuxiliary.getSnomedDescriptionTypeId(termFactory.getUids(tuple.getTypeId())), 10));
        descriptionDto.setInitialCapitalStatusCode(tuple.getInitialCaseSignificant() ? '1' : '0');
        descriptionDto.setLanguageCode(tuple.getLang());
        descriptionDto.setLanguageId(ArchitectonicAuxiliary.getLanguageConcept(tuple.getLang()).getUids().iterator().next());
        descriptionDto.setNamespace(getNamespace(idParts, tuple));
        descriptionDto.setType(TYPE.DESCRIPTION);
        descriptionDto.setTypeId(termFactory.getUids(tuple.getTypeId()).iterator().next());
        descriptionDto.setRf2TypeId(getRf2DescriptionType(tuple.getTypeId()));

        componentDto.getDescriptionDtos().add(descriptionDto);
    }

    /**
     * Get the Rf2 description type for the Architectonic description type.
     *
     * @param typeNid int
     * @return UUID for either Fully specified name or synonym
     * @throws IOException
     */
    private UUID getRf2DescriptionType(int typeNid) throws IOException {
        UUID rf2DescriptionType = synonymDescriptionType.getUids().get(0);

        if(fullySpecifiedDescriptionType.getNid() == typeNid){
            rf2DescriptionType = fullySpecifiedDescriptionType.getUids().get(0);
        }

        return rf2DescriptionType;
    }

    /**
     * Update the ADRS members.
     *
     * @param componentDto ComponentDto
     * @param conceptDescriptionTuples list of I_DescriptionTuple
     * @throws Exception
     */
    private void updateAdrsComponentDto(ComponentDto componentDto, Collection<I_DescriptionTuple> conceptDescriptionTuples) throws Exception {
        I_DescriptionTuple latestPreferredTerm = null;
        List<I_DescriptionTuple> latestSynonyms = new ArrayList<I_DescriptionTuple>();
        List<I_DescriptionTuple> latestUnSpecifiedDescriptionTypes = new ArrayList<I_DescriptionTuple>();

        Collection<I_DescriptionTuple> latestsConceptDescriptionTuples = TupleVersionPart.getLatestMatchingTuples(conceptDescriptionTuples);

        for (I_DescriptionTuple currentDescription : latestsConceptDescriptionTuples) {
            if (currentDescription.getStatusId() == activeConcept.getNid() || currentDescription.getStatusId() == currentConcept.getNid()) {
                if (currentDescription.getTypeId() == preferredDescriptionType.getNid()) {
                    latestPreferredTerm = getAdrsVersion(currentDescription, latestPreferredTerm, true);
                } else if (currentDescription.getTypeId() == synonymDescriptionType.getNid()) {
                    latestSynonyms = getAdrsVersion(currentDescription, latestSynonyms, false);
                } else if (currentDescription.getTypeId() == unspecifiedDescriptionType.getNid()) {
                    latestUnSpecifiedDescriptionTypes = getAdrsVersion(currentDescription, latestUnSpecifiedDescriptionTypes, false);
                }
            }
        }
        for (I_DescriptionTuple currentDescription : latestsConceptDescriptionTuples) {
            I_ThinExtByRefVersioned currentLanguageExtensions = getRefsetExtensionVersioned(adrsNid,
                currentDescription.getDescId());
            if (currentLanguageExtensions != null) {
                //Is there a new Description for the ADRS (retired description)
                if ((latestPreferredTerm == null || currentLanguageExtensions.getComponentId() != latestPreferredTerm.getDescId())
                    && !isDescriptionInList(latestSynonyms, currentLanguageExtensions.getComponentId())
                    && !isDescriptionInList(latestUnSpecifiedDescriptionTypes, currentLanguageExtensions.getComponentId())){

                    I_ThinExtByRefPart retireLatestPart = TupleVersionPart.getLatestPart(
                        currentLanguageExtensions.getVersions()).duplicate();
                    currentLanguageExtensions.addVersion(retireLatestPart);
                    retireLatestPart.setStatusId(retiredConcept.getNid());
                    retireLatestPart.setPathId(releasePart.getPathId());
                    retireLatestPart.setVersion(releasePart.getVersion());

                    componentDto.getConceptExtensionDtos().addAll(
                        extensionProcessor.processList(currentLanguageExtensions,
                            currentLanguageExtensions.getVersions(), TYPE.DESCRIPTION, true));
                }
            }
        }

        if (latestPreferredTerm != null) {
            setAdrsExtension(componentDto, latestPreferredTerm, rf2PreferredDescriptionTypeNid);
        }

        if (!latestSynonyms.isEmpty()) {
            for (I_DescriptionTuple iDescriptionTuple : latestSynonyms) {
                setAdrsExtension(componentDto, iDescriptionTuple, rf2AcceptableDescriptionTypeNid);
            }
        } else if (!latestUnSpecifiedDescriptionTypes.isEmpty()) {
            for (I_DescriptionTuple iDescriptionTuple : latestUnSpecifiedDescriptionTypes) {
                setAdrsExtension(componentDto, iDescriptionTuple, rf2AcceptableDescriptionTypeNid);
            }
        }
    }

    /**
     * Create an adrs extension using the extension processor for the rf2 description type (Acceptable or Preferred)
     *
     * Checks if the current extension is of the same type, if not it creates a new version (part) for the required <code>desctriptionTypeNid</code>
     *
     * @param componentDto ComponentDto
     * @param descriptionTuple I_DescriptionTuple
     * @param desctriptionTypeNid desctriptionTypeNid
     * @throws Exception
     */
    private void setAdrsExtension(ComponentDto componentDto, I_DescriptionTuple descriptionTuple, int desctriptionTypeNid) throws Exception {
        I_ThinExtByRefVersioned adrsVersioned = getRefsetExtensionVersioned(adrsNid, descriptionTuple.getDescId());
        if (adrsVersioned == null) {
            adrsVersioned = getThinExtByRefTuple(adrsNid, 0, descriptionTuple.getDescId(),
                desctriptionTypeNid, releasePart);
        } else {
            I_ThinExtByRefPartConcept latestPart = (I_ThinExtByRefPartConcept) adrsVersioned.getLatestVersion();
            if(latestPart.getC1id() != desctriptionTypeNid){
                I_ThinExtByRefPartConcept conceptExtension = new ThinExtByRefPartConcept();

                conceptExtension.setC1id(desctriptionTypeNid);
                conceptExtension.setPathId(releasePart.getPathId());
                conceptExtension.setStatusId(releasePart.getStatusId());
                conceptExtension.setVersion(releasePart.getVersion());

                adrsVersioned.addVersion(conceptExtension);
            }
        }

        componentDto.getConceptExtensionDtos().addAll(
            extensionProcessor.processList(adrsVersioned, adrsVersioned.getVersions(), TYPE.DESCRIPTION, true));
    }

    /**
     * Is the description id in the list of descriptions
     *
     * @param tuples List of I_DescriptionTuple
     * @param descriptionNid int
     * @return true if in list.
     */
    private boolean isDescriptionInList(List<I_DescriptionTuple>  tuples, int descriptionNid){
        boolean isInList = false;

        for (I_DescriptionTuple currentDescriptionTuple : tuples) {
            if (currentDescriptionTuple != null && currentDescriptionTuple.getDescId() == descriptionNid) {
                isInList = true;
                break;
            }
        }

        return isInList;
    }

    /**
     * Updates the list of description tuples.
     *
     * If the list is empty the <code>currentTuple</code> is returned in the
     * list
     * If the list contains the same type of descriptions at the same language
     * type the the <code>currentTuple</code> is added to the list
     * If the <code>currentTuple</code> is a different language type or newer
     * than the list of items then <code>currentTuple</code> is returned in the
     * list and the other items are removed.
     *
     * @param currentTuple I_DescriptionTuple
     * @param adrsTuples List of I_DescriptionTuple
     * @param usAllowed boolean are US descriptions allowed
     *
     * @return List of I_DescriptionTuple
     */
    private List<I_DescriptionTuple> getAdrsVersion(I_DescriptionTuple currentTuple, List<I_DescriptionTuple>  adrsTuples, boolean usAllowed) {
        I_DescriptionTuple adrsTuple = null;
        I_DescriptionTuple selectedAdrsTuple;

        if(!adrsTuples.isEmpty()){
            adrsTuple = adrsTuples.get(0);
        }
        selectedAdrsTuple = getAdrsVersion(currentTuple, adrsTuple, usAllowed);

        if (adrsTuple == null && selectedAdrsTuple != null) {
            adrsTuples.add(selectedAdrsTuple);
        } else if(selectedAdrsTuple != null && selectedAdrsTuple.getDescId() != currentTuple.getDescId()
                && selectedAdrsTuple.getLang().equals(currentTuple.getLang())
                && selectedAdrsTuple.getVersion() == currentTuple.getVersion()){
            adrsTuples.add(currentTuple);
        } else if(selectedAdrsTuple != null && selectedAdrsTuple.getDescId() != adrsTuple.getDescId()
                && (!selectedAdrsTuple.getLang().equals(adrsTuple.getLang())
                || selectedAdrsTuple.getVersion() > adrsTuple.getVersion())) {
            adrsTuples.clear();
            adrsTuples.add(selectedAdrsTuple);
        }

        return adrsTuples;
    }
    /**
     * Gets the Language I_DescriptionVersioned to use for the refset.
     *
     * Order of language type preference is en_AU, en_GB, en then en_US if allowed.
     *
     * @param descriptionVersion I_DescriptionTuple
     * @param currentAdrsVersion I_DescriptionTuple can be null
     * @param boolean are US descriptions allowed
     * @return I_DescriptionVersioned
     */
    private I_DescriptionTuple getAdrsVersion(I_DescriptionTuple currentTuple, I_DescriptionTuple adrsTuple,
            boolean usAllowed) {
        if (adrsTuple != null) {
            if (currentTuple.getLang().equals(EN_AU)) {
                adrsTuple = currentTuple;
            } else if (adrsTuple.getLang().equals(EN_GB)
                    && currentTuple.getLang().equals(EN_AU)) {
                adrsTuple = currentTuple;
            } else if (adrsTuple.getLang().equals(EN)
                && (currentTuple.getLang().equals(EN_GB)
                        || currentTuple.getLang().equals(EN_AU))) {
                adrsTuple = currentTuple;
            } else if (adrsTuple.getLang().equals(EN_US)
                && (currentTuple.getLang().equals(EN)
                        || currentTuple.getLang().equals(EN_GB)
                        || currentTuple.getLang().equals(EN_AU))) {
                adrsTuple = currentTuple;
            } else if (adrsTuple.getLang().equals(currentTuple.getLang())
                && currentTuple.getVersion() > adrsTuple.getVersion()) {
                adrsTuple = currentTuple;
            }
        } else {
            if (usAllowed) {
                adrsTuple = currentTuple;
            } else if (!currentTuple.getLang().equals(EN_US)) {
                adrsTuple = currentTuple;
            }
        }

        return adrsTuple;
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
     * @throws Exception
     */
    private ComponentDto updateComponentDto(ComponentDto componentDto, I_ConceptAttributeTuple tuple, boolean latest)
            throws Exception {
        ConceptDto conceptDto = new ConceptDto();
        I_GetConceptData conceptData = termFactory.getConcept(tuple.getConId());
        conceptDto.setConceptId(getIdMap(tuple, tuple.getConId()));

        getBaseConceptDto(conceptDto, tuple, conceptData.getId().getVersions(), latest);

        if (latest) {
            I_IdPart ctv3IdPart = getLatesIdtVersion(conceptData.getId().getVersions(), ctv3Id.getConceptId(), tuple);
            if (ctv3IdPart != null) {
                conceptDto.setCtv3Id(ctv3IdPart.getSourceId().toString());
                setCtv3ReferenceSet(componentDto, tuple, ctv3IdPart ,latest);
            } else {
                conceptDto.setCtv3Id("");
            }

            I_IdPart snomedIdPart = getLatesIdtVersion(conceptData.getId().getVersions(), snomedRtId.getConceptId(), tuple);
            if(snomedIdPart != null){
                conceptDto.setSnomedId(snomedIdPart.getSourceId().toString());
                setSnomedRtIdReferenceSet(componentDto, tuple, snomedIdPart ,latest);
            } else {
                conceptDto.setSnomedId("");
            }
        }


        setUuidSctIdIdentifier(conceptDto, tuple, conceptData.getId().getVersions(), TYPE.CONCEPT, tuple.getConId(), latest);

        List<I_DescriptionTuple> descriptionTuples = new ArrayList<I_DescriptionTuple>();
        descriptionTuples.addAll(TupleVersionPart.getLatestMatchingTuples(conceptData.getDescriptionTuples(null, fullySpecifiedDescriptionTypeIntSet, null, true)));
        Collections.sort(descriptionTuples, new TupleVersionComparator());

        String fsn = "NO FSN!!!";
        I_DescriptionTuple fsnTuple = null;
        if (!descriptionTuples.isEmpty()) {
            for (I_DescriptionTuple iDescriptionTuple : descriptionTuples) {
                if (isDescriptionActive(iDescriptionTuple.getStatusId()) || iDescriptionTuple.getStatusId() == aceLimitedStatusNId) {
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
            logger.severe("No FSN for: " + tuple.getVersion() + " concept "
                + termFactory.getConcept(conceptData.getNid()));
        }
        conceptDto.setFullySpecifiedName(fsn);

        conceptDto.setPrimative(tuple.isDefined());
        conceptDto.setType(TYPE.CONCEPT);

        componentDto.getConceptDtos().add(conceptDto);

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
    private void setConceptHistory(ComponentDto componentDto, I_RelVersioned versionedRel, boolean latest) throws Exception {
        for (I_RelPart versionPart : versionedRel.getVersions()) {
            if (historyStatusRefsetMap.containsKey(versionPart.getTypeId())) {
                I_ThinExtByRefVersioned extensionVersioned = getThinExtByRefTuple(historyStatusRefsetMap.get(versionPart.getTypeId()), 0,
                    versionedRel.getC1Id(), versionedRel.getC2Id(), versionPart);

                List<I_ThinExtByRefPart> partsToRemove = new ArrayList<I_ThinExtByRefPart>();
                for (I_ThinExtByRefPart part : extensionVersioned.getVersions()) {
                    if(part.getPathId() != versionPart.getPathId()
                            || part.getVersion() != versionPart.getVersion()){
                        partsToRemove.add(part);
                    }
                }
                extensionVersioned.getVersions().removeAll(partsToRemove);

                componentDto.getRelationshipExtensionDtos().addAll(
                    extensionProcessor.processList(extensionVersioned, extensionVersioned.getVersions(), TYPE.CONCEPT, false));
            }
        }
    }

    /**
     * Adds the component to the <code>inactivationIndicatorRefsetNid</code> if
     * the component is not active current or inactive.
     *
     * @param componentDto ComponentDto to add the member to
     * @param tuple I_ConceptAttributeTuple
     * @param inactivationIndicatorRefsetNid int the inactivation refset id
     *            (concept, description or relationship)
     * @param type TYPE
     * @throws IOException
     * @throws TerminologyException
     */
    private void setComponentInactivationReferenceSet(List<ExtensionDto> extensionDtos, int componentNid,
            I_AmTuple tuple, int inactivationIndicatorRefsetNid, TYPE type) throws Exception {
        I_ThinExtByRefVersioned extensionVersioned = null;

        if (tuple != null) {
            int rf2InactiveStatus = getRf2Status(tuple.getStatusId());
            if (rf2InactiveStatus != -1) {
                // if the status is INACTIVE or ACTIVE there is no need for a
                // reason. For simplicity, CURRENT will be treated this way too,
                if (tuple.getStatusId() != activeConcept.getNid() && tuple.getStatusId() != inActiveConcept.getNid()
                    && tuple.getStatusId() != currentConcept.getNid()) {

                    extensionVersioned = getThinExtByRefTuple(inactivationIndicatorRefsetNid, 0, componentNid,
                        rf2InactiveStatus, tuple);

                    //Fixed for R1 data, remove parts that don't relate to the tuple part
                    List<I_ThinExtByRefPart> partsToRemove = new ArrayList<I_ThinExtByRefPart>();
                    for (I_ThinExtByRefPart part : extensionVersioned.getVersions()) {
                        if(part.getPathId() != tuple.getPathId()
                                || part.getVersion() != tuple.getVersion()){
                            partsToRemove.add(part);
                        }
                    }
                    extensionVersioned.getVersions().removeAll(partsToRemove);

                    //Left with one part so make the membership active
                    extensionVersioned.getVersions().get(0).setStatusId(activeConcept.getNid());
                }
            } else {
                //if no inactivation, check for previous inactivation
                extensionVersioned = getRefsetExtensionVersioned(inactivationIndicatorRefsetNid, componentNid);
                if (extensionVersioned != null) {
                    retireLastestPart(extensionVersioned, tuple);
                }
            }

            if (extensionVersioned != null) {
                extensionDtos.addAll(extensionProcessor.processList(extensionVersioned,
                    extensionVersioned.getVersions(), type, false));
            }
        }
    }

    /**
     * Adds the component to the <code>inactivationIndicatorRefsetNid</code> if
     * the component is not active current or inactive.
     *
     * @param extensionDtos
     * @param componentNid
     * @param tuples
     * @param inactivationIndicatorRefsetNid
     * @param type
     * @throws Exception
     */
    private void setComponentInactivationReferenceSet(List<ExtensionDto> extensionDtos, int componentNid,
            Collection<? extends I_AmTuple> tuples, int inactivationIndicatorRefsetNid, TYPE type) throws Exception {
        setComponentInactivationReferenceSet(extensionDtos, componentNid, TupleVersionPart.getLatestPart(tuples), inactivationIndicatorRefsetNid, type);

    }

    /**
     * Export relationship refinability (relationships refinability reference) for all relationships.
     *
     * @param extensionDtos to add the refinability to
     * @param relationshipTuple I_RelTuple
     * @throws Exception
     */
    private void setRelationshipRefinabilityReferenceSet(List<ExtensionDto> extensionDtos,
            I_RelTuple relationshipTuple, boolean latest) throws Exception {
        I_ThinExtByRefVersioned extensionVersioned = getThinExtByRefTuple(relationshipRefinabilityExtensionNid, 0,
            relationshipTuple.getRelId(), relationshipTuple.getRefinabilityId(), relationshipTuple);

        List<I_ThinExtByRefPart> partsToRemove = new ArrayList<I_ThinExtByRefPart>();
        for (I_ThinExtByRefPart part : extensionVersioned.getVersions()) {
            if(part.getPathId() != relationshipTuple.getPathId()
                    || part.getVersion() != relationshipTuple.getVersion()){
                partsToRemove.add(part);
            }
        }
        extensionVersioned.getVersions().removeAll(partsToRemove);

        extensionDtos.addAll(extensionProcessor.processList(extensionVersioned, extensionVersioned.getVersions(),
            TYPE.RELATIONSHIP, false));
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
    private void setCtv3ReferenceSet(ComponentDto componentDto, I_ConceptAttributeTuple tuple, I_IdPart ctv3IdPart, boolean latest)
            throws Exception {
        I_ThinExtByRefVersioned ctv3Versioned = getThinExtByRefTuple(ctv3IdMapExtension.getConceptId(), 0, tuple.getConId(),
            ctv3IdPart, ctv3IdPart.getSourceId().toString());

        List<I_ThinExtByRefPart> partsToRemove = new ArrayList<I_ThinExtByRefPart>();
        for (I_ThinExtByRefPart part : ctv3Versioned.getVersions()) {
            if(part.getPathId() != ctv3IdPart.getPathId()
                    || part.getVersion() != ctv3IdPart.getVersion()){
                partsToRemove.add(part);
            }
        }
        ctv3Versioned.getVersions().removeAll(partsToRemove);


        componentDto.getConceptExtensionDtos().addAll(
            extensionProcessor.processList(ctv3Versioned, ctv3Versioned.getVersions(), TYPE.CONCEPT, false));
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
    private void setSnomedRtIdReferenceSet(ComponentDto componentDto, I_ConceptAttributeTuple tuple, I_IdPart snomedIdPart, boolean latest)
            throws Exception {
        I_ThinExtByRefVersioned snomedIdVersioned = getThinExtByRefTuple(snomedRtIdMapExtension.getConceptId(), 0,
            tuple.getConId(), snomedIdPart, snomedIdPart.getSourceId().toString());

        List<I_ThinExtByRefPart> partsToRemove = new ArrayList<I_ThinExtByRefPart>();
        for (I_ThinExtByRefPart part : snomedIdVersioned.getVersions()) {
            if(part.getPathId() != snomedIdPart.getPathId()
                    || part.getVersion() != snomedIdPart.getVersion()){
                partsToRemove.add(part);
            }
        }
        snomedIdVersioned.getVersions().removeAll(partsToRemove);

        componentDto.getConceptExtensionDtos().addAll(
            extensionProcessor.processList(snomedIdVersioned, snomedIdVersioned.getVersions(), TYPE.CONCEPT, false));
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
     * @throws Exception
     */
    private I_ThinExtByRefVersioned getThinExtByRefTuple(int refsetNid, int memberNid, int referencedComponentNid,
            int component1, I_AmPart amPart) throws Exception {
        I_ThinExtByRefVersioned thinExtByRefVersioned = getRefsetExtensionVersioned(refsetNid, referencedComponentNid);
        I_ThinExtByRefPartConcept conceptExtension = null;

        if (thinExtByRefVersioned == null) {
            thinExtByRefVersioned = new ThinExtByRefVersioned(refsetNid, memberNid, referencedComponentNid,
                conceptExtensionNid);
        } else {
            for (I_ThinExtByRefPart conceptParts : thinExtByRefVersioned.getVersions()) {
                if(conceptParts.getPathId() == amPart.getPathId()
                        && conceptParts.getStatusId() == amPart.getStatusId()
                        && conceptParts.getVersion() == amPart.getVersion()){
                    conceptExtension = (I_ThinExtByRefPartConcept) conceptParts;
                    break;
                }
            }
        }

        if (conceptExtension == null) {
            conceptExtension = new ThinExtByRefPartConcept();

            conceptExtension.setC1id(component1);
            conceptExtension.setPathId(amPart.getPathId());
            conceptExtension.setStatusId(amPart.getStatusId());
            conceptExtension.setVersion(amPart.getVersion());

            thinExtByRefVersioned.addVersion(conceptExtension);
        }

        return thinExtByRefVersioned;
    }

    /**
     * Adds a retired part to the versioned.
     *
     * @param thinExtByRefVersioned I_ThinExtByRefVersioned
     * @param retireForPart I_AmPart path and version to use for the retired part
     */
    private void retireLastestPart(I_ThinExtByRefVersioned thinExtByRefVersioned, I_AmPart retireForPart) {
        ThinExtByRefPartConcept latestPart = (ThinExtByRefPartConcept) TupleVersionPart.getLatestPart(thinExtByRefVersioned.getVersions());

        latestPart.setC1id(latestPart.getC1id());
        latestPart.setPathId(retireForPart.getPathId());
        latestPart.setStatusId(retiredConcept.getNid());
        latestPart.setVersion(retireForPart.getVersion());
    }

    /**
     * Create a I_ThinExtByRefTuple for a string reference set member.
     *
     * Path version and status are copied from amPart.
     *
     * @param refsetNid int
     * @param memberNid int
     * @param referencedComponentNid int
     * @param amPart I_AmPart
     * @param string String
     * @return
     * @throws Exception
     */
    private I_ThinExtByRefVersioned getThinExtByRefTuple(int refsetNid, int memberNid, int referencedComponentNid,
            I_AmPart amPart, String string) throws Exception {
        I_ThinExtByRefVersioned thinExtByRefVersioned = getRefsetExtensionVersioned(refsetNid, referencedComponentNid);
        I_ThinExtByRefPartString conceptExtension = null;

        if (thinExtByRefVersioned == null) {
            thinExtByRefVersioned = new ThinExtByRefVersioned(refsetNid, memberNid, referencedComponentNid,
                stringExtensionNid);
        } else {
            for (I_ThinExtByRefPart stringParts : thinExtByRefVersioned.getVersions()) {
                if(stringParts.getPathId() == amPart.getPathId()
                        && stringParts.getStatusId() == amPart.getStatusId()
                        && stringParts.getVersion() == amPart.getVersion()){
                    conceptExtension = (I_ThinExtByRefPartString) stringParts;
                    break;
                }
            }
        }

        if(conceptExtension == null){
            conceptExtension = new ThinExtByRefPartString();
            conceptExtension.setStringValue(string);
            conceptExtension.setPathId(amPart.getPathId());
            conceptExtension.setStatusId(amPart.getStatusId());
            conceptExtension.setVersion(amPart.getVersion());
            thinExtByRefVersioned.addVersion(conceptExtension);
        }

        return thinExtByRefVersioned;
    }

    /**
     * Obtain all current extensions (latest part only) for a particular refset
     * that exist on a
     * specific concept.
     *
     * This method is strongly typed. The caller must provide the actual type of
     * the refset.
     *
     * @param <T> the strong/concrete type of the refset extension
     * @param refsetId Only returns extensions matching this reference set
     * @param conceptId Only returns extensions that exists on this concept
     * @return All matching refset extension (latest version parts only)
     * @throws Exception if unable to complete (never returns null)
     * @throws ClassCastException if a matching refset extension is not of type
     *             T
     */
    public I_ThinExtByRefVersioned getRefsetExtensionVersioned(int refsetId, int conceptId)
            throws Exception {
        I_ThinExtByRefVersioned result = null;

        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(conceptId)) {
            if (extension.getRefsetId() == refsetId) {
                result = extension;
                break;
            }
        }

        return result;
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
    private BaseConceptDto getBaseConceptDto(BaseConceptDto baseConceptDto, I_AmPart tuple, List<I_IdPart> idVersions, boolean latest) throws IOException, TerminologyException {
        baseConceptDto.setLatest(latest);
        baseConceptDto.setActive(isActive(tuple.getStatusId()));
        baseConceptDto.setDateTime(new Date(tuple.getTime()));
        baseConceptDto.setNamespace(getNamespace(idVersions,tuple));
        baseConceptDto.setProject(getProject(tuple));
        baseConceptDto.setPathId(termFactory.getConcept(tuple.getPathId()).getUids().get(0));
        baseConceptDto.setStatusId(termFactory.getConcept(tuple.getStatusId()).getUids().get(0));
        baseConceptDto.setStatusCode(ArchitectonicAuxiliary.getSnomedConceptStatusId(
            termFactory.getConcept(tuple.getStatusId()).getUids()) + "");

        return baseConceptDto;
    }

    /**
     * Adds an identifier row for UUID (type 3 and 5) to SCTID to the ConceptDto.
     *
     * If there is no SCTID in the Berkeley database a new identifierDto is created and added to the concept.
     *
     * @param conceptDto ConceptDto
     * @param tuple I_ConceptAttributeTuple
     * @param conceptData I_GetConceptData
     * @param type
     * @throws Exception
     */
    private void setUuidSctIdIdentifier(ConceptDto conceptDto, I_AmPart tuple,
            List<I_IdPart> idVersions, TYPE type, int componentNid, boolean latest) throws Exception {

        UUID uuid = null;
        I_IdPart type5UuidPart = getLatesIdtVersion(idVersions, unspecifiedUuid.getConceptId(), tuple);
        I_IdPart type3UidPart = getLatesIdtVersion(idVersions, snomedT3Uuid.getConceptId(), tuple);
        I_IdPart sctIdPart = getLatesIdtVersion(idVersions, snomedIntId.getConceptId(), tuple);

        if (type5UuidPart != null) {
            uuid = UUID.fromString(type5UuidPart.getSourceId().toString());
        } else if (type3UidPart != null) {
            uuid = UUID.fromString(type3UidPart.getSourceId().toString());
        } else {
            uuid = termFactory.getUids(componentNid).iterator().next();
        }

        // If there is no sct id the concept is not live (previously released)
        conceptDto.setLive(sctIdPart != null);
        if (sctIdPart == null) {
            sctIdPart = new ThinIdPart();

            sctIdPart.setPathId(tuple.getPathId());
            sctIdPart.setSource(snomedIntId.getConceptId());
            sctIdPart.setSourceId(uuidSnomedDbMapHandler.getWithGeneration(uuid, getNamespace(idVersions, tuple), type, getProject(tuple)));
            sctIdPart.setStatusId(activeConcept.getConceptId());
            sctIdPart.setVersion(tuple.getVersion());
        }

        setIdentifier(conceptDto, tuple, idVersions, type, uuid, sctIdPart, latest);
    }

    /**
     * Get the UUID to sct id map. If no sct id exists then the uuid is mapped to null
     *
     * @param tuple I_AmPart
     * @param componentNid int
     * @return Map for UUID to Long sct id
     * @throws TerminologyException
     * @throws IOException
     */
    private Map<UUID, Long> getIdMap(I_AmPart tuple, int componentNid) throws TerminologyException, IOException {
        Map<UUID, Long> map = new HashMap<UUID, Long>();
        List<I_IdPart> versions = termFactory.getId(componentNid).getVersions();

        I_IdPart t3UuidPart = getLatesIdtVersion(versions, snomedT3Uuid.getNid(), tuple);
        I_IdPart uuidPart = getLatesIdtVersion(versions, unspecifiedUuid.getNid(), tuple);
        I_IdPart sctIdPart = getLatesIdtVersion(versions, snomedIntId.getConceptId(), tuple);

        Long sctId = (sctIdPart != null) ? Long.parseLong(sctIdPart.getSourceId().toString()) : null;
        if(t3UuidPart != null){
            map.put(UUID.fromString(t3UuidPart.getSourceId().toString()), sctId);
        } else if (uuidPart != null) {
            map.put(UUID.fromString(uuidPart.getSourceId().toString()), sctId);
        } else {
            map.put(termFactory.getUids(componentNid).iterator().next(), sctId);
        }

        return map;
    }

    /**
     * Sets the id mapping for the UUID to SCTID
     *
     * @param conceptDto ConceptDto to add the identifier to.
     * @param tuple I_AmPart
     * @param idVersions List of I_IdPart
     * @param type TYPE
     * @param uuid UUID
     * @param sctIdPart I_IdPart
     * @throws IOException
     * @throws TerminologyException
     */
    private void setIdentifier(ConceptDto conceptDto, I_AmPart tuple, List<I_IdPart> idVersions, TYPE type,
            UUID uuid, I_IdPart sctIdPart, boolean latest) throws IOException, TerminologyException {
        Map<UUID, Long> idMap = new HashMap<UUID, Long>();;
        IdentifierDto identifierDto = new IdentifierDto();

        getBaseConceptDto(identifierDto, tuple, idVersions, latest);

        idMap.put(uuid, Long.valueOf(sctIdPart.getSourceId().toString()));
        identifierDto.setConceptId(idMap);
        identifierDto.setType(type);
        identifierDto.setActive(isActive(sctIdPart.getStatusId()));
        identifierDto.setLive(conceptDto.isLive());
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
     * Get the SNOMED name space for the tuple based on the SCTID of the concept
     * or if no SCTID then use the tuple path.
     *
     * NB currently if no SCTID and not the international path then the
     * defaultNamespace is returned.
     *
     * @param tuple I_AmPart
     *
     * @return NAMESPACE
     *
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    private PROJECT getProject(I_AmPart tuple) throws IOException, TerminologyException {
        PROJECT project;

        if (isInternationalPath(tuple.getPathId())) {
            project = PROJECT.SNOMED_CT;
        } else {
            project = defaultProject;
        }

        return project;
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
     * Checks if the Status equals the Concept.ACTIVE or Current
     * or is a child of Concept.ACTIVE
     *
     * @param statusNid int
     * @return boolean true if the statusNid is active
     * @throws TerminologyException DB error
     * @throws IOException DB error
     */
    private boolean isActive(final int statusNid) throws IOException, TerminologyException {
        boolean activate = false;
        I_GetConceptData statusConcept = termFactory.getConcept(statusNid);

        if (rf2ActiveConcept.isParentOf(statusConcept, null, null, null, false)) {
            activate = true;
        } else if (rf2ActiveConcept.getNid() == statusConcept.getNid()) {
            activate = true;
        } else if (activeConcept.getNid() == statusConcept.getNid()) {
            activate = true;
        } else if (currentConcept.getNid() == statusConcept.getNid()) {
            activate = true;
        }

        return activate;
    }

    /**
     * Checks if the Status equals ACTIVE, Current, Pending Move, Concept Retired, Moved Elsewhere
     *
     * @param statusNid int
     * @return boolean true if the statusNid is active
     * @throws TerminologyException DB error
     * @throws IOException DB error
     */
    private boolean isDescriptionActive(final int statusNid) {
        boolean activate = false;

        if (activeConcept.getNid() == statusNid) {
            activate = true;
        } else if (currentConcept.getNid() == statusNid) {
            activate = true;
        } else if (pendingMove.getNid() == statusNid) {
            activate = true;
        } else if(conceptRetired.getNid() == statusNid) {
            activate = true;
        } else if (aceMovedElsewhereStatusNId == statusNid) {
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
        } else if (statusNid == aceLimitedStatusNId) {
            rf2Status = limitedStatusNId;
        }

        return rf2Status;
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
            if (includeConceptData.isParentOf(concept, currentActiveStatusIntSet, snomedIsATypeIntSet, null, false)) {
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
            if (excludedConceptData.isParentOf(concept, currentActiveStatusIntSet, snomedIsATypeIntSet, null, false)) {
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
     * @param releasePart the releasePart to set
     */
    public final void setReleasePart(ThinConPart releasePart) {
        this.releasePart = releasePart;
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
         * @throws Exception ye'old DB errors
         */
        ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned, T tuple, TYPE type, boolean latest)  throws Exception;
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
        public List<ExtensionDto> processList(I_ThinExtByRefVersioned thinExtByRefVersioned,
                Collection<? extends I_ThinExtByRefPart> list, TYPE type, boolean isClinical) throws Exception {
            List<ExtensionDto> extensionDtos = new ArrayList<ExtensionDto>();
            I_ThinExtByRefPart latestPart = TupleVersionPart.getLatestPart(list);

            for (I_ThinExtByRefPart t : list) {
                I_GetConceptData refsetConcept = termFactory.getConcept(thinExtByRefVersioned.getRefsetId());
                if (isExportableConcept(refsetConcept)) {
                    I_AmExtensionProcessor<T> extensionProcessor = extensionMap.get(thinExtByRefVersioned.getTypeId());
                    if(extensionProcessor != null){
                        ExtensionDto extensionDto = extensionProcessor.getExtensionDto(thinExtByRefVersioned, (T) t, type, t.getVersion() == latestPart.getVersion());
                        extensionDto.setIsClinical(isClinical);
                        extensionDtos.add(extensionDto);
                    } else {
                        logger.severe("No extension processor for refset " + termFactory.getConcept(thinExtByRefVersioned.getRefsetId()).getInitialText());
                        logger.severe("No extension processor for concept " + termFactory.getConcept(thinExtByRefVersioned.getComponentId()).getInitialText());
                        logger.severe("No extension processor for type " + termFactory.getConcept(thinExtByRefVersioned.getTypeId()).getInitialText());
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
         * @throws Exception
         * @see org.dwfa.mojo.export.ExportSpecification.I_AmExtensionProcessor#getExtensionDto(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, org.dwfa.maven.transform.SctIdGenerator.TYPE)
         *
         * Creates the ExtensionDto for a Concept extension.
         */
        @Override
        public ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned,
                I_ThinExtByRefPart tuple, TYPE type, boolean latest) throws Exception {
            ExtensionDto extensionDto = new ExtensionDto();
            List<I_IdPart> idParts;

            if(thinExtByRefVersioned.getMemberId() != 0){
                idParts = termFactory.getId(thinExtByRefVersioned.getMemberId()).getVersions();
            } else {
                idParts = new ArrayList<I_IdPart>(1);
                idParts.add(getIdUuidSctIdPart(thinExtByRefVersioned, tuple));
            }

            getBaseConceptDto(extensionDto, tuple, idParts, latest);

            setUuidSctIdIdentifier(extensionDto, tuple, idParts, TYPE.REFSET, thinExtByRefVersioned.getMemberId(), latest);

            extensionDto.setConceptId(getIdMap(tuple, thinExtByRefVersioned.getRefsetId()));

            extensionDto.setReferencedConceptId(getIdMap(tuple, thinExtByRefVersioned.getComponentId()));

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
            sctUuidPart.setSource(unspecifiedUuid.getNid());
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
                I_ThinExtByRefPartConcept tuple, TYPE type, boolean latest) throws Exception {
            ExtensionDto extensionDto = extensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type, latest);

            extensionDto.setConcept1Id(getIdMap(tuple, tuple.getC1id()));

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
                I_ThinExtByRefPartConceptString tuple, TYPE type, boolean latest) throws Exception {
            ExtensionDto extensionDto = conceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type, latest);

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
                I_ThinExtByRefPartString tuple, TYPE type, boolean latest) throws Exception {
            ExtensionDto extensionDto = extensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type, latest);

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
                I_ThinExtByRefPartConceptInt tuple, TYPE type, boolean latest) throws Exception {
            ExtensionDto extensionDto = conceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type, latest);

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
                I_ThinExtByRefPartConceptConcept tuple, TYPE type, boolean latest) throws Exception {
            ExtensionDto extensionDto = conceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type, latest);

            extensionDto.setConcept2Id(getIdMap(tuple, tuple.getC2id()));

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
                I_ThinExtByRefPartConceptConceptString tuple, TYPE type, boolean latest) throws Exception {
            ExtensionDto extensionDto = conceptConceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type, latest);

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
                I_ThinExtByRefPartConceptConceptConcept tuple, TYPE type, boolean latest) throws Exception {
            ExtensionDto extensionDto = conceptConceptExtensionProcessor.getExtensionDto(thinExtByRefVersioned, tuple, type, latest);

            extensionDto.setConcept3Id(getIdMap(tuple, tuple.getC3id()));

            return extensionDto;
        }
    }
}
