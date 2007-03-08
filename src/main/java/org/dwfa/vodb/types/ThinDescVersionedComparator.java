package org.dwfa.vodb.types;

import java.util.Comparator;

public class ThinDescVersionedComparator implements Comparator<ThinDescVersioned> {

	public int compare(ThinDescVersioned o1, ThinDescVersioned o2) {
		return o1.getVersions().get(0).getText().compareTo(o2.getVersions().get(0).getText());
	}

}
