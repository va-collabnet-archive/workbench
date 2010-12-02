package org.ihtsdo.tk.example.binding;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class Snomed {
	public static ConceptSpec BODY_STRUCTURE = 
		new ConceptSpec("Body structures", 
						UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790"));

	public static ConceptSpec IS_A = 
		new ConceptSpec("Is a (attribute)", 
						UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));

	public static ConceptSpec FINDING_SITE = 
		new ConceptSpec("Finding site (attribute)", 
						UUID.fromString("3a6d919d-6c25-3aae-9bc3-983ead83a928"));

	public static ConceptSpec CLINICAL_FINDING = 
		new ConceptSpec("Clinical finding (finding)", 
						UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c"));
	
	public static ConceptSpec FULLY_SPECIFIED_DESCRIPTION_TYPE = 
		new ConceptSpec("fully specified name (description type)", 
						UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66"));

}
