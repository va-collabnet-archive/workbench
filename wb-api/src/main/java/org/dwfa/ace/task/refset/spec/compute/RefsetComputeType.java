package org.dwfa.ace.task.refset.spec.compute;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery.GROUPING_TYPE;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecStatement.QUERY_TOKENS;
import org.dwfa.cement.RefsetAuxiliary;

public enum RefsetComputeType {

	CONCEPT, DESCRIPTION, RELATIONSHIP;
	// DIFF;

	private RefsetComputeType() {
	}

	public static RefsetComputeType getTypeFromGrouping(
			I_GetConceptData groupingToken) {
		GROUPING_TYPE grouping = null;
		for (GROUPING_TYPE token : GROUPING_TYPE.values()) {
			if (groupingToken.getConceptId() == token.getNid()) {
				grouping = token;
				break;
			}
		}

		if (grouping == null) {
			throw new RuntimeException("Unknown query type : " + groupingToken);
		}
		switch (grouping) {
		case OR:
		case AND:
			return CONCEPT;
		case CONCEPT_CONTAINS_REL:
		case NOT_CONCEPT_CONTAINS_REL:
			return RELATIONSHIP;
		case NOT_CONCEPT_CONTAINS_DESC:
		case CONCEPT_CONTAINS_DESC:
			return DESCRIPTION;
		case V1:
		case V2:
			return CONCEPT;
		default:
			throw new RuntimeException("Can't handle queryToken: "
					+ groupingToken);
		}
	}

	public static RefsetComputeType getTypeFromQueryToken(
			I_GetConceptData groupingToken) {
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
		case REL_DESTINATION_IS:
		case REL_DESTINATION_IS_KIND_OF:
		case REL_DESTINATION_IS_CHILD_OF:
		case REL_DESTINATION_IS_DESCENDENT_OF:
			return RELATIONSHIP;
		case V1_IS:
		case V2_IS:
		case ADDED_CONCEPT:
		case CHANGED_CONCEPT_STATUS:
		case CHANGED_CONCEPT_DEFINED:
			return CONCEPT;
		case ADDED_DESCRIPTION:
		case CHANGED_DESCRIPTION_CASE:
		case CHANGED_DESCRIPTION_LANGUAGE:
		case CHANGED_DESCRIPTION_STATUS:
		case CHANGED_DESCRIPTION_TERM:
		case CHANGED_DESCRIPTION_TYPE:
			return DESCRIPTION;
		case ADDED_RELATIONSHIP:
		case CHANGED_RELATIONSHIP_CHARACTERISTIC:
		case CHANGED_RELATIONSHIP_GROUP:
		case CHANGED_RELATIONSHIP_REFINABILITY:
		case CHANGED_RELATIONSHIP_STATUS:
		case CHANGED_RELATIONSHIP_TYPE:
			return RELATIONSHIP;
		default:
			throw new RuntimeException("Can't handle queryToken: "
					+ groupingToken);
		}
	}

};
