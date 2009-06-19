package org.dwfa.mojo.comparator;

import org.dwfa.ace.api.I_DescriptionTuple;

import java.util.Comparator;

/**
 * This comparator sorts a list of tuples, firstly by concept id (arbitrary value,
 * but can be used to group versions) and then by description version
 */
public class TupleComparator implements Comparator {

    public int compare(Object o, Object o1) {

        I_DescriptionTuple t1 = (I_DescriptionTuple) o;
        I_DescriptionTuple t2 = (I_DescriptionTuple) o1;

        if (t1.getDescId() < t2.getDescId()) {
            return -1;
        } else if (t1.getDescId() > t2.getDescId()) {
            return 1;
        }
        return Integer.valueOf(t1.getVersion()).
                compareTo(Integer.valueOf(t2.getVersion()));
    }    
}
