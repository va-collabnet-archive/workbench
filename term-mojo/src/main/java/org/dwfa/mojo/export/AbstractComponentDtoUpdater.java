/*
 *  Copyright 2010 International Health Terminology Standards Development
 *  Organisation.
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
package org.dwfa.mojo.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.util.TupleVersionComparator;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.dto.BaseConceptDto;
import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.DescriptionDto;
import org.dwfa.dto.IdentifierDto;
import org.dwfa.maven.sctid.SctIdValidator;
import org.dwfa.maven.sctid.UuidSnomedDbMapHandler;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ThinIdPart;

/**
 *
 * @author Matthew Edwards
 */
public abstract class AbstractComponentDtoUpdater {

    /** Class logger. */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    /** TermFactory. */
    protected static final I_TermFactory termFactory = LocalVersionedTerminology.get();
    /** SNOMED Id concept. */
    private final I_GetConceptData snomedIntId;
    /** SNOMED UUID concept. */
    private final I_GetConceptData snomedT3Uuid;
    /** UUID Source concept. */
    private final I_GetConceptData unspecifiedUuid;
    /** Mapping of UUID's to SCT ids. */
    private final UuidSnomedDbMapHandler uuidSnomedDbMapHandler;
    /** The active concept. */
    protected final I_GetConceptData activeConcept;
    /** The default namespace to use for export. */
    private final NAMESPACE defaultNamespace;
    /** The default project to use for export. */
    private final PROJECT defaultProject;
    /** International release path. */
    private final I_GetConceptData snomedReleasePath;
    /** The active concept. */
    protected final I_GetConceptData currentConcept;
    protected final int aceLimitedStatusNId;
    /** definition status */
    private final I_GetConceptData primationDefinitionStatusConcept;
    /** definition status */
    private final I_GetConceptData fullyDefinedDefinitionStatusConcept;
    /** Initial case not sensitive concepts. */
    private final I_GetConceptData initialCharacterNotCaseSensitive;
    /** Initial all characters sensitive concepts. */
    private final I_GetConceptData allCharactersCaseSensitive;
    /** Fully specified name description type. */
    private final I_GetConceptData fullySpecifiedDescriptionType;
    /** Synonym description type. */
    private final I_GetConceptData synonymDescriptionType;
    /** Synonym description type. */
    private final I_GetConceptData conceptNonCurrentStatus;
    protected StatusChecker check;

    public AbstractComponentDtoUpdater(NAMESPACE defaultNamespace, PROJECT defaultProject) throws Exception {

        snomedIntId = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getUids().iterator().next());

