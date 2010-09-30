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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.util.TupleVersionPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.dto.ComponentDto;
import org.dwfa.dto.ExtensionDto;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.PROJECT;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;
import org.dwfa.vodb.types.ThinExtByRefVersioned;

/**
 * Contains the hierarchy to be exports and optionally not exported that are on the exportable positions.
 */
public class SnomedExportSpecification extends AbstractExportSpecification {

    /** The RF2 active concept. */
    private final I_GetConceptData rf2ActiveConcept;
    /** Retired concept */
    private final I_GetConceptData retiredConcept;
    /** Preferred desctiption type. */
    private final I_GetConceptData unspecifiedDescriptionType;
    /** Preferred desctiption type. */
    private final I_GetConceptData preferredDescriptionType;

    /** concept extension type. */
    private final int conceptExtensionNid;
    /** RF2 acceptable description type */
    private final int rf2AcceptableDescriptionTypeNid;
    /** RF2 preferred description type */
    private final int rf2PreferredDescriptionTypeNid;
    /** Ace Workbench inactive status. */
     private final int aceDuplicateStatusNId;
     private final int aceAmbiguousStatusNId;
     private final int aceErroneousStatusNId;
     private final int aceOutdatedStatusNId;
     private final int aceInappropriateStatusNId;
    /** RF2 inactive status. */
     private final     int duplicateStatusNId;
     private final int ambiguousStatusNId;
     private final int erroneousStatusNId;
     private final int outdatedStatusNId;
     private final int inappropriateStatusNId;
     private final int movedElsewhereStatusNId;
     private final int limitedStatusNId;
    /** release part for new content namely ADRS members */
    ThinConPart releasePart;
    /** Generate/update a language reference set */
    private boolean generateLangaugeRefset = true;
    private static final String EN_US = "en-US";
    private static final String EN = "en";
    private static final String EN_GB = "en-GB";
    private static final String EN_AU = "en-AU";

    /** Int set of fsn type. */
    private final I_IntSet fullySpecifiedDescriptionTypeIntSet = new IntSet();
    private final int adrsNid;
    /** Fully specified name description type. */
    protected final I_GetConceptData fullySpecifiedDescriptionType;
    /** Synonym description type. */
    private final I_GetConceptData synonymDescriptionType;

    public SnomedExportSpecification(List<Position> positions, List<I_GetConceptData> inclusions,
            List<I_GetConceptData> exclusions, NAMESPACE defaultNamespace, PROJECT defaultProject) throws Exception {
        super(positions, inclusions, exclusions, defaultNamespace, defaultProject);

        rf2ActiveConcept = termFactory.getConcept(ConceptConstants.ACTIVE_VALUE.localize().getNid());
        retiredConcept = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.RETIRED.localize().getUids().iterator().next());

