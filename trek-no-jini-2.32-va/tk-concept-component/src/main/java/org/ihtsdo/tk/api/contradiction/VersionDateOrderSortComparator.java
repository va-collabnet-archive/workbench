/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.api.contradiction;

import java.util.Comparator;

import org.ihtsdo.tk.api.ComponentVersionBI;

/**
 * The Class VersionDateOrderSortComparator compares component versions according
 * to the commit time and date.
 */
public class VersionDateOrderSortComparator implements Comparator<ComponentVersionBI> {

    private boolean reverseOrder = false;

    /**
     * Compares the given component versions according to their associated
     * commit times.
     *
     * @param componentVersion1 the first version to compare
     * @param componentVersion2 the second version to compare
     * @return 0 if the times are equal, 1 if version1 is greater than version2,
     * -1 if version2 is greater than version1. The opposite applies if using
     * reverse order.
     */
    @Override
    public int compare(ComponentVersionBI componentVersion1, ComponentVersionBI componentVersion2) {
        if (reverseOrder) {
            if (componentVersion2.getTime() - componentVersion1.getTime() > 0) {
                return 1;
            } else if (componentVersion2.getTime() - componentVersion1.getTime() < 0) {
                return -1;
            }
            return 0;
        } else {
            if (componentVersion2.getTime() - componentVersion1.getTime() > 0) {
                return -1;
            } else if (componentVersion2.getTime() - componentVersion1.getTime() < 0) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * Instantiates a new version date order sort comparator.
     *
     * @param reverseOrder set to <code>true</code> to use reverse date order
     * with oldest versions returned first
     */
    public VersionDateOrderSortComparator(boolean reverseOrder) {
        super();
        this.reverseOrder = reverseOrder;
    }
}
