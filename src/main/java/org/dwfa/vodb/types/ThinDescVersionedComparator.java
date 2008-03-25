package org.dwfa.vodb.types;

import java.util.Comparator;

import org.dwfa.ace.api.I_DescriptionVersioned;

public class ThinDescVersionedComparator implements Comparator<I_DescriptionVersioned> {

	public int compare(I_DescriptionVersioned o1, I_DescriptionVersioned o2) {
		return o1.getVersions().get(0).getText().compareTo(o2.getVersions().get(0).getText());
	}

}