        preferredDescriptionType = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getUids().iterator().next());
        unspecifiedDescriptionType = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE.localize().getUids().iterator().next());
        rf2AcceptableDescriptionTypeNid = termFactory.getConcept(
                org.dwfa.ace.refset.ConceptConstants.ACCEPTABLE.getUuids()).getNid();
        rf2PreferredDescriptionTypeNid = termFactory.getConcept(
                org.dwfa.ace.refset.ConceptConstants.PREFERRED.getUuids()).getNid();

        fullySpecifiedDescriptionType = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getUids().iterator().next());

        fullySpecifiedDescriptionTypeIntSet.add(fullySpecifiedDescriptionType.getConceptId());

        snomedIsATypeIntSet.add(termFactory.getConcept(org.dwfa.ace.refset.ConceptConstants.SNOMED_IS_A.getUuids()).
                getConceptId());
        currentActiveStatusIntSet.add(currentConcept.getNid());
        currentActiveStatusIntSet.add(activeConcept.getNid());


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

        this.defaultProject = defaultProject;

        extensionProcessor = new ExtensionProcessor<I_ThinExtByRefPart>();

        synonymDescriptionType = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getUids().iterator().next());


        this.updater = new SnomedComponentDtoUpdater(defaultNamespace, defaultProject, extensionProcessor);

        utility = new SnomedExportUtility();

        I_GetConceptData conceptRetired = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getUids().iterator().next());

        I_GetConceptData pendingMove = termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getUids().iterator().next());

        this.check = new SnomedStatusChecker(rf2ActiveConcept, activeConcept, currentConcept, pendingMove, conceptRetired, aceMovedElsewhereStatusNId);

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
            setComponentInactivationReferenceSet(componentDto.getConceptExtensionDtos(), concept.getNid(), matchingConceptTuples,
                    conceptInactivationIndicatorNid, TYPE.CONCEPT);

            setExtensionDto(componentDto.getConceptExtensionDtos(), concept.getConceptId(), TYPE.CONCEPT);

            Set<I_DescriptionTuple> latestDescriptionTuples = new HashSet<I_DescriptionTuple>();
            latestDescriptionTuples.addAll(TupleVersionPart.getLatestMatchingTuples(matchingDescriptionTuples));
            for (I_DescriptionTuple tuple : matchingDescriptionTuples) {
                setDescriptionDto(componentDto, tuple, latestDescriptionTuples.contains(tuple));
            }
//            for (I_DescriptionTuple latestDescription : latestDescriptionTuples) {
//                setComponentInactivationReferenceSet(componentDto.getDescriptionExtensionDtos(), latestDescription.
//                        getDescId(), latestDescription,
//                        descriptionInactivationIndicatorNid, TYPE.DESCRIPTION);
//            }

            if (generateLangaugeRefset) {
            	updateAdrsComponentDto(componentDto, matchingDescriptionTuples);
            }

            Set<I_RelTuple> latestRelationshipTuples = new HashSet<I_RelTuple>();
            latestRelationshipTuples.addAll(TupleVersionPart.getLatestMatchingTuples(matchingRelationshipTuples));
            for (I_RelTuple tuple : matchingRelationshipTuples) {
                setRelationshipDto(componentDto, tuple, latestRelationshipTuples.contains(tuple));
            }
