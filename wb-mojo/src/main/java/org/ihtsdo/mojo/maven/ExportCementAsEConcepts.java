package org.ihtsdo.mojo.maven;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

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
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.dwfa.tapi.spec.TaxonomySpec;
import org.ihtsdo.etypes.EConcept;

/**
 * Export the specified CEMeNT (Common Enumerations and Metadata to Normalize
 * Terminology) taxonomies in eConcept format.
 * 
 * @goal export-cement-as-econcept
 * @phase process-resources
 */

public class ExportCementAsEConcepts extends AbstractMojo {

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
     * Specifies whether to create new files (default) or append to existing
     * files.
     * 
     * @parameter default-value=false
     * @required
     */
    private boolean append;

    public void execute() throws MojoExecutionException, MojoFailureException {

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
    			File eConceptsFile = new File(directory, "cement.jbin");
    			eConceptsFile.getParentFile().mkdirs();
    			BufferedOutputStream eConceptsBos = new BufferedOutputStream(
    					new FileOutputStream(eConceptsFile));
    			DataOutputStream eConceptDOS = new DataOutputStream(eConceptsBos);
    			for (I_ConceptualizeLocally localConcept: mts.getConcepts()) {
    				EConcept eC = new EConcept(localConcept, mts);
    				eC.writeExternal(eConceptDOS);
    			}
    			eConceptDOS.close();

            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
    }
}