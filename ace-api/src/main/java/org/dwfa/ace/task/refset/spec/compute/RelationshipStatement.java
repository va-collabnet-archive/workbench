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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.spec.SpecRefsetHelper;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents partial information contained in a refset spec.
 * An example of a statement is : "NOT: Concept is : Paracetamol"
 * 
 * @author Chrissy Hill
 * 
 */
public class RelationshipStatement extends RefsetSpecStatement {

    /**
     * Constructor for refset spec statement.
     * 
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     */
    public RelationshipStatement(boolean useNotQualifier, I_GetConceptData queryToken, I_AmTermComponent queryConstraint) {
        super(useNotQualifier, queryToken, queryConstraint);
        for (QUERY_TOKENS token : QUERY_TOKENS.values()) {
            if (queryToken.getConceptId() == token.nid) {
                tokenEnum = token;
                break;
            }
        }

        if (tokenEnum == null) {
            throw new RuntimeException("Unknown query type : " + queryToken);
        }
    }

    public boolean getStatementResult(I_AmTermComponent component) throws IOException, TerminologyException {

        I_RelVersioned relVersioned = (I_RelVersioned) component;
        I_RelTuple relTuple = relVersioned.getLastTuple();
        switch (tokenEnum) {
        case REL_IS:
            return relIs(relTuple);
        case REL_RESTRICTION_IS:
            return relRestrictionIs(relTuple);
        case REL_IS_MEMBER_OF:
            return relIsMemberOf(relTuple);
        case REL_STATUS_IS:
            return relStatusIs(relTuple);
        case REL_STATUS_IS_KIND_OF:
            return relStatusIsKindOf(relTuple);
        case REL_STATUS_IS_CHILD_OF:
            return relStatusIsChildOf(relTuple);
        case REL_STATUS_IS_DESCENDENT_OF:
            return relStatusIsDescendentOf(relTuple);
        case REL_TYPE_IS:
            return relTypeIs(relTuple);
        case REL_TYPE_IS_KIND_OF:
            return relTypeIsKindOf(relTuple);
        case REL_TYPE_IS_CHILD_OF:
            return relTypeIsChildOf(relTuple);
        case REL_TYPE_IS_DESCENDENT_OF:
            return relTypeIsDescendentOf(relTuple);
        case REL_LOGICAL_QUANTIFIER_IS:
            return relLogicalQuantifierIs(relTuple);
        case REL_LOGICAL_QUANTIFIER_IS_KIND_OF:
            return relLogicalQuantifierIsKindOf(relTuple);
        case REL_LOGICAL_QUANTIFIER_IS_CHILD_OF:
            return relLogicalQuantifierIsChildOf(relTuple);
        case REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF:
            return relLogicalQuantifierIsDescendentOf(relTuple);
        case REL_CHARACTERISTIC_IS:
            return relCharIs(relTuple);
        case REL_CHARACTERISTIC_IS_KIND_OF:
            return relCharIsKindOf(relTuple);
        case REL_CHARACTERISTIC_IS_CHILD_OF:
            return relCharIsChildOf(relTuple);
        case REL_CHARACTERISTIC_IS_DESCENDENT_OF:
            return relCharIsDescendentOf(relTuple);
        case REL_REFINABILITY_IS:
            return relRefinabilityIs(relTuple);
        case REL_REFINABILITY_IS_KIND_OF:
            return relRefinabilityIsKindOf(relTuple);
        case REL_REFINABILITY_IS_CHILD_OF:
            return relRefinabilityIsChildOf(relTuple);
        case REL_REFINABILITY_IS_DESCENDENT_OF:
            return relRefinabilityIsDescendentOf(relTuple);
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
    }

    @Override
    public I_RepresentIdSet getPossibleConcepts(I_ConfigAceFrame configFrame, I_RepresentIdSet parentPossibleConcepts)
            throws TerminologyException, IOException {
        I_RepresentIdSet possibleConcepts = termFactory.getEmptyIdSet();
        if (parentPossibleConcepts == null) {
            parentPossibleConcepts = termFactory.getRelationshipIdSet();
        }

        switch (tokenEnum) {
        case REL_IS_MEMBER_OF:
            List<I_ThinExtByRefVersioned> refsetExtensions =
                    termFactory.getRefsetExtensionMembers(((I_GetConceptData) queryConstraint).getConceptId());
            Set<I_GetConceptData> refsetMembers = new HashSet<I_GetConceptData>();
            for (I_ThinExtByRefVersioned ext : refsetExtensions) {
                refsetMembers.add(termFactory.getConcept(ext.getComponentId()));
            }
            I_RepresentIdSet refsetMemberSet = termFactory.getIdSetfromTermCollection(refsetMembers);
            if (isNegated()) {
                possibleConcepts.or(parentPossibleConcepts);
                // possibleConcepts = termFactory.getConceptIdSet();
                // possibleConcepts.removeAll(refsetMemberSet);
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
        case REL_REFINABILITY_IS:
        case REL_REFINABILITY_IS_KIND_OF:
        case REL_REFINABILITY_IS_CHILD_OF:
        case REL_REFINABILITY_IS_DESCENDENT_OF:
            possibleConcepts.or(parentPossibleConcepts);
            break;
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        setPossibleConceptsCount(possibleConcepts.size());
        return possibleConcepts;
    }

    @Override
    public I_RepresentIdSet getPossibleDescriptions(I_ConfigAceFrame config, I_RepresentIdSet parentPossibleConcepts)
            throws TerminologyException, IOException {
        throw new TerminologyException("Get possible descriptions in rel statement unsupported operation.");
    }

    @Override
    public I_RepresentIdSet getPossibleRelationships(I_ConfigAceFrame configFrame,
            I_RepresentIdSet parentPossibleConcepts) throws TerminologyException, IOException {
        throw new TerminologyException("Get possible relationships in rel statement unsupported operation.");
    }

    private boolean relRefinabilityIsDescendentOf(I_RelTuple relTuple) throws TerminologyException, IOException {
        return relRefinabilityIsDescendentOf((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relRefinabilityIsDescendentOf(I_GetConceptData requiredRefinability, I_RelTuple relTuple)
            throws TerminologyException, IOException {
        try {
            I_IntSet allowedTypes = getIsAIds();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> children =
                    requiredRefinability.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

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
            I_IntSet allowedTypes = getIsAIds();

            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> children =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

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
        return relTuple.getRefinabilityId() == requiredRefinability.getConceptId();
    }

    private boolean relCharIsDescendentOf(I_RelTuple relTuple) throws IOException, TerminologyException {
        return relCharIsDescendentOf((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relCharIsDescendentOf(I_GetConceptData requiredCharType, I_RelTuple relTuple) throws IOException,
            TerminologyException {
        try {
            I_IntSet allowedTypes = getIsAIds();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> children =
                    requiredCharType.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

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
            I_IntSet allowedTypes = getIsAIds();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> children =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

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
        return relTuple.getCharacteristicId() == requiredCharType.getConceptId();
    }

    private boolean relIsMemberOf(I_RelTuple relTuple) throws IOException, TerminologyException {
        return componentIsMemberOf(relTuple.getRelId());
    }

    private boolean relTypeIs(I_RelTuple relTuple) {
        return relTypeIs((I_GetConceptData) queryConstraint, relTuple);
    }

    private boolean relTypeIs(I_GetConceptData requiredRelType, I_RelTuple relTuple) {
        return relTuple.getTypeId() == requiredRelType.getConceptId();
    }

    private boolean relTypeIsKindOf(I_RelTuple relTuple) throws IOException, TerminologyException {

        if (relTypeIs(relTuple)) {
            return true;
        }

        return relTypeIsDescendentOf(relTuple);
    }

    private boolean relTypeIsChildOf(I_RelTuple relTuple) throws TerminologyException, IOException {
        try {
            I_IntSet allowedTypes = getIsAIds();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> children =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

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
            I_IntSet allowedTypes = getIsAIds();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> children =
                    requiredRelType.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

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
            I_IntSet allowedTypes = getIsAIds();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> children =
                    requiredStatus.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

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
            I_IntSet allowedTypes = getIsAIds();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> children =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

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
        return relTuple.equals(queryConstraintRel); // TODO check
    }

    private boolean relRestrictionIs(I_RelTuple relTuple) throws IOException, TerminologyException {
        throw new TerminologyException("Unimplemented query : rel restriction is"); // unimplemented
        // TODO
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

}
