package org.dwfa.ace.refset.spec;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;

public interface I_HelpSpecRefset extends I_HelpRefsets {

    I_HelpMemberRefset getMemberHelper(int memberRefsetId, int memberTypeId) throws Exception;

    /**
     * A simple template for logic that defines if a process should be executed
     * on a particular subject (concept).
     */
    public interface Condition {
        public boolean evaluate(I_GetConceptData concept) throws Exception;
    }

    /**
     * Gets the last I_ExtendByRefPartCid that has a status of current for
     * a refset and concept.
     * 
     * @param refsetId
     *            int
     * @param conceptId
     *            int
     * @return I_ExtendByRefPartCid with a status of current.
     * 
     * @throws Exception
     *             if cannot get all extension for a concept id..
     */
    public I_ExtendByRefPartCid getCurrentRefsetExtension(int refsetId, int conceptId) throws Exception;

    public boolean hasCurrentRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception;

    public Set<Integer> getCurrentStatusIds();

    public I_IntSet getCurrentStatusIntSet();

    public boolean hasCurrentConceptConceptRefsetExtension(int refsetId, int conceptId, int c1Id, int c2Id, int statusId)
            throws Exception;

    public boolean hasCurrentConceptRefsetExtension(int refsetId, int componentId, int conceptId, int statusId)
            throws Exception;

    public boolean hasCurrentIntRefsetExtension(int refsetId, int componentId, int value, int statusId)
            throws Exception;

    public boolean hasCurrentConceptConceptConceptRefsetExtension(int refsetId, int conceptId, int c1Id, int c2Id,
            int c3Id, int statusId) throws Exception;

    public boolean hasCurrentConceptConceptStringRefsetExtension(int refsetId, int conceptId, int c1Id, int c2Id,
            String stringInput, int statusId) throws Exception;

    /**
     * Add a concept to a refset (if it doesn't already exist)
     * 
     * @see {@link #newRefsetExtension(int, int, int, boolean)}
     */
    public boolean newRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception;

    /**
     * Add a concept to a refset
     * 
     * @param refsetId
     *            The subject refset
     * @param conceptId
     *            The concept to be added
     * @param memberTypeId
     *            The value of the concept extension to be added to the new
     *            member concept.
     * @param checkNotExists
     *            Is true, will only execute if the extension does not already
     *            exist.
     */
    public boolean newRefsetExtension(int refsetId, int conceptId, int memberTypeId, boolean checkNotExists)
            throws Exception;

    public UUID generateUuid(UUID uuid, UUID uuid2, UUID uuid3);

    public boolean newConceptConceptRefsetExtension(int refsetId, int componentId, int c1Id, int c2Id, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception;

    public boolean newStringRefsetExtension(int refsetId, int componentId, String extString, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception;

    public boolean newConceptStringRefsetExtension(int refsetId, int componentId, int c1Id, String extString,
            UUID memberUuid, UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception;

    public boolean newIntRefsetExtension(int refsetId, int componentId, int value, UUID memberUuid, UUID pathUuid,
            UUID statusUuid, long effectiveTime) throws Exception;

    public boolean newConceptRefsetExtension(int refsetId, int componentId, int conceptId, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception;

    public boolean newConceptConceptConceptRefsetExtension(int refsetId, int componentId, int c1Id, int c2Id, int c3Id,
            UUID memberUuid, UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception;

    public boolean newConceptConceptStringRefsetExtension(int refsetId, int componentId, int c1Id, int c2Id,
            String stringValue, UUID memberUuid, UUID pathUuid, UUID statusUuid, long effectiveTime) throws Exception;

    /**
     * Remove a concept from a refset
     * 
     * @param refsetId
     *            The subject refset
     * @param conceptId
     *            The concept to be removed
     * @param memberTypeId
     *            The value of the concept extension to be removed (the
     *            membership type).
     */
    public boolean retireRefsetExtension(int refsetId, int conceptId, int memberTypeId) throws Exception;

    /**
     * Get all the descendants (children, children of children, etc) of a
     * particular concept.
     */
    public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept, Condition... conditions) throws Exception;

    /**
     * Get all the descendants (children, children of children, etc) of a
     * particular concept.
     */
    public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept, I_GetConceptData memberRefset,
            Condition... conditions) throws Exception;

    /**
     * Get all the ancestors (parents, parents of parents, etc) of a particular
     * concept.
     */
    public Set<I_GetConceptData> getAllAncestors(I_GetConceptData concept, Condition... conditions) throws Exception;

    public int getConceptTypeId();

    public void setConceptTypeId(int conceptTypeId);

    public boolean newConceptExtensionPart(int refsetId, int componentId, int c1Id) throws Exception;

    public boolean newConceptExtensionPart(int refsetId, int componentId, int c1Id, int statusId) throws Exception;

    public boolean retireConceptExtension(int refsetId, int componentId) throws Exception;

    public List<I_GetConceptData> filterListByConceptType(Collection<? extends I_ExtendByRef> list,
            I_GetConceptData requiredPromotionStatusConcept) throws Exception;

    public I_GetConceptData newConcept(I_ConfigAceFrame aceConfig, I_GetConceptData status) throws Exception;

    public void newDescription(I_GetConceptData concept, I_GetConceptData descriptionType, String description,
            I_ConfigAceFrame aceConfig, I_GetConceptData status) throws Exception;

    public void newRelationship(I_GetConceptData concept, I_GetConceptData relationshipType,
            I_GetConceptData destination, I_ConfigAceFrame aceConfig, I_GetConceptData status) throws Exception;

    public Set<? extends I_GetConceptData> getAllValidUsers() throws Exception;

    public String getInbox(I_GetConceptData concept) throws Exception;

    public boolean hasConceptRefsetExtensionWithAnyPromotionStatus(int refsetId, int conceptId) throws IOException;

    public I_ExtendByRefPart getLatestPart(I_ExtendByRef memberExtension);

    int countMembersOfType(Collection<? extends I_ExtendByRef> allExtensions,
            I_GetConceptData requiredPromotionStatusConcept) throws Exception;
}