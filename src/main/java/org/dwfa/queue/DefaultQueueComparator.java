/*
 * Created on Apr 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue;

import java.util.Comparator;

import org.dwfa.bpa.process.I_DescribeQueueEntry;

/**
 * @author kec
 *
 */
public class DefaultQueueComparator implements Comparator {

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2) {
        I_DescribeQueueEntry d1 = (I_DescribeQueueEntry) o1;
        I_DescribeQueueEntry d2 = (I_DescribeQueueEntry) o2;
        int compareValue = compareComparables(d1.getPriority(), d2.getPriority());
        if (compareValue != 0) {
            return compareValue;
        }
        compareValue = compareComparables(d1.getDeadline(), d2.getDeadline());
        if (compareValue != 0) {
            return compareValue;
        }
        compareValue = compareComparables(d1.getDeadline(), d2.getDeadline());
        if (compareValue != 0) {
            return compareValue;
        }
        compareValue = compareComparables(d1.getSubject(), d2.getSubject());
        if (compareValue != 0) {
            return compareValue;
        }
        compareValue = compareComparables(d1.getOriginator(), d2.getOriginator());
        if (compareValue != 0) {
            return compareValue;
        }
        compareValue = d1.getProcessID().toString().compareTo(d2.getProcessID().toString());
        if (compareValue != 0) {
            return compareValue;
        }
        return d1.getEntryID().toString().compareTo(d2.getEntryID().toString());
    }
    
    @SuppressWarnings("unchecked")
	private int compareComparables(Comparable c1, Comparable c2) {
        if (c1 == c2) {
            return 0;
        }
        if (c1 != null) {
            if (c2 != null) {
                return c1.compareTo(c2);
            }
            // c1 != null, c2 == null
            return -1;
        }
        // c1 == null, c2 != null
        return 1;
        
    }
    

}
