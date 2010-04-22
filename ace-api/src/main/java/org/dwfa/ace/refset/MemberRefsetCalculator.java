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
package org.dwfa.ace.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import static org.dwfa.ace.refset.ConflictReportWriter.RESOLUTION;

/**
 * Generates the corresponding member refsets for the specification refsets.
 */
@Deprecated
public class MemberRefsetCalculator extends RefsetUtilities {

    private int commitSize = 1000;

    private boolean useNonTxInterface = false;

    private File outputDirectory;

    private File changeSetOutputDirectory;

    private boolean validateOnly = true;
    private boolean markParents = true;

    private boolean additionalLogging = false;

    /**
     * The ids of the concepts which may be included in the member set (due to
     * lineage).
     * These may be excluded if they explicitly state a refset exclusion.
     */
    protected Map<Integer, ClosestDistanceHashSet> newRefsetMembers = new HashMap<Integer, ClosestDistanceHashSet>();

    /**
     * The ids of the concepts which may be excluded from the member set (due to
     * lineage).
     * These may be included if they explicitly state a refset inclusion.
     */
    protected Map<Integer, ClosestDistanceHashSet> newRefsetExclusion = new HashMap<Integer, ClosestDistanceHashSet>();

    /**
     * The ids of the concepts which may be excluded from the member set (due to
     * lineage).
     * These may be included if they explicitly state a refset inclusion.
     */
    private Map<Integer, ClosestDistanceHashSet> existingRefsetMembers = new HashMap<Integer, ClosestDistanceHashSet>();

    /**
     * The ids of the concepts which may be excluded from the member set (due to
     * lineage).
     * These may be included if they explicitly state a refset inclusion.
     */
    private Map<Integer, ClosestDistanceHashSet> existingParentMembers = new HashMap<Integer, ClosestDistanceHashSet>();

    /**
     * The list of specification refsets to be analysed
     */
    private List<Integer> allowedRefsets = new ArrayList<Integer>();

    private int includeLineage;
    private int includeIndividual;
    private int excludeLineage;
    private int excludeIndividual;

    private File reportFile = null;
    private BufferedWriter reportWriter = null;

    private Map<Integer, List<Integer>> conceptsWithDirectInclusion = new HashMap<Integer, List<Integer>>();
    private Map<Integer, List<Integer>> conceptsWithDirectExclusion = new HashMap<Integer, List<Integer>>();

    private MemberRefsetChangesetWriter nonTxWriter;

    private int normalMemberId;

    private int markedParentMemberId;

