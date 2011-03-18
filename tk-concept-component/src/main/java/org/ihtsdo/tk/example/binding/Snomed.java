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
	
	public static ConceptSpec CORE = 
		new ConceptSpec("Core Namespace",
						UUID.fromString("d6bbe207-7b5c-3e32-a2a1-f9259a7260c1"));
		
	public static ConceptSpec EXTENSION_0= 
		new ConceptSpec("Extension Namespace 1000000",
						UUID.fromString("18388bfd-9fab-3581-9e22-cbae53725ef2"));
	
	public static ConceptSpec EXTENSION_13 = 
		new ConceptSpec("Extension Namespace 1000013",
						UUID.fromString("bb57db0f-def7-3fb7-b7f2-89fa7710bffa"));
	
	public static ConceptSpec CONCEPT_HISTORY_ATTRIB = 
		new ConceptSpec("Concept history attribute",
						UUID.fromString("f323b5dd-1f97-3873-bcbc-3563663dda14"));
	
	public static ConceptSpec PRODUCT = 
		new ConceptSpec("Pharmaceutical / biologic product (product)",
						UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449"));

}
