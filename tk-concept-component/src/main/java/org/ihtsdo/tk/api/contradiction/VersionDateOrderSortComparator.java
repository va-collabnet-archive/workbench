package org.ihtsdo.tk.api.contradiction;

import java.util.Comparator;

import org.ihtsdo.tk.api.ComponentVersionBI;

public class VersionDateOrderSortComparator implements Comparator<ComponentVersionBI> {
    private boolean reverseOrder = false;

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

    public VersionDateOrderSortComparator(boolean reverseOrder) {
        super();
        this.reverseOrder = reverseOrder;
    }
}
