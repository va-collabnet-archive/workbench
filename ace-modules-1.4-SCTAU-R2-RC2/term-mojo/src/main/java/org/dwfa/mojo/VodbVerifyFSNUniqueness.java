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
package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.DescriptionHasNoVersionsException;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Goal which ensures latest FSNs are unique for the given VODB.
 *
 * @goal vodb-verify-fsn-uniqueness
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbVerifyFSNUniqueness extends AbstractMojo {

    /**
     * The output file location.
     *
     * @parameter expression="${project.build.directory}/reports"
     */
    private File outputDirectory;

    /**
     * The output file name.
     *
     * @parameter
     */
    private String outputFileName = "release_fsn_uniqueness_report.txt";

    /**
     * Whether to continue on, if a non-unique FSN is found.
     * Default is to not continue - the build will fail.
     *
     * @parameter
     */
    private boolean failBuildOnException = false;

    /**
     * Statuses to consider when checking FSNs
     *
     * @parameter
     */
    private ConceptDescriptor[] statuses;

    public void execute() throws MojoExecutionException, MojoFailureException {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {
            FindUniqueFSNs find = new FindUniqueFSNs();
            termFactory.iterateConcepts(find);
            find.getWriter().flush();
            find.getWriter().close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private class FindUniqueFSNs implements I_ProcessConcepts {

        I_TermFactory termFactory;
        BufferedWriter outputWriter;
        HashSet<String> uniqueFsns;
        List<UUID> statusUuids;
        UUID fsnUuid;
        boolean shouldCheckStatus;

        public FindUniqueFSNs() throws Exception {
            outputDirectory.mkdirs();
            outputWriter = new BufferedWriter(new BufferedWriter(new FileWriter(getReportFile())));
            termFactory = LocalVersionedTerminology.get();
            uniqueFsns = new HashSet<String>();
            statusUuids = new ArrayList<UUID>();
            fsnUuid = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids().iterator().next();

            // Only check the status if there is more than one status in the
            // list
            shouldCheckStatus = statuses.length > 0;
            getLog().info("shouldCheckStatus: " + shouldCheckStatus);

            for (ConceptDescriptor status : statuses) {
                statusUuids.add(UUID.fromString(status.getUuid()));
            }
        }

        public void processConcept(final I_GetConceptData concept) throws Exception {
            for (I_DescriptionVersioned descriptionVersioned : concept.getDescriptions()) {
                processDescription(concept, descriptionVersioned);
            }
        }

        private String getReportFile() {
            return outputDirectory + File.separator
                    + outputFileName;
        }

        private void processDescription(final I_GetConceptData concept, final I_DescriptionVersioned description)
                throws Exception {

            I_DescriptionTuple latestDescription = null;

            try {
                // catches invalid data with 0 description versions. Once the
                // data is stablized remove this try/catch.
                latestDescription = description.getLastTuple();
            } catch (DescriptionHasNoVersionsException e) {
                getLog().warn("Skipping description with 0 versions -> " + description + ", for concept: " + concept);
                return;
            }

            //get type
            UUID currentDescriptionTypeUuid = getConceptType(latestDescription.getTypeId());

            // get status
            UUID currentDescriptionStatusUuid = getConceptType(latestDescription.getStatusId());

            boolean isFSN = currentDescriptionTypeUuid.equals(fsnUuid);
            boolean isSpecifiedStatus = false;

            if (shouldCheckStatus) {
                isSpecifiedStatus = statusUuids.contains(currentDescriptionStatusUuid);
            }

            if (isValidFSNAndStatusIgnored(isFSN) || isValidStatusAndFSN(isFSN, isSpecifiedStatus)) {

                String descriptionText = latestDescription.getText();

                if (isDuplicateFSN(descriptionText)) {
                    if (logOnErrors()) {
                        getLog().warn("Duplicate FSN: " + descriptionText +
                                "\nSee " +  getReportFile() + "for details.");
                        outputWriter.write("Duplicate FSN: " + descriptionText);
                        outputWriter.newLine();
                    } else {
                        throw new MojoFailureException("Duplicate FSN: " + descriptionText);
                    }
                } else {
                    uniqueFsns.add(descriptionText);
                }
            }

        }

        private UUID getConceptType(int partId) throws Exception {
            return termFactory.getConcept(partId).getUids().iterator().next();
        }

        private boolean isValidFSNAndStatusIgnored(final boolean isFSN) {
            return (!shouldCheckStatus && isFSN);
        }

        private boolean isValidStatusAndFSN(final boolean isFSN, final boolean isSpecifiedStatus) {
            return shouldCheckStatus && isSpecifiedStatus && isFSN;
        }

        private boolean logOnErrors() {
            return !failBuildOnException;
        }

        private boolean isDuplicateFSN(final String descriptionText) {
            return uniqueFsns.contains(descriptionText);
        }

        public BufferedWriter getWriter() {
            return outputWriter;
        }

        public void setWriter(BufferedWriter outputWriter) {
            this.outputWriter = outputWriter;
        }
    }

}