//            for (I_RelTuple latestRelationship : latestRelationshipTuples) {
//                setComponentInactivationReferenceSet(componentDto.getRelationshipExtensionDtos(), latestRelationship.
//                        getRelId(), latestRelationship,
//                        relationshipInactivationIndicatorNid, TYPE.RELATIONSHIP);
//
//            }
        }
        return (!matchingConceptTuples.isEmpty()) ? componentDto : null;
    }

    /**
     * Update the ADRS members.
	 *
     * for each of the latest concept descriptions
     * get latest active descriptions for a preferred, synonyms and unspecifieds terms
     * If the extension is inactive, check the description is inactive
     * If the extension is active, check the description is active
     *
     * @param componentDto ComponentDto
     * @param conceptDescriptionTuples list of I_DescriptionTuple
     * @throws Exception
     */
    private void updateAdrsComponentDto(ComponentDto componentDto, Collection<I_DescriptionTuple> conceptDescriptionTuples) throws Exception {
        I_DescriptionTuple latestPreferredTerm = null;
        List<I_DescriptionTuple> latestSynonyms = new ArrayList<I_DescriptionTuple>();
        List<I_DescriptionTuple> latestUnSpecifiedDescriptionTypes = new ArrayList<I_DescriptionTuple>();
        Map<Integer,I_ThinExtByRefVersioned> extensionSet = new HashMap<Integer,I_ThinExtByRefVersioned>();

        for (I_DescriptionTuple currentDescription : TupleVersionPart.getLatestMatchingTuples(conceptDescriptionTuples)) {
            I_ThinExtByRefVersioned currentLanguageExtensions = getRefsetExtensionVersioned(adrsNid,currentDescription.getDescId());

            if (check.isDescriptionActive(currentDescription.getStatusId())) {
                if (currentDescription.getTypeId() == preferredDescriptionType.getNid()) {
                    latestPreferredTerm = getAdrsVersion(currentDescription, latestPreferredTerm, true, true);
                } else if (currentDescription.getTypeId() == synonymDescriptionType.getNid()) {
                    latestSynonyms = getAdrsVersion(currentDescription, latestSynonyms, false);
                } else if (currentDescription.getTypeId() == unspecifiedDescriptionType.getNid()) {
                    latestUnSpecifiedDescriptionTypes = getAdrsVersion(currentDescription, latestUnSpecifiedDescriptionTypes, false);
                }
            }

			if (currentLanguageExtensions != null) {
	            extensionSet.put(currentDescription.getDescId(), currentLanguageExtensions);
	            I_ThinExtByRefPartConcept latestPart = (I_ThinExtByRefPartConcept) TupleVersionPart.getLatestPart(currentLanguageExtensions.getVersions());
	            if ((check.isActive(latestPart.getStatusId()) != check.isDescriptionActive(currentDescription.getStatusId()))
	            		|| latestPart.getC1id() != getLangaugeType(currentDescription.getTypeId())) {
	            	int status = (check.isDescriptionActive(currentDescription.getStatusId())) ? activeConcept.getNid() : retiredConcept.getNid();
	            	I_ThinExtByRefPartConcept updatedPart = (I_ThinExtByRefPartConcept) latestPart.duplicate();
	                currentLanguageExtensions.addVersion(updatedPart);
	                updatedPart.setStatusId(status);
	                updatedPart.setC1id(getLangaugeType(currentDescription.getTypeId()));
	                updatedPart.setPathId(releasePart.getPathId());
	                updatedPart.setVersion(releasePart.getVersion());
	            }
	            componentDto.getDescriptionExtensionDtos().addAll(
                        extensionProcessor.processList(currentLanguageExtensions,
                        currentLanguageExtensions.getVersions(), TYPE.DESCRIPTION, true));
            }
        }

        //If update preferred term if new available
        if (latestPreferredTerm != null) {
        	I_ThinExtByRefVersioned currentLanguageExtensions = extensionSet.get(latestPreferredTerm.getDescId());
        	if(currentLanguageExtensions == null){
        		setAdrsExtension(componentDto, latestPreferredTerm, rf2PreferredDescriptionTypeNid);
        	}
        }
        //If no extension added a new extension for active Synonyms or UnSpecified descriptions
		List<I_DescriptionTuple> acceptableDescriptions = (!latestSynonyms.isEmpty()) ? latestSynonyms : latestUnSpecifiedDescriptionTypes;
        for (I_DescriptionTuple acceptableDescriptionTuple : acceptableDescriptions) {
        	I_ThinExtByRefVersioned currentLanguageExtensions = extensionSet.get(acceptableDescriptionTuple.getDescId());
        	if(currentLanguageExtensions == null){
        		setAdrsExtension(componentDto, acceptableDescriptionTuple, rf2AcceptableDescriptionTypeNid);
        	}
        }
    }

    /**
     * Get the RF2 language reference set value for the description type
     * @param descriptionStatus int
     * @return int
     */
    private int getLangaugeType(int descriptionStatus) {
    	int languageType = rf2AcceptableDescriptionTypeNid;

    	if (descriptionStatus == preferredDescriptionType.getNid()) {
    		languageType = rf2PreferredDescriptionTypeNid;
        }

    	return languageType;
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
            adrsVersioned = getThinExtByRefTuple(adrsNid, 0, descriptionTuple.getDescId(), desctriptionTypeNid,
                    releasePart);
        } else {
            I_ThinExtByRefPartConcept latestPart = (I_ThinExtByRefPartConcept) adrsVersioned.getLatestVersion();
            if (latestPart.getC1id() != desctriptionTypeNid) {
                I_ThinExtByRefPartConcept conceptExtension = new ThinExtByRefPartConcept();

                conceptExtension.setC1id(desctriptionTypeNid);
                conceptExtension.setPathId(releasePart.getStatusId());
                conceptExtension.setStatusId(releasePart.getStatusId());
                conceptExtension.setVersion(releasePart.getVersion());

                adrsVersioned.addVersion(conceptExtension);
            }
        }

        componentDto.getDescriptionExtensionDtos().addAll(
                extensionProcessor.processList(adrsVersioned, adrsVersioned.getVersions(), TYPE.DESCRIPTION, true));
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
    private List<I_DescriptionTuple> getAdrsVersion(I_DescriptionTuple currentTuple, List<I_DescriptionTuple> adrsTuples, boolean usAllowed) {
        I_DescriptionTuple adrsTuple = null;
        I_DescriptionTuple selectedAdrsTuple;

        if (!adrsTuples.isEmpty()) {
            adrsTuple = adrsTuples.get(0);
        }
        selectedAdrsTuple = getAdrsVersion(currentTuple, adrsTuple, usAllowed, false);

        if (adrsTuple == null && selectedAdrsTuple != null) {
            adrsTuples.add(selectedAdrsTuple);
        } else if (selectedAdrsTuple != null && selectedAdrsTuple.getDescId() != currentTuple.getDescId()
                && selectedAdrsTuple.getLang().equals(currentTuple.getLang())) {
            adrsTuples.add(currentTuple);
        } else if (selectedAdrsTuple != null
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
            boolean usAllowed, boolean getLatest) {
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
                    && getLatest && currentTuple.getVersion() > adrsTuple.getVersion()) {
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
                        if (part.getPathId() != tuple.getPathId()
                                || part.getVersion() != tuple.getVersion()) {
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
     * Adds a retired part to the versioned.
     *
     * @param thinExtByRefVersioned I_ThinExtByRefVersioned
     * @param retireForPart I_AmPart path and version to use for the retired part
     */
    private void retireLastestPart(I_ThinExtByRefVersioned thinExtByRefVersioned, I_AmPart retireForPart) {
        ThinExtByRefPartConcept latestPart = (ThinExtByRefPartConcept) TupleVersionPart.getLatestPart(thinExtByRefVersioned.
                getVersions());

        latestPart.setC1id(latestPart.getC1id());
        latestPart.setPathId(retireForPart.getPathId());
        latestPart.setStatusId(retiredConcept.getNid());
        latestPart.setVersion(retireForPart.getVersion());
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
     * Checks if the Status equals the Concept.ACTIVE or Current
     * or is a child of Concept.ACTIVE
     *
     * @param statusNid int
     * @return boolean true if the statusNid is active
     * @throws TerminologyException DB error
     * @throws IOException DB error
     */
//    @Override
//    protected boolean isActive(final int statusNid) throws IOException, TerminologyException {
//        boolean activate = false;
//        I_GetConceptData statusConcept = termFactory.getConcept(statusNid);
//
//        if (rf2ActiveConcept.isParentOf(statusConcept, null, null, null, false)) {
//            activate = true;
//        } else if (rf2ActiveConcept.getNid() == statusConcept.getNid()) {
//            activate = true;
//        } else if (activeConcept.getNid() == statusConcept.getNid()) {
//            activate = true;
//        } else if (currentConcept.getNid() == statusConcept.getNid()) {
//            activate = true;
//        }
//
//        return activate;
//    }


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
     * Is the concept an included hierarchy or a child element of.
     * @param concept I_GetConceptData
     *
     * @return boolean
     *
     * @throws IOException DB errors
     * @throws TerminologyException DB errors
     */
    @Override
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
    @Override
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
     * @param releasePart the releasePart to set
     */
    public final void setReleasePart(ThinConPart releasePart) {
        this.releasePart = releasePart;
    }
}
