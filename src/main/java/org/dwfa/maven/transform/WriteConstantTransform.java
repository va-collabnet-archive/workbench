package org.dwfa.maven.transform;

import org.dwfa.maven.Transform;

/**
 * Writes a constant string value
 *
 */

public class WriteConstantTransform extends AbstractTransform {
	
	String param;
	public String transform(String input) throws Exception {
		return setLastTransform(param);
	}
	public void setupImpl(Transform transformer) {
		
	}
}
