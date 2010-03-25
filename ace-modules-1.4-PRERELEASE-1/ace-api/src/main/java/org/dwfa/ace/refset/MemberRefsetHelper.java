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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.BeanPropertyMap;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.api.ebr.ThinExtByRefPartProperty;
import org.dwfa.ace.batch.Batch;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.SuppressDataChecks;

/**
 * Utility class providing refset membership operations.
 */
@AllowDataCheckSuppression
public class MemberRefsetHelper extends RefsetHelper {

    private Logger logger = Logger.getLogger(MemberRefsetHelper.class.getName());

    private int memberTypeId;
    private int memberRefsetId;

    public MemberRefsetHelper(int memberRefsetId) throws Exception {
        this(memberRefsetId, RefsetAuxiliary.Concept.NORMAL_MEMBER.localize().getNid());
    }
    
    public MemberRefsetHelper(int memberRefsetId, int memberTypeId) throws Exception {
        super();
        setMemberRefsetId(memberRefsetId);
        setMemberTypeId(memberTypeId);
    }

    /**
     * Add a collection of concepts to a refset.
     * 
     * @param members
     *            The collection of concepts to be added to the refset
     * @param batchDescription
     *            A textual description of the batch being processed. Used in
     *            the progress reports given during processing.
     */
    public void addAllToRefset(Collection<I_GetConceptData> members, String batchDescription, boolean useMonitor)
            throws Exception {

        Batch<I_GetConceptData> batch = new Batch<I_GetConceptData>(members, batchDescription, useMonitor) {

            Set<Integer> newMembers = new HashSet<Integer>();

            @Override
            public void processItem(I_GetConceptData item) throws Exception {
                boolean extAdded = newRefsetExtension(getMemberRefsetId(), item.getConceptId(),
                    I_ThinExtByRefPartConcept.class, new BeanPropertyMap().with(ThinExtByRefPartProperty.CONCEPT_ONE,
                        getMemberTypeId()));
                if (extAdded) {
                    newMembers.add(item.getConceptId());
                }
            }

            @Override
            public void onComplete() throws Exception {
                List<UUID> markedParentsUuid = Arrays.asList(ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getUuids());

                if (termFactory.hasId(markedParentsUuid)) {
                    if (useMonitor) {
                        monitor.setText("Adding marked parent members...");
                        monitor.setIndeterminate(true);
                    }
                    logger.fine("Adding marked parents.");
                    addMarkedParents(newMembers.toArray(new Integer[] {}));
                }
            }

            @Override
            public void onCancel() throws Exception {
                logger.info("Batch operation '" + description + "' cancelled by user.");
                termFactory.cancel();
            }

        };

        batch.run();
    }

    /**
     * Add a collection of concepts to a refset.
     * 
     * @param members
     *            The collection of concepts to be added to the refset
     * @param batchDescription
     *            A textual description of the batch being processed. Used in
     *            the progress reports given during processing.
     */
    public void addAllToRefset(Collection<I_GetConceptData> members, String batchDescription) throws Exception {
        boolean useMonitor = true;
        addAllToRefset(members, batchDescription, useMonitor);
    }

    public void addMarkedParents(Integer... conceptIds) throws Exception {
        new MarkedParentRefsetHelper(memberRefsetId, memberTypeId).addParentMembers(conceptIds);
    }

    public void removeMarkedParents(Integer... conceptIds) throws Exception {
        new MarkedParentRefsetHelper(memberRefsetId, memberTypeId).removeParentMembers(conceptIds);
    }

