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
package org.dwfa.mojo.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.ConceptDto;
import org.dwfa.dto.ExtensionDto;
import org.dwfa.dto.RelationshipDto;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.mojo.export.AbstractExportSpecification.ExtensionProcessor;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefPartString;
import org.dwfa.vodb.types.ThinExtByRefVersioned;

/**
 *
 * @author Matthew Edwards
 */
public final class SnomedComponentDtoUpdater extends AbstractComponentDtoUpdater {

    /** CTV3 reference set. */
    private final I_GetConceptData ctv3IdMapExtension;
    /** SNOMED RT Id concept. */
    private final I_GetConceptData snomedRtId;
    /** CTV3 Id concept. */
    private final I_GetConceptData ctv3Id;
    /** History relationship types to History relationship reference set map. */
    private final Map<Integer, Integer> historyStatusRefsetMap = new HashMap<Integer, Integer>();
    /** relationship refinablility reference set */
    private final int relationshipRefinabilityExtensionNid;
    /** Snomed Rt Id reference set. */
    private final I_GetConceptData snomedRtIdMapExtension;
    /** Component extension processor. */
    private final ExtensionProcessor<I_ThinExtByRefPart> extensionProcessor;
    /** concept extension type. */
    private final int conceptExtensionNid;
    /** concept string extension type. */
    private final int stringExtensionNid;

