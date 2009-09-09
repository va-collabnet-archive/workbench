package org.dwfa.mojo;

import org.dwfa.tapi.spec.ConceptSpec;

/**
 * Concept constants file - used to statically reference required concepts from the terminology
 *
 * @author Dion McMurtrie
 */
public class ConceptConstants {

    public static final ConceptSpec GENERATES_REL =
            new ConceptSpec("generates", "ca77a82c-ffa8-4dc1-8d31-6b137b4607ca");
    public static final ConceptSpec CREATES_MEMBERSHIP_TYPE =
            new ConceptSpec("creates membership type", "adccc8cc-cfe0-4947-b2b2-2c493034ac3a");
    public static final ConceptSpec SNOMED_IS_A =
            new ConceptSpec("Is a (attribute)", "c93a30b9-ba77-3adb-a9b8-4589c9f8fb25");
    public static final ConceptSpec RELATIONSHIP_REFINABILITY_EXTENSION =
            new ConceptSpec("Relationship refinability reference set", "a69353f3-fcb2-488f-ad00-84152672ef49");
    public static final ConceptSpec STATUS_REASON_EXTENSION =
            new ConceptSpec("Status reason reference set", "e61d012e-0a7d-4991-a81c-aafa287aedec");
    public static final ConceptSpec DEFINITION_TYPE_EXTENSION =
            new ConceptSpec("Definition status reference set", "6e88ee62-3fa0-495f-945f-809723a78c4b");
    public static final ConceptSpec SNOMED_ID_MAP_EXTENSION =
            new ConceptSpec("snomed id map reference set", "RF2 - meta-data");
    public static final ConceptSpec CTV3_ID_MAP_EXTENSION =
            new ConceptSpec("CTV3 id map reference set", "RF2 - meta-data");

}
