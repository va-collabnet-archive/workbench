package org.ihtsdo.arena.spec;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class Refsets {
	
	public static ConceptSpec TEST = 
		new ConceptSpec("Terminology Auxiliary concept", 
						UUID.fromString("f4d2fabc-7e96-3b3a-a348-ae867ba74029"));
	
	public static ConceptSpec TEST2 = 
		new ConceptSpec("Refset Auxiliary Concept", 
						UUID.fromString("1c698388-c309-3dfa-96f0-86248753fac5"));
	
	public static ConceptSpec EN_GB_LANG = 
		new ConceptSpec("GB English Dialect Subset", 
						UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd"));
	
	public static ConceptSpec EN_US_LANG = 
		new ConceptSpec("US English Dialect Subset", 
						UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6"));
	
	public static ConceptSpec NON_HUMAN = 
		new ConceptSpec("Non-Human refset", 
						UUID.fromString("3636a34d-cdf0-3f8f-a73e-c1e8f4c8cd2a"));
	
	public static ConceptSpec VMP = 
		new ConceptSpec("VMP refset", 
						UUID.fromString("0fcbf94e-2918-365a-8fb7-c29ed2c2189e"));
	
	public static ConceptSpec VTM = 
		new ConceptSpec("VTM refset", 
						UUID.fromString("e65ea362-72d4-3641-bfba-fe4429eea6f9"));
	/*
	public static ConceptSpec CONCEPT_INACTIVATION = 
		new ConceptSpec("Concept inactivation refset", 
						UUID.fromString("938fcf78-7c92-3bf6-b9d0-12ed3ca96198"));
	
	public static ConceptSpec CTV3ID = 
		new ConceptSpec("CTV3ID refset", 
						UUID.fromString("f96ce9da-5a75-3a80-86c1-934d1afe1268"));
	
	public static ConceptSpec DESC_INACTIVATION = 
		new ConceptSpec("Description inactivation refset", 
						UUID.fromString("e4223d26-8204-3a5d-9d75-ce0c07aecd4c"));
	
	public static ConceptSpec DESC_TYPE = 
		new ConceptSpec("Description type refset", 
						UUID.fromString("73ea5fc2-7840-3f53-945f-5f26473b2596"));
	
	public static ConceptSpec HISTORICAL_ASSOC = 
		new ConceptSpec("Historical association refset", 
						UUID.fromString("4cbb35a9-72fb-3708-bb0a-56e217685f70"));
	
	public static ConceptSpec ICD_03 = 
		new ConceptSpec("ICD-O3 Refset", 
						UUID.fromString("1e7bb5d9-0a96-325e-9507-23b5fd9f8014"));
	
	public static ConceptSpec MODULE_DEPENDENCY = 
		new ConceptSpec("Module dependency refset", 
						UUID.fromString("517a39da-81c1-3075-9908-8eba85a19a28"));
	
	public static ConceptSpec REFERENCES = 
		new ConceptSpec("References refset", 
						UUID.fromString("d87d067b-de43-3673-a923-89ced99d9cfe"));
	
	public static ConceptSpec REFINABILITY = 
		new ConceptSpec("Refinability refset", 
						UUID.fromString("6148ab7c-476a-32d9-9b3c-ef7684e60936"));
	
	public static ConceptSpec REFSET_DESCRIPTOR = 
		new ConceptSpec("Refset descriptor refset", 
						UUID.fromString("d76abe74-ccae-332a-9ea5-17d8e8bf9401"));
	
	public static ConceptSpec SNOMEDID = 
		new ConceptSpec("SNOMEDID refset", 
						UUID.fromString("be81d0ca-bc50-31a8-a12a-b602957180e0"));*/
	
}
