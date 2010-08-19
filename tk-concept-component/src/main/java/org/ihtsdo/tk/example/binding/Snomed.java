package org.ihtsdo.tk.example.binding;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class Snomed {
	public static ConceptSpec BODY_STRUCTURE = 
		new ConceptSpec("Body structures", 
						UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790"));

}
