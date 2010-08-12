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
package org.ihtsdo.mojo.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.DocumentAuxiliary;
import org.dwfa.cement.HL7;
import org.dwfa.cement.QueueType;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.dwfa.tapi.spec.TaxonomySpec;

/**
 * Export the specified CEMeNT (Common Enumerations and Metadata to Normalize
 * Terminology) taxonomies in
 * the ACE format (with branch ids and effective dates for standard
 * components--concepts, descriptions, relationships).
 * 
 * @goal export-cement-taxonomy
 * @phase process-resources
 */

public class ExportCementTaxonomyInAceFormat extends AbstractMojo {

    /**
     * An enumeration of the taxonomies that can be exported via this maven
     * plugin goal.
     * 
     * @author kec
     * 
     */
    public enum TAXONOMIES {
        /**
         * The standard taxonomy required by the ACE environment to normalize
         * descriptions, relationships, and other
         * types treated as enumerations by SNOMED, and implicit in many other
         * terminologies.
         */
        ACE_AUXILIARY,
        /**
         * A taxonomy for organizing sections of clinical documentation.
         */
        DOCUMENT_AUXILIARY,
        /**
         * A taxonomy that contains the HL7 entity name part concepts
         */
        HL7,
        /**
         * A taxonomy that contains queue types use by the workflow environemnt
         * during queue service discovery.
         */
        QUEUE_TYPE,
        /**
         * A taxonomy that provides the metadata necessary to implement refsets.
         */
        REFSET_AUXILIARY,
        /**
         * A taxonomy that provides the metadata necessary to implement workflow.
         */
        WORKFLOW_AUXILIARY
    };

    /**
     * Taxonomies to export. Must be one or more of the Taxonomies provided in
     * the <code>TAXONOMIES enum</code> in this class.
     * 
     * @parameter
     * @required
     */
    private String[] taxonomies;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the build directory.
     * 
     * @parameter
     */
    private String output;

    /**
     * Effective date for exported content - defaults to now if not set
     * 
     * @parameter
     */
    private Date effectiveDate;

    /**
     * The maven session
     * 
     * @parameter expression="${session}"
     * @required
     */
    private MavenSession session;

    /**
     * Specification of the concepts to exclude from this export
     * 
     * @parameter
     */
    private TaxonomySpec[] exclusions;

    /**
     * Only execute this mojo for one of the allowed goals.
     * This will prevent unexpected execution of plugins when
     * other goals are executed such as eclipse:eclipse or
     * site:site.
     * 
     * There may be better ways to do this... If you find one,
     * please let us know :-)
     */

    private String[] allowedGoals = new String[] { "install", "deploy" };

    /**
     * Specifies whether to create new files (default) or append to existing
     * files.
     * 
     * @parameter default-value=false
     * @required
     */
    private boolean append;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (MojoUtil.allowedGoal(getLog(), session.getGoals(), allowedGoals)) {
            getLog().info("Exporting cement taxonomies");
            try {
                String prefix;
                if (output == null) {
                    prefix = outputDirectory.getCanonicalPath() + File.separatorChar + "generated-resources"
                        + File.separatorChar + "cement" + File.separatorChar;
                } else {
                    output = output.replace('/', File.separatorChar);
                    prefix = output + File.separator;
                }
                MemoryTermServer mts = new MemoryTermServer();
                if (effectiveDate != null) {
                    mts.setEffectiveDate(effectiveDate);
                }
                LocalFixedTerminology.setStore(mts);
                mts.setGenerateIds(true);
                for (String tstr : taxonomies) {
                    TAXONOMIES t = TAXONOMIES.valueOf(tstr);
                    switch (t) {
                    case ACE_AUXILIARY:
                        ArchitectonicAuxiliary aa = new ArchitectonicAuxiliary();
                        aa.addToMemoryTermServer(mts);
                        break;
                    case DOCUMENT_AUXILIARY:
                        DocumentAuxiliary da = new DocumentAuxiliary();
                        da.addToMemoryTermServer(mts);
                        break;
                    case HL7:
                        HL7 hl7 = new HL7();
                        hl7.addToMemoryTermServer(mts);
                        break;
                    case QUEUE_TYPE:
                        QueueType qt = new QueueType();
                        qt.addToMemoryTermServer(mts);
                        break;
                    case REFSET_AUXILIARY:
                        RefsetAuxiliary ra = new RefsetAuxiliary();
                        ra.addToMemoryTermServer(mts);
                        break;

                    default:
                        throw new Exception("Don't know how to handle taxonomy: " + t);
                    }
                }

                File directory = new File(prefix);
                directory.mkdirs();
                File conceptFile = new File(directory, "concepts.txt");
                File descFile = new File(directory, "descriptions.txt");
                File relFile = new File(directory, "relationships.txt");
                File rootsFile = new File(directory, "roots.txt");
                File extTypeFile = new File(directory, "extensions.txt");
                File altIdFile = new File(directory, "alt_ids.txt");

                mts.setGenerateIds(false);
                mts.setExclusions(exclusions);

                Writer altIdWriter = createWriter(altIdFile);

                Writer conceptWriter = createWriter(conceptFile);
                mts.writeConcepts(conceptWriter, altIdWriter, MemoryTermServer.FILE_FORMAT.ACE);
                conceptWriter.close();

                Writer descWriter = createWriter(descFile);
                mts.writeDescriptions(descWriter, altIdWriter, MemoryTermServer.FILE_FORMAT.ACE);
                descWriter.close();

                Writer relWriter = createWriter(relFile);
                mts.writeRelationships(relWriter, altIdWriter, MemoryTermServer.FILE_FORMAT.ACE);
                relWriter.close();

                Writer rootsWriter = createWriter(rootsFile);
                mts.writeRoots(rootsWriter, MemoryTermServer.FILE_FORMAT.ACE);
                rootsWriter.close();

                Writer extensionTypeWriter = createWriter(extTypeFile);
                mts.writeExtensionTypes(extensionTypeWriter, altIdWriter, MemoryTermServer.FILE_FORMAT.ACE);
                extensionTypeWriter.close();

                I_ConceptualizeLocally[] descTypeOrder = new I_ConceptualizeLocally[] { mts.getConcept(mts.getNid(ArchitectonicAuxiliary.Concept.EXTENSION_TABLE.getUids())) };
                List<I_ConceptualizeLocally> descTypePriorityList = Arrays.asList(descTypeOrder);

                for (I_ConceptualizeLocally extensionType : mts.getExtensionTypes()) {
                    I_DescribeConceptLocally typeDesc = extensionType.getDescription(descTypePriorityList);
                    File extensionFile = new File(directory, typeDesc.getText() + ".txt");
                    Writer extensionWriter = createWriter(extensionFile);
                    mts.writeExtension(extensionType, extensionWriter, altIdWriter, MemoryTermServer.FILE_FORMAT.ACE);
                    extensionWriter.close();
                }
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    private Writer createWriter(final File conceptFile) throws IOException {
        return new FileWriter(conceptFile, append);
    }
}
