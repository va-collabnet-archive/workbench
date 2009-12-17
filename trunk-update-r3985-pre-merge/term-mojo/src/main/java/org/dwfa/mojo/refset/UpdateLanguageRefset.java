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
package org.dwfa.mojo.refset;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.MemberRefsetHelper;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.file.AceIdentifierWriter;
import org.dwfa.mojo.refset.writers.MemberRefsetHandler;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;

/**
 * 
 * @author ean
 * @goal update-language-refset
 */
public class UpdateLanguageRefset extends ReferenceSetExport {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Refset to create lanaguage extensions for.
     * 
     * @parameter
     * @required
     */
    private String referencesetUuidStr;

    /**
     * Path to create the extensions on
     * 
     * @parameter
     * @required
     */
    private String exportPathUuidStr;

    /**
     * Create/updated reset extensions.
     */
    private MemberRefsetHelper memberRefsetHelper;

    /**
     * Refset to add the members to.
     */
    private I_GetConceptData referencesetConcept;

    /**
     * Latest version of the refset.
     */
    private I_ConceptAttributePart referencesetConceptLatestVersion;

    /**
     * Path to create the extensions on
     */
    private I_Path exportPath;

    /**
     * Acceptable (foundation metadata concept)
     */
    private int acceptableDescriptionTypeNid;

    /**
     * Preferred (foundation metatdata concept)
     */
    private int preferredDescriptionTypeNid;

    /**
     * prefferred term native id.
     */
    private int prefferredTermNid;

    /**
     * synonym native id
     */
    private int synonymNid;

    /**
     * synonym native id
     */
    private int unSpecifiedDescriptionTypeNid;

    /**
     * A factory of terms.
     */
    private I_TermFactory termFactory = LocalVersionedTerminology.get();