    public SnomedComponentDtoUpdater(NAMESPACE defaultNamespace, PROJECT defaultProject, ExtensionProcessor<I_ThinExtByRefPart> extensionProcessor) throws Exception {
        super(defaultNamespace, defaultProject);

        this.ctv3IdMapExtension = termFactory.getConcept(ConceptConstants.CTV3_ID_MAP_EXTENSION.localize().getNid());

        this.snomedRtId = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.localize().getUids().iterator().next());
        this.ctv3Id = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CTV3_ID.localize().getUids().iterator().next());

        //Initialise History Refset Status Map.
        this.historyStatusRefsetMap.put(ConceptConstants.MOVED_FROM_HISTORY.localize().getNid(),
                ConceptConstants.MOVED_FROM_HISTORY_REFSET.localize().getNid());
        this.historyStatusRefsetMap.put(ConceptConstants.MOVED_TO_HISTORY.localize().getNid(),
                ConceptConstants.MOVED_TO_HISTORY_REFSET.localize().getNid());
        this.historyStatusRefsetMap.put(ConceptConstants.REPLACED_BY_HISTORY.localize().getNid(),
                ConceptConstants.REPLACED_BY_HISTORY_REFSET.localize().getNid());
        this.historyStatusRefsetMap.put(ConceptConstants.SAME_AS_HISTORY.localize().getNid(),
                ConceptConstants.SAME_AS_HISTORY_REFSET.localize().getNid());
        this.historyStatusRefsetMap.put(ConceptConstants.WAS_A_HISTORY.localize().getNid(),
                ConceptConstants.WAS_A_HISTORY_REFSET.localize().getNid());

        this.relationshipRefinabilityExtensionNid = ConceptConstants.RELATIONSHIP_REFINABILITY_EXTENSION.localize().
                getNid();

        this.snomedRtIdMapExtension = termFactory.getConcept(ConceptConstants.SNOMED_ID_MAP_EXTENSION.localize().getNid());

        this.extensionProcessor = extensionProcessor;

        this.conceptExtensionNid = RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid();

        this.stringExtensionNid = RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid();

        I_GetConceptData rf2ActiveConcept = termFactory.getConcept(ConceptConstants.ACTIVE_VALUE.localize().getNid());

        I_GetConceptData conceptRetired = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getUids().iterator().next());

        I_GetConceptData pendingMove = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getUids().iterator().next());

        int aceMovedElsewhereStatusNId = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();

        this.check = new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);
    }

    @Override
    public ComponentDto updateComponentDto(ComponentDto componentDto, I_ConceptAttributeTuple tuple, Collection<I_DescriptionTuple> descriptionTuples, boolean latest) throws Exception {
        ConceptDto conceptDto = new ConceptDto();
        I_GetConceptData conceptData = termFactory.getConcept(tuple.getConId());
        conceptDto.setConceptId(getIdMap(tuple, tuple.getConId()));

        getBaseConceptDto(conceptDto, tuple, conceptData.getId().getVersions(), latest);

        if (latest) {
            I_IdPart ctv3IdPart = getLatesIdtVersion(conceptData.getId().getVersions(), ctv3Id.getConceptId(), tuple);
            if (ctv3IdPart != null) {
                conceptDto.setCtv3Id(ctv3IdPart.getSourceId().toString());
                setCtv3ReferenceSet(componentDto, tuple, ctv3IdPart, latest);
            } else {
                conceptDto.setCtv3Id("");
            }

            I_IdPart snomedIdPart = getLatesIdtVersion(conceptData.getId().getVersions(), snomedRtId.getConceptId(), tuple);
            if (snomedIdPart != null) {
                conceptDto.setSnomedId(snomedIdPart.getSourceId().toString());
                setSnomedRtIdReferenceSet(componentDto, tuple, snomedIdPart, latest);
            } else {
                conceptDto.setSnomedId("");
            }
        }

        addUuidSctIdIndentifierToConceptDto(conceptDto, tuple, conceptData.getId().getVersions(), TYPE.CONCEPT, tuple.
                getConId(), latest);

        conceptDto.setFullySpecifiedName(getFsn(descriptionTuples));

        conceptDto.setPrimative(!tuple.isDefined());
        conceptDto.setDefinitionStatusUuid(getDefinitionStatusUuid(tuple.isDefined()));
        conceptDto.setType(TYPE.CONCEPT);

        componentDto.getConceptDtos().add(conceptDto);

        return componentDto;
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
        I_ThinExtByRefVersioned ctv3Versioned = getThinExtByRefTuple(ctv3IdMapExtension.getConceptId(), 0,
                tuple.getConId(), ctv3IdPart, ctv3IdPart.getSourceId().toString());

        List<I_ThinExtByRefPart> partsToRemove = new ArrayList<I_ThinExtByRefPart>();
        for (I_ThinExtByRefPart part : ctv3Versioned.getVersions()) {
            if (part.getPathId() != ctv3IdPart.getPathId()
                    || part.getVersion() != ctv3IdPart.getVersion()) {
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
            if (part.getPathId() != snomedIdPart.getPathId()
                    || part.getVersion() != snomedIdPart.getVersion()) {
                partsToRemove.add(part);
            }
        }
        snomedIdVersioned.getVersions().removeAll(partsToRemove);

        componentDto.getConceptExtensionDtos().addAll(
                extensionProcessor.processList(snomedIdVersioned, snomedIdVersioned.getVersions(), TYPE.CONCEPT, false));
    }

    @Override
    public void updateComponentDto(ComponentDto componentDto, I_RelTuple tuple, boolean latest) throws Exception, TerminologyException {
        RelationshipDto relationshipDto = new RelationshipDto();
        List<I_IdPart> idParts = termFactory.getId(tuple.getRelId()).getVersions();

        getBaseConceptDto(relationshipDto, tuple, idParts, latest);

        if (latest) {
            //TODO not implementing the concept history refsets yet
            //setConceptHistory(componentDto, tuple.getRelVersioned(), latest);
            setRelationshipRefinabilityReferenceSet(componentDto.getRelationshipExtensionDtos(), tuple, latest);
        }

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
                I_ThinExtByRefVersioned extensionVersioned = getThinExtByRefTuple(historyStatusRefsetMap.get(versionPart.
                        getTypeId()), 0,
                        versionedRel.getC1Id(), versionedRel.getC2Id(), versionPart);

                List<I_ThinExtByRefPart> partsToRemove = new ArrayList<I_ThinExtByRefPart>();
                for (I_ThinExtByRefPart part : extensionVersioned.getVersions()) {
                    if (part.getPathId() != versionPart.getPathId()
                            || part.getVersion() != versionPart.getVersion()) {
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
            if (part.getPathId() != relationshipTuple.getPathId()
                    || part.getVersion() != relationshipTuple.getVersion()) {
                partsToRemove.add(part);
            }
        }
        extensionVersioned.getVersions().removeAll(partsToRemove);

        extensionDtos.addAll(extensionProcessor.processList(extensionVersioned, extensionVersioned.getVersions(),
                TYPE.RELATIONSHIP, false));
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
                if (stringParts.getPathId() == amPart.getPathId()
                        && stringParts.getVersion() == amPart.getVersion()) {
                    conceptExtension = (I_ThinExtByRefPartString) stringParts;
                    break;
                }
            }
        }

        if (conceptExtension == null) {
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
                if (conceptParts.getPathId() == amPart.getPathId()
                        && conceptParts.getVersion() == amPart.getVersion()) {
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
    private I_ThinExtByRefVersioned getRefsetExtensionVersioned(int refsetId, int conceptId)
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
}
