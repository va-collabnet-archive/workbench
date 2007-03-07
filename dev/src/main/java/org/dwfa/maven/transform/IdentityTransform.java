package org.dwfa.maven.transform;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public class IdentityTransform extends AbstractTransform implements I_ReadAndTransform {

	public void setup(Transform transformer) {
		// Nothing to setup
	}

	public String transform(String input) throws Exception {
		return setLastTransform(input);
	}

}
