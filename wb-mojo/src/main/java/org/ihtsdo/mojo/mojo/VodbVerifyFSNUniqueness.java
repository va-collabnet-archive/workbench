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
package org.ihtsdo.mojo.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_ProcessDescriptions;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;

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
     * Any exceptions to the uniqueness test.
     * 
     * @parameter
     */
    private ArrayList<String> exceptions = new ArrayList<String>();

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

    private class FindUniqueFSNs implements I_ProcessDescriptions {

        I_TermFactory termFactory;
        BufferedWriter outputWriter;
        HashSet<String> uniqueFsns;
        List<UUID> statusUuids;
        UUID fsnUuid;
        boolean isCheckStatus;

        public FindUniqueFSNs() throws Exception {
            outputDirectory.mkdirs();
            outputWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputDirectory + File.separator
                + outputFileName)));
            termFactory = LocalVersionedTerminology.get();
            uniqueFsns = new HashSet<String>();
            statusUuids = new ArrayList<UUID>();
            fsnUuid = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids().iterator().next();

            // Only check the status if there is more than one status in the
            // list
            isCheckStatus = statuses.length > 0;
            getLog().info("isCheckStatus: " + isCheckStatus);

            for (ConceptDescriptor status : statuses) {
                statusUuids.add(UUID.fromString(status.getUuid()));
            }
        }

        public void processDescription(I_DescriptionVersioned description) throws Exception {

            I_DescriptionTuple latestDescription = description.getLastTuple();

            // check if it's a FSN
            UUID currentDescriptionTypeUuid = termFactory.getConcept(latestDescription.getTypeId())
                .getUids()
                .iterator()
                .next();

            // check if it's an active status
            UUID currentDescriptionStatusUuid = termFactory.getConcept(latestDescription.getStatusId())
                .getUids()
                .iterator()
                .next();

            // Check the Uuids are equal
            boolean isUuidsEqual = currentDescriptionTypeUuid.equals(fsnUuid);

            // Is the status defined in the list of statuses to check?
            boolean isStatusListed = false;

            if (isCheckStatus) {
                isStatusListed = statusUuids.contains(currentDescriptionStatusUuid);
            }

            if ((!isCheckStatus && isUuidsEqual) || (isCheckStatus && isStatusListed && isUuidsEqual)) {

                String descriptionText = latestDescription.getText();

                if (uniqueFsns.contains(descriptionText)) {
                    if (exceptions.contains(descriptionText)) {
                        // don't report as ie t's an exception
                    } else {
                        if (!failBuildOnException) {
                            getLog().info("Duplicate FSN: " + descriptionText);
                            outputWriter.write("Duplicate FSN: " + descriptionText);
                            outputWriter.newLine();
                        } else {
                            throw new MojoFailureException("Duplicate FSN: " + descriptionText);
                        }
                    }
                } else {
                    uniqueFsns.add(descriptionText);
                }
            }

        }

        public BufferedWriter getWriter() {
            return outputWriter;
        }

        public void setWriter(BufferedWriter outputWriter) {
            this.outputWriter = outputWriter;
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {
            FindUniqueFSNs find = new FindUniqueFSNs();
            termFactory.iterateDescriptions(find);
            find.getWriter().flush();
            find.getWriter().close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