    /**
     * Remove a collection of concepts from a refset.
     * 
     * @param members
     *            The collection of concepts to be removed from the refset
     * @param batchDescription
     *            A textual description of the batch being processed. Used in
     *            the progress reports given during processing.
     */
    public void removeAllFromRefset(Collection<I_GetConceptData> members, String batchDescription, boolean useMonitor)
            throws Exception {

        Batch<I_GetConceptData> batch = new Batch<I_GetConceptData>(members, batchDescription, useMonitor) {

            Set<Integer> removedMembers = new HashSet<Integer>();

            @Override
            public void processItem(I_GetConceptData item) throws Exception {
                if (retireRefsetExtension(getMemberRefsetId(), item.getConceptId(), new BeanPropertyMap().with(
                    ThinExtByRefPartProperty.CONCEPT_ONE, getMemberTypeId()))) {
                    removedMembers.add(item.getConceptId());
                }
            }

            @Override
            public void onComplete() throws Exception {
                List<UUID> markedParentsUuid = Arrays.asList(ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getUuids());

                if (termFactory.hasId(markedParentsUuid)) {
                    if (useMonitor) {
                        monitor.setText("Removing marked parent members...");
                        monitor.setIndeterminate(true);
                    }
                    removeMarkedParents(removedMembers.toArray(new Integer[] {}));
                }
            }

            @Override
            public void onCancel() throws Exception {
                logger.info("Batch operation '" + description + "' cancelled by user.");
                termFactory.cancel();
            }

        };

        batch.run();
    }

    /**
     * Remove a collection of concepts from a refset.
     * 
     * @param members
     *            The collection of concepts to be removed from the refset
     * @param batchDescription
     *            A textual description of the batch being processed. Used in
     *            the progress reports given during processing.
     */
    public void removeAllFromRefset(Collection<I_GetConceptData> members, String batchDescription) throws Exception {
        boolean useMonitor = true;
        removeAllFromRefset(members, batchDescription, useMonitor);

    }

    /**
     * Add a concept to a refset
     * 
     * @param newMemberId
     *            The concept to be added
     */
    public boolean addToRefset(int conceptId) throws Exception {
        boolean extAdded = newRefsetExtension(getMemberRefsetId(), conceptId, I_ThinExtByRefPartConcept.class,
            new BeanPropertyMap().with(ThinExtByRefPartProperty.CONCEPT_ONE, getMemberTypeId()));
        if (extAdded) {
            addMarkedParents(conceptId);
            return true;
        } else
            return false;
    }

    /**
     * Remove a concept from a refset
     * 
     * @param newMemberId
     *            The concept to be removed
     */
    public boolean removeFromRefset(int conceptId) throws Exception {
        if (retireRefsetExtension(getMemberRefsetId(), conceptId, new BeanPropertyMap().with(
            ThinExtByRefPartProperty.CONCEPT_ONE, getMemberTypeId()))) {
            removeMarkedParents(conceptId);
            return true;
        } else
            return false;
    }

    public int getMemberTypeId() {
        return memberTypeId;
    }

    public void setMemberTypeId(int memberTypeId) {
        this.memberTypeId = memberTypeId;
    }

    public int getMemberRefsetId() {
        return memberRefsetId;
    }

    public void setMemberRefsetId(int memberRefsetId) {
        this.memberRefsetId = memberRefsetId;
    }

    public Set<Integer> getExistingMembers() throws Exception {

        HashSet<Integer> results = new HashSet<Integer>();

        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

        List<I_ThinExtByRefVersioned> extVersions = termFactory.getRefsetExtensionMembers(memberRefsetId);

        for (I_ThinExtByRefVersioned thinExtByRefVersioned : extVersions) {

            List<I_ThinExtByRefTuple> extensions = thinExtByRefVersioned.getTuples(config.getAllowedStatus(),
                config.getViewPositionSet(), true, false);

            for (I_ThinExtByRefTuple thinExtByRefTuple : extensions) {
                if (thinExtByRefTuple.getRefsetId() == memberRefsetId) {

                    I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) thinExtByRefTuple.getPart();
                    if (part.getC1id() == memberTypeId) {
                        results.add(thinExtByRefTuple.getComponentId());
                    }
                }
            }
        }

