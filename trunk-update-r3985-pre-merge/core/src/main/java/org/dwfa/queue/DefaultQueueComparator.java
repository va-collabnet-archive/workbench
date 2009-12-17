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
/*
 * Created on Apr 21, 2005
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
