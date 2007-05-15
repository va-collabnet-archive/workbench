package org.dwfa.maven.transform;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public class IdentityTransformWithQuoteDelimiters extends AbstractTransform implements I_ReadAndTransform {

	public void setupImpl(Transform transformer) {
		// Nothing to setup
	}

	public String transform(String input) throws Exception {

		if (input.contains("\"") || input.contains(",")) {
			StringBuffer sb = new StringBuffer();
			sb.append('"');
			for (int i = 0; i < input.length(); i++) {
				char c = input.charAt(i);
				sb.append(c);
			}
			sb.append('"');
			return setLastTransform(sb.toString());
		}
		return setLastTransform(input);
	}

}
