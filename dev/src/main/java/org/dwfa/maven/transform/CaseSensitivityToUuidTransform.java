package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

/**
 * Converts a initial capitalisation status from the original SNOMED definition to
 * the UUID of the concept enumeration equivalent
 * 
 * @author Dion McMurtrie
 *
 */
public class CaseSensitivityToUuidTransform extends AbstractTransform implements
		I_ReadAndTransform {

	public void setupImpl(Transform transformer) {

	}

	public String transform(String input) throws Exception {
		
		ArchitectonicAuxiliary.Concept concept;

		if (input.equals("0")) {
			concept = Concept.INITIAL_CHARACTER_NOT_CASE_SENSITIVE;
		} else if (input.equals("1")) {
			concept = Concept.ALL_CHARACTERS_CASE_SENSITIVE;
		} else {
			throw new Exception("Failed converting input " + input + " not one of the expected values 0 and 1");
		}
		
		UUID uuid = concept.getUids().iterator().next();

		return setLastTransform(uuid.toString());
	}
}