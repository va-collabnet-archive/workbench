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
package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery.GROUPING_TYPE;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.time.TimeUtil;

/**
 * Represents partial information contained in a refset spec.
 * An example of a statement is : "NOT: Concept is : Paracetamol"
 * 
 * @author Chrissy Hill
 * 
 */
public class RelationshipStatement extends RefsetSpecStatement {

    private Collection<I_ShowActivity> activities;

    /**
     * Constructor for refset spec statement.
     * 
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     * @throws Exception
     */
    public RelationshipStatement(boolean useNotQualifier, I_GetConceptData queryToken, I_AmTermComponent queryConstraint,
            int refsetSpecNid, I_ConfigAceFrame config) throws Exception {
        super(useNotQualifier, queryToken, queryConstraint, refsetSpecNid, config);
        this.config = config;
        for (QUERY_TOKENS token : QUERY_TOKENS.values()) {
            if (queryToken.getConceptNid() == token.nid) {
                tokenEnum = token;
                break;
            }
        }

        if (tokenEnum == null) {
            throw new RuntimeException("Unknown query type : " + queryToken);
        }
    }

    public boolean getStatementResult(I_AmTermComponent component, GROUPING_TYPE version, I_Position v1_is, I_Position v2_is)
            throws IOException, TerminologyException {

        I_RelVersioned relVersioned = (I_RelVersioned) component;
        I_RelTuple relTuple = relVersioned.getLastTuple();

        if (version != null || v1_is != null || v2_is != null) {
            if (version == null)
                throw new TerminologyException("Not in scope of V1 or V2: " + tokenEnum + " " + relTuple);
            if (v1_is == null)
                throw new TerminologyException("Need to set V1 IS: " + tokenEnum + " " + relTuple);
            if (v2_is == null)
                throw new TerminologyException("Need to set V2 IS: " + tokenEnum + " " + relTuple);
        }
        switch (tokenEnum) {
        case REL_IS:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relIs(relTuple);
        case REL_RESTRICTION_IS:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relRestrictionIs(relTuple);
        case REL_IS_MEMBER_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relIsMemberOf(relTuple);
        case REL_STATUS_IS:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relStatusIs(relTuple);
        case REL_STATUS_IS_KIND_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relStatusIsKindOf(relTuple);
        case REL_STATUS_IS_CHILD_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relStatusIsChildOf(relTuple);
        case REL_STATUS_IS_DESCENDENT_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relStatusIsDescendentOf(relTuple);
        case REL_TYPE_IS:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relTypeIs(relTuple);
        case REL_TYPE_IS_KIND_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relTypeIsKindOf(relTuple);
        case REL_TYPE_IS_CHILD_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relTypeIsChildOf(relTuple);
        case REL_TYPE_IS_DESCENDENT_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relTypeIsDescendentOf(relTuple);
        case REL_LOGICAL_QUANTIFIER_IS:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relLogicalQuantifierIs(relTuple);
        case REL_LOGICAL_QUANTIFIER_IS_KIND_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relLogicalQuantifierIsKindOf(relTuple);
        case REL_LOGICAL_QUANTIFIER_IS_CHILD_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relLogicalQuantifierIsChildOf(relTuple);
        case REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relLogicalQuantifierIsDescendentOf(relTuple);
        case REL_CHARACTERISTIC_IS:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relCharIs(relTuple);
        case REL_CHARACTERISTIC_IS_KIND_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relCharIsKindOf(relTuple);
        case REL_CHARACTERISTIC_IS_CHILD_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relCharIsChildOf(relTuple);
        case REL_CHARACTERISTIC_IS_DESCENDENT_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relCharIsDescendentOf(relTuple);
        case REL_REFINABILITY_IS:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relRefinabilityIs(relTuple);
        case REL_REFINABILITY_IS_KIND_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relRefinabilityIsKindOf(relTuple);
        case REL_REFINABILITY_IS_CHILD_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relRefinabilityIsChildOf(relTuple);
        case REL_REFINABILITY_IS_DESCENDENT_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relRefinabilityIsDescendentOf(relTuple);
        case REL_DESTINATION_IS:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relDestinationIs(relTuple);
        case REL_DESTINATION_IS_KIND_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relDestinationIsKindOf(relTuple);
        case REL_DESTINATION_IS_CHILD_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relDestinationIsChildOf(relTuple);
        case REL_DESTINATION_IS_DESCENDENT_OF:
            if (version != null)
                throw new TerminologyException(tokenEnum + ": Unsupported operation for version scope.");
            return relDestinationIsDescendentOf(relTuple);
        case ADDED_RELATIONSHIP:
            return addedRelationship(relVersioned, version, v1_is, v2_is);
        case CHANGED_RELATIONSHIP_CHARACTERISTIC:
            return changedRelationshipCharacteristic(relVersioned, version, v1_is, v2_is);
        case CHANGED_RELATIONSHIP_GROUP:
            return changedRelationshipGroup(relVersioned, version, v1_is, v2_is);
        case CHANGED_RELATIONSHIP_REFINABILITY:
            return changedRelationshipRefinability(relVersioned, version, v1_is, v2_is);
        case CHANGED_RELATIONSHIP_STATUS:
            return changedRelationshipStatus(relVersioned, version, v1_is, v2_is);
        case CHANGED_RELATIONSHIP_TYPE:
            return changedRelationshipType(relVersioned, version, v1_is, v2_is);
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
    }

    @Override
    public I_RepresentIdSet getPossibleConcepts(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException {
        I_ShowActivity activity = null;
        long startTime = System.currentTimeMillis();
        this.activities = activities;

        I_RepresentIdSet possibleConcepts = termFactory.getEmptyIdSet();
        if (parentPossibleConcepts == null) {
            parentPossibleConcepts = termFactory.getConceptNidSet();
        }
        activity.setProgressInfoLower("Incoming count: " + parentPossibleConcepts.cardinality());

        switch (tokenEnum) {
        case REL_IS_MEMBER_OF:
            Collection<? extends I_ExtendByRef> refsetExtensions =
                    termFactory.getRefsetExtensionMembers(((I_GetConceptData) queryConstraint).getConceptNid());
            Set<I_GetConceptData> refsetMembers = new HashSet<I_GetConceptData>();
            for (I_ExtendByRef ext : refsetExtensions) {
                refsetMembers.add(termFactory.getConcept(ext.getComponentId()));
            }
            I_RepresentIdSet refsetMemberSet = termFactory.getIdSetfromTermCollection(refsetMembers);
            if (isNegated()) {
                possibleConcepts.or(parentPossibleConcepts);
            } else {
                possibleConcepts.or(refsetMemberSet);
            }

            break;
        case REL_IS:
        case REL_RESTRICTION_IS:
        case REL_STATUS_IS:
        case REL_STATUS_IS_KIND_OF:
        case REL_STATUS_IS_CHILD_OF:
        case REL_STATUS_IS_DESCENDENT_OF:
        case REL_TYPE_IS:
        case REL_TYPE_IS_KIND_OF:
        case REL_TYPE_IS_CHILD_OF:
        case REL_TYPE_IS_DESCENDENT_OF:
        case REL_LOGICAL_QUANTIFIER_IS:
        case REL_LOGICAL_QUANTIFIER_IS_KIND_OF:
        case REL_LOGICAL_QUANTIFIER_IS_CHILD_OF:
        case REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF:
        case REL_CHARACTERISTIC_IS:
        case REL_CHARACTERISTIC_IS_KIND_OF:
        case REL_CHARACTERISTIC_IS_CHILD_OF:
        case REL_CHARACTERISTIC_IS_DESCENDENT_OF:
        case REL_DESTINATION_IS:
        case REL_DESTINATION_IS_KIND_OF:
        case REL_DESTINATION_IS_CHILD_OF:
        case REL_DESTINATION_IS_DESCENDENT_OF:
        case REL_REFINABILITY_IS:
        case REL_REFINABILITY_IS_KIND_OF:
        case REL_REFINABILITY_IS_CHILD_OF:
        case REL_REFINABILITY_IS_DESCENDENT_OF:
            possibleConcepts.or(parentPossibleConcepts);
            break;
        case ADDED_RELATIONSHIP:
        case CHANGED_RELATIONSHIP_CHARACTERISTIC:
        case CHANGED_RELATIONSHIP_GROUP:
        case CHANGED_RELATIONSHIP_REFINABILITY:
        case CHANGED_RELATIONSHIP_STATUS:
        case CHANGED_RELATIONSHIP_TYPE:
            possibleConcepts.or(parentPossibleConcepts);
            break;
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        setPossibleConceptsCount(possibleConcepts.cardinality());

        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
        activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";  Incoming count: "
            + parentPossibleConcepts.cardinality() + "; Outgoing count: " + possibleConcepts.cardinality());
        activity.complete();
        return possibleConcepts;
    }

    @Override
    public I_RepresentIdSet getPossibleDescriptions(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException {
        throw new TerminologyException("Get possible descriptions in rel statement unsupported operation.");
    }

    @Override
    public I_RepresentIdSet getPossibleRelationships(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException {
        throw new TerminologyException("Get possible relationships in rel statement unsupported operation.");
    }

    private boolean relRefinabilityIsDescendentOf(I_RelTuple relTuple) throws TerminologyException, IOException {
        return relRefinabilityIsDescendentOf((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relRefinabilityIsDescendentOf(I_GetConceptData requiredRefinability, I_RelTuple relTuple)
            throws TerminologyException, IOException {

        try {

            Set<? extends I_GetConceptData> children =
                    requiredRefinability.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
                        .getConflictResolutionStrategy());

            for (I_GetConceptData child : children) {
                if (relRefinabilityIs(child, relTuple)) {
                    return true;
                } else if (relRefinabilityIsDescendentOf(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean relRefinabilityIsChildOf(I_RelTuple relTuple) throws TerminologyException, IOException {
        try {

            Set<? extends I_GetConceptData> children =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
                        .getConflictResolutionStrategy());

            for (I_GetConceptData child : children) {
                if (relRefinabilityIs(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean relRefinabilityIsKindOf(I_RelTuple relTuple) throws TerminologyException, IOException {
        if (relRefinabilityIs(relTuple)) {
            return true;
        }

        return relRefinabilityIsDescendentOf((I_GetConceptData) queryConstraint, relTuple);

    }

    private boolean relRefinabilityIs(I_RelTuple relTuple) {
        return relRefinabilityIs((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relRefinabilityIs(I_GetConceptData requiredRefinability, I_RelTuple relTuple) {
        return relTuple.getRefinabilityId() == requiredRefinability.getConceptNid();
    }

    private boolean relCharIsDescendentOf(I_RelTuple relTuple) throws IOException, TerminologyException {
        return relCharIsDescendentOf((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relCharIsDescendentOf(I_GetConceptData requiredCharType, I_RelTuple relTuple) throws IOException,
            TerminologyException {
        try {

            Set<? extends I_GetConceptData> children =
                    requiredCharType.getDestRelOrigins(currentStatuses, allowedTypes, termFactory.getActiveAceFrameConfig()
                        .getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_GetConceptData child : children) {
                if (relCharIs(child, relTuple)) {
                    return true;
                } else if (relCharIsDescendentOf(child, relTuple)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }

        return false;
    }

    private boolean relCharIsChildOf(I_RelTuple relTuple) throws TerminologyException, IOException {
        try {

            Set<? extends I_GetConceptData> children =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
                        .getConflictResolutionStrategy());

            for (I_GetConceptData child : children) {
                if (relCharIs(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean relCharIsKindOf(I_RelTuple relTuple) throws IOException, TerminologyException {

        if (relCharIs(relTuple)) {
            return true;
        }

        return relCharIsDescendentOf((I_GetConceptData) queryConstraint, relTuple);

    }

    private boolean relCharIs(I_RelTuple relTuple) {
        return relCharIs((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relCharIs(I_GetConceptData requiredCharType, I_RelTuple relTuple) {
        return relTuple.getCharacteristicId() == requiredCharType.getConceptNid();
    }

    private boolean relIsMemberOf(I_RelTuple relTuple) throws IOException, TerminologyException {
        return componentIsMemberOf(relTuple.getRelId());
    }

    private boolean relTypeIs(I_RelTuple relTuple) {
        return relTypeIs((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relTypeIs(I_GetConceptData requiredRelType, I_RelTuple relTuple) {
        return relTuple.getTypeId() == requiredRelType.getConceptNid();
    }

    private boolean relTypeIsKindOf(I_RelTuple relTuple) throws IOException, TerminologyException {

        if (relTypeIs(relTuple)) {
            return true;
        }

        return relTypeIsDescendentOf(relTuple);
    }

    private boolean relTypeIsChildOf(I_RelTuple relTuple) throws TerminologyException, IOException {
        try {

            Set<? extends I_GetConceptData> children =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
                        .getConflictResolutionStrategy());

            for (I_GetConceptData child : children) {
                if (relTypeIs(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean relTypeIsDescendentOf(I_RelTuple relTuple) throws IOException, TerminologyException {
        return relTypeIsDescendentOf((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relTypeIsDescendentOf(I_GetConceptData requiredRelType, I_RelTuple relTuple) throws IOException,
            TerminologyException {
        try {

            Set<? extends I_GetConceptData> children =
                    requiredRelType.getDestRelOrigins(currentStatuses, allowedTypes, termFactory.getActiveAceFrameConfig()
                        .getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_GetConceptData child : children) {
                if (relTypeIs(child, relTuple)) {
                    return true;
                } else if (relTypeIsDescendentOf(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean relStatusIsDescendentOf(I_RelTuple relTuple) throws TerminologyException, IOException {
        return relStatusIsDescendentOf((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relStatusIsDescendentOf(I_GetConceptData requiredStatus, I_RelTuple relTuple)
            throws TerminologyException, IOException {
        try {

            Set<? extends I_GetConceptData> children =
                    requiredStatus.getDestRelOrigins(currentStatuses, allowedTypes, termFactory.getActiveAceFrameConfig()
                        .getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_GetConceptData child : children) {
                if (relStatusIs(child, relTuple)) {
                    return true;
                } else if (relStatusIsDescendentOf(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean relStatusIsChildOf(I_RelTuple relTuple) throws IOException, TerminologyException {
        try {

            Set<? extends I_GetConceptData> children =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
                        .getConflictResolutionStrategy());

            for (I_GetConceptData child : children) {
                if (relStatusIs(child, relTuple)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }

        return false;
    }

    private boolean relStatusIsKindOf(I_RelTuple relTuple) throws TerminologyException, IOException {
        if (relStatusIs(relTuple)) {
            return true;
        }

        return relStatusIsDescendentOf(relTuple);
    }

    private boolean relStatusIs(I_RelTuple relTuple) {
        return relStatusIs((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relStatusIs(I_GetConceptData requiredStatus, I_RelTuple relTuple) {
        return componentStatusIs(requiredStatus, relTuple);
    }

    private boolean relIs(I_RelTuple relTuple) throws IOException, TerminologyException {
        I_RelTuple queryConstraintRel = (I_RelTuple) queryConstraint;
        return relTuple.equals(queryConstraintRel);
    }

    private boolean relRestrictionIs(I_RelTuple relTuple) throws IOException, TerminologyException {
        throw new TerminologyException("Unimplemented query : rel restriction is"); // TODO unimplemented
    }

    private boolean relLogicalQuantifierIsDescendentOf(I_RelTuple relTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : rel logical quantifier is descendent");
        // unimplemented TODO
    }

    private boolean relLogicalQuantifierIsChildOf(I_RelTuple relTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : rel logical quantifier is child of");
        // unimplemented TODO
    }

    private boolean relLogicalQuantifierIsKindOf(I_RelTuple relTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : rel logical quantifier is kind of");
        // unimplemented TODO
    }

    private boolean relLogicalQuantifierIs(I_RelTuple relTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : rel logical quantifier is");
        // unimplemented TODO
    }

    private boolean relDestinationIs(I_RelTuple relTuple) {
        return relDestinationIs((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relDestinationIs(I_GetConceptData requiredDestination, I_RelTuple relTuple) {
        return relTuple.getC2Id() == requiredDestination.getConceptNid();
    }

    private boolean relDestinationIsKindOf(I_RelTuple relTuple) throws IOException, TerminologyException {

        if (relDestinationIs(relTuple)) {
            return true;
        }

        return relDestinationIsDescendentOf(relTuple);
    }

    private boolean relDestinationIsChildOf(I_RelTuple relTuple) throws TerminologyException, IOException {
        try {

            Set<? extends I_GetConceptData> children =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
                        .getConflictResolutionStrategy());

            for (I_GetConceptData child : children) {
                if (relDestinationIs(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean relDestinationIsDescendentOf(I_RelTuple relTuple) throws IOException, TerminologyException {
        return relDestinationIsDescendentOf((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relDestinationIsDescendentOf(I_GetConceptData requiredDestination, I_RelTuple relTuple)
            throws IOException, TerminologyException {
        try {

            Set<? extends I_GetConceptData> children =
                    requiredDestination.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
                        .getConflictResolutionStrategy());

            for (I_GetConceptData child : children) {
                if (relDestinationIs(child, relTuple)) {
                    return true;
                } else if (relDestinationIsDescendentOf(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private I_RelPart getVersion(I_RelVersioned descriptionBeingTested, I_Position vn_is) throws TerminologyException {
        ArrayList<I_AmPart> parts = new ArrayList<I_AmPart>(descriptionBeingTested.getMutableParts());
        I_AmPart part = getVersion(parts, vn_is, false);
        return (I_RelPart) part;
    }

    private boolean addedRelationship(I_RelVersioned relBeingTested, GROUPING_TYPE version, I_Position v1_is,
            I_Position v2_is) throws TerminologyException, IOException {
        try {
            I_RelPart a1 = getVersion(relBeingTested, v1_is);
            I_RelPart a2 = getVersion(relBeingTested, v2_is);
            return (a1 == null && a2 != null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean changedRelationshipCharacteristic(I_RelVersioned relBeingTested, GROUPING_TYPE version,
            I_Position v1_is, I_Position v2_is) throws TerminologyException, IOException {
        try {
            I_RelPart a1 = getVersion(relBeingTested, v1_is);
            I_RelPart a2 = getVersion(relBeingTested, v2_is);
            return (a1 != null && a2 != null && a1.getVersion() != a2.getVersion() && a1.getCharacteristicId() != a2
                .getCharacteristicId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean changedRelationshipGroup(I_RelVersioned relBeingTested, GROUPING_TYPE version, I_Position v1_is,
            I_Position v2_is) throws TerminologyException, IOException {
        try {
            I_RelPart a1 = getVersion(relBeingTested, v1_is);
            I_RelPart a2 = getVersion(relBeingTested, v2_is);
            return (a1 != null && a2 != null && a1.getVersion() != a2.getVersion() && a1.getGroup() != a2.getGroup());
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean changedRelationshipRefinability(I_RelVersioned relBeingTested, GROUPING_TYPE version, I_Position v1_is,
            I_Position v2_is) throws TerminologyException, IOException {
        try {
            I_RelPart a1 = getVersion(relBeingTested, v1_is);
            I_RelPart a2 = getVersion(relBeingTested, v2_is);
            return (a1 != null && a2 != null && a1.getVersion() != a2.getVersion() && a1.getRefinabilityId() != a2
                .getRefinabilityId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean changedRelationshipStatus(I_RelVersioned relBeingTested, GROUPING_TYPE version, I_Position v1_is,
            I_Position v2_is) throws TerminologyException, IOException {
        try {
            I_RelPart a1 = getVersion(relBeingTested, v1_is);
            I_RelPart a2 = getVersion(relBeingTested, v2_is);
            return (a1 != null && a2 != null && a1.getVersion() != a2.getVersion() && a1.getStatusId() != a2.getStatusId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean changedRelationshipType(I_RelVersioned relBeingTested, GROUPING_TYPE version, I_Position v1_is,
            I_Position v2_is) throws TerminologyException, IOException {
        try {
            I_RelPart a1 = getVersion(relBeingTested, v1_is);
            I_RelPart a2 = getVersion(relBeingTested, v2_is);
            return (a1 != null && a2 != null && a1.getVersion() != a2.getVersion() && a1.getTypeId() != a2.getTypeId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

}
