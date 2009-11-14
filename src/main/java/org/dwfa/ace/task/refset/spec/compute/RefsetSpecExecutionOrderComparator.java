package org.dwfa.ace.task.refset.spec.compute;

import java.util.Comparator;

public class RefsetSpecExecutionOrderComparator implements Comparator<RefsetSpecComponent> {

    public int compare(RefsetSpecComponent object1, RefsetSpecComponent object2) {

        if (object1 instanceof RefsetSpecQuery) {
            RefsetSpecQuery query1 = (RefsetSpecQuery) object1;
            switch (query1.getGroupingType()) {
            case AND:
                if (object2 instanceof RefsetSpecQuery) {
                    return 100;
                } else {
                    RefsetSpecStatement statement = (RefsetSpecStatement) object2;
                    boolean isNegated = statement.isNegated();
                    switch (statement.getTokenEnum()) {
                    case CONCEPT_IS:
                        if (isNegated) {
                            return 99;
                        } else {
                            return -100;
                        }
                    case CONCEPT_IS_CHILD_OF:
                    case CONCEPT_IS_KIND_OF:
                    case CONCEPT_IS_DESCENDENT_OF:
                        if (isNegated) {
                            return 100;
                        } else {
                            return 0;
                        }
                    case CONCEPT_IS_MEMBER_OF:
                        if (isNegated) {
                            return 80;
                        } else {
                            return -80;
                        }
                    default: // status, contains rel/desc grouping
                        return 100;
                    }
                }

            case OR:
                return 100;
            default:
                return 100;
            }
        }

        return 100;
    }
}
