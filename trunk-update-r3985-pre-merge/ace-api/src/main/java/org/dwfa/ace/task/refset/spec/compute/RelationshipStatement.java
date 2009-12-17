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
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
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
public class RelationshipStatement extends RefsetSpecStatement {

    /**
     * Constructor for refset spec statement.
     * 
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     */
    public RelationshipStatement(boolean useNotQualifier, I_GetConceptData queryToken, I_GetConceptData queryConstraint) {
        super(useNotQualifier, queryToken, queryConstraint);
    }

    public boolean execute(I_RelTuple relTuple) throws IOException, TerminologyException {

        return false;
    }

    public boolean getStatementResult(I_AmTermComponent component) throws IOException, TerminologyException {

        I_RelVersioned relVersioned = (I_RelVersioned) component;
        I_RelTuple relTuple = relVersioned.getLastTuple();

        if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_IS_MEMBER_OF.getUids()))) {
            return relationshipIsMemberOf(relTuple);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_STATUS_IS.getUids()))) {
            return componentStatusIs(relTuple);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_STATUS_IS_KIND_OF.getUids()))) {
            return componentStatusIsKindOf(relTuple);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_TYPE_IS.getUids()))) {
            return relationshipTypeIs(relTuple);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_TYPE_IS_KIND_OF.getUids()))) {
            return relationshipTypeIsKindOf(relTuple);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_LOGICAL_QUANTIFIER_IS.getUids()))) {
            return relationshipLogicalQuantifierIs(relTuple);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_LOGICAL_QUANTIFIER_IS_KIND_OF.getUids()))) {
            return relationshipLogicalQuantifierIsKindOf(relTuple);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_CHARACTERISTIC_IS.getUids()))) {
            return relationshipCharacteristicIs(relTuple);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_CHARACTERISTIC_IS_KIND_OF.getUids()))) {
            return relationshipCharacteristicIsKindOf(relTuple);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_REFINABILITY_IS.getUids()))) {
            return relationshipRefinabilityIs(relTuple);
        } else if (queryToken.equals(termFactory.getConcept(RefsetAuxiliary.Concept.REL_REFINABILITY_IS_KIND_OF.getUids()))) {
            return relationshipRefinabilityIsKindOf(relTuple);
        } else {
            throw new TerminologyException("Unknown desc query type : " + queryToken.getInitialText());
        }
    }

    private boolean relationshipIsMemberOf(I_RelTuple rel) throws IOException, TerminologyException {
        return componentIsMemberOf(rel.getRelId());
    }

    private boolean relationshipTypeIs(I_RelTuple rel) {
        return relationshipTypeIs(queryConstraint, rel);
    }

    private boolean relationshipTypeIs(I_GetConceptData relType, I_RelTuple rel) {
        return rel.getTypeId() == relType.getConceptId();
    }

    private boolean relationshipTypeIsKindOf(I_RelTuple rel) throws IOException, TerminologyException {

        if (relationshipTypeIs(rel)) {
            return true;
        }

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        // get list of all children of input concept
        Set<I_GetConceptData> childDescTypes = queryConstraint.getDestRelOrigins(termFactory.getActiveAceFrameConfig()
            .getAllowedStatus(), allowedTypes, null, true, true);

        // call relationshipTypeIs on each
        for (I_GetConceptData childDescType : childDescTypes) {
            if (relationshipTypeIs(childDescType, rel)) {
                return true;
            }
        }

        return false;
    }

    private boolean relationshipLogicalQuantifierIs(I_RelTuple rel) throws TerminologyException {
        throw new TerminologyException("Unimplemented.");
    }

    private boolean relationshipLogicalQuantifierIsKindOf(I_RelTuple rel) throws TerminologyException {
        throw new TerminologyException("Unimplemented.");
    }

    private boolean relationshipCharacteristicIs(I_RelTuple rel) {
        return relationshipCharacteristicIs(queryConstraint, rel);
    }

    private boolean relationshipCharacteristicIs(I_GetConceptData charType, I_RelTuple rel) {
        return rel.getCharacteristicId() == charType.getConceptId();
    }

    private boolean relationshipCharacteristicIsKindOf(I_RelTuple rel) throws TerminologyException, IOException {

        if (relationshipCharacteristicIs(rel)) {
            return true;
        }

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

        // get list of all children of input concept
        Set<I_GetConceptData> childDescTypes = queryConstraint.getDestRelOrigins(termFactory.getActiveAceFrameConfig()
            .getAllowedStatus(), allowedTypes, null, true, true);

        // call relationshipCharIs on each
        for (I_GetConceptData childDescType : childDescTypes) {
            if (relationshipCharacteristicIs(childDescType, rel)) {
                return true;
            }
        }

        return false;
    }

    private boolean relationshipRefinabilityIs(I_RelTuple rel) {
        return relationshipRefinabilityIs(queryConstraint, rel);
    }

    private boolean relationshipRefinabilityIs(I_GetConceptData refinability, I_RelTuple rel) {
        return rel.getRefinabilityId() == refinability.getConceptId();
    }

    private boolean relationshipRefinabilityIsKindOf(I_RelTuple rel) {
        return false;
    }
}
