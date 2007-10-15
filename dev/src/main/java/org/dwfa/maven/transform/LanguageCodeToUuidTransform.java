package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public class LanguageCodeToUuidTransform extends AbstractTransform implements
		I_ReadAndTransform {

	public void setupImpl(Transform transformer) {

	}

	public String transform(String input) throws Exception {
		String languageCode = input.toUpperCase();
		languageCode.replace('-', '_');
		
		ArchitectonicAuxiliary.Concept concept = Enum.valueOf(ArchitectonicAuxiliary.Concept.class, languageCode);

		UUID uuid = concept.getUids().iterator().next();

		return setLastTransform(uuid.toString());
	}
}