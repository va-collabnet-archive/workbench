/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
/**
 * 
 */
package org.ihtsdo.tk.api.contradiction;

import java.util.Comparator;
import org.ihtsdo.tk.api.ComponentVersionBI;

// TODO: Auto-generated Javadoc
/**
 * The Class PartDateOrderSortComparator.
 */
class PartDateOrderSortComparator implements Comparator<ComponentVersionBI> {
    
    /** The reverse order. */
    private boolean reverseOrder = false;

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
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
     * Instantiates a new part date order sort comparator.
     *
     * @param reverseOrder the reverse order
     */
    public PartDateOrderSortComparator(boolean reverseOrder) {
        super();
        this.reverseOrder = reverseOrder;
    }
}
