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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
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
    public DescStatement(boolean useNotQualifier, I_GetConceptData queryType, I_GetConceptData queryDestination,
            HashSet<Integer> allConcepts) {
        super(useNotQualifier, queryType, queryDestination, allConcepts);
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

    private boolean descriptionTypeIsChildOf(I_DescriptionTuple descriptionTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : desc type is child of");
    }

    private boolean descriptionStatusIsDescendentOf(I_DescriptionTuple descriptionTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : desc status is descendent of");
    }

    private boolean descriptionStatusIsKindOf(I_DescriptionTuple descriptionTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : desc status is kind of");
    }

    private boolean descriptionStatusIsChildOf(I_DescriptionTuple descriptionTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : desc status is child of");
    }

    private boolean descriptionStatusIs(I_DescriptionTuple descriptionTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : desc status is");
    }

    private boolean descriptionIs(I_DescriptionTuple descriptionTuple) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : desc is"); // unimplemented
    }

    private boolean descriptionIsMemberOf(I_DescriptionTuple descriptionBeingTested) throws IOException,
            TerminologyException {

        // get all extensions for this component
        List<I_ThinExtByRefVersioned> extensions =
                termFactory.getAllExtensionsForComponent(descriptionBeingTested.getDescId());

        for (I_ThinExtByRefVersioned ext : extensions) {
            if (ext.getRefsetId() == queryConstraint.getConceptId()) {
                // check they are of the specified refset

                List<? extends I_ThinExtByRefPart> parts = ext.getVersions();

                I_ThinExtByRefPart latestPart = null;
                int latestPartVersion = Integer.MIN_VALUE;

                // get latest part & check that it is current
                for (I_ThinExtByRefPart part : parts) {
                    if (part.getVersion() > latestPartVersion) {
                        latestPartVersion = part.getVersion();
                        latestPart = part;
                    }
                }

                if (latestPart.getStatusId() == termFactory
                    .getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean descriptionTypeIs(I_DescriptionTuple descriptionBeingTested) {
        return descriptionTypeIs(queryConstraint, descriptionBeingTested);
    }

    private boolean descriptionTypeIs(I_GetConceptData requiredDescriptionType,
            I_DescriptionTuple descriptionBeingTested) {
        return descriptionBeingTested.getTypeId() == requiredDescriptionType.getConceptId();
    }

    // //////////////////////////////////////////////////////////////////////////////////

    /**
     * Checks if the description being tested has a description type matching the query constraint.
     * This also checks for the description type's children (depth >= 1);
     */
    private boolean descriptionTypeIsKindOf(I_DescriptionTuple descriptionBeingChecked) throws IOException,
            TerminologyException {

        if (descriptionTypeIs(descriptionBeingChecked)) {
            return true;
        }

        return descriptionTypeIsDescendentOf(descriptionBeingChecked);
    }

    // TODO
    private boolean descriptionTypeIsDescendentOf(I_DescriptionTuple descriptionBeingChecked) throws IOException,
            TerminologyException {

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        // get list of all children of input concept
        Set<I_GetConceptData> childDescTypes =
                queryConstraint.getDestRelOrigins(termFactory.getActiveAceFrameConfig().getAllowedStatus(),
                    allowedTypes, null, true, true);

        // call descriptionTypeIs on each
        for (I_GetConceptData childDescType : childDescTypes) {
            if (descriptionTypeIs(childDescType, descriptionBeingChecked)) {
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