        return results;
    }

    /**
     * Find all the current member refset concepts.
     * <p>
     * Member refsets must have the following properties:
     * <ul>
     * <li>Is a <i>refset identity</i>
     * <li>A <i>refset purpose</i> of <i>refset membership</i>
     */
    public static Set<Integer> getMemberRefsets() throws Exception {

        HashSet<Integer> memberRefsets = new HashSet<Integer>();
        I_TermFactory termFactory = LocalVersionedTerminology.get();

        int currentStatusId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
        int memberRefsetPurposeId = ConceptConstants.REFSET_MEMBER_PURPOSE.localize().getNid();
        int refsetIdenityId = RefsetAuxiliary.Concept.REFSET_IDENTITY.localize().getNid();

        I_IntSet statuses = termFactory.newIntSet();
        statuses.add(currentStatusId);

        I_IntSet purposeTypes = termFactory.newIntSet();
        purposeTypes.add(RefsetAuxiliary.Concept.REFSET_PURPOSE.localize().getNid());

        I_IntSet isATypes = termFactory.newIntSet();
        isATypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
        isATypes.add(ConceptConstants.SNOMED_IS_A.localize().getNid());

        I_GetConceptData memberPurpose = termFactory.getConcept(memberRefsetPurposeId);

        for (I_GetConceptData origin : memberPurpose.getDestRelOrigins(statuses, purposeTypes, null, false, true)) {
            // Check origin is a refset (ie. has not been retired as a refset)
            for (I_GetConceptData target : origin.getSourceRelTargets(statuses, isATypes, null, false, true)) {
                if (target.getConceptId() == refsetIdenityId) {
                    memberRefsets.add(origin.getConceptId());
                }
            }
        }

        return memberRefsets;
    }

    /**
     * Create a new member reference set.
     * This will create a concept extension (attribute) type reference set.
     * It will also create a separate, associated marked parent refset. 
     * 
     * @param description 
     *          The name/term for the new refset. This is be post-fixed accordingly.
     *          
     * @return An initialised instance of this helper class ready to work with the new refset.
     */
    @SuppressDataChecks
    public static MemberRefsetHelper createNewRefset(String description) 
            throws Exception {
        
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
        
        // Load references
        
        I_GetConceptData definingCharacteristic = 
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid());
        I_GetConceptData optionalRefinability = 
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.localize().getNid());
        I_GetConceptData currentStatus = 
            termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
        I_GetConceptData markedParentRefsetRel = 
            termFactory.getConcept(RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.localize().getNid());   
        
        // Remove any (common) refset qualifications (if present) from the description  
        
        if (description.toLowerCase().endsWith(" reference set")) {
            description = description.substring(0, description.length() - 14);
        }
        if (description.toLowerCase().endsWith(" refset")) {
            description = description.substring(0, description.length() - 7);
        }
        if (description.toLowerCase().endsWith(" member")) {
            description = description.substring(0, description.length() - 7);
        }
        String refsetName = description.concat(" member reference set");
        String parentRefsetName = description.concat(" marked parent member reference set");

        // Create the refset

        I_GetConceptData newMemberRefsetConcept = 
            newRefset(I_ThinExtByRefPartConcept.class, refsetName, RefsetAuxiliary.Concept.REFSET_MEMBER_PURPOSE);
        
        I_GetConceptData newMarkedParentRefsetConcept =
            newRefset(I_ThinExtByRefPartConcept.class, parentRefsetName, RefsetAuxiliary.Concept.REFSET_PARENT_MEMBER_PURPOSE);

        // Link them together
        
        termFactory.newRelationship(
            UUID.randomUUID(), newMemberRefsetConcept, markedParentRefsetRel, newMarkedParentRefsetConcept, 
            definingCharacteristic, optionalRefinability, currentStatus, 0, config);
        
        return new MemberRefsetHelper(newMemberRefsetConcept.getNid());
    }
    
}
