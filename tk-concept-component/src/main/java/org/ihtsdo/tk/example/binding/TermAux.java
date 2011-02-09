package org.ihtsdo.tk.example.binding;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class TermAux {

	public static ConceptSpec IS_A = 
		new ConceptSpec("is a (relationship type)", 
						UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));

	public static ConceptSpec CURRENT = 
		new ConceptSpec("current (active status type)", 
						UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66"));

	public static ConceptSpec RETIRED = 
		new ConceptSpec("retired (inactive status type)", 
						UUID.fromString("e1956e7b-08b4-3ad0-ab02-b411869f1c09"));
	
	public static ConceptSpec INACTIVE_STATUS = 
		new ConceptSpec("inactive (inactive status type)", 
						UUID.fromString("1464ec56-7118-3051-9d21-0f95c1a39080"));
	
	public static ConceptSpec MOVED_TO = 
		new ConceptSpec("moved elsewhere (inactive status type)", 
						UUID.fromString("76367831-522f-3250-83a4-8609ab298436"));
	
}
