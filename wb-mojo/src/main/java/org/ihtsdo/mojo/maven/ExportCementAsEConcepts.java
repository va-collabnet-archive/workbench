package org.ihtsdo.mojo.maven;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.log.AceLog;
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
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.cid.TkRefsetCidMember;

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
    				if (RefsetAuxiliary.Concept.REFSET_PATHS.getUids().contains(eC.getPrimordialUuid())) {
    					// Add the workbench auxiliary path...
    					TkRefsetCidMember member = new TkRefsetCidMember();
    					member.primordialUuid = UUID.fromString("9353a710-a1c0-11df-981c-0800200c9a66");
    					member.componentUuid = ArchitectonicAuxiliary.Concept.PATH.getPrimoridalUid();
    					member.c1Uuid = ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getPrimoridalUid();
    					member.setRefsetUuid(eC.primordialUuid);
    					member.statusUuid = eC.conceptAttributes.statusUuid;
    					member.authorUuid = eC.conceptAttributes.authorUuid;
    					member.pathUuid = eC.conceptAttributes.pathUuid;
    					member.time = eC.conceptAttributes.time;
    					List<TkRefsetAbstractMember<?>> memberList = new ArrayList<TkRefsetAbstractMember<?>>();
    					memberList.add(member);
    					eC.setRefsetMembers(memberList);
    				}
    				if (ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getPrimoridalUid().equals(eC.getPrimordialUuid())) {
    			        addImage(eC, "1c4214ec-147a-11db-ac5d-0800200c9a66", "Semiotic Triangle with Circle",
    			        		".gif", "/Informatics-Circle-Small.gif", ArchitectonicAuxiliary.Concept.AUXILLARY_IMAGE.getPrimoridalUid());
    				} else if (RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getPrimoridalUid().equals(eC.getPrimordialUuid())) {
    			        addImage(eC, "70e86440-7f31-11dc-8314-0800200c9a66", "icon for included individual",
    			        		".png", "/16x16/plain/add.png", ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getPrimoridalUid());
    				} else if (RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getPrimoridalUid().equals(eC.getPrimordialUuid())) {
    			        addImage(eC, "70e86441-7f31-11dc-8314-0800200c9a66", "icon for included lineage",
    			        		".png", "/16x16/plain/add2.png", ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getPrimoridalUid());
    				} else if (RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getPrimoridalUid().equals(eC.getPrimordialUuid())) {
    			        addImage(eC, "70e86442-7f31-11dc-8314-0800200c9a66", "icon for excluded individual",
    			        		".png", "/16x16/plain/delete.png", ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getPrimoridalUid());
    				} else if (RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getPrimoridalUid().equals(eC.getPrimordialUuid())) {
    			        addImage(eC, "70e86443-7f31-11dc-8314-0800200c9a66", "icon for excluded lineage",
    			        		".png", "/16x16/plain/delete2.png", ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getPrimoridalUid());
    				} else if (RefsetAuxiliary.Concept.BOOLEAN_CHECK_CROSS_ICONS_FALSE.getPrimoridalUid().equals(eC.getPrimordialUuid())) {
    			        addImage(eC, "5b7f3f12-8034-11dc-8314-0800200c9a66", "icon for false",
    			        		".png", "/16x16/plain/navigate_cross.png", ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getPrimoridalUid());
    				} else if (RefsetAuxiliary.Concept.BOOLEAN_CHECK_CROSS_ICONS_TRUE.getPrimoridalUid().equals(eC.getPrimordialUuid())) {
    			        addImage(eC, "5b7f3f13-8034-11dc-8314-0800200c9a66", "icon for true",
    			        		".png", "/16x16/plain/navigate_check.png", ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getPrimoridalUid());
    				} else if (RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.getPrimoridalUid().equals(eC.getPrimordialUuid())) {
    			        addImage(eC, "5b7f3f14-8034-11dc-8314-0800200c9a66", "icon for false",
    			        		".png", "/16x16/plain/forbidden.png", ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getPrimoridalUid());
    				} else if (RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.getPrimoridalUid().equals(eC.getPrimordialUuid())) {
    			        addImage(eC, "5b7f3f15-8034-11dc-8314-0800200c9a66", "icon for true",
    			        		".png", "/16x16/plain/check.png", ArchitectonicAuxiliary.Concept.VIEWER_IMAGE.getPrimoridalUid());
    				}

    				eC.writeExternal(eConceptDOS);
    			}
    			eConceptDOS.close();

            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
    }

	private void addImage(EConcept eC, String primordialUuidStr,
			String textDescription, String format, String resource,
			UUID typeUuid) {
		URL imageURL = ExportCementAsEConcepts.class.getResource(resource);
		if (imageURL != null) {
			try {
				InputStream fis = imageURL.openStream();
				int size = (int) fis.available();

				byte[] image = new byte[size];
				int read = fis.read(image, 0, image.length);
				while (read != size) {
				    size = size - read;
				    read = fis.read(image, read, size);
				}
				TkMedia media = new TkMedia();
				
				
				media.primordialUuid = UUID.fromString(primordialUuidStr);
				media.conceptUuid = eC.conceptAttributes.primordialUuid;
				media.textDescription = textDescription;
				media.format = format;
				media.image = image;
				media.typeUuid = typeUuid;
				media.statusUuid = eC.conceptAttributes.statusUuid;
				media.authorUuid = eC.conceptAttributes.authorUuid;
				media.pathUuid = eC.conceptAttributes.pathUuid;
				media.time = eC.conceptAttributes.time;
				List<TkMedia> images = new ArrayList<TkMedia>();
				images.add(media);
				eC.setImages(images);
			} catch (Throwable e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		} else {
			AceLog.getAppLog().warning("Null url for: " + resource);
		}
		
		
	}
}