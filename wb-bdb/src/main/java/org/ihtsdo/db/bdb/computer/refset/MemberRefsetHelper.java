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
import org.dwfa.ace.api.I_HelpMemberRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.batch.Batch;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

/**
 * Utility class providing refset membership operations.
 */
@AllowDataCheckSuppression
public class MemberRefsetHelper extends RefsetHelper implements I_HelpMemberRefsets {

    private static final Logger logger = Logger.getLogger(MemberRefsetHelper.class.getName());

    private int memberTypeId;
    private int memberRefsetId;

    private MarkedParentRefsetHelper markedParentHelper = null;

    public MemberRefsetHelper(I_ConfigAceFrame frameConfig, int memberRefsetId, int memberTypeId) throws Exception {
        super(frameConfig);
        setMemberRefsetId(memberRefsetId);
        setMemberTypeId(memberTypeId);
        MarkedParentRefsetHelper mph = new MarkedParentRefsetHelper(frameConfig, memberRefsetId, memberTypeId);
        if (mph.getParentRefsetId() != Integer.MIN_VALUE) {
            markedParentHelper = new MarkedParentRefsetHelper(frameConfig, memberRefsetId, memberTypeId);
        }
 
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#addAllToRefset(java.util.Collection, java.lang.String, boolean)
	 */
    public void addAllToRefset(Collection<I_GetConceptData> members, String batchDescription, boolean useMonitor)
            throws Exception {

        Batch<I_GetConceptData> batch = new Batch<I_GetConceptData>(members, batchDescription, useMonitor) {

            Set<Integer> newMembers = new HashSet<Integer>();

            @Override
            public void processItem(I_GetConceptData item) throws Exception {
                boolean extAdded = newRefsetExtension(getMemberRefsetId(), item.getConceptNid(),
                		REFSET_TYPES.CID, new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE,
                        getMemberTypeId()),
                        getConfig());
                if (extAdded) {
                    newMembers.add(item.getConceptNid());
                }
            }

            @Override
            public void onComplete() throws Exception {
                List<UUID> markedParentsUuid = Arrays.asList(ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getUuids());

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
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#addAllToRefset(java.util.Collection, java.lang.String)
	 */
    public void addAllToRefset(Collection<I_GetConceptData> members, String batchDescription) throws Exception {
        boolean useMonitor = true;
        addAllToRefset(members, batchDescription, useMonitor);
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#addMarkedParents(java.lang.Integer)
	 */
    public void addMarkedParents(Integer... conceptIds) throws Exception {
        if (markedParentHelper != null) {
            markedParentHelper.addParentMembers(conceptIds);
        }
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#removeMarkedParents(java.lang.Integer)
	 */
    public void removeMarkedParents(Integer... conceptIds) throws Exception {
        if (markedParentHelper != null) {
            markedParentHelper.removeParentMembers(conceptIds);
        }
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#removeAllFromRefset(java.util.Collection, java.lang.String, boolean)
	 */
    public void removeAllFromRefset(Collection<I_GetConceptData> members, String batchDescription, boolean useMonitor)
            throws Exception {

        Batch<I_GetConceptData> batch = new Batch<I_GetConceptData>(members, batchDescription, useMonitor) {

            Set<Integer> removedMembers = new HashSet<Integer>();

            @Override
            public void processItem(I_GetConceptData item) throws Exception {
                if (retireRefsetExtension(getMemberRefsetId(), item.getConceptNid(), new RefsetPropertyMap().with(
                		RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, getMemberTypeId()))) {
                    removedMembers.add(item.getConceptNid());
                }
            }

            @Override
            public void onComplete() throws Exception {
                List<UUID> markedParentsUuid = Arrays.asList(ConceptConstants.INCLUDES_MARKED_PARENTS_REL_TYPE.getUuids());

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
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#removeAllFromRefset(java.util.Collection, java.lang.String)
	 */
    public void removeAllFromRefset(Collection<I_GetConceptData> members, String batchDescription) throws Exception {
        boolean useMonitor = true;
        removeAllFromRefset(members, batchDescription, useMonitor);

    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#addToRefset(int)
	 */
    public boolean addToRefset(int conceptId) throws Exception {
       
       
        boolean extAdded = newRefsetExtension(getMemberRefsetId(), conceptId, REFSET_TYPES.CID,
            new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, getMemberTypeId()),
            getConfig());
        if (extAdded) {
            addMarkedParents(conceptId);
            return true;
        } else
            return false;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#removeFromRefset(int)
	 */
    public boolean removeFromRefset(int conceptId) throws Exception {
        if (retireRefsetExtension(getMemberRefsetId(), conceptId, new RefsetPropertyMap().with(
        		RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, getMemberTypeId()))) {
            removeMarkedParents(conceptId);
            return true;
        } else
            return false;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#getMemberTypeId()
	 */
    public int getMemberTypeId() {
        return memberTypeId;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#setMemberTypeId(int)
	 */
    public void setMemberTypeId(int memberTypeId) {
        this.memberTypeId = memberTypeId;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#getMemberRefsetId()
	 */
    public int getMemberRefsetId() {
        return memberRefsetId;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#setMemberRefsetId(int)
	 */
    public void setMemberRefsetId(int memberRefsetId) {
        this.memberRefsetId = memberRefsetId;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.refset.I_HelpMemberRefsets#getExistingMembers()
	 */
    public Set<Integer> getExistingMembers() throws Exception {

        HashSet<Integer> results = new HashSet<Integer>();

        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        Collection<? extends I_ExtendByRef> extVersions = Terms.get().getRefsetExtensionMembers(memberRefsetId);

        for (I_ExtendByRef thinExtByRefVersioned : extVersions) {

            List<? extends I_ExtendByRefVersion> extensions = thinExtByRefVersioned.getTuples(config.getAllowedStatus(),
                config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

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


}
