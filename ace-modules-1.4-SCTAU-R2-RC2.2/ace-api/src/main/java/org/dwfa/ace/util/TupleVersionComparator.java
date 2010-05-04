package org.dwfa.ace.util;

import java.util.Comparator;

import org.dwfa.ace.api.I_AmTuple;

/**
 * Compares Tuples by there version
 */
public  class TupleVersionComparator implements Comparator<I_AmTuple> {
    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(I_AmTuple o1, I_AmTuple o2) {
        return (int) Math.signum(o1.getVersion() - o2.getVersion());
    }
}
