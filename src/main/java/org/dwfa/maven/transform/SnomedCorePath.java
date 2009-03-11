package org.dwfa.maven.transform;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.Transform;

public class SnomedCorePath extends AbstractTransform {

	UUID pathUUID = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()
			.iterator().next();

	public String transform(String input) throws Exception {
		return setLastTransform(pathUUID.toString());
	}

	@Override
	public void setupImpl(Transform transformer) throws IOException,
			ClassNotFoundException {
		 // nothing to do...
	}
}
