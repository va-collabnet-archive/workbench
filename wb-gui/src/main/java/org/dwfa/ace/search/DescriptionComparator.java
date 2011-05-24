package org.dwfa.ace.search;

import java.util.Comparator;

import org.dwfa.ace.api.I_DescriptionVersioned;

public class DescriptionComparator implements Comparator<I_DescriptionVersioned<?>> {

    @Override
    public int compare(I_DescriptionVersioned<?> o1, I_DescriptionVersioned<?> o2) {
        int comparison = o1.getMutableParts().get(0).getText().compareTo(o2.getMutableParts().get(0).getText());
        if (comparison == 0) {
            if (o1.getDescId() != o2.getDescId()) {
                if (o1.getDescId() > o2.getDescId()) {
                    comparison = 1;
                } else {
                    comparison = -1;
                }
            }
        }
        return comparison;
    }

}
