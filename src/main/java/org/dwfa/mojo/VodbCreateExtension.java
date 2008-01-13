package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import java.util.UUID;

/**
 * Creates an extension. To be used in testing VodbCalculateMemberSet class.
 * @author Christine Hill
 *
 */

/**
 *
 * @goal vodb-create-extension
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbCreateExtension extends AbstractMojo {

    /**
     * @parameter
     * @required
     * The concept descriptor for the ref set spec.
     */
    private ConceptDescriptor refSetSpecDescriptor;

    /**
     * @parameter
     * @required
     * The concept descriptor for the member set path.
     */
    private ConceptDescriptor refSetPathDescriptor;

    /**
     * @parameter
     * @required
     * The ID of the reference set of which we wish to calculate the member set.
     */
    private ConceptDescriptor refSetTypeDescriptor;

    private I_Path memberSetPath;

    public void execute() throws MojoExecutionException, MojoFailureException {

        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {


            I_GetConceptData refSetPathConcept = refSetPathDescriptor.getVerifiedConcept();
            memberSetPath = termFactory.getPath(refSetPathConcept.getUids());

            // execute calculate member set plugin
            ExtensionCreator extensionCreator =
                new ExtensionCreator(refSetSpecDescriptor,
                        refSetTypeDescriptor);

            // iterate over each concept
            termFactory.iterateConcepts(extensionCreator);

            String message = "Number of new extensions: "
                            + extensionCreator.getExtensionCount();
            getLog().info(message);

            System.out.println("Uncommitted end: " + termFactory.getUncommitted().size());
            termFactory.commit();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private class ExtensionCreator implements I_ProcessConcepts {
        private I_TermFactory termFactory;

        private int extensionCount;

        private int referenceSetId;

        private int typeId;

        public ExtensionCreator(ConceptDescriptor referenceSetDescriptor,
                ConceptDescriptor typeConceptDescriptor)
                throws Exception {
            termFactory = LocalVersionedTerminology.get();
            extensionCount = 0;
            I_GetConceptData refConcept = referenceSetDescriptor.getVerifiedConcept();
            this.referenceSetId = refConcept.getConceptId();
            I_GetConceptData typeConcept = typeConceptDescriptor.getVerifiedConcept();
            this.typeId = typeConcept.getConceptId();
        }

        public void processConcept(I_GetConceptData concept) throws Exception {

            //System.out.println(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids().iterator().next());
            int conceptId = concept.getConceptId();
            int currentStatusId = termFactory.uuidToNative(
                    ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());

            int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
                    ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
                    termFactory.getPaths(), Integer.MAX_VALUE);

            //System.out.println(termFactory.getExtensionsForComponent(conceptId).size());
            I_ThinExtByRefVersioned extension =
                termFactory.newExtension(referenceSetId, memberId, conceptId,
                    typeId);

            //I_ThinExtByRefPartConcept conceptExtension = termFactory.newConceptExtensionPart();
            I_ThinExtByRefPartConcept conceptExtension = termFactory.newConceptExtensionPart();
            //ThinExtByRefPartConcept conceptExtension = new ThinExtByRefPartConcept();

            conceptExtension.setPathId(memberSetPath.getConceptId());
            conceptExtension.setStatus(currentStatusId);
            conceptExtension.setVersion(Integer.MAX_VALUE);
            conceptExtension.setConceptId(conceptId);

            extension.addVersion(conceptExtension);
            //System.out.println("Number of ext for member: "
            //        + termFactory.getExtension(memberId));

            termFactory.addUncommitted(concept);
            //System.out.println("Uncommitted 2: " + termFactory.getUncommitted().size());
            //termFactory.commit();

            //ExtensionByReferenceBean ebrBean = ExtensionByReferenceBean.makeNew(
            //        extension.getMemberId(), extension);
            //System.out.println("Number of ext for concept: "
             //       + termFactory.getExtensionsForComponent(conceptId).size());

            extensionCount++;

        }

        /**
         * Gets the number of extensions created.
         * @return
         */
        public int getExtensionCount() {
            return extensionCount;
        }

        /**
         * Sets the number of extensions.
         * @param memberSetCount
         */
        public void setExtensionCount(int extensionCount) {
            this.extensionCount = extensionCount;
        }
    }
}
