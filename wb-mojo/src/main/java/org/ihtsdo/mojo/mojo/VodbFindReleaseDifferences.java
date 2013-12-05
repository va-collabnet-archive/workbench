/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.mojo.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * Goal which finds all concepts that have no parents.
 *
 * @goal vodb-find-release-differences
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbFindReleaseDifferences extends AbstractMojo {

    /**
     * The html output file location.
     *
     * @parameter expression="${project.build.directory}/classes"
     */
    private File outputHtmlDirectory;

    /**
     * The html output file name.
     *
     * @parameter
     */
    private final String outputHtmlFileName = "report_dropped.html";

    /**
     * The text file output location.
     *
     * @parameter expression="${project.build.directory}/classes"
     */
    private File outputTextDirectory;

    /**
     * The text file containing uuids file name.
     *
     * @parameter
     */
    private final String outputTextFileName = "report_dropped.txt";

    /**
     * The time for the test in yyyy.mm.dd hh:mm:ss format
     *
     * @parameter
     */
    private final String dateTimeOldStr = "2012.01.31 00:00:00";

    /**
     * description type uuid
     *
     * @parameter
     */
    private final String extensionDescTypeUuid = "ecfd4324-04de-5503-8274-3116f8f07217";
    private final long nidExtensionDescType;

    private final long timeOld;

    private final int nidSnomedIsa;
    private final int nidStatusActive;

    private final int nidSnomedCorePath;

    private I_TermFactory tf;

    /**
     *
     * @throws org.apache.maven.plugin.MojoFailureException
     */
    public VodbFindReleaseDifferences() throws MojoFailureException {
        try {
            DateFormat df = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
            timeOld = df.parse(dateTimeOldStr).getTime();

            nidStatusActive = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
            nidSnomedIsa = Snomed.IS_A.getLenient().getNid();

            tf = Terms.get();
            nidExtensionDescType = tf.getConcept(UUID.fromString(extensionDescTypeUuid)).getConceptNid();

            UUID snomedPathUuid = UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2");
            nidSnomedCorePath = tf.uuidToNative(snomedPathUuid);

            /* concept of interest
             Mechanical complication due to cystostomy catheter 
             87656001

             SNOMED Core
             8c230474-9f11-30ce-9cad-185a96fd03a2
             */
            /* one approach would be to check for IS_A Special Concept ...
             Inactive concept
             f267fc6f-7c4d-3a79-9f17-88b82b42709a

             Ambiguous concept
             5adbed85-55d8-3304-a404-4bebab660fff

             Duplicate concept
             a5db42d4-6d94-33b7-92e7-d4a1d0f0d814

             Erroneous concept
             d4227098-db7a-331e-8f00-9d1e27626fc5

             Limited status concept
             0c7b717a-3e41-372b-be57-621befb9b3ee

             Moved elsewhere
             e730d11f-e155-3482-a423-9637db3bc1a2

             Outdated concept
             d8a42cc5-05dd-3fcf-a1f7-62856e38874a

             Reason not stated concept
             a0db7e17-c6b2-3acc-811d-8a523274e869

             Special concept
             65cc653d-94b0-32f1-b2bf-4166d980e7cc
             */
        } catch (IOException ex) {
            throw new MojoFailureException("critical SNOMED nid not found", ex);
        } catch (ParseException ex) {
            throw new MojoFailureException("date time conversion failed", ex);
        } catch (TerminologyException ex) {
            throw new MojoFailureException("terminology error", ex);
        }
    }

    private class FindComponents implements I_ProcessConcepts {

        I_TermFactory termFactory;
        Set<PositionBI> origins;
        BufferedWriter textWriter;
        BufferedWriter htmlWriter;

        public FindComponents() throws IOException {
            outputHtmlDirectory.mkdirs();
            outputTextDirectory.mkdirs();
            textWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputTextDirectory + File.separator
                    + outputTextFileName)));
            htmlWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputHtmlDirectory + File.separator
                    + outputHtmlFileName)));
            origins = new HashSet<>();
            termFactory = Terms.get();
        }

        @Override
        public void processConcept(I_GetConceptData concept) throws Exception {
            // Is this a SNOMED concept?
            boolean isSnomedConcept = false;
            Collection<? extends RelationshipChronicleBI> relsIncoming = concept.getRelationshipsIncoming();
            for (RelationshipChronicleBI rel : relsIncoming) {
                Collection<? extends RelationshipVersionBI> relVersions = rel.getVersions();
                long time = Long.MIN_VALUE;
                RelationshipVersionBI mostRecentVersion = null;
                for (RelationshipVersionBI rv : relVersions) {
                    if (rv.getStatusNid() == nidStatusActive && rv.getTime() > time) {
                        time = rv.getTime();
                        mostRecentVersion = rv;
                    }
                }
                if (mostRecentVersion != null
                        && mostRecentVersion.getStatusNid() == nidStatusActive
                        && mostRecentVersion.getTypeNid() == nidSnomedIsa) {
                    isSnomedConcept = true;
                    break;
                }
            }

            if (!isSnomedConcept) {
                return;
            }

            // 
            ConceptAttributeChronicleBI ca = concept.getConceptAttributes();
            Collection<? extends ConceptAttributeVersionBI> cavs = ca.getVersions();
            if (cavs == null) {
                return;
            }
            long recentTimeOld = Long.MIN_VALUE;
            long recentTimeNew = Long.MIN_VALUE;
            ConceptAttributeVersionBI cavbiOld = null;
            ConceptAttributeVersionBI cavbiNew = null;
            for (ConceptAttributeVersionBI cavbi : cavs) {
                if (cavbi.getTime() > recentTimeOld
                        && cavbi.getTime() <= timeOld
                        && cavbi.getPathNid() == nidSnomedCorePath) {
                    recentTimeOld = cavbi.getTime();
                    cavbiOld = cavbi;
                }
                if (cavbi.getTime() > recentTimeNew
                        && cavbi.getPathNid() == nidSnomedCorePath) {
                    recentTimeNew = cavbi.getTime();
                    cavbiNew = cavbi;
                }
            }

            boolean droppedCase = false;
            if (cavbiOld == null && cavbiNew == null) {
                return; // skip concepts without any release in Snomed Core
            }

            if (cavbiOld == null && cavbiNew != null) { // added
                return; // not looking for added values at this time.
            }

            if (cavbiOld != null && cavbiNew == null) { // dropped, not present
                droppedCase = true;
            }

            if (cavbiOld != null && cavbiNew != null) { // dropped, retired
                if (cavbiOld.getStatusNid() == nidStatusActive
                        && cavbiNew.getStatusNid() != nidStatusActive) {
                    droppedCase = true;
                }
            }

            boolean activeExtensionFound = false;
            if (droppedCase) { // Is there an active extension desription type?
                Collection<? extends DescriptionChronicleBI> d = concept.getDescriptions();
                for (DescriptionChronicleBI dcbi : d) {
                    Collection<? extends DescriptionVersionBI> versions = dcbi.getVersions();
                    // find most recent version
                    long recentTime = Long.MIN_VALUE;
                    DescriptionVersionBI recentDV = null;
                    for (DescriptionVersionBI dv : versions) {
                        if (dv.getTime() > recentTime) {
                            recentTime = dv.getTime();
                            recentDV = dv;
                        }
                    }
                    if (recentDV != null
                            && recentDV.getTypeNid() == nidExtensionDescType
                            && recentDV.getStatusNid() == nidStatusActive) {
                        activeExtensionFound = true;
                    }
                }
            }

            if (activeExtensionFound) {
                String message = "Found dropped concept with extension: " + concept.getPrimUuid().toString() + "  " + concept.toUserString();
                getLog().info(message);
                htmlWriter.append(message);
                htmlWriter.append("<br>");
                textWriter.append(concept.getUids().toString());
                textWriter.newLine();
            }
        }

        public BufferedWriter getHtmlWriter() {
            return htmlWriter;
        }

        public void setHtmlWriter(BufferedWriter htmlWriter) {
            this.htmlWriter = htmlWriter;
        }

        public BufferedWriter getTextWriter() {
            return textWriter;
        }

        public void setTextWriter(BufferedWriter textWriter) {
            this.textWriter = textWriter;
        }

    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            FindComponents find = new FindComponents();
            tf.iterateConcepts(find);
            find.getTextWriter().close();
            find.getHtmlWriter().close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
