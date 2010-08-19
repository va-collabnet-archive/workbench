package org.ihtsdo.tk.spec;

import java.util.UUID;

public class WbDescType {

	public static ConceptSpec FULLY_SPECIFIED = 
		new ConceptSpec("fully specified name", 
				UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66"));

	public static ConceptSpec PREFERRED = 
		new ConceptSpec("preferred term", 
				UUID.fromString("d8e3b37d-7c11-33ef-b1d0-8769e2264d44"));

}
