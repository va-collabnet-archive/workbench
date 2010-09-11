package org.ihtsdo.tk.example.binding;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

public class TermAux {

	public static ConceptSpec IS_A = 
		new ConceptSpec("is a (relationship type)", 
						UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));

}
