package org.dwfa.maven.transform;

import org.dwfa.maven.transform.*;
import org.dwfa.maven.*;

public class IdentityTransform extends AbstractTransform implements I_ReadAndTransform {

	public void setupImpl(Transform transformer) {
		// Nothing to setup
	}

	public String transform(String input) throws Exception {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			sb.append(c);
			if (c == '"') {
				sb.append('"');
			}	
		}
		return setLastTransform(sb.toString());
	}

}
