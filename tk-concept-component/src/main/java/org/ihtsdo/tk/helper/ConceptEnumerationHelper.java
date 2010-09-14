package org.ihtsdo.tk.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class ConceptEnumerationHelper {
	private List<ConceptSpec> concepts;
	private List<String> enumeration;

	public ConceptEnumerationHelper() {
		super();
		fillConcepts();
	}

	private void fillConcepts() {
		concepts = new ArrayList<ConceptSpec>();
		enumeration = new ArrayList<String>();
		concepts.add(new ConceptSpec("Terminology Auxiliary concept", 
				UUID.fromString("f4d2fabc-7e96-3b3a-a348-ae867ba74029")));
		concepts.add(new ConceptSpec("Refset Auxiliary Concept", 
				UUID.fromString("1c698388-c309-3dfa-96f0-86248753fac5")));
		concepts.add(new ConceptSpec("SNOMED CT Concept", 
		         UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")));
		concepts.add(new ConceptSpec("Body structures", 
				UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")));
		concepts.add(new ConceptSpec("Is a (attribute)", 
				UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
		concepts.add(new ConceptSpec("Finding site (attribute)", 
				UUID.fromString("3a6d919d-6c25-3aae-9bc3-983ead83a928")));
		concepts.add(new ConceptSpec("Clinical finding (finding)", 
				UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c")));
		
		for (ConceptSpec loopSpec : concepts) {
			try {
				enumeration.add(loopSpec.getDescription() + "=" + loopSpec.getNid());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Collections.sort(enumeration);
		
	}
	
	public List<String> getEnumerationOptions() {
		return enumeration;
	}
	

}
