package org.dwfa.mojo;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;

/**
 * Calculates the member set of a particular reference set.
 * @author Christine Hill
 *
 */

/**
 *
 * @goal vodb-calculate-member-set
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbCalculateMemberSet extends AbstractMojo {

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
    private ConceptDescriptor memberSetPathDescriptor;

    /**
     * @parameter
     * @required
     * The concept descriptor for the member set spec.
     */
    private ConceptDescriptor memberSetSpecDescriptor;

    /**
     * The path that the member set will be written on.
     */
    private I_Path memberSetPath;

    /**
     * The id of the member set.
     */
    private int memberSetId;

    /**
     * The ids of the concepts which form part of the member set.
     */
    private Set<Integer> memberSet;

    /**
     * The ids of the concepts which should be EXCLUDED from the member set.
     */
    private Set<Integer> excludedMemberSet;

    /**
     * Iterates over each concept and calculates the member set.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {

            // verify concepts
            I_GetConceptData refConcept = refSetSpecDescriptor.getVerifiedConcept();
            int referenceSetId = refConcept.getConceptId();

            I_GetConceptData memberSetPathConcept = memberSetPathDescriptor.getVerifiedConcept();
            memberSetPath = termFactory.getPath(memberSetPathConcept.getUids());

            I_GetConceptData memberSetSpecConcept = memberSetSpecDescriptor.getVerifiedConcept();
            memberSetId = memberSetSpecConcept.getConceptId();

            // initialise sets
            memberSet = new HashSet<Integer>();
            excludedMemberSet = new HashSet<Integer>();

            // execute calculate member set plugin
            MemberSetCalculator calculator =
                new MemberSetCalculator(referenceSetId);

            // iterate over each concept
            termFactory.iterateConcepts(calculator);

            String message = "Number of members found in reference set: "
                            + calculator.getMemberSetCount();
            getLog().info(message);
            termFactory.commit();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private class MemberSetCalculator implements I_ProcessConcepts {

        private I_TermFactory termFactory;
        private int memberSetCount;
        private int referenceSetId;
        private int includeLineageId;
        private int includeIndividualId;
        private int excludeLineageId;
        private int excludeIndividualId;
        private int typeId;
        private int currentStatusId;

        /**
         * Calculates a member set given a reference set spec.
         * @param referenceSetId The id of the reference set of which we wish to
         * calculate the member set.
         * @throws Exception
         */
        public MemberSetCalculator(int referenceSetId) throws Exception {

            termFactory = LocalVersionedTerminology.get();
            memberSetCount = 0;
            this.referenceSetId = referenceSetId;
            this.includeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids().iterator().next());
            this.includeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
            this.excludeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids().iterator().next());
            this.excludeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids().iterator().next());

            typeId = termFactory.uuidToNative(
                    RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
            currentStatusId = termFactory.uuidToNative(
                    ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());
        }

        /**
         * Processes each concept in the database. Concepts may be included
         * or excluded from the member set based on the reference set extension
         * type. Lineage (children) of the concept may also be included or excluded
         * (recursively).
         */
        public void processConcept(I_GetConceptData concept) throws Exception {

            int conceptId = concept.getConceptId();

            List<I_GetExtensionData> extensions =
                termFactory.getExtensionsForComponent(conceptId);

            // process each reference set extension
            for (I_GetExtensionData refSetExtension: extensions) {
                //System.out.println(refSetExtension.getMemberId());

                I_ThinExtByRefVersioned part = refSetExtension.getExtension();

                if (refSetExtension.getExtension().getRefsetId() == referenceSetId) {
                    // only look at the ref set extensions that correspond to
                    // the reference set as specified in maven plugin config
                    boolean include = false;;
                    if (part.getTypeId() == includeIndividualId) {
                        if (!memberSet.contains(conceptId)) {
                            addToMemberSet(conceptId);
                        }
                    } else if (part.getTypeId() == excludeIndividualId) {
                        excludeFromMemberSet(conceptId);
                    } else if (part.getTypeId() == includeLineageId) {
                        include = true;
                        addToMemberSet(conceptId);
                        processChildren(concept, include);
                    } else if (part.getTypeId() == excludeLineageId) {
                        include = false;
                        excludeFromMemberSet(conceptId);
                        processChildren(concept, include);
                    } else {
                        throw new Exception("Unknown extension type: " + part.getTypeId());
                    }
                }

            }

            termFactory.addUncommitted(concept);
        }

        /**
         * Adds a particular concept to the member set.
         * @param conceptId the concept id of the concept we wish to add to the member set.
         * @throws Exception
         */
        public void addToMemberSet(int conceptId) throws Exception {

            if (!excludedMemberSet.contains(conceptId)) {
                memberSetCount++;

                int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
                        ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
                        termFactory.getPaths(), Integer.MAX_VALUE);

                I_ThinExtByRefVersioned newExtension =
                    termFactory.newExtension(memberSetId, memberId, conceptId,
                        typeId);

                I_ThinExtByRefPartConcept conceptExtension =
                    termFactory.newConceptExtensionPart();

                conceptExtension.setPathId(memberSetPath.getConceptId());
                conceptExtension.setStatus(currentStatusId);
                conceptExtension.setVersion(Integer.MAX_VALUE);
                conceptExtension.setConceptId(conceptId);

                newExtension.addVersion(conceptExtension);

                memberSet.add(conceptId);
            }
        }

        /**
         * Excludes a concept from inclusion in the member set.
         * @param conceptId The concept id of the concept we wish to exclude.
         * @throws Exception
         */
        public void excludeFromMemberSet(int conceptId) throws Exception {

            excludedMemberSet.add(conceptId);
        }

        /**
         * Finds the children of a particular concept, and includes/excludes based
         * on provided parameter. Occurs recursively so that the entire lineage is
         * calculated.
         * @param concept The concept who's children we wish to process.
         * @param includeChildren Whether children will be included or excluded when processed.
         * @throws Exception
         */
        public void processChildren(I_GetConceptData concept, boolean includeChildren)
            throws Exception {

            // get latest IS-A relationships
            I_IntSet isARel = termFactory.newIntSet();
            isARel.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.
                    IS_A_REL.getUids()).getConceptId());

            List<I_RelTuple> children = concept.getDestRelTuples(
                    null, isARel, null, false);

            for (I_RelTuple child : children) {

                int childId = child.getC1Id();

                if (includeChildren) {
                    addToMemberSet(childId);
                } else {
                    excludeFromMemberSet(childId);
                }
                processChildren(termFactory.getConcept(childId), includeChildren);
            }
        }

        /**
         * Gets the number of members in the specified reference set.
         * @return
         */
        public int getMemberSetCount() {

            return memberSetCount;
        }

        /**
         * Sets the number of members in the specified reference set.
         * @param memberSetCount
         */
        public void setMemberSetCount(int memberSetCount) {

            this.memberSetCount = memberSetCount;
        }
    }
}