    public void run() {

        termFactory = LocalVersionedTerminology.get();

        try {

            setUp();

            if (useNonTxInterface) {
                assert changeSetOutputDirectory != null;
                UUID path = pathConcept.getUids().iterator().next();
                nonTxWriter = new MemberRefsetChangesetWriter(changeSetOutputDirectory, termFactory, path);
            }

            if (allowedRefsets.size() == 0) {
                allowedRefsets = getSpecificationRefsets();
            }

            /*
             * Iterate over the concepts in the specification refset
             */

            for (Integer i : allowedRefsets) {

                int memberSetId = getMemberSetConcept(i).getConceptId();

                I_GetConceptData memberSet = getConcept(memberSetId);
                System.out.println("Checking refset: " + memberSet);

                List<Integer> conceptsWithInclusion = new ArrayList<Integer>();
                List<Integer> conceptsWithExclusion = new ArrayList<Integer>();
                List<Integer> conceptsWithIncludeIndividual = new ArrayList<Integer>();
                List<Integer> conceptsWithExcludeIndividual = new ArrayList<Integer>();

                List<I_ThinExtByRefVersioned> refsetMembers = termFactory.getRefsetExtensionMembers(i);
                /*
                 * For each inclusion concept, find the type and keep a list of
                 * which are each type...reasons for inclusion
                 *
                 * stop descending if child is in the exclusion lineage set
                 *
                 * Skip children which are in the exclusion individual set
                 */
                for (I_ThinExtByRefVersioned member : refsetMembers) {

                    I_GetConceptData concept = termFactory.getConcept(member.getComponentId());
                    System.out.println("getting versions for " + concept);

                    // Get both current and retired version, if the latest is
                    // retired then should not be included
                    List<I_ThinExtByRefTuple> versions = member.getTuples(getIntSet(
                        ArchitectonicAuxiliary.Concept.CURRENT, ArchitectonicAuxiliary.Concept.RETIRED), null, false);
                    System.out.println("done getting versions for " + concept + " they were " + versions);

                    if (versions.size() >= 1) {
                        int inclusiontype = getInclusionTypeForRefset(member);
                        if (inclusiontype == this.includeLineage) {
                            System.out.println(memberSet + " include lineage: " + concept);
                            conceptsWithInclusion.add(member.getComponentId());
                        } else if (inclusiontype == this.excludeLineage) {
                            System.out.println(memberSet + " exclude lineage: " + concept);
                            conceptsWithExclusion.add(member.getComponentId());
                        } else if (inclusiontype == this.includeIndividual) {
                            System.out.println(memberSet + " include ind: " + concept);
                            conceptsWithIncludeIndividual.add(member.getComponentId());
                        } else if (inclusiontype == this.excludeIndividual) {
                            System.out.println(memberSet + " exclude ind: " + concept);
                            conceptsWithExcludeIndividual.add(member.getComponentId());
                        }
                    }
                }

                // If the specification refset has a "exclude members" source
                // relationship then get all
                // the members of the destination refset and add them to
                // conceptsWithExcludeIndividual

                int excludeMembersRefsetId = getExcludeMembersRefset(i);
                if (excludeMembersRefsetId != ID_NOT_FOUND) {
                    I_GetConceptData exclusionSet = getConcept(excludeMembersRefsetId);
                    System.out.println("Applying additional exclusion members from "
                        + exclusionSet.getId().getUIDs().iterator().next() + " to member refset "
                        + memberSet.getId().getUIDs().iterator().next());

                    for (I_ThinExtByRefVersioned additionalExclusionMember : termFactory.getRefsetExtensionMembers(excludeMembersRefsetId)) {
                        conceptsWithExcludeIndividual.add(additionalExclusionMember.getComponentId());
                    }
                }

                System.out.println("Done calcuating for refset " + memberSet + " - commencing update");

                conceptsWithDirectInclusion.put(memberSetId, conceptsWithInclusion);
                conceptsWithDirectExclusion.put(memberSetId, conceptsWithExclusion);

                for (Integer member : conceptsWithInclusion) {
                    System.out.println("getting children for lineage include for concept "
                        + termFactory.getConcept(member) + " for member set " + memberSet);
                    IncludeAllChildren(member, memberSetId, member, 0);
                }

                /*
                 * Iterate over exclusions and find which ones are excluded
                 *
                 * stop descending if child is in the inclusion lineage set
                 *
                 * skip children which are in the inclusion individual set
                 */
                for (Integer member : conceptsWithExclusion) {
                    System.out.println("getting children for lineage exclude for concept "
                        + termFactory.getConcept(member) + " for member set " + memberSet);
                    removeFromRefsetInclusion(member, memberSetId);
                    ExcludeAllChildren(member, memberSetId, member, 0);
                }

                for (Integer member : conceptsWithIncludeIndividual) {
                    IncludeConcept(member, memberSetId, member);
                }

                for (Integer member : conceptsWithExcludeIndividual) {
                    ExcludeConcept(member, memberSetId, member);
                }

                List<I_ThinExtByRefVersioned> conceptsInMemberRefset = termFactory.getRefsetExtensionMembers(memberSetId);
                /*
                 * Add all members to the member refset so we know what was
                 * already there
                 */
                int parent_marker_nid = ConceptConstants.PARENT_MARKER.localize().getNid();

                System.out.println("collecting existing refset members for comparison");
                int counter = 0;
                for (I_ThinExtByRefVersioned member : conceptsInMemberRefset) {
                    counter++;
                    if (counter % 1000 == 0) {
                        ClosestDistanceHashSet existingMembers = existingRefsetMembers.get(memberSetId);
                        String existingMemberSize = "empty";
                        if (existingMembers != null) {
                            existingMemberSize = existingMembers.size() + "";
                        }
                        ClosestDistanceHashSet existingParents = existingParentMembers.get(memberSetId);
                        String existingParentsSize = "empty";
                        if (existingParents != null) {
                            existingParentsSize = existingParents.size() + "";
                        }
                        System.out.println("processed " + counter + " of " + conceptsInMemberRefset.size()
                            + " for refset " + memberSet + " existing member set is " + existingMemberSize
                            + " existing parents size is " + existingParentsSize);
                    }

                    I_ThinExtByRefPart latest = getLatestVersion(member);
                    if (latest != null && latest.getStatus() == currentStatusId) {
                        I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) latest;
                        if (part.getConceptId() != parent_marker_nid) {
                            addToExistingRefsetMembers(new ConceptRefsetInclusionDetails(member.getComponentId(),
                                includeIndividual, member.getComponentId(), 0), memberSetId);
                        } else {
                            addToExistingParentMembers(new ConceptRefsetInclusionDetails(member.getComponentId(),
                                includeIndividual, member.getComponentId(), 0), memberSetId);
                        }
                    }
                }

            }

            setMembers();

            shutDown();
        } catch (Exception e) {
            throw new RuntimeException("Member refset generation failed with exception", e);
        }
    }

    public void IncludeConcept(int componentId, int refsetId, int parentId) throws TerminologyException, IOException {
        addToRefsetMembers(new ConceptRefsetInclusionDetails(componentId, includeIndividual, parentId, 0), refsetId);
        removeFromRefsetExclusion(componentId, refsetId);
    }

    public void ExcludeConcept(int componentId, int refsetId, int parentId) throws TerminologyException, IOException {
        addToRefsetExclusion(new ConceptRefsetInclusionDetails(componentId, excludeIndividual, parentId, 0), refsetId);
        removeFromRefsetInclusion(componentId, refsetId);
    }

    private void ExcludeAllChildren(int componentId, int refsetId, int parentId, int distance) throws IOException,
            Exception {
        
        for (Integer descendantId : getDescendantsOfConcept(componentId)) {
            /*
             * Make sure the concept isn't already directly included
             */
            if (!conceptsWithDirectInclusion.get(refsetId).contains(descendantId)) {
                addToRefsetExclusion(new ConceptRefsetInclusionDetails(descendantId, excludeLineage, parentId, distance), refsetId);
            }
        }
    }

    private void removeFromRefsetExclusion(int componentId, int refsetId) {
        ClosestDistanceHashSet members = newRefsetExclusion.get(refsetId);
        if (members != null) {
            members.remove(componentId);
        }
    }

    private void removeFromRefsetInclusion(int componentId, int refsetId) {
        ClosestDistanceHashSet members = newRefsetMembers.get(refsetId);
        if (members != null) {
            members.remove(componentId);
        }
    }

    private void IncludeAllChildren(int componentId, int refsetId, int parentId, int distance) throws IOException,
            Exception {
        
        for (Integer descendantId : getDescendantsOfConcept(componentId)) {
            if (!conceptsWithDirectExclusion.get(refsetId).contains(descendantId)) {
                addToRefsetMembers(new ConceptRefsetInclusionDetails(descendantId, includeLineage, parentId, distance), refsetId);
            }
        }
    }

    public void addToExistingRefsetMembers(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
        addToNestedSet(existingRefsetMembers, conceptDetails, refset);
    }

    public void addToExistingParentMembers(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
        addToNestedSet(existingParentMembers, conceptDetails, refset);
    }

    public void addToRefsetMembers(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
        if (isAdditionalLogging()) {
            System.out.println("*** addToRefsetMembers refset=" + refset + " concept="
                + conceptToString(conceptDetails.getConceptId()));
        }
        addToNestedSet(newRefsetMembers, conceptDetails, refset);
    }

    public void addToRefsetExclusion(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
        if (isAdditionalLogging()) {
            System.out.println("*** addToRefsetExclusion refset=" + refset + " concept="
                + conceptToString(conceptDetails.getConceptId()));
        }
        addToNestedSet(newRefsetExclusion, conceptDetails, refset);
    }

    protected String conceptToString(int nid) {
        try {
            if (isAdditionalLogging()){
                I_GetConceptData concept = termFactory.getConcept(nid);
                return Integer.toString(nid) + " "
                        + concept.getUids().iterator().next().toString() 
                        + concept.getInitialText();
            } else {
                return Integer.toString(nid);
            }
        } catch (Exception e) {
            return Integer.toString(nid);
        }
    }

    private void shutDown() throws Exception {
        reportWriter.close();
        if (useNonTxInterface) {
            nonTxWriter.close();
        }
    }

    private void setUp() throws TerminologyException, IOException {
        termFactory = LocalVersionedTerminology.get();

        typeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
        includeLineage = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids().iterator().next());
        includeIndividual = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids()
            .iterator()
            .next());
        excludeLineage = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids().iterator().next());
        excludeIndividual = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids()
            .iterator()
            .next());

        retiredConceptId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
        currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());

        normalMemberId = RefsetAuxiliary.Concept.NORMAL_MEMBER.localize().getNid();
        markedParentMemberId = RefsetAuxiliary.Concept.MARKED_PARENT.localize().getNid();
        
        reportFile = new File(outputDirectory.getAbsolutePath() + File.separatorChar + "classes",
            "conceptsAddedToRefset.txt");
        reportFile.getParentFile().mkdirs();

        reportWriter = new BufferedWriter(new FileWriter(reportFile));

    }

    protected void setMembers() throws Exception {

        System.out.println("Starting reporting " + new Date());
        for (Integer refset : newRefsetMembers.keySet()) {
            reportWriter.write("\n\nIncluded members of refset " + getConcept(refset) + " are: ");
            reportWriter.newLine();
            ClosestDistanceHashSet newMembers = newRefsetMembers.get(refset);
            if (newMembers != null) {
                for (ConceptRefsetInclusionDetails i : newMembers.values()) {
                    reportWriter.write(getConcept(i.getConceptId()).toString());
                    reportWriter.newLine();
                }
            }
        }
        for (Integer refset : newRefsetExclusion.keySet()) {
            reportWriter.write("\n\nExcluded members of refset " + getConcept(refset) + " are: ");
            reportWriter.newLine();
            ClosestDistanceHashSet newMembers = newRefsetExclusion.get(refset);
            if (newMembers != null) {
                for (ConceptRefsetInclusionDetails i : newMembers.values()) {
                    reportWriter.write(getConcept(i.getConceptId()).toString());
                    reportWriter.newLine();
                }
            }
        }

        for (Integer refset : existingRefsetMembers.keySet()) {
            reportWriter.write("\n\nPrevious members of refset " + getConcept(refset) + " are: ");
            reportWriter.newLine();
            ClosestDistanceHashSet newMembers = existingRefsetMembers.get(refset);
            if (newMembers != null) {
                for (ConceptRefsetInclusionDetails i : newMembers.values()) {
                    reportWriter.write(getConcept(i.getConceptId()).toString());
                    reportWriter.newLine();
                }
            }
        }

        System.out.println("id for lineage include is " + this.includeLineage);
        System.out.println("id for lineage exclude is " + this.excludeLineage);
        System.out.println("id for individual include is " + this.includeIndividual);
        System.out.println("id for individual exclude is " + this.excludeIndividual);

        /** Need to process all new and old members */
        Set<Integer> newAndOldRefsetNid = new HashSet<Integer>();
        newAndOldRefsetNid.addAll(newRefsetMembers.keySet());
        newAndOldRefsetNid.addAll(newRefsetExclusion.keySet());

        for (Integer refset : newAndOldRefsetNid) {
            ClosestDistanceHashSet exclusions = new ClosestDistanceHashSet();

            ConflictReportWriter conflictWriter = new ConflictReportWriter(outputDirectory.getAbsolutePath()
                + File.separatorChar + "reports", getConcept(refset));

            // Share the concept cache for quicker lookups
            conflictWriter.setConceptCache(conceptCache);

            ClosestDistanceHashSet newMembers = newRefsetMembers.get(refset);
            ClosestDistanceHashSet oldMembers = newRefsetExclusion.get(refset);
            ClosestDistanceHashSet newMembersToBeRemoved = new ClosestDistanceHashSet();
            ClosestDistanceHashSet oldMembersToBeRemoved = new ClosestDistanceHashSet();
            if (newMembers != null && oldMembers != null) {
                Set<Integer> keySet = new HashSet<Integer>();
                keySet.addAll(newMembers.keySet());
                keySet.retainAll(oldMembers.keySet());
                for (Integer key : keySet) {
                    ConceptRefsetInclusionDetails newMember = newMembers.get(key);
                    ConceptRefsetInclusionDetails old = oldMembers.get(key);

                    RESOLUTION resolvedTo = null;

                    if ((old.getInclusionTypeId() == this.excludeLineage)
                        && ((newMember.getInclusionTypeId() == this.includeLineage) || (newMember.getInclusionTypeId() == this.includeIndividual))) {
                        // Resolve the inclusion on the new member
                        oldMembersToBeRemoved.add(old);
                        resolvedTo = RESOLUTION.INCLUDE;

                    } else if ((newMember.getInclusionTypeId() == this.excludeLineage)
                        && ((old.getInclusionTypeId() == this.includeLineage) || (old.getInclusionTypeId() == this.includeIndividual))) {
                        // Resolve to the inclusion on the old member
                        newMembersToBeRemoved.add(newMember);
                        resolvedTo = RESOLUTION.INCLUDE;

                    } else if (newMember.getInclusionTypeId() == this.excludeIndividual) {
                        // Resolve to the exclusion on the new member
                        newMembersToBeRemoved.add(newMember);
                        resolvedTo = RESOLUTION.EXCLUDE;

                    } else if (old.getInclusionTypeId() == this.excludeIndividual) {
                        // Resolve to the exclusion on the old member
                        oldMembersToBeRemoved.add(newMember);
                        resolvedTo = RESOLUTION.EXCLUDE;

                    } else {
                        throw new RuntimeException("Unable to resolve conflict due to unhandled inclusion types! ("
                            + getConceptName(newMember.getInclusionTypeId()) + " and "
                            + getConceptName(old.getInclusionTypeId()) + ")");
                    }

                    // check for conflict with self and if it is an individual
                    // include or exclude - if so suppress conflict from the
                    // report
                    if ((newMember.getConceptId() == newMember.getInclusionReasonId() && isIndividualType(newMember.getInclusionTypeId()))
                        || (newMember.getConceptId() == old.getInclusionReasonId() && isIndividualType(old.getInclusionTypeId()))) {
                        resolvedTo = null;
                    } else {
                        conflictWriter.addConflict(newMember.getConceptId(), newMember.getInclusionReasonId(),
                            newMember.getInclusionTypeId(), old.getInclusionReasonId(), old.getInclusionTypeId(),
                            resolvedTo);
                    }
                }
            }

            if (newMembers != null && newMembersToBeRemoved != null) {
                newMembers.removeAll(newMembersToBeRemoved);
            }
            if (oldMembers != null && oldMembersToBeRemoved != null) {
                oldMembers.removeAll(oldMembersToBeRemoved);
            }

            reportWriter.write("\n\nNew included members of refset " + getConcept(refset) + " are: ");
            reportWriter.newLine();
            newMembers = newRefsetMembers.get(refset);
            if(newMembers == null){
                newMembers = new ClosestDistanceHashSet();
            }
            oldMembers = existingRefsetMembers.get(refset);

            //let the madness begin
            Set<Integer> keySet = new HashSet<Integer>();
            keySet.addAll(newMembers.keySet());
            if (oldMembers != null) {
                keySet.removeAll(oldMembers.keySet());
            }

            int count = 0;
            long sysTime = System.currentTimeMillis();
            for (Integer conceptId : keySet) {
                count++;
                if (count % commitSize == 0) {
                    System.out.println("adding member " + count + " of " + keySet.size() + " ("
                        + (System.currentTimeMillis() - sysTime) + ")");
                    sysTime = System.currentTimeMillis();
                }
                if (!useNonTxInterface && termFactory.getUncommitted().size() > commitSize) {
                    termFactory.commit();
                }
                ConceptRefsetInclusionDetails member = newMembers.get(conceptId);
                reportWriter.write(getConcept(member.getConceptId()).toString());
                reportWriter.newLine();
                if (!validateOnly) {
                    if (useNonTxInterface) {
                        nonTxWriter.addToRefset(null, member.getConceptId(),
                            getMembershipType(member.getInclusionTypeId()), refset, currentStatusId);
                    } else {
                        addToMemberSet(member.getConceptId(), member.getInclusionTypeId(), refset);
                    }
                }
            }

            newMembers = existingRefsetMembers.get(refset);
            oldMembers = newRefsetMembers.get(refset);

            if (newMembers != null) {
                keySet = new HashSet<Integer>();
                keySet.addAll(newMembers.keySet());
                if (oldMembers != null) {
                    keySet.removeAll(oldMembers.keySet());
                }
                for (Integer conceptId : keySet) {
                    exclusions.add(newMembers.get(conceptId));
                }
            }

            ClosestDistanceHashSet existingMembers = existingRefsetMembers.get(refset);
            ClosestDistanceHashSet newExclusionMembers = newRefsetExclusion.get(refset);
            if (existingMembers != null) {
                keySet = new HashSet<Integer>();
                keySet.addAll(existingMembers.keySet());
                if (newExclusionMembers != null) {
                    keySet.retainAll(newExclusionMembers.keySet());
                } else {
                    keySet.clear();
                }
                for (Integer key : keySet) {
                    exclusions.add(existingMembers.get(key));
                }
            }
            reportWriter.write("\n\nNew excluded members who used to be members of refset " + getConcept(refset)
                + " are: ");
            reportWriter.newLine();
            for (ConceptRefsetInclusionDetails i : exclusions.values()) {
                reportWriter.write(getConcept(i.getConceptId()).toString());
                reportWriter.newLine();
                if (!validateOnly) {
                    I_ThinExtByRefVersioned ext = getExtensionForComponent(i.getConceptId(), refset, normalMemberId);
                    if (ext != null) {
                        if (!newestPartRetired(ext)) {
                            if (useNonTxInterface) {
                                I_ThinExtByRefPartConcept latestExtVersion = (I_ThinExtByRefPartConcept) getLatestVersion(ext);
                                nonTxWriter.addToRefset(ext.getMemberId(), i.getConceptId(),
                                    latestExtVersion.getConceptId(), refset, retiredConceptId);
                            } else {
                                retireLatestExtension(ext);
                            }
                        }
                    } else {
                        System.out.println("No extension exists with this refset id for this component");
                    }
                }
            }

            if (markParents) {
                ClosestDistanceHashSet oldparents = existingParentMembers.get(refset);
                ClosestDistanceHashSet parents = findParentsToBeMarked(newRefsetMembers.get(refset));

                if (existingRefsetMembers.get(refset) != null && oldparents != null) {
                    oldparents.removeAll(existingRefsetMembers.get(refset));
                }
                if (newRefsetMembers.get(refset) != null && parents != null) {
                    parents.removeAll(newRefsetMembers.get(refset));
                }

                reportWriter.write("\n\nParents that are not marked but will be marked in refset "
                    + getConcept(refset) + " are: ");
                reportWriter.newLine();
                for (ConceptRefsetInclusionDetails parent : parents.values()) {
                    if (oldparents == null
                        || (oldparents != null && !oldparents.containsKey(parent.getConceptId()))) {
                        if (!validateOnly) {
                            if (useNonTxInterface) {
                                nonTxWriter.addToRefset(null, parent.getConceptId(),
                                    ConceptConstants.PARENT_MARKER.localize().getNid(), refset, currentStatusId);
                            } else {
                                addToMemberSetAsParent(parent.getConceptId(), refset);
                            }
                            reportWriter.write(getConcept(parent.getConceptId()).toString());
                            reportWriter.newLine();
                        }
                    } else {
                        reportWriter.write(getConcept(parent.getConceptId()).toString()
                            + " ------- is already marked as parent");
                        reportWriter.newLine();
                    }
                }
                if (oldparents != null) {
                    oldparents.removeAll(parents);
                    for (ConceptRefsetInclusionDetails existingParent : oldparents.values()) {
                        I_ThinExtByRefVersioned ext = 
                            getExtensionForComponent(existingParent.getConceptId(), refset, markedParentMemberId);
                        if (ext != null) {
                            if (!newestPartRetired(ext)) {
                                if (useNonTxInterface) {
                                    I_ThinExtByRefPartConcept latestExtVersion = (I_ThinExtByRefPartConcept) getLatestVersion(ext);
                                    nonTxWriter.addToRefset(ext.getMemberId(), existingParent.getConceptId(),
                                        latestExtVersion.getConceptId(), refset, retiredConceptId);
                                } else {
                                    retireLatestExtension(ext);
                                }
                            }
                        } else {
                            System.out.println("No extension exists with this refset id for this component : "
                                + getConcept(existingParent.getConceptId()).toString());
                        }
                        reportWriter.write(getConcept(existingParent.getConceptId()).toString()
                            + " ------- to be retired");
                        reportWriter.newLine();
                    }
                }

            }

            new Thread(conflictWriter).start();
        }
    }

    private boolean newestPartRetired(I_ThinExtByRefVersioned ext) {
        I_ThinExtByRefPart newestPart = null;
        for (I_ThinExtByRefPart part : ext.getVersions()) {
            if (newestPart == null || part.getVersion() > newestPart.getVersion()) {
                newestPart = part;
            }
        }
        return newestPart.getStatus() == retiredConceptId;
    }

    private ClosestDistanceHashSet findParentsToBeMarked(ClosestDistanceHashSet concepts) throws IOException, Exception {
        ClosestDistanceHashSet nonMarkedParents = new ClosestDistanceHashSet();

        if (concepts != null) {
            int count = 0;
            long sysTime = System.currentTimeMillis();
            for (ConceptRefsetInclusionDetails conceptId : concepts.values()) {
                count++;
                if (count % commitSize == 0) {
                    System.out.println("finding parent to be marked " + count + " of " + concepts.size() + " ("
                        + (System.currentTimeMillis() - sysTime) + ")");
                    sysTime = System.currentTimeMillis();
                }

                if (isAdditionalLogging()) {
                    String conceptUuid = termFactory.getUids(conceptId.getConceptId()).iterator().next().toString();
                    System.out.println("* concept " + conceptUuid + " (" + conceptToString(conceptId.getConceptId()) + ")");
                }

                Set<Integer> parents = getAncestorsOfConcept(conceptId.getConceptId(), concepts);
                for (Integer parentId : parents) {

                    if (isAdditionalLogging()) {
                        String parentUuid = termFactory.getUids(parentId).iterator().next().toString();
                        System.out.println("    parent " + parentUuid + " (" + conceptToString(parentId) + ")");
                    }

                    ConceptRefsetInclusionDetails parent = new ConceptRefsetInclusionDetails(parentId, 0, 0, 0);
                    if (!concepts.containsKey(parent.getConceptId())) {
                        nonMarkedParents.add(parent);
                    }
                }
            }
        }
        return nonMarkedParents;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public boolean isValidateOnly() {
        return validateOnly;
    }

    public void setValidateOnly(boolean validateOnly) {
        this.validateOnly = validateOnly;
    }

    public List<Integer> getAllowedRefsets() {
        return allowedRefsets;
    }

    public void setAllowedRefsets(List<Integer> allowedRefsets) {
        this.allowedRefsets = allowedRefsets;
    }

    public boolean isMarkParents() {
        return markParents;
    }

    public void setMarkParents(boolean markParents) {
        this.markParents = markParents;
    }

    public File getReportFile() {
        return reportFile;
    }

    public void setReportFile(File reportFile) {
        this.reportFile = reportFile;
    }

    public int getCommitSize() {
        return commitSize;
    }

    public void setCommitSize(int commitSize) {
        this.commitSize = commitSize;
    }

    public boolean getUseNonTxInterface() {
        return useNonTxInterface;
    }

    public void setUseNonTxInterface(boolean useNonTxInterface) {
        this.useNonTxInterface = useNonTxInterface;
    }

    public File getChangeSetOutputDirectory() {
        return changeSetOutputDirectory;
    }

    public void setChangeSetOutputDirectory(File changeSetOutputDirectory) {
        this.changeSetOutputDirectory = changeSetOutputDirectory;
    }

    private boolean isIndividualType(int typeId) {
        return ((typeId == this.includeIndividual) || (typeId == this.excludeIndividual));
    }

    public boolean isAdditionalLogging() {
        return additionalLogging;
    }

    public void setAdditionalLogging(boolean additionalLogging) {
        this.additionalLogging = additionalLogging;
    }
}
