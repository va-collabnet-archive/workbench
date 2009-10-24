package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents partial information contained in a refset spec.
 * An example of a statement is : "NOT: Concept is : Paracetamol"
 * 
 * @author Chrissy Hill
 * 
 */
public class ConceptStatement extends RefsetSpecStatement {

    /**
     * Constructor for refset spec statement.
     * 
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     */
    public ConceptStatement(boolean useNotQualifier, I_GetConceptData queryToken, I_GetConceptData queryConstraint) {
        super(useNotQualifier, queryToken, queryConstraint);
    }

    @Override
    public boolean getStatementResult(I_AmTermComponent component) throws TerminologyException, IOException {
        I_GetConceptData concept = (I_GetConceptData) component;

        if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS.getUids()))) {
            return conceptIs(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS_CHILD_OF.getUids()))) {
            return conceptIsChildOf(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS_KIND_OF.getUids()))) {
            return conceptIsKindOf(concept);
        } else if (queryToken
            .equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS_DESCENDENT_OF.getUids()))) {
            return conceptIsDescendantOf(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS_MEMBER_OF.getUids()))) {
            return conceptIsMemberOf(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_STATUS_IS.getUids()))) {
            return conceptStatusIs(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_STATUS_IS_CHILD_OF
            .getUids()))) {
            return conceptStatusIsChildOf(concept);
        } else if (queryToken.equals(termFactory
            .getConcept(RefsetAuxiliary.Concept.CONCEPT_STATUS_IS_KIND_OF.getUids()))) {
            return conceptStatusIsKindOf(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_STATUS_IS_DESCENDENT_OF
            .getUids()))) {
            return conceptStatusIsDescendantOf(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING
            .getUids()))) {
            return conceptContainsRelGrouping(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING
            .getUids()))) {
            return conceptContainsDescGrouping(concept);
        } else {
            throw new TerminologyException("Unknown query type : " + queryToken.getInitialText());
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
        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        Set<I_GetConceptData> children = queryConstraint.getDestRelOrigins(null, allowedTypes, null, true, true);

        for (I_GetConceptData child : children) {
            if (conceptBeingTested.equals(child)) {
                return true;
            }
        }
        return false;
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

        // get all extensions for this concept
        List<I_ThinExtByRefVersioned> extensions =
                termFactory.getAllExtensionsForComponent(conceptBeingTested.getConceptId());

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

    private boolean conceptContainsRelGrouping(I_GetConceptData conceptBeingTested) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : contains rel grouping"); // unimplemented
    }

    private boolean conceptContainsDescGrouping(I_GetConceptData conceptBeingTested) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : contains desc grouping"); // unimplemented
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
        return queryConstraint.isParentOf(conceptBeingTested, true);
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
        return queryConstraint.isParentOfOrEqualTo(conceptBeingTested, true);
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
        return conceptStatusIs(conceptBeingTested, queryConstraint);
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
        List<I_ConceptAttributeTuple> tuples = conceptBeingTested.getConceptAttributeTuples(null, null, true, true);

        // get latest tuple
        I_ConceptAttributeTuple latestTuple = null;
        int latestTupleVersion = Integer.MIN_VALUE;
        for (I_ConceptAttributeTuple tuple : tuples) {
            if (tuple.getVersion() > latestTupleVersion) {
                latestTupleVersion = tuple.getVersion();
                latestTuple = tuple;
            }
        }

        if (latestTuple != null && latestTuple.getConceptStatus() == requiredStatusConcept.getConceptId()) {
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
    private boolean conceptStatusIsChildOf(I_GetConceptData conceptBeingTested) throws IOException,
            TerminologyException {

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        // get list of all children of input concept
        Set<I_GetConceptData> childStatuses = queryConstraint.getDestRelOrigins(null, allowedTypes, null, true, true);

        // call conceptStatusIs on each
        for (I_GetConceptData childStatus : childStatuses) {
            if (conceptStatusIs(conceptBeingTested, childStatus)) {
                return true;
            }

        }

        return false;
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

        return conceptStatusIsDescendantOf(conceptBeingTested, queryConstraint);
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
            throws IOException, TerminologyException {

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        Set<I_GetConceptData> childStatuses = status.getDestRelOrigins(null, allowedTypes, null, true, true);

        for (I_GetConceptData childStatus : childStatuses) {
            if (conceptStatusIs(conceptBeingTested, childStatus)) {
                return true;
            } else if (conceptStatusIsDescendantOf(conceptBeingTested, childStatus)) {
                return true;
            }
        }

        return false;
    }
}
