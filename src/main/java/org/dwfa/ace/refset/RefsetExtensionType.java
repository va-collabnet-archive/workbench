package org.dwfa.ace.refset;

import org.dwfa.cement.RefsetAuxiliary.Concept;

/**
 * Extends {@link org.dwfa.cement.RefsetAuxiliary.Concept} to define additional properties. 
 *
 */
public enum RefsetExtensionType {

	BOOLEAN_EXTENSION(
			Concept.BOOLEAN_EXTENSION, 
			".boolean.refset"),
			
	CONCEPT_EXTENSION(
			Concept.CONCEPT_EXTENSION, 
			".concept.refset"),
	
	CONCEPT_INT_EXTENSION(
			Concept.CONCEPT_INT_EXTENSION, 
			".conint.refset"),
	
	STRING_EXTENSION(
			Concept.STRING_EXTENSION, 
			".string.refset"),
	
	INT_EXTENSION(
			Concept.INT_EXTENSION, 
			".integer.refset"),
	
	MEASUREMENT_EXTENSION(
			Concept.MEASUREMENT_EXTENSION, 
			".measurement.refset"),
	
	LANGUAGE_EXTENSION(
			Concept.LANGUAGE_EXTENSION, 
			".language.refset");
	
	
	private String fileExtension;
	
	private Concept auxiliaryConcept;
	
	
	private RefsetExtensionType(Concept auxiliaryConcept, String fileExtension) {
		this.auxiliaryConcept = auxiliaryConcept;
		this.fileExtension = fileExtension;
	}
	
	private boolean matches(String filename) {
		return (filename.toLowerCase().endsWith(this.fileExtension));
	}

	public Concept getAuxiliaryConcept() {
		return this.auxiliaryConcept;
	}
	
	/**
	 * @param filename 
	 * @return The appropriate refset type based on the file name. Returns null if cannot be matched.
	 */
	public static RefsetExtensionType findByFilename(String filename) {
		for (RefsetExtensionType t : RefsetExtensionType.values()) {
			if (t.matches(filename)) {
				return t;
			}
		} 
		return null;
	}
}
