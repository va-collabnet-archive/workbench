package org.dwfa.ace.task.refset.spec.compute;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecStatement.QUERY_TOKENS;

public enum RefsetComputeType {

    CONCEPT,
    DESCRIPTION,
    RELATIONSHIP;

    private RefsetComputeType() {
    }

    public static RefsetComputeType getTypeFromGroupingToken(I_GetConceptData groupingToken) {
        QUERY_TOKENS tokenEnum = null;
        for (QUERY_TOKENS token : QUERY_TOKENS.values()) {
            if (groupingToken.getConceptId() == token.nid) {
                tokenEnum = token;
                break;
            }
        }
        if (tokenEnum == null) {
            throw new RuntimeException("Unknown query type : " + groupingToken);
        }
        switch (tokenEnum) {
        case CONCEPT_IS:
        case CONCEPT_IS_CHILD_OF:
        case CONCEPT_IS_DESCENDENT_OF:
        case CONCEPT_IS_KIND_OF:
        case CONCEPT_IS_MEMBER_OF:
        case CONCEPT_STATUS_IS:
        case CONCEPT_STATUS_IS_CHILD_OF:
        case CONCEPT_STATUS_IS_DESCENDENT_OF:
        case CONCEPT_STATUS_IS_KIND_OF:
            return CONCEPT;
        case DESC_IS:
        case DESC_IS_MEMBER_OF:
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
            return DESCRIPTION;
        case REL_IS_MEMBER_OF:
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
            return RELATIONSHIP;
        default:
            throw new RuntimeException("Can't handle queryToken: " + groupingToken);
        }
    }

};
