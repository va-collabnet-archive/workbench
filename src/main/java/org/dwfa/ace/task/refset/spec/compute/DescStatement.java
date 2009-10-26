package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents partial information contained in a refset spec.
 * An example of a statement is : "NOT: Concept is : Paracetamol"
 * @author Chrissy Hill
 *
 */
public class DescStatement extends RefsetSpecStatement {

    /**
     * Constructor for refset spec statement.
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     */
    public DescStatement(boolean useNotQualifier, I_GetConceptData queryType,
            I_GetConceptData queryDestination) {
        super(useNotQualifier, queryType, queryDestination);
    }

    public boolean getStatementResult(I_AmTermComponent component)
        throws IOException, TerminologyException {

        I_DescriptionVersioned descriptionVersioned =
            (I_DescriptionVersioned) component;
        I_DescriptionTuple descriptionTuple =
            descriptionVersioned.getLastTuple();

        if (queryToken.equals(termFactory.getConcept(
                RefsetAuxiliary.Concept.DESC_IS_MEMBER_OF.getUids()))) {
            return descriptionIsMemberOf(descriptionTuple);
        } else if (queryToken.equals(termFactory.getConcept(
                RefsetAuxiliary.Concept.DESC_STATUS_IS.getUids()))) {
            return componentStatusIs(descriptionTuple);
        } else if (queryToken.equals(termFactory.getConcept(
                RefsetAuxiliary.Concept.DESC_STATUS_IS_KIND_OF.getUids()))) {
            return componentStatusIsKindOf(descriptionTuple);
        } else if (queryToken.equals(termFactory.getConcept(
                RefsetAuxiliary.Concept.DESC_TYPE_IS.getUids()))) {
            return descriptionTypeIs(descriptionTuple);
        } else if (queryToken.equals(termFactory.getConcept(
                RefsetAuxiliary.Concept.DESC_TYPE_IS_KIND_OF.getUids()))) {
            return descriptionTypeIsKindOf(descriptionTuple);
        } else if (queryToken.equals(termFactory.getConcept(
                RefsetAuxiliary.Concept.DESC_LUCENE_MATCH.getUids()))) {
            return descriptionRegexMatch(descriptionTuple);
        } else if (queryToken.equals(termFactory.getConcept(
                RefsetAuxiliary.Concept.DESC_REGEX_MATCH.getUids()))) {
            return descriptionLuceneMatch(descriptionTuple);
        } else {
            throw new TerminologyException("Unknown desc query type : "
                    + queryToken.getInitialText());
        }
    }

    private boolean descriptionIsMemberOf(I_DescriptionTuple descriptionTuple)
        throws IOException, TerminologyException {
        return componentIsMemberOf(descriptionTuple.getDescId());
    }

    private boolean descriptionTypeIs(I_DescriptionTuple descriptionTuple) {
        return descriptionTypeIs(queryConstraint, descriptionTuple);
    }

    private boolean descriptionTypeIs(I_GetConceptData descriptionType,
            I_DescriptionTuple descriptionTuple) {
        return descriptionTuple.getTypeId() == descriptionType.getConceptId();
    }

    private boolean descriptionTypeIsKindOf(I_DescriptionTuple descriptionTuple) throws IOException, TerminologyException {

        if (descriptionTypeIs(descriptionTuple)) {
            return true;
        }

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(
                ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        // get list of all children of input concept
        Set<I_GetConceptData> childDescTypes = queryConstraint.getDestRelOrigins(
                termFactory.getActiveAceFrameConfig().getAllowedStatus(), allowedTypes, null, true, true);

        // call descriptionTypeIs on each
        for (I_GetConceptData childDescType : childDescTypes) {
            if (descriptionTypeIs(childDescType, descriptionTuple)) {
                return true;
            }
        }

        return false;
    }

    private boolean descriptionRegexMatch(I_DescriptionTuple descriptionTuple)
        throws TerminologyException {

        throw new TerminologyException("Unimplemented"); // TODO
    }

    private boolean descriptionLuceneMatch(I_DescriptionTuple descriptionTuple)
        throws TerminologyException {

        //termFactory.doLuceneSearch("");
        //termFactory.getActiveAceFrameConfig().performLuceneSearch(String query, I_GetConceptData root);

        //termFactory.getActiveAceFrameConfig().performLuceneSearch(String query, List<I_TestSearchResults> extraCriterion);
        throw new TerminologyException("Unimplemented"); // TODO
    }

	@Override
	public Set<Integer> getPossibleConcepts(I_ConfigAceFrame configFrame)
			throws TerminologyException, IOException {
		throw new UnsupportedOperationException();
	}


}
