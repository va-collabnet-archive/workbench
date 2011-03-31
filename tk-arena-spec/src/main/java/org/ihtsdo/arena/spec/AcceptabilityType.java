package org.ihtsdo.arena.spec;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class AcceptabilityType {
	
	public static ConceptSpec PREF = 
		new ConceptSpec("preferred acceptability", 
						UUID.fromString("15877c09-60d7-3464-bed8-635a98a7e5b2"));
	
	public static ConceptSpec ACCEPTABLE = 
		new ConceptSpec("acceptable", 
						UUID.fromString("51b45763-09c4-34eb-a303-062ba8e0c0e9"));
	
	public static ConceptSpec NOT_ACCEPTABLE = 
		new ConceptSpec("not acceptable", 
						UUID.fromString("87ee1cd0-59d4-3943-8d69-b9eefd062ce6"));
	
	
	
}
