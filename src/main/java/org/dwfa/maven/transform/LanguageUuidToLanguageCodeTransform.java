package org.dwfa.maven.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

/**
 * Transform that given the name of a language meta data concept returns the language
 * code for that concept.
 * 
 * @author Dion McMurtrie
 */
public class LanguageUuidToLanguageCodeTransform extends AbstractTransform implements
		I_ReadAndTransform {

	public void setupImpl(Transform transformer) {

	}

	public String transform(String input) throws Exception {
		Collection<UUID> uuids = new ArrayList<UUID>();
		
		uuids.add(UUID.fromString(input));
		
		return setLastTransform(ArchitectonicAuxiliary.getLanguageCode(uuids));
	}
}