/**
 * 
 */
package org.dwfa.vodb.conflict;

import java.util.Comparator;

import org.dwfa.ace.api.I_AmPart;

class PartDateOrderSortComparator implements Comparator<I_AmPart> {
	private boolean reverseOrder = false;

	public int compare(I_AmPart o1, I_AmPart o2) {
		if (reverseOrder) {
			return o2.getVersion() - o1.getVersion();
		} else {
			return o1.getVersion() - o2.getVersion();
		}
	}

	public PartDateOrderSortComparator(boolean reverseOrder) {
		super();
		this.reverseOrder = reverseOrder;
	}
}