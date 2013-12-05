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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.PrimordialId;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.TermAux;

/**
 * Goal which finds all concepts that have no parents.
 *
 * @goal vodb-find-multi-ids
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbFindMultiIds extends AbstractMojo {

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
    private final String outputHtmlMultiUuidFileName = "report_multi_uuid.html";

    /**
     * The html output file name.
     *
     * @parameter
     */
    private final String outputHtmlMultiSctidFileName = "report_multi_sctid.html";

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
    private final String outputTextMultiUuidFileName = "multi_uuid.txt";

    /**
     * The text file containing uuids file name.
     *
     * @parameter
     */
    private final String outputTextMultiSctidFileName = "multi_uuid.txt";

    private final int nidSnomedIsa;
    private final int nidStatusActive;
    private final int nidUnspecifiedUuid;
    private final int nidSctid;

    /**
     *
     * @throws org.apache.maven.plugin.MojoFailureException
     */
    public VodbFindMultiIds() throws MojoFailureException {
        try {
            nidSctid = TermAux.SCT_ID_AUTHORITY.getLenient().getNid();
            nidUnspecifiedUuid = PrimordialId.ACE_AUX_ENCODING_ID.getNativeId();
            nidStatusActive = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid();
            nidSnomedIsa = Snomed.IS_A.getLenient().getNid();
        } catch (IOException ex) {
            throw new MojoFailureException("critical SNOMED nid not found", ex);
        }
    }

    private class FindComponents implements I_ProcessConcepts {

        Set<PositionBI> origins;
        BufferedWriter textMultiUuidWriter;
        BufferedWriter htmlMultiUuidWriter;
        BufferedWriter textMultiSctidWriter;
        BufferedWriter htmlMultiSctidWriter;

        public FindComponents() throws IOException {
            outputHtmlDirectory.mkdirs();
            outputTextDirectory.mkdirs();
            textMultiUuidWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputTextDirectory + File.separator
                    + outputTextMultiUuidFileName)));
            htmlMultiUuidWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputHtmlDirectory + File.separator
                    + outputHtmlMultiUuidFileName)));
            textMultiSctidWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputTextDirectory + File.separator
                    + outputTextMultiSctidFileName)));
            htmlMultiSctidWriter = new BufferedWriter(new BufferedWriter(new FileWriter(outputHtmlDirectory + File.separator
                    + outputHtmlMultiSctidFileName)));
            origins = new HashSet<>();
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

            Collection<? extends IdBI> allIds = concept.getAllIds();
            ArrayList<IdBI> uuidList = new ArrayList<>();
            ArrayList<IdBI> sctidList = new ArrayList<>();
            for (IdBI id : allIds) {
                if (id.getAuthorityNid() == nidUnspecifiedUuid) {
                    uuidList.add(id);
                } else if (id.getAuthorityNid() == nidSctid) {
                    sctidList.add(id);
                }
            }

            if (uuidList.size() > 1) {
                String message = "Found multi uuid concept: " + concept.getPrimUuid().toString() + "  " + concept.toUserString();
                getLog().info(message);
                htmlMultiUuidWriter.append(message);
                htmlMultiUuidWriter.append("<br>");
                textMultiUuidWriter.append(concept.getUids().toString());
                textMultiUuidWriter.newLine();
            }
            if (sctidList.size() > 1) {
                String message = "Found multi sctid concept: " + concept.getPrimUuid().toString() + "  " + concept.toUserString();
                getLog().info(message);
                htmlMultiSctidWriter.append(message);
                htmlMultiSctidWriter.append("<br>");
                textMultiSctidWriter.append(concept.getUids().toString());
                textMultiSctidWriter.newLine();
            }
        }

        public BufferedWriter getHtmlMultiUuidWriter() {
            return htmlMultiUuidWriter;
        }

        public void setHtmlMultiUuidWriter(BufferedWriter htmlWriter) {
            this.htmlMultiUuidWriter = htmlWriter;
        }

        public BufferedWriter getTextMultiUuidWriter() {
            return textMultiUuidWriter;
        }

        public void setTextMultiUuidWriter(BufferedWriter textWriter) {
            this.textMultiUuidWriter = textWriter;
        }
        
        public BufferedWriter getHtmlMultiSctidWriter() {
            return htmlMultiSctidWriter;
        }

        public void setHtmlMultiSctidWriter(BufferedWriter htmlWriter) {
            this.htmlMultiSctidWriter = htmlWriter;
        }

        public BufferedWriter getTextMultiSctidWriter() {
            return textMultiSctidWriter;
        }

        public void setTextMultiSctidWriter(BufferedWriter textWriter) {
            this.textMultiSctidWriter = textWriter;
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        I_TermFactory tf = Terms.get();
        try {
            FindComponents find = new FindComponents();
            tf.iterateConcepts(find);
            find.getTextMultiUuidWriter().close();
            find.getHtmlMultiUuidWriter().close();
            find.getTextMultiSctidWriter().close();
            find.getHtmlMultiSctidWriter().close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
