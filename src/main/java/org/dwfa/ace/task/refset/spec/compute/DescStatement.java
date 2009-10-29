package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
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
    public DescStatement(boolean useNotQualifier, I_GetConceptData queryType, I_GetConceptData queryDestination) {
        super(useNotQualifier, queryType, queryDestination);
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

    private boolean descriptionTypeIsDescendentOf(I_DescriptionTuple descriptionTuple) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean descriptionTypeIsChildOf(I_DescriptionTuple descriptionTuple) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean descriptionStatusIsDescendentOf(I_DescriptionTuple descriptionTuple) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean descriptionStatusIsKindOf(I_DescriptionTuple descriptionTuple) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean descriptionStatusIsChildOf(I_DescriptionTuple descriptionTuple) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean descriptionStatusIs(I_DescriptionTuple descriptionTuple) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean descriptionIs(I_DescriptionTuple descriptionTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : desc is"); // unimplemented
    }

    @Override
    public Set<Integer> getPossibleConcepts(I_ConfigAceFrame configFrame) throws TerminologyException, IOException {
        Set<Integer> possibleConcepts = new HashSet<Integer>();

        switch (tokenEnum) {
        case DESC_IS:
            break;
        case DESC_IS_MEMBER_OF:
            break;
        case DESC_STATUS_IS:
            break;
        case DESC_STATUS_IS_CHILD_OF:
            break;
        case DESC_STATUS_IS_KIND_OF:
            break;
        case DESC_STATUS_IS_DESCENDENT_OF:
            break;
        case DESC_TYPE_IS:
            break;
        case DESC_TYPE_IS_CHILD_OF:
            break;
        case DESC_TYPE_IS_KIND_OF:
            break;
        case DESC_TYPE_IS_DESCENDENT_OF:
            break;
        case DESC_REGEX_MATCH:
            break;
        case DESC_LUCENE_MATCH:
            break;
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }

        // TODO
        possibleConcepts.addAll(allConcepts);
        return possibleConcepts;
    }

    // //////////////////////////////////////////////////////////////////////////////////
    private boolean descriptionIsMemberOf(I_DescriptionTuple descriptionTuple) throws IOException, TerminologyException {
        return componentIsMemberOf(descriptionTuple.getDescId());
    }

    private boolean descriptionTypeIs(I_DescriptionTuple descriptionTuple) {
        return descriptionTypeIs(queryConstraint, descriptionTuple);
    }

    private boolean descriptionTypeIs(I_GetConceptData descriptionType, I_DescriptionTuple descriptionTuple) {
        return descriptionTuple.getTypeId() == descriptionType.getConceptId();
    }

    private boolean descriptionTypeIsKindOf(I_DescriptionTuple descriptionTuple) throws IOException,
            TerminologyException {

        if (descriptionTypeIs(descriptionTuple)) {
            return true;
        }

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        // get list of all children of input concept
        Set<I_GetConceptData> childDescTypes =
                queryConstraint.getDestRelOrigins(termFactory.getActiveAceFrameConfig().getAllowedStatus(),
                    allowedTypes, null, true, true);

        // call descriptionTypeIs on each
        for (I_GetConceptData childDescType : childDescTypes) {
            if (descriptionTypeIs(childDescType, descriptionTuple)) {
                return true;
            }
        }

        return false;
    }

    private boolean descriptionRegexMatch(I_DescriptionTuple descriptionTuple) throws TerminologyException {

        throw new TerminologyException("Unimplemented"); // TODO
    }

    private boolean descriptionLuceneMatch(I_DescriptionTuple descriptionTuple) throws TerminologyException {

        // termFactory.doLuceneSearch("");
        // termFactory.getActiveAceFrameConfig().performLuceneSearch(String
        // query, I_GetConceptData root);

        // termFactory.getActiveAceFrameConfig().performLuceneSearch(String
        // query, List<I_TestSearchResults> extraCriterion);
        throw new TerminologyException("Unimplemented"); // TODO
    }

}
