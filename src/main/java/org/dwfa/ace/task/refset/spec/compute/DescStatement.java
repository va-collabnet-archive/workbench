package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents partial information contained in a refset spec.
 * An example of a statement is : "NOT: Concept is : Paracetamol"
 * 
 * @author Chrissy Hill
 * 
 */
public class DescStatement extends RefsetSpecStatement {

    /**
     * Constructor for refset spec statement.
     * 
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     */
    public DescStatement(boolean useNotQualifier, I_GetConceptData queryToken, I_GetConceptData queryConstraint) {
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

        I_DescriptionVersioned descriptionVersioned = (I_DescriptionVersioned) component;
        I_DescriptionTuple descriptionTuple = descriptionVersioned.getLastTuple();

        switch (tokenEnum) {
        case DESC_IS:
            return descriptionIs(descriptionTuple);
        case DESC_IS_MEMBER_OF:
            return descriptionIsMemberOf(descriptionTuple);
        case DESC_STATUS_IS:
            return descriptionStatusIs(descriptionTuple);
        case DESC_STATUS_IS_CHILD_OF:
            return descriptionStatusIsChildOf(descriptionTuple);
        case DESC_STATUS_IS_KIND_OF:
            return descriptionStatusIsKindOf(descriptionTuple);
        case DESC_STATUS_IS_DESCENDENT_OF:
            return descriptionStatusIsDescendentOf(descriptionTuple);
        case DESC_TYPE_IS:
            return descriptionTypeIs(descriptionTuple);
        case DESC_TYPE_IS_CHILD_OF:
            return descriptionTypeIsChildOf(descriptionTuple);
        case DESC_TYPE_IS_KIND_OF:
            return descriptionTypeIsKindOf(descriptionTuple);
        case DESC_TYPE_IS_DESCENDENT_OF:
            return descriptionTypeIsDescendentOf(descriptionTuple);
        case DESC_REGEX_MATCH:
            return descriptionRegexMatch(descriptionTuple);
        case DESC_LUCENE_MATCH:
            return descriptionLuceneMatch(descriptionTuple);
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
    }

    @Override
    public I_RepresentIdSet getPossibleConcepts(I_ConfigAceFrame configFrame, I_RepresentIdSet parentPossibleConcepts)
            throws TerminologyException, IOException {
        I_RepresentIdSet possibleConcepts = termFactory.getEmptyIdSet();
        if (parentPossibleConcepts == null) {
            parentPossibleConcepts = termFactory.getConceptIdSet();
        }

        switch (tokenEnum) {
        case DESC_IS:
            possibleConcepts.or(parentPossibleConcepts);
            break;
        case DESC_IS_MEMBER_OF:
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
        case DESC_STATUS_IS:
        case DESC_STATUS_IS_CHILD_OF:
        case DESC_STATUS_IS_KIND_OF:
        case DESC_STATUS_IS_DESCENDENT_OF:
        case DESC_TYPE_IS:
        case DESC_TYPE_IS_CHILD_OF:
        case DESC_TYPE_IS_KIND_OF:
        case DESC_TYPE_IS_DESCENDENT_OF:
        case DESC_REGEX_MATCH:
        case DESC_LUCENE_MATCH:
            possibleConcepts.or(parentPossibleConcepts);
            break;
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        setPossibleConceptsCount(possibleConcepts.size());
        return possibleConcepts;
    }

    private boolean descriptionIsMemberOf(I_DescriptionTuple descriptionBeingTested) throws IOException,
            TerminologyException {
        return componentIsMemberOf(descriptionBeingTested.getDescId());
    }

    private boolean descriptionTypeIs(I_DescriptionTuple descriptionBeingTested) {
        return descriptionTypeIs((I_GetConceptData) queryConstraint, descriptionBeingTested);
    }

    private boolean descriptionTypeIs(I_GetConceptData requiredDescriptionType,
            I_DescriptionTuple descriptionBeingTested) {
        return descriptionBeingTested.getTypeId() == requiredDescriptionType.getConceptId();
    }

    /**
     * Checks if the description being tested has a description type matching the query constraint.
     * This also checks for the description type's children (depth >= 1);
     */
    private boolean descriptionTypeIsKindOf(I_DescriptionTuple descriptionBeingChecked) throws IOException,
            TerminologyException {

        if (descriptionTypeIs(descriptionBeingChecked)) {
            return true;
        }

        return descriptionTypeIsDescendentOf((I_GetConceptData) queryConstraint, descriptionBeingChecked);
    }

    /**
     * This checks the description type for depth >= 1.
     * 
     * @param requiredType
     * @param descriptionBeingChecked
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean descriptionTypeIsDescendentOf(I_GetConceptData requiredType,
            I_DescriptionTuple descriptionBeingChecked) throws IOException, TerminologyException {

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        // get list of all children of input concept
        Set<I_GetConceptData> childDescTypes = requiredType.getDestRelOrigins(null, allowedTypes, null, true, true);

        // call descriptionTypeIs on each
        for (I_GetConceptData childDescType : childDescTypes) {

            if (descriptionTypeIs(childDescType, descriptionBeingChecked)) {
                return true;
            } else if (descriptionTypeIsDescendentOf(childDescType, descriptionBeingChecked)) {
                return true;
            }
        }

        return false;
    }

    private boolean descriptionTypeIsDescendentOf(I_DescriptionTuple descriptionBeingChecked) throws IOException,
            TerminologyException {
        return descriptionTypeIsDescendentOf((I_GetConceptData) queryConstraint, descriptionBeingChecked);
    }

    private boolean descriptionTypeIsChildOf(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException,
            IOException {
        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        // get list of all children of input concept
        Set<I_GetConceptData> childDescTypes =
                ((I_GetConceptData) queryConstraint).getDestRelOrigins(null, allowedTypes, null, true, true);

        // call descriptionTypeIs on each
        for (I_GetConceptData childDescType : childDescTypes) {
            if (descriptionTypeIs(childDescType, descriptionBeingChecked)) {
                return true;
            }
        }

        return false;
    }

    private boolean descriptionStatusIs(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        return descriptionBeingChecked.getStatusId() == ((I_GetConceptData) queryConstraint).getConceptId();
    }

    private boolean descriptionStatusIs(I_GetConceptData requiredStatus, I_DescriptionTuple descriptionBeingChecked)
            throws TerminologyException {
        return descriptionBeingChecked.getStatusId() == requiredStatus.getConceptId();
    }

    private boolean descriptionStatusIsChildOf(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException,
            IOException {
        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        Set<I_GetConceptData> childStatuses =
                ((I_GetConceptData) queryConstraint).getDestRelOrigins(null, allowedTypes, null, true, true);

        for (I_GetConceptData childStatus : childStatuses) {
            if (descriptionStatusIs(childStatus, descriptionBeingChecked)) {
                return true;
            }
        }

        return false;
    }

    private boolean descriptionStatusIsDescendentOf(I_DescriptionTuple descriptionBeingChecked)
            throws TerminologyException, IOException {
        return descriptionStatusIsDescendentOf((I_GetConceptData) queryConstraint, descriptionBeingChecked);
    }

    private boolean descriptionStatusIsDescendentOf(I_GetConceptData requiredStatus,
            I_DescriptionTuple descriptionBeingChecked) throws TerminologyException, IOException {
        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        Set<I_GetConceptData> childStatuses = requiredStatus.getDestRelOrigins(null, allowedTypes, null, true, true);

        for (I_GetConceptData childStatus : childStatuses) {
            if (descriptionStatusIs(childStatus, descriptionBeingChecked)) {
                return true;
            } else if (descriptionStatusIsDescendentOf(childStatus, descriptionBeingChecked)) {
                return true;
            }
        }

        return false;
    }

    private boolean descriptionStatusIsKindOf(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException,
            IOException {
        if (descriptionStatusIs(descriptionBeingChecked)) {
            return true;
        }

        return descriptionStatusIsDescendentOf(descriptionBeingChecked);
    }

    // //////////////////////////////////////////////////////////////////////////////////

    private boolean descriptionIs(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        I_DescriptionTuple queryConstraintDesc = (I_DescriptionTuple) queryConstraint;
        return descriptionBeingChecked.equals(queryConstraintDesc); // TODO check
    }

    private boolean descriptionRegexMatch(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        throw new TerminologyException("Unimplemented"); // TODO
    }

    private boolean descriptionLuceneMatch(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        // String queryConstraintString = (String) queryConstraint;
        // Hits hits = termFactory.doLuceneSearch(queryConstraintString);
        // termFactory.doLuceneSearch(query)
        // termFactory.doLuceneSearch("");
        // termFactory.getActiveAceFrameConfig().performLuceneSearch(String
        // query, I_GetConceptData root);

        // termFactory.getActiveAceFrameConfig().performLuceneSearch(String
        // query, List<I_TestSearchResults> extraCriterion);
        throw new TerminologyException("Unimplemented"); // TODO
    }
}
