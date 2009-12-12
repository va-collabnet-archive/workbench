/**
 * 
 */
package org.ihtsdo.db.bdb.concept;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.vodb.types.IntSet;

public class PossibleOriginNidTypeNidArray {
	int[] originNids;
	int[] typeNids;
	
	public int[] getPossibleOriginNids() {
		return originNids;
	}

	public int[] getPossibleOriginNidsForType(IntSet types) {
		ArrayIntList results = new ArrayIntList();
		for (int i = 0; i < originNids.length; i++) {
			if (types.contains(typeNids[i])) {
				results.add(originNids[i]);
			}
		}
		return results.toArray();
	}

	
}