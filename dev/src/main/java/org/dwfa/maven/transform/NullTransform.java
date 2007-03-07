package org.dwfa.maven.transform;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public class NullTransform extends AbstractTransform implements I_ReadAndTransform {

	public void setup(Transform transformer) {
	}

	public String transform(String input) throws Exception {
		return null;
	}

}
