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
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS_MEMBER_OF.getUids()))) {
            return conceptIsMemberOf(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS_KIND_OF.getUids()))) {
            return conceptIsKindOf(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_STATUS_IS.getUids()))) {
            return conceptStatusIs(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_STATUS_IS_KIND_OF.getUids()))) {
            return conceptStatusIsKindOf(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS_CHILD_OF.getUids()))) {
            return conceptIsChildOf(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING.getUids()))) {
            return conceptContainsRelGrouping(concept);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING.getUids()))) {
            return conceptContainsDescGrouping(concept);
        } else {
            throw new TerminologyException("Unknown query type : " + queryToken.getInitialText());
        }
    }

    /**
     * Tests of the current concept is a member of the specified refset.
     * 
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptIsMemberOf(I_GetConceptData concept) throws IOException, TerminologyException {

        // get all extensions for this concept
        List<I_ThinExtByRefVersioned> extensions = termFactory.getAllExtensionsForComponent(concept.getConceptId());

        for (I_ThinExtByRefVersioned ext : extensions) {
            if (ext.getRefsetId() == queryConstraint.getConceptId()) { // check
                                                                       // they
                                                                       // are of
                                                                       // the
                                                                       // specified
                                                                       // refset

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

                if (latestPart.getStatusId() == termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids())
                    .getConceptId()) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean conceptContainsRelGrouping(I_GetConceptData concept) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : contains rel grouping"); // unimplemented
    }

    private boolean conceptContainsDescGrouping(I_GetConceptData concept) throws TerminologyException {
        throw new TerminologyException("Unimplemented query : contains desc grouping"); // unimplemented
    }

    /**
     * Tests of the current concept is the same as the destination concept.
     * 
     * @param concept
     * @return
     */
    private boolean conceptIs(I_GetConceptData concept) {
        return concept.equals(queryConstraint);
    }

    /**
     * Tests if the current concept is a child of the destination concept. This
     * does not
     * return true if they are the same concept.
     * 
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptIsChildOf(I_GetConceptData concept) throws IOException, TerminologyException {
        return queryConstraint.isParentOf(concept, true);
    }

    /**
     * Tests if the current concept is a child of or the same as the destination
     * concept.
     * 
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptIsKindOf(I_GetConceptData concept) throws IOException, TerminologyException {
        return queryConstraint.isParentOfOrEqualTo(concept, true);
    }

    /**
     * Tests if the current concept has a status of the status represented by
     * the destination concept.
     * 
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIs(I_GetConceptData concept) throws IOException, TerminologyException {
        return conceptStatusIs(concept, queryConstraint);
    }

    /**
     * Tests if the current concept has a status of the status represented by
     * the destination concept.
     * 
     * @param statusConcept
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIs(I_GetConceptData concept, I_GetConceptData statusConcept) throws IOException,
            TerminologyException {
        List<I_ConceptAttributeTuple> tuples = concept.getConceptAttributeTuples(termFactory.getActiveAceFrameConfig()
            .getAllowedStatus(), null, true, true);

        // get latest tuple
        I_ConceptAttributeTuple latestTuple = null;
        int latestTupleVersion = Integer.MIN_VALUE;
        for (I_ConceptAttributeTuple tuple : tuples) {
            if (tuple.getVersion() > latestTupleVersion) {
                latestTupleVersion = tuple.getVersion();
                latestTuple = tuple;
            }
        }

        if (latestTuple != null && latestTuple.getConceptStatus() == statusConcept.getConceptId()) {
            return true;
        }

        return false;
    }

    /**
     * Tests if the current concept has a status of the status represented by
     * the destination concept,
     * or any of its children.
     * 
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIsKindOf(I_GetConceptData concept) throws IOException, TerminologyException {

        // check if the concept
        if (conceptStatusIs(concept)) {
            return true;
        }

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        // get list of all children of input concept
        Set<I_GetConceptData> childStatuses = concept.getDestRelOrigins(termFactory.getActiveAceFrameConfig()
            .getAllowedStatus(), allowedTypes, null, true, true);

        // call conceptStatusIs on each
        for (I_GetConceptData childStatus : childStatuses) {
            if (conceptStatusIs(concept, childStatus)) {
                return true;
            }
        }

        return false;
    }

}
