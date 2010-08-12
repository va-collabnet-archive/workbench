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
package org.dwfa.mojo.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
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
import org.dwfa.ace.util.TupleVersionPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.ExtensionDto;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.ThinIdPart;

/**
 *
 * @author matt
 */
public abstract class AbstractExportSpecification implements ExportSpecification {

    /**Terminology Factory.*/
    protected static final I_TermFactory termFactory = LocalVersionedTerminology.get();
    /** Class logger. */
    protected final Logger logger = Logger.getLogger(this.getClass().getName());
    /** Export positions */
    protected final List<Position> positions;
    /** Included hierarchy */
    protected final List<I_GetConceptData> inclusions;
    /** Excluded hierarchy - within one or more included hierarchy */
    protected final List<I_GetConceptData> exclusions;
    /** The default name-space to use for export */
    protected final NAMESPACE defaultNamespace;
    /** The active concept. */
    protected final I_GetConceptData activeConcept;
    /** primitive INT set of FSN type. */
    protected final I_IntSet snomedIsATypeIntSet = new IntSet();
    /** primitive INT set of FSN type. */
    protected final I_IntSet currentActiveStatusIntSet = new IntSet();
 
    /** inactivation indicators. */
    protected final int descriptionInactivationIndicatorNid;
    protected final int relationshipInactivationIndicatorNid;
    protected final int conceptInactivationIndicatorNid;
    /** ADRS TODO need to change this to be configurable */
    protected final List<Integer> structuralRefsets = new ArrayList<Integer>();
    /** UUID Source concept. */
    private final I_GetConceptData unspecifiedUuid;
    protected PROJECT defaultProject;
    /** Component extension processor. */
    protected ExtensionProcessor<I_ThinExtByRefPart> extensionProcessor;
    protected final int aceLimitedStatusNId;
    protected final int aceMovedElsewhereStatusNId;
    protected final boolean fullExport = false;
    /** The active concept. */
    protected final I_GetConceptData currentConcept;
    /** The in active concept. */
    protected final I_GetConceptData inActiveConcept;
    
    protected AbstractComponentDtoUpdater updater;

    protected DatabaseExportUtility utility;

     protected StatusChecker check;

    public AbstractExportSpecification(final List<Position> positions, final List<I_GetConceptData> inclusions,
            final List<I_GetConceptData> exclusions, final NAMESPACE defaultNamespace, final PROJECT defaultProject)
            throws Exception {

        this.positions = Collections.unmodifiableList(positions);
        this.inclusions = inclusions;
        this.exclusions = exclusions;
        this.defaultNamespace = defaultNamespace;
        this.defaultProject = defaultProject;
        this.aceLimitedStatusNId = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
        this.aceMovedElsewhereStatusNId = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();

        activeConcept = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.ACTIVE.localize().getUids().iterator().next());

       

