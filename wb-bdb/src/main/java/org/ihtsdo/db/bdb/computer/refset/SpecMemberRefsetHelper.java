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
package org.ihtsdo.db.bdb.computer.refset;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.batch.Batch;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.spec.I_HelpMemberRefset;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.NoMappingException;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;

/**
 * Utility class providing refset membership operations.
 */
@AllowDataCheckSuppression
public class SpecMemberRefsetHelper extends SpecRefsetHelper implements I_HelpMemberRefset {

    private Logger logger = Logger.getLogger(SpecMemberRefsetHelper.class.getName());

    private int memberTypeId;
    private int memberRefsetId;

    public SpecMemberRefsetHelper(I_ConfigAceFrame config, int memberRefsetId, int memberTypeId) throws Exception {
        super(config);
        setMemberRefsetId(memberRefsetId);
        setMemberTypeId(memberTypeId);
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#addAllToRefset(java.util.Collection, java.lang.String, boolean)
	 */
    public void addAllToRefset(Collection<I_GetConceptData> members, String batchDescription, boolean useMonitor)
            throws Exception {

        Batch<I_GetConceptData> batch = new Batch<I_GetConceptData>(members, batchDescription, useMonitor) {

            Set<Integer> newMembers = new HashSet<Integer>();

            @Override
            public void processItem(I_GetConceptData item) throws Exception {
                if (newRefsetExtension(getMemberRefsetId(), item.getConceptId(), getMemberTypeId())) {
                    newMembers.add(item.getConceptId());
                }
            }

            @Override
            public void onComplete() throws Exception {
                List<UUID> markedParentsUuid =
                        Arrays.asList(ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getUuids());

                if (Terms.get().hasId(markedParentsUuid)) {
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
                Terms.get().cancel();
            }

        };

        batch.run();
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#addAllToRefset(java.util.Collection, java.lang.String)
	 */
    public void addAllToRefset(Collection<I_GetConceptData> members, String batchDescription) throws Exception {
        boolean useMonitor = true;
        addAllToRefset(members, batchDescription, useMonitor);
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#addMarkedParents(java.lang.Integer)
	 */
    public void addMarkedParents(Integer... conceptIds) throws Exception {
        getSpecMarkedParentRefsetHelper().addParentMembers(conceptIds);
    }

    private SpecMarkedParentRefsetHelper specMarkedParentRefsetHelper;
	private SpecMarkedParentRefsetHelper getSpecMarkedParentRefsetHelper()
			throws Exception {
		if (specMarkedParentRefsetHelper == null) {
			specMarkedParentRefsetHelper = new SpecMarkedParentRefsetHelper(getConfig(), memberRefsetId, memberTypeId);
			specMarkedParentRefsetHelper.setAutocommitActive(isAutocommitActive());
		}
		return specMarkedParentRefsetHelper;
	}

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#addDescriptionMarkedParents(java.lang.Integer)
	 */
    public void addDescriptionMarkedParents(Integer... conceptIds) throws Exception {
        getSpecMarkedParentRefsetHelper().addDescriptionParentMembers(conceptIds);
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#removeMarkedParents(java.lang.Integer)
	 */
    public void removeMarkedParents(Integer... conceptIds) throws Exception {
        getSpecMarkedParentRefsetHelper().removeParentMembers(conceptIds);
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#removeDescriptionMarkedParents(java.lang.Integer)
	 */
    public void removeDescriptionMarkedParents(Integer... conceptIds) throws Exception {
        getSpecMarkedParentRefsetHelper().removeDescriptionParentMembers(conceptIds);
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#removeAllFromRefset(java.util.Collection, java.lang.String, boolean)
	 */
    public void removeAllFromRefset(Collection<I_GetConceptData> members, String batchDescription, boolean useMonitor)
            throws Exception {

        Batch<I_GetConceptData> batch = new Batch<I_GetConceptData>(members, batchDescription, useMonitor) {

            Set<Integer> removedMembers = new HashSet<Integer>();

            @Override
            public void processItem(I_GetConceptData item) throws Exception {
                if (retireRefsetExtension(getMemberRefsetId(), item.getConceptId(), getMemberTypeId())) {
                    removedMembers.add(item.getConceptId());
                }
            }

            @Override
            public void onComplete() throws Exception {
                List<UUID> markedParentsUuid =
                        Arrays.asList(ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getUuids());

                if (Terms.get().hasId(markedParentsUuid)) {
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
                Terms.get().cancel();
            }

        };

        batch.run();
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#removeAllFromRefset(java.util.Collection, java.lang.String)
	 */
    public void removeAllFromRefset(Collection<I_GetConceptData> members, String batchDescription) throws Exception {
        boolean useMonitor = true;
        removeAllFromRefset(members, batchDescription, useMonitor);

    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#addToRefset(int)
	 */
    public boolean addToRefset(int conceptId) throws Exception {
        if (newRefsetExtension(getMemberRefsetId(), conceptId, getMemberTypeId())) {
            addMarkedParents(conceptId);
            return true;
        } else
            return false;
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#removeFromRefset(int)
	 */
    public boolean removeFromRefset(int conceptId) throws Exception {
        if (retireRefsetExtension(getMemberRefsetId(), conceptId, getMemberTypeId())) {
            removeMarkedParents(conceptId);
            return true;
        } else
            return false;
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#getMemberTypeId()
	 */
    public int getMemberTypeId() {
        return memberTypeId;
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#setMemberTypeId(int)
	 */
    public void setMemberTypeId(int memberTypeId) {
        this.memberTypeId = memberTypeId;
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#getMemberRefsetId()
	 */
    public int getMemberRefsetId() {
        return memberRefsetId;
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#setMemberRefsetId(int)
	 */
    public void setMemberRefsetId(int memberRefsetId) {
        this.memberRefsetId = memberRefsetId;
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#getExistingMembers()
	 */
    public Set<Integer> getExistingMembers() throws Exception {

        HashSet<Integer> results = new HashSet<Integer>();

        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        Collection<? extends I_ExtendByRef> extVersions = Terms.get().getRefsetExtensionMembers(memberRefsetId);

        for (I_ExtendByRef thinExtByRefVersioned : extVersions) {

            List<? extends I_ExtendByRefVersion> extensions =
                    thinExtByRefVersioned
                        .getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), 
                        config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_ExtendByRefVersion thinExtByRefTuple : extensions) {
                if (thinExtByRefTuple.getRefsetId() == memberRefsetId) {

                    I_ExtendByRefPartCid part = (I_ExtendByRefPartCid) thinExtByRefTuple.getMutablePart();
                    if (part.getC1id() == memberTypeId) {
                        results.add(thinExtByRefTuple.getComponentId());
                    }
                }
            }
        }

        return results;
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.refset.spec.I_HelpMemberRefset#getMemberRefsets()
	 */
    public Set<Integer> getMemberRefsets() throws Exception {

        HashSet<Integer> memberRefsets = new HashSet<Integer>();

        I_IntSet statuses = Terms.get().newIntSet();
        statuses.add(ReferenceConcepts.CURRENT.getNid());

        I_IntSet purposeTypes = Terms.get().newIntSet();
        purposeTypes.add(RefsetAuxiliary.Concept.REFSET_PURPOSE.localize().getNid());

        I_IntSet isATypes = getAvailableIsARelTypes();

        I_GetConceptData memberPurpose = Terms.get().getConcept(ReferenceConcepts.REFSET_MEMBER_PURPOSE.getNid());

        for (I_GetConceptData origin : memberPurpose.getDestRelOrigins(statuses, purposeTypes, null, 
                                getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy())) {
            // Check origin is a refset (ie. has not been retired as a refset)
            for (I_GetConceptData target : origin.getSourceRelTargets(statuses, isATypes, null, 
                getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy())) {
                if (target.getConceptId() == ReferenceConcepts.REFSET_IDENTITY.getNid()) {
                    memberRefsets.add(origin.getConceptId());
                }
            }
        }

        return memberRefsets;
    }

    /**
     * Check for a is_a relationship type defined on the member refset concept
     * otherwise default to just using
     * either a SNOMED or ArchitectonicAuxiliary is_a relationship type
     * 
     * @throws Exception
     */
    protected I_IntSet getAvailableIsARelTypes() throws Exception {
        I_IntSet results = Terms.get().newIntSet();

        try {

            I_IntSet relTypes = Terms.get().newIntSet();
            relTypes.add(ReferenceConcepts.MARKED_PARENT_IS_A_TYPE.getNid());
            I_GetConceptData memberRefset = Terms.get().getConcept(getMemberRefsetId());
            Set<? extends I_GetConceptData> requiredIsAType =
                    memberRefset.getSourceRelTargets(getCurrentStatusIntSet(), relTypes, null, 
                        getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());

            if (requiredIsAType != null && requiredIsAType.size() > 0) {
                // relationship exists so use the is-a specified by the
                // marked-parent-is-a relationship
                for (I_GetConceptData concept : requiredIsAType) {
                    results.add(concept.getConceptId());
                }

                // Added for backwards compatability. All newly created refset specs will have one or more
                // relationships specifiying the relationship types to use. e.g. if in a database with Snomed IS-a
                // and the AA is-a, it will have 2 relationships... one to each. Previously only one relationship
                // would have been created (to the SNOMED is-a), so this step ensures that the AA is-a is also added
                // in SNOMED databases.
                if (!results.contains(ReferenceConcepts.AUX_IS_A.getNid())) {
                    results.add(ReferenceConcepts.AUX_IS_A.getNid());
                }
            } else {
                // no specified marked-parent-is-a relationship defined, so
                // first default to using SNOMED and ArchitectonicAuxiliary is_a relationship type
                results.add(ReferenceConcepts.SNOMED_IS_A.getNid());
                results.add(ReferenceConcepts.AUX_IS_A.getNid());
            }
        } catch (NoMappingException ex) {
            // marked-parent-is-a relationship type is unknown so default to just using AA is-a
            results.add(ReferenceConcepts.AUX_IS_A.getNid());
        }

        return results;
    }
}
