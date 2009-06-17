package org.dwfa.ace.refset;

import org.dwfa.tapi.spec.ConceptSpec;

/**
 * Concept constants file - used to statically reference required concepts from the terminology
 * 
 * @author Dion McMurtrie
 */
public class ConceptConstants {

	public static final ConceptSpec AU_CT_EDIT_PATH = new ConceptSpec("au-ct edit path", "c65b08ce-8512-52fa-be06-0844bd7310d6");
	public static final ConceptSpec GENERATES_REL = new ConceptSpec("generates", "ca77a82c-ffa8-4dc1-8d31-6b137b4607ca");
	public static final ConceptSpec CREATES_MEMBERSHIP_TYPE = new ConceptSpec("creates membership type", "adccc8cc-cfe0-4947-b2b2-2c493034ac3a");
	public static final ConceptSpec SNOMED_IS_A = new ConceptSpec("Is a (attribute)", "c93a30b9-ba77-3adb-a9b8-4589c9f8fb25");
	public static final ConceptSpec PARENT_MARKER = new ConceptSpec("marked parent member", "125f3d04-de17-490e-afec-1431c2a39e29");
	public static final ConceptSpec REFSET_TYPE_REL = new ConceptSpec("refset type rel","f353db14-3b0d-3001-9f27-7d8b00f51a1c"); 
	public static final ConceptSpec REFSET_PURPOSE_REL = new ConceptSpec("refest purpose rel","f60922c9-cb3d-3099-8960-1097d2c5afdc");
	public static final ConceptSpec INCLUSION_TYPE = new ConceptSpec("inclusion specification type", "566c380d-a9ac-318e-9e96-9df2fd405a53");
	public static final ConceptSpec REFSET = new ConceptSpec("refset identity", "3e0cd740-2cc6-3d68-ace7-bad2eb2621da");
	public static final ConceptSpec REFSET_MEMBER_PURPOSE = new ConceptSpec("refset membership", "a4f5319e-7127-48a7-8b87-075c758f7abd");
	
	public static final ConceptSpec BOOLEAN_EXT = new ConceptSpec("boolean extension by reference", "893b86c9-1f2d-395c-a184-f10358c37856");
	public static final ConceptSpec CONCEPT_EXT = new ConceptSpec("concept extension by reference", "d815700e-dd66-3f91-8f05-99c60b995eb4");
	public static final ConceptSpec CON_INT_EXT = new ConceptSpec("concept int extension by reference", "1c006a8b-48ab-3120-9089-ffb2e6c06e0c");
	public static final ConceptSpec CROSS_MAP_EXT = new ConceptSpec("cross map extension", "93998788-57ad-3b35-9c3a-3e96de2293fe");
	public static final ConceptSpec CROSS_MAP_REL_EXT = new ConceptSpec("cross map relationship extenstion", "9d00f2d2-3f05-3171-a8cf-74c4e2fd5eb1");
	public static final ConceptSpec INT_EXT = new ConceptSpec("int extension by reference", "bf91e36c-ff77-35cf-ad92-890518d0f5f2");
	public static final ConceptSpec LANGUAGE_EXT = new ConceptSpec("language extension by reference", "ea92b438-c52e-37dc-beae-8724a036a0e0");
	public static final ConceptSpec MEASUREMENT_EXT = new ConceptSpec("measurement extension by reference", "a03ac733-8959-354f-87d7-011f6761f638");
	public static final ConceptSpec SCOPED_LANG_EXT = new ConceptSpec("scoped language extension by reference", "facf7fc4-8eb6-3d11-bd66-842baaa1c8ab");
	public static final ConceptSpec STRING_EXT = new ConceptSpec("string extension by reference", "4a5d2768-e2ae-3bc1-be2d-8d733cd4abdb");
	public static final ConceptSpec TEMPLATE_EXT = new ConceptSpec("template extension", "92b3f835-6cc7-3163-aea0-d4a388ddc5ed");
	public static final ConceptSpec TEMPLATE_FOR_REL_EXT = new ConceptSpec("template relationship extension", "344f3d87-8f28-315a-a0e5-4d057bf050ae");

	public static final ConceptSpec PATH_VERSION_REFSET = new ConceptSpec("Path version reference set", "999d61ec-ba1e-48eb-b759-62ffeefbf4b1");

	public static final ConceptSpec EXCLUDE_MEMBERS_REL_TYPE = new ConceptSpec("exclude members", "4fdc7569-ff5a-49ec-bcfb-cc41308abcc0");
	
	public static final ConceptSpec INCLUDES_MARKED_PARENTS_REL_TYPE = new ConceptSpec("includes marked parents", "6e2518c8-13f0-4766-896f-83fc95d4cd0e");
	
}
