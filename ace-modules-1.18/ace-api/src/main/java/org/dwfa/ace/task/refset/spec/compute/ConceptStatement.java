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
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.spec.SpecRefsetHelper;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents partial information contained in a refset spec.
 * An example of a statement is : "NOT: Concept is : Paracetamol"
 * 
 * @author Chrissy Hill, Keith Campbell
 * 
 */
public class ConceptStatement extends RefsetSpecStatement {

    I_GetConceptData queryConstraintConcept;

    /**
     * Constructor for refset spec statement.
     * 
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     */
    public ConceptStatement(boolean useNotQualifier, I_GetConceptData queryToken, I_AmTermComponent queryConstraint) {
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
        queryConstraintConcept = (I_GetConceptData) queryConstraint;
    }

    @Override
    public I_RepresentIdSet getPossibleDescriptions(I_ConfigAceFrame config, I_RepresentIdSet parentPossibleConcepts)
            throws TerminologyException, IOException {
        throw new TerminologyException("Get possible descriptions in concept statement unsupported operation.");
    }

    @Override
    public I_RepresentIdSet getPossibleRelationships(I_ConfigAceFrame configFrame,
            I_RepresentIdSet parentPossibleConcepts) throws TerminologyException, IOException {
        throw new TerminologyException("Get possible relationships in concept statement unsupported operation.");
    }

