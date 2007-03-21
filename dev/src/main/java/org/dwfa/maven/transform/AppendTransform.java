package org.dwfa.maven.transform;

import org.dwfa.maven.Transform;
import org.dwfa.maven.transform.*;

/**
 * Transforms ingredient field into a Snomed FSD.
 *
 */

public class AppendTransform extends AbstractTransform {
	
	String param;
	public String transform(String input) throws Exception {
		StringBuffer sb = new StringBuffer();

		//sb.append('"');
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			sb.append(c);
			if (c == '"') {
				sb.append('"');
			}	
		}
		sb.append(' ');
		sb.append(param);
		//sb.append('"');

		return setLastTransform(sb.toString());
	}
	public void setupImpl(Transform transformer) {
		
	}
}
