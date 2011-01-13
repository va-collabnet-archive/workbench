package org.ihtsdo.arena.spec;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class Refsets {
	
	public static ConceptSpec TEST = 
		new ConceptSpec("Refset Auxiliary Concept", 
						UUID.fromString("1c698388-c309-3dfa-96f0-86248753fac5"));
	
	public static ConceptSpec TEST2 = 
		new ConceptSpec("Terminology Auxiliary concept", 
						UUID.fromString("f4d2fabc-7e96-3b3a-a348-ae867ba74029"));
}
