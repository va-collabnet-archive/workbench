package org.dwfa.ace.refset;

import org.dwfa.tapi.spec.ConceptSpec;

/**
 * Concept constants file - used to statically reference required concepts from the terminology
 * 
 * @author Dion McMurtrie
 */
public class ConceptConstants {

	public static final ConceptSpec GENERATES_REL = new ConceptSpec("generates", "ca77a82c-ffa8-4dc1-8d31-6b137b4607ca");
	public static final ConceptSpec CREATES_MEMBERSHIP_TYPE = new ConceptSpec("creates membership type", "adccc8cc-cfe0-4947-b2b2-2c493034ac3a");
	public static final ConceptSpec SNOMED_IS_A = new ConceptSpec("Is a (attribute)", "c93a30b9-ba77-3adb-a9b8-4589c9f8fb25");
	public static final ConceptSpec PARENT_MARKER = new ConceptSpec("marked parent member", "818297c2-34b7-4cc1-81d0-c003c8fe9477");
}
