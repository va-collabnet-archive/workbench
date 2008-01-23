package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.Collection;

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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
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
     * The ids of the concepts which may be included in the member set (due to lineage).
     * These may be excluded if they explicitly state a refset exclusion.
     */
    private Set<Integer> includedLineage;

    /**
     * The ids of the concepts which may be excluded from the member set (due to lineage).
     * These may be included if they explicitly state a refset inclusion.
     */
    private Set<Integer> excludedLineage;

    /**
     * The ids of the concepts which have already been added to member set.
     */
    private Set<Integer> includedMemberSet;

    /**
     * The ids of the concepts which have be explicitly exclude from the member set.
     */
    private Set<Integer> excludedMemberSet;

    /**
     * Location to write list of uuids for included concepts.
     * @parameter
     */
    private File refsetInclusionsOutputFile = new File("refsetInclusions");

    /**
     * Location to write list of uuids for excluded concepts.
     * @parameter
     */
    private File refsetExclusionsOutputFile = new File("refsetExclusions");

    /**
     * @parameter
     * @required
     * The root concept.
     */
    private ConceptDescriptor rootDescriptor;

    /**
     * Iterates over each concept and calculates the member set.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {

            // execute calculate member set plugin
            MemberSetCalculator calculator = new MemberSetCalculator();

            // iterate over each concept, starting at the root
            calculator.processConcept(calculator.getRoot());

            // write list of uuids for concepts that were included
            // in the member set
            refsetInclusionsOutputFile.getParentFile().mkdirs();
            BufferedWriter uuidWriter = new BufferedWriter(
                    new FileWriter(refsetInclusionsOutputFile));
            for (int i : includedMemberSet) {
                Collection<UUID> uuids = termFactory.getUids(i);
                for (UUID uuid: uuids) {
                    uuidWriter.write(uuid.toString());
                    uuidWriter.append("\t");
                    uuidWriter.append(getFsnFromConceptId(i));
                    uuidWriter.newLine();
                }
            }
            uuidWriter.close();

            // write list of uuids for concepts that were excluded
            // from member set
            refsetExclusionsOutputFile.getParentFile().mkdirs();
            uuidWriter = new BufferedWriter(
                    new FileWriter(refsetExclusionsOutputFile));
            for (int i : excludedMemberSet) {
                Collection<UUID> uuids = termFactory.getUids(i);
                for (UUID uuid: uuids) {
                    uuidWriter.write(uuid.toString());
                    uuidWriter.append("\t");
                    uuidWriter.append(getFsnFromConceptId(i));
                    uuidWriter.newLine();
                }
            }

            String message = "Number of members found in reference set: "
                            + calculator.getMemberSetCount();
            getLog().info(message);
            termFactory.commit();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    public String getFsnFromConceptId(int conceptId) throws Exception {

        I_GetConceptData concept = LocalVersionedTerminology.get().getConcept(conceptId);

        List<I_DescriptionVersioned> descriptions = concept.getDescriptions();
        int fsnId = LocalVersionedTerminology.get().uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.
                getUids().iterator().next());
        for (I_DescriptionVersioned description : descriptions) {
            List<I_DescriptionPart> parts = description.getVersions();
            for (I_DescriptionPart part : parts) {
                if (fsnId == part.getTypeId()) {
                    return part.getText();
                }
            }
        }

        return "unknown";
    }

    private class MemberSetCalculator implements I_ProcessConcepts {

        private I_TermFactory termFactory;
        private int memberSetCount;
        private int referenceSetId;
        private int includeLineageId;
        private int includeIndividualId;
        private int excludeLineageId;
        private int excludeIndividualId;
        private int conceptTypeId;
        private int retiredConceptId;
        private int typeId;
        private int currentStatusId;
        private I_GetConceptData root;

        /**
         * Calculates a member set given a reference set spec.
         * @param referenceSetId The id of the reference set of which we wish to
         * calculate the member set.
         * @throws Exception
         */
        public MemberSetCalculator() throws Exception {

            termFactory = LocalVersionedTerminology.get();

            // verify concepts
            I_GetConceptData refConcept = refSetSpecDescriptor.getVerifiedConcept();
            referenceSetId = refConcept.getConceptId();

            I_GetConceptData memberSetPathConcept = memberSetPathDescriptor.getVerifiedConcept();
            memberSetPath = termFactory.getPath(memberSetPathConcept.getUids());

            I_GetConceptData memberSetSpecConcept = memberSetSpecDescriptor.getVerifiedConcept();
            memberSetId = memberSetSpecConcept.getConceptId();

            root = rootDescriptor.getVerifiedConcept();

            // initialise sets
            includedLineage = new HashSet<Integer>();
            excludedLineage = new HashSet<Integer>();
            includedMemberSet = new HashSet<Integer>();
            excludedMemberSet = new HashSet<Integer>();

            memberSetCount = 0;

            includeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids().iterator().next());
            includeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
            excludeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids().iterator().next());
            excludeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids().iterator().next());
            conceptTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next());
            retiredConceptId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
            typeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
            currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());
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

            // process each refset associated with this concept and work out
            // if any of them are the refset we are looking for
            int refsetCount = 0;
            I_ThinExtByRefVersioned memberSet = null;
            for (I_GetExtensionData refSetExtension: extensions) {
                if (refSetExtension.getExtension().getRefsetId() == referenceSetId) {
                    refsetCount++;
                }
                if (refSetExtension.getExtension().getRefsetId() == memberSetId) {
                    memberSet = refSetExtension.getExtension();
                }
            }
            boolean includedInLatestMemberSet = latestMembersetIncludesConcept(memberSet);

            if (refsetCount == 0) {
                // no refsets have been found so check if there are any inherited
                // conditions
                if (includedLineage.contains(conceptId)) {
                    // this concept has an inherited condition for inclusion
                    if (!includedInLatestMemberSet) {
                        addToMemberSet(conceptId);
                    }
                } else if (excludedLineage.contains(conceptId)) {
                   excludedMemberSet.add(conceptId);
                }
            }

            // process each reference set extension
            for (I_GetExtensionData extensionData: extensions) {

                I_ThinExtByRefVersioned part = extensionData.getExtension();
                int extensionTypeId = part.getTypeId();

                if (extensionTypeId == conceptTypeId &&
                        extensionData.getExtension().getRefsetId() == referenceSetId) {
                    // only look at the ref set extensions that correspond to
                    // the reference set as specified in maven plugin config
                    int typeId = 0;


                    List<? extends I_ThinExtByRefPart> versions = part.getVersions();
                    for (I_ThinExtByRefPart version : versions) {
                        I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) version;
                        typeId = temp.getConceptId();
                    }

                    boolean include = true;
                    if (typeId == includeIndividualId) {
                        if (!includedInLatestMemberSet) {
                            addToMemberSet(conceptId);
                       }
                    } else if (typeId == includeLineageId) {
                        if (!includedInLatestMemberSet) {
                            addToMemberSet(conceptId);
                        }
                        markAllChildren(concept, include);
                    } else if (typeId == excludeIndividualId) {
                        if (includedInLatestMemberSet) {
                            retireLatestExtension(memberSet);
                            excludedMemberSet.add(conceptId);
                        }
                    } else if (typeId == excludeLineageId) {
                        if (includedInLatestMemberSet) {
                            retireLatestExtension(memberSet);
                            excludedMemberSet.add(conceptId);
                        }
                        markAllChildren(concept, !include);
                    } else {
                        System.out.println(termFactory.getConcept(typeId));
                        throw new Exception("Unknown extension type: " + typeId);
                    }
                }
            }

            // find the children of this concept
            I_IntSet isARel = termFactory.newIntSet();
            isARel.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.
                    IS_A_REL.getUids()).getConceptId());

            List<I_RelTuple> children = concept.getDestRelTuples(
                    null, isARel, null, false);

            for (I_RelTuple child : children) {
                int childId = child.getC1Id();
                processConcept(termFactory.getConcept(childId));
            }

            termFactory.addUncommitted(concept);
        }

        /**
         * Calculates if the latest version of the extension includes the current concept.
         * @param extensionPart The extension to check.
         * @return True if the member set includes the concept, false if it doesn't.
         * @throws Exception
         */
        public boolean latestMembersetIncludesConcept(I_ThinExtByRefVersioned extensionPart) throws Exception {
            if (extensionPart == null) {
                return false;
            }

            int latest = Integer.MIN_VALUE;

            List<? extends I_ThinExtByRefPart> extensionVersions = extensionPart.getVersions();
            for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                if (currentVersion.getVersion() > latest) {
                    latest = currentVersion.getVersion();
                }
            }

            for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                if (currentVersion.getVersion() == latest) {
                    if (currentVersion.getStatus() == retiredConceptId) {
                        // the concept has been retired, so the latest version
                        // does NOT include this concept
                        return false;
                    } else if (currentVersion.getStatus() == currentStatusId) {
                        return true;
                    } else {
                        System.out.println("Unknown status: " + currentVersion.getStatus());
                        System.exit(1);
                        return false;
                    }
                }
            }
            return false;
        }

        /**
         * Retires the latest version of a specified extension.
         * @param extensionPart The extension to check.
         * @throws Exception
         */
        public void retireLatestExtension(I_ThinExtByRefVersioned extensionPart) throws Exception {
            if (extensionPart != null) {

                int latest = Integer.MIN_VALUE;

                List<? extends I_ThinExtByRefPart> extensionVersions = extensionPart.getVersions();
                for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                    if (currentVersion.getVersion() > latest) {
                        latest = currentVersion.getVersion();
                    }
                }

                for (I_ThinExtByRefPart currentVersion : extensionVersions) {
                    if (currentVersion.getVersion() == latest) {
                        // this is the latest version, so mark as retired
                        currentVersion.setStatus(retiredConceptId);
                        extensionPart.addVersion(currentVersion);
                    }
                }
            }
        }

        /**
         * Adds a particular concept to the member set.
         * @param conceptId the concept id of the concept we wish to add to the member set.
         * @throws Exception
         */
        public void addToMemberSet(int conceptId) throws Exception {

            if (!includedMemberSet.contains(conceptId)) {
                memberSetCount++;
                includedMemberSet.add(conceptId);

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
            }
        }

        /**
         * Finds the children of a particular concept, and includes/excludes based
         * on provided parameter. Occurs recursively so that the entire lineage is
         * calculated.
         * @param concept The concept who's children we wish to process.
         * @param includeChildren Whether children will be included or excluded when processed.
         * @throws Exception
         */
        public void markAllChildren(I_GetConceptData concept, boolean includeChildren)
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
                    if (excludedLineage.contains(Integer.valueOf(childId))) {
                        excludedLineage.remove(Integer.valueOf(childId));
                    }
                    includedLineage.add(Integer.valueOf(childId));
                } else {
                    if (includedLineage.contains(Integer.valueOf(childId))) {
                        includedLineage.remove(Integer.valueOf(childId));
                    }
                    excludedLineage.add(Integer.valueOf(childId));
                }
                markAllChildren(termFactory.getConcept(childId), includeChildren);
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

        public I_GetConceptData getRoot() {
            return root;
        }

        public void setRoot(I_GetConceptData root) {
            this.root = root;
        }
    }

    public ConceptDescriptor getMemberSetPathDescriptor() {
        return memberSetPathDescriptor;
    }

    public void setMemberSetPathDescriptor(ConceptDescriptor memberSetPathDescriptor) {
        this.memberSetPathDescriptor = memberSetPathDescriptor;
    }

    public ConceptDescriptor getMemberSetSpecDescriptor() {
        return memberSetSpecDescriptor;
    }

    public void setMemberSetSpecDescriptor(ConceptDescriptor memberSetSpecDescriptor) {
        this.memberSetSpecDescriptor = memberSetSpecDescriptor;
    }

    public ConceptDescriptor getRefSetSpecDescriptor() {
        return refSetSpecDescriptor;
    }

    public void setRefSetSpecDescriptor(ConceptDescriptor refSetSpecDescriptor) {
        this.refSetSpecDescriptor = refSetSpecDescriptor;
    }

    public ConceptDescriptor getRootDescriptor() {
        return rootDescriptor;
    }

    public void setRootDescriptor(ConceptDescriptor rootDescriptor) {
        this.rootDescriptor = rootDescriptor;
    }
}
