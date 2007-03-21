package org.dwfa.maven.transform;

import org.dwfa.maven.Transform;
import org.dwfa.maven.transform.*;

/**
 * Transforms ingredient field into a Snomed FSD.
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