    @Override
    public I_RepresentIdSet getPossibleConcepts(I_ConfigAceFrame configFrame, I_RepresentIdSet parentPossibleConcepts)
            throws TerminologyException, IOException {
        queryConstraint = (I_GetConceptData) queryConstraint;
        I_RepresentIdSet possibleConcepts = termFactory.getEmptyIdSet();
        if (parentPossibleConcepts == null) {
            parentPossibleConcepts = termFactory.getConceptIdSet();
        }

        switch (tokenEnum) {
        case CONCEPT_IS:
            if (isNegated()) {
                // possibleConcepts = termFactory.getConceptIdSet();
                possibleConcepts.or(parentPossibleConcepts);
                possibleConcepts.setNotMember(queryConstraintConcept.getConceptId());
            } else {
                possibleConcepts.setMember(queryConstraintConcept.getConceptId());
            }
            break;
        case CONCEPT_IS_CHILD_OF:
        case CONCEPT_IS_DESCENDENT_OF:
        case CONCEPT_IS_KIND_OF:
            I_RepresentIdSet results = queryConstraintConcept.getPossibleKindOfConcepts(configFrame);
            if (isNegated()) {
                // possibleConcepts = termFactory.getConceptIdSet();
                possibleConcepts.or(parentPossibleConcepts);
                // possibleConcepts.removeAll(results);
            } else {
                possibleConcepts.or(results);
            }
            break;
        case CONCEPT_IS_MEMBER_OF:
            List<I_ThinExtByRefVersioned> refsetExtensions =
                    termFactory.getRefsetExtensionMembers(queryConstraintConcept.getConceptId());
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
        case CONCEPT_STATUS_IS:
        case CONCEPT_STATUS_IS_CHILD_OF:
        case CONCEPT_STATUS_IS_DESCENDENT_OF:
        case CONCEPT_STATUS_IS_KIND_OF:
            // possibleConcepts = termFactory.getConceptIdSet();
            possibleConcepts.or(parentPossibleConcepts);
            break;
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        setPossibleConceptsCount(possibleConcepts.size());
        return possibleConcepts;
    }

    @Override
    public boolean getStatementResult(I_AmTermComponent component) throws TerminologyException, IOException {
        I_GetConceptData concept = (I_GetConceptData) component;

        switch (tokenEnum) {
        case CONCEPT_IS:
            return conceptIs(concept);
        case CONCEPT_IS_CHILD_OF:
            return conceptIsChildOf(concept);
        case CONCEPT_IS_DESCENDENT_OF:
            return conceptIsDescendantOf(concept);
        case CONCEPT_IS_KIND_OF:
            return conceptIsKindOf(concept);
        case CONCEPT_IS_MEMBER_OF:
            return conceptIsMemberOf(concept);
        case CONCEPT_STATUS_IS:
            return conceptStatusIs(concept);
        case CONCEPT_STATUS_IS_CHILD_OF:
            return conceptStatusIsChildOf(concept);
        case CONCEPT_STATUS_IS_DESCENDENT_OF:
            return conceptStatusIsDescendantOf(concept);
        case CONCEPT_STATUS_IS_KIND_OF:
            return conceptStatusIsKindOf(concept);
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
    }

    /**
     * Tests if the concept being tested is an immediate child of the query
     * constraint.
     * 
     * @param conceptBeingTested
     * @return
     * @throws TerminologyException
     * @throws IOException
     */
    private boolean conceptIsChildOf(I_GetConceptData conceptBeingTested) throws TerminologyException, IOException {
        try {
            I_IntSet allowedTypes = getIsAIds();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> children =
                    queryConstraintConcept.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

            for (I_GetConceptData child : children) {
                if (conceptBeingTested.equals(child)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    /**
     * Tests of the concept being tested is a member of the specified refset.
     * 
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptIsMemberOf(I_GetConceptData conceptBeingTested) throws IOException, TerminologyException {
        return componentIsMemberOf(conceptBeingTested.getConceptId());
    }

    /**
     * Tests of the current concept is the same as the query constraint.
     * 
     * @param concept
     * @return
     */
    private boolean conceptIs(I_GetConceptData conceptBeingTested) {
        return conceptBeingTested.equals(queryConstraint);
    }

    /**
     * Tests if the current concept is a child of the query constraint. This
     * does not return true if they are the same concept. This will check depth
     * >= 1 to find children.
     * 
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptIsDescendantOf(I_GetConceptData conceptBeingTested) throws IOException, TerminologyException {
        return queryConstraintConcept.isParentOf(conceptBeingTested, true);
    }

    /**
     * Tests if the current concept is a child of the query constraint. This
     * will return true if they are the same concept. This will check depth
     * >= 1 to find children.
     * 
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptIsKindOf(I_GetConceptData conceptBeingTested) throws IOException, TerminologyException {
        return queryConstraintConcept.isParentOfOrEqualTo(conceptBeingTested, true);
    }

    /**
     * Tests if the current concept has a status the same as the query
     * constraint.
     * 
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIs(I_GetConceptData conceptBeingTested) throws IOException, TerminologyException {
        return conceptStatusIs(conceptBeingTested, queryConstraintConcept);
    }

    /**
     * Tests if the current concept has a status matching the inputted status.
     * 
     * @param requiredStatusConcept
     * @param conceptBeingTested
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIs(I_GetConceptData conceptBeingTested, I_GetConceptData requiredStatusConcept)
            throws IOException, TerminologyException {

        List<? extends I_ConceptAttributeTuple> tuples =
                conceptBeingTested.getConceptAttributeTuples(null, termFactory.getActiveAceFrameConfig()
                    .getViewPositionSetReadOnly(), true, true);

        // get latest tuple
        I_ConceptAttributeTuple latestTuple = null;
        int latestTupleVersion = Integer.MIN_VALUE;
        for (I_ConceptAttributeTuple tuple : tuples) {
            if (tuple.getVersion() > latestTupleVersion) {
                latestTupleVersion = tuple.getVersion();
                latestTuple = tuple;
            }
        }

        if (latestTuple != null && latestTuple.getStatusId() == requiredStatusConcept.getConceptId()) {
            return true;
        }

        return false;
    }

    /**
     * Tests if the current concept has a status matching the query constraint,
     * or any of its children (depth >=1).
     * 
     * @param conceptBeingTested
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIsKindOf(I_GetConceptData conceptBeingTested) throws IOException, TerminologyException {

        // check if the concept's status matches the specified status
        if (conceptStatusIs(conceptBeingTested)) {
            return true;
        }

        return conceptStatusIsDescendantOf(conceptBeingTested);
    }

    /**
     * Tests if the current concept has a status matching the query constraint's
     * immediate children.
     * 
     * @param conceptBeingTested
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIsChildOf(I_GetConceptData conceptBeingTested) throws TerminologyException,
            IOException {

        try {
            I_IntSet allowedTypes = getIsAIds();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            // get list of all children of input concept
            Set<? extends I_GetConceptData> childStatuses =
                    queryConstraintConcept.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

            // call conceptStatusIs on each
            for (I_GetConceptData childStatus : childStatuses) {
                if (conceptStatusIs(conceptBeingTested, childStatus)) {
                    return true;
                }

            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    /**
     * Tests if the current concept has a status matching the query constraint's
     * children to depth >= 1.
     * 
     * @param conceptBeingTested
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIsDescendantOf(I_GetConceptData conceptBeingTested) throws IOException,
            TerminologyException {

        return conceptStatusIsDescendantOf(conceptBeingTested, queryConstraintConcept);
    }

    /**
     * Tests if the current concept has a status matching the specified status'
     * children to depth >= 1.
     * 
     * @param conceptBeingTested
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIsDescendantOf(I_GetConceptData conceptBeingTested, I_GetConceptData status)
            throws TerminologyException, IOException {

        try {
            I_IntSet allowedTypes = getIsAIds();
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

            Set<? extends I_GetConceptData> childStatuses =
                    status.getDestRelOrigins(currentStatuses, allowedTypes, termFactory.getActiveAceFrameConfig()
                        .getViewPositionSetReadOnly(), true, true);

            for (I_GetConceptData childStatus : childStatuses) {
                if (conceptStatusIs(conceptBeingTested, childStatus)) {
                    return true;
                } else if (conceptStatusIsDescendantOf(conceptBeingTested, childStatus)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

}
