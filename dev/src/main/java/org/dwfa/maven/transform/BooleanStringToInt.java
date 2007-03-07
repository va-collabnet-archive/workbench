package org.dwfa.maven.transform;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public class BooleanStringToInt extends AbstractTransform implements
		I_ReadAndTransform {

	public void setup(Transform transformer) {
		// Nothing to do...
	}

	public String transform(String input) throws Exception {
		if (input.toLowerCase().startsWith("t")) {
			return setLastTransform("1");
		}
		return setLastTransform("0");
	}

}