       this.currentConcept = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.CURRENT.localize().getUids().iterator().next());

        currentActiveStatusIntSet.add(currentConcept.getNid());
        currentActiveStatusIntSet.add(activeConcept.getNid());

        descriptionInactivationIndicatorNid = ConceptConstants.DESCRIPTION_INACTIVATION_INDICATOR.localize().getNid();
        relationshipInactivationIndicatorNid = ConceptConstants.RELATIONSHIP_INACTIVATION_INDICATOR.localize().getNid();
        conceptInactivationIndicatorNid = ConceptConstants.CONCEPT_INACTIVATION_INDICATOR.localize().getNid();
       
        unspecifiedUuid = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getUids().iterator().next());

        extensionProcessor = new ExtensionProcessor<I_ThinExtByRefPart>();

        inActiveConcept = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.INACTIVE.localize().getUids().iterator().next());
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

    /**
     * Is the concept an included hierarchy or a child element of.
     * @param concept I_GetConceptData
     *
     * @return boolean
     *
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    protected boolean isIncluded(I_GetConceptData concept) throws IOException, TerminologyException {
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
    protected boolean isExcluded(I_GetConceptData concept) throws IOException, TerminologyException {
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
     * Extension processing interface.
     *
     * @param <T> extends I_ThinExtByRefPart
     */
    protected interface I_AmExtensionProcessor<T extends I_ThinExtByRefPart> {

        /**
         * Create a ExtensionDto for the reference set member version
         *
         * @param thinExtByRefVersioned I_ThinExtByRefVersioned
         * @param tuple <T>
         * @param type TYPE
         * @return ExtensionDto
         * @throws Exception ye'old DB errors
         */
        ExtensionDto getExtensionDto(I_ThinExtByRefVersioned thinExtByRefVersioned, T tuple, TYPE type, boolean latest) throws Exception;
    }

    /**
     * Processes each of the extension types that extend I_ThinExtByRefPart.
     *
     * @param <T> the extension types
     */
    protected class ExtensionProcessor<T extends I_ThinExtByRefPart> {

        /** Map of extension handlers for each extension type. */
        protected Map<Integer, I_AmExtensionProcessor<T>> extensionMap =
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

                boolean isActiveConcept = check.isActive(TupleVersionPart
                        .getLatestPart(refsetConcept.getConceptAttributeTuples(null, null, false, false)).getStatusId());

                if (isExportableConcept(refsetConcept) && isActiveConcept) {
                    I_AmExtensionProcessor<T> extensionProcessor = extensionMap.get(thinExtByRefVersioned.getTypeId());
                    if (extensionProcessor != null) {
                        ExtensionDto extensionDto = extensionProcessor.getExtensionDto(thinExtByRefVersioned, (T) t,
                                type, t.getVersion() == latestPart.getVersion());
                        extensionDto.setIsClinical(isClinical);
                        extensionDtos.add(extensionDto);
                    } else {
                        final String message = "No extension processor for %1$s '%2$s'";
                        logger.severe(String.format(message, "refset",
                                termFactory.getConcept(thinExtByRefVersioned.getRefsetId()).getInitialText()));
                        logger.severe(String.format(message, "concept",
                                termFactory.getConcept(thinExtByRefVersioned.getComponentId()).getInitialText()));
                        logger.severe(String.format(message, "type",
                                termFactory.getConcept(thinExtByRefVersioned.getTypeId()).getInitialText()));
                    }
                }
            }
            return extensionDtos;
        }
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
    protected void setConceptDto(ComponentDto componentDto, I_ConceptAttributeTuple tuple, boolean latest)
            throws Exception, IOException, TerminologyException {
        updater.updateComponentDto(componentDto, tuple, latest);
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
    protected void setDescriptionDto(ComponentDto componentDto, I_DescriptionTuple tuple, boolean latest) throws Exception, IOException,
            TerminologyException {
        updater.updateComponentDto(componentDto, tuple, latest);
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
    protected void setRelationshipDto(ComponentDto componentDto, I_RelTuple tuple, boolean latest) throws TerminologyException,
            IOException, Exception {
        I_GetConceptData destinationConcept = termFactory.getConcept(tuple.getC2Id());
        I_GetConceptData relationshipType = termFactory.getConcept(tuple.getTypeId());

        if (isExportableConcept(destinationConcept) && isExportableConcept(relationshipType)) {
            updater.updateComponentDto(componentDto, tuple, latest);
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
    protected void setExtensionDto(List<ExtensionDto> extensionList, int nid, TYPE type) throws Exception {
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
    protected boolean isExportableConcept(final I_GetConceptData concept)
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
     * Extension processor for  I_ThinExtByRefPartConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConcept
     */
    protected class BaseExtensionProcessor<T extends I_ThinExtByRefPart> implements
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

            if (thinExtByRefVersioned.getMemberId() != 0) {
                idParts = termFactory.getId(thinExtByRefVersioned.getMemberId()).getVersions();
            } else {
                idParts = new ArrayList<I_IdPart>(1);
                idParts.add(getIdUuidSctIdPart(thinExtByRefVersioned, tuple));
            }

            updater.getBaseConceptDto(extensionDto, tuple, idParts, latest);

            updater.addUuidSctIdIndentifierToConceptDto(extensionDto, tuple, idParts, TYPE.REFSET, thinExtByRefVersioned.
                    getMemberId(), latest);

            extensionDto.setConceptId(updater.getIdMap(tuple, thinExtByRefVersioned.getRefsetId()));

            extensionDto.setReferencedConceptId(updater.getIdMap(tuple, thinExtByRefVersioned.getComponentId()));

            extensionDto.setMemberId(getUuid(thinExtByRefVersioned));
            extensionDto.setNamespace(updater.getNamespace(idParts, tuple));
            extensionDto.setType(type);
            extensionDto.setFullySpecifiedName(termFactory.getConcept(thinExtByRefVersioned.getRefsetId()).
                    getInitialText());

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
        protected I_IdPart getIdUuidSctIdPart(I_ThinExtByRefVersioned thinExtByRefVersioned,
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
        protected UUID getUuid(I_ThinExtByRefVersioned thinExtByRefVersioned)
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
    protected class ConceptExtensionProcessor<T extends I_ThinExtByRefPartConcept> implements
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

            extensionDto.setConcept1Id(updater.getIdMap(tuple, tuple.getC1id()));

            return extensionDto;
        }
    }

    /**
     * Extension processor for  I_ThinExtByRefPartConceptConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConceptConcept
     */
    protected class ConceptStringExtensionProcessor<T extends I_ThinExtByRefPartConceptString> implements
            I_AmExtensionProcessor<I_ThinExtByRefPartConceptString> {

        ConceptExtensionProcessor<T> conceptExtensionProcessor = new ConceptExtensionProcessor<T>();

        /**
         * @see org.dwfa.mojo.export.ExportSpecification.I_AmExtensionProcessor#
         * getExtensionDto(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned, org.dwfa.ace.api.ebr.I_ThinExtByRefPart, org.dwfa.maven.transform.SctIdGenerator.TYPE)
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
    protected class StringExtensionProcessor<T extends I_ThinExtByRefPartString> implements
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
    protected class ConceptIntegerExtensionProcessor<T extends I_ThinExtByRefPartConceptInt> implements
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
    protected class ConceptConceptExtensionProcessor<T extends I_ThinExtByRefPartConceptConcept> implements
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

            extensionDto.setConcept2Id(updater.getIdMap(tuple, tuple.getC2id()));

            return extensionDto;
        }
    }

    /**
     * Extension processor for  I_ThinExtByRefPartConceptConcept.
     *
     * @param <T>  extends I_ThinExtByRefPartConceptConcept
     */
    protected class ConceptConceptStringExtensionProcessor<T extends I_ThinExtByRefPartConceptConceptString> implements
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
    protected class ConceptConceptConceptExtensionProcessor<T extends I_ThinExtByRefPartConceptConceptConcept> implements
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
            extensionDto.setConcept3Id(updater.getIdMap(tuple, tuple.getC3id()));
            return extensionDto;
        }
    }
}
