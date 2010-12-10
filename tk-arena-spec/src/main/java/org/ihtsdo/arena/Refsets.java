package org.ihtsdo.arena.spec;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class Refsets {
	
	public static ConceptSpec TEST = 
		new ConceptSpec("test refset", 
						UUID.fromString("ecccdc40-9fe3-4d4a-902f-2a921ec07310"));
	
	public static ConceptSpec TEST2 = 
		new ConceptSpec("test2 refset", 
						UUID.fromString("73e4daa4-16ff-4734-92ec-929002585680"));
}
