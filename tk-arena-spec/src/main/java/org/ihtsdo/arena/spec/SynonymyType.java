package org.ihtsdo.arena.spec;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class SynonymyType {
	
	public static ConceptSpec SYNONYM = 
		new ConceptSpec("synonymous", 
						UUID.fromString("fdc8e1fe-0281-3fd1-b932-8ae13d88f361"));
	
	public static ConceptSpec NEAR_SYNONYMOUS = 
		new ConceptSpec("near synonymous", 
						UUID.fromString("15e776d8-874c-3ae1-ad45-4c37d80627a8"));
	
	public static ConceptSpec NON_SYNONYMOUS = 
		new ConceptSpec("non synonymous", 
						UUID.fromString("1fc9f6d0-bcf4-354e-a5ee-237ec91de697"));
	
	
	
}