        snomedT3Uuid = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.SNOMED_T3_UUID.localize().getUids().iterator().next());

        unspecifiedUuid = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getUids().iterator().next());

        uuidSnomedDbMapHandler = UuidSnomedDbMapHandler.getInstance();

        uuidSnomedDbMapHandler.updateNextSequenceMap();

        activeConcept = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.ACTIVE.localize().getUids().iterator().next());

        this.defaultNamespace = defaultNamespace;

        this.defaultProject = defaultProject;

        snomedReleasePath = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.SNOMED_CORE.localize().getUids().iterator().next());

        aceLimitedStatusNId = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();

        conceptNonCurrentStatus = termFactory.getConcept(ConceptConstants.CONCEPT_NON_CURRENT.localize().getNid());

        currentConcept = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.CURRENT.localize().getUids().iterator().next());

        primationDefinitionStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PRIMITIVE_DEFINITION.
                getUids());

        fullyDefinedDefinitionStatusConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINED_DEFINITION.
                getUids());

        initialCharacterNotCaseSensitive = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.INITIAL_CHARACTER_NOT_CASE_SENSITIVE.localize().getUids().iterator().next());

        allCharactersCaseSensitive = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.ALL_CHARACTERS_CASE_SENSITIVE.localize().getUids().iterator().next());


        fullySpecifiedDescriptionType = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getUids().iterator().next());

        synonymDescriptionType = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getUids().iterator().next());
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
     * @throws Exception DB errors
     * @throws TerminologyException DB errors
     */
    public abstract void updateComponentDto(ComponentDto componentDto, I_RelTuple tuple, boolean latest)
            throws Exception, TerminologyException;

    /**
     * Update the ComponentDto with the concept details.
     *
     * Adds ctv3 and SNOMED RT id map members.
     *
     * Adds concept inactivation members.
     *
     * @param componentDto ComponentDto - updated with the concept details
     * @param tuple I_ConceptAttributeTuple - to add to the ComponentDto
     * @param descriptionTuples list of descriptions on the export path/s
     * @throws Exception
     */
    public abstract ComponentDto updateComponentDto(ComponentDto componentDto, I_ConceptAttributeTuple tuple, Collection<I_DescriptionTuple> descriptionTuples, boolean latest)
            throws Exception;

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
    public BaseConceptDto getBaseConceptDto(BaseConceptDto baseConceptDto, I_AmPart tuple, List<I_IdPart> idVersions, boolean latest) throws IOException, TerminologyException {
        baseConceptDto.setLatest(latest);
        baseConceptDto.setActive(check.isActive(tuple.getStatusId()));
        baseConceptDto.setDateTime(new Date(tuple.getTime()));
        baseConceptDto.setNamespace(getNamespace(idVersions, tuple));
        baseConceptDto.setProject(getProject(tuple));
        baseConceptDto.setPathId(termFactory.getConcept(tuple.getPathId()).getUids().get(0));
        baseConceptDto.setStatusId(termFactory.getConcept(tuple.getStatusId()).getUids().get(0));
        baseConceptDto.setStatusCode(ArchitectonicAuxiliary.getSnomedConceptStatusId(
                termFactory.getConcept(tuple.getStatusId()).getUids()) + "");

        return baseConceptDto;
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
    public Map<UUID, Long> getIdMap(I_AmPart tuple, int componentNid) throws TerminologyException, IOException {
        Map<UUID, Long> map = new HashMap<UUID, Long>();
        List<I_IdPart> versions = termFactory.getId(componentNid).getVersions();

        I_IdPart t3UuidPart = getLatesIdtVersion(versions, snomedT3Uuid.getNid(), tuple);
        I_IdPart uuidPart = getLatesIdtVersion(versions, unspecifiedUuid.getNid(), tuple);
        I_IdPart sctIdPart = getLatesIdtVersion(versions, snomedIntId.getConceptId(), tuple);

        Long sctId = (sctIdPart != null) ? Long.parseLong(sctIdPart.getSourceId().toString()) : null;
        if (t3UuidPart != null) {
            map.put(UUID.fromString(t3UuidPart.getSourceId().toString()), sctId);
        } else if (uuidPart != null) {
            map.put(UUID.fromString(uuidPart.getSourceId().toString()), sctId);
        } else {
            map.put(termFactory.getUids(componentNid).iterator().next(), sctId);
        }

        return map;
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
    public void addUuidSctIdIndentifierToConceptDto(ConceptDto conceptDto, I_AmPart tuple,
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
        if (sctIdPart == null && check.isActive(tuple.getStatusId())) {
            sctIdPart = new ThinIdPart();

            sctIdPart.setPathId(tuple.getPathId());
            sctIdPart.setSource(unspecifiedUuid.getConceptId());
            sctIdPart.setSourceId(uuidSnomedDbMapHandler.getWithGeneration(uuid, getNamespace(idVersions, tuple), type, getProject(tuple)));
            sctIdPart.setStatusId(activeConcept.getConceptId());
            sctIdPart.setVersion(tuple.getVersion());
        }

        if (sctIdPart != null) {
            setIdentifier(conceptDto, idVersions, type, uuid, sctIdPart, latest);
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
    public NAMESPACE getNamespace(List<I_IdPart> idVersions, I_AmPart tuple) throws IOException, TerminologyException {
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
    protected final I_IdPart getLatesIdtVersion(List<I_IdPart> idParts, int sourceIdConceptNid, I_AmPart attributeTuple)
            throws TerminologyException, IOException, NoMappingException {
        I_IdPart latestIdPart = null;

        for (I_IdPart iIdPart : idParts) {
            if (iIdPart.getSource() == sourceIdConceptNid
                    && ((latestIdPart == null && iIdPart.getVersion() <= attributeTuple.getVersion()) || (latestIdPart != null
                    && iIdPart.getVersion() > latestIdPart.getVersion() && latestIdPart.getVersion() <= attributeTuple.
                    getVersion()))) {
                latestIdPart = iIdPart;
            }
        }

        return latestIdPart;
    }

    /**
     * Sets the id mapping for the UUID to SCTID
     *
     * @param conceptDto ConceptDto to add the identifier to.
     * @param idVersions List of I_IdPart
     * @param type TYPE
     * @param uuid UUID
     * @param sctIdPart I_IdPart
     * @throws IOException
     * @throws TerminologyException
     */
    private void setIdentifier(ConceptDto conceptDto, List<I_IdPart> idVersions, TYPE type,
            UUID uuid, I_IdPart sctIdPart, boolean latest) throws IOException, TerminologyException {
        Map<UUID, Long> idMap = new HashMap<UUID, Long>();

        IdentifierDto identifierDto = new IdentifierDto();

        getBaseConceptDto(identifierDto, sctIdPart, idVersions, latest);

        idMap.put(uuid, Long.valueOf(sctIdPart.getSourceId().toString()));
        identifierDto.setConceptId(idMap);
        identifierDto.setType(type);
        identifierDto.setActive(check.isActive(sctIdPart.getStatusId()));
        identifierDto.setLive(conceptDto.isLive());
        identifierDto.setReferencedSctId(Long.valueOf(sctIdPart.getSourceId().toString()));
        identifierDto.setIdentifierSchemeUuid(snomedIntId.getUids().get(0));

        conceptDto.getIdentifierDtos().add(identifierDto);
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
     * Gets the UUID for the concept
     *
     * @param definitionInt String 0 = well defined 1 = primative
     *
     * @return sctid String
     * @throws IOException
     */
    protected UUID getDefinitionStatusUuid(boolean defined) throws IOException {
        UUID definitionSctId = primationDefinitionStatusConcept.getUids().get(0);
        if (defined) {
            definitionSctId = fullyDefinedDefinitionStatusConcept.getUids().get(0);
        }
        return definitionSctId;
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
    public void updateComponentDto(ComponentDto componentDto, I_DescriptionTuple tuple, boolean latest) throws Exception {
        DescriptionDto descriptionDto = new DescriptionDto();
        List<I_IdPart> idParts = termFactory.getId(tuple.getDescId()).getVersions();

        getBaseConceptDto(descriptionDto, tuple, idParts, latest);

        descriptionDto.setRf2DateTime(new Date(
                getRf2DescriptionEffectiveDateMap(tuple.getDescVersioned().getTuples()).get(tuple.getTime())));

        descriptionDto.setActive(check.isDescriptionActive(tuple.getStatusId()));
        descriptionDto.setStatusCode(ArchitectonicAuxiliary.getSnomedDescriptionStatusId(
                termFactory.getConcept(tuple.getStatusId()).getUids()) + "");

        if ("-1".equals(descriptionDto.getStatusCode())) {
            if (tuple.getStatusId() == conceptNonCurrentStatus.getNid()) {
                descriptionDto.setStatusCode("8");
            }
        }

        addUuidSctIdIndentifierToConceptDto(descriptionDto, tuple, idParts, TYPE.DESCRIPTION, tuple.getDescId(), latest);

        descriptionDto.setCaseSignificanceId(getInitialCaseSignificant(tuple.getInitialCaseSignificant()));
        descriptionDto.setConceptId(getIdMap(tuple, tuple.getConceptId()));
        descriptionDto.setDescription(tuple.getText());
        descriptionDto.setDescriptionId(termFactory.getUids(tuple.getDescId()).iterator().next());
        descriptionDto.setDescriptionTypeCode(Character.forDigit(
                ArchitectonicAuxiliary.getSnomedDescriptionTypeId(termFactory.getUids(tuple.getTypeId())), 10));
        descriptionDto.setInitialCapitalStatusCode(tuple.getInitialCaseSignificant() ? '1' : '0');
        descriptionDto.setLanguageCode(tuple.getLang());
        descriptionDto.setLanguageId(ArchitectonicAuxiliary.getLanguageConcept(tuple.getLang()).getUids().iterator().
                next());
        descriptionDto.setNamespace(getNamespace(idParts, tuple));
        descriptionDto.setType(TYPE.DESCRIPTION);
        descriptionDto.setTypeId(termFactory.getUids(tuple.getTypeId()).iterator().next());
        descriptionDto.setRf2TypeId(getRf2DescriptionType(tuple.getTypeId()));

        componentDto.getDescriptionDtos().add(descriptionDto);
    }

    /**
     * Gets the latest active FSN description from the description collection
     * @param descriptionTuples Collection of I_DescriptionTuple
     * @return String or null if no active FSN
     */
    protected String getFsn(Collection<I_DescriptionTuple> descriptionTuples) {
        List<I_DescriptionTuple> descriptionTupleList = new ArrayList<I_DescriptionTuple>();
        for (I_DescriptionTuple iDescriptionTuple : descriptionTuples) {
            if(fullySpecifiedDescriptionType.getConceptId() == iDescriptionTuple.getTypeId()){
                descriptionTupleList.add(iDescriptionTuple);
            }
        }
        Collections.sort(descriptionTupleList, new TupleVersionComparator());

        String fsn = null;
        I_DescriptionTuple fsnTuple = null;
        if (!descriptionTupleList.isEmpty()) {
            for (I_DescriptionTuple iDescriptionTuple : descriptionTupleList) {
                if (check.isDescriptionActive(iDescriptionTuple.getStatusId()) || iDescriptionTuple.getStatusId() == aceLimitedStatusNId) {
                    if (fsnTuple == null || fsnTuple.getVersion() < iDescriptionTuple.getVersion()) {
                        fsnTuple = iDescriptionTuple;
                    }
                }
            }

            //If no active/limited FSN get the latest inactive FSN
            if (fsnTuple != null) {
                fsn = fsnTuple.getText();
            } else {
                fsn = descriptionTupleList.iterator().next().getText();
            }
        }

        return fsn;
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
     * Get the Rf2 description type for the Architectonic description type.
     *
     * @param typeNid int
     * @return UUID for either Fully specified name or synonym
     * @throws IOException
     */
    private UUID getRf2DescriptionType(int typeNid) throws IOException {
        UUID rf2DescriptionType = synonymDescriptionType.getUids().get(0);

        if (fullySpecifiedDescriptionType.getNid() == typeNid) {
            rf2DescriptionType = fullySpecifiedDescriptionType.getUids().get(0);
        }

        return rf2DescriptionType;
    }


    /**
     * Map the Ace versions to RF2 versions to account for the differences in RF1 and RF2 status
     *
     * @param conceptTuples List of I_ConceptAttributeTuple
     * @param check StatusChecker
     * @return Map of Ace version dates to RF2 version dates
     * @throws Exception DB errors
     */
    public final Map<Long,Long> getRf2ConceptEffectiveDateMap(final List<I_ConceptAttributeTuple> conceptTuples) throws Exception {
        Map<Long,Long> effectiveDateMap = new HashMap<Long,Long>();
        Collections.sort(conceptTuples, new TupleVersionComparator());
        Collections.reverse(conceptTuples);
        I_ConceptAttributeTuple firstChange = null;

        for (I_ConceptAttributeTuple conceptAttributeTuple : conceptTuples) {
            if(firstChange == null
                    || check.isActive(firstChange.getPart().getStatusId()) != check.isActive(conceptAttributeTuple.getPart().getStatusId())
                    || firstChange.getPart().getPathId() != conceptAttributeTuple.getPart().getPathId()
                    || firstChange.getPart().isDefined() != conceptAttributeTuple.getPart().isDefined()){
                firstChange = conceptAttributeTuple;
            }

            effectiveDateMap.put(ThinVersionHelper.convert(conceptAttributeTuple.getVersion()),
                    ThinVersionHelper.convert(firstChange.getVersion()));
        }

        return effectiveDateMap;

    }

    /**
     * Map the Ace versions to RF2 versions to account for the differences in RF1 and RF2 status
    String descriptionSctId;
    String effectiveTime;
    String active;
    String moduleSctId;
    String conceptSctId;
    String lanaguageCode;
    String typeSctId;
    String term;
    String caseSignificaceSctId;

     *
     * @param conceptTuples List of I_ConceptAttributeTuple
     * @param check StatusChecker
     * @return Map of Ace version dates to RF2 version dates
     * @throws Exception DB errors
     */
    public final Map<Long,Long> getRf2DescriptionEffectiveDateMap(final List<I_DescriptionTuple> descriptionTuples) throws Exception {
        Map<Long,Long> effectiveDateMap = new HashMap<Long,Long>();
        Collections.sort(descriptionTuples, new TupleVersionComparator());
        Collections.reverse(descriptionTuples);
        I_DescriptionTuple firstChange = null;

        for (I_DescriptionTuple conceptAttributeTuple : descriptionTuples) {
            if(firstChange == null
                    || check.isDescriptionActive(firstChange.getPart().getStatusId()) != check.isDescriptionActive(conceptAttributeTuple.getPart().getStatusId())
                    || firstChange.getPart().getPathId() != conceptAttributeTuple.getPart().getPathId()
                    || ! getRf2DescriptionType(firstChange.getPart().getTypeId()).equals(getRf2DescriptionType(conceptAttributeTuple.getPart().getTypeId()))
                    || ! firstChange.getPart().getText().equals(conceptAttributeTuple.getPart().getText())
                    || firstChange.getPart().getInitialCaseSignificant() != conceptAttributeTuple.getPart().getInitialCaseSignificant()){
                firstChange = conceptAttributeTuple;
            }

            effectiveDateMap.put(ThinVersionHelper.convert(conceptAttributeTuple.getVersion()),
                    ThinVersionHelper.convert(firstChange.getVersion()));
        }

        return effectiveDateMap;

    }

    /**
     * Map the Ace versions to RF2 versions to account for the differences in RF1 and RF2 status
     *
     * @param relationshipTuples List of I_RelTuple
     * @param check StatusChecker
     * @return Map of Ace version dates to RF2 version dates
     * @throws Exception DB errors
     */
    public final Map<Long,Long> getRf2RelationshipEffectiveDateMap(final List<I_RelTuple> relationshipTuples) throws Exception {
        Map<Long,Long> effectiveDateMap = new HashMap<Long,Long>();
        Collections.sort(relationshipTuples, new TupleVersionComparator());
        Collections.reverse(relationshipTuples);
        I_RelTuple firstChange = null;

        for (I_RelTuple replationshipTuple : relationshipTuples) {
            if(firstChange == null
                    || check.isActive(firstChange.getPart().getStatusId()) != check.isActive(replationshipTuple.getPart().getStatusId())
                    || firstChange.getPart().getPathId() != replationshipTuple.getPart().getPathId()
                    || firstChange.getPart().getGroup() != replationshipTuple.getPart().getGroup()
                    || firstChange.getPart().getTypeId() != replationshipTuple.getPart().getTypeId()
                    || firstChange.getPart().getCharacteristicId() != replationshipTuple.getPart().getCharacteristicId()
                    ){
                firstChange = replationshipTuple;
            }

            effectiveDateMap.put(ThinVersionHelper.convert(replationshipTuple.getVersion()),
                    ThinVersionHelper.convert(firstChange.getVersion()));
        }

        return effectiveDateMap;

    }
}
