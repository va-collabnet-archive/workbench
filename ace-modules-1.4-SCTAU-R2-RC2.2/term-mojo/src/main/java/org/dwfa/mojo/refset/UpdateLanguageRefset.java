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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.util.TupleVersionPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.file.AceIdentifierWriter;
import org.dwfa.mojo.refset.writers.MemberRefsetHandler;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.AceDateFormat;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;

/**
 * Get all the descriptions for the concepts and create a description extension
 * for the latest active preferred term and either a synonym or a unspecified
 * description that meets the Order of language type preference is en_AU, en_GB,
 * en then en_US.
 *
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
     * Retired status.
     */
    private int retiredNId;

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
        retiredNId = org.dwfa.cement.ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
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

        Collection<I_ThinExtByRefTuple> currentRefsetExtensions = TupleVersionPart.getLatestMatchingTuples( getAllActiveOrCurrentRefsetExtensions(refsetId, descriptionNid));

        if (currentRefsetExtensions.isEmpty()) {
            // stunt extension part.
            I_ThinExtByRefPartConcept part = new ThinExtByRefPartConcept();
            part.setC1id(extensionTypeId);
            part.setPathId(exportPath.getConceptId());
            part.setStatusId(activeNId);
            part.setVersion(ThinVersionHelper.convert(AceDateFormat.getRf2DateFormat().parse(releaseVersion).getTime()));
            export(part, null, refsetId, descriptionNid, TYPE.DESCRIPTION);
        } else {
            for (I_ThinExtByRefTuple currentRefsetExtension : currentRefsetExtensions) {
                I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) currentRefsetExtension.getPart();
                int currentRefsetExtensionType = part.getC1id();
                if (currentRefsetExtensionType == extensionTypeId) {
                    export(part, null, refsetId, descriptionNid, TYPE.DESCRIPTION);
                }
            }
        }
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
    @SuppressWarnings("unchecked")
    public <T extends I_ThinExtByRefTuple> List<T> getAllActiveOrCurrentRefsetExtensions(int refsetId, int conceptId)
            throws Exception {

        ArrayList<T> result = new ArrayList<T>();

        for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(conceptId, true)) {
            if (extension.getRefsetId() == refsetId) {

                // get the latest version
                I_ThinExtByRefTuple latestTuple = null;
                for (I_ThinExtByRefTuple tuple : extension.getTuples(null, null, false, true)) {
                    if ((latestTuple == null) || (tuple.getVersion() >= latestTuple.getVersion())) {
                        latestTuple = tuple;
                    }
                }

                // confirm its the right extension value and its status is
                // current
                if (latestTuple.getStatusId() == activeNId || latestTuple.getStatusId() == currentNId) {
                    result.add((T) latestTuple);
                }
            }
        }

        return result;
    }

    /**
     * Go over all concepts and add a language extension for both the latest
     * synonym and preferred term.
     *
     * If there is no en-GB preferred term then the latest unspecified
     * description is used.
     */
    class ConceptIterator implements I_ProcessConcepts {
        private static final String EN_US = "en-US";
        private static final String EN = "en";
        private static final String EN_GB = "en-GB";
        private static final String EN_AU = "en-AU";
        int processedLineCount = 0;

        /**
         * Get all the descriptions for the concepts and create a description
         * extension for the latest active preferred term and either a synonym
         * or a un specified description that meets the Order of language type
         * preference is en_AU, en_GB, en then en_US.
         *
         * @param I_GetConceptData concept
         * @throws Exception reading the database.
         */
        @Override
        public void processConcept(I_GetConceptData concept) throws Exception {
            List<I_DescriptionVersioned> descriptions;
            I_DescriptionTuple latestPreferredTerm;
            I_DescriptionTuple latestSynonym;
            I_DescriptionTuple unSpecifiedDescriptionType;

            for (ExportSpecification spec : exportSpecifications) {
                if (spec.test(concept) && isConceptAllowedStatus(concept)) {
                    descriptions = concept.getDescriptions();
                    latestPreferredTerm = null;
                    latestSynonym = null;
                    unSpecifiedDescriptionType = null;
                    Collection<I_DescriptionTuple> latestTuples;
                    for (I_DescriptionVersioned descriptionVersioned : descriptions) {
                        latestTuples = TupleVersionPart.getLatestMatchingTuples(descriptionVersioned.getTuples());

                        for (I_DescriptionTuple latest : latestTuples) {
                            if (latest.getStatusId() == activeNId || latest.getStatusId() == currentNId) {
                                if (latest.getTypeId() == prefferredTermNid) {
                                    latestPreferredTerm = getAdrsVersion(latest, latestPreferredTerm);
                                } else if (latest.getTypeId() == synonymNid) {
                                    latestSynonym = getAdrsVersion(latest, latestSynonym);
                                } else if (latest.getTypeId() == unSpecifiedDescriptionTypeNid) {
                                    unSpecifiedDescriptionType = getAdrsVersion(latest, unSpecifiedDescriptionType);
                                }
                            } else {
                                Collection<I_ThinExtByRefTuple> currentLanguageExtensions = getAllActiveOrCurrentRefsetExtensions(referencesetConcept.getConceptId(), latest.getDescId());
                                for (I_ThinExtByRefTuple currentLanguageExtension : currentLanguageExtensions) {
                                    retireOldExtension(latest, descriptionVersioned, (I_ThinExtByRefPartConcept) currentLanguageExtension.getPart());
                                }
                            }

                        }
                    }

                    if (latestPreferredTerm != null) {
                        exportResetExtentions(latestPreferredTerm.getDescId(), preferredDescriptionTypeNid);
                    }

                    if (latestSynonym != null) {
                        exportResetExtentions(latestSynonym.getDescId(), acceptableDescriptionTypeNid);
                    } else if (unSpecifiedDescriptionType != null) {
                        exportResetExtentions(unSpecifiedDescriptionType.getDescId(), acceptableDescriptionTypeNid);
                    }
                }
            }

            processedLineCount++;
            if (processedLineCount % 1000 == 0) {
                logger.info("Processed " + processedLineCount);
            }
        }

        /**
         * Adds a retired member row to the refset file
         *
         * @param latest I_DescriptionPart
         * @param descriptionVersioned I_DescriptionPart
         * @param currentLanguageExtension I_ThinExtByRefPartConcept
         * @throws Exception
         */
        private void retireOldExtension(I_DescriptionTuple latest, I_DescriptionVersioned descriptionVersioned,
                I_ThinExtByRefPartConcept currentLanguageExtension) throws Exception {
            I_ThinExtByRefPartConcept part = new ThinExtByRefPartConcept();
            part.setC1id(currentLanguageExtension.getC1id());
            part.setPathId(exportPath.getConceptId());
            part.setStatusId(retiredNId);
            part.setVersion(ThinVersionHelper.convert(AceDateFormat.getRf2DateFormat().parse(releaseVersion).getTime()));
            export(part, null, referencesetConcept.getConceptId(), descriptionVersioned.getDescId(), TYPE.DESCRIPTION);
        }

        /**
         * Gets the Language I_DescriptionVersioned to use for the refset.
         *
         * Order of language type preference is en_AU, en_GB, en then en_US.
         *
         * @param descriptionVersion I_DescriptionTuple
         * @param currentAdrsVersion I_DescriptionTuple can be null
         * @return I_DescriptionVersioned
         */
        private I_DescriptionTuple getAdrsVersion(I_DescriptionTuple currentTuple, I_DescriptionTuple adrsTuple) {
            if (adrsTuple != null) {
                if(currentTuple.getLang().equals(EN_AU)) {
                    adrsTuple = currentTuple;
                } else if (currentTuple.getLang().equals(EN_GB)
                        && adrsTuple.getLang().equals(EN_AU)) {
                    adrsTuple = currentTuple;
                } else if (currentTuple.getLang().equals(EN)
                        && adrsTuple.getLang().equals(EN_GB)
                        && adrsTuple.getLang().equals(EN_AU)) {
                    adrsTuple = currentTuple;
                } else if (currentTuple.getLang().equals(EN_US)
                        && adrsTuple.getLang().equals(EN)
                        && adrsTuple.getLang().equals(EN_GB)
                        && adrsTuple.getLang().equals(EN_AU)) {
                    adrsTuple = currentTuple;
                }
            } else {
                adrsTuple = currentTuple;
            }

            return adrsTuple;
        }
    }
}