    /**
     * Using the exportSpecifications create a language reference set.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        logger.info("Start");

        sctidRefsetOutputDirectory.mkdirs();
        uuidRefsetOutputDirectory.mkdirs();
        if (useRF2) {
            sctidStructuralRefsetOutputDirectory.mkdirs();
            aceIdentifierFile.getParentFile().mkdirs();
        }

        if (useRF2 && aceIdentifierFile == null || aceStructuralIdentifierFile == null) {
            throw new MojoExecutionException(
                "Cannot proceed, RF2 requires a aceIdentifierFile and aceStructuralIdentifierFile");
        }
        if (useRF2 && aceIdentifierFile != null && aceStructuralIdentifierFile != null) {
            try {
                aceIdentifierWriter = new AceIdentifierWriter(aceIdentifierFile, true);
                aceStructuralIdentifierWriter = new AceIdentifierWriter(aceStructuralIdentifierFile, true);
            } catch (IOException e) {
                throw new MojoExecutionException("cannot open id file", e);
            }
        }

        try {
            MemberRefsetHandler.setFixedMapDirectory(fixedMapDirectory);
            MemberRefsetHandler.setReadWriteMapDirectory(readWriteMapDirectory);
            MemberRefsetHandler.setPathReleaseDateConfig(pathReleaseDateConfig);

            if (rf2Descriptor != null && rf2Descriptor.getModule() != null) {
                MemberRefsetHandler.setModule(rf2Descriptor.getModule());
            }

            allowedStatuses = tf.newIntSet();
            for (ExportSpecification spec : exportSpecifications) {
                for (ConceptDescriptor status : spec.getStatusValuesForExport()) {
                    allowedStatuses.add(status.getVerifiedConcept().getConceptId());
                }
            }

            initVariables();

            termFactory.iterateConcepts(new ConceptIterator());

            for (BufferedWriter writer : writerMap.values()) {
                writer.close();
            }
            if (useRF2 && aceStructuralIdentifierFile != null && aceIdentifierFile != null) {
                aceStructuralIdentifierWriter.close();
                aceIdentifierWriter.close();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Processing error: ", e);
        }

        logger.info("Finish");
    }

    /**
     * Initialise the mojo variables.
     * 
     * @throws TerminologyException DB error
     * @throws IOException DB error
     * @throws Exception DB error
     */
    private void initVariables() throws TerminologyException, IOException, Exception {
        referencesetConcept = termFactory.getConcept(UUID.fromString(referencesetUuidStr));
        referencesetConceptLatestVersion = getLatestVersion(referencesetConcept);
        exportPath = termFactory.getPath(UUID.fromString(exportPathUuidStr));
        prefferredTermNid = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids())
            .getConceptId();
        synonymNid = termFactory.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids())
            .getConceptId();
        unSpecifiedDescriptionTypeNid = termFactory.getConcept(
            ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE.getUids()).getConceptId();
        activeNId = org.dwfa.cement.ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
        currentNId = org.dwfa.cement.ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
        memberRefsetHelper = new MemberRefsetHelper(referencesetConcept.getConceptId(),
            RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid());
        acceptableDescriptionTypeNid = termFactory.getConcept(ConceptConstants.ACCEPTABLE.getUuids()).getConceptId();
        preferredDescriptionTypeNid = termFactory.getConcept(ConceptConstants.PREFERRED.getUuids()).getConceptId();
    }

    /**
     * Gets the latest version.
     * 
     * @param concept I_GetConceptData
     * @return I_ConceptAttributePart
     * @throws IOException database error
     */
    private I_ConceptAttributePart getLatestVersion(I_GetConceptData concept) throws IOException {
        I_ConceptAttributePart latest = null;
        for (I_ConceptAttributePart curVersion : concept.getConceptAttributes().getVersions()) {
            if (latest == null || latest.getVersion() < curVersion.getVersion()) {
                latest = curVersion;
            }
        }

        return latest;
    }

    /**
     * Is the concept status one of the allowed status
     * 
     * @param concept I_GetConceptData
     * @return boolean true if concept status in an allowed status
     * @throws IOException database error
     * @throws TerminologyException database error
     */
    private boolean isConceptAllowedStatus(I_GetConceptData concept) throws IOException, TerminologyException {
        boolean isAllowed = false;

        for (int allowedStatusNid : allowedStatuses.getSetValues()) {
            I_ConceptAttributePart latest = getLatestAttributePart(concept);
            if (latest != null && latest.getStatusId() == allowedStatusNid) {
                isAllowed = true;
                break;
            }
        }

        return isAllowed;
    }

    /**
     * Updates the concept extension for the refset.
     * 
     * If the concept extension exists for the refset and description
     * this will be retired if the extension type is different to the language
     * subset type and a new extension is created.
     * 
     * @param monitor used to inform the user of any errors or warnings.
     * @param refsetId int the reset to update the extension with
     * @param conceptDescription LanguageSubsetMemberLine the current line in
     *            the subset file
     * 
     * @throws IOException file read errors
     * @throws TerminologyException looking up concepts etc.
     * @throws Exception MemberRefsetHelper errors
     */
    private void exportResetExtentions(int descriptionNid, int extensionTypeId) throws IOException,
            TerminologyException, Exception {
        // check if a current extension exists
        int refsetId = referencesetConcept.getConceptId();

        I_ThinExtByRefPartConcept currentRefsetExtension = memberRefsetHelper.getFirstCurrentRefsetExtension(refsetId,
            descriptionNid);
        if (currentRefsetExtension != null) {
            int currentRefsetExtensionType = currentRefsetExtension.getC1id();
            if (currentRefsetExtensionType == extensionTypeId) {
                export(currentRefsetExtension, null, refsetId, currentRefsetExtension.getC1id(), TYPE.DESCRIPTION);
            }
        } else {
            I_ThinExtByRefPartConcept part = new ThinExtByRefPartConcept();// stunt
                                                                           // extension
                                                                           // part.
            part.setC1id(extensionTypeId);
            part.setPathId(exportPath.getConceptId());
            part.setStatusId(activeNId);
            part.setVersion(referencesetConceptLatestVersion.getVersion());
            export(part, null, refsetId, descriptionNid, TYPE.DESCRIPTION);

        }
    }

    /**
     * Go over all concepts and add a language extension for both the latest
     * synonym and preferred term.
     * 
     * If there is no en en-GB preferred term then the latest unspecified
     * description is used.
     * 
     * Excludes en-US descriptions.
     */
    class ConceptIterator implements I_ProcessConcepts {
        int processedLineCount = 0;

        @Override
        public void processConcept(I_GetConceptData concept) throws Exception {
            List<I_DescriptionVersioned> descriptions;
            I_DescriptionVersioned latestPreferredTerm;
            I_DescriptionVersioned latestSynonym;
            I_DescriptionVersioned unSpecifiedDescriptionType;

            for (ExportSpecification spec : exportSpecifications) {
                if (spec.test(concept) && isConceptAllowedStatus(concept)) {
                    descriptions = concept.getDescriptions();
                    latestPreferredTerm = null;
                    latestSynonym = null;
                    unSpecifiedDescriptionType = null;
                    I_DescriptionPart latest;
                    for (I_DescriptionVersioned descriptionVersioned : descriptions) {
                        latest = getLatest(descriptionVersioned);
                        if (latest.getStatusId() == currentNId
                            && (latest.getLang().equals("en-GB") || latest.getLang().equals("en"))) {
                            if (latest.getTypeId() == prefferredTermNid) {
                                latestPreferredTerm = descriptionVersioned;
                            } else if (latest.getTypeId() == synonymNid) {
                                latestSynonym = descriptionVersioned;
                            } else if (latest.getTypeId() == unSpecifiedDescriptionTypeNid) {
                                unSpecifiedDescriptionType = descriptionVersioned;
                            }
                        }
                    }
                    // Mimic UK subset, use the latest en/GB unspecified
                    // description if no en/GB preferred term
                    if (latestPreferredTerm != null) {
                        exportResetExtentions(latestPreferredTerm.getDescId(), preferredDescriptionTypeNid);
                    } else if (unSpecifiedDescriptionType != null) {
                        exportResetExtentions(unSpecifiedDescriptionType.getDescId(), preferredDescriptionTypeNid);
                    }

                    if (latestSynonym != null) {
                        exportResetExtentions(latestSynonym.getDescId(), acceptableDescriptionTypeNid);
                    }
                }
            }

            processedLineCount++;
            if (processedLineCount % 1000 == 0) {
                logger.info("Processed " + processedLineCount);
            }
        }

        private I_DescriptionPart getLatest(I_DescriptionVersioned descriptionVersioned) {
            I_DescriptionPart latestDescriptionPart = null;

            for (I_DescriptionPart descriptionPart : descriptionVersioned.getVersions()) {
                if (latestDescriptionPart == null || latestDescriptionPart.getVersion() < descriptionPart.getVersion()) {
                    latestDescriptionPart = descriptionPart;
                }
            }

            return latestDescriptionPart;
        }

        /**
         * Is this the latest version of the description type.
         * 
         * @param typeNid int
         * @param currentLatest I_DescriptionTuple
         * @param description I_DescriptionTuple
         * @return true if the description is the latest.
         */
        boolean isLatest(int typeNid, I_DescriptionPart currentLatest, I_DescriptionPart description) {
            boolean latest = false;

            if (typeNid == description.getTypeId()
                && (currentLatest == null || description.getVersion() > currentLatest.getVersion())) {
                latest = true;
            }

            return latest;
        }
    }
}
