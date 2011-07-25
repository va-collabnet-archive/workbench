package org.ihtsdo.rf2.constant;

/**
 * Title: I_Constants Description: Declared all the constants used by export routines Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class I_Constants {

	public final static String CORE_MODULE_ID = "900000000000207008";
	public final static String META_MOULE_ID = "900000000000012004";
	public final static String META_SCTID = "900000000000441003";

	// set definitionStatus Id
	public final static String FULLY_DEFINED = "900000000000073002";
	public final static String PRIMITIVE = "900000000000074008";

	// set caseSignificanceId
	public final static String INSENSITIVE_CASE = "900000000000448009";
	public final static String SENSITIVE_CASE = "900000000000017005";
	public final static String INITIAL_INSENSITIVE = "900000000000020002";

	// set description type Id
	public final static String FSN = "900000000000003001";
	public final static String SYN = "900000000000013009";
	public final static String DEFINITION = "900000000000550004";

	// set up characteristic type ID
	public final static String ADDITIONALRELATION = "900000000000227009";
	public final static String DEFININGRELATION = "900000000000006009";
	public final static String QUALIFYRELATION = "900000000000225001";
	public final static String STATED = "900000000000010007"; //special change
	public final static String INFERRED = "900000000000011006"; //special change
	public final static String HISTORICAL = "HISTORICAL";
	// set up refinability
	
	public final static String NON_REFINABLE = "900000000000007000";
	public final static String OPTIONAL_REFINABLE = "900000000000216007";
	public final static String MANTOTARY = "900000000000218008";

	// if (status==retId) valueId="Don'tknow value"; //1 Retired without reason
	// (foundation metadata concept)
	public final static String DUPLICATE = "900000000000482003";
	public final static String OUTDATED = "900000000000483008";
	public final static String ERRONEOUS = "900000000000485001";
	public final static String LIMITED = "900000000000486000";
	public final static String INAPPROPRIATE = "900000000000494007";
	public final static String CONCEPT_NON_CURRENT = "900000000000495008";
	public final static String MOVED_ELSE_WHERE = "900000000000487009";
	public final static String PENDING_MOVE = "900000000000492006";
	public final static String AMBIGUOUS = "900000000000484002";
	

	public final static String DUPLICATE_CONCEPT = "363662004";
	public final static String OUTDATED_CONCEPT  = "363663009";
	public final static String ERRONEOUS_CONCEPT  = "363664003";
	public final static String LIMITED_CONCEPT  = "443559000";
	public final static String REASON_NOT_STATED_CONCEPT  = "363661006";
	public final static String MOVED_ELSEWHERE_CONCEPT  = "370126003";
	public final static String AMBIGUOUS_CONCEPT  = "363660007";
	// Set is a sctid
	public final static String ISA = "116680003";
	public final static String IS_A_UID = "c93a30b9-ba77-3adb-a9b8-4589c9f8fb25";
	
	// set historical sctid
	public final static String MAY_BE = "149016008";
	public final static String WAS_A = "159083000";
	public final static String SAME_AS = "168666000";
	public final static String REPLACED_BY = "370124000";
	public final static String MOVED_FROM = "384598002";
	public final static String MOVED_TO = "370125004";

	// set up default modifierId
	public final static String SOMEMODIFIER = "900000000000451002";

	// set out mode: true = yes; false = all data; comment out inappropriate one
	public final static boolean rf2Format = true; // true

	/** The read only. */
	public static boolean readOnly = false;

	/** The cache size. */
	public static Long cacheSize = Long.getLong("600000000");

	// private static String outRelPath ="CORE_STATED";
	// private static String outRelPath ="CORE_INFERRED";

	// set output relationship path
	public static String outRelPath = "ALL";
	public static String limited_policy_change = "20100131";

	public static String TimeFormat = "yyyyMMdd"; // 20100305 Per SC no TZ only YMD

	// set Snomed Refset sctid
	public final static String SNOMED_REFSET_ID = "900000000000498005";
	public final static String SNOMED_REFSET_UID = "675f8dbb-9c76-3cd2-a567-83aab81ee4da";

	public final static String CTV3_REFSET_ID = "900000000000497000";
	public final static String CTV3_REFSET_UID = "c134524d-aa26-3444-b316-f0d29c487d57";

	public final static String SIMPLE_MAP_REFSET_ACTIVE = "1";

	public final static String CONCEPT_INACTIVATION_REFSET_ID = "900000000000489007";
	public final static String CONCEPT_INACTIVATION_REFSET_UID = "a959e36c-39a3-3ec6-8a0b-9a4ee564348b";

	public final static String DESCRIPTION_INACTIVATION_REFSET_ID = "900000000000490003";
	public final static String DESCRIPTION_INACTIVATION_REFSET_UID = "d40e6d5c-6a93-34ab-b6e1-11f24e350380";

	public final static String REFINIBILITY_REFSET_ID = "900000000000488004";
	public final static String REFINIBILITY_REFSET_UID = "b0a14b91-038f-31e6-a556-38a1714d3667";

	public final static String MAY_BE_REFSET_ID = "900000000000523009";
	public final static String MAY_BE_REFSET_UID = "2cb0d803-0aa8-3c4a-a58c-949fa85a5016";

	public final static String MOVED_FROM_REFSET_ID = "900000000000525002";
	public final static String MOVED_FROM_REFSET_UID = "34a2171a-af64-3ab2-b27d-e19914f058e3";

	public final static String MOVED_TO_REFSET_ID = "900000000000524003";
	public final static String MOVED_TO_REFSET_UID = "53e5f8b2-2f4b-3268-8a5e-04f30c241c89";

	public final static String REPLACED_BY_REFSET_ID = "900000000000526001";
	public final static String REPLACED_BY_REFSET_UID = "9cb37094-2be5-3b05-a586-73ae6727e9c2";

	public final static String SAME_AS_REFSET_ID = "900000000000527005";
	public final static String SAME_AS_REFSET_UID = "58d18fcb-02fd-3c2d-82e6-0ff2593d9df4";

	public final static String WAS_A_REFSET_ID = "900000000000528000";
	public final static String WAS_A_REFSET_UID = "0c4a8933-bf14-39b9-a743-a5dc07faa7bb";

	public final static String SNOMED_CORE_PATH_UID = "8c230474-9f11-30ce-9cad-185a96fd03a2";
	public final static String SNOMED_META_PATH_UID = "a60bd881-9010-3260-9653-0c85716b4391";
	public final static String SNOMED_INFERRED_PATH_UID = "5e51196f-903e-5dd4-8b3e-658f7e0a4fe6";
	public final static String SNOMED_STATED_PATH_UID = "8a2d3b5e-56eb-58e1-8050-f80810306a6c";
		
	public final static String SNOROCKET_AUTHOR_UID = "7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9";
	public final static String USER_AUTHOR_UID = "f7495b58-6630-3499-a44e-2052b5fcf06c";

	public final static String US_LANG_REFSET_ID = "900000000000509007";
	public final static String US_LANG_REFSET_UID = "bca0a686-3516-3daf-8fcf-fe396d13cfad";
	public final static String USLANG_REFSET_UID_TERM_AUX = "29bf812c-7a77-595d-8b12-ea37c473a5e6";
	
	public final static String GB_LANG_REFSET_ID = "900000000000508004";
	public final static String GB_LANG_REFSET_UID = "eb9a5e42-3cba-356d-b623-3ed472e20b30";
	public final static String GBLANG_REFSET_UID_TERM_AUX = "a0982f18-ec51-56d2-a8b1-6ff8964813dd";
	
	
	public final static String NON_HUMAN_REFSET_ID = "447564002";
	public final static String NON_HUMAN_REFSET_UID = "???????????????????";
	public final static String NON_HUMAN_REFSET_UID_TERM_AUX = "0e2687b7-db28-5a01-b968-b98865648f2b";
	

	public final static String VTM_REFSET_ID = "447565001";
	public final static String VTM_REFSET_UID = "???????????????";
	public final static String VTM_REFSET_UID_TERM_AUX = "1a090a21-28c4-3a87-9d04-766f04600494";
	
	public final static String VMP_REFSET_ID = "447566000";
	public final static String VMP_REFSET_UID = "??????????????????";
	public final static String VMP_REFSET_UID_TERM_AUX = "c259d808-8011-3772-bece-b4fbde18d375";
	
	public final static String ICDO_REFSET_ID = "446608001";
	public final static String ICDO_REFSET_UID = "??????????????????";
	public final static String ICDO_REFSET_UID_TERM_AUX = "5ef10e09-8f16-398e-99b5-55cff5bd820a";
															
	public final static String MODULE_DEPENDENCY_REFSET_ID = "900000000000534007";
	
	// set acceptabilityId language refset
	public final static String PREFERRED = "900000000000548007";
	public final static String ACCEPTABLE = "900000000000549004";
	
	// set references refset ids
	public final static String ALTERNATIVE_REFERENCES_REFSET_ID = "900000000000530003";
	public final static String ALTERNATIVE_REFERENCES_REFSET_UID = "e116d83a-4402-3f4e-9628-6edafcd98ed7";
	public final static String ALTERNATIVE_REFERENCES_REFSET_UID_TERM_AUX = "e0674a75-74b3-5a6b-8db4-7c464f7aeaa0";
	
	public final static String REFERS_REFERENCES_REFSET_ID = "900000000000531004";
	public final static String REFERS_REFERENCES_REFSET_UID = "d15fde65-ed52-3a73-926b-8981e9743ee9";
	public final static String REFERS_REFERENCES_REFSET_UID_TERM_AUX = "1b122b8f-172f-53d5-a2e2-eb1161737c2a";
		
	public final static String REPLACED_REFERENCES_REFSET_ID = "900000000000526001";
	public final static String REPLACED_REFERENCES_REFSET_UID = "9cb37094-2be5-3b05-a586-73ae6727e9c2";
	public final static String REPLACED_REFERENCES_REFSET_UID_TERM_AUX = "32429976-257c-5013-82ee-599404b55cc6";
	
	public final static String DUPLICATE_REFERENCES_REFSET_ID = "900000000000527005"; //900000000000527005 , 900000000000523009
	public final static String DUPLICATE_REFERENCES_REFSET_UID = "58d18fcb-02fd-3c2d-82e6-0ff2593d9df4";
	public final static String DUPLICATE_REFERENCES_REFSET_UID_TERM_AUX = "3bb41291-ade6-5dcc-bca4-ec511722b4e3";
	
	public final static String POSSIBLY_EQUIVALENT_REFERENCES_REFSET_ID = "900000000000523009";
	public final static String POSSIBLY_EQUIVALENT_REFERENCES_REFSET_UID = "2cb0d803-0aa8-3c4a-a58c-949fa85a5016";
	public final static String POSSIBLY_EQUIVALENT_REFERENCES_REFSET_UID_TERM_AUX = "????";
	
	public final static String MOVED_FROM_REFERENCES_REFSET_ID = "900000000000525002";
	public final static String MOVED_FROM_REFERENCES_REFSET_UID = "34a2171a-af64-3ab2-b27d-e19914f058e3";
	public final static String MOVED_FROM_REFERENCES_REFSET_UID_TERM_AUX = "bb787b5b-ff66-578c-a40f-39122d3538d6";
	
	public final static String MOVED_TO_REFERENCES_REFSET_ID = "900000000000524003";
	public final static String MOVED_TO_REFERENCES_REFSET_UID = "53e5f8b2-2f4b-3268-8a5e-04f30c241c89";
	public final static String MOVED_TO_REFERENCES_REFSET_UID_TERM_AUX = "1b4857c9-ab2b-58b1-b477-2edaf7188c85";
	
	public final static String SIMILAR_REFERENCES_REFSET_ID = "900000000000529008";
	public final static String SIMILAR_REFERENCES_REFSET_UID = "ac70d14f-dd81-312a-b123-559d43bb44d6";
	public final static String SIMILAR_REFERENCES_REFSET_UID_TERM_AUX = "d4e52f40-7fd9-5b3f-b45b-fb9f414d0290";
	
	public final static String WAS_A_REFERENCES_REFSET_ID = "900000000000528000";
	public final static String WAS_A_REFERENCES_REFSET_UID = "0c4a8933-bf14-39b9-a743-a5dc07faa7bb";
	public final static String WAS_A_REFERENCES_REFSET_UID_TERM_AUX = "????";
	
	
	
	
	
	
	
	

}
