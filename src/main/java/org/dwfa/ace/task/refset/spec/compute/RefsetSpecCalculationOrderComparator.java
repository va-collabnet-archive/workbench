package org.dwfa.ace.task.refset.spec.compute;

import java.util.Comparator;

public class RefsetSpecCalculationOrderComparator implements Comparator<RefsetSpecComponent> {

    public int compare(RefsetSpecComponent object1, RefsetSpecComponent object2) {
        int object1Count = object1.getPossibleConceptsCount();
        int object2Count = object2.getPossibleConceptsCount();

        return object1Count - object2Count;
    }
}
