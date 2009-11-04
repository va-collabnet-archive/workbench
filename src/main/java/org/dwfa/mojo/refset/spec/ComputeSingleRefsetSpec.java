package org.dwfa.mojo.refset.spec;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.spec.SpecMemberRefsetHelper;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.RefsetQueryFactory;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.MojoUtil;
import org.dwfa.mojo.ConceptDescriptor;

/**
 * Computes the membership of the specified refset spec.
 * 
 * @goal compute-single-refset-membership
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class ComputeSingleRefsetSpec extends AbstractMojo {

    /**
     * The refset spec.
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor refsetSpecDescriptor;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + refsetSpecDescriptor, this
                .getClass(), targetDirectory)) {
                return;
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        try {
            long startTime = new Date().getTime();
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            I_ConfigAceFrame configFrame = termFactory.getActiveAceFrameConfig();
            int conceptsProcessed = 0;
            I_GetConceptData currentConcept;

            List<UUID> markedParentsUuid = Arrays.asList(ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getUuids());
            Set<Integer> newMembers = new HashSet<Integer>();
            Set<Integer> retiredMembers = new HashSet<Integer>();
            I_GetConceptData normalMemberConcept =
                    termFactory.getConcept(RefsetAuxiliary.Concept.NORMAL_MEMBER.getUids());

            I_GetConceptData refsetSpec = refsetSpecDescriptor.getVerifiedConcept();
            RefsetSpec rsHelper = new RefsetSpec(refsetSpec);
            if (refsetSpec == null) {
                getLog().info("Refset spec is null.");
                throw new MojoFailureException("Refset spec is null.");
            }
            I_GetConceptData refset = rsHelper.getMemberRefsetConcept();
            if (refset == null) {
                getLog().info("Refset is null.");
                throw new MojoFailureException("Refset is null.");
            }

            // Step 1: create the query object, based on the refset spec
            RefsetSpecQuery query = RefsetQueryFactory.createQuery(configFrame, termFactory, refsetSpec, refset);
            RefsetSpecQuery possibleQuery =
                    RefsetQueryFactory.createPossibleQuery(configFrame, termFactory, refsetSpec, refset);
            SpecMemberRefsetHelper memberRefsetHelper =
                    new SpecMemberRefsetHelper(refset.getConceptId(), normalMemberConcept.getConceptId());

            // check validity of query
            if (query.getTotalStatementCount() == 0) {
                getLog().info("Refset spec is empty - skipping execution.");
                throw new MojoFailureException("Refset spec is empty - skipping execution.");
            }
            if (!query.isValidQuery()) {
                getLog().info("Refset spec has dangling AND/OR. These must have sub-statements.");
                throw new MojoFailureException("Refset spec has dangling AND/OR. These must have sub-statements.");
            }

            getLog().info("Start execution of refset spec : " + refsetSpec.getInitialText());

            // create a list of all the current refset members (this requires
            // filtering out retired versions)
            List<I_ThinExtByRefVersioned> allRefsetMembers =
                    termFactory.getRefsetExtensionMembers(refset.getConceptId());
            HashSet<Integer> currentRefsetMemberIds =
                    filterNonCurrentRefsetMembers(allRefsetMembers, memberRefsetHelper, refset.getConceptId(),
                        normalMemberConcept.getConceptId());

            // Compute the possible concepts to iterate over here...
            I_RepresentIdSet possibleConcepts = possibleQuery.getPossibleConcepts(configFrame);
            possibleConcepts.or(termFactory.getIdSetFromIntCollection(currentRefsetMemberIds));

            I_IterateIds nidIterator = possibleConcepts.iterator();
            while (nidIterator.next()) {
                int nid = nidIterator.nid();
                if (possibleConcepts.isMember(nid)) {
                    currentConcept = termFactory.getConcept(nid);
                    conceptsProcessed++;

                    boolean containsCurrentMember = currentRefsetMemberIds.contains(currentConcept.getConceptId());
                    if (query.execute(currentConcept)) {
                        if (!containsCurrentMember) {
                            newMembers.add(currentConcept.getConceptId());
                        }
                    } else {
                        if (containsCurrentMember) {
                            retiredMembers.add(currentConcept.getConceptId());
                        }
                    }

                    if (conceptsProcessed % 10000 == 0) {
                        getLog().info(
                            "Concepts processed: " + conceptsProcessed + " / " + termFactory.getConceptCount());
                        getLog().info("New members: " + newMembers.size());
                        getLog().info("Retired members: " + retiredMembers.size());
                    }
                }
            }

            // Step 3 : create new member refsets
            getLog().info("Creating new member refsets");
            for (Integer memberId : newMembers) {
                memberRefsetHelper.newRefsetExtension(refset.getConceptId(), memberId, normalMemberConcept
                    .getConceptId(), false);
            }

            // Step 4: retire old member refsets
            getLog().info("Retiring old member refsets");
            for (Integer retiredMemberId : retiredMembers) {
                memberRefsetHelper.retireRefsetExtension(refset.getConceptId(), retiredMemberId, normalMemberConcept
                    .getConceptId());
            }

            // Step 5 : add / remove marked parent refsets
            if (termFactory.hasId(markedParentsUuid)) {
                getLog().info("Adding marked parents");
                for (Integer newMember : newMembers) {
                    memberRefsetHelper.addMarkedParents(new Integer[] { newMember });
                }

                getLog().info("Removing marked parents");
                for (Integer retiredMember : retiredMembers) {
                    memberRefsetHelper.removeMarkedParents(new Integer[] { retiredMember });
                }
            }
            long endTime = new Date().getTime();
            long minutes = (endTime - startTime) / 60000;
            long seconds = ((endTime - startTime) % 60000) / 1000;
            String executionTimeString = "Total execution time: " + minutes + " minutes, " + seconds + " seconds.";
            getLog().info(">>>>>>>>>>><<<<<<<<<<<");
            getLog().info("Number of new refset members: " + newMembers.size());
            getLog().info("Total number of concepts processed: " + conceptsProcessed);
            getLog().info("End execution of refset spec: " + refsetSpec.getInitialText());
            getLog().info("Total execution time: " + executionTimeString);
            getLog().info(">>>>>> COMPLETE <<<<<<");

            termFactory.commit();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private HashSet<Integer> filterNonCurrentRefsetMembers(List<I_ThinExtByRefVersioned> list,
            SpecMemberRefsetHelper refsetHelper, int refsetId, int memberTypeId) throws Exception {

        HashSet<Integer> newList = new HashSet<Integer>();
        for (I_ThinExtByRefVersioned v : list) {
            if (refsetHelper.hasCurrentRefsetExtension(refsetId, v.getComponentId(), memberTypeId)) {
                newList.add(v.getComponentId());
            }
        }
        return newList;
    }
}